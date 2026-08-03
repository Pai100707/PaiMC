package net.minecraft.world.level.chunk.storage;

import com.mojang.datafixers.DataFixer;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.datafix.DataFixTypes;
import org.apache.commons.io.FileUtils;

public class RecreatingSimpleRegionStorage extends SimpleRegionStorage {
   private final IOWorker writeWorker;
   private final Path writeFolder;

   public RecreatingSimpleRegionStorage(
      RegionStorageInfo $$0, Path $$1, RegionStorageInfo $$2, Path $$3, DataFixer $$4, boolean $$5, DataFixTypes $$6, Supplier<LegacyTagFixer> $$7
   ) {
      super($$0, $$1, $$4, $$5, $$6, $$7);
      this.writeFolder = $$3;
      this.writeWorker = new IOWorker($$2, $$3, $$5);
   }

   @Override
   public CompletableFuture<Void> write(net.minecraft.world.level.ChunkPos $$0, Supplier<CompoundTag> $$1) {
      this.markChunkDone($$0);
      return this.writeWorker.store($$0, $$1);
   }

   @Override
   public void close() throws IOException {
      super.close();
      this.writeWorker.close();
      if (this.writeFolder.toFile().exists()) {
         FileUtils.deleteDirectory(this.writeFolder.toFile());
      }
   }
}
