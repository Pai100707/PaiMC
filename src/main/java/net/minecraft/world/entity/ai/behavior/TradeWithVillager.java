package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class TradeWithVillager extends Behavior<Villager> {
   private Set<Item> trades = ImmutableSet.of();

   public TradeWithVillager() {
      super(
         ImmutableMap.of(
            MemoryModuleType.INTERACTION_TARGET, MemoryStatus.VALUE_PRESENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
         )
      );
   }

   protected boolean checkExtraStartConditions(ServerLevel $$0, Villager $$1) {
      return BehaviorUtils.targetIsValid($$1.getBrain(), MemoryModuleType.INTERACTION_TARGET, net.minecraft.world.entity.EntityType.VILLAGER);
   }

   protected boolean canStillUse(ServerLevel $$0, Villager $$1, long $$2) {
      return this.checkExtraStartConditions($$0, $$1);
   }

   protected void start(ServerLevel $$0, Villager $$1, long $$2) {
      Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5F, 2);
      this.trades = figureOutWhatIAmWillingToTrade($$1, $$3);
   }

   protected void tick(ServerLevel $$0, Villager $$1, long $$2) {
      Villager $$3 = (Villager)$$1.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      if (!($$1.distanceToSqr($$3) > 5.0)) {
         BehaviorUtils.lockGazeAndWalkToEachOther($$1, $$3, 0.5F, 2);
         $$1.gossip($$0, $$3, $$2);
         boolean $$4 = $$1.getVillagerData().profession().is(VillagerProfession.FARMER);
         if ($$1.hasExcessFood() && ($$4 || $$3.wantsMoreFood())) {
            throwHalfStack($$1, Villager.FOOD_POINTS.keySet(), $$3);
         }

         if ($$4 && $$1.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getDefaultMaxStackSize() / 2) {
            throwHalfStack($$1, ImmutableSet.of(Items.WHEAT), $$3);
         }

         if (!this.trades.isEmpty() && $$1.getInventory().hasAnyOf(this.trades)) {
            throwHalfStack($$1, this.trades, $$3);
         }
      }
   }

   protected void stop(ServerLevel $$0, Villager $$1, long $$2) {
      $$1.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
   }

   private static Set<Item> figureOutWhatIAmWillingToTrade(Villager $$0, Villager $$1) {
      ImmutableSet<Item> $$2 = ((VillagerProfession)$$1.getVillagerData().profession().value()).requestedItems();
      ImmutableSet<Item> $$3 = ((VillagerProfession)$$0.getVillagerData().profession().value()).requestedItems();
      return $$2.stream().filter($$1x -> !$$3.contains($$1x)).collect(Collectors.toSet());
   }

   private static void throwHalfStack(Villager $$0, Set<Item> $$1, net.minecraft.world.entity.LivingEntity $$2) {
      SimpleContainer $$3 = $$0.getInventory();
      ItemStack $$4 = ItemStack.EMPTY;
      int $$5 = 0;

      while ($$5 < $$3.getContainerSize()) {
         ItemStack $$6;
         Item $$7;
         int $$8;
         label28: {
            $$6 = $$3.getItem($$5);
            if (!$$6.isEmpty()) {
               $$7 = $$6.getItem();
               if ($$1.contains($$7)) {
                  if ($$6.getCount() > $$6.getMaxStackSize() / 2) {
                     $$8 = $$6.getCount() / 2;
                     break label28;
                  }

                  if ($$6.getCount() > 24) {
                     $$8 = $$6.getCount() - 24;
                     break label28;
                  }
               }
            }

            $$5++;
            continue;
         }

         $$6.shrink($$8);
         $$4 = new ItemStack($$7, $$8);
         break;
      }

      if (!$$4.isEmpty()) {
         BehaviorUtils.throwItem($$0, $$4, $$2.position());
      }
   }
}
