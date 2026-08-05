/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Ghast
extends Mob
implements Enemy {
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(Ghast.class, EntityDataSerializers.BOOLEAN);
    private static final byte DEFAULT_EXPLOSION_POWER = 1;
    private int explosionPower = 1;

    public Ghast(EntityType<? extends Ghast> $$0, Level $$1) {
        super((EntityType<? extends Mob>)$$0, $$1);
        this.xpReward = 5;
        this.moveControl = new GhastMoveControl(this, false, () -> false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new RandomFloatAroundGoal(this));
        this.goalSelector.addGoal(7, new GhastLookGoal(this));
        this.goalSelector.addGoal(7, new GhastShootFireballGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, ($$0, $$1) -> Math.abs($$0.getY() - this.getY()) <= 4.0));
    }

    public boolean isCharging() {
        return this.entityData.get(DATA_IS_CHARGING);
    }

    public void setCharging(boolean $$0) {
        this.entityData.set(DATA_IS_CHARGING, $$0);
    }

    public int getExplosionPower() {
        return this.explosionPower;
    }

    private static boolean isReflectedFireball(DamageSource $$0) {
        return $$0.getDirectEntity() instanceof LargeFireball && $$0.getEntity() instanceof Player;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel $$0, DamageSource $$1) {
        return this.isInvulnerable() && !$$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !Ghast.isReflectedFireball($$1) && super.isInvulnerableTo($$0, $$1);
    }

    @Override
    protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 $$0) {
        this.travelFlying($$0, 0.02f);
    }

    @Override
    public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
        if (Ghast.isReflectedFireball($$1)) {
            super.hurtServer($$0, $$1, 1000.0f);
            return true;
        }
        if (this.isInvulnerableTo($$0, $$1)) {
            return false;
        }
        return super.hurtServer($$0, $$1, $$2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_IS_CHARGING, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FOLLOW_RANGE, 100.0).add(Attributes.CAMERA_DISTANCE, 8.0).add(Attributes.FLYING_SPEED, 0.06);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public static boolean checkGhastSpawnRules(EntityType<Ghast> $$0, LevelAccessor $$1, EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4) {
        return $$1.getDifficulty() != Difficulty.PEACEFUL && $$4.nextInt(20) == 0 && Ghast.checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putByte("ExplosionPower", (byte)this.explosionPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.explosionPower = $$0.getByteOr("ExplosionPower", (byte)1);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0;
    }

    public static void faceMovementDirection(Mob $$0) {
        if ($$0.getTarget() == null) {
            Vec3 $$1 = $$0.getDeltaMovement();
            $$0.setYRot(-((float)Mth.atan2($$1.x, $$1.z)) * 57.295776f);
            $$0.yBodyRot = $$0.getYRot();
        } else {
            LivingEntity $$2 = $$0.getTarget();
            double $$3 = 64.0;
            if ($$2.distanceToSqr($$0) < 4096.0) {
                double $$4 = $$2.getX() - $$0.getX();
                double $$5 = $$2.getZ() - $$0.getZ();
                $$0.setYRot(-((float)Mth.atan2($$4, $$5)) * 57.295776f);
                $$0.yBodyRot = $$0.getYRot();
            }
        }
    }

    public static class GhastMoveControl
    extends MoveControl {
        private final Mob ghast;
        private int floatDuration;
        private final boolean careful;
        private final BooleanSupplier shouldBeStopped;

        public GhastMoveControl(Mob $$0, boolean $$1, BooleanSupplier $$2) {
            super($$0);
            this.ghast = $$0;
            this.careful = $$1;
            this.shouldBeStopped = $$2;
        }

        @Override
        public void tick() {
            if (this.shouldBeStopped.getAsBoolean()) {
                this.operation = MoveControl.Operation.WAIT;
                this.ghast.stopInPlace();
            }
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                return;
            }
            if (this.floatDuration-- <= 0) {
                this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
                Vec3 $$0 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
                if (this.canReach($$0)) {
                    this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add($$0.normalize().scale(this.ghast.getAttributeValue(Attributes.FLYING_SPEED) * 5.0 / 3.0)));
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }

        private boolean canReach(Vec3 $$0) {
            AABB $$1 = this.ghast.getBoundingBox();
            AABB $$2 = $$1.move($$0);
            if (this.careful) {
                for (BlockPos $$3 : BlockPos.betweenClosed($$2.inflate(1.0))) {
                    if (this.blockTraversalPossible(this.ghast.level(), null, null, $$3, false, false)) continue;
                    return false;
                }
            }
            boolean $$4 = this.ghast.isInWater();
            boolean $$52 = this.ghast.isInLava();
            Vec3 $$62 = this.ghast.position();
            Vec3 $$7 = $$62.add($$0);
            return BlockGetter.forEachBlockIntersectedBetween($$62, $$7, $$2, ($$5, $$6) -> {
                if ($$1.intersects($$5)) {
                    return true;
                }
                return this.blockTraversalPossible(this.ghast.level(), $$62, $$7, $$5, $$4, $$52);
            });
        }

        private boolean blockTraversalPossible(BlockGetter $$0, @Nullable Vec3 $$1, @Nullable Vec3 $$2, BlockPos $$3, boolean $$4, boolean $$5) {
            boolean $$8;
            boolean $$7;
            BlockState $$6 = $$0.getBlockState($$3);
            if ($$6.isAir()) {
                return true;
            }
            boolean bl = $$7 = $$1 != null && $$2 != null;
            boolean bl2 = $$7 ? !this.ghast.collidedWithShapeMovingFrom($$1, $$2, $$6.getCollisionShape($$0, $$3).move(new Vec3($$3)).toAabbs()) : ($$8 = $$6.getCollisionShape($$0, $$3).isEmpty());
            if (!this.careful) {
                return $$8;
            }
            if ($$6.is(BlockTags.HAPPY_GHAST_AVOIDS)) {
                return false;
            }
            FluidState $$9 = $$0.getFluidState($$3);
            if (!($$9.isEmpty() || $$7 && !this.ghast.collidedWithFluid($$9, $$3, $$1, $$2))) {
                if ($$9.is(FluidTags.WATER)) {
                    return $$4;
                }
                if ($$9.is(FluidTags.LAVA)) {
                    return $$5;
                }
            }
            return $$8;
        }
    }

    public static class RandomFloatAroundGoal
    extends Goal {
        private static final int MAX_ATTEMPTS = 64;
        private final Mob ghast;
        private final int distanceToBlocks;

        public RandomFloatAroundGoal(Mob $$0) {
            this($$0, 0);
        }

        public RandomFloatAroundGoal(Mob $$0, int $$1) {
            this.ghast = $$0;
            this.distanceToBlocks = $$1;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            double $$3;
            double $$2;
            MoveControl $$0 = this.ghast.getMoveControl();
            if (!$$0.hasWanted()) {
                return true;
            }
            double $$1 = $$0.getWantedX() - this.ghast.getX();
            double $$4 = $$1 * $$1 + ($$2 = $$0.getWantedY() - this.ghast.getY()) * $$2 + ($$3 = $$0.getWantedZ() - this.ghast.getZ()) * $$3;
            return $$4 < 1.0 || $$4 > 3600.0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Vec3 $$0 = RandomFloatAroundGoal.getSuitableFlyToPosition(this.ghast, this.distanceToBlocks);
            this.ghast.getMoveControl().setWantedPosition($$0.x(), $$0.y(), $$0.z(), 1.0);
        }

        public static Vec3 getSuitableFlyToPosition(Mob $$0, int $$1) {
            BlockPos $$7;
            int $$8;
            Level $$2 = $$0.level();
            RandomSource $$3 = $$0.getRandom();
            Vec3 $$4 = $$0.position();
            Vec3 $$5 = null;
            for (int $$6 = 0; $$6 < 64; ++$$6) {
                $$5 = RandomFloatAroundGoal.chooseRandomPositionWithRestriction($$0, $$4, $$3);
                if ($$5 == null || !RandomFloatAroundGoal.isGoodTarget($$2, $$5, $$1)) continue;
                return $$5;
            }
            if ($$5 == null) {
                $$5 = RandomFloatAroundGoal.chooseRandomPosition($$4, $$3);
            }
            if (($$8 = $$2.getHeight(Heightmap.Types.MOTION_BLOCKING, ($$7 = BlockPos.containing($$5)).getX(), $$7.getZ())) < $$7.getY() && $$8 > $$2.getMinY()) {
                $$5 = new Vec3($$5.x(), $$0.getY() - Math.abs($$0.getY() - $$5.y()), $$5.z());
            }
            return $$5;
        }

        private static boolean isGoodTarget(Level $$0, Vec3 $$1, int $$2) {
            if ($$2 <= 0) {
                return true;
            }
            BlockPos $$3 = BlockPos.containing($$1);
            if (!$$0.getBlockState($$3).isAir()) {
                return false;
            }
            for (Direction $$4 : Direction.values()) {
                for (int $$5 = 1; $$5 < $$2; ++$$5) {
                    BlockPos $$6 = $$3.relative($$4, $$5);
                    if ($$0.getBlockState($$6).isAir()) continue;
                    return true;
                }
            }
            return false;
        }

        private static Vec3 chooseRandomPosition(Vec3 $$0, RandomSource $$1) {
            double $$2 = $$0.x() + (double)(($$1.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double $$3 = $$0.y() + (double)(($$1.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double $$4 = $$0.z() + (double)(($$1.nextFloat() * 2.0f - 1.0f) * 16.0f);
            return new Vec3($$2, $$3, $$4);
        }

        private static @Nullable Vec3 chooseRandomPositionWithRestriction(Mob $$0, Vec3 $$1, RandomSource $$2) {
            Vec3 $$3 = RandomFloatAroundGoal.chooseRandomPosition($$1, $$2);
            if ($$0.hasHome() && !$$0.isWithinHome($$3)) {
                return null;
            }
            return $$3;
        }
    }

    public static class GhastLookGoal
    extends Goal {
        private final Mob ghast;

        public GhastLookGoal(Mob $$0) {
            this.ghast = $$0;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            Ghast.faceMovementDirection(this.ghast);
        }
    }

    static class GhastShootFireballGoal
    extends Goal {
        private final Ghast ghast;
        public int chargeTime;

        public GhastShootFireballGoal(Ghast $$0) {
            this.ghast = $$0;
        }

        @Override
        public boolean canUse() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.chargeTime = 0;
        }

        @Override
        public void stop() {
            this.ghast.setCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity $$0 = this.ghast.getTarget();
            if ($$0 == null) {
                return;
            }
            double $$1 = 64.0;
            if ($$0.distanceToSqr(this.ghast) < 4096.0 && this.ghast.hasLineOfSight($$0)) {
                Level $$2 = this.ghast.level();
                ++this.chargeTime;
                if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                    $$2.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
                }
                if (this.chargeTime == 20) {
                    double $$3 = 4.0;
                    Vec3 $$4 = this.ghast.getViewVector(1.0f);
                    double $$5 = $$0.getX() - (this.ghast.getX() + $$4.x * 4.0);
                    double $$6 = $$0.getY(0.5) - (0.5 + this.ghast.getY(0.5));
                    double $$7 = $$0.getZ() - (this.ghast.getZ() + $$4.z * 4.0);
                    Vec3 $$8 = new Vec3($$5, $$6, $$7);
                    if (!this.ghast.isSilent()) {
                        $$2.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                    }
                    LargeFireball $$9 = new LargeFireball($$2, (LivingEntity)this.ghast, $$8.normalize(), this.ghast.getExplosionPower());
                    $$9.setPos(this.ghast.getX() + $$4.x * 4.0, this.ghast.getY(0.5) + 0.5, $$9.getZ() + $$4.z * 4.0);
                    $$2.addFreshEntity($$9);
                    this.chargeTime = -40;
                }
            } else if (this.chargeTime > 0) {
                --this.chargeTime;
            }
            this.ghast.setCharging(this.chargeTime > 10);
        }
    }
}

