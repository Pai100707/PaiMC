package net.minecraft.world.item.enchantment;

import java.util.function.Consumer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public record EnchantedItemInUse(
   net.minecraft.world.item.ItemStack itemStack, EquipmentSlot inSlot, LivingEntity owner, Consumer<net.minecraft.world.item.Item> onBreak
) {
   public EnchantedItemInUse(net.minecraft.world.item.ItemStack $$0, EquipmentSlot $$1, LivingEntity $$2) {
      this($$0, $$1, $$2, $$2x -> $$2.onEquippedItemBroken($$2x, $$1));
   }
}
