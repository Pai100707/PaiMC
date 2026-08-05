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
import net.minecraft.util.Mth;

public record FloatTag(float value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 12;
    public static final FloatTag ZERO = new FloatTag(0.0f);
    public static final TagType<FloatTag> TYPE = new TagType.StaticSize<FloatTag>(){

        @Override
        public FloatTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return FloatTag.valueOf(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static float readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(12L);
            return $$0.readFloat();
        }

        @Override
        public int size() {
            return 4;
        }

        @Override
        public String getName() {
            return "FLOAT";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Float";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };

    public static FloatTag valueOf(float $$0) {
        if ($$0 == 0.0f) {
            return ZERO;
        }
        return new FloatTag($$0);
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeFloat(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 12;
    }

    @Override
    public byte getId() {
        return 5;
    }

    public TagType<FloatTag> getType() {
        return TYPE;
    }

    @Override
    public FloatTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitFloat(this);
    }

    @Override
    public long longValue() {
        return (long)this.value;
    }

    @Override
    public int intValue() {
        return Mth.floor(this.value);
    }

    @Override
    public short shortValue() {
        return (short)(Mth.floor(this.value) & 0xFFFF);
    }

    @Override
    public byte byteValue() {
        return (byte)(Mth.floor(this.value) & 0xFF);
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
        return Float.valueOf(this.value);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        return $$0.visit(this.value);
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitFloat(this);
        return $$0.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

