package net.minecraft.world.entity.ai.behavior.warden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.warden.Warden;

public class Emerging<E extends Warden> extends Behavior<E> {
   public Emerging(int $$0) {
      super(
         ImmutableMap.of(
            MemoryModuleType.IS_EMERGING,
            MemoryStatus.VALUE_PRESENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.LOOK_TARGET,
            MemoryStatus.REGISTERED
         ),
         $$0
      );
   }

   protected boolean canStillUse(ServerLevel $$0, E $$1, long $$2) {
      return true;
   }

   protected void start(ServerLevel $$0, E $$1, long $$2) {
      $$1.setPose(net.minecraft.world.entity.Pose.EMERGING);
      $$1.playSound(SoundEvents.WARDEN_EMERGE, 5.0F, 1.0F);
   }

   protected void stop(ServerLevel $$0, E $$1, long $$2) {
      if ($$1.hasPose(net.minecraft.world.entity.Pose.EMERGING)) {
         $$1.setPose(net.minecraft.world.entity.Pose.STANDING);
      }
   }
}
