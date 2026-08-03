package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class PlayerSensor extends Sensor<net.minecraft.world.entity.LivingEntity> {
   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(
         MemoryModuleType.NEAREST_PLAYERS,
         MemoryModuleType.NEAREST_VISIBLE_PLAYER,
         MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER,
         MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS
      );
   }

   @Override
   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1) {
      List<Player> $$2 = $$0.players()
         .stream()
         .filter(net.minecraft.world.entity.EntitySelector.NO_SPECTATORS)
         .filter($$1x -> $$1.closerThan($$1x, this.getFollowDistance($$1)))
         .sorted(Comparator.comparingDouble($$1::distanceToSqr))
         .collect(Collectors.toList());
      Brain<?> $$3 = $$1.getBrain();
      $$3.setMemory(MemoryModuleType.NEAREST_PLAYERS, $$2);
      List<Player> $$4 = $$2.stream().filter($$2x -> isEntityTargetable($$0, $$1, $$2x)).collect(Collectors.toList());
      $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, $$4.isEmpty() ? null : $$4.get(0));
      List<Player> $$5 = $$4.stream().filter($$2x -> isEntityAttackable($$0, $$1, $$2x)).toList();
      $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYERS, $$5);
      $$3.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, $$5.isEmpty() ? null : $$5.get(0));
   }

   protected double getFollowDistance(net.minecraft.world.entity.LivingEntity $$0) {
      return $$0.getAttributeValue(Attributes.FOLLOW_RANGE);
   }
}
