package net.minecraft.world.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jspecify.annotations.Nullable;

class ArmorSlot extends net.minecraft.world.inventory.Slot {
   private final LivingEntity owner;
   private final EquipmentSlot slot;
   @Nullable
   private final Identifier emptyIcon;

   public ArmorSlot(Container $$0, LivingEntity $$1, EquipmentSlot $$2, int $$3, int $$4, int $$5, @Nullable Identifier $$6) {
      super($$0, $$3, $$4, $$5);
      this.owner = $$1;
      this.slot = $$2;
      this.emptyIcon = $$6;
   }

   @Override
   public void setByPlayer(ItemStack $$0, ItemStack $$1) {
      this.owner.onEquipItem(this.slot, $$1, $$0);
      super.setByPlayer($$0, $$1);
   }

   @Override
   public int getMaxStackSize() {
      return 1;
   }

   @Override
   public boolean mayPlace(ItemStack $$0) {
      return this.owner.isEquippableInSlot($$0, this.slot);
   }

   @Override
   public boolean isActive() {
      return this.owner.canUseSlot(this.slot);
   }

   @Override
   public boolean mayPickup(Player $$0) {
      ItemStack $$1 = this.getItem();
      return !$$1.isEmpty() && !$$0.isCreative() && EnchantmentHelper.has($$1, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) ? false : super.mayPickup($$0);
   }

   @Nullable
   @Override
   public Identifier getNoItemIcon() {
      return this.emptyIcon;
   }
}
