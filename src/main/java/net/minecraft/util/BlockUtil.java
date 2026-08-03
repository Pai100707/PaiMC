package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockUtil {
   public static net.minecraft.util.BlockUtil.FoundRectangle getLargestRectangleAround(
      BlockPos $$0, Axis $$1, int $$2, Axis $$3, int $$4, Predicate<BlockPos> $$5
   ) {
      MutableBlockPos $$6 = $$0.mutable();
      Direction $$7 = Direction.get(AxisDirection.NEGATIVE, $$1);
      Direction $$8 = $$7.getOpposite();
      Direction $$9 = Direction.get(AxisDirection.NEGATIVE, $$3);
      Direction $$10 = $$9.getOpposite();
      int $$11 = getLimit($$5, $$6.set($$0), $$7, $$2);
      int $$12 = getLimit($$5, $$6.set($$0), $$8, $$2);
      int $$13 = $$11;
      net.minecraft.util.BlockUtil.IntBounds[] $$14 = new net.minecraft.util.BlockUtil.IntBounds[$$11 + 1 + $$12];
      $$14[$$11] = new net.minecraft.util.BlockUtil.IntBounds(getLimit($$5, $$6.set($$0), $$9, $$4), getLimit($$5, $$6.set($$0), $$10, $$4));
      int $$15 = $$14[$$11].min;

      for (int $$16 = 1; $$16 <= $$11; $$16++) {
         net.minecraft.util.BlockUtil.IntBounds $$17 = $$14[$$13 - ($$16 - 1)];
         $$14[$$13 - $$16] = new net.minecraft.util.BlockUtil.IntBounds(
            getLimit($$5, $$6.set($$0).move($$7, $$16), $$9, $$17.min), getLimit($$5, $$6.set($$0).move($$7, $$16), $$10, $$17.max)
         );
      }

      for (int $$18 = 1; $$18 <= $$12; $$18++) {
         net.minecraft.util.BlockUtil.IntBounds $$19 = $$14[$$13 + $$18 - 1];
         $$14[$$13 + $$18] = new net.minecraft.util.BlockUtil.IntBounds(
            getLimit($$5, $$6.set($$0).move($$8, $$18), $$9, $$19.min), getLimit($$5, $$6.set($$0).move($$8, $$18), $$10, $$19.max)
         );
      }

      int $$20 = 0;
      int $$21 = 0;
      int $$22 = 0;
      int $$23 = 0;
      int[] $$24 = new int[$$14.length];

      for (int $$25 = $$15; $$25 >= 0; $$25--) {
         for (int $$26 = 0; $$26 < $$14.length; $$26++) {
            net.minecraft.util.BlockUtil.IntBounds $$27 = $$14[$$26];
            int $$28 = $$15 - $$27.min;
            int $$29 = $$15 + $$27.max;
            $$24[$$26] = $$25 >= $$28 && $$25 <= $$29 ? $$29 + 1 - $$25 : 0;
         }

         Pair<net.minecraft.util.BlockUtil.IntBounds, Integer> $$30 = getMaxRectangleLocation($$24);
         net.minecraft.util.BlockUtil.IntBounds $$31 = (net.minecraft.util.BlockUtil.IntBounds)$$30.getFirst();
         int $$32 = 1 + $$31.max - $$31.min;
         int $$33 = (Integer)$$30.getSecond();
         if ($$32 * $$33 > $$22 * $$23) {
            $$20 = $$31.min;
            $$21 = $$25;
            $$22 = $$32;
            $$23 = $$33;
         }
      }

      return new net.minecraft.util.BlockUtil.FoundRectangle($$0.relative($$1, $$20 - $$13).relative($$3, $$21 - $$15), $$22, $$23);
   }

   private static int getLimit(Predicate<BlockPos> $$0, MutableBlockPos $$1, Direction $$2, int $$3) {
      int $$4 = 0;

      while ($$4 < $$3 && $$0.test($$1.move($$2))) {
         $$4++;
      }

      return $$4;
   }

   @VisibleForTesting
   static Pair<net.minecraft.util.BlockUtil.IntBounds, Integer> getMaxRectangleLocation(int[] $$0) {
      int $$1 = 0;
      int $$2 = 0;
      int $$3 = 0;
      IntStack $$4 = new IntArrayList();
      $$4.push(0);

      for (int $$5 = 1; $$5 <= $$0.length; $$5++) {
         int $$6 = $$5 == $$0.length ? 0 : $$0[$$5];

         while (!$$4.isEmpty()) {
            int $$7 = $$0[$$4.topInt()];
            if ($$6 >= $$7) {
               $$4.push($$5);
               break;
            }

            $$4.popInt();
            int $$8 = $$4.isEmpty() ? 0 : $$4.topInt() + 1;
            if ($$7 * ($$5 - $$8) > $$3 * ($$2 - $$1)) {
               $$2 = $$5;
               $$1 = $$8;
               $$3 = $$7;
            }
         }

         if ($$4.isEmpty()) {
            $$4.push($$5);
         }
      }

      return new Pair(new net.minecraft.util.BlockUtil.IntBounds($$1, $$2 - 1), $$3);
   }

   public static Optional<BlockPos> getTopConnectedBlock(BlockGetter $$0, BlockPos $$1, Block $$2, Direction $$3, Block $$4) {
      MutableBlockPos $$5 = $$1.mutable();

      BlockState $$6;
      do {
         $$5.move($$3);
         $$6 = $$0.getBlockState($$5);
      } while ($$6.is($$2));

      return $$6.is($$4) ? Optional.of($$5) : Optional.empty();
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

   public static class IntBounds {
      public final int min;
      public final int max;

      public IntBounds(int $$0, int $$1) {
         this.min = $$0;
         this.max = $$1;
      }

      @Override
      public String toString() {
         return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
      }
   }
}
