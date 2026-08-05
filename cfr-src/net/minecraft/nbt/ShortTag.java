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

public record ShortTag(short value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 10;
    public static final TagType<ShortTag> TYPE = new TagType.StaticSize<ShortTag>(){

        @Override
        public ShortTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return ShortTag.valueOf(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static short readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(10L);
            return $$0.readShort();
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public String getName() {
            return "SHORT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Short";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };

    public static ShortTag valueOf(short $$0) {
        if ($$0 >= -128 && $$0 <= 1024) {
            return Cache.cache[$$0 - -128];
        }
        return new ShortTag($$0);
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeShort(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 10;
    }

    @Override
    public byte getId() {
        return 2;
    }

    public TagType<ShortTag> getType() {
        return TYPE;
    }

    @Override
    public ShortTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitShort(this);
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
        return (byte)(this.value & 0xFF);
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
        $$0.visitShort(this);
        return $$0.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }

    static class Cache {
        private static final int HIGH = 1024;
        private static final int LOW = -128;
        static final ShortTag[] cache = new ShortTag[1153];

        private Cache() {
        }

        static {
            for (int $$0 = 0; $$0 < cache.length; ++$$0) {
                Cache.cache[$$0] = new ShortTag((short)(-128 + $$0));
            }
        }
    }
}

