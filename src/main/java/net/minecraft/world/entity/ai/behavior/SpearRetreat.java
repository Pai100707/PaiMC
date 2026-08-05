package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class SpearRetreat extends Behavior<net.minecraft.world.entity.PathfinderMob> {
   public static final int MIN_COOLDOWN_DISTANCE = 9;
   public static final int MAX_COOLDOWN_DISTANCE = 11;
   public static final int MAX_FLEEING_TIME = 100;
   double speedModifierWhenRepositioning;

   public SpearRetreat(double $$0) {
      super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryStatus.VALUE_PRESENT), 100);
      this.speedModifierWhenRepositioning = $$0;
   }

   
   private net.minecraft.world.entity.LivingEntity getTarget(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
   }

   private boolean ableToAttack(net.minecraft.world.entity.PathfinderMob $$0) {
      return this.getTarget($$0) != null && $$0.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      if (this.ableToAttack($$1) && !$$1.isUsingItem()) {
         if ($$1.getBrain().getMemory(MemoryModuleType.SPEAR_STATUS).orElse(SpearAttack.SpearStatus.APPROACH) != SpearAttack.SpearStatus.RETREAT) {
            return false;
         } else {
            net.minecraft.world.entity.LivingEntity $$2 = this.getTarget($$1);
            double $$3 = $$1.distanceToSqr($$2.getX(), $$2.getY(), $$2.getZ());
            int $$4 = $$1.isPassenger() ? 2 : 0;
            double $$5 = Math.sqrt($$3);
            Vec3 $$6 = LandRandomPos.getPosAway($$1, Math.max(0.0, 9 + $$4 - $$5), Math.max(1.0, 11 + $$4 - $$5), 7, $$2.position());
            if ($$6 == null) {
               return false;
            } else {
               $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_POSITION, $$6);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.setAggressive(true);
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, 0);
      super.start($$0, $$1, $$2);
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      return $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(100) < 100
         && $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION).isPresent()
         && !$$1.getNavigation().isDone()
         && this.ableToAttack($$1);
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = this.getTarget($$1);
      float $$6 = $$1.getRootVehicle() instanceof net.minecraft.world.entity.Mob $$5 ? $$5.chargeSpeedModifier() : 1.0F;
      $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$3, true));
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_FLEEING_TIME, $$1.getBrain().getMemory(MemoryModuleType.SPEAR_FLEEING_TIME).orElse(0) + 1);
      $$1.getBrain()
         .getMemory(MemoryModuleType.SPEAR_FLEEING_POSITION)
         .ifPresent($$2x -> $$1.getNavigation().moveTo($$2x.x, $$2x.y, $$2x.z, $$6 * this.speedModifierWhenRepositioning));
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.getNavigation().stop();
      $$1.setAggressive(false);
      $$1.stopUsingItem();
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_TIME);
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_FLEEING_POSITION);
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_STATUS);
   }
}
