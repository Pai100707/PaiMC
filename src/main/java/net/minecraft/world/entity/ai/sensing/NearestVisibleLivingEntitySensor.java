package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public abstract class NearestVisibleLivingEntitySensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   protected abstract boolean isMatchingEntity(ServerLevel var1, net.minecraft.world.entity.LivingEntity var2, net.minecraft.world.entity.LivingEntity var3);

   protected abstract MemoryModuleType<net.minecraft.world.entity.LivingEntity> getMemory();

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(this.getMemory());
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$1.getBrain().setMemory(this.getMemory(), this.getNearestEntity($$0, $$1));
   }

   private Optional<net.minecraft.world.entity.LivingEntity> getNearestEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      return this.getVisibleEntities($$1).flatMap($$2 -> $$2.findClosest($$2x -> this.isMatchingEntity($$0, $$1, $$2x)));
   }

   protected Optional<NearestVisibleLivingEntities> getVisibleEntities(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
   }
}
