/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class LongArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>(){

        @Override
        public LongArrayTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return new LongArrayTag(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static long[] readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(24L);
            int $$2 = $$0.readInt();
            $$1.accountBytes(8L, $$2);
            long[] $$3 = new long[$$2];
            for (int $$4 = 0; $$4 < $$2; ++$$4) {
                $$3[$$4] = $$0.readLong();
            }
            return $$3;
        }

        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$0.skipBytes($$0.readInt() * 8);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private long[] data;

    public LongArrayTag(long[] $$0) {
        this.data = $$0;
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeInt(this.data.length);
        for (long $$1 : this.data) {
            $$0.writeLong($$1);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 8 * this.data.length;
    }

    @Override
    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitLongArray(this);
        return $$0.build();
    }

    @Override
    public LongArrayTag copy() {
        long[] $$0 = new long[this.data.length];
        System.arraycopy(this.data, 0, $$0, 0, this.data.length);
        return new LongArrayTag($$0);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        return $$0 instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)$$0).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitLongArray(this);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public LongTag get(int $$0) {
        return LongTag.valueOf(this.data[$$0]);
    }

    @Override
    public boolean setTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data[$$0] = $$2.longValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data = ArrayUtils.add((long[])this.data, (int)$$0, (long)$$2.longValue());
            return true;
        }
        return false;
    }

    @Override
    public LongTag remove(int $$0) {
        long $$1 = this.data[$$0];
        this.data = ArrayUtils.remove((long[])this.data, (int)$$0);
        return LongTag.valueOf($$1);
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public Optional<long[]> asLongArray() {
        return Optional.of(this.data);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        return $$0.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag get(int n) {
        return this.get(n);
    }

    @Override
    public /* synthetic */ Tag remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

