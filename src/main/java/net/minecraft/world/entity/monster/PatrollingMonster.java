package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class PatrollingMonster extends Monster {
   private static final boolean DEFAULT_PATROL_LEADER = false;
   private static final boolean DEFAULT_PATROLLING = false;
   @Nullable
   private BlockPos patrolTarget;
   private boolean patrolLeader = false;
   private boolean patrolling = false;

   protected PatrollingMonster(net.minecraft.world.entity.EntityType<? extends PatrollingMonster> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(4, new PatrollingMonster.LongDistancePatrolGoal<>(this, 0.7, 0.595));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.storeNullable("patrol_target", BlockPos.CODEC, this.patrolTarget);
      $$0.putBoolean("PatrolLeader", this.patrolLeader);
      $$0.putBoolean("Patrolling", this.patrolling);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.patrolTarget = (BlockPos)$$0.read("patrol_target", BlockPos.CODEC).orElse(null);
      this.patrolLeader = $$0.getBooleanOr("PatrolLeader", false);
      this.patrolling = $$0.getBooleanOr("Patrolling", false);
   }

   public boolean canBeLeader() {
      return true;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$2 != net.minecraft.world.entity.EntitySpawnReason.PATROL
         && $$2 != net.minecraft.world.entity.EntitySpawnReason.EVENT
         && $$2 != net.minecraft.world.entity.EntitySpawnReason.STRUCTURE
         && $$0.getRandom().nextFloat() < 0.06F
         && this.canBeLeader()) {
         this.patrolLeader = true;
      }

      if (this.isPatrolLeader()) {
         this.setItemSlot(
            net.minecraft.world.entity.EquipmentSlot.HEAD, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN))
         );
         this.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 2.0F);
      }

      if ($$2 == net.minecraft.world.entity.EntitySpawnReason.PATROL) {
         this.patrolling = true;
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public static boolean checkPatrollingMonsterSpawnRules(
      net.minecraft.world.entity.EntityType<? extends PatrollingMonster> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getBrightness(LightLayer.BLOCK, $$3) > 8 ? false : checkAnyLightMonsterSpawnRules($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return !this.patrolling || $$0 > 16384.0;
   }

   public void setPatrolTarget(BlockPos $$0) {
      this.patrolTarget = $$0;
      this.patrolling = true;
   }

   @Nullable
   public BlockPos getPatrolTarget() {
      return this.patrolTarget;
   }

   public boolean hasPatrolTarget() {
      return this.patrolTarget != null;
   }

   public void setPatrolLeader(boolean $$0) {
      this.patrolLeader = $$0;
      this.patrolling = true;
   }

   public boolean isPatrolLeader() {
      return this.patrolLeader;
   }

   public boolean canJoinPatrol() {
      return true;
   }

   public void findPatrolTarget() {
      this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
      this.patrolling = true;
   }

   protected boolean isPatrolling() {
      return this.patrolling;
   }

   protected void setPatrolling(boolean $$0) {
      this.patrolling = $$0;
   }

   public static class LongDistancePatrolGoal<T extends PatrollingMonster> extends Goal {
      private static final int NAVIGATION_FAILED_COOLDOWN = 200;
      private final T mob;
      private final double speedModifier;
      private final double leaderSpeedModifier;
      private long cooldownUntil;

      public LongDistancePatrolGoal(T $$0, double $$1, double $$2) {
         this.mob = $$0;
         this.speedModifier = $$1;
         this.leaderSpeedModifier = $$2;
         this.cooldownUntil = -1L;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         boolean $$0 = this.mob.level().getGameTime() < this.cooldownUntil;
         return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.hasControllingPassenger() && this.mob.hasPatrolTarget() && !$$0;
      }

      @Override
      public void start() {
      }

      @Override
      public void stop() {
      }

      @Override
      public void tick() {
         boolean $$0 = this.mob.isPatrolLeader();
         PathNavigation $$1 = this.mob.getNavigation();
         if ($$1.isDone()) {
            List<PatrollingMonster> $$2 = this.findPatrolCompanions();
            if (this.mob.isPatrolling() && $$2.isEmpty()) {
               this.mob.setPatrolling(false);
            } else if ($$0 && this.mob.getPatrolTarget().closerToCenterThan(this.mob.position(), 10.0)) {
               this.mob.findPatrolTarget();
            } else {
               Vec3 $$3 = Vec3.atBottomCenterOf(this.mob.getPatrolTarget());
               Vec3 $$4 = this.mob.position();
               Vec3 $$5 = $$4.subtract($$3);
               $$3 = $$5.yRot(90.0F).scale(0.4).add($$3);
               Vec3 $$6 = $$3.subtract($$4).normalize().scale(10.0).add($$4);
               BlockPos $$7 = BlockPos.containing($$6);
               $$7 = this.mob.level().getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, $$7);
               if (!$$1.moveTo($$7.getX(), $$7.getY(), $$7.getZ(), $$0 ? this.leaderSpeedModifier : this.speedModifier)) {
                  this.moveRandomly();
                  this.cooldownUntil = this.mob.level().getGameTime() + 200L;
               } else if ($$0) {
                  for (PatrollingMonster $$8 : $$2) {
                     $$8.setPatrolTarget($$7);
                  }
               }
            }
         }
      }

      private List<PatrollingMonster> findPatrolCompanions() {
         return this.mob
            .level()
            .getEntitiesOfClass(PatrollingMonster.class, this.mob.getBoundingBox().inflate(16.0), $$0 -> $$0.canJoinPatrol() && !$$0.is(this.mob));
      }

      private boolean moveRandomly() {
         RandomSource $$0 = this.mob.getRandom();
         BlockPos $$1 = this.mob
            .level()
            .getHeightmapPos(Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + $$0.nextInt(16), 0, -8 + $$0.nextInt(16)));
         return this.mob.getNavigation().moveTo($$1.getX(), $$1.getY(), $$1.getZ(), this.speedModifier);
      }
   }
}
