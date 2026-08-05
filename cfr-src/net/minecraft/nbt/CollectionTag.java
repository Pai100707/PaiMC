/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;

public sealed interface CollectionTag
extends Iterable<Tag>,
Tag
permits ListTag, ByteArrayTag, IntArrayTag, LongArrayTag {
    public void clear();

    public boolean setTag(int var1, Tag var2);

    public boolean addTag(int var1, Tag var2);

    public Tag remove(int var1);

    public Tag get(int var1);

    public int size();

    default public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    default public Iterator<Tag> iterator() {
        return new Iterator<Tag>(){
            private int index;

            @Override
            public boolean hasNext() {
                return this.index < CollectionTag.this.size();
            }

            @Override
            public Tag next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return CollectionTag.this.get(this.index++);
            }

            @Override
            public /* synthetic */ Object next() {
                return this.next();
            }
        };
    }

    default public Stream<Tag> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}

