package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class MangroveLeavesBlock extends TintedParticleLeavesBlock implements BonemealableBlock {
   public static final MapCodec<MangroveLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ExtraCodecs.floatRange(0.0F, 1.0F).fieldOf("leaf_particle_chance").forGetter($$0x -> $$0x.leafParticleChance), propertiesCodec())
         .apply($$0, MangroveLeavesBlock::new)
   );

   @Override
   public MapCodec<MangroveLeavesBlock> codec() {
      return CODEC;
   }

   public MangroveLeavesBlock(float $$0, BlockBehaviour.Properties $$1) {
      super($$0, $$1);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$0.getBlockState($$1.below()).isAir();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      $$0.setBlock($$2.below(), MangrovePropaguleBlock.createNewHangingPropagule(), 2);
   }

   @Override
   public BlockPos getParticlePos(BlockPos $$0) {
      return $$0.below();
   }
}
