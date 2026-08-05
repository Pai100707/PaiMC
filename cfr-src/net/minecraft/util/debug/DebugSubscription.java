/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.debug;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class DebugSubscription<T> {
    public static final int DOES_NOT_EXPIRE = 0;
    final @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec;
    private final int expireAfterTicks;

    public DebugSubscription(@Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> $$0, int $$1) {
        this.valueStreamCodec = $$0;
        this.expireAfterTicks = $$1;
    }

    public DebugSubscription(@Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> $$0) {
        this($$0, 0);
    }

    public Update<T> packUpdate(@Nullable T $$0) {
        return new Update<T>(this, Optional.ofNullable($$0));
    }

    public Update<T> emptyUpdate() {
        return new Update(this, Optional.empty());
    }

    public Event<T> packEvent(T $$0) {
        return new Event<T>(this, $$0);
    }

    public String toString() {
        return Util.getRegisteredName(BuiltInRegistries.DEBUG_SUBSCRIPTION, this);
    }

    public @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> valueStreamCodec() {
        return this.valueStreamCodec;
    }

    public int expireAfterTicks() {
        return this.expireAfterTicks;
    }

    public record Update<T>(DebugSubscription<T> subscription, Optional<T> value) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Update<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION).dispatch(Update::subscription, Update::streamCodec);

        private static <T> StreamCodec<? super RegistryFriendlyByteBuf, Update<T>> streamCodec(DebugSubscription<T> $$0) {
            return ByteBufCodecs.optional(Objects.requireNonNull($$0.valueStreamCodec)).map($$1 -> new Update($$0, $$1), Update::value);
        }
    }

    public record Event<T>(DebugSubscription<T> subscription, T value) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Event<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.DEBUG_SUBSCRIPTION).dispatch(Event::subscription, Event::streamCodec);

        private static <T> StreamCodec<? super RegistryFriendlyByteBuf, Event<T>> streamCodec(DebugSubscription<T> $$0) {
            return Objects.requireNonNull($$0.valueStreamCodec).map($$1 -> new Event<Object>($$0, $$1), Event::value);
        }
    }
}

