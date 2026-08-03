package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;

public class PiglinBruteSpecificSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      Brain<?> $$2 = $$1.getBrain();
      List<AbstractPiglin> $$3 = Lists.newArrayList();
      NearestVisibleLivingEntities $$4 = $$2.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
      Optional<net.minecraft.world.entity.Mob> $$5 = $$4.findClosest($$0x -> $$0x instanceof WitherSkeleton || $$0x instanceof WitherBoss)
         .map(net.minecraft.world.entity.Mob.class::cast);

      for (net.minecraft.world.entity.LivingEntity $$7 : $$2.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(ImmutableList.of())) {
         if ($$7 instanceof AbstractPiglin && ((AbstractPiglin)$$7).isAdult()) {
            $$3.add((AbstractPiglin)$$7);
         }
      }

      $$2.setMemory(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, $$5);
      $$2.setMemory(MemoryModuleType.NEARBY_ADULT_PIGLINS, $$3);
   }
}
