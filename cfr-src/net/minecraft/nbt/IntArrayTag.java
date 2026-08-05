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
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class IntArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>(){

        @Override
        public IntArrayTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return new IntArrayTag(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static int[] readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(24L);
            int $$2 = $$0.readInt();
            $$1.accountBytes(4L, $$2);
            int[] $$3 = new int[$$2];
            for (int $$4 = 0; $$4 < $$2; ++$$4) {
                $$3[$$4] = $$0.readInt();
            }
            return $$3;
        }

        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$0.skipBytes($$0.readInt() * 4);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private int[] data;

    public IntArrayTag(int[] $$0) {
        this.data = $$0;
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeInt(this.data.length);
        for (int $$1 : this.data) {
            $$0.writeInt($$1);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 4 * this.data.length;
    }

    @Override
    public byte getId() {
        return 11;
    }

    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitIntArray(this);
        return $$0.build();
    }

    @Override
    public IntArrayTag copy() {
        int[] $$0 = new int[this.data.length];
        System.arraycopy(this.data, 0, $$0, 0, this.data.length);
        return new IntArrayTag($$0);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        return $$0 instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)$$0).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitIntArray(this);
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public IntTag get(int $$0) {
        return IntTag.valueOf(this.data[$$0]);
    }

    @Override
    public boolean setTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data[$$0] = $$2.intValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data = ArrayUtils.add((int[])this.data, (int)$$0, (int)$$2.intValue());
            return true;
        }
        return false;
    }

    @Override
    public IntTag remove(int $$0) {
        int $$1 = this.data[$$0];
        this.data = ArrayUtils.remove((int[])this.data, (int)$$0);
        return IntTag.valueOf($$1);
    }

    @Override
    public void clear() {
        this.data = new int[0];
    }

    @Override
    public Optional<int[]> asIntArray() {
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

