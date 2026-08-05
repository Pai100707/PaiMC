package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class SpearApproach extends Behavior<net.minecraft.world.entity.PathfinderMob> {
   double speedModifierWhenRepositioning;
   float approachDistanceSq;

   public SpearApproach(double $$0, float $$1) {
      super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryStatus.VALUE_ABSENT));
      this.speedModifierWhenRepositioning = $$0;
      this.approachDistanceSq = $$1 * $$1;
   }

   private boolean ableToAttack(net.minecraft.world.entity.PathfinderMob $$0) {
      return this.getTarget($$0) != null && $$0.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      return this.ableToAttack($$1) && !$$1.isUsingItem();
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.setAggressive(true);
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.APPROACH);
      super.start($$0, $$1, $$2);
   }

   
   private net.minecraft.world.entity.LivingEntity getTarget(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      return this.ableToAttack($$1) && this.farEnough($$1);
   }

   private boolean farEnough(net.minecraft.world.entity.PathfinderMob $$0) {
      net.minecraft.world.entity.LivingEntity $$1 = this.getTarget($$0);
      double $$2 = $$0.distanceToSqr($$1.getX(), $$1.getY(), $$1.getZ());
      return $$2 > this.approachDistanceSq;
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = this.getTarget($$1);
      net.minecraft.world.entity.Entity $$4 = $$1.getRootVehicle();
      float $$5 = 1.0F;
      if ($$4 instanceof net.minecraft.world.entity.Mob $$6) {
         $$5 = $$6.chargeSpeedModifier();
      }

      $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$3, true));
      $$1.getNavigation().moveTo($$3, $$5 * this.speedModifierWhenRepositioning);
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.getNavigation().stop();
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.CHARGING);
   }

   @Override
   protected boolean timedOut(long $$0) {
      return false;
   }
}
