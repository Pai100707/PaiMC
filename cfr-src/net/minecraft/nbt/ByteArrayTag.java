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
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class ByteArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>(){

        @Override
        public ByteArrayTag load(DataInput $$0, NbtAccounter $$1) throws IOException {
            return new ByteArrayTag(1.readAccounted($$0, $$1));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
            return $$1.visit(1.readAccounted($$0, $$2));
        }

        private static byte[] readAccounted(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$1.accountBytes(24L);
            int $$2 = $$0.readInt();
            $$1.accountBytes(1L, $$2);
            byte[] $$3 = new byte[$$2];
            $$0.readFully($$3);
            return $$3;
        }

        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$0.skipBytes($$0.readInt() * 1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] $$0) {
        this.data = $$0;
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
        $$0.writeInt(this.data.length);
        $$0.write(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 24 + 1 * this.data.length;
    }

    @Override
    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitByteArray(this);
        return $$0.build();
    }

    @Override
    public Tag copy() {
        byte[] $$0 = new byte[this.data.length];
        System.arraycopy(this.data, 0, $$0, 0, this.data.length);
        return new ByteArrayTag($$0);
    }

    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        return $$0 instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)$$0).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitByteArray(this);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public ByteTag get(int $$0) {
        return ByteTag.valueOf(this.data[$$0]);
    }

    @Override
    public boolean setTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data[$$0] = $$2.byteValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int $$0, Tag $$1) {
        if ($$1 instanceof NumericTag) {
            NumericTag $$2 = (NumericTag)$$1;
            this.data = ArrayUtils.add((byte[])this.data, (int)$$0, (byte)$$2.byteValue());
            return true;
        }
        return false;
    }

    @Override
    public ByteTag remove(int $$0) {
        byte $$1 = this.data[$$0];
        this.data = ArrayUtils.remove((byte[])this.data, (int)$$0);
        return ByteTag.valueOf($$1);
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    public Optional<byte[]> asByteArray() {
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
}

