package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.warden.WardenAi;

public class Sniffing<E extends Warden> extends Behavior<E> {
   private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_XZ = 6.0;
   private static final double ANGER_FROM_SNIFFING_MAX_DISTANCE_Y = 20.0;

   public Sniffing(int $$0) {
      super(
         ImmutableMap.of(
            MemoryModuleType.IS_SNIFFING,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED,
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryStatus.REGISTERED,
            MemoryModuleType.DISTURBANCE_LOCATION,
            MemoryStatus.REGISTERED,
            MemoryModuleType.SNIFF_COOLDOWN,
            MemoryStatus.REGISTERED
         ),
         $$0
      );
   }

   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return true;
   }

   protected void start(ServerLevel $$0, E $$1, long $$2) {
      $$1.playSound(SoundEvents.WARDEN_SNIFF, 5.0F, 1.0F);
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.hasPose(net.minecraft.world.entity.Pose.SNIFFING)) {
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      }

      $$1.getBrain().eraseMemory(MemoryModuleType.IS_SNIFFING);
      $$1.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).filter($$1::canTargetEntity).ifPresent($$1x -> {
         if ($$1.closerThan($$1x, 6.0, 20.0)) {
            $$1.increaseAngerAt($$1x);
         }

         if (!$$1.getBrain().hasMemoryValue(MemoryModuleType.DISTURBANCE_LOCATION)) {
            WardenAi.setDisturbanceLocation($$1, $$1x.blockPosition());
         }
      });
   }
}
