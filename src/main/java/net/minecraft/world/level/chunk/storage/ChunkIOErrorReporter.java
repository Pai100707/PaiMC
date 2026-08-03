package net.minecraft.world.level.chunk.storage;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;

public interface ChunkIOErrorReporter {
   void reportChunkLoadFailure(Throwable var1, RegionStorageInfo var2, net.minecraft.world.level.ChunkPos var3);

   void reportChunkSaveFailure(Throwable var1, RegionStorageInfo var2, net.minecraft.world.level.ChunkPos var3);

   static ReportedException createMisplacedChunkReport(net.minecraft.world.level.ChunkPos $$0, net.minecraft.world.level.ChunkPos $$1) {
      CrashReport $$2 = CrashReport.forThrowable(
         new IllegalStateException("Retrieved chunk position " + $$0 + " does not match requested " + $$1), "Chunk found in invalid location"
      );
      CrashReportCategory $$3 = $$2.addCategory("Misplaced Chunk");
      $$3.setDetail("Stored Position", $$0::toString);
      return new ReportedException($$2);
   }

   default void reportMisplacedChunk(net.minecraft.world.level.ChunkPos $$0, net.minecraft.world.level.ChunkPos $$1, RegionStorageInfo $$2) {
      this.reportChunkLoadFailure(createMisplacedChunkReport($$0, $$1), $$2, $$1);
   }
}
