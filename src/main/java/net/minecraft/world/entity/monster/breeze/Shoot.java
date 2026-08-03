package net.minecraft.world.entity.monster.breeze;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.item.ItemStack;

public class Shoot extends Behavior<Breeze> {
   private static final int ATTACK_RANGE_MAX_SQRT = 256;
   private static final int UNCERTAINTY_BASE = 5;
   private static final int UNCERTAINTY_MULTIPLIER = 4;
   private static final float PROJECTILE_MOVEMENT_SCALE = 0.7F;
   private static final int SHOOT_INITIAL_DELAY_TICKS = Math.round(15.0F);
   private static final int SHOOT_RECOVER_DELAY_TICKS = Math.round(4.0F);
   private static final int SHOOT_COOLDOWN_TICKS = Math.round(10.0F);

   @VisibleForTesting
   public Shoot() {
      super(
         ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.BREEZE_SHOOT_COOLDOWN,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_SHOOT_CHARGING,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_SHOOT_RECOVERING,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_SHOOT,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.BREEZE_JUMP_TARGET,
            MemoryStatus.VALUE_ABSENT
         ),
         SHOOT_INITIAL_DELAY_TICKS + 1 + SHOOT_RECOVER_DELAY_TICKS
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Breeze $$1) {
      return $$1.getPose() != net.minecraft.world.entity.Pose.STANDING
         ? false
         : $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).map($$1x -> isTargetWithinRange($$1, $$1x)).map($$1x -> {
            if (!$$1x) {
               $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
            }

            return $$1x;
         }).orElse(false);
   }

   protected boolean canStillUse(ServerLevel $$0, Breeze $$1, long $$2) {
      return $$1.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && $$1.getBrain().hasMemoryValue(MemoryModuleType.BREEZE_SHOOT);
   }

   protected void start(ServerLevel $$0, Breeze $$1, long $$2) {
      $$1.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent($$1x -> $$1.setPose(net.minecraft.world.entity.Pose.SHOOTING));
      $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, SHOOT_INITIAL_DELAY_TICKS);
      $$1.playSound(SoundEvents.BREEZE_INHALE, 1.0F, 1.0F);
   }

   protected void stop(ServerLevel $$0, Breeze $$1, long $$2) {
      if ($$1.getPose() == net.minecraft.world.entity.Pose.SHOOTING) {
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      }

      $$1.getBrain().setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, SHOOT_COOLDOWN_TICKS);
      $$1.getBrain().eraseMemory(MemoryModuleType.BREEZE_SHOOT);
   }

   protected void tick(ServerLevel $$0, Breeze $$1, long $$2) {
      Brain<Breeze> $$3 = $$1.getBrain();
      net.minecraft.world.entity.LivingEntity $$4 = $$3.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
      if ($$4 != null) {
         $$1.lookAt(Anchor.EYES, $$4.position());
         if (!$$3.getMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() && !$$3.getMemory(MemoryModuleType.BREEZE_SHOOT_RECOVERING).isPresent()) {
            $$3.setMemoryWithExpiry(MemoryModuleType.BREEZE_SHOOT_RECOVERING, Unit.INSTANCE, SHOOT_RECOVER_DELAY_TICKS);
            double $$5 = $$4.getX() - $$1.getX();
            double $$6 = $$4.getY($$4.isPassenger() ? 0.8 : 0.3) - $$1.getFiringYPosition();
            double $$7 = $$4.getZ() - $$1.getZ();
            Projectile.spawnProjectileUsingShoot(new BreezeWindCharge($$1, $$0), $$0, ItemStack.EMPTY, $$5, $$6, $$7, 0.7F, 5 - $$0.getDifficulty().getId() * 4);
            $$1.playSound(SoundEvents.BREEZE_SHOOT, 1.5F, 1.0F);
         }
      }
   }

   private static boolean isTargetWithinRange(Breeze $$0, net.minecraft.world.entity.LivingEntity $$1) {
      double $$2 = $$0.position().distanceToSqr($$1.position());
      return $$2 < 256.0;
   }
}
