/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.core;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

public interface HolderLookup<T>
extends HolderGetter<T> {
    public Stream<Holder.Reference<T>> listElements();

    default public Stream<ResourceKey<T>> listElementIds() {
        return this.listElements().map(Holder.Reference::key);
    }

    public Stream<HolderSet.Named<T>> listTags();

    default public Stream<TagKey<T>> listTagIds() {
        return this.listTags().map(HolderSet.Named::key);
    }

    public static interface Provider
    extends HolderGetter.Provider {
        public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys();

        default public Stream<RegistryLookup<?>> listRegistries() {
            return this.listRegistryKeys().map(resourceKey -> this.lookupOrThrow((ResourceKey)resourceKey));
        }

        public <T> Optional<? extends RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> var1);

        default public <T> RegistryLookup<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> $$0) {
            return this.lookup($$0).orElseThrow(() -> new IllegalStateException("Registry " + String.valueOf($$0.identifier()) + " not found"));
        }

        default public <V> RegistryOps<V> createSerializationContext(DynamicOps<V> $$0) {
            return RegistryOps.create($$0, this);
        }

        public static Provider create(Stream<RegistryLookup<?>> $$02) {
            final Map<ResourceKey, RegistryLookup> $$1 = $$02.collect(Collectors.toUnmodifiableMap(RegistryLookup::key, $$0 -> $$0));
            return new Provider(){

                @Override
                public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                    return $$1.keySet().stream();
                }

                public <T> Optional<RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
                    return Optional.ofNullable((RegistryLookup)$$1.get($$0));
                }
            };
        }

        default public Lifecycle allRegistriesLifecycle() {
            return this.listRegistries().map(RegistryLookup::registryLifecycle).reduce(Lifecycle.stable(), Lifecycle::add);
        }
    }

    public static interface RegistryLookup<T>
    extends HolderLookup<T>,
    HolderOwner<T> {
        public ResourceKey<? extends Registry<? extends T>> key();

        public Lifecycle registryLifecycle();

        default public RegistryLookup<T> filterFeatures(FeatureFlagSet $$0) {
            if (FeatureElement.FILTERED_REGISTRIES.contains(this.key())) {
                return this.filterElements($$1 -> ((FeatureElement)$$1).isEnabled($$0));
            }
            return this;
        }

        default public RegistryLookup<T> filterElements(final Predicate<T> $$0) {
            return new Delegate<T>(){

                @Override
                public RegistryLookup<T> parent() {
                    return this;
                }

                @Override
                public Optional<Holder.Reference<T>> get(ResourceKey<T> $$02) {
                    return this.parent().get($$02).filter($$1 -> $$0.test($$1.value()));
                }

                @Override
                public Stream<Holder.Reference<T>> listElements() {
                    return this.parent().listElements().filter($$1 -> $$0.test($$1.value()));
                }
            };
        }

        public static interface Delegate<T>
        extends RegistryLookup<T> {
            public RegistryLookup<T> parent();

            @Override
            default public ResourceKey<? extends Registry<? extends T>> key() {
                return this.parent().key();
            }

            @Override
            default public Lifecycle registryLifecycle() {
                return this.parent().registryLifecycle();
            }

            @Override
            default public Optional<Holder.Reference<T>> get(ResourceKey<T> $$0) {
                return this.parent().get($$0);
            }

            @Override
            default public Stream<Holder.Reference<T>> listElements() {
                return this.parent().listElements();
            }

            @Override
            default public Optional<HolderSet.Named<T>> get(TagKey<T> $$0) {
                return this.parent().get($$0);
            }

            @Override
            default public Stream<HolderSet.Named<T>> listTags() {
                return this.parent().listTags();
            }
        }
    }
}

