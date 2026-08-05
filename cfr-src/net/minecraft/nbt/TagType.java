/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.IOException;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.Tag;

public interface TagType<T extends Tag> {
    public T load(DataInput var1, NbtAccounter var2) throws IOException;

    public StreamTagVisitor.ValueResult parse(DataInput var1, StreamTagVisitor var2, NbtAccounter var3) throws IOException;

    default public void parseRoot(DataInput $$0, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
        switch ($$1.visitRootEntry(this)) {
            case CONTINUE: {
                this.parse($$0, $$1, $$2);
                break;
            }
            case HALT: {
                break;
            }
            case BREAK: {
                this.skip($$0, $$2);
            }
        }
    }

    public void skip(DataInput var1, int var2, NbtAccounter var3) throws IOException;

    public void skip(DataInput var1, NbtAccounter var2) throws IOException;

    public String getName();

    public String getPrettyName();

    public static TagType<EndTag> createInvalid(final int $$0) {
        return new TagType<EndTag>(){

            private IOException createException() {
                return new IOException("Invalid tag id: " + $$0);
            }

            @Override
            public EndTag load(DataInput $$02, NbtAccounter $$1) throws IOException {
                throw this.createException();
            }

            @Override
            public StreamTagVisitor.ValueResult parse(DataInput $$02, StreamTagVisitor $$1, NbtAccounter $$2) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput $$02, int $$1, NbtAccounter $$2) throws IOException {
                throw this.createException();
            }

            @Override
            public void skip(DataInput $$02, NbtAccounter $$1) throws IOException {
                throw this.createException();
            }

            @Override
            public String getName() {
                return "INVALID[" + $$0 + "]";
            }

            @Override
            public String getPrettyName() {
                return "UNKNOWN_" + $$0;
            }

            @Override
            public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
                return this.load(dataInput, nbtAccounter);
            }
        };
    }

    public static interface VariableSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput $$0, int $$1, NbtAccounter $$2) throws IOException {
            for (int $$3 = 0; $$3 < $$1; ++$$3) {
                this.skip($$0, $$2);
            }
        }
    }

    public static interface StaticSize<T extends Tag>
    extends TagType<T> {
        @Override
        default public void skip(DataInput $$0, NbtAccounter $$1) throws IOException {
            $$0.skipBytes(this.size());
        }

        @Override
        default public void skip(DataInput $$0, int $$1, NbtAccounter $$2) throws IOException {
            $$0.skipBytes(this.size() * $$1);
        }

        public int size();
    }
}

