package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GolemSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   private static final int GOLEM_SCAN_RATE = 200;
   private static final int MEMORY_TIME_TO_LIVE = 599;

   public GolemSensor() {
      this(200);
   }

   public GolemSensor(int $$0) {
      super($$0);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      checkForNearbyGolem($$1);
   }

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_LIVING_ENTITIES);
   }

   public static void checkForNearbyGolem(net.minecraft.world.entity.LivingEntity $$0) {
      Optional<List<net.minecraft.world.entity.LivingEntity>> $$1 = $$0.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
      if (!$$1.isEmpty()) {
         boolean $$2 = $$1.get().stream().anyMatch($$0x -> $$0x.getType().equals(net.minecraft.world.entity.EntityType.IRON_GOLEM));
         if ($$2) {
            golemDetected($$0);
         }
      }
   }

   public static void golemDetected(net.minecraft.world.entity.LivingEntity $$0) {
      $$0.getBrain().setMemoryWithExpiry(MemoryModuleType.GOLEM_DETECTED_RECENTLY, true, 599L);
   }
}
