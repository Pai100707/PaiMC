/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractStringBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class NbtOps
implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();

    private NbtOps() {
    }

    public Tag empty() {
        return EndTag.INSTANCE;
    }

    public Tag emptyList() {
        return new ListTag();
    }

    public Tag emptyMap() {
        return new CompoundTag();
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public <U> U convertTo(DynamicOps<U> $$0, Tag $$1) {
        Object object;
        Tag tag = $$1;
        Objects.requireNonNull(tag);
        Tag tag2 = tag;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EndTag.class, ByteTag.class, ShortTag.class, IntTag.class, LongTag.class, FloatTag.class, DoubleTag.class, ByteArrayTag.class, StringTag.class, ListTag.class, CompoundTag.class, IntArrayTag.class, LongArrayTag.class}, (Object)tag2, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                EndTag $$2 = (EndTag)tag2;
                object = $$0.empty();
                return (U)object;
            }
            case 1: {
                byte $$3;
                ByteTag byteTag = (ByteTag)tag2;
                try {
                    byte by;
                    $$3 = by = byteTag.value();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                object = $$0.createByte($$3);
                return (U)object;
            }
            case 2: {
                short $$4;
                ShortTag shortTag = (ShortTag)tag2;
                {
                    short s;
                    $$4 = s = shortTag.value();
                }
                object = $$0.createShort($$4);
                return (U)object;
            }
            case 3: {
                int $$5;
                IntTag intTag = (IntTag)tag2;
                {
                    int n2;
                    $$5 = n2 = intTag.value();
                }
                object = $$0.createInt($$5);
                return (U)object;
            }
            case 4: {
                long $$6;
                LongTag longTag = (LongTag)tag2;
                {
                    long l;
                    $$6 = l = longTag.value();
                }
                object = $$0.createLong($$6);
                return (U)object;
            }
            case 5: {
                float $$7;
                FloatTag floatTag = (FloatTag)tag2;
                {
                    float f;
                    $$7 = f = floatTag.value();
                }
                object = $$0.createFloat($$7);
                return (U)object;
            }
            case 6: {
                double $$8;
                DoubleTag doubleTag = (DoubleTag)tag2;
                {
                    double d;
                    $$8 = d = doubleTag.value();
                }
                object = $$0.createDouble($$8);
                return (U)object;
            }
            case 7: {
                ByteArrayTag $$9 = (ByteArrayTag)tag2;
                object = $$0.createByteList(ByteBuffer.wrap($$9.getAsByteArray()));
                return (U)object;
            }
            case 8: {
                String $$10;
                StringTag stringTag = (StringTag)tag2;
                {
                    String string;
                    $$10 = string = stringTag.value();
                }
                object = $$0.createString($$10);
                return (U)object;
            }
            case 9: {
                ListTag $$11 = (ListTag)tag2;
                object = this.convertList($$0, $$11);
                return (U)object;
            }
            case 10: {
                CompoundTag $$12 = (CompoundTag)tag2;
                object = this.convertMap($$0, $$12);
                return (U)object;
            }
            case 11: {
                IntArrayTag $$13 = (IntArrayTag)tag2;
                object = $$0.createIntList(Arrays.stream($$13.getAsIntArray()));
                return (U)object;
            }
            case 12: 
        }
        LongArrayTag $$14 = (LongArrayTag)tag2;
        object = $$0.createLongList(Arrays.stream($$14.getAsLongArray()));
        return (U)object;
    }

    public DataResult<Number> getNumberValue(Tag $$0) {
        return $$0.asNumber().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
    }

    public Tag createNumeric(Number $$0) {
        return DoubleTag.valueOf($$0.doubleValue());
    }

    public Tag createByte(byte $$0) {
        return ByteTag.valueOf($$0);
    }

    public Tag createShort(short $$0) {
        return ShortTag.valueOf($$0);
    }

    public Tag createInt(int $$0) {
        return IntTag.valueOf($$0);
    }

    public Tag createLong(long $$0) {
        return LongTag.valueOf($$0);
    }

    public Tag createFloat(float $$0) {
        return FloatTag.valueOf($$0);
    }

    public Tag createDouble(double $$0) {
        return DoubleTag.valueOf($$0);
    }

    public Tag createBoolean(boolean $$0) {
        return ByteTag.valueOf($$0);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<String> getStringValue(Tag $$0) {
        String $$1;
        if (!($$0 instanceof StringTag)) return DataResult.error(() -> "Not a string");
        StringTag stringTag = (StringTag)$$0;
        try {
            String string;
            $$1 = string = stringTag.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        return DataResult.success((Object)$$1);
    }

    public Tag createString(String $$0) {
        return StringTag.valueOf($$0);
    }

    public DataResult<Tag> mergeToList(Tag $$0, Tag $$12) {
        return NbtOps.createCollector($$0).map($$1 -> DataResult.success((Object)$$1.accept($$12).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf($$0), (Object)$$0));
    }

    public DataResult<Tag> mergeToList(Tag $$0, List<Tag> $$12) {
        return NbtOps.createCollector($$0).map($$1 -> DataResult.success((Object)$$1.acceptAll($$12).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf($$0), (Object)$$0));
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<Tag> mergeToMap(Tag $$0, Tag $$1, Tag $$2) {
        void $$4;
        CompoundTag compoundTag;
        if (!($$0 instanceof CompoundTag) && !($$0 instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf($$0), (Object)$$0);
        }
        if (!($$1 instanceof StringTag)) return DataResult.error(() -> "key is not a string: " + String.valueOf($$1), (Object)$$0);
        StringTag stringTag = (StringTag)$$1;
        try {
            String string;
            String $$3 = string = stringTag.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$5 = (CompoundTag)$$0;
            compoundTag = $$5.shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag $$6 = compoundTag;
        $$6.put((String)$$4, $$2);
        return DataResult.success((Object)$$6);
    }

    public DataResult<Tag> mergeToMap(Tag $$0, MapLike<Tag> $$1) {
        CompoundTag compoundTag;
        if (!($$0 instanceof CompoundTag) && !($$0 instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf($$0), (Object)$$0);
        }
        Iterator $$22 = $$1.entries().iterator();
        if (!$$22.hasNext()) {
            if ($$0 == this.empty()) {
                return DataResult.success((Object)this.emptyMap());
            }
            return DataResult.success((Object)$$0);
        }
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$3 = (CompoundTag)$$0;
            compoundTag = $$3.shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag $$4 = compoundTag;
        ArrayList $$5 = new ArrayList();
        $$22.forEachRemaining($$2 -> {
            void $$7;
            Tag $$3 = (Tag)$$2.getFirst();
            if (!($$3 instanceof StringTag)) {
                $$5.add($$3);
                return;
            }
            StringTag $$4 = (StringTag)$$3;
            try {
                String $$5;
                String $$6 = $$5 = $$4.value();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            $$4.put((String)$$7, (Tag)$$2.getSecond());
        });
        if (!$$5.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf($$5), (Object)$$4);
        }
        return DataResult.success((Object)$$4);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public DataResult<Tag> mergeToMap(Tag $$0, Map<Tag, Tag> $$1) {
        CompoundTag compoundTag;
        if (!($$0 instanceof CompoundTag) && !($$0 instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf($$0), (Object)$$0);
        }
        if ($$1.isEmpty()) {
            if ($$0 == this.empty()) {
                return DataResult.success((Object)this.emptyMap());
            }
            return DataResult.success((Object)$$0);
        }
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$2 = (CompoundTag)$$0;
            compoundTag = $$2.shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag $$3 = compoundTag;
        ArrayList<Tag> $$4 = new ArrayList<Tag>();
        for (Map.Entry<Tag, Tag> $$5 : $$1.entrySet()) {
            Tag $$6 = $$5.getKey();
            if ($$6 instanceof StringTag) {
                StringTag stringTag = (StringTag)$$6;
                try {
                    String string;
                    String $$7 = string = stringTag.value();
                    $$3.put($$7, $$5.getValue());
                    continue;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            $$4.add($$6);
        }
        if (!$$4.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + String.valueOf($$4), (Object)$$3);
        }
        return DataResult.success((Object)$$3);
    }

    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag $$02) {
        if ($$02 instanceof CompoundTag) {
            CompoundTag $$1 = (CompoundTag)$$02;
            return DataResult.success($$1.entrySet().stream().map($$0 -> Pair.of((Object)this.createString((String)$$0.getKey()), (Object)((Tag)$$0.getValue()))));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf($$02));
    }

    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag $$0) {
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$12 = (CompoundTag)$$0;
            return DataResult.success($$1 -> {
                for (Map.Entry<String, Tag> $$2 : $$12.entrySet()) {
                    $$1.accept(this.createString($$2.getKey()), $$2.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf($$0));
    }

    public DataResult<MapLike<Tag>> getMap(Tag $$0) {
        if ($$0 instanceof CompoundTag) {
            final CompoundTag $$1 = (CompoundTag)$$0;
            return DataResult.success((Object)new MapLike<Tag>(){

                /*
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                public @Nullable Tag get(Tag $$0) {
                    if (!($$0 instanceof StringTag)) throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + String.valueOf($$0));
                    StringTag stringTag = (StringTag)$$0;
                    try {
                        String string;
                        String $$12 = string = stringTag.value();
                        return $$1.get($$12);
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }

                public @Nullable Tag get(String $$0) {
                    return $$1.get($$0);
                }

                public Stream<Pair<Tag, Tag>> entries() {
                    return $$1.entrySet().stream().map($$0 -> Pair.of((Object)NbtOps.this.createString((String)$$0.getKey()), (Object)((Tag)$$0.getValue())));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf($$1) + "]";
                }

                public /* synthetic */ @Nullable Object get(String string) {
                    return this.get(string);
                }

                public /* synthetic */ @Nullable Object get(Object object) {
                    return this.get((Tag)object);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf($$0));
    }

    public Tag createMap(Stream<Pair<Tag, Tag>> $$0) {
        CompoundTag $$12 = new CompoundTag();
        $$0.forEach($$1 -> {
            Tag $$2 = (Tag)$$1.getFirst();
            Tag $$3 = (Tag)$$1.getSecond();
            if (!($$2 instanceof StringTag)) throw new UnsupportedOperationException("Cannot create map with non-string key: " + String.valueOf($$2));
            StringTag $$4 = (StringTag)$$2;
            try {
                String $$5;
                String $$6 = $$5 = $$4.value();
                $$12.put($$6, $$3);
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        });
        return $$12;
    }

    public DataResult<Stream<Tag>> getStream(Tag $$0) {
        if ($$0 instanceof CollectionTag) {
            CollectionTag $$1 = (CollectionTag)$$0;
            return DataResult.success($$1.stream());
        }
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<Tag>>> getList(Tag $$0) {
        if ($$0 instanceof CollectionTag) {
            CollectionTag $$1 = (CollectionTag)$$0;
            return DataResult.success($$1::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf($$0));
    }

    public DataResult<ByteBuffer> getByteBuffer(Tag $$0) {
        if ($$0 instanceof ByteArrayTag) {
            ByteArrayTag $$1 = (ByteArrayTag)$$0;
            return DataResult.success((Object)ByteBuffer.wrap($$1.getAsByteArray()));
        }
        return super.getByteBuffer((Object)$$0);
    }

    public Tag createByteList(ByteBuffer $$0) {
        ByteBuffer $$1 = $$0.duplicate().clear();
        byte[] $$2 = new byte[$$0.capacity()];
        $$1.get(0, $$2, 0, $$2.length);
        return new ByteArrayTag($$2);
    }

    public DataResult<IntStream> getIntStream(Tag $$0) {
        if ($$0 instanceof IntArrayTag) {
            IntArrayTag $$1 = (IntArrayTag)$$0;
            return DataResult.success((Object)Arrays.stream($$1.getAsIntArray()));
        }
        return super.getIntStream((Object)$$0);
    }

    public Tag createIntList(IntStream $$0) {
        return new IntArrayTag($$0.toArray());
    }

    public DataResult<LongStream> getLongStream(Tag $$0) {
        if ($$0 instanceof LongArrayTag) {
            LongArrayTag $$1 = (LongArrayTag)$$0;
            return DataResult.success((Object)Arrays.stream($$1.getAsLongArray()));
        }
        return super.getLongStream((Object)$$0);
    }

    public Tag createLongList(LongStream $$0) {
        return new LongArrayTag($$0.toArray());
    }

    public Tag createList(Stream<Tag> $$0) {
        return new ListTag($$0.collect(Util.toMutableList()));
    }

    public Tag remove(Tag $$0, String $$1) {
        if ($$0 instanceof CompoundTag) {
            CompoundTag $$2 = (CompoundTag)$$0;
            CompoundTag $$3 = $$2.shallowCopy();
            $$3.remove($$1);
            return $$3;
        }
        return $$0;
    }

    public String toString() {
        return "NBT";
    }

    public RecordBuilder<Tag> mapBuilder() {
        return new NbtRecordBuilder(this);
    }

    private static Optional<ListCollector> createCollector(Tag $$0) {
        if ($$0 instanceof EndTag) {
            return Optional.of(new GenericListCollector());
        }
        if ($$0 instanceof CollectionTag) {
            CollectionTag $$1 = (CollectionTag)$$0;
            if ($$1.isEmpty()) {
                return Optional.of(new GenericListCollector());
            }
            CollectionTag collectionTag = $$1;
            Objects.requireNonNull(collectionTag);
            CollectionTag collectionTag2 = collectionTag;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ListTag.class, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class}, (Object)collectionTag2, n)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    ListTag $$2 = (ListTag)collectionTag2;
                    yield Optional.of(new GenericListCollector($$2));
                }
                case 1 -> {
                    ByteArrayTag $$3 = (ByteArrayTag)collectionTag2;
                    yield Optional.of(new ByteListCollector($$3.getAsByteArray()));
                }
                case 2 -> {
                    IntArrayTag $$4 = (IntArrayTag)collectionTag2;
                    yield Optional.of(new IntListCollector($$4.getAsIntArray()));
                }
                case 3 -> {
                    LongArrayTag $$5 = (LongArrayTag)collectionTag2;
                    yield Optional.of(new LongListCollector($$5.getAsLongArray()));
                }
            };
        }
        return Optional.empty();
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Tag)object, string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Tag)object);
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Tag)object);
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Tag)object);
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Tag)object);
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Tag)object);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Tag)object);
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Tag)object);
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Tag)object);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Tag)object, (MapLike<Tag>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Tag)object, (Map<Tag, Tag>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Tag)object, (Tag)object2, (Tag)object3);
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Tag)object, (List<Tag>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Tag)object, (Tag)object2);
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Tag)object);
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
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
        return this.getNumberValue((Tag)object);
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Tag)object);
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

    class NbtRecordBuilder
    extends RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder(NbtOps nbtOps) {
            super((DynamicOps)nbtOps);
        }

        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        protected CompoundTag append(String $$0, Tag $$1, CompoundTag $$2) {
            $$2.put($$0, $$1);
            return $$2;
        }

        protected DataResult<Tag> build(CompoundTag $$0, Tag $$1) {
            if ($$1 == null || $$1 == EndTag.INSTANCE) {
                return DataResult.success((Object)$$0);
            }
            if ($$1 instanceof CompoundTag) {
                CompoundTag $$2 = (CompoundTag)$$1;
                CompoundTag $$3 = $$2.shallowCopy();
                for (Map.Entry<String, Tag> $$4 : $$0.entrySet()) {
                    $$3.put($$4.getKey(), $$4.getValue());
                }
                return DataResult.success((Object)$$3);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf($$1), (Object)$$1);
        }

        protected /* synthetic */ Object append(String string, Object object, Object object2) {
            return this.append(string, (Tag)object, (CompoundTag)object2);
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((CompoundTag)object, (Tag)object2);
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class GenericListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        GenericListCollector() {
        }

        GenericListCollector(ListTag $$0) {
            this.result.addAll($$0);
        }

        public GenericListCollector(IntArrayList $$02) {
            $$02.forEach($$0 -> this.result.add(IntTag.valueOf($$0)));
        }

        public GenericListCollector(ByteArrayList $$02) {
            $$02.forEach($$0 -> this.result.add(ByteTag.valueOf($$0)));
        }

        public GenericListCollector(LongArrayList $$02) {
            $$02.forEach($$0 -> this.result.add(LongTag.valueOf($$0)));
        }

        @Override
        public ListCollector accept(Tag $$0) {
            this.result.add($$0);
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class ByteListCollector
    implements ListCollector {
        private final ByteArrayList values = new ByteArrayList();

        public ByteListCollector(byte[] $$0) {
            this.values.addElements(0, $$0);
        }

        @Override
        public ListCollector accept(Tag $$0) {
            if ($$0 instanceof ByteTag) {
                ByteTag $$1 = (ByteTag)$$0;
                this.values.add($$1.byteValue());
                return this;
            }
            return new GenericListCollector(this.values).accept($$0);
        }

        @Override
        public Tag result() {
            return new ByteArrayTag(this.values.toByteArray());
        }
    }

    static class IntListCollector
    implements ListCollector {
        private final IntArrayList values = new IntArrayList();

        public IntListCollector(int[] $$0) {
            this.values.addElements(0, $$0);
        }

        @Override
        public ListCollector accept(Tag $$0) {
            if ($$0 instanceof IntTag) {
                IntTag $$1 = (IntTag)$$0;
                this.values.add($$1.intValue());
                return this;
            }
            return new GenericListCollector(this.values).accept($$0);
        }

        @Override
        public Tag result() {
            return new IntArrayTag(this.values.toIntArray());
        }
    }

    static class LongListCollector
    implements ListCollector {
        private final LongArrayList values = new LongArrayList();

        public LongListCollector(long[] $$0) {
            this.values.addElements(0, $$0);
        }

        @Override
        public ListCollector accept(Tag $$0) {
            if ($$0 instanceof LongTag) {
                LongTag $$1 = (LongTag)$$0;
                this.values.add($$1.longValue());
                return this;
            }
            return new GenericListCollector(this.values).accept($$0);
        }

        @Override
        public Tag result() {
            return new LongArrayTag(this.values.toLongArray());
        }
    }

    static interface ListCollector {
        public ListCollector accept(Tag var1);

        default public ListCollector acceptAll(Iterable<Tag> $$0) {
            ListCollector $$1 = this;
            for (Tag $$2 : $$0) {
                $$1 = $$1.accept($$2);
            }
            return $$1;
        }

        default public ListCollector acceptAll(Stream<Tag> $$0) {
            return this.acceptAll($$0::iterator);
        }

        public Tag result();
    }
}

