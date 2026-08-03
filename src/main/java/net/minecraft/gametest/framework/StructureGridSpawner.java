package net.minecraft.gametest.framework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public class StructureGridSpawner implements GameTestRunner.StructureSpawner {
   private static final int SPACE_BETWEEN_COLUMNS = 5;
   private static final int SPACE_BETWEEN_ROWS = 6;
   private final int testsPerRow;
   private int currentRowCount;
   private AABB rowBounds;
   private final MutableBlockPos nextTestNorthWestCorner;
   private final BlockPos firstTestNorthWestCorner;
   private final boolean clearOnBatch;
   private float maxX = -1.0F;
   private final Collection<GameTestInfo> testInLastBatch = new ArrayList<>();

   public StructureGridSpawner(BlockPos $$0, int $$1, boolean $$2) {
      this.testsPerRow = $$1;
      this.nextTestNorthWestCorner = $$0.mutable();
      this.rowBounds = new AABB(this.nextTestNorthWestCorner);
      this.firstTestNorthWestCorner = $$0;
      this.clearOnBatch = $$2;
   }

   @Override
   public void onBatchStart(ServerLevel $$0) {
      if (this.clearOnBatch) {
         this.testInLastBatch.forEach($$1 -> {
            BoundingBox $$2 = $$1.getTestInstanceBlockEntity().getStructureBoundingBox();
            StructureUtils.clearSpaceForStructure($$2, $$0);
         });
         this.testInLastBatch.clear();
         this.rowBounds = new AABB(this.firstTestNorthWestCorner);
         this.nextTestNorthWestCorner.set(this.firstTestNorthWestCorner);
      }
   }

   @Override
   public Optional<GameTestInfo> spawnStructure(GameTestInfo $$0) {
      BlockPos $$1 = new BlockPos(this.nextTestNorthWestCorner);
      $$0.setTestBlockPos($$1);
      GameTestInfo $$2 = $$0.prepareTestStructure();
      if ($$2 == null) {
         return Optional.empty();
      } else {
         $$2.startExecution(1);
         AABB $$3 = $$0.getTestInstanceBlockEntity().getStructureBounds();
         this.rowBounds = this.rowBounds.minmax($$3);
         this.nextTestNorthWestCorner.move((int)$$3.getXsize() + 5, 0, 0);
         if (this.nextTestNorthWestCorner.getX() > this.maxX) {
            this.maxX = this.nextTestNorthWestCorner.getX();
         }

         if (++this.currentRowCount >= this.testsPerRow) {
            this.currentRowCount = 0;
            this.nextTestNorthWestCorner.move(0, 0, (int)this.rowBounds.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            this.rowBounds = new AABB(this.nextTestNorthWestCorner);
         }

         this.testInLastBatch.add($$0);
         return Optional.of($$0);
      }
   }
}
