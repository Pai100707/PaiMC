/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class Piglin
extends AbstractPiglin
implements CrossbowAttackMob,
InventoryCarrier {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final Identifier SPEED_MODIFIER_BABY_ID = Identifier.withDefaultNamespace("baby");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_ID, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final int MAX_HEALTH = 16;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35f;
    private static final int ATTACK_DAMAGE = 5;
    private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1f;
    private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
    private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2f;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.PIGLIN.getDimensions().scale(0.5f).withEyeHeight(0.97f);
    private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5;
    private static final boolean DEFAULT_IS_BABY = false;
    private static final boolean DEFAULT_CANNOT_HUNT = false;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt = false;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, (Object[])new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT, MemoryModuleType.SPEAR_FLEEING_TIME, MemoryModuleType.SPEAR_FLEEING_POSITION, MemoryModuleType.SPEAR_CHARGE_POSITION, MemoryModuleType.SPEAR_ENGAGE_TIME, MemoryModuleType.SPEAR_STATUS});

    public Piglin(EntityType<? extends AbstractPiglin> $$0, Level $$1) {
        super($$0, $$1);
        this.xpReward = 5;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("IsBaby", this.isBaby());
        $$0.putBoolean("CannotHunt", this.cannotHunt);
        this.writeInventoryToTag($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setBaby($$0.getBooleanOr("IsBaby", false));
        this.setCannotHunt($$0.getBooleanOr("CannotHunt", false));
        this.readInventoryFromTag($$0);
    }

    @Override
    @VisibleForDebug
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel $$0, DamageSource $$12, boolean $$2) {
        super.dropCustomDeathLoot($$0, $$12, $$2);
        this.inventory.removeAllItems().forEach($$1 -> this.spawnAtLocation($$0, (ItemStack)$$1));
    }

    protected ItemStack addToInventory(ItemStack $$0) {
        return this.inventory.addItem($$0);
    }

    protected boolean canAddToInventory(ItemStack $$0) {
        return this.inventory.canAddItem($$0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_BABY_ID, false);
        $$0.define(DATA_IS_CHARGING_CROSSBOW, false);
        $$0.define(DATA_IS_DANCING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
        super.onSyncedDataUpdated($$0);
        if (DATA_BABY_ID.equals($$0)) {
            this.refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.35f).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return !$$1.getBlockState($$3.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        if ($$2 != EntitySpawnReason.STRUCTURE) {
            if ($$4.nextFloat() < 0.2f) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }
        PiglinAi.initMemories(this, $$0.getRandom());
        this.populateDefaultEquipmentSlots($$4, $$1);
        this.populateDefaultEquipmentEnchantments($$0, $$4, $$1);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        if (this.isAdult()) {
            this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), $$0);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), $$0);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), $$0);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), $$0);
        }
    }

    private void maybeWearArmor(EquipmentSlot $$0, ItemStack $$1, RandomSource $$2) {
        if ($$2.nextFloat() < 0.1f) {
            this.setItemSlot($$0, $$1);
        }
    }

    protected Brain.Provider<Piglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> $$0) {
        return PiglinAi.makeBrain(this, this.brainProvider().makeBrain($$0));
    }

    public Brain<Piglin> getBrain() {
        return super.getBrain();
    }

    @Override
    public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        InteractionResult $$2 = super.mobInteract($$0, $$1);
        if ($$2.consumesAction()) {
            return $$2;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$3 = (ServerLevel)level;
            return PiglinAi.mobInteract($$3, this, $$0, $$1);
        }
        boolean $$4 = PiglinAi.canAdmire(this, $$0.getItemInHand($$1)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
        return $$4 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    @Override
    public void setBaby(boolean $$0) {
        this.getEntityData().set(DATA_BABY_ID, $$0);
        if (!this.level().isClientSide()) {
            AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
            $$1.removeModifier(SPEED_MODIFIER_BABY.id());
            if ($$0) {
                $$1.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    private void setCannotHunt(boolean $$0) {
        this.cannotHunt = $$0;
    }

    @Override
    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        ProfilerFiller $$1 = Profiler.get();
        $$1.push("piglinBrain");
        this.getBrain().tick($$0, this);
        $$1.pop();
        PiglinAi.updateActivity(this);
        super.customServerAiStep($$0);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel $$0) {
        return this.xpReward;
    }

    @Override
    protected void finishConversion(ServerLevel $$0) {
        PiglinAi.cancelAdmiring($$0, this);
        this.inventory.removeAllItems().forEach($$1 -> this.spawnAtLocation($$0, (ItemStack)$$1));
        super.finishConversion($$0);
    }

    private ItemStack createSpawnWeapon() {
        if ((double)this.random.nextFloat() < 0.5) {
            return new ItemStack(Items.CROSSBOW);
        }
        return new ItemStack(this.random.nextInt(10) == 0 ? Items.GOLDEN_SPEAR : Items.GOLDEN_SWORD);
    }

    @Override
    public @Nullable TagKey<Item> getPreferredWeaponType() {
        if (this.isBaby()) {
            return null;
        }
        return ItemTags.PIGLIN_PREFERRED_WEAPONS;
    }

    private boolean isChargingCrossbow() {
        return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean $$0) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, $$0);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public PiglinArmPose getArmPose() {
        if (this.isDancing()) {
            return PiglinArmPose.DANCING;
        }
        if (PiglinAi.isLovedItem(this.getOffhandItem())) {
            return PiglinArmPose.ADMIRING_ITEM;
        }
        if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        }
        if (this.isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        }
        if (this.isHolding(Items.CROSSBOW) && CrossbowItem.isCharged(this.getWeaponItem())) {
            return PiglinArmPose.CROSSBOW_HOLD;
        }
        return PiglinArmPose.DEFAULT;
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_IS_DANCING);
    }

    public void setDancing(boolean $$0) {
        this.entityData.set(DATA_IS_DANCING, $$0);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        Entity entity;
        boolean $$3 = super.hurtServer($$0, $$1, $$2);
        if ($$3 && (entity = $$1.getEntity()) instanceof LivingEntity) {
            LivingEntity $$4 = (LivingEntity)entity;
            PiglinAi.wasHurtBy($$0, this, $$4);
        }
        return $$3;
    }

    @Override
    public void performRangedAttack(LivingEntity $$0, float $$1) {
        this.performCrossbowAttack(this, 1.6f);
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack $$0) {
        return $$0.getItem() == Items.CROSSBOW || $$0.has(DataComponents.KINETIC_WEAPON);
    }

    protected void holdInMainHand(ItemStack $$0) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, $$0);
    }

    protected void holdInOffHand(ItemStack $$0) {
        if ($$0.is(PiglinAi.BARTERING_ITEM)) {
            this.setItemSlot(EquipmentSlot.OFFHAND, $$0);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, $$0);
        }
    }

    @Override
    public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
        return $$0.getGameRules().get(GameRules.MOB_GRIEFING) != false && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, $$1);
    }

    protected boolean canReplaceCurrentItem(ItemStack $$0) {
        EquipmentSlot $$1 = this.getEquipmentSlotForItem($$0);
        ItemStack $$2 = this.getItemBySlot($$1);
        return this.canReplaceCurrentItem($$0, $$2, $$1);
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack $$0, ItemStack $$1, EquipmentSlot $$2) {
        boolean $$5;
        if (EnchantmentHelper.has($$1, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        TagKey<Item> $$3 = this.getPreferredWeaponType();
        boolean $$4 = PiglinAi.isLovedItem($$0) || $$3 != null && $$0.is($$3);
        boolean bl = $$5 = PiglinAi.isLovedItem($$1) || $$3 != null && $$1.is($$3);
        if ($$4 && !$$5) {
            return true;
        }
        if (!$$4 && $$5) {
            return false;
        }
        return super.canReplaceCurrentItem($$0, $$1, $$2);
    }

    @Override
    protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
        this.onItemPickup($$1);
        PiglinAi.pickUpItem($$0, this, $$1);
    }

    @Override
    public boolean startRiding(Entity $$0, boolean $$1, boolean $$2) {
        if (this.isBaby() && $$0.getType() == EntityType.HOGLIN) {
            $$0 = this.getTopPassenger($$0, 3);
        }
        return super.startRiding($$0, $$1, $$2);
    }

    private Entity getTopPassenger(Entity $$0, int $$1) {
        List<Entity> $$2 = $$0.getPassengers();
        if ($$1 == 1 || $$2.isEmpty()) {
            return $$0;
        }
        return this.getTopPassenger($$2.getFirst(), $$1 - 1);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        if (this.level().isClientSide()) {
            return null;
        }
        return PiglinAi.getSoundForCurrentActivity(this).orElse(null);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        this.playSound(SoundEvents.PIGLIN_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playConvertedSound() {
        this.makeSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}

