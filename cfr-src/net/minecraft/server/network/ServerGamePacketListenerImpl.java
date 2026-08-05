/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.primitives.Floats
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.ParseResults
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.SocketAddress;
import java.util.ArrayList;
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
import java.util.concurrent.Executor;
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
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignableCommand;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
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
import net.minecraft.network.protocol.game.GameProtocols;
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
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.Filterable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.FutureChain;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.StringUtil;
import net.minecraft.util.TickThrottler;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.CrafterMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
implements GameProtocols.Context,
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
    private @Nullable Entity lastVehicle;
    private double vehicleFirstGoodX;
    private double vehicleFirstGoodY;
    private double vehicleFirstGoodZ;
    private double vehicleLastGoodX;
    private double vehicleLastGoodY;
    private double vehicleLastGoodZ;
    private @Nullable Vec3 awaitingPositionFromClient;
    private int awaitingTeleport;
    private int awaitingTeleportTime;
    private boolean clientIsFloating;
    private int aboveGroundTickCount;
    private boolean clientVehicleIsFloating;
    private int aboveGroundVehicleTickCount;
    private int receivedMovePacketCount;
    private int knownMovePacketCount;
    private boolean receivedMovementThisTick;
    private @Nullable RemoteChatSession chatSession;
    private SignedMessageChain.Decoder signedMessageDecoder;
    private final LastSeenMessagesValidator lastSeenMessages = new LastSeenMessagesValidator(20);
    private int nextChatIndex;
    private final MessageSignatureCache messageSignatureCache = MessageSignatureCache.createDefault();
    private final FutureChain chatMessageChain;
    private boolean waitingForSwitchToConfig;
    private boolean waitingForRespawn;
    private int clientLoadedTimeoutTimer;

    public ServerGamePacketListenerImpl(MinecraftServer $$0, Connection $$1, ServerPlayer $$2, CommonListenerCookie $$3) {
        super($$0, $$1, $$3);
        this.restartClientLoadTimerAfterRespawn();
        this.chunkSender = new PlayerChunkSender($$1.isMemoryConnection());
        this.player = $$2;
        $$2.connection = this;
        $$2.getTextFilter().join();
        this.signedMessageDecoder = SignedMessageChain.Decoder.unsigned($$2.getUUID(), $$0::enforceSecureProfile);
        this.chatMessageChain = new FutureChain($$0);
    }

    @Override
    public void tick() {
        if (this.ackBlockChangesUpTo > -1) {
            this.send(new ClientboundBlockChangedAckPacket(this.ackBlockChangesUpTo));
            this.ackBlockChangesUpTo = -1;
        }
        if (!this.server.isPaused() && this.tickPlayer()) {
            return;
        }
        this.keepConnectionAlive();
        this.chatSpamThrottler.tick();
        this.dropSpamThrottler.tick();
        if (this.player.getLastActionTime() > 0L && this.server.playerIdleTimeout() > 0 && Util.getMillis() - this.player.getLastActionTime() > TimeUnit.MINUTES.toMillis(this.server.playerIdleTimeout()) && !this.player.wonGame) {
            this.disconnect(Component.translatable("multiplayer.disconnect.idling"));
        }
    }

    private boolean tickPlayer() {
        this.resetPosition();
        this.player.xo = this.player.getX();
        this.player.yo = this.player.getY();
        this.player.zo = this.player.getZ();
        this.player.doTick();
        this.player.absSnapTo(this.firstGoodX, this.firstGoodY, this.firstGoodZ, this.player.getYRot(), this.player.getXRot());
        ++this.tickCount;
        this.knownMovePacketCount = this.receivedMovePacketCount;
        if (this.clientIsFloating && !this.player.isSleeping() && !this.player.isPassenger() && !this.player.isDeadOrDying()) {
            if (++this.aboveGroundTickCount > this.getMaximumFlyingTicks(this.player)) {
                LOGGER.warn("{} was kicked for floating too long!", (Object)this.player.getPlainTextName());
                this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                return true;
            }
        } else {
            this.clientIsFloating = false;
            this.aboveGroundTickCount = 0;
        }
        this.lastVehicle = this.player.getRootVehicle();
        if (this.lastVehicle == this.player || this.lastVehicle.getControllingPassenger() != this.player) {
            this.lastVehicle = null;
            this.clientVehicleIsFloating = false;
            this.aboveGroundVehicleTickCount = 0;
        } else {
            this.vehicleFirstGoodX = this.lastVehicle.getX();
            this.vehicleFirstGoodY = this.lastVehicle.getY();
            this.vehicleFirstGoodZ = this.lastVehicle.getZ();
            this.vehicleLastGoodX = this.lastVehicle.getX();
            this.vehicleLastGoodY = this.lastVehicle.getY();
            this.vehicleLastGoodZ = this.lastVehicle.getZ();
            if (this.clientVehicleIsFloating && this.lastVehicle.getControllingPassenger() == this.player) {
                if (++this.aboveGroundVehicleTickCount > this.getMaximumFlyingTicks(this.lastVehicle)) {
                    LOGGER.warn("{} was kicked for floating a vehicle too long!", (Object)this.player.getPlainTextName());
                    this.disconnect(Component.translatable("multiplayer.disconnect.flying"));
                    return true;
                }
            } else {
                this.clientVehicleIsFloating = false;
                this.aboveGroundVehicleTickCount = 0;
            }
        }
        return false;
    }

    private int getMaximumFlyingTicks(Entity $$0) {
        double $$1 = $$0.getGravity();
        if ($$1 < (double)1.0E-5f) {
            return Integer.MAX_VALUE;
        }
        double $$2 = 0.08 / $$1;
        return Mth.ceil(80.0 * Math.max($$2, 1.0));
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

    @Override
    public boolean isAcceptingMessages() {
        return this.connection.isConnected() && !this.waitingForSwitchToConfig;
    }

    @Override
    public boolean shouldHandleMessage(Packet<?> $$0) {
        if (super.shouldHandleMessage($$0)) {
            return true;
        }
        return this.waitingForSwitchToConfig && this.connection.isConnected() && $$0 instanceof ServerboundConfigurationAcknowledgedPacket;
    }

    @Override
    protected GameProfile playerProfile() {
        return this.player.getGameProfile();
    }

    private <T, R> CompletableFuture<R> filterTextPacket(T $$02, BiFunction<TextFilter, T, CompletableFuture<R>> $$1) {
        return $$1.apply(this.player.getTextFilter(), (TextFilter)$$02).thenApply($$0 -> {
            if (!this.isAcceptingMessages()) {
                LOGGER.debug("Ignoring packet due to disconnection");
                throw new CancellationException("disconnected");
            }
            return $$0;
        });
    }

    private CompletableFuture<FilteredText> filterTextPacket(String $$0) {
        return this.filterTextPacket($$0, TextFilter::processStreamMessage);
    }

    private CompletableFuture<List<FilteredText>> filterTextPacket(List<String> $$0) {
        return this.filterTextPacket($$0, TextFilter::processMessageBundle);
    }

    @Override
    public void handlePlayerInput(ServerboundPlayerInputPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.setLastClientInput($$0.input());
        if (this.hasClientLoaded()) {
            this.player.resetLastActionTime();
            this.player.setShiftKeyDown($$0.input().shift());
        }
    }

    private static boolean containsInvalidValues(double $$0, double $$1, double $$2, float $$3, float $$4) {
        return Double.isNaN($$0) || Double.isNaN($$1) || Double.isNaN($$2) || !Floats.isFinite((float)$$4) || !Floats.isFinite((float)$$3);
    }

    private static double clampHorizontal(double $$0) {
        return Mth.clamp($$0, -3.0E7, 3.0E7);
    }

    private static double clampVertical(double $$0) {
        return Mth.clamp($$0, -2.0E7, 2.0E7);
    }

    @Override
    public void handleMoveVehicle(ServerboundMoveVehiclePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (ServerGamePacketListenerImpl.containsInvalidValues($$0.position().x(), $$0.position().y(), $$0.position().z(), $$0.yRot(), $$0.xRot())) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
            return;
        }
        if (this.updateAwaitingTeleport() || !this.hasClientLoaded()) {
            return;
        }
        Entity $$1 = this.player.getRootVehicle();
        if ($$1 != this.player && $$1.getControllingPassenger() == this.player && $$1 == this.lastVehicle) {
            LivingEntity $$18;
            ServerLevel $$2 = this.player.level();
            double $$3 = $$1.getX();
            double $$4 = $$1.getY();
            double $$5 = $$1.getZ();
            double $$6 = ServerGamePacketListenerImpl.clampHorizontal($$0.position().x());
            double $$7 = ServerGamePacketListenerImpl.clampVertical($$0.position().y());
            double $$8 = ServerGamePacketListenerImpl.clampHorizontal($$0.position().z());
            float $$9 = Mth.wrapDegrees($$0.yRot());
            float $$10 = Mth.wrapDegrees($$0.xRot());
            double $$11 = $$6 - this.vehicleFirstGoodX;
            double $$12 = $$7 - this.vehicleFirstGoodY;
            double $$13 = $$8 - this.vehicleFirstGoodZ;
            double $$15 = $$11 * $$11 + $$12 * $$12 + $$13 * $$13;
            double $$14 = $$1.getDeltaMovement().lengthSqr();
            if ($$15 - $$14 > 100.0 && !this.isSingleplayerOwner()) {
                LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}", new Object[]{$$1.getPlainTextName(), this.player.getPlainTextName(), $$11, $$12, $$13});
                this.send(ClientboundMoveVehiclePacket.fromEntity($$1));
                return;
            }
            AABB $$16 = $$1.getBoundingBox();
            $$11 = $$6 - this.vehicleLastGoodX;
            $$12 = $$7 - this.vehicleLastGoodY;
            $$13 = $$8 - this.vehicleLastGoodZ;
            boolean $$17 = $$1.verticalCollisionBelow;
            if ($$1 instanceof LivingEntity && ($$18 = (LivingEntity)$$1).onClimbable()) {
                $$18.resetFallDistance();
            }
            $$1.move(MoverType.PLAYER, new Vec3($$11, $$12, $$13));
            double $$19 = $$12;
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
            this.clientVehicleIsFloating = $$19 >= -0.03125 && !$$17 && !this.server.allowFlight() && !$$1.isFlyingVehicle() && !$$1.isNoGravity() && this.noBlocksAround($$1);
            this.vehicleLastGoodX = $$1.getX();
            this.vehicleLastGoodY = $$1.getY();
            this.vehicleLastGoodZ = $$1.getZ();
        }
    }

    private boolean noBlocksAround(Entity $$0) {
        return $$0.level().getBlockStates($$0.getBoundingBox().inflate(0.0625).expandTowards(0.0, -0.55, 0.0)).allMatch(BlockBehaviour.BlockStateBase::isAir);
    }

    @Override
    public void handleAcceptTeleportPacket(ServerboundAcceptTeleportationPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if ($$0.getId() == this.awaitingTeleport) {
            if (this.awaitingPositionFromClient == null) {
                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
                return;
            }
            this.player.absSnapTo(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            this.lastGoodX = this.awaitingPositionFromClient.x;
            this.lastGoodY = this.awaitingPositionFromClient.y;
            this.lastGoodZ = this.awaitingPositionFromClient.z;
            this.player.hasChangedDimension();
            this.awaitingPositionFromClient = null;
        }
    }

    @Override
    public void handleAcceptPlayerLoad(ServerboundPlayerLoadedPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.markClientLoaded();
    }

    @Override
    public void handleRecipeBookSeenRecipePacket(ServerboundRecipeBookSeenRecipePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        RecipeManager.ServerDisplayInfo $$1 = this.server.getRecipeManager().getRecipeFromDisplay($$0.recipe());
        if ($$1 != null) {
            this.player.getRecipeBook().removeHighlight($$1.parent().id());
        }
    }

    @Override
    public void handleBundleItemSelectedPacket(ServerboundSelectBundleItemPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.containerMenu.setSelectedBundleItemIndex($$0.slotId(), $$0.selectedItemIndex());
    }

    @Override
    public void handleRecipeBookChangeSettingsPacket(ServerboundRecipeBookChangeSettingsPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.getRecipeBook().setBookSetting($$0.getBookType(), $$0.isOpen(), $$0.isFiltering());
    }

    @Override
    public void handleSeenAdvancements(ServerboundSeenAdvancementsPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if ($$0.getAction() == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
            Identifier $$1 = Objects.requireNonNull($$0.getTab());
            AdvancementHolder $$2 = this.server.getAdvancements().get($$1);
            if ($$2 != null) {
                this.player.getAdvancements().setSelectedTab($$2);
            }
        }
    }

    @Override
    public void handleCustomCommandSuggestions(ServerboundCommandSuggestionPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        StringReader $$12 = new StringReader($$0.getCommand());
        if ($$12.canRead() && $$12.peek() == '/') {
            $$12.skip();
        }
        ParseResults $$2 = this.server.getCommands().getDispatcher().parse($$12, (Object)this.player.createCommandSourceStack());
        this.server.getCommands().getDispatcher().getCompletionSuggestions($$2).thenAccept($$1 -> {
            Suggestions $$2 = $$1.getList().size() <= 1000 ? $$1 : new Suggestions($$1.getRange(), $$1.getList().subList(0, 1000));
            this.send(new ClientboundCommandSuggestionsPacket($$0.getId(), $$2));
        });
    }

    @Override
    public void handleSetCommandBlock(ServerboundSetCommandBlockPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
            return;
        }
        BaseCommandBlock $$1 = null;
        CommandBlockEntity $$2 = null;
        BlockPos $$3 = $$0.getPos();
        BlockEntity $$4 = this.player.level().getBlockEntity($$3);
        if ($$4 instanceof CommandBlockEntity) {
            CommandBlockEntity $$5;
            $$2 = $$5 = (CommandBlockEntity)$$4;
            $$1 = $$2.getCommandBlock();
        }
        String $$6 = $$0.getCommand();
        boolean $$7 = $$0.isTrackOutput();
        if ($$1 != null) {
            CommandBlockEntity.Mode $$8 = $$2.getMode();
            BlockState $$9 = this.player.level().getBlockState($$3);
            Direction $$10 = $$9.getValue(CommandBlock.FACING);
            BlockState $$11 = switch ($$0.getMode()) {
                case CommandBlockEntity.Mode.SEQUENCE -> Blocks.CHAIN_COMMAND_BLOCK.defaultBlockState();
                case CommandBlockEntity.Mode.AUTO -> Blocks.REPEATING_COMMAND_BLOCK.defaultBlockState();
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
                this.player.sendSystemMessage(Component.translatable(this.player.level().isCommandBlockEnabled() ? "advMode.setCommand.success" : "advMode.setCommand.disabled", $$6));
            }
        }
    }

    @Override
    public void handleSetCommandMinecart(ServerboundSetCommandMinecartPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            this.player.sendSystemMessage(Component.translatable("advMode.notAllowed"));
            return;
        }
        BaseCommandBlock $$1 = $$0.getCommandBlock(this.player.level());
        if ($$1 != null) {
            boolean $$3;
            String $$2 = $$0.getCommand();
            $$1.setCommand($$2);
            $$1.setTrackOutput($$0.isTrackOutput());
            if (!$$0.isTrackOutput()) {
                $$1.setLastOutput(null);
            }
            if ($$3 = this.player.level().isCommandBlockEnabled()) {
                $$1.onUpdated(this.player.level());
            }
            if (!StringUtil.isNullOrEmpty($$2)) {
                this.player.sendSystemMessage(Component.translatable($$3 ? "advMode.setCommand.success" : "advMode.setCommand.disabled", $$2));
            }
        }
    }

    @Override
    public void handlePickItemFromBlock(ServerboundPickItemFromBlockPacket $$0) {
        boolean $$4;
        ServerLevel $$1 = this.player.level();
        PacketUtils.ensureRunningOnSameThread($$0, this, $$1);
        BlockPos $$2 = $$0.pos();
        if (!this.player.isWithinBlockInteractionRange($$2, 1.0)) {
            return;
        }
        if (!$$1.isLoaded($$2)) {
            return;
        }
        BlockState $$3 = $$1.getBlockState($$2);
        ItemStack $$5 = $$3.getCloneItemStack($$1, $$2, $$4 = this.player.hasInfiniteMaterials() && $$0.includeData());
        if ($$5.isEmpty()) {
            return;
        }
        if ($$4) {
            ServerGamePacketListenerImpl.addBlockDataToItem($$3, $$1, $$2, $$5);
        }
        this.tryPickItem($$5);
    }

    private static void addBlockDataToItem(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3) {
        BlockEntity $$4;
        BlockEntity blockEntity = $$4 = $$0.hasBlockEntity() ? $$1.getBlockEntity($$2) : null;
        if ($$4 != null) {
            try (ProblemReporter.ScopedCollector $$5 = new ProblemReporter.ScopedCollector($$4.problemPath(), LOGGER);){
                TagValueOutput $$6 = TagValueOutput.createWithContext($$5, $$1.registryAccess());
                $$4.saveCustomOnly($$6);
                $$4.removeComponentsFromTag($$6);
                BlockItem.setBlockEntityData($$3, $$4.getType(), $$6);
                $$3.applyComponents($$4.collectComponents());
            }
        }
    }

    @Override
    public void handlePickItemFromEntity(ServerboundPickItemFromEntityPacket $$0) {
        ServerLevel $$1 = this.player.level();
        PacketUtils.ensureRunningOnSameThread($$0, this, $$1);
        Entity $$2 = $$1.getEntityOrPart($$0.id());
        if ($$2 == null || !this.player.isWithinEntityInteractionRange($$2, 3.0)) {
            return;
        }
        ItemStack $$3 = $$2.getPickResult();
        if ($$3 != null && !$$3.isEmpty()) {
            this.tryPickItem($$3);
        }
    }

    private void tryPickItem(ItemStack $$0) {
        if (!$$0.isItemEnabled(this.player.level().enabledFeatures())) {
            return;
        }
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

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof AnvilMenu) {
            AnvilMenu $$1 = (AnvilMenu)abstractContainerMenu;
            if (!$$1.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)$$1);
                return;
            }
            $$1.setItemName($$0.getName());
        }
    }

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof BeaconMenu) {
            BeaconMenu $$1 = (BeaconMenu)abstractContainerMenu;
            if (!this.player.containerMenu.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
                return;
            }
            $$1.updateEffects($$0.primary(), $$0.secondary());
        }
    }

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos $$1 = $$0.getPos();
        BlockState $$2 = this.player.level().getBlockState($$1);
        BlockEntity $$3 = this.player.level().getBlockEntity($$1);
        if ($$3 instanceof StructureBlockEntity) {
            StructureBlockEntity $$4 = (StructureBlockEntity)$$3;
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
                if ($$0.getUpdateType() == StructureBlockEntity.UpdateType.SAVE_AREA) {
                    if ($$4.saveStructure()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.save_success", $$5), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.save_failure", $$5), false);
                    }
                } else if ($$0.getUpdateType() == StructureBlockEntity.UpdateType.LOAD_AREA) {
                    if (!$$4.isStructureLoadable()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_not_found", $$5), false);
                    } else if ($$4.placeStructureIfSameSize(this.player.level())) {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_success", $$5), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.load_prepare", $$5), false);
                    }
                } else if ($$0.getUpdateType() == StructureBlockEntity.UpdateType.SCAN_AREA) {
                    if ($$4.detectSize()) {
                        this.player.displayClientMessage(Component.translatable("structure_block.size_success", $$5), false);
                    } else {
                        this.player.displayClientMessage(Component.translatable("structure_block.size_failure"), false);
                    }
                }
            } else {
                this.player.displayClientMessage(Component.translatable("structure_block.invalid_structure_name", $$0.getName()), false);
            }
            $$4.setChanged();
            this.player.level().sendBlockUpdated($$1, $$2, $$2, 3);
        }
    }

    @Override
    public void handleSetTestBlock(ServerboundSetTestBlockPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos $$1 = $$0.position();
        BlockState $$2 = this.player.level().getBlockState($$1);
        BlockEntity $$3 = this.player.level().getBlockEntity($$1);
        if ($$3 instanceof TestBlockEntity) {
            TestBlockEntity $$4 = (TestBlockEntity)$$3;
            $$4.setMode($$0.mode());
            $$4.setMessage($$0.message());
            $$4.setChanged();
            this.player.level().sendBlockUpdated($$1, $$2, $$4.getBlockState(), 3);
        }
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public void handleTestInstanceBlockAction(ServerboundTestInstanceBlockActionPacket $$02) {
        BlockEntity blockEntity;
        PacketUtils.ensureRunningOnSameThread($$02, this, this.player.level());
        BlockPos $$1 = $$02.pos();
        if (!this.player.canUseGameMasterBlocks() || !((blockEntity = this.player.level().getBlockEntity($$1)) instanceof TestInstanceBlockEntity)) {
            return;
        }
        TestInstanceBlockEntity $$2 = (TestInstanceBlockEntity)blockEntity;
        if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.QUERY || $$02.action() == ServerboundTestInstanceBlockActionPacket.Action.INIT) {
            Optional<Vec3i> $$9;
            MutableComponent $$7;
            HolderLookup.RegistryLookup $$4 = this.player.registryAccess().lookupOrThrow(Registries.TEST_INSTANCE);
            Optional $$5 = $$02.data().test().flatMap(((Registry)$$4)::get);
            if ($$5.isPresent()) {
                Component $$6 = ((GameTestInstance)((Holder.Reference)$$5.get()).value()).describe();
            } else {
                $$7 = Component.translatable("test_instance.description.no_test").withStyle(ChatFormatting.RED);
            }
            if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.QUERY) {
                Optional $$8 = $$02.data().test().flatMap($$0 -> TestInstanceBlockEntity.getStructureSize(this.player.level(), $$0));
            } else {
                $$9 = Optional.empty();
            }
            this.connection.send(new ClientboundTestInstanceBlockStatus($$7, $$9));
        } else {
            void $$3;
            $$3.set($$02.data());
            if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.RESET) {
                $$3.resetTest(this.player::sendSystemMessage);
            } else if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.SAVE) {
                $$3.saveTest(this.player::sendSystemMessage);
            } else if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.EXPORT) {
                $$3.exportTest(this.player::sendSystemMessage);
            } else if ($$02.action() == ServerboundTestInstanceBlockActionPacket.Action.RUN) {
                $$3.runTest(this.player::sendSystemMessage);
            }
            BlockState $$10 = this.player.level().getBlockState($$1);
            this.player.level().sendBlockUpdated($$1, Blocks.AIR.defaultBlockState(), $$10, 3);
        }
    }

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos $$1 = $$0.getPos();
        BlockState $$2 = this.player.level().getBlockState($$1);
        BlockEntity $$3 = this.player.level().getBlockEntity($$1);
        if ($$3 instanceof JigsawBlockEntity) {
            JigsawBlockEntity $$4 = (JigsawBlockEntity)$$3;
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

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.canUseGameMasterBlocks()) {
            return;
        }
        BlockPos $$1 = $$0.getPos();
        BlockEntity $$2 = this.player.level().getBlockEntity($$1);
        if ($$2 instanceof JigsawBlockEntity) {
            JigsawBlockEntity $$3 = (JigsawBlockEntity)$$2;
            $$3.generate(this.player.level(), $$0.levels(), $$0.keepJigsaws());
        }
    }

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        int $$1 = $$0.getItem();
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof MerchantMenu) {
            MerchantMenu $$2 = (MerchantMenu)abstractContainerMenu;
            if (!$$2.stillValid(this.player)) {
                LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)$$2);
                return;
            }
            $$2.setSelectionHint($$1);
            $$2.tryMoveItems($$1);
        }
    }

    @Override
    public void handleEditBook(ServerboundEditBookPacket $$0) {
        int $$12 = $$0.slot();
        if (!Inventory.isHotbarSlot($$12) && $$12 != 40) {
            return;
        }
        ArrayList $$2 = Lists.newArrayList();
        Optional<String> $$3 = $$0.title();
        $$3.ifPresent($$2::add);
        $$2.addAll($$0.pages());
        Consumer<List> $$4 = $$3.isPresent() ? $$1 -> this.signBook((FilteredText)$$1.get(0), $$1.subList(1, $$1.size()), $$12) : $$1 -> this.updateBookContents((List<FilteredText>)$$1, $$12);
        this.filterTextPacket($$2).thenAcceptAsync($$4, (Executor)this.server);
    }

    private void updateBookContents(List<FilteredText> $$0, int $$1) {
        ItemStack $$2 = this.player.getInventory().getItem($$1);
        if (!$$2.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        List<Filterable<String>> $$3 = $$0.stream().map(this::filterableFromOutgoing).toList();
        $$2.set(DataComponents.WRITABLE_BOOK_CONTENT, new WritableBookContent($$3));
    }

    private void signBook(FilteredText $$02, List<FilteredText> $$1, int $$2) {
        ItemStack $$3 = this.player.getInventory().getItem($$2);
        if (!$$3.has(DataComponents.WRITABLE_BOOK_CONTENT)) {
            return;
        }
        ItemStack $$4 = $$3.transmuteCopy(Items.WRITTEN_BOOK);
        $$4.remove(DataComponents.WRITABLE_BOOK_CONTENT);
        List<Filterable<Component>> $$5 = $$1.stream().map($$0 -> this.filterableFromOutgoing((FilteredText)$$0).map(Component::literal)).toList();
        $$4.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(this.filterableFromOutgoing($$02), this.player.getPlainTextName(), 0, $$5, true));
        this.player.getInventory().setItem($$2, $$4);
    }

    private Filterable<String> filterableFromOutgoing(FilteredText $$0) {
        if (this.player.isTextFilteringEnabled()) {
            return Filterable.passThrough($$0.filteredOrEmpty());
        }
        return Filterable.from($$0);
    }

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }
        Entity $$1 = this.player.level().getEntity($$0.getEntityId());
        if ($$1 != null) {
            try (ProblemReporter.ScopedCollector $$2 = new ProblemReporter.ScopedCollector($$1.problemPath(), LOGGER);){
                TagValueOutput $$3 = TagValueOutput.createWithContext($$2, $$1.registryAccess());
                $$1.saveWithoutId($$3);
                CompoundTag $$4 = $$3.buildResult();
                this.send(new ClientboundTagQueryPacket($$0.getTransactionId(), $$4));
            }
        }
    }

    @Override
    public void handleContainerSlotStateChanged(ServerboundContainerSlotStateChangedPacket $$0) {
        CrafterMenu $$1;
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (this.player.isSpectator() || $$0.containerId() != this.player.containerMenu.containerId) {
            return;
        }
        Object object = this.player.containerMenu;
        if (object instanceof CrafterMenu && (object = ($$1 = (CrafterMenu)object).getContainer()) instanceof CrafterBlockEntity) {
            CrafterBlockEntity $$2 = (CrafterBlockEntity)object;
            $$2.setSlotState($$0.slotId(), $$0.newState());
        }
    }

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }
        BlockEntity $$1 = this.player.level().getBlockEntity($$0.getPos());
        CompoundTag $$2 = $$1 != null ? $$1.saveWithoutMetadata(this.player.registryAccess()) : null;
        this.send(new ClientboundTagQueryPacket($$0.getTransactionId(), $$2));
    }

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket $$0) {
        boolean $$19;
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (ServerGamePacketListenerImpl.containsInvalidValues($$0.getX(0.0), $$0.getY(0.0), $$0.getZ(0.0), $$0.getYRot(0.0f), $$0.getXRot(0.0f))) {
            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
            return;
        }
        ServerLevel $$1 = this.player.level();
        if (this.player.wonGame) {
            return;
        }
        if (this.tickCount == 0) {
            this.resetPosition();
        }
        if (!this.hasClientLoaded()) {
            return;
        }
        float $$2 = Mth.wrapDegrees($$0.getYRot(this.player.getYRot()));
        float $$3 = Mth.wrapDegrees($$0.getXRot(this.player.getXRot()));
        if (this.updateAwaitingTeleport()) {
            this.player.absSnapRotationTo($$2, $$3);
            return;
        }
        double $$4 = ServerGamePacketListenerImpl.clampHorizontal($$0.getX(this.player.getX()));
        double $$5 = ServerGamePacketListenerImpl.clampVertical($$0.getY(this.player.getY()));
        double $$6 = ServerGamePacketListenerImpl.clampHorizontal($$0.getZ(this.player.getZ()));
        if (this.player.isPassenger()) {
            this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), $$2, $$3);
            this.player.level().getChunkSource().move(this.player);
            return;
        }
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
            return;
        }
        boolean $$15 = this.player.isFallFlying();
        if ($$1.tickRateManager().runsNormally()) {
            ++this.receivedMovePacketCount;
            int $$16 = this.receivedMovePacketCount - this.knownMovePacketCount;
            if ($$16 > 5) {
                LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", (Object)this.player.getPlainTextName(), (Object)$$16);
                $$16 = 1;
            }
            if (this.shouldCheckPlayerMovement($$15)) {
                float $$17;
                float f = $$17 = $$15 ? 300.0f : 100.0f;
                if ($$14 - $$13 > (double)($$17 * (float)$$16)) {
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
        boolean bl = $$19 = $$11 > 0.0;
        if (this.player.onGround() && !$$0.isOnGround() && $$19) {
            this.player.jumpFromGround();
        }
        boolean $$20 = this.player.verticalCollisionBelow;
        this.player.move(MoverType.PLAYER, new Vec3($$10, $$11, $$12));
        double $$21 = $$11;
        $$10 = $$4 - this.player.getX();
        $$11 = $$5 - this.player.getY();
        if ($$11 > -0.5 || $$11 < 0.5) {
            $$11 = 0.0;
        }
        $$12 = $$6 - this.player.getZ();
        $$14 = $$10 * $$10 + $$11 * $$11 + $$12 * $$12;
        boolean $$22 = false;
        if (!(this.player.isChangingDimension() || !($$14 > 0.0625) || this.player.isSleeping() || this.player.isCreative() || this.player.isSpectator() || this.player.isInPostImpulseGraceTime())) {
            $$22 = true;
            LOGGER.warn("{} moved wrongly!", (Object)this.player.getPlainTextName());
        }
        if (!this.player.noPhysics && !this.player.isSleeping() && ($$22 && $$1.noCollision(this.player, $$18) || this.isEntityCollidingWithAnythingNew($$1, this.player, $$18, $$4, $$5, $$6))) {
            this.teleport($$7, $$8, $$9, $$2, $$3);
            this.player.doCheckFallDamage(this.player.getX() - $$7, this.player.getY() - $$8, this.player.getZ() - $$9, $$0.isOnGround());
            this.player.removeLatestMovementRecording();
            return;
        }
        this.player.absSnapTo($$4, $$5, $$6, $$2, $$3);
        boolean $$23 = this.player.isAutoSpinAttack();
        this.clientIsFloating = $$21 >= -0.03125 && !$$20 && !this.player.isSpectator() && !this.server.allowFlight() && !this.player.getAbilities().mayfly && !this.player.hasEffect(MobEffects.LEVITATION) && !$$15 && !$$23 && this.noBlocksAround(this.player);
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
    }

    private boolean shouldCheckPlayerMovement(boolean $$0) {
        if (this.isSingleplayerOwner()) {
            return false;
        }
        if (this.player.isChangingDimension()) {
            return false;
        }
        GameRules $$1 = this.player.level().getGameRules();
        if (!$$1.get(GameRules.PLAYER_MOVEMENT_CHECK).booleanValue()) {
            return false;
        }
        return !$$0 || $$1.get(GameRules.ELYTRA_MOVEMENT_CHECK) != false;
    }

    private boolean updateAwaitingTeleport() {
        if (this.awaitingPositionFromClient != null) {
            if (this.tickCount - this.awaitingTeleportTime > 20) {
                this.awaitingTeleportTime = this.tickCount;
                this.teleport(this.awaitingPositionFromClient.x, this.awaitingPositionFromClient.y, this.awaitingPositionFromClient.z, this.player.getYRot(), this.player.getXRot());
            }
            return true;
        }
        this.awaitingTeleportTime = this.tickCount;
        return false;
    }

    private boolean isEntityCollidingWithAnythingNew(LevelReader $$0, Entity $$1, AABB $$2, double $$3, double $$4, double $$5) {
        AABB $$6 = $$1.getBoundingBox().move($$3 - $$1.getX(), $$4 - $$1.getY(), $$5 - $$1.getZ());
        Iterable<VoxelShape> $$7 = $$0.getPreMoveCollisions($$1, $$6.deflate(1.0E-5f), $$2.getBottomCenter());
        VoxelShape $$8 = Shapes.create($$2.deflate(1.0E-5f));
        for (VoxelShape $$9 : $$7) {
            if (Shapes.joinIsNotEmpty($$9, $$8, BooleanOp.AND)) continue;
            return true;
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

    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.hasClientLoaded()) {
            return;
        }
        BlockPos $$1 = $$0.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action $$2 = $$0.getAction();
        switch ($$2) {
            case STAB: {
                if (this.player.isSpectator()) {
                    return;
                }
                ItemStack $$3 = this.player.getItemInHand(InteractionHand.MAIN_HAND);
                if (this.player.cannotAttackWithItem($$3, 5)) {
                    return;
                }
                PiercingWeapon $$4 = $$3.get(DataComponents.PIERCING_WEAPON);
                if ($$4 != null) {
                    $$4.attack(this.player, EquipmentSlot.MAINHAND);
                }
                return;
            }
            case SWAP_ITEM_WITH_OFFHAND: {
                if (!this.player.isSpectator()) {
                    ItemStack $$5 = this.player.getItemInHand(InteractionHand.OFF_HAND);
                    this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                    this.player.setItemInHand(InteractionHand.MAIN_HAND, $$5);
                    this.player.stopUsingItem();
                }
                return;
            }
            case DROP_ITEM: {
                if (!this.player.isSpectator()) {
                    this.player.drop(false);
                }
                return;
            }
            case DROP_ALL_ITEMS: {
                if (!this.player.isSpectator()) {
                    this.player.drop(true);
                }
                return;
            }
            case RELEASE_USE_ITEM: {
                this.player.releaseUsingItem();
                return;
            }
            case START_DESTROY_BLOCK: 
            case ABORT_DESTROY_BLOCK: 
            case STOP_DESTROY_BLOCK: {
                this.player.gameMode.handleBlockBreakAction($$1, $$2, $$0.getDirection(), this.player.level().getMaxY(), $$0.getSequence());
                this.ackBlockChangesUpTo($$0.getSequence());
                return;
            }
        }
        throw new IllegalArgumentException("Invalid player action");
    }

    private static boolean wasBlockPlacementAttempt(ServerPlayer $$0, ItemStack $$1) {
        BucketItem $$3;
        if ($$1.isEmpty()) {
            return false;
        }
        Item $$2 = $$1.getItem();
        return ($$2 instanceof BlockItem || $$2 instanceof BucketItem && ($$3 = (BucketItem)$$2).getContent() != Fluids.EMPTY) && !$$0.getCooldowns().isOnCooldown($$1);
    }

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.hasClientLoaded()) {
            return;
        }
        this.ackBlockChangesUpTo($$0.getSequence());
        ServerLevel $$1 = this.player.level();
        InteractionHand $$2 = $$0.getHand();
        ItemStack $$3 = this.player.getItemInHand($$2);
        if (!$$3.isItemEnabled($$1.enabledFeatures())) {
            return;
        }
        BlockHitResult $$4 = $$0.getHitResult();
        Vec3 $$5 = $$4.getLocation();
        BlockPos $$6 = $$4.getBlockPos();
        if (!this.player.isWithinBlockInteractionRange($$6, 1.0)) {
            return;
        }
        Vec3 $$7 = $$5.subtract(Vec3.atCenterOf($$6));
        double $$8 = 1.0000001;
        if (!(Math.abs($$7.x()) < 1.0000001 && Math.abs($$7.y()) < 1.0000001 && Math.abs($$7.z()) < 1.0000001)) {
            LOGGER.warn("Rejecting UseItemOnPacket from {}: Location {} too far away from hit block {}.", new Object[]{this.player.getGameProfile().name(), $$5, $$6});
            return;
        }
        Direction $$9 = $$4.getDirection();
        this.player.resetLastActionTime();
        int $$10 = this.player.level().getMaxY();
        if ($$6.getY() <= $$10) {
            if (this.awaitingPositionFromClient == null && $$1.mayInteract(this.player, $$6)) {
                InteractionResult.Success $$13;
                InteractionResult $$11 = this.player.gameMode.useItemOn(this.player, $$1, $$3, $$2, $$4);
                if ($$11.consumesAction()) {
                    CriteriaTriggers.ANY_BLOCK_USE.trigger(this.player, $$4.getBlockPos(), $$3.copy());
                }
                if ($$9 == Direction.UP && !$$11.consumesAction() && $$6.getY() >= $$10 && ServerGamePacketListenerImpl.wasBlockPlacementAttempt(this.player, $$3)) {
                    MutableComponent $$12 = Component.translatable("build.tooHigh", $$10).withStyle(ChatFormatting.RED);
                    this.player.sendSystemMessage($$12, true);
                } else if ($$11 instanceof InteractionResult.Success && ($$13 = (InteractionResult.Success)$$11).swingSource() == InteractionResult.SwingSource.SERVER) {
                    this.player.swing($$2, true);
                }
            }
        } else {
            MutableComponent $$14 = Component.translatable("build.tooHigh", $$10).withStyle(ChatFormatting.RED);
            this.player.sendSystemMessage($$14, true);
        }
        this.send(new ClientboundBlockUpdatePacket($$1, $$6));
        this.send(new ClientboundBlockUpdatePacket($$1, $$6.relative($$9)));
    }

    @Override
    public void handleUseItem(ServerboundUseItemPacket $$0) {
        InteractionResult.Success $$7;
        InteractionResult $$6;
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.hasClientLoaded()) {
            return;
        }
        this.ackBlockChangesUpTo($$0.getSequence());
        ServerLevel $$1 = this.player.level();
        InteractionHand $$2 = $$0.getHand();
        ItemStack $$3 = this.player.getItemInHand($$2);
        this.player.resetLastActionTime();
        if ($$3.isEmpty() || !$$3.isItemEnabled($$1.enabledFeatures())) {
            return;
        }
        float $$4 = Mth.wrapDegrees($$0.getYRot());
        float $$5 = Mth.wrapDegrees($$0.getXRot());
        if ($$5 != this.player.getXRot() || $$4 != this.player.getYRot()) {
            this.player.absSnapRotationTo($$4, $$5);
        }
        if (($$6 = this.player.gameMode.useItem(this.player, $$1, $$3, $$2)) instanceof InteractionResult.Success && ($$7 = (InteractionResult.Success)$$6).swingSource() == InteractionResult.SwingSource.SERVER) {
            this.player.swing($$2, true);
        }
    }

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (this.player.isSpectator()) {
            for (ServerLevel $$1 : this.server.getAllLevels()) {
                Entity $$2 = $$0.getEntity($$1);
                if ($$2 == null) continue;
                this.player.teleportTo($$1, $$2.getX(), $$2.getY(), $$2.getZ(), Set.of(), $$2.getYRot(), $$2.getXRot(), true);
                return;
            }
        }
    }

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        Entity $$1 = this.player.getControlledVehicle();
        if ($$1 instanceof AbstractBoat) {
            AbstractBoat $$2 = (AbstractBoat)$$1;
            $$2.setPaddleState($$0.getLeft(), $$0.getRight());
        }
    }

    @Override
    public void onDisconnect(DisconnectionDetails $$0) {
        LOGGER.info("{} lost connection: {}", (Object)this.player.getPlainTextName(), (Object)$$0.reason().getString());
        this.removePlayerFromWorld();
        super.onDisconnect($$0);
    }

    private void removePlayerFromWorld() {
        this.chatMessageChain.close();
        this.server.invalidateStatus();
        this.server.getPlayerList().broadcastSystemMessage(Component.translatable("multiplayer.player.left", this.player.getDisplayName()).withStyle(ChatFormatting.YELLOW), false);
        this.player.disconnect();
        this.server.getPlayerList().remove(this.player);
        this.player.getTextFilter().leave();
    }

    public void ackBlockChangesUpTo(int $$0) {
        if ($$0 < 0) {
            throw new IllegalArgumentException("Expected packet sequence nr >= 0");
        }
        this.ackBlockChangesUpTo = Math.max($$0, this.ackBlockChangesUpTo);
    }

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if ($$0.getSlot() < 0 || $$0.getSlot() >= Inventory.getSelectionSize()) {
            LOGGER.warn("{} tried to set an invalid carried item", (Object)this.player.getPlainTextName());
            return;
        }
        if (this.player.getInventory().getSelectedSlot() != $$0.getSlot() && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            this.player.stopUsingItem();
        }
        this.player.getInventory().setSelectedSlot($$0.getSlot());
        this.player.resetLastActionTime();
    }

    @Override
    public void handleChat(ServerboundChatPacket $$0) {
        Optional<LastSeenMessages> $$1 = this.unpackAndApplyLastSeen($$0.lastSeenMessages());
        if ($$1.isEmpty()) {
            return;
        }
        this.tryHandleChat($$0.message(), false, () -> {
            void $$4;
            try {
                PlayerChatMessage $$2 = this.getSignedMessage($$0, (LastSeenMessages)$$1.get());
            }
            catch (SignedMessageChain.DecodeException $$3) {
                this.handleMessageDecodeFailure($$3);
                return;
            }
            CompletableFuture<FilteredText> $$5 = this.filterTextPacket($$4.signedContent());
            Component $$6 = this.server.getChatDecorator().decorate(this.player, $$4.decoratedContent());
            this.chatMessageChain.append($$5, arg_0 -> this.lambda$handleChat$6((PlayerChatMessage)$$4, $$6, arg_0));
        });
    }

    @Override
    public void handleChatCommand(ServerboundChatCommandPacket $$0) {
        this.tryHandleChat($$0.command(), true, () -> {
            this.performUnsignedChatCommand($$0.command());
            this.detectRateSpam();
        });
    }

    private void performUnsignedChatCommand(String $$0) {
        ParseResults<CommandSourceStack> $$1 = this.parseCommand($$0);
        if (this.server.enforceSecureProfile() && SignableCommand.hasSignableArguments($$1)) {
            LOGGER.error("Received unsigned command packet from {}, but the command requires signable arguments: {}", (Object)this.player.getGameProfile().name(), (Object)$$0);
            this.player.sendSystemMessage(INVALID_COMMAND_SIGNATURE);
            return;
        }
        this.server.getCommands().performCommand($$1, $$0);
    }

    @Override
    public void handleSignedChatCommand(ServerboundChatCommandSignedPacket $$0) {
        Optional<LastSeenMessages> $$1 = this.unpackAndApplyLastSeen($$0.lastSeenMessages());
        if ($$1.isEmpty()) {
            return;
        }
        this.tryHandleChat($$0.command(), true, () -> {
            this.performSignedChatCommand($$0, (LastSeenMessages)$$1.get());
            this.detectRateSpam();
        });
    }

    /*
     * WARNING - void declaration
     */
    private void performSignedChatCommand(ServerboundChatCommandSignedPacket $$0, LastSeenMessages $$12) {
        void $$5;
        ParseResults<CommandSourceStack> $$2 = this.parseCommand($$0.command());
        try {
            Map<String, PlayerChatMessage> $$3 = this.collectSignedArguments($$0, SignableCommand.of($$2), $$12);
        }
        catch (SignedMessageChain.DecodeException $$4) {
            this.handleMessageDecodeFailure($$4);
            return;
        }
        CommandSigningContext.SignedArguments $$6 = new CommandSigningContext.SignedArguments((Map<String, PlayerChatMessage>)$$5);
        $$2 = Commands.mapSource($$2, $$1 -> $$1.withSigningContext($$6, this.chatMessageChain));
        this.server.getCommands().performCommand($$2, $$0.command());
    }

    private void handleMessageDecodeFailure(SignedMessageChain.DecodeException $$0) {
        LOGGER.warn("Failed to update secure chat state for {}: '{}'", (Object)this.player.getGameProfile().name(), (Object)$$0.getComponent().getString());
        this.player.sendSystemMessage($$0.getComponent().copy().withStyle(ChatFormatting.RED));
    }

    private <S> Map<String, PlayerChatMessage> collectSignedArguments(ServerboundChatCommandSignedPacket $$0, SignableCommand<S> $$1, LastSeenMessages $$2) throws SignedMessageChain.DecodeException {
        List<ArgumentSignatures.Entry> $$3 = $$0.argumentSignatures().entries();
        List<SignableCommand.Argument<S>> $$4 = $$1.arguments();
        if ($$3.isEmpty()) {
            return this.collectUnsignedArguments($$4);
        }
        Object2ObjectOpenHashMap $$5 = new Object2ObjectOpenHashMap();
        for (ArgumentSignatures.Entry entry : $$3) {
            SignableCommand.Argument<S> $$7 = $$1.getArgument(entry.name());
            if ($$7 == null) {
                this.signedMessageDecoder.setChainBroken();
                throw ServerGamePacketListenerImpl.createSignedArgumentMismatchException($$0.command(), $$3, $$4);
            }
            SignedMessageBody $$8 = new SignedMessageBody($$7.value(), $$0.timeStamp(), $$0.salt(), $$2);
            $$5.put($$7.name(), this.signedMessageDecoder.unpack(entry.signature(), $$8));
        }
        for (SignableCommand.Argument argument : $$4) {
            if ($$5.containsKey(argument.name())) continue;
            throw ServerGamePacketListenerImpl.createSignedArgumentMismatchException($$0.command(), $$3, $$4);
        }
        return $$5;
    }

    private <S> Map<String, PlayerChatMessage> collectUnsignedArguments(List<SignableCommand.Argument<S>> $$0) throws SignedMessageChain.DecodeException {
        HashMap<String, PlayerChatMessage> $$1 = new HashMap<String, PlayerChatMessage>();
        for (SignableCommand.Argument<S> $$2 : $$0) {
            SignedMessageBody $$3 = SignedMessageBody.unsigned($$2.value());
            $$1.put($$2.name(), this.signedMessageDecoder.unpack(null, $$3));
        }
        return $$1;
    }

    private static <S> SignedMessageChain.DecodeException createSignedArgumentMismatchException(String $$0, List<ArgumentSignatures.Entry> $$1, List<SignableCommand.Argument<S>> $$2) {
        String $$3 = $$1.stream().map(ArgumentSignatures.Entry::name).collect(Collectors.joining(", "));
        String $$4 = $$2.stream().map(SignableCommand.Argument::name).collect(Collectors.joining(", "));
        LOGGER.error("Signed command mismatch between server and client ('{}'): got [{}] from client, but expected [{}]", new Object[]{$$0, $$3, $$4});
        return new SignedMessageChain.DecodeException(INVALID_COMMAND_SIGNATURE);
    }

    private ParseResults<CommandSourceStack> parseCommand(String $$0) {
        CommandDispatcher<CommandSourceStack> $$1 = this.server.getCommands().getDispatcher();
        return $$1.parse($$0, (Object)this.player.createCommandSourceStack());
    }

    private void tryHandleChat(String $$0, boolean $$1, Runnable $$2) {
        if (ServerGamePacketListenerImpl.isChatMessageIllegal($$0)) {
            this.disconnect(Component.translatable("multiplayer.disconnect.illegal_characters"));
            return;
        }
        if (!$$1 && this.player.getChatVisibility() == ChatVisiblity.HIDDEN) {
            this.send(new ClientboundSystemChatPacket(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED), false));
            return;
        }
        this.player.resetLastActionTime();
        this.server.execute($$2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Optional<LastSeenMessages> unpackAndApplyLastSeen(LastSeenMessages.Update $$0) {
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            try {
                LastSeenMessages $$1 = this.lastSeenMessages.applyUpdate($$0);
                return Optional.of($$1);
            }
            catch (LastSeenMessagesValidator.ValidationException $$2) {
                LOGGER.error("Failed to validate message acknowledgements from {}: {}", (Object)this.player.getPlainTextName(), (Object)$$2.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED);
                return Optional.empty();
            }
        }
    }

    private static boolean isChatMessageIllegal(String $$0) {
        for (int $$1 = 0; $$1 < $$0.length(); ++$$1) {
            if (StringUtil.isAllowedChatCharacter($$0.charAt($$1))) continue;
            return true;
        }
        return false;
    }

    private PlayerChatMessage getSignedMessage(ServerboundChatPacket $$0, LastSeenMessages $$1) throws SignedMessageChain.DecodeException {
        SignedMessageBody $$2 = new SignedMessageBody($$0.message(), $$0.timeStamp(), $$0.salt(), $$1);
        return this.signedMessageDecoder.unpack($$0.signature(), $$2);
    }

    private void broadcastChatMessage(PlayerChatMessage $$0) {
        this.server.getPlayerList().broadcastChatMessage($$0, this.player, ChatType.bind(ChatType.CHAT, this.player));
        this.detectRateSpam();
    }

    private void detectRateSpam() {
        this.chatSpamThrottler.increment();
        if (!(this.chatSpamThrottler.isUnderThreshold() || this.server.getPlayerList().isOp(this.player.nameAndId()) || this.server.isSingleplayerOwner(this.player.nameAndId()))) {
            this.disconnect(Component.translatable("disconnect.spam"));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleChatAck(ServerboundChatAckPacket $$0) {
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            try {
                this.lastSeenMessages.applyOffset($$0.offset());
            }
            catch (LastSeenMessagesValidator.ValidationException $$1) {
                LOGGER.error("Failed to validate message acknowledgement offset from {}: {}", (Object)this.player.getPlainTextName(), (Object)$$1.getMessage());
                this.disconnect(CHAT_VALIDATION_FAILED);
            }
        }
    }

    @Override
    public void handleAnimate(ServerboundSwingPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.resetLastActionTime();
        this.player.swing($$0.getHand());
    }

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.hasClientLoaded()) {
            return;
        }
        this.player.resetLastActionTime();
        switch ($$0.getAction()) {
            case START_SPRINTING: {
                this.player.setSprinting(true);
                break;
            }
            case STOP_SPRINTING: {
                this.player.setSprinting(false);
                break;
            }
            case STOP_SLEEPING: {
                if (!this.player.isSleeping()) break;
                this.player.stopSleepInBed(false, true);
                this.awaitingPositionFromClient = this.player.position();
                break;
            }
            case START_RIDING_JUMP: {
                Entity entity = this.player.getControlledVehicle();
                if (!(entity instanceof PlayerRideableJumping)) break;
                PlayerRideableJumping $$1 = (PlayerRideableJumping)((Object)entity);
                int $$2 = $$0.getData();
                if (!$$1.canJump() || $$2 <= 0) break;
                $$1.handleStartJump($$2);
                break;
            }
            case STOP_RIDING_JUMP: {
                Entity entity = this.player.getControlledVehicle();
                if (!(entity instanceof PlayerRideableJumping)) break;
                PlayerRideableJumping $$3 = (PlayerRideableJumping)((Object)entity);
                $$3.handleStopJump();
                break;
            }
            case OPEN_INVENTORY: {
                Entity entity = this.player.getVehicle();
                if (!(entity instanceof HasCustomInventoryScreen)) break;
                HasCustomInventoryScreen $$4 = (HasCustomInventoryScreen)((Object)entity);
                $$4.openCustomInventoryScreen(this.player);
                break;
            }
            case START_FALL_FLYING: {
                if (this.player.tryToStartFallFlying()) break;
                this.player.stopFallFlying();
                break;
            }
            default: {
                throw new IllegalArgumentException("Invalid client command!");
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * WARNING - void declaration
     */
    public void sendPlayerChatMessage(PlayerChatMessage $$0, ChatType.Bound $$1) {
        void $$4;
        this.send(new ClientboundPlayerChatPacket(this.nextChatIndex++, $$0.link().sender(), $$0.link().index(), $$0.signature(), $$0.signedBody().pack(this.messageSignatureCache), $$0.unsignedContent(), $$0.filterMask(), $$1));
        MessageSignature $$2 = $$0.signature();
        if ($$2 == null) {
            return;
        }
        this.messageSignatureCache.push($$0.signedBody(), $$0.signature());
        LastSeenMessagesValidator lastSeenMessagesValidator = this.lastSeenMessages;
        synchronized (lastSeenMessagesValidator) {
            this.lastSeenMessages.addPending($$2);
            int $$3 = this.lastSeenMessages.trackedMessagesCount();
        }
        if ($$4 > 4096) {
            this.disconnect(Component.translatable("multiplayer.disconnect.too_many_pending_chats"));
        }
    }

    public void sendDisguisedChatMessage(Component $$0, ChatType.Bound $$1) {
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

    @Override
    public void handlePingRequest(ServerboundPingRequestPacket $$0) {
        this.connection.send(new ClientboundPongResponsePacket($$0.getTime()));
    }

    @Override
    public void handleInteract(ServerboundInteractPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.hasClientLoaded()) {
            return;
        }
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
                $$0.dispatch(new ServerboundInteractPacket.Handler(){

                    private void performInteraction(InteractionHand $$0, EntityInteraction $$12) {
                        ItemStack $$22 = ServerGamePacketListenerImpl.this.player.getItemInHand($$0);
                        if (!$$22.isItemEnabled($$1.enabledFeatures())) {
                            return;
                        }
                        ItemStack $$3 = $$22.copy();
                        InteractionResult $$4 = $$12.run(ServerGamePacketListenerImpl.this.player, $$2, $$0);
                        if ($$4 instanceof InteractionResult.Success) {
                            InteractionResult.Success $$5 = (InteractionResult.Success)$$4;
                            ItemStack $$6 = $$5.wasItemInteraction() ? $$3 : ItemStack.EMPTY;
                            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(ServerGamePacketListenerImpl.this.player, $$6, $$2);
                            if ($$5.swingSource() == InteractionResult.SwingSource.SERVER) {
                                ServerGamePacketListenerImpl.this.player.swing($$0, true);
                            }
                        }
                    }

                    @Override
                    public void onInteraction(InteractionHand $$0) {
                        this.performInteraction($$0, Player::interactOn);
                    }

                    @Override
                    public void onInteraction(InteractionHand $$0, Vec3 $$12) {
                        this.performInteraction($$0, ($$1, $$2, $$3) -> $$2.interactAt($$1, $$12, $$3));
                    }

                    @Override
                    public void onAttack() {
                        AbstractArrow $$0;
                        if ($$2 instanceof ItemEntity || $$2 instanceof ExperienceOrb || $$2 == ServerGamePacketListenerImpl.this.player || $$2 instanceof AbstractArrow && !($$0 = (AbstractArrow)$$2).isAttackable()) {
                            ServerGamePacketListenerImpl.this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            LOGGER.warn("Player {} tried to attack an invalid entity", (Object)ServerGamePacketListenerImpl.this.player.getPlainTextName());
                            return;
                        }
                        ItemStack $$12 = ServerGamePacketListenerImpl.this.player.getItemInHand(InteractionHand.MAIN_HAND);
                        if (!$$12.isItemEnabled($$1.enabledFeatures())) {
                            return;
                        }
                        if (ServerGamePacketListenerImpl.this.player.cannotAttackWithItem($$12, 5)) {
                            return;
                        }
                        ServerGamePacketListenerImpl.this.player.attack($$2);
                    }
                });
            }
        }
    }

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.resetLastActionTime();
        ServerboundClientCommandPacket.Action $$1 = $$0.getAction();
        switch ($$1) {
            case PERFORM_RESPAWN: {
                if (this.player.wonGame) {
                    this.player.wonGame = false;
                    this.player = this.server.getPlayerList().respawn(this.player, true, Entity.RemovalReason.CHANGED_DIMENSION);
                    this.resetPosition();
                    this.restartClientLoadTimerAfterRespawn();
                    CriteriaTriggers.CHANGED_DIMENSION.trigger(this.player, Level.END, Level.OVERWORLD);
                    break;
                }
                if (this.player.getHealth() > 0.0f) {
                    return;
                }
                this.player = this.server.getPlayerList().respawn(this.player, false, Entity.RemovalReason.KILLED);
                this.resetPosition();
                this.restartClientLoadTimerAfterRespawn();
                if (!this.server.isHardcore()) break;
                this.player.setGameMode(GameType.SPECTATOR);
                this.player.level().getGameRules().set(GameRules.SPECTATORS_GENERATE_CHUNKS, false, this.server);
                break;
            }
            case REQUEST_STATS: {
                this.player.getStats().sendStats(this.player);
            }
        }
    }

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.doCloseContainer();
    }

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId != $$0.containerId()) {
            return;
        }
        if (this.player.isSpectator()) {
            this.player.containerMenu.sendAllDataToRemote();
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        short $$1 = $$0.slotNum();
        if (!this.player.containerMenu.isValidSlotIndex($$1)) {
            LOGGER.debug("Player {} clicked invalid slot index: {}, available slots: {}", new Object[]{this.player.getPlainTextName(), (int)$$1, this.player.containerMenu.slots.size()});
            return;
        }
        boolean $$2 = $$0.stateId() != this.player.containerMenu.getStateId();
        this.player.containerMenu.suppressRemoteUpdates();
        this.player.containerMenu.clicked($$1, $$0.buttonNum(), $$0.clickType(), this.player);
        for (Int2ObjectMap.Entry $$3 : Int2ObjectMaps.fastIterable($$0.changedSlots())) {
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

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.isSpectator() || this.player.containerMenu.containerId != $$0.containerId()) {
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        RecipeManager.ServerDisplayInfo $$1 = this.server.getRecipeManager().getRecipeFromDisplay($$0.recipe());
        if ($$1 == null) {
            return;
        }
        RecipeHolder<?> $$2 = $$1.parent();
        if (!this.player.getRecipeBook().contains($$2.id())) {
            return;
        }
        AbstractContainerMenu abstractContainerMenu = this.player.containerMenu;
        if (abstractContainerMenu instanceof RecipeBookMenu) {
            RecipeBookMenu $$3 = (RecipeBookMenu)abstractContainerMenu;
            if ($$2.value().placementInfo().isImpossibleToPlace()) {
                LOGGER.debug("Player {} tried to place impossible recipe {}", (Object)this.player, (Object)$$2.id().identifier());
                return;
            }
            RecipeBookMenu.PostPlaceAction $$4 = $$3.handlePlacement($$0.useMaxItems(), this.player.isCreative(), $$2, this.player.level(), this.player.getInventory());
            if ($$4 == RecipeBookMenu.PostPlaceAction.PLACE_GHOST_RECIPE) {
                this.send(new ClientboundPlaceGhostRecipePacket(this.player.containerMenu.containerId, $$1.display().display()));
            }
        }
    }

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.resetLastActionTime();
        if (this.player.containerMenu.containerId != $$0.containerId() || this.player.isSpectator()) {
            return;
        }
        if (!this.player.containerMenu.stillValid(this.player)) {
            LOGGER.debug("Player {} interacted with invalid menu {}", (Object)this.player, (Object)this.player.containerMenu);
            return;
        }
        boolean $$1 = this.player.containerMenu.clickMenuButton(this.player, $$0.buttonId());
        if ($$1) {
            this.player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (this.player.hasInfiniteMaterials()) {
            boolean $$4;
            boolean $$1 = $$0.slotNum() < 0;
            ItemStack $$2 = $$0.itemStack();
            if (!$$2.isItemEnabled(this.player.level().enabledFeatures())) {
                return;
            }
            boolean $$3 = $$0.slotNum() >= 1 && $$0.slotNum() <= 45;
            boolean bl = $$4 = $$2.isEmpty() || $$2.getCount() <= $$2.getMaxStackSize();
            if ($$3 && $$4) {
                this.player.inventoryMenu.getSlot($$0.slotNum()).setByPlayer($$2);
                this.player.inventoryMenu.setRemoteSlot($$0.slotNum(), $$2);
                this.player.inventoryMenu.broadcastChanges();
            } else if ($$1 && $$4) {
                if (this.dropSpamThrottler.isUnderThreshold()) {
                    this.dropSpamThrottler.increment();
                    this.player.drop($$2, true);
                } else {
                    LOGGER.warn("Player {} was dropping items too fast in creative mode, ignoring.", (Object)this.player.getPlainTextName());
                }
            }
        }
    }

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket $$0) {
        List<String> $$12 = Stream.of($$0.getLines()).map(ChatFormatting::stripFormatting).collect(Collectors.toList());
        this.filterTextPacket($$12).thenAcceptAsync($$1 -> this.updateSignText($$0, (List<FilteredText>)$$1), (Executor)this.server);
    }

    /*
     * WARNING - void declaration
     */
    private void updateSignText(ServerboundSignUpdatePacket $$0, List<FilteredText> $$1) {
        this.player.resetLastActionTime();
        ServerLevel $$2 = this.player.level();
        BlockPos $$3 = $$0.getPos();
        if ($$2.hasChunkAt($$3)) {
            void $$6;
            BlockEntity $$4 = $$2.getBlockEntity($$3);
            if (!($$4 instanceof SignBlockEntity)) {
                return;
            }
            SignBlockEntity $$5 = (SignBlockEntity)$$4;
            $$6.updateSignText(this.player, $$0.isFrontText(), $$1);
        }
    }

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.getAbilities().flying = $$0.isFlying() && this.player.getAbilities().mayfly;
    }

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        boolean $$1 = this.player.isModelPartShown(PlayerModelPart.HAT);
        this.player.updateOptions($$0.information());
        if (this.player.isModelPartShown(PlayerModelPart.HAT) != $$1) {
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT, this.player));
        }
    }

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && !this.isSingleplayerOwner()) {
            LOGGER.warn("Player {} tried to change difficulty to {} without required permissions", (Object)this.player.getGameProfile().name(), (Object)$$0.difficulty().getDisplayName());
            return;
        }
        this.server.setDifficulty($$0.difficulty(), false);
    }

    @Override
    public void handleChangeGameMode(ServerboundChangeGameModePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!GameModeCommand.PERMISSION_CHECK.check(this.player.permissions())) {
            LOGGER.warn("Player {} tried to change game mode to {} without required permissions", (Object)this.player.getGameProfile().name(), (Object)$$0.mode().getShortDisplayName().getString());
            return;
        }
        GameModeCommand.setGameMode(this.player, $$0.mode());
    }

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) && !this.isSingleplayerOwner()) {
            return;
        }
        this.server.setDifficultyLocked($$0.isLocked());
    }

    @Override
    public void handleChatSessionUpdate(ServerboundChatSessionUpdatePacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        RemoteChatSession.Data $$1 = $$0.chatSession();
        ProfilePublicKey.Data $$2 = this.chatSession != null ? this.chatSession.profilePublicKey().data() : null;
        ProfilePublicKey.Data $$3 = $$1.profilePublicKey();
        if (Objects.equals($$2, $$3)) {
            return;
        }
        if ($$2 != null && $$3.expiresAt().isBefore($$2.expiresAt())) {
            this.disconnect(ProfilePublicKey.EXPIRED_PROFILE_PUBLIC_KEY);
            return;
        }
        try {
            SignatureValidator $$4 = this.server.services().profileKeySignatureValidator();
            if ($$4 == null) {
                LOGGER.warn("Ignoring chat session from {} due to missing Services public key", (Object)this.player.getGameProfile().name());
                return;
            }
            this.resetPlayerChatState($$1.validate(this.player.getGameProfile(), $$4));
        }
        catch (ProfilePublicKey.ValidationException $$5) {
            LOGGER.error("Failed to validate profile key: {}", (Object)$$5.getMessage());
            this.disconnect($$5.getComponent());
        }
    }

    @Override
    public void handleConfigurationAcknowledged(ServerboundConfigurationAcknowledgedPacket $$0) {
        if (!this.waitingForSwitchToConfig) {
            throw new IllegalStateException("Client acknowledged config, but none was requested");
        }
        this.connection.setupInboundProtocol(ConfigurationProtocols.SERVERBOUND, new ServerConfigurationPacketListenerImpl(this.server, this.connection, this.createCookie(this.player.clientInformation())));
    }

    @Override
    public void handleChunkBatchReceived(ServerboundChunkBatchReceivedPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.chunkSender.onChunkBatchReceivedByClient($$0.desiredChunksPerTick());
    }

    @Override
    public void handleDebugSubscriptionRequest(ServerboundDebugSubscriptionRequestPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        this.player.requestDebugSubscriptions($$0.subscriptions());
    }

    private void resetPlayerChatState(RemoteChatSession $$0) {
        this.chatSession = $$0;
        this.signedMessageDecoder = $$0.createMessageDecoder(this.player.getUUID());
        this.chatMessageChain.append(() -> {
            this.player.setChatSession($$0);
            this.server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT), List.of(this.player)));
        });
    }

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket $$0) {
    }

    @Override
    public void handleClientTickEnd(ServerboundClientTickEndPacket $$0) {
        PacketUtils.ensureRunningOnSameThread($$0, this, this.player.level());
        if (!this.receivedMovementThisTick) {
            this.player.setKnownMovement(Vec3.ZERO);
        }
        this.receivedMovementThisTick = false;
    }

    private void handlePlayerKnownMovement(Vec3 $$0) {
        if ($$0.lengthSqr() > (double)1.0E-5f) {
            this.player.resetLastActionTime();
        }
        this.player.setKnownMovement($$0);
        this.receivedMovementThisTick = true;
    }

    @Override
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
            --this.clientLoadedTimeoutTimer;
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

    private /* synthetic */ void lambda$handleChat$6(PlayerChatMessage $$0, Component $$1, FilteredText $$2) {
        PlayerChatMessage $$3 = $$0.withUnsignedContent($$1).filter($$2.mask());
        this.broadcastChatMessage($$3);
    }

    @FunctionalInterface
    static interface EntityInteraction {
        public InteractionResult run(ServerPlayer var1, Entity var2, InteractionHand var3);
    }
}

