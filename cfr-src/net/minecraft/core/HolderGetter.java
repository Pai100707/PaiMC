/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

public interface HolderGetter<T> {
    public Optional<Holder.Reference<T>> get(ResourceKey<T> var1);

    default public Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
        return this.get($$0).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf($$0)));
    }

    public Optional<HolderSet.Named<T>> get(TagKey<T> var1);

    default public HolderSet.Named<T> getOrThrow(TagKey<T> $$0) {
        return this.get($$0).orElseThrow(() -> new IllegalStateException("Missing tag " + String.valueOf($$0)));
    }

    default public Optional<Holder<T>> getRandomElementOf(TagKey<T> $$0, RandomSource $$12) {
        return this.get($$0).flatMap($$1 -> $$1.getRandomElement($$12));
    }

    public static interface Provider {
        public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default public <T> HolderGetter<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> $$0) {
            return this.lookup($$0).orElseThrow(() -> new IllegalStateException("Registry " + String.valueOf($$0.identifier()) + " not found"));
        }

        default public <T> Optional<Holder.Reference<T>> get(ResourceKey<T> $$0) {
            return this.lookup($$0.registryKey()).flatMap($$1 -> $$1.get($$0));
        }

        default public <T> Holder.Reference<T> getOrThrow(ResourceKey<T> $$0) {
            return (Holder.Reference)this.lookup($$0.registryKey()).flatMap($$1 -> $$1.get($$0)).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf($$0)));
        }
    }
}

