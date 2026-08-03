package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import net.minecraft.world.item.ItemStack;

public class EntityEquipment {
   public static final Codec<net.minecraft.world.entity.EntityEquipment> CODEC = Codec.unboundedMap(
         net.minecraft.world.entity.EquipmentSlot.CODEC, ItemStack.CODEC
      )
      .xmap($$0 -> {
         EnumMap<net.minecraft.world.entity.EquipmentSlot, ItemStack> $$1 = new EnumMap<>(net.minecraft.world.entity.EquipmentSlot.class);
         $$1.putAll($$0);
         return new net.minecraft.world.entity.EntityEquipment($$1);
      }, $$0 -> {
         Map<net.minecraft.world.entity.EquipmentSlot, ItemStack> $$1 = new EnumMap<>($$0.items);
         $$1.values().removeIf(ItemStack::isEmpty);
         return $$1;
      });
   private final EnumMap<net.minecraft.world.entity.EquipmentSlot, ItemStack> items;

   private EntityEquipment(EnumMap<net.minecraft.world.entity.EquipmentSlot, ItemStack> $$0) {
      this.items = $$0;
   }

   public EntityEquipment() {
      this(new EnumMap<>(net.minecraft.world.entity.EquipmentSlot.class));
   }

   public ItemStack set(net.minecraft.world.entity.EquipmentSlot $$0, ItemStack $$1) {
      return Objects.requireNonNullElse(this.items.put($$0, $$1), ItemStack.EMPTY);
   }

   public ItemStack get(net.minecraft.world.entity.EquipmentSlot $$0) {
      return this.items.getOrDefault($$0, ItemStack.EMPTY);
   }

   public boolean isEmpty() {
      for (ItemStack $$0 : this.items.values()) {
         if (!$$0.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public void tick(net.minecraft.world.entity.Entity $$0) {
      for (Entry<net.minecraft.world.entity.EquipmentSlot, ItemStack> $$1 : this.items.entrySet()) {
         ItemStack $$2 = $$1.getValue();
         if (!$$2.isEmpty()) {
            $$2.inventoryTick($$0.level(), $$0, $$1.getKey());
         }
      }
   }

   public void setAll(net.minecraft.world.entity.EntityEquipment $$0) {
      this.items.clear();
      this.items.putAll($$0.items);
   }

   public void dropAll(net.minecraft.world.entity.LivingEntity $$0) {
      for (ItemStack $$1 : this.items.values()) {
         $$0.drop($$1, true, false);
      }

      this.clear();
   }

   public void clear() {
      this.items.replaceAll(($$0, $$1) -> ItemStack.EMPTY);
   }
}
