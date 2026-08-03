package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenEntitySensor extends NearestLivingEntitySensor<Warden> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.copyOf(Iterables.concat(super.requires(), List.of(MemoryModuleType.NEAREST_ATTACKABLE)));
   }

   protected void doTick(ServerLevel $$0, Warden $$1) {
      super.doTick($$0, $$1);
      getClosest($$1, $$0x -> $$0x.getType() == net.minecraft.world.entity.EntityType.PLAYER)
         .or(() -> getClosest($$1, $$0xx -> $$0xx.getType() != net.minecraft.world.entity.EntityType.PLAYER))
         .ifPresentOrElse(
            $$1x -> $$1.getBrain().setMemory(MemoryModuleType.NEAREST_ATTACKABLE, $$1x), () -> $$1.getBrain().eraseMemory(MemoryModuleType.NEAREST_ATTACKABLE)
         );
   }

   private static Optional<net.minecraft.world.entity.LivingEntity> getClosest(Warden $$0, Predicate<net.minecraft.world.entity.LivingEntity> $$1) {
      return $$0.getBrain()
         .getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES)
         .stream()
         .flatMap(Collection::stream)
         .filter($$0::canTargetEntity)
         .filter($$1)
         .findFirst();
   }
}
