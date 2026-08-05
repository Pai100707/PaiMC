/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class MinecartDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final EntityType<? extends AbstractMinecart> entityType;

    public MinecartDispenseItemBehavior(EntityType<? extends AbstractMinecart> $$0) {
        this.entityType = $$0;
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ItemStack execute(BlockSource $$0, ItemStack $$1) {
        void $$16;
        Direction $$2 = $$0.state().getValue(DispenserBlock.FACING);
        ServerLevel $$3 = $$0.level();
        Vec3 $$4 = $$0.center();
        double $$5 = $$4.x() + (double)$$2.getStepX() * 1.125;
        double $$6 = Math.floor($$4.y()) + (double)$$2.getStepY();
        double $$7 = $$4.z() + (double)$$2.getStepZ() * 1.125;
        BlockPos $$8 = $$0.pos().relative($$2);
        BlockState $$9 = $$3.getBlockState($$8);
        if ($$9.is(BlockTags.RAILS)) {
            if (MinecartDispenseItemBehavior.getRailShape($$9).isSlope()) {
                double $$10 = 0.6;
            } else {
                double $$11 = 0.1;
            }
        } else {
            if (!$$9.isAir()) return this.defaultDispenseItemBehavior.dispense($$0, $$1);
            BlockState $$12 = $$3.getBlockState($$8.below());
            if (!$$12.is(BlockTags.RAILS)) return this.defaultDispenseItemBehavior.dispense($$0, $$1);
            if ($$2 == Direction.DOWN || !MinecartDispenseItemBehavior.getRailShape($$12).isSlope()) {
                double $$13 = -0.9;
            } else {
                double $$14 = -0.4;
            }
        }
        Vec3 $$17 = new Vec3($$5, $$6 + $$16, $$7);
        AbstractMinecart $$18 = AbstractMinecart.createMinecart($$3, $$17.x, $$17.y, $$17.z, this.entityType, EntitySpawnReason.DISPENSER, $$1, null);
        if ($$18 == null) return $$1;
        $$3.addFreshEntity($$18);
        $$1.shrink(1);
        return $$1;
    }

    private static RailShape getRailShape(BlockState $$0) {
        RailShape railShape;
        Block block = $$0.getBlock();
        if (block instanceof BaseRailBlock) {
            BaseRailBlock $$1 = (BaseRailBlock)block;
            railShape = $$0.getValue($$1.getShapeProperty());
        } else {
            railShape = RailShape.NORTH_SOUTH;
        }
        return railShape;
    }

    @Override
    protected void playSound(BlockSource $$0) {
        $$0.level().levelEvent(1000, $$0.pos(), 0);
    }
}

