/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Either<Holder<T>, ResourceKey<T>> contents) {
    public EitherHolder(Holder<T> $$0) {
        this(Either.left($$0));
    }

    public EitherHolder(ResourceKey<T> $$0) {
        this(Either.right($$0));
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> $$02, Codec<Holder<T>> $$1) {
        return Codec.either($$1, (Codec)ResourceKey.codec($$02).comapFlatMap($$0 -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())).xmap(EitherHolder::new, EitherHolder::contents);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(ResourceKey<Registry<T>> $$0, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> $$1) {
        return StreamCodec.composite(ByteBufCodecs.either($$1, ResourceKey.streamCodec($$0)), EitherHolder::contents, EitherHolder::new);
    }

    public Optional<T> unwrap(Registry<T> $$02) {
        return (Optional)this.contents.map($$0 -> Optional.of($$0.value()), $$02::getOptional);
    }

    public Optional<Holder<T>> unwrap(HolderLookup.Provider $$0) {
        return (Optional)this.contents.map(Optional::of, $$1 -> $$0.get($$1).map($$0 -> $$0));
    }

    public Optional<ResourceKey<T>> key() {
        return (Optional)this.contents.map(Holder::unwrapKey, Optional::of);
    }
}

