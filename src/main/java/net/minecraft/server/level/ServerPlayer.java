package net.minecraft.server.level;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.HashCode;
import com.google.common.net.InetAddresses;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEndPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatEnterPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundServerDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCursorItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerStatus.Favicon;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.stats.ServerRecipeBook.Packed;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.HashOps;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.NautilusInventoryMenu;
import net.minecraft.world.inventory.RemoteSlot;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.RemoteSlot.Synchronized;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.ServerItemCooldowns;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.portal.TeleportTransition.PostTeleportTransition;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.LevelData.RespawnData;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.Team.Visibility;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class ServerPlayer extends Player {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_XZ = 32;
   private static final int NEUTRAL_MOB_DEATH_NOTIFICATION_RADII_Y = 10;
   private static final int FLY_STAT_RECORDING_SPEED = 25;
   public static final double BLOCK_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 1.0;
   public static final double ENTITY_INTERACTION_DISTANCE_VERIFICATION_BUFFER = 3.0;
   public static final int ENDER_PEARL_TICKET_RADIUS = 2;
   public static final String ENDER_PEARLS_TAG = "ender_pearls";
   public static final String ENDER_PEARL_DIMENSION_TAG = "ender_pearl_dimension";
   public static final String TAG_DIMENSION = "Dimension";
   private static final AttributeModifier CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
      Identifier.withDefaultNamespace("creative_mode_block_range"), 0.5, Operation.ADD_VALUE
   );
   private static final AttributeModifier CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER = new AttributeModifier(
      Identifier.withDefaultNamespace("creative_mode_entity_range"), 2.0, Operation.ADD_VALUE
   );
   private static final Component SPAWN_SET_MESSAGE = Component.translatable("block.minecraft.set_spawn");
   private static final AttributeModifier WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER = new AttributeModifier(
      Identifier.withDefaultNamespace("waypoint_transmit_range_crouch"), -1.0, Operation.ADD_MULTIPLIED_TOTAL
   );
   private static final boolean DEFAULT_SEEN_CREDITS = false;
   private static final boolean DEFAULT_SPAWN_EXTRA_PARTICLES_ON_FALL = false;
   public ServerGamePacketListenerImpl connection;
   private final net.minecraft.server.MinecraftServer server;
   public final ServerPlayerGameMode gameMode;
   private final net.minecraft.server.PlayerAdvancements advancements;
   private final ServerStatsCounter stats;
   private float lastRecordedHealthAndAbsorption = Float.MIN_VALUE;
   private int lastRecordedFoodLevel = Integer.MIN_VALUE;
   private int lastRecordedAirLevel = Integer.MIN_VALUE;
   private int lastRecordedArmor = Integer.MIN_VALUE;
   private int lastRecordedLevel = Integer.MIN_VALUE;
   private int lastRecordedExperience = Integer.MIN_VALUE;
   private float lastSentHealth = -1.0E8F;
   private int lastSentFood = -99999999;
   private boolean lastFoodSaturationZero = true;
   private int lastSentExp = -99999999;
   private ChatVisiblity chatVisibility = ChatVisiblity.FULL;
   private ParticleStatus particleStatus = ParticleStatus.ALL;
   private boolean canChatColor = true;
   private long lastActionTime = Util.getMillis();
   
   private Entity camera;
   private boolean isChangingDimension;
   public boolean seenCredits = false;
   private final ServerRecipeBook recipeBook;
   
   private Vec3 levitationStartPos;
   private int levitationStartTime;
   private boolean disconnected;
   private int requestedViewDistance = 2;
   private String language = "en_us";
   
   private Vec3 startingToFallPosition;
   
   private Vec3 enteredNetherPosition;
   
   private Vec3 enteredLavaOnVehiclePosition;
   private SectionPos lastSectionPos = SectionPos.of(0, 0, 0);
   private ChunkTrackingView chunkTrackingView = ChunkTrackingView.EMPTY;
   
   private ServerPlayer.RespawnConfig respawnConfig;
   private final TextFilter textFilter;
   private boolean textFilteringEnabled;
   private boolean allowsListing;
   private boolean spawnExtraParticlesOnFall = false;
   private WardenSpawnTracker wardenSpawnTracker = new WardenSpawnTracker();
   
   private BlockPos raidOmenPosition;
   private Vec3 lastKnownClientMovement = Vec3.ZERO;
   private Input lastClientInput = Input.EMPTY;
   private final Set<ThrownEnderpearl> enderPearls = new HashSet<>();
   private long timeEntitySatOnShoulder;
   private CompoundTag shoulderEntityLeft = new CompoundTag();
   private CompoundTag shoulderEntityRight = new CompoundTag();
   private final ContainerSynchronizer containerSynchronizer = new ContainerSynchronizer() {
      private final LoadingCache<TypedDataComponent<?>, Integer> cache = CacheBuilder.newBuilder()
         .maximumSize(256L)
         .build(
            new CacheLoader<TypedDataComponent<?>, Integer>() {
               private final DynamicOps<HashCode> registryHashOps = ServerPlayer.this.registryAccess().createSerializationContext(HashOps.CRC32C_INSTANCE);

               public Integer load(TypedDataComponent<?> $$0) {
                  return ((HashCode)$$0.encodeValue(this.registryHashOps).getOrThrow($$1 -> new IllegalArgumentException("Failed to hash " + $$0 + ": " + $$1)))
                     .asInt();
               }
            }
         );

      public void sendInitialData(AbstractContainerMenu $$0, List<ItemStack> $$1, ItemStack $$2, int[] $$3) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetContentPacket($$0.containerId, $$0.incrementStateId(), $$1, $$2));

         for (int $$4 = 0; $$4 < $$3.length; $$4++) {
            this.broadcastDataValue($$0, $$4, $$3[$$4]);
         }
      }

      public void sendSlotChange(AbstractContainerMenu $$0, int $$1, ItemStack $$2) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetSlotPacket($$0.containerId, $$0.incrementStateId(), $$1, $$2));
      }

      public void sendCarriedChange(AbstractContainerMenu $$0, ItemStack $$1) {
         ServerPlayer.this.connection.send(new ClientboundSetCursorItemPacket($$1));
      }

      public void sendDataChange(AbstractContainerMenu $$0, int $$1, int $$2) {
         this.broadcastDataValue($$0, $$1, $$2);
      }

      private void broadcastDataValue(AbstractContainerMenu $$0, int $$1, int $$2) {
         ServerPlayer.this.connection.send(new ClientboundContainerSetDataPacket($$0.containerId, $$1, $$2));
      }

      public RemoteSlot createSlot() {
         return new Synchronized(this.cache::getUnchecked);
      }
   };
   private final ContainerListener containerListener = new ContainerListener() {
      public void slotChanged(AbstractContainerMenu $$0, int $$1, ItemStack $$2) {
         Slot $$3 = $$0.getSlot($$1);
         if (!($$3 instanceof ResultSlot)) {
            if ($$3.container == ServerPlayer.this.getInventory()) {
               CriteriaTriggers.INVENTORY_CHANGED.trigger(ServerPlayer.this, ServerPlayer.this.getInventory(), $$2);
            }
         }
      }

      public void dataChanged(AbstractContainerMenu $$0, int $$1, int $$2) {
      }
   };
   
   private RemoteChatSession chatSession;
   
   public final Object object;
   private final CommandSource commandSource = new CommandSource() {
      public boolean acceptsSuccess() {
         return (Boolean)ServerPlayer.this.level().getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK);
      }

      public boolean acceptsFailure() {
         return true;
      }

      public boolean shouldInformAdmins() {
         return true;
      }

      public void sendSystemMessage(Component $$0) {
         ServerPlayer.this.sendSystemMessage($$0);
      }
   };
   private Set<DebugSubscription<?>> requestedDebugSubscriptions = Set.of();
   private int containerCounter;
   public boolean wonGame;

   public ServerPlayer(net.minecraft.server.MinecraftServer $$0, ServerLevel $$1, GameProfile $$2, ClientInformation $$3) {
      super($$1, $$2);
      this.server = $$0;
      this.textFilter = $$0.createTextFilterForPlayer(this);
      this.gameMode = $$0.createGameModeForPlayer(this);
      this.gameMode.setGameModeForPlayer(this.calculateGameModeForNewPlayer(null), null);
      this.recipeBook = new ServerRecipeBook(($$1x, $$2x) -> $$0.getRecipeManager().listDisplaysForRecipe($$1x, $$2x));
      this.stats = $$0.getPlayerList().getPlayerStats(this);
      this.advancements = $$0.getPlayerList().getPlayerAdvancements(this);
      this.updateOptions($$3);
      this.object = null;
   }

   public BlockPos adjustSpawnLocation(ServerLevel $$0, BlockPos $$1) {
      CompletableFuture<Vec3> $$2 = PlayerSpawnFinder.findSpawn($$0, $$1);
      this.server.managedBlock($$2::isDone);
      return BlockPos.containing((Position)$$2.join());
   }

   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.wardenSpawnTracker = (WardenSpawnTracker)$$0.read("warden_spawn_tracker", WardenSpawnTracker.CODEC).orElseGet(WardenSpawnTracker::new);
      this.enteredNetherPosition = (Vec3)$$0.read("entered_nether_pos", Vec3.CODEC).orElse(null);
      this.seenCredits = $$0.getBooleanOr("seenCredits", false);
      $$0.read("recipeBook", Packed.CODEC)
         .ifPresent($$0x -> this.recipeBook.loadUntrusted($$0x, $$0xx -> this.server.getRecipeManager().byKey($$0xx).isPresent()));
      if (this.isSleeping()) {
         this.stopSleeping();
      }

      this.respawnConfig = (ServerPlayer.RespawnConfig)$$0.read("respawn", ServerPlayer.RespawnConfig.CODEC).orElse(null);
      this.spawnExtraParticlesOnFall = $$0.getBooleanOr("spawn_extra_particles_on_fall", false);
      this.raidOmenPosition = (BlockPos)$$0.read("raid_omen_position", BlockPos.CODEC).orElse(null);
      this.gameMode
         .setGameModeForPlayer(this.calculateGameModeForNewPlayer(readPlayerMode($$0, "playerGameType")), readPlayerMode($$0, "previousPlayerGameType"));
      this.setShoulderEntityLeft((CompoundTag)$$0.read("ShoulderEntityLeft", CompoundTag.CODEC).orElseGet(CompoundTag::new));
      this.setShoulderEntityRight((CompoundTag)$$0.read("ShoulderEntityRight", CompoundTag.CODEC).orElseGet(CompoundTag::new));
   }

   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("warden_spawn_tracker", WardenSpawnTracker.CODEC, this.wardenSpawnTracker);
      this.storeGameTypes($$0);
      $$0.putBoolean("seenCredits", this.seenCredits);
      $$0.storeNullable("entered_nether_pos", Vec3.CODEC, this.enteredNetherPosition);
      this.saveParentVehicle($$0);
      $$0.store("recipeBook", Packed.CODEC, this.recipeBook.pack());
      $$0.putString("Dimension", this.level().dimension().identifier().toString());
      $$0.storeNullable("respawn", ServerPlayer.RespawnConfig.CODEC, this.respawnConfig);
      $$0.putBoolean("spawn_extra_particles_on_fall", this.spawnExtraParticlesOnFall);
      $$0.storeNullable("raid_omen_position", BlockPos.CODEC, this.raidOmenPosition);
      this.saveEnderPearls($$0);
      if (!this.getShoulderEntityLeft().isEmpty()) {
         $$0.store("ShoulderEntityLeft", CompoundTag.CODEC, this.getShoulderEntityLeft());
      }

      if (!this.getShoulderEntityRight().isEmpty()) {
         $$0.store("ShoulderEntityRight", CompoundTag.CODEC, this.getShoulderEntityRight());
      }
   }

   private void saveParentVehicle(ValueOutput $$0) {
      Entity $$1 = this.getRootVehicle();
      Entity $$2 = this.getVehicle();
      if ($$2 != null && $$1 != this && $$1.hasExactlyOnePlayerPassenger()) {
         ValueOutput $$3 = $$0.child("RootVehicle");
         $$3.store("Attach", UUIDUtil.CODEC, $$2.getUUID());
         $$1.save($$3.child("Entity"));
      }
   }

   public void loadAndSpawnParentVehicle(ValueInput $$0) {
      Optional<ValueInput> $$1 = $$0.child("RootVehicle");
      if (!$$1.isEmpty()) {
         ServerLevel $$2 = this.level();
         Entity $$3 = EntityType.loadEntityRecursive(
            $$1.get().childOrEmpty("Entity"), $$2, EntitySpawnReason.LOAD, $$1x -> !$$2.addWithUUID($$1x) ? null : $$1x
         );
         if ($$3 != null) {
            UUID $$4 = (UUID)$$1.get().read("Attach", UUIDUtil.CODEC).orElse(null);
            if ($$3.getUUID().equals($$4)) {
               this.startRiding($$3, true, false);
            } else {
               for (Entity $$5 : $$3.getIndirectPassengers()) {
                  if ($$5.getUUID().equals($$4)) {
                     this.startRiding($$5, true, false);
                     break;
                  }
               }
            }

            if (!this.isPassenger()) {
               LOGGER.warn("Couldn't reattach entity to player");
               $$3.discard();

               for (Entity $$6 : $$3.getIndirectPassengers()) {
                  $$6.discard();
               }
            }
         }
      }
   }

   private void saveEnderPearls(ValueOutput $$0) {
      if (!this.enderPearls.isEmpty()) {
         ValueOutputList $$1 = $$0.childrenList("ender_pearls");

         for (ThrownEnderpearl $$2 : this.enderPearls) {
            if ($$2.isRemoved()) {
               LOGGER.warn("Trying to save removed ender pearl, skipping");
            } else {
               ValueOutput $$3 = $$1.addChild();
               $$2.save($$3);
               $$3.store("ender_pearl_dimension", Level.RESOURCE_KEY_CODEC, $$2.level().dimension());
            }
         }
      }
   }

   public void loadAndSpawnEnderPearls(ValueInput $$0) {
      $$0.childrenListOrEmpty("ender_pearls").forEach(this::loadAndSpawnEnderPearl);
   }

   private void loadAndSpawnEnderPearl(ValueInput $$0) {
      Optional<ResourceKey<Level>> $$1 = $$0.read("ender_pearl_dimension", Level.RESOURCE_KEY_CODEC);
      if (!$$1.isEmpty()) {
         ServerLevel $$2 = this.level().getServer().getLevel($$1.get());
         if ($$2 != null) {
            Entity $$3 = EntityType.loadEntityRecursive($$0, $$2, EntitySpawnReason.LOAD, $$1x -> !$$2.addWithUUID($$1x) ? null : $$1x);
            if ($$3 != null) {
               placeEnderPearlTicket($$2, $$3.chunkPosition());
            } else {
               LOGGER.warn("Failed to spawn player ender pearl in level ({}), skipping", $$1.get());
            }
         } else {
            LOGGER.warn("Trying to load ender pearl without level ({}) being loaded, skipping", $$1.get());
         }
      }
   }

   public void setExperiencePoints(int $$0) {
      float $$1 = this.getXpNeededForNextLevel();
      float $$2 = ($$1 - 1.0F) / $$1;
      float $$3 = Mth.clamp($$0 / $$1, 0.0F, $$2);
      if ($$3 != this.experienceProgress) {
         this.experienceProgress = $$3;
         this.lastSentExp = -1;
      }
   }

   public void setExperienceLevels(int $$0) {
      if ($$0 != this.experienceLevel) {
         this.experienceLevel = $$0;
         this.lastSentExp = -1;
      }
   }

   public void giveExperienceLevels(int $$0) {
      if ($$0 != 0) {
         super.giveExperienceLevels($$0);
         this.lastSentExp = -1;
      }
   }

   public void onEnchantmentPerformed(ItemStack $$0, int $$1) {
      super.onEnchantmentPerformed($$0, $$1);
      this.lastSentExp = -1;
   }

   private void initMenu(AbstractContainerMenu $$0) {
      $$0.addSlotListener(this.containerListener);
      $$0.setSynchronizer(this.containerSynchronizer);
   }

   public void initInventoryMenu() {
      this.initMenu(this.inventoryMenu);
   }

   public void onEnterCombat() {
      super.onEnterCombat();
      this.connection.send(ClientboundPlayerCombatEnterPacket.INSTANCE);
   }

   public void onLeaveCombat() {
      super.onLeaveCombat();
      this.connection.send(new ClientboundPlayerCombatEndPacket(this.getCombatTracker()));
   }

   public void onInsideBlock(BlockState $$0) {
      CriteriaTriggers.ENTER_BLOCK.trigger(this, $$0);
   }

   protected ItemCooldowns createItemCooldowns() {
      return new ServerItemCooldowns(this);
   }

   public void tick() {
      this.connection.tickClientLoadTimeout();
      this.gameMode.tick();
      this.wardenSpawnTracker.tick();
      if (this.invulnerableTime > 0) {
         this.invulnerableTime--;
      }

      this.containerMenu.broadcastChanges();
      if (!this.containerMenu.stillValid(this)) {
         this.closeContainer();
         this.containerMenu = this.inventoryMenu;
      }

      Entity $$0 = this.getCamera();
      if ($$0 != this) {
         if ($$0.isAlive()) {
            this.absSnapTo($$0.getX(), $$0.getY(), $$0.getZ(), $$0.getYRot(), $$0.getXRot());
            this.level().getChunkSource().move(this);
            if (this.wantsToStopRiding()) {
               this.setCamera(this);
            }
         } else {
            this.setCamera(this);
         }
      }

      CriteriaTriggers.TICK.trigger(this);
      if (this.levitationStartPos != null) {
         CriteriaTriggers.LEVITATION.trigger(this, this.levitationStartPos, this.tickCount - this.levitationStartTime);
      }

      this.trackStartFallingPosition();
      this.trackEnteredOrExitedLavaOnVehicle();
      this.updatePlayerAttributes();
      this.advancements.flushDirty(this, true);
   }

   private void updatePlayerAttributes() {
      AttributeInstance $$0 = this.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
      if ($$0 != null) {
         if (this.isCreative()) {
            $$0.addOrUpdateTransientModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
         } else {
            $$0.removeModifier(CREATIVE_BLOCK_INTERACTION_RANGE_MODIFIER);
         }
      }

      AttributeInstance $$1 = this.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
      if ($$1 != null) {
         if (this.isCreative()) {
            $$1.addOrUpdateTransientModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
         } else {
            $$1.removeModifier(CREATIVE_ENTITY_INTERACTION_RANGE_MODIFIER);
         }
      }

      AttributeInstance $$2 = this.getAttribute(Attributes.WAYPOINT_TRANSMIT_RANGE);
      if ($$2 != null) {
         if (this.isCrouching()) {
            $$2.addOrUpdateTransientModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
         } else {
            $$2.removeModifier(WAYPOINT_TRANSMIT_RANGE_CROUCH_MODIFIER);
         }
      }
   }

   public void doTick() {
      try {
         if (!this.isSpectator() || !this.touchingUnloadedChunk()) {
            super.tick();
            if (!this.containerMenu.stillValid(this)) {
               this.closeContainer();
               this.containerMenu = this.inventoryMenu;
            }

            this.foodData.tick(this);
            this.awardStat(Stats.PLAY_TIME);
            this.awardStat(Stats.TOTAL_WORLD_TIME);
            if (this.isAlive()) {
               this.awardStat(Stats.TIME_SINCE_DEATH);
            }

            if (this.isDiscrete()) {
               this.awardStat(Stats.CROUCH_TIME);
            }

            if (!this.isSleeping()) {
               this.awardStat(Stats.TIME_SINCE_REST);
            }
         }

         for (int $$0 = 0; $$0 < this.getInventory().getContainerSize(); $$0++) {
            ItemStack $$1 = this.getInventory().getItem($$0);
            if (!$$1.isEmpty()) {
               this.synchronizeSpecialItemUpdates($$1);
            }
         }

         if (this.getHealth() != this.lastSentHealth
            || this.lastSentFood != this.foodData.getFoodLevel()
            || this.foodData.getSaturationLevel() == 0.0F != this.lastFoodSaturationZero) {
            this.connection.send(new ClientboundSetHealthPacket(this.getHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
            this.lastSentHealth = this.getHealth();
            this.lastSentFood = this.foodData.getFoodLevel();
            this.lastFoodSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
         }

         if (this.getHealth() + this.getAbsorptionAmount() != this.lastRecordedHealthAndAbsorption) {
            this.lastRecordedHealthAndAbsorption = this.getHealth() + this.getAbsorptionAmount();
            this.updateScoreForCriteria(ObjectiveCriteria.HEALTH, Mth.ceil(this.lastRecordedHealthAndAbsorption));
         }

         if (this.foodData.getFoodLevel() != this.lastRecordedFoodLevel) {
            this.lastRecordedFoodLevel = this.foodData.getFoodLevel();
            this.updateScoreForCriteria(ObjectiveCriteria.FOOD, Mth.ceil(this.lastRecordedFoodLevel));
         }

         if (this.getAirSupply() != this.lastRecordedAirLevel) {
            this.lastRecordedAirLevel = this.getAirSupply();
            this.updateScoreForCriteria(ObjectiveCriteria.AIR, Mth.ceil(this.lastRecordedAirLevel));
         }

         if (this.getArmorValue() != this.lastRecordedArmor) {
            this.lastRecordedArmor = this.getArmorValue();
            this.updateScoreForCriteria(ObjectiveCriteria.ARMOR, Mth.ceil(this.lastRecordedArmor));
         }

         if (this.totalExperience != this.lastRecordedExperience) {
            this.lastRecordedExperience = this.totalExperience;
            this.updateScoreForCriteria(ObjectiveCriteria.EXPERIENCE, Mth.ceil(this.lastRecordedExperience));
         }

         if (this.experienceLevel != this.lastRecordedLevel) {
            this.lastRecordedLevel = this.experienceLevel;
            this.updateScoreForCriteria(ObjectiveCriteria.LEVEL, Mth.ceil(this.lastRecordedLevel));
         }

         if (this.totalExperience != this.lastSentExp) {
            this.lastSentExp = this.totalExperience;
            this.connection.send(new ClientboundSetExperiencePacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
         }

         if (this.tickCount % 20 == 0) {
            CriteriaTriggers.LOCATION.trigger(this);
         }
      } catch (Throwable var4) {
         CrashReport $$3 = CrashReport.forThrowable(var4, "Ticking player");
         CrashReportCategory $$4 = $$3.addCategory("Player being ticked");
         this.fillCrashReportCategory($$4);
         throw new ReportedException($$3);
      }
   }

   private void synchronizeSpecialItemUpdates(ItemStack $$0) {
      MapId $$1 = (MapId)$$0.get(DataComponents.MAP_ID);
      MapItemSavedData $$2 = MapItem.getSavedData($$1, this.level());
      if ($$2 != null) {
         Packet<?> $$3 = $$2.getUpdatePacket($$1, this);
         if ($$3 != null) {
            this.connection.send($$3);
         }
      }
   }

   protected void tickRegeneration() {
      if (this.level().getDifficulty() == Difficulty.PEACEFUL && (Boolean)this.level().getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION)) {
         if (this.tickCount % 20 == 0) {
            if (this.getHealth() < this.getMaxHealth()) {
               this.heal(1.0F);
            }

            float $$0 = this.foodData.getSaturationLevel();
            if ($$0 < 20.0F) {
               this.foodData.setSaturation($$0 + 1.0F);
            }
         }

         if (this.tickCount % 10 == 0 && this.foodData.needsFood()) {
            this.foodData.setFoodLevel(this.foodData.getFoodLevel() + 1);
         }
      }
   }

   public void handleShoulderEntities() {
      this.playShoulderEntityAmbientSound(this.getShoulderEntityLeft());
      this.playShoulderEntityAmbientSound(this.getShoulderEntityRight());
      if (this.fallDistance > 0.5 || this.isInWater() || this.getAbilities().flying || this.isSleeping() || this.isInPowderSnow) {
         this.removeEntitiesOnShoulder();
      }
   }

   private void playShoulderEntityAmbientSound(CompoundTag $$0) {
      if (!$$0.isEmpty() && !$$0.getBooleanOr("Silent", false)) {
         if (this.random.nextInt(200) == 0) {
            EntityType<?> $$1 = (EntityType<?>)$$0.read("id", EntityType.CODEC).orElse(null);
            if ($$1 == EntityType.PARROT && !Parrot.imitateNearbyMobs(this.level(), this)) {
               this.level()
                  .playSound(
                     null,
                     this.getX(),
                     this.getY(),
                     this.getZ(),
                     Parrot.getAmbient(this.level(), this.random),
                     this.getSoundSource(),
                     1.0F,
                     Parrot.getPitch(this.random)
                  );
            }
         }
      }
   }

   public boolean setEntityOnShoulder(CompoundTag $$0) {
      if (this.isPassenger() || !this.onGround() || this.isInWater() || this.isInPowderSnow) {
         return false;
      } else if (this.getShoulderEntityLeft().isEmpty()) {
         this.setShoulderEntityLeft($$0);
         this.timeEntitySatOnShoulder = this.level().getGameTime();
         return true;
      } else if (this.getShoulderEntityRight().isEmpty()) {
         this.setShoulderEntityRight($$0);
         this.timeEntitySatOnShoulder = this.level().getGameTime();
         return true;
      } else {
         return false;
      }
   }

   protected void removeEntitiesOnShoulder() {
      if (this.timeEntitySatOnShoulder + 20L < this.level().getGameTime()) {
         this.respawnEntityOnShoulder(this.getShoulderEntityLeft());
         this.setShoulderEntityLeft(new CompoundTag());
         this.respawnEntityOnShoulder(this.getShoulderEntityRight());
         this.setShoulderEntityRight(new CompoundTag());
      }
   }

   private void respawnEntityOnShoulder(CompoundTag $$0) {
      ServerLevel $$2 = this.level();
      if ($$2 instanceof ServerLevel) {
         ServerLevel $$1 = $$2;
         if (!$$0.isEmpty()) {
            ScopedCollector $$2x = new ScopedCollector(this.problemPath(), LOGGER);

            try {
               EntityType.create(TagValueInput.create($$2x.forChild(() -> ".shoulder"), $$1.registryAccess(), $$0), $$1, EntitySpawnReason.LOAD)
                  .ifPresent($$1x -> {
                     if ($$1x instanceof TamableAnimal $$2xx) {
                        $$2xx.setOwner(this);
                     }

                     $$1x.setPos(this.getX(), this.getY() + 0.7F, this.getZ());
                     $$1.addWithUUID($$1x);
                  });
            } catch (Throwable var7) {
               try {
                  $$2x.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            $$2x.close();
         }
      }
   }

   public void resetFallDistance() {
      if (this.getHealth() > 0.0F && this.startingToFallPosition != null) {
         CriteriaTriggers.FALL_FROM_HEIGHT.trigger(this, this.startingToFallPosition);
      }

      this.startingToFallPosition = null;
      super.resetFallDistance();
   }

   public void trackStartFallingPosition() {
      if (this.fallDistance > 0.0 && this.startingToFallPosition == null) {
         this.startingToFallPosition = this.position();
         if (this.currentImpulseImpactPos != null && this.currentImpulseImpactPos.y <= this.startingToFallPosition.y) {
            CriteriaTriggers.FALL_AFTER_EXPLOSION.trigger(this, this.currentImpulseImpactPos, this.currentExplosionCause);
         }
      }
   }

   public void trackEnteredOrExitedLavaOnVehicle() {
      if (this.getVehicle() != null && this.getVehicle().isInLava()) {
         if (this.enteredLavaOnVehiclePosition == null) {
            this.enteredLavaOnVehiclePosition = this.position();
         } else {
            CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.trigger(this, this.enteredLavaOnVehiclePosition);
         }
      }

      if (this.enteredLavaOnVehiclePosition != null && (this.getVehicle() == null || !this.getVehicle().isInLava())) {
         this.enteredLavaOnVehiclePosition = null;
      }
   }

   private void updateScoreForCriteria(ObjectiveCriteria $$0, int $$1) {
      this.level().getScoreboard().forAllObjectives($$0, this, $$1x -> $$1x.set($$1));
   }

   public void die(DamageSource $$0) {
      this.gameEvent(GameEvent.ENTITY_DIE);
      boolean $$1 = (Boolean)this.level().getGameRules().get(GameRules.SHOW_DEATH_MESSAGES);
      if ($$1) {
         Component $$2 = this.getCombatTracker().getDeathMessage();
         this.connection
            .send(
               new ClientboundPlayerCombatKillPacket(this.getId(), $$2),
               PacketSendListener.exceptionallySend(
                  () -> {
                     int $$1x = 256;
                     String $$2x = $$2.getString(256);
                     Component $$3x = Component.translatable(
                        "death.attack.message_too_long", new Object[]{Component.literal($$2x).withStyle(ChatFormatting.YELLOW)}
                     );
                     Component $$4x = Component.translatable("death.attack.even_more_magic", new Object[]{this.getDisplayName()})
                        .withStyle($$1xx -> $$1xx.withHoverEvent(new ShowText($$3x)));
                     return new ClientboundPlayerCombatKillPacket(this.getId(), $$4x);
                  }
               )
            );
         Team $$3 = this.getTeam();
         if ($$3 == null || $$3.getDeathMessageVisibility() == Visibility.ALWAYS) {
            this.server.getPlayerList().broadcastSystemMessage($$2, false);
         } else if ($$3.getDeathMessageVisibility() == Visibility.HIDE_FOR_OTHER_TEAMS) {
            this.server.getPlayerList().broadcastSystemToTeam(this, $$2);
         } else if ($$3.getDeathMessageVisibility() == Visibility.HIDE_FOR_OWN_TEAM) {
            this.server.getPlayerList().broadcastSystemToAllExceptTeam(this, $$2);
         }
      } else {
         this.connection.send(new ClientboundPlayerCombatKillPacket(this.getId(), CommonComponents.EMPTY));
      }

      this.removeEntitiesOnShoulder();
      if ((Boolean)this.level().getGameRules().get(GameRules.FORGIVE_DEAD_PLAYERS)) {
         this.tellNeutralMobsThatIDied();
      }

      if (!this.isSpectator()) {
         this.dropAllDeathLoot(this.level(), $$0);
      }

      this.level().getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, this, ScoreAccess::increment);
      LivingEntity $$4 = this.getKillCredit();
      if ($$4 != null) {
         this.awardStat(Stats.ENTITY_KILLED_BY.get($$4.getType()));
         $$4.awardKillScore(this, $$0);
         this.createWitherRose($$4);
      }

      this.level().broadcastEntityEvent(this, (byte)3);
      this.awardStat(Stats.DEATHS);
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      this.clearFire();
      this.setTicksFrozen(0);
      this.setSharedFlagOnFire(false);
      this.getCombatTracker().recheckStatus();
      this.setLastDeathLocation(Optional.of(GlobalPos.of(this.level().dimension(), this.blockPosition())));
      this.connection.markClientUnloadedAfterDeath();
   }

   private void tellNeutralMobsThatIDied() {
      AABB $$0 = new AABB(this.blockPosition()).inflate(32.0, 10.0, 32.0);
      this.level()
         .getEntitiesOfClass(Mob.class, $$0, EntitySelector.NO_SPECTATORS)
         .stream()
         .filter($$0x -> $$0x instanceof NeutralMob)
         .forEach($$0x -> ((NeutralMob)$$0x).playerDied(this.level(), this));
   }

   public void awardKillScore(Entity $$0, DamageSource $$1) {
      if ($$0 != this) {
         super.awardKillScore($$0, $$1);
         Scoreboard $$2 = this.level().getScoreboard();
         $$2.forAllObjectives(ObjectiveCriteria.KILL_COUNT_ALL, this, ScoreAccess::increment);
         if ($$0 instanceof Player) {
            this.awardStat(Stats.PLAYER_KILLS);
            $$2.forAllObjectives(ObjectiveCriteria.KILL_COUNT_PLAYERS, this, ScoreAccess::increment);
         } else {
            this.awardStat(Stats.MOB_KILLS);
         }

         this.handleTeamKill(this, $$0, ObjectiveCriteria.TEAM_KILL);
         this.handleTeamKill($$0, this, ObjectiveCriteria.KILLED_BY_TEAM);
         CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(this, $$0, $$1);
      }
   }

   private void handleTeamKill(ScoreHolder $$0, ScoreHolder $$1, ObjectiveCriteria[] $$2) {
      Scoreboard $$3 = this.level().getScoreboard();
      PlayerTeam $$4 = $$3.getPlayersTeam($$1.getScoreboardName());
      if ($$4 != null) {
         int $$5 = $$4.getColor().getId();
         if ($$5 >= 0 && $$5 < $$2.length) {
            $$3.forAllObjectives($$2[$$5], $$0, ScoreAccess::increment);
         }
      }
   }

   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isInvulnerableTo($$0, $$1)) {
         return false;
      } else {
         Entity $$3 = $$1.getEntity();
         if ($$3 instanceof Player $$4 && !this.canHarmPlayer($$4)) {
            return false;
         } else {
            return $$3 instanceof AbstractArrow $$5 && $$5.getOwner() instanceof Player $$7 && !this.canHarmPlayer($$7)
               ? false
               : super.hurtServer($$0, $$1, $$2);
         }
      }
   }

   public boolean canHarmPlayer(Player $$0) {
      return !this.isPvpAllowed() ? false : super.canHarmPlayer($$0);
   }

   private boolean isPvpAllowed() {
      return this.level().isPvpAllowed();
   }

   public TeleportTransition findRespawnPositionAndUseSpawnBlock(boolean $$0, PostTeleportTransition $$1) {
      ServerPlayer.RespawnConfig $$2 = this.getRespawnConfig();
      ServerLevel $$3 = this.server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault($$2));
      if ($$3 != null && $$2 != null) {
         Optional<ServerPlayer.RespawnPosAngle> $$4 = findRespawnAndUseSpawnBlock($$3, $$2, $$0);
         if ($$4.isPresent()) {
            ServerPlayer.RespawnPosAngle $$5 = $$4.get();
            return new TeleportTransition($$3, $$5.position(), Vec3.ZERO, $$5.yaw(), $$5.pitch(), $$1);
         } else {
            return TeleportTransition.missingRespawnBlock(this, $$1);
         }
      } else {
         return TeleportTransition.createDefault(this, $$1);
      }
   }

   public boolean isReceivingWaypoints() {
      return this.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE) > 0.0;
   }

   protected void onAttributeUpdated(Holder<Attribute> $$0) {
      if ($$0.is(Attributes.WAYPOINT_RECEIVE_RANGE)) {
         ServerWaypointManager $$1 = this.level().getWaypointManager();
         if (this.getAttributes().getValue($$0) > 0.0) {
            $$1.addPlayer(this);
         } else {
            $$1.removePlayer(this);
         }
      }

      super.onAttributeUpdated($$0);
   }

   private static Optional<ServerPlayer.RespawnPosAngle> findRespawnAndUseSpawnBlock(ServerLevel $$0, ServerPlayer.RespawnConfig $$1, boolean $$2) {
      RespawnData $$3 = $$1.respawnData;
      BlockPos $$4 = $$3.pos();
      float $$5 = $$3.yaw();
      float $$6 = $$3.pitch();
      boolean $$7 = $$1.forced;
      BlockState $$8 = $$0.getBlockState($$4);
      Block $$9 = $$8.getBlock();
      if ($$9 instanceof RespawnAnchorBlock && ($$7 || (Integer)$$8.getValue(RespawnAnchorBlock.CHARGE) > 0) && RespawnAnchorBlock.canSetSpawn($$0, $$4)) {
         Optional<Vec3> $$10 = RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, $$0, $$4);
         if (!$$7 && $$2 && $$10.isPresent()) {
            $$0.setBlock($$4, (BlockState)$$8.setValue(RespawnAnchorBlock.CHARGE, (Integer)$$8.getValue(RespawnAnchorBlock.CHARGE) - 1), 3);
         }

         return $$10.map($$1x -> ServerPlayer.RespawnPosAngle.of($$1x, $$4, 0.0F));
      } else if ($$9 instanceof BedBlock && ((BedRule)$$0.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, $$4)).canSetSpawn($$0)) {
         return BedBlock.findStandUpPosition(EntityType.PLAYER, $$0, $$4, (Direction)$$8.getValue(BedBlock.FACING), $$5)
            .map($$1x -> ServerPlayer.RespawnPosAngle.of($$1x, $$4, 0.0F));
      } else if (!$$7) {
         return Optional.empty();
      } else {
         boolean $$11 = $$9.isPossibleToRespawnInThis($$8);
         BlockState $$12 = $$0.getBlockState($$4.above());
         boolean $$13 = $$12.getBlock().isPossibleToRespawnInThis($$12);
         return $$11 && $$13
            ? Optional.of(new ServerPlayer.RespawnPosAngle(new Vec3($$4.getX() + 0.5, $$4.getY() + 0.1, $$4.getZ() + 0.5), $$5, $$6))
            : Optional.empty();
      }
   }

   public void showEndCredits() {
      this.unRide();
      this.level().removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION);
      if (!this.wonGame) {
         this.wonGame = true;
         this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
         this.seenCredits = true;
      }
   }

   
   public ServerPlayer teleport(TeleportTransition $$0) {
      if (this.isRemoved()) {
         return null;
      } else {
         if ($$0.missingRespawnBlock()) {
            this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
         }

         ServerLevel $$1 = $$0.newLevel();
         ServerLevel $$2 = this.level();
         ResourceKey<Level> $$3 = $$2.dimension();
         if (!$$0.asPassenger()) {
            this.removeVehicle();
         }

         if ($$1.dimension() == $$3) {
            this.connection.teleport(PositionMoveRotation.of($$0), $$0.relatives());
            this.connection.resetPosition();
            $$0.postTeleportTransition().onTransition(this);
            return this;
         } else {
            this.isChangingDimension = true;
            LevelData $$4 = $$1.getLevelData();
            this.connection.send(new ClientboundRespawnPacket(this.createCommonSpawnInfo($$1), (byte)3));
            this.connection.send(new ClientboundChangeDifficultyPacket($$4.getDifficulty(), $$4.isDifficultyLocked()));
            PlayerList $$5 = this.server.getPlayerList();
            $$5.sendPlayerPermissionLevel(this);
            $$2.removePlayerImmediately(this, RemovalReason.CHANGED_DIMENSION);
            this.unsetRemoved();
            ProfilerFiller $$6 = Profiler.get();
            $$6.push("moving");
            if ($$3 == Level.OVERWORLD && $$1.dimension() == Level.NETHER) {
               this.enteredNetherPosition = this.position();
            }

            $$6.pop();
            $$6.push("placing");
            this.setServerLevel($$1);
            this.connection.teleport(PositionMoveRotation.of($$0), $$0.relatives());
            this.connection.resetPosition();
            $$1.addDuringTeleport(this);
            $$6.pop();
            this.triggerDimensionChangeTriggers($$2);
            this.stopUsingItem();
            this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
            $$5.sendLevelInfo(this, $$1);
            $$5.sendAllPlayerInfo(this);
            $$5.sendActivePlayerEffects(this);
            $$0.postTeleportTransition().onTransition(this);
            this.lastSentExp = -1;
            this.lastSentHealth = -1.0F;
            this.lastSentFood = -1;
            this.teleportSpectators($$0, $$2);
            return this;
         }
      }
   }

   public void forceSetRotation(float $$0, boolean $$1, float $$2, boolean $$3) {
      super.forceSetRotation($$0, $$1, $$2, $$3);
      this.connection.send(new ClientboundPlayerRotationPacket($$0, $$1, $$2, $$3));
   }

   private void triggerDimensionChangeTriggers(ServerLevel $$0) {
      ResourceKey<Level> $$1 = $$0.dimension();
      ResourceKey<Level> $$2 = this.level().dimension();
      CriteriaTriggers.CHANGED_DIMENSION.trigger(this, $$1, $$2);
      if ($$1 == Level.NETHER && $$2 == Level.OVERWORLD && this.enteredNetherPosition != null) {
         CriteriaTriggers.NETHER_TRAVEL.trigger(this, this.enteredNetherPosition);
      }

      if ($$2 != Level.NETHER) {
         this.enteredNetherPosition = null;
      }
   }

   public boolean broadcastToPlayer(ServerPlayer $$0) {
      if ($$0.isSpectator()) {
         return this.getCamera() == this;
      } else {
         return this.isSpectator() ? false : super.broadcastToPlayer($$0);
      }
   }

   public void take(Entity $$0, int $$1) {
      super.take($$0, $$1);
      this.containerMenu.broadcastChanges();
   }

   public Either<BedSleepingProblem, Unit> startSleepInBed(BlockPos $$0) {
      Direction $$1 = (Direction)this.level().getBlockState($$0).getValue(HorizontalDirectionalBlock.FACING);
      if (!this.isSleeping() && this.isAlive()) {
         BedRule $$2 = (BedRule)this.level().environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, $$0);
         boolean $$3 = $$2.canSleep(this.level());
         boolean $$4 = $$2.canSetSpawn(this.level());
         if (!$$4 && !$$3) {
            return Either.left($$2.asProblem());
         } else if (!this.bedInRange($$0, $$1)) {
            return Either.left(BedSleepingProblem.TOO_FAR_AWAY);
         } else if (this.bedBlocked($$0, $$1)) {
            return Either.left(BedSleepingProblem.OBSTRUCTED);
         } else {
            if ($$4) {
               this.setRespawnPosition(
                  new ServerPlayer.RespawnConfig(RespawnData.of(this.level().dimension(), $$0, this.getYRot(), this.getXRot()), false), true
               );
            }

            if (!$$3) {
               return Either.left($$2.asProblem());
            } else {
               if (!this.isCreative()) {
                  double $$5 = 8.0;
                  double $$6 = 5.0;
                  Vec3 $$7 = Vec3.atBottomCenterOf($$0);
                  List<Monster> $$8 = this.level()
                     .getEntitiesOfClass(
                        Monster.class,
                        new AABB($$7.x() - 8.0, $$7.y() - 5.0, $$7.z() - 8.0, $$7.x() + 8.0, $$7.y() + 5.0, $$7.z() + 8.0),
                        $$0x -> $$0x.isPreventingPlayerRest(this.level(), this)
                     );
                  if (!$$8.isEmpty()) {
                     return Either.left(BedSleepingProblem.NOT_SAFE);
                  }
               }

               Either<BedSleepingProblem, Unit> $$9 = super.startSleepInBed($$0).ifRight($$0x -> {
                  this.awardStat(Stats.SLEEP_IN_BED);
                  CriteriaTriggers.SLEPT_IN_BED.trigger(this);
               });
               if (!this.level().canSleepThroughNights()) {
                  this.displayClientMessage(Component.translatable("sleep.not_possible"), true);
               }

               this.level().updateSleepingPlayerList();
               return $$9;
            }
         }
      } else {
         return Either.left(BedSleepingProblem.OTHER_PROBLEM);
      }
   }

   public void startSleeping(BlockPos $$0) {
      this.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
      super.startSleeping($$0);
   }

   private boolean bedInRange(BlockPos $$0, Direction $$1) {
      return this.isReachableBedBlock($$0) || this.isReachableBedBlock($$0.relative($$1.getOpposite()));
   }

   private boolean isReachableBedBlock(BlockPos $$0) {
      Vec3 $$1 = Vec3.atBottomCenterOf($$0);
      return Math.abs(this.getX() - $$1.x()) <= 3.0 && Math.abs(this.getY() - $$1.y()) <= 2.0 && Math.abs(this.getZ() - $$1.z()) <= 3.0;
   }

   private boolean bedBlocked(BlockPos $$0, Direction $$1) {
      BlockPos $$2 = $$0.above();
      return !this.freeAt($$2) || !this.freeAt($$2.relative($$1.getOpposite()));
   }

   public void stopSleepInBed(boolean $$0, boolean $$1) {
      if (this.isSleeping()) {
         this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket(this, 2));
      }

      super.stopSleepInBed($$0, $$1);
      if (this.connection != null) {
         this.connection.teleport(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
      }
   }

   public boolean isInvulnerableTo(ServerLevel $$0, DamageSource $$1) {
      return super.isInvulnerableTo($$0, $$1) || this.isChangingDimension() && !$$1.is(DamageTypes.ENDER_PEARL) || !this.connection.hasClientLoaded();
   }

   protected void onChangedBlock(ServerLevel $$0, BlockPos $$1) {
      if (!this.isSpectator()) {
         super.onChangedBlock($$0, $$1);
      }
   }

   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
      if (this.spawnExtraParticlesOnFall && $$1 && this.fallDistance > 0.0) {
         Vec3 $$4 = $$3.getCenter().add(0.0, 0.5, 0.0);
         int $$5 = (int)Mth.clamp(50.0 * this.fallDistance, 0.0, 200.0);
         this.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, $$2), $$4.x, $$4.y, $$4.z, $$5, 0.3F, 0.3F, 0.3F, 0.15F);
         this.spawnExtraParticlesOnFall = false;
      }

      super.checkFallDamage($$0, $$1, $$2, $$3);
   }

   public void onExplosionHit(Entity $$0) {
      super.onExplosionHit($$0);
      this.currentImpulseImpactPos = this.position();
      this.currentExplosionCause = $$0;
      this.setIgnoreFallDamageFromCurrentImpulse($$0 != null && $$0.getType() == EntityType.WIND_CHARGE);
   }

   protected void pushEntities() {
      if (this.level().tickRateManager().runsNormally()) {
         super.pushEntities();
      }
   }

   public void openTextEdit(SignBlockEntity $$0, boolean $$1) {
      this.connection.send(new ClientboundBlockUpdatePacket(this.level(), $$0.getBlockPos()));
      this.connection.send(new ClientboundOpenSignEditorPacket($$0.getBlockPos(), $$1));
   }

   public void openDialog(Holder<Dialog> $$0) {
      this.connection.send(new ClientboundShowDialogPacket($$0));
   }

   private void nextContainerCounter() {
      this.containerCounter = this.containerCounter % 100 + 1;
   }

   public OptionalInt openMenu(MenuProvider $$0) {
      if ($$0 == null) {
         return OptionalInt.empty();
      } else {
         if (this.containerMenu != this.inventoryMenu) {
            this.closeContainer();
         }

         this.nextContainerCounter();
         AbstractContainerMenu $$1 = $$0.createMenu(this.containerCounter, this.getInventory(), this);
         if ($$1 == null) {
            if (this.isSpectator()) {
               this.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
            }

            return OptionalInt.empty();
         } else {
            this.connection.send(new ClientboundOpenScreenPacket($$1.containerId, $$1.getType(), $$0.getDisplayName()));
            this.initMenu($$1);
            this.containerMenu = $$1;
            return OptionalInt.of(this.containerCounter);
         }
      }
   }

   public void sendMerchantOffers(int $$0, MerchantOffers $$1, int $$2, int $$3, boolean $$4, boolean $$5) {
      this.connection.send(new ClientboundMerchantOffersPacket($$0, $$1, $$2, $$3, $$4, $$5));
   }

   public void openHorseInventory(AbstractHorse $$0, Container $$1) {
      if (this.containerMenu != this.inventoryMenu) {
         this.closeContainer();
      }

      this.nextContainerCounter();
      int $$2 = $$0.getInventoryColumns();
      this.connection.send(new ClientboundMountScreenOpenPacket(this.containerCounter, $$2, $$0.getId()));
      this.containerMenu = new HorseInventoryMenu(this.containerCounter, this.getInventory(), $$1, $$0, $$2);
      this.initMenu(this.containerMenu);
   }

   public void openNautilusInventory(AbstractNautilus $$0, Container $$1) {
      if (this.containerMenu != this.inventoryMenu) {
         this.closeContainer();
      }

      this.nextContainerCounter();
      int $$2 = $$0.getInventoryColumns();
      this.connection.send(new ClientboundMountScreenOpenPacket(this.containerCounter, $$2, $$0.getId()));
      this.containerMenu = new NautilusInventoryMenu(this.containerCounter, this.getInventory(), $$1, $$0, $$2);
      this.initMenu(this.containerMenu);
   }

   public void openItemGui(ItemStack $$0, InteractionHand $$1) {
      if ($$0.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
         if (WrittenBookContent.resolveForItem($$0, this.createCommandSourceStack(), this)) {
            this.containerMenu.broadcastChanges();
         }

         this.connection.send(new ClientboundOpenBookPacket($$1));
      }
   }

   public void openCommandBlock(CommandBlockEntity $$0) {
      this.connection.send(ClientboundBlockEntityDataPacket.create($$0, BlockEntity::saveCustomOnly));
   }

   public void closeContainer() {
      this.connection.send(new ClientboundContainerClosePacket(this.containerMenu.containerId));
      this.doCloseContainer();
   }

   public void doCloseContainer() {
      this.containerMenu.removed(this);
      this.inventoryMenu.transferState(this.containerMenu);
      this.containerMenu = this.inventoryMenu;
   }

   public void rideTick() {
      double $$0 = this.getX();
      double $$1 = this.getY();
      double $$2 = this.getZ();
      super.rideTick();
      this.checkRidingStatistics(this.getX() - $$0, this.getY() - $$1, this.getZ() - $$2);
   }

   public void checkMovementStatistics(double $$0, double $$1, double $$2) {
      if (!this.isPassenger() && !didNotMove($$0, $$1, $$2)) {
         if (this.isSwimming()) {
            int $$3 = Math.round((float)Math.sqrt($$0 * $$0 + $$1 * $$1 + $$2 * $$2) * 100.0F);
            if ($$3 > 0) {
               this.awardStat(Stats.SWIM_ONE_CM, $$3);
               this.causeFoodExhaustion(0.01F * $$3 * 0.01F);
            }
         } else if (this.isEyeInFluid(FluidTags.WATER)) {
            int $$4 = Math.round((float)Math.sqrt($$0 * $$0 + $$1 * $$1 + $$2 * $$2) * 100.0F);
            if ($$4 > 0) {
               this.awardStat(Stats.WALK_UNDER_WATER_ONE_CM, $$4);
               this.causeFoodExhaustion(0.01F * $$4 * 0.01F);
            }
         } else if (this.isInWater()) {
            int $$5 = Math.round((float)Math.sqrt($$0 * $$0 + $$2 * $$2) * 100.0F);
            if ($$5 > 0) {
               this.awardStat(Stats.WALK_ON_WATER_ONE_CM, $$5);
               this.causeFoodExhaustion(0.01F * $$5 * 0.01F);
            }
         } else if (this.onClimbable()) {
            if ($$1 > 0.0) {
               this.awardStat(Stats.CLIMB_ONE_CM, (int)Math.round($$1 * 100.0));
            }
         } else if (this.onGround()) {
            int $$6 = Math.round((float)Math.sqrt($$0 * $$0 + $$2 * $$2) * 100.0F);
            if ($$6 > 0) {
               if (this.isSprinting()) {
                  this.awardStat(Stats.SPRINT_ONE_CM, $$6);
                  this.causeFoodExhaustion(0.1F * $$6 * 0.01F);
               } else if (this.isCrouching()) {
                  this.awardStat(Stats.CROUCH_ONE_CM, $$6);
                  this.causeFoodExhaustion(0.0F * $$6 * 0.01F);
               } else {
                  this.awardStat(Stats.WALK_ONE_CM, $$6);
                  this.causeFoodExhaustion(0.0F * $$6 * 0.01F);
               }
            }
         } else if (this.isFallFlying()) {
            int $$7 = Math.round((float)Math.sqrt($$0 * $$0 + $$1 * $$1 + $$2 * $$2) * 100.0F);
            this.awardStat(Stats.AVIATE_ONE_CM, $$7);
         } else {
            int $$8 = Math.round((float)Math.sqrt($$0 * $$0 + $$2 * $$2) * 100.0F);
            if ($$8 > 25) {
               this.awardStat(Stats.FLY_ONE_CM, $$8);
            }
         }
      }
   }

   private void checkRidingStatistics(double $$0, double $$1, double $$2) {
      if (this.isPassenger() && !didNotMove($$0, $$1, $$2)) {
         int $$3 = Math.round((float)Math.sqrt($$0 * $$0 + $$1 * $$1 + $$2 * $$2) * 100.0F);
         Entity $$4 = this.getVehicle();
         if ($$4 instanceof AbstractMinecart) {
            this.awardStat(Stats.MINECART_ONE_CM, $$3);
         } else if ($$4 instanceof AbstractBoat) {
            this.awardStat(Stats.BOAT_ONE_CM, $$3);
         } else if ($$4 instanceof Pig) {
            this.awardStat(Stats.PIG_ONE_CM, $$3);
         } else if ($$4 instanceof AbstractHorse) {
            this.awardStat(Stats.HORSE_ONE_CM, $$3);
         } else if ($$4 instanceof Strider) {
            this.awardStat(Stats.STRIDER_ONE_CM, $$3);
         } else if ($$4 instanceof HappyGhast) {
            this.awardStat(Stats.HAPPY_GHAST_ONE_CM, $$3);
         } else if ($$4 instanceof AbstractNautilus) {
            this.awardStat(Stats.NAUTILUS_ONE_CM, $$3);
         }
      }
   }

   private static boolean didNotMove(double $$0, double $$1, double $$2) {
      return $$0 == 0.0 && $$1 == 0.0 && $$2 == 0.0;
   }

   public void awardStat(Stat<?> $$0, int $$1) {
      this.stats.increment(this, $$0, $$1);
      this.level().getScoreboard().forAllObjectives($$0, this, $$1x -> $$1x.add($$1));
   }

   public void resetStat(Stat<?> $$0) {
      this.stats.setValue(this, $$0, 0);
      this.level().getScoreboard().forAllObjectives($$0, this, ScoreAccess::reset);
   }

   public int awardRecipes(Collection<RecipeHolder<?>> $$0) {
      return this.recipeBook.addRecipes($$0, this);
   }

   public void triggerRecipeCrafted(RecipeHolder<?> $$0, List<ItemStack> $$1) {
      CriteriaTriggers.RECIPE_CRAFTED.trigger(this, $$0.id(), $$1);
   }

   public void awardRecipesByKey(List<ResourceKey<Recipe<?>>> $$0) {
      List<RecipeHolder<?>> $$1 = $$0.stream().flatMap($$0x -> this.server.getRecipeManager().byKey($$0x).stream()).collect(Collectors.toList());
      this.awardRecipes($$1);
   }

   public int resetRecipes(Collection<RecipeHolder<?>> $$0) {
      return this.recipeBook.removeRecipes($$0, this);
   }

   public void jumpFromGround() {
      super.jumpFromGround();
      this.awardStat(Stats.JUMP);
      if (this.isSprinting()) {
         this.causeFoodExhaustion(0.2F);
      } else {
         this.causeFoodExhaustion(0.05F);
      }
   }

   public void giveExperiencePoints(int $$0) {
      if ($$0 != 0) {
         super.giveExperiencePoints($$0);
         this.lastSentExp = -1;
      }
   }

   public void disconnect() {
      this.disconnected = true;
      this.ejectPassengers();
      if (this.isSleeping()) {
         this.stopSleepInBed(true, false);
      }
   }

   public boolean hasDisconnected() {
      return this.disconnected;
   }

   public void resetSentInfo() {
      this.lastSentHealth = -1.0E8F;
   }

   public void displayClientMessage(Component $$0, boolean $$1) {
      this.sendSystemMessage($$0, $$1);
   }

   protected void completeUsingItem() {
      if (!this.useItem.isEmpty() && this.isUsingItem()) {
         this.connection.send(new ClientboundEntityEventPacket(this, (byte)9));
         super.completeUsingItem();
      }
   }

   public void lookAt(Anchor $$0, Vec3 $$1) {
      super.lookAt($$0, $$1);
      this.connection.send(new ClientboundPlayerLookAtPacket($$0, $$1.x, $$1.y, $$1.z));
   }

   public void lookAt(Anchor $$0, Entity $$1, Anchor $$2) {
      Vec3 $$3 = $$2.apply($$1);
      super.lookAt($$0, $$3);
      this.connection.send(new ClientboundPlayerLookAtPacket($$0, $$1, $$2));
   }

   public void restoreFrom(ServerPlayer $$0, boolean $$1) {
      this.wardenSpawnTracker = $$0.wardenSpawnTracker;
      this.chatSession = $$0.chatSession;
      this.gameMode.setGameModeForPlayer($$0.gameMode.getGameModeForPlayer(), $$0.gameMode.getPreviousGameModeForPlayer());
      this.onUpdateAbilities();
      this.getAttributes().assignBaseValues($$0.getAttributes());
      if ($$1) {
         this.getAttributes().assignPermanentModifiers($$0.getAttributes());
         this.setHealth($$0.getHealth());
         this.foodData = $$0.foodData;

         for (MobEffectInstance $$2 : $$0.getActiveEffects()) {
            this.addEffect(new MobEffectInstance($$2));
         }

         this.transferInventoryXpAndScore($$0);
         this.portalProcess = $$0.portalProcess;
      } else {
         this.setHealth(this.getMaxHealth());
         if ((Boolean)this.level().getGameRules().get(GameRules.KEEP_INVENTORY) || $$0.isSpectator()) {
            this.transferInventoryXpAndScore($$0);
         }
      }

      this.enchantmentSeed = $$0.enchantmentSeed;
      this.enderChestInventory = $$0.enderChestInventory;
      this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (Byte)$$0.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
      this.lastSentExp = -1;
      this.lastSentHealth = -1.0F;
      this.lastSentFood = -1;
      this.recipeBook.copyOverData($$0.recipeBook);
      this.seenCredits = $$0.seenCredits;
      this.enteredNetherPosition = $$0.enteredNetherPosition;
      this.chunkTrackingView = $$0.chunkTrackingView;
      this.requestedDebugSubscriptions = $$0.requestedDebugSubscriptions;
      this.setShoulderEntityLeft($$0.getShoulderEntityLeft());
      this.setShoulderEntityRight($$0.getShoulderEntityRight());
      this.setLastDeathLocation($$0.getLastDeathLocation());
      this.waypointIcon().copyFrom($$0.waypointIcon());
   }

   private void transferInventoryXpAndScore(Player $$0) {
      this.getInventory().replaceWith($$0.getInventory());
      this.experienceLevel = $$0.experienceLevel;
      this.totalExperience = $$0.totalExperience;
      this.experienceProgress = $$0.experienceProgress;
      this.setScore($$0.getScore());
   }

   protected void onEffectAdded(MobEffectInstance $$0, Entity $$1) {
      super.onEffectAdded($$0, $$1);
      this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), $$0, true));
      if ($$0.is(MobEffects.LEVITATION)) {
         this.levitationStartTime = this.tickCount;
         this.levitationStartPos = this.position();
      }

      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, $$1);
   }

   protected void onEffectUpdated(MobEffectInstance $$0, boolean $$1, Entity $$2) {
      super.onEffectUpdated($$0, $$1, $$2);
      this.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), $$0, false));
      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, $$2);
   }

   protected void onEffectsRemoved(Collection<MobEffectInstance> $$0) {
      super.onEffectsRemoved($$0);

      for (MobEffectInstance $$1 : $$0) {
         this.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), $$1.getEffect()));
         if ($$1.is(MobEffects.LEVITATION)) {
            this.levitationStartPos = null;
         }
      }

      CriteriaTriggers.EFFECTS_CHANGED.trigger(this, null);
   }

   public void teleportTo(double $$0, double $$1, double $$2) {
      this.connection
         .teleport(new PositionMoveRotation(new Vec3($$0, $$1, $$2), Vec3.ZERO, 0.0F, 0.0F), Relative.union(new Set[]{Relative.DELTA, Relative.ROTATION}));
   }

   public void teleportRelative(double $$0, double $$1, double $$2) {
      this.connection.teleport(new PositionMoveRotation(new Vec3($$0, $$1, $$2), Vec3.ZERO, 0.0F, 0.0F), Relative.ALL);
   }

   public boolean teleportTo(ServerLevel $$0, double $$1, double $$2, double $$3, Set<Relative> $$4, float $$5, float $$6, boolean $$7) {
      if (this.isSleeping()) {
         this.stopSleepInBed(true, true);
      }

      if ($$7) {
         this.setCamera(this);
      }

      boolean $$8 = super.teleportTo($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      if ($$8) {
         this.setYHeadRot($$4.contains(Relative.Y_ROT) ? this.getYHeadRot() + $$5 : $$5);
         this.connection.resetFlyingTicks();
      }

      return $$8;
   }

   public void snapTo(double $$0, double $$1, double $$2) {
      super.snapTo($$0, $$1, $$2);
      this.connection.resetPosition();
   }

   public void crit(Entity $$0) {
      this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket($$0, 4));
   }

   public void magicCrit(Entity $$0) {
      this.level().getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundAnimatePacket($$0, 5));
   }

   public void onUpdateAbilities() {
      if (this.connection != null) {
         this.connection.send(new ClientboundPlayerAbilitiesPacket(this.getAbilities()));
         this.updateInvisibilityStatus();
      }
   }

   public ServerLevel level() {
      return (ServerLevel)super.level();
   }

   public boolean setGameMode(GameType $$0) {
      boolean $$1 = this.isSpectator();
      if (!this.gameMode.changeGameModeForPlayer($$0)) {
         return false;
      } else {
         this.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, $$0.getId()));
         if ($$0 == GameType.SPECTATOR) {
            this.removeEntitiesOnShoulder();
            this.stopRiding();
            this.stopUsingItem();
            EnchantmentHelper.stopLocationBasedEffects(this);
         } else {
            this.setCamera(this);
            if ($$1) {
               EnchantmentHelper.runLocationChangedEffects(this.level(), this);
            }
         }

         this.onUpdateAbilities();
         this.updateEffectVisibility();
         return true;
      }
   }

   public GameType gameMode() {
      return this.gameMode.getGameModeForPlayer();
   }

   public CommandSource commandSource() {
      return this.commandSource;
   }

   public CommandSourceStack createCommandSourceStack() {
      return new CommandSourceStack(
         this.commandSource(),
         this.position(),
         this.getRotationVector(),
         this.level(),
         this.permissions(),
         this.getPlainTextName(),
         this.getDisplayName(),
         this.server,
         this
      );
   }

   public void sendSystemMessage(Component $$0) {
      this.sendSystemMessage($$0, false);
   }

   public void sendSystemMessage(Component $$0, boolean $$1) {
      if (this.acceptsSystemMessages($$1)) {
         this.connection
            .send(
               new ClientboundSystemChatPacket($$0, $$1),
               PacketSendListener.exceptionallySend(
                  () -> {
                     if (this.acceptsSystemMessages(false)) {
                        int $$1x = 256;
                        String $$2 = $$0.getString(256);
                        Component $$3 = Component.literal($$2).withStyle(ChatFormatting.YELLOW);
                        return new ClientboundSystemChatPacket(
                           Component.translatable("multiplayer.message_not_delivered", new Object[]{$$3}).withStyle(ChatFormatting.RED), false
                        );
                     } else {
                        return null;
                     }
                  }
               )
            );
      }
   }

   public void sendChatMessage(OutgoingChatMessage $$0, boolean $$1, Bound $$2) {
      if (this.acceptsChatMessages()) {
         $$0.sendToPlayer(this, $$1, $$2);
      }
   }

   public String getIpAddress() {
      return this.connection.getRemoteAddress() instanceof InetSocketAddress $$1 ? InetAddresses.toAddrString($$1.getAddress()) : "<unknown>";
   }

   public void updateOptions(ClientInformation $$0) {
      this.language = $$0.language();
      this.requestedViewDistance = $$0.viewDistance();
      this.chatVisibility = $$0.chatVisibility();
      this.canChatColor = $$0.chatColors();
      this.textFilteringEnabled = $$0.textFilteringEnabled();
      this.allowsListing = $$0.allowsListing();
      this.particleStatus = $$0.particleStatus();
      this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, (byte)$$0.modelCustomisation());
      this.getEntityData().set(DATA_PLAYER_MAIN_HAND, $$0.mainHand());
   }

   public ClientInformation clientInformation() {
      int $$0 = (Byte)this.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION);
      return new ClientInformation(
         this.language,
         this.requestedViewDistance,
         this.chatVisibility,
         this.canChatColor,
         $$0,
         this.getMainArm(),
         this.textFilteringEnabled,
         this.allowsListing,
         this.particleStatus
      );
   }

   public boolean canChatInColor() {
      return this.canChatColor;
   }

   public ChatVisiblity getChatVisibility() {
      return this.chatVisibility;
   }

   private boolean acceptsSystemMessages(boolean $$0) {
      return this.chatVisibility == ChatVisiblity.HIDDEN ? $$0 : true;
   }

   private boolean acceptsChatMessages() {
      return this.chatVisibility == ChatVisiblity.FULL;
   }

   public int requestedViewDistance() {
      return this.requestedViewDistance;
   }

   public void sendServerStatus(ServerStatus $$0) {
      this.connection.send(new ClientboundServerDataPacket($$0.description(), $$0.favicon().map(Favicon::iconBytes)));
   }

   public PermissionSet permissions() {
      return this.server.getProfilePermissions(this.nameAndId());
   }

   public void resetLastActionTime() {
      this.lastActionTime = Util.getMillis();
   }

   public ServerStatsCounter getStats() {
      return this.stats;
   }

   public ServerRecipeBook getRecipeBook() {
      return this.recipeBook;
   }

   protected void updateInvisibilityStatus() {
      if (this.isSpectator()) {
         this.removeEffectParticles();
         this.setInvisible(true);
      } else {
         super.updateInvisibilityStatus();
      }
   }

   public Entity getCamera() {
      return (Entity)(this.camera == null ? this : this.camera);
   }

   public void setCamera(Entity $$0) {
      Entity $$1 = this.getCamera();
      this.camera = (Entity)($$0 == null ? this : $$0);
      if ($$1 != this.camera) {
         if (this.camera.level() instanceof ServerLevel $$2) {
            this.teleportTo($$2, this.camera.getX(), this.camera.getY(), this.camera.getZ(), Set.of(), this.getYRot(), this.getXRot(), false);
         }

         if ($$0 != null) {
            this.level().getChunkSource().move(this);
         }

         this.connection.send(new ClientboundSetCameraPacket(this.camera));
         this.connection.resetPosition();
      }
   }

   protected void processPortalCooldown() {
      if (!this.isChangingDimension) {
         super.processPortalCooldown();
      }
   }

   public void attack(Entity $$0) {
      if (this.isSpectator()) {
         this.setCamera($$0);
      } else {
         super.attack($$0);
      }
   }

   public long getLastActionTime() {
      return this.lastActionTime;
   }

   
   public Component getTabListDisplayName() {
      return null;
   }

   public int getTabListOrder() {
      return 0;
   }

   public void swing(InteractionHand $$0) {
      super.swing($$0);
      this.resetAttackStrengthTicker();
   }

   public boolean isChangingDimension() {
      return this.isChangingDimension;
   }

   public void hasChangedDimension() {
      this.isChangingDimension = false;
   }

   public net.minecraft.server.PlayerAdvancements getAdvancements() {
      return this.advancements;
   }

   
   public ServerPlayer.RespawnConfig getRespawnConfig() {
      return this.respawnConfig;
   }

   public void copyRespawnPosition(ServerPlayer $$0) {
      this.setRespawnPosition($$0.respawnConfig, false);
   }

   public void setRespawnPosition(ServerPlayer.RespawnConfig $$0, boolean $$1) {
      if ($$1 && $$0 != null && !$$0.isSamePosition(this.respawnConfig)) {
         this.sendSystemMessage(SPAWN_SET_MESSAGE);
      }

      this.respawnConfig = $$0;
   }

   public SectionPos getLastSectionPos() {
      return this.lastSectionPos;
   }

   public void setLastSectionPos(SectionPos $$0) {
      this.lastSectionPos = $$0;
   }

   public ChunkTrackingView getChunkTrackingView() {
      return this.chunkTrackingView;
   }

   public void setChunkTrackingView(ChunkTrackingView $$0) {
      this.chunkTrackingView = $$0;
   }

   public ItemEntity drop(ItemStack $$0, boolean $$1, boolean $$2) {
      ItemEntity $$3 = super.drop($$0, $$1, $$2);
      if ($$2) {
         ItemStack $$4 = $$3 != null ? $$3.getItem() : ItemStack.EMPTY;
         if (!$$4.isEmpty()) {
            this.awardStat(Stats.ITEM_DROPPED.get($$4.getItem()), $$0.getCount());
            this.awardStat(Stats.DROP);
         }
      }

      return $$3;
   }

   public TextFilter getTextFilter() {
      return this.textFilter;
   }

   public void setServerLevel(ServerLevel $$0) {
      this.setLevel($$0);
      this.gameMode.setLevel($$0);
   }

   
   private static GameType readPlayerMode(ValueInput $$0, String $$1) {
      return (GameType)$$0.read($$1, GameType.LEGACY_ID_CODEC).orElse(null);
   }

   private GameType calculateGameModeForNewPlayer(GameType $$0) {
      GameType $$1 = this.server.getForcedGameType();
      if ($$1 != null) {
         return $$1;
      } else {
         return $$0 != null ? $$0 : this.server.getDefaultGameType();
      }
   }

   private void storeGameTypes(ValueOutput $$0) {
      $$0.store("playerGameType", GameType.LEGACY_ID_CODEC, this.gameMode.getGameModeForPlayer());
      GameType $$1 = this.gameMode.getPreviousGameModeForPlayer();
      $$0.storeNullable("previousPlayerGameType", GameType.LEGACY_ID_CODEC, $$1);
   }

   public boolean isTextFilteringEnabled() {
      return this.textFilteringEnabled;
   }

   public boolean shouldFilterMessageTo(ServerPlayer $$0) {
      return $$0 == this ? false : this.textFilteringEnabled || $$0.textFilteringEnabled;
   }

   public boolean mayInteract(ServerLevel $$0, BlockPos $$1) {
      return super.mayInteract($$0, $$1) && $$0.mayInteract(this, $$1);
   }

   protected void updateUsingItem(ItemStack $$0) {
      CriteriaTriggers.USING_ITEM.trigger(this, $$0);
      super.updateUsingItem($$0);
   }

   public void drop(boolean $$0) {
      Inventory $$1 = this.getInventory();
      ItemStack $$2 = $$1.removeFromSelected($$0);
      this.containerMenu.findSlot($$1, $$1.getSelectedSlot()).ifPresent($$1x -> this.containerMenu.setRemoteSlot($$1x, $$1.getSelectedItem()));
      if (this.useItem.isEmpty()) {
         this.stopUsingItem();
      }

      this.drop($$2, false, true);
   }

   public void handleExtraItemsCreatedOnUse(ItemStack $$0) {
      if (!this.getInventory().add($$0)) {
         this.drop($$0, false);
      }
   }

   public boolean allowsListing() {
      return this.allowsListing;
   }

   public Optional<WardenSpawnTracker> getWardenSpawnTracker() {
      return Optional.of(this.wardenSpawnTracker);
   }

   public void setSpawnExtraParticlesOnFall(boolean $$0) {
      this.spawnExtraParticlesOnFall = $$0;
   }

   public void onItemPickup(ItemEntity $$0) {
      super.onItemPickup($$0);
      Entity $$1 = $$0.getOwner();
      if ($$1 != null) {
         CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.trigger(this, $$0.getItem(), $$1);
      }
   }

   public void setChatSession(RemoteChatSession $$0) {
      this.chatSession = $$0;
   }

   
   public RemoteChatSession getChatSession() {
      return this.chatSession != null && this.chatSession.hasExpired() ? null : this.chatSession;
   }

   public void indicateDamage(double $$0, double $$1) {
      this.hurtDir = (float)(Mth.atan2($$1, $$0) * 180.0F / (float)Math.PI - this.getYRot());
      this.connection.send(new ClientboundHurtAnimationPacket(this));
   }

   public boolean startRiding(Entity $$0, boolean $$1, boolean $$2) {
      if (super.startRiding($$0, $$1, $$2)) {
         $$0.positionRider(this);
         this.connection.teleport(new PositionMoveRotation(this.position(), Vec3.ZERO, 0.0F, 0.0F), Relative.ROTATION);
         if ($$0 instanceof LivingEntity $$3) {
            this.server.getPlayerList().sendActiveEffects($$3, this.connection);
         }

         this.connection.send(new ClientboundSetPassengersPacket($$0));
         return true;
      } else {
         return false;
      }
   }

   public void removeVehicle() {
      Entity $$0 = this.getVehicle();
      super.removeVehicle();
      if ($$0 instanceof LivingEntity $$1) {
         for (MobEffectInstance $$2 : $$1.getActiveEffects()) {
            this.connection.send(new ClientboundRemoveMobEffectPacket($$0.getId(), $$2.getEffect()));
         }
      }

      if ($$0 != null) {
         this.connection.send(new ClientboundSetPassengersPacket($$0));
      }
   }

   public CommonPlayerSpawnInfo createCommonSpawnInfo(ServerLevel $$0) {
      return new CommonPlayerSpawnInfo(
         $$0.dimensionTypeRegistration(),
         $$0.dimension(),
         BiomeManager.obfuscateSeed($$0.getSeed()),
         this.gameMode.getGameModeForPlayer(),
         this.gameMode.getPreviousGameModeForPlayer(),
         $$0.isDebug(),
         $$0.isFlat(),
         this.getLastDeathLocation(),
         this.getPortalCooldown(),
         $$0.getSeaLevel()
      );
   }

   public void setRaidOmenPosition(BlockPos $$0) {
      this.raidOmenPosition = $$0;
   }

   public void clearRaidOmenPosition() {
      this.raidOmenPosition = null;
   }

   
   public BlockPos getRaidOmenPosition() {
      return this.raidOmenPosition;
   }

   public Vec3 getKnownMovement() {
      Entity $$0 = this.getVehicle();
      return $$0 != null && $$0.getControllingPassenger() != this ? $$0.getKnownMovement() : this.lastKnownClientMovement;
   }

   public Vec3 getKnownSpeed() {
      Entity $$0 = this.getVehicle();
      return $$0 != null && $$0.getControllingPassenger() != this ? $$0.getKnownSpeed() : this.lastKnownClientMovement;
   }

   public void setKnownMovement(Vec3 $$0) {
      this.lastKnownClientMovement = $$0;
   }

   protected float getEnchantedDamage(Entity $$0, float $$1, DamageSource $$2) {
      return EnchantmentHelper.modifyDamage(this.level(), this.getWeaponItem(), $$0, $$2, $$1);
   }

   public void onEquippedItemBroken(Item $$0, EquipmentSlot $$1) {
      super.onEquippedItemBroken($$0, $$1);
      this.awardStat(Stats.ITEM_BROKEN.get($$0));
   }

   public Input getLastClientInput() {
      return this.lastClientInput;
   }

   public void setLastClientInput(Input $$0) {
      this.lastClientInput = $$0;
   }

   public Vec3 getLastClientMoveIntent() {
      float $$0 = this.lastClientInput.left() == this.lastClientInput.right() ? 0.0F : (this.lastClientInput.left() ? 1.0F : -1.0F);
      float $$1 = this.lastClientInput.forward() == this.lastClientInput.backward() ? 0.0F : (this.lastClientInput.forward() ? 1.0F : -1.0F);
      return getInputVector(new Vec3($$0, 0.0, $$1), 1.0F, this.getYRot());
   }

   public void registerEnderPearl(ThrownEnderpearl $$0) {
      this.enderPearls.add($$0);
   }

   public void deregisterEnderPearl(ThrownEnderpearl $$0) {
      this.enderPearls.remove($$0);
   }

   public Set<ThrownEnderpearl> getEnderPearls() {
      return this.enderPearls;
   }

   public CompoundTag getShoulderEntityLeft() {
      return this.shoulderEntityLeft;
   }

   protected void setShoulderEntityLeft(CompoundTag $$0) {
      this.shoulderEntityLeft = $$0;
      this.setShoulderParrotLeft(extractParrotVariant($$0));
   }

   public CompoundTag getShoulderEntityRight() {
      return this.shoulderEntityRight;
   }

   protected void setShoulderEntityRight(CompoundTag $$0) {
      this.shoulderEntityRight = $$0;
      this.setShoulderParrotRight(extractParrotVariant($$0));
   }

   public long registerAndUpdateEnderPearlTicket(ThrownEnderpearl $$0) {
      if ($$0.level() instanceof ServerLevel $$1) {
         ChunkPos $$2 = $$0.chunkPosition();
         this.registerEnderPearl($$0);
         $$1.resetEmptyTime();
         return placeEnderPearlTicket($$1, $$2) - 1L;
      } else {
         return 0L;
      }
   }

   public static long placeEnderPearlTicket(ServerLevel $$0, ChunkPos $$1) {
      $$0.getChunkSource().addTicketWithRadius(TicketType.ENDER_PEARL, $$1, 2);
      return TicketType.ENDER_PEARL.timeout();
   }

   public void requestDebugSubscriptions(Set<DebugSubscription<?>> $$0) {
      this.requestedDebugSubscriptions = Set.copyOf($$0);
   }

   public Set<DebugSubscription<?>> debugSubscriptions() {
      return !this.server.debugSubscribers().hasRequiredPermissions(this) ? Set.of() : this.requestedDebugSubscriptions;
   }

   public record RespawnConfig(RespawnData respawnData, boolean forced) {
      public static final Codec<ServerPlayer.RespawnConfig> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               RespawnData.MAP_CODEC.forGetter(ServerPlayer.RespawnConfig::respawnData),
               Codec.BOOL.optionalFieldOf("forced", false).forGetter(ServerPlayer.RespawnConfig::forced)
            )
            .apply($$0, ServerPlayer.RespawnConfig::new)
      );

      static ResourceKey<Level> getDimensionOrDefault(ServerPlayer.RespawnConfig $$0) {
         return $$0 != null ? $$0.respawnData().dimension() : Level.OVERWORLD;
      }

      public boolean isSamePosition(ServerPlayer.RespawnConfig $$0) {
         return $$0 != null && this.respawnData.globalPos().equals($$0.respawnData.globalPos());
      }
   }

   record RespawnPosAngle(Vec3 position, float yaw, float pitch) {
      public static ServerPlayer.RespawnPosAngle of(Vec3 $$0, BlockPos $$1, float $$2) {
         return new ServerPlayer.RespawnPosAngle($$0, calculateLookAtYaw($$0, $$1), $$2);
      }

      private static float calculateLookAtYaw(Vec3 $$0, BlockPos $$1) {
         Vec3 $$2 = Vec3.atBottomCenterOf($$1).subtract($$0).normalize();
         return (float)Mth.wrapDegrees(Mth.atan2($$2.z, $$2.x) * 180.0F / (float)Math.PI - 90.0);
      }
   }

   public record SavedPosition(Optional<ResourceKey<Level>> dimension, Optional<Vec3> position, Optional<Vec2> rotation) {
      public static final MapCodec<ServerPlayer.SavedPosition> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Level.RESOURCE_KEY_CODEC.optionalFieldOf("Dimension").forGetter(ServerPlayer.SavedPosition::dimension),
               Vec3.CODEC.optionalFieldOf("Pos").forGetter(ServerPlayer.SavedPosition::position),
               Vec2.CODEC.optionalFieldOf("Rotation").forGetter(ServerPlayer.SavedPosition::rotation)
            )
            .apply($$0, ServerPlayer.SavedPosition::new)
      );
      public static final ServerPlayer.SavedPosition EMPTY = new ServerPlayer.SavedPosition(Optional.empty(), Optional.empty(), Optional.empty());
   }
}
