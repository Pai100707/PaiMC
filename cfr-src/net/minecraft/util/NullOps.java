/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.ListBuilder
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractUniversalBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.util.AbstractListBuilder;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public class NullOps
implements DynamicOps<Unit> {
    public static final NullOps INSTANCE = new NullOps();
    private static final MapLike<Unit> EMPTY_MAP = new MapLike<Unit>(){

        public @Nullable Unit get(Unit $$0) {
            return null;
        }

        public @Nullable Unit get(String $$0) {
            return null;
        }

        public Stream<Pair<Unit, Unit>> entries() {
            return Stream.empty();
        }

        public /* synthetic */ @Nullable Object get(String string) {
            return this.get(string);
        }

        public /* synthetic */ @Nullable Object get(Object object) {
            return this.get((Unit)((Object)object));
        }
    };

    private NullOps() {
    }

    public <U> U convertTo(DynamicOps<U> $$0, Unit $$1) {
        return (U)$$0.empty();
    }

    public Unit empty() {
        return Unit.INSTANCE;
    }

    public Unit emptyMap() {
        return Unit.INSTANCE;
    }

    public Unit emptyList() {
        return Unit.INSTANCE;
    }

    public Unit createNumeric(Number $$0) {
        return Unit.INSTANCE;
    }

    public Unit createByte(byte $$0) {
        return Unit.INSTANCE;
    }

    public Unit createShort(short $$0) {
        return Unit.INSTANCE;
    }

    public Unit createInt(int $$0) {
        return Unit.INSTANCE;
    }

    public Unit createLong(long $$0) {
        return Unit.INSTANCE;
    }

    public Unit createFloat(float $$0) {
        return Unit.INSTANCE;
    }

    public Unit createDouble(double $$0) {
        return Unit.INSTANCE;
    }

    public Unit createBoolean(boolean $$0) {
        return Unit.INSTANCE;
    }

    public Unit createString(String $$0) {
        return Unit.INSTANCE;
    }

    public DataResult<Number> getNumberValue(Unit $$0) {
        return DataResult.success((Object)0);
    }

    public DataResult<Boolean> getBooleanValue(Unit $$0) {
        return DataResult.success((Object)false);
    }

    public DataResult<String> getStringValue(Unit $$0) {
        return DataResult.success((Object)"");
    }

    public DataResult<Unit> mergeToList(Unit $$0, Unit $$1) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToList(Unit $$0, List<Unit> $$1) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit $$0, Unit $$1, Unit $$2) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit $$0, Map<Unit, Unit> $$1) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Unit> mergeToMap(Unit $$0, MapLike<Unit> $$1) {
        return DataResult.success((Object)((Object)Unit.INSTANCE));
    }

    public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit $$0) {
        return DataResult.success(Stream.empty());
    }

    public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit $$02) {
        return DataResult.success($$0 -> {});
    }

    public DataResult<MapLike<Unit>> getMap(Unit $$0) {
        return DataResult.success(EMPTY_MAP);
    }

    public DataResult<Stream<Unit>> getStream(Unit $$0) {
        return DataResult.success(Stream.empty());
    }

    public DataResult<Consumer<Consumer<Unit>>> getList(Unit $$02) {
        return DataResult.success($$0 -> {});
    }

    public DataResult<ByteBuffer> getByteBuffer(Unit $$0) {
        return DataResult.success((Object)ByteBuffer.wrap(new byte[0]));
    }

    public DataResult<IntStream> getIntStream(Unit $$0) {
        return DataResult.success((Object)IntStream.empty());
    }

    public DataResult<LongStream> getLongStream(Unit $$0) {
        return DataResult.success((Object)LongStream.empty());
    }

    public Unit createMap(Stream<Pair<Unit, Unit>> $$0) {
        return Unit.INSTANCE;
    }

    public Unit createMap(Map<Unit, Unit> $$0) {
        return Unit.INSTANCE;
    }

    public Unit createList(Stream<Unit> $$0) {
        return Unit.INSTANCE;
    }

    public Unit createByteList(ByteBuffer $$0) {
        return Unit.INSTANCE;
    }

    public Unit createIntList(IntStream $$0) {
        return Unit.INSTANCE;
    }

    public Unit createLongList(LongStream $$0) {
        return Unit.INSTANCE;
    }

    public Unit remove(Unit $$0, String $$1) {
        return $$0;
    }

    public RecordBuilder<Unit> mapBuilder() {
        return new NullMapBuilder(this);
    }

    public ListBuilder<Unit> listBuilder() {
        return new NullListBuilder(this);
    }

    public String toString() {
        return "Null";
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Unit)((Object)object), string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Unit)((Object)object));
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList((Stream<Unit>)stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Unit)((Object)object));
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Unit)((Object)object));
    }

    public /* synthetic */ Object createMap(Map map) {
        return this.createMap((Map<Unit, Unit>)map);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Unit)((Object)object));
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap((Stream<Pair<Unit, Unit>>)stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Unit)((Object)object));
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Unit)((Object)object));
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Unit)((Object)object), (MapLike<Unit>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Unit)((Object)object), (Map<Unit, Unit>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Unit)((Object)object), (List<Unit>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Unit)((Object)object), (Unit)((Object)object2));
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Unit)((Object)object));
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    public /* synthetic */ DataResult getBooleanValue(Object object) {
        return this.getBooleanValue((Unit)((Object)object));
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

    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((Unit)((Object)object));
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Unit)((Object)object));
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

    static final class NullMapBuilder
    extends RecordBuilder.AbstractUniversalBuilder<Unit, Unit> {
        public NullMapBuilder(DynamicOps<Unit> $$0) {
            super($$0);
        }

        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        protected Unit append(Unit $$0, Unit $$1, Unit $$2) {
            return $$2;
        }

        protected DataResult<Unit> build(Unit $$0, Unit $$1) {
            return DataResult.success((Object)((Object)$$1));
        }

        protected /* synthetic */ Object append(Object object, Object object2, Object object3) {
            return this.append((Unit)((Object)object), (Unit)((Object)object2), (Unit)((Object)object3));
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((Unit)((Object)object), (Unit)((Object)object2));
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static final class NullListBuilder
    extends AbstractListBuilder<Unit, Unit> {
        public NullListBuilder(DynamicOps<Unit> $$0) {
            super($$0);
        }

        @Override
        protected Unit initBuilder() {
            return Unit.INSTANCE;
        }

        @Override
        protected Unit append(Unit $$0, Unit $$1) {
            return $$0;
        }

        @Override
        protected DataResult<Unit> build(Unit $$0, Unit $$1) {
            return DataResult.success((Object)((Object)$$0));
        }

        @Override
        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }
}

