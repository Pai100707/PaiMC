/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;

public final class EndTag
implements Tag {
    private static final int SELF_SIZE_IN_BYTES = 8;
    public static final TagType<EndTag> TYPE = new TagType<EndTag>(){

        @Override
        public EndTag load(DataInput $$0, NbtAccounter $$1) {
            $$1.accountBytes(8L);
            return INSTANCE;
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) {
            $$2.accountBytes(8L);
            return $$1.visitEnd();
        }

        @Override
        public void skip(DataInput $$0, int $$1, NbtAccounter $$2) {
        }

        @Override
        public void skip(DataInput $$0, NbtAccounter $$1) {
        }

        @Override
        public String getName() {
            return "END";
        }

        @Override
        public String getPrettyName() {
            return "TAG_End";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    public static final EndTag INSTANCE = new EndTag();

    private EndTag() {
    }

    @Override
    public void write(DataOutput $$0) throws IOException {
    }

    @Override
    public int sizeInBytes() {
        return 8;
    }

    @Override
    public byte getId() {
        return 0;
    }

    public TagType<EndTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor $$0 = new StringTagVisitor();
        $$0.visitEnd(this);
        return $$0.build();
    }

    @Override
    public EndTag copy() {
        return this;
    }

    @Override
    public void accept(TagVisitor $$0) {
        $$0.visitEnd(this);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor $$0) {
        return $$0.visitEnd();
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

