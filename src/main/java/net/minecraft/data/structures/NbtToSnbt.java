package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NbtToSnbt implements net.minecraft.data.DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Iterable<Path> inputFolders;
   private final net.minecraft.data.PackOutput output;

   public NbtToSnbt(net.minecraft.data.PackOutput $$0, Collection<Path> $$1) {
      this.inputFolders = $$1;
      this.output = $$0;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      Path $$1 = this.output.getOutputFolder();
      List<CompletableFuture<?>> $$2 = new ArrayList<>();

      for (Path $$3 : this.inputFolders) {
         $$2.add(
            CompletableFuture.<CompletableFuture>supplyAsync(
                  () -> {
                     try {
                        CompletableFuture var4;
                        try (Stream<Path> $$3x = Files.walk($$3)) {
                           var4 = CompletableFuture.allOf(
                              $$3x.filter($$0xx -> $$0xx.toString().endsWith(".nbt"))
                                 .map($$3xx -> CompletableFuture.runAsync(() -> convertStructure($$0, $$3xx, getName($$3, $$3xx), $$1), Util.ioPool()))
                                 .toArray(CompletableFuture[]::new)
                           );
                        }

                        return var4;
                     } catch (IOException var8) {
                        LOGGER.error("Failed to read structure input directory", var8);
                        return CompletableFuture.completedFuture(null);
                     }
                  },
                  Util.backgroundExecutor().forName("NbtToSnbt")
               )
               .thenCompose($$0x -> $$0x)
         );
      }

      return CompletableFuture.allOf($$2.toArray(CompletableFuture[]::new));
   }

   @Override
   public final String getName() {
      return "NBT -> SNBT";
   }

   private static String getName(Path $$0, Path $$1) {
      String $$2 = $$0.relativize($$1).toString().replaceAll("\\\\", "/");
      return $$2.substring(0, $$2.length() - ".nbt".length());
   }

   @Nullable
   public static Path convertStructure(net.minecraft.data.CachedOutput $$0, Path $$1, String $$2, Path $$3) {
      try {
         Path var7;
         try (
            InputStream $$4 = Files.newInputStream($$1);
            InputStream $$5 = new FastBufferedInputStream($$4);
         ) {
            Path $$6 = $$3.resolve($$2 + ".snbt");
            writeSnbt($$0, $$6, NbtUtils.structureToSnbt(NbtIo.readCompressed($$5, NbtAccounter.unlimitedHeap())));
            LOGGER.info("Converted {} from NBT to SNBT", $$2);
            var7 = $$6;
         }

         return var7;
      } catch (IOException var12) {
         LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", new Object[]{$$2, $$1, var12});
         return null;
      }
   }

   public static void writeSnbt(net.minecraft.data.CachedOutput $$0, Path $$1, String $$2) throws IOException {
      ByteArrayOutputStream $$3 = new ByteArrayOutputStream();
      HashingOutputStream $$4 = new HashingOutputStream(Hashing.sha1(), $$3);
      $$4.write($$2.getBytes(StandardCharsets.UTF_8));
      $$4.write(10);
      $$0.writeIfNeeded($$1, $$3.toByteArray(), $$4.hash());
   }
}
