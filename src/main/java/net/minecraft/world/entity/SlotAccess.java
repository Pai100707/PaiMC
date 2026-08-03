package net.minecraft.world.entity;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

public interface SlotAccess {
   ItemStack get();

   boolean set(ItemStack var1);

   static net.minecraft.world.entity.SlotAccess of(final Supplier<ItemStack> $$0, final Consumer<ItemStack> $$1) {
      return new net.minecraft.world.entity.SlotAccess() {
         @Override
         public ItemStack get() {
            return $$0.get();
         }

         @Override
         public boolean set(ItemStack $$0x) {
            $$1.accept($$0);
            return true;
         }
      };
   }

   static net.minecraft.world.entity.SlotAccess forEquipmentSlot(
      final net.minecraft.world.entity.LivingEntity $$0, final net.minecraft.world.entity.EquipmentSlot $$1, final Predicate<ItemStack> $$2
   ) {
      return new net.minecraft.world.entity.SlotAccess() {
         @Override
         public ItemStack get() {
            return $$0.getItemBySlot($$1);
         }

         @Override
         public boolean set(ItemStack $$0x) {
            if (!$$2.test($$0)) {
               return false;
            } else {
               $$0.setItemSlot($$1, $$0);
               return true;
            }
         }
      };
   }

   static net.minecraft.world.entity.SlotAccess forEquipmentSlot(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.EquipmentSlot $$1) {
      return forEquipmentSlot($$0, $$1, $$0x -> true);
   }

   static net.minecraft.world.entity.SlotAccess forListElement(final List<ItemStack> $$0, final int $$1) {
      return new net.minecraft.world.entity.SlotAccess() {
         @Override
         public ItemStack get() {
            return $$0.get($$1);
         }

         @Override
         public boolean set(ItemStack $$0x) {
            $$0.set($$1, $$0);
            return true;
         }
      };
   }
}
