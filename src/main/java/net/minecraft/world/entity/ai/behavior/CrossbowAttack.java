package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;

public class CrossbowAttack<E extends net.minecraft.world.entity.Mob & CrossbowAttackMob, T extends net.minecraft.world.entity.LivingEntity> extends Behavior<E> {
   private static final int TIMEOUT = 1200;
   private int attackDelay;
   private CrossbowAttack.CrossbowState crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;

   public CrossbowAttack() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED, MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT), 1200);
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, E $$1) {
      net.minecraft.world.entity.LivingEntity $$2 = getAttackTarget($$1);
      return $$1.isHolding(Items.CROSSBOW) && BehaviorUtils.canSee($$1, $$2) && BehaviorUtils.isWithinAttackRange($$1, $$2, 0);
   }

   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && this.checkExtraStartConditions($$0, $$1);
   }

   protected void tick(ServerLevel $$0, E $$1, long $$2) {
      net.minecraft.world.entity.LivingEntity $$3 = getAttackTarget($$1);
      this.lookAtTarget($$1, $$3);
      this.crossbowAttack($$1, $$3);
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.isUsingItem()) {
         $$1.stopUsingItem();
      }

      if ($$1.isHolding(Items.CROSSBOW)) {
         $$1.setChargingCrossbow(false);
         $$1.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
      }
   }

   private void crossbowAttack(E $$0, net.minecraft.world.entity.LivingEntity $$1) {
      if (this.crossbowState == CrossbowAttack.CrossbowState.UNCHARGED) {
         $$0.startUsingItem(ProjectileUtil.getWeaponHoldingHand($$0, Items.CROSSBOW));
         this.crossbowState = CrossbowAttack.CrossbowState.CHARGING;
         $$0.setChargingCrossbow(true);
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGING) {
         if (!$$0.isUsingItem()) {
            this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
         }

         int $$2 = $$0.getTicksUsingItem();
         ItemStack $$3 = $$0.getUseItem();
         if ($$2 >= CrossbowItem.getChargeDuration($$3, $$0)) {
            $$0.releaseUsingItem();
            this.crossbowState = CrossbowAttack.CrossbowState.CHARGED;
            this.attackDelay = 20 + $$0.getRandom().nextInt(20);
            $$0.setChargingCrossbow(false);
         }
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.CHARGED) {
         this.attackDelay--;
         if (this.attackDelay == 0) {
            this.crossbowState = CrossbowAttack.CrossbowState.READY_TO_ATTACK;
         }
      } else if (this.crossbowState == CrossbowAttack.CrossbowState.READY_TO_ATTACK) {
         $$0.performRangedAttack($$1, 1.0F);
         this.crossbowState = CrossbowAttack.CrossbowState.UNCHARGED;
      }
   }

   private void lookAtTarget(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$0.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker($$1, true));
   }

   private static net.minecraft.world.entity.LivingEntity getAttackTarget(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).get();
   }

   static enum CrossbowState {
      UNCHARGED,
      CHARGING,
      CHARGED,
      READY_TO_ATTACK;
   }
}
