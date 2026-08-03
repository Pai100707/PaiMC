package net.minecraft.world.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity.Occupant;
import org.apache.commons.lang3.math.Fraction;
import org.jspecify.annotations.Nullable;

public final class BundleContents implements TooltipComponent {
   public static final BundleContents EMPTY = new BundleContents(List.of());
   public static final Codec<BundleContents> CODEC = net.minecraft.world.item.ItemStack.CODEC
      .listOf()
      .flatXmap(BundleContents::checkAndCreate, $$0 -> DataResult.success($$0.items));
   public static final StreamCodec<RegistryFriendlyByteBuf, BundleContents> STREAM_CODEC = net.minecraft.world.item.ItemStack.STREAM_CODEC
      .apply(ByteBufCodecs.list())
      .map(BundleContents::new, $$0 -> $$0.items);
   private static final Fraction BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(1, 16);
   private static final int NO_STACK_INDEX = -1;
   public static final int NO_SELECTED_ITEM_INDEX = -1;
   final List<net.minecraft.world.item.ItemStack> items;
   final Fraction weight;
   final int selectedItem;

   BundleContents(List<net.minecraft.world.item.ItemStack> $$0, Fraction $$1, int $$2) {
      this.items = $$0;
      this.weight = $$1;
      this.selectedItem = $$2;
   }

   private static DataResult<BundleContents> checkAndCreate(List<net.minecraft.world.item.ItemStack> $$0) {
      try {
         Fraction $$1 = computeContentWeight($$0);
         return DataResult.success(new BundleContents($$0, $$1, -1));
      } catch (ArithmeticException var2) {
         return DataResult.error(() -> "Excessive total bundle weight");
      }
   }

   public BundleContents(List<net.minecraft.world.item.ItemStack> $$0) {
      this($$0, computeContentWeight($$0), -1);
   }

   private static Fraction computeContentWeight(List<net.minecraft.world.item.ItemStack> $$0) {
      Fraction $$1 = Fraction.ZERO;

      for (net.minecraft.world.item.ItemStack $$2 : $$0) {
         $$1 = $$1.add(getWeight($$2).multiplyBy(Fraction.getFraction($$2.getCount(), 1)));
      }

      return $$1;
   }

   static Fraction getWeight(net.minecraft.world.item.ItemStack $$0) {
      BundleContents $$1 = (BundleContents)$$0.get(DataComponents.BUNDLE_CONTENTS);
      if ($$1 != null) {
         return BUNDLE_IN_BUNDLE_WEIGHT.add($$1.weight());
      } else {
         List<Occupant> $$2 = ((Bees)$$0.getOrDefault(DataComponents.BEES, Bees.EMPTY)).bees();
         return !$$2.isEmpty() ? Fraction.ONE : Fraction.getFraction(1, $$0.getMaxStackSize());
      }
   }

   public static boolean canItemBeInBundle(net.minecraft.world.item.ItemStack $$0) {
      return !$$0.isEmpty() && $$0.getItem().canFitInsideContainerItems();
   }

   public int getNumberOfItemsToShow() {
      int $$0 = this.size();
      int $$1 = $$0 > 12 ? 11 : 12;
      int $$2 = $$0 % 4;
      int $$3 = $$2 == 0 ? 0 : 4 - $$2;
      return Math.min($$0, $$1 - $$3);
   }

   public net.minecraft.world.item.ItemStack getItemUnsafe(int $$0) {
      return this.items.get($$0);
   }

   public Stream<net.minecraft.world.item.ItemStack> itemCopyStream() {
      return this.items.stream().map(net.minecraft.world.item.ItemStack::copy);
   }

   public Iterable<net.minecraft.world.item.ItemStack> items() {
      return this.items;
   }

   public Iterable<net.minecraft.world.item.ItemStack> itemsCopy() {
      return Lists.transform(this.items, net.minecraft.world.item.ItemStack::copy);
   }

   public int size() {
      return this.items.size();
   }

   public Fraction weight() {
      return this.weight;
   }

   public boolean isEmpty() {
      return this.items.isEmpty();
   }

   public int getSelectedItem() {
      return this.selectedItem;
   }

   public boolean hasSelectedItem() {
      return this.selectedItem != -1;
   }

   @Override
   public boolean equals(Object $$0) {
      if (this == $$0) {
         return true;
      } else {
         return !($$0 instanceof BundleContents $$1)
            ? false
            : this.weight.equals($$1.weight) && net.minecraft.world.item.ItemStack.listMatches(this.items, $$1.items);
      }
   }

   @Override
   public int hashCode() {
      return net.minecraft.world.item.ItemStack.hashStackList(this.items);
   }

   @Override
   public String toString() {
      return "BundleContents" + this.items;
   }

   public static class Mutable {
      private final List<net.minecraft.world.item.ItemStack> items;
      private Fraction weight;
      private int selectedItem;

      public Mutable(BundleContents $$0) {
         this.items = new ArrayList<>($$0.items);
         this.weight = $$0.weight;
         this.selectedItem = $$0.selectedItem;
      }

      public BundleContents.Mutable clearItems() {
         this.items.clear();
         this.weight = Fraction.ZERO;
         this.selectedItem = -1;
         return this;
      }

      private int findStackIndex(net.minecraft.world.item.ItemStack $$0) {
         if (!$$0.isStackable()) {
            return -1;
         } else {
            for (int $$1 = 0; $$1 < this.items.size(); $$1++) {
               if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(this.items.get($$1), $$0)) {
                  return $$1;
               }
            }

            return -1;
         }
      }

      private int getMaxAmountToAdd(net.minecraft.world.item.ItemStack $$0) {
         Fraction $$1 = Fraction.ONE.subtract(this.weight);
         return Math.max($$1.divideBy(BundleContents.getWeight($$0)).intValue(), 0);
      }

      public int tryInsert(net.minecraft.world.item.ItemStack $$0) {
         if (!BundleContents.canItemBeInBundle($$0)) {
            return 0;
         } else {
            int $$1 = Math.min($$0.getCount(), this.getMaxAmountToAdd($$0));
            if ($$1 == 0) {
               return 0;
            } else {
               this.weight = this.weight.add(BundleContents.getWeight($$0).multiplyBy(Fraction.getFraction($$1, 1)));
               int $$2 = this.findStackIndex($$0);
               if ($$2 != -1) {
                  net.minecraft.world.item.ItemStack $$3 = this.items.remove($$2);
                  net.minecraft.world.item.ItemStack $$4 = $$3.copyWithCount($$3.getCount() + $$1);
                  $$0.shrink($$1);
                  this.items.add(0, $$4);
               } else {
                  this.items.add(0, $$0.split($$1));
               }

               return $$1;
            }
         }
      }

      public int tryTransfer(Slot $$0, Player $$1) {
         net.minecraft.world.item.ItemStack $$2 = $$0.getItem();
         int $$3 = this.getMaxAmountToAdd($$2);
         return BundleContents.canItemBeInBundle($$2) ? this.tryInsert($$0.safeTake($$2.getCount(), $$3, $$1)) : 0;
      }

      public void toggleSelectedItem(int $$0) {
         this.selectedItem = this.selectedItem != $$0 && !this.indexIsOutsideAllowedBounds($$0) ? $$0 : -1;
      }

      private boolean indexIsOutsideAllowedBounds(int $$0) {
         return $$0 < 0 || $$0 >= this.items.size();
      }

      @Nullable
      public net.minecraft.world.item.ItemStack removeOne() {
         if (this.items.isEmpty()) {
            return null;
         } else {
            int $$0 = this.indexIsOutsideAllowedBounds(this.selectedItem) ? 0 : this.selectedItem;
            net.minecraft.world.item.ItemStack $$1 = this.items.remove($$0).copy();
            this.weight = this.weight.subtract(BundleContents.getWeight($$1).multiplyBy(Fraction.getFraction($$1.getCount(), 1)));
            this.toggleSelectedItem(-1);
            return $$1;
         }
      }

      public Fraction weight() {
         return this.weight;
      }

      public BundleContents toImmutable() {
         return new BundleContents(List.copyOf(this.items), this.weight, this.selectedItem);
      }
   }
}
