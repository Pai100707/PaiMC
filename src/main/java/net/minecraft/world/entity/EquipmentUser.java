package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

public interface EquipmentUser {
   void setItemSlot(net.minecraft.world.entity.EquipmentSlot var1, ItemStack var2);

   ItemStack getItemBySlot(net.minecraft.world.entity.EquipmentSlot var1);

   void setDropChance(net.minecraft.world.entity.EquipmentSlot var1, float var2);

   default void equip(net.minecraft.world.entity.EquipmentTable $$0, LootParams $$1) {
      this.equip($$0.lootTable(), $$1, $$0.slotDropChances());
   }

   default void equip(ResourceKey<LootTable> $$0, LootParams $$1, Map<net.minecraft.world.entity.EquipmentSlot, Float> $$2) {
      this.equip($$0, $$1, 0L, $$2);
   }

   default void equip(ResourceKey<LootTable> $$0, LootParams $$1, long $$2, Map<net.minecraft.world.entity.EquipmentSlot, Float> $$3) {
      LootTable $$4 = $$1.getLevel().getServer().reloadableRegistries().getLootTable($$0);
      if ($$4 != LootTable.EMPTY) {
         List<ItemStack> $$5 = $$4.getRandomItems($$1, $$2);
         List<net.minecraft.world.entity.EquipmentSlot> $$6 = new ArrayList<>();

         for (ItemStack $$7 : $$5) {
            net.minecraft.world.entity.EquipmentSlot $$8 = this.resolveSlot($$7, $$6);
            if ($$8 != null) {
               ItemStack $$9 = $$8.limit($$7);
               this.setItemSlot($$8, $$9);
               Float $$10 = $$3.get($$8);
               if ($$10 != null) {
                  this.setDropChance($$8, $$10);
               }

               $$6.add($$8);
            }
         }
      }
   }

   
   default net.minecraft.world.entity.EquipmentSlot resolveSlot(ItemStack $$0, List<net.minecraft.world.entity.EquipmentSlot> $$1) {
      if ($$0.isEmpty()) {
         return null;
      } else {
         Equippable $$2 = (Equippable)$$0.get(DataComponents.EQUIPPABLE);
         if ($$2 != null) {
            net.minecraft.world.entity.EquipmentSlot $$3 = $$2.slot();
            if (!$$1.contains($$3)) {
               return $$3;
            }
         } else if (!$$1.contains(net.minecraft.world.entity.EquipmentSlot.MAINHAND)) {
            return net.minecraft.world.entity.EquipmentSlot.MAINHAND;
         }

         return null;
      }
   }
}
