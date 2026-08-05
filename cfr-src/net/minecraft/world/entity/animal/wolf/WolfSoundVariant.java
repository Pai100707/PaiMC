/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.animal.wolf;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.sounds.SoundEvent;

public record WolfSoundVariant(Holder<SoundEvent> ambientSound, Holder<SoundEvent> deathSound, Holder<SoundEvent> growlSound, Holder<SoundEvent> hurtSound, Holder<SoundEvent> pantSound, Holder<SoundEvent> whineSound) {
    public static final Codec<WolfSoundVariant> DIRECT_CODEC = WolfSoundVariant.getWolfSoundVariantCodec();
    public static final Codec<WolfSoundVariant> NETWORK_CODEC = WolfSoundVariant.getWolfSoundVariantCodec();
    public static final Codec<Holder<WolfSoundVariant>> CODEC = RegistryFixedCodec.create(Registries.WOLF_SOUND_VARIANT);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<WolfSoundVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.WOLF_SOUND_VARIANT);

    private static Codec<WolfSoundVariant> getWolfSoundVariantCodec() {
        return RecordCodecBuilder.create($$0 -> $$0.group((App)SoundEvent.CODEC.fieldOf("ambient_sound").forGetter(WolfSoundVariant::ambientSound), (App)SoundEvent.CODEC.fieldOf("death_sound").forGetter(WolfSoundVariant::deathSound), (App)SoundEvent.CODEC.fieldOf("growl_sound").forGetter(WolfSoundVariant::growlSound), (App)SoundEvent.CODEC.fieldOf("hurt_sound").forGetter(WolfSoundVariant::hurtSound), (App)SoundEvent.CODEC.fieldOf("pant_sound").forGetter(WolfSoundVariant::pantSound), (App)SoundEvent.CODEC.fieldOf("whine_sound").forGetter(WolfSoundVariant::whineSound)).apply((Applicative)$$0, WolfSoundVariant::new));
    }
}

