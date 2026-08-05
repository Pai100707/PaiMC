/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Objects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.waypoints.ServerWaypointManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Attackable;
import net.minecraft.world.entity.ElytraAnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.effects.EnchantmentLocationBasedEffect;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HoneyBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable,
WaypointTransmitter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_ACTIVE_EFFECTS = "active_effects";
    public static final String TAG_ATTRIBUTES = "attributes";
    public static final String TAG_SLEEPING_POS = "sleeping_pos";
    public static final String TAG_EQUIPMENT = "equipment";
    public static final String TAG_BRAIN = "Brain";
    public static final String TAG_FALL_FLYING = "FallFlying";
    public static final String TAG_HURT_TIME = "HurtTime";
    public static final String TAG_DEATH_TIME = "DeathTime";
    public static final String TAG_HURT_BY_TIMESTAMP = "HurtByTimestamp";
    public static final String TAG_HEALTH = "Health";
    private static final Identifier SPEED_MODIFIER_POWDER_SNOW_ID = Identifier.withDefaultNamespace("powder_snow");
    private static final Identifier SPRINTING_MODIFIER_ID = Identifier.withDefaultNamespace("sprinting");
    private static final AttributeModifier SPEED_MODIFIER_SPRINTING = new AttributeModifier(SPRINTING_MODIFIER_ID, 0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int EQUIPMENT_SLOT_OFFSET = 98;
    public static final int ARMOR_SLOT_OFFSET = 100;
    public static final int BODY_ARMOR_OFFSET = 105;
    public static final int SADDLE_OFFSET = 106;
    public static final int PLAYER_HURT_EXPERIENCE_TIME = 100;
    private static final int DAMAGE_SOURCE_TIMEOUT = 40;
    public static final double MIN_MOVEMENT_DISTANCE = 0.003;
    public static final double DEFAULT_BASE_GRAVITY = 0.08;
    public static final int DEATH_DURATION = 20;
    protected static final float INPUT_FRICTION = 0.98f;
    private static final int TICKS_PER_ELYTRA_FREE_FALL_EVENT = 10;
    private static final int FREE_FALL_EVENTS_PER_ELYTRA_BREAK = 2;
    public static final float BASE_JUMP_POWER = 0.42f;
    protected static final float DEFAULT_KNOCKBACK = 0.4f;
    protected static final int INVULNERABLE_DURATION = 20;
    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 128.0;
    protected static final int LIVING_ENTITY_FLAG_IS_USING = 1;
    protected static final int LIVING_ENTITY_FLAG_OFF_HAND = 2;
    protected static final int LIVING_ENTITY_FLAG_SPIN_ATTACK = 4;
    protected static final EntityDataAccessor<Byte> DATA_LIVING_ENTITY_FLAGS = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> DATA_HEALTH_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<List<ParticleOptions>> DATA_EFFECT_PARTICLES = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.PARTICLES);
    private static final EntityDataAccessor<Boolean> DATA_EFFECT_AMBIENCE_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ARROW_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STINGER_COUNT_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockPos>> SLEEPING_POS_ID = SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int PARTICLE_FREQUENCY_WHEN_INVISIBLE = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float EXTRA_RENDER_CULLING_SIZE_WITH_BIG_HAT = 0.5f;
    public static final float DEFAULT_BABY_SCALE = 0.5f;
    private static final float WATER_FLOAT_IMPULSE = 0.04f;
    public static final Predicate<LivingEntity> PLAYER_NOT_WEARING_DISGUISE_ITEM = $$0 -> {
        void $$2;
        if (!($$0 instanceof Player)) {
            return true;
        }
        Player $$1 = (Player)$$0;
        ItemStack $$3 = $$2.getItemBySlot(EquipmentSlot.HEAD);
        return !$$3.is(ItemTags.GAZE_DISGUISE_EQUIPMENT);
    };
    private static final Dynamic<?> EMPTY_BRAIN = new Dynamic((DynamicOps)JavaOps.INSTANCE, Map.of("memories", Map.of()));
    private final AttributeMap attributes;
    private final CombatTracker combatTracker = new CombatTracker(this);
    private final Map<Holder<MobEffect>, MobEffectInstance> activeEffects = Maps.newHashMap();
    private final Map<EquipmentSlot, ItemStack> lastEquipmentItems = Util.makeEnumMap(EquipmentSlot.class, $$0 -> ItemStack.EMPTY);
    public boolean swinging;
    private boolean discardFriction = false;
    public InteractionHand swingingArm;
    public int swingTime;
    public int removeArrowTime;
    public int removeStingerTime;
    public int hurtTime;
    public int hurtDuration;
    public int deathTime;
    public float oAttackAnim;
    public float attackAnim;
    protected int attackStrengthTicker;
    protected int itemSwapTicker;
    public final WalkAnimationState walkAnimation = new WalkAnimationState();
    public float yBodyRot;
    public float yBodyRotO;
    public float yHeadRot;
    public float yHeadRotO;
    public final ElytraAnimationState elytraAnimationState = new ElytraAnimationState(this);
    protected @Nullable EntityReference<Player> lastHurtByPlayer;
    protected int lastHurtByPlayerMemoryTime;
    protected boolean dead;
    protected int noActionTime;
    protected float lastHurt;
    protected boolean jumping;
    public float xxa;
    public float yya;
    public float zza;
    protected InterpolationHandler interpolation = new InterpolationHandler(this);
    protected double lerpYHeadRot;
    protected int lerpHeadSteps;
    private boolean effectsDirty = true;
    private @Nullable EntityReference<LivingEntity> lastHurtByMob;
    private int lastHurtByMobTimestamp;
    private @Nullable LivingEntity lastHurtMob;
    private int lastHurtMobTimestamp;
    private float speed;
    private int noJumpDelay;
    private float absorptionAmount;
    protected ItemStack useItem = ItemStack.EMPTY;
    protected int useItemRemaining;
    protected int fallFlyTicks;
    private long lastKineticHitFeedbackTime = Integer.MIN_VALUE;
    private BlockPos lastPos;
    private Optional<BlockPos> lastClimbablePos = Optional.empty();
    private @Nullable DamageSource lastDamageSource;
    private long lastDamageStamp;
    protected int autoSpinAttackTicks;
    protected float autoSpinAttackDmg;
    protected @Nullable ItemStack autoSpinAttackItemStack;
    protected @Nullable Object2LongMap<Entity> recentKineticEnemies;
    private float swimAmount;
    private float swimAmountO;
    protected Brain<?> brain;
    private boolean skipDropExperience;
    private final EnumMap<EquipmentSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> activeLocationDependentEnchantments = new EnumMap(EquipmentSlot.class);
    protected final EntityEquipment equipment;
    private Waypoint.Icon locatorBarIcon = new Waypoint.Icon();

    protected LivingEntity(EntityType<? extends LivingEntity> $$02, Level $$1) {
        super($$02, $$1);
        this.attributes = new AttributeMap(DefaultAttributes.getSupplier($$02));
        this.setHealth(this.getMaxHealth());
        this.equipment = this.createEquipment();
        this.blocksBuilding = true;
        this.reapplyPosition();
        this.setYRot(this.random.nextFloat() * ((float)Math.PI * 2));
        this.yHeadRot = this.getYRot();
        this.brain = this.makeBrain(EMPTY_BRAIN);
    }

    @Override
    public @Nullable LivingEntity asLivingEntity() {
        return this;
    }

    @Contract(pure=true)
    protected EntityEquipment createEquipment() {
        return new EntityEquipment();
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Provider<?> brainProvider() {
        return Brain.provider(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return this.brainProvider().makeBrain($$0);
    }

    @Override
    public void kill(ServerLevel $$0) {
        this.hurtServer($$0, this.damageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canAttackType(EntityType<?> $$0) {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        $$0.define(DATA_LIVING_ENTITY_FLAGS, (byte)0);
        $$0.define(DATA_EFFECT_PARTICLES, List.of());
        $$0.define(DATA_EFFECT_AMBIENCE_ID, false);
        $$0.define(DATA_ARROW_COUNT_ID, 0);
        $$0.define(DATA_STINGER_COUNT_ID, 0);
        $$0.define(DATA_HEALTH_ID, Float.valueOf(1.0f));
        $$0.define(SLEEPING_POS_ID, Optional.empty());
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return AttributeSupplier.builder().add(Attributes.MAX_HEALTH).add(Attributes.KNOCKBACK_RESISTANCE).add(Attributes.MOVEMENT_SPEED).add(Attributes.ARMOR).add(Attributes.ARMOR_TOUGHNESS).add(Attributes.MAX_ABSORPTION).add(Attributes.STEP_HEIGHT).add(Attributes.SCALE).add(Attributes.GRAVITY).add(Attributes.SAFE_FALL_DISTANCE).add(Attributes.FALL_DAMAGE_MULTIPLIER).add(Attributes.JUMP_STRENGTH).add(Attributes.OXYGEN_BONUS).add(Attributes.BURNING_TIME).add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(Attributes.WATER_MOVEMENT_EFFICIENCY).add(Attributes.MOVEMENT_EFFICIENCY).add(Attributes.ATTACK_KNOCKBACK).add(Attributes.CAMERA_DISTANCE).add(Attributes.WAYPOINT_TRANSMIT_RANGE);
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
        Level level;
        if (!this.isInWater()) {
            this.updateInWaterStateAndDoWaterCurrentPushing();
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel $$4 = (ServerLevel)level;
            if ($$1 && this.fallDistance > 0.0) {
                this.onChangedBlock($$4, $$3);
                double $$5 = Math.max(0, Mth.floor(this.calculateFallPower(this.fallDistance)));
                if ($$5 > 0.0 && !$$2.isAir()) {
                    double $$6 = this.getX();
                    double $$7 = this.getY();
                    double $$8 = this.getZ();
                    BlockPos $$9 = this.blockPosition();
                    if ($$3.getX() != $$9.getX() || $$3.getZ() != $$9.getZ()) {
                        double $$10 = $$6 - (double)$$3.getX() - 0.5;
                        double $$11 = $$8 - (double)$$3.getZ() - 0.5;
                        double $$12 = Math.max(Math.abs($$10), Math.abs($$11));
                        $$6 = (double)$$3.getX() + 0.5 + $$10 / $$12 * 0.5;
                        $$8 = (double)$$3.getZ() + 0.5 + $$11 / $$12 * 0.5;
                    }
                    double $$13 = Math.min((double)0.2f + $$5 / 15.0, 2.5);
                    int $$14 = (int)(150.0 * $$13);
                    $$4.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, $$2), $$6, $$7, $$8, $$14, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.checkFallDamage($$0, $$1, $$2, $$3);
        if ($$1) {
            this.lastClimbablePos = Optional.empty();
        }
    }

    public boolean canBreatheUnderwater() {
        return this.getType().is(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getSwimAmount(float $$0) {
        return Mth.lerp($$0, this.swimAmountO, this.swimAmount);
    }

    public boolean hasLandedInLiquid() {
        return this.getDeltaMovement().y() < (double)1.0E-5f && this.isInLiquid();
    }

    @Override
    public void baseTick() {
        LivingEntity $$8;
        Level level;
        Level level2;
        this.oAttackAnim = this.attackAnim;
        if (this.firstTick) {
            this.getSleepingPos().ifPresent(this::setPosToBed);
        }
        if ((level2 = this.level()) instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level2;
            EnchantmentHelper.tickEffects($$0, this);
        }
        super.baseTick();
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("livingEntityBaseTick");
        if (this.isAlive() && (level = this.level()) instanceof ServerLevel) {
            double $$5;
            double $$4;
            ServerLevel $$2 = (ServerLevel)level;
            boolean $$3 = this instanceof Player;
            if (this.isInWall()) {
                this.hurtServer($$2, this.damageSources().inWall(), 1.0f);
            } else if ($$3 && !$$2.getWorldBorder().isWithinBounds(this.getBoundingBox()) && ($$4 = $$2.getWorldBorder().getDistanceToBorder(this) + $$2.getWorldBorder().getSafeZone()) < 0.0 && ($$5 = $$2.getWorldBorder().getDamagePerBlock()) > 0.0) {
                this.hurtServer($$2, this.damageSources().outOfBorder(), Math.max(1, Mth.floor(-$$4 * $$5)));
            }
            if (this.isEyeInFluid(FluidTags.WATER) && !$$2.getBlockState(BlockPos.containing(this.getX(), this.getEyeY(), this.getZ())).is(Blocks.BUBBLE_COLUMN)) {
                boolean $$6;
                boolean bl = $$6 = !this.canBreatheUnderwater() && !MobEffectUtil.hasWaterBreathing(this) && (!$$3 || !((Player)this).getAbilities().invulnerable);
                if ($$6) {
                    this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()));
                    if (this.shouldTakeDrowningDamage()) {
                        this.setAirSupply(0);
                        $$2.broadcastEntityEvent(this, (byte)67);
                        this.hurtServer($$2, this.damageSources().drown(), 2.0f);
                    }
                } else if (this.getAirSupply() < this.getMaxAirSupply() && MobEffectUtil.shouldEffectsRefillAirsupply(this)) {
                    this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                }
                if (this.isPassenger() && this.getVehicle() != null && this.getVehicle().dismountsUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAirSupply() < this.getMaxAirSupply()) {
                this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
            }
            BlockPos $$7 = this.blockPosition();
            if (!Objects.equal((Object)this.lastPos, (Object)$$7)) {
                this.lastPos = $$7;
                this.onChangedBlock($$2, $$7);
            }
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.invulnerableTime > 0 && !(this instanceof ServerPlayer)) {
            --this.invulnerableTime;
        }
        if (this.isDeadOrDying() && this.level().shouldTickDeath(this)) {
            this.tickDeath();
        }
        if (this.lastHurtByPlayerMemoryTime > 0) {
            --this.lastHurtByPlayerMemoryTime;
        } else {
            this.lastHurtByPlayer = null;
        }
        if (this.lastHurtMob != null && !this.lastHurtMob.isAlive()) {
            this.lastHurtMob = null;
        }
        if (($$8 = this.getLastHurtByMob()) != null) {
            if (!$$8.isAlive()) {
                this.setLastHurtByMob(null);
            } else if (this.tickCount - this.lastHurtByMobTimestamp > 100) {
                this.setLastHurtByMob(null);
            }
        }
        this.tickEffects();
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        $$1.pop();
    }

    protected boolean shouldTakeDrowningDamage() {
        return this.getAirSupply() <= -20;
    }

    @Override
    protected float getBlockSpeedFactor() {
        return Mth.lerp((float)this.getAttributeValue(Attributes.MOVEMENT_EFFICIENCY), super.getBlockSpeedFactor(), 1.0f);
    }

    public float getLuck() {
        return 0.0f;
    }

    protected void removeFrost() {
        AttributeInstance $$0 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if ($$0 == null) {
            return;
        }
        if ($$0.getModifier(SPEED_MODIFIER_POWDER_SNOW_ID) != null) {
            $$0.removeModifier(SPEED_MODIFIER_POWDER_SNOW_ID);
        }
    }

    protected void tryAddFrost() {
        int $$0;
        if (!this.getBlockStateOnLegacy().isAir() && ($$0 = this.getTicksFrozen()) > 0) {
            AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if ($$1 == null) {
                return;
            }
            float $$2 = -0.05f * this.getPercentFrozen();
            $$1.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_POWDER_SNOW_ID, $$2, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void onChangedBlock(ServerLevel $$0, BlockPos $$1) {
        EnchantmentHelper.runLocationChangedEffects($$0, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getAgeScale() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public final float getScale() {
        AttributeMap $$0 = this.getAttributes();
        if ($$0 == null) {
            return 1.0f;
        }
        return this.sanitizeScale((float)$$0.getValue(Attributes.SCALE));
    }

    protected float sanitizeScale(float $$0) {
        return $$0;
    }

    public boolean isAffectedByFluids() {
        return true;
    }

    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot(ServerLevel $$0) {
        return !this.isBaby() && $$0.getGameRules().get(GameRules.MOB_DROPS) != false;
    }

    protected int decreaseAirSupply(int $$0) {
        double $$3;
        AttributeInstance $$1 = this.getAttribute(Attributes.OXYGEN_BONUS);
        if ($$1 != null) {
            double $$2 = $$1.getValue();
        } else {
            $$3 = 0.0;
        }
        if ($$3 > 0.0 && this.random.nextDouble() >= 1.0 / ($$3 + 1.0)) {
            return $$0;
        }
        return $$0 - 1;
    }

    protected int increaseAirSupply(int $$0) {
        return Math.min($$0 + 4, this.getMaxAirSupply());
    }

    public final int getExperienceReward(ServerLevel $$0, @Nullable Entity $$1) {
        return EnchantmentHelper.processMobExperience($$0, $$1, this, this.getBaseExperienceReward($$0));
    }

    protected int getBaseExperienceReward(ServerLevel $$0) {
        return 0;
    }

    protected boolean isAlwaysExperienceDropper() {
        return false;
    }

    public @Nullable LivingEntity getLastHurtByMob() {
        return EntityReference.getLivingEntity(this.lastHurtByMob, this.level());
    }

    public @Nullable Player getLastHurtByPlayer() {
        return EntityReference.getPlayer(this.lastHurtByPlayer, this.level());
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getLastHurtByMob();
    }

    public int getLastHurtByMobTimestamp() {
        return this.lastHurtByMobTimestamp;
    }

    public void setLastHurtByPlayer(Player $$0, int $$1) {
        this.setLastHurtByPlayer(EntityReference.of($$0), $$1);
    }

    public void setLastHurtByPlayer(UUID $$0, int $$1) {
        this.setLastHurtByPlayer(EntityReference.of($$0), $$1);
    }

    private void setLastHurtByPlayer(EntityReference<Player> $$0, int $$1) {
        this.lastHurtByPlayer = $$0;
        this.lastHurtByPlayerMemoryTime = $$1;
    }

    public void setLastHurtByMob(@Nullable LivingEntity $$0) {
        this.lastHurtByMob = EntityReference.of($$0);
        this.lastHurtByMobTimestamp = this.tickCount;
    }

    public @Nullable LivingEntity getLastHurtMob() {
        return this.lastHurtMob;
    }

    public int getLastHurtMobTimestamp() {
        return this.lastHurtMobTimestamp;
    }

    public void setLastHurtMob(Entity $$0) {
        this.lastHurtMob = $$0 instanceof LivingEntity ? (LivingEntity)$$0 : null;
        this.lastHurtMobTimestamp = this.tickCount;
    }

    public int getNoActionTime() {
        return this.noActionTime;
    }

    public void setNoActionTime(int $$0) {
        this.noActionTime = $$0;
    }

    public boolean shouldDiscardFriction() {
        return this.discardFriction;
    }

    public void setDiscardFriction(boolean $$0) {
        this.discardFriction = $$0;
    }

    protected boolean doesEmitEquipEvent(EquipmentSlot $$0) {
        return true;
    }

    public void onEquipItem(EquipmentSlot $$0, ItemStack $$1, ItemStack $$2) {
        if (this.level().isClientSide() || this.isSpectator()) {
            return;
        }
        if (ItemStack.isSameItemSameComponents($$1, $$2) || this.firstTick) {
            return;
        }
        Equippable $$3 = $$2.get(DataComponents.EQUIPPABLE);
        if (!this.isSilent() && $$3 != null && $$0 == $$3.slot()) {
            this.level().playSeededSound(null, this.getX(), this.getY(), this.getZ(), this.getEquipSound($$0, $$2, $$3), this.getSoundSource(), 1.0f, 1.0f, this.random.nextLong());
        }
        if (this.doesEmitEquipEvent($$0)) {
            this.gameEvent($$3 != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
        }
    }

    protected Holder<SoundEvent> getEquipSound(EquipmentSlot $$0, ItemStack $$1, Equippable $$2) {
        return $$2.equipSound();
    }

    @Override
    public void remove(Entity.RemovalReason $$0) {
        Level level;
        if (($$0 == Entity.RemovalReason.KILLED || $$0 == Entity.RemovalReason.DISCARDED) && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            this.triggerOnDeathMobEffects($$1, $$0);
        }
        super.remove($$0);
        this.brain.clearMemories();
    }

    @Override
    public void onRemoval(Entity.RemovalReason $$0) {
        super.onRemoval($$0);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            $$1.getWaypointManager().untrackWaypoint(this);
        }
    }

    protected void triggerOnDeathMobEffects(ServerLevel $$0, Entity.RemovalReason $$1) {
        for (MobEffectInstance $$2 : this.getActiveEffects()) {
            $$2.onMobRemoved($$0, this, $$1);
        }
        this.activeEffects.clear();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$02) {
        $$02.putFloat(TAG_HEALTH, this.getHealth());
        $$02.putShort(TAG_HURT_TIME, (short)this.hurtTime);
        $$02.putInt(TAG_HURT_BY_TIMESTAMP, this.lastHurtByMobTimestamp);
        $$02.putShort(TAG_DEATH_TIME, (short)this.deathTime);
        $$02.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        $$02.store(TAG_ATTRIBUTES, AttributeInstance.Packed.LIST_CODEC, this.getAttributes().pack());
        if (!this.activeEffects.isEmpty()) {
            $$02.store(TAG_ACTIVE_EFFECTS, MobEffectInstance.CODEC.listOf(), List.copyOf(this.activeEffects.values()));
        }
        $$02.putBoolean(TAG_FALL_FLYING, this.isFallFlying());
        this.getSleepingPos().ifPresent($$1 -> $$02.store(TAG_SLEEPING_POS, BlockPos.CODEC, $$1));
        DataResult $$12 = this.brain.serializeStart(NbtOps.INSTANCE).map($$0 -> new Dynamic((DynamicOps)NbtOps.INSTANCE, $$0));
        $$12.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent($$1 -> $$02.store(TAG_BRAIN, Codec.PASSTHROUGH, $$1));
        if (this.lastHurtByPlayer != null) {
            this.lastHurtByPlayer.store($$02, "last_hurt_by_player");
            $$02.putInt("last_hurt_by_player_memory_time", this.lastHurtByPlayerMemoryTime);
        }
        if (this.lastHurtByMob != null) {
            this.lastHurtByMob.store($$02, "last_hurt_by_mob");
            $$02.putInt("ticks_since_last_hurt_by_mob", this.tickCount - this.lastHurtByMobTimestamp);
        }
        if (!this.equipment.isEmpty()) {
            $$02.store(TAG_EQUIPMENT, EntityEquipment.CODEC, this.equipment);
        }
        if (this.locatorBarIcon.hasData()) {
            $$02.store("locator_bar_icon", Waypoint.Icon.CODEC, this.locatorBarIcon);
        }
    }

    public @Nullable ItemEntity drop(ItemStack $$0, boolean $$1, boolean $$2) {
        if ($$0.isEmpty()) {
            return null;
        }
        if (this.level().isClientSide()) {
            this.swing(InteractionHand.MAIN_HAND);
            return null;
        }
        ItemEntity $$3 = this.createItemStackToDrop($$0, $$1, $$2);
        if ($$3 != null) {
            this.level().addFreshEntity($$3);
        }
        return $$3;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$02) {
        this.internalSetAbsorptionAmount($$02.getFloatOr("AbsorptionAmount", 0.0f));
        if (this.level() != null && !this.level().isClientSide()) {
            $$02.read(TAG_ATTRIBUTES, AttributeInstance.Packed.LIST_CODEC).ifPresent(this.getAttributes()::apply);
        }
        List $$1 = $$02.read(TAG_ACTIVE_EFFECTS, MobEffectInstance.CODEC.listOf()).orElse(List.of());
        this.activeEffects.clear();
        for (MobEffectInstance $$2 : $$1) {
            this.activeEffects.put($$2.getEffect(), $$2);
            this.effectsDirty = true;
        }
        this.setHealth($$02.getFloatOr(TAG_HEALTH, this.getMaxHealth()));
        this.hurtTime = $$02.getShortOr(TAG_HURT_TIME, (short)0);
        this.deathTime = $$02.getShortOr(TAG_DEATH_TIME, (short)0);
        this.lastHurtByMobTimestamp = $$02.getIntOr(TAG_HURT_BY_TIMESTAMP, 0);
        $$02.getString("Team").ifPresent($$0 -> {
            boolean $$3;
            Scoreboard $$1 = this.level().getScoreboard();
            PlayerTeam $$2 = $$1.getPlayerTeam((String)$$0);
            boolean bl = $$3 = $$2 != null && $$1.addPlayerToTeam(this.getStringUUID(), $$2);
            if (!$$3) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", $$0);
            }
        });
        this.setSharedFlag(7, $$02.getBooleanOr(TAG_FALL_FLYING, false));
        $$02.read(TAG_SLEEPING_POS, BlockPos.CODEC).ifPresentOrElse($$0 -> {
            this.setSleepingPos((BlockPos)$$0);
            this.entityData.set(DATA_POSE, Pose.SLEEPING);
            if (!this.firstTick) {
                this.setPosToBed((BlockPos)$$0);
            }
        }, this::clearSleepingPos);
        $$02.read(TAG_BRAIN, Codec.PASSTHROUGH).ifPresent($$0 -> {
            this.brain = this.makeBrain((Dynamic<?>)$$0);
        });
        this.lastHurtByPlayer = EntityReference.read($$02, "last_hurt_by_player");
        this.lastHurtByPlayerMemoryTime = $$02.getIntOr("last_hurt_by_player_memory_time", 0);
        this.lastHurtByMob = EntityReference.read($$02, "last_hurt_by_mob");
        this.lastHurtByMobTimestamp = $$02.getIntOr("ticks_since_last_hurt_by_mob", 0) + this.tickCount;
        this.equipment.setAll($$02.read(TAG_EQUIPMENT, EntityEquipment.CODEC).orElseGet(EntityEquipment::new));
        this.locatorBarIcon = $$02.read("locator_bar_icon", Waypoint.Icon.CODEC).orElseGet(Waypoint.Icon::new);
    }

    @Override
    public void updateDataBeforeSync() {
        super.updateDataBeforeSync();
        this.updateDirtyEffects();
    }

    protected void tickEffects() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            Iterator<Object> $$1 = this.activeEffects.keySet().iterator();
            try {
                while ($$1.hasNext()) {
                    Holder $$2 = (Holder)$$1.next();
                    MobEffectInstance $$3 = this.activeEffects.get($$2);
                    if (!$$3.tickServer($$0, this, () -> this.onEffectUpdated($$3, true, null))) {
                        $$1.remove();
                        this.onEffectsRemoved(List.of($$3));
                        continue;
                    }
                    if ($$3.getDuration() % 600 != 0) continue;
                    this.onEffectUpdated($$3, false, null);
                }
            }
            catch (ConcurrentModificationException $$2) {}
        } else {
            for (MobEffectInstance $$4 : this.activeEffects.values()) {
                $$4.tickClient();
            }
            List<ParticleOptions> $$5 = this.entityData.get(DATA_EFFECT_PARTICLES);
            if (!$$5.isEmpty()) {
                int $$8;
                boolean $$6 = this.entityData.get(DATA_EFFECT_AMBIENCE_ID);
                int $$7 = this.isInvisible() ? 15 : 4;
                int n = $$8 = $$6 ? 5 : 1;
                if (this.random.nextInt($$7 * $$8) == 0) {
                    this.level().addParticle(Util.getRandom($$5, this.random), this.getRandomX(0.5), this.getRandomY(), this.getRandomZ(0.5), 1.0, 1.0, 1.0);
                }
            }
        }
    }

    private void updateDirtyEffects() {
        if (this.effectsDirty) {
            this.updateInvisibilityStatus();
            this.updateGlowingStatus();
            this.effectsDirty = false;
        }
    }

    protected void updateInvisibilityStatus() {
        if (this.activeEffects.isEmpty()) {
            this.removeEffectParticles();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        this.updateSynchronizedMobEffectParticles();
    }

    private void updateSynchronizedMobEffectParticles() {
        List<ParticleOptions> $$0 = this.activeEffects.values().stream().filter(MobEffectInstance::isVisible).map(MobEffectInstance::getParticleOptions).toList();
        this.entityData.set(DATA_EFFECT_PARTICLES, $$0);
        this.entityData.set(DATA_EFFECT_AMBIENCE_ID, LivingEntity.areAllEffectsAmbient(this.activeEffects.values()));
    }

    private void updateGlowingStatus() {
        boolean $$0 = this.isCurrentlyGlowing();
        if (this.getSharedFlag(6) != $$0) {
            this.setSharedFlag(6, $$0);
        }
    }

    public double getVisibilityPercent(@Nullable Entity $$0) {
        double $$1 = 1.0;
        if (this.isDiscrete()) {
            $$1 *= 0.8;
        }
        if (this.isInvisible()) {
            float $$2 = this.getArmorCoverPercentage();
            if ($$2 < 0.1f) {
                $$2 = 0.1f;
            }
            $$1 *= 0.7 * (double)$$2;
        }
        if ($$0 != null) {
            ItemStack $$3 = this.getItemBySlot(EquipmentSlot.HEAD);
            EntityType<?> $$4 = $$0.getType();
            if ($$4 == EntityType.SKELETON && $$3.is(Items.SKELETON_SKULL) || $$4 == EntityType.ZOMBIE && $$3.is(Items.ZOMBIE_HEAD) || $$4 == EntityType.PIGLIN && $$3.is(Items.PIGLIN_HEAD) || $$4 == EntityType.PIGLIN_BRUTE && $$3.is(Items.PIGLIN_HEAD) || $$4 == EntityType.CREEPER && $$3.is(Items.CREEPER_HEAD)) {
                $$1 *= 0.5;
            }
        }
        return $$1;
    }

    public boolean canAttack(LivingEntity $$0) {
        if ($$0 instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return $$0.canBeSeenAsEnemy();
    }

    public boolean canBeSeenAsEnemy() {
        return !this.isInvulnerable() && this.canBeSeenByAnyone();
    }

    public boolean canBeSeenByAnyone() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean areAllEffectsAmbient(Collection<MobEffectInstance> $$0) {
        for (MobEffectInstance $$1 : $$0) {
            if (!$$1.isVisible() || $$1.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void removeEffectParticles() {
        this.entityData.set(DATA_EFFECT_PARTICLES, List.of());
    }

    public boolean removeAllEffects() {
        if (this.level().isClientSide()) {
            return false;
        }
        if (this.activeEffects.isEmpty()) {
            return false;
        }
        HashMap $$0 = Maps.newHashMap(this.activeEffects);
        this.activeEffects.clear();
        this.onEffectsRemoved($$0.values());
        return true;
    }

    public Collection<MobEffectInstance> getActiveEffects() {
        return this.activeEffects.values();
    }

    public Map<Holder<MobEffect>, MobEffectInstance> getActiveEffectsMap() {
        return this.activeEffects;
    }

    public boolean hasEffect(Holder<MobEffect> $$0) {
        return this.activeEffects.containsKey($$0);
    }

    public @Nullable MobEffectInstance getEffect(Holder<MobEffect> $$0) {
        return this.activeEffects.get($$0);
    }

    public float getEffectBlendFactor(Holder<MobEffect> $$0, float $$1) {
        MobEffectInstance $$2 = this.getEffect($$0);
        if ($$2 != null) {
            return $$2.getBlendFactor(this, $$1);
        }
        return 0.0f;
    }

    public final boolean addEffect(MobEffectInstance $$0) {
        return this.addEffect($$0, null);
    }

    public boolean addEffect(MobEffectInstance $$0, @Nullable Entity $$1) {
        if (!this.canBeAffected($$0)) {
            return false;
        }
        MobEffectInstance $$2 = this.activeEffects.get($$0.getEffect());
        boolean $$3 = false;
        if ($$2 == null) {
            this.activeEffects.put($$0.getEffect(), $$0);
            this.onEffectAdded($$0, $$1);
            $$3 = true;
            $$0.onEffectAdded(this);
        } else if ($$2.update($$0)) {
            this.onEffectUpdated($$2, true, $$1);
            $$3 = true;
        }
        $$0.onEffectStarted(this);
        return $$3;
    }

    public boolean canBeAffected(MobEffectInstance $$0) {
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !$$0.is(MobEffects.INFESTED);
        }
        if (this.getType().is(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !$$0.is(MobEffects.OOZING);
        }
        if (this.getType().is(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !$$0.is(MobEffects.REGENERATION) && !$$0.is(MobEffects.POISON);
        }
        return true;
    }

    public void forceAddEffect(MobEffectInstance $$0, @Nullable Entity $$1) {
        if (!this.canBeAffected($$0)) {
            return;
        }
        MobEffectInstance $$2 = this.activeEffects.put($$0.getEffect(), $$0);
        if ($$2 == null) {
            this.onEffectAdded($$0, $$1);
        } else {
            $$0.copyBlendState($$2);
            this.onEffectUpdated($$0, true, $$1);
        }
    }

    public boolean isInvertedHealAndHarm() {
        return this.getType().is(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    public final @Nullable MobEffectInstance removeEffectNoUpdate(Holder<MobEffect> $$0) {
        return this.activeEffects.remove($$0);
    }

    public boolean removeEffect(Holder<MobEffect> $$0) {
        MobEffectInstance $$1 = this.removeEffectNoUpdate($$0);
        if ($$1 != null) {
            this.onEffectsRemoved(List.of($$1));
            return true;
        }
        return false;
    }

    protected void onEffectAdded(MobEffectInstance $$0, @Nullable Entity $$1) {
        if (!this.level().isClientSide()) {
            this.effectsDirty = true;
            $$0.getEffect().value().addAttributeModifiers(this.getAttributes(), $$0.getAmplifier());
            this.sendEffectToPassengers($$0);
        }
    }

    public void sendEffectToPassengers(MobEffectInstance $$0) {
        for (Entity $$1 : this.getPassengers()) {
            if (!($$1 instanceof ServerPlayer)) continue;
            ServerPlayer $$2 = (ServerPlayer)$$1;
            $$2.connection.send(new ClientboundUpdateMobEffectPacket(this.getId(), $$0, false));
        }
    }

    protected void onEffectUpdated(MobEffectInstance $$0, boolean $$1, @Nullable Entity $$2) {
        if (this.level().isClientSide()) {
            return;
        }
        this.effectsDirty = true;
        if ($$1) {
            MobEffect $$3 = $$0.getEffect().value();
            $$3.removeAttributeModifiers(this.getAttributes());
            $$3.addAttributeModifiers(this.getAttributes(), $$0.getAmplifier());
            this.refreshDirtyAttributes();
        }
        this.sendEffectToPassengers($$0);
    }

    protected void onEffectsRemoved(Collection<MobEffectInstance> $$0) {
        if (this.level().isClientSide()) {
            return;
        }
        this.effectsDirty = true;
        for (MobEffectInstance $$1 : $$0) {
            $$1.getEffect().value().removeAttributeModifiers(this.getAttributes());
            for (Entity $$2 : this.getPassengers()) {
                if (!($$2 instanceof ServerPlayer)) continue;
                ServerPlayer $$3 = (ServerPlayer)$$2;
                $$3.connection.send(new ClientboundRemoveMobEffectPacket(this.getId(), $$1.getEffect()));
            }
        }
        this.refreshDirtyAttributes();
    }

    private void refreshDirtyAttributes() {
        Set<AttributeInstance> $$0 = this.getAttributes().getAttributesToUpdate();
        for (AttributeInstance $$1 : $$0) {
            this.onAttributeUpdated($$1.getAttribute());
        }
        $$0.clear();
    }

    protected void onAttributeUpdated(Holder<Attribute> $$0) {
        Level level;
        if ($$0.is(Attributes.MAX_HEALTH)) {
            float $$1 = this.getMaxHealth();
            if (this.getHealth() > $$1) {
                this.setHealth($$1);
            }
        } else if ($$0.is(Attributes.MAX_ABSORPTION)) {
            float $$2 = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > $$2) {
                this.setAbsorptionAmount($$2);
            }
        } else if ($$0.is(Attributes.SCALE)) {
            this.refreshDimensions();
        } else if ($$0.is(Attributes.WAYPOINT_TRANSMIT_RANGE) && (level = this.level()) instanceof ServerLevel) {
            ServerLevel $$3 = (ServerLevel)level;
            ServerWaypointManager $$4 = $$3.getWaypointManager();
            if (this.attributes.getValue($$0) > 0.0) {
                $$4.trackWaypoint(this);
            } else {
                $$4.untrackWaypoint(this);
            }
        }
    }

    public void heal(float $$0) {
        float $$1 = this.getHealth();
        if ($$1 > 0.0f) {
            this.setHealth($$1 + $$0);
        }
    }

    public float getHealth() {
        return this.entityData.get(DATA_HEALTH_ID).floatValue();
    }

    public void setHealth(float $$0) {
        this.entityData.set(DATA_HEALTH_ID, Float.valueOf(Mth.clamp($$0, 0.0f, this.getMaxHealth())));
    }

    public boolean isDeadOrDying() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity entity;
        boolean $$13;
        boolean $$6;
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        if (this.isDeadOrDying()) {
            return false;
        }
        if ($$1.is(DamageTypeTags.IS_FIRE) && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.noActionTime = 0;
        if ($$2 < 0.0f) {
            $$2 = 0.0f;
        }
        float $$3 = $$2;
        ItemStack $$4 = this.getUseItem();
        float $$5 = this.applyItemBlocking($$0, $$1, $$2);
        $$2 -= $$5;
        boolean bl = $$6 = $$5 > 0.0f;
        if ($$1.is(DamageTypeTags.IS_FREEZING) && this.getType().is(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            $$2 *= 5.0f;
        }
        if ($$1.is(DamageTypeTags.DAMAGES_HELMET) && !this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            this.hurtHelmet($$1, $$2);
            $$2 *= 0.75f;
        }
        if (Float.isNaN($$2) || Float.isInfinite($$2)) {
            $$2 = Float.MAX_VALUE;
        }
        boolean $$7 = true;
        if ((float)this.invulnerableTime > 10.0f && !$$1.is(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if ($$2 <= this.lastHurt) {
                return false;
            }
            this.actuallyHurt($$0, $$1, $$2 - this.lastHurt);
            this.lastHurt = $$2;
            $$7 = false;
        } else {
            this.lastHurt = $$2;
            this.invulnerableTime = 20;
            this.actuallyHurt($$0, $$1, $$2);
            this.hurtTime = this.hurtDuration = 10;
        }
        this.resolveMobResponsibleForDamage($$1);
        this.resolvePlayerResponsibleForDamage($$1);
        if ($$7) {
            BlocksAttacks $$8 = $$4.get(DataComponents.BLOCKS_ATTACKS);
            if ($$6 && $$8 != null) {
                $$8.onBlocked($$0, this);
            } else {
                $$0.broadcastDamageEvent(this, $$1);
            }
            if (!($$1.is(DamageTypeTags.NO_IMPACT) || $$6 && !($$2 > 0.0f))) {
                this.markHurt();
            }
            if (!$$1.is(DamageTypeTags.NO_KNOCKBACK)) {
                double $$9 = 0.0;
                double $$10 = 0.0;
                Entity entity2 = $$1.getDirectEntity();
                if (entity2 instanceof Projectile) {
                    Projectile $$11 = (Projectile)entity2;
                    DoubleDoubleImmutablePair $$12 = $$11.calculateHorizontalHurtKnockbackDirection(this, $$1);
                    $$9 = -$$12.leftDouble();
                    $$10 = -$$12.rightDouble();
                } else if ($$1.getSourcePosition() != null) {
                    $$9 = $$1.getSourcePosition().x() - this.getX();
                    $$10 = $$1.getSourcePosition().z() - this.getZ();
                }
                this.knockback(0.4f, $$9, $$10);
                if (!$$6) {
                    this.indicateDamage($$9, $$10);
                }
            }
        }
        if (this.isDeadOrDying()) {
            if (!this.checkTotemDeathProtection($$1)) {
                if ($$7) {
                    this.makeSound(this.getDeathSound());
                    this.playSecondaryHurtSound($$1);
                }
                this.die($$1);
            }
        } else if ($$7) {
            this.playHurtSound($$1);
            this.playSecondaryHurtSound($$1);
        }
        boolean bl2 = $$13 = !$$6 || $$2 > 0.0f;
        if ($$13) {
            this.lastDamageSource = $$1;
            this.lastDamageStamp = this.level().getGameTime();
            for (MobEffectInstance $$14 : this.getActiveEffects()) {
                $$14.onMobHurt($$0, this, $$1, $$2);
            }
        }
        if ((entity = this) instanceof ServerPlayer) {
            ServerPlayer $$15 = (ServerPlayer)entity;
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger($$15, $$1, $$3, $$2, $$6);
            if ($$5 > 0.0f && $$5 < 3.4028235E37f) {
                $$15.awardStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round($$5 * 10.0f));
            }
        }
        if ((entity = $$1.getEntity()) instanceof ServerPlayer) {
            ServerPlayer $$16 = (ServerPlayer)entity;
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger($$16, this, $$1, $$3, $$2, $$6);
        }
        return $$13;
    }

    public float applyItemBlocking(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity $$12;
        double $$10;
        AbstractArrow $$5;
        BlocksAttacks $$4;
        ItemStack $$3;
        block10: {
            block9: {
                if ($$2 <= 0.0f) {
                    return 0.0f;
                }
                $$3 = this.getItemBlockingWith();
                if ($$3 == null) {
                    return 0.0f;
                }
                $$4 = $$3.get(DataComponents.BLOCKS_ATTACKS);
                if ($$4 == null) break block9;
                if (!$$4.bypassedBy().map($$1::is).orElse(false).booleanValue()) break block10;
            }
            return 0.0f;
        }
        Entity entity = $$1.getDirectEntity();
        if (entity instanceof AbstractArrow && ($$5 = (AbstractArrow)entity).getPierceLevel() > 0) {
            return 0.0f;
        }
        Vec3 $$6 = $$1.getSourcePosition();
        if ($$6 != null) {
            Vec3 $$7 = this.calculateViewVector(0.0f, this.getYHeadRot());
            Vec3 $$8 = $$6.subtract(this.position());
            $$8 = new Vec3($$8.x, 0.0, $$8.z).normalize();
            double $$9 = Math.acos($$8.dot($$7));
        } else {
            $$10 = 3.1415927410125732;
        }
        float $$11 = $$4.resolveBlockedDamage($$1, $$2, $$10);
        $$4.hurtBlockingItem(this.level(), $$3, this, this.getUsedItemHand(), $$11);
        if ($$11 > 0.0f && !$$1.is(DamageTypeTags.IS_PROJECTILE) && ($$12 = $$1.getDirectEntity()) instanceof LivingEntity) {
            LivingEntity $$13 = (LivingEntity)$$12;
            this.blockUsingItem($$0, $$13);
        }
        return $$11;
    }

    private void playSecondaryHurtSound(DamageSource $$0) {
        if ($$0.is(DamageTypes.THORNS)) {
            SoundSource $$1 = this instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            this.level().playSound(null, this.position().x, this.position().y, this.position().z, SoundEvents.THORNS_HIT, $$1);
        }
    }

    protected void resolveMobResponsibleForDamage(DamageSource $$0) {
        Entity entity = $$0.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity $$1 = (LivingEntity)entity;
            if (!($$0.is(DamageTypeTags.NO_ANGER) || $$0.is(DamageTypes.WIND_CHARGE) && this.getType().is(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                this.setLastHurtByMob($$1);
            }
        }
    }

    protected @Nullable Player resolvePlayerResponsibleForDamage(DamageSource $$0) {
        Wolf $$3;
        Entity $$1 = $$0.getEntity();
        if ($$1 instanceof Player) {
            Player $$2 = (Player)$$1;
            this.setLastHurtByPlayer($$2, 100);
        } else if ($$1 instanceof Wolf && ($$3 = (Wolf)$$1).isTame()) {
            if ($$3.getOwnerReference() != null) {
                this.setLastHurtByPlayer($$3.getOwnerReference().getUUID(), 100);
            } else {
                this.lastHurtByPlayer = null;
                this.lastHurtByPlayerMemoryTime = 0;
            }
        }
        return EntityReference.getPlayer(this.lastHurtByPlayer, this.level());
    }

    protected void blockUsingItem(ServerLevel $$0, LivingEntity $$1) {
        $$1.blockedByItem(this);
    }

    protected void blockedByItem(LivingEntity $$0) {
        $$0.knockback(0.5, $$0.getX() - this.getX(), $$0.getZ() - this.getZ());
    }

    private boolean checkTotemDeathProtection(DamageSource $$0) {
        if ($$0.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack $$1 = null;
        DeathProtection $$2 = null;
        for (InteractionHand $$3 : InteractionHand.values()) {
            ItemStack $$4 = this.getItemInHand($$3);
            $$2 = $$4.get(DataComponents.DEATH_PROTECTION);
            if ($$2 == null) continue;
            $$1 = $$4.copy();
            $$4.shrink(1);
            break;
        }
        if ($$1 != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayer) {
                ServerPlayer $$5 = (ServerPlayer)livingEntity;
                $$5.awardStat(Stats.ITEM_USED.get($$1.getItem()));
                CriteriaTriggers.USED_TOTEM.trigger($$5, $$1);
                $$1.causeUseVibration(this, GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            $$2.applyEffects($$1, this);
            this.level().broadcastEntityEvent(this, (byte)35);
        }
        return $$2 != null;
    }

    public @Nullable DamageSource getLastDamageSource() {
        if (this.level().getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource $$0) {
        this.makeSound(this.getHurtSound($$0));
    }

    public void makeSound(@Nullable SoundEvent $$0) {
        if ($$0 != null) {
            this.playSound($$0, this.getSoundVolume(), this.getVoicePitch());
        }
    }

    private void breakItem(ItemStack $$0) {
        if (!$$0.isEmpty()) {
            Holder<SoundEvent> $$1 = $$0.get(DataComponents.BREAK_SOUND);
            if ($$1 != null && !this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), $$1.value(), this.getSoundSource(), 0.8f, 0.8f + this.level().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles($$0, 5);
        }
    }

    public void die(DamageSource $$0) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity $$1 = $$0.getEntity();
        LivingEntity $$2 = this.getKillCredit();
        if ($$2 != null) {
            $$2.awardKillScore(this, $$0);
        }
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        this.stopUsingItem();
        if (!this.level().isClientSide() && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getCombatTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getCombatTracker().recheckStatus();
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$3 = (ServerLevel)level;
            if ($$1 == null || $$1.killedEntity($$3, this, $$0)) {
                this.gameEvent(GameEvent.ENTITY_DIE);
                this.dropAllDeathLoot($$3, $$0);
                this.createWitherRose($$2);
            }
            this.level().broadcastEntityEvent(this, (byte)3);
        }
        this.setPose(Pose.DYING);
    }

    /*
     * WARNING - void declaration
     */
    protected void createWitherRose(@Nullable LivingEntity $$0) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel $$1 = (ServerLevel)level;
        boolean $$3 = false;
        if ($$0 instanceof WitherBoss) {
            void $$2;
            if ($$2.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                BlockPos $$4 = this.blockPosition();
                BlockState $$5 = Blocks.WITHER_ROSE.defaultBlockState();
                if (this.level().getBlockState($$4).isAir() && $$5.canSurvive(this.level(), $$4)) {
                    this.level().setBlock($$4, $$5, 3);
                    $$3 = true;
                }
            }
            if (!$$3) {
                ItemEntity $$6 = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.level().addFreshEntity($$6);
            }
        }
    }

    protected void dropAllDeathLoot(ServerLevel $$0, DamageSource $$1) {
        boolean $$2;
        boolean bl = $$2 = this.lastHurtByPlayerMemoryTime > 0;
        if (this.shouldDropLoot($$0)) {
            this.dropFromLootTable($$0, $$1, $$2);
            this.dropCustomDeathLoot($$0, $$1, $$2);
        }
        this.dropEquipment($$0);
        this.dropExperience($$0, $$1.getEntity());
    }

    protected void dropEquipment(ServerLevel $$0) {
    }

    protected void dropExperience(ServerLevel $$0, @Nullable Entity $$1) {
        if (!this.wasExperienceConsumed() && (this.isAlwaysExperienceDropper() || this.lastHurtByPlayerMemoryTime > 0 && this.shouldDropExperience() && $$0.getGameRules().get(GameRules.MOB_DROPS).booleanValue())) {
            ExperienceOrb.award($$0, this.position(), this.getExperienceReward($$0, $$1));
        }
    }

    protected void dropCustomDeathLoot(ServerLevel $$0, DamageSource $$1, boolean $$2) {
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getKnockback(Entity $$0, DamageSource $$1) {
        float $$2 = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$3 = (ServerLevel)level;
            return EnchantmentHelper.modifyKnockback($$3, this.getWeaponItem(), $$0, $$1, $$2) / 2.0f;
        }
        return $$2 / 2.0f;
    }

    protected void dropFromLootTable(ServerLevel $$0, DamageSource $$1, boolean $$2) {
        Optional<ResourceKey<LootTable>> $$3 = this.getLootTable();
        if ($$3.isEmpty()) {
            return;
        }
        this.dropFromLootTable($$0, $$1, $$2, $$3.get());
    }

    public void dropFromLootTable(ServerLevel $$0, DamageSource $$12, boolean $$2, ResourceKey<LootTable> $$3) {
        this.dropFromLootTable($$0, $$12, $$2, $$3, $$1 -> this.spawnAtLocation($$0, (ItemStack)$$1));
    }

    public void dropFromLootTable(ServerLevel $$0, DamageSource $$1, boolean $$2, ResourceKey<LootTable> $$3, Consumer<ItemStack> $$4) {
        LootTable $$5 = $$0.getServer().reloadableRegistries().getLootTable($$3);
        LootParams.Builder $$6 = new LootParams.Builder($$0).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.DAMAGE_SOURCE, $$1).withOptionalParameter(LootContextParams.ATTACKING_ENTITY, $$1.getEntity()).withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, $$1.getDirectEntity());
        Player $$7 = this.getLastHurtByPlayer();
        if ($$2 && $$7 != null) {
            $$6 = $$6.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, $$7).withLuck($$7.getLuck());
        }
        LootParams $$8 = $$6.create(LootContextParamSets.ENTITY);
        $$5.getRandomItems($$8, this.getLootTableSeed(), $$4);
    }

    public boolean dropFromEntityInteractLootTable(ServerLevel $$0, ResourceKey<LootTable> $$1, @Nullable Entity $$22, ItemStack $$3, BiConsumer<ServerLevel, ItemStack> $$4) {
        return this.dropFromLootTable($$0, $$1, $$2 -> $$2.withParameter(LootContextParams.TARGET_ENTITY, this).withOptionalParameter(LootContextParams.INTERACTING_ENTITY, $$22).withParameter(LootContextParams.TOOL, $$3).create(LootContextParamSets.ENTITY_INTERACT), $$4);
    }

    public boolean dropFromGiftLootTable(ServerLevel $$02, ResourceKey<LootTable> $$1, BiConsumer<ServerLevel, ItemStack> $$2) {
        return this.dropFromLootTable($$02, $$1, $$0 -> $$0.withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.GIFT), $$2);
    }

    protected void dropFromShearingLootTable(ServerLevel $$0, ResourceKey<LootTable> $$12, ItemStack $$2, BiConsumer<ServerLevel, ItemStack> $$3) {
        this.dropFromLootTable($$0, $$12, $$1 -> $$1.withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.TOOL, $$2).create(LootContextParamSets.SHEARING), $$3);
    }

    protected boolean dropFromLootTable(ServerLevel $$0, ResourceKey<LootTable> $$1, Function<LootParams.Builder, LootParams> $$22, BiConsumer<ServerLevel, ItemStack> $$3) {
        LootParams $$5;
        LootTable $$4 = $$0.getServer().reloadableRegistries().getLootTable($$1);
        ObjectArrayList<ItemStack> $$6 = $$4.getRandomItems($$5 = $$22.apply(new LootParams.Builder($$0)));
        if (!$$6.isEmpty()) {
            $$6.forEach($$2 -> $$3.accept($$0, (ItemStack)$$2));
            return true;
        }
        return false;
    }

    public void knockback(double $$0, double $$1, double $$2) {
        if (($$0 *= 1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.needsSync = true;
        Vec3 $$3 = this.getDeltaMovement();
        while ($$1 * $$1 + $$2 * $$2 < (double)1.0E-5f) {
            $$1 = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
            $$2 = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
        }
        Vec3 $$4 = new Vec3($$1, 0.0, $$2).normalize().scale($$0);
        this.setDeltaMovement($$3.x / 2.0 - $$4.x, this.onGround() ? Math.min(0.4, $$3.y / 2.0 + $$0) : $$3.y, $$3.z / 2.0 - $$4.z);
    }

    public void indicateDamage(double $$0, double $$1) {
    }

    protected @Nullable SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.GENERIC_HURT;
    }

    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    private SoundEvent getFallDamageSound(int $$0) {
        return $$0 > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void skipDropExperience() {
        this.skipDropExperience = true;
    }

    public boolean wasExperienceConsumed() {
        return this.skipDropExperience;
    }

    public float getHurtDir() {
        return 0.0f;
    }

    protected AABB getHitbox() {
        AABB $$0 = this.getBoundingBox();
        Entity $$1 = this.getVehicle();
        if ($$1 != null) {
            Vec3 $$2 = $$1.getPassengerRidingPosition(this);
            return $$0.setMinY(Math.max($$2.y, $$0.minY));
        }
        return $$0;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> activeLocationDependentEnchantments(EquipmentSlot $$02) {
        return (Map)this.activeLocationDependentEnchantments.computeIfAbsent($$02, $$0 -> new Reference2ObjectArrayMap());
    }

    public void lungeForwardMaybe() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$0 = (ServerLevel)level;
            EnchantmentHelper.doLungeEffects($$0, this);
        }
    }

    public Fallsounds getFallSounds() {
        return new Fallsounds(SoundEvents.GENERIC_SMALL_FALL, SoundEvents.GENERIC_BIG_FALL);
    }

    public Optional<BlockPos> getLastClimbablePos() {
        return this.lastClimbablePos;
    }

    public boolean onClimbable() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos $$0 = this.blockPosition();
        BlockState $$1 = this.getInBlockState();
        if (this.isFallFlying() && $$1.is(BlockTags.CAN_GLIDE_THROUGH)) {
            return false;
        }
        if ($$1.is(BlockTags.CLIMBABLE)) {
            this.lastClimbablePos = Optional.of($$0);
            return true;
        }
        if ($$1.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder($$0, $$1)) {
            this.lastClimbablePos = Optional.of($$0);
            return true;
        }
        return false;
    }

    private boolean trapdoorUsableAsLadder(BlockPos $$0, BlockState $$1) {
        if ($$1.getValue(TrapDoorBlock.OPEN).booleanValue()) {
            BlockState $$2 = this.level().getBlockState($$0.below());
            return $$2.is(Blocks.LADDER) && $$2.getValue(LadderBlock.FACING) == $$1.getValue(TrapDoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    public boolean isLookingAtMe(LivingEntity $$0, double $$1, boolean $$2, boolean $$3, double ... $$4) {
        Vec3 $$5 = $$0.getViewVector(1.0f).normalize();
        for (double $$6 : $$4) {
            Vec3 $$7 = new Vec3(this.getX() - $$0.getX(), $$6 - $$0.getEyeY(), this.getZ() - $$0.getZ());
            double $$8 = $$7.length();
            $$7 = $$7.normalize();
            double $$9 = $$5.dot($$7);
            double d = $$2 ? $$8 : 1.0;
            if (!($$9 > 1.0 - $$1 / d) || !$$0.hasLineOfSight(this, $$3 ? ClipContext.Block.VISUAL : ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$6)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        return this.getComfortableFallDistance(0.0f);
    }

    protected final int getComfortableFallDistance(float $$0) {
        return Mth.floor($$0 + 3.0f);
    }

    @Override
    public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
        boolean $$3 = super.causeFallDamage($$0, $$1, $$2);
        int $$4 = this.calculateFallDamage($$0, $$1);
        if ($$4 > 0) {
            this.playSound(this.getFallDamageSound($$4), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.hurt($$2, $$4);
            return true;
        }
        return $$3;
    }

    protected int calculateFallDamage(double $$0, float $$1) {
        if (this.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        double $$2 = this.calculateFallPower($$0);
        return Mth.floor($$2 * (double)$$1 * this.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER));
    }

    private double calculateFallPower(double $$0) {
        return $$0 + 1.0E-6 - this.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int $$0 = Mth.floor(this.getX());
        int $$1 = Mth.floor(this.getY() - (double)0.2f);
        int $$2 = Mth.floor(this.getZ());
        BlockState $$3 = this.level().getBlockState(new BlockPos($$0, $$1, $$2));
        if (!$$3.isAir()) {
            SoundType $$4 = $$3.getSoundType();
            this.playSound($$4.getFallSound(), $$4.getVolume() * 0.5f, $$4.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateHurt(float $$0) {
        this.hurtTime = this.hurtDuration = 10;
    }

    public int getArmorValue() {
        return Mth.floor(this.getAttributeValue(Attributes.ARMOR));
    }

    protected void hurtArmor(DamageSource $$0, float $$1) {
    }

    protected void hurtHelmet(DamageSource $$0, float $$1) {
    }

    protected void doHurtEquipment(DamageSource $$0, float $$1, EquipmentSlot ... $$2) {
        if ($$1 <= 0.0f) {
            return;
        }
        int $$3 = (int)Math.max(1.0f, $$1 / 4.0f);
        for (EquipmentSlot $$4 : $$2) {
            ItemStack $$5 = this.getItemBySlot($$4);
            Equippable $$6 = $$5.get(DataComponents.EQUIPPABLE);
            if ($$6 == null || !$$6.damageOnHurt() || !$$5.isDamageableItem() || !$$5.canBeHurtBy($$0)) continue;
            $$5.hurtAndBreak($$3, this, $$4);
        }
    }

    protected float getDamageAfterArmorAbsorb(DamageSource $$0, float $$1) {
        if (!$$0.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor($$0, $$1);
            $$1 = CombatRules.getDamageAfterAbsorb(this, $$1, $$0, this.getArmorValue(), (float)this.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        return $$1;
    }

    protected float getDamageAfterMagicAbsorb(DamageSource $$0, float $$1) {
        float $$9;
        int $$2;
        int $$3;
        float $$4;
        float $$5;
        float $$6;
        if ($$0.is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return $$1;
        }
        if (this.hasEffect(MobEffects.RESISTANCE) && !$$0.is(DamageTypeTags.BYPASSES_RESISTANCE) && ($$6 = ($$5 = $$1) - ($$1 = Math.max(($$4 = $$1 * (float)($$3 = 25 - ($$2 = (this.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && $$6 < 3.4028235E37f) {
            if (this instanceof ServerPlayer) {
                ((ServerPlayer)this).awardStat(Stats.DAMAGE_RESISTED, Math.round($$6 * 10.0f));
            } else if ($$0.getEntity() instanceof ServerPlayer) {
                ((ServerPlayer)$$0.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round($$6 * 10.0f));
            }
        }
        if ($$1 <= 0.0f) {
            return 0.0f;
        }
        if ($$0.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return $$1;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$7 = (ServerLevel)level;
            float $$8 = EnchantmentHelper.getDamageProtection($$7, this, $$0);
        } else {
            $$9 = 0.0f;
        }
        if ($$9 > 0.0f) {
            $$1 = CombatRules.getDamageAfterMagicAbsorb($$1, $$9);
        }
        return $$1;
    }

    protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity entity;
        if (this.isInvulnerableTo($$0, $$1)) {
            return;
        }
        $$2 = this.getDamageAfterArmorAbsorb($$1, $$2);
        float $$3 = $$2 = this.getDamageAfterMagicAbsorb($$1, $$2);
        $$2 = Math.max($$2 - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - ($$3 - $$2));
        float $$4 = $$3 - $$2;
        if ($$4 > 0.0f && $$4 < 3.4028235E37f && (entity = $$1.getEntity()) instanceof ServerPlayer) {
            ServerPlayer $$5 = (ServerPlayer)entity;
            $$5.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round($$4 * 10.0f));
        }
        if ($$2 == 0.0f) {
            return;
        }
        this.getCombatTracker().recordDamage($$1, $$2);
        this.setHealth(this.getHealth() - $$2);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - $$2);
        this.gameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    public @Nullable LivingEntity getKillCredit() {
        if (this.lastHurtByPlayer != null) {
            return this.lastHurtByPlayer.getEntity(this.level(), Player.class);
        }
        if (this.lastHurtByMob != null) {
            return this.lastHurtByMob.getEntity(this.level(), LivingEntity.class);
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(Attributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(Attributes.MAX_ABSORPTION);
    }

    public final int getArrowCount() {
        return this.entityData.get(DATA_ARROW_COUNT_ID);
    }

    public final void setArrowCount(int $$0) {
        this.entityData.set(DATA_ARROW_COUNT_ID, $$0);
    }

    public final int getStingerCount() {
        return this.entityData.get(DATA_STINGER_COUNT_ID);
    }

    public final void setStingerCount(int $$0) {
        this.entityData.set(DATA_STINGER_COUNT_ID, $$0);
    }

    private int getCurrentSwingDuration() {
        ItemStack $$0 = this.getItemInHand(InteractionHand.MAIN_HAND);
        int $$1 = $$0.getSwingAnimation().duration();
        if (MobEffectUtil.hasDigSpeed(this)) {
            return $$1 - (1 + MobEffectUtil.getDigSpeedAmplification(this));
        }
        if (this.hasEffect(MobEffects.MINING_FATIGUE)) {
            return $$1 + (1 + this.getEffect(MobEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return $$1;
    }

    public void swing(InteractionHand $$0) {
        this.swing($$0, false);
    }

    public void swing(InteractionHand $$0, boolean $$1) {
        if (!this.swinging || this.swingTime >= this.getCurrentSwingDuration() / 2 || this.swingTime < 0) {
            this.swingTime = -1;
            this.swinging = true;
            this.swingingArm = $$0;
            if (this.level() instanceof ServerLevel) {
                ClientboundAnimatePacket $$2 = new ClientboundAnimatePacket(this, $$0 == InteractionHand.MAIN_HAND ? 0 : 3);
                ServerChunkCache $$3 = ((ServerLevel)this.level()).getChunkSource();
                if ($$1) {
                    $$3.sendToTrackingPlayersAndSelf(this, $$2);
                } else {
                    $$3.sendToTrackingPlayers(this, $$2);
                }
            }
        }
    }

    @Override
    public void handleDamageEvent(DamageSource $$0) {
        this.walkAnimation.setSpeed(1.5f);
        this.invulnerableTime = 20;
        this.hurtTime = this.hurtDuration = 10;
        SoundEvent $$1 = this.getHurtSound($$0);
        if ($$1 != null) {
            this.playSound($$1, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.lastDamageSource = $$0;
        this.lastDamageStamp = this.level().getGameTime();
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        switch ($$0) {
            case 3: {
                SoundEvent $$1 = this.getDeathSound();
                if ($$1 != null) {
                    this.playSound($$1, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof Player) break;
                this.setHealth(0.0f);
                this.die(this.damageSources().generic());
                break;
            }
            case 46: {
                int $$2 = 128;
                for (int $$3 = 0; $$3 < 128; ++$$3) {
                    double $$4 = (double)$$3 / 127.0;
                    float $$5 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float $$6 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float $$7 = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double $$8 = Mth.lerp($$4, this.xo, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    double $$9 = Mth.lerp($$4, this.yo, this.getY()) + this.random.nextDouble() * (double)this.getBbHeight();
                    double $$10 = Mth.lerp($$4, this.zo, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth() * 2.0;
                    this.level().addParticle(ParticleTypes.PORTAL, $$8, $$9, $$10, $$5, $$6, $$7);
                }
                break;
            }
            case 47: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.BODY));
                break;
            }
            case 68: {
                this.breakItem(this.getItemBySlot(EquipmentSlot.SADDLE));
                break;
            }
            case 54: {
                HoneyBlock.showJumpParticles(this);
                break;
            }
            case 55: {
                this.swapHandItems();
                break;
            }
            case 60: {
                this.makePoofParticles();
                break;
            }
            case 67: {
                this.makeDrownParticles();
                break;
            }
            case 2: {
                this.onKineticHit();
                break;
            }
            default: {
                super.handleEntityEvent($$0);
            }
        }
    }

    public float getTicksSinceLastKineticHitFeedback(float $$0) {
        if (this.lastKineticHitFeedbackTime < 0L) {
            return 0.0f;
        }
        return (float)(this.level().getGameTime() - this.lastKineticHitFeedbackTime) + $$0;
    }

    public void makePoofParticles() {
        for (int $$0 = 0; $$0 < 20; ++$$0) {
            double $$1 = this.random.nextGaussian() * 0.02;
            double $$2 = this.random.nextGaussian() * 0.02;
            double $$3 = this.random.nextGaussian() * 0.02;
            double $$4 = 10.0;
            this.level().addParticle(ParticleTypes.POOF, this.getRandomX(1.0) - $$1 * 10.0, this.getRandomY() - $$2 * 10.0, this.getRandomZ(1.0) - $$3 * 10.0, $$1, $$2, $$3);
        }
    }

    private void makeDrownParticles() {
        Vec3 $$0 = this.getDeltaMovement();
        for (int $$1 = 0; $$1 < 8; ++$$1) {
            double $$2 = this.random.triangle(0.0, 1.0);
            double $$3 = this.random.triangle(0.0, 1.0);
            double $$4 = this.random.triangle(0.0, 1.0);
            this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + $$2, this.getY() + $$3, this.getZ() + $$4, $$0.x, $$0.y, $$0.z);
        }
    }

    private void onKineticHit() {
        if (this.level().getGameTime() - this.lastKineticHitFeedbackTime <= 10L) {
            return;
        }
        this.lastKineticHitFeedbackTime = this.level().getGameTime();
        KineticWeapon $$0 = this.useItem.get(DataComponents.KINETIC_WEAPON);
        if ($$0 == null) {
            return;
        }
        $$0.makeLocalHitSound(this);
    }

    private void swapHandItems() {
        ItemStack $$0 = this.getItemBySlot(EquipmentSlot.OFFHAND);
        this.setItemSlot(EquipmentSlot.OFFHAND, this.getItemBySlot(EquipmentSlot.MAINHAND));
        this.setItemSlot(EquipmentSlot.MAINHAND, $$0);
    }

    @Override
    protected void onBelowWorld() {
        this.hurt(this.damageSources().fellOutOfWorld(), 4.0f);
    }

    protected void updateSwingTime() {
        int $$0 = this.getCurrentSwingDuration();
        if (this.swinging) {
            ++this.swingTime;
            if (this.swingTime >= $$0) {
                this.swingTime = 0;
                this.swinging = false;
            }
        } else {
            this.swingTime = 0;
        }
        this.attackAnim = (float)this.swingTime / (float)$$0;
    }

    public @Nullable AttributeInstance getAttribute(Holder<Attribute> $$0) {
        return this.getAttributes().getInstance($$0);
    }

    public double getAttributeValue(Holder<Attribute> $$0) {
        return this.getAttributes().getValue($$0);
    }

    public double getAttributeBaseValue(Holder<Attribute> $$0) {
        return this.getAttributes().getBaseValue($$0);
    }

    public AttributeMap getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandItem() {
        return this.getItemBySlot(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffhandItem() {
        return this.getItemBySlot(EquipmentSlot.OFFHAND);
    }

    public ItemStack getItemHeldByArm(HumanoidArm $$0) {
        return this.getMainArm() == $$0 ? this.getMainHandItem() : this.getOffhandItem();
    }

    @Override
    public ItemStack getWeaponItem() {
        return this.getMainHandItem();
    }

    public AttackRange entityAttackRange() {
        AttackRange $$0 = this.getActiveItem().get(DataComponents.ATTACK_RANGE);
        return $$0 != null ? $$0 : AttackRange.defaultFor(this);
    }

    public ItemStack getActiveItem() {
        if (this.isUsingItem()) {
            return this.getUseItem();
        }
        return this.getMainHandItem();
    }

    public boolean isHolding(Item $$0) {
        return this.isHolding((ItemStack $$1) -> $$1.is($$0));
    }

    public boolean isHolding(Predicate<ItemStack> $$0) {
        return $$0.test(this.getMainHandItem()) || $$0.test(this.getOffhandItem());
    }

    public ItemStack getItemInHand(InteractionHand $$0) {
        if ($$0 == InteractionHand.MAIN_HAND) {
            return this.getItemBySlot(EquipmentSlot.MAINHAND);
        }
        if ($$0 == InteractionHand.OFF_HAND) {
            return this.getItemBySlot(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)$$0));
    }

    public void setItemInHand(InteractionHand $$0, ItemStack $$1) {
        if ($$0 == InteractionHand.MAIN_HAND) {
            this.setItemSlot(EquipmentSlot.MAINHAND, $$1);
        } else if ($$0 == InteractionHand.OFF_HAND) {
            this.setItemSlot(EquipmentSlot.OFFHAND, $$1);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)$$0));
        }
    }

    public boolean hasItemInSlot(EquipmentSlot $$0) {
        return !this.getItemBySlot($$0).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot $$0) {
        return true;
    }

    public ItemStack getItemBySlot(EquipmentSlot $$0) {
        return this.equipment.get($$0);
    }

    public void setItemSlot(EquipmentSlot $$0, ItemStack $$1) {
        this.onEquipItem($$0, this.equipment.set($$0, $$1), $$1);
    }

    public float getArmorCoverPercentage() {
        int $$0 = 0;
        int $$1 = 0;
        for (EquipmentSlot $$2 : EquipmentSlotGroup.ARMOR) {
            if ($$2.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack $$3 = this.getItemBySlot($$2);
            if (!$$3.isEmpty()) {
                ++$$1;
            }
            ++$$0;
        }
        return $$0 > 0 ? (float)$$1 / (float)$$0 : 0.0f;
    }

    @Override
    public void setSprinting(boolean $$0) {
        super.setSprinting($$0);
        AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        $$1.removeModifier(SPEED_MODIFIER_SPRINTING.id());
        if ($$0) {
            $$1.addTransientModifier(SPEED_MODIFIER_SPRINTING);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getVoicePitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDeadOrDying();
    }

    @Override
    public void push(Entity $$0) {
        if (!this.isSleeping()) {
            super.push($$0);
        }
    }

    private void dismountVehicle(Entity $$0) {
        Vec3 $$8;
        if (this.isRemoved()) {
            Vec3 $$12 = this.position();
        } else if ($$0.isRemoved() || this.level().getBlockState($$0.blockPosition()).is(BlockTags.PORTALS)) {
            boolean $$4;
            double $$2 = Math.max(this.getY(), $$0.getY());
            Vec3 $$3 = new Vec3(this.getX(), $$2, this.getZ());
            boolean bl = $$4 = this.getBbWidth() <= 4.0f && this.getBbHeight() <= 4.0f;
            if ($$4) {
                double $$5 = (double)this.getBbHeight() / 2.0;
                Vec3 $$6 = $$3.add(0.0, $$5, 0.0);
                VoxelShape $$7 = Shapes.create(AABB.ofSize($$6, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()));
                $$3 = this.level().findFreePosition(this, $$7, $$6, this.getBbWidth(), this.getBbHeight(), this.getBbWidth()).map($$1 -> $$1.add(0.0, -$$5, 0.0)).orElse($$3);
            }
        } else {
            $$8 = $$0.getDismountLocationForPassenger(this);
        }
        this.dismountTo($$8.x, $$8.y, $$8.z);
    }

    @Override
    public boolean shouldShowName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpPower() {
        return this.getJumpPower(1.0f);
    }

    protected float getJumpPower(float $$0) {
        return (float)this.getAttributeValue(Attributes.JUMP_STRENGTH) * $$0 * this.getBlockJumpFactor() + this.getJumpBoostPower();
    }

    public float getJumpBoostPower() {
        return this.hasEffect(MobEffects.JUMP_BOOST) ? 0.1f * ((float)this.getEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jumpFromGround() {
        float $$0 = this.getJumpPower();
        if ($$0 <= 1.0E-5f) {
            return;
        }
        Vec3 $$1 = this.getDeltaMovement();
        this.setDeltaMovement($$1.x, Math.max((double)$$0, $$1.y), $$1.z);
        if (this.isSprinting()) {
            float $$2 = this.getYRot() * ((float)Math.PI / 180);
            this.addDeltaMovement(new Vec3((double)(-Mth.sin($$2)) * 0.2, 0.0, (double)Mth.cos($$2) * 0.2));
        }
        this.needsSync = true;
    }

    protected void goDownInWater() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04f, 0.0));
    }

    protected void jumpInLiquid(TagKey<Fluid> $$0) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04f, 0.0));
    }

    protected float getWaterSlowDown() {
        return 0.8f;
    }

    public boolean canStandOnFluid(FluidState $$0) {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return this.getAttributeValue(Attributes.GRAVITY);
    }

    protected double getEffectiveGravity() {
        boolean $$0;
        boolean bl = $$0 = this.getDeltaMovement().y <= 0.0;
        if ($$0 && this.hasEffect(MobEffects.SLOW_FALLING)) {
            return Math.min(this.getGravity(), 0.01);
        }
        return this.getGravity();
    }

    public void travel(Vec3 $$0) {
        if (this.shouldTravelInFluid(this.level().getFluidState(this.blockPosition()))) {
            this.travelInFluid($$0);
        } else if (this.isFallFlying()) {
            this.travelFallFlying($$0);
        } else {
            this.travelInAir($$0);
        }
    }

    protected boolean shouldTravelInFluid(FluidState $$0) {
        return (this.isInWater() || this.isInLava()) && this.isAffectedByFluids() && !this.canStandOnFluid($$0);
    }

    protected void travelFlying(Vec3 $$0, float $$1) {
        this.travelFlying($$0, 0.02f, 0.02f, $$1);
    }

    protected void travelFlying(Vec3 $$0, float $$1, float $$2, float $$3) {
        if (this.isInWater()) {
            this.moveRelative($$1, $$0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8f));
        } else if (this.isInLava()) {
            this.moveRelative($$2, $$0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            this.moveRelative($$3, $$0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91f));
        }
    }

    private void travelInAir(Vec3 $$0) {
        BlockPos $$1 = this.getBlockPosBelowThatAffectsMyMovement();
        float $$2 = this.onGround() ? this.level().getBlockState($$1).getBlock().getFriction() : 1.0f;
        float $$3 = $$2 * 0.91f;
        Vec3 $$4 = this.handleRelativeFrictionAndCalculateMovement($$0, $$2);
        double $$5 = $$4.y;
        MobEffectInstance $$6 = this.getEffect(MobEffects.LEVITATION);
        $$5 = $$6 != null ? ($$5 += (0.05 * (double)($$6.getAmplifier() + 1) - $$4.y) * 0.2) : (!this.level().isClientSide() || this.level().hasChunkAt($$1) ? ($$5 -= this.getEffectiveGravity()) : (this.getY() > (double)this.level().getMinY() ? -0.1 : 0.0));
        if (this.shouldDiscardFriction()) {
            this.setDeltaMovement($$4.x, $$5, $$4.z);
        } else {
            float $$7 = this instanceof FlyingAnimal ? $$3 : 0.98f;
            this.setDeltaMovement($$4.x * (double)$$3, $$5 * (double)$$7, $$4.z * (double)$$3);
        }
    }

    private void travelInFluid(Vec3 $$0) {
        boolean $$1 = this.getDeltaMovement().y <= 0.0;
        double $$2 = this.getY();
        double $$3 = this.getEffectiveGravity();
        if (this.isInWater()) {
            this.travelInWater($$0, $$3, $$1, $$2);
            this.floatInWaterWhileRidden();
        } else {
            this.travelInLava($$0, $$3, $$1, $$2);
        }
    }

    protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
        float $$4 = this.isSprinting() ? 0.9f : this.getWaterSlowDown();
        float $$5 = 0.02f;
        float $$6 = (float)this.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);
        if (!this.onGround()) {
            $$6 *= 0.5f;
        }
        if ($$6 > 0.0f) {
            $$4 += (0.54600006f - $$4) * $$6;
            $$5 += (this.getSpeed() - $$5) * $$6;
        }
        if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            $$4 = 0.96f;
        }
        this.moveRelative($$5, $$0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 $$7 = this.getDeltaMovement();
        if (this.horizontalCollision && this.onClimbable()) {
            $$7 = new Vec3($$7.x, 0.2, $$7.z);
        }
        $$7 = $$7.multiply($$4, 0.8f, $$4);
        this.setDeltaMovement(this.getFluidFallingAdjustedMovement($$1, $$2, $$7));
        this.jumpOutOfFluid($$3);
    }

    private void travelInLava(Vec3 $$0, double $$1, boolean $$2, double $$3) {
        this.moveRelative(0.02f, $$0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8f, 0.5));
            Vec3 $$4 = this.getFluidFallingAdjustedMovement($$1, $$2, this.getDeltaMovement());
            this.setDeltaMovement($$4);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        }
        if ($$1 != 0.0) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -$$1 / 4.0, 0.0));
        }
        this.jumpOutOfFluid($$3);
    }

    private void jumpOutOfFluid(double $$0) {
        Vec3 $$1 = this.getDeltaMovement();
        if (this.horizontalCollision && this.isFree($$1.x, $$1.y + (double)0.6f - this.getY() + $$0, $$1.z)) {
            this.setDeltaMovement($$1.x, 0.3f, $$1.z);
        }
    }

    private void floatInWaterWhileRidden() {
        boolean $$0 = this.getType().is(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN);
        if ($$0 && this.isVehicle() && this.getFluidHeight(FluidTags.WATER) > this.getFluidJumpThreshold()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.04f, 0.0));
        }
    }

    private void travelFallFlying(Vec3 $$0) {
        if (this.onClimbable()) {
            this.travelInAir($$0);
            this.stopFallFlying();
            return;
        }
        Vec3 $$1 = this.getDeltaMovement();
        double $$2 = $$1.horizontalDistance();
        this.setDeltaMovement(this.updateFallFlyingMovement($$1));
        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level().isClientSide()) {
            double $$3 = this.getDeltaMovement().horizontalDistance();
            this.handleFallFlyingCollisions($$2, $$3);
        }
    }

    public void stopFallFlying() {
        this.setSharedFlag(7, true);
        this.setSharedFlag(7, false);
    }

    private Vec3 updateFallFlyingMovement(Vec3 $$0) {
        Vec3 $$1 = this.getLookAngle();
        float $$2 = this.getXRot() * ((float)Math.PI / 180);
        double $$3 = Math.sqrt($$1.x * $$1.x + $$1.z * $$1.z);
        double $$4 = $$0.horizontalDistance();
        double $$5 = this.getEffectiveGravity();
        double $$6 = Mth.square(Math.cos($$2));
        $$0 = $$0.add(0.0, $$5 * (-1.0 + $$6 * 0.75), 0.0);
        if ($$0.y < 0.0 && $$3 > 0.0) {
            double $$7 = $$0.y * -0.1 * $$6;
            $$0 = $$0.add($$1.x * $$7 / $$3, $$7, $$1.z * $$7 / $$3);
        }
        if ($$2 < 0.0f && $$3 > 0.0) {
            double $$8 = $$4 * (double)(-Mth.sin($$2)) * 0.04;
            $$0 = $$0.add(-$$1.x * $$8 / $$3, $$8 * 3.2, -$$1.z * $$8 / $$3);
        }
        if ($$3 > 0.0) {
            $$0 = $$0.add(($$1.x / $$3 * $$4 - $$0.x) * 0.1, 0.0, ($$1.z / $$3 * $$4 - $$0.z) * 0.1);
        }
        return $$0.multiply(0.99f, 0.98f, 0.99f);
    }

    private void handleFallFlyingCollisions(double $$0, double $$1) {
        double $$2;
        float $$3;
        if (this.horizontalCollision && ($$3 = (float)(($$2 = $$0 - $$1) * 10.0 - 3.0)) > 0.0f) {
            this.playSound(this.getFallDamageSound((int)$$3), 1.0f, 1.0f);
            this.hurt(this.damageSources().flyIntoWall(), $$3);
        }
    }

    private void travelRidden(Player $$0, Vec3 $$1) {
        Vec3 $$2 = this.getRiddenInput($$0, $$1);
        this.tickRidden($$0, $$2);
        if (this.canSimulateMovement()) {
            this.setSpeed(this.getRiddenSpeed($$0));
            this.travel($$2);
        } else {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    protected void tickRidden(Player $$0, Vec3 $$1) {
    }

    protected Vec3 getRiddenInput(Player $$0, Vec3 $$1) {
        return $$1;
    }

    protected float getRiddenSpeed(Player $$0) {
        return this.getSpeed();
    }

    public void calculateEntityAnimation(boolean $$0) {
        float $$1 = (float)Mth.length(this.getX() - this.xo, $$0 ? this.getY() - this.yo : 0.0, this.getZ() - this.zo);
        if (this.isPassenger() || !this.isAlive()) {
            this.walkAnimation.stop();
        } else {
            this.updateWalkAnimation($$1);
        }
    }

    protected void updateWalkAnimation(float $$0) {
        float $$1 = Math.min($$0 * 4.0f, 1.0f);
        this.walkAnimation.update($$1, 0.4f, this.isBaby() ? 3.0f : 1.0f);
    }

    private Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 $$0, float $$1) {
        this.moveRelative(this.getFrictionInfluencedSpeed($$1), $$0);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 $$2 = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.wasInPowderSnow && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            $$2 = new Vec3($$2.x, 0.2, $$2.z);
        }
        return $$2;
    }

    public Vec3 getFluidFallingAdjustedMovement(double $$0, boolean $$1, Vec3 $$2) {
        if ($$0 != 0.0 && !this.isSprinting()) {
            double $$4;
            if ($$1 && Math.abs($$2.y - 0.005) >= 0.003 && Math.abs($$2.y - $$0 / 16.0) < 0.003) {
                double $$3 = -0.003;
            } else {
                $$4 = $$2.y - $$0 / 16.0;
            }
            return new Vec3($$2.x, $$4, $$2.z);
        }
        return $$2;
    }

    private Vec3 handleOnClimbable(Vec3 $$0) {
        if (this.onClimbable()) {
            this.resetFallDistance();
            float $$1 = 0.15f;
            double $$2 = Mth.clamp($$0.x, (double)-0.15f, (double)0.15f);
            double $$3 = Mth.clamp($$0.z, (double)-0.15f, (double)0.15f);
            double $$4 = Math.max($$0.y, (double)-0.15f);
            if ($$4 < 0.0 && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
                $$4 = 0.0;
            }
            $$0 = new Vec3($$2, $$4, $$3);
        }
        return $$0;
    }

    private float getFrictionInfluencedSpeed(float $$0) {
        if (this.onGround()) {
            return this.getSpeed() * (0.21600002f / ($$0 * $$0 * $$0));
        }
        return this.getFlyingSpeed();
    }

    protected float getFlyingSpeed() {
        return this.getControllingPassenger() instanceof Player ? this.getSpeed() * 0.1f : 0.02f;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float $$0) {
        this.speed = $$0;
    }

    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        this.setLastHurtMob($$1);
        return false;
    }

    public void causeExtraKnockback(Entity $$0, float $$1, Vec3 $$2) {
        if ($$1 > 0.0f && $$0 instanceof LivingEntity) {
            LivingEntity $$3 = (LivingEntity)$$0;
            $$3.knockback($$1, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
    }

    protected void playAttackSound() {
    }

    @Override
    public void tick() {
        super.tick();
        this.updatingUsingItem();
        this.updateSwimAmount();
        if (!this.level().isClientSide()) {
            int $$1;
            int $$0 = this.getArrowCount();
            if ($$0 > 0) {
                if (this.removeArrowTime <= 0) {
                    this.removeArrowTime = 20 * (30 - $$0);
                }
                --this.removeArrowTime;
                if (this.removeArrowTime <= 0) {
                    this.setArrowCount($$0 - 1);
                }
            }
            if (($$1 = this.getStingerCount()) > 0) {
                if (this.removeStingerTime <= 0) {
                    this.removeStingerTime = 20 * (30 - $$1);
                }
                --this.removeStingerTime;
                if (this.removeStingerTime <= 0) {
                    this.setStingerCount($$1 - 1);
                }
            }
            this.detectEquipmentUpdates();
            if (this.tickCount % 20 == 0) {
                this.getCombatTracker().recheckStatus();
            }
            if (!(!this.isSleeping() || this.canInteractWithLevel() && this.checkBedExists())) {
                this.stopSleeping();
            }
        }
        if (!this.isRemoved()) {
            this.aiStep();
        }
        double $$2 = this.getX() - this.xo;
        double $$3 = this.getZ() - this.zo;
        float $$4 = (float)($$2 * $$2 + $$3 * $$3);
        float $$5 = this.yBodyRot;
        if ($$4 > 0.0025000002f) {
            float $$6 = (float)Mth.atan2($$3, $$2) * 57.295776f - 90.0f;
            float $$7 = Mth.abs(Mth.wrapDegrees(this.getYRot()) - $$6);
            $$5 = 95.0f < $$7 && $$7 < 265.0f ? $$6 - 180.0f : $$6;
        }
        if (this.attackAnim > 0.0f) {
            $$5 = this.getYRot();
        }
        ProfilerFiller $$8 = Profiler.get();
        $$8.push("headTurn");
        this.tickHeadTurn($$5);
        $$8.pop();
        $$8.push("rangeChecks");
        while (this.getYRot() - this.yRotO < -180.0f) {
            this.yRotO -= 360.0f;
        }
        while (this.getYRot() - this.yRotO >= 180.0f) {
            this.yRotO += 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO < -180.0f) {
            this.yBodyRotO -= 360.0f;
        }
        while (this.yBodyRot - this.yBodyRotO >= 180.0f) {
            this.yBodyRotO += 360.0f;
        }
        while (this.getXRot() - this.xRotO < -180.0f) {
            this.xRotO -= 360.0f;
        }
        while (this.getXRot() - this.xRotO >= 180.0f) {
            this.xRotO += 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO < -180.0f) {
            this.yHeadRotO -= 360.0f;
        }
        while (this.yHeadRot - this.yHeadRotO >= 180.0f) {
            this.yHeadRotO += 360.0f;
        }
        $$8.pop();
        this.fallFlyTicks = this.isFallFlying() ? ++this.fallFlyTicks : 0;
        if (this.isSleeping()) {
            this.setXRot(0.0f);
        }
        this.refreshDirtyAttributes();
        this.elytraAnimationState.tick();
    }

    public boolean wasRecentlyStabbed(Entity $$0, int $$1) {
        if (this.recentKineticEnemies == null) {
            return false;
        }
        if (this.recentKineticEnemies.containsKey((Object)$$0)) {
            return this.level().getGameTime() - this.recentKineticEnemies.getLong((Object)$$0) < (long)$$1;
        }
        return false;
    }

    public void rememberStabbedEntity(Entity $$0) {
        if (this.recentKineticEnemies != null) {
            this.recentKineticEnemies.put((Object)$$0, this.level().getGameTime());
        }
    }

    public int stabbedEntities(Predicate<Entity> $$0) {
        if (this.recentKineticEnemies == null) {
            return 0;
        }
        return (int)this.recentKineticEnemies.keySet().stream().filter($$0).count();
    }

    /*
     * WARNING - void declaration
     */
    public boolean stabAttack(EquipmentSlot $$0, Entity $$1, float $$2, boolean $$3, boolean $$4, boolean $$5) {
        void $$7;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel $$6 = (ServerLevel)level;
        ItemStack $$8 = this.getItemBySlot($$0);
        DamageSource $$9 = $$8.getDamageSource(this, () -> this.damageSources().mobAttack(this));
        float $$10 = EnchantmentHelper.modifyDamage((ServerLevel)$$7, $$8, $$1, $$9, $$2);
        Vec3 $$11 = $$1.getDeltaMovement();
        boolean $$12 = $$4;
        boolean $$13 = $$3 && $$1.hurtServer((ServerLevel)$$7, $$9, $$10);
        $$12 |= $$13;
        if ($$4) {
            this.causeExtraKnockback($$1, 0.4f + this.getKnockback($$1, $$9), $$11);
        }
        if ($$5 && $$1.isPassenger()) {
            $$12 = true;
            $$1.stopRiding();
        }
        if ($$1 instanceof LivingEntity) {
            LivingEntity $$14 = (LivingEntity)$$1;
            $$8.hurtEnemy($$14, this);
        }
        if ($$13) {
            EnchantmentHelper.doPostAttackEffects((ServerLevel)$$7, $$1, $$9);
        }
        if (!$$12) {
            return false;
        }
        this.setLastHurtMob($$1);
        this.playAttackSound();
        return true;
    }

    public void onAttack() {
    }

    private void detectEquipmentUpdates() {
        Map<EquipmentSlot, ItemStack> $$0 = this.collectEquipmentChanges();
        if ($$0 != null) {
            this.handleHandSwap($$0);
            if (!$$0.isEmpty()) {
                this.handleEquipmentChanges($$0);
            }
        }
    }

    private @Nullable Map<EquipmentSlot, ItemStack> collectEquipmentChanges() {
        Map $$02 = null;
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack $$3;
            ItemStack $$2 = this.lastEquipmentItems.get(equipmentSlot);
            if (!this.equipmentHasChanged($$2, $$3 = this.getItemBySlot(equipmentSlot))) continue;
            if ($$02 == null) {
                $$02 = Maps.newEnumMap(EquipmentSlot.class);
            }
            $$02.put(equipmentSlot, $$3);
            AttributeMap $$4 = this.getAttributes();
            if ($$2.isEmpty()) continue;
            this.stopLocationBasedEffects($$2, equipmentSlot, $$4);
        }
        if ($$02 != null) {
            for (Map.Entry entry : $$02.entrySet()) {
                EquipmentSlot $$6 = (EquipmentSlot)entry.getKey();
                ItemStack $$7 = (ItemStack)entry.getValue();
                if ($$7.isEmpty() || $$7.isBroken()) continue;
                $$7.forEachModifier($$6, ($$0, $$1) -> {
                    AttributeInstance $$2 = this.attributes.getInstance((Holder<Attribute>)$$0);
                    if ($$2 != null) {
                        $$2.removeModifier($$1.id());
                        $$2.addTransientModifier((AttributeModifier)$$1);
                    }
                });
                Level level = this.level();
                if (!(level instanceof ServerLevel)) continue;
                ServerLevel $$8 = (ServerLevel)level;
                EnchantmentHelper.runLocationChangedEffects($$8, $$7, this, $$6);
            }
        }
        return $$02;
    }

    public boolean equipmentHasChanged(ItemStack $$0, ItemStack $$1) {
        return !ItemStack.matches($$1, $$0);
    }

    private void handleHandSwap(Map<EquipmentSlot, ItemStack> $$0) {
        ItemStack $$1 = $$0.get(EquipmentSlot.MAINHAND);
        ItemStack $$2 = $$0.get(EquipmentSlot.OFFHAND);
        if ($$1 != null && $$2 != null && ItemStack.matches($$1, this.lastEquipmentItems.get(EquipmentSlot.OFFHAND)) && ItemStack.matches($$2, this.lastEquipmentItems.get(EquipmentSlot.MAINHAND))) {
            ((ServerLevel)this.level()).getChunkSource().sendToTrackingPlayers(this, new ClientboundEntityEventPacket(this, 55));
            $$0.remove(EquipmentSlot.MAINHAND);
            $$0.remove(EquipmentSlot.OFFHAND);
            this.lastEquipmentItems.put(EquipmentSlot.MAINHAND, $$1.copy());
            this.lastEquipmentItems.put(EquipmentSlot.OFFHAND, $$2.copy());
        }
    }

    private void handleEquipmentChanges(Map<EquipmentSlot, ItemStack> $$0) {
        ArrayList $$12 = Lists.newArrayListWithCapacity((int)$$0.size());
        $$0.forEach(($$1, $$2) -> {
            ItemStack $$3 = $$2.copy();
            $$12.add(Pair.of((Object)$$1, (Object)$$3));
            this.lastEquipmentItems.put((EquipmentSlot)$$1, $$3);
        });
        ((ServerLevel)this.level()).getChunkSource().sendToTrackingPlayers(this, new ClientboundSetEquipmentPacket(this.getId(), $$12));
    }

    protected void tickHeadTurn(float $$0) {
        float $$1 = Mth.wrapDegrees($$0 - this.yBodyRot);
        this.yBodyRot += $$1 * 0.3f;
        float $$2 = Mth.wrapDegrees(this.getYRot() - this.yBodyRot);
        float $$3 = this.getMaxHeadRotationRelativeToBody();
        if (Math.abs($$2) > $$3) {
            this.yBodyRot += $$2 - (float)Mth.sign($$2) * $$3;
        }
    }

    protected float getMaxHeadRotationRelativeToBody() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void aiStep() {
        if (this.noJumpDelay > 0) {
            --this.noJumpDelay;
        }
        if (this.isInterpolating()) {
            this.getInterpolation().interpolate();
        } else if (!this.canSimulateMovement()) {
            this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        }
        if (this.lerpHeadSteps > 0) {
            this.lerpHeadRotationStep(this.lerpHeadSteps, this.lerpYHeadRot);
            --this.lerpHeadSteps;
        }
        this.equipment.tick(this);
        $$0 = this.getDeltaMovement();
        $$1 = $$0.x;
        $$2 = $$0.y;
        $$3 = $$0.z;
        if (this.getType().equals(EntityType.PLAYER)) {
            if ($$0.horizontalDistanceSqr() < 9.0E-6) {
                $$1 = 0.0;
                $$3 = 0.0;
            }
        } else {
            if (Math.abs($$0.x) < 0.003) {
                $$1 = 0.0;
            }
            if (Math.abs($$0.z) < 0.003) {
                $$3 = 0.0;
            }
        }
        if (Math.abs($$0.y) < 0.003) {
            $$2 = 0.0;
        }
        this.setDeltaMovement($$1, $$2, $$3);
        $$4 = Profiler.get();
        $$4.push("ai");
        this.applyInput();
        if (this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0f;
            this.zza = 0.0f;
        } else if (this.isEffectiveAi() && !this.level().isClientSide()) {
            $$4.push("newAi");
            this.serverAiStep();
            $$4.pop();
        }
        $$4.pop();
        $$4.push("jump");
        if (this.jumping && this.isAffectedByFluids()) {
            if (this.isInLava()) {
                $$5 = this.getFluidHeight(FluidTags.LAVA);
            } else {
                $$6 = this.getFluidHeight(FluidTags.WATER);
            }
            $$7 = this.isInWater() != false && $$6 > 0.0;
            $$8 = this.getFluidJumpThreshold();
            if ($$7 && (!this.onGround() || $$6 > $$8)) {
                this.jumpInLiquid(FluidTags.WATER);
            } else if (this.isInLava() && (!this.onGround() || $$6 > $$8)) {
                this.jumpInLiquid(FluidTags.LAVA);
            } else if ((this.onGround() || $$7 && $$6 <= $$8) && this.noJumpDelay == 0) {
                this.jumpFromGround();
                this.noJumpDelay = 10;
            }
        } else {
            this.noJumpDelay = 0;
        }
        $$4.pop();
        $$4.push("travel");
        if (this.isFallFlying()) {
            this.updateFallFlying();
        }
        $$9 = this.getBoundingBox();
        $$10 = new Vec3(this.xxa, this.yya, this.zza);
        if (this.hasEffect(MobEffects.SLOW_FALLING) || this.hasEffect(MobEffects.LEVITATION)) {
            this.resetFallDistance();
        }
        if (!((var12_13 = this.getControllingPassenger()) instanceof Player)) ** GOTO lbl-1000
        $$11 = (Player)var12_13;
        if (this.isAlive()) {
            this.travelRidden($$11, $$10);
        } else if (this.canSimulateMovement() && this.isEffectiveAi()) {
            this.travel($$10);
        }
        if (!this.level().isClientSide() || this.isLocalInstanceAuthoritative()) {
            this.applyEffectsFromBlocks();
        }
        if (this.level().isClientSide()) {
            this.calculateEntityAnimation(this instanceof FlyingAnimal);
        }
        $$4.pop();
        var12_13 = this.level();
        if (var12_13 instanceof ServerLevel) {
            $$12 = (ServerLevel)var12_13;
            $$4.push("freezing");
            if (!this.isInPowderSnow || !this.canFreeze()) {
                this.setTicksFrozen(Math.max(0, this.getTicksFrozen() - 2));
            }
            this.removeFrost();
            this.tryAddFrost();
            if (this.tickCount % 40 == 0 && this.isFullyFrozen() && this.canFreeze()) {
                this.hurtServer($$12, this.damageSources().freeze(), 1.0f);
            }
            $$4.pop();
        }
        $$4.push("push");
        if (this.autoSpinAttackTicks > 0) {
            --this.autoSpinAttackTicks;
            this.checkAutoSpinAttack($$9, this.getBoundingBox());
        }
        this.pushEntities();
        $$4.pop();
        var12_13 = this.level();
        if (var12_13 instanceof ServerLevel) {
            $$13 = (ServerLevel)var12_13;
            if (this.isSensitiveToWater() && this.isInWaterOrRain()) {
                this.hurtServer($$13, this.damageSources().drown(), 1.0f);
            }
        }
    }

    protected void applyInput() {
        this.xxa *= 0.98f;
        this.zza *= 0.98f;
    }

    public boolean isSensitiveToWater() {
        return false;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    protected void updateFallFlying() {
        this.checkFallDistanceAccumulation();
        if (!this.level().isClientSide()) {
            if (!this.canGlide()) {
                this.setSharedFlag(7, false);
                return;
            }
            int $$02 = this.fallFlyTicks + 1;
            if ($$02 % 10 == 0) {
                int $$1 = $$02 / 10;
                if ($$1 % 2 == 0) {
                    List<EquipmentSlot> $$2 = EquipmentSlot.VALUES.stream().filter($$0 -> LivingEntity.canGlideUsing(this.getItemBySlot((EquipmentSlot)$$0), $$0)).toList();
                    EquipmentSlot $$3 = Util.getRandom($$2, this.random);
                    this.getItemBySlot($$3).hurtAndBreak(1, this, $$3);
                }
                this.gameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
    }

    protected boolean canGlide() {
        if (this.onGround() || this.isPassenger() || this.hasEffect(MobEffects.LEVITATION)) {
            return false;
        }
        for (EquipmentSlot $$0 : EquipmentSlot.VALUES) {
            if (!LivingEntity.canGlideUsing(this.getItemBySlot($$0), $$0)) continue;
            return true;
        }
        return false;
    }

    protected void serverAiStep() {
    }

    protected void pushEntities() {
        ServerLevel $$1;
        int $$2;
        List<Entity> $$0 = this.level().getPushableEntities(this, this.getBoundingBox());
        if ($$0.isEmpty()) {
            return;
        }
        Level level = this.level();
        if (level instanceof ServerLevel && ($$2 = ($$1 = (ServerLevel)level).getGameRules().get(GameRules.MAX_ENTITY_CRAMMING).intValue()) > 0 && $$0.size() > $$2 - 1 && this.random.nextInt(4) == 0) {
            int $$3 = 0;
            for (Entity $$4 : $$0) {
                if ($$4.isPassenger()) continue;
                ++$$3;
            }
            if ($$3 > $$2 - 1) {
                this.hurtServer($$1, this.damageSources().cramming(), 6.0f);
            }
        }
        for (Entity $$5 : $$0) {
            this.doPush($$5);
        }
    }

    protected void checkAutoSpinAttack(AABB $$0, AABB $$1) {
        AABB $$2 = $$0.minmax($$1);
        List<Entity> $$3 = this.level().getEntities(this, $$2);
        if (!$$3.isEmpty()) {
            for (Entity $$4 : $$3) {
                if (!($$4 instanceof LivingEntity)) continue;
                this.doAutoAttackOnTouch((LivingEntity)$$4);
                this.autoSpinAttackTicks = 0;
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.autoSpinAttackTicks = 0;
        }
        if (!this.level().isClientSide() && this.autoSpinAttackTicks <= 0) {
            this.setLivingEntityFlag(4, false);
            this.autoSpinAttackDmg = 0.0f;
            this.autoSpinAttackItemStack = null;
        }
    }

    protected void doPush(Entity $$0) {
        $$0.push(this);
    }

    protected void doAutoAttackOnTouch(LivingEntity $$0) {
    }

    public boolean isAutoSpinAttack() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity $$0 = this.getVehicle();
        super.stopRiding();
        if ($$0 != null && $$0 != this.getVehicle() && !this.level().isClientSide()) {
            this.dismountVehicle($$0);
        }
    }

    @Override
    public void rideTick() {
        super.rideTick();
        this.resetFallDistance();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    @Override
    public void lerpHeadTo(float $$0, int $$1) {
        this.lerpYHeadRot = $$0;
        this.lerpHeadSteps = $$1;
    }

    public void setJumping(boolean $$0) {
        this.jumping = $$0;
    }

    public void onItemPickup(ItemEntity $$0) {
        Entity $$1 = $$0.getOwner();
        if ($$1 instanceof ServerPlayer) {
            CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayer)$$1, $$0.getItem(), this);
        }
    }

    public void take(Entity $$0, int $$1) {
        if (!$$0.isRemoved() && !this.level().isClientSide() && ($$0 instanceof ItemEntity || $$0 instanceof AbstractArrow || $$0 instanceof ExperienceOrb)) {
            ((ServerLevel)this.level()).getChunkSource().sendToTrackingPlayers($$0, new ClientboundTakeItemEntityPacket($$0.getId(), this.getId(), $$1));
        }
    }

    public boolean hasLineOfSight(Entity $$0) {
        return this.hasLineOfSight($$0, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, $$0.getEyeY());
    }

    public boolean hasLineOfSight(Entity $$0, ClipContext.Block $$1, ClipContext.Fluid $$2, double $$3) {
        if ($$0.level() != this.level()) {
            return false;
        }
        Vec3 $$4 = new Vec3(this.getX(), this.getEyeY(), this.getZ());
        Vec3 $$5 = new Vec3($$0.getX(), $$3, $$0.getZ());
        if ($$5.distanceTo($$4) > 128.0) {
            return false;
        }
        return this.level().clip(new ClipContext($$4, $$5, $$1, $$2, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getViewYRot(float $$0) {
        if ($$0 == 1.0f) {
            return this.yHeadRot;
        }
        return Mth.rotLerp($$0, this.yHeadRotO, this.yHeadRot);
    }

    public float getAttackAnim(float $$0) {
        float $$1 = this.attackAnim - this.oAttackAnim;
        if ($$1 < 0.0f) {
            $$1 += 1.0f;
        }
        return this.oAttackAnim + $$1 * $$0;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.onClimbable();
    }

    @Override
    public float getYHeadRot() {
        return this.yHeadRot;
    }

    @Override
    public void setYHeadRot(float $$0) {
        this.yHeadRot = $$0;
    }

    @Override
    public void setYBodyRot(float $$0) {
        this.yBodyRot = $$0;
    }

    @Override
    public Vec3 getRelativePortalPosition(Direction.Axis $$0, BlockUtil.FoundRectangle $$1) {
        return LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition($$0, $$1));
    }

    public static Vec3 resetForwardDirectionOfRelativePortalPosition(Vec3 $$0) {
        return new Vec3($$0.x, $$0.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float $$0) {
        this.internalSetAbsorptionAmount(Mth.clamp($$0, 0.0f, this.getMaxAbsorption()));
    }

    protected void internalSetAbsorptionAmount(float $$0) {
        this.absorptionAmount = $$0;
    }

    public void onEnterCombat() {
    }

    public void onLeaveCombat() {
    }

    protected void updateEffectVisibility() {
        this.effectsDirty = true;
    }

    public abstract HumanoidArm getMainArm();

    public boolean isUsingItem() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 1) > 0;
    }

    public InteractionHand getUsedItemHand() {
        return (this.entityData.get(DATA_LIVING_ENTITY_FLAGS) & 2) > 0 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private void updatingUsingItem() {
        if (this.isUsingItem()) {
            if (ItemStack.isSameItem(this.getItemInHand(this.getUsedItemHand()), this.useItem)) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                this.updateUsingItem(this.useItem);
            } else {
                this.stopUsingItem();
            }
        }
    }

    private @Nullable ItemEntity createItemStackToDrop(ItemStack $$0, boolean $$1, boolean $$2) {
        if ($$0.isEmpty()) {
            return null;
        }
        double $$3 = this.getEyeY() - (double)0.3f;
        ItemEntity $$4 = new ItemEntity(this.level(), this.getX(), $$3, this.getZ(), $$0);
        $$4.setPickUpDelay(40);
        if ($$2) {
            $$4.setThrower(this);
        }
        if ($$1) {
            float $$5 = this.random.nextFloat() * 0.5f;
            float $$6 = this.random.nextFloat() * ((float)Math.PI * 2);
            $$4.setDeltaMovement(-Mth.sin($$6) * $$5, 0.2f, Mth.cos($$6) * $$5);
        } else {
            float $$7 = 0.3f;
            float $$8 = Mth.sin(this.getXRot() * ((float)Math.PI / 180));
            float $$9 = Mth.cos(this.getXRot() * ((float)Math.PI / 180));
            float $$10 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float $$11 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            float $$12 = this.random.nextFloat() * ((float)Math.PI * 2);
            float $$13 = 0.02f * this.random.nextFloat();
            $$4.setDeltaMovement((double)(-$$10 * $$9 * 0.3f) + Math.cos($$12) * (double)$$13, -$$8 * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)($$11 * $$9 * 0.3f) + Math.sin($$12) * (double)$$13);
        }
        return $$4;
    }

    protected void updateUsingItem(ItemStack $$0) {
        $$0.onUseTick(this.level(), this, this.getUseItemRemainingTicks());
        if (--this.useItemRemaining == 0 && !this.level().isClientSide() && !$$0.useOnRelease()) {
            this.completeUsingItem();
        }
    }

    private void updateSwimAmount() {
        this.swimAmountO = this.swimAmount;
        this.swimAmount = this.isVisuallySwimming() ? Math.min(1.0f, this.swimAmount + 0.09f) : Math.max(0.0f, this.swimAmount - 0.09f);
    }

    protected void setLivingEntityFlag(int $$0, boolean $$1) {
        int $$2 = this.entityData.get(DATA_LIVING_ENTITY_FLAGS).byteValue();
        $$2 = $$1 ? ($$2 |= $$0) : ($$2 &= ~$$0);
        this.entityData.set(DATA_LIVING_ENTITY_FLAGS, (byte)$$2);
    }

    public void startUsingItem(InteractionHand $$0) {
        ItemStack $$1 = this.getItemInHand($$0);
        if ($$1.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.useItem = $$1;
        this.useItemRemaining = $$1.getUseDuration(this);
        if (!this.level().isClientSide()) {
            this.setLivingEntityFlag(1, true);
            this.setLivingEntityFlag(2, $$0 == InteractionHand.OFF_HAND);
            this.useItem.causeUseVibration(this, GameEvent.ITEM_INTERACT_START);
            if (this.useItem.has(DataComponents.KINETIC_WEAPON)) {
                this.recentKineticEnemies = new Object2LongOpenHashMap();
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (SLEEPING_POS_ID.equals($$0)) {
            if (this.level().isClientSide()) {
                this.getSleepingPos().ifPresent(this::setPosToBed);
            }
        } else if (DATA_LIVING_ENTITY_FLAGS.equals($$0) && this.level().isClientSide()) {
            if (this.isUsingItem() && this.useItem.isEmpty()) {
                this.useItem = this.getItemInHand(this.getUsedItemHand());
                if (!this.useItem.isEmpty()) {
                    this.useItemRemaining = this.useItem.getUseDuration(this);
                }
            } else if (!this.isUsingItem() && !this.useItem.isEmpty()) {
                this.useItem = ItemStack.EMPTY;
                this.useItemRemaining = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgument.Anchor $$0, Vec3 $$1) {
        super.lookAt($$0, $$1);
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot = this.yHeadRot;
    }

    @Override
    public float getPreciseBodyRotation(float $$0) {
        return Mth.lerp($$0, this.yBodyRotO, this.yBodyRot);
    }

    public void spawnItemParticles(ItemStack $$0, int $$1) {
        for (int $$2 = 0; $$2 < $$1; ++$$2) {
            Vec3 $$3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, (double)this.random.nextFloat() * 0.1 + 0.1, 0.0);
            $$3 = $$3.xRot(-this.getXRot() * ((float)Math.PI / 180));
            $$3 = $$3.yRot(-this.getYRot() * ((float)Math.PI / 180));
            double $$4 = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3 $$5 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.3, $$4, 0.6);
            $$5 = $$5.xRot(-this.getXRot() * ((float)Math.PI / 180));
            $$5 = $$5.yRot(-this.getYRot() * ((float)Math.PI / 180));
            $$5 = $$5.add(this.getX(), this.getEyeY(), this.getZ());
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, $$0), $$5.x, $$5.y, $$5.z, $$3.x, $$3.y + 0.05, $$3.z);
        }
    }

    protected void completeUsingItem() {
        if (this.level().isClientSide() && !this.isUsingItem()) {
            return;
        }
        InteractionHand $$0 = this.getUsedItemHand();
        if (!this.useItem.equals(this.getItemInHand($$0))) {
            this.releaseUsingItem();
            return;
        }
        if (!this.useItem.isEmpty() && this.isUsingItem()) {
            ItemStack $$1 = this.useItem.finishUsingItem(this.level(), this);
            if ($$1 != this.useItem) {
                this.setItemInHand($$0, $$1);
            }
            this.stopUsingItem();
        }
    }

    public void handleExtraItemsCreatedOnUse(ItemStack $$0) {
    }

    public ItemStack getUseItem() {
        return this.useItem;
    }

    public int getUseItemRemainingTicks() {
        return this.useItemRemaining;
    }

    public int getTicksUsingItem() {
        if (this.isUsingItem()) {
            return this.useItem.getUseDuration(this) - this.getUseItemRemainingTicks();
        }
        return 0;
    }

    public float getTicksUsingItem(float $$0) {
        if (!this.isUsingItem()) {
            return 0.0f;
        }
        return (float)this.getTicksUsingItem() + $$0;
    }

    public void releaseUsingItem() {
        ItemStack $$0 = this.getItemInHand(this.getUsedItemHand());
        if (!this.useItem.isEmpty() && ItemStack.isSameItem($$0, this.useItem)) {
            this.useItem = $$0;
            this.useItem.releaseUsing(this.level(), this, this.getUseItemRemainingTicks());
            if (this.useItem.useOnRelease()) {
                this.updatingUsingItem();
            }
        }
        this.stopUsingItem();
    }

    public void stopUsingItem() {
        if (!this.level().isClientSide()) {
            boolean $$0 = this.isUsingItem();
            this.recentKineticEnemies = null;
            this.setLivingEntityFlag(1, false);
            if ($$0) {
                this.useItem.causeUseVibration(this, GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.useItem = ItemStack.EMPTY;
        this.useItemRemaining = 0;
    }

    public boolean isBlocking() {
        return this.getItemBlockingWith() != null;
    }

    public @Nullable ItemStack getItemBlockingWith() {
        int $$1;
        if (!this.isUsingItem()) {
            return null;
        }
        BlocksAttacks $$0 = this.useItem.get(DataComponents.BLOCKS_ATTACKS);
        if ($$0 != null && ($$1 = this.useItem.getItem().getUseDuration(this.useItem, this) - this.useItemRemaining) >= $$0.blockDelayTicks()) {
            return this.useItem;
        }
        return null;
    }

    public boolean isSuppressingSlidingDownLadder() {
        return this.isShiftKeyDown();
    }

    public boolean isFallFlying() {
        return this.getSharedFlag(7);
    }

    @Override
    public boolean isVisuallySwimming() {
        return super.isVisuallySwimming() || !this.isFallFlying() && this.hasPose(Pose.FALL_FLYING);
    }

    public int getFallFlyingTicks() {
        return this.fallFlyTicks;
    }

    public boolean randomTeleport(double $$0, double $$1, double $$2, boolean $$3) {
        LivingEntity livingEntity;
        double $$4 = this.getX();
        double $$5 = this.getY();
        double $$6 = this.getZ();
        double $$7 = $$1;
        boolean $$8 = false;
        BlockPos $$9 = BlockPos.containing($$0, $$7, $$2);
        Level $$10 = this.level();
        if ($$10.hasChunkAt($$9)) {
            boolean $$11 = false;
            while (!$$11 && $$9.getY() > $$10.getMinY()) {
                BlockPos $$12 = $$9.below();
                BlockState $$13 = $$10.getBlockState($$12);
                if ($$13.blocksMotion()) {
                    $$11 = true;
                    continue;
                }
                $$7 -= 1.0;
                $$9 = $$12;
            }
            if ($$11) {
                this.teleportTo($$0, $$7, $$2);
                if ($$10.noCollision(this) && !$$10.containsAnyLiquid(this.getBoundingBox())) {
                    $$8 = true;
                }
            }
        }
        if (!$$8) {
            this.teleportTo($$4, $$5, $$6);
            return false;
        }
        if ($$3) {
            $$10.broadcastEntityEvent(this, (byte)46);
        }
        if ((livingEntity = this) instanceof PathfinderMob) {
            PathfinderMob $$14 = (PathfinderMob)livingEntity;
            $$14.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedByPotions() {
        return !this.isDeadOrDying();
    }

    public boolean attackable() {
        return true;
    }

    public void setRecordPlayingNearby(BlockPos $$0, boolean $$1) {
    }

    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(Pose $$0) {
        return $$0 == Pose.SLEEPING ? SLEEPING_DIMENSIONS : this.getDefaultDimensions($$0).scale(this.getScale());
    }

    protected EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.getType().getDimensions().scale(this.getAgeScale());
    }

    public ImmutableList<Pose> getDismountPoses() {
        return ImmutableList.of((Object)Pose.STANDING);
    }

    public AABB getLocalBoundsForPose(Pose $$0) {
        EntityDimensions $$1 = this.getDimensions($$0);
        return new AABB(-$$1.width() / 2.0f, 0.0, -$$1.width() / 2.0f, $$1.width() / 2.0f, $$1.height(), $$1.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateAtTargetPose(Pose $$0) {
        AABB $$1 = this.getDimensions($$0).makeBoundingBox(this.position());
        return this.level().noBlockCollision(this, $$1);
    }

    @Override
    public boolean canUsePortal(boolean $$0) {
        return super.canUsePortal($$0) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPos() {
        return this.entityData.get(SLEEPING_POS_ID);
    }

    public void setSleepingPos(BlockPos $$0) {
        this.entityData.set(SLEEPING_POS_ID, Optional.of($$0));
    }

    public void clearSleepingPos() {
        this.entityData.set(SLEEPING_POS_ID, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPos().isPresent();
    }

    public void startSleeping(BlockPos $$0) {
        BlockState $$1;
        if (this.isPassenger()) {
            this.stopRiding();
        }
        if (($$1 = this.level().getBlockState($$0)).getBlock() instanceof BedBlock) {
            this.level().setBlock($$0, (BlockState)$$1.setValue(BedBlock.OCCUPIED, true), 3);
        }
        this.setPose(Pose.SLEEPING);
        this.setPosToBed($$0);
        this.setSleepingPos($$0);
        this.setDeltaMovement(Vec3.ZERO);
        this.needsSync = true;
    }

    private void setPosToBed(BlockPos $$0) {
        this.setPos((double)$$0.getX() + 0.5, (double)$$0.getY() + 0.6875, (double)$$0.getZ() + 0.5);
    }

    private boolean checkBedExists() {
        return this.getSleepingPos().map($$0 -> this.level().getBlockState((BlockPos)$$0).getBlock() instanceof BedBlock).orElse(false);
    }

    public void stopSleeping() {
        this.getSleepingPos().filter(this.level()::hasChunkAt).ifPresent($$0 -> {
            BlockState $$1 = this.level().getBlockState((BlockPos)$$0);
            if ($$1.getBlock() instanceof BedBlock) {
                Direction $$2 = (Direction)$$1.getValue(BedBlock.FACING);
                this.level().setBlock((BlockPos)$$0, (BlockState)$$1.setValue(BedBlock.OCCUPIED, false), 3);
                Vec3 $$3 = BedBlock.findStandUpPosition(this.getType(), this.level(), $$0, $$2, this.getYRot()).orElseGet(() -> {
                    BlockPos $$1 = $$0.above();
                    return new Vec3((double)$$1.getX() + 0.5, (double)$$1.getY() + 0.1, (double)$$1.getZ() + 0.5);
                });
                Vec3 $$4 = Vec3.atBottomCenterOf($$0).subtract($$3).normalize();
                float $$5 = (float)Mth.wrapDegrees(Mth.atan2($$4.z, $$4.x) * 57.2957763671875 - 90.0);
                this.setPos($$3.x, $$3.y, $$3.z);
                this.setYRot($$5);
                this.setXRot(0.0f);
            }
        });
        Vec3 $$02 = this.position();
        this.setPose(Pose.STANDING);
        this.setPos($$02.x, $$02.y, $$02.z);
        this.clearSleepingPos();
    }

    public @Nullable Direction getBedOrientation() {
        BlockPos $$0 = this.getSleepingPos().orElse(null);
        return $$0 != null ? BedBlock.getBedOrientation(this.level(), $$0) : null;
    }

    @Override
    public boolean isInWall() {
        return !this.isSleeping() && super.isInWall();
    }

    public ItemStack getProjectile(ItemStack $$0) {
        return ItemStack.EMPTY;
    }

    private static byte entityEventForEquipmentBreak(EquipmentSlot $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
            case EquipmentSlot.SADDLE -> 68;
        };
    }

    public void onEquippedItemBroken(Item $$0, EquipmentSlot $$1) {
        this.level().broadcastEntityEvent(this, LivingEntity.entityEventForEquipmentBreak($$1));
        this.stopLocationBasedEffects(this.getItemBySlot($$1), $$1, this.attributes);
    }

    private void stopLocationBasedEffects(ItemStack $$0, EquipmentSlot $$12, AttributeMap $$22) {
        $$0.forEachModifier($$12, ($$1, $$2) -> {
            AttributeInstance $$3 = $$22.getInstance((Holder<Attribute>)$$1);
            if ($$3 != null) {
                $$3.removeModifier((AttributeModifier)$$2);
            }
        });
        EnchantmentHelper.stopLocationBasedEffects($$0, this, $$12);
    }

    public final boolean canEquipWithDispenser(ItemStack $$0) {
        if (!this.isAlive() || this.isSpectator()) {
            return false;
        }
        Equippable $$1 = $$0.get(DataComponents.EQUIPPABLE);
        if ($$1 == null || !$$1.dispensable()) {
            return false;
        }
        EquipmentSlot $$2 = $$1.slot();
        if (!this.canUseSlot($$2) || !$$1.canBeEquippedBy(this.getType())) {
            return false;
        }
        return this.getItemBySlot($$2).isEmpty() && this.canDispenserEquipIntoSlot($$2);
    }

    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return true;
    }

    public final EquipmentSlot getEquipmentSlotForItem(ItemStack $$0) {
        Equippable $$1 = $$0.get(DataComponents.EQUIPPABLE);
        if ($$1 != null && this.canUseSlot($$1.slot())) {
            return $$1.slot();
        }
        return EquipmentSlot.MAINHAND;
    }

    public final boolean isEquippableInSlot(ItemStack $$0, EquipmentSlot $$1) {
        Equippable $$2 = $$0.get(DataComponents.EQUIPPABLE);
        if ($$2 == null) {
            return $$1 == EquipmentSlot.MAINHAND && this.canUseSlot(EquipmentSlot.MAINHAND);
        }
        return $$1 == $$2.slot() && this.canUseSlot($$2.slot()) && $$2.canBeEquippedBy(this.getType());
    }

    private static SlotAccess createEquipmentSlotAccess(LivingEntity $$0, EquipmentSlot $$1) {
        if ($$1 == EquipmentSlot.HEAD || $$1 == EquipmentSlot.MAINHAND || $$1 == EquipmentSlot.OFFHAND) {
            return SlotAccess.forEquipmentSlot($$0, $$1);
        }
        return SlotAccess.forEquipmentSlot($$0, $$1, $$2 -> $$2.isEmpty() || $$0.getEquipmentSlotForItem((ItemStack)$$2) == $$1);
    }

    private static @Nullable EquipmentSlot getEquipmentSlot(int $$0) {
        if ($$0 == 100 + EquipmentSlot.HEAD.getIndex()) {
            return EquipmentSlot.HEAD;
        }
        if ($$0 == 100 + EquipmentSlot.CHEST.getIndex()) {
            return EquipmentSlot.CHEST;
        }
        if ($$0 == 100 + EquipmentSlot.LEGS.getIndex()) {
            return EquipmentSlot.LEGS;
        }
        if ($$0 == 100 + EquipmentSlot.FEET.getIndex()) {
            return EquipmentSlot.FEET;
        }
        if ($$0 == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if ($$0 == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if ($$0 == 105) {
            return EquipmentSlot.BODY;
        }
        if ($$0 == 106) {
            return EquipmentSlot.SADDLE;
        }
        return null;
    }

    @Override
    public @Nullable SlotAccess getSlot(int $$0) {
        EquipmentSlot $$1 = LivingEntity.getEquipmentSlot($$0);
        if ($$1 != null) {
            return LivingEntity.createEquipmentSlotAccess(this, $$1);
        }
        return super.getSlot($$0);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        for (EquipmentSlot $$0 : EquipmentSlotGroup.ARMOR) {
            if (!this.getItemBySlot($$0).is(ItemTags.FREEZE_IMMUNE_WEARABLES)) continue;
            return false;
        }
        return super.canFreeze();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return !this.level().isClientSide() && this.hasEffect(MobEffects.GLOWING) || super.isCurrentlyGlowing();
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return this.yBodyRot;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
        double $$1 = $$0.getX();
        double $$2 = $$0.getY();
        double $$3 = $$0.getZ();
        float $$4 = $$0.getYRot();
        float $$5 = $$0.getXRot();
        this.syncPacketPositionCodec($$1, $$2, $$3);
        this.yBodyRot = $$0.getYHeadRot();
        this.yHeadRot = $$0.getYHeadRot();
        this.yBodyRotO = this.yBodyRot;
        this.yHeadRotO = this.yHeadRot;
        this.setId($$0.getId());
        this.setUUID($$0.getUUID());
        this.absSnapTo($$1, $$2, $$3, $$4, $$5);
        this.setDeltaMovement($$0.getMovement());
    }

    public float getSecondsToDisableBlocking() {
        ItemStack $$0 = this.getWeaponItem();
        Weapon $$1 = $$0.get(DataComponents.WEAPON);
        return $$1 != null && $$0 == this.getActiveItem() ? $$1.disableBlockingForSeconds() : 0.0f;
    }

    @Override
    public float maxUpStep() {
        float $$0 = (float)this.getAttributeValue(Attributes.STEP_HEIGHT);
        return this.getControllingPassenger() instanceof Player ? Math.max($$0, 1.0f) : $$0;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity $$0) {
        return this.position().add(this.getPassengerAttachmentPoint($$0, this.getDimensions(this.getPose()), this.getScale() * this.getAgeScale()));
    }

    protected void lerpHeadRotationStep(int $$0, double $$1) {
        this.yHeadRot = (float)Mth.rotLerp(1.0 / (double)$$0, (double)this.yHeadRot, $$1);
    }

    @Override
    public void igniteForTicks(int $$0) {
        super.igniteForTicks(Mth.ceil((double)$$0 * this.getAttributeValue(Attributes.BURNING_TIME)));
    }

    public boolean hasInfiniteMaterials() {
        return false;
    }

    public boolean isInvulnerableTo(ServerLevel $$0, DamageSource $$1) {
        return this.isInvulnerableToBase($$1) || EnchantmentHelper.isImmuneToDamage($$0, this, $$1);
    }

    public static boolean canGlideUsing(ItemStack $$0, EquipmentSlot $$1) {
        if (!$$0.has(DataComponents.GLIDER)) {
            return false;
        }
        Equippable $$2 = $$0.get(DataComponents.EQUIPPABLE);
        return $$2 != null && $$1 == $$2.slot() && !$$0.nextDamageWillBreak();
    }

    @VisibleForTesting
    public int getLastHurtByPlayerMemoryTime() {
        return this.lastHurtByPlayerMemoryTime;
    }

    @Override
    public boolean isTransmittingWaypoint() {
        return this.getAttributeValue(Attributes.WAYPOINT_TRANSMIT_RANGE) > 0.0;
    }

    @Override
    public Optional<WaypointTransmitter.Connection> makeWaypointConnectionWith(ServerPlayer $$0) {
        if (this.firstTick || $$0 == this) {
            return Optional.empty();
        }
        if (WaypointTransmitter.doesSourceIgnoreReceiver(this, $$0)) {
            return Optional.empty();
        }
        Waypoint.Icon $$1 = this.locatorBarIcon.cloneAndAssignStyle(this);
        if (WaypointTransmitter.isReallyFar(this, $$0)) {
            return Optional.of(new WaypointTransmitter.EntityAzimuthConnection(this, $$1, $$0));
        }
        if (!WaypointTransmitter.isChunkVisible(this.chunkPosition(), $$0)) {
            return Optional.of(new WaypointTransmitter.EntityChunkConnection(this, $$1, $$0));
        }
        return Optional.of(new WaypointTransmitter.EntityBlockConnection(this, $$1, $$0));
    }

    @Override
    public Waypoint.Icon waypointIcon() {
        return this.locatorBarIcon;
    }

    public record Fallsounds(SoundEvent small, SoundEvent big) {
    }
}

