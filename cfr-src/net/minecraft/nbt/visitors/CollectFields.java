/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 */
package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.FieldTree;

public class CollectFields
extends CollectToTag {
    private int fieldsToGetCount;
    private final Set<TagType<?>> wantedTypes;
    private final Deque<FieldTree> stack = new ArrayDeque<FieldTree>();

    public CollectFields(FieldSelector ... $$0) {
        this.fieldsToGetCount = $$0.length;
        ImmutableSet.Builder $$1 = ImmutableSet.builder();
        FieldTree $$2 = FieldTree.createRoot();
        for (FieldSelector $$3 : $$0) {
            $$2.addEntry($$3);
            $$1.add($$3.type());
        }
        this.stack.push($$2);
        $$1.add(CompoundTag.TYPE);
        this.wantedTypes = $$1.build();
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> $$0) {
        if ($$0 != CompoundTag.TYPE) {
            return StreamTagVisitor.ValueResult.HALT;
        }
        return super.visitRootEntry($$0);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0) {
        FieldTree $$1 = this.stack.element();
        if (this.depth() > $$1.depth()) {
            return super.visitEntry($$0);
        }
        if (this.fieldsToGetCount <= 0) {
            return StreamTagVisitor.EntryResult.BREAK;
        }
        if (!this.wantedTypes.contains($$0)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        return super.visitEntry($$0);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0, String $$1) {
        FieldTree $$3;
        FieldTree $$2 = this.stack.element();
        if (this.depth() > $$2.depth()) {
            return super.visitEntry($$0, $$1);
        }
        if ($$2.selectedFields().remove($$1, $$0)) {
            --this.fieldsToGetCount;
            return super.visitEntry($$0, $$1);
        }
        if ($$0 == CompoundTag.TYPE && ($$3 = $$2.fieldsToRecurse().get($$1)) != null) {
            this.stack.push($$3);
            return super.visitEntry($$0, $$1);
        }
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }
}

