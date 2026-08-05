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
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

public record AmbientParticle(ParticleOptions particle, float probability) {
    public static final Codec<AmbientParticle> CODEC = RecordCodecBuilder.create($$02 -> $$02.group((App)ParticleTypes.CODEC.fieldOf("particle").forGetter($$0 -> $$0.particle), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter($$0 -> Float.valueOf($$0.probability))).apply((Applicative)$$02, AmbientParticle::new));

    public boolean canSpawn(RandomSource $$0) {
        return $$0.nextFloat() <= this.probability;
    }

    public static List<AmbientParticle> of(ParticleOptions $$0, float $$1) {
        return List.of(new AmbientParticle($$0, $$1));
    }
}

