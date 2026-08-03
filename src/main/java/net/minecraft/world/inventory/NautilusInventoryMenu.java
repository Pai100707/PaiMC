package net.minecraft.world.inventory;

import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.player.Inventory;

public class NautilusInventoryMenu extends net.minecraft.world.inventory.AbstractMountInventoryMenu {
   private static final Identifier SADDLE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/saddle");
   private static final Identifier ARMOR_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/nautilus_armor_inventory");

   public NautilusInventoryMenu(int $$0, Inventory $$1, Container $$2, final AbstractNautilus $$3, int $$4) {
      super($$0, $$1, $$2, $$3);
      Container $$5 = $$3.createEquipmentSlotContainer(EquipmentSlot.SADDLE);
      this.addSlot(new net.minecraft.world.inventory.ArmorSlot($$5, $$3, EquipmentSlot.SADDLE, 0, 8, 18, SADDLE_SLOT_SPRITE) {
         @Override
         public boolean isActive() {
            return $$3.canUseSlot(EquipmentSlot.SADDLE);
         }
      });
      Container $$6 = $$3.createEquipmentSlotContainer(EquipmentSlot.BODY);
      this.addSlot(new net.minecraft.world.inventory.ArmorSlot($$6, $$3, EquipmentSlot.BODY, 0, 8, 36, ARMOR_SLOT_SPRITE) {
         @Override
         public boolean isActive() {
            return $$3.canUseSlot(EquipmentSlot.BODY);
         }
      });
      this.addStandardInventorySlots($$1, 8, 84);
   }

   @Override
   protected boolean hasInventoryChanged(Container $$0) {
      return ((AbstractNautilus)this.mount).hasInventoryChanged($$0);
   }
}
