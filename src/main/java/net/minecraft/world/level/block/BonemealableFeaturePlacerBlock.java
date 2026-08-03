package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class BonemealableFeaturePlacerBlock extends Block implements BonemealableBlock {
   public static final MapCodec<BonemealableFeaturePlacerBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter($$0x -> $$0x.feature), propertiesCodec())
         .apply($$0, BonemealableFeaturePlacerBlock::new)
   );
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   @Override
   public MapCodec<BonemealableFeaturePlacerBlock> codec() {
      return CODEC;
   }

   public BonemealableFeaturePlacerBlock(ResourceKey<ConfiguredFeature<?, ?>> $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.feature = $$0;
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
      $$0.registryAccess()
         .lookup(Registries.CONFIGURED_FEATURE)
         .flatMap($$0x -> $$0x.get(this.feature))
         .ifPresent($$3x -> ((ConfiguredFeature)$$3x.value()).place($$0, $$0.getChunkSource().getGenerator(), $$1, $$2.above()));
   }

   @Override
   public BonemealableBlock.Type getType() {
      return BonemealableBlock.Type.NEIGHBOR_SPREADER;
   }
}
