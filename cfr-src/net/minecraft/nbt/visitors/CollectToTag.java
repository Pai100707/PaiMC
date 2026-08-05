/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.jspecify.annotations.Nullable;

public class CollectToTag
implements StreamTagVisitor {
    private final Deque<ContainerBuilder> containerStack = new ArrayDeque<ContainerBuilder>();

    public CollectToTag() {
        this.containerStack.addLast(new RootBuilder());
    }

    public @Nullable Tag getResult() {
        return this.containerStack.getFirst().build();
    }

    protected int depth() {
        return this.containerStack.size() - 1;
    }

    private void appendEntry(Tag $$0) {
        this.containerStack.getLast().acceptValue($$0);
    }

    @Override
    public StreamTagVisitor.ValueResult visitEnd() {
        this.appendEntry(EndTag.INSTANCE);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(String $$0) {
        this.appendEntry(StringTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte $$0) {
        this.appendEntry(ByteTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(short $$0) {
        this.appendEntry(ShortTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int $$0) {
        this.appendEntry(IntTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long $$0) {
        this.appendEntry(LongTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(float $$0) {
        this.appendEntry(FloatTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(double $$0) {
        this.appendEntry(DoubleTag.valueOf($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte[] $$0) {
        this.appendEntry(new ByteArrayTag($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int[] $$0) {
        this.appendEntry(new IntArrayTag($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long[] $$0) {
        this.appendEntry(new LongArrayTag($$0));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitList(TagType<?> $$0, int $$1) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.EntryResult visitElement(TagType<?> $$0, int $$1) {
        this.enterContainerIfNeeded($$0);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0) {
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0, String $$1) {
        this.containerStack.getLast().acceptKey($$1);
        this.enterContainerIfNeeded($$0);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    private void enterContainerIfNeeded(TagType<?> $$0) {
        if ($$0 == ListTag.TYPE) {
            this.containerStack.addLast(new ListBuilder());
        } else if ($$0 == CompoundTag.TYPE) {
            this.containerStack.addLast(new CompoundBuilder());
        }
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        ContainerBuilder $$0 = this.containerStack.removeLast();
        Tag $$1 = $$0.build();
        if ($$1 != null) {
            this.containerStack.getLast().acceptValue($$1);
        }
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> $$0) {
        this.enterContainerIfNeeded($$0);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    static class RootBuilder
    implements ContainerBuilder {
        private @Nullable Tag result;

        RootBuilder() {
        }

        @Override
        public void acceptValue(Tag $$0) {
            this.result = $$0;
        }

        @Override
        public @Nullable Tag build() {
            return this.result;
        }
    }

    static interface ContainerBuilder {
        default public void acceptKey(String $$0) {
        }

        public void acceptValue(Tag var1);

        public @Nullable Tag build();
    }

    static class ListBuilder
    implements ContainerBuilder {
        private final ListTag list = new ListTag();

        ListBuilder() {
        }

        @Override
        public void acceptValue(Tag $$0) {
            this.list.addAndUnwrap($$0);
        }

        @Override
        public Tag build() {
            return this.list;
        }
    }

    static class CompoundBuilder
    implements ContainerBuilder {
        private final CompoundTag compound = new CompoundTag();
        private String lastId = "";

        CompoundBuilder() {
        }

        @Override
        public void acceptKey(String $$0) {
            this.lastId = $$0;
        }

        @Override
        public void acceptValue(Tag $$0) {
            this.compound.put(this.lastId, $$0);
        }

        @Override
        public Tag build() {
            return this.compound;
        }
    }
}

