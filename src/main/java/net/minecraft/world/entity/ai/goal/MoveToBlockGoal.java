package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.LevelReader;

public abstract class MoveToBlockGoal extends Goal {
   private static final int GIVE_UP_TICKS = 1200;
   private static final int STAY_TICKS = 1200;
   private static final int INTERVAL_TICKS = 200;
   protected final net.minecraft.world.entity.PathfinderMob mob;
   public final double speedModifier;
   protected int nextStartTick;
   protected int tryTicks;
   private int maxStayTicks;
   protected BlockPos blockPos = BlockPos.ZERO;
   private boolean reachedTarget;
   private final int searchRange;
   private final int verticalSearchRange;
   protected int verticalSearchStart;

   public MoveToBlockGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, int $$2) {
      this($$0, $$1, $$2, 1);
   }

   public MoveToBlockGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1, int $$2, int $$3) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.searchRange = $$2;
      this.verticalSearchStart = 0;
      this.verticalSearchRange = $$3;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
   }

   @Override
   public boolean canUse() {
      if (this.nextStartTick > 0) {
         this.nextStartTick--;
         return false;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         return this.findNearestBlock();
      }
   }

   protected int nextStartTick(net.minecraft.world.entity.PathfinderMob $$0) {
      return reducedTickDelay(200 + $$0.getRandom().nextInt(200));
   }

   @Override
   public boolean canContinueToUse() {
      return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.mob.level(), this.blockPos);
   }

   @Override
   public void start() {
      this.moveMobToBlock();
      this.tryTicks = 0;
      this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
   }

   protected void moveMobToBlock() {
      this.mob.getNavigation().moveTo(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5, this.speedModifier);
   }

   public double acceptedDistance() {
      return 1.0;
   }

   protected BlockPos getMoveToTarget() {
      return this.blockPos.above();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      BlockPos $$0 = this.getMoveToTarget();
      if (!$$0.closerToCenterThan(this.mob.position(), this.acceptedDistance())) {
         this.reachedTarget = false;
         this.tryTicks++;
         if (this.shouldRecalculatePath()) {
            this.mob.getNavigation().moveTo($$0.getX() + 0.5, $$0.getY(), $$0.getZ() + 0.5, this.speedModifier);
         }
      } else {
         this.reachedTarget = true;
         this.tryTicks--;
      }
   }

   public boolean shouldRecalculatePath() {
      return this.tryTicks % 40 == 0;
   }

   protected boolean isReachedTarget() {
      return this.reachedTarget;
   }

   protected boolean findNearestBlock() {
      int $$0 = this.searchRange;
      int $$1 = this.verticalSearchRange;
      BlockPos $$2 = this.mob.blockPosition();
      MutableBlockPos $$3 = new MutableBlockPos();

      for (int $$4 = this.verticalSearchStart; $$4 <= $$1; $$4 = $$4 > 0 ? -$$4 : 1 - $$4) {
         for (int $$5 = 0; $$5 < $$0; $$5++) {
            for (int $$6 = 0; $$6 <= $$5; $$6 = $$6 > 0 ? -$$6 : 1 - $$6) {
               for (int $$7 = $$6 < $$5 && $$6 > -$$5 ? $$5 : 0; $$7 <= $$5; $$7 = $$7 > 0 ? -$$7 : 1 - $$7) {
                  $$3.setWithOffset($$2, $$6, $$4 - 1, $$7);
                  if (this.mob.isWithinHome($$3) && this.isValidTarget(this.mob.level(), $$3)) {
                     this.blockPos = $$3;
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   protected abstract boolean isValidTarget(LevelReader var1, BlockPos var2);
}
