/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public record ByteTag(byte value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 9;
    public static final TagType<ByteTag> TYPE = new TagType.StaticSize<ByteTag>(){

        @Override
        public ByteTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return ByteTag.valueOf(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static byte readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(9L);
            return $$0.readByte();
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public String getName() {
            return "BYTE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    public static final ByteTag ZERO = ByteTag.valueOf((byte)0);
    public static final ByteTag ONE = ByteTag.valueOf((byte)1);

    public static ByteTag valueOf(byte $$0) {
        return Cache.cache[128 + $$0];
    }

    public static ByteTag valueOf(boolean $$0) {
        return $$0 ? ONE : ZERO;
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeByte(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 9;
    }

    @Override
    public byte getId() {
        return 1;
    }

    public TagType<ByteTag> getType() {
        return TYPE;
    }

    @Override
    public ByteTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitByte(this);
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public short shortValue() {
        return this.value;
    }

    @Override
    public byte byteValue() {
        return this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public Number box() {
        return this.value;
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        return $$0.visit(this.value);
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitByte(this);
        return $$0.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    static class Cache {
        static final ByteTag[] cache = new ByteTag[256];

        private Cache() {
        }

        static {
            for (int $$0 = 0; $$0 < cache.length; ++$$0) {
                Cache.cache[$$0] = new ByteTag((byte)($$0 - 128));
            }
        }
    }
}

