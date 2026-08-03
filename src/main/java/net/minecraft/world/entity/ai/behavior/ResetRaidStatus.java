package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ResetRaidStatus {
   public static BehaviorControl<net.minecraft.world.entity.LivingEntity> create() {
      return BehaviorBuilder.create($$0 -> $$0.point(($$0x, $$1, $$2) -> {
         if ($$0x.random.nextInt(20) != 0) {
            return false;
         } else {
            Brain<?> $$3 = $$1.getBrain();
            Raid $$4 = $$0x.getRaidAt($$1.blockPosition());
            if ($$4 == null || $$4.isStopped() || $$4.isLoss()) {
               $$3.setDefaultActivity(Activity.IDLE);
               $$3.updateActivityFromSchedule($$0x.environmentAttributes(), $$0x.getGameTime(), $$1.position());
            }

            return true;
         }
      }));
   }
}
