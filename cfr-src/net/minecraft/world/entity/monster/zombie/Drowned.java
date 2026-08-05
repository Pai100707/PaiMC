/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.zombie;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Drowned
extends Zombie
implements RangedAttackMob {
    public static final float NAUTILUS_SHELL_CHANCE = 0.03f;
    private static final float ZOMBIE_NAUTILUS_JOCKEY_CHANCE = 0.5f;
    boolean searchingForLand;

    public Drowned(EntityType<? extends Drowned> $$0, Level $$1) {
        super((EntityType<? extends Zombie>)$$0, $$1);
        this.moveControl = new DrownedMoveControl(this);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected PathNavigation createNavigation(Level $$0) {
        return new AmphibiousPathNavigation(this, $$0);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new DrownedGoToWaterGoal(this, 1.0));
        this.goalSelector.addGoal(2, new DrownedTridentAttackGoal(this, 1.0, 40, 10.0f));
        this.goalSelector.addGoal(2, new DrownedAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new DrownedGoToBeachGoal(this, 1.0));
        this.goalSelector.addGoal(6, new DrownedSwimUpGoal(this, 1.0, this.level().getSeaLevel()));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Drowned.class).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, ($$0, $$1) -> this.okTarget($$0)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Axolotl>((Mob)this, Axolotl.class, true, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<Turtle>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        ZombieNautilus $$4;
        $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
        if (this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && $$0.getRandom().nextFloat() < 0.03f) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        }
        if (($$2 == EntitySpawnReason.NATURAL || $$2 == EntitySpawnReason.STRUCTURE) && this.getMainHandItem().is(Items.TRIDENT) && $$0.getRandom().nextFloat() < 0.5f && !this.isBaby() && !$$0.getBiome(this.blockPosition()).is(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS) && ($$4 = EntityType.ZOMBIE_NAUTILUS.create(this.level(), EntitySpawnReason.JOCKEY)) != null) {
            if ($$2 == EntitySpawnReason.STRUCTURE) {
                $$4.setPersistenceRequired();
            }
            $$4.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
            $$4.finalizeSpawn($$0, $$1, $$2, null);
            this.startRiding($$4, false, false);
            $$0.addFreshEntity($$4);
        }
        return $$3;
    }

    public static boolean checkDrownedSpawnRules(EntityType<Drowned> $$0, ServerLevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        boolean $$6;
        if (!$$1.getFluidState($$3.below()).is(FluidTags.WATER) && !EntitySpawnReason.isSpawner($$2)) {
            return false;
        }
        Holder<Biome> $$5 = $$1.getBiome($$3);
        boolean bl = $$6 = !($$1.getDifficulty() == Difficulty.PEACEFUL || !EntitySpawnReason.ignoresLightRequirements($$2) && !Drowned.isDarkEnoughToSpawn($$1, $$3, $$4) || !EntitySpawnReason.isSpawner($$2) && !$$1.getFluidState($$3).is(FluidTags.WATER));
        if ($$6 && (EntitySpawnReason.isSpawner($$2) || $$2 == EntitySpawnReason.REINFORCEMENT)) {
            return true;
        }
        if ($$5.is(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS)) {
            return $$4.nextInt(15) == 0 && $$6;
        }
        return $$4.nextInt(40) == 0 && Drowned.isDeepEnoughToSpawn($$1, $$3) && $$6;
    }

    private static boolean isDeepEnoughToSpawn(LevelAccessor $$0, BlockPos $$1) {
        return $$1.getY() < $$0.getSeaLevel() - 5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isInWater()) {
            return SoundEvents.DROWNED_AMBIENT_WATER;
        }
        return SoundEvents.DROWNED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        if (this.isInWater()) {
            return SoundEvents.DROWNED_HURT_WATER;
        }
        return SoundEvents.DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isInWater()) {
            return SoundEvents.DROWNED_DEATH_WATER;
        }
        return SoundEvents.DROWNED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.DROWNED_STEP;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.DROWNED_SWIM;
    }

    @Override
    protected boolean canSpawnInLiquids() {
        return true;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        if ((double)$$0.nextFloat() > 0.9) {
            int $$2 = $$0.nextInt(16);
            if ($$2 < 10) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            }
        }
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack $$0, ItemStack $$1, EquipmentSlot $$2) {
        if ($$1.is(Items.NAUTILUS_SHELL)) {
            return false;
        }
        return super.canReplaceCurrentItem($$0, $$1, $$2);
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader $$0) {
        return $$0.isUnobstructed(this);
    }

    public boolean okTarget(@Nullable LivingEntity $$0) {
        if ($$0 != null) {
            return !this.level().isBrightOutside() || $$0.isInWater();
        }
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return !this.isSwimming();
    }

    boolean wantsToSwim() {
        if (this.searchingForLand) {
            return true;
        }
        LivingEntity $$0 = this.getTarget();
        return $$0 != null && $$0.isInWater();
    }

    @Override
    protected void travelInWater(Vec3 $$0, double $$1, boolean $$2, double $$3) {
        if (this.isUnderWater() && this.wantsToSwim()) {
            this.moveRelative(0.01f, $$0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travelInWater($$0, $$1, $$2, $$3);
        }
    }

    @Override
    public void updateSwimming() {
        if (!this.level().isClientSide()) {
            this.setSwimming(this.isEffectiveAi() && this.isUnderWater() && this.wantsToSwim());
        }
    }

    @Override
    public boolean isVisuallySwimming() {
        return this.isSwimming() && !this.isPassenger();
    }

    protected boolean closeToNextPos() {
        double $$2;
        BlockPos $$1;
        Path $$0 = this.getNavigation().getPath();
        return $$0 != null && ($$1 = $$0.getTarget()) != null && ($$2 = this.distanceToSqr($$1.getX(), $$1.getY(), $$1.getZ())) < 4.0;
    }

    @Override
    public void performRangedAttack(LivingEntity $$0, float $$1) {
        ItemStack $$2 = this.getMainHandItem();
        ItemStack $$3 = $$2.is(Items.TRIDENT) ? $$2 : new ItemStack(Items.TRIDENT);
        ThrownTrident $$4 = new ThrownTrident(this.level(), this, $$3);
        double $$5 = $$0.getX() - this.getX();
        double $$6 = $$0.getY(0.3333333333333333) - $$4.getY();
        double $$7 = $$0.getZ() - this.getZ();
        double $$8 = Math.sqrt($$5 * $$5 + $$7 * $$7);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$9 = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot($$4, $$9, $$3, $$5, $$6 + $$8 * (double)0.2f, $$7, 1.6f, 14 - this.level().getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.DROWNED_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public TagKey<Item> getPreferredWeaponType() {
        return ItemTags.DROWNED_PREFERRED_WEAPONS;
    }

    public void setSearchingForLand(boolean $$0) {
        this.searchingForLand = $$0;
    }

    @Override
    public void rideTick() {
        super.rideTick();
        Entity entity = this.getControlledVehicle();
        if (entity instanceof PathfinderMob) {
            PathfinderMob $$0 = (PathfinderMob)entity;
            this.yBodyRot = $$0.yBodyRot;
        }
    }

    @Override
    public boolean wantsToPickUp(ServerLevel $$0, ItemStack $$1) {
        if ($$1.is(ItemTags.SPEARS)) {
            return false;
        }
        return super.wantsToPickUp($$0, $$1);
    }

    static class DrownedMoveControl
    extends MoveControl {
        private final Drowned drowned;

        public DrownedMoveControl(Drowned $$0) {
            super($$0);
            this.drowned = $$0;
        }

        @Override
        public void tick() {
            LivingEntity $$0 = this.drowned.getTarget();
            if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
                if ($$0 != null && $$0.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, 0.002, 0.0));
                }
                if (this.operation != MoveControl.Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
                    this.drowned.setSpeed(0.0f);
                    return;
                }
                double $$1 = this.wantedX - this.drowned.getX();
                double $$2 = this.wantedY - this.drowned.getY();
                double $$3 = this.wantedZ - this.drowned.getZ();
                double $$4 = Math.sqrt($$1 * $$1 + $$2 * $$2 + $$3 * $$3);
                $$2 /= $$4;
                float $$5 = (float)(Mth.atan2($$3, $$1) * 57.2957763671875) - 90.0f;
                this.drowned.setYRot(this.rotlerp(this.drowned.getYRot(), $$5, 90.0f));
                this.drowned.yBodyRot = this.drowned.getYRot();
                float $$6 = (float)(this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float $$7 = Mth.lerp(0.125f, this.drowned.getSpeed(), $$6);
                this.drowned.setSpeed($$7);
                this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add((double)$$7 * $$1 * 0.005, (double)$$7 * $$2 * 0.1, (double)$$7 * $$3 * 0.005));
            } else {
                if (!this.drowned.onGround()) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, -0.008, 0.0));
                }
                super.tick();
            }
        }
    }

    static class DrownedGoToWaterGoal
    extends Goal {
        private final PathfinderMob mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final Level level;

        public DrownedGoToWaterGoal(PathfinderMob $$0, double $$1) {
            this.mob = $$0;
            this.speedModifier = $$1;
            this.level = $$0.level();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.level.isBrightOutside()) {
                return false;
            }
            if (this.mob.isInWater()) {
                return false;
            }
            Vec3 $$0 = this.getWaterPos();
            if ($$0 == null) {
                return false;
            }
            this.wantedX = $$0.x;
            this.wantedY = $$0.y;
            this.wantedZ = $$0.z;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }

        private @Nullable Vec3 getWaterPos() {
            RandomSource $$0 = this.mob.getRandom();
            BlockPos $$1 = this.mob.blockPosition();
            for (int $$2 = 0; $$2 < 10; ++$$2) {
                BlockPos $$3 = $$1.offset($$0.nextInt(20) - 10, 2 - $$0.nextInt(8), $$0.nextInt(20) - 10);
                if (!this.level.getBlockState($$3).is(Blocks.WATER)) continue;
                return Vec3.atBottomCenterOf($$3);
            }
            return null;
        }
    }

    static class DrownedTridentAttackGoal
    extends RangedAttackGoal {
        private final Drowned drowned;

        public DrownedTridentAttackGoal(RangedAttackMob $$0, double $$1, int $$2, float $$3) {
            super($$0, $$1, $$2, $$3);
            this.drowned = (Drowned)$$0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.drowned.getMainHandItem().is(Items.TRIDENT);
        }

        @Override
        public void start() {
            super.start();
            this.drowned.setAggressive(true);
            this.drowned.startUsingItem(InteractionHand.MAIN_HAND);
        }

        @Override
        public void stop() {
            super.stop();
            this.drowned.stopUsingItem();
            this.drowned.setAggressive(false);
        }
    }

    static class DrownedAttackGoal
    extends ZombieAttackGoal {
        private final Drowned drowned;

        public DrownedAttackGoal(Drowned $$0, double $$1, boolean $$2) {
            super($$0, $$1, $$2);
            this.drowned = $$0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
        }
    }

    static class DrownedGoToBeachGoal
    extends MoveToBlockGoal {
        private final Drowned drowned;

        public DrownedGoToBeachGoal(Drowned $$0, double $$1) {
            super($$0, $$1, 8, 2);
            this.drowned = $$0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.drowned.level().isBrightOutside() && this.drowned.isInWater() && this.drowned.getY() >= (double)(this.drowned.level().getSeaLevel() - 3);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse();
        }

        @Override
        protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
            BlockPos $$2 = $$1.above();
            if (!$$0.isEmptyBlock($$2) || !$$0.isEmptyBlock($$2.above())) {
                return false;
            }
            return $$0.getBlockState($$1).entityCanStandOn($$0, $$1, this.drowned);
        }

        @Override
        public void start() {
            this.drowned.setSearchingForLand(false);
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
        }
    }

    static class DrownedSwimUpGoal
    extends Goal {
        private final Drowned drowned;
        private final double speedModifier;
        private final int seaLevel;
        private boolean stuck;

        public DrownedSwimUpGoal(Drowned $$0, double $$1, int $$2) {
            this.drowned = $$0;
            this.speedModifier = $$1;
            this.seaLevel = $$2;
        }

        @Override
        public boolean canUse() {
            return !this.drowned.level().isBrightOutside() && this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel - 2);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && !this.stuck;
        }

        @Override
        public void tick() {
            if (this.drowned.getY() < (double)(this.seaLevel - 1) && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
                Vec3 $$0 = DefaultRandomPos.getPosTowards(this.drowned, 4, 8, new Vec3(this.drowned.getX(), this.seaLevel - 1, this.drowned.getZ()), 1.5707963705062866);
                if ($$0 == null) {
                    this.stuck = true;
                    return;
                }
                this.drowned.getNavigation().moveTo($$0.x, $$0.y, $$0.z, this.speedModifier);
            }
        }

        @Override
        public void start() {
            this.drowned.setSearchingForLand(true);
            this.stuck = false;
        }

        @Override
        public void stop() {
            this.drowned.setSearchingForLand(false);
        }
    }
}

