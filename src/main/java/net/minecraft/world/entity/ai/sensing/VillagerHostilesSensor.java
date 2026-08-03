package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class VillagerHostilesSensor extends NearestVisibleLivingEntitySensor {
   private static final ImmutableMap<net.minecraft.world.entity.EntityType<?>, Float> ACCEPTABLE_DISTANCE_FROM_HOSTILES = ImmutableMap.builder()
      .put(net.minecraft.world.entity.EntityType.DROWNED, 8.0F)
      .put(net.minecraft.world.entity.EntityType.EVOKER, 12.0F)
      .put(net.minecraft.world.entity.EntityType.HUSK, 8.0F)
      .put(net.minecraft.world.entity.EntityType.ILLUSIONER, 12.0F)
      .put(net.minecraft.world.entity.EntityType.PILLAGER, 15.0F)
      .put(net.minecraft.world.entity.EntityType.RAVAGER, 12.0F)
      .put(net.minecraft.world.entity.EntityType.VEX, 8.0F)
      .put(net.minecraft.world.entity.EntityType.VINDICATOR, 10.0F)
      .put(net.minecraft.world.entity.EntityType.ZOGLIN, 10.0F)
      .put(net.minecraft.world.entity.EntityType.ZOMBIE, 8.0F)
      .put(net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, 8.0F)
      .build();

   @Override
   protected boolean isMatchingEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, net.minecraft.world.entity.LivingEntity $$2) {
      return this.isHostile($$2) && this.isClose($$1, $$2);
   }

   private boolean isClose(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      float $$2 = (Float)ACCEPTABLE_DISTANCE_FROM_HOSTILES.get($$1.getType());
      return $$1.distanceToSqr($$0) <= $$2 * $$2;
   }

   @Override
   protected MemoryModuleType<net.minecraft.world.entity.LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_HOSTILE;
   }

   private boolean isHostile(net.minecraft.world.entity.LivingEntity $$0) {
      return ACCEPTABLE_DISTANCE_FROM_HOSTILES.containsKey($$0.getType());
   }
}
