package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Ghast extends net.minecraft.world.entity.Mob implements Enemy {
   private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(Ghast.class, EntityDataSerializers.BOOLEAN);
   private static final byte DEFAULT_EXPLOSION_POWER = 1;
   private int explosionPower = 1;

   public Ghast(net.minecraft.world.entity.EntityType<? extends Ghast> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 5;
      this.moveControl = new Ghast.GhastMoveControl(this, false, () -> false);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this));
      this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
      this.goalSelector.addGoal(7, new Ghast.GhastShootFireballGoal(this));
      this.targetSelector
         .addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, ($$0, $$1) -> Math.abs($$0.getY() - this.getY()) <= 4.0));
   }

   public boolean isCharging() {
      return (Boolean)this.entityData.get(DATA_IS_CHARGING);
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
      return this.isInvulnerable() && !$$1.is(DamageTypeTags.BYPASSES_INVULNERABILITY) || !isReflectedFireball($$1) && super.isInvulnerableTo($$0, $$1);
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
      this.travelFlying($$0, 0.02F);
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (isReflectedFireball($$1)) {
         super.hurtServer($$0, $$1, 1000.0F);
         return true;
      } else {
         return this.isInvulnerableTo($$0, $$1) ? false : super.hurtServer($$0, $$1, $$2);
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_IS_CHARGING, false);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes()
         .add(Attributes.MAX_HEALTH, 10.0)
         .add(Attributes.FOLLOW_RANGE, 100.0)
         .add(Attributes.CAMERA_DISTANCE, 8.0)
         .add(Attributes.FLYING_SPEED, 0.06);
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
      return 5.0F;
   }

   public static boolean checkGhastSpawnRules(
      net.minecraft.world.entity.EntityType<Ghast> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      return $$1.getDifficulty() != Difficulty.PEACEFUL && $$4.nextInt(20) == 0 && checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
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

   public static void faceMovementDirection(net.minecraft.world.entity.Mob $$0) {
      if ($$0.getTarget() == null) {
         Vec3 $$1 = $$0.getDeltaMovement();
         $$0.setYRot(-((float)Mth.atan2($$1.x, $$1.z)) * (180.0F / (float)Math.PI));
         $$0.yBodyRot = $$0.getYRot();
      } else {
         net.minecraft.world.entity.LivingEntity $$2 = $$0.getTarget();
         double $$3 = 64.0;
         if ($$2.distanceToSqr($$0) < 4096.0) {
            double $$4 = $$2.getX() - $$0.getX();
            double $$5 = $$2.getZ() - $$0.getZ();
            $$0.setYRot(-((float)Mth.atan2($$4, $$5)) * (180.0F / (float)Math.PI));
            $$0.yBodyRot = $$0.getYRot();
         }
      }
   }

   public static class GhastLookGoal extends Goal {
      private final net.minecraft.world.entity.Mob ghast;

      public GhastLookGoal(net.minecraft.world.entity.Mob $$0) {
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

   public static class GhastMoveControl extends MoveControl {
      private final net.minecraft.world.entity.Mob ghast;
      private int floatDuration;
      private final boolean careful;
      private final BooleanSupplier shouldBeStopped;

      public GhastMoveControl(net.minecraft.world.entity.Mob $$0, boolean $$1, BooleanSupplier $$2) {
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

         if (this.operation == MoveControl.Operation.MOVE_TO) {
            if (this.floatDuration-- <= 0) {
               this.floatDuration = this.floatDuration + this.ghast.getRandom().nextInt(5) + 2;
               Vec3 $$0 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
               if (this.canReach($$0)) {
                  this.ghast
                     .setDeltaMovement(
                        this.ghast.getDeltaMovement().add($$0.normalize().scale(this.ghast.getAttributeValue(Attributes.FLYING_SPEED) * 5.0 / 3.0))
                     );
               } else {
                  this.operation = MoveControl.Operation.WAIT;
               }
            }
         }
      }

      private boolean canReach(Vec3 $$0) {
         AABB $$1 = this.ghast.getBoundingBox();
         AABB $$2 = $$1.move($$0);
         if (this.careful) {
            for (BlockPos $$3 : BlockPos.betweenClosed($$2.inflate(1.0))) {
               if (!this.blockTraversalPossible(this.ghast.level(), null, null, $$3, false, false)) {
                  return false;
               }
            }
         }

         boolean $$4 = this.ghast.isInWater();
         boolean $$5 = this.ghast.isInLava();
         Vec3 $$6 = this.ghast.position();
         Vec3 $$7 = $$6.add($$0);
         return BlockGetter.forEachBlockIntersectedBetween(
            $$6, $$7, $$2, ($$5x, $$6x) -> $$1.intersects($$5x) ? true : this.blockTraversalPossible(this.ghast.level(), $$6, $$7, $$5x, $$4, $$5)
         );
      }

      private boolean blockTraversalPossible(BlockGetter $$0, @Nullable Vec3 $$1, @Nullable Vec3 $$2, BlockPos $$3, boolean $$4, boolean $$5) {
         BlockState $$6 = $$0.getBlockState($$3);
         if ($$6.isAir()) {
            return true;
         } else {
            boolean $$7 = $$1 != null && $$2 != null;
            boolean $$8 = $$7
               ? !this.ghast.collidedWithShapeMovingFrom($$1, $$2, $$6.getCollisionShape($$0, $$3).move(new Vec3($$3)).toAabbs())
               : $$6.getCollisionShape($$0, $$3).isEmpty();
            if (!this.careful) {
               return $$8;
            } else if ($$6.is(BlockTags.HAPPY_GHAST_AVOIDS)) {
               return false;
            } else {
               FluidState $$9 = $$0.getFluidState($$3);
               if (!$$9.isEmpty() && (!$$7 || this.ghast.collidedWithFluid($$9, $$3, $$1, $$2))) {
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
      }
   }

   static class GhastShootFireballGoal extends Goal {
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
         net.minecraft.world.entity.LivingEntity $$0 = this.ghast.getTarget();
         if ($$0 != null) {
            double $$1 = 64.0;
            if ($$0.distanceToSqr(this.ghast) < 4096.0 && this.ghast.hasLineOfSight($$0)) {
               Level $$2 = this.ghast.level();
               this.chargeTime++;
               if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                  $$2.levelEvent(null, 1015, this.ghast.blockPosition(), 0);
               }

               if (this.chargeTime == 20) {
                  double $$3 = 4.0;
                  Vec3 $$4 = this.ghast.getViewVector(1.0F);
                  double $$5 = $$0.getX() - (this.ghast.getX() + $$4.x * 4.0);
                  double $$6 = $$0.getY(0.5) - (0.5 + this.ghast.getY(0.5));
                  double $$7 = $$0.getZ() - (this.ghast.getZ() + $$4.z * 4.0);
                  Vec3 $$8 = new Vec3($$5, $$6, $$7);
                  if (!this.ghast.isSilent()) {
                     $$2.levelEvent(null, 1016, this.ghast.blockPosition(), 0);
                  }

                  LargeFireball $$9 = new LargeFireball($$2, this.ghast, $$8.normalize(), this.ghast.getExplosionPower());
                  $$9.setPos(this.ghast.getX() + $$4.x * 4.0, this.ghast.getY(0.5) + 0.5, $$9.getZ() + $$4.z * 4.0);
                  $$2.addFreshEntity($$9);
                  this.chargeTime = -40;
               }
            } else if (this.chargeTime > 0) {
               this.chargeTime--;
            }

            this.ghast.setCharging(this.chargeTime > 10);
         }
      }
   }

   public static class RandomFloatAroundGoal extends Goal {
      private static final int MAX_ATTEMPTS = 64;
      private final net.minecraft.world.entity.Mob ghast;
      private final int distanceToBlocks;

      public RandomFloatAroundGoal(net.minecraft.world.entity.Mob $$0) {
         this($$0, 0);
      }

      public RandomFloatAroundGoal(net.minecraft.world.entity.Mob $$0, int $$1) {
         this.ghast = $$0;
         this.distanceToBlocks = $$1;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         MoveControl $$0 = this.ghast.getMoveControl();
         if (!$$0.hasWanted()) {
            return true;
         } else {
            double $$1 = $$0.getWantedX() - this.ghast.getX();
            double $$2 = $$0.getWantedY() - this.ghast.getY();
            double $$3 = $$0.getWantedZ() - this.ghast.getZ();
            double $$4 = $$1 * $$1 + $$2 * $$2 + $$3 * $$3;
            return $$4 < 1.0 || $$4 > 3600.0;
         }
      }

      @Override
      public boolean canContinueToUse() {
         return false;
      }

      @Override
      public void start() {
         Vec3 $$0 = getSuitableFlyToPosition(this.ghast, this.distanceToBlocks);
         this.ghast.getMoveControl().setWantedPosition($$0.x(), $$0.y(), $$0.z(), 1.0);
      }

      public static Vec3 getSuitableFlyToPosition(net.minecraft.world.entity.Mob $$0, int $$1) {
         Level $$2 = $$0.level();
         RandomSource $$3 = $$0.getRandom();
         Vec3 $$4 = $$0.position();
         Vec3 $$5 = null;

         for (int $$6 = 0; $$6 < 64; $$6++) {
            $$5 = chooseRandomPositionWithRestriction($$0, $$4, $$3);
            if ($$5 != null && isGoodTarget($$2, $$5, $$1)) {
               return $$5;
            }
         }

         if ($$5 == null) {
            $$5 = chooseRandomPosition($$4, $$3);
         }

         BlockPos $$7 = BlockPos.containing($$5);
         int $$8 = $$2.getHeight(Types.MOTION_BLOCKING, $$7.getX(), $$7.getZ());
         if ($$8 < $$7.getY() && $$8 > $$2.getMinY()) {
            $$5 = new Vec3($$5.x(), $$0.getY() - Math.abs($$0.getY() - $$5.y()), $$5.z());
         }

         return $$5;
      }

      private static boolean isGoodTarget(Level $$0, Vec3 $$1, int $$2) {
         if ($$2 <= 0) {
            return true;
         } else {
            BlockPos $$3 = BlockPos.containing($$1);
            if (!$$0.getBlockState($$3).isAir()) {
               return false;
            } else {
               for (Direction $$4 : Direction.values()) {
                  for (int $$5 = 1; $$5 < $$2; $$5++) {
                     BlockPos $$6 = $$3.relative($$4, $$5);
                     if (!$$0.getBlockState($$6).isAir()) {
                        return true;
                     }
                  }
               }

               return false;
            }
         }
      }

      private static Vec3 chooseRandomPosition(Vec3 $$0, RandomSource $$1) {
         double $$2 = $$0.x() + ($$1.nextFloat() * 2.0F - 1.0F) * 16.0F;
         double $$3 = $$0.y() + ($$1.nextFloat() * 2.0F - 1.0F) * 16.0F;
         double $$4 = $$0.z() + ($$1.nextFloat() * 2.0F - 1.0F) * 16.0F;
         return new Vec3($$2, $$3, $$4);
      }

      @Nullable
      private static Vec3 chooseRandomPositionWithRestriction(net.minecraft.world.entity.Mob $$0, Vec3 $$1, RandomSource $$2) {
         Vec3 $$3 = chooseRandomPosition($$1, $$2);
         return $$0.hasHome() && !$$0.isWithinHome($$3) ? null : $$3;
      }
   }
}
