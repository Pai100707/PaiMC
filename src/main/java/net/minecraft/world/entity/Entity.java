package net.minecraft.world.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.EntityTooltipInfo;
import net.minecraft.network.chat.HoverEvent.ShowEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.BlockUtil.FoundRectangle;
import net.minecraft.util.ProblemReporter.PathElement;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.debug.DebugEntityBlockIntersection;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.debug.DebugValueSource.Registration;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Team;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class Entity
   implements SyncedDataHolder,
   DebugValueSource,
   Nameable,
   net.minecraft.world.entity.ItemOwner,
   net.minecraft.world.entity.SlotProvider,
   EntityAccess,
   ScoreHolder,
   DataComponentGetter {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String TAG_ID = "id";
   public static final String TAG_UUID = "UUID";
   public static final String TAG_PASSENGERS = "Passengers";
   public static final String TAG_DATA = "data";
   public static final String TAG_POS = "Pos";
   public static final String TAG_MOTION = "Motion";
   public static final String TAG_ROTATION = "Rotation";
   public static final String TAG_PORTAL_COOLDOWN = "PortalCooldown";
   public static final String TAG_NO_GRAVITY = "NoGravity";
   public static final String TAG_AIR = "Air";
   public static final String TAG_ON_GROUND = "OnGround";
   public static final String TAG_FALL_DISTANCE = "fall_distance";
   public static final String TAG_FIRE = "Fire";
   public static final String TAG_SILENT = "Silent";
   public static final String TAG_GLOWING = "Glowing";
   public static final String TAG_INVULNERABLE = "Invulnerable";
   public static final String TAG_CUSTOM_NAME = "CustomName";
   private static final AtomicInteger ENTITY_COUNTER = new AtomicInteger();
   public static final int CONTENTS_SLOT_INDEX = 0;
   public static final int BOARDING_COOLDOWN = 60;
   public static final int TOTAL_AIR_SUPPLY = 300;
   public static final int MAX_ENTITY_TAG_COUNT = 1024;
   private static final Codec<List<String>> TAG_LIST_CODEC = Codec.STRING.sizeLimitedListOf(1024);
   public static final float DELTA_AFFECTED_BY_BLOCKS_BELOW_0_2 = 0.2F;
   public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_0_5 = 0.500001;
   public static final double DELTA_AFFECTED_BY_BLOCKS_BELOW_1_0 = 0.999999;
   public static final int BASE_TICKS_REQUIRED_TO_FREEZE = 140;
   public static final int FREEZE_HURT_FREQUENCY = 40;
   public static final int BASE_SAFE_FALL_DISTANCE = 3;
   private static final AABB INITIAL_AABB = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
   private static final double WATER_FLOW_SCALE = 0.014;
   private static final double LAVA_FAST_FLOW_SCALE = 0.007;
   private static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335;
   private static final int MAX_BLOCK_ITERATIONS_ALONG_TRAVEL_PER_TICK = 16;
   private static final double MAX_MOVEMENT_RESETTING_TRACE_DISTANCE = 8.0;
   private static double viewScale = 1.0;
   private final net.minecraft.world.entity.EntityType<?> type;
   private boolean requiresPrecisePosition;
   private int id = ENTITY_COUNTER.incrementAndGet();
   public boolean blocksBuilding;
   private ImmutableList<net.minecraft.world.entity.Entity> passengers = ImmutableList.of();
   protected int boardingCooldown;
   @Nullable
   private net.minecraft.world.entity.Entity vehicle;
   private Level level;
   public double xo;
   public double yo;
   public double zo;
   private Vec3 position;
   private BlockPos blockPosition;
   private ChunkPos chunkPosition;
   private Vec3 deltaMovement = Vec3.ZERO;
   private float yRot;
   private float xRot;
   public float yRotO;
   public float xRotO;
   private AABB bb = INITIAL_AABB;
   private boolean onGround;
   public boolean horizontalCollision;
   public boolean verticalCollision;
   public boolean verticalCollisionBelow;
   public boolean minorHorizontalCollision;
   public boolean hurtMarked;
   protected Vec3 stuckSpeedMultiplier = Vec3.ZERO;
   @Nullable
   private net.minecraft.world.entity.Entity.RemovalReason removalReason;
   public static final float DEFAULT_BB_WIDTH = 0.6F;
   public static final float DEFAULT_BB_HEIGHT = 1.8F;
   public float moveDist;
   public float flyDist;
   public double fallDistance;
   private float nextStep = 1.0F;
   public double xOld;
   public double yOld;
   public double zOld;
   public boolean noPhysics;
   protected final RandomSource random = RandomSource.create();
   public int tickCount;
   private int remainingFireTicks;
   protected boolean wasTouchingWater;
   protected Object2DoubleMap<TagKey<Fluid>> fluidHeight = new Object2DoubleArrayMap(2);
   protected boolean wasEyeInWater;
   private final Set<TagKey<Fluid>> fluidOnEyes = new HashSet<>();
   public int invulnerableTime;
   protected boolean firstTick = true;
   protected final SynchedEntityData entityData;
   protected static final EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.BYTE
   );
   protected static final int FLAG_ONFIRE = 0;
   private static final int FLAG_SHIFT_KEY_DOWN = 1;
   private static final int FLAG_SPRINTING = 3;
   private static final int FLAG_SWIMMING = 4;
   private static final int FLAG_INVISIBLE = 5;
   protected static final int FLAG_GLOWING = 6;
   protected static final int FLAG_FALL_FLYING = 7;
   private static final EntityDataAccessor<Integer> DATA_AIR_SUPPLY_ID = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.INT
   );
   private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.OPTIONAL_COMPONENT
   );
   private static final EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Boolean> DATA_SILENT = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.BOOLEAN
   );
   private static final EntityDataAccessor<Boolean> DATA_NO_GRAVITY = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.BOOLEAN
   );
   protected static final EntityDataAccessor<net.minecraft.world.entity.Pose> DATA_POSE = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.POSE
   );
   private static final EntityDataAccessor<Integer> DATA_TICKS_FROZEN = SynchedEntityData.defineId(
      net.minecraft.world.entity.Entity.class, EntityDataSerializers.INT
   );
   private EntityInLevelCallback levelCallback = EntityInLevelCallback.NULL;
   private final VecDeltaCodec packetPositionCodec = new VecDeltaCodec();
   public boolean needsSync;
   @Nullable
   public net.minecraft.world.entity.PortalProcessor portalProcess;
   private int portalCooldown;
   private boolean invulnerable;
   protected UUID uuid = Mth.createInsecureUUID(this.random);
   protected String stringUUID = this.uuid.toString();
   private boolean hasGlowingTag;
   private final Set<String> tags = Sets.newHashSet();
   private final double[] pistonDeltas = new double[]{0.0, 0.0, 0.0};
   private long pistonDeltasGameTime;
   private net.minecraft.world.entity.EntityDimensions dimensions;
   private float eyeHeight;
   public boolean isInPowderSnow;
   public boolean wasInPowderSnow;
   public Optional<BlockPos> mainSupportingBlockPos = Optional.empty();
   private boolean onGroundNoBlocks = false;
   private float crystalSoundIntensity;
   private int lastCrystalSoundPlayTick;
   private boolean hasVisualFire;
   private Vec3 lastKnownSpeed = Vec3.ZERO;
   @Nullable
   private Vec3 lastKnownPosition;
   @Nullable
   private BlockState inBlockState = null;
   public static final int MAX_MOVEMENTS_HANDELED_PER_TICK = 100;
   private final ArrayDeque<net.minecraft.world.entity.Entity.Movement> movementThisTick = new ArrayDeque<>(100);
   private final List<net.minecraft.world.entity.Entity.Movement> finalMovementsThisTick = new ObjectArrayList();
   private final LongSet visitedBlocks = new LongOpenHashSet();
   private final net.minecraft.world.entity.InsideBlockEffectApplier.StepBasedCollector insideEffectCollector = new net.minecraft.world.entity.InsideBlockEffectApplier.StepBasedCollector();
   private CustomData customData = CustomData.EMPTY;

   public Entity(net.minecraft.world.entity.EntityType<?> $$0, Level $$1) {
      this.type = $$0;
      this.level = $$1;
      this.dimensions = $$0.getDimensions();
      this.position = Vec3.ZERO;
      this.blockPosition = BlockPos.ZERO;
      this.chunkPosition = ChunkPos.ZERO;
      net.minecraft.network.syncher.SynchedEntityData.Builder $$2 = new net.minecraft.network.syncher.SynchedEntityData.Builder(this);
      $$2.define(DATA_SHARED_FLAGS_ID, (byte)0);
      $$2.define(DATA_AIR_SUPPLY_ID, this.getMaxAirSupply());
      $$2.define(DATA_CUSTOM_NAME_VISIBLE, false);
      $$2.define(DATA_CUSTOM_NAME, Optional.empty());
      $$2.define(DATA_SILENT, false);
      $$2.define(DATA_NO_GRAVITY, false);
      $$2.define(DATA_POSE, net.minecraft.world.entity.Pose.STANDING);
      $$2.define(DATA_TICKS_FROZEN, 0);
      this.defineSynchedData($$2);
      this.entityData = $$2.build();
      this.setPos(0.0, 0.0, 0.0);
      this.eyeHeight = this.dimensions.eyeHeight();
   }

   public boolean isColliding(BlockPos $$0, BlockState $$1) {
      VoxelShape $$2 = $$1.getCollisionShape(this.level(), $$0, CollisionContext.of(this)).move($$0);
      return Shapes.joinIsNotEmpty($$2, Shapes.create(this.getBoundingBox()), BooleanOp.AND);
   }

   public int getTeamColor() {
      Team $$0 = this.getTeam();
      return $$0 != null && $$0.getColor().getColor() != null ? $$0.getColor().getColor() : 16777215;
   }

   public boolean isSpectator() {
      return false;
   }

   public boolean canInteractWithLevel() {
      return this.isAlive() && !this.isRemoved() && !this.isSpectator();
   }

   public final void unRide() {
      if (this.isVehicle()) {
         this.ejectPassengers();
      }

      if (this.isPassenger()) {
         this.stopRiding();
      }
   }

   public void syncPacketPositionCodec(double $$0, double $$1, double $$2) {
      this.packetPositionCodec.setBase(new Vec3($$0, $$1, $$2));
   }

   public VecDeltaCodec getPositionCodec() {
      return this.packetPositionCodec;
   }

   public net.minecraft.world.entity.EntityType<?> getType() {
      return this.type;
   }

   public boolean getRequiresPrecisePosition() {
      return this.requiresPrecisePosition;
   }

   public void setRequiresPrecisePosition(boolean $$0) {
      this.requiresPrecisePosition = $$0;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int $$0) {
      this.id = $$0;
   }

   public Set<String> getTags() {
      return this.tags;
   }

   public boolean addTag(String $$0) {
      return this.tags.size() >= 1024 ? false : this.tags.add($$0);
   }

   public boolean removeTag(String $$0) {
      return this.tags.remove($$0);
   }

   public void kill(ServerLevel $$0) {
      this.remove(net.minecraft.world.entity.Entity.RemovalReason.KILLED);
      this.gameEvent(GameEvent.ENTITY_DIE);
   }

   public final void discard() {
      this.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
   }

   protected abstract void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder var1);

   public SynchedEntityData getEntityData() {
      return this.entityData;
   }

   @Override
   public boolean equals(Object $$0) {
      return $$0 instanceof net.minecraft.world.entity.Entity ? ((net.minecraft.world.entity.Entity)$$0).id == this.id : false;
   }

   @Override
   public int hashCode() {
      return this.id;
   }

   public void remove(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      this.setRemoved($$0);
   }

   public void onClientRemoval() {
   }

   public void onRemoval(net.minecraft.world.entity.Entity.RemovalReason $$0) {
   }

   public void setPose(net.minecraft.world.entity.Pose $$0) {
      this.entityData.set(DATA_POSE, $$0);
   }

   public net.minecraft.world.entity.Pose getPose() {
      return (net.minecraft.world.entity.Pose)this.entityData.get(DATA_POSE);
   }

   public boolean hasPose(net.minecraft.world.entity.Pose $$0) {
      return this.getPose() == $$0;
   }

   public boolean closerThan(net.minecraft.world.entity.Entity $$0, double $$1) {
      return this.position().closerThan($$0.position(), $$1);
   }

   public boolean closerThan(net.minecraft.world.entity.Entity $$0, double $$1, double $$2) {
      double $$3 = $$0.getX() - this.getX();
      double $$4 = $$0.getY() - this.getY();
      double $$5 = $$0.getZ() - this.getZ();
      return Mth.lengthSquared($$3, $$5) < Mth.square($$1) && Mth.square($$4) < Mth.square($$2);
   }

   protected void setRot(float $$0, float $$1) {
      this.setYRot($$0 % 360.0F);
      this.setXRot($$1 % 360.0F);
   }

   public final void setPos(Vec3 $$0) {
      this.setPos($$0.x(), $$0.y(), $$0.z());
   }

   public void setPos(double $$0, double $$1, double $$2) {
      this.setPosRaw($$0, $$1, $$2);
      this.setBoundingBox(this.makeBoundingBox());
   }

   protected final AABB makeBoundingBox() {
      return this.makeBoundingBox(this.position);
   }

   protected AABB makeBoundingBox(Vec3 $$0) {
      return this.dimensions.makeBoundingBox($$0);
   }

   protected void reapplyPosition() {
      this.lastKnownPosition = null;
      this.setPos(this.position.x, this.position.y, this.position.z);
   }

   public void turn(double $$0, double $$1) {
      float $$2 = (float)$$1 * 0.15F;
      float $$3 = (float)$$0 * 0.15F;
      this.setXRot(this.getXRot() + $$2);
      this.setYRot(this.getYRot() + $$3);
      this.setXRot(Mth.clamp(this.getXRot(), -90.0F, 90.0F));
      this.xRotO += $$2;
      this.yRotO += $$3;
      this.xRotO = Mth.clamp(this.xRotO, -90.0F, 90.0F);
      if (this.vehicle != null) {
         this.vehicle.onPassengerTurned(this);
      }
   }

   public void updateDataBeforeSync() {
   }

   public void tick() {
      this.baseTick();
   }

   public void baseTick() {
      ProfilerFiller $$0 = Profiler.get();
      $$0.push("entityBaseTick");
      this.computeSpeed();
      this.inBlockState = null;
      if (this.isPassenger() && this.getVehicle().isRemoved()) {
         this.stopRiding();
      }

      if (this.boardingCooldown > 0) {
         this.boardingCooldown--;
      }

      this.handlePortal();
      if (this.canSpawnSprintParticle()) {
         this.spawnSprintParticle();
      }

      this.wasInPowderSnow = this.isInPowderSnow;
      this.isInPowderSnow = false;
      this.updateInWaterStateAndDoFluidPushing();
      this.updateFluidOnEyes();
      this.updateSwimming();
      if (this.level() instanceof ServerLevel $$1) {
         if (this.remainingFireTicks > 0) {
            if (this.fireImmune()) {
               this.clearFire();
            } else {
               if (this.remainingFireTicks % 20 == 0 && !this.isInLava()) {
                  this.hurtServer($$1, this.damageSources().onFire(), 1.0F);
               }

               this.setRemainingFireTicks(this.remainingFireTicks - 1);
            }
         }
      } else {
         this.clearFire();
      }

      if (this.isInLava()) {
         this.fallDistance *= 0.5;
      }

      this.checkBelowWorld();
      if (!this.level().isClientSide()) {
         this.setSharedFlagOnFire(this.remainingFireTicks > 0);
      }

      this.firstTick = false;
      if (this.level() instanceof ServerLevel $$2 && this instanceof net.minecraft.world.entity.Leashable) {
         net.minecraft.world.entity.Leashable.tickLeash($$2, (net.minecraft.world.entity.Entity & net.minecraft.world.entity.Leashable)this);
      }

      $$0.pop();
   }

   protected void computeSpeed() {
      if (this.lastKnownPosition == null) {
         this.lastKnownPosition = this.position();
      }

      this.lastKnownSpeed = this.position().subtract(this.lastKnownPosition);
      this.lastKnownPosition = this.position();
   }

   public void setSharedFlagOnFire(boolean $$0) {
      this.setSharedFlag(0, $$0 || this.hasVisualFire);
   }

   public void checkBelowWorld() {
      if (this.getY() < this.level().getMinY() - 64) {
         this.onBelowWorld();
      }
   }

   public void setPortalCooldown() {
      this.portalCooldown = this.getDimensionChangingDelay();
   }

   public void setPortalCooldown(int $$0) {
      this.portalCooldown = $$0;
   }

   public int getPortalCooldown() {
      return this.portalCooldown;
   }

   public boolean isOnPortalCooldown() {
      return this.portalCooldown > 0;
   }

   protected void processPortalCooldown() {
      if (this.isOnPortalCooldown()) {
         this.portalCooldown--;
      }
   }

   public void lavaIgnite() {
      if (!this.fireImmune()) {
         this.igniteForSeconds(15.0F);
      }
   }

   public void lavaHurt() {
      if (!this.fireImmune()) {
         if (this.level() instanceof ServerLevel $$0
            && this.hurtServer($$0, this.damageSources().lava(), 4.0F)
            && this.shouldPlayLavaHurtSound()
            && !this.isSilent()) {
            $$0.playSound(
               null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_BURN, this.getSoundSource(), 0.4F, 2.0F + this.random.nextFloat() * 0.4F
            );
         }
      }
   }

   protected boolean shouldPlayLavaHurtSound() {
      return true;
   }

   public final void igniteForSeconds(float $$0) {
      this.igniteForTicks(Mth.floor($$0 * 20.0F));
   }

   public void igniteForTicks(int $$0) {
      if (this.remainingFireTicks < $$0) {
         this.setRemainingFireTicks($$0);
      }

      this.clearFreeze();
   }

   public void setRemainingFireTicks(int $$0) {
      this.remainingFireTicks = $$0;
   }

   public int getRemainingFireTicks() {
      return this.remainingFireTicks;
   }

   public void clearFire() {
      this.setRemainingFireTicks(Math.min(0, this.getRemainingFireTicks()));
   }

   protected void onBelowWorld() {
      this.discard();
   }

   public boolean isFree(double $$0, double $$1, double $$2) {
      return this.isFree(this.getBoundingBox().move($$0, $$1, $$2));
   }

   private boolean isFree(AABB $$0) {
      return this.level().noCollision(this, $$0) && !this.level().containsAnyLiquid($$0);
   }

   public void setOnGround(boolean $$0) {
      this.onGround = $$0;
      this.checkSupportingBlock($$0, null);
   }

   public void setOnGroundWithMovement(boolean $$0, Vec3 $$1) {
      this.setOnGroundWithMovement($$0, this.horizontalCollision, $$1);
   }

   public void setOnGroundWithMovement(boolean $$0, boolean $$1, Vec3 $$2) {
      this.onGround = $$0;
      this.horizontalCollision = $$1;
      this.checkSupportingBlock($$0, $$2);
   }

   public boolean isSupportedBy(BlockPos $$0) {
      return this.mainSupportingBlockPos.isPresent() && this.mainSupportingBlockPos.get().equals($$0);
   }

   protected void checkSupportingBlock(boolean $$0, @Nullable Vec3 $$1) {
      if ($$0) {
         AABB $$2 = this.getBoundingBox();
         AABB $$3 = new AABB($$2.minX, $$2.minY - 1.0E-6, $$2.minZ, $$2.maxX, $$2.minY, $$2.maxZ);
         Optional<BlockPos> $$4 = this.level.findSupportingBlock(this, $$3);
         if ($$4.isPresent() || this.onGroundNoBlocks) {
            this.mainSupportingBlockPos = $$4;
         } else if ($$1 != null) {
            AABB $$5 = $$3.move(-$$1.x, 0.0, -$$1.z);
            $$4 = this.level.findSupportingBlock(this, $$5);
            this.mainSupportingBlockPos = $$4;
         }

         this.onGroundNoBlocks = $$4.isEmpty();
      } else {
         this.onGroundNoBlocks = false;
         if (this.mainSupportingBlockPos.isPresent()) {
            this.mainSupportingBlockPos = Optional.empty();
         }
      }
   }

   public boolean onGround() {
      return this.onGround;
   }

   public void move(net.minecraft.world.entity.MoverType $$0, Vec3 $$1) {
      if (this.noPhysics) {
         this.setPos(this.getX() + $$1.x, this.getY() + $$1.y, this.getZ() + $$1.z);
         this.horizontalCollision = false;
         this.verticalCollision = false;
         this.verticalCollisionBelow = false;
         this.minorHorizontalCollision = false;
      } else {
         if ($$0 == net.minecraft.world.entity.MoverType.PISTON) {
            $$1 = this.limitPistonMovement($$1);
            if ($$1.equals(Vec3.ZERO)) {
               return;
            }
         }

         ProfilerFiller $$2 = Profiler.get();
         $$2.push("move");
         if (this.stuckSpeedMultiplier.lengthSqr() > 1.0E-7) {
            if ($$0 != net.minecraft.world.entity.MoverType.PISTON) {
               $$1 = $$1.multiply(this.stuckSpeedMultiplier);
            }

            this.stuckSpeedMultiplier = Vec3.ZERO;
            this.setDeltaMovement(Vec3.ZERO);
         }

         $$1 = this.maybeBackOffFromEdge($$1, $$0);
         Vec3 $$3 = this.collide($$1);
         double $$4 = $$3.lengthSqr();
         if ($$4 > 1.0E-7 || $$1.lengthSqr() - $$4 < 1.0E-7) {
            if (this.fallDistance != 0.0 && $$4 >= 1.0) {
               double $$5 = Math.min($$3.length(), 8.0);
               Vec3 $$6 = this.position().add($$3.normalize().scale($$5));
               BlockHitResult $$7 = this.level()
                  .clip(new ClipContext(this.position(), $$6, Block.FALLDAMAGE_RESETTING, net.minecraft.world.level.ClipContext.Fluid.WATER, this));
               if ($$7.getType() != Type.MISS) {
                  this.resetFallDistance();
               }
            }

            Vec3 $$8 = this.position();
            Vec3 $$9 = $$8.add($$3);
            this.addMovementThisTick(new net.minecraft.world.entity.Entity.Movement($$8, $$9, $$1));
            this.setPos($$9);
         }

         $$2.pop();
         $$2.push("rest");
         boolean $$10 = !Mth.equal($$1.x, $$3.x);
         boolean $$11 = !Mth.equal($$1.z, $$3.z);
         this.horizontalCollision = $$10 || $$11;
         if (Math.abs($$1.y) > 0.0 || this.isLocalInstanceAuthoritative()) {
            this.verticalCollision = $$1.y != $$3.y;
            this.verticalCollisionBelow = this.verticalCollision && $$1.y < 0.0;
            this.setOnGroundWithMovement(this.verticalCollisionBelow, this.horizontalCollision, $$3);
         }

         if (this.horizontalCollision) {
            this.minorHorizontalCollision = this.isHorizontalCollisionMinor($$3);
         } else {
            this.minorHorizontalCollision = false;
         }

         BlockPos $$12 = this.getOnPosLegacy();
         BlockState $$13 = this.level().getBlockState($$12);
         if (this.isLocalInstanceAuthoritative()) {
            this.checkFallDamage($$3.y, this.onGround(), $$13, $$12);
         }

         if (this.isRemoved()) {
            $$2.pop();
         } else {
            if (this.horizontalCollision) {
               Vec3 $$14 = this.getDeltaMovement();
               this.setDeltaMovement($$10 ? 0.0 : $$14.x, $$14.y, $$11 ? 0.0 : $$14.z);
            }

            if (this.canSimulateMovement()) {
               net.minecraft.world.level.block.Block $$15 = $$13.getBlock();
               if ($$1.y != $$3.y) {
                  $$15.updateEntityMovementAfterFallOn(this.level(), this);
               }
            }

            if (!this.level().isClientSide() || this.isLocalInstanceAuthoritative()) {
               net.minecraft.world.entity.Entity.MovementEmission $$16 = this.getMovementEmission();
               if ($$16.emitsAnything() && !this.isPassenger()) {
                  this.applyMovementEmissionAndPlaySound($$16, $$3, $$12, $$13);
               }
            }

            float $$17 = this.getBlockSpeedFactor();
            this.setDeltaMovement(this.getDeltaMovement().multiply($$17, 1.0, $$17));
            $$2.pop();
         }
      }
   }

   private void applyMovementEmissionAndPlaySound(net.minecraft.world.entity.Entity.MovementEmission $$0, Vec3 $$1, BlockPos $$2, BlockState $$3) {
      float $$4 = 0.6F;
      float $$5 = (float)($$1.length() * 0.6F);
      float $$6 = (float)($$1.horizontalDistance() * 0.6F);
      BlockPos $$7 = this.getOnPos();
      BlockState $$8 = this.level().getBlockState($$7);
      boolean $$9 = this.isStateClimbable($$8);
      this.moveDist += $$9 ? $$5 : $$6;
      this.flyDist += $$5;
      if (this.moveDist > this.nextStep && !$$8.isAir()) {
         boolean $$10 = $$7.equals($$2);
         boolean $$11 = this.vibrationAndSoundEffectsFromBlock($$2, $$3, $$0.emitsSounds(), $$10, $$1);
         if (!$$10) {
            $$11 |= this.vibrationAndSoundEffectsFromBlock($$7, $$8, false, $$0.emitsEvents(), $$1);
         }

         if ($$11) {
            this.nextStep = this.nextStep();
         } else if (this.isInWater()) {
            this.nextStep = this.nextStep();
            if ($$0.emitsSounds()) {
               this.waterSwimSound();
            }

            if ($$0.emitsEvents()) {
               this.gameEvent(GameEvent.SWIM);
            }
         }
      } else if ($$8.isAir()) {
         this.processFlappingMovement();
      }
   }

   protected void applyEffectsFromBlocks() {
      this.finalMovementsThisTick.clear();
      this.finalMovementsThisTick.addAll(this.movementThisTick);
      this.movementThisTick.clear();
      if (this.finalMovementsThisTick.isEmpty()) {
         this.finalMovementsThisTick.add(new net.minecraft.world.entity.Entity.Movement(this.oldPosition(), this.position()));
      } else if (this.finalMovementsThisTick.getLast().to.distanceToSqr(this.position()) > 9.9999994E-11F) {
         this.finalMovementsThisTick.add(new net.minecraft.world.entity.Entity.Movement(this.finalMovementsThisTick.getLast().to, this.position()));
      }

      this.applyEffectsFromBlocks(this.finalMovementsThisTick);
   }

   private void addMovementThisTick(net.minecraft.world.entity.Entity.Movement $$0) {
      if (this.movementThisTick.size() >= 100) {
         net.minecraft.world.entity.Entity.Movement $$1 = this.movementThisTick.removeFirst();
         net.minecraft.world.entity.Entity.Movement $$2 = this.movementThisTick.removeFirst();
         net.minecraft.world.entity.Entity.Movement $$3 = new net.minecraft.world.entity.Entity.Movement($$1.from(), $$2.to());
         this.movementThisTick.addFirst($$3);
      }

      this.movementThisTick.add($$0);
   }

   public void removeLatestMovementRecording() {
      if (!this.movementThisTick.isEmpty()) {
         this.movementThisTick.removeLast();
      }
   }

   protected void clearMovementThisTick() {
      this.movementThisTick.clear();
   }

   public boolean hasMovedHorizontallyRecently() {
      return Math.abs(this.lastKnownSpeed.horizontalDistance()) > 1.0E-5F;
   }

   public void applyEffectsFromBlocks(Vec3 $$0, Vec3 $$1) {
      this.applyEffectsFromBlocks(List.of(new net.minecraft.world.entity.Entity.Movement($$0, $$1)));
   }

   private void applyEffectsFromBlocks(List<net.minecraft.world.entity.Entity.Movement> $$0) {
      if (this.isAffectedByBlocks()) {
         if (this.onGround()) {
            BlockPos $$1 = this.getOnPosLegacy();
            BlockState $$2 = this.level().getBlockState($$1);
            $$2.getBlock().stepOn(this.level(), $$1, $$2, this);
         }

         boolean $$3 = this.isOnFire();
         boolean $$4 = this.isFreezing();
         int $$5 = this.getRemainingFireTicks();
         this.checkInsideBlocks($$0, this.insideEffectCollector);
         this.insideEffectCollector.applyAndClear(this);
         if (this.isInRain()) {
            this.clearFire();
         }

         if ($$3 && !this.isOnFire() || $$4 && !this.isFreezing()) {
            this.playEntityOnFireExtinguishedSound();
         }

         boolean $$6 = this.getRemainingFireTicks() > $$5;
         if (!this.level().isClientSide() && !this.isOnFire() && !$$6) {
            this.setRemainingFireTicks(-this.getFireImmuneTicks());
         }
      }
   }

   protected boolean isAffectedByBlocks() {
      return !this.isRemoved() && !this.noPhysics;
   }

   private boolean isStateClimbable(BlockState $$0) {
      return $$0.is(BlockTags.CLIMBABLE) || $$0.is(Blocks.POWDER_SNOW);
   }

   private boolean vibrationAndSoundEffectsFromBlock(BlockPos $$0, BlockState $$1, boolean $$2, boolean $$3, Vec3 $$4) {
      if ($$1.isAir()) {
         return false;
      } else {
         boolean $$5 = this.isStateClimbable($$1);
         if ((this.onGround() || $$5 || this.isCrouching() && $$4.y == 0.0 || this.isOnRails()) && !this.isSwimming()) {
            if ($$2) {
               this.walkingStepSound($$0, $$1);
            }

            if ($$3) {
               this.level().gameEvent(GameEvent.STEP, this.position(), Context.of(this, $$1));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean isHorizontalCollisionMinor(Vec3 $$0) {
      return false;
   }

   protected void playEntityOnFireExtinguishedSound() {
      if (!this.level.isClientSide()) {
         this.level()
            .playSound(
               null,
               this.getX(),
               this.getY(),
               this.getZ(),
               SoundEvents.GENERIC_EXTINGUISH_FIRE,
               this.getSoundSource(),
               0.7F,
               1.6F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F
            );
      }
   }

   public void extinguishFire() {
      if (this.isOnFire()) {
         this.playEntityOnFireExtinguishedSound();
      }

      this.clearFire();
   }

   protected void processFlappingMovement() {
      if (this.isFlapping()) {
         this.onFlap();
         if (this.getMovementEmission().emitsEvents()) {
            this.gameEvent(GameEvent.FLAP);
         }
      }
   }

   @Deprecated
   public BlockPos getOnPosLegacy() {
      return this.getOnPos(0.2F);
   }

   public BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.500001F);
   }

   public BlockPos getOnPos() {
      return this.getOnPos(1.0E-5F);
   }

   protected BlockPos getOnPos(float $$0) {
      if (this.mainSupportingBlockPos.isPresent()) {
         BlockPos $$1 = this.mainSupportingBlockPos.get();
         if (!($$0 > 1.0E-5F)) {
            return $$1;
         } else {
            BlockState $$2 = this.level().getBlockState($$1);
            return (!($$0 <= 0.5) || !$$2.is(BlockTags.FENCES)) && !$$2.is(BlockTags.WALLS) && !($$2.getBlock() instanceof FenceGateBlock)
               ? $$1.atY(Mth.floor(this.position.y - $$0))
               : $$1;
         }
      } else {
         int $$3 = Mth.floor(this.position.x);
         int $$4 = Mth.floor(this.position.y - $$0);
         int $$5 = Mth.floor(this.position.z);
         return new BlockPos($$3, $$4, $$5);
      }
   }

   protected float getBlockJumpFactor() {
      float $$0 = this.level().getBlockState(this.blockPosition()).getBlock().getJumpFactor();
      float $$1 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getJumpFactor();
      return $$0 == 1.0 ? $$1 : $$0;
   }

   protected float getBlockSpeedFactor() {
      BlockState $$0 = this.level().getBlockState(this.blockPosition());
      float $$1 = $$0.getBlock().getSpeedFactor();
      if (!$$0.is(Blocks.WATER) && !$$0.is(Blocks.BUBBLE_COLUMN)) {
         return $$1 == 1.0 ? this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getSpeedFactor() : $$1;
      } else {
         return $$1;
      }
   }

   protected Vec3 maybeBackOffFromEdge(Vec3 $$0, net.minecraft.world.entity.MoverType $$1) {
      return $$0;
   }

   protected Vec3 limitPistonMovement(Vec3 $$0) {
      if ($$0.lengthSqr() <= 1.0E-7) {
         return $$0;
      } else {
         long $$1 = this.level().getGameTime();
         if ($$1 != this.pistonDeltasGameTime) {
            Arrays.fill(this.pistonDeltas, 0.0);
            this.pistonDeltasGameTime = $$1;
         }

         if ($$0.x != 0.0) {
            double $$2 = this.applyPistonMovementRestriction(Axis.X, $$0.x);
            return Math.abs($$2) <= 1.0E-5F ? Vec3.ZERO : new Vec3($$2, 0.0, 0.0);
         } else if ($$0.y != 0.0) {
            double $$3 = this.applyPistonMovementRestriction(Axis.Y, $$0.y);
            return Math.abs($$3) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, $$3, 0.0);
         } else if ($$0.z != 0.0) {
            double $$4 = this.applyPistonMovementRestriction(Axis.Z, $$0.z);
            return Math.abs($$4) <= 1.0E-5F ? Vec3.ZERO : new Vec3(0.0, 0.0, $$4);
         } else {
            return Vec3.ZERO;
         }
      }
   }

   private double applyPistonMovementRestriction(Axis $$0, double $$1) {
      int $$2 = $$0.ordinal();
      double $$3 = Mth.clamp($$1 + this.pistonDeltas[$$2], -0.51, 0.51);
      $$1 = $$3 - this.pistonDeltas[$$2];
      this.pistonDeltas[$$2] = $$3;
      return $$1;
   }

   public double getAvailableSpaceBelow(double $$0) {
      AABB $$1 = this.getBoundingBox();
      AABB $$2 = $$1.setMinY($$1.minY - $$0).setMaxY($$1.minY);
      List<VoxelShape> $$3 = collectAllColliders(this, this.level, $$2);
      return $$3.isEmpty() ? $$0 : -Shapes.collide(Axis.Y, $$1, $$3, -$$0);
   }

   private Vec3 collide(Vec3 $$0) {
      AABB $$1 = this.getBoundingBox();
      List<VoxelShape> $$2 = this.level().getEntityCollisions(this, $$1.expandTowards($$0));
      Vec3 $$3 = $$0.lengthSqr() == 0.0 ? $$0 : collideBoundingBox(this, $$0, $$1, this.level(), $$2);
      boolean $$4 = $$0.x != $$3.x;
      boolean $$5 = $$0.y != $$3.y;
      boolean $$6 = $$0.z != $$3.z;
      boolean $$7 = $$5 && $$0.y < 0.0;
      if (this.maxUpStep() > 0.0F && ($$7 || this.onGround()) && ($$4 || $$6)) {
         AABB $$8 = $$7 ? $$1.move(0.0, $$3.y, 0.0) : $$1;
         AABB $$9 = $$8.expandTowards($$0.x, this.maxUpStep(), $$0.z);
         if (!$$7) {
            $$9 = $$9.expandTowards(0.0, -1.0E-5F, 0.0);
         }

         List<VoxelShape> $$10 = collectColliders(this, this.level, $$2, $$9);
         float $$11 = (float)$$3.y;
         float[] $$12 = collectCandidateStepUpHeights($$8, $$10, this.maxUpStep(), $$11);

         for (float $$13 : $$12) {
            Vec3 $$14 = collideWithShapes(new Vec3($$0.x, $$13, $$0.z), $$8, $$10);
            if ($$14.horizontalDistanceSqr() > $$3.horizontalDistanceSqr()) {
               double $$15 = $$1.minY - $$8.minY;
               return $$14.subtract(0.0, $$15, 0.0);
            }
         }
      }

      return $$3;
   }

   private static float[] collectCandidateStepUpHeights(AABB $$0, List<VoxelShape> $$1, float $$2, float $$3) {
      FloatSet $$4 = new FloatArraySet(4);

      for (VoxelShape $$5 : $$1) {
         DoubleList $$6 = $$5.getCoords(Axis.Y);
         DoubleListIterator var8 = $$6.iterator();

         while (var8.hasNext()) {
            double $$7 = (Double)var8.next();
            float $$8 = (float)($$7 - $$0.minY);
            if (!($$8 < 0.0F) && $$8 != $$3) {
               if ($$8 > $$2) {
                  break;
               }

               $$4.add($$8);
            }
         }
      }

      float[] $$9 = $$4.toFloatArray();
      FloatArrays.unstableSort($$9);
      return $$9;
   }

   public static Vec3 collideBoundingBox(@Nullable net.minecraft.world.entity.Entity $$0, Vec3 $$1, AABB $$2, Level $$3, List<VoxelShape> $$4) {
      List<VoxelShape> $$5 = collectColliders($$0, $$3, $$4, $$2.expandTowards($$1));
      return collideWithShapes($$1, $$2, $$5);
   }

   public static List<VoxelShape> collectAllColliders(@Nullable net.minecraft.world.entity.Entity $$0, Level $$1, AABB $$2) {
      List<VoxelShape> $$3 = $$1.getEntityCollisions($$0, $$2);
      return collectColliders($$0, $$1, $$3, $$2);
   }

   private static List<VoxelShape> collectColliders(@Nullable net.minecraft.world.entity.Entity $$0, Level $$1, List<VoxelShape> $$2, AABB $$3) {
      Builder<VoxelShape> $$4 = ImmutableList.builderWithExpectedSize($$2.size() + 1);
      if (!$$2.isEmpty()) {
         $$4.addAll($$2);
      }

      WorldBorder $$5 = $$1.getWorldBorder();
      boolean $$6 = $$0 != null && $$5.isInsideCloseToBorder($$0, $$3);
      if ($$6) {
         $$4.add($$5.getCollisionShape());
      }

      $$4.addAll($$1.getBlockCollisions($$0, $$3));
      return $$4.build();
   }

   private static Vec3 collideWithShapes(Vec3 $$0, AABB $$1, List<VoxelShape> $$2) {
      if ($$2.isEmpty()) {
         return $$0;
      } else {
         Vec3 $$3 = Vec3.ZERO;
         UnmodifiableIterator var4 = Direction.axisStepOrder($$0).iterator();

         while (var4.hasNext()) {
            Axis $$4 = (Axis)var4.next();
            double $$5 = $$0.get($$4);
            if ($$5 != 0.0) {
               double $$6 = Shapes.collide($$4, $$1.move($$3), $$2, $$5);
               $$3 = $$3.with($$4, $$6);
            }
         }

         return $$3;
      }
   }

   protected float nextStep() {
      return (int)this.moveDist + 1;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.GENERIC_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   protected SoundEvent getSwimHighSpeedSplashSound() {
      return SoundEvents.GENERIC_SPLASH;
   }

   private void checkInsideBlocks(
      List<net.minecraft.world.entity.Entity.Movement> $$0, net.minecraft.world.entity.InsideBlockEffectApplier.StepBasedCollector $$1
   ) {
      if (this.isAffectedByBlocks()) {
         LongSet $$2 = this.visitedBlocks;

         for (net.minecraft.world.entity.Entity.Movement $$3 : $$0) {
            Vec3 $$4 = $$3.from;
            Vec3 $$5 = $$3.to().subtract($$3.from());
            int $$6 = 16;
            if ($$3.axisDependentOriginalMovement().isPresent() && $$5.lengthSqr() > 0.0) {
               UnmodifiableIterator var9 = Direction.axisStepOrder($$3.axisDependentOriginalMovement().get()).iterator();

               while (var9.hasNext()) {
                  Axis $$7 = (Axis)var9.next();
                  double $$8 = $$5.get($$7);
                  if ($$8 != 0.0) {
                     Vec3 $$9 = $$4.relative($$7.getPositive(), $$8);
                     $$6 -= this.checkInsideBlocks($$4, $$9, $$1, $$2, $$6);
                     $$4 = $$9;
                  }
               }
            } else {
               $$6 -= this.checkInsideBlocks($$3.from(), $$3.to(), $$1, $$2, 16);
            }

            if ($$6 <= 0) {
               this.checkInsideBlocks($$3.to(), $$3.to(), $$1, $$2, 1);
            }
         }

         $$2.clear();
      }
   }

   private int checkInsideBlocks(Vec3 $$0, Vec3 $$1, net.minecraft.world.entity.InsideBlockEffectApplier.StepBasedCollector $$2, LongSet $$3, int $$4) {
      AABB $$5 = this.makeBoundingBox($$1).deflate(1.0E-5F);
      boolean $$6 = $$0.distanceToSqr($$1) > Mth.square(0.9999900000002526);
      boolean $$8 = this.level instanceof ServerLevel $$7
         && $$7.getServer().debugSubscribers().hasAnySubscriberFor(DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS);
      AtomicInteger $$9 = new AtomicInteger();
      BlockGetter.forEachBlockIntersectedBetween($$0, $$1, $$5, ($$9x, $$10) -> {
         if (!this.isAlive()) {
            return false;
         } else if ($$10 >= $$4) {
            return false;
         } else {
            $$9.set($$10);
            BlockState $$11 = this.level().getBlockState($$9x);
            if ($$11.isAir()) {
               if ($$8) {
                  this.debugBlockIntersection((ServerLevel)this.level(), $$9x.immutable(), false, false);
               }

               return true;
            } else {
               VoxelShape $$12 = $$11.getEntityInsideCollisionShape(this.level(), $$9x, this);
               boolean $$13 = $$12 == Shapes.block() || this.collidedWithShapeMovingFrom($$0, $$1, $$12.move(new Vec3($$9x)).toAabbs());
               boolean $$14 = this.collidedWithFluid($$11.getFluidState(), $$9x, $$0, $$1);
               if (($$13 || $$14) && $$3.add($$9x.asLong())) {
                  if ($$13) {
                     try {
                        boolean $$15 = $$6 || $$5.intersects($$9x);
                        $$2.advanceStep($$10);
                        $$11.entityInside(this.level(), $$9x, this, $$2, $$15);
                        this.onInsideBlock($$11);
                     } catch (Throwable var20) {
                        CrashReport $$17 = CrashReport.forThrowable(var20, "Colliding entity with block");
                        CrashReportCategory $$18 = $$17.addCategory("Block being collided with");
                        CrashReportCategory.populateBlockDetails($$18, this.level(), $$9x, $$11);
                        CrashReportCategory $$19 = $$17.addCategory("Entity being checked for collision");
                        this.fillCrashReportCategory($$19);
                        throw new ReportedException($$17);
                     }
                  }

                  if ($$14) {
                     $$2.advanceStep($$10);
                     $$11.getFluidState().entityInside(this.level(), $$9x, this, $$2);
                  }

                  if ($$8) {
                     this.debugBlockIntersection((ServerLevel)this.level(), $$9x.immutable(), $$13, $$14);
                  }

                  return true;
               } else {
                  return true;
               }
            }
         }
      });
      return $$9.get() + 1;
   }

   private void debugBlockIntersection(ServerLevel $$0, BlockPos $$1, boolean $$2, boolean $$3) {
      DebugEntityBlockIntersection $$4;
      if ($$3) {
         $$4 = DebugEntityBlockIntersection.IN_FLUID;
      } else if ($$2) {
         $$4 = DebugEntityBlockIntersection.IN_BLOCK;
      } else {
         $$4 = DebugEntityBlockIntersection.IN_AIR;
      }

      $$0.debugSynchronizers().sendBlockValue($$1, DebugSubscriptions.ENTITY_BLOCK_INTERSECTIONS, $$4);
   }

   public boolean collidedWithFluid(FluidState $$0, BlockPos $$1, Vec3 $$2, Vec3 $$3) {
      AABB $$4 = $$0.getAABB(this.level(), $$1);
      return $$4 != null && this.collidedWithShapeMovingFrom($$2, $$3, List.of($$4));
   }

   public boolean collidedWithShapeMovingFrom(Vec3 $$0, Vec3 $$1, List<AABB> $$2) {
      AABB $$3 = this.makeBoundingBox($$0);
      Vec3 $$4 = $$1.subtract($$0);
      return $$3.collidedAlongVector($$4, $$2);
   }

   protected void onInsideBlock(BlockState $$0) {
   }

   public BlockPos adjustSpawnLocation(ServerLevel $$0, BlockPos $$1) {
      BlockPos $$2 = $$0.getRespawnData().pos();
      Vec3 $$3 = $$2.getCenter();
      int $$4 = $$0.getChunkAt($$2).getHeight(Types.MOTION_BLOCKING_NO_LEAVES, $$2.getX(), $$2.getZ()) + 1;
      return BlockPos.containing($$3.x, $$4, $$3.z);
   }

   public void gameEvent(Holder<GameEvent> $$0, @Nullable net.minecraft.world.entity.Entity $$1) {
      this.level().gameEvent($$1, $$0, this.position);
   }

   public void gameEvent(Holder<GameEvent> $$0) {
      this.gameEvent($$0, this);
   }

   private void walkingStepSound(BlockPos $$0, BlockState $$1) {
      this.playStepSound($$0, $$1);
      if (this.shouldPlayAmethystStepSound($$1)) {
         this.playAmethystStepSound();
      }
   }

   protected void waterSwimSound() {
      net.minecraft.world.entity.Entity $$0 = Objects.requireNonNullElse(this.getControllingPassenger(), this);
      float $$1 = $$0 == this ? 0.35F : 0.4F;
      Vec3 $$2 = $$0.getDeltaMovement();
      float $$3 = Math.min(1.0F, (float)Math.sqrt($$2.x * $$2.x * 0.2F + $$2.y * $$2.y + $$2.z * $$2.z * 0.2F) * $$1);
      this.playSwimSound($$3);
   }

   protected BlockPos getPrimaryStepSoundBlockPos(BlockPos $$0) {
      BlockPos $$1 = $$0.above();
      BlockState $$2 = this.level().getBlockState($$1);
      return !$$2.is(BlockTags.INSIDE_STEP_SOUND_BLOCKS) && !$$2.is(BlockTags.COMBINATION_STEP_SOUND_BLOCKS) ? $$0 : $$1;
   }

   protected void playCombinationStepSounds(BlockState $$0, BlockState $$1) {
      SoundType $$2 = $$0.getSoundType();
      this.playSound($$2.getStepSound(), $$2.getVolume() * 0.15F, $$2.getPitch());
      this.playMuffledStepSound($$1);
   }

   protected void playMuffledStepSound(BlockState $$0) {
      SoundType $$1 = $$0.getSoundType();
      this.playSound($$1.getStepSound(), $$1.getVolume() * 0.05F, $$1.getPitch() * 0.8F);
   }

   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      SoundType $$2 = $$1.getSoundType();
      this.playSound($$2.getStepSound(), $$2.getVolume() * 0.15F, $$2.getPitch());
   }

   private boolean shouldPlayAmethystStepSound(BlockState $$0) {
      return $$0.is(BlockTags.CRYSTAL_SOUND_BLOCKS) && this.tickCount >= this.lastCrystalSoundPlayTick + 20;
   }

   private void playAmethystStepSound() {
      this.crystalSoundIntensity = this.crystalSoundIntensity * (float)Math.pow(0.997, this.tickCount - this.lastCrystalSoundPlayTick);
      this.crystalSoundIntensity = Math.min(1.0F, this.crystalSoundIntensity + 0.07F);
      float $$0 = 0.5F + this.crystalSoundIntensity * this.random.nextFloat() * 1.2F;
      float $$1 = 0.1F + this.crystalSoundIntensity * 1.2F;
      this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, $$1, $$0);
      this.lastCrystalSoundPlayTick = this.tickCount;
   }

   protected void playSwimSound(float $$0) {
      this.playSound(this.getSwimSound(), $$0, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
   }

   protected void onFlap() {
   }

   protected boolean isFlapping() {
      return false;
   }

   public void playSound(SoundEvent $$0, float $$1, float $$2) {
      if (!this.isSilent()) {
         this.level().playSound(null, this.getX(), this.getY(), this.getZ(), $$0, this.getSoundSource(), $$1, $$2);
      }
   }

   public void playSound(SoundEvent $$0) {
      if (!this.isSilent()) {
         this.playSound($$0, 1.0F, 1.0F);
      }
   }

   public boolean isSilent() {
      return (Boolean)this.entityData.get(DATA_SILENT);
   }

   public void setSilent(boolean $$0) {
      this.entityData.set(DATA_SILENT, $$0);
   }

   public boolean isNoGravity() {
      return (Boolean)this.entityData.get(DATA_NO_GRAVITY);
   }

   public void setNoGravity(boolean $$0) {
      this.entityData.set(DATA_NO_GRAVITY, $$0);
   }

   protected double getDefaultGravity() {
      return 0.0;
   }

   public final double getGravity() {
      return this.isNoGravity() ? 0.0 : this.getDefaultGravity();
   }

   protected void applyGravity() {
      double $$0 = this.getGravity();
      if ($$0 != 0.0) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0, -$$0, 0.0));
      }
   }

   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.ALL;
   }

   public boolean dampensVibrations() {
      return false;
   }

   public final void doCheckFallDamage(double $$0, double $$1, double $$2, boolean $$3) {
      if (!this.touchingUnloadedChunk()) {
         this.checkSupportingBlock($$3, new Vec3($$0, $$1, $$2));
         BlockPos $$4 = this.getOnPosLegacy();
         BlockState $$5 = this.level().getBlockState($$4);
         this.checkFallDamage($$1, $$3, $$5, $$4);
      }
   }

   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
      if (!this.isInWater() && $$0 < 0.0) {
         this.fallDistance -= (float)$$0;
      }

      if ($$1) {
         if (this.fallDistance > 0.0) {
            $$2.getBlock().fallOn(this.level(), $$2, $$3, this, this.fallDistance);
            this.level()
               .gameEvent(
                  GameEvent.HIT_GROUND,
                  this.position,
                  Context.of(this, this.mainSupportingBlockPos.<BlockState>map($$0x -> this.level().getBlockState($$0x)).orElse($$2))
               );
         }

         this.resetFallDistance();
      }
   }

   public boolean fireImmune() {
      return this.getType().fireImmune();
   }

   public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
      if (this.type.is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
         return false;
      } else {
         this.propagateFallToPassengers($$0, $$1, $$2);
         return false;
      }
   }

   protected void propagateFallToPassengers(double $$0, float $$1, DamageSource $$2) {
      if (this.isVehicle()) {
         for (net.minecraft.world.entity.Entity $$3 : this.getPassengers()) {
            $$3.causeFallDamage($$0, $$1, $$2);
         }
      }
   }

   public boolean isInWater() {
      return this.wasTouchingWater;
   }

   boolean isInRain() {
      BlockPos $$0 = this.blockPosition();
      return this.level().isRainingAt($$0) || this.level().isRainingAt(BlockPos.containing($$0.getX(), this.getBoundingBox().maxY, $$0.getZ()));
   }

   public boolean isInWaterOrRain() {
      return this.isInWater() || this.isInRain();
   }

   public boolean isInLiquid() {
      return this.isInWater() || this.isInLava();
   }

   public boolean isUnderWater() {
      return this.wasEyeInWater && this.isInWater();
   }

   public boolean isInShallowWater() {
      return this.isInWater() && !this.isUnderWater();
   }

   public boolean isInClouds() {
      if (ARGB.alpha((Integer)this.level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_COLOR, this.position())) == 0) {
         return false;
      } else {
         float $$0 = (Float)this.level.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_HEIGHT, this.position());
         if (this.getY() + this.getBbHeight() < $$0) {
            return false;
         } else {
            float $$1 = $$0 + 4.0F;
            return this.getY() <= $$1;
         }
      }
   }

   public void updateSwimming() {
      if (this.isSwimming()) {
         this.setSwimming(this.isSprinting() && this.isInWater() && !this.isPassenger());
      } else {
         this.setSwimming(
            this.isSprinting() && this.isUnderWater() && !this.isPassenger() && this.level().getFluidState(this.blockPosition).is(FluidTags.WATER)
         );
      }
   }

   protected boolean updateInWaterStateAndDoFluidPushing() {
      this.fluidHeight.clear();
      this.updateInWaterStateAndDoWaterCurrentPushing();
      double $$0 = this.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA) ? 0.007 : 0.0023333333333333335;
      boolean $$1 = this.updateFluidHeightAndDoFluidPushing(FluidTags.LAVA, $$0);
      return this.isInWater() || $$1;
   }

   void updateInWaterStateAndDoWaterCurrentPushing() {
      if (this.getVehicle() instanceof AbstractBoat $$0 && !$$0.isUnderWater()) {
         this.wasTouchingWater = false;
      } else if (this.updateFluidHeightAndDoFluidPushing(FluidTags.WATER, 0.014)) {
         if (!this.wasTouchingWater && !this.firstTick) {
            this.doWaterSplashEffect();
         }

         this.resetFallDistance();
         this.wasTouchingWater = true;
      } else {
         this.wasTouchingWater = false;
      }
   }

   private void updateFluidOnEyes() {
      this.wasEyeInWater = this.isEyeInFluid(FluidTags.WATER);
      this.fluidOnEyes.clear();
      double $$0 = this.getEyeY();
      if (!(this.getVehicle() instanceof AbstractBoat $$2 && !$$2.isUnderWater() && $$2.getBoundingBox().maxY >= $$0 && $$2.getBoundingBox().minY <= $$0)) {
         BlockPos $$3 = BlockPos.containing(this.getX(), $$0, this.getZ());
         FluidState $$4 = this.level().getFluidState($$3);
         double $$5 = $$3.getY() + $$4.getHeight(this.level(), $$3);
         if ($$5 > $$0) {
            $$4.getTags().forEach(this.fluidOnEyes::add);
         }
      }
   }

   protected void doWaterSplashEffect() {
      net.minecraft.world.entity.Entity $$0 = Objects.requireNonNullElse(this.getControllingPassenger(), this);
      float $$1 = $$0 == this ? 0.2F : 0.9F;
      Vec3 $$2 = $$0.getDeltaMovement();
      float $$3 = Math.min(1.0F, (float)Math.sqrt($$2.x * $$2.x * 0.2F + $$2.y * $$2.y + $$2.z * $$2.z * 0.2F) * $$1);
      if ($$3 < 0.25F) {
         this.playSound(this.getSwimSplashSound(), $$3, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      } else {
         this.playSound(this.getSwimHighSpeedSplashSound(), $$3, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
      }

      float $$4 = Mth.floor(this.getY());

      for (int $$5 = 0; $$5 < 1.0F + this.dimensions.width() * 20.0F; $$5++) {
         double $$6 = (this.random.nextDouble() * 2.0 - 1.0) * this.dimensions.width();
         double $$7 = (this.random.nextDouble() * 2.0 - 1.0) * this.dimensions.width();
         this.level()
            .addParticle(ParticleTypes.BUBBLE, this.getX() + $$6, $$4 + 1.0F, this.getZ() + $$7, $$2.x, $$2.y - this.random.nextDouble() * 0.2F, $$2.z);
      }

      for (int $$8 = 0; $$8 < 1.0F + this.dimensions.width() * 20.0F; $$8++) {
         double $$9 = (this.random.nextDouble() * 2.0 - 1.0) * this.dimensions.width();
         double $$10 = (this.random.nextDouble() * 2.0 - 1.0) * this.dimensions.width();
         this.level().addParticle(ParticleTypes.SPLASH, this.getX() + $$9, $$4 + 1.0F, this.getZ() + $$10, $$2.x, $$2.y, $$2.z);
      }

      this.gameEvent(GameEvent.SPLASH);
   }

   @Deprecated
   protected BlockState getBlockStateOnLegacy() {
      return this.level().getBlockState(this.getOnPosLegacy());
   }

   public BlockState getBlockStateOn() {
      return this.level().getBlockState(this.getOnPos());
   }

   public boolean canSpawnSprintParticle() {
      return this.isSprinting() && !this.isInWater() && !this.isSpectator() && !this.isCrouching() && !this.isInLava() && this.isAlive();
   }

   protected void spawnSprintParticle() {
      BlockPos $$0 = this.getOnPosLegacy();
      BlockState $$1 = this.level().getBlockState($$0);
      if ($$1.getRenderShape() != RenderShape.INVISIBLE) {
         Vec3 $$2 = this.getDeltaMovement();
         BlockPos $$3 = this.blockPosition();
         double $$4 = this.getX() + (this.random.nextDouble() - 0.5) * this.dimensions.width();
         double $$5 = this.getZ() + (this.random.nextDouble() - 0.5) * this.dimensions.width();
         if ($$3.getX() != $$0.getX()) {
            $$4 = Mth.clamp($$4, $$0.getX(), $$0.getX() + 1.0);
         }

         if ($$3.getZ() != $$0.getZ()) {
            $$5 = Mth.clamp($$5, $$0.getZ(), $$0.getZ() + 1.0);
         }

         this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, $$1), $$4, this.getY() + 0.1, $$5, $$2.x * -4.0, 1.5, $$2.z * -4.0);
      }
   }

   public boolean isEyeInFluid(TagKey<Fluid> $$0) {
      return this.fluidOnEyes.contains($$0);
   }

   public boolean isInLava() {
      return !this.firstTick && this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0;
   }

   public void moveRelative(float $$0, Vec3 $$1) {
      Vec3 $$2 = getInputVector($$1, $$0, this.getYRot());
      this.setDeltaMovement(this.getDeltaMovement().add($$2));
   }

   protected static Vec3 getInputVector(Vec3 $$0, float $$1, float $$2) {
      double $$3 = $$0.lengthSqr();
      if ($$3 < 1.0E-7) {
         return Vec3.ZERO;
      } else {
         Vec3 $$4 = ($$3 > 1.0 ? $$0.normalize() : $$0).scale($$1);
         float $$5 = Mth.sin($$2 * (float) (Math.PI / 180.0));
         float $$6 = Mth.cos($$2 * (float) (Math.PI / 180.0));
         return new Vec3($$4.x * $$6 - $$4.z * $$5, $$4.y, $$4.z * $$6 + $$4.x * $$5);
      }
   }

   @Deprecated
   public float getLightLevelDependentMagicValue() {
      return this.level().hasChunkAt(this.getBlockX(), this.getBlockZ())
         ? this.level().getLightLevelDependentMagicValue(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ()))
         : 0.0F;
   }

   public void absSnapTo(double $$0, double $$1, double $$2, float $$3, float $$4) {
      this.absSnapTo($$0, $$1, $$2);
      this.absSnapRotationTo($$3, $$4);
   }

   public void absSnapRotationTo(float $$0, float $$1) {
      this.setYRot($$0 % 360.0F);
      this.setXRot(Mth.clamp($$1, -90.0F, 90.0F) % 360.0F);
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   public void absSnapTo(double $$0, double $$1, double $$2) {
      double $$3 = Mth.clamp($$0, -3.0E7, 3.0E7);
      double $$4 = Mth.clamp($$2, -3.0E7, 3.0E7);
      this.xo = $$3;
      this.yo = $$1;
      this.zo = $$4;
      this.setPos($$3, $$1, $$4);
   }

   public void snapTo(Vec3 $$0) {
      this.snapTo($$0.x, $$0.y, $$0.z);
   }

   public void snapTo(double $$0, double $$1, double $$2) {
      this.snapTo($$0, $$1, $$2, this.getYRot(), this.getXRot());
   }

   public void snapTo(BlockPos $$0, float $$1, float $$2) {
      this.snapTo($$0.getBottomCenter(), $$1, $$2);
   }

   public void snapTo(Vec3 $$0, float $$1, float $$2) {
      this.snapTo($$0.x, $$0.y, $$0.z, $$1, $$2);
   }

   public void snapTo(double $$0, double $$1, double $$2, float $$3, float $$4) {
      this.setPosRaw($$0, $$1, $$2);
      this.setYRot($$3);
      this.setXRot($$4);
      this.setOldPosAndRot();
      this.reapplyPosition();
   }

   public final void setOldPosAndRot() {
      this.setOldPos();
      this.setOldRot();
   }

   public final void setOldPosAndRot(Vec3 $$0, float $$1, float $$2) {
      this.setOldPos($$0);
      this.setOldRot($$1, $$2);
   }

   protected void setOldPos() {
      this.setOldPos(this.position);
   }

   public void setOldRot() {
      this.setOldRot(this.getYRot(), this.getXRot());
   }

   private void setOldPos(Vec3 $$0) {
      this.xo = this.xOld = $$0.x;
      this.yo = this.yOld = $$0.y;
      this.zo = this.zOld = $$0.z;
   }

   private void setOldRot(float $$0, float $$1) {
      this.yRotO = $$0;
      this.xRotO = $$1;
   }

   public final Vec3 oldPosition() {
      return new Vec3(this.xOld, this.yOld, this.zOld);
   }

   public float distanceTo(net.minecraft.world.entity.Entity $$0) {
      float $$1 = (float)(this.getX() - $$0.getX());
      float $$2 = (float)(this.getY() - $$0.getY());
      float $$3 = (float)(this.getZ() - $$0.getZ());
      return Mth.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
   }

   public double distanceToSqr(double $$0, double $$1, double $$2) {
      double $$3 = this.getX() - $$0;
      double $$4 = this.getY() - $$1;
      double $$5 = this.getZ() - $$2;
      return $$3 * $$3 + $$4 * $$4 + $$5 * $$5;
   }

   public double distanceToSqr(net.minecraft.world.entity.Entity $$0) {
      return this.distanceToSqr($$0.position());
   }

   public double distanceToSqr(Vec3 $$0) {
      double $$1 = this.getX() - $$0.x;
      double $$2 = this.getY() - $$0.y;
      double $$3 = this.getZ() - $$0.z;
      return $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
   }

   public void playerTouch(Player $$0) {
   }

   public void push(net.minecraft.world.entity.Entity $$0) {
      if (!this.isPassengerOfSameVehicle($$0)) {
         if (!$$0.noPhysics && !this.noPhysics) {
            double $$1 = $$0.getX() - this.getX();
            double $$2 = $$0.getZ() - this.getZ();
            double $$3 = Mth.absMax($$1, $$2);
            if ($$3 >= 0.01F) {
               $$3 = Math.sqrt($$3);
               $$1 /= $$3;
               $$2 /= $$3;
               double $$4 = 1.0 / $$3;
               if ($$4 > 1.0) {
                  $$4 = 1.0;
               }

               $$1 *= $$4;
               $$2 *= $$4;
               $$1 *= 0.05F;
               $$2 *= 0.05F;
               if (!this.isVehicle() && this.isPushable()) {
                  this.push(-$$1, 0.0, -$$2);
               }

               if (!$$0.isVehicle() && $$0.isPushable()) {
                  $$0.push($$1, 0.0, $$2);
               }
            }
         }
      }
   }

   public void push(Vec3 $$0) {
      if ($$0.isFinite()) {
         this.push($$0.x, $$0.y, $$0.z);
      }
   }

   public void push(double $$0, double $$1, double $$2) {
      if (Double.isFinite($$0) && Double.isFinite($$1) && Double.isFinite($$2)) {
         this.setDeltaMovement(this.getDeltaMovement().add($$0, $$1, $$2));
         this.needsSync = true;
      }
   }

   protected void markHurt() {
      this.hurtMarked = true;
   }

   @Deprecated
   public final void hurt(DamageSource $$0, float $$1) {
      if (this.level instanceof ServerLevel $$2) {
         this.hurtServer($$2, $$0, $$1);
      }
   }

   @Deprecated
   public final boolean hurtOrSimulate(DamageSource $$0, float $$1) {
      return this.level instanceof ServerLevel $$2 ? this.hurtServer($$2, $$0, $$1) : this.hurtClient($$0);
   }

   public abstract boolean hurtServer(ServerLevel var1, DamageSource var2, float var3);

   public boolean hurtClient(DamageSource $$0) {
      return false;
   }

   public final Vec3 getViewVector(float $$0) {
      return this.calculateViewVector(this.getViewXRot($$0), this.getViewYRot($$0));
   }

   public Direction getNearestViewDirection() {
      return Direction.getApproximateNearest(this.getViewVector(1.0F));
   }

   public float getViewXRot(float $$0) {
      return this.getXRot($$0);
   }

   public float getViewYRot(float $$0) {
      return this.getYRot($$0);
   }

   public float getXRot(float $$0) {
      return $$0 == 1.0F ? this.getXRot() : Mth.lerp($$0, this.xRotO, this.getXRot());
   }

   public float getYRot(float $$0) {
      return $$0 == 1.0F ? this.getYRot() : Mth.rotLerp($$0, this.yRotO, this.getYRot());
   }

   public final Vec3 calculateViewVector(float $$0, float $$1) {
      float $$2 = $$0 * (float) (Math.PI / 180.0);
      float $$3 = -$$1 * (float) (Math.PI / 180.0);
      float $$4 = Mth.cos($$3);
      float $$5 = Mth.sin($$3);
      float $$6 = Mth.cos($$2);
      float $$7 = Mth.sin($$2);
      return new Vec3($$5 * $$6, -$$7, $$4 * $$6);
   }

   public final Vec3 getUpVector(float $$0) {
      return this.calculateUpVector(this.getViewXRot($$0), this.getViewYRot($$0));
   }

   protected final Vec3 calculateUpVector(float $$0, float $$1) {
      return this.calculateViewVector($$0 - 90.0F, $$1);
   }

   public final Vec3 getEyePosition() {
      return new Vec3(this.getX(), this.getEyeY(), this.getZ());
   }

   public final Vec3 getEyePosition(float $$0) {
      double $$1 = Mth.lerp($$0, this.xo, this.getX());
      double $$2 = Mth.lerp($$0, this.yo, this.getY()) + this.getEyeHeight();
      double $$3 = Mth.lerp($$0, this.zo, this.getZ());
      return new Vec3($$1, $$2, $$3);
   }

   public Vec3 getLightProbePosition(float $$0) {
      return this.getEyePosition($$0);
   }

   public final Vec3 getPosition(float $$0) {
      double $$1 = Mth.lerp($$0, this.xo, this.getX());
      double $$2 = Mth.lerp($$0, this.yo, this.getY());
      double $$3 = Mth.lerp($$0, this.zo, this.getZ());
      return new Vec3($$1, $$2, $$3);
   }

   public HitResult pick(double $$0, float $$1, boolean $$2) {
      Vec3 $$3 = this.getEyePosition($$1);
      Vec3 $$4 = this.getViewVector($$1);
      Vec3 $$5 = $$3.add($$4.x * $$0, $$4.y * $$0, $$4.z * $$0);
      return this.level()
         .clip(
            new ClipContext(
               $$3, $$5, Block.OUTLINE, $$2 ? net.minecraft.world.level.ClipContext.Fluid.ANY : net.minecraft.world.level.ClipContext.Fluid.NONE, this
            )
         );
   }

   public boolean canBeHitByProjectile() {
      return this.isAlive() && this.isPickable();
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isPushable() {
      return false;
   }

   public void awardKillScore(net.minecraft.world.entity.Entity $$0, DamageSource $$1) {
      if ($$0 instanceof ServerPlayer) {
         CriteriaTriggers.ENTITY_KILLED_PLAYER.trigger((ServerPlayer)$$0, this, $$1);
      }
   }

   public boolean shouldRender(double $$0, double $$1, double $$2) {
      double $$3 = this.getX() - $$0;
      double $$4 = this.getY() - $$1;
      double $$5 = this.getZ() - $$2;
      double $$6 = $$3 * $$3 + $$4 * $$4 + $$5 * $$5;
      return this.shouldRenderAtSqrDistance($$6);
   }

   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = this.getBoundingBox().getSize();
      if (Double.isNaN($$1)) {
         $$1 = 1.0;
      }

      $$1 *= 64.0 * viewScale;
      return $$0 < $$1 * $$1;
   }

   public boolean saveAsPassenger(ValueOutput $$0) {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else {
         String $$1 = this.getEncodeId();
         if ($$1 == null) {
            return false;
         } else {
            $$0.putString("id", $$1);
            this.saveWithoutId($$0);
            return true;
         }
      }
   }

   public boolean save(ValueOutput $$0) {
      return this.isPassenger() ? false : this.saveAsPassenger($$0);
   }

   public void saveWithoutId(ValueOutput $$0) {
      try {
         if (this.vehicle != null) {
            $$0.store("Pos", Vec3.CODEC, new Vec3(this.vehicle.getX(), this.getY(), this.vehicle.getZ()));
         } else {
            $$0.store("Pos", Vec3.CODEC, this.position());
         }

         $$0.store("Motion", Vec3.CODEC, this.getDeltaMovement());
         $$0.store("Rotation", Vec2.CODEC, new Vec2(this.getYRot(), this.getXRot()));
         $$0.putDouble("fall_distance", this.fallDistance);
         $$0.putShort("Fire", (short)this.remainingFireTicks);
         $$0.putShort("Air", (short)this.getAirSupply());
         $$0.putBoolean("OnGround", this.onGround());
         $$0.putBoolean("Invulnerable", this.invulnerable);
         $$0.putInt("PortalCooldown", this.portalCooldown);
         $$0.store("UUID", UUIDUtil.CODEC, this.getUUID());
         $$0.storeNullable("CustomName", ComponentSerialization.CODEC, this.getCustomName());
         if (this.isCustomNameVisible()) {
            $$0.putBoolean("CustomNameVisible", this.isCustomNameVisible());
         }

         if (this.isSilent()) {
            $$0.putBoolean("Silent", this.isSilent());
         }

         if (this.isNoGravity()) {
            $$0.putBoolean("NoGravity", this.isNoGravity());
         }

         if (this.hasGlowingTag) {
            $$0.putBoolean("Glowing", true);
         }

         int $$1 = this.getTicksFrozen();
         if ($$1 > 0) {
            $$0.putInt("TicksFrozen", this.getTicksFrozen());
         }

         if (this.hasVisualFire) {
            $$0.putBoolean("HasVisualFire", this.hasVisualFire);
         }

         if (!this.tags.isEmpty()) {
            $$0.store("Tags", TAG_LIST_CODEC, List.copyOf(this.tags));
         }

         if (!this.customData.isEmpty()) {
            $$0.store("data", CustomData.CODEC, this.customData);
         }

         this.addAdditionalSaveData($$0);
         if (this.isVehicle()) {
            ValueOutputList $$2 = $$0.childrenList("Passengers");

            for (net.minecraft.world.entity.Entity $$3 : this.getPassengers()) {
               ValueOutput $$4 = $$2.addChild();
               if (!$$3.saveAsPassenger($$4)) {
                  $$2.discardLast();
               }
            }

            if ($$2.isEmpty()) {
               $$0.discard("Passengers");
            }
         }
      } catch (Throwable var7) {
         CrashReport $$6 = CrashReport.forThrowable(var7, "Saving entity NBT");
         CrashReportCategory $$7 = $$6.addCategory("Entity being saved");
         this.fillCrashReportCategory($$7);
         throw new ReportedException($$6);
      }
   }

   public void load(ValueInput $$0) {
      try {
         Vec3 $$1 = $$0.read("Pos", Vec3.CODEC).orElse(Vec3.ZERO);
         Vec3 $$2 = $$0.read("Motion", Vec3.CODEC).orElse(Vec3.ZERO);
         Vec2 $$3 = $$0.read("Rotation", Vec2.CODEC).orElse(Vec2.ZERO);
         this.setDeltaMovement(Math.abs($$2.x) > 10.0 ? 0.0 : $$2.x, Math.abs($$2.y) > 10.0 ? 0.0 : $$2.y, Math.abs($$2.z) > 10.0 ? 0.0 : $$2.z);
         this.needsSync = true;
         double $$4 = 3.0000512E7;
         this.setPosRaw(Mth.clamp($$1.x, -3.0000512E7, 3.0000512E7), Mth.clamp($$1.y, -2.0E7, 2.0E7), Mth.clamp($$1.z, -3.0000512E7, 3.0000512E7));
         this.setYRot($$3.x);
         this.setXRot($$3.y);
         this.setOldPosAndRot();
         this.setYHeadRot(this.getYRot());
         this.setYBodyRot(this.getYRot());
         this.fallDistance = $$0.getDoubleOr("fall_distance", 0.0);
         this.remainingFireTicks = $$0.getShortOr("Fire", (short)0);
         this.setAirSupply($$0.getIntOr("Air", this.getMaxAirSupply()));
         this.onGround = $$0.getBooleanOr("OnGround", false);
         this.invulnerable = $$0.getBooleanOr("Invulnerable", false);
         this.portalCooldown = $$0.getIntOr("PortalCooldown", 0);
         $$0.read("UUID", UUIDUtil.CODEC).ifPresent($$0x -> {
            this.uuid = $$0x;
            this.stringUUID = this.uuid.toString();
         });
         if (!Double.isFinite(this.getX()) || !Double.isFinite(this.getY()) || !Double.isFinite(this.getZ())) {
            throw new IllegalStateException("Entity has invalid position");
         } else if (Double.isFinite(this.getYRot()) && Double.isFinite(this.getXRot())) {
            this.reapplyPosition();
            this.setRot(this.getYRot(), this.getXRot());
            this.setCustomName((Component)$$0.read("CustomName", ComponentSerialization.CODEC).orElse(null));
            this.setCustomNameVisible($$0.getBooleanOr("CustomNameVisible", false));
            this.setSilent($$0.getBooleanOr("Silent", false));
            this.setNoGravity($$0.getBooleanOr("NoGravity", false));
            this.setGlowingTag($$0.getBooleanOr("Glowing", false));
            this.setTicksFrozen($$0.getIntOr("TicksFrozen", 0));
            this.hasVisualFire = $$0.getBooleanOr("HasVisualFire", false);
            this.customData = $$0.read("data", CustomData.CODEC).orElse(CustomData.EMPTY);
            this.tags.clear();
            $$0.read("Tags", TAG_LIST_CODEC).ifPresent(this.tags::addAll);
            this.readAdditionalSaveData($$0);
            if (this.repositionEntityAfterLoad()) {
               this.reapplyPosition();
            }
         } else {
            throw new IllegalStateException("Entity has invalid rotation");
         }
      } catch (Throwable var7) {
         CrashReport $$6 = CrashReport.forThrowable(var7, "Loading entity NBT");
         CrashReportCategory $$7 = $$6.addCategory("Entity being loaded");
         this.fillCrashReportCategory($$7);
         throw new ReportedException($$6);
      }
   }

   protected boolean repositionEntityAfterLoad() {
      return true;
   }

   @Nullable
   protected final String getEncodeId() {
      net.minecraft.world.entity.EntityType<?> $$0 = this.getType();
      Identifier $$1 = net.minecraft.world.entity.EntityType.getKey($$0);
      return !$$0.canSerialize() ? null : $$1.toString();
   }

   protected abstract void readAdditionalSaveData(ValueInput var1);

   protected abstract void addAdditionalSaveData(ValueOutput var1);

   @Nullable
   public ItemEntity spawnAtLocation(ServerLevel $$0, ItemLike $$1) {
      return this.spawnAtLocation($$0, new ItemStack($$1), 0.0F);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ServerLevel $$0, ItemStack $$1) {
      return this.spawnAtLocation($$0, $$1, 0.0F);
   }

   @Nullable
   public ItemEntity spawnAtLocation(ServerLevel $$0, ItemStack $$1, Vec3 $$2) {
      if ($$1.isEmpty()) {
         return null;
      } else {
         ItemEntity $$3 = new ItemEntity($$0, this.getX() + $$2.x, this.getY() + $$2.y, this.getZ() + $$2.z, $$1);
         $$3.setDefaultPickUpDelay();
         $$0.addFreshEntity($$3);
         return $$3;
      }
   }

   @Nullable
   public ItemEntity spawnAtLocation(ServerLevel $$0, ItemStack $$1, float $$2) {
      return this.spawnAtLocation($$0, $$1, new Vec3(0.0, $$2, 0.0));
   }

   public boolean isAlive() {
      return !this.isRemoved();
   }

   public boolean isInWall() {
      if (this.noPhysics) {
         return false;
      } else {
         float $$0 = this.dimensions.width() * 0.8F;
         AABB $$1 = AABB.ofSize(this.getEyePosition(), $$0, 1.0E-6, $$0);
         return BlockPos.betweenClosedStream($$1)
            .anyMatch(
               $$1x -> {
                  BlockState $$2 = this.level().getBlockState($$1x);
                  return !$$2.isAir()
                     && $$2.isSuffocating(this.level(), $$1x)
                     && Shapes.joinIsNotEmpty($$2.getCollisionShape(this.level(), $$1x).move($$1x), Shapes.create($$1), BooleanOp.AND);
               }
            );
      }
   }

   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      if (!this.level().isClientSide()
         && $$0.isSecondaryUseActive()
         && this instanceof net.minecraft.world.entity.Leashable $$2
         && $$2.canBeLeashed()
         && this.isAlive()
         && !(this instanceof net.minecraft.world.entity.LivingEntity $$3 && $$3.isBaby())) {
         List<net.minecraft.world.entity.Leashable> $$4 = net.minecraft.world.entity.Leashable.leashableInArea(this, $$1x -> $$1x.getLeashHolder() == $$0);
         if (!$$4.isEmpty()) {
            boolean $$5 = false;

            for (net.minecraft.world.entity.Leashable $$6 : $$4) {
               if ($$6.canHaveALeashAttachedTo(this)) {
                  $$6.setLeashedTo(this, true);
                  $$5 = true;
               }
            }

            if ($$5) {
               this.level().gameEvent(GameEvent.ENTITY_ACTION, this.blockPosition(), Context.of($$0));
               this.playSound(SoundEvents.LEAD_TIED);
               return InteractionResult.SUCCESS_SERVER.withoutItem();
            }
         }
      }

      ItemStack $$7 = $$0.getItemInHand($$1);
      if ($$7.is(Items.SHEARS) && this.shearOffAllLeashConnections($$0)) {
         $$7.hurtAndBreak(1, $$0, $$1);
         return InteractionResult.SUCCESS;
      } else if (this instanceof net.minecraft.world.entity.Mob $$8
         && $$7.is(Items.SHEARS)
         && $$8.canShearEquipment($$0)
         && !$$0.isSecondaryUseActive()
         && this.attemptToShearEquipment($$0, $$1, $$7, $$8)) {
         return InteractionResult.SUCCESS;
      } else {
         if (this.isAlive() && this instanceof net.minecraft.world.entity.Leashable $$9) {
            if ($$9.getLeashHolder() == $$0) {
               if (!this.level().isClientSide()) {
                  if ($$0.hasInfiniteMaterials()) {
                     $$9.removeLeash();
                  } else {
                     $$9.dropLeash();
                  }

                  this.gameEvent(GameEvent.ENTITY_INTERACT, $$0);
                  this.playSound(SoundEvents.LEAD_UNTIED);
               }

               return InteractionResult.SUCCESS.withoutItem();
            }

            ItemStack $$10 = $$0.getItemInHand($$1);
            if ($$10.is(Items.LEAD) && !($$9.getLeashHolder() instanceof Player)) {
               if (this.level().isClientSide()) {
                  return InteractionResult.CONSUME;
               }

               if ($$9.canHaveALeashAttachedTo($$0)) {
                  if ($$9.isLeashed()) {
                     $$9.dropLeash();
                  }

                  $$9.setLeashedTo($$0, true);
                  this.playSound(SoundEvents.LEAD_TIED);
                  $$10.shrink(1);
                  return InteractionResult.SUCCESS_SERVER;
               }
            }
         }

         return InteractionResult.PASS;
      }
   }

   public boolean shearOffAllLeashConnections(@Nullable Player $$0) {
      boolean $$1 = this.dropAllLeashConnections($$0);
      if ($$1 && this.level() instanceof ServerLevel $$2) {
         $$2.playSound(null, this.blockPosition(), SoundEvents.SHEARS_SNIP, $$0 != null ? $$0.getSoundSource() : this.getSoundSource());
      }

      return $$1;
   }

   public boolean dropAllLeashConnections(@Nullable Player $$0) {
      List<net.minecraft.world.entity.Leashable> $$1 = net.minecraft.world.entity.Leashable.leashableLeashedTo(this);
      boolean $$2 = !$$1.isEmpty();
      if (this instanceof net.minecraft.world.entity.Leashable $$3 && $$3.isLeashed()) {
         $$3.dropLeash();
         $$2 = true;
      }

      for (net.minecraft.world.entity.Leashable $$4 : $$1) {
         $$4.dropLeash();
      }

      if ($$2) {
         this.gameEvent(GameEvent.SHEAR, $$0);
         return true;
      } else {
         return false;
      }
   }

   private boolean attemptToShearEquipment(Player $$0, InteractionHand $$1, ItemStack $$2, net.minecraft.world.entity.Mob $$3) {
      for (net.minecraft.world.entity.EquipmentSlot $$4 : net.minecraft.world.entity.EquipmentSlot.VALUES) {
         ItemStack $$5 = $$3.getItemBySlot($$4);
         Equippable $$6 = (Equippable)$$5.get(DataComponents.EQUIPPABLE);
         if ($$6 != null && $$6.canBeSheared() && (!EnchantmentHelper.has($$5, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || $$0.isCreative())) {
            $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
            Vec3 $$7 = this.dimensions.attachments().getAverage(net.minecraft.world.entity.EntityAttachment.PASSENGER);
            $$3.setItemSlotAndDropWhenKilled($$4, ItemStack.EMPTY);
            this.gameEvent(GameEvent.SHEAR, $$0);
            this.playSound((SoundEvent)$$6.shearingSound().value());
            if (this.level() instanceof ServerLevel $$8) {
               this.spawnAtLocation($$8, $$5, $$7);
               CriteriaTriggers.PLAYER_SHEARED_EQUIPMENT.trigger((ServerPlayer)$$0, $$5, $$3);
            }

            return true;
         }
      }

      return false;
   }

   public boolean canCollideWith(net.minecraft.world.entity.Entity $$0) {
      return $$0.canBeCollidedWith(this) && !this.isPassengerOfSameVehicle($$0);
   }

   public boolean canBeCollidedWith(@Nullable net.minecraft.world.entity.Entity $$0) {
      return false;
   }

   public void rideTick() {
      this.setDeltaMovement(Vec3.ZERO);
      this.tick();
      if (this.isPassenger()) {
         this.getVehicle().positionRider(this);
      }
   }

   public final void positionRider(net.minecraft.world.entity.Entity $$0) {
      if (this.hasPassenger($$0)) {
         this.positionRider($$0, net.minecraft.world.entity.Entity::setPos);
      }
   }

   protected void positionRider(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Entity.MoveFunction $$1) {
      Vec3 $$2 = this.getPassengerRidingPosition($$0);
      Vec3 $$3 = $$0.getVehicleAttachmentPoint(this);
      $$1.accept($$0, $$2.x - $$3.x, $$2.y - $$3.y, $$2.z - $$3.z);
   }

   public void onPassengerTurned(net.minecraft.world.entity.Entity $$0) {
   }

   public Vec3 getVehicleAttachmentPoint(net.minecraft.world.entity.Entity $$0) {
      return this.getAttachments().get(net.minecraft.world.entity.EntityAttachment.VEHICLE, 0, this.yRot);
   }

   public Vec3 getPassengerRidingPosition(net.minecraft.world.entity.Entity $$0) {
      return this.position().add(this.getPassengerAttachmentPoint($$0, this.dimensions, 1.0F));
   }

   protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.EntityDimensions $$1, float $$2) {
      return getDefaultPassengerAttachmentPoint(this, $$0, $$1.attachments());
   }

   protected static Vec3 getDefaultPassengerAttachmentPoint(
      net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Entity $$1, net.minecraft.world.entity.EntityAttachments $$2
   ) {
      int $$3 = $$0.getPassengers().indexOf($$1);
      return $$2.getClamped(net.minecraft.world.entity.EntityAttachment.PASSENGER, $$3, $$0.yRot);
   }

   public final boolean startRiding(net.minecraft.world.entity.Entity $$0) {
      return this.startRiding($$0, false, true);
   }

   public boolean showVehicleHealth() {
      return this instanceof net.minecraft.world.entity.LivingEntity;
   }

   public boolean startRiding(net.minecraft.world.entity.Entity $$0, boolean $$1, boolean $$2) {
      if ($$0 == this.vehicle) {
         return false;
      } else if (!$$0.couldAcceptPassenger()) {
         return false;
      } else if (!this.level().isClientSide() && !$$0.type.canSerialize()) {
         return false;
      } else {
         for (net.minecraft.world.entity.Entity $$3 = $$0; $$3.vehicle != null; $$3 = $$3.vehicle) {
            if ($$3.vehicle == this) {
               return false;
            }
         }

         if ($$1 || this.canRide($$0) && $$0.canAddPassenger(this)) {
            if (this.isPassenger()) {
               this.stopRiding();
            }

            this.setPose(net.minecraft.world.entity.Pose.STANDING);
            this.vehicle = $$0;
            this.vehicle.addPassenger(this);
            if ($$2) {
               this.level().gameEvent(this, GameEvent.ENTITY_MOUNT, this.vehicle.position);
               $$0.getIndirectPassengersStream()
                  .filter($$0x -> $$0x instanceof ServerPlayer)
                  .forEach($$0x -> CriteriaTriggers.START_RIDING_TRIGGER.trigger((ServerPlayer)$$0x));
            }

            return true;
         } else {
            return false;
         }
      }
   }

   protected boolean canRide(net.minecraft.world.entity.Entity $$0) {
      return !this.isShiftKeyDown() && this.boardingCooldown <= 0;
   }

   public void ejectPassengers() {
      for (int $$0 = this.passengers.size() - 1; $$0 >= 0; $$0--) {
         ((net.minecraft.world.entity.Entity)this.passengers.get($$0)).stopRiding();
      }
   }

   public void removeVehicle() {
      if (this.vehicle != null) {
         net.minecraft.world.entity.Entity $$0 = this.vehicle;
         this.vehicle = null;
         $$0.removePassenger(this);
         net.minecraft.world.entity.Entity.RemovalReason $$1 = this.getRemovalReason();
         if ($$1 == null || $$1.shouldDestroy()) {
            this.level().gameEvent(this, GameEvent.ENTITY_DISMOUNT, $$0.position);
         }
      }
   }

   public void stopRiding() {
      this.removeVehicle();
   }

   protected void addPassenger(net.minecraft.world.entity.Entity $$0) {
      if ($$0.getVehicle() != this) {
         throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
      } else {
         if (this.passengers.isEmpty()) {
            this.passengers = ImmutableList.of($$0);
         } else {
            List<net.minecraft.world.entity.Entity> $$1 = Lists.newArrayList(this.passengers);
            if (!this.level().isClientSide() && $$0 instanceof Player && !(this.getFirstPassenger() instanceof Player)) {
               $$1.add(0, $$0);
            } else {
               $$1.add($$0);
            }

            this.passengers = ImmutableList.copyOf($$1);
         }
      }
   }

   protected void removePassenger(net.minecraft.world.entity.Entity $$0) {
      if ($$0.getVehicle() == this) {
         throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
      } else {
         if (this.passengers.size() == 1 && this.passengers.get(0) == $$0) {
            this.passengers = ImmutableList.of();
         } else {
            this.passengers = this.passengers.stream().filter($$1 -> $$1 != $$0).collect(ImmutableList.toImmutableList());
         }

         $$0.boardingCooldown = 60;
      }
   }

   protected boolean canAddPassenger(net.minecraft.world.entity.Entity $$0) {
      return this.passengers.isEmpty();
   }

   protected boolean couldAcceptPassenger() {
      return true;
   }

   public final boolean isInterpolating() {
      return this.getInterpolation() != null && this.getInterpolation().hasActiveInterpolation();
   }

   public final void moveOrInterpolateTo(Vec3 $$0, float $$1, float $$2) {
      this.moveOrInterpolateTo(Optional.of($$0), Optional.of($$1), Optional.of($$2));
   }

   public final void moveOrInterpolateTo(float $$0, float $$1) {
      this.moveOrInterpolateTo(Optional.empty(), Optional.of($$0), Optional.of($$1));
   }

   public final void moveOrInterpolateTo(Vec3 $$0) {
      this.moveOrInterpolateTo(Optional.of($$0), Optional.empty(), Optional.empty());
   }

   public final void moveOrInterpolateTo(Optional<Vec3> $$0, Optional<Float> $$1, Optional<Float> $$2) {
      net.minecraft.world.entity.InterpolationHandler $$3 = this.getInterpolation();
      if ($$3 != null) {
         $$3.interpolateTo($$0.orElse($$3.position()), $$1.orElse($$3.yRot()), $$2.orElse($$3.xRot()));
      } else {
         $$0.ifPresent(this::setPos);
         $$1.ifPresent($$0x -> this.setYRot($$0x % 360.0F));
         $$2.ifPresent($$0x -> this.setXRot($$0x % 360.0F));
      }
   }

   @Nullable
   public net.minecraft.world.entity.InterpolationHandler getInterpolation() {
      return null;
   }

   public void lerpHeadTo(float $$0, int $$1) {
      this.setYHeadRot($$0);
   }

   public float getPickRadius() {
      return 0.0F;
   }

   public Vec3 getLookAngle() {
      return this.calculateViewVector(this.getXRot(), this.getYRot());
   }

   public Vec3 getHeadLookAngle() {
      return this.calculateViewVector(this.getXRot(), this.getYHeadRot());
   }

   public Vec3 getHandHoldingItemAngle(Item $$0) {
      if (!(this instanceof Player $$1)) {
         return Vec3.ZERO;
      } else {
         boolean $$2 = $$1.getOffhandItem().is($$0) && !$$1.getMainHandItem().is($$0);
         net.minecraft.world.entity.HumanoidArm $$3 = $$2 ? $$1.getMainArm().getOpposite() : $$1.getMainArm();
         return this.calculateViewVector(0.0F, this.getYRot() + ($$3 == net.minecraft.world.entity.HumanoidArm.RIGHT ? 80 : -80)).scale(0.5);
      }
   }

   public Vec2 getRotationVector() {
      return new Vec2(this.getXRot(), this.getYRot());
   }

   public Vec3 getForward() {
      return Vec3.directionFromRotation(this.getRotationVector());
   }

   public void setAsInsidePortal(Portal $$0, BlockPos $$1) {
      if (this.isOnPortalCooldown()) {
         this.setPortalCooldown();
      } else {
         if (this.portalProcess == null || !this.portalProcess.isSamePortal($$0)) {
            this.portalProcess = new net.minecraft.world.entity.PortalProcessor($$0, $$1.immutable());
         } else if (!this.portalProcess.isInsidePortalThisTick()) {
            this.portalProcess.updateEntryPosition($$1.immutable());
            this.portalProcess.setAsInsidePortalThisTick(true);
         }
      }
   }

   protected void handlePortal() {
      if (this.level() instanceof ServerLevel $$0) {
         this.processPortalCooldown();
         if (this.portalProcess != null) {
            if (this.portalProcess.processPortalTeleportation($$0, this, this.canUsePortal(false))) {
               ProfilerFiller $$2 = Profiler.get();
               $$2.push("portal");
               this.setPortalCooldown();
               TeleportTransition $$3 = this.portalProcess.getPortalDestination($$0, this);
               if ($$3 != null) {
                  ServerLevel $$4 = $$3.newLevel();
                  if ($$0.isAllowedToEnterPortal($$4) && ($$4.dimension() == $$0.dimension() || this.canTeleport($$0, $$4))) {
                     this.teleport($$3);
                  }
               }

               $$2.pop();
            } else if (this.portalProcess.hasExpired()) {
               this.portalProcess = null;
            }
         }
      }
   }

   public int getDimensionChangingDelay() {
      net.minecraft.world.entity.Entity $$0 = this.getFirstPassenger();
      return $$0 instanceof ServerPlayer ? $$0.getDimensionChangingDelay() : 300;
   }

   public void lerpMotion(Vec3 $$0) {
      this.setDeltaMovement($$0);
   }

   public void handleDamageEvent(DamageSource $$0) {
   }

   public void handleEntityEvent(byte $$0) {
      switch ($$0) {
         case 53:
            HoneyBlock.showSlideParticles(this);
      }
   }

   public void animateHurt(float $$0) {
   }

   public boolean isOnFire() {
      boolean $$0 = this.level() != null && this.level().isClientSide();
      return !this.fireImmune() && (this.remainingFireTicks > 0 || $$0 && this.getSharedFlag(0));
   }

   public boolean isPassenger() {
      return this.getVehicle() != null;
   }

   public boolean isVehicle() {
      return !this.passengers.isEmpty();
   }

   public boolean dismountsUnderwater() {
      return this.getType().is(EntityTypeTags.DISMOUNTS_UNDERWATER);
   }

   public boolean canControlVehicle() {
      return !this.getType().is(EntityTypeTags.NON_CONTROLLING_RIDER);
   }

   public void setShiftKeyDown(boolean $$0) {
      this.setSharedFlag(1, $$0);
   }

   public boolean isShiftKeyDown() {
      return this.getSharedFlag(1);
   }

   public boolean isSteppingCarefully() {
      return this.isShiftKeyDown();
   }

   public boolean isSuppressingBounce() {
      return this.isShiftKeyDown();
   }

   public boolean isDiscrete() {
      return this.isShiftKeyDown();
   }

   public boolean isDescending() {
      return this.isShiftKeyDown();
   }

   public boolean isCrouching() {
      return this.hasPose(net.minecraft.world.entity.Pose.CROUCHING);
   }

   public boolean isSprinting() {
      return this.getSharedFlag(3);
   }

   public void setSprinting(boolean $$0) {
      this.setSharedFlag(3, $$0);
   }

   public boolean isSwimming() {
      return this.getSharedFlag(4);
   }

   public boolean isVisuallySwimming() {
      return this.hasPose(net.minecraft.world.entity.Pose.SWIMMING);
   }

   public boolean isVisuallyCrawling() {
      return this.isVisuallySwimming() && !this.isInWater();
   }

   public void setSwimming(boolean $$0) {
      this.setSharedFlag(4, $$0);
   }

   public final boolean hasGlowingTag() {
      return this.hasGlowingTag;
   }

   public final void setGlowingTag(boolean $$0) {
      this.hasGlowingTag = $$0;
      this.setSharedFlag(6, this.isCurrentlyGlowing());
   }

   public boolean isCurrentlyGlowing() {
      return this.level().isClientSide() ? this.getSharedFlag(6) : this.hasGlowingTag;
   }

   public boolean isInvisible() {
      return this.getSharedFlag(5);
   }

   public boolean isInvisibleTo(Player $$0) {
      if ($$0.isSpectator()) {
         return false;
      } else {
         Team $$1 = this.getTeam();
         return $$1 != null && $$0 != null && $$0.getTeam() == $$1 && $$1.canSeeFriendlyInvisibles() ? false : this.isInvisible();
      }
   }

   public boolean isOnRails() {
      return false;
   }

   public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> $$0) {
   }

   @Nullable
   public PlayerTeam getTeam() {
      return this.level().getScoreboard().getPlayersTeam(this.getScoreboardName());
   }

   public final boolean isAlliedTo(@Nullable net.minecraft.world.entity.Entity $$0) {
      return $$0 == null ? false : this == $$0 || this.considersEntityAsAlly($$0) || $$0.considersEntityAsAlly(this);
   }

   protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity $$0) {
      return this.isAlliedTo($$0.getTeam());
   }

   public boolean isAlliedTo(@Nullable Team $$0) {
      return this.getTeam() != null ? this.getTeam().isAlliedTo($$0) : false;
   }

   public void setInvisible(boolean $$0) {
      this.setSharedFlag(5, $$0);
   }

   protected boolean getSharedFlag(int $$0) {
      return ((Byte)this.entityData.get(DATA_SHARED_FLAGS_ID) & 1 << $$0) != 0;
   }

   protected void setSharedFlag(int $$0, boolean $$1) {
      byte $$2 = (Byte)this.entityData.get(DATA_SHARED_FLAGS_ID);
      if ($$1) {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)($$2 | 1 << $$0));
      } else {
         this.entityData.set(DATA_SHARED_FLAGS_ID, (byte)($$2 & ~(1 << $$0)));
      }
   }

   public int getMaxAirSupply() {
      return 300;
   }

   public int getAirSupply() {
      return (Integer)this.entityData.get(DATA_AIR_SUPPLY_ID);
   }

   public void setAirSupply(int $$0) {
      this.entityData.set(DATA_AIR_SUPPLY_ID, $$0);
   }

   public void clearFreeze() {
      this.setTicksFrozen(0);
   }

   public int getTicksFrozen() {
      return (Integer)this.entityData.get(DATA_TICKS_FROZEN);
   }

   public void setTicksFrozen(int $$0) {
      this.entityData.set(DATA_TICKS_FROZEN, $$0);
   }

   public float getPercentFrozen() {
      int $$0 = this.getTicksRequiredToFreeze();
      return (float)Math.min(this.getTicksFrozen(), $$0) / $$0;
   }

   public boolean isFullyFrozen() {
      return this.getTicksFrozen() >= this.getTicksRequiredToFreeze();
   }

   public int getTicksRequiredToFreeze() {
      return 140;
   }

   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      this.setRemainingFireTicks(this.remainingFireTicks + 1);
      if (this.remainingFireTicks == 0) {
         this.igniteForSeconds(8.0F);
      }

      this.hurtServer($$0, this.damageSources().lightningBolt(), 5.0F);
   }

   public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
      handleOnAboveBubbleColumn(this, $$0, $$1);
   }

   protected static void handleOnAboveBubbleColumn(net.minecraft.world.entity.Entity $$0, boolean $$1, BlockPos $$2) {
      Vec3 $$3 = $$0.getDeltaMovement();
      double $$4;
      if ($$1) {
         $$4 = Math.max(-0.9, $$3.y - 0.03);
      } else {
         $$4 = Math.min(1.8, $$3.y + 0.1);
      }

      $$0.setDeltaMovement($$3.x, $$4, $$3.z);
      sendBubbleColumnParticles($$0.level, $$2);
   }

   protected static void sendBubbleColumnParticles(Level $$0, BlockPos $$1) {
      if ($$0 instanceof ServerLevel $$2) {
         for (int $$3 = 0; $$3 < 2; $$3++) {
            $$2.sendParticles(
               ParticleTypes.SPLASH, $$1.getX() + $$0.random.nextDouble(), $$1.getY() + 1, $$1.getZ() + $$0.random.nextDouble(), 1, 0.0, 0.0, 0.0, 1.0
            );
            $$2.sendParticles(
               ParticleTypes.BUBBLE, $$1.getX() + $$0.random.nextDouble(), $$1.getY() + 1, $$1.getZ() + $$0.random.nextDouble(), 1, 0.0, 0.01, 0.0, 0.2
            );
         }
      }
   }

   public void onInsideBubbleColumn(boolean $$0) {
      handleOnInsideBubbleColumn(this, $$0);
   }

   protected static void handleOnInsideBubbleColumn(net.minecraft.world.entity.Entity $$0, boolean $$1) {
      Vec3 $$2 = $$0.getDeltaMovement();
      double $$3;
      if ($$1) {
         $$3 = Math.max(-0.3, $$2.y - 0.03);
      } else {
         $$3 = Math.min(0.7, $$2.y + 0.06);
      }

      $$0.setDeltaMovement($$2.x, $$3, $$2.z);
      $$0.resetFallDistance();
   }

   public boolean killedEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, DamageSource $$2) {
      return true;
   }

   public void checkFallDistanceAccumulation() {
      if (this.getDeltaMovement().y() > -0.5 && this.fallDistance > 1.0) {
         this.fallDistance = 1.0;
      }
   }

   public void resetFallDistance() {
      this.fallDistance = 0.0;
   }

   protected void moveTowardsClosestSpace(double $$0, double $$1, double $$2) {
      BlockPos $$3 = BlockPos.containing($$0, $$1, $$2);
      Vec3 $$4 = new Vec3($$0 - $$3.getX(), $$1 - $$3.getY(), $$2 - $$3.getZ());
      MutableBlockPos $$5 = new MutableBlockPos();
      Direction $$6 = Direction.UP;
      double $$7 = Double.MAX_VALUE;

      for (Direction $$8 : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP}) {
         $$5.setWithOffset($$3, $$8);
         if (!this.level().getBlockState($$5).isCollisionShapeFullBlock(this.level(), $$5)) {
            double $$9 = $$4.get($$8.getAxis());
            double $$10 = $$8.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 - $$9 : $$9;
            if ($$10 < $$7) {
               $$7 = $$10;
               $$6 = $$8;
            }
         }
      }

      float $$11 = this.random.nextFloat() * 0.2F + 0.1F;
      float $$12 = $$6.getAxisDirection().getStep();
      Vec3 $$13 = this.getDeltaMovement().scale(0.75);
      if ($$6.getAxis() == Axis.X) {
         this.setDeltaMovement($$12 * $$11, $$13.y, $$13.z);
      } else if ($$6.getAxis() == Axis.Y) {
         this.setDeltaMovement($$13.x, $$12 * $$11, $$13.z);
      } else if ($$6.getAxis() == Axis.Z) {
         this.setDeltaMovement($$13.x, $$13.y, $$12 * $$11);
      }
   }

   public void makeStuckInBlock(BlockState $$0, Vec3 $$1) {
      this.resetFallDistance();
      this.stuckSpeedMultiplier = $$1;
   }

   private static Component removeAction(Component $$0) {
      MutableComponent $$1 = $$0.plainCopy().setStyle($$0.getStyle().withClickEvent(null));

      for (Component $$2 : $$0.getSiblings()) {
         $$1.append(removeAction($$2));
      }

      return $$1;
   }

   public Component getName() {
      Component $$0 = this.getCustomName();
      return $$0 != null ? removeAction($$0) : this.getTypeName();
   }

   protected Component getTypeName() {
      return this.type.getDescription();
   }

   public boolean is(net.minecraft.world.entity.Entity $$0) {
      return this == $$0;
   }

   public float getYHeadRot() {
      return 0.0F;
   }

   public void setYHeadRot(float $$0) {
   }

   public void setYBodyRot(float $$0) {
   }

   public boolean isAttackable() {
      return true;
   }

   public boolean skipAttackInteraction(net.minecraft.world.entity.Entity $$0) {
      return false;
   }

   @Override
   public String toString() {
      String $$0 = this.level() == null ? "~NULL~" : this.level().toString();
      return this.removalReason != null
         ? String.format(
            Locale.ROOT,
            "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f, removed=%s]",
            this.getClass().getSimpleName(),
            this.getPlainTextName(),
            this.id,
            $$0,
            this.getX(),
            this.getY(),
            this.getZ(),
            this.removalReason
         )
         : String.format(
            Locale.ROOT,
            "%s['%s'/%d, l='%s', x=%.2f, y=%.2f, z=%.2f]",
            this.getClass().getSimpleName(),
            this.getPlainTextName(),
            this.id,
            $$0,
            this.getX(),
            this.getY(),
            this.getZ()
         );
   }

   protected final boolean isInvulnerableToBase(DamageSource $$0) {
      return this.isRemoved()
         || this.invulnerable && !$$0.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !$$0.isCreativePlayer()
         || $$0.is(DamageTypeTags.IS_FIRE) && this.fireImmune()
         || $$0.is(DamageTypeTags.IS_FALL) && this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public void setInvulnerable(boolean $$0) {
      this.invulnerable = $$0;
   }

   public void copyPosition(net.minecraft.world.entity.Entity $$0) {
      this.snapTo($$0.getX(), $$0.getY(), $$0.getZ(), $$0.getYRot(), $$0.getXRot());
   }

   public void restoreFrom(net.minecraft.world.entity.Entity $$0) {
      ScopedCollector $$1 = new ScopedCollector(this.problemPath(), LOGGER);

      try {
         TagValueOutput $$2 = TagValueOutput.createWithContext($$1, $$0.registryAccess());
         $$0.saveWithoutId($$2);
         this.load(TagValueInput.create($$1, this.registryAccess(), $$2.buildResult()));
      } catch (Throwable var6) {
         try {
            $$1.close();
         } catch (Throwable var5) {
            var6.addSuppressed(var5);
         }

         throw var6;
      }

      $$1.close();
      this.portalCooldown = $$0.portalCooldown;
      this.portalProcess = $$0.portalProcess;
   }

   @Nullable
   public net.minecraft.world.entity.Entity teleport(TeleportTransition $$0) {
      if (this.level() instanceof ServerLevel $$1 && !this.isRemoved()) {
         ServerLevel $$3 = $$0.newLevel();
         boolean $$4 = $$3.dimension() != $$1.dimension();
         if (!$$0.asPassenger()) {
            this.stopRiding();
         }

         return $$4 ? this.teleportCrossDimension($$1, $$3, $$0) : this.teleportSameDimension($$1, $$0);
      } else {
         return null;
      }
   }

   private net.minecraft.world.entity.Entity teleportSameDimension(ServerLevel $$0, TeleportTransition $$1) {
      for (net.minecraft.world.entity.Entity $$2 : this.getPassengers()) {
         $$2.teleport(this.calculatePassengerTransition($$1, $$2));
      }

      ProfilerFiller $$3 = Profiler.get();
      $$3.push("teleportSameDimension");
      this.teleportSetPosition(net.minecraft.world.entity.PositionMoveRotation.of($$1), $$1.relatives());
      if (!$$1.asPassenger()) {
         this.sendTeleportTransitionToRidingPlayers($$1);
      }

      $$1.postTeleportTransition().onTransition(this);
      $$3.pop();
      return this;
   }

   @Nullable
   private net.minecraft.world.entity.Entity teleportCrossDimension(ServerLevel $$0, ServerLevel $$1, TeleportTransition $$2) {
      List<net.minecraft.world.entity.Entity> $$3 = this.getPassengers();
      List<net.minecraft.world.entity.Entity> $$4 = new ArrayList<>($$3.size());
      this.ejectPassengers();

      for (net.minecraft.world.entity.Entity $$5 : $$3) {
         net.minecraft.world.entity.Entity $$6 = $$5.teleport(this.calculatePassengerTransition($$2, $$5));
         if ($$6 != null) {
            $$4.add($$6);
         }
      }

      ProfilerFiller $$7 = Profiler.get();
      $$7.push("teleportCrossDimension");
      net.minecraft.world.entity.Entity $$8 = this.getType().create($$1, net.minecraft.world.entity.EntitySpawnReason.DIMENSION_TRAVEL);
      if ($$8 == null) {
         $$7.pop();
         return null;
      } else {
         $$8.restoreFrom(this);
         this.removeAfterChangingDimensions();
         $$8.teleportSetPosition(
            net.minecraft.world.entity.PositionMoveRotation.of(this), net.minecraft.world.entity.PositionMoveRotation.of($$2), $$2.relatives()
         );
         $$1.addDuringTeleport($$8);

         for (net.minecraft.world.entity.Entity $$9 : $$4) {
            $$9.startRiding($$8, true, false);
         }

         $$1.resetEmptyTime();
         $$2.postTeleportTransition().onTransition($$8);
         this.teleportSpectators($$2, $$0);
         $$7.pop();
         return $$8;
      }
   }

   protected void teleportSpectators(TeleportTransition $$0, ServerLevel $$1) {
      for (ServerPlayer $$3 : List.copyOf($$1.players())) {
         if ($$3.getCamera() == this) {
            $$3.teleport($$0);
            $$3.setCamera(null);
         }
      }
   }

   private TeleportTransition calculatePassengerTransition(TeleportTransition $$0, net.minecraft.world.entity.Entity $$1) {
      float $$2 = $$0.yRot() + ($$0.relatives().contains(net.minecraft.world.entity.Relative.Y_ROT) ? 0.0F : $$1.getYRot() - this.getYRot());
      float $$3 = $$0.xRot() + ($$0.relatives().contains(net.minecraft.world.entity.Relative.X_ROT) ? 0.0F : $$1.getXRot() - this.getXRot());
      Vec3 $$4 = $$1.position().subtract(this.position());
      Vec3 $$5 = $$0.position()
         .add(
            $$0.relatives().contains(net.minecraft.world.entity.Relative.X) ? 0.0 : $$4.x(),
            $$0.relatives().contains(net.minecraft.world.entity.Relative.Y) ? 0.0 : $$4.y(),
            $$0.relatives().contains(net.minecraft.world.entity.Relative.Z) ? 0.0 : $$4.z()
         );
      return $$0.withPosition($$5).withRotation($$2, $$3).transitionAsPassenger();
   }

   private void sendTeleportTransitionToRidingPlayers(TeleportTransition $$0) {
      net.minecraft.world.entity.Entity $$1 = this.getControllingPassenger();

      for (net.minecraft.world.entity.Entity $$2 : this.getIndirectPassengers()) {
         if ($$2 instanceof ServerPlayer $$3) {
            if ($$1 != null && $$3.getId() == $$1.getId()) {
               $$3.connection
                  .send(
                     ClientboundTeleportEntityPacket.teleport(
                        this.getId(), net.minecraft.world.entity.PositionMoveRotation.of($$0), $$0.relatives(), this.onGround
                     )
                  );
            } else {
               $$3.connection
                  .send(
                     ClientboundTeleportEntityPacket.teleport(this.getId(), net.minecraft.world.entity.PositionMoveRotation.of(this), Set.of(), this.onGround)
                  );
            }
         }
      }
   }

   public void teleportSetPosition(net.minecraft.world.entity.PositionMoveRotation $$0, Set<net.minecraft.world.entity.Relative> $$1) {
      this.teleportSetPosition(net.minecraft.world.entity.PositionMoveRotation.of(this), $$0, $$1);
   }

   public void teleportSetPosition(
      net.minecraft.world.entity.PositionMoveRotation $$0, net.minecraft.world.entity.PositionMoveRotation $$1, Set<net.minecraft.world.entity.Relative> $$2
   ) {
      net.minecraft.world.entity.PositionMoveRotation $$3 = net.minecraft.world.entity.PositionMoveRotation.calculateAbsolute($$0, $$1, $$2);
      this.setPosRaw($$3.position().x, $$3.position().y, $$3.position().z);
      this.setYRot($$3.yRot());
      this.setYHeadRot($$3.yRot());
      this.setXRot($$3.xRot());
      this.reapplyPosition();
      this.setOldPosAndRot();
      this.setDeltaMovement($$3.deltaMovement());
      this.clearMovementThisTick();
   }

   public void forceSetRotation(float $$0, boolean $$1, float $$2, boolean $$3) {
      Set<net.minecraft.world.entity.Relative> $$4 = net.minecraft.world.entity.Relative.rotation($$1, $$3);
      net.minecraft.world.entity.PositionMoveRotation $$5 = net.minecraft.world.entity.PositionMoveRotation.of(this);
      net.minecraft.world.entity.PositionMoveRotation $$6 = $$5.withRotation($$0, $$2);
      net.minecraft.world.entity.PositionMoveRotation $$7 = net.minecraft.world.entity.PositionMoveRotation.calculateAbsolute($$5, $$6, $$4);
      this.setYRot($$7.yRot());
      this.setYHeadRot($$7.yRot());
      this.setXRot($$7.xRot());
      this.setOldRot();
   }

   public void placePortalTicket(BlockPos $$0) {
      if (this.level() instanceof ServerLevel $$1) {
         $$1.getChunkSource().addTicketWithRadius(TicketType.PORTAL, new ChunkPos($$0), 3);
      }
   }

   protected void removeAfterChangingDimensions() {
      this.setRemoved(net.minecraft.world.entity.Entity.RemovalReason.CHANGED_DIMENSION);
      if (this instanceof net.minecraft.world.entity.Leashable $$0) {
         $$0.removeLeash();
      }

      if (this instanceof WaypointTransmitter $$1 && this.level instanceof ServerLevel $$2) {
         $$2.getWaypointManager().untrackWaypoint($$1);
      }
   }

   public Vec3 getRelativePortalPosition(Axis $$0, FoundRectangle $$1) {
      return PortalShape.getRelativePosition($$1, $$0, this.position(), this.getDimensions(this.getPose()));
   }

   public boolean canUsePortal(boolean $$0) {
      return ($$0 || !this.isPassenger()) && this.isAlive();
   }

   public boolean canTeleport(Level $$0, Level $$1) {
      if ($$0.dimension() == Level.END && $$1.dimension() == Level.OVERWORLD) {
         for (net.minecraft.world.entity.Entity $$2 : this.getPassengers()) {
            if ($$2 instanceof ServerPlayer $$3 && !$$3.seenCredits) {
               return false;
            }
         }
      }

      return true;
   }

   public float getBlockExplosionResistance(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, FluidState $$4, float $$5) {
      return $$5;
   }

   public boolean shouldBlockExplode(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, float $$4) {
      return true;
   }

   public int getMaxFallDistance() {
      return 3;
   }

   public boolean isIgnoringBlockTriggers() {
      return false;
   }

   public void fillCrashReportCategory(CrashReportCategory $$0) {
      $$0.setDetail("Entity Type", () -> net.minecraft.world.entity.EntityType.getKey(this.getType()) + " (" + this.getClass().getCanonicalName() + ")");
      $$0.setDetail("Entity ID", this.id);
      $$0.setDetail("Entity Name", () -> this.getPlainTextName());
      $$0.setDetail("Entity's Exact location", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", this.getX(), this.getY(), this.getZ()));
      $$0.setDetail(
         "Entity's Block location", CrashReportCategory.formatLocation(this.level(), Mth.floor(this.getX()), Mth.floor(this.getY()), Mth.floor(this.getZ()))
      );
      Vec3 $$1 = this.getDeltaMovement();
      $$0.setDetail("Entity's Momentum", String.format(Locale.ROOT, "%.2f, %.2f, %.2f", $$1.x, $$1.y, $$1.z));
      $$0.setDetail("Entity's Passengers", () -> this.getPassengers().toString());
      $$0.setDetail("Entity's Vehicle", () -> String.valueOf(this.getVehicle()));
   }

   public boolean displayFireAnimation() {
      return this.isOnFire() && !this.isSpectator();
   }

   public void setUUID(UUID $$0) {
      this.uuid = $$0;
      this.stringUUID = this.uuid.toString();
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public String getStringUUID() {
      return this.stringUUID;
   }

   public String getScoreboardName() {
      return this.stringUUID;
   }

   public boolean isPushedByFluid() {
      return true;
   }

   public static double getViewScale() {
      return viewScale;
   }

   public static void setViewScale(double $$0) {
      viewScale = $$0;
   }

   public Component getDisplayName() {
      return PlayerTeam.formatNameForTeam(this.getTeam(), this.getName())
         .withStyle($$0 -> $$0.withHoverEvent(this.createHoverEvent()).withInsertion(this.getStringUUID()));
   }

   public void setCustomName(@Nullable Component $$0) {
      this.entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable($$0));
   }

   @Nullable
   public Component getCustomName() {
      return (Component)((Optional)this.entityData.get(DATA_CUSTOM_NAME)).orElse(null);
   }

   public boolean hasCustomName() {
      return ((Optional)this.entityData.get(DATA_CUSTOM_NAME)).isPresent();
   }

   public void setCustomNameVisible(boolean $$0) {
      this.entityData.set(DATA_CUSTOM_NAME_VISIBLE, $$0);
   }

   public boolean isCustomNameVisible() {
      return (Boolean)this.entityData.get(DATA_CUSTOM_NAME_VISIBLE);
   }

   public boolean teleportTo(
      ServerLevel $$0, double $$1, double $$2, double $$3, Set<net.minecraft.world.entity.Relative> $$4, float $$5, float $$6, boolean $$7
   ) {
      net.minecraft.world.entity.Entity $$8 = this.teleport(
         new TeleportTransition($$0, new Vec3($$1, $$2, $$3), Vec3.ZERO, $$5, $$6, $$4, TeleportTransition.DO_NOTHING)
      );
      return $$8 != null;
   }

   public void dismountTo(double $$0, double $$1, double $$2) {
      this.teleportTo($$0, $$1, $$2);
   }

   public void teleportTo(double $$0, double $$1, double $$2) {
      if (this.level() instanceof ServerLevel) {
         this.snapTo($$0, $$1, $$2, this.getYRot(), this.getXRot());
         this.teleportPassengers();
      }
   }

   private void teleportPassengers() {
      this.getSelfAndPassengers().forEach($$0 -> {
         UnmodifiableIterator var1 = $$0.passengers.iterator();

         while (var1.hasNext()) {
            net.minecraft.world.entity.Entity $$1 = (net.minecraft.world.entity.Entity)var1.next();
            $$0.positionRider($$1, net.minecraft.world.entity.Entity::snapTo);
         }
      });
   }

   public void teleportRelative(double $$0, double $$1, double $$2) {
      this.teleportTo(this.getX() + $$0, this.getY() + $$1, this.getZ() + $$2);
   }

   public boolean shouldShowName() {
      return this.isCustomNameVisible();
   }

   public void onSyncedDataUpdated(List<DataValue<?>> $$0) {
   }

   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_POSE.equals($$0)) {
         this.refreshDimensions();
      }
   }

   @Deprecated
   protected void fixupDimensions() {
      net.minecraft.world.entity.Pose $$0 = this.getPose();
      net.minecraft.world.entity.EntityDimensions $$1 = this.getDimensions($$0);
      this.dimensions = $$1;
      this.eyeHeight = $$1.eyeHeight();
   }

   public void refreshDimensions() {
      net.minecraft.world.entity.EntityDimensions $$0 = this.dimensions;
      net.minecraft.world.entity.Pose $$1 = this.getPose();
      net.minecraft.world.entity.EntityDimensions $$2 = this.getDimensions($$1);
      this.dimensions = $$2;
      this.eyeHeight = $$2.eyeHeight();
      this.reapplyPosition();
      boolean $$3 = $$2.width() <= 4.0F && $$2.height() <= 4.0F;
      if (!this.level.isClientSide()
         && !this.firstTick
         && !this.noPhysics
         && $$3
         && ($$2.width() > $$0.width() || $$2.height() > $$0.height())
         && !(this instanceof Player)) {
         this.fudgePositionAfterSizeChange($$0);
      }
   }

   public boolean fudgePositionAfterSizeChange(net.minecraft.world.entity.EntityDimensions $$0) {
      net.minecraft.world.entity.EntityDimensions $$1 = this.getDimensions(this.getPose());
      Vec3 $$2 = this.position().add(0.0, $$0.height() / 2.0, 0.0);
      double $$3 = Math.max(0.0F, $$1.width() - $$0.width()) + 1.0E-6;
      double $$4 = Math.max(0.0F, $$1.height() - $$0.height()) + 1.0E-6;
      VoxelShape $$5 = Shapes.create(AABB.ofSize($$2, $$3, $$4, $$3));
      Optional<Vec3> $$6 = this.level.findFreePosition(this, $$5, $$2, $$1.width(), $$1.height(), $$1.width());
      if ($$6.isPresent()) {
         this.setPos($$6.get().add(0.0, -$$1.height() / 2.0, 0.0));
         return true;
      } else {
         if ($$1.width() > $$0.width() && $$1.height() > $$0.height()) {
            VoxelShape $$7 = Shapes.create(AABB.ofSize($$2, $$3, 1.0E-6, $$3));
            Optional<Vec3> $$8 = this.level.findFreePosition(this, $$7, $$2, $$1.width(), $$0.height(), $$1.width());
            if ($$8.isPresent()) {
               this.setPos($$8.get().add(0.0, -$$0.height() / 2.0 + 1.0E-6, 0.0));
               return true;
            }
         }

         return false;
      }
   }

   public Direction getDirection() {
      return Direction.fromYRot(this.getYRot());
   }

   public Direction getMotionDirection() {
      return this.getDirection();
   }

   protected HoverEvent createHoverEvent() {
      return new ShowEntity(new EntityTooltipInfo(this.getType(), this.getUUID(), this.getName()));
   }

   public boolean broadcastToPlayer(ServerPlayer $$0) {
      return true;
   }

   public final AABB getBoundingBox() {
      return this.bb;
   }

   public final void setBoundingBox(AABB $$0) {
      this.bb = $$0;
   }

   public final float getEyeHeight(net.minecraft.world.entity.Pose $$0) {
      return this.getDimensions($$0).eyeHeight();
   }

   public final float getEyeHeight() {
      return this.eyeHeight;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SlotAccess getSlot(int $$0) {
      return null;
   }

   public InteractionResult interactAt(Player $$0, Vec3 $$1, InteractionHand $$2) {
      return InteractionResult.PASS;
   }

   public boolean ignoreExplosion(Explosion $$0) {
      return false;
   }

   public void startSeenByPlayer(ServerPlayer $$0) {
   }

   public void stopSeenByPlayer(ServerPlayer $$0) {
   }

   public float rotate(Rotation $$0) {
      float $$1 = Mth.wrapDegrees(this.getYRot());

      return switch ($$0) {
         case CLOCKWISE_180 -> $$1 + 180.0F;
         case COUNTERCLOCKWISE_90 -> $$1 + 270.0F;
         case CLOCKWISE_90 -> $$1 + 90.0F;
         default -> $$1;
      };
   }

   public float mirror(Mirror $$0) {
      float $$1 = Mth.wrapDegrees(this.getYRot());

      return switch ($$0) {
         case FRONT_BACK -> -$$1;
         case LEFT_RIGHT -> 180.0F - $$1;
         default -> $$1;
      };
   }

   public ProjectileDeflection deflection(Projectile $$0) {
      return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? ProjectileDeflection.REVERSE : ProjectileDeflection.NONE;
   }

   @Nullable
   public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
      return null;
   }

   public final boolean hasControllingPassenger() {
      return this.getControllingPassenger() != null;
   }

   public final List<net.minecraft.world.entity.Entity> getPassengers() {
      return this.passengers;
   }

   @Nullable
   public net.minecraft.world.entity.Entity getFirstPassenger() {
      return this.passengers.isEmpty() ? null : (net.minecraft.world.entity.Entity)this.passengers.get(0);
   }

   public boolean hasPassenger(net.minecraft.world.entity.Entity $$0) {
      return this.passengers.contains($$0);
   }

   public boolean hasPassenger(Predicate<net.minecraft.world.entity.Entity> $$0) {
      UnmodifiableIterator var2 = this.passengers.iterator();

      while (var2.hasNext()) {
         net.minecraft.world.entity.Entity $$1 = (net.minecraft.world.entity.Entity)var2.next();
         if ($$0.test($$1)) {
            return true;
         }
      }

      return false;
   }

   private Stream<net.minecraft.world.entity.Entity> getIndirectPassengersStream() {
      return this.passengers.stream().flatMap(net.minecraft.world.entity.Entity::getSelfAndPassengers);
   }

   public Stream<net.minecraft.world.entity.Entity> getSelfAndPassengers() {
      return Stream.concat(Stream.of(this), this.getIndirectPassengersStream());
   }

   public Stream<net.minecraft.world.entity.Entity> getPassengersAndSelf() {
      return Stream.concat(this.passengers.stream().flatMap(net.minecraft.world.entity.Entity::getPassengersAndSelf), Stream.of(this));
   }

   public Iterable<net.minecraft.world.entity.Entity> getIndirectPassengers() {
      return () -> this.getIndirectPassengersStream().iterator();
   }

   public int countPlayerPassengers() {
      return (int)this.getIndirectPassengersStream().filter($$0 -> $$0 instanceof Player).count();
   }

   public boolean hasExactlyOnePlayerPassenger() {
      return this.countPlayerPassengers() == 1;
   }

   public net.minecraft.world.entity.Entity getRootVehicle() {
      net.minecraft.world.entity.Entity $$0 = this;

      while ($$0.isPassenger()) {
         $$0 = $$0.getVehicle();
      }

      return $$0;
   }

   public boolean isPassengerOfSameVehicle(net.minecraft.world.entity.Entity $$0) {
      return this.getRootVehicle() == $$0.getRootVehicle();
   }

   public boolean hasIndirectPassenger(net.minecraft.world.entity.Entity $$0) {
      if (!$$0.isPassenger()) {
         return false;
      } else {
         net.minecraft.world.entity.Entity $$1 = $$0.getVehicle();
         return $$1 == this ? true : this.hasIndirectPassenger($$1);
      }
   }

   public final boolean isLocalInstanceAuthoritative() {
      return this.level.isClientSide() ? this.isLocalClientAuthoritative() : !this.isClientAuthoritative();
   }

   protected boolean isLocalClientAuthoritative() {
      net.minecraft.world.entity.LivingEntity $$0 = this.getControllingPassenger();
      return $$0 != null && $$0.isLocalClientAuthoritative();
   }

   public boolean isClientAuthoritative() {
      net.minecraft.world.entity.LivingEntity $$0 = this.getControllingPassenger();
      return $$0 != null && $$0.isClientAuthoritative();
   }

   public boolean canSimulateMovement() {
      return this.isLocalInstanceAuthoritative();
   }

   public boolean isEffectiveAi() {
      return this.isLocalInstanceAuthoritative();
   }

   protected static Vec3 getCollisionHorizontalEscapeVector(double $$0, double $$1, float $$2) {
      double $$3 = ($$0 + $$1 + 1.0E-5F) / 2.0;
      float $$4 = -Mth.sin($$2 * (float) (Math.PI / 180.0));
      float $$5 = Mth.cos($$2 * (float) (Math.PI / 180.0));
      float $$6 = Math.max(Math.abs($$4), Math.abs($$5));
      return new Vec3($$4 * $$3 / $$6, 0.0, $$5 * $$3 / $$6);
   }

   public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity $$0) {
      return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
   }

   @Nullable
   public net.minecraft.world.entity.Entity getVehicle() {
      return this.vehicle;
   }

   @Nullable
   public net.minecraft.world.entity.Entity getControlledVehicle() {
      return this.vehicle != null && this.vehicle.getControllingPassenger() == this ? this.vehicle : null;
   }

   public PushReaction getPistonPushReaction() {
      return PushReaction.NORMAL;
   }

   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   protected int getFireImmuneTicks() {
      return 0;
   }

   public CommandSourceStack createCommandSourceStackForNameResolution(ServerLevel $$0) {
      return new CommandSourceStack(
         CommandSource.NULL,
         this.position(),
         this.getRotationVector(),
         $$0,
         PermissionSet.NO_PERMISSIONS,
         this.getPlainTextName(),
         this.getDisplayName(),
         $$0.getServer(),
         this
      );
   }

   public void lookAt(Anchor $$0, Vec3 $$1) {
      Vec3 $$2 = $$0.apply(this);
      double $$3 = $$1.x - $$2.x;
      double $$4 = $$1.y - $$2.y;
      double $$5 = $$1.z - $$2.z;
      double $$6 = Math.sqrt($$3 * $$3 + $$5 * $$5);
      this.setXRot(Mth.wrapDegrees((float)(-(Mth.atan2($$4, $$6) * 180.0F / (float)Math.PI))));
      this.setYRot(Mth.wrapDegrees((float)(Mth.atan2($$5, $$3) * 180.0F / (float)Math.PI) - 90.0F));
      this.setYHeadRot(this.getYRot());
      this.xRotO = this.getXRot();
      this.yRotO = this.getYRot();
   }

   public float getPreciseBodyRotation(float $$0) {
      return Mth.lerp($$0, this.yRotO, this.yRot);
   }

   public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> $$0, double $$1) {
      if (this.touchingUnloadedChunk()) {
         return false;
      } else {
         AABB $$2 = this.getBoundingBox().deflate(0.001);
         int $$3 = Mth.floor($$2.minX);
         int $$4 = Mth.ceil($$2.maxX);
         int $$5 = Mth.floor($$2.minY);
         int $$6 = Mth.ceil($$2.maxY);
         int $$7 = Mth.floor($$2.minZ);
         int $$8 = Mth.ceil($$2.maxZ);
         double $$9 = 0.0;
         boolean $$10 = this.isPushedByFluid();
         boolean $$11 = false;
         Vec3 $$12 = Vec3.ZERO;
         int $$13 = 0;
         MutableBlockPos $$14 = new MutableBlockPos();

         for (int $$15 = $$3; $$15 < $$4; $$15++) {
            for (int $$16 = $$5; $$16 < $$6; $$16++) {
               for (int $$17 = $$7; $$17 < $$8; $$17++) {
                  $$14.set($$15, $$16, $$17);
                  FluidState $$18 = this.level().getFluidState($$14);
                  if ($$18.is($$0)) {
                     double $$19 = $$16 + $$18.getHeight(this.level(), $$14);
                     if ($$19 >= $$2.minY) {
                        $$11 = true;
                        $$9 = Math.max($$19 - $$2.minY, $$9);
                        if ($$10) {
                           Vec3 $$20 = $$18.getFlow(this.level(), $$14);
                           if ($$9 < 0.4) {
                              $$20 = $$20.scale($$9);
                           }

                           $$12 = $$12.add($$20);
                           $$13++;
                        }
                     }
                  }
               }
            }
         }

         if ($$12.length() > 0.0) {
            if ($$13 > 0) {
               $$12 = $$12.scale(1.0 / $$13);
            }

            if (!(this instanceof Player)) {
               $$12 = $$12.normalize();
            }

            Vec3 $$21 = this.getDeltaMovement();
            $$12 = $$12.scale($$1);
            double $$22 = 0.003;
            if (Math.abs($$21.x) < 0.003 && Math.abs($$21.z) < 0.003 && $$12.length() < 0.0045000000000000005) {
               $$12 = $$12.normalize().scale(0.0045000000000000005);
            }

            this.setDeltaMovement(this.getDeltaMovement().add($$12));
         }

         this.fluidHeight.put($$0, $$9);
         return $$11;
      }
   }

   public boolean touchingUnloadedChunk() {
      AABB $$0 = this.getBoundingBox().inflate(1.0);
      int $$1 = Mth.floor($$0.minX);
      int $$2 = Mth.ceil($$0.maxX);
      int $$3 = Mth.floor($$0.minZ);
      int $$4 = Mth.ceil($$0.maxZ);
      return !this.level().hasChunksAt($$1, $$3, $$2, $$4);
   }

   public double getFluidHeight(TagKey<Fluid> $$0) {
      return this.fluidHeight.getDouble($$0);
   }

   public double getFluidJumpThreshold() {
      return this.getEyeHeight() < 0.4 ? 0.0 : 0.4;
   }

   public final float getBbWidth() {
      return this.dimensions.width();
   }

   public final float getBbHeight() {
      return this.dimensions.height();
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      return new ClientboundAddEntityPacket(this, $$0);
   }

   public net.minecraft.world.entity.EntityDimensions getDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.type.getDimensions();
   }

   public final net.minecraft.world.entity.EntityAttachments getAttachments() {
      return this.dimensions.attachments();
   }

   @Override
   public Vec3 position() {
      return this.position;
   }

   public Vec3 trackingPosition() {
      return this.position();
   }

   public BlockPos blockPosition() {
      return this.blockPosition;
   }

   public BlockState getInBlockState() {
      if (this.inBlockState == null) {
         this.inBlockState = this.level().getBlockState(this.blockPosition());
      }

      return this.inBlockState;
   }

   public ChunkPos chunkPosition() {
      return this.chunkPosition;
   }

   public Vec3 getDeltaMovement() {
      return this.deltaMovement;
   }

   public void setDeltaMovement(Vec3 $$0) {
      if ($$0.isFinite()) {
         this.deltaMovement = $$0;
      }
   }

   public void addDeltaMovement(Vec3 $$0) {
      if ($$0.isFinite()) {
         this.setDeltaMovement(this.getDeltaMovement().add($$0));
      }
   }

   public void setDeltaMovement(double $$0, double $$1, double $$2) {
      this.setDeltaMovement(new Vec3($$0, $$1, $$2));
   }

   public final int getBlockX() {
      return this.blockPosition.getX();
   }

   public final double getX() {
      return this.position.x;
   }

   public double getX(double $$0) {
      return this.position.x + this.getBbWidth() * $$0;
   }

   public double getRandomX(double $$0) {
      return this.getX((2.0 * this.random.nextDouble() - 1.0) * $$0);
   }

   public final int getBlockY() {
      return this.blockPosition.getY();
   }

   public final double getY() {
      return this.position.y;
   }

   public double getY(double $$0) {
      return this.position.y + this.getBbHeight() * $$0;
   }

   public double getRandomY() {
      return this.getY(this.random.nextDouble());
   }

   public double getEyeY() {
      return this.position.y + this.eyeHeight;
   }

   public final int getBlockZ() {
      return this.blockPosition.getZ();
   }

   public final double getZ() {
      return this.position.z;
   }

   public double getZ(double $$0) {
      return this.position.z + this.getBbWidth() * $$0;
   }

   public double getRandomZ(double $$0) {
      return this.getZ((2.0 * this.random.nextDouble() - 1.0) * $$0);
   }

   public final void setPosRaw(double $$0, double $$1, double $$2) {
      if (this.position.x != $$0 || this.position.y != $$1 || this.position.z != $$2) {
         this.position = new Vec3($$0, $$1, $$2);
         int $$3 = Mth.floor($$0);
         int $$4 = Mth.floor($$1);
         int $$5 = Mth.floor($$2);
         if ($$3 != this.blockPosition.getX() || $$4 != this.blockPosition.getY() || $$5 != this.blockPosition.getZ()) {
            this.blockPosition = new BlockPos($$3, $$4, $$5);
            this.inBlockState = null;
            if (SectionPos.blockToSectionCoord($$3) != this.chunkPosition.x || SectionPos.blockToSectionCoord($$5) != this.chunkPosition.z) {
               this.chunkPosition = new ChunkPos(this.blockPosition);
            }
         }

         this.levelCallback.onMove();
         if (!this.firstTick && this.level instanceof ServerLevel $$6 && !this.isRemoved()) {
            if (this instanceof WaypointTransmitter $$7 && $$7.isTransmittingWaypoint()) {
               $$6.getWaypointManager().updateWaypoint($$7);
            }

            if (this instanceof ServerPlayer $$8 && $$8.isReceivingWaypoints() && $$8.connection != null) {
               $$6.getWaypointManager().updatePlayer($$8);
            }
         }
      }
   }

   public void checkDespawn() {
   }

   public Vec3[] getQuadLeashHolderOffsets() {
      return net.minecraft.world.entity.Leashable.createQuadLeashOffsets(this, 0.0, 0.5, 0.5, 0.0);
   }

   public boolean supportQuadLeashAsHolder() {
      return false;
   }

   public void notifyLeashHolder(net.minecraft.world.entity.Leashable $$0) {
   }

   public void notifyLeasheeRemoved(net.minecraft.world.entity.Leashable $$0) {
   }

   public Vec3 getRopeHoldPosition(float $$0) {
      return this.getPosition($$0).add(0.0, this.eyeHeight * 0.7, 0.0);
   }

   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      int $$1 = $$0.getId();
      double $$2 = $$0.getX();
      double $$3 = $$0.getY();
      double $$4 = $$0.getZ();
      this.syncPacketPositionCodec($$2, $$3, $$4);
      this.snapTo($$2, $$3, $$4, $$0.getYRot(), $$0.getXRot());
      this.setId($$1);
      this.setUUID($$0.getUUID());
      this.setDeltaMovement($$0.getMovement());
   }

   @Nullable
   public ItemStack getPickResult() {
      return null;
   }

   public void setIsInPowderSnow(boolean $$0) {
      this.isInPowderSnow = $$0;
   }

   public boolean canFreeze() {
      return !this.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES);
   }

   public boolean isFreezing() {
      return this.getTicksFrozen() > 0;
   }

   public float getYRot() {
      return this.yRot;
   }

   @Override
   public float getVisualRotationYInDegrees() {
      return this.getYRot();
   }

   public void setYRot(float $$0) {
      if (!Float.isFinite($$0)) {
         Util.logAndPauseIfInIde("Invalid entity rotation: " + $$0 + ", discarding.");
      } else {
         this.yRot = $$0;
      }
   }

   public float getXRot() {
      return this.xRot;
   }

   public void setXRot(float $$0) {
      if (!Float.isFinite($$0)) {
         Util.logAndPauseIfInIde("Invalid entity rotation: " + $$0 + ", discarding.");
      } else {
         this.xRot = Math.clamp($$0 % 360.0F, -90.0F, 90.0F);
      }
   }

   public boolean canSprint() {
      return false;
   }

   public float maxUpStep() {
      return 0.0F;
   }

   public void onExplosionHit(@Nullable net.minecraft.world.entity.Entity $$0) {
   }

   public final boolean isRemoved() {
      return this.removalReason != null;
   }

   @Nullable
   public net.minecraft.world.entity.Entity.RemovalReason getRemovalReason() {
      return this.removalReason;
   }

   public final void setRemoved(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      if (this.removalReason == null) {
         this.removalReason = $$0;
      }

      if (this.removalReason.shouldDestroy()) {
         this.stopRiding();
      }

      this.getPassengers().forEach(net.minecraft.world.entity.Entity::stopRiding);
      this.levelCallback.onRemove($$0);
      this.onRemoval($$0);
   }

   protected void unsetRemoved() {
      this.removalReason = null;
   }

   public void setLevelCallback(EntityInLevelCallback $$0) {
      this.levelCallback = $$0;
   }

   public boolean shouldBeSaved() {
      if (this.removalReason != null && !this.removalReason.shouldSave()) {
         return false;
      } else {
         return this.isPassenger() ? false : !this.isVehicle() || !this.hasExactlyOnePlayerPassenger();
      }
   }

   public boolean isAlwaysTicking() {
      return false;
   }

   public boolean mayInteract(ServerLevel $$0, BlockPos $$1) {
      return true;
   }

   public boolean isFlyingVehicle() {
      return false;
   }

   @Override
   public Level level() {
      return this.level;
   }

   protected void setLevel(Level $$0) {
      this.level = $$0;
   }

   public DamageSources damageSources() {
      return this.level().damageSources();
   }

   public RegistryAccess registryAccess() {
      return this.level().registryAccess();
   }

   protected void lerpPositionAndRotationStep(int $$0, double $$1, double $$2, double $$3, double $$4, double $$5) {
      double $$6 = 1.0 / $$0;
      double $$7 = Mth.lerp($$6, this.getX(), $$1);
      double $$8 = Mth.lerp($$6, this.getY(), $$2);
      double $$9 = Mth.lerp($$6, this.getZ(), $$3);
      float $$10 = (float)Mth.rotLerp($$6, this.getYRot(), $$4);
      float $$11 = (float)Mth.lerp($$6, this.getXRot(), $$5);
      this.setPos($$7, $$8, $$9);
      this.setRot($$10, $$11);
   }

   public RandomSource getRandom() {
      return this.random;
   }

   public Vec3 getKnownMovement() {
      return this.getControllingPassenger() instanceof Player $$0 && this.isAlive() ? $$0.getKnownMovement() : this.getDeltaMovement();
   }

   public Vec3 getKnownSpeed() {
      return this.getControllingPassenger() instanceof Player $$0 && this.isAlive() ? $$0.getKnownSpeed() : this.lastKnownSpeed;
   }

   @Nullable
   public ItemStack getWeaponItem() {
      return null;
   }

   public Optional<ResourceKey<LootTable>> getLootTable() {
      return this.type.getDefaultLootTable();
   }

   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.CUSTOM_NAME);
      this.applyImplicitComponentIfPresent($$0, DataComponents.CUSTOM_DATA);
   }

   public final void applyComponentsFromItemStack(ItemStack $$0) {
      this.applyImplicitComponents($$0.getComponents());
   }

   @Nullable
   public <T> T get(DataComponentType<? extends T> $$0) {
      if ($$0 == DataComponents.CUSTOM_NAME) {
         return castComponentValue((DataComponentType<T>)$$0, this.getCustomName());
      } else {
         return $$0 == DataComponents.CUSTOM_DATA ? castComponentValue((DataComponentType<T>)$$0, this.customData) : null;
      }
   }

   @Contract("_,!null->!null;_,_->_")
   @Nullable
   protected static <T> T castComponentValue(DataComponentType<T> $$0, @Nullable Object $$1) {
      return (T)$$1;
   }

   public <T> void setComponent(DataComponentType<T> $$0, T $$1) {
      this.applyImplicitComponent($$0, $$1);
   }

   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.CUSTOM_NAME) {
         this.setCustomName(castComponentValue(DataComponents.CUSTOM_NAME, $$1));
         return true;
      } else if ($$0 == DataComponents.CUSTOM_DATA) {
         this.customData = castComponentValue(DataComponents.CUSTOM_DATA, $$1);
         return true;
      } else {
         return false;
      }
   }

   protected <T> boolean applyImplicitComponentIfPresent(DataComponentGetter $$0, DataComponentType<T> $$1) {
      T $$2 = (T)$$0.get($$1);
      return $$2 != null ? this.applyImplicitComponent($$1, $$2) : false;
   }

   public PathElement problemPath() {
      return new net.minecraft.world.entity.Entity.EntityPathElement(this);
   }

   public void registerDebugValues(ServerLevel $$0, Registration $$1) {
   }

   record EntityPathElement(net.minecraft.world.entity.Entity entity) implements PathElement {
      public String get() {
         return this.entity.toString();
      }
   }

   @FunctionalInterface
   public interface MoveFunction {
      void accept(net.minecraft.world.entity.Entity var1, double var2, double var4, double var6);
   }

   record Movement(Vec3 from, Vec3 to, Optional<Vec3> axisDependentOriginalMovement) {

      public Movement(Vec3 $$0, Vec3 $$1, Vec3 $$2) {
         this($$0, $$1, Optional.of($$2));
      }

      public Movement(Vec3 $$0, Vec3 $$1) {
         this($$0, $$1, Optional.empty());
      }
   }

   public static enum MovementEmission {
      NONE(false, false),
      SOUNDS(true, false),
      EVENTS(false, true),
      ALL(true, true);

      final boolean sounds;
      final boolean events;

      private MovementEmission(final boolean $$0, final boolean $$1) {
         this.sounds = $$0;
         this.events = $$1;
      }

      public boolean emitsAnything() {
         return this.events || this.sounds;
      }

      public boolean emitsEvents() {
         return this.events;
      }

      public boolean emitsSounds() {
         return this.sounds;
      }
   }

   public static enum RemovalReason {
      KILLED(true, false),
      DISCARDED(true, false),
      UNLOADED_TO_CHUNK(false, true),
      UNLOADED_WITH_PLAYER(false, false),
      CHANGED_DIMENSION(false, false);

      private final boolean destroy;
      private final boolean save;

      private RemovalReason(final boolean $$0, final boolean $$1) {
         this.destroy = $$0;
         this.save = $$1;
      }

      public boolean shouldDestroy() {
         return this.destroy;
      }

      public boolean shouldSave() {
         return this.save;
      }
   }
}
