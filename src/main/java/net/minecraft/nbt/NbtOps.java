package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtOps implements DynamicOps<net.minecraft.nbt.Tag> {
   public static final net.minecraft.nbt.NbtOps INSTANCE = new net.minecraft.nbt.NbtOps();

   private NbtOps() {
   }

   public net.minecraft.nbt.Tag empty() {
      return net.minecraft.nbt.EndTag.INSTANCE;
   }

   public net.minecraft.nbt.Tag emptyList() {
      return new net.minecraft.nbt.ListTag();
   }

   public net.minecraft.nbt.Tag emptyMap() {
      return new net.minecraft.nbt.CompoundTag();
   }

   public <U> U convertTo(DynamicOps<U> $$0, net.minecraft.nbt.Tag $$1) {
      return (U)(switch ($$1) {
         case net.minecraft.nbt.EndTag $$2 -> (Object)$$0.empty();
         case net.minecraft.nbt.ByteTag(byte var34) -> (Object)$$0.createByte(var34);
         case net.minecraft.nbt.ShortTag(short var35) -> (Object)$$0.createShort(var35);
         case net.minecraft.nbt.IntTag(int var36) -> (Object)$$0.createInt(var36);
         case net.minecraft.nbt.LongTag(long var37) -> (Object)$$0.createLong(var37);
         case net.minecraft.nbt.FloatTag(float var38) -> (Object)$$0.createFloat(var38);
         case net.minecraft.nbt.DoubleTag(double var39) -> (Object)$$0.createDouble(var39);
         case net.minecraft.nbt.ByteArrayTag $$9 -> (Object)$$0.createByteList(ByteBuffer.wrap($$9.getAsByteArray()));
         case net.minecraft.nbt.StringTag(String var40) -> (Object)$$0.createString(var40);
         case net.minecraft.nbt.ListTag $$11 -> (Object)this.convertList($$0, $$11);
         case net.minecraft.nbt.CompoundTag $$12 -> (Object)this.convertMap($$0, $$12);
         case net.minecraft.nbt.IntArrayTag $$13 -> (Object)$$0.createIntList(Arrays.stream($$13.getAsIntArray()));
         case net.minecraft.nbt.LongArrayTag $$14 -> (Object)$$0.createLongList(Arrays.stream($$14.getAsLongArray()));
         default -> throw new MatchException(null, null);
      });
   }

   public DataResult<Number> getNumberValue(net.minecraft.nbt.Tag $$0) {
      return $$0.asNumber().<DataResult<Number>>map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
   }

   public net.minecraft.nbt.Tag createNumeric(Number $$0) {
      return net.minecraft.nbt.DoubleTag.valueOf($$0.doubleValue());
   }

   public net.minecraft.nbt.Tag createByte(byte $$0) {
      return net.minecraft.nbt.ByteTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createShort(short $$0) {
      return net.minecraft.nbt.ShortTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createInt(int $$0) {
      return net.minecraft.nbt.IntTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createLong(long $$0) {
      return net.minecraft.nbt.LongTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createFloat(float $$0) {
      return net.minecraft.nbt.FloatTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createDouble(double $$0) {
      return net.minecraft.nbt.DoubleTag.valueOf($$0);
   }

   public net.minecraft.nbt.Tag createBoolean(boolean $$0) {
      return net.minecraft.nbt.ByteTag.valueOf($$0);
   }

   public DataResult<String> getStringValue(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.StringTag(String var4) ? DataResult.success(var4) : DataResult.error(() -> "Not a string");
   }

   public net.minecraft.nbt.Tag createString(String $$0) {
      return net.minecraft.nbt.StringTag.valueOf($$0);
   }

   public DataResult<net.minecraft.nbt.Tag> mergeToList(net.minecraft.nbt.Tag $$0, net.minecraft.nbt.Tag $$1) {
      return createCollector($$0)
         .map($$1x -> DataResult.success($$1x.accept($$1).result()))
         .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + $$0, $$0));
   }

   public DataResult<net.minecraft.nbt.Tag> mergeToList(net.minecraft.nbt.Tag $$0, List<net.minecraft.nbt.Tag> $$1) {
      return createCollector($$0)
         .map($$1x -> DataResult.success($$1x.acceptAll($$1).result()))
         .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + $$0, $$0));
   }

   public DataResult<net.minecraft.nbt.Tag> mergeToMap(net.minecraft.nbt.Tag $$0, net.minecraft.nbt.Tag $$1, net.minecraft.nbt.Tag $$2) {
      if (!($$0 instanceof net.minecraft.nbt.CompoundTag) && !($$0 instanceof net.minecraft.nbt.EndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + $$0, $$0);
      } else if ($$1 instanceof net.minecraft.nbt.StringTag(String var10)) {
         String $$5 = var10;
         net.minecraft.nbt.CompoundTag $$6 = $$0 instanceof net.minecraft.nbt.CompoundTag $$5x ? $$5x.shallowCopy() : new net.minecraft.nbt.CompoundTag();
         $$6.put($$5, $$2);
         return DataResult.success($$6);
      } else {
         return DataResult.error(() -> "key is not a string: " + $$1, $$0);
      }
   }

   public DataResult<net.minecraft.nbt.Tag> mergeToMap(net.minecraft.nbt.Tag $$0, MapLike<net.minecraft.nbt.Tag> $$1) {
      if (!($$0 instanceof net.minecraft.nbt.CompoundTag) && !($$0 instanceof net.minecraft.nbt.EndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + $$0, $$0);
      } else {
         Iterator<Pair<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag>> $$2 = $$1.entries().iterator();
         if (!$$2.hasNext()) {
            return $$0 == this.empty() ? DataResult.success(this.emptyMap()) : DataResult.success($$0);
         } else {
            net.minecraft.nbt.CompoundTag $$4 = $$0 instanceof net.minecraft.nbt.CompoundTag $$3 ? $$3.shallowCopy() : new net.minecraft.nbt.CompoundTag();
            List<net.minecraft.nbt.Tag> $$5 = new ArrayList<>();
            $$2.forEachRemaining($$2x -> {
               net.minecraft.nbt.Tag $$3x = (net.minecraft.nbt.Tag)$$2x.getFirst();
               if ($$3x instanceof net.minecraft.nbt.StringTag(String $$5x)) {
                  $$4.put($$5x, (net.minecraft.nbt.Tag)$$2x.getSecond());
               } else {
                  $$5.add($$3x);
               }
            });
            return !$$5.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + $$5, $$4) : DataResult.success($$4);
         }
      }
   }

   public DataResult<net.minecraft.nbt.Tag> mergeToMap(net.minecraft.nbt.Tag $$0, Map<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag> $$1) {
      if (!($$0 instanceof net.minecraft.nbt.CompoundTag) && !($$0 instanceof net.minecraft.nbt.EndTag)) {
         return DataResult.error(() -> "mergeToMap called with not a map: " + $$0, $$0);
      } else if ($$1.isEmpty()) {
         return $$0 == this.empty() ? DataResult.success(this.emptyMap()) : DataResult.success($$0);
      } else {
         net.minecraft.nbt.CompoundTag $$3 = $$0 instanceof net.minecraft.nbt.CompoundTag $$2 ? $$2.shallowCopy() : new net.minecraft.nbt.CompoundTag();
         List<net.minecraft.nbt.Tag> $$4 = new ArrayList<>();

         for (Entry<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag> $$5 : $$1.entrySet()) {
            net.minecraft.nbt.Tag $$6 = $$5.getKey();
            if ($$6 instanceof net.minecraft.nbt.StringTag(String var10)) {
               $$3.put(var10, $$5.getValue());
            } else {
               $$4.add($$6);
            }
         }

         return !$$4.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + $$4, $$3) : DataResult.success($$3);
      }
   }

   public DataResult<Stream<Pair<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag>>> getMapValues(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.CompoundTag $$1
         ? DataResult.success($$1.entrySet().stream().map($$0x -> Pair.of(this.createString((String)$$0x.getKey()), (net.minecraft.nbt.Tag)$$0x.getValue())))
         : DataResult.error(() -> "Not a map: " + $$0);
   }

   public DataResult<Consumer<BiConsumer<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag>>> getMapEntries(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.CompoundTag $$1 ? DataResult.success((Consumer<BiConsumer>)$$1x -> {
         for (Entry<String, net.minecraft.nbt.Tag> $$2 : $$1.entrySet()) {
            $$1x.accept(this.createString($$2.getKey()), $$2.getValue());
         }
      }) : DataResult.error(() -> "Not a map: " + $$0);
   }

   public DataResult<MapLike<net.minecraft.nbt.Tag>> getMap(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.CompoundTag $$1 ? DataResult.success(new MapLike<net.minecraft.nbt.Tag>() {
         @Nullable
         public net.minecraft.nbt.Tag get(net.minecraft.nbt.Tag $$0) {
            if ($$0 instanceof net.minecraft.nbt.StringTag(String var4)) {
               return $$1.get(var4);
            } else {
               throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + $$0);
            }
         }

         @Nullable
         public net.minecraft.nbt.Tag get(String $$0) {
            return $$1.get($$0);
         }

         public Stream<Pair<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag>> entries() {
            return $$1.entrySet().stream().map($$0x -> Pair.of(NbtOps.this.createString($$0x.getKey()), $$0x.getValue()));
         }

         @Override
         public String toString() {
            return "MapLike[" + $$1 + "]";
         }
      }) : DataResult.error(() -> "Not a map: " + $$0);
   }

   public net.minecraft.nbt.Tag createMap(Stream<Pair<net.minecraft.nbt.Tag, net.minecraft.nbt.Tag>> $$0) {
      net.minecraft.nbt.CompoundTag $$1 = new net.minecraft.nbt.CompoundTag();
      $$0.forEach($$1x -> {
         net.minecraft.nbt.Tag $$2 = (net.minecraft.nbt.Tag)$$1x.getFirst();
         net.minecraft.nbt.Tag $$3 = (net.minecraft.nbt.Tag)$$1x.getSecond();
         if ($$2 instanceof net.minecraft.nbt.StringTag(String $$5)) {
            $$1.put($$5, $$3);
         } else {
            throw new UnsupportedOperationException("Cannot create map with non-string key: " + $$2);
         }
      });
      return $$1;
   }

   public DataResult<Stream<net.minecraft.nbt.Tag>> getStream(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.CollectionTag $$1 ? DataResult.success($$1.stream()) : DataResult.error(() -> "Not a list");
   }

   public DataResult<Consumer<Consumer<net.minecraft.nbt.Tag>>> getList(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.CollectionTag $$1 ? DataResult.success($$1::forEach) : DataResult.error(() -> "Not a list: " + $$0);
   }

   public DataResult<ByteBuffer> getByteBuffer(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.ByteArrayTag $$1 ? DataResult.success(ByteBuffer.wrap($$1.getAsByteArray())) : super.getByteBuffer($$0);
   }

   public net.minecraft.nbt.Tag createByteList(ByteBuffer $$0) {
      ByteBuffer $$1 = $$0.duplicate().clear();
      byte[] $$2 = new byte[$$0.capacity()];
      $$1.get(0, $$2, 0, $$2.length);
      return new net.minecraft.nbt.ByteArrayTag($$2);
   }

   public DataResult<IntStream> getIntStream(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.IntArrayTag $$1 ? DataResult.success(Arrays.stream($$1.getAsIntArray())) : super.getIntStream($$0);
   }

   public net.minecraft.nbt.Tag createIntList(IntStream $$0) {
      return new net.minecraft.nbt.IntArrayTag($$0.toArray());
   }

   public DataResult<LongStream> getLongStream(net.minecraft.nbt.Tag $$0) {
      return $$0 instanceof net.minecraft.nbt.LongArrayTag $$1 ? DataResult.success(Arrays.stream($$1.getAsLongArray())) : super.getLongStream($$0);
   }

   public net.minecraft.nbt.Tag createLongList(LongStream $$0) {
      return new net.minecraft.nbt.LongArrayTag($$0.toArray());
   }

   public net.minecraft.nbt.Tag createList(Stream<net.minecraft.nbt.Tag> $$0) {
      return new net.minecraft.nbt.ListTag($$0.collect(Util.toMutableList()));
   }

   public net.minecraft.nbt.Tag remove(net.minecraft.nbt.Tag $$0, String $$1) {
      if ($$0 instanceof net.minecraft.nbt.CompoundTag $$2) {
         net.minecraft.nbt.CompoundTag $$3 = $$2.shallowCopy();
         $$3.remove($$1);
         return $$3;
      } else {
         return $$0;
      }
   }

   @Override
   public String toString() {
      return "NBT";
   }

   public RecordBuilder<net.minecraft.nbt.Tag> mapBuilder() {
      return new net.minecraft.nbt.NbtOps.NbtRecordBuilder();
   }

   private static Optional<net.minecraft.nbt.NbtOps.ListCollector> createCollector(net.minecraft.nbt.Tag $$0) {
      if ($$0 instanceof net.minecraft.nbt.EndTag) {
         return Optional.of(new net.minecraft.nbt.NbtOps.GenericListCollector());
      } else if ($$0 instanceof net.minecraft.nbt.CollectionTag $$1) {
         if ($$1.isEmpty()) {
            return Optional.of(new net.minecraft.nbt.NbtOps.GenericListCollector());
         } else {
            return switch ($$1) {
               case net.minecraft.nbt.ListTag $$2 -> Optional.of(new net.minecraft.nbt.NbtOps.GenericListCollector($$2));
               case net.minecraft.nbt.ByteArrayTag $$3 -> Optional.of(new net.minecraft.nbt.NbtOps.ByteListCollector($$3.getAsByteArray()));
               case net.minecraft.nbt.IntArrayTag $$4 -> Optional.of(new net.minecraft.nbt.NbtOps.IntListCollector($$4.getAsIntArray()));
               case net.minecraft.nbt.LongArrayTag $$5 -> Optional.of(new net.minecraft.nbt.NbtOps.LongListCollector($$5.getAsLongArray()));
               default -> throw new MatchException(null, null);
            };
         }
      } else {
         return Optional.empty();
      }
   }

   static class ByteListCollector implements net.minecraft.nbt.NbtOps.ListCollector {
      private final ByteArrayList values = new ByteArrayList();

      public ByteListCollector(byte[] $$0) {
         this.values.addElements(0, $$0);
      }

      @Override
      public net.minecraft.nbt.NbtOps.ListCollector accept(net.minecraft.nbt.Tag $$0) {
         if ($$0 instanceof net.minecraft.nbt.ByteTag $$1) {
            this.values.add($$1.byteValue());
            return this;
         } else {
            return new net.minecraft.nbt.NbtOps.GenericListCollector(this.values).accept($$0);
         }
      }

      @Override
      public net.minecraft.nbt.Tag result() {
         return new net.minecraft.nbt.ByteArrayTag(this.values.toByteArray());
      }
   }

   static class GenericListCollector implements net.minecraft.nbt.NbtOps.ListCollector {
      private final net.minecraft.nbt.ListTag result = new net.minecraft.nbt.ListTag();

      GenericListCollector() {
      }

      GenericListCollector(net.minecraft.nbt.ListTag $$0) {
         this.result.addAll($$0);
      }

      public GenericListCollector(IntArrayList $$0) {
         $$0.forEach($$0x -> this.result.add(net.minecraft.nbt.IntTag.valueOf($$0x)));
      }

      public GenericListCollector(ByteArrayList $$0) {
         $$0.forEach($$0x -> this.result.add(net.minecraft.nbt.ByteTag.valueOf($$0x)));
      }

      public GenericListCollector(LongArrayList $$0) {
         $$0.forEach($$0x -> this.result.add(net.minecraft.nbt.LongTag.valueOf($$0x)));
      }

      @Override
      public net.minecraft.nbt.NbtOps.ListCollector accept(net.minecraft.nbt.Tag $$0) {
         this.result.add($$0);
         return this;
      }

      @Override
      public net.minecraft.nbt.Tag result() {
         return this.result;
      }
   }

   static class IntListCollector implements net.minecraft.nbt.NbtOps.ListCollector {
      private final IntArrayList values = new IntArrayList();

      public IntListCollector(int[] $$0) {
         this.values.addElements(0, $$0);
      }

      @Override
      public net.minecraft.nbt.NbtOps.ListCollector accept(net.minecraft.nbt.Tag $$0) {
         if ($$0 instanceof net.minecraft.nbt.IntTag $$1) {
            this.values.add($$1.intValue());
            return this;
         } else {
            return new net.minecraft.nbt.NbtOps.GenericListCollector(this.values).accept($$0);
         }
      }

      @Override
      public net.minecraft.nbt.Tag result() {
         return new net.minecraft.nbt.IntArrayTag(this.values.toIntArray());
      }
   }

   interface ListCollector {
      net.minecraft.nbt.NbtOps.ListCollector accept(net.minecraft.nbt.Tag var1);

      default net.minecraft.nbt.NbtOps.ListCollector acceptAll(Iterable<net.minecraft.nbt.Tag> $$0) {
         net.minecraft.nbt.NbtOps.ListCollector $$1 = this;

         for (net.minecraft.nbt.Tag $$2 : $$0) {
            $$1 = $$1.accept($$2);
         }

         return $$1;
      }

      default net.minecraft.nbt.NbtOps.ListCollector acceptAll(Stream<net.minecraft.nbt.Tag> $$0) {
         return this.acceptAll($$0::iterator);
      }

      net.minecraft.nbt.Tag result();
   }

   static class LongListCollector implements net.minecraft.nbt.NbtOps.ListCollector {
      private final LongArrayList values = new LongArrayList();

      public LongListCollector(long[] $$0) {
         this.values.addElements(0, $$0);
      }

      @Override
      public net.minecraft.nbt.NbtOps.ListCollector accept(net.minecraft.nbt.Tag $$0) {
         if ($$0 instanceof net.minecraft.nbt.LongTag $$1) {
            this.values.add($$1.longValue());
            return this;
         } else {
            return new net.minecraft.nbt.NbtOps.GenericListCollector(this.values).accept($$0);
         }
      }

      @Override
      public net.minecraft.nbt.Tag result() {
         return new net.minecraft.nbt.LongArrayTag(this.values.toLongArray());
      }
   }

   class NbtRecordBuilder extends AbstractStringBuilder<net.minecraft.nbt.Tag, net.minecraft.nbt.CompoundTag> {
      protected NbtRecordBuilder() {
         super(NbtOps.this);
      }

      protected net.minecraft.nbt.CompoundTag initBuilder() {
         return new net.minecraft.nbt.CompoundTag();
      }

      protected net.minecraft.nbt.CompoundTag append(String $$0, net.minecraft.nbt.Tag $$1, net.minecraft.nbt.CompoundTag $$2) {
         $$2.put($$0, $$1);
         return $$2;
      }

      protected DataResult<net.minecraft.nbt.Tag> build(net.minecraft.nbt.CompoundTag $$0, net.minecraft.nbt.Tag $$1) {
         if ($$1 == null || $$1 == net.minecraft.nbt.EndTag.INSTANCE) {
            return DataResult.success($$0);
         } else if (!($$1 instanceof net.minecraft.nbt.CompoundTag $$2)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + $$1, $$1);
         } else {
            net.minecraft.nbt.CompoundTag $$3 = $$2.shallowCopy();

            for (Entry<String, net.minecraft.nbt.Tag> $$4 : $$0.entrySet()) {
               $$3.put($$4.getKey(), $$4.getValue());
            }

            return DataResult.success($$3);
         }
      }
   }
}
