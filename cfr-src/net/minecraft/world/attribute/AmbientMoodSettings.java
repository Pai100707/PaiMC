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
import net.minecraft.sounds.SoundEvents;

public record AmbientMoodSettings(Holder<SoundEvent> soundEvent, int tickDelay, int blockSearchExtent, double soundPositionOffset) {
    public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter($$0 -> $$0.soundEvent), (App)Codec.INT.fieldOf("tick_delay").forGetter($$0 -> $$0.tickDelay), (App)Codec.INT.fieldOf("block_search_extent").forGetter($$0 -> $$0.blockSearchExtent), (App)Codec.DOUBLE.fieldOf("offset").forGetter($$0 -> $$0.soundPositionOffset)).apply((Applicative)$$02, AmbientMoodSettings::new));
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
}

