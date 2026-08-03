package net.minecraft.world.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

public class ItemCombinerMenuSlotDefinition {
   private final List<net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition> slots;
   private final net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot;

   ItemCombinerMenuSlotDefinition(
      List<net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition> $$0,
      net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition $$1
   ) {
      if (!$$0.isEmpty() && !$$1.equals(net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY)) {
         this.slots = $$0;
         this.resultSlot = $$1;
      } else {
         throw new IllegalArgumentException("Need to define both inputSlots and resultSlot");
      }
   }

   public static net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.Builder create() {
      return new net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.Builder();
   }

   public net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition getSlot(int $$0) {
      return this.slots.get($$0);
   }

   public net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition getResultSlot() {
      return this.resultSlot;
   }

   public List<net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition> getSlots() {
      return this.slots;
   }

   public int getNumOfInputSlots() {
      return this.slots.size();
   }

   public int getResultSlotIndex() {
      return this.getNumOfInputSlots();
   }

   public static class Builder {
      private final List<net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition> inputSlots = new ArrayList<>();
      private net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition resultSlot = net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition.EMPTY;

      public net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.Builder withSlot(int $$0, int $$1, int $$2, Predicate<ItemStack> $$3) {
         this.inputSlots.add(new net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition($$0, $$1, $$2, $$3));
         return this;
      }

      public net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.Builder withResultSlot(int $$0, int $$1, int $$2) {
         this.resultSlot = new net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition($$0, $$1, $$2, $$0x -> false);
         return this;
      }

      public net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition build() {
         int $$0 = this.inputSlots.size();

         for (int $$1 = 0; $$1 < $$0; $$1++) {
            net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition $$2 = this.inputSlots.get($$1);
            if ($$2.slotIndex != $$1) {
               throw new IllegalArgumentException("Expected input slots to have continous indexes");
            }
         }

         if (this.resultSlot.slotIndex != $$0) {
            throw new IllegalArgumentException("Expected result slot index to follow last input slot");
         } else {
            return new net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition(this.inputSlots, this.resultSlot);
         }
      }
   }

   public record SlotDefinition(int slotIndex, int x, int y, Predicate<ItemStack> mayPlace) {
      static final net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition EMPTY = new net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition.SlotDefinition(
         0, 0, 0, $$0 -> true
      );
   }
}
