package net.minecraft.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger extends SimpleCriterionTrigger<InventoryChangeTrigger.TriggerInstance> {
   @Override
   public Codec<InventoryChangeTrigger.TriggerInstance> codec() {
      return InventoryChangeTrigger.TriggerInstance.CODEC;
   }

   public void trigger(ServerPlayer $$0, Inventory $$1, ItemStack $$2) {
      int $$3 = 0;
      int $$4 = 0;
      int $$5 = 0;

      for (int $$6 = 0; $$6 < $$1.getContainerSize(); $$6++) {
         ItemStack $$7 = $$1.getItem($$6);
         if ($$7.isEmpty()) {
            $$4++;
         } else {
            $$5++;
            if ($$7.getCount() >= $$7.getMaxStackSize()) {
               $$3++;
            }
         }
      }

      this.trigger($$0, $$1, $$2, $$3, $$4, $$5);
   }

   private void trigger(ServerPlayer $$0, Inventory $$1, ItemStack $$2, int $$3, int $$4, int $$5) {
      this.trigger($$0, $$5x -> $$5x.matches($$1, $$2, $$3, $$4, $$5));
   }

   public record TriggerInstance(Optional<ContextAwarePredicate> player, InventoryChangeTrigger.TriggerInstance.Slots slots, List<ItemPredicate> items)
      implements SimpleCriterionTrigger.SimpleInstance {
      public static final Codec<InventoryChangeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(InventoryChangeTrigger.TriggerInstance::player),
               InventoryChangeTrigger.TriggerInstance.Slots.CODEC
                  .optionalFieldOf("slots", InventoryChangeTrigger.TriggerInstance.Slots.ANY)
                  .forGetter(InventoryChangeTrigger.TriggerInstance::slots),
               ItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(InventoryChangeTrigger.TriggerInstance::items)
            )
            .apply($$0, InventoryChangeTrigger.TriggerInstance::new)
      );

      public static net.minecraft.advancements.Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate.Builder... $$0) {
         return hasItems(Stream.of($$0).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
      }

      public static net.minecraft.advancements.Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemPredicate... $$0) {
         return net.minecraft.advancements.CriteriaTriggers.INVENTORY_CHANGED
            .createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of($$0)));
      }

      public static net.minecraft.advancements.Criterion<InventoryChangeTrigger.TriggerInstance> hasItems(ItemLike... $$0) {
         ItemPredicate[] $$1 = new ItemPredicate[$$0.length];

         for (int $$2 = 0; $$2 < $$0.length; $$2++) {
            $$1[$$2] = new ItemPredicate(
               Optional.of(HolderSet.direct(new Holder[]{$$0[$$2].asItem().builtInRegistryHolder()})), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY
            );
         }

         return hasItems($$1);
      }

      public boolean matches(Inventory $$0, ItemStack $$1, int $$2, int $$3, int $$4) {
         if (!this.slots.matches($$2, $$3, $$4)) {
            return false;
         } else if (this.items.isEmpty()) {
            return true;
         } else if (this.items.size() != 1) {
            List<ItemPredicate> $$5 = new ObjectArrayList(this.items);
            int $$6 = $$0.getContainerSize();

            for (int $$7 = 0; $$7 < $$6; $$7++) {
               if ($$5.isEmpty()) {
                  return true;
               }

               ItemStack $$8 = $$0.getItem($$7);
               if (!$$8.isEmpty()) {
                  $$5.removeIf($$1x -> $$1x.test($$8));
               }
            }

            return $$5.isEmpty();
         } else {
            return !$$1.isEmpty() && this.items.get(0).test($$1);
         }
      }

      public record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
         public static final Codec<InventoryChangeTrigger.TriggerInstance.Slots> CODEC = RecordCodecBuilder.create(
            $$0 -> $$0.group(
                  MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::occupied),
                  MinMaxBounds.Ints.CODEC.optionalFieldOf("full", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::full),
                  MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", MinMaxBounds.Ints.ANY).forGetter(InventoryChangeTrigger.TriggerInstance.Slots::empty)
               )
               .apply($$0, InventoryChangeTrigger.TriggerInstance.Slots::new)
         );
         public static final InventoryChangeTrigger.TriggerInstance.Slots ANY = new InventoryChangeTrigger.TriggerInstance.Slots(
            MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY
         );

         public boolean matches(int $$0, int $$1, int $$2) {
            if (!this.full.matches($$0)) {
               return false;
            } else {
               return !this.empty.matches($$1) ? false : this.occupied.matches($$2);
            }
         }
      }
   }
}
