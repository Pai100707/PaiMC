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

public record DoubleTag(double value) implements NumericTag
{
    private static final int SELF_SIZE_IN_BYTES = 16;
    public static final DoubleTag ZERO = new DoubleTag(0.0);
    public static final TagType<DoubleTag> TYPE = new TagType.StaticSize<DoubleTag>(){

        @Override
        public DoubleTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return DoubleTag.valueOf(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static double readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(16L);
            return $$0.readDouble();
        }

        @Override
        public int size() {
            return 8;
        }

        @Override
        public String getName() {
            return "DOUBLE";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Double";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };

    public static DoubleTag valueOf(double $$0) {
        if ($$0 == 0.0) {
            return ZERO;
        }
        return new DoubleTag($$0);
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeDouble(this.value);
    }

    @Override
    public int sizeInBytes() {
        return 16;
    }

    @Override
    public byte getId() {
        return 6;
    }

    public TagType<DoubleTag> getType() {
        return TYPE;
    }

    @Override
    public DoubleTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitDouble(this);
    }

    @Override
    public long longValue() {
        return (long)Math.floor(this.value);
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
        return (float)this.value;
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
        $$0.visitDouble(this);
        return $$0.build();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

