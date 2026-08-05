/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public interface DataComponentGetter {
    public <T> @Nullable T get(DataComponentType<? extends T> var1);

    default public <T> T getOrDefault(DataComponentType<? extends T> $$0, T $$1) {
        T $$2 = this.get($$0);
        return $$2 != null ? $$2 : $$1;
    }

    default public <T> @Nullable TypedDataComponent<T> getTyped(DataComponentType<T> $$0) {
        T $$1 = this.get($$0);
        return $$1 != null ? new TypedDataComponent<T>($$0, $$1) : null;
    }
}

