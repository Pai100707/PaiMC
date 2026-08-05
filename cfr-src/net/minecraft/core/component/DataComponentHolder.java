/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public interface DataComponentHolder
extends DataComponentGetter {
    public DataComponentMap getComponents();

    @Override
    default public <T> @Nullable T get(DataComponentType<? extends T> $$0) {
        return this.getComponents().get($$0);
    }

    default public <T> Stream<T> getAllOfType(Class<? extends T> $$02) {
        return this.getComponents().stream().map(TypedDataComponent::value).filter($$1 -> $$02.isAssignableFrom($$1.getClass())).map($$0 -> $$0);
    }

    @Override
    default public <T> T getOrDefault(DataComponentType<? extends T> $$0, T $$1) {
        return this.getComponents().getOrDefault($$0, $$1);
    }

    default public boolean has(DataComponentType<?> $$0) {
        return this.getComponents().has($$0);
    }
}

