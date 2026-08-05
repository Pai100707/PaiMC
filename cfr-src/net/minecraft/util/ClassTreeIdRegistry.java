/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.util;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.Util;

public class ClassTreeIdRegistry {
    public static final int NO_ID_VALUE = -1;
    private final Object2IntMap<Class<?>> classToLastIdCache = (Object2IntMap)Util.make(new Object2IntOpenHashMap(), $$0 -> $$0.defaultReturnValue(-1));

    public int getLastIdFor(Class<?> $$0) {
        int $$1 = this.classToLastIdCache.getInt($$0);
        if ($$1 != -1) {
            return $$1;
        }
        Class<?> $$2 = $$0;
        while (($$2 = $$2.getSuperclass()) != Object.class) {
            int $$3 = this.classToLastIdCache.getInt($$2);
            if ($$3 == -1) continue;
            return $$3;
        }
        return -1;
    }

    public int getCount(Class<?> $$0) {
        return this.getLastIdFor($$0) + 1;
    }

    public int define(Class<?> $$0) {
        int $$1 = this.getLastIdFor($$0);
        int $$2 = $$1 == -1 ? 0 : $$1 + 1;
        this.classToLastIdCache.put($$0, $$2);
        return $$2;
    }
}

