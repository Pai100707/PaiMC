package net.minecraft.world.entity.ai.control;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MoveControl implements Control {
   public static final float MIN_SPEED = 5.0E-4F;
   public static final float MIN_SPEED_SQR = 2.5000003E-7F;
   protected static final int MAX_TURN = 90;
   protected final net.minecraft.world.entity.Mob mob;
   protected double wantedX;
   protected double wantedY;
   protected double wantedZ;
   protected double speedModifier;
   protected float strafeForwards;
   protected float strafeRight;
   protected MoveControl.Operation operation = MoveControl.Operation.WAIT;

   public MoveControl(net.minecraft.world.entity.Mob $$0) {
      this.mob = $$0;
   }

   public boolean hasWanted() {
      return this.operation == MoveControl.Operation.MOVE_TO;
   }

   public double getSpeedModifier() {
      return this.speedModifier;
   }

   public void setWantedPosition(double $$0, double $$1, double $$2, double $$3) {
      this.wantedX = $$0;
      this.wantedY = $$1;
      this.wantedZ = $$2;
      this.speedModifier = $$3;
      if (this.operation != MoveControl.Operation.JUMPING) {
         this.operation = MoveControl.Operation.MOVE_TO;
      }
   }

   public void strafe(float $$0, float $$1) {
      this.operation = MoveControl.Operation.STRAFE;
      this.strafeForwards = $$0;
      this.strafeRight = $$1;
      this.speedModifier = 0.25;
   }

   public void tick() {
      if (this.operation == MoveControl.Operation.STRAFE) {
         float $$0 = (float)this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
         float $$1 = (float)this.speedModifier * $$0;
         float $$2 = this.strafeForwards;
         float $$3 = this.strafeRight;
         float $$4 = Mth.sqrt($$2 * $$2 + $$3 * $$3);
         if ($$4 < 1.0F) {
            $$4 = 1.0F;
         }

         $$4 = $$1 / $$4;
         $$2 *= $$4;
         $$3 *= $$4;
         float $$5 = Mth.sin(this.mob.getYRot() * (float) (Math.PI / 180.0));
         float $$6 = Mth.cos(this.mob.getYRot() * (float) (Math.PI / 180.0));
         float $$7 = $$2 * $$6 - $$3 * $$5;
         float $$8 = $$3 * $$6 + $$2 * $$5;
         if (!this.isWalkable($$7, $$8)) {
            this.strafeForwards = 1.0F;
            this.strafeRight = 0.0F;
         }

         this.mob.setSpeed($$1);
         this.mob.setZza(this.strafeForwards);
         this.mob.setXxa(this.strafeRight);
         this.operation = MoveControl.Operation.WAIT;
      } else if (this.operation == MoveControl.Operation.MOVE_TO) {
         this.operation = MoveControl.Operation.WAIT;
         double $$9 = this.wantedX - this.mob.getX();
         double $$10 = this.wantedZ - this.mob.getZ();
         double $$11 = this.wantedY - this.mob.getY();
         double $$12 = $$9 * $$9 + $$11 * $$11 + $$10 * $$10;
         if ($$12 < 2.5000003E-7F) {
            this.mob.setZza(0.0F);
            return;
         }

         float $$13 = (float)(Mth.atan2($$10, $$9) * 180.0F / (float)Math.PI) - 90.0F;
         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), $$13, 90.0F));
         this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
         BlockPos $$14 = this.mob.blockPosition();
         BlockState $$15 = this.mob.level().getBlockState($$14);
         VoxelShape $$16 = $$15.getCollisionShape(this.mob.level(), $$14);
         if ($$11 > this.mob.maxUpStep() && $$9 * $$9 + $$10 * $$10 < Math.max(1.0F, this.mob.getBbWidth())
            || !$$16.isEmpty() && this.mob.getY() < $$16.max(Axis.Y) + $$14.getY() && !$$15.is(BlockTags.DOORS) && !$$15.is(BlockTags.FENCES)) {
            this.mob.getJumpControl().jump();
            this.operation = MoveControl.Operation.JUMPING;
         }
      } else if (this.operation == MoveControl.Operation.JUMPING) {
         this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
         if (this.mob.onGround() || this.mob.isInLiquid() && this.mob.isAffectedByFluids()) {
            this.operation = MoveControl.Operation.WAIT;
         }
      } else {
         this.mob.setZza(0.0F);
      }
   }

   private boolean isWalkable(float $$0, float $$1) {
      PathNavigation $$2 = this.mob.getNavigation();
      if ($$2 != null) {
         NodeEvaluator $$3 = $$2.getNodeEvaluator();
         if ($$3 != null
            && $$3.getPathType(this.mob, BlockPos.containing(this.mob.getX() + $$0, this.mob.getBlockY(), this.mob.getZ() + $$1)) != PathType.WALKABLE) {
            return false;
         }
      }

      return true;
   }

   protected float rotlerp(float $$0, float $$1, float $$2) {
      float $$3 = Mth.wrapDegrees($$1 - $$0);
      if ($$3 > $$2) {
         $$3 = $$2;
      }

      if ($$3 < -$$2) {
         $$3 = -$$2;
      }

      float $$4 = $$0 + $$3;
      if ($$4 < 0.0F) {
         $$4 += 360.0F;
      } else if ($$4 > 360.0F) {
         $$4 -= 360.0F;
      }

      return $$4;
   }

   public double getWantedX() {
      return this.wantedX;
   }

   public double getWantedY() {
      return this.wantedY;
   }

   public double getWantedZ() {
      return this.wantedZ;
   }

   public void setWait() {
      this.operation = MoveControl.Operation.WAIT;
   }

   protected static enum Operation {
      WAIT,
      MOVE_TO,
      STRAFE,
      JUMPING;
   }
}
