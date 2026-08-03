package net.minecraft.world.entity.ai.sensing;

import com.google.common.collect.ImmutableSet;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TemptingSensor extends Sensor<net.minecraft.world.entity.PathfinderMob> {
   private static final TargetingConditions TEMPT_TARGETING = TargetingConditions.forNonCombat().ignoreLineOfSight();
   private final BiPredicate<net.minecraft.world.entity.PathfinderMob, ItemStack> temptations;

   public TemptingSensor(Predicate<ItemStack> $$0) {
      this(($$1, $$2) -> $$0.test($$2));
   }

   public static TemptingSensor forAnimal() {
      return new TemptingSensor(($$0, $$1) -> $$0 instanceof Animal $$2 ? $$2.isFood($$1) : false);
   }

   private TemptingSensor(BiPredicate<net.minecraft.world.entity.PathfinderMob, ItemStack> $$0) {
      this.temptations = $$0;
   }

   protected void doTick(ServerLevel $$0, net.minecraft.world.entity.PathfinderMob $$1) {
      Brain<?> $$2 = $$1.getBrain();
      TargetingConditions $$3 = TEMPT_TARGETING.copy().range((float)$$1.getAttributeValue(Attributes.TEMPT_RANGE));
      List<Player> $$4 = $$0.players()
         .stream()
         .filter(net.minecraft.world.entity.EntitySelector.NO_SPECTATORS)
         .filter($$3x -> $$3.test($$0, $$1, $$3x))
         .filter($$1x -> this.playerHoldingTemptation($$1, $$1x))
         .filter($$1x -> !$$1.hasPassenger($$1x))
         .sorted(Comparator.comparingDouble($$1::distanceToSqr))
         .collect(Collectors.toList());
      if (!$$4.isEmpty()) {
         Player $$5 = $$4.get(0);
         $$2.setMemory(MemoryModuleType.TEMPTING_PLAYER, $$5);
      } else {
         $$2.eraseMemory(MemoryModuleType.TEMPTING_PLAYER);
      }
   }

   private boolean playerHoldingTemptation(net.minecraft.world.entity.PathfinderMob $$0, Player $$1) {
      return this.isTemptation($$0, $$1.getMainHandItem()) || this.isTemptation($$0, $$1.getOffhandItem());
   }

   private boolean isTemptation(net.minecraft.world.entity.PathfinderMob $$0, ItemStack $$1) {
      return this.temptations.test($$0, $$1);
   }

   @Override
   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of(MemoryModuleType.TEMPTING_PLAYER);
   }
}
