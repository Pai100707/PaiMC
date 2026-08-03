package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class HurtBySensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<?> $$2 = $$1.getBrain();
      DamageSource $$3 = $$1.getLastDamageSource();
      if ($$3 != null) {
         $$2.setMemory(MemoryModuleType.HURT_BY, $$1.getLastDamageSource());
         net.minecraft.world.entity.Entity $$4 = $$3.getEntity();
         if ($$4 instanceof net.minecraft.world.entity.LivingEntity) {
            $$2.setMemory(MemoryModuleType.HURT_BY_ENTITY, (net.minecraft.world.entity.LivingEntity)$$4);
         }
      } else {
         $$2.eraseMemory(MemoryModuleType.HURT_BY);
      }

      $$2.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent($$2x -> {
         if (!$$2x.isAlive() || $$2x.level() != $$0) {
            $$2.eraseMemory(MemoryModuleType.HURT_BY_ENTITY);
         }
      });
   }
}
