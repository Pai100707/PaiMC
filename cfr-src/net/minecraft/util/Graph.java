/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class Graph {
    private Graph() {
    }

    public static <T> boolean depthFirstSearch(Map<T, Set<T>> $$0, Set<T> $$1, Set<T> $$2, Consumer<T> $$3, T $$4) {
        if ($$1.contains($$4)) {
            return false;
        }
        if ($$2.contains($$4)) {
            return true;
        }
        $$2.add($$4);
        for (Object $$5 : (Set)$$0.getOrDefault($$4, (Set<T>)ImmutableSet.of())) {
            if (!Graph.depthFirstSearch($$0, $$1, $$2, $$3, $$5)) continue;
            return true;
        }
        $$2.remove($$4);
        $$1.add($$4);
        $$3.accept($$4);
        return false;
    }
}

