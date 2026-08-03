package net.minecraft.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class HashOps implements DynamicOps<HashCode> {
   private static final byte TAG_EMPTY = 1;
   private static final byte TAG_MAP_START = 2;
   private static final byte TAG_MAP_END = 3;
   private static final byte TAG_LIST_START = 4;
   private static final byte TAG_LIST_END = 5;
   private static final byte TAG_BYTE = 6;
   private static final byte TAG_SHORT = 7;
   private static final byte TAG_INT = 8;
   private static final byte TAG_LONG = 9;
   private static final byte TAG_FLOAT = 10;
   private static final byte TAG_DOUBLE = 11;
   private static final byte TAG_STRING = 12;
   private static final byte TAG_BOOLEAN = 13;
   private static final byte TAG_BYTE_ARRAY_START = 14;
   private static final byte TAG_BYTE_ARRAY_END = 15;
   private static final byte TAG_INT_ARRAY_START = 16;
   private static final byte TAG_INT_ARRAY_END = 17;
   private static final byte TAG_LONG_ARRAY_START = 18;
   private static final byte TAG_LONG_ARRAY_END = 19;
   private static final byte[] EMPTY_PAYLOAD = new byte[]{1};
   private static final byte[] FALSE_PAYLOAD = new byte[]{13, 0};
   private static final byte[] TRUE_PAYLOAD = new byte[]{13, 1};
   public static final byte[] EMPTY_MAP_PAYLOAD = new byte[]{2, 3};
   public static final byte[] EMPTY_LIST_PAYLOAD = new byte[]{4, 5};
   private static final DataResult<Object> UNSUPPORTED_OPERATION_ERROR = DataResult.error(() -> "Unsupported operation");
   private static final Comparator<HashCode> HASH_COMPARATOR = Comparator.comparingLong(HashCode::padToLong);
   private static final Comparator<Entry<HashCode, HashCode>> MAP_ENTRY_ORDER = Entry.<HashCode, HashCode>comparingByKey(HASH_COMPARATOR)
      .thenComparing(Entry.comparingByValue(HASH_COMPARATOR));
   private static final Comparator<Pair<HashCode, HashCode>> MAPLIKE_ENTRY_ORDER = Comparator.<Pair<HashCode, HashCode>, HashCode>comparing(
         Pair::getFirst, HASH_COMPARATOR
      )
      .thenComparing(Pair::getSecond, HASH_COMPARATOR);
   public static final net.minecraft.util.HashOps CRC32C_INSTANCE = new net.minecraft.util.HashOps(Hashing.crc32c());
   final HashFunction hashFunction;
   final HashCode empty;
   private final HashCode emptyMap;
   private final HashCode emptyList;
   private final HashCode trueHash;
   private final HashCode falseHash;

   public HashOps(HashFunction $$0) {
      this.hashFunction = $$0;
      this.empty = $$0.hashBytes(EMPTY_PAYLOAD);
      this.emptyMap = $$0.hashBytes(EMPTY_MAP_PAYLOAD);
      this.emptyList = $$0.hashBytes(EMPTY_LIST_PAYLOAD);
      this.falseHash = $$0.hashBytes(FALSE_PAYLOAD);
      this.trueHash = $$0.hashBytes(TRUE_PAYLOAD);
   }

   public HashCode empty() {
      return this.empty;
   }

   public HashCode emptyMap() {
      return this.emptyMap;
   }

   public HashCode emptyList() {
      return this.emptyList;
   }

   public HashCode createNumeric(Number $$0) {
      return switch ($$0) {
         case Byte $$1 -> this.createByte($$1);
         case Short $$2 -> this.createShort($$2);
         case Integer $$3 -> this.createInt($$3);
         case Long $$4 -> this.createLong($$4);
         case Double $$5 -> this.createDouble($$5);
         case Float $$6 -> this.createFloat($$6);
         default -> this.createDouble($$0.doubleValue());
      };
   }

   public HashCode createByte(byte $$0) {
      return this.hashFunction.newHasher(2).putByte((byte)6).putByte($$0).hash();
   }

   public HashCode createShort(short $$0) {
      return this.hashFunction.newHasher(3).putByte((byte)7).putShort($$0).hash();
   }

   public HashCode createInt(int $$0) {
      return this.hashFunction.newHasher(5).putByte((byte)8).putInt($$0).hash();
   }

   public HashCode createLong(long $$0) {
      return this.hashFunction.newHasher(9).putByte((byte)9).putLong($$0).hash();
   }

   public HashCode createFloat(float $$0) {
      return this.hashFunction.newHasher(5).putByte((byte)10).putFloat($$0).hash();
   }

   public HashCode createDouble(double $$0) {
      return this.hashFunction.newHasher(9).putByte((byte)11).putDouble($$0).hash();
   }

   public HashCode createString(String $$0) {
      return this.hashFunction.newHasher().putByte((byte)12).putInt($$0.length()).putUnencodedChars($$0).hash();
   }

   public HashCode createBoolean(boolean $$0) {
      return $$0 ? this.trueHash : this.falseHash;
   }

   private static Hasher hashMap(Hasher $$0, Map<HashCode, HashCode> $$1) {
      $$0.putByte((byte)2);
      $$1.entrySet()
         .stream()
         .sorted(MAP_ENTRY_ORDER)
         .forEach($$1x -> $$0.putBytes(((HashCode)$$1x.getKey()).asBytes()).putBytes(((HashCode)$$1x.getValue()).asBytes()));
      $$0.putByte((byte)3);
      return $$0;
   }

   static Hasher hashMap(Hasher $$0, Stream<Pair<HashCode, HashCode>> $$1) {
      $$0.putByte((byte)2);
      $$1.sorted(MAPLIKE_ENTRY_ORDER).forEach($$1x -> $$0.putBytes(((HashCode)$$1x.getFirst()).asBytes()).putBytes(((HashCode)$$1x.getSecond()).asBytes()));
      $$0.putByte((byte)3);
      return $$0;
   }

   public HashCode createMap(Stream<Pair<HashCode, HashCode>> $$0) {
      return hashMap(this.hashFunction.newHasher(), $$0).hash();
   }

   public HashCode createMap(Map<HashCode, HashCode> $$0) {
      return hashMap(this.hashFunction.newHasher(), $$0).hash();
   }

   public HashCode createList(Stream<HashCode> $$0) {
      Hasher $$1 = this.hashFunction.newHasher();
      $$1.putByte((byte)4);
      $$0.forEach($$1x -> $$1.putBytes($$1x.asBytes()));
      $$1.putByte((byte)5);
      return $$1.hash();
   }

   public HashCode createByteList(ByteBuffer $$0) {
      Hasher $$1 = this.hashFunction.newHasher();
      $$1.putByte((byte)14);
      $$1.putBytes($$0);
      $$1.putByte((byte)15);
      return $$1.hash();
   }

   public HashCode createIntList(IntStream $$0) {
      Hasher $$1 = this.hashFunction.newHasher();
      $$1.putByte((byte)16);
      $$0.forEach($$1::putInt);
      $$1.putByte((byte)17);
      return $$1.hash();
   }

   public HashCode createLongList(LongStream $$0) {
      Hasher $$1 = this.hashFunction.newHasher();
      $$1.putByte((byte)18);
      $$0.forEach($$1::putLong);
      $$1.putByte((byte)19);
      return $$1.hash();
   }

   public HashCode remove(HashCode $$0, String $$1) {
      return $$0;
   }

   public RecordBuilder<HashCode> mapBuilder() {
      return new net.minecraft.util.HashOps.MapHashBuilder();
   }

   public ListBuilder<HashCode> listBuilder() {
      return new net.minecraft.util.HashOps.ListHashBuilder();
   }

   @Override
   public String toString() {
      return "Hash " + this.hashFunction;
   }

   public <U> U convertTo(DynamicOps<U> $$0, HashCode $$1) {
      throw new UnsupportedOperationException("Can't convert from this type");
   }

   public Number getNumberValue(HashCode $$0, Number $$1) {
      return $$1;
   }

   public HashCode set(HashCode $$0, String $$1, HashCode $$2) {
      return $$0;
   }

   public HashCode update(HashCode $$0, String $$1, Function<HashCode, HashCode> $$2) {
      return $$0;
   }

   public HashCode updateGeneric(HashCode $$0, HashCode $$1, Function<HashCode, HashCode> $$2) {
      return $$0;
   }

   private static <T> DataResult<T> unsupported() {
      return (DataResult<T>)UNSUPPORTED_OPERATION_ERROR;
   }

   public DataResult<HashCode> get(HashCode $$0, String $$1) {
      return unsupported();
   }

   public DataResult<HashCode> getGeneric(HashCode $$0, HashCode $$1) {
      return unsupported();
   }

   public DataResult<Number> getNumberValue(HashCode $$0) {
      return unsupported();
   }

   public DataResult<Boolean> getBooleanValue(HashCode $$0) {
      return unsupported();
   }

   public DataResult<String> getStringValue(HashCode $$0) {
      return unsupported();
   }

   boolean isEmpty(HashCode $$0) {
      return $$0.equals(this.empty);
   }

   public DataResult<HashCode> mergeToList(HashCode $$0, HashCode $$1) {
      return this.isEmpty($$0) ? DataResult.success(this.createList(Stream.of($$1))) : unsupported();
   }

   public DataResult<HashCode> mergeToList(HashCode $$0, List<HashCode> $$1) {
      return this.isEmpty($$0) ? DataResult.success(this.createList($$1.stream())) : unsupported();
   }

   public DataResult<HashCode> mergeToMap(HashCode $$0, HashCode $$1, HashCode $$2) {
      return this.isEmpty($$0) ? DataResult.success(this.createMap(Map.of($$1, $$2))) : unsupported();
   }

   public DataResult<HashCode> mergeToMap(HashCode $$0, Map<HashCode, HashCode> $$1) {
      return this.isEmpty($$0) ? DataResult.success(this.createMap($$1)) : unsupported();
   }

   public DataResult<HashCode> mergeToMap(HashCode $$0, MapLike<HashCode> $$1) {
      return this.isEmpty($$0) ? DataResult.success(this.createMap($$1.entries())) : unsupported();
   }

   public DataResult<Stream<Pair<HashCode, HashCode>>> getMapValues(HashCode $$0) {
      return unsupported();
   }

   public DataResult<Consumer<BiConsumer<HashCode, HashCode>>> getMapEntries(HashCode $$0) {
      return unsupported();
   }

   public DataResult<Stream<HashCode>> getStream(HashCode $$0) {
      return unsupported();
   }

   public DataResult<Consumer<Consumer<HashCode>>> getList(HashCode $$0) {
      return unsupported();
   }

   public DataResult<MapLike<HashCode>> getMap(HashCode $$0) {
      return unsupported();
   }

   public DataResult<ByteBuffer> getByteBuffer(HashCode $$0) {
      return unsupported();
   }

   public DataResult<IntStream> getIntStream(HashCode $$0) {
      return unsupported();
   }

   public DataResult<LongStream> getLongStream(HashCode $$0) {
      return unsupported();
   }

   class ListHashBuilder extends net.minecraft.util.AbstractListBuilder<HashCode, Hasher> {
      public ListHashBuilder() {
         super(HashOps.this);
      }

      protected Hasher initBuilder() {
         return HashOps.this.hashFunction.newHasher().putByte((byte)4);
      }

      protected Hasher append(Hasher $$0, HashCode $$1) {
         return $$0.putBytes($$1.asBytes());
      }

      protected DataResult<HashCode> build(Hasher $$0, HashCode $$1) {
         assert $$1.equals(HashOps.this.empty);

         $$0.putByte((byte)5);
         return DataResult.success($$0.hash());
      }
   }

   final class MapHashBuilder extends AbstractUniversalBuilder<HashCode, List<Pair<HashCode, HashCode>>> {
      public MapHashBuilder() {
         super(HashOps.this);
      }

      protected List<Pair<HashCode, HashCode>> initBuilder() {
         return new ArrayList<>();
      }

      protected List<Pair<HashCode, HashCode>> append(HashCode $$0, HashCode $$1, List<Pair<HashCode, HashCode>> $$2) {
         $$2.add(Pair.of($$0, $$1));
         return $$2;
      }

      protected DataResult<HashCode> build(List<Pair<HashCode, HashCode>> $$0, HashCode $$1) {
         assert HashOps.this.isEmpty($$1);

         return DataResult.success(net.minecraft.util.HashOps.hashMap(HashOps.this.hashFunction.newHasher(), $$0.stream()).hash());
      }
   }
}
