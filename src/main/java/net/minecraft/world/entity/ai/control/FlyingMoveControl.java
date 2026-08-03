package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FlyingMoveControl extends MoveControl {
   private final int maxTurn;
   private final boolean hoversInPlace;

   public FlyingMoveControl(net.minecraft.world.entity.Mob $$0, int $$1, boolean $$2) {
      super($$0);
      this.maxTurn = $$1;
      this.hoversInPlace = $$2;
   }

   @Override
   public void tick() {
      if (this.operation == MoveControl.Operation.MOVE_TO) {
         this.operation = MoveControl.Operation.WAIT;
         this.mob.setNoGravity(true);
         double $$0 = this.wantedX - this.mob.getX();
         double $$1 = this.wantedY - this.mob.getY();
         double $$2 = this.wantedZ - this.mob.getZ();
         double $$3 = $$0 * $$0 + $$1 * $$1 + $$2 * $$2;
         if ($$3 < 2.5000003E-7F) {
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
            return;
         }

         float $$4 = (float)(Mth.atan2($$2, $$0) * 180.0F / (float)Math.PI) - 90.0F;
         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), $$4, 90.0F));
         float $$5;
         if (this.mob.onGround()) {
            $$5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
         } else {
            $$5 = (float)(this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));
         }

         this.mob.setSpeed($$5);
         double $$7 = Math.sqrt($$0 * $$0 + $$2 * $$2);
         if (Math.abs($$1) > 1.0E-5F || Math.abs($$7) > 1.0E-5F) {
            float $$8 = (float)(-(Mth.atan2($$1, $$7) * 180.0F / (float)Math.PI));
            this.mob.setXRot(this.rotlerp(this.mob.getXRot(), $$8, this.maxTurn));
            this.mob.setYya($$1 > 0.0 ? $$5 : -$$5);
         }
      } else {
         if (!this.hoversInPlace) {
            this.mob.setNoGravity(false);
         }

         this.mob.setYya(0.0F);
         this.mob.setZza(0.0F);
      }
   }
}
