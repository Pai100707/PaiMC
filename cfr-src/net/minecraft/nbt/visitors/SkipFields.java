/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.FieldTree;

public class SkipFields
extends CollectToTag {
    private final Deque<FieldTree> stack = new ArrayDeque<FieldTree>();

    public SkipFields(FieldSelector ... $$0) {
        FieldTree $$1 = FieldTree.createRoot();
        for (FieldSelector $$2 : $$0) {
            $$1.addEntry($$2);
        }
        this.stack.push($$1);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0, String $$1) {
        FieldTree $$3;
        FieldTree $$2 = this.stack.element();
        if ($$2.isSelected($$0, $$1)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        if ($$0 == CompoundTag.TYPE && ($$3 = $$2.fieldsToRecurse().get($$1)) != null) {
            this.stack.push($$3);
        }
        return super.visitEntry($$0, $$1);
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }
}

