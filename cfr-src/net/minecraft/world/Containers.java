/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Containers {
    public static void dropContents(Level $$0, BlockPos $$1, Container $$2) {
        Containers.dropContents($$0, $$1.getX(), $$1.getY(), $$1.getZ(), $$2);
    }

    public static void dropContents(Level $$0, Entity $$1, Container $$2) {
        Containers.dropContents($$0, $$1.getX(), $$1.getY(), $$1.getZ(), $$2);
    }

    private static void dropContents(Level $$0, double $$1, double $$2, double $$3, Container $$4) {
        for (int $$5 = 0; $$5 < $$4.getContainerSize(); ++$$5) {
            Containers.dropItemStack($$0, $$1, $$2, $$3, $$4.getItem($$5));
        }
    }

    public static void dropContents(Level $$0, BlockPos $$1, NonNullList<ItemStack> $$22) {
        $$22.forEach($$2 -> Containers.dropItemStack($$0, $$1.getX(), $$1.getY(), $$1.getZ(), $$2));
    }

    public static void dropItemStack(Level $$0, double $$1, double $$2, double $$3, ItemStack $$4) {
        double $$5 = EntityType.ITEM.getWidth();
        double $$6 = 1.0 - $$5;
        double $$7 = $$5 / 2.0;
        double $$8 = Math.floor($$1) + $$0.random.nextDouble() * $$6 + $$7;
        double $$9 = Math.floor($$2) + $$0.random.nextDouble() * $$6;
        double $$10 = Math.floor($$3) + $$0.random.nextDouble() * $$6 + $$7;
        while (!$$4.isEmpty()) {
            ItemEntity $$11 = new ItemEntity($$0, $$8, $$9, $$10, $$4.split($$0.random.nextInt(21) + 10));
            float $$12 = 0.05f;
            $$11.setDeltaMovement($$0.random.triangle(0.0, 0.11485000171139836), $$0.random.triangle(0.2, 0.11485000171139836), $$0.random.triangle(0.0, 0.11485000171139836));
            $$0.addFreshEntity($$11);
        }
    }

    public static void updateNeighboursAfterDestroy(BlockState $$0, Level $$1, BlockPos $$2) {
        $$1.updateNeighbourForOutputSignal($$2, $$0.getBlock());
    }
}

