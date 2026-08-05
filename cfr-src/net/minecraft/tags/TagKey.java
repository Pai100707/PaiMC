/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Interner
 *  com.google.common.collect.Interners
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.tags;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record TagKey<T>(ResourceKey<? extends Registry<T>> registry, Identifier location) {
    private static final Interner<TagKey<?>> VALUES = Interners.newWeakInterner();

    public static <T> Codec<TagKey<T>> codec(ResourceKey<? extends Registry<T>> $$0) {
        return Identifier.CODEC.xmap($$1 -> TagKey.create($$0, $$1), TagKey::location);
    }

    public static <T> Codec<TagKey<T>> hashedCodec(ResourceKey<? extends Registry<T>> $$02) {
        return Codec.STRING.comapFlatMap($$12 -> $$12.startsWith("#") ? Identifier.read($$12.substring(1)).map($$1 -> TagKey.create($$02, $$1)) : DataResult.error(() -> "Not a tag id"), $$0 -> "#" + String.valueOf($$0.location));
    }

    public static <T> StreamCodec<ByteBuf, TagKey<T>> streamCodec(ResourceKey<? extends Registry<T>> $$0) {
        return Identifier.STREAM_CODEC.map($$1 -> TagKey.create($$0, $$1), TagKey::location);
    }

    public static <T> TagKey<T> create(ResourceKey<? extends Registry<T>> $$0, Identifier $$1) {
        return (TagKey)VALUES.intern(new TagKey<T>($$0, $$1));
    }

    public boolean isFor(ResourceKey<? extends Registry<?>> $$0) {
        return this.registry == $$0;
    }

    public <E> Optional<TagKey<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
        return this.isFor($$0) ? Optional.of(this) : Optional.empty();
    }

    @Override
    public String toString() {
        return "TagKey[" + String.valueOf(this.registry.identifier()) + " / " + String.valueOf(this.location) + "]";
    }
}

