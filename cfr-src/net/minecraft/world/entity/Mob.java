/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.debug.DebugBrainDump;
import net.minecraft.util.debug.DebugGoalInfo;
import net.minecraft.util.debug.DebugPathInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.EquipmentUser;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jspecify.annotations.Nullable;

public abstract class Mob
extends LivingEntity
implements EquipmentUser,
Leashable,
Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    private static final List<EquipmentSlot> EQUIPMENT_POPULATION_ORDER = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15f;
    public static final float WEARING_ARMOR_UPGRADE_MATERIAL_CHANCE = 0.1087f;
    public static final float WEARING_ARMOR_UPGRADE_MATERIAL_ATTEMPTS = 3.0f;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25f;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04f) - (double)0.6f;
    private static final boolean DEFAULT_CAN_PICK_UP_LOOT = false;
    private static final boolean DEFAULT_PERSISTENCE_REQUIRED = false;
    private static final boolean DEFAULT_LEFT_HANDED = false;
    private static final boolean DEFAULT_NO_AI = false;
    protected static final Identifier RANDOM_SPAWN_BONUS_ID = Identifier.withDefaultNamespace("random_spawn_bonus");
    public static final String TAG_DROP_CHANCES = "drop_chances";
    public static final String TAG_LEFT_HANDED = "LeftHanded";
    public static final String TAG_CAN_PICK_UP_LOOT = "CanPickUpLoot";
    public static final String TAG_NO_AI = "NoAI";
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    private @Nullable LivingEntity target;
    private final Sensing sensing;
    private DropChances dropChances = DropChances.DEFAULT;
    private boolean canPickUpLoot = false;
    private boolean persistenceRequired = false;
    private final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
    private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
    private long lootTableSeed;
    private @Nullable Leashable.LeashData leashData;
    private BlockPos homePosition = BlockPos.ZERO;
    private int homeRadius = -1;

    protected Mob(EntityType<? extends Mob> $$0, Level $$1) {
        super((EntityType<? extends LivingEntity>)$$0, $$1);
        this.goalSelector = new GoalSelector();
        this.targetSelector = new GoalSelector();
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation($$1);
        this.sensing = new Sensing(this);
        if ($$1 instanceof ServerLevel) {
            this.registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    protected PathNavigation createNavigation(Level $$0) {
        return new GroundPathNavigation(this, $$0);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(PathType $$0) {
        Mob $$3;
        Mob $$1;
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob && ($$1 = (Mob)entity).shouldPassengersInheritMalus()) {
            Mob $$2 = $$1;
        } else {
            $$3 = this;
        }
        Float $$4 = $$3.pathfindingMalus.get((Object)$$0);
        return $$4 == null ? $$0.getMalus() : $$4.floatValue();
    }

    public void setPathfindingMalus(PathType $$0, float $$1) {
        this.pathfindingMalus.put($$0, Float.valueOf($$1));
    }

    public void onPathfindingStart() {
    }

    public void onPathfindingDone() {
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob $$0 = (Mob)entity;
            return $$0.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob $$0 = (Mob)entity;
            return $$0.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity $$0 = this.getFirstPassenger();
        if (this.isNoAi()) return null;
        if (!($$0 instanceof Mob)) return null;
        Mob $$1 = (Mob)$$0;
        if (!$$0.canControlVehicle()) return null;
        Mob mob = $$1;
        return mob;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    protected final @Nullable LivingEntity getTargetFromBrain() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity $$0) {
        this.target = $$0;
    }

    @Override
    public boolean canAttackType(EntityType<?> $$0) {
        return $$0 != EntityType.GHAST;
    }

    public boolean canUseNonMeleeWeapon(ItemStack $$0) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        this.makeSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        ProfilerFiller $$0 = Profiler.get();
        $$0.push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }
        $$0.pop();
    }

    @Override
    protected void playHurtSound(DamageSource $$0) {
        this.resetAmbientSoundTime();
        super.playHurtSound($$0);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel $$0) {
        if (this.xpReward > 0) {
            int $$1 = this.xpReward;
            for (EquipmentSlot $$2 : EquipmentSlot.VALUES) {
                ItemStack $$3;
                if (!$$2.canIncreaseExperience() || ($$3 = this.getItemBySlot($$2)).isEmpty() || !(this.dropChances.byEquipment($$2) <= 1.0f)) continue;
                $$1 += 1 + this.random.nextInt(3);
            }
            return $$1;
        }
        return this.xpReward;
    }

    public void spawnAnim() {
        if (this.level().isClientSide()) {
            this.makePoofParticles();
        } else {
            this.level().broadcastEntityEvent(this, (byte)20);
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent($$0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
            this.updateControlFlags();
        }
    }

    protected void updateControlFlags() {
        boolean $$0 = !(this.getControllingPassenger() instanceof Mob);
        boolean $$1 = !(this.getVehicle() instanceof AbstractBoat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, $$0);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, $$0 && $$1);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, $$0);
    }

    @Override
    protected void tickHeadTurn(float $$0) {
        this.bodyRotationControl.clientTick();
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean(TAG_CAN_PICK_UP_LOOT, this.canPickUpLoot());
        $$0.putBoolean("PersistenceRequired", this.persistenceRequired);
        if (!this.dropChances.equals(DropChances.DEFAULT)) {
            $$0.store(TAG_DROP_CHANCES, DropChances.CODEC, this.dropChances);
        }
        this.writeLeashData($$0, this.leashData);
        if (this.hasHome()) {
            $$0.putInt("home_radius", this.homeRadius);
            $$0.store("home_pos", BlockPos.CODEC, this.homePosition);
        }
        $$0.putBoolean(TAG_LEFT_HANDED, this.isLeftHanded());
        this.lootTable.ifPresent($$1 -> $$0.store("DeathLootTable", LootTable.KEY_CODEC, $$1));
        if (this.lootTableSeed != 0L) {
            $$0.putLong("DeathLootTableSeed", this.lootTableSeed);
        }
        if (this.isNoAi()) {
            $$0.putBoolean(TAG_NO_AI, this.isNoAi());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setCanPickUpLoot($$0.getBooleanOr(TAG_CAN_PICK_UP_LOOT, false));
        this.persistenceRequired = $$0.getBooleanOr("PersistenceRequired", false);
        this.dropChances = $$0.read(TAG_DROP_CHANCES, DropChances.CODEC).orElse(DropChances.DEFAULT);
        this.readLeashData($$0);
        this.homeRadius = $$0.getIntOr("home_radius", -1);
        if (this.homeRadius >= 0) {
            this.homePosition = $$0.read("home_pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        }
        this.setLeftHanded($$0.getBooleanOr(TAG_LEFT_HANDED, false));
        this.lootTable = $$0.read("DeathLootTable", LootTable.KEY_CODEC);
        this.lootTableSeed = $$0.getLongOr("DeathLootTableSeed", 0L);
        this.setNoAi($$0.getBooleanOr(TAG_NO_AI, false));
    }

    @Override
    protected void dropFromLootTable(ServerLevel $$0, DamageSource $$1, boolean $$2) {
        super.dropFromLootTable($$0, $$1, $$2);
        this.lootTable = Optional.empty();
    }

    @Override
    public final Optional<ResourceKey<LootTable>> getLootTable() {
        if (this.lootTable.isPresent()) {
            return this.lootTable;
        }
        return super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setZza(float $$0) {
        this.zza = $$0;
    }

    public void setYya(float $$0) {
        this.yya = $$0;
    }

    public void setXxa(float $$0) {
        this.xxa = $$0;
    }

    @Override
    public void setSpeed(float $$0) {
        super.setSpeed($$0);
        this.setZza($$0);
    }

    public void stopInPlace() {
        this.getNavigation().stop();
        this.setXxa(0.0f);
        this.setYya(0.0f);
        this.setSpeed(0.0f);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.resetAngularLeashMomentum();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getType().is(EntityTypeTags.BURN_IN_DAYLIGHT)) {
            this.burnUndead();
        }
        ProfilerFiller $$0 = Profiler.get();
        $$0.push("looting");
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$1 = (ServerLevel)level;
            if (this.canPickUpLoot() && this.isAlive() && !this.dead && $$1.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                Vec3i $$2 = this.getPickupReach();
                List<ItemEntity> $$3 = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate($$2.getX(), $$2.getY(), $$2.getZ()));
                for (ItemEntity $$4 : $$3) {
                    if ($$4.isRemoved() || $$4.getItem().isEmpty() || $$4.hasPickUpDelay() || !this.wantsToPickUp($$1, $$4.getItem())) continue;
                    this.pickUpItem($$1, $$4);
                }
            }
        }
        $$0.pop();
    }

    protected EquipmentSlot sunProtectionSlot() {
        return EquipmentSlot.HEAD;
    }

    private void burnUndead() {
        if (!this.isAlive() || !this.isSunBurnTick()) {
            return;
        }
        EquipmentSlot $$0 = this.sunProtectionSlot();
        ItemStack $$1 = this.getItemBySlot($$0);
        if (!$$1.isEmpty()) {
            if ($$1.isDamageableItem()) {
                Item $$2 = $$1.getItem();
                $$1.setDamageValue($$1.getDamageValue() + this.random.nextInt(2));
                if ($$1.getDamageValue() >= $$1.getMaxDamage()) {
                    this.onEquippedItemBroken($$2, $$0);
                    this.setItemSlot($$0, ItemStack.EMPTY);
                }
            }
            return;
        }
        this.igniteForSeconds(8.0f);
    }

    private boolean isSunBurnTick() {
        if (!this.level().isClientSide() && this.level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, this.position()).booleanValue()) {
            boolean $$2;
            float $$0 = this.getLightLevelDependentMagicValue();
            BlockPos $$1 = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean bl = $$2 = this.isInWaterOrRain() || this.isInPowderSnow || this.wasInPowderSnow;
            if ($$0 > 0.5f && this.random.nextFloat() * 30.0f < ($$0 - 0.4f) * 2.0f && !$$2 && this.level().canSeeSky($$1)) {
                return true;
            }
        }
        return false;
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
        ItemStack $$2 = $$1.getItem();
        ItemStack $$3 = this.equipItemIfPossible($$0, $$2.copy());
        if (!$$3.isEmpty()) {
            this.onItemPickup($$1);
            this.take($$1, $$3.getCount());
            $$2.shrink($$3.getCount());
            if ($$2.isEmpty()) {
                $$1.discard();
            }
        }
    }

    public ItemStack equipItemIfPossible(ServerLevel $$0, ItemStack $$1) {
        EquipmentSlot $$2 = this.getEquipmentSlotForItem($$1);
        if (!this.isEquippableInSlot($$1, $$2)) {
            return ItemStack.EMPTY;
        }
        ItemStack $$3 = this.getItemBySlot($$2);
        boolean $$4 = this.canReplaceCurrentItem($$1, $$3, $$2);
        if ($$2.isArmor() && !$$4) {
            $$2 = EquipmentSlot.MAINHAND;
            $$3 = this.getItemBySlot($$2);
            $$4 = $$3.isEmpty();
        }
        if ($$4 && this.canHoldItem($$1)) {
            double $$5 = this.dropChances.byEquipment($$2);
            if (!$$3.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < $$5) {
                this.spawnAtLocation($$0, $$3);
            }
            ItemStack $$6 = $$2.limit($$1);
            this.setItemSlotAndDropWhenKilled($$2, $$6);
            return $$6;
        }
        return ItemStack.EMPTY;
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot $$0, ItemStack $$1) {
        this.setItemSlot($$0, $$1);
        this.setGuaranteedDrop($$0);
        this.persistenceRequired = true;
    }

    protected boolean canShearEquipment(Player $$0) {
        return !this.isVehicle();
    }

    public void setGuaranteedDrop(EquipmentSlot $$0) {
        this.dropChances = this.dropChances.withGuaranteedDrop($$0);
    }

    protected boolean canReplaceCurrentItem(ItemStack $$0, ItemStack $$1, EquipmentSlot $$2) {
        if ($$1.isEmpty()) {
            return true;
        }
        if ($$2.isArmor()) {
            return this.compareArmor($$0, $$1, $$2);
        }
        if ($$2 == EquipmentSlot.MAINHAND) {
            return this.compareWeapons($$0, $$1, $$2);
        }
        return false;
    }

    private boolean compareArmor(ItemStack $$0, ItemStack $$1, EquipmentSlot $$2) {
        if (EnchantmentHelper.has($$1, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        double $$3 = this.getApproximateAttributeWith($$0, Attributes.ARMOR, $$2);
        double $$4 = this.getApproximateAttributeWith($$1, Attributes.ARMOR, $$2);
        double $$5 = this.getApproximateAttributeWith($$0, Attributes.ARMOR_TOUGHNESS, $$2);
        double $$6 = this.getApproximateAttributeWith($$1, Attributes.ARMOR_TOUGHNESS, $$2);
        if ($$3 != $$4) {
            return $$3 > $$4;
        }
        if ($$5 != $$6) {
            return $$5 > $$6;
        }
        return this.canReplaceEqualItem($$0, $$1);
    }

    private boolean compareWeapons(ItemStack $$0, ItemStack $$1, EquipmentSlot $$2) {
        double $$5;
        double $$4;
        TagKey<Item> $$3 = this.getPreferredWeaponType();
        if ($$3 != null) {
            if ($$1.is($$3) && !$$0.is($$3)) {
                return false;
            }
            if (!$$1.is($$3) && $$0.is($$3)) {
                return true;
            }
        }
        if (($$4 = this.getApproximateAttributeWith($$0, Attributes.ATTACK_DAMAGE, $$2)) != ($$5 = this.getApproximateAttributeWith($$1, Attributes.ATTACK_DAMAGE, $$2))) {
            return $$4 > $$5;
        }
        return this.canReplaceEqualItem($$0, $$1);
    }

    private double getApproximateAttributeWith(ItemStack $$0, Holder<Attribute> $$1, EquipmentSlot $$2) {
        double $$3 = this.getAttributes().hasAttribute($$1) ? this.getAttributeBaseValue($$1) : 0.0;
        ItemAttributeModifiers $$4 = $$0.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return $$4.compute($$1, $$3, $$2);
    }

    public boolean canReplaceEqualItem(ItemStack $$0, ItemStack $$1) {
        int $$5;
        Set<Object2IntMap.Entry<Holder<Enchantment>>> $$2 = $$1.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        Set<Object2IntMap.Entry<Holder<Enchantment>>> $$3 = $$0.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        if ($$3.size() != $$2.size()) {
            return $$3.size() > $$2.size();
        }
        int $$4 = $$0.getDamageValue();
        if ($$4 != ($$5 = $$1.getDamageValue())) {
            return $$4 < $$5;
        }
        return $$0.has(DataComponents.CUSTOM_NAME) && !$$1.has(DataComponents.CUSTOM_NAME);
    }

    public boolean canHoldItem(ItemStack $$0) {
        return true;
    }

    public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
        return this.canHoldItem($$1);
    }

    public @Nullable TagKey<Item> getPreferredWeaponType() {
        return null;
    }

    public boolean removeWhenFarAway(double $$0) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Player $$0 = this.level().getNearestPlayer(this, -1.0);
        if ($$0 != null) {
            int $$2;
            int $$3;
            double $$1 = $$0.distanceToSqr(this);
            if ($$1 > (double)($$3 = ($$2 = this.getType().getCategory().getDespawnDistance()) * $$2) && this.removeWhenFarAway($$1)) {
                this.discard();
            }
            int $$4 = this.getType().getCategory().getNoDespawnDistance();
            int $$5 = $$4 * $$4;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && $$1 > (double)$$5 && this.removeWhenFarAway($$1)) {
                this.discard();
            } else if ($$1 < (double)$$5) {
                this.noActionTime = 0;
            }
        }
    }

    @Override
    protected final void serverAiStep() {
        ++this.noActionTime;
        ProfilerFiller $$0 = Profiler.get();
        $$0.push("sensing");
        this.sensing.tick();
        $$0.pop();
        int $$1 = this.tickCount + this.getId();
        if ($$1 % 2 == 0 || this.tickCount <= 1) {
            $$0.push("targetSelector");
            this.targetSelector.tick();
            $$0.pop();
            $$0.push("goalSelector");
            this.goalSelector.tick();
            $$0.pop();
        } else {
            $$0.push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            $$0.pop();
            $$0.push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            $$0.pop();
        }
        $$0.push("navigation");
        this.navigation.tick();
        $$0.pop();
        $$0.push("mob tick");
        this.customServerAiStep((ServerLevel)this.level());
        $$0.pop();
        $$0.push("controls");
        $$0.push("move");
        this.moveControl.tick();
        $$0.popPush("look");
        this.lookControl.tick();
        $$0.popPush("jump");
        this.jumpControl.tick();
        $$0.pop();
        $$0.pop();
    }

    protected void customServerAiStep(ServerLevel $$0) {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    protected void clampHeadRotationToBody() {
        float $$0 = this.getMaxHeadYRot();
        float $$1 = this.getYHeadRot();
        float $$2 = Mth.wrapDegrees(this.yBodyRot - $$1);
        float $$3 = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - $$1), -$$0, $$0);
        float $$4 = $$1 + $$2 - $$3;
        this.setYHeadRot($$4);
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity $$0, float $$1, float $$2) {
        double $$7;
        double $$3 = $$0.getX() - this.getX();
        double $$4 = $$0.getZ() - this.getZ();
        if ($$0 instanceof LivingEntity) {
            LivingEntity $$5 = (LivingEntity)$$0;
            double $$6 = $$5.getEyeY() - this.getEyeY();
        } else {
            $$7 = ($$0.getBoundingBox().minY + $$0.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double $$8 = Math.sqrt($$3 * $$3 + $$4 * $$4);
        float $$9 = (float)(Mth.atan2($$4, $$3) * 57.2957763671875) - 90.0f;
        float $$10 = (float)(-(Mth.atan2($$7, $$8) * 57.2957763671875));
        this.setXRot(this.rotlerp(this.getXRot(), $$10, $$2));
        this.setYRot(this.rotlerp(this.getYRot(), $$9, $$1));
    }

    private float rotlerp(float $$0, float $$1, float $$2) {
        float $$3 = Mth.wrapDegrees($$1 - $$0);
        if ($$3 > $$2) {
            $$3 = $$2;
        }
        if ($$3 < -$$2) {
            $$3 = -$$2;
        }
        return $$0 + $$3;
    }

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        BlockPos $$5 = $$3.below();
        return EntitySpawnReason.isSpawner($$2) || $$1.getBlockState($$5).isValidSpawn($$1, $$5, $$0);
    }

    public boolean checkSpawnRules(LevelAccessor $$0, EntitySpawnReason $$1) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader $$0) {
        return !$$0.containsAnyLiquid(this.getBoundingBox()) && $$0.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int $$0) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return this.getComfortableFallDistance(0.0f);
        }
        int $$0 = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if (($$0 -= (3 - this.level().getDifficulty().getId()) * 4) < 0) {
            $$0 = 0;
        }
        return this.getComfortableFallDistance($$0);
    }

    public ItemStack getBodyArmorItem() {
        return this.getItemBySlot(EquipmentSlot.BODY);
    }

    public boolean isSaddled() {
        return this.hasValidEquippableItemForSlot(EquipmentSlot.SADDLE);
    }

    public boolean isWearingBodyArmor() {
        return this.hasValidEquippableItemForSlot(EquipmentSlot.BODY);
    }

    private boolean hasValidEquippableItemForSlot(EquipmentSlot $$0) {
        return this.hasItemInSlot($$0) && this.isEquippableInSlot(this.getItemBySlot($$0), $$0);
    }

    public void setBodyArmorItem(ItemStack $$0) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, $$0);
    }

    public Container createEquipmentSlotContainer(final EquipmentSlot $$0) {
        return new ContainerSingleItem(){

            @Override
            public ItemStack getTheItem() {
                return Mob.this.getItemBySlot($$0);
            }

            @Override
            public void setTheItem(ItemStack $$02) {
                Mob.this.setItemSlot($$0, $$02);
                if (!$$02.isEmpty()) {
                    Mob.this.setGuaranteedDrop($$0);
                    Mob.this.setPersistenceRequired();
                }
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player $$02) {
                return $$02.getVehicle() == Mob.this || $$02.isWithinEntityInteractionRange(Mob.this, 4.0);
            }
        };
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel $$0, DamageSource $$1, boolean $$2) {
        super.dropCustomDeathLoot($$0, $$1, $$2);
        for (EquipmentSlot $$3 : EquipmentSlot.VALUES) {
            ItemStack $$4 = this.getItemBySlot($$3);
            float $$5 = this.dropChances.byEquipment($$3);
            if ($$5 == 0.0f) continue;
            boolean $$6 = this.dropChances.isPreserved($$3);
            Object object = $$1.getEntity();
            if (object instanceof LivingEntity) {
                LivingEntity $$7 = (LivingEntity)object;
                object = this.level();
                if (object instanceof ServerLevel) {
                    ServerLevel $$8 = (ServerLevel)object;
                    $$5 = EnchantmentHelper.processEquipmentDropChance($$8, $$7, $$1, $$5);
                }
            }
            if ($$4.isEmpty() || EnchantmentHelper.has($$4, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) || !$$2 && !$$6 || !(this.random.nextFloat() < $$5)) continue;
            if (!$$6 && $$4.isDamageableItem()) {
                $$4.setDamageValue($$4.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max($$4.getMaxDamage() - 3, 1))));
            }
            this.spawnAtLocation($$0, $$4);
            this.setItemSlot($$3, ItemStack.EMPTY);
        }
    }

    public DropChances getDropChances() {
        return this.dropChances;
    }

    public void dropPreservedEquipment(ServerLevel $$02) {
        this.dropPreservedEquipment($$02, $$0 -> true);
    }

    public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel $$0, Predicate<ItemStack> $$1) {
        HashSet<EquipmentSlot> $$2 = new HashSet<EquipmentSlot>();
        for (EquipmentSlot $$3 : EquipmentSlot.VALUES) {
            ItemStack $$4 = this.getItemBySlot($$3);
            if ($$4.isEmpty()) continue;
            if (!$$1.test($$4)) {
                $$2.add($$3);
                continue;
            }
            if (!this.dropChances.isPreserved($$3)) continue;
            this.setItemSlot($$3, ItemStack.EMPTY);
            this.spawnAtLocation($$0, $$4);
        }
        return $$2;
    }

    private LootParams createEquipmentParams(ServerLevel $$0) {
        return new LootParams.Builder($$0).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.EQUIPMENT);
    }

    public void equip(EquipmentTable $$0) {
        this.equip($$0.lootTable(), $$0.slotDropChances());
    }

    public void equip(ResourceKey<LootTable> $$0, Map<EquipmentSlot, Float> $$1) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$2 = (ServerLevel)level;
            this.equip($$0, this.createEquipmentParams($$2), $$1);
        }
    }

    protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        if ($$0.nextFloat() < 0.15f * $$1.getSpecialMultiplier()) {
            int $$2 = $$0.nextInt(3);
            int $$3 = 1;
            while ((float)$$3 <= 3.0f) {
                if ($$0.nextFloat() < 0.1087f) {
                    ++$$2;
                }
                ++$$3;
            }
            float $$4 = this.level().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            boolean $$5 = true;
            for (EquipmentSlot $$6 : EQUIPMENT_POPULATION_ORDER) {
                Item $$8;
                ItemStack $$7 = this.getItemBySlot($$6);
                if (!$$5 && $$0.nextFloat() < $$4) break;
                $$5 = false;
                if (!$$7.isEmpty() || ($$8 = Mob.getEquipmentForSlot($$6, $$2)) == null) continue;
                this.setItemSlot($$6, new ItemStack($$8));
            }
        }
    }

    public static @Nullable Item getEquipmentForSlot(EquipmentSlot $$0, int $$1) {
        switch ($$0) {
            case HEAD: {
                if ($$1 == 0) {
                    return Items.LEATHER_HELMET;
                }
                if ($$1 == 1) {
                    return Items.COPPER_HELMET;
                }
                if ($$1 == 2) {
                    return Items.GOLDEN_HELMET;
                }
                if ($$1 == 3) {
                    return Items.CHAINMAIL_HELMET;
                }
                if ($$1 == 4) {
                    return Items.IRON_HELMET;
                }
                if ($$1 == 5) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if ($$1 == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if ($$1 == 1) {
                    return Items.COPPER_CHESTPLATE;
                }
                if ($$1 == 2) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if ($$1 == 3) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if ($$1 == 4) {
                    return Items.IRON_CHESTPLATE;
                }
                if ($$1 == 5) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if ($$1 == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if ($$1 == 1) {
                    return Items.COPPER_LEGGINGS;
                }
                if ($$1 == 2) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if ($$1 == 3) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if ($$1 == 4) {
                    return Items.IRON_LEGGINGS;
                }
                if ($$1 == 5) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if ($$1 == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if ($$1 == 1) {
                    return Items.COPPER_BOOTS;
                }
                if ($$1 == 2) {
                    return Items.GOLDEN_BOOTS;
                }
                if ($$1 == 3) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if ($$1 == 4) {
                    return Items.IRON_BOOTS;
                }
                if ($$1 != 5) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor $$0, RandomSource $$1, DifficultyInstance $$2) {
        this.enchantSpawnedWeapon($$0, $$1, $$2);
        for (EquipmentSlot $$3 : EquipmentSlot.VALUES) {
            if ($$3.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantSpawnedArmor($$0, $$1, $$3, $$2);
        }
    }

    protected void enchantSpawnedWeapon(ServerLevelAccessor $$0, RandomSource $$1, DifficultyInstance $$2) {
        this.enchantSpawnedEquipment($$0, EquipmentSlot.MAINHAND, $$1, 0.25f, $$2);
    }

    protected void enchantSpawnedArmor(ServerLevelAccessor $$0, RandomSource $$1, EquipmentSlot $$2, DifficultyInstance $$3) {
        this.enchantSpawnedEquipment($$0, $$2, $$1, 0.5f, $$3);
    }

    private void enchantSpawnedEquipment(ServerLevelAccessor $$0, EquipmentSlot $$1, RandomSource $$2, float $$3, DifficultyInstance $$4) {
        ItemStack $$5 = this.getItemBySlot($$1);
        if (!$$5.isEmpty() && $$2.nextFloat() < $$3 * $$4.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider($$5, $$0.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, $$4, $$2);
            this.setItemSlot($$1, $$5);
        }
    }

    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        AttributeInstance $$5 = Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE));
        if (!$$5.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
            $$5.addPermanentModifier(new AttributeModifier(RANDOM_SPAWN_BONUS_ID, $$4.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded($$4.nextFloat() < 0.05f);
        return $$3;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    @Override
    public void setDropChance(EquipmentSlot $$0, float $$1) {
        this.dropChances = this.dropChances.withEquipmentChance($$0, $$1);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean $$0) {
        this.canPickUpLoot = $$0;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        }
        InteractionResult $$2 = this.checkAndHandleImportantInteractions($$0, $$1);
        if ($$2.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, $$0);
            return $$2;
        }
        InteractionResult $$3 = super.interact($$0, $$1);
        if ($$3 != InteractionResult.PASS) {
            return $$3;
        }
        $$2 = this.mobInteract($$0, $$1);
        if ($$2.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, $$0);
            return $$2;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult checkAndHandleImportantInteractions(Player $$0, InteractionHand $$12) {
        InteractionResult $$3;
        ItemStack $$2 = $$0.getItemInHand($$12);
        if ($$2.is(Items.NAME_TAG) && ($$3 = $$2.interactLivingEntity($$0, this, $$12)).consumesAction()) {
            return $$3;
        }
        Item item = $$2.getItem();
        if (item instanceof SpawnEggItem) {
            SpawnEggItem $$4 = (SpawnEggItem)item;
            if (this.level() instanceof ServerLevel) {
                Optional<Mob> $$5 = $$4.spawnOffspringFromSpawnEgg($$0, this, this.getType(), (ServerLevel)this.level(), this.position(), $$2);
                $$5.ifPresent($$1 -> this.onOffspringSpawnedFromEgg($$0, (Mob)$$1));
                if ($$5.isEmpty()) {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    protected void onOffspringSpawnedFromEgg(Player $$0, Mob $$1) {
    }

    protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        return InteractionResult.PASS;
    }

    protected void usePlayerItem(Player $$0, InteractionHand $$1, ItemStack $$2) {
        int $$3 = $$2.getCount();
        UseRemainder $$4 = $$2.get(DataComponents.USE_REMAINDER);
        $$2.consume(1, $$0);
        if ($$4 != null) {
            ItemStack $$5 = $$4.convertIntoRemainder($$2, $$3, $$0.hasInfiniteMaterials(), $$0::handleExtraItemsCreatedOnUse);
            $$0.setItemInHand($$1, $$5);
        }
    }

    public boolean isWithinHome() {
        return this.isWithinHome(this.blockPosition());
    }

    public boolean isWithinHome(BlockPos $$0) {
        if (this.homeRadius == -1) {
            return true;
        }
        return this.homePosition.distSqr($$0) < (double)(this.homeRadius * this.homeRadius);
    }

    public boolean isWithinHome(Vec3 $$0) {
        if (this.homeRadius == -1) {
            return true;
        }
        return this.homePosition.distToCenterSqr($$0) < (double)(this.homeRadius * this.homeRadius);
    }

    public void setHomeTo(BlockPos $$0, int $$1) {
        this.homePosition = $$0;
        this.homeRadius = $$1;
    }

    public BlockPos getHomePosition() {
        return this.homePosition;
    }

    public int getHomeRadius() {
        return this.homeRadius;
    }

    public void clearHome() {
        this.homeRadius = -1;
    }

    public boolean hasHome() {
        return this.homeRadius != -1;
    }

    public <T extends Mob> @Nullable T convertTo(EntityType<T> $$0, ConversionParams $$1, EntitySpawnReason $$2, ConversionParams.AfterConversion<T> $$3) {
        if (this.isRemoved()) {
            return null;
        }
        Mob $$4 = (Mob)$$0.create(this.level(), $$2);
        if ($$4 == null) {
            return null;
        }
        $$1.type().convert(this, $$4, $$1);
        $$3.finalizeConversion($$4);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$5 = (ServerLevel)level;
            $$5.addFreshEntity($$4);
        }
        if ($$1.type().shouldDiscardAfterConversion()) {
            this.discard();
        }
        return (T)$$4;
    }

    public <T extends Mob> @Nullable T convertTo(EntityType<T> $$0, ConversionParams $$1, ConversionParams.AfterConversion<T> $$2) {
        return this.convertTo($$0, $$1, EntitySpawnReason.CONVERSION, $$2);
    }

    @Override
    public @Nullable Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    private void resetAngularLeashMomentum() {
        if (this.leashData != null) {
            this.leashData.angularMomentum = 0.0;
        }
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData $$0) {
        this.leashData = $$0;
    }

    @Override
    public void onLeashRemoved() {
        if (this.getLeashData() == null) {
            this.clearHome();
        }
    }

    @Override
    public void leashTooFarBehaviour() {
        Leashable.super.leashTooFarBehaviour();
        this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Enemy);
    }

    @Override
    public boolean startRiding(Entity $$0, boolean $$1, boolean $$2) {
        boolean $$3 = super.startRiding($$0, $$1, $$2);
        if ($$3 && this.isLeashed()) {
            this.dropLeash();
        }
        return $$3;
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, $$0 ? (byte)($$1 | 1) : (byte)($$1 & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, $$0 ? (byte)($$1 | 2) : (byte)($$1 & 0xFFFFFFFD));
    }

    public void setAggressive(boolean $$0) {
        byte $$1 = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, $$0 ? (byte)($$1 | 4) : (byte)($$1 & 0xFFFFFFFB));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean $$0) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public boolean isWithinMeleeAttackRange(LivingEntity $$0) {
        double $$5;
        double $$4;
        AttackRange $$1 = this.getActiveItem().get(DataComponents.ATTACK_RANGE);
        if ($$1 == null) {
            double $$2 = DEFAULT_ATTACK_REACH;
            double $$3 = 0.0;
        } else {
            $$4 = $$1.effectiveMaxRange(this);
            $$5 = $$1.effectiveMinRange(this);
        }
        AABB $$6 = $$0.getHitbox();
        return this.getAttackBoundingBox($$4).intersects($$6) && ($$5 <= 0.0 || !this.getAttackBoundingBox($$5).intersects($$6));
    }

    protected AABB getAttackBoundingBox(double $$0) {
        AABB $$5;
        Entity $$1 = this.getVehicle();
        if ($$1 != null) {
            AABB $$2 = $$1.getBoundingBox();
            AABB $$3 = this.getBoundingBox();
            AABB $$4 = new AABB(Math.min($$3.minX, $$2.minX), $$3.minY, Math.min($$3.minZ, $$2.minZ), Math.max($$3.maxX, $$2.maxX), $$3.maxY, Math.max($$3.maxZ, $$2.maxZ));
        } else {
            $$5 = this.getBoundingBox();
        }
        return $$5.inflate($$0, 0.0, $$0);
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        float $$2 = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack $$3 = this.getWeaponItem();
        DamageSource $$4 = $$3.getDamageSource(this, () -> this.damageSources().mobAttack(this));
        $$2 = EnchantmentHelper.modifyDamage($$0, $$3, $$1, $$4, $$2);
        $$2 += $$3.getItem().getAttackDamageBonus($$1, $$2, $$4);
        Vec3 $$5 = $$1.getDeltaMovement();
        boolean $$6 = $$1.hurtServer($$0, $$4, $$2);
        if ($$6) {
            this.causeExtraKnockback($$1, this.getKnockback($$1, $$4), $$5);
            if ($$1 instanceof LivingEntity) {
                LivingEntity $$7 = (LivingEntity)$$1;
                $$3.hurtEnemy($$7, this);
            }
            EnchantmentHelper.doPostAttackEffects($$0, $$1, $$4);
            this.setLastHurtMob($$1);
            this.playAttackSound();
        }
        this.lungeForwardMaybe();
        return $$6;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> $$0) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid($$0);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void removeFreeWill() {
        this.removeAllGoals($$0 -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> $$0) {
        this.goalSelector.removeAllGoals($$0);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        for (EquipmentSlot $$0 : EquipmentSlot.VALUES) {
            ItemStack $$1 = this.getItemBySlot($$0);
            if ($$1.isEmpty()) continue;
            $$1.setCount(0);
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        SpawnEggItem $$0 = SpawnEggItem.byId(this.getType());
        if ($$0 == null) {
            return null;
        }
        return new ItemStack($$0);
    }

    @Override
    protected void onAttributeUpdated(Holder<Attribute> $$0) {
        super.onAttributeUpdated($$0);
        if ($$0.is(Attributes.FOLLOW_RANGE) || $$0.is(Attributes.TEMPT_RANGE)) {
            this.getNavigation().updatePathfinderMaxVisitedNodes();
        }
    }

    @Override
    public void registerDebugValues(ServerLevel $$0, DebugValueSource.Registration $$1) {
        $$1.register(DebugSubscriptions.ENTITY_PATHS, () -> {
            Path $$0 = this.getNavigation().getPath();
            if ($$0 != null && $$0.debugData() != null) {
                return new DebugPathInfo($$0.copy(), this.getNavigation().getMaxDistanceToWaypoint());
            }
            return null;
        });
        $$1.register(DebugSubscriptions.GOAL_SELECTORS, () -> {
            Set<WrappedGoal> $$0 = this.goalSelector.getAvailableGoals();
            ArrayList<DebugGoalInfo.DebugGoal> $$12 = new ArrayList<DebugGoalInfo.DebugGoal>($$0.size());
            $$0.forEach($$1 -> $$12.add(new DebugGoalInfo.DebugGoal($$1.getPriority(), $$1.isRunning(), $$1.getGoal().getClass().getSimpleName())));
            return new DebugGoalInfo($$12);
        });
        if (!this.brain.isBrainDead()) {
            $$1.register(DebugSubscriptions.BRAINS, () -> DebugBrainDump.takeBrainDump($$0, this));
        }
    }

    public float chargeSpeedModifier() {
        return 1.0f;
    }
}

