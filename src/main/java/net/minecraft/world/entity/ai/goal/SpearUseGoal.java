package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpearUseGoal<T extends Monster> extends Goal {
   static final int MIN_REPOSITION_DISTANCE = 6;
   static final int MAX_REPOSITION_DISTANCE = 7;
   static final int MIN_COOLDOWN_DISTANCE = 9;
   static final int MAX_COOLDOWN_DISTANCE = 11;
   static final double MAX_FLEEING_TIME = reducedTickDelay(100);
   private final T mob;
   @Nullable
   private SpearUseGoal.SpearUseState state;
   double speedModifierWhenCharging;
   double speedModifierWhenRepositioning;
   float approachDistanceSq;
   float targetInRangeRadiusSq;

   public SpearUseGoal(T $$0, double $$1, double $$2, float $$3, float $$4) {
      this.mob = $$0;
      this.speedModifierWhenCharging = $$1;
      this.speedModifierWhenRepositioning = $$2;
      this.approachDistanceSq = $$3 * $$3;
      this.targetInRangeRadiusSq = $$4 * $$4;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      return this.ableToAttack() && !this.mob.isUsingItem();
   }

   private boolean ableToAttack() {
      return this.mob.getTarget() != null && this.mob.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
   }

   private int getKineticWeaponUseDuration() {
      int $$0 = Optional.ofNullable((KineticWeapon)this.mob.getMainHandItem().get(DataComponents.KINETIC_WEAPON))
         .<Integer>map(KineticWeapon::computeDamageUseDuration)
         .orElse(0);
      return reducedTickDelay($$0);
   }

   @Override
   public boolean canContinueToUse() {
      return this.state != null && !this.state.done && this.ableToAttack();
   }

   @Override
   public void start() {
      super.start();
      this.mob.setAggressive(true);
      this.state = new SpearUseGoal.SpearUseState();
   }

   @Override
   public void stop() {
      super.stop();
      this.mob.getNavigation().stop();
      this.mob.setAggressive(false);
      this.state = null;
      this.mob.stopUsingItem();
   }

   @Override
   public void tick() {
      if (this.state != null) {
         net.minecraft.world.entity.LivingEntity $$0 = this.mob.getTarget();
         double $$1 = this.mob.distanceToSqr($$0.getX(), $$0.getY(), $$0.getZ());
         net.minecraft.world.entity.Entity $$2 = this.mob.getRootVehicle();
         float $$3 = 1.0F;
         if ($$2 instanceof net.minecraft.world.entity.Mob $$4) {
            $$3 = $$4.chargeSpeedModifier();
         }

         int $$5 = this.mob.isPassenger() ? 2 : 0;
         this.mob.lookAt($$0, 30.0F, 30.0F);
         this.mob.getLookControl().setLookAt($$0, 30.0F, 30.0F);
         if (this.state.notEngagedYet()) {
            if ($$1 > this.approachDistanceSq) {
               this.mob.getNavigation().moveTo($$0, $$3 * this.speedModifierWhenRepositioning);
               return;
            }

            this.state.startEngagement(this.getKineticWeaponUseDuration());
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
         }

         if (this.state.tickAndCheckEngagement()) {
            this.mob.stopUsingItem();
            double $$6 = Math.sqrt($$1);
            this.state.awayPos = LandRandomPos.getPosAway(this.mob, Math.max(0.0, 9 + $$5 - $$6), Math.max(1.0, 11 + $$5 - $$6), 7, $$0.position());
            this.state.fleeingTime = 1;
         }

         if (!this.state.tickAndCheckFleeing()) {
            if (this.state.awayPos != null) {
               this.mob.getNavigation().moveTo(this.state.awayPos.x, this.state.awayPos.y, this.state.awayPos.z, $$3 * this.speedModifierWhenRepositioning);
               if (this.mob.getNavigation().isDone()) {
                  if (this.state.fleeingTime > 0) {
                     this.state.done = true;
                     return;
                  }

                  this.state.awayPos = null;
               }
            } else {
               this.mob.getNavigation().moveTo($$0, $$3 * this.speedModifierWhenCharging);
               if ($$1 < this.targetInRangeRadiusSq || this.mob.getNavigation().isDone()) {
                  double $$7 = Math.sqrt($$1);
                  this.state.awayPos = LandRandomPos.getPosAway(this.mob, 6 + $$5 - $$7, 7 + $$5 - $$7, 7, $$0.position());
               }
            }
         }
      }
   }

   public static class SpearUseState {
      private int engageTime = -1;
      int fleeingTime = -1;
      @Nullable
      Vec3 awayPos;
      boolean done = false;

      public boolean notEngagedYet() {
         return this.engageTime < 0;
      }

      public void startEngagement(int $$0) {
         this.engageTime = $$0;
      }

      public boolean tickAndCheckEngagement() {
         if (this.engageTime > 0) {
            this.engageTime--;
            if (this.engageTime == 0) {
               return true;
            }
         }

         return false;
      }

      public boolean tickAndCheckFleeing() {
         if (this.fleeingTime > 0) {
            this.fleeingTime++;
            if (this.fleeingTime > SpearUseGoal.MAX_FLEEING_TIME) {
               this.done = true;
               return true;
            }
         }

         return false;
      }
   }
}
