package net.minecraft.world.level.storage;

import com.google.common.collect.Iterables;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.FastBufferedInputStream;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DimensionDataStorage implements AutoCloseable {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Map<SavedDataType<?>, Optional<SavedData>> cache = new HashMap<>();
   private final DataFixer fixerUpper;
   private final Provider registries;
   private final Path dataFolder;
   private CompletableFuture<?> pendingWriteFuture = CompletableFuture.completedFuture(null);

   public DimensionDataStorage(Path $$0, DataFixer $$1, Provider $$2) {
      this.fixerUpper = $$1;
      this.dataFolder = $$0;
      this.registries = $$2;
   }

   private Path getDataFile(String $$0) {
      return this.dataFolder.resolve($$0 + ".dat");
   }

   public <T extends SavedData> T computeIfAbsent(SavedDataType<T> $$0) {
      T $$1 = this.get($$0);
      if ($$1 != null) {
         return $$1;
      } else {
         T $$2 = (T)$$0.constructor().get();
         this.set($$0, $$2);
         return $$2;
      }
   }

   @Nullable
   public <T extends SavedData> T get(SavedDataType<T> $$0) {
      Optional<SavedData> $$1 = this.cache.get($$0);
      if ($$1 == null) {
         $$1 = Optional.ofNullable(this.readSavedData($$0));
         this.cache.put($$0, $$1);
      }

      return (T)$$1.orElse(null);
   }

   @Nullable
   private <T extends SavedData> T readSavedData(SavedDataType<T> $$0) {
      try {
         Path $$1 = this.getDataFile($$0.id());
         if (Files.exists($$1)) {
            CompoundTag $$2 = this.readTagFromDisk($$0.id(), $$0.dataFixType(), SharedConstants.getCurrentVersion().dataVersion().version());
            RegistryOps<Tag> $$3 = this.registries.createSerializationContext(NbtOps.INSTANCE);
            return (T)$$0.codec()
               .parse($$3, $$2.get("data"))
               .resultOrPartial($$1x -> LOGGER.error("Failed to parse saved data for '{}': {}", $$0, $$1x))
               .orElse(null);
         }
      } catch (Exception var5) {
         LOGGER.error("Error loading saved data: {}", $$0, var5);
      }

      return null;
   }

   public <T extends SavedData> void set(SavedDataType<T> $$0, T $$1) {
      this.cache.put($$0, Optional.of($$1));
      $$1.setDirty();
   }

   public CompoundTag readTagFromDisk(String $$0, DataFixTypes $$1, int $$2) throws IOException {
      CompoundTag var8;
      try (
         InputStream $$3 = Files.newInputStream(this.getDataFile($$0));
         PushbackInputStream $$4 = new PushbackInputStream(new FastBufferedInputStream($$3), 2);
      ) {
         CompoundTag $$5;
         if (this.isGzip($$4)) {
            $$5 = NbtIo.readCompressed($$4, NbtAccounter.unlimitedHeap());
         } else {
            try (DataInputStream $$6 = new DataInputStream($$4)) {
               $$5 = NbtIo.read($$6);
            }
         }

         int $$9 = NbtUtils.getDataVersion($$5, 1343);
         var8 = $$1.update(this.fixerUpper, $$5, $$9, $$2);
      }

      return var8;
   }

   private boolean isGzip(PushbackInputStream $$0) throws IOException {
      byte[] $$1 = new byte[2];
      boolean $$2 = false;
      int $$3 = $$0.read($$1, 0, 2);
      if ($$3 == 2) {
         int $$4 = ($$1[1] & 255) << 8 | $$1[0] & 255;
         if ($$4 == 35615) {
            $$2 = true;
         }
      }

      if ($$3 != 0) {
         $$0.unread($$1, 0, $$3);
      }

      return $$2;
   }

   public CompletableFuture<?> scheduleSave() {
      Map<SavedDataType<?>, CompoundTag> $$0 = this.collectDirtyTagsToSave();
      if ($$0.isEmpty()) {
         return CompletableFuture.completedFuture(null);
      } else {
         int $$1 = Util.maxAllowedExecutorThreads();
         int $$2 = $$0.size();
         if ($$2 > $$1) {
            this.pendingWriteFuture = this.pendingWriteFuture.thenCompose($$3 -> {
               List<CompletableFuture<?>> $$4 = new ArrayList<>($$1);
               int $$5 = Mth.positiveCeilDiv($$2, $$1);

               for (List<Entry<SavedDataType<?>, CompoundTag>> $$6 : Iterables.partition($$0.entrySet(), $$5)) {
                  $$4.add(CompletableFuture.runAsync(() -> {
                     for (Entry<SavedDataType<?>, CompoundTag> $$1xx : $$6) {
                        this.tryWrite($$1xx.getKey(), $$1xx.getValue());
                     }
                  }, Util.ioPool()));
               }

               return CompletableFuture.allOf($$4.toArray(CompletableFuture[]::new));
            });
         } else {
            this.pendingWriteFuture = this.pendingWriteFuture
               .thenCompose(
                  $$1x -> CompletableFuture.allOf(
                     $$0.entrySet()
                        .stream()
                        .map(
                           $$0xx -> CompletableFuture.runAsync(
                              () -> this.tryWrite((SavedDataType<?>)$$0xx.getKey(), (CompoundTag)$$0xx.getValue()), Util.ioPool()
                           )
                        )
                        .toArray(CompletableFuture[]::new)
                  )
               );
         }

         return this.pendingWriteFuture;
      }
   }

   private Map<SavedDataType<?>, CompoundTag> collectDirtyTagsToSave() {
      Map<SavedDataType<?>, CompoundTag> $$0 = new Object2ObjectArrayMap();
      RegistryOps<Tag> $$1 = this.registries.createSerializationContext(NbtOps.INSTANCE);
      this.cache.forEach(($$2, $$3) -> $$3.filter(SavedData::isDirty).ifPresent($$3x -> {
         $$0.put($$2, this.encodeUnchecked($$2, $$3x, $$1));
         $$3x.setDirty(false);
      }));
      return $$0;
   }

   private <T extends SavedData> CompoundTag encodeUnchecked(SavedDataType<T> $$0, SavedData $$1, RegistryOps<Tag> $$2) {
      Codec<T> $$3 = $$0.codec();
      CompoundTag $$4 = new CompoundTag();
      $$4.put("data", (Tag)$$3.encodeStart($$2, $$1).getOrThrow());
      NbtUtils.addCurrentDataVersion($$4);
      return $$4;
   }

   private void tryWrite(SavedDataType<?> $$0, CompoundTag $$1) {
      Path $$2 = this.getDataFile($$0.id());

      try {
         NbtIo.writeCompressed($$1, $$2);
      } catch (IOException var5) {
         LOGGER.error("Could not save data to {}", $$2.getFileName(), var5);
      }
   }

   public void saveAndJoin() {
      this.scheduleSave().join();
   }

   @Override
   public void close() {
      this.saveAndJoin();
   }
}
