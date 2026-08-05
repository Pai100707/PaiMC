/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.server.network;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.FilteredText;

public record Filterable<T>(T raw, Optional<T> filtered) {
    public static <T> Codec<Filterable<T>> codec(Codec<T> $$0) {
        Codec $$12 = RecordCodecBuilder.create($$1 -> $$1.group((App)$$0.fieldOf("raw").forGetter(Filterable::raw), (App)$$0.optionalFieldOf("filtered").forGetter(Filterable::filtered)).apply((Applicative)$$1, Filterable::new));
        Codec $$2 = $$0.xmap(Filterable::passThrough, Filterable::raw);
        return Codec.withAlternative((Codec)$$12, (Codec)$$2);
    }

    public static <B extends ByteBuf, T> StreamCodec<B, Filterable<T>> streamCodec(StreamCodec<B, T> $$0) {
        return StreamCodec.composite($$0, Filterable::raw, $$0.apply(ByteBufCodecs::optional), Filterable::filtered, Filterable::new);
    }

    public static <T> Filterable<T> passThrough(T $$0) {
        return new Filterable<T>($$0, Optional.empty());
    }

    public static Filterable<String> from(FilteredText $$0) {
        return new Filterable<String>($$0.raw(), $$0.isFiltered() ? Optional.of($$0.filteredOrEmpty()) : Optional.empty());
    }

    public T get(boolean $$0) {
        if ($$0) {
            return this.filtered.orElse(this.raw);
        }
        return this.raw;
    }

    public <U> Filterable<U> map(Function<T, U> $$0) {
        return new Filterable<U>($$0.apply(this.raw), this.filtered.map($$0));
    }

    public <U> Optional<Filterable<U>> resolve(Function<T, Optional<U>> $$0) {
        Optional<U> $$1 = $$0.apply(this.raw);
        if ($$1.isEmpty()) {
            return Optional.empty();
        }
        if (this.filtered.isPresent()) {
            Optional<U> $$2 = $$0.apply(this.filtered.get());
            if ($$2.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Filterable<U>($$1.get(), $$2));
        }
        return Optional.of(new Filterable<U>($$1.get(), Optional.empty()));
    }
}

