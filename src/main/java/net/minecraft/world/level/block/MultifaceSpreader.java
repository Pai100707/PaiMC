package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class MultifaceSpreader {
   public static final MultifaceSpreader.SpreadType[] DEFAULT_SPREAD_ORDER = new MultifaceSpreader.SpreadType[]{
      MultifaceSpreader.SpreadType.SAME_POSITION, MultifaceSpreader.SpreadType.SAME_PLANE, MultifaceSpreader.SpreadType.WRAP_AROUND
   };
   private final MultifaceSpreader.SpreadConfig config;

   public MultifaceSpreader(MultifaceBlock $$0) {
      this(new MultifaceSpreader.DefaultSpreaderConfig($$0));
   }

   public MultifaceSpreader(MultifaceSpreader.SpreadConfig $$0) {
      this.config = $$0;
   }

   public boolean canSpreadInAnyDirection(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return Direction.stream().anyMatch($$4 -> this.getSpreadFromFaceTowardDirection($$0, $$1, $$2, $$3, $$4, this.config::canSpreadInto).isPresent());
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromRandomFaceTowardRandomDirection(
      BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, RandomSource $$3
   ) {
      return Direction.allShuffled($$3)
         .stream()
         .filter($$1x -> this.config.canSpreadFrom($$0, $$1x))
         .map($$4 -> this.spreadFromFaceTowardRandomDirection($$0, $$1, $$2, $$4, $$3, false))
         .filter(Optional::isPresent)
         .findFirst()
         .orElse(Optional.empty());
   }

   public long spreadAll(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, boolean $$3) {
      return Direction.stream()
         .filter($$1x -> this.config.canSpreadFrom($$0, $$1x))
         .map($$4 -> this.spreadFromFaceTowardAllDirections($$0, $$1, $$2, $$4, $$3))
         .reduce(0L, Long::sum);
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardRandomDirection(
      BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, Direction $$3, RandomSource $$4, boolean $$5
   ) {
      return Direction.allShuffled($$4)
         .stream()
         .map($$5x -> this.spreadFromFaceTowardDirection($$0, $$1, $$2, $$3, $$5x, $$5))
         .filter(Optional::isPresent)
         .findFirst()
         .orElse(Optional.empty());
   }

   private long spreadFromFaceTowardAllDirections(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, Direction $$3, boolean $$4) {
      return Direction.stream().map($$5 -> this.spreadFromFaceTowardDirection($$0, $$1, $$2, $$3, $$5, $$4)).filter(Optional::isPresent).count();
   }

   @VisibleForTesting
   public Optional<MultifaceSpreader.SpreadPos> spreadFromFaceTowardDirection(
      BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, Direction $$3, Direction $$4, boolean $$5
   ) {
      return this.getSpreadFromFaceTowardDirection($$0, $$1, $$2, $$3, $$4, this.config::canSpreadInto).flatMap($$2x -> this.spreadToFace($$1, $$2x, $$5));
   }

   public Optional<MultifaceSpreader.SpreadPos> getSpreadFromFaceTowardDirection(
      BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3, Direction $$4, MultifaceSpreader.SpreadPredicate $$5
   ) {
      if ($$4.getAxis() == $$3.getAxis()) {
         return Optional.empty();
      } else if (this.config.isOtherBlockValidAsSource($$0) || this.config.hasFace($$0, $$3) && !this.config.hasFace($$0, $$4)) {
         for (MultifaceSpreader.SpreadType $$6 : this.config.getSpreadTypes()) {
            MultifaceSpreader.SpreadPos $$7 = $$6.getSpreadPos($$2, $$4, $$3);
            if ($$5.test($$1, $$2, $$7)) {
               return Optional.of($$7);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public Optional<MultifaceSpreader.SpreadPos> spreadToFace(net.minecraft.world.level.LevelAccessor $$0, MultifaceSpreader.SpreadPos $$1, boolean $$2) {
      BlockState $$3 = $$0.getBlockState($$1.pos());
      return this.config.placeBlock($$0, $$1, $$3, $$2) ? Optional.of($$1) : Optional.empty();
   }

   public static class DefaultSpreaderConfig implements MultifaceSpreader.SpreadConfig {
      protected MultifaceBlock block;

      public DefaultSpreaderConfig(MultifaceBlock $$0) {
         this.block = $$0;
      }

      @Nullable
      @Override
      public BlockState getStateForPlacement(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
         return this.block.getStateForPlacement($$0, $$1, $$2, $$3);
      }

      protected boolean stateCanBeReplaced(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockPos $$2, Direction $$3, BlockState $$4) {
         return $$4.isAir() || $$4.is(this.block) || $$4.is(Blocks.WATER) && $$4.getFluidState().isSource();
      }

      @Override
      public boolean canSpreadInto(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, MultifaceSpreader.SpreadPos $$2) {
         BlockState $$3 = $$0.getBlockState($$2.pos());
         return this.stateCanBeReplaced($$0, $$1, $$2.pos(), $$2.face(), $$3) && this.block.isValidStateForPlacement($$0, $$3, $$2.pos(), $$2.face());
      }
   }

   public interface SpreadConfig {
      @Nullable
      BlockState getStateForPlacement(BlockState var1, net.minecraft.world.level.BlockGetter var2, BlockPos var3, Direction var4);

      boolean canSpreadInto(net.minecraft.world.level.BlockGetter var1, BlockPos var2, MultifaceSpreader.SpreadPos var3);

      default MultifaceSpreader.SpreadType[] getSpreadTypes() {
         return MultifaceSpreader.DEFAULT_SPREAD_ORDER;
      }

      default boolean hasFace(BlockState $$0, Direction $$1) {
         return MultifaceBlock.hasFace($$0, $$1);
      }

      default boolean isOtherBlockValidAsSource(BlockState $$0) {
         return false;
      }

      default boolean canSpreadFrom(BlockState $$0, Direction $$1) {
         return this.isOtherBlockValidAsSource($$0) || this.hasFace($$0, $$1);
      }

      default boolean placeBlock(net.minecraft.world.level.LevelAccessor $$0, MultifaceSpreader.SpreadPos $$1, BlockState $$2, boolean $$3) {
         BlockState $$4 = this.getStateForPlacement($$2, $$0, $$1.pos(), $$1.face());
         if ($$4 != null) {
            if ($$3) {
               $$0.getChunk($$1.pos()).markPosForPostprocessing($$1.pos());
            }

            return $$0.setBlock($$1.pos(), $$4, 2);
         } else {
            return false;
         }
      }
   }

   public record SpreadPos(BlockPos pos, Direction face) {
   }

   @FunctionalInterface
   public interface SpreadPredicate {
      boolean test(net.minecraft.world.level.BlockGetter var1, BlockPos var2, MultifaceSpreader.SpreadPos var3);
   }

   public static enum SpreadType {
      SAME_POSITION {
         @Override
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos $$0, Direction $$1, Direction $$2) {
            return new MultifaceSpreader.SpreadPos($$0, $$1);
         }
      },
      SAME_PLANE {
         @Override
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos $$0, Direction $$1, Direction $$2) {
            return new MultifaceSpreader.SpreadPos($$0.relative($$1), $$2);
         }
      },
      WRAP_AROUND {
         @Override
         public MultifaceSpreader.SpreadPos getSpreadPos(BlockPos $$0, Direction $$1, Direction $$2) {
            return new MultifaceSpreader.SpreadPos($$0.relative($$1).relative($$2), $$1.getOpposite());
         }
      };

      public abstract MultifaceSpreader.SpreadPos getSpreadPos(BlockPos var1, Direction var2, Direction var3);
   }
}
