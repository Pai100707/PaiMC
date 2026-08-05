/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public interface SkipAll
extends StreamTagVisitor {
    public static final SkipAll INSTANCE = new SkipAll(){};

    @Override
    default public StreamTagVisitor.ValueResult visitEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(String $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(byte $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(short $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(int $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(long $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(float $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(double $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(byte[] $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(int[] $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visit(long[] $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitList(TagType<?> $$0, int $$1) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitElement(TagType<?> $$0, int $$1) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.EntryResult visitEntry(TagType<?> $$0, String $$1) {
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitContainerEnd() {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    default public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> $$0) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }
}

