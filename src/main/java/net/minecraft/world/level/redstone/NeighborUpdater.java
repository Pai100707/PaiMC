package net.minecraft.world.level.redstone;

import java.util.Locale;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public interface NeighborUpdater {
   Direction[] UPDATE_ORDER = new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH};

   void shapeUpdate(Direction var1, BlockState var2, BlockPos var3, BlockPos var4, @Block.UpdateFlags int var5, int var6);

   void neighborChanged(BlockPos var1, Block var2, @Nullable Orientation var3);

   void neighborChanged(BlockState var1, BlockPos var2, Block var3, @Nullable Orientation var4, boolean var5);

   default void updateNeighborsAtExceptFromFacing(BlockPos $$0, Block $$1, @Nullable Direction $$2, @Nullable Orientation $$3) {
      for (Direction $$4 : UPDATE_ORDER) {
         if ($$4 != $$2) {
            this.neighborChanged($$0.relative($$4), $$1, null);
         }
      }
   }

   static void executeShapeUpdate(
      net.minecraft.world.level.LevelAccessor $$0, Direction $$1, BlockPos $$2, BlockPos $$3, BlockState $$4, @Block.UpdateFlags int $$5, int $$6
   ) {
      BlockState $$7 = $$0.getBlockState($$2);
      if (($$5 & 128) == 0 || !$$7.is(Blocks.REDSTONE_WIRE)) {
         BlockState $$8 = $$7.updateShape($$0, $$0, $$2, $$1, $$3, $$4, $$0.getRandom());
         Block.updateOrDestroy($$7, $$8, $$0, $$2, $$5, $$6);
      }
   }

   static void executeUpdate(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      try {
         $$1.handleNeighborChanged($$0, $$2, $$3, $$4, $$5);
      } catch (Throwable var9) {
         CrashReport $$7 = CrashReport.forThrowable(var9, "Exception while updating neighbours");
         CrashReportCategory $$8 = $$7.addCategory("Block being updated");
         $$8.setDetail(
            "Source block type",
            () -> {
               try {
                  return String.format(
                     Locale.ROOT, "ID #%s (%s // %s)", BuiltInRegistries.BLOCK.getKey($$3), $$3.getDescriptionId(), $$3.getClass().getCanonicalName()
                  );
               } catch (Throwable var2x) {
                  return "ID #" + BuiltInRegistries.BLOCK.getKey($$3);
               }
            }
         );
         CrashReportCategory.populateBlockDetails($$8, $$0, $$2, $$1);
         throw new ReportedException($$7);
      }
   }
}
