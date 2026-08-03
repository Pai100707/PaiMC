package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SectionStorage<R, P> implements AutoCloseable {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final String SECTIONS_TAG = "Sections";
   private final SimpleRegionStorage simpleRegionStorage;
   private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap();
   private final LongLinkedOpenHashSet dirtyChunks = new LongLinkedOpenHashSet();
   private final Codec<P> codec;
   private final Function<R, P> packer;
   private final BiFunction<P, Runnable, R> unpacker;
   private final Function<Runnable, R> factory;
   private final RegistryAccess registryAccess;
   private final ChunkIOErrorReporter errorReporter;
   protected final net.minecraft.world.level.LevelHeightAccessor levelHeightAccessor;
   private final LongSet loadedChunks = new LongOpenHashSet();
   private final Long2ObjectMap<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> pendingLoads = new Long2ObjectOpenHashMap();
   private final Object loadLock = new Object();

   public SectionStorage(
      SimpleRegionStorage $$0,
      Codec<P> $$1,
      Function<R, P> $$2,
      BiFunction<P, Runnable, R> $$3,
      Function<Runnable, R> $$4,
      RegistryAccess $$5,
      ChunkIOErrorReporter $$6,
      net.minecraft.world.level.LevelHeightAccessor $$7
   ) {
      this.simpleRegionStorage = $$0;
      this.codec = $$1;
      this.packer = $$2;
      this.unpacker = $$3;
      this.factory = $$4;
      this.registryAccess = $$5;
      this.errorReporter = $$6;
      this.levelHeightAccessor = $$7;
   }

   protected void tick(BooleanSupplier $$0) {
      LongIterator $$1 = this.dirtyChunks.iterator();

      while ($$1.hasNext() && $$0.getAsBoolean()) {
         net.minecraft.world.level.ChunkPos $$2 = new net.minecraft.world.level.ChunkPos($$1.nextLong());
         $$1.remove();
         this.writeChunk($$2);
      }

      this.unpackPendingLoads();
   }

   private void unpackPendingLoads() {
      synchronized (this.loadLock) {
         Iterator<Entry<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>>> $$0 = Long2ObjectMaps.fastIterator(this.pendingLoads);

         while ($$0.hasNext()) {
            Entry<CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>> $$1 = $$0.next();
            Optional<SectionStorage.PackedChunk<P>> $$2 = (Optional<SectionStorage.PackedChunk<P>>)((CompletableFuture)$$1.getValue()).getNow(null);
            if ($$2 != null) {
               long $$3 = $$1.getLongKey();
               this.unpackChunk(new net.minecraft.world.level.ChunkPos($$3), $$2.orElse(null));
               $$0.remove();
               this.loadedChunks.add($$3);
            }
         }
      }
   }

   public void flushAll() {
      if (!this.dirtyChunks.isEmpty()) {
         this.dirtyChunks.forEach($$0 -> this.writeChunk(new net.minecraft.world.level.ChunkPos($$0)));
         this.dirtyChunks.clear();
      }
   }

   public boolean hasWork() {
      return !this.dirtyChunks.isEmpty();
   }

   @Nullable
   protected Optional<R> get(long $$0) {
      return (Optional<R>)this.storage.get($$0);
   }

   protected Optional<R> getOrLoad(long $$0) {
      if (this.outsideStoredRange($$0)) {
         return Optional.empty();
      } else {
         Optional<R> $$1 = this.get($$0);
         if ($$1 != null) {
            return $$1;
         } else {
            this.unpackChunk(SectionPos.of($$0).chunk());
            $$1 = this.get($$0);
            if ($$1 == null) {
               throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
            } else {
               return $$1;
            }
         }
      }
   }

   protected boolean outsideStoredRange(long $$0) {
      int $$1 = SectionPos.sectionToBlockCoord(SectionPos.y($$0));
      return this.levelHeightAccessor.isOutsideBuildHeight($$1);
   }

   protected R getOrCreate(long $$0) {
      if (this.outsideStoredRange($$0)) {
         throw (IllegalArgumentException)Util.pauseInIde(new IllegalArgumentException("sectionPos out of bounds"));
      } else {
         Optional<R> $$1 = this.getOrLoad($$0);
         if ($$1.isPresent()) {
            return $$1.get();
         } else {
            R $$2 = this.factory.apply(() -> this.setDirty($$0));
            this.storage.put($$0, Optional.of($$2));
            return $$2;
         }
      }
   }

   public CompletableFuture<?> prefetch(net.minecraft.world.level.ChunkPos $$0) {
      synchronized (this.loadLock) {
         long $$1 = $$0.toLong();
         return this.loadedChunks.contains($$1)
            ? CompletableFuture.completedFuture(null)
            : (CompletableFuture)this.pendingLoads.computeIfAbsent($$1, $$1x -> this.tryRead($$0));
      }
   }

   private void unpackChunk(net.minecraft.world.level.ChunkPos $$0) {
      long $$1 = $$0.toLong();
      CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> $$2;
      synchronized (this.loadLock) {
         if (!this.loadedChunks.add($$1)) {
            return;
         }

         $$2 = (CompletableFuture<Optional<SectionStorage.PackedChunk<P>>>)this.pendingLoads.computeIfAbsent($$1, $$1x -> this.tryRead($$0));
      }

      this.unpackChunk($$0, $$2.join().orElse(null));
      synchronized (this.loadLock) {
         this.pendingLoads.remove($$1);
      }
   }

   private CompletableFuture<Optional<SectionStorage.PackedChunk<P>>> tryRead(net.minecraft.world.level.ChunkPos $$0) {
      RegistryOps<Tag> $$1 = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
      return this.simpleRegionStorage
         .read($$0)
         .thenApplyAsync(
            $$1x -> $$1x.map($$1xx -> SectionStorage.PackedChunk.parse(this.codec, $$1, $$1xx, this.simpleRegionStorage, this.levelHeightAccessor)),
            Util.backgroundExecutor().forName("parseSection")
         )
         .exceptionally($$1x -> {
            if ($$1x instanceof CompletionException) {
               $$1x = $$1x.getCause();
            }

            if ($$1x instanceof IOException $$2) {
               LOGGER.error("Error reading chunk {} data from disk", $$0, $$2);
               this.errorReporter.reportChunkLoadFailure($$2, this.simpleRegionStorage.storageInfo(), $$0);
               return Optional.empty();
            } else {
               throw new CompletionException($$1x);
            }
         });
   }

   private void unpackChunk(net.minecraft.world.level.ChunkPos $$0, @Nullable SectionStorage.PackedChunk<P> $$1) {
      if ($$1 == null) {
         for (int $$2 = this.levelHeightAccessor.getMinSectionY(); $$2 <= this.levelHeightAccessor.getMaxSectionY(); $$2++) {
            this.storage.put(getKey($$0, $$2), Optional.empty());
         }
      } else {
         boolean $$3 = $$1.versionChanged();

         for (int $$4 = this.levelHeightAccessor.getMinSectionY(); $$4 <= this.levelHeightAccessor.getMaxSectionY(); $$4++) {
            long $$5 = getKey($$0, $$4);
            Optional<R> $$6 = Optional.ofNullable($$1.sectionsByY.get($$4)).map($$1x -> this.unpacker.apply((P)$$1x, () -> this.setDirty($$5)));
            this.storage.put($$5, $$6);
            $$6.ifPresent($$2 -> {
               this.onSectionLoad($$5);
               if ($$3) {
                  this.setDirty($$5);
               }
            });
         }
      }
   }

   private void writeChunk(net.minecraft.world.level.ChunkPos $$0) {
      RegistryOps<Tag> $$1 = this.registryAccess.createSerializationContext(NbtOps.INSTANCE);
      Dynamic<Tag> $$2 = this.writeChunk($$0, $$1);
      Tag $$3 = (Tag)$$2.getValue();
      if ($$3 instanceof CompoundTag $$4) {
         this.simpleRegionStorage.write($$0, $$4).exceptionally($$1x -> {
            this.errorReporter.reportChunkSaveFailure($$1x, this.simpleRegionStorage.storageInfo(), $$0);
            return null;
         });
      } else {
         LOGGER.error("Expected compound tag, got {}", $$3);
      }
   }

   private <T> Dynamic<T> writeChunk(net.minecraft.world.level.ChunkPos $$0, DynamicOps<T> $$1) {
      Map<T, T> $$2 = Maps.newHashMap();

      for (int $$3 = this.levelHeightAccessor.getMinSectionY(); $$3 <= this.levelHeightAccessor.getMaxSectionY(); $$3++) {
         long $$4 = getKey($$0, $$3);
         Optional<R> $$5 = (Optional<R>)this.storage.get($$4);
         if ($$5 != null && !$$5.isEmpty()) {
            DataResult<T> $$6 = this.codec.encodeStart($$1, this.packer.apply($$5.get()));
            String $$7 = Integer.toString($$3);
            $$6.resultOrPartial(LOGGER::error).ifPresent($$3x -> $$2.put((T)$$1.createString($$7), (T)$$3x));
         }
      }

      return new Dynamic(
         $$1,
         $$1.createMap(
            ImmutableMap.of(
               $$1.createString("Sections"),
               $$1.createMap($$2),
               $$1.createString("DataVersion"),
               $$1.createInt(SharedConstants.getCurrentVersion().dataVersion().version())
            )
         )
      );
   }

   private static long getKey(net.minecraft.world.level.ChunkPos $$0, int $$1) {
      return SectionPos.asLong($$0.x, $$1, $$0.z);
   }

   protected void onSectionLoad(long $$0) {
   }

   protected void setDirty(long $$0) {
      Optional<R> $$1 = (Optional<R>)this.storage.get($$0);
      if ($$1 != null && !$$1.isEmpty()) {
         this.dirtyChunks.add(net.minecraft.world.level.ChunkPos.asLong(SectionPos.x($$0), SectionPos.z($$0)));
      } else {
         LOGGER.warn("No data for position: {}", SectionPos.of($$0));
      }
   }

   public void flush(net.minecraft.world.level.ChunkPos $$0) {
      if (this.dirtyChunks.remove($$0.toLong())) {
         this.writeChunk($$0);
      }
   }

   @Override
   public void close() throws IOException {
      this.simpleRegionStorage.close();
   }

   record PackedChunk<T>(Int2ObjectMap<T> sectionsByY, boolean versionChanged) {

      public static <T> SectionStorage.PackedChunk<T> parse(
         Codec<T> $$0, DynamicOps<Tag> $$1, Tag $$2, SimpleRegionStorage $$3, net.minecraft.world.level.LevelHeightAccessor $$4
      ) {
         Dynamic<Tag> $$5 = new Dynamic($$1, $$2);
         Dynamic<Tag> $$6 = $$3.upgradeChunkTag($$5, 1945);
         boolean $$7 = $$5 != $$6;
         OptionalDynamic<Tag> $$8 = $$6.get("Sections");
         Int2ObjectMap<T> $$9 = new Int2ObjectOpenHashMap();

         for (int $$10 = $$4.getMinSectionY(); $$10 <= $$4.getMaxSectionY(); $$10++) {
            Optional<T> $$11 = $$8.get(Integer.toString($$10)).result().flatMap($$1x -> $$0.parse($$1x).resultOrPartial(SectionStorage.LOGGER::error));
            if ($$11.isPresent()) {
               $$9.put($$10, $$11.get());
            }
         }

         return new SectionStorage.PackedChunk<>($$9, $$7);
      }
   }
}
