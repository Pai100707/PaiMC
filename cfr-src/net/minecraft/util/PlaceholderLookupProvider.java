/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RegistryContextSwapper;

public class PlaceholderLookupProvider
implements HolderGetter.Provider {
    final HolderLookup.Provider context;
    final UniversalLookup lookup = new UniversalLookup();
    final Map<ResourceKey<Object>, Holder.Reference<Object>> holders = new HashMap<ResourceKey<Object>, Holder.Reference<Object>>();
    final Map<TagKey<Object>, HolderSet.Named<Object>> holderSets = new HashMap<TagKey<Object>, HolderSet.Named<Object>>();

    public PlaceholderLookupProvider(HolderLookup.Provider $$0) {
        this.context = $$0;
    }

    @Override
    public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
        return Optional.of(this.lookup.castAsLookup());
    }

    public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
        return RegistryOps.create($$0, new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
                return PlaceholderLookupProvider.this.context.lookup($$0).map(RegistryOps.RegistryInfo::fromRegistryLookup).or(() -> Optional.of(new RegistryOps.RegistryInfo(PlaceholderLookupProvider.this.lookup.castAsOwner(), PlaceholderLookupProvider.this.lookup.castAsLookup(), Lifecycle.experimental())));
            }
        });
    }

    public RegistryContextSwapper createSwapper() {
        return new RegistryContextSwapper(){

            @Override
            public <T> DataResult<T> swapTo(Codec<T> $$0, T $$1, HolderLookup.Provider $$22) {
                return $$0.encodeStart(PlaceholderLookupProvider.this.createSerializationContext(JavaOps.INSTANCE), $$1).flatMap($$2 -> $$0.parse($$22.createSerializationContext(JavaOps.INSTANCE), $$2));
            }
        };
    }

    public boolean hasRegisteredPlaceholders() {
        return !this.holders.isEmpty() || !this.holderSets.isEmpty();
    }

    class UniversalLookup
    implements HolderGetter<Object>,
    HolderOwner<Object> {
        UniversalLookup() {
        }

        @Override
        public Optional<Holder.Reference<Object>> get(ResourceKey<Object> $$0) {
            return Optional.of(this.getOrCreate($$0));
        }

        @Override
        public Holder.Reference<Object> getOrThrow(ResourceKey<Object> $$0) {
            return this.getOrCreate($$0);
        }

        private Holder.Reference<Object> getOrCreate(ResourceKey<Object> $$02) {
            return PlaceholderLookupProvider.this.holders.computeIfAbsent($$02, $$0 -> Holder.Reference.createStandAlone(this, $$0));
        }

        @Override
        public Optional<HolderSet.Named<Object>> get(TagKey<Object> $$0) {
            return Optional.of(this.getOrCreate($$0));
        }

        @Override
        public HolderSet.Named<Object> getOrThrow(TagKey<Object> $$0) {
            return this.getOrCreate($$0);
        }

        private HolderSet.Named<Object> getOrCreate(TagKey<Object> $$02) {
            return PlaceholderLookupProvider.this.holderSets.computeIfAbsent($$02, $$0 -> HolderSet.emptyNamed(this, $$0));
        }

        public <T> HolderGetter<T> castAsLookup() {
            return this;
        }

        public <T> HolderOwner<T> castAsOwner() {
            return this;
        }
    }
}

