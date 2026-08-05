/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public class BoatDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final EntityType<? extends AbstractBoat> type;

    public BoatDispenseItemBehavior(EntityType<? extends AbstractBoat> $$0) {
        this.type = $$0;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public ItemStack execute(BlockSource $$0, ItemStack $$1) {
        Direction $$2 = $$0.state().getValue(DispenserBlock.FACING);
        ServerLevel $$3 = $$0.level();
        Vec3 $$4 = $$0.center();
        double $$5 = 0.5625 + (double)this.type.getWidth() / 2.0;
        double $$6 = $$4.x() + (double)$$2.getStepX() * $$5;
        double $$7 = $$4.y() + (double)((float)$$2.getStepY() * 1.125f);
        double $$8 = $$4.z() + (double)$$2.getStepZ() * $$5;
        BlockPos $$9 = $$0.pos().relative($$2);
        if ($$3.getFluidState($$9).is(FluidTags.WATER)) {
            double $$10 = 1.0;
        } else if ($$3.getBlockState($$9).isAir() && $$3.getFluidState($$9.below()).is(FluidTags.WATER)) {
            double $$11 = 0.0;
        } else {
            return this.defaultDispenseItemBehavior.dispense($$0, $$1);
        }
        AbstractBoat $$13 = this.type.create($$3, EntitySpawnReason.DISPENSER);
        if ($$13 != null) {
            void $$12;
            $$13.setInitialPos($$6, $$7 + $$12, $$8);
            EntityType.createDefaultStackConfig($$3, $$1, null).accept($$13);
            $$13.setYRot($$2.toYRot());
            $$3.addFreshEntity($$13);
            $$1.shrink(1);
        }
        return $$1;
    }

    @Override
    protected void playSound(BlockSource $$0) {
        $$0.level().levelEvent(1000, $$0.pos(), 0);
    }
}

