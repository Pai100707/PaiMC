package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class DoorInteractGoal extends Goal {
   protected net.minecraft.world.entity.Mob mob;
   protected BlockPos doorPos = BlockPos.ZERO;
   protected boolean hasDoor;
   private boolean passed;
   private float doorOpenDirX;
   private float doorOpenDirZ;

   public DoorInteractGoal(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
      if (!GoalUtils.hasGroundPathNavigation($$0)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   protected boolean isOpen() {
      if (!this.hasDoor) {
         return false;
      } else {
         BlockState $$0 = this.mob.level().getBlockState(this.doorPos);
         if (!($$0.getBlock() instanceof DoorBlock)) {
            this.hasDoor = false;
            return false;
         } else {
            return (Boolean)$$0.getValue(DoorBlock.OPEN);
         }
      }
   }

   protected void setOpen(boolean $$0) {
      if (this.hasDoor) {
         BlockState $$1 = this.mob.level().getBlockState(this.doorPos);
         if ($$1.getBlock() instanceof DoorBlock) {
            ((DoorBlock)$$1.getBlock()).setOpen(this.mob, this.mob.level(), $$1, this.doorPos, $$0);
         }
      }
   }

   @Override
   public boolean canUse() {
      if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
         return false;
      } else if (!this.mob.horizontalCollision) {
         return false;
      } else {
         Path $$0 = this.mob.getNavigation().getPath();
         if ($$0 != null && !$$0.isDone()) {
            for (int $$1 = 0; $$1 < Math.min($$0.getNextNodeIndex() + 2, $$0.getNodeCount()); $$1++) {
               Node $$2 = $$0.getNode($$1);
               this.doorPos = new BlockPos($$2.x, $$2.y + 1, $$2.z);
               if (!(this.mob.distanceToSqr(this.doorPos.getX(), this.mob.getY(), this.doorPos.getZ()) > 2.25)) {
                  this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level(), this.doorPos);
                  if (this.hasDoor) {
                     return true;
                  }
               }
            }

            this.doorPos = this.mob.blockPosition().above();
            this.hasDoor = DoorBlock.isWoodenDoor(this.mob.level(), this.doorPos);
            return this.hasDoor;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.passed;
   }

   @Override
   public void start() {
      this.passed = false;
      this.doorOpenDirX = (float)(this.doorPos.getX() + 0.5 - this.mob.getX());
      this.doorOpenDirZ = (float)(this.doorPos.getZ() + 0.5 - this.mob.getZ());
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      float $$0 = (float)(this.doorPos.getX() + 0.5 - this.mob.getX());
      float $$1 = (float)(this.doorPos.getZ() + 0.5 - this.mob.getZ());
      float $$2 = this.doorOpenDirX * $$0 + this.doorOpenDirZ * $$1;
      if ($$2 < 0.0F) {
         this.passed = true;
      }
   }
}
