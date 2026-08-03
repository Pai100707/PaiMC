package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import org.jspecify.annotations.Nullable;

public class BreedGoal extends Goal {
   private static final TargetingConditions PARTNER_TARGETING = TargetingConditions.forNonCombat().range(8.0).ignoreLineOfSight();
   protected final Animal animal;
   private final Class<? extends Animal> partnerClass;
   protected final ServerLevel level;
   @Nullable
   protected Animal partner;
   private int loveTime;
   private final double speedModifier;

   public BreedGoal(Animal $$0, double $$1) {
      this($$0, $$1, (Class<? extends Animal>)$$0.getClass());
   }

   public BreedGoal(Animal $$0, double $$1, Class<? extends Animal> $$2) {
      this.animal = $$0;
      this.level = getServerLevel($$0);
      this.partnerClass = $$2;
      this.speedModifier = $$1;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   @Override
   public boolean canUse() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.partner = this.getFreePartner();
         return this.partner != null;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return this.partner.isAlive() && this.partner.isInLove() && this.loveTime < 60 && !this.partner.isPanicking();
   }

   @Override
   public void stop() {
      this.partner = null;
      this.loveTime = 0;
   }

   @Override
   public void tick() {
      this.animal.getLookControl().setLookAt(this.partner, 10.0F, this.animal.getMaxHeadXRot());
      this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
      this.loveTime++;
      if (this.loveTime >= this.adjustedTickDelay(60) && this.animal.distanceToSqr(this.partner) < 9.0) {
         this.breed();
      }
   }

   @Nullable
   private Animal getFreePartner() {
      List<? extends Animal> $$0 = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(8.0));
      double $$1 = Double.MAX_VALUE;
      Animal $$2 = null;

      for (Animal $$3 : $$0) {
         if (this.animal.canMate($$3) && !$$3.isPanicking() && this.animal.distanceToSqr($$3) < $$1) {
            $$2 = $$3;
            $$1 = this.animal.distanceToSqr($$3);
         }
      }

      return $$2;
   }

   protected void breed() {
      this.animal.spawnChildFromBreeding(this.level, this.partner);
   }
}
