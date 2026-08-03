package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CountDownCooldownTicks extends Behavior<net.minecraft.world.entity.LivingEntity> {
   private final MemoryModuleType<Integer> cooldownTicks;

   public CountDownCooldownTicks(MemoryModuleType<Integer> $$0) {
      super(ImmutableMap.of($$0, MemoryStatus.VALUE_PRESENT));
      this.cooldownTicks = $$0;
   }

   private Optional<Integer> getCooldownTickMemory(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(this.cooldownTicks);
   }

   @Override
   protected boolean timedOut(long $$0) {
      return false;
   }

   @Override
   protected boolean canStillUse(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, long $$2) {
      Optional<Integer> $$3 = this.getCooldownTickMemory($$1);
      return $$3.isPresent() && $$3.get() > 0;
   }

   @Override
   protected void tick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, long $$2) {
      Optional<Integer> $$3 = this.getCooldownTickMemory($$1);
      $$1.getBrain().setMemory(this.cooldownTicks, $$3.get() - 1);
   }

   @Override
   protected void stop(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, long $$2) {
      $$1.getBrain().eraseMemory(this.cooldownTicks);
   }
}
