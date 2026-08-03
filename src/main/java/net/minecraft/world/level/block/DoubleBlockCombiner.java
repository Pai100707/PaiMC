package net.minecraft.world.level.block;

import java.util.function.BiPredicate;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class DoubleBlockCombiner {
   public static <S extends BlockEntity> DoubleBlockCombiner.NeighborCombineResult<S> combineWithNeigbour(
      BlockEntityType<S> $$0,
      Function<BlockState, DoubleBlockCombiner.BlockType> $$1,
      Function<BlockState, Direction> $$2,
      Property<Direction> $$3,
      BlockState $$4,
      net.minecraft.world.level.LevelAccessor $$5,
      BlockPos $$6,
      BiPredicate<net.minecraft.world.level.LevelAccessor, BlockPos> $$7
   ) {
      S $$8 = $$0.getBlockEntity($$5, $$6);
      if ($$8 == null) {
         return DoubleBlockCombiner.Combiner::acceptNone;
      } else if ($$7.test($$5, $$6)) {
         return DoubleBlockCombiner.Combiner::acceptNone;
      } else {
         DoubleBlockCombiner.BlockType $$9 = $$1.apply($$4);
         boolean $$10 = $$9 == DoubleBlockCombiner.BlockType.SINGLE;
         boolean $$11 = $$9 == DoubleBlockCombiner.BlockType.FIRST;
         if ($$10) {
            return new DoubleBlockCombiner.NeighborCombineResult.Single<>($$8);
         } else {
            BlockPos $$12 = $$6.relative($$2.apply($$4));
            BlockState $$13 = $$5.getBlockState($$12);
            if ($$13.is($$4.getBlock())) {
               DoubleBlockCombiner.BlockType $$14 = $$1.apply($$13);
               if ($$14 != DoubleBlockCombiner.BlockType.SINGLE && $$9 != $$14 && $$13.getValue($$3) == $$4.getValue($$3)) {
                  if ($$7.test($$5, $$12)) {
                     return DoubleBlockCombiner.Combiner::acceptNone;
                  }

                  S $$15 = $$0.getBlockEntity($$5, $$12);
                  if ($$15 != null) {
                     S $$16 = $$11 ? $$8 : $$15;
                     S $$17 = $$11 ? $$15 : $$8;
                     return new DoubleBlockCombiner.NeighborCombineResult.Double<>($$16, $$17);
                  }
               }
            }

            return new DoubleBlockCombiner.NeighborCombineResult.Single<>($$8);
         }
      }
   }

   public static enum BlockType {
      SINGLE,
      FIRST,
      SECOND;
   }

   public interface Combiner<S, T> {
      T acceptDouble(S var1, S var2);

      T acceptSingle(S var1);

      T acceptNone();
   }

   public interface NeighborCombineResult<S> {
      <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> var1);

      public static final class Double<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
         private final S first;
         private final S second;

         public Double(S $$0, S $$1) {
            this.first = $$0;
            this.second = $$1;
         }

         @Override
         public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> $$0) {
            return $$0.acceptDouble(this.first, this.second);
         }
      }

      public static final class Single<S> implements DoubleBlockCombiner.NeighborCombineResult<S> {
         private final S single;

         public Single(S $$0) {
            this.single = $$0;
         }

         @Override
         public <T> T apply(DoubleBlockCombiner.Combiner<? super S, T> $$0) {
            return $$0.acceptSingle(this.single);
         }
      }
   }
}
