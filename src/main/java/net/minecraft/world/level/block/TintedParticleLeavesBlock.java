package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TintedParticleLeavesBlock extends LeavesBlock {
   public static final MapCodec<TintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter($$0x -> $$0x.leafParticleChance), propertiesCodec())
         .apply($$0, TintedParticleLeavesBlock::new)
   );

   public TintedParticleLeavesBlock(float $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   protected void spawnFallingLeavesParticle(net.minecraft.world.level.Level $$0, BlockPos $$1, RandomSource $$2) {
      ColorParticleOption $$3 = ColorParticleOption.create(ParticleTypes.TINTED_LEAVES, $$0.getClientLeafTintColor($$1));
      ParticleUtils.spawnParticleBelow($$0, $$1, $$2, $$3);
   }

   @Override
   public MapCodec<? extends TintedParticleLeavesBlock> codec() {
      return CODEC;
   }
}
