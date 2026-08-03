package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.NetherFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.lighting.LightEngine;

public class NyliumBlock extends Block implements BonemealableBlock {
   public static final MapCodec<NyliumBlock> CODEC = simpleCodec(NyliumBlock::new);

   @Override
   public MapCodec<NyliumBlock> codec() {
      return CODEC;
   }

   protected NyliumBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   private static boolean canBeNylium(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.above();
      BlockState $$4 = $$1.getBlockState($$3);
      int $$5 = LightEngine.getLightBlockInto($$0, $$4, Direction.UP, $$4.getLightBlock());
      return $$5 < 15;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!canBeNylium($$0, $$1, $$2)) {
         $$1.setBlockAndUpdate($$2, Blocks.NETHERRACK.defaultBlockState());
      }
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return $$0.getBlockState($$1.above()).isAir();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockState $$4 = $$0.getBlockState($$2);
      BlockPos $$5 = $$2.above();
      ChunkGenerator $$6 = $$0.getChunkSource().getGenerator();
      Registry<ConfiguredFeature<?, ?>> $$7 = $$0.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE);
      if ($$4.is(Blocks.CRIMSON_NYLIUM)) {
         this.place($$7, NetherFeatures.CRIMSON_FOREST_VEGETATION_BONEMEAL, $$0, $$6, $$1, $$5);
      } else if ($$4.is(Blocks.WARPED_NYLIUM)) {
         this.place($$7, NetherFeatures.WARPED_FOREST_VEGETATION_BONEMEAL, $$0, $$6, $$1, $$5);
         this.place($$7, NetherFeatures.NETHER_SPROUTS_BONEMEAL, $$0, $$6, $$1, $$5);
         if ($$1.nextInt(8) == 0) {
            this.place($$7, NetherFeatures.TWISTING_VINES_BONEMEAL, $$0, $$6, $$1, $$5);
         }
      }
   }

   private void place(
      Registry<ConfiguredFeature<?, ?>> $$0, ResourceKey<ConfiguredFeature<?, ?>> $$1, ServerLevel $$2, ChunkGenerator $$3, RandomSource $$4, BlockPos $$5
   ) {
      $$0.get($$1).ifPresent($$4x -> ((ConfiguredFeature)$$4x.value()).place($$2, $$3, $$4, $$5));
   }

   @Override
   public BonemealableBlock.Type getType() {
      return BonemealableBlock.Type.NEIGHBOR_SPREADER;
   }
}
