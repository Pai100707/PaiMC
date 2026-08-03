package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class OwnerHurtByTargetGoal extends TargetGoal {
   private final net.minecraft.world.entity.TamableAnimal tameAnimal;
   private net.minecraft.world.entity.LivingEntity ownerLastHurtBy;
   private int timestamp;

   public OwnerHurtByTargetGoal(net.minecraft.world.entity.TamableAnimal $$0) {
      super($$0, false);
      this.tameAnimal = $$0;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
   }

   @Override
   public boolean canUse() {
      if (this.tameAnimal.isTame() && !this.tameAnimal.isOrderedToSit()) {
         net.minecraft.world.entity.LivingEntity $$0 = this.tameAnimal.getOwner();
         if ($$0 == null) {
            return false;
         } else {
            this.ownerLastHurtBy = $$0.getLastHurtByMob();
            int $$1 = $$0.getLastHurtByMobTimestamp();
            return $$1 != this.timestamp
               && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
               && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, $$0);
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      this.mob.setTarget(this.ownerLastHurtBy);
      net.minecraft.world.entity.LivingEntity $$0 = this.tameAnimal.getOwner();
      if ($$0 != null) {
         this.timestamp = $$0.getLastHurtByMobTimestamp();
      }

      super.start();
   }
}
