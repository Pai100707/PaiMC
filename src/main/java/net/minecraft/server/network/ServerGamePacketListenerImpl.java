package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSigningContext.SignedArguments;
import net.minecraft.commands.arguments.ArgumentSignatures.Entry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.HashedStack;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenMessagesValidator;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSignatureCache;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.chat.LastSeenMessages.Update;
import net.minecraft.network.chat.LastSeenMessagesValidator.ValidationException;
import net.minecraft.network.chat.RemoteChatSession.Data;
import net.minecraft.network.chat.SignableCommand.Argument;
import net.minecraft.network.chat.SignedMessageChain.DecodeException;
import net.minecraft.network.chat.SignedMessageChain.Decoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTestInstanceBlockStatus;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.network.protocol.game.ServerboundChatAckPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatSessionUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundConfigurationAcknowledgedPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundDebugSubscriptionRequestPacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.network.protocol.game.GameProtocols.Context;
import net.minecraft.network.protocol.game.ServerboundInteractPacket.Handler;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket.Action;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.util.TickThrottler;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookMenu.PostPlaceAction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager.ServerDisplayInfo;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity.Mode;
import net.minecraft.world.level.block.entity.StructureBlockEntity.UpdateType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerGamePacketListenerImpl
   extends ServerCommonPacketListenerImpl
   implements Context,
   ServerGamePacketListener,
   ServerPlayerConnection,
   TickablePacketListener {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int NO_BLOCK_UPDATES_TO_ACK = -1;
   private static final int TRACKED_MESSAGE_DISCONNECT_THRESHOLD = 4096;
   private static final int MAXIMUM_FLYING_TICKS = 80;
   private static final int ATTACK_INDICATOR_TOLERANCE_TICKS = 5;
   public static final int CLIENT_LOADED_TIMEOUT_TIME = 60;
   private static final Component CHAT_VALIDATION_FAILED = Component.translatable("multiplayer.disconnect.chat_validation_failed");
   private static final Component INVALID_COMMAND_SIGNATURE = Component.translatable("chat.disabled.invalid_command_signature").withStyle(ChatFormatting.RED);
   private static final int MAX_COMMAND_SUGGESTIONS = 1000;
   public ServerPlayer player;
   public final PlayerChunkSender chunkSender;
   private int tickCount;
   private int ackBlockChangesUpTo = -1;
   private final TickThrottler chatSpamThrottler = new TickThrottler(20, 200);
   private final TickThrottler dropSpamThrottler = new TickThrottler(20, 1480);
   private double firstGoodX;
   private double firstGoodY;
   private double firstGoodZ;
   private double lastGoodX;
   private double lastGoodY;
   private double lastGoodZ;
   @Nullable
   private Entity lastVehicle;
   private double vehicleFirstGoodX;
   private double vehicleFirstGoodY;
   private double vehicleFirstGoodZ;
   private double vehicleLastGoodX;
   private double vehicleLastGoodY;
   private double vehicleLastGoodZ;
   @Nullable
   private Vec3 awaitingPositionFromClient;
   private int awaitingTeleport;
   private int awaitingTeleportTime;
   private boolean clientIsFloating;
   private int aboveGroundTickCount;
   private boolean clientVehicleIsFloating;
   private int aboveGroundVehicleTickCount;
   private int receivedMovePacketCount;
   private int knownMovePacketCount;
   private boolean receivedMovementThisTick;
   @Nullable
   private RemoteChatSession chatSession;
   private Decoder signedMessageDecoder;
   private final LastSeenMessagesValidator lastSeenMessages = new LastSeenMessagesValidator(20);
   private int nextChatIndex;
   private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
   private final FutureChain chatMessageChain;
   private boolean waitingForSwitchToConfig;
   private boolean waitingForRespawn;
   private int clientLoadedTimeoutTimer;

   public ServerGamePacketListenerImpl(net.minecraft.server.MinecraftServer $$0, Connection $$1, ServerPlayer $$2, CommonListenerCookie $$3) {
      super($$0, $$1, $$3);
      this.restartClientLoadTimerAfterRespawn();
      this.chunkSender = new PlayerChunkSender($$1.isMemoryConnection());
      this.player = $$2;
      $$2.connection = this;
      $$2.getTextFilter().join();
      this.signedMessageDecoder = Decoder.unsigned($$2.getUUID(), $$0::enforceSecureProfile);
      this.chatMessageChain = new FutureChain($$0);
   }

   public void tick() {
      if (this.ackBlockChangesUpTo > -1) {
         this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
         this.ackBlockChangesUpTo = -1;
      }

      if (this.server.isPaused() || !this.tickPlayer()) {
         this.keepConnectionAlive();
         this.chatSpamThrottler.tick();
         this.dropSpamThrottler.tick();
         if (this.player.getLastActionTime() > 0L
            && this.server.playerIdleTimeout() > 0
            && Util.getMillis() - this.player.getLastActionTime() > TimeUnit.MINUTES.toMillis(this.server.playerIdleTimeout())
            && !this.player.wonGame) {
            this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
         }
      }
   }

   private boolean tickPlayer() {
      this.resetPosition();
      this.player.xo = this.player.getX();
      this.player.yo = this.player.getY();
      this.player.zo = this.player.getZ();
      this.player.doTick();
      this.player.absSnapTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
      this.tickCount++;
      this.knownMovePacketCount = this.receivedMovePacketCount;
      if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
         if (++this.aboveGroundTickCount > this.getMaximumFlyingTicks(this.player)) {
            LOGGER.warn("{} was kicked for floating too long!", this.player.getPlainTextName());
            this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
            return true;
         }
      } else {
         this.clientIsFloating = false;
         this.aboveGroundTickCount = 0;
      }

      this.lastVehicle = this.player.getRootVehicle();
      if (this.lastVehicle != this.player && this.lastVehicle.getControllingPassenger() == this.player) {
         this.vehicleFirstGoodX = this.lastVehicle.getX();
         this.vehicleFirstGoodY = this.lastVehicle.getY();
         this.vehicleFirstGoodZ = this.lastVehicle.getZ();
         this.vehicleLastGoodX = this.lastVehicle.getX();
         this.vehicleLastGoodY = this.lastVehicle.getY();
         this.vehicleLastGoodZ = this.lastVehicle.getZ();
         if (this.clientVehicleIsFloating && this.lastVehicle.getControllingPassenger() == this.player) {
            if (++this.aboveGroundVehicleTickCount > this.getMaximumFlyingTicks(this.lastVehicle)) {
               LOGGER.warn("{} was kicked for floating a vehicle too long!", this.player.getPlainTextName());
               this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
               return true;
            }
         } else {
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
         }
      } else {
         this.lastVehicle = null;
         this.clientVehicleIsFloating = false;
         this.aboveGroundVehicleTickCount = 0;
      }

      return false;
   }

   private int getMaximumFlyingTicks(Entity $$0) {
      double $$1 = $$0.getGravity();
      if ($$1 < 1.0E-5F) {
         return Integer.MAX_VALUE;
      } else {
         double $$2 = 0.08 / $$1;
         return Mth.ceil(80.0 * Math.max($$2, 1.0));
      }
   }

   public void resetFlyingTicks() {
      this.aboveGroundTickCount = 0;
      this.aboveGroundVehicleTickCount = 0;
   }

   public void resetPosition() {
      this.firstGoodX = this.player.getX();
      this.firstGoodY = this.player.getY();
      this.firstGoodZ = this.player.getZ();
      this.lastGoodX = this.player.getX();
      this.lastGoodY = this.player.getY();
      this.lastGoodZ = this.player.getZ();
   }

   public boolean isAcceptingMessages() {
      return this.connection.isConnected() && !this.waitingForSwitchToConfig;
   }

   public boolean shouldHandleMessage(Packet<?> $$0) {
      return super.shouldHandleMessage($$0)
         ? true
         : this.waitingForSwitchToConfig && this.connection.isConnected() && $$0 instanceof ServerboundConfigurationAcknowledgedPacket;
   }

   @Override
   protected GameProfile playerProfile() {
      return this.player.getGameProfile();
   }

   private <T, R> CompletableFuture<R> filterTextPacket(T $$0, BiFunction<TextFilter, T, CompletableFuture<R>> $$1) {
      return $$1.apply(this.player.getTextFilter(), $$0).thenApply($$0x -> {
         if (!this.isAcceptingMessages()) {
            LOGGER.debug("Ignoring packet due to disconnection");
            throw new CancellationException("disconnected");
         } else {
            return (R)$$0x;
         }
      });
   }

   private CompletableFuture<FilteredText> filterTextPacket(String $$0) {
      return this.filterTextPacket($$0, TextFilter::processStreamMessage);
   }

   private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> $$0) {
      return this.filterTextPacket($$0, TextFilter::processMessageBundle);
   }

   public void handlePlayerInput(ServerboundPlayerInputPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.setLastClientInput($$0.input());
      if (this.hasClientLoaded()) {
         this.player.resetLastActionTime();
         this.player.setShiftKeyDown($$0.input().shift());
      }
   }

   private static boolean containsInvalidValues(double $$0, double $$1, double $$2, float $$3, float $$4) {
      return Double.isNaN($$0) || Double.isNaN($$1) || Double.isNaN($$2) || !Floats.isFinite($$4) || !Floats.isFinite($$3);
   }

   private static double clampHorizontal(double $$0) {
      return Mth.clamp($$0, -3.0E7, 3.0E7);
   }

   private static double clampVertical(double $$0) {
      return Mth.clamp($$0, -2.0E7, 2.0E7);
   }

   public void handleMoveVehicle(ServerboundMoveVehiclePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (containsInvalidValues($$0.position().x(), $$0.position().y(), $$0.position().z(), $$0.yRot(), $$0.xRot())) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
      } else if (!this.updateAwaitingTeleport() && this.hasClientLoaded()) {
         Entity $$1 = this.player.getRootVehicle();
         if ($$1 != this.player && $$1.getControllingPassenger() == this.player && $$1 == this.lastVehicle) {
            ServerLevel $$2 = this.player.level();
            double $$3 = $$1.getX();
            double $$4 = $$1.getY();
            double $$5 = $$1.getZ();
            double $$6 = clampHorizontal($$0.position().x());
            double $$7 = clampVertical($$0.position().y());
            double $$8 = clampHorizontal($$0.position().z());
            float $$9 = Mth.wrapDegrees($$0.yRot());
            float $$10 = Mth.wrapDegrees($$0.xRot());
            double $$11 = $$6 - this.vehicleFirstGoodX;
            double $$12 = $$7 - this.vehicleFirstGoodY;
            double $$13 = $$8 - this.vehicleFirstGoodZ;
            double $$14 = $$1.getDeltaMovement().lengthSqr();
            double $$15 = $$11 * $$11 + $$12 * $$12 + $$13 * $$13;
            if ($$15 - $$14 > 100.0 && !this.isSingleplayerOwner()) {
               LOGGER.warn(
                  "{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{$$1.getPlainTextName(), this.player.getPlainTextName(), $$11, $$12, $$13}
               );
               this.send(ClientboundMoveVehiclePacket.fromEntity($$1));
               return;
            }

            AABB $$16 = $$1.getBoundingBox();
            $$11 = $$6 - this.vehicleLastGoodX;
            $$12 = $$7 - this.vehicleLastGoodY;
            $$13 = $$8 - this.vehicleLastGoodZ;
            boolean $$17 = $$1.verticalCollisionBelow;
            if ($$1 instanceof LivingEntity $$18 && $$18.onClimbable()) {
               $$18.resetFallDistance();
            }

            $$1.move(MoverType.PLAYER, new Vec3($$11, $$12, $$13));
            $$11 = $$6 - $$1.getX();
            $$12 = $$7 - $$1.getY();
            if ($$12 > -0.5 || $$12 < 0.5) {
               $$12 = 0.0;
            }

            $$13 = $$8 - $$1.getZ();
            $$15 = $$11 * $$11 + $$12 * $$12 + $$13 * $$13;
            boolean $$20 = false;
            if ($$15 > 0.0625) {
               $$20 = true;
               LOGGER.warn("{} (vehicle of {}) moved wrongly! {}", new Object[]{$$1.getPlainTextName(), this.player.getPlainTextName(), Math.sqrt($$15)});
            }

            if ($$20 && $$2.noCollision($$1, $$16) || this.isEntityCollidingWithAnythingNew($$2, $$1, $$16, $$6, $$7, $$8)) {
               $$1.absSnapTo($$3, $$4, $$5, $$9, $$10);
               this.send(ClientboundMoveVehiclePacket.fromEntity($$1));
               $$1.removeLatestMovementRecording();
               return;
            }

            $$1.absSnapTo($$6, $$7, $$8, $$9, $$10);
            this.player.level().getChunkSource().move(this.player);
            Vec3 $$21 = new Vec3($$1.getX() - $$3, $$1.getY() - $$4, $$1.getZ() - $$5);
            this.handlePlayerKnownMovement($$21);
            $$1.setOnGroundWithMovement($$0.onGround(), $$21);
            $$1.doCheckFallDamage($$21.x, $$21.y, $$21.z, $$0.onGround());
            this.player.checkMovementStatistics($$21.x, $$21.y, $$21.z);
            this.clientVehicleIsFloating = $$12 >= -0.03125
               && !$$17
               && !this.server.allowFlight()
               && !$$1.isFlyingVehicle()
               && !$$1.isNoGravity()
               && this.noBlocksAround($$1);
            this.vehicleLastGoodX = $$1.getX();
            this.vehicleLastGoodY = $$1.getY();
            this.vehicleLastGoodZ = $$1.getZ();
         }
      }
   }

   private boolean noBlocksAround(Entity $$0) {
      return $$0.level().getBlockStates($$0.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockStateBase::isAir);
   }

   public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if ($$0.getId() == this.awaitingTeleport) {
         if (this.awaitingPositionFromClient == null) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
         }

         this.player
            .absSnapTo(
               this.awaitingPositionFromClient.x,
               this.awaitingPositionFromClient.y,
               this.awaitingPositionFromClient.z,
               this.player.getYRot(),
               this.player.getXRot()
            );
         this.lastGoodX = this.awaitingPositionFromClient.x;
         this.lastGoodY = this.awaitingPositionFromClient.y;
         this.lastGoodZ = this.awaitingPositionFromClient.z;
         this.player.hasChangedDimension();
         this.awaitingPositionFromClient = null;
      }
   }

   public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.markClientLoaded();
   }

   public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      ServerDisplayInfo $$1 = this.server.getRecipeManager().getRecipeFromDisplay($$0.recipe());
      if ($$1 != null) {
         this.player.getRecipeBook().removeHighlight($$1.parent().id());
      }
   }

   public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.containerMenu.setSelectedBundleItemIndex($$0.slotId(), $$0.selectedItemIndex());
   }

   public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.getRecipeBook().setBookSetting($$0.getBookType(), $$0.isOpen(), $$0.isFiltering());
   }

   public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if ($$0.getAction() == Action.OPENED_TAB) {
         Identifier $$1 = Objects.requireNonNull($$0.getTab());
         AdvancementHolder $$2 = this.server.getAdvancements().get($$1);
         if ($$2 != null) {
            this.player.getAdvancements().setSelectedTab($$2);
         }
      }
   }

   public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      StringReader $$1 = new StringReader($$0.getCommand());
      if ($$1.canRead() && $$1.peek() == '/') {
         $$1.skip();
      }

      ParseResults<CommandSourceStack> $$2 = this.server.getCommands().getDispatcher().parse($$1, this.player.createCommandSourceStack());
      this.server.getCommands().getDispatcher().getCompletionSuggestions($$2).thenAccept($$1x -> {
         Suggestions $$2x = $$1x.getList().size() <= 1000 ? $$1x : new Suggestions($$1x.getRange(), $$1x.getList().subList(0, 1000));
         this.send(new ClientboundCommandSuggestionsPacket($$0.getId(), $$2x));
      });
   }

   public void handleSetCommandBlock(ServerboundSetCommandBlockPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock $$1 = null;
         CommandBlockEntity $$2 = null;
         BlockPos $$3 = $$0.getPos();
         BlockEntity $$4 = this.player.level().getBlockEntity($$3);
         if ($$4 instanceof CommandBlockEntity $$5) {
            $$2 = $$5;
            $$1 = $$5.getCommandBlock();
         }

         String $$6 = $$0.getCommand();
         boolean $$7 = $$0.isTrackOutput();
         if ($$1 != null) {
            Mode $$8 = $$2.getMode();
            BlockState $$9 = this.player.level().getBlockState($$3);
            Direction $$10 = (Direction)$$9.getValue(CommandBlock.FACING);

            BlockState $$11 = switch ($$0.getMode()) {
               case SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
               case AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
               default -> Blocks.COMMAND_BLOCK.defaultBlockState();
            };
            BlockState $$12 = (BlockState)((BlockState)$$11.setValue(CommandBlock.FACING, $$10)).setValue(CommandBlock.CONDITIONAL, $$0.isConditional());
            if ($$12 != $$9) {
               this.player.level().setBlock($$3, $$12, 2);
               $$4.setBlockState($$12);
               this.player.level().getChunkAt($$3).setBlockEntity($$4);
            }

            $$1.setCommand($$6);
            $$1.setTrackOutput($$7);
            if (!$$7) {
               $$1.setLastOutput(null);
            }

            $$2.setAutomatic($$0.isAutomatic());
            if ($$8 != $$0.getMode()) {
               $$2.onModeSwitch();
            }

            if (this.player.level().isCommandBlockEnabled()) {
               $$1.onUpdated(this.player.level());
            }

            if (!StringUtil.isNullOrEmpty($$6)) {
               this.player
                  .sendSystemMessage(
                     Component.translatable(
                        this.player.level().isCommandBlockEnabled() ? "advMode.setCommand.success" : "advMode.setCommand.disabled", new Object[]{$$6}
                     )
                  );
            }
         }
      }
   }

   public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!this.player.canUseGameMasterBlocks()) {
         this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
      } else {
         BaseCommandBlock $$1 = $$0.getCommandBlock(this.player.level());
         if ($$1 != null) {
            String $$2 = $$0.getCommand();
            $$1.setCommand($$2);
            $$1.setTrackOutput($$0.isTrackOutput());
            if (!$$0.isTrackOutput()) {
               $$1.setLastOutput(null);
            }

            boolean $$3 = this.player.level().isCommandBlockEnabled();
            if ($$3) {
               $$1.onUpdated(this.player.level());
            }

            if (!StringUtil.isNullOrEmpty($$2)) {
               this.player.sendSystemMessage(Component.translatable($$3 ? "advMode.setCommand.success" : "advMode.setCommand.disabled", new Object[]{$$2}));
            }
         }
      }
   }

   public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket $$0) {
      ServerLevel $$1 = this.player.level();
      PacketUtils.ensureRunningOnSameThread($$0, this, $$1);
      BlockPos $$2 = $$0.pos();
      if (this.player.isWithinBlockInteractionRange($$2, 1.0)) {
         if ($$1.isLoaded($$2)) {
            BlockState $$3 = $$1.getBlockState($$2);
            boolean $$4 = this.player.hasInfiniteMaterials() && $$0.includeData();
            ItemStack $$5 = $$3.getCloneItemStack($$1, $$2, $$4);
            if (!$$5.isEmpty()) {
               if ($$4) {
                  addBlockDataToItem($$3, $$1, $$2, $$5);
               }

               this.tryPickItem($$5);
            }
         }
      }
   }

   private static void addBlockDataToItem(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3) {
      BlockEntity $$4 = $$0.hasBlockEntity() ? $$1.getBlockEntity($$2) : null;
      if ($$4 != null) {
         ScopedCollector $$5 = new ScopedCollector($$4.problemPath(), LOGGER);

         try {
            TagValueOutput $$6 = TagValueOutput.createWithContext($$5, $$1.registryAccess());
            $$4.saveCustomOnly($$6);
            $$4.removeComponentsFromTag($$6);
            BlockItem.setBlockEntityData($$3, $$4.getType(), $$6);
            $$3.applyComponents($$4.collectComponents());
         } catch (Throwable var9) {
            try {
               $$5.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }

            throw var9;
         }

         $$5.close();
      }
   }

   public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket $$0) {
      ServerLevel $$1 = this.player.level();
      PacketUtils.ensureRunningOnSameThread($$0, this, $$1);
      Entity $$2 = $$1.getEntityOrPart($$0.id());
      if ($$2 != null && this.player.isWithinEntityInteractionRange($$2, 3.0)) {
         ItemStack $$3 = $$2.getPickResult();
         if ($$3 != null && !$$3.isEmpty()) {
            this.tryPickItem($$3);
         }
      }
   }

   private void tryPickItem(ItemStack $$0) {
      if ($$0.isItemEnabled(this.player.level().enabledFeatures())) {
         Inventory $$1 = this.player.getInventory();
         int $$2 = $$1.findSlotMatchingItem($$0);
         if ($$2 != -1) {
            if (Inventory.isHotbarSlot($$2)) {
               $$1.setSelectedSlot($$2);
            } else {
               $$1.pickSlot($$2);
            }
         } else if (this.player.hasInfiniteMaterials()) {
            $$1.addAndPickItem($$0);
         }

         this.send(new ClientboundSetHeldSlotPacket($$1.getSelectedSlot()));
         this.player.inventoryMenu.broadcastChanges();
      }
   }

   public void handleRenameItem(ServerboundRenameItemPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.containerMenu instanceof AnvilMenu $$1) {
         if (!$$1.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, $$1);
            return;
         }

         $$1.setItemName($$0.getName());
      }
   }

   public void handleSetBeaconPacket(ServerboundSetBeaconPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.containerMenu instanceof BeaconMenu $$1) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
            return;
         }

         $$1.updateEffects($$0.primary(), $$0.secondary());
      }
   }

   public void handleSetStructureBlock(ServerboundSetStructureBlockPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos $$1 = $$0.getPos();
         BlockState $$2 = this.player.level().getBlockState($$1);
         if (this.player.level().getBlockEntity($$1) instanceof StructureBlockEntity $$4) {
            $$4.setMode($$0.getMode());
            $$4.setStructureName($$0.getName());
            $$4.setStructurePos($$0.getOffset());
            $$4.setStructureSize($$0.getSize());
            $$4.setMirror($$0.getMirror());
            $$4.setRotation($$0.getRotation());
            $$4.setMetaData($$0.getData());
            $$4.setIgnoreEntities($$0.isIgnoreEntities());
            $$4.setStrict($$0.isStrict());
            $$4.setShowAir($$0.isShowAir());
            $$4.setShowBoundingBox($$0.isShowBoundingBox());
            $$4.setIntegrity($$0.getIntegrity());
            $$4.setSeed($$0.getSeed());
            if ($$4.hasStructureName()) {
               String $$5 = $$4.getStructureName();
               if ($$0.getUpdateType() == UpdateType.SAVE_AREA) {
                  if ($$4.saveStructure()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_success", new Object[]{$$5}), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.save_failure", new Object[]{$$5}), false);
                  }
               } else if ($$0.getUpdateType() == UpdateType.LOAD_AREA) {
                  if (!$$4.isStructureLoadable()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", new Object[]{$$5}), false);
                  } else if ($$4.placeStructureIfSameSize(this.player.level())) {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_success", new Object[]{$$5}), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", new Object[]{$$5}), false);
                  }
               } else if ($$0.getUpdateType() == UpdateType.SCAN_AREA) {
                  if ($$4.detectSize()) {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_success", new Object[]{$$5}), false);
                  } else {
                     this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                  }
               }
            } else {
               this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", new Object[]{$$0.getName()}), false);
            }

            $$4.setChanged();
            this.player.level().sendBlockUpdated($$1, $$2, $$2, 3);
         }
      }
   }

   public void handleSetTestBlock(ServerboundSetTestBlockPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos $$1 = $$0.position();
         BlockState $$2 = this.player.level().getBlockState($$1);
         if (this.player.level().getBlockEntity($$1) instanceof TestBlockEntity $$4) {
            $$4.setMode($$0.mode());
            $$4.setMessage($$0.message());
            $$4.setChanged();
            this.player.level().sendBlockUpdated($$1, $$2, $$4.getBlockState(), 3);
         }
      }
   }

   public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      BlockPos $$1 = $$0.pos();
      if (this.player.canUseGameMasterBlocks() && this.player.level().getBlockEntity($$1) instanceof TestInstanceBlockEntity $$2) {
         if ($$0.action() != net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.QUERY
            && $$0.action() != net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.INIT) {
            $$2.set($$0.data());
            if ($$0.action() == net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.RESET) {
               $$2.resetTest(this.player::sendSystemMessage);
            } else if ($$0.action() == net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.SAVE) {
               $$2.saveTest(this.player::sendSystemMessage);
            } else if ($$0.action() == net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.EXPORT) {
               $$2.exportTest(this.player::sendSystemMessage);
            } else if ($$0.action() == net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.RUN) {
               $$2.runTest(this.player::sendSystemMessage);
            }

            BlockState $$10 = this.player.level().getBlockState($$1);
            this.player.level().sendBlockUpdated($$1, Blocks.AIR.defaultBlockState(), $$10, 3);
         } else {
            Registry<GameTestInstance> $$4 = this.player.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
            Optional<Reference<GameTestInstance>> $$5 = $$0.data().test().flatMap($$4::get);
            Component $$6;
            if ($$5.isPresent()) {
               $$6 = ((GameTestInstance)$$5.get().value()).describe();
            } else {
               $$6 = Component.translatable("test_instance.description.no_test").withStyle(ChatFormatting.RED);
            }

            Optional<Vec3i> $$8;
            if ($$0.action() == net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket.Action.QUERY) {
               $$8 = $$0.data().test().flatMap($$0x -> TestInstanceBlockEntity.getStructureSize(this.player.level(), $$0x));
            } else {
               $$8 = Optional.empty();
            }

            this.connection.send(new ClientboundTestInstanceBlockStatus($$6, $$8));
         }
      }
   }

   public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos $$1 = $$0.getPos();
         BlockState $$2 = this.player.level().getBlockState($$1);
         if (this.player.level().getBlockEntity($$1) instanceof JigsawBlockEntity $$4) {
            $$4.setName($$0.getName());
            $$4.setTarget($$0.getTarget());
            $$4.setPool(ResourceKey.create(Registries.TEMPLATE_POOL, $$0.getPool()));
            $$4.setFinalState($$0.getFinalState());
            $$4.setJoint($$0.getJoint());
            $$4.setPlacementPriority($$0.getPlacementPriority());
            $$4.setSelectionPriority($$0.getSelectionPriority());
            $$4.setChanged();
            this.player.level().sendBlockUpdated($$1, $$2, $$2, 3);
         }
      }
   }

   public void handleJigsawGenerate(ServerboundJigsawGeneratePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.canUseGameMasterBlocks()) {
         BlockPos $$1 = $$0.getPos();
         if (this.player.level().getBlockEntity($$1) instanceof JigsawBlockEntity $$3) {
            $$3.generate(this.player.level(), $$0.levels(), $$0.keepJigsaws());
         }
      }
   }

   public void handleSelectTrade(ServerboundSelectTradePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      int $$1 = $$0.getItem();
      if (this.player.containerMenu instanceof MerchantMenu $$2) {
         if (!$$2.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, $$2);
            return;
         }

         $$2.setSelectionHint($$1);
         $$2.tryMoveItems($$1);
      }
   }

   public void handleEditBook(ServerboundEditBookPacket $$0) {
      int $$1 = $$0.slot();
      if (Inventory.isHotbarSlot($$1) || $$1 == 40) {
         List<String> $$2 = Lists.newArrayList();
         Optional<String> $$3 = $$0.title();
         $$3.ifPresent($$2::add);
         $$2.addAll($$0.pages());
         Consumer<List<FilteredText>> $$4 = $$3.isPresent()
            ? $$1x -> this.signBook((FilteredText)$$1x.get(0), $$1x.subList(1, $$1x.size()), $$1)
            : $$1x -> this.updateBookContents($$1x, $$1);
         this.filterTextPacket($$2).thenAcceptAsync($$4, this.server);
      }
   }

   private void updateBookContents(List<FilteredText> $$0, int $$1) {
      ItemStack $$2 = this.player.getInventory().getItem($$1);
      if ($$2.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
         List<Filterable<String>> $$3 = $$0.stream().map(this::filterableFromOutgoing).toList();
         $$2.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent($$3));
      }
   }

   private void signBook(FilteredText $$0, List<FilteredText> $$1, int $$2) {
      ItemStack $$3 = this.player.getInventory().getItem($$2);
      if ($$3.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
         ItemStack $$4 = $$3.transmuteCopy(Items.WRITTEN_BOOK);
         $$4.remove(DataComponents.WRITABLE_BOOK_CONTENT);
         List<Filterable<Component>> $$5 = $$1.stream().map($$0x -> this.filterableFromOutgoing($$0x).map(Component::literal)).toList();
         $$4.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(this.filterableFromOutgoing($$0), this.player.getPlainTextName(), 0, $$5, true));
         this.player.getInventory().setItem($$2, $$4);
      }
   }

   private Filterable<String> filterableFromOutgoing(FilteredText $$0) {
      return this.player.isTextFilteringEnabled() ? Filterable.passThrough($$0.filteredOrEmpty()) : Filterable.from($$0);
   }

   public void handleEntityTagQuery(ServerboundEntityTagQueryPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         Entity $$1 = this.player.level().getEntity($$0.getEntityId());
         if ($$1 != null) {
            ScopedCollector $$2 = new ScopedCollector($$1.problemPath(), LOGGER);

            try {
               TagValueOutput $$3 = TagValueOutput.createWithContext($$2, $$1.registryAccess());
               $$1.saveWithoutId($$3);
               CompoundTag $$4 = $$3.buildResult();
               this.send(new ClientboundTagQueryPacket($$0.getTransactionId(), $$4));
            } catch (Throwable var7) {
               try {
                  $$2.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            $$2.close();
         }
      }
   }

   public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!this.player.isSpectator() && $$0.containerId() == this.player.containerMenu.containerId) {
         if (this.player.containerMenu instanceof CrafterMenu $$1 && $$1.getContainer() instanceof CrafterBlockEntity $$2) {
            $$2.setSlotState($$0.slotId(), $$0.newState());
         }
      }
   }

   public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
         BlockEntity $$1 = this.player.level().getBlockEntity($$0.getPos());
         CompoundTag $$2 = $$1 != null ? $$1.saveWithoutMetadata(this.player.registryAccess()) : null;
         this.send(new ClientboundTagQueryPacket($$0.getTransactionId(), $$2));
      }
   }

   public void handleMovePlayer(ServerboundMovePlayerPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (containsInvalidValues($$0.getX(0.0), $$0.getY(0.0), $$0.getZ(0.0), $$0.getYRot(0.0F), $$0.getXRot(0.0F))) {
         this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
      } else {
         ServerLevel $$1 = this.player.level();
         if (!this.player.wonGame) {
            if (this.tickCount == 0) {
               this.resetPosition();
            }

            if (this.hasClientLoaded()) {
               float $$2 = Mth.wrapDegrees($$0.getYRot(this.player.getYRot()));
               float $$3 = Mth.wrapDegrees($$0.getXRot(this.player.getXRot()));
               if (this.updateAwaitingTeleport()) {
                  this.player.absSnapRotationTo($$2, $$3);
               } else {
                  double $$4 = clampHorizontal($$0.getX(this.player.getX()));
                  double $$5 = clampVertical($$0.getY(this.player.getY()));
                  double $$6 = clampHorizontal($$0.getZ(this.player.getZ()));
                  if (this.player.isPassenger()) {
                     this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), $$2, $$3);
                     this.player.level().getChunkSource().move(this.player);
                  } else {
                     double $$7 = this.player.getX();
                     double $$8 = this.player.getY();
                     double $$9 = this.player.getZ();
                     double $$10 = $$4 - this.firstGoodX;
                     double $$11 = $$5 - this.firstGoodY;
                     double $$12 = $$6 - this.firstGoodZ;
                     double $$13 = this.player.getDeltaMovement().lengthSqr();
                     double $$14 = $$10 * $$10 + $$11 * $$11 + $$12 * $$12;
                     if (this.player.isSleeping()) {
                        if ($$14 > 1.0) {
                           this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), $$2, $$3);
                        }
                     } else {
                        boolean $$15 = this.player.isFallFlying();
                        if ($$1.tickRateManager().runsNormally()) {
                           this.receivedMovePacketCount++;
                           int $$16 = this.receivedMovePacketCount - this.knownMovePacketCount;
                           if ($$16 > 5) {
                              LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getPlainTextName(), $$16);
                              $$16 = 1;
                           }

                           if (this.shouldCheckPlayerMovement($$15)) {
                              float $$17 = $$15 ? 300.0F : 100.0F;
                              if ($$14 - $$13 > $$17 * $$16) {
                                 LOGGER.warn("{} moved too quickly! {},{},{}", new Object[]{this.player.getPlainTextName(), $$10, $$11, $$12});
                                 this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
                                 return;
                              }
                           }
                        }

                        AABB $$18 = this.player.getBoundingBox();
                        $$10 = $$4 - this.lastGoodX;
                        $$11 = $$5 - this.lastGoodY;
                        $$12 = $$6 - this.lastGoodZ;
                        boolean $$19 = $$11 > 0.0;
                        if (this.player.onGround() && !$$0.isOnGround() && $$19) {
                           this.player.jumpFromGround();
                        }

                        boolean $$20 = this.player.verticalCollisionBelow;
                        this.player.move(MoverType.PLAYER, new Vec3($$10, $$11, $$12));
                        $$10 = $$4 - this.player.getX();
                        $$11 = $$5 - this.player.getY();
                        if ($$11 > -0.5 || $$11 < 0.5) {
                           $$11 = 0.0;
                        }

                        $$12 = $$6 - this.player.getZ();
                        $$14 = $$10 * $$10 + $$11 * $$11 + $$12 * $$12;
                        boolean $$22 = false;
                        if (!this.player.isChangingDimension()
                           && $$14 > 0.0625
                           && !this.player.isSleeping()
                           && !this.player.isCreative()
                           && !this.player.isSpectator()
                           && !this.player.isInPostImpulseGraceTime()) {
                           $$22 = true;
                           LOGGER.warn("{} moved wrongly!", this.player.getPlainTextName());
                        }

                        if (this.player.noPhysics
                           || this.player.isSleeping()
                           || (!$$22 || !$$1.noCollision(this.player, $$18)) && !this.isEntityCollidingWithAnythingNew($$1, this.player, $$18, $$4, $$5, $$6)) {
                           this.player.absSnapTo($$4, $$5, $$6, $$2, $$3);
                           boolean $$23 = this.player.isAutoSpinAttack();
                           this.clientIsFloating = $$11 >= -0.03125
                              && !$$20
                              && !this.player.isSpectator()
                              && !this.server.allowFlight()
                              && !this.player.getAbilities().mayfly
                              && !this.player.hasEffect(MobEffects.LEVITATION)
                              && !$$15
                              && !$$23
                              && this.noBlocksAround(this.player);
                           this.player.level().getChunkSource().move(this.player);
                           Vec3 $$24 = new Vec3(this.player.getX() - $$7, this.player.getY() - $$8, this.player.getZ() - $$9);
                           this.player.setOnGroundWithMovement($$0.isOnGround(), $$0.horizontalCollision(), $$24);
                           this.player.doCheckFallDamage($$24.x, $$24.y, $$24.z, $$0.isOnGround());
                           this.handlePlayerKnownMovement($$24);
                           if ($$19) {
                              this.player.resetFallDistance();
                           }

                           if ($$0.isOnGround() || this.player.hasLandedInLiquid() || this.player.onClimbable() || this.player.isSpectator() || $$15 || $$23) {
                              this.player.tryResetCurrentImpulseContext();
                           }

                           this.player.checkMovementStatistics(this.player.getX() - $$7, this.player.getY() - $$8, this.player.getZ() - $$9);
                           this.lastGoodX = this.player.getX();
                           this.lastGoodY = this.player.getY();
                           this.lastGoodZ = this.player.getZ();
                        } else {
                           this.teleport($$7, $$8, $$9, $$2, $$3);
                           this.player.doCheckFallDamage(this.player.getX() - $$7, this.player.getY() - $$8, this.player.getZ() - $$9, $$0.isOnGround());
                           this.player.removeLatestMovementRecording();
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldCheckPlayerMovement(boolean $$0) {
      if (this.isSingleplayerOwner()) {
         return false;
      } else if (this.player.isChangingDimension()) {
         return false;
      } else {
         GameRules $$1 = this.player.level().getGameRules();
         return !$$1.get(GameRules.PLAYER_MOVEMENT_CHECK) ? false : !$$0 || (Boolean)$$1.get(GameRules.ELYTRA_MOVEMENT_CHECK);
      }
   }

   private boolean updateAwaitingTeleport() {
      if (this.awaitingPositionFromClient != null) {
         if (this.tickCount - this.awaitingTeleportTime > 20) {
            this.awaitingTeleportTime = this.tickCount;
            this.teleport(
               this.awaitingPositionFromClient.x,
               this.awaitingPositionFromClient.y,
               this.awaitingPositionFromClient.z,
               this.player.getYRot(),
               this.player.getXRot()
            );
         }

         return true;
      } else {
         this.awaitingTeleportTime = this.tickCount;
         return false;
      }
   }

   private boolean isEntityCollidingWithAnythingNew(LevelReader $$0, Entity $$1, AABB $$2, double $$3, double $$4, double $$5) {
      AABB $$6 = $$1.getBoundingBox().move($$3 - $$1.getX(), $$4 - $$1.getY(), $$5 - $$1.getZ());
      Iterable<VoxelShape> $$7 = $$0.getPreMoveCollisions($$1, $$6.deflate(1.0E-5F), $$2.getBottomCenter());
      VoxelShape $$8 = Shapes.create($$2.deflate(1.0E-5F));

      for (VoxelShape $$9 : $$7) {
         if (!Shapes.joinIsNotEmpty($$9, $$8, BooleanOp.AND)) {
            return true;
         }
      }

      return false;
   }

   public void teleport(double $$0, double $$1, double $$2, float $$3, float $$4) {
      this.teleport(new PositionMoveRotation(new Vec3($$0, $$1, $$2), Vec3.ZERO, $$3, $$4), Collections.emptySet());
   }

   public void teleport(PositionMoveRotation $$0, Set<Relative> $$1) {
      this.awaitingTeleportTime = this.tickCount;
      if (++this.awaitingTeleport == Integer.MAX_VALUE) {
         this.awaitingTeleport = 0;
      }

      this.player.teleportSetPosition($$0, $$1);
      this.awaitingPositionFromClient = this.player.position();
      this.send(ClientboundPlayerPositionPacket.of(this.awaitingTeleport, $$0, $$1));
   }

   public void handlePlayerAction(ServerboundPlayerActionPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.hasClientLoaded()) {
         BlockPos $$1 = $$0.getPos();
         this.player.resetLastActionTime();
         net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action $$2 = $$0.getAction();
         switch ($$2) {
            case STAB:
               if (this.player.isSpectator()) {
                  return;
               } else {
                  ItemStack $$3 = this.player.getItemInHand(InteractionHand.MAIN_HAND);
                  if (this.player.cannotAttackWithItem($$3, 5)) {
                     return;
                  }

                  PiercingWeapon $$4 = (PiercingWeapon)$$3.get(DataComponents.PIERCING_WEAPON);
                  if ($$4 != null) {
                     $$4.attack(this.player, EquipmentSlot.MAINHAND);
                  }

                  return;
               }
            case SWAP_ITEM_WITH_OFFHAND:
               if (!this.player.isSpectator()) {
                  ItemStack $$5 = this.player.getItemInHand(InteractionHand.OFF_HAND);
                  this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                  this.player.setItemInHand(InteractionHand.MAIN_HAND, $$5);
                  this.player.stopUsingItem();
               }

               return;
            case DROP_ITEM:
               if (!this.player.isSpectator()) {
                  this.player.drop(false);
               }

               return;
            case DROP_ALL_ITEMS:
               if (!this.player.isSpectator()) {
                  this.player.drop(true);
               }

               return;
            case RELEASE_USE_ITEM:
               this.player.releaseUsingItem();
               return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
               this.player.gameMode.handleBlockBreakAction($$1, $$2, $$0.getDirection(), this.player.level().getMaxY(), $$0.getSequence());
               this.ackBlockChangesUpTo($$0.getSequence());
               return;
            default:
               throw new IllegalArgumentException("Invalid player action");
         }
      }
   }

   private static boolean wasBlockPlacementAttempt(ServerPlayer $$0, ItemStack $$1) {
      if ($$1.isEmpty()) {
         return false;
      } else {
         Item $$2 = $$1.getItem();
         return ($$2 instanceof BlockItem || $$2 instanceof BucketItem $$3 && $$3.getContent() != Fluids.EMPTY) && !$$0.getCooldowns().isOnCooldown($$1);
      }
   }

   public void handleUseItemOn(ServerboundUseItemOnPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.hasClientLoaded()) {
         this.ackBlockChangesUpTo($$0.getSequence());
         ServerLevel $$1 = this.player.level();
         InteractionHand $$2 = $$0.getHand();
         ItemStack $$3 = this.player.getItemInHand($$2);
         if ($$3.isItemEnabled($$1.enabledFeatures())) {
            BlockHitResult $$4 = $$0.getHitResult();
            Vec3 $$5 = $$4.getLocation();
            BlockPos $$6 = $$4.getBlockPos();
            if (this.player.isWithinBlockInteractionRange($$6, 1.0)) {
               Vec3 $$7 = $$5.subtract(Vec3.atCenterOf($$6));
               double $$8 = 1.0000001;
               if (Math.abs($$7.x()) < 1.0000001 && Math.abs($$7.y()) < 1.0000001 && Math.abs($$7.z()) < 1.0000001) {
                  Direction $$9 = $$4.getDirection();
                  this.player.resetLastActionTime();
                  int $$10 = this.player.level().getMaxY();
                  if ($$6.getY() <= $$10) {
                     if (this.awaitingPositionFromClient == null && $$1.mayInteract(this.player, $$6)) {
                        InteractionResult $$11 = this.player.gameMode.useItemOn(this.player, $$1, $$3, $$2, $$4);
                        if ($$11.consumesAction()) {
                           CriteriaTriggers.ANY_BLOCK_USE.trigger(this.player, $$4.getBlockPos(), $$3.copy());
                        }

                        if ($$9 == Direction.UP && !$$11.consumesAction() && $$6.getY() >= $$10 && wasBlockPlacementAttempt(this.player, $$3)) {
                           Component $$12 = Component.translatable("build.tooHigh", new Object[]{$$10}).withStyle(ChatFormatting.RED);
                           this.player.sendSystemMessage($$12, true);
                        } else if ($$11 instanceof Success $$13 && $$13.swingSource() == SwingSource.SERVER) {
                           this.player.swing($$2, true);
                        }
                     }
                  } else {
                     Component $$14 = Component.translatable("build.tooHigh", new Object[]{$$10}).withStyle(ChatFormatting.RED);
                     this.player.sendSystemMessage($$14, true);
                  }

                  this.send(new ClientboundBlockUpdatePacket($$1, $$6));
                  this.send(new ClientboundBlockUpdatePacket($$1, $$6.relative($$9)));
               } else {
                  LOGGER.warn(
                     "Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.",
                     new Object[]{this.player.getGameProfile().name(), $$5, $$6}
                  );
               }
            }
         }
      }
   }

   public void handleUseItem(ServerboundUseItemPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.hasClientLoaded()) {
         this.ackBlockChangesUpTo($$0.getSequence());
         ServerLevel $$1 = this.player.level();
         InteractionHand $$2 = $$0.getHand();
         ItemStack $$3 = this.player.getItemInHand($$2);
         this.player.resetLastActionTime();
         if (!$$3.isEmpty() && $$3.isItemEnabled($$1.enabledFeatures())) {
            float $$4 = Mth.wrapDegrees($$0.getYRot());
            float $$5 = Mth.wrapDegrees($$0.getXRot());
            if ($$5 != this.player.getXRot() || $$4 != this.player.getYRot()) {
               this.player.absSnapRotationTo($$4, $$5);
            }

            if (this.player.gameMode.useItem(this.player, $$1, $$3, $$2) instanceof Success $$7 && $$7.swingSource() == SwingSource.SERVER) {
               this.player.swing($$2, true);
            }
         }
      }
   }

   public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.isSpectator()) {
         for (ServerLevel $$1 : this.server.getAllLevels()) {
            Entity $$2 = $$0.getEntity($$1);
            if ($$2 != null) {
               this.player.teleportTo($$1, $$2.getX(), $$2.getY(), $$2.getZ(), Set.of(), $$2.getYRot(), $$2.getXRot(), true);
               return;
            }
         }
      }
   }

   public void handlePaddleBoat(ServerboundPaddleBoatPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.getControlledVehicle() instanceof AbstractBoat $$2) {
         $$2.setPaddleState($$0.getLeft(), $$0.getRight());
      }
   }

   @Override
   public void onDisconnect(DisconnectionDetails $$0) {
      LOGGER.info("{} lost connection: {}", this.player.getPlainTextName(), $$0.reason().getString());
      this.removePlayerFromWorld();
      super.onDisconnect($$0);
   }

   private void removePlayerFromWorld() {
      this.chatMessageChain.close();
      this.server.invalidateStatus();
      this.server
         .getPlayerList()
         .broadcastSystemMessage(
            Component.translatable("multiplayer.player.left", new Object[]{this.player.getDisplayName()}).withStyle(ChatFormatting.YELLOW), false
         );
      this.player.disconnect();
      this.server.getPlayerList().remove(this.player);
      this.player.getTextFilter().leave();
   }

   public void ackBlockChangesUpTo(int $$0) {
      if ($$0 < 0) {
         throw new IllegalArgumentException("Expected packet sequence nr >= 0");
      } else {
         this.ackBlockChangesUpTo = Math.max($$0, this.ackBlockChangesUpTo);
      }
   }

   public void handleSetCarriedItem(ServerboundSetCarriedItemPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if ($$0.getSlot() >= 0 && $$0.getSlot() < Inventory.getSelectionSize()) {
         if (this.player.getInventory().getSelectedSlot() != $$0.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.player.stopUsingItem();
         }

         this.player.getInventory().setSelectedSlot($$0.getSlot());
         this.player.resetLastActionTime();
      } else {
         LOGGER.warn("{} tried to set an invalid carried item", this.player.getPlainTextName());
      }
   }

   public void handleChat(ServerboundChatPacket $$0) {
      Optional<LastSeenMessages> $$1 = this.unpackAndApplyLastSeen($$0.lastSeenMessages());
      if (!$$1.isEmpty()) {
         this.tryHandleChat($$0.message(), false, () -> {
            PlayerChatMessage $$2;
            try {
               $$2 = this.getSignedMessage($$0, $$1.get());
            } catch (DecodeException var6) {
               this.handleMessageDecodeFailure(var6);
               return;
            }

            CompletableFuture<FilteredText> $$5 = this.filterTextPacket($$2.signedContent());
            Component $$6 = this.server.getChatDecorator().decorate(this.player, $$2.decoratedContent());
            this.chatMessageChain.append($$5, $$2x -> {
               PlayerChatMessage $$3 = $$2.withUnsignedContent($$6).filter($$2x.mask());
               this.broadcastChatMessage($$3);
            });
         });
      }
   }

   public void handleChatCommand(ServerboundChatCommandPacket $$0) {
      this.tryHandleChat($$0.command(), true, () -> {
         this.performUnsignedChatCommand($$0.command());
         this.detectRateSpam();
      });
   }

   private void performUnsignedChatCommand(String $$0) {
      ParseResults<CommandSourceStack> $$1 = this.parseCommand($$0);
      if (this.server.enforceSecureProfile() && SignableCommand.hasSignableArguments($$1)) {
         LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", this.player.getGameProfile().name(), $$0);
         this.player.sendSystemMessage(INVALID_COMMAND_SIGNATURE);
      } else {
         this.server.getCommands().performCommand($$1, $$0);
      }
   }

   public void handleSignedChatCommand(ServerboundChatCommandSignedPacket $$0) {
      Optional<LastSeenMessages> $$1 = this.unpackAndApplyLastSeen($$0.lastSeenMessages());
      if (!$$1.isEmpty()) {
         this.tryHandleChat($$0.command(), true, () -> {
            this.performSignedChatCommand($$0, $$1.get());
            this.detectRateSpam();
         });
      }
   }

   private void performSignedChatCommand(ServerboundChatCommandSignedPacket $$0, LastSeenMessages $$1) {
      ParseResults<CommandSourceStack> $$2 = this.parseCommand($$0.command());

      Map<String, PlayerChatMessage> $$3;
      try {
         $$3 = this.collectSignedArguments($$0, SignableCommand.of($$2), $$1);
      } catch (DecodeException var6) {
         this.handleMessageDecodeFailure(var6);
         return;
      }

      CommandSigningContext $$6 = new SignedArguments($$3);
      $$2 = Commands.mapSource($$2, $$1x -> $$1x.withSigningContext($$6, this.chatMessageChain));
      this.server.getCommands().performCommand($$2, $$0.command());
   }

   private void handleMessageDecodeFailure(DecodeException $$0) {
      LOGGER.warn("Failed to update secure chat state for {}: '{}'", this.player.getGameProfile().name(), $$0.getComponent().getString());
      this.player.sendSystemMessage($$0.getComponent().copy().withStyle(ChatFormatting.RED));
   }

   private <S> Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandSignedPacket $$0, SignableCommand<S> $$1, LastSeenMessages $$2) throws DecodeException {
      List<Entry> $$3 = $$0.argumentSignatures().entries();
      List<Argument<S>> $$4 = $$1.arguments();
      if ($$3.isEmpty()) {
         return this.collectUnsignedArguments($$4);
      } else {
         Map<String, PlayerChatMessage> $$5 = new Object2ObjectOpenHashMap();

         for (Entry $$6 : $$3) {
            Argument<S> $$7 = $$1.getArgument($$6.name());
            if ($$7 == null) {
               this.signedMessageDecoder.setChainBroken();
               throw createSignedArgumentMismatchException($$0.command(), $$3, $$4);
            }

            SignedMessageBody $$8 = new SignedMessageBody($$7.value(), $$0.timeStamp(), $$0.salt(), $$2);
            $$5.put($$7.name(), this.signedMessageDecoder.unpack($$6.signature(), $$8));
         }

         for (Argument<S> $$9 : $$4) {
            if (!$$5.containsKey($$9.name())) {
               throw createSignedArgumentMismatchException($$0.command(), $$3, $$4);
            }
         }

         return $$5;
      }
   }

   private <S> Map<String, PlayerChatMessage> collectUnsignedArguments(List<Argument<S>> $$0) throws DecodeException {
      Map<String, PlayerChatMessage> $$1 = new HashMap<>();

      for (Argument<S> $$2 : $$0) {
         SignedMessageBody $$3 = SignedMessageBody.unsigned($$2.value());
         $$1.put($$2.name(), this.signedMessageDecoder.unpack(null, $$3));
      }

      return $$1;
   }

   private static <S> DecodeException createSignedArgumentMismatchException(String $$0, List<Entry> $$1, List<Argument<S>> $$2) {
      String $$3 = $$1.stream().<CharSequence>map(Entry::name).collect(Collectors.joining(", "));
      String $$4 = $$2.stream().<CharSequence>map(Argument::name).collect(Collectors.joining(", "));
      LOGGER.error("Signed command mismatch between server and client ('{}'): got [{}] from client, but expected [{}]", new Object[]{$$0, $$3, $$4});
      return new DecodeException(INVALID_COMMAND_SIGNATURE);
   }

   private ParseResults<CommandSourceStack> parseCommand(String $$0) {
      CommandDispatcher<CommandSourceStack> $$1 = this.server.getCommands().getDispatcher();
      return $$1.parse($$0, this.player.createCommandSourceStack());
   }

   private void tryHandleChat(String $$0, boolean $$1, Runnable $$2) {
      if (isChatMessageIllegal($$0)) {
         this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
      } else if (!$$1 && this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
         this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
      } else {
         this.player.resetLastActionTime();
         this.server.execute($$2);
      }
   }

   private Optional<LastSeenMessages> unpackAndApplyLastSeen(Update $$0) {
      synchronized (this.lastSeenMessages) {
         Optional var10000;
         try {
            LastSeenMessages $$1 = this.lastSeenMessages.applyUpdate($$0);
            var10000 = Optional.of($$1);
         } catch (ValidationException var5) {
            LOGGER.error("Failed to validate message acknowledgements from {}: {}", this.player.getPlainTextName(), var5.getMessage());
            this.disconnect(CHAT_VALIDATION_FAILED);
            return Optional.empty();
         }

         return var10000;
      }
   }

   private static boolean isChatMessageIllegal(String $$0) {
      for (int $$1 = 0; $$1 < $$0.length(); $$1++) {
         if (!StringUtil.isAllowedChatCharacter($$0.charAt($$1))) {
            return true;
         }
      }

      return false;
   }

   private PlayerChatMessage getSignedMessage(ServerboundChatPacket $$0, LastSeenMessages $$1) throws DecodeException {
      SignedMessageBody $$2 = new SignedMessageBody($$0.message(), $$0.timeStamp(), $$0.salt(), $$1);
      return this.signedMessageDecoder.unpack($$0.signature(), $$2);
   }

   private void broadcastChatMessage(PlayerChatMessage $$0) {
      this.server.getPlayerList().broadcastChatMessage($$0, this.player, ChatType.bind(ChatType.CHAT, this.player));
      this.detectRateSpam();
   }

   private void detectRateSpam() {
      this.chatSpamThrottler.increment();
      if (!this.chatSpamThrottler.isUnderThreshold()
         && !this.server.getPlayerList().isOp(this.player.nameAndId())
         && !this.server.isSingleplayerOwner(this.player.nameAndId())) {
         this.disconnect(Component.translatable("disconnect.spam"));
      }
   }

   public void handleChatAck(ServerboundChatAckPacket $$0) {
      synchronized (this.lastSeenMessages) {
         try {
            this.lastSeenMessages.applyOffset($$0.offset());
         } catch (ValidationException var5) {
            LOGGER.error("Failed to validate message acknowledgement offset from {}: {}", this.player.getPlainTextName(), var5.getMessage());
            this.disconnect(CHAT_VALIDATION_FAILED);
         }
      }
   }

   public void handleAnimate(ServerboundSwingPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.resetLastActionTime();
      this.player.swing($$0.getHand());
   }

   public void handlePlayerCommand(ServerboundPlayerCommandPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.hasClientLoaded()) {
         this.player.resetLastActionTime();
         switch ($$0.getAction()) {
            case START_SPRINTING:
               this.player.setSprinting(true);
               break;
            case STOP_SPRINTING:
               this.player.setSprinting(false);
               break;
            case STOP_SLEEPING:
               if (this.player.isSleeping()) {
                  this.player.stopSleepInBed(false, true);
                  this.awaitingPositionFromClient = this.player.position();
               }
               break;
            case START_RIDING_JUMP:
               if (this.player.getControlledVehicle() instanceof PlayerRideableJumping $$1) {
                  int $$2 = $$0.getData();
                  if ($$1.canJump() && $$2 > 0) {
                     $$1.handleStartJump($$2);
                  }
               }
               break;
            case STOP_RIDING_JUMP:
               if (this.player.getControlledVehicle() instanceof PlayerRideableJumping $$3) {
                  $$3.handleStopJump();
               }
               break;
            case OPEN_INVENTORY:
               if (this.player.getVehicle() instanceof HasCustomInventoryScreen $$4) {
                  $$4.openCustomInventoryScreen(this.player);
               }
               break;
            case START_FALL_FLYING:
               if (!this.player.tryToStartFallFlying()) {
                  this.player.stopFallFlying();
               }
               break;
            default:
               throw new IllegalArgumentException("Invalid client command!");
         }
      }
   }

   public void sendPlayerChatMessage(PlayerChatMessage $$0, Bound $$1) {
      this.send(
         new ClientboundPlayerChatPacket(
            this.nextChatIndex++,
            $$0.link().sender(),
            $$0.link().index(),
            $$0.signature(),
            $$0.signedBody().pack(this.messageSignatureCache),
            $$0.unsignedContent(),
            $$0.filterMask(),
            $$1
         )
      );
      MessageSignature $$2 = $$0.signature();
      if ($$2 != null) {
         this.messageSignatureCache.push($$0.signedBody(), $$0.signature());
         int $$3;
         synchronized (this.lastSeenMessages) {
            this.lastSeenMessages.addPending($$2);
            $$3 = this.lastSeenMessages.trackedMessagesCount();
         }

         if ($$3 > 4096) {
            this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
         }
      }
   }

   public void sendDisguisedChatMessage(Component $$0, Bound $$1) {
      this.send(new ClientboundDisguisedChatPacket($$0, $$1));
   }

   public SocketAddress getRemoteAddress() {
      return this.connection.getRemoteAddress();
   }

   public void switchToConfig() {
      this.waitingForSwitchToConfig = true;
      this.removePlayerFromWorld();
      this.send(ClientboundStartConfigurationPacket.INSTANCE);
      this.connection.setupOutboundProtocol(ConfigurationProtocols.CLIENTBOUND);
   }

   public void handlePingRequest(ServerboundPingRequestPacket $$0) {
      this.connection.send(new ClientboundPongResponsePacket($$0.getTime()));
   }

   public void handleInteract(ServerboundInteractPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.hasClientLoaded()) {
         final ServerLevel $$1 = this.player.level();
         final Entity $$2 = $$0.getTarget($$1);
         this.player.resetLastActionTime();
         this.player.setShiftKeyDown($$0.isUsingSecondaryAction());
         if ($$2 != null) {
            if (!$$1.getWorldBorder().isWithinBounds($$2.blockPosition())) {
               return;
            }

            AABB $$3 = $$2.getBoundingBox();
            if ($$0.isWithinRange(this.player, $$3, 3.0)) {
               $$0.dispatch(
                  new Handler() {
                     private void performInteraction(InteractionHand $$0, ServerGamePacketListenerImpl.EntityInteraction $$1x) {
                        ItemStack $$2x = ServerGamePacketListenerImpl.this.player.getItemInHand($$0);
                        if ($$2x.isItemEnabled($$1.enabledFeatures())) {
                           ItemStack $$3x = $$2x.copy();
                           if ($$1.run(ServerGamePacketListenerImpl.this.player, $$2, $$0) instanceof Success $$5) {
                              ItemStack $$6 = $$5.wasItemInteraction() ? $$3x : ItemStack.EMPTY;
                              CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, $$6, $$2);
                              if ($$5.swingSource() == SwingSource.SERVER) {
                                 ServerGamePacketListenerImpl.this.player.swing($$0, true);
                              }
                           }
                        }
                     }

                     public void onInteraction(InteractionHand $$0) {
                        this.performInteraction($$0, Player::interactOn);
                     }

                     public void onInteraction(InteractionHand $$0, Vec3 $$1x) {
                        this.performInteraction($$0, ($$1xxx, $$2xx, $$3x) -> $$2xx.interactAt($$1xxx, $$1, $$3x));
                     }

                     public void onAttack() {
                        if (!($$2 instanceof ItemEntity)
                           && !($$2 instanceof ExperienceOrb)
                           && $$2 != ServerGamePacketListenerImpl.this.player
                           && !($$2 instanceof AbstractArrow $$0x && !$$0x.isAttackable())) {
                           ItemStack $$1x = ServerGamePacketListenerImpl.this.player.getItemInHand(InteractionHand.MAIN_HAND);
                           if ($$1x.isItemEnabled($$1.enabledFeatures())) {
                              if (!ServerGamePacketListenerImpl.this.player.cannotAttackWithItem($$1x, 5)) {
                                 ServerGamePacketListenerImpl.this.player.attack($$2);
                              }
                           }
                        } else {
                           ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                           ServerGamePacketListenerImpl.LOGGER
                              .warn("Player {} tried to attack an invalid entity", ServerGamePacketListenerImpl.this.player.getPlainTextName());
                        }
                     }
                  }
               );
            }
         }
      }
   }

   public void handleClientCommand(ServerboundClientCommandPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.resetLastActionTime();
      net.minecraft.network.protocol.game.ServerboundClientCommandPacket.Action $$1 = $$0.getAction();
      switch ($$1) {
         case PERFORM_RESPAWN:
            if (this.player.wonGame) {
               this.player.wonGame = false;
               this.player = this.server.getPlayerList().respawn(this.player, true, RemovalReason.CHANGED_DIMENSION);
               this.resetPosition();
               this.restartClientLoadTimerAfterRespawn();
               CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
            } else {
               if (this.player.getHealth() > 0.0F) {
                  return;
               }

               this.player = this.server.getPlayerList().respawn(this.player, false, RemovalReason.KILLED);
               this.resetPosition();
               this.restartClientLoadTimerAfterRespawn();
               if (this.server.isHardcore()) {
                  this.player.setGameMode(GameType.SPECTATOR);
                  this.player.level().getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, false, this.server);
               }
            }
            break;
         case REQUEST_STATS:
            this.player.getStats().sendStats(this.player);
      }
   }

   public void handleContainerClose(ServerboundContainerClosePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.doCloseContainer();
   }

   public void handleContainerClick(ServerboundContainerClickPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == $$0.containerId()) {
         if (this.player.isSpectator()) {
            this.player.containerMenu.sendAllDataToRemote();
         } else if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            int $$1 = $$0.slotNum();
            if (!this.player.containerMenu.isValidSlotIndex($$1)) {
               LOGGER.debug(
                  "Player {} clicked invalid slot index: {}, available slots: {}",
                  new Object[]{this.player.getPlainTextName(), $$1, this.player.containerMenu.slots.size()}
               );
            } else {
               boolean $$2 = $$0.stateId() != this.player.containerMenu.getStateId();
               this.player.containerMenu.suppressRemoteUpdates();
               this.player.containerMenu.clicked($$1, $$0.buttonNum(), $$0.clickType(), this.player);
               ObjectIterator var4 = Int2ObjectMaps.fastIterable($$0.changedSlots()).iterator();

               while (var4.hasNext()) {
                  it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<HashedStack> $$3 = (it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<HashedStack>)var4.next();
                  this.player.containerMenu.setRemoteSlotUnsafe($$3.getIntKey(), (HashedStack)$$3.getValue());
               }

               this.player.containerMenu.setRemoteCarried($$0.carriedItem());
               this.player.containerMenu.resumeRemoteUpdates();
               if ($$2) {
                  this.player.containerMenu.broadcastFullState();
               } else {
                  this.player.containerMenu.broadcastChanges();
               }
            }
         }
      }
   }

   public void handlePlaceRecipe(ServerboundPlaceRecipePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.resetLastActionTime();
      if (!this.player.isSpectator() && this.player.containerMenu.containerId == $$0.containerId()) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            ServerDisplayInfo $$1 = this.server.getRecipeManager().getRecipeFromDisplay($$0.recipe());
            if ($$1 != null) {
               RecipeHolder<?> $$2 = $$1.parent();
               if (this.player.getRecipeBook().contains($$2.id())) {
                  if (this.player.containerMenu instanceof RecipeBookMenu $$3) {
                     if ($$2.value().placementInfo().isImpossibleToPlace()) {
                        LOGGER.debug("Player {} tried to place impossible recipe {}", this.player, $$2.id().identifier());
                        return;
                     }

                     PostPlaceAction $$4 = $$3.handlePlacement(
                        $$0.useMaxItems(), this.player.isCreative(), $$2, this.player.level(), this.player.getInventory()
                     );
                     if ($$4 == PostPlaceAction.PLACE_GHOST_RECIPE) {
                        this.send(new ClientboundPlaceGhostRecipePacket(this.player.containerMenu.containerId, $$1.display().display()));
                     }
                  }
               }
            }
         }
      }
   }

   public void handleContainerButtonClick(ServerboundContainerButtonClickPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.resetLastActionTime();
      if (this.player.containerMenu.containerId == $$0.containerId() && !this.player.isSpectator()) {
         if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", this.player, this.player.containerMenu);
         } else {
            boolean $$1 = this.player.containerMenu.clickMenuButton(this.player, $$0.buttonId());
            if ($$1) {
               this.player.containerMenu.broadcastChanges();
            }
         }
      }
   }

   public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.hasInfiniteMaterials()) {
         boolean $$1 = $$0.slotNum() < 0;
         ItemStack $$2 = $$0.itemStack();
         if (!$$2.isItemEnabled(this.player.level().enabledFeatures())) {
            return;
         }

         boolean $$3 = $$0.slotNum() >= 1 && $$0.slotNum() <= 45;
         boolean $$4 = $$2.isEmpty() || $$2.getCount() <= $$2.getMaxStackSize();
         if ($$3 && $$4) {
            this.player.inventoryMenu.getSlot($$0.slotNum()).setByPlayer($$2);
            this.player.inventoryMenu.setRemoteSlot($$0.slotNum(), $$2);
            this.player.inventoryMenu.broadcastChanges();
         } else if ($$1 && $$4) {
            if (this.dropSpamThrottler.isUnderThreshold()) {
               this.dropSpamThrottler.increment();
               this.player.drop($$2, true);
            } else {
               LOGGER.warn("Player {} was dropping items too fast in creative mode, ignoring.", this.player.getPlainTextName());
            }
         }
      }
   }

   public void handleSignUpdate(ServerboundSignUpdatePacket $$0) {
      List<String> $$1 = Stream.of($$0.getLines()).<String>map(ChatFormatting::stripFormatting).collect(Collectors.toList());
      this.filterTextPacket($$1).thenAcceptAsync($$1x -> this.updateSignText($$0, $$1x), this.server);
   }

   private void updateSignText(ServerboundSignUpdatePacket $$0, List<FilteredText> $$1) {
      this.player.resetLastActionTime();
      ServerLevel $$2 = this.player.level();
      BlockPos $$3 = $$0.getPos();
      if ($$2.hasChunkAt($$3)) {
         if (!($$2.getBlockEntity($$3) instanceof SignBlockEntity $$5)) {
            return;
         }

         $$5.updateSignText(this.player, $$0.isFrontText(), $$1);
      }
   }

   public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.getAbilities().flying = $$0.isFlying() && this.player.getAbilities().mayfly;
   }

   public void handleClientInformation(ServerboundClientInformationPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      boolean $$1 = this.player.isModelPartShown(PlayerModelPart.HAT);
      this.player.updateOptions($$0.information());
      if (this.player.isModelPartShown(PlayerModelPart.HAT) != $$1) {
         this.server
            .getPlayerList()
            .broadcastAll(
               new ClientboundPlayerInfoUpdatePacket(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT, this.player)
            );
      }
   }

   public void handleChangeDifficulty(ServerboundChangeDifficultyPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && !this.isSingleplayerOwner()) {
         LOGGER.warn(
            "Player {} tried to change difficulty to {} without required permissions", this.player.getGameProfile().name(), $$0.difficulty().getDisplayName()
         );
      } else {
         this.server.setDifficulty($$0.difficulty(), false);
      }
   }

   public void handleChangeGameMode(ServerboundChangeGameModePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!GameModeCommand.PERMISSION_CHECK.check(this.player.permissions())) {
         LOGGER.warn(
            "Player {} tried to change game mode to {} without required permissions",
            this.player.getGameProfile().name(),
            $$0.mode().getShortDisplayName().getString()
         );
      } else {
         GameModeCommand.setGameMode(this.player, $$0.mode());
      }
   }

   public void handleLockDifficulty(ServerboundLockDifficultyPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) || this.isSingleplayerOwner()) {
         this.server.setDifficultyLocked($$0.isLocked());
      }
   }

   public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      Data $$1 = $$0.chatSession();
      net.minecraft.world.entity.player.ProfilePublicKey.Data $$2 = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
      net.minecraft.world.entity.player.ProfilePublicKey.Data $$3 = $$1.profilePublicKey();
      if (!Objects.equals($$2, $$3)) {
         if ($$2 != null && $$3.expiresAt().isBefore($$2.expiresAt())) {
            this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
         } else {
            try {
               SignatureValidator $$4 = this.server.services().profileKeySignatureValidator();
               if ($$4 == null) {
                  LOGGER.warn("Ignoring chat session from {} due to missing Services public key", this.player.getGameProfile().name());
                  return;
               }

               this.resetPlayerChatState($$1.validate(this.player.getGameProfile(), $$4));
            } catch (net.minecraft.world.entity.player.ProfilePublicKey.ValidationException var6) {
               LOGGER.error("Failed to validate profile key: {}", var6.getMessage());
               this.disconnect(var6.getComponent());
            }
         }
      }
   }

   public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket $$0) {
      if (!this.waitingForSwitchToConfig) {
         throw new IllegalStateException("Client acknowledged config, but none was requested");
      } else {
         this.connection
            .setupInboundProtocol(
               ConfigurationProtocols.SERVERBOUND,
               new ServerConfigurationPacketListenerImpl(this.server, this.connection, this.createCookie(this.player.clientInformation()))
            );
      }
   }

   public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.chunkSender.onChunkBatchReceivedByClient($$0.desiredChunksPerTick());
   }

   public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      this.player.requestDebugSubscriptions($$0.subscriptions());
   }

   private void resetPlayerChatState(RemoteChatSession $$0) {
      this.chatSession = $$0;
      this.signedMessageDecoder = $$0.createMessageDecoder(this.player.getUUID());
      this.chatMessageChain
         .append(
            () -> {
               this.player.setChatSession($$0);
               this.server
                  .getPlayerList()
                  .broadcastAll(
                     new ClientboundPlayerInfoUpdatePacket(
                        EnumSet.of(net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)
                     )
                  );
            }
         );
   }

   @Override
   public void handleCustomPayload(ServerboundCustomPayloadPacket $$0) {
   }

   public void handleClientTickEnd(ServerboundClientTickEndPacket $$0) {
      PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
      if (!this.receivedMovementThisTick) {
         this.player.setKnownMovement(Vec3.ZERO);
      }

      this.receivedMovementThisTick = false;
   }

   private void handlePlayerKnownMovement(Vec3 $$0) {
      if ($$0.lengthSqr() > 1.0E-5F) {
         this.player.resetLastActionTime();
      }

      this.player.setKnownMovement($$0);
      this.receivedMovementThisTick = true;
   }

   public boolean hasInfiniteMaterials() {
      return this.player.hasInfiniteMaterials();
   }

   @Override
   public ServerPlayer getPlayer() {
      return this.player;
   }

   public boolean hasClientLoaded() {
      return !this.waitingForRespawn && this.clientLoadedTimeoutTimer <= 0;
   }

   public void tickClientLoadTimeout() {
      if (this.clientLoadedTimeoutTimer > 0) {
         this.clientLoadedTimeoutTimer--;
      }
   }

   private void markClientLoaded() {
      this.clientLoadedTimeoutTimer = 0;
   }

   public void markClientUnloadedAfterDeath() {
      this.waitingForRespawn = true;
   }

   private void restartClientLoadTimerAfterRespawn() {
      this.waitingForRespawn = false;
      this.clientLoadedTimeoutTimer = 60;
   }

   @FunctionalInterface
   interface EntityInteraction {
      InteractionResult run(ServerPlayer var1, Entity var2, InteractionHand var3);
   }
}
