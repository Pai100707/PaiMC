package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UntintedParticleLeavesBlock extends LeavesBlock {
   public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter($$0x -> $$0x.leafParticleChance),
            ParticleTypes.CODEC.fieldOf("leaf_particle").forGetter($$0x -> $$0x.leafParticle),
            propertiesCodec()
         )
         .apply($$0, UntintedParticleLeavesBlock::new)
   );
   protected final ParticleOptions leafParticle;

   public UntintedParticleLeavesBlock(float $$0, ParticleOptions $$1, BlockBehaviour.Properties $$2) {
      super($$0, $$2);
      this.leafParticle = $$1;
   }

   @Override
   protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level $$0, BlockPos $$1, RandomSource $$2) {
      ParticleUtils.spawnParticleBelow($$0, $$1, $$2, this.leafParticle);
   }

   @Override
   public MapCodec<UntintedParticleLeavesBlock> codec() {
      return CODEC;
   }
}
