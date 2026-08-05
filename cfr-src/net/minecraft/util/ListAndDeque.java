/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util;

import java.io.Serializable;
import java.util.Deque;
import java.util.List;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import org.jspecify.annotations.Nullable;

public interface ListAndDeque<T>
extends Serializable,
Cloneable,
Deque<T>,
List<T>,
RandomAccess {
    @Override
    public ListAndDeque<T> reversed();

    @Override
    public T getFirst();

    @Override
    public T getLast();

    @Override
    public void addFirst(T var1);

    @Override
    public void addLast(T var1);

    @Override
    public T removeFirst();

    @Override
    public T removeLast();

    @Override
    default public boolean offer(T $$0) {
        return this.offerLast($$0);
    }

    @Override
    default public T remove() {
        return this.removeFirst();
    }

    @Override
    default public @Nullable T poll() {
        return (T)this.pollFirst();
    }

    @Override
    default public T element() {
        return this.getFirst();
    }

    @Override
    default public @Nullable T peek() {
        return (T)this.peekFirst();
    }

    @Override
    default public void push(T $$0) {
        this.addFirst($$0);
    }

    @Override
    default public T pop() {
        return this.removeFirst();
    }

    @Override
    default public /* synthetic */ List reversed() {
        return this.reversed();
    }

    @Override
    default public /* synthetic */ SequencedCollection reversed() {
        return this.reversed();
    }

    @Override
    default public /* synthetic */ Deque reversed() {
        return this.reversed();
    }
}

