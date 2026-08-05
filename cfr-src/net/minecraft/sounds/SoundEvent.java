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
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;

public record SoundEvent(Identifier location, Optional<Float> fixedRange) {
    public static final Codec<SoundEvent> DIRECT_CODEC = RecordCodecBuilder.create($$0 -> $$0.group((App)Identifier.CODEC.fieldOf("sound_id").forGetter(SoundEvent::location), (App)Codec.FLOAT.lenientOptionalFieldOf("range").forGetter(SoundEvent::fixedRange)).apply((Applicative)$$0, SoundEvent::create));
    public static final Codec<Holder<SoundEvent>> CODEC = RegistryFileCodec.create(Registries.SOUND_EVENT, DIRECT_CODEC);
    public static final StreamCodec<ByteBuf, SoundEvent> DIRECT_STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, SoundEvent::location, ByteBufCodecs.FLOAT.apply(ByteBufCodecs::optional), SoundEvent::fixedRange, SoundEvent::create);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<SoundEvent>> STREAM_CODEC = ByteBufCodecs.holder(Registries.SOUND_EVENT, DIRECT_STREAM_CODEC);

    private static SoundEvent create(Identifier $$0, Optional<Float> $$12) {
        return $$12.map($$1 -> SoundEvent.createFixedRangeEvent($$0, $$1.floatValue())).orElseGet(() -> SoundEvent.createVariableRangeEvent($$0));
    }

    public static SoundEvent createVariableRangeEvent(Identifier $$0) {
        return new SoundEvent($$0, Optional.empty());
    }

    public static SoundEvent createFixedRangeEvent(Identifier $$0, float $$1) {
        return new SoundEvent($$0, Optional.of(Float.valueOf($$1)));
    }

    public float getRange(float $$0) {
        return this.fixedRange.orElse(Float.valueOf($$0 > 1.0f ? 16.0f * $$0 : 16.0f)).floatValue();
    }
}

