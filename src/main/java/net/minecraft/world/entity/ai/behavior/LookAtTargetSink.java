package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class LookAtTargetSink extends Behavior<net.minecraft.world.entity.Mob> {
   public LookAtTargetSink(int $$0, int $$1) {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_PRESENT), $$0, $$1);
   }

   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      return $$1.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).filter($$1x -> $$1x.isVisibleBy($$1)).isPresent();
   }

   protected void stop(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   protected void tick(ServerLevel $$0, net.minecraft.world.entity.Mob $$1, long $$2) {
      $$1.getBrain().getMemory(MemoryModuleType.LOOK_TARGET).ifPresent($$1x -> $$1.getLookControl().setLookAt($$1x.currentPosition()));
   }
}
