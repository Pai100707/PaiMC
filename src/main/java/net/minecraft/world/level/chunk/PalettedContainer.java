package net.minecraft.world.level.chunk;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.ThreadingDetector;
import net.minecraft.util.ZeroBitStorage;
import net.minecraft.util.SimpleBitStorage.InitializationException;

public class PalettedContainer<T> implements PaletteResize<T>, PalettedContainerRO<T> {
   private static final int MIN_PALETTE_BITS = 0;
   private volatile PalettedContainer.Data<T> data;
   private final Strategy<T> strategy;
   private final ThreadingDetector threadingDetector = new ThreadingDetector("PalettedContainer");

   public void acquire() {
      this.threadingDetector.checkAndLock();
   }

   public void release() {
      this.threadingDetector.checkAndUnlock();
   }

   public static <T> Codec<PalettedContainer<T>> codecRW(Codec<T> $$0, Strategy<T> $$1, T $$2) {
      PalettedContainerRO.Unpacker<T, PalettedContainer<T>> $$3 = PalettedContainer::unpack;
      return codec($$0, $$1, $$2, $$3);
   }

   public static <T> Codec<PalettedContainerRO<T>> codecRO(Codec<T> $$0, Strategy<T> $$1, T $$2) {
      PalettedContainerRO.Unpacker<T, PalettedContainerRO<T>> $$3 = ($$0x, $$1x) -> unpack($$0x, $$1x).map($$0xx -> $$0xx);
      return codec($$0, $$1, $$2, $$3);
   }

   private static <T, C extends PalettedContainerRO<T>> Codec<C> codec(Codec<T> $$0, Strategy<T> $$1, T $$2, PalettedContainerRO.Unpacker<T, C> $$3) {
      return RecordCodecBuilder.create(
            $$2x -> $$2x.group(
                  $$0.mapResult(ExtraCodecs.orElsePartial($$2)).listOf().fieldOf("palette").forGetter(PalettedContainerRO.PackedData::paletteEntries),
                  Codec.LONG_STREAM.lenientOptionalFieldOf("data").forGetter(PalettedContainerRO.PackedData::storage)
               )
               .apply($$2x, PalettedContainerRO.PackedData::new)
         )
         .comapFlatMap($$2x -> $$3.read($$1, $$2x), $$1x -> $$1x.pack($$1));
   }

   private PalettedContainer(Strategy<T> $$0, Configuration $$1, BitStorage $$2, Palette<T> $$3) {
      this.strategy = $$0;
      this.data = new PalettedContainer.Data<>($$1, $$2, $$3);
   }

   private PalettedContainer(PalettedContainer<T> $$0) {
      this.strategy = $$0.strategy;
      this.data = $$0.data.copy();
   }

   public PalettedContainer(T $$0, Strategy<T> $$1) {
      this.strategy = $$1;
      this.data = this.createOrReuseData(null, 0);
      this.data.palette.idFor($$0, this);
   }

   private PalettedContainer.Data<T> createOrReuseData(PalettedContainer.Data<T> $$0, int $$1) {
      Configuration $$2 = this.strategy.getConfigurationForBitCount($$1);
      if ($$0 != null && $$2.equals($$0.configuration())) {
         return $$0;
      } else {
         BitStorage $$3 = (BitStorage)($$2.bitsInMemory() == 0
            ? new ZeroBitStorage(this.strategy.entryCount())
            : new SimpleBitStorage($$2.bitsInMemory(), this.strategy.entryCount()));
         Palette<T> $$4 = $$2.createPalette(this.strategy, List.of());
         return new PalettedContainer.Data<>($$2, $$3, $$4);
      }
   }

   @Override
   public int onResize(int $$0, T $$1) {
      PalettedContainer.Data<T> $$2 = this.data;
      PalettedContainer.Data<T> $$3 = this.createOrReuseData($$2, $$0);
      $$3.copyFrom($$2.palette, $$2.storage);
      this.data = $$3;
      return $$3.palette.idFor($$1, PaletteResize.noResizeExpected());
   }

   public T getAndSet(int $$0, int $$1, int $$2, T $$3) {
      this.acquire();

      Object var5;
      try {
         var5 = this.getAndSet(this.strategy.getIndex($$0, $$1, $$2), $$3);
      } finally {
         this.release();
      }

      return (T)var5;
   }

   public T getAndSetUnchecked(int $$0, int $$1, int $$2, T $$3) {
      return this.getAndSet(this.strategy.getIndex($$0, $$1, $$2), $$3);
   }

   private T getAndSet(int $$0, T $$1) {
      int $$2 = this.data.palette.idFor($$1, this);
      int $$3 = this.data.storage.getAndSet($$0, $$2);
      return this.data.palette.valueFor($$3);
   }

   public void set(int $$0, int $$1, int $$2, T $$3) {
      this.acquire();

      try {
         this.set(this.strategy.getIndex($$0, $$1, $$2), $$3);
      } finally {
         this.release();
      }
   }

   private void set(int $$0, T $$1) {
      int $$2 = this.data.palette.idFor($$1, this);
      this.data.storage.set($$0, $$2);
   }

   @Override
   public T get(int $$0, int $$1, int $$2) {
      return this.get(this.strategy.getIndex($$0, $$1, $$2));
   }

   protected T get(int $$0) {
      PalettedContainer.Data<T> $$1 = this.data;
      return $$1.palette.valueFor($$1.storage.get($$0));
   }

   @Override
   public void getAll(Consumer<T> $$0) {
      Palette<T> $$1 = this.data.palette();
      IntSet $$2 = new IntArraySet();
      this.data.storage.getAll($$2::add);
      $$2.forEach($$2x -> $$0.accept($$1.valueFor($$2x)));
   }

   public void read(FriendlyByteBuf $$0) {
      this.acquire();

      try {
         int $$1 = $$0.readByte();
         PalettedContainer.Data<T> $$2 = this.createOrReuseData(this.data, $$1);
         $$2.palette.read($$0, this.strategy.globalMap());
         $$0.readFixedSizeLongArray($$2.storage.getRaw());
         this.data = $$2;
      } finally {
         this.release();
      }
   }

   @Override
   public void write(FriendlyByteBuf $$0) {
      this.acquire();

      try {
         this.data.write($$0, this.strategy.globalMap());
      } finally {
         this.release();
      }
   }

   @VisibleForTesting
   public static <T> DataResult<PalettedContainer<T>> unpack(Strategy<T> $$0, PalettedContainerRO.PackedData<T> $$1) {
      List<T> $$2 = $$1.paletteEntries();
      int $$3 = $$0.entryCount();
      Configuration $$4 = $$0.getConfigurationForPaletteSize($$2.size());
      int $$5 = $$4.bitsInStorage();
      if ($$1.bitsPerEntry() != -1 && $$5 != $$1.bitsPerEntry()) {
         return DataResult.error(() -> "Invalid bit count, calculated " + $$5 + ", but container declared " + $$1.bitsPerEntry());
      } else {
         BitStorage $$7;
         Palette<T> $$6;
         if ($$4.bitsInMemory() == 0) {
            $$6 = $$4.createPalette($$0, $$2);
            $$7 = new ZeroBitStorage($$3);
         } else {
            Optional<LongStream> $$8 = $$1.storage();
            if ($$8.isEmpty()) {
               return DataResult.error(() -> "Missing values for non-zero storage");
            }

            long[] $$9 = $$8.get().toArray();

            try {
               if (!$$4.alwaysRepack() && $$4.bitsInMemory() == $$5) {
                  $$6 = $$4.createPalette($$0, $$2);
                  $$7 = new SimpleBitStorage($$4.bitsInMemory(), $$3, $$9);
               } else {
                  Palette<T> $$10 = new HashMapPalette<>($$5, $$2);
                  SimpleBitStorage $$11 = new SimpleBitStorage($$5, $$3, $$9);
                  Palette<T> $$12 = $$4.createPalette($$0, $$2);
                  int[] $$13 = reencodeContents($$11, $$10, $$12);
                  $$6 = $$12;
                  $$7 = new SimpleBitStorage($$4.bitsInMemory(), $$3, $$13);
               }
            } catch (InitializationException var14) {
               return DataResult.error(() -> "Failed to read PalettedContainer: " + var14.getMessage());
            }
         }

         return DataResult.success(new PalettedContainer<>($$0, $$4, $$7, $$6));
      }
   }

   @Override
   public PalettedContainerRO.PackedData<T> pack(Strategy<T> $$0) {
      this.acquire();

      PalettedContainerRO.PackedData var14;
      try {
         BitStorage $$1 = this.data.storage;
         Palette<T> $$2 = this.data.palette;
         HashMapPalette<T> $$3 = new HashMapPalette<>($$1.getBits());
         int $$4 = $$0.entryCount();
         int[] $$5 = reencodeContents($$1, $$2, $$3);
         Configuration $$6 = $$0.getConfigurationForPaletteSize($$3.getSize());
         int $$7 = $$6.bitsInStorage();
         Optional<LongStream> $$9;
         if ($$7 != 0) {
            SimpleBitStorage $$8 = new SimpleBitStorage($$7, $$4, $$5);
            $$9 = Optional.of(Arrays.stream($$8.getRaw()));
         } else {
            $$9 = Optional.empty();
         }

         var14 = new PalettedContainerRO.PackedData<>($$3.getEntries(), $$9, $$7);
      } finally {
         this.release();
      }

      return var14;
   }

   private static <T> int[] reencodeContents(BitStorage $$0, Palette<T> $$1, Palette<T> $$2) {
      int[] $$3 = new int[$$0.getSize()];
      $$0.unpack($$3);
      PaletteResize<T> $$4 = PaletteResize.noResizeExpected();
      int $$5 = -1;
      int $$6 = -1;

      for (int $$7 = 0; $$7 < $$3.length; $$7++) {
         int $$8 = $$3[$$7];
         if ($$8 != $$5) {
            $$5 = $$8;
            $$6 = $$2.idFor($$1.valueFor($$8), $$4);
         }

         $$3[$$7] = $$6;
      }

      return $$3;
   }

   @Override
   public int getSerializedSize() {
      return this.data.getSerializedSize(this.strategy.globalMap());
   }

   @Override
   public int bitsPerEntry() {
      return this.data.storage().getBits();
   }

   @Override
   public boolean maybeHas(Predicate<T> $$0) {
      return this.data.palette.maybeHas($$0);
   }

   @Override
   public PalettedContainer<T> copy() {
      return new PalettedContainer<>(this);
   }

   @Override
   public PalettedContainer<T> recreate() {
      return new PalettedContainer<>(this.data.palette.valueFor(0), this.strategy);
   }

   @Override
   public void count(PalettedContainer.CountConsumer<T> $$0) {
      if (this.data.palette.getSize() == 1) {
         $$0.accept(this.data.palette.valueFor(0), this.data.storage.getSize());
      } else {
         Int2IntOpenHashMap $$1 = new Int2IntOpenHashMap();
         this.data.storage.getAll($$1x -> $$1.addTo($$1x, 1));
         $$1.int2IntEntrySet().forEach($$1x -> $$0.accept(this.data.palette.valueFor($$1x.getIntKey()), $$1x.getIntValue()));
      }
   }

   @FunctionalInterface
   public interface CountConsumer<T> {
      void accept(T var1, int var2);
   }

   record Data<T>(Configuration configuration, BitStorage storage, Palette<T> palette) {

      public void copyFrom(Palette<T> $$0, BitStorage $$1) {
         PaletteResize<T> $$2 = PaletteResize.noResizeExpected();

         for (int $$3 = 0; $$3 < $$1.getSize(); $$3++) {
            T $$4 = $$0.valueFor($$1.get($$3));
            this.storage.set($$3, this.palette.idFor($$4, $$2));
         }
      }

      public int getSerializedSize(IdMap<T> $$0) {
         return 1 + this.palette.getSerializedSize($$0) + this.storage.getRaw().length * 8;
      }

      public void write(FriendlyByteBuf $$0, IdMap<T> $$1) {
         $$0.writeByte(this.storage.getBits());
         this.palette.write($$0, $$1);
         $$0.writeFixedSizeLongArray(this.storage.getRaw());
      }

      public PalettedContainer.Data<T> copy() {
         return new PalettedContainer.Data<>(this.configuration, this.storage.copy(), this.palette.copy());
      }
   }
}
