/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.zombie;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.SpearUseGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class ZombifiedPiglin
extends Zombie
implements NeutralMob {
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIFIED_PIGLIN.getDimensions().scale(0.5f).withEyeHeight(0.97f);
    private static final Identifier SPEED_MODIFIER_ATTACKING_ID = Identifier.withDefaultNamespace("attacking");
    private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_ID, 0.05, AttributeModifier.Operation.ADD_VALUE);
    private static final UniformInt FIRST_ANGER_SOUND_DELAY = TimeUtil.rangeOfSeconds(0, 1);
    private int playFirstAngerSoundIn;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private long persistentAngerEndTime;
    private @Nullable EntityReference<LivingEntity> persistentAngerTarget;
    private static final int ALERT_RANGE_Y = 10;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private int ticksUntilNextAlert;

    public ZombifiedPiglin(EntityType<? extends ZombifiedPiglin> $$0, Level $$1) {
        super((EntityType<? extends Zombie>)$$0, $$1);
        this.setPathfindingMalus(PathType.LAVA, 8.0f);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new SpearUseGoal<ZombifiedPiglin>(this, 1.0, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<ZombifiedPiglin>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0).add(Attributes.MOVEMENT_SPEED, 0.23f).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose $$0) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        AttributeInstance $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (this.isAngry()) {
            if (!this.isBaby() && !$$1.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
                $$1.addTransientModifier(SPEED_MODIFIER_ATTACKING);
            }
            this.maybePlayFirstAngerSound();
        } else if ($$1.hasModifier(SPEED_MODIFIER_ATTACKING_ID)) {
            $$1.removeModifier(SPEED_MODIFIER_ATTACKING_ID);
        }
        this.updatePersistentAnger($$0, true);
        if (this.getTarget() != null) {
            this.maybeAlertOthers();
        }
        super.customServerAiStep($$0);
    }

    private void maybePlayFirstAngerSound() {
        if (this.playFirstAngerSoundIn > 0) {
            --this.playFirstAngerSoundIn;
            if (this.playFirstAngerSoundIn == 0) {
                this.playAngerSound();
            }
        }
    }

    private void maybeAlertOthers() {
        if (this.ticksUntilNextAlert > 0) {
            --this.ticksUntilNextAlert;
            return;
        }
        if (this.getSensing().hasLineOfSight(this.getTarget())) {
            this.alertOthers();
        }
        this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
    }

    private void alertOthers() {
        double $$02 = this.getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB $$1 = AABB.unitCubeFromLowerCorner(this.position()).inflate($$02, 10.0, $$02);
        this.level().getEntitiesOfClass(ZombifiedPiglin.class, $$1, EntitySelector.NO_SPECTATORS).stream().filter($$0 -> $$0 != this).filter($$0 -> $$0.getTarget() == null).filter($$0 -> !$$0.isAlliedTo(this.getTarget())).forEach($$0 -> $$0.setTarget(this.getTarget()));
    }

    private void playAngerSound() {
        this.playSound(SoundEvents.ZOMBIFIED_PIGLIN_ANGRY, this.getSoundVolume() * 2.0f, this.getVoicePitch() * 1.8f);
    }

    @Override
    public void setTarget(@Nullable LivingEntity $$0) {
        if (this.getTarget() == null && $$0 != null) {
            this.playFirstAngerSoundIn = FIRST_ANGER_SOUND_DELAY.sample(this.random);
            this.ticksUntilNextAlert = ALERT_INTERVAL.sample(this.random);
        }
        super.setTarget($$0);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public static boolean checkZombifiedPiglinSpawnRules(EntityType<ZombifiedPiglin> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getDifficulty() != Difficulty.PEACEFUL && !$$1.getBlockState($$3.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return $$0.isUnobstructed(this) && !$$0.containsAnyLiquid(this.getBoundingBox());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        this.addPersistentAngerSaveData($$0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.readPersistentAngerSaveData(this.level(), $$0);
    }

    @Override
    public void setPersistentAngerEndTime(long $$0) {
        this.persistentAngerEndTime = $$0;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> $$0) {
        this.persistentAngerTarget = $$0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEvents.ZOMBIFIED_PIGLIN_ANGRY : SoundEvents.ZOMBIFIED_PIGLIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.ZOMBIFIED_PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIFIED_PIGLIN_DEATH;
    }

    @Override
    public void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack($$0.nextInt(20) == 0 ? Items.GOLDEN_SPEAR : Items.GOLDEN_SWORD));
    }

    @Override
    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(0.0);
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public boolean isPreventingPlayerRest(ServerLevel $$0, Player $$1) {
        return this.isAngryAt($$1, $$0);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
        return this.canHoldItem($$1);
    }
}

