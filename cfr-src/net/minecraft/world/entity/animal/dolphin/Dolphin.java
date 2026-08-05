/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.dolphin;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.DolphinJumpGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Dolphin
extends AgeableWaterCreature {
    private static final EntityDataAccessor<Boolean> GOT_FISH = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MOISTNESS_LEVEL = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.INT);
    static final TargetingConditions SWIM_WITH_PLAYER_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
    public static final int TOTAL_AIR_SUPPLY = 4800;
    private static final int TOTAL_MOISTNESS_LEVEL = 2400;
    public static final Predicate<ItemEntity> ALLOWED_ITEMS = $$0 -> !$$0.hasPickUpDelay() && $$0.isAlive() && $$0.isInWater();
    public static final float BABY_SCALE = 0.65f;
    private static final boolean DEFAULT_GOT_FISH = false;
    @Nullable BlockPos treasurePos;

    public Dolphin(EntityType<? extends Dolphin> $$0, Level $$1) {
        super((EntityType<? extends AgeableWaterCreature>)$$0, $$1);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setCanPickUpLoot(true);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        this.setAirSupply(this.getMaxAirSupply());
        this.setXRot(0.0f);
        SpawnGroupData $$4 = Objects.requireNonNullElseGet($$3, () -> new AgeableMob.AgeableMobGroupData(0.1f));
        return super.finalizeSpawn($$0, $$1, $$2, $$4);
    }

    @Override
    public @Nullable Dolphin getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return EntityType.DOLPHIN.create($$0, EntitySpawnReason.BREEDING);
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.65f : 1.0f;
    }

    @Override
    protected void handleAirSupply(int $$0) {
    }

    public boolean gotFish() {
        return this.entityData.get(GOT_FISH);
    }

    public void setGotFish(boolean $$0) {
        this.entityData.set(GOT_FISH, $$0);
    }

    public int getMoistnessLevel() {
        return this.entityData.get(MOISTNESS_LEVEL);
    }

    public void setMoisntessLevel(int $$0) {
        this.entityData.set(MOISTNESS_LEVEL, $$0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(GOT_FISH, false);
        $$0.define(MOISTNESS_LEVEL, 2400);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("GotFish", this.gotFish());
        $$0.putInt("Moistness", this.getMoistnessLevel());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setGotFish($$0.getBooleanOr("GotFish", false));
        this.setMoisntessLevel($$0.getIntOr("Moistness", 2400));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BreathAirGoal(this));
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new DolphinSwimToTreasureGoal(this));
        this.goalSelector.addGoal(2, new DolphinSwimWithPlayerGoal(this, 4.0));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new DolphinJumpGoal(this, 10));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2f, true));
        this.goalSelector.addGoal(8, new PlayWithItemsGoal());
        this.goalSelector.addGoal(8, new FollowBoatGoal(this));
        this.goalSelector.addGoal(9, new AvoidEntityGoal<Guardian>(this, Guardian.class, 8.0f, 1.0, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Guardian.class).setAlertOthers(new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 1.2f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new WaterBoundPathNavigation(this, $$0);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.DOLPHIN_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public boolean canAttack(LivingEntity $$0) {
        return !this.isBaby() && super.canAttack($$0);
    }

    @Override
    public int getMaxAirSupply() {
        return 4800;
    }

    @Override
    protected int increaseAirSupply(int $$0) {
        return this.getMaxAirSupply();
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override
    protected boolean canRide(Entity $$0) {
        return true;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot $$0) {
        return $$0 == EquipmentSlot.MAINHAND && this.canPickUpLoot();
    }

    @Override
    protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
        ItemStack $$2;
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.canHoldItem($$2 = $$1.getItem())) {
            this.onItemPickup($$1);
            this.setItemSlot(EquipmentSlot.MAINHAND, $$2);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take($$1, $$2.getCount());
            $$1.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isNoAi()) {
            this.setAirSupply(this.getMaxAirSupply());
            return;
        }
        if (this.isInWaterOrRain()) {
            this.setMoisntessLevel(2400);
        } else {
            this.setMoisntessLevel(this.getMoistnessLevel() - 1);
            if (this.getMoistnessLevel() <= 0) {
                this.hurt(this.damageSources().dryOut(), 1.0f);
            }
            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.2f, 0.5, (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f));
                this.setYRot(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.needsSync = true;
            }
        }
        if (this.level().isClientSide() && this.isInWater() && this.getDeltaMovement().lengthSqr() > 0.03) {
            Vec3 $$0 = this.getViewVector(0.0f);
            float $$1 = Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * 0.3f;
            float $$2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180)) * 0.3f;
            float $$3 = 1.2f - this.random.nextFloat() * 0.7f;
            for (int $$4 = 0; $$4 < 2; ++$$4) {
                this.level().addParticle(ParticleTypes.DOLPHIN, this.getX() - $$0.x * (double)$$3 + (double)$$1, this.getY() - $$0.y, this.getZ() - $$0.z * (double)$$3 + (double)$$2, 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.DOLPHIN, this.getX() - $$0.x * (double)$$3 - (double)$$1, this.getY() - $$0.y, this.getZ() - $$0.z * (double)$$3 - (double)$$2, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte $$0) {
        if ($$0 == 38) {
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else {
            super.handleEntityEvent($$0);
        }
    }

    private void addParticlesAroundSelf(ParticleOptions $$0) {
        for (int $$1 = 0; $$1 < 7; ++$$1) {
            double $$2 = this.random.nextGaussian() * 0.01;
            double $$3 = this.random.nextGaussian() * 0.01;
            double $$4 = this.random.nextGaussian() * 0.01;
            this.level().addParticle($$0, this.getRandomX(1.0), this.getRandomY() + 0.2, this.getRandomZ(1.0), $$2, $$3, $$4);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
        ItemStack $$2 = $$0.getItemInHand($$1);
        if (!$$2.isEmpty() && $$2.is(ItemTags.FISHES)) {
            if (!this.level().isClientSide()) {
                this.playSound(SoundEvents.DOLPHIN_EAT, 1.0f, 1.0f);
            }
            if (this.isBaby()) {
                $$2.consume(1, $$0);
                this.ageUp(Dolphin.getSpeedUpSecondsWhenFeeding(-this.age), true);
            } else {
                this.setGotFish(true);
                $$2.consume(1, $$0);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract($$0, $$1);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.DOLPHIN_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.DOLPHIN_SWIM;
    }

    protected boolean closeToNextPos() {
        BlockPos $$0 = this.getNavigation().getTargetPos();
        if ($$0 != null) {
            return $$0.closerToCenterThan(this.position(), 12.0);
        }
        return false;
    }

    @Override
    protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
        this.moveRelative(this.getSpeed(), $$0);
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        if (this.getTarget() == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
        }
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class DolphinSwimToTreasureGoal
    extends Goal {
        private final Dolphin dolphin;
        private boolean stuck;

        DolphinSwimToTreasureGoal(Dolphin $$0) {
            this.dolphin = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean canUse() {
            return this.dolphin.gotFish() && this.dolphin.getAirSupply() >= 100;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos $$0 = this.dolphin.treasurePos;
            if ($$0 == null) {
                return false;
            }
            return !BlockPos.containing($$0.getX(), this.dolphin.getY(), $$0.getZ()).closerToCenterThan(this.dolphin.position(), 4.0) && !this.stuck && this.dolphin.getAirSupply() >= 100;
        }

        @Override
        public void start() {
            if (!(this.dolphin.level() instanceof ServerLevel)) {
                return;
            }
            ServerLevel $$0 = (ServerLevel)this.dolphin.level();
            this.stuck = false;
            this.dolphin.getNavigation().stop();
            BlockPos $$1 = this.dolphin.blockPosition();
            BlockPos $$2 = $$0.findNearestMapStructure(StructureTags.DOLPHIN_LOCATED, $$1, 50, false);
            if ($$2 == null) {
                this.stuck = true;
                return;
            }
            this.dolphin.treasurePos = $$2;
            $$0.broadcastEntityEvent(this.dolphin, (byte)38);
        }

        @Override
        public void stop() {
            BlockPos $$0 = this.dolphin.treasurePos;
            if ($$0 == null || BlockPos.containing($$0.getX(), this.dolphin.getY(), $$0.getZ()).closerToCenterThan(this.dolphin.position(), 4.0) || this.stuck) {
                this.dolphin.setGotFish(false);
            }
        }

        @Override
        public void tick() {
            if (this.dolphin.treasurePos == null) {
                return;
            }
            Level $$0 = this.dolphin.level();
            if (this.dolphin.closeToNextPos() || this.dolphin.getNavigation().isDone()) {
                BlockPos $$3;
                Vec3 $$1 = Vec3.atCenterOf(this.dolphin.treasurePos);
                Vec3 $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 16, 1, $$1, 0.3926991f);
                if ($$2 == null) {
                    $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 4, $$1, 1.5707963705062866);
                }
                if (!($$2 == null || $$0.getFluidState($$3 = BlockPos.containing($$2)).is(FluidTags.WATER) && $$0.getBlockState($$3).isPathfindable(PathComputationType.WATER))) {
                    $$2 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 5, $$1, 1.5707963705062866);
                }
                if ($$2 == null) {
                    this.stuck = true;
                    return;
                }
                this.dolphin.getLookControl().setLookAt($$2.x, $$2.y, $$2.z, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
                this.dolphin.getNavigation().moveTo($$2.x, $$2.y, $$2.z, 1.3);
                if ($$0.random.nextInt(this.adjustedTickDelay(80)) == 0) {
                    $$0.broadcastEntityEvent(this.dolphin, (byte)38);
                }
            }
        }
    }

    static class DolphinSwimWithPlayerGoal
    extends Goal {
        private final Dolphin dolphin;
        private final double speedModifier;
        private @Nullable Player player;

        DolphinSwimWithPlayerGoal(Dolphin $$0, double $$1) {
            this.dolphin = $$0;
            this.speedModifier = $$1;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.player = DolphinSwimWithPlayerGoal.getServerLevel(this.dolphin).getNearestPlayer(SWIM_WITH_PLAYER_TARGETING, this.dolphin);
            if (this.player == null) {
                return false;
            }
            return this.player.isSwimming() && this.dolphin.getTarget() != this.player;
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isSwimming() && this.dolphin.distanceToSqr(this.player) < 256.0;
        }

        @Override
        public void start() {
            this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
        }

        @Override
        public void stop() {
            this.player = null;
            this.dolphin.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.dolphin.getLookControl().setLookAt(this.player, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
            if (this.dolphin.distanceToSqr(this.player) < 6.25) {
                this.dolphin.getNavigation().stop();
            } else {
                this.dolphin.getNavigation().moveTo(this.player, this.speedModifier);
            }
            if (this.player.isSwimming() && this.player.level().random.nextInt(6) == 0) {
                this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
            }
        }
    }

    class PlayWithItemsGoal
    extends Goal {
        private int cooldown;

        PlayWithItemsGoal() {
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > Dolphin.this.tickCount) {
                return false;
            }
            List<ItemEntity> $$0 = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            return !$$0.isEmpty() || !Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void start() {
            List<ItemEntity> $$0 = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            if (!$$0.isEmpty()) {
                Dolphin.this.getNavigation().moveTo($$0.get(0), (double)1.2f);
                Dolphin.this.playSound(SoundEvents.DOLPHIN_PLAY, 1.0f, 1.0f);
            }
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack $$0 = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!$$0.isEmpty()) {
                this.drop($$0);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.cooldown = Dolphin.this.tickCount + Dolphin.this.random.nextInt(100);
            }
        }

        @Override
        public void tick() {
            List<ItemEntity> $$0 = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            ItemStack $$1 = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!$$1.isEmpty()) {
                this.drop($$1);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else if (!$$0.isEmpty()) {
                Dolphin.this.getNavigation().moveTo($$0.get(0), (double)1.2f);
            }
        }

        private void drop(ItemStack $$0) {
            if ($$0.isEmpty()) {
                return;
            }
            double $$1 = Dolphin.this.getEyeY() - (double)0.3f;
            ItemEntity $$2 = new ItemEntity(Dolphin.this.level(), Dolphin.this.getX(), $$1, Dolphin.this.getZ(), $$0);
            $$2.setPickUpDelay(40);
            $$2.setThrower(Dolphin.this);
            float $$3 = 0.3f;
            float $$4 = Dolphin.this.random.nextFloat() * ((float)Math.PI * 2);
            float $$5 = 0.02f * Dolphin.this.random.nextFloat();
            $$2.setDeltaMovement(0.3f * -Mth.sin(Dolphin.this.getYRot() * ((float)Math.PI / 180)) * Mth.cos(Dolphin.this.getXRot() * ((float)Math.PI / 180)) + Mth.cos($$4) * $$5, 0.3f * Mth.sin(Dolphin.this.getXRot() * ((float)Math.PI / 180)) * 1.5f, 0.3f * Mth.cos(Dolphin.this.getYRot() * ((float)Math.PI / 180)) * Mth.cos(Dolphin.this.getXRot() * ((float)Math.PI / 180)) + Mth.sin($$4) * $$5);
            Dolphin.this.level().addFreshEntity($$2);
        }
    }
}

