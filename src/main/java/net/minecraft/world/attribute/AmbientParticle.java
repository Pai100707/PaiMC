package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;

public record AmbientParticle(ParticleOptions particle, float probability) {
   public static final Codec<net.minecraft.world.attribute.AmbientParticle> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter($$0x -> $$0x.particle),
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter($$0x -> $$0x.probability)
         )
         .apply($$0, net.minecraft.world.attribute.AmbientParticle::new)
   );

   public boolean canSpawn(RandomSource $$0) {
      return $$0.nextFloat() <= this.probability;
   }

   public static List<net.minecraft.world.attribute.AmbientParticle> of(ParticleOptions $$0, float $$1) {
      return List.of(new net.minecraft.world.attribute.AmbientParticle($$0, $$1));
   }
}
