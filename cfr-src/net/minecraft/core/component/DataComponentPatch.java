/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import org.jspecify.annotations.Nullable;

public final class DataComponentPatch {
    public static final DataComponentPatch EMPTY = new DataComponentPatch(Reference2ObjectMaps.emptyMap());
    public static final Codec<DataComponentPatch> CODEC = Codec.dispatchedMap(PatchKey.CODEC, PatchKey::valueCodec).xmap($$0 -> {
        if ($$0.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap $$1 = new Reference2ObjectArrayMap($$0.size());
        for (Map.Entry $$2 : $$0.entrySet()) {
            PatchKey $$3 = (PatchKey)$$2.getKey();
            if ($$3.removed()) {
                $$1.put($$3.type(), Optional.empty());
                continue;
            }
            $$1.put($$3.type(), Optional.of($$2.getValue()));
        }
        return new DataComponentPatch((Reference2ObjectMap<DataComponentType<?>, Optional<?>>)$$1);
    }, $$0 -> {
        Reference2ObjectArrayMap $$1 = new Reference2ObjectArrayMap($$0.map.size());
        for (Map.Entry $$2 : Reference2ObjectMaps.fastIterable($$0.map)) {
            DataComponentType $$3 = (DataComponentType)$$2.getKey();
            if ($$3.isTransient()) continue;
            Optional $$4 = (Optional)$$2.getValue();
            if ($$4.isPresent()) {
                $$1.put((Object)new PatchKey($$3, false), $$4.get());
                continue;
            }
            $$1.put((Object)new PatchKey($$3, true), (Object)Unit.INSTANCE);
        }
        return $$1;
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> STREAM_CODEC = DataComponentPatch.createStreamCodec(new CodecGetter(){

        public <T> StreamCodec<RegistryFriendlyByteBuf, T> apply(DataComponentType<T> $$0) {
            return $$0.streamCodec().cast();
        }
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> DELIMITED_STREAM_CODEC = DataComponentPatch.createStreamCodec(new CodecGetter(){

        public <T> StreamCodec<RegistryFriendlyByteBuf, T> apply(DataComponentType<T> $$0) {
            StreamCodec $$1 = $$0.streamCodec().cast();
            return $$1.apply(ByteBufCodecs.registryFriendlyLengthPrefixed(Integer.MAX_VALUE));
        }
    });
    private static final String REMOVED_PREFIX = "!";
    final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map;

    private static StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch> createStreamCodec(final CodecGetter $$0) {
        return new StreamCodec<RegistryFriendlyByteBuf, DataComponentPatch>(){

            @Override
            public DataComponentPatch decode(RegistryFriendlyByteBuf $$02) {
                int $$1 = $$02.readVarInt();
                int $$2 = $$02.readVarInt();
                if ($$1 == 0 && $$2 == 0) {
                    return EMPTY;
                }
                int $$3 = $$1 + $$2;
                Reference2ObjectArrayMap $$4 = new Reference2ObjectArrayMap(Math.min($$3, 65536));
                for (int $$5 = 0; $$5 < $$1; ++$$5) {
                    DataComponentType $$6 = (DataComponentType)DataComponentType.STREAM_CODEC.decode($$02);
                    Object $$7 = $$0.apply($$6).decode($$02);
                    $$4.put((Object)$$6, Optional.of($$7));
                }
                for (int $$8 = 0; $$8 < $$2; ++$$8) {
                    DataComponentType $$9 = (DataComponentType)DataComponentType.STREAM_CODEC.decode($$02);
                    $$4.put((Object)$$9, Optional.empty());
                }
                return new DataComponentPatch((Reference2ObjectMap<DataComponentType<?>, Optional<?>>)$$4);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf $$02, DataComponentPatch $$1) {
                if ($$1.isEmpty()) {
                    $$02.writeVarInt(0);
                    $$02.writeVarInt(0);
                    return;
                }
                int $$2 = 0;
                int $$3 = 0;
                for (Reference2ObjectMap.Entry $$4 : Reference2ObjectMaps.fastIterable($$1.map)) {
                    if (((Optional)$$4.getValue()).isPresent()) {
                        ++$$2;
                        continue;
                    }
                    ++$$3;
                }
                $$02.writeVarInt($$2);
                $$02.writeVarInt($$3);
                for (Reference2ObjectMap.Entry $$5 : Reference2ObjectMaps.fastIterable($$1.map)) {
                    Optional $$6 = (Optional)$$5.getValue();
                    if (!$$6.isPresent()) continue;
                    DataComponentType $$7 = (DataComponentType)$$5.getKey();
                    DataComponentType.STREAM_CODEC.encode($$02, $$7);
                    this.encodeComponent($$02, $$7, $$6.get());
                }
                for (Reference2ObjectMap.Entry $$8 : Reference2ObjectMaps.fastIterable($$1.map)) {
                    if (!((Optional)$$8.getValue()).isEmpty()) continue;
                    DataComponentType $$9 = (DataComponentType)$$8.getKey();
                    DataComponentType.STREAM_CODEC.encode($$02, $$9);
                }
            }

            private <T> void encodeComponent(RegistryFriendlyByteBuf $$02, DataComponentType<T> $$1, Object $$2) {
                $$0.apply($$1).encode($$02, $$2);
            }

            @Override
            public /* synthetic */ void encode(Object object, Object object2) {
                this.encode((RegistryFriendlyByteBuf)((Object)object), (DataComponentPatch)object2);
            }

            @Override
            public /* synthetic */ Object decode(Object object) {
                return this.decode((RegistryFriendlyByteBuf)((Object)object));
            }
        };
    }

    DataComponentPatch(Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$0) {
        this.map = $$0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public <T> @Nullable Optional<? extends T> get(DataComponentType<? extends T> $$0) {
        return (Optional)this.map.get($$0);
    }

    public Set<Map.Entry<DataComponentType<?>, Optional<?>>> entrySet() {
        return this.map.entrySet();
    }

    public int size() {
        return this.map.size();
    }

    public DataComponentPatch forget(Predicate<DataComponentType<?>> $$0) {
        if (this.isEmpty()) {
            return EMPTY;
        }
        Reference2ObjectArrayMap $$1 = new Reference2ObjectArrayMap(this.map);
        $$1.keySet().removeIf($$0);
        if ($$1.isEmpty()) {
            return EMPTY;
        }
        return new DataComponentPatch((Reference2ObjectMap<DataComponentType<?>, Optional<?>>)$$1);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public SplitResult split() {
        if (this.isEmpty()) {
            return SplitResult.EMPTY;
        }
        DataComponentMap.Builder $$0 = DataComponentMap.builder();
        Set $$1 = Sets.newIdentityHashSet();
        this.map.forEach(($$2, $$3) -> {
            if ($$3.isPresent()) {
                $$0.setUnchecked($$2, $$3.get());
            } else {
                $$1.add($$2);
            }
        });
        return new SplitResult($$0.build(), $$1);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object $$0) {
        if (this == $$0) {
            return true;
        }
        if (!($$0 instanceof DataComponentPatch)) return false;
        DataComponentPatch $$1 = (DataComponentPatch)$$0;
        if (!this.map.equals($$1.map)) return false;
        return true;
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public String toString() {
        return DataComponentPatch.toString(this.map);
    }

    static String toString(Reference2ObjectMap<DataComponentType<?>, Optional<?>> $$0) {
        StringBuilder $$1 = new StringBuilder();
        $$1.append('{');
        boolean $$2 = true;
        for (Map.Entry $$3 : Reference2ObjectMaps.fastIterable($$0)) {
            if ($$2) {
                $$2 = false;
            } else {
                $$1.append(", ");
            }
            Optional $$4 = (Optional)$$3.getValue();
            if ($$4.isPresent()) {
                $$1.append($$3.getKey());
                $$1.append("=>");
                $$1.append($$4.get());
                continue;
            }
            $$1.append(REMOVED_PREFIX);
            $$1.append($$3.getKey());
        }
        $$1.append('}');
        return $$1.toString();
    }

    @FunctionalInterface
    static interface CodecGetter {
        public <T> StreamCodec<? super RegistryFriendlyByteBuf, T> apply(DataComponentType<T> var1);
    }

    public static class Builder {
        private final Reference2ObjectMap<DataComponentType<?>, Optional<?>> map = new Reference2ObjectArrayMap();

        Builder() {
        }

        public <T> Builder set(DataComponentType<T> $$0, T $$1) {
            this.map.put($$0, Optional.of($$1));
            return this;
        }

        public <T> Builder remove(DataComponentType<T> $$0) {
            this.map.put($$0, Optional.empty());
            return this;
        }

        public <T> Builder set(TypedDataComponent<T> $$0) {
            return this.set($$0.type(), $$0.value());
        }

        public DataComponentPatch build() {
            if (this.map.isEmpty()) {
                return EMPTY;
            }
            return new DataComponentPatch(this.map);
        }
    }

    public record SplitResult(DataComponentMap added, Set<DataComponentType<?>> removed) {
        public static final SplitResult EMPTY = new SplitResult(DataComponentMap.EMPTY, Set.of());
    }

    record PatchKey(DataComponentType<?> type, boolean removed) {
        public static final Codec<PatchKey> CODEC = Codec.STRING.flatXmap($$0 -> {
            Identifier $$2;
            DataComponentType<?> $$3;
            boolean $$1 = $$0.startsWith(DataComponentPatch.REMOVED_PREFIX);
            if ($$1) {
                $$0 = $$0.substring(DataComponentPatch.REMOVED_PREFIX.length());
            }
            if (($$3 = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue($$2 = Identifier.tryParse($$0))) == null) {
                return DataResult.error(() -> "No component with type: '" + String.valueOf($$2) + "'");
            }
            if ($$3.isTransient()) {
                return DataResult.error(() -> "'" + String.valueOf($$2) + "' is not a persistent component");
            }
            return DataResult.success((Object)new PatchKey($$3, $$1));
        }, $$0 -> {
            DataComponentType<?> $$1 = $$0.type();
            Identifier $$2 = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey($$1);
            if ($$2 == null) {
                return DataResult.error(() -> "Unregistered component: " + String.valueOf($$1));
            }
            return DataResult.success((Object)($$0.removed() ? DataComponentPatch.REMOVED_PREFIX + String.valueOf($$2) : $$2.toString()));
        });

        public Codec<?> valueCodec() {
            return this.removed ? Codec.EMPTY.codec() : this.type.codecOrThrow();
        }
    }
}

