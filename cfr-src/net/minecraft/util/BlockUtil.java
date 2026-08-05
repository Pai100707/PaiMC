/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
    public static FoundRectangle getLargestRectangleAround(BlockPos $$0, Direction.Axis $$1, int $$2, Direction.Axis $$3, int $$4, Predicate<BlockPos> $$5) {
        BlockPos.MutableBlockPos $$6 = $$0.mutable();
        Direction $$7 = Direction.get(Direction.AxisDirection.NEGATIVE, $$1);
        Direction $$8 = $$7.getOpposite();
        Direction $$9 = Direction.get(Direction.AxisDirection.NEGATIVE, $$3);
        Direction $$10 = $$9.getOpposite();
        int $$11 = BlockUtil.getLimit($$5, $$6.set($$0), $$7, $$2);
        int $$12 = BlockUtil.getLimit($$5, $$6.set($$0), $$8, $$2);
        int $$13 = $$11;
        IntBounds[] $$14 = new IntBounds[$$13 + 1 + $$12];
        $$14[$$13] = new IntBounds(BlockUtil.getLimit($$5, $$6.set($$0), $$9, $$4), BlockUtil.getLimit($$5, $$6.set($$0), $$10, $$4));
        int $$15 = $$14[$$13].min;
        for (int $$16 = 1; $$16 <= $$11; ++$$16) {
            IntBounds $$17 = $$14[$$13 - ($$16 - 1)];
            $$14[$$13 - $$16] = new IntBounds(BlockUtil.getLimit($$5, $$6.set($$0).move($$7, $$16), $$9, $$17.min), BlockUtil.getLimit($$5, $$6.set($$0).move($$7, $$16), $$10, $$17.max));
        }
        for (int $$18 = 1; $$18 <= $$12; ++$$18) {
            IntBounds $$19 = $$14[$$13 + $$18 - 1];
            $$14[$$13 + $$18] = new IntBounds(BlockUtil.getLimit($$5, $$6.set($$0).move($$8, $$18), $$9, $$19.min), BlockUtil.getLimit($$5, $$6.set($$0).move($$8, $$18), $$10, $$19.max));
        }
        int $$20 = 0;
        int $$21 = 0;
        int $$22 = 0;
        int $$23 = 0;
        int[] $$24 = new int[$$14.length];
        for (int $$25 = $$15; $$25 >= 0; --$$25) {
            for (int $$26 = 0; $$26 < $$14.length; ++$$26) {
                IntBounds $$27 = $$14[$$26];
                int $$28 = $$15 - $$27.min;
                int $$29 = $$15 + $$27.max;
                $$24[$$26] = $$25 >= $$28 && $$25 <= $$29 ? $$29 + 1 - $$25 : 0;
            }
            Pair<IntBounds, Integer> $$30 = BlockUtil.getMaxRectangleLocation($$24);
            IntBounds $$31 = (IntBounds)$$30.getFirst();
            int $$32 = 1 + $$31.max - $$31.min;
            int $$33 = (Integer)$$30.getSecond();
            if ($$32 * $$33 <= $$22 * $$23) continue;
            $$20 = $$31.min;
            $$21 = $$25;
            $$22 = $$32;
            $$23 = $$33;
        }
        return new FoundRectangle($$0.relative($$1, $$20 - $$13).relative($$3, $$21 - $$15), $$22, $$23);
    }

    private static int getLimit(Predicate<BlockPos> $$0, BlockPos.MutableBlockPos $$1, Direction $$2, int $$3) {
        int $$4;
        for ($$4 = 0; $$4 < $$3 && $$0.test($$1.move($$2)); ++$$4) {
        }
        return $$4;
    }

    @VisibleForTesting
    static Pair<IntBounds, Integer> getMaxRectangleLocation(int[] $$0) {
        int $$1 = 0;
        int $$2 = 0;
        int $$3 = 0;
        IntArrayList $$4 = new IntArrayList();
        $$4.push(0);
        for (int $$5 = 1; $$5 <= $$0.length; ++$$5) {
            int $$6;
            int n = $$6 = $$5 == $$0.length ? 0 : $$0[$$5];
            while (!$$4.isEmpty()) {
                int $$7 = $$0[$$4.topInt()];
                if ($$6 >= $$7) {
                    $$4.push($$5);
                    break;
                }
                $$4.popInt();
                int $$8 = $$4.isEmpty() ? 0 : $$4.topInt() + 1;
                if ($$7 * ($$5 - $$8) <= $$3 * ($$2 - $$1)) continue;
                $$2 = $$5;
                $$1 = $$8;
                $$3 = $$7;
            }
            if (!$$4.isEmpty()) continue;
            $$4.push($$5);
        }
        return new Pair((Object)new IntBounds($$1, $$2 - 1), (Object)$$3);
    }

    public static Optional<BlockPos> getTopConnectedBlock(BlockGetter $$0, BlockPos $$1, Block $$2, Direction $$3, Block $$4) {
        BlockState $$6;
        BlockPos.MutableBlockPos $$5 = $$1.mutable();
        do {
            $$5.move($$3);
        } while (($$6 = $$0.getBlockState($$5)).is($$2));
        if ($$6.is($$4)) {
            return Optional.of($$5);
        }
        return Optional.empty();
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int $$0, int $$1) {
            this.min = $$0;
            this.max = $$1;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
        }
    }

    public static class FoundRectangle {
        public final BlockPos minCorner;
        public final int axis1Size;
        public final int axis2Size;

        public FoundRectangle(BlockPos $$0, int $$1, int $$2) {
            this.minCorner = $$0;
            this.axis1Size = $$1;
            this.axis2Size = $$2;
        }
    }
}

