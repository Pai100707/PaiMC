package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.Util;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class SnbtToNbt implements net.minecraft.data.DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final net.minecraft.data.PackOutput output;
   private final Iterable<Path> inputFolders;
   private final List<SnbtToNbt.Filter> filters = Lists.newArrayList();

   public SnbtToNbt(net.minecraft.data.PackOutput $$0, Iterable<Path> $$1) {
      this.output = $$0;
      this.inputFolders = $$1;
   }

   public SnbtToNbt addFilter(SnbtToNbt.Filter $$0) {
      this.filters.add($$0);
      return this;
   }

   private CompoundTag applyFilters(String $$0, CompoundTag $$1) {
      CompoundTag $$2 = $$1;

      for (SnbtToNbt.Filter $$3 : this.filters) {
         $$2 = $$3.apply($$0, $$2);
      }

      return $$2;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      Path $$1 = this.output.getOutputFolder();
      List<CompletableFuture<?>> $$2 = Lists.newArrayList();

      for (Path $$3 : this.inputFolders) {
         $$2.add(CompletableFuture.<CompletableFuture>supplyAsync(() -> {
            try {
               CompletableFuture var5x;
               try (Stream<Path> $$3x = Files.walk($$3)) {
                  var5x = CompletableFuture.allOf($$3x.filter($$0xx -> $$0xx.toString().endsWith(".snbt")).map($$3xx -> CompletableFuture.runAsync(() -> {
                     SnbtToNbt.TaskResult $$4 = this.readStructure($$3xx, this.getName($$3, $$3xx));
                     this.storeStructureIfChanged($$0, $$4, $$1);
                  }, Util.backgroundExecutor().forName("SnbtToNbt"))).toArray(CompletableFuture[]::new));
               }

               return var5x;
            } catch (Exception var9) {
               throw new RuntimeException("Failed to read structure input directory, aborting", var9);
            }
         }, Util.backgroundExecutor().forName("SnbtToNbt")).thenCompose($$0x -> $$0x));
      }

      return Util.sequenceFailFast($$2);
   }

   @Override
   public final String getName() {
      return "SNBT -> NBT";
   }

   private String getName(Path $$0, Path $$1) {
      String $$2 = $$0.relativize($$1).toString().replaceAll("\\\\", "/");
      return $$2.substring(0, $$2.length() - ".snbt".length());
   }

   private SnbtToNbt.TaskResult readStructure(Path $$0, String $$1) {
      try {
         SnbtToNbt.TaskResult var10;
         try (BufferedReader $$2 = Files.newBufferedReader($$0)) {
            String $$3 = IOUtils.toString($$2);
            CompoundTag $$4 = this.applyFilters($$1, NbtUtils.snbtToStructure($$3));
            ByteArrayOutputStream $$5 = new ByteArrayOutputStream();
            HashingOutputStream $$6 = new HashingOutputStream(Hashing.sha1(), $$5);
            NbtIo.writeCompressed($$4, $$6);
            byte[] $$7 = $$5.toByteArray();
            HashCode $$8 = $$6.hash();
            var10 = new SnbtToNbt.TaskResult($$1, $$7, $$8);
         }

         return var10;
      } catch (Throwable var13) {
         throw new SnbtToNbt.StructureConversionException($$0, var13);
      }
   }

   private void storeStructureIfChanged(net.minecraft.data.CachedOutput $$0, SnbtToNbt.TaskResult $$1, Path $$2) {
      Path $$3 = $$2.resolve($$1.name + ".nbt");

      try {
         $$0.writeIfNeeded($$3, $$1.payload, $$1.hash);
      } catch (IOException var6) {
         LOGGER.error("Couldn't write structure {} at {}", new Object[]{$$1.name, $$3, var6});
      }
   }

   @FunctionalInterface
   public interface Filter {
      CompoundTag apply(String var1, CompoundTag var2);
   }

   static class StructureConversionException extends RuntimeException {
      public StructureConversionException(Path $$0, Throwable $$1) {
         super($$0.toAbsolutePath().toString(), $$1);
      }
   }

   record TaskResult(String name, byte[] payload, HashCode hash) {
   }
}
