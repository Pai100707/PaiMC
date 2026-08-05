package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FleeSunGoal extends Goal {
   protected final net.minecraft.world.entity.PathfinderMob mob;
   private double wantedX;
   private double wantedY;
   private double wantedZ;
   private final double speedModifier;
   private final Level level;

   public FleeSunGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
      this.mob = $$0;
      this.speedModifier = $$1;
      this.level = $$0.level();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.mob.getTarget() != null) {
         return false;
      } else if (!this.level.isBrightOutside()) {
         return false;
      } else if (!this.mob.isOnFire()) {
         return false;
      } else if (!this.level.canSeeSky(this.mob.blockPosition())) {
         return false;
      } else {
         return !this.mob.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD).isEmpty() ? false : this.setWantedPos();
      }
   }

   protected boolean setWantedPos() {
      Vec3 $$0 = this.getHidePos();
      if ($$0 == null) {
         return false;
      } else {
         this.wantedX = $$0.x;
         this.wantedY = $$0.y;
         this.wantedZ = $$0.z;
         return true;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone();
   }

   @Override
   public void start() {
      this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
   }

   
   protected Vec3 getHidePos() {
      RandomSource $$0 = this.mob.getRandom();
      BlockPos $$1 = this.mob.blockPosition();

      for (int $$2 = 0; $$2 < 10; $$2++) {
         BlockPos $$3 = $$1.offset($$0.nextInt(20) - 10, $$0.nextInt(6) - 3, $$0.nextInt(20) - 10);
         if (!this.level.canSeeSky($$3) && this.mob.getWalkTargetValue($$3) < 0.0F) {
            return Vec3.atBottomCenterOf($$3);
         }
      }

      return null;
   }
}
