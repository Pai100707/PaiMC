package net.minecraft.world.entity.ai.behavior;

import java.util.Map;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.item.component.KineticWeapon;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpearAttack extends Behavior<net.minecraft.world.entity.PathfinderMob> {
   public static final int MIN_REPOSITION_DISTANCE = 6;
   public static final int MAX_REPOSITION_DISTANCE = 7;
   double speedModifierWhenCharging;
   double speedModifierWhenRepositioning;
   float approachDistanceSq;
   float targetInRangeRadiusSq;

   public SpearAttack(double $$0, double $$1, float $$2, float $$3) {
      super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryStatus.VALUE_PRESENT));
      this.speedModifierWhenCharging = $$0;
      this.speedModifierWhenRepositioning = $$1;
      this.approachDistanceSq = $$2 * $$2;
      this.targetInRangeRadiusSq = $$3 * $$3;
   }

   @Nullable
   private net.minecraft.world.entity.LivingEntity getTarget(net.minecraft.world.entity.PathfinderMob $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
   }

   private boolean ableToAttack(net.minecraft.world.entity.PathfinderMob $$0) {
      return this.getTarget($$0) != null && $$0.getMainHandItem().has(DataComponents.KINETIC_WEAPON);
   }

   private int getKineticWeaponUseDuration(net.minecraft.world.entity.PathfinderMob $$0) {
      return Optional.ofNullable((KineticWeapon)$$0.getMainHandItem().get(DataComponents.KINETIC_WEAPON))
         .<Integer>map(KineticWeapon::computeDamageUseDuration)
         .orElse(0);
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      return $$1.getBrain().getMemory(MemoryModuleType.SPEAR_STATUS).orElse(SpearAttack.SpearStatus.APPROACH) == SpearAttack.SpearStatus.CHARGING
         && this.ableToAttack($$1)
         && !$$1.isUsingItem();
   }

   protected void start(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.setAggressive(true);
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_ENGAGE_TIME, this.getKineticWeaponUseDuration($$1));
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_CHARGE_POSITION);
      $$1.startUsingItem(InteractionHand.MAIN_HAND);
      super.start($$0, $$1, $$2);
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      return $$1.getBrain().getMemory(MemoryModuleType.SPEAR_ENGAGE_TIME).orElse(0) > 0 && this.ableToAttack($$1);
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = this.getTarget($$1);
      double $$4 = $$1.distanceToSqr($$3.getX(), $$3.getY(), $$3.getZ());
      net.minecraft.world.entity.Entity $$5 = $$1.getRootVehicle();
      float $$6 = 1.0F;
      if ($$5 instanceof net.minecraft.world.entity.Mob $$7) {
         $$6 = $$7.chargeSpeedModifier();
      }

      int $$8 = $$1.isPassenger() ? 2 : 0;
      $$1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$3, true));
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_ENGAGE_TIME, $$1.getBrain().getMemory(MemoryModuleType.SPEAR_ENGAGE_TIME).orElse(0) - 1);
      Vec3 $$9 = $$1.getBrain().getMemory(MemoryModuleType.SPEAR_CHARGE_POSITION).orElse(null);
      if ($$9 != null) {
         $$1.getNavigation().moveTo($$9.x, $$9.y, $$9.z, $$6 * this.speedModifierWhenRepositioning);
         if ($$1.getNavigation().isDone()) {
            $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_CHARGE_POSITION);
         }
      } else {
         $$1.getNavigation().moveTo($$3, $$6 * this.speedModifierWhenCharging);
         if ($$4 < this.targetInRangeRadiusSq || $$1.getNavigation().isDone()) {
            double $$10 = Math.sqrt($$4);
            Vec3 $$11 = LandRandomPos.getPosAway($$1, 6 + $$8 - $$10, 7 + $$8 - $$10, 7, $$3.position());
            $$1.getBrain().setMemory(MemoryModuleType.SPEAR_CHARGE_POSITION, $$11);
         }
      }
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1, long $$2) {
      $$1.getNavigation().stop();
      $$1.stopUsingItem();
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_CHARGE_POSITION);
      $$1.getBrain().eraseMemory(MemoryModuleType.SPEAR_ENGAGE_TIME);
      $$1.getBrain().setMemory(MemoryModuleType.SPEAR_STATUS, SpearAttack.SpearStatus.RETREAT);
   }

   @Override
   protected boolean timedOut(long $$0) {
      return false;
   }

   public static enum SpearStatus {
      APPROACH,
      CHARGING,
      RETREAT;
   }
}
