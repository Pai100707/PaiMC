package net.minecraft.world.level.chunk.storage;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import org.jspecify.annotations.Nullable;

public class SimpleRegionStorage implements AutoCloseable {
   private final IOWorker worker;
   private final DataFixer fixerUpper;
   private final DataFixTypes dataFixType;
   private final Supplier<LegacyTagFixer> legacyFixer;

   public SimpleRegionStorage(RegionStorageInfo $$0, Path $$1, DataFixer $$2, boolean $$3, DataFixTypes $$4) {
      this($$0, $$1, $$2, $$3, $$4, LegacyTagFixer.EMPTY);
   }

   public SimpleRegionStorage(RegionStorageInfo $$0, Path $$1, DataFixer $$2, boolean $$3, DataFixTypes $$4, Supplier<LegacyTagFixer> $$5) {
      this.fixerUpper = $$2;
      this.dataFixType = $$4;
      this.worker = new IOWorker($$0, $$1, $$3);
      this.legacyFixer = Suppliers.memoize($$5::get);
   }

   public boolean isOldChunkAround(net.minecraft.world.level.ChunkPos $$0, int $$1) {
      return this.worker.isOldChunkAround($$0, $$1);
   }

   public CompletableFuture<Optional<CompoundTag>> read(net.minecraft.world.level.ChunkPos $$0) {
      return this.worker.loadAsync($$0);
   }

   public CompletableFuture<Void> write(net.minecraft.world.level.ChunkPos $$0, CompoundTag $$1) {
      return this.write($$0, (Supplier<CompoundTag>)(() -> $$1));
   }

   public CompletableFuture<Void> write(net.minecraft.world.level.ChunkPos $$0, Supplier<CompoundTag> $$1) {
      this.markChunkDone($$0);
      return this.worker.store($$0, $$1);
   }

   public CompoundTag upgradeChunkTag(CompoundTag $$0, int $$1, @Nullable CompoundTag $$2) {
      int $$3 = NbtUtils.getDataVersion($$0, $$1);
      if ($$3 == SharedConstants.getCurrentVersion().dataVersion().version()) {
         return $$0;
      } else {
         try {
            $$0 = this.legacyFixer.get().applyFix($$0);
            injectDatafixingContext($$0, $$2);
            $$0 = this.dataFixType.updateToCurrentVersion(this.fixerUpper, $$0, Math.max(this.legacyFixer.get().targetDataVersion(), $$3));
            removeDatafixingContext($$0);
            NbtUtils.addCurrentDataVersion($$0);
            return $$0;
         } catch (Exception var8) {
            CrashReport $$5 = CrashReport.forThrowable(var8, "Updated chunk");
            CrashReportCategory $$6 = $$5.addCategory("Updated chunk details");
            $$6.setDetail("Data version", $$3);
            throw new ReportedException($$5);
         }
      }
   }

   public CompoundTag upgradeChunkTag(CompoundTag $$0, int $$1) {
      return this.upgradeChunkTag($$0, $$1, null);
   }

   public Dynamic<Tag> upgradeChunkTag(Dynamic<Tag> $$0, int $$1) {
      return new Dynamic($$0.getOps(), this.upgradeChunkTag((CompoundTag)$$0.getValue(), $$1, null));
   }

   public static void injectDatafixingContext(CompoundTag $$0, @Nullable CompoundTag $$1) {
      if ($$1 != null) {
         $$0.put("__context", $$1);
      }
   }

   private static void removeDatafixingContext(CompoundTag $$0) {
      $$0.remove("__context");
   }

   protected void markChunkDone(net.minecraft.world.level.ChunkPos $$0) {
      this.legacyFixer.get().markChunkDone($$0);
   }

   public CompletableFuture<Void> synchronize(boolean $$0) {
      return this.worker.synchronize($$0);
   }

   @Override
   public void close() throws IOException {
      this.worker.close();
   }

   public ChunkScanAccess chunkScanner() {
      return this.worker;
   }

   public RegionStorageInfo storageInfo() {
      return this.worker.storageInfo();
   }
}
