package net.minecraft.world.level.storage.loot;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotCollection;

public interface ContainerComponentManipulator<T> {
   DataComponentType<T> type();

   T empty();

   T setContents(T var1, Stream<ItemStack> var2);

   Stream<ItemStack> getContents(T var1);

   default void setContents(ItemStack $$0, T $$1, Stream<ItemStack> $$2) {
      T $$3 = (T)$$0.getOrDefault(this.type(), $$1);
      T $$4 = this.setContents($$3, $$2);
      $$0.set(this.type(), $$4);
   }

   default void setContents(ItemStack $$0, Stream<ItemStack> $$1) {
      this.setContents($$0, this.empty(), $$1);
   }

   default void modifyItems(ItemStack $$0, UnaryOperator<ItemStack> $$1) {
      T $$2 = (T)$$0.get(this.type());
      if ($$2 != null) {
         UnaryOperator<ItemStack> $$3 = $$1x -> {
            if ($$1x.isEmpty()) {
               return $$1x;
            } else {
               ItemStack $$2x = $$1.apply($$1x);
               $$2x.limitSize($$2x.getMaxStackSize());
               return $$2x;
            }
         };
         this.setContents($$0, this.getContents($$2).map($$3));
      }
   }

   default SlotCollection getSlots(ItemStack $$0) {
      return () -> {
         T $$1 = (T)$$0.get(this.type());
         return $$1 != null ? this.getContents($$1).filter($$0xx -> !$$0xx.isEmpty()) : Stream.empty();
      };
   }
}
