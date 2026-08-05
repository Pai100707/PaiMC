/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.HashCode
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hasher
 *  com.google.common.hash.Hashing
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.ListBuilder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractUniversalBuilder
 */
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
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.AbstractListBuilder;

public class HashOps
implements DynamicOps<HashCode> {
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
    private static final Comparator<Map.Entry<HashCode, HashCode>> MAP_ENTRY_ORDER = Map.Entry.comparingByKey(HASH_COMPARATOR).thenComparing(Map.Entry.comparingByValue(HASH_COMPARATOR));
    private static final Comparator<Pair<HashCode, HashCode>> MAPLIKE_ENTRY_ORDER = Comparator.comparing(Pair::getFirst, HASH_COMPARATOR).thenComparing(Pair::getSecond, HASH_COMPARATOR);
    public static final HashOps CRC32C_INSTANCE = new HashOps(Hashing.crc32c());
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
        Number number = $$0;
        Objects.requireNonNull(number);
        Number number2 = number;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Byte.class, Short.class, Integer.class, Long.class, Double.class, Float.class}, (Object)number2, n)) {
            case 0 -> {
                Byte $$1 = (Byte)number2;
                yield this.createByte($$1);
            }
            case 1 -> {
                Short $$2 = (Short)number2;
                yield this.createShort($$2);
            }
            case 2 -> {
                Integer $$3 = (Integer)number2;
                yield this.createInt($$3);
            }
            case 3 -> {
                Long $$4 = (Long)number2;
                yield this.createLong($$4);
            }
            case 4 -> {
                Double $$5 = (Double)number2;
                yield this.createDouble($$5);
            }
            case 5 -> {
                Float $$6 = (Float)number2;
                yield this.createFloat($$6.floatValue());
            }
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
        return this.hashFunction.newHasher().putByte((byte)12).putInt($$0.length()).putUnencodedChars((CharSequence)$$0).hash();
    }

    public HashCode createBoolean(boolean $$0) {
        return $$0 ? this.trueHash : this.falseHash;
    }

    private static Hasher hashMap(Hasher $$0, Map<HashCode, HashCode> $$12) {
        $$0.putByte((byte)2);
        $$12.entrySet().stream().sorted(MAP_ENTRY_ORDER).forEach($$1 -> $$0.putBytes(((HashCode)$$1.getKey()).asBytes()).putBytes(((HashCode)$$1.getValue()).asBytes()));
        $$0.putByte((byte)3);
        return $$0;
    }

    static Hasher hashMap(Hasher $$0, Stream<Pair<HashCode, HashCode>> $$12) {
        $$0.putByte((byte)2);
        $$12.sorted(MAPLIKE_ENTRY_ORDER).forEach($$1 -> $$0.putBytes(((HashCode)$$1.getFirst()).asBytes()).putBytes(((HashCode)$$1.getSecond()).asBytes()));
        $$0.putByte((byte)3);
        return $$0;
    }

    public HashCode createMap(Stream<Pair<HashCode, HashCode>> $$0) {
        return HashOps.hashMap(this.hashFunction.newHasher(), $$0).hash();
    }

    public HashCode createMap(Map<HashCode, HashCode> $$0) {
        return HashOps.hashMap(this.hashFunction.newHasher(), $$0).hash();
    }

    public HashCode createList(Stream<HashCode> $$0) {
        Hasher $$12 = this.hashFunction.newHasher();
        $$12.putByte((byte)4);
        $$0.forEach($$1 -> $$12.putBytes($$1.asBytes()));
        $$12.putByte((byte)5);
        return $$12.hash();
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
        $$0.forEach(arg_0 -> ((Hasher)$$1).putInt(arg_0));
        $$1.putByte((byte)17);
        return $$1.hash();
    }

    public HashCode createLongList(LongStream $$0) {
        Hasher $$1 = this.hashFunction.newHasher();
        $$1.putByte((byte)18);
        $$0.forEach(arg_0 -> ((Hasher)$$1).putLong(arg_0));
        $$1.putByte((byte)19);
        return $$1.hash();
    }

    public HashCode remove(HashCode $$0, String $$1) {
        return $$0;
    }

    public RecordBuilder<HashCode> mapBuilder() {
        return new MapHashBuilder();
    }

    public ListBuilder<HashCode> listBuilder() {
        return new ListHashBuilder();
    }

    public String toString() {
        return "Hash " + String.valueOf(this.hashFunction);
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
        return UNSUPPORTED_OPERATION_ERROR;
    }

    public DataResult<HashCode> get(HashCode $$0, String $$1) {
        return HashOps.unsupported();
    }

    public DataResult<HashCode> getGeneric(HashCode $$0, HashCode $$1) {
        return HashOps.unsupported();
    }

    public DataResult<Number> getNumberValue(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<Boolean> getBooleanValue(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<String> getStringValue(HashCode $$0) {
        return HashOps.unsupported();
    }

    boolean isEmpty(HashCode $$0) {
        return $$0.equals((Object)this.empty);
    }

    public DataResult<HashCode> mergeToList(HashCode $$0, HashCode $$1) {
        if (this.isEmpty($$0)) {
            return DataResult.success((Object)this.createList(Stream.of($$1)));
        }
        return HashOps.unsupported();
    }

    public DataResult<HashCode> mergeToList(HashCode $$0, List<HashCode> $$1) {
        if (this.isEmpty($$0)) {
            return DataResult.success((Object)this.createList($$1.stream()));
        }
        return HashOps.unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode $$0, HashCode $$1, HashCode $$2) {
        if (this.isEmpty($$0)) {
            return DataResult.success((Object)this.createMap(Map.of($$1, $$2)));
        }
        return HashOps.unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode $$0, Map<HashCode, HashCode> $$1) {
        if (this.isEmpty($$0)) {
            return DataResult.success((Object)this.createMap($$1));
        }
        return HashOps.unsupported();
    }

    public DataResult<HashCode> mergeToMap(HashCode $$0, MapLike<HashCode> $$1) {
        if (this.isEmpty($$0)) {
            return DataResult.success((Object)this.createMap((Stream<Pair<HashCode, HashCode>>)$$1.entries()));
        }
        return HashOps.unsupported();
    }

    public DataResult<Stream<Pair<HashCode, HashCode>>> getMapValues(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<Consumer<BiConsumer<HashCode, HashCode>>> getMapEntries(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<Stream<HashCode>> getStream(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<Consumer<Consumer<HashCode>>> getList(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<MapLike<HashCode>> getMap(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<ByteBuffer> getByteBuffer(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<IntStream> getIntStream(HashCode $$0) {
        return HashOps.unsupported();
    }

    public DataResult<LongStream> getLongStream(HashCode $$0) {
        return HashOps.unsupported();
    }

    public /* synthetic */ Object updateGeneric(Object object, Object object2, Function function) {
        return this.updateGeneric((HashCode)object, (HashCode)object2, (Function<HashCode, HashCode>)function);
    }

    public /* synthetic */ Object update(Object object, String string, Function function) {
        return this.update((HashCode)object, string, (Function<HashCode, HashCode>)function);
    }

    public /* synthetic */ Object set(Object object, String string, Object object2) {
        return this.set((HashCode)object, string, (HashCode)object2);
    }

    public /* synthetic */ DataResult getGeneric(Object object, Object object2) {
        return this.getGeneric((HashCode)object, (HashCode)object2);
    }

    public /* synthetic */ DataResult get(Object object, String string) {
        return this.get((HashCode)object, string);
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((HashCode)object, string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((HashCode)object);
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((HashCode)object);
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((HashCode)object);
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList((Stream<HashCode>)stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((HashCode)object);
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((HashCode)object);
    }

    public /* synthetic */ Object createMap(Map map) {
        return this.createMap((Map<HashCode, HashCode>)map);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((HashCode)object);
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap((Stream<Pair<HashCode, HashCode>>)stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((HashCode)object);
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((HashCode)object);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((HashCode)object, (MapLike<HashCode>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((HashCode)object, (Map<HashCode, HashCode>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((HashCode)object, (HashCode)object2, (HashCode)object3);
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((HashCode)object, (List<HashCode>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((HashCode)object, (HashCode)object2);
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((HashCode)object);
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    public /* synthetic */ DataResult getBooleanValue(Object object) {
        return this.getBooleanValue((HashCode)object);
    }

    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    public /* synthetic */ Object createInt(int n) {
        return this.createInt(n);
    }

    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    public /* synthetic */ Object createByte(byte by) {
        return this.createByte(by);
    }

    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    public /* synthetic */ Number getNumberValue(Object object, Number number) {
        return this.getNumberValue((HashCode)object, number);
    }

    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((HashCode)object);
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (HashCode)object);
    }

    public /* synthetic */ Object emptyList() {
        return this.emptyList();
    }

    public /* synthetic */ Object emptyMap() {
        return this.emptyMap();
    }

    public /* synthetic */ Object empty() {
        return this.empty();
    }

    final class MapHashBuilder
    extends RecordBuilder.AbstractUniversalBuilder<HashCode, List<Pair<HashCode, HashCode>>> {
        public MapHashBuilder() {
            super((DynamicOps)HashOps.this);
        }

        protected List<Pair<HashCode, HashCode>> initBuilder() {
            return new ArrayList<Pair<HashCode, HashCode>>();
        }

        protected List<Pair<HashCode, HashCode>> append(HashCode $$0, HashCode $$1, List<Pair<HashCode, HashCode>> $$2) {
            $$2.add((Pair<HashCode, HashCode>)Pair.of((Object)$$0, (Object)$$1));
            return $$2;
        }

        protected DataResult<HashCode> build(List<Pair<HashCode, HashCode>> $$0, HashCode $$1) {
            assert (HashOps.this.isEmpty($$1));
            return DataResult.success((Object)HashOps.hashMap(HashOps.this.hashFunction.newHasher(), $$0.stream()).hash());
        }

        protected /* synthetic */ Object append(Object object, Object object2, Object object3) {
            return this.append((HashCode)object, (HashCode)object2, (List)object3);
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((List)object, (HashCode)object2);
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    class ListHashBuilder
    extends AbstractListBuilder<HashCode, Hasher> {
        public ListHashBuilder() {
            super(HashOps.this);
        }

        @Override
        protected Hasher initBuilder() {
            return HashOps.this.hashFunction.newHasher().putByte((byte)4);
        }

        @Override
        protected Hasher append(Hasher $$0, HashCode $$1) {
            return $$0.putBytes($$1.asBytes());
        }

        @Override
        protected DataResult<HashCode> build(Hasher $$0, HashCode $$1) {
            assert ($$1.equals((Object)HashOps.this.empty));
            $$0.putByte((byte)5);
            return DataResult.success((Object)$$0.hash());
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

