/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagTypes;
import net.minecraft.nbt.TagVisitor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public final class CompoundTag
implements Tag {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<CompoundTag> CODEC = Codec.PASSTHROUGH.comapFlatMap($$0 -> {
        Tag $$1 = (Tag)$$0.convert((DynamicOps)NbtOps.INSTANCE).getValue();
        if ($$1 instanceof CompoundTag) {
            CompoundTag $$2 = (CompoundTag)$$1;
            return DataResult.success((Object)($$2 == $$0.getValue() ? $$2.copy() : $$2));
        }
        return DataResult.error(() -> "Not a compound tag: " + String.valueOf($$1));
    }, $$0 -> new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)$$0.copy()));
    private static final int SELF_SIZE_IN_BYTES = 48;
    private static final int MAP_ENTRY_SIZE_IN_BYTES = 32;
    public static final TagType<CompoundTag> TYPE = new TagType.VariableSize<CompoundTag>(){

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public CompoundTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.pushDepth();
            try {
                CompoundTag compoundTag = 1.loadCompound($$0, $$1);
                return compoundTag;
            }
            finally {
                $$1.popDepth();
            }
        }

        private static CompoundTag loadCompound(DataInput $$0, NbtAccounter $$1) throws IOException {
            byte $$3;
            $$1.accountBytes(48L);
            HashMap $$2 = Maps.newHashMap();
            while (($$3 = $$0.readByte()) != 0) {
                Tag $$5;
                String $$4 = 1.readString($$0, $$1);
                if ($$2.put($$4, $$5 = CompoundTag.readNamedTagData(TagTypes.getType($$3), $$4, $$0, $$1)) != null) continue;
                $$1.accountBytes(36L);
            }
            return new CompoundTag($$2);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            $$2.pushDepth();
            try {
                StreamTagVisitor.ValueResult valueResult = 1.parseCompound($$0, $$1, $$2);
                return valueResult;
            }
            finally {
                $$2.popDepth();
            }
        }

        private static StreamTagVisitor.ValueResult parseCompound(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            byte $$3;
            $$2.accountBytes(48L);
            block13: while (($$3 = $$0.readByte()) != 0) {
                TagType<?> $$4 = TagTypes.getType($$3);
                switch ($$1.visitEntry($$4)) {
                    case HALT: {
                        return StreamTagVisitor.ValueResult.HALT;
                    }
                    case BREAK: {
                        StringTag.skipString($$0);
                        $$4.skip($$0, $$2);
                        break block13;
                    }
                    case SKIP: {
                        StringTag.skipString($$0);
                        $$4.skip($$0, $$2);
                        continue block13;
                    }
                    default: {
                        String $$5 = 1.readString($$0, $$2);
                        switch ($$1.visitEntry($$4, $$5)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                            case BREAK: {
                                $$4.skip($$0, $$2);
                                break block13;
                            }
                            case SKIP: {
                                $$4.skip($$0, $$2);
                                continue block13;
                            }
                        }
                        $$2.accountBytes(36L);
                        switch ($$4.parse($$0, $$1, $$2)) {
                            case HALT: {
                                return StreamTagVisitor.ValueResult.HALT;
                            }
                        }
                        continue block13;
                    }
                }
            }
            if ($$3 != 0) {
                while (($$3 = $$0.readByte()) != 0) {
                    StringTag.skipString($$0);
                    TagTypes.getType($$3).skip($$0, $$2);
                }
            }
            return $$1.visitContainerEnd();
        }

        private static String readString(DataInput $$0, NbtAccounter $$1) throws IOException {
            String $$2 = $$0.readUTF();
            $$1.accountBytes(28L);
            $$1.accountBytes(2L, $$2.length());
            return $$2;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.pushDepth();
            try {
                byte $$2;
                while (($$2 = $$0.readByte()) != 0) {
                    StringTag.skipString($$0);
                    TagTypes.getType($$2).skip($$0, $$1);
                }
            }
            finally {
                $$1.popDepth();
            }
        }

        @Override
        public String getName() {
            return "COMPOUND";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Compound";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private final Map<String, Tag> tags;

    CompoundTag(Map<String, Tag> $$0) {
        this.tags = $$0;
    }

    public CompoundTag() {
        this(new HashMap<String, Tag>());
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        for (String $$1 : this.tags.keySet()) {
            Tag $$2 = this.tags.get($$1);
            CompoundTag.writeNamedTag($$1, $$2, $$0);
        }
        $$0.writeByte(0);
    }

    @Override
    public int sizeInBytes() {
        int $$0 = 48;
        for (Map.Entry<String, Tag> $$1 : this.tags.entrySet()) {
            $$0 += 28 + 2 * $$1.getKey().length();
            $$0 += 36;
            $$0 += $$1.getValue().sizeInBytes();
        }
        return $$0;
    }

    public Set<String> keySet() {
        return this.tags.keySet();
    }

    public Set<Map.Entry<String, Tag>> entrySet() {
        return this.tags.entrySet();
    }

    public Collection<Tag> values() {
        return this.tags.values();
    }

    public void forEach(BiConsumer<String, Tag> $$0) {
        this.tags.forEach($$0);
    }

    @Override
    public byte getId() {
        return 10;
    }

    public TagType<CompoundTag> getType() {
        return TYPE;
    }

    public int size() {
        return this.tags.size();
    }

    public @Nullable Tag put(String $$0, Tag $$1) {
        return this.tags.put($$0, $$1);
    }

    public void putByte(String $$0, byte $$1) {
        this.tags.put($$0, ByteTag.valueOf($$1));
    }

    public void putShort(String $$0, short $$1) {
        this.tags.put($$0, ShortTag.valueOf($$1));
    }

    public void putInt(String $$0, int $$1) {
        this.tags.put($$0, IntTag.valueOf($$1));
    }

    public void putLong(String $$0, long $$1) {
        this.tags.put($$0, LongTag.valueOf($$1));
    }

    public void putFloat(String $$0, float $$1) {
        this.tags.put($$0, FloatTag.valueOf($$1));
    }

    public void putDouble(String $$0, double $$1) {
        this.tags.put($$0, DoubleTag.valueOf($$1));
    }

    public void putString(String $$0, String $$1) {
        this.tags.put($$0, StringTag.valueOf($$1));
    }

    public void putByteArray(String $$0, byte[] $$1) {
        this.tags.put($$0, new ByteArrayTag($$1));
    }

    public void putIntArray(String $$0, int[] $$1) {
        this.tags.put($$0, new IntArrayTag($$1));
    }

    public void putLongArray(String $$0, long[] $$1) {
        this.tags.put($$0, new LongArrayTag($$1));
    }

    public void putBoolean(String $$0, boolean $$1) {
        this.tags.put($$0, ByteTag.valueOf($$1));
    }

    public @Nullable Tag get(String $$0) {
        return this.tags.get($$0);
    }

    public boolean contains(String $$0) {
        return this.tags.containsKey($$0);
    }

    private Optional<Tag> getOptional(String $$0) {
        return Optional.ofNullable(this.tags.get($$0));
    }

    public Optional<Byte> getByte(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asByte);
    }

    public byte getByteOr(String $$0, byte $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.byteValue();
        }
        return $$1;
    }

    public Optional<Short> getShort(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asShort);
    }

    public short getShortOr(String $$0, short $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.shortValue();
        }
        return $$1;
    }

    public Optional<Integer> getInt(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asInt);
    }

    public int getIntOr(String $$0, int $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.intValue();
        }
        return $$1;
    }

    public Optional<Long> getLong(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asLong);
    }

    public long getLongOr(String $$0, long $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.longValue();
        }
        return $$1;
    }

    public Optional<Float> getFloat(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asFloat);
    }

    public float getFloatOr(String $$0, float $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.floatValue();
        }
        return $$1;
    }

    public Optional<Double> getDouble(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asDouble);
    }

    public double getDoubleOr(String $$0, double $$1) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)tag;
            return $$2.doubleValue();
        }
        return $$1;
    }

    public Optional<String> getString(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asString);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public String getStringOr(String $$0, String $$1) {
        Tag tag = this.tags.get($$0);
        if (!(tag instanceof StringTag)) return $$1;
        StringTag stringTag = (StringTag)tag;
        try {
            String string = stringTag.value();
            return string;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public Optional<byte[]> getByteArray(String $$0) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof ByteArrayTag) {
            ByteArrayTag $$1 = (ByteArrayTag)tag;
            return Optional.of($$1.getAsByteArray());
        }
        return Optional.empty();
    }

    public Optional<int[]> getIntArray(String $$0) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof IntArrayTag) {
            IntArrayTag $$1 = (IntArrayTag)tag;
            return Optional.of($$1.getAsIntArray());
        }
        return Optional.empty();
    }

    public Optional<long[]> getLongArray(String $$0) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof LongArrayTag) {
            LongArrayTag $$1 = (LongArrayTag)tag;
            return Optional.of($$1.getAsLongArray());
        }
        return Optional.empty();
    }

    public Optional<CompoundTag> getCompound(String $$0) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof CompoundTag) {
            CompoundTag $$1 = (CompoundTag)tag;
            return Optional.of($$1);
        }
        return Optional.empty();
    }

    public CompoundTag getCompoundOrEmpty(String $$0) {
        return this.getCompound($$0).orElseGet(CompoundTag::new);
    }

    public Optional<ListTag> getList(String $$0) {
        Tag tag = this.tags.get($$0);
        if (tag instanceof ListTag) {
            ListTag $$1 = (ListTag)tag;
            return Optional.of($$1);
        }
        return Optional.empty();
    }

    public ListTag getListOrEmpty(String $$0) {
        return this.getList($$0).orElseGet(ListTag::new);
    }

    public Optional<Boolean> getBoolean(String $$0) {
        return this.getOptional($$0).flatMap(Tag::asBoolean);
    }

    public boolean getBooleanOr(String $$0, boolean $$1) {
        return this.getByteOr($$0, $$1 ? (byte)1 : 0) != 0;
    }

    public @Nullable Tag remove(String $$0) {
        return this.tags.remove($$0);
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitCompound(this);
        return $$0.build();
    }

    public boolean isEmpty() {
        return this.tags.isEmpty();
    }

    protected CompoundTag shallowCopy() {
        return new CompoundTag(new HashMap<String, Tag>(this.tags));
    }

    @Override
    public CompoundTag copy() {
        HashMap<String, Tag> $$0 = new HashMap<String, Tag>();
        this.tags.forEach((? super K $$1, ? super V $$2) -> $$0.put((String)$$1, $$2.copy()));
        return new CompoundTag($$0);
    }

    @Override
    public Optional<CompoundTag> asCompound() {
        return Optional.of(this);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        return $$0 instanceof CompoundTag && Objects.equals(this.tags, ((CompoundTag)$$0).tags);
    }

    public int hashCode() {
        return this.tags.hashCode();
    }

    private static void writeNamedTag(String $$0, Tag $$1, DataOutput $$2) throws IOException {
        $$2.writeByte($$1.getId());
        if ($$1.getId() == 0) {
            return;
        }
        $$2.writeUTF($$0);
        $$1.write($$2);
    }

    static Tag readNamedTagData(TagType<?> $$0, String $$1, DataInput $$2, NbtAccounter $$3) {
        try {
            return $$0.load($$2, $$3);
        }
        catch (IOException $$4) {
            CrashReport $$5 = CrashReport.forThrowable($$4, "Loading NBT data");
            CrashReportCategory $$6 = $$5.addCategory("NBT Tag");
            $$6.setDetail("Tag name", $$1);
            $$6.setDetail("Tag type", $$0.getName());
            throw new ReportedNbtException($$5);
        }
    }

    public CompoundTag merge(CompoundTag $$0) {
        for (String $$1 : $$0.tags.keySet()) {
            Tag $$2 = $$0.tags.get($$1);
            if ($$2 instanceof CompoundTag) {
                CompoundTag $$3 = (CompoundTag)$$2;
                Tag tag = this.tags.get($$1);
                if (tag instanceof CompoundTag) {
                    CompoundTag $$4 = (CompoundTag)tag;
                    $$4.merge($$3);
                    continue;
                }
            }
            this.put($$1, $$2.copy());
        }
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitCompound(this);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        block14: for (Map.Entry<String, Tag> $$1 : this.tags.entrySet()) {
            Tag $$2 = $$1.getValue();
            TagType<?> $$3 = $$2.getType();
            StreamTagVisitor.EntryResult $$4 = $$0.visitEntry($$3);
            switch ($$4) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return $$0.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            $$4 = $$0.visitEntry($$3, $$1.getKey());
            switch ($$4) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return $$0.visitContainerEnd();
                }
                case SKIP: {
                    continue block14;
                }
            }
            StreamTagVisitor.ValueResult $$5 = $$2.accept($$0);
            switch ($$5) {
                case HALT: {
                    return StreamTagVisitor.ValueResult.HALT;
                }
                case BREAK: {
                    return $$0.visitContainerEnd();
                }
            }
        }
        return $$0.visitContainerEnd();
    }

    public <T> void store(String $$0, Codec<T> $$1, T $$2) {
        this.store($$0, $$1, NbtOps.INSTANCE, $$2);
    }

    public <T> void storeNullable(String $$0, Codec<T> $$1, @Nullable T $$2) {
        if ($$2 != null) {
            this.store($$0, $$1, $$2);
        }
    }

    public <T> void store(String $$0, Codec<T> $$1, DynamicOps<Tag> $$2, T $$3) {
        this.put($$0, (Tag)$$1.encodeStart($$2, $$3).getOrThrow());
    }

    public <T> void storeNullable(String $$0, Codec<T> $$1, DynamicOps<Tag> $$2, @Nullable T $$3) {
        if ($$3 != null) {
            this.store($$0, $$1, $$2, $$3);
        }
    }

    public <T> void store(MapCodec<T> $$0, T $$1) {
        this.store($$0, NbtOps.INSTANCE, $$1);
    }

    public <T> void store(MapCodec<T> $$0, DynamicOps<Tag> $$1, T $$2) {
        this.merge((CompoundTag)$$0.encoder().encodeStart($$1, $$2).getOrThrow());
    }

    public <T> Optional<T> read(String $$0, Codec<T> $$1) {
        return this.read($$0, $$1, NbtOps.INSTANCE);
    }

    public <T> Optional<T> read(String $$0, Codec<T> $$1, DynamicOps<Tag> $$22) {
        Tag $$3 = this.get($$0);
        if ($$3 == null) {
            return Optional.empty();
        }
        return $$1.parse($$22, (Object)$$3).resultOrPartial($$2 -> LOGGER.error("Failed to read field ({}={}): {}", new Object[]{$$0, $$3, $$2}));
    }

    public <T> Optional<T> read(MapCodec<T> $$0) {
        return this.read($$0, NbtOps.INSTANCE);
    }

    public <T> Optional<T> read(MapCodec<T> $$02, DynamicOps<Tag> $$1) {
        return $$02.decode($$1, (MapLike)$$1.getMap((Object)this).getOrThrow()).resultOrPartial($$0 -> LOGGER.error("Failed to read value ({}): {}", (Object)this, $$0));
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

