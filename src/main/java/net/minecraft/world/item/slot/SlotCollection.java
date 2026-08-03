package net.minecraft.world.item.slot;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.SlotAccess;

public interface SlotCollection {
   SlotCollection EMPTY = Stream::empty;

   Stream<net.minecraft.world.item.ItemStack> itemCopies();

   default SlotCollection filter(Predicate<net.minecraft.world.item.ItemStack> $$0) {
      return new SlotCollection.Filtered(this, $$0);
   }

   default SlotCollection flatMap(Function<net.minecraft.world.item.ItemStack, ? extends SlotCollection> $$0) {
      return new SlotCollection.FlatMapped(this, $$0);
   }

   default SlotCollection limit(int $$0) {
      return new SlotCollection.Limited(this, $$0);
   }

   static SlotCollection of(SlotAccess $$0) {
      return () -> Stream.of($$0.get().copy());
   }

   static SlotCollection of(Collection<? extends SlotAccess> $$0) {
      return switch ($$0.size()) {
         case 0 -> EMPTY;
         case 1 -> of($$0.iterator().next());
         default -> () -> $$0.stream().<net.minecraft.world.item.ItemStack>map(SlotAccess::get).map(net.minecraft.world.item.ItemStack::copy);
      };
   }

   static SlotCollection concat(SlotCollection $$0, SlotCollection $$1) {
      return () -> Stream.concat($$0.itemCopies(), $$1.itemCopies());
   }

   static SlotCollection concat(List<? extends SlotCollection> $$0) {
      return switch ($$0.size()) {
         case 0 -> EMPTY;
         case 1 -> (SlotCollection)$$0.getFirst();
         case 2 -> concat($$0.get(0), $$0.get(1));
         default -> () -> $$0.stream().flatMap(SlotCollection::itemCopies);
      };
   }

   public record Filtered(SlotCollection slots, Predicate<net.minecraft.world.item.ItemStack> filter) implements SlotCollection {
      @Override
      public Stream<net.minecraft.world.item.ItemStack> itemCopies() {
         return this.slots.itemCopies().filter(this.filter);
      }

      @Override
      public SlotCollection filter(Predicate<net.minecraft.world.item.ItemStack> $$0) {
         return new SlotCollection.Filtered(this.slots, this.filter.and($$0));
      }
   }

   public record FlatMapped(SlotCollection slots, Function<net.minecraft.world.item.ItemStack, ? extends SlotCollection> mapper) implements SlotCollection {
      @Override
      public Stream<net.minecraft.world.item.ItemStack> itemCopies() {
         return this.slots.itemCopies().map(this.mapper).flatMap(SlotCollection::itemCopies);
      }
   }

   public record Limited(SlotCollection slots, int limit) implements SlotCollection {
      @Override
      public Stream<net.minecraft.world.item.ItemStack> itemCopies() {
         return this.slots.itemCopies().limit(this.limit);
      }

      @Override
      public SlotCollection limit(int $$0) {
         return new SlotCollection.Limited(this.slots, Math.min(this.limit, $$0));
      }
   }
}
