/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public record AmbientAdditionsSettings(Holder<SoundEvent> soundEvent, double tickChance) {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter($$0 -> $$0.soundEvent), (App)Codec.DOUBLE.fieldOf("tick_chance").forGetter($$0 -> $$0.tickChance)).apply((Applicative)$$02, AmbientAdditionsSettings::new));
}

