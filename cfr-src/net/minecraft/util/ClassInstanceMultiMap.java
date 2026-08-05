/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Util;

public class ClassInstanceMultiMap<T>
extends AbstractCollection<T> {
    private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
    private final Class<T> baseClass;
    private final List<T> allInstances = Lists.newArrayList();

    public ClassInstanceMultiMap(Class<T> $$0) {
        this.baseClass = $$0;
        this.byClass.put($$0, this.allInstances);
    }

    @Override
    public boolean add(T $$0) {
        boolean $$1 = false;
        for (Map.Entry<Class<?>, List<T>> $$2 : this.byClass.entrySet()) {
            if (!$$2.getKey().isInstance($$0)) continue;
            $$1 |= $$2.getValue().add($$0);
        }
        return $$1;
    }

    @Override
    public boolean remove(Object $$0) {
        boolean $$1 = false;
        for (Map.Entry<Class<?>, List<T>> $$2 : this.byClass.entrySet()) {
            if (!$$2.getKey().isInstance($$0)) continue;
            List<T> $$3 = $$2.getValue();
            $$1 |= $$3.remove($$0);
        }
        return $$1;
    }

    @Override
    public boolean contains(Object $$0) {
        return this.find($$0.getClass()).contains($$0);
    }

    public <S> Collection<S> find(Class<S> $$02) {
        if (!this.baseClass.isAssignableFrom($$02)) {
            throw new IllegalArgumentException("Don't know how to search for " + String.valueOf($$02));
        }
        List $$1 = this.byClass.computeIfAbsent($$02, $$0 -> this.allInstances.stream().filter($$0::isInstance).collect(Util.toMutableList()));
        return Collections.unmodifiableCollection($$1);
    }

    @Override
    public Iterator<T> iterator() {
        if (this.allInstances.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Iterators.unmodifiableIterator(this.allInstances.iterator());
    }

    public List<T> getAllInstances() {
        return ImmutableList.copyOf(this.allInstances);
    }

    @Override
    public int size() {
        return this.allInstances.size();
    }
}

