package net.minecraft.world;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface Container extends Clearable, SlotProvider, Iterable<ItemStack> {
   float DEFAULT_DISTANCE_BUFFER = 4.0F;

   int getContainerSize();

   boolean isEmpty();

   ItemStack getItem(int var1);

   ItemStack removeItem(int var1, int var2);

   ItemStack removeItemNoUpdate(int var1);

   void setItem(int var1, ItemStack var2);

   default int getMaxStackSize() {
      return 99;
   }

   default int getMaxStackSize(ItemStack $$0) {
      return Math.min(this.getMaxStackSize(), $$0.getMaxStackSize());
   }

   void setChanged();

   boolean stillValid(Player var1);

   default void startOpen(ContainerUser $$0) {
   }

   default void stopOpen(ContainerUser $$0) {
   }

   default List<ContainerUser> getEntitiesWithContainerOpen() {
      return List.of();
   }

   default boolean canPlaceItem(int $$0, ItemStack $$1) {
      return true;
   }

   default boolean canTakeItem(Container $$0, int $$1, ItemStack $$2) {
      return true;
   }

   default int countItem(Item $$0) {
      int $$1 = 0;

      for (ItemStack $$2 : this) {
         if ($$2.getItem().equals($$0)) {
            $$1 += $$2.getCount();
         }
      }

      return $$1;
   }

   default boolean hasAnyOf(Set<Item> $$0) {
      return this.hasAnyMatching($$1 -> !$$1.isEmpty() && $$0.contains($$1.getItem()));
   }

   default boolean hasAnyMatching(Predicate<ItemStack> $$0) {
      for (ItemStack $$1 : this) {
         if ($$0.test($$1)) {
            return true;
         }
      }

      return false;
   }

   static boolean stillValidBlockEntity(BlockEntity $$0, Player $$1) {
      return stillValidBlockEntity($$0, $$1, 4.0F);
   }

   static boolean stillValidBlockEntity(BlockEntity $$0, Player $$1, float $$2) {
      Level $$3 = $$0.getLevel();
      BlockPos $$4 = $$0.getBlockPos();
      if ($$3 == null) {
         return false;
      } else {
         return $$3.getBlockEntity($$4) != $$0 ? false : $$1.isWithinBlockInteractionRange($$4, $$2);
      }
   }

   
   default SlotAccess getSlot(final int $$0) {
      return $$0 >= 0 && $$0 < this.getContainerSize() ? new SlotAccess() {
         public ItemStack get() {
            return Container.this.getItem($$0);
         }

         public boolean set(ItemStack $$0x) {
            Container.this.setItem($$0, $$0);
            return true;
         }
      } : null;
   }

   @Override
   default Iterator<ItemStack> iterator() {
      return new Container.ContainerIterator(this);
   }

   public static class ContainerIterator implements Iterator<ItemStack> {
      private final Container container;
      private int index;
      private final int size;

      public ContainerIterator(Container $$0) {
         this.container = $$0;
         this.size = $$0.getContainerSize();
      }

      @Override
      public boolean hasNext() {
         return this.index < this.size;
      }

      public ItemStack next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            return this.container.getItem(this.index++);
         }
      }
   }
}
