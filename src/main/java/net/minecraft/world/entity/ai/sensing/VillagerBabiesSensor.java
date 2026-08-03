package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class VillagerBabiesSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.VISIBLE_VILLAGER_BABIES);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      $$1.getBrain().setMemory(MemoryModuleType.VISIBLE_VILLAGER_BABIES, this.getNearestVillagerBabies($$1));
   }

   private List<net.minecraft.world.entity.LivingEntity> getNearestVillagerBabies(net.minecraft.world.entity.LivingEntity $$0) {
      return ImmutableList.copyOf(this.getVisibleEntities($$0).findAll(this::isVillagerBaby));
   }

   private boolean isVillagerBaby(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getType() == net.minecraft.world.entity.EntityType.VILLAGER && $$0.isBaby();
   }

   private NearestVisibleLivingEntities getVisibleEntities(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
   }
}
