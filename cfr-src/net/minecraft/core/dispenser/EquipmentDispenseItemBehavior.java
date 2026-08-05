/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

public class EquipmentDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    public static final EquipmentDispenseItemBehavior INSTANCE = new EquipmentDispenseItemBehavior();

    @Override
    protected ItemStack execute(BlockSource $$0, ItemStack $$1) {
        return EquipmentDispenseItemBehavior.dispenseEquipment($$0, $$1) ? $$1 : super.execute($$0, $$1);
    }

    public static boolean dispenseEquipment(BlockSource $$0, ItemStack $$12) {
        BlockPos $$2 = $$0.pos().relative($$0.state().getValue(DispenserBlock.FACING));
        List<LivingEntity> $$3 = $$0.level().getEntitiesOfClass(LivingEntity.class, new AABB($$2), $$1 -> $$1.canEquipWithDispenser($$12));
        if ($$3.isEmpty()) {
            return false;
        }
        LivingEntity $$4 = $$3.getFirst();
        EquipmentSlot $$5 = $$4.getEquipmentSlotForItem($$12);
        ItemStack $$6 = $$12.split(1);
        $$4.setItemSlot($$5, $$6);
        if ($$4 instanceof Mob) {
            Mob $$7 = (Mob)$$4;
            $$7.setGuaranteedDrop($$5);
            $$7.setPersistenceRequired();
        }
        return true;
    }
}

