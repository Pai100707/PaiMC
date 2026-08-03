package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class EquipmentDispenseItemBehavior extends DefaultDispenseItemBehavior {
   public static final EquipmentDispenseItemBehavior INSTANCE = new EquipmentDispenseItemBehavior();

   @Override
   protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
      return dispenseEquipment($$0, $$1) ? $$1 : super.execute($$0, $$1);
   }

   public static boolean dispenseEquipment(BlockSource $$0, ItemStack $$1) {
      net.minecraft.core.BlockPos $$2 = $$0.pos().relative((net.minecraft.core.Direction)$$0.state().getValue(DispenserBlock.FACING));
      List<LivingEntity> $$3 = $$0.level().getEntitiesOfClass(LivingEntity.class, new AABB($$2), $$1x -> $$1x.canEquipWithDispenser($$1));
      if ($$3.isEmpty()) {
         return false;
      } else {
         LivingEntity $$4 = $$3.getFirst();
         EquipmentSlot $$5 = $$4.getEquipmentSlotForItem($$1);
         ItemStack $$6 = $$1.split(1);
         $$4.setItemSlot($$5, $$6);
         if ($$4 instanceof Mob $$7) {
            $$7.setGuaranteedDrop($$5);
            $$7.setPersistenceRequired();
         }

         return true;
      }
   }
}
