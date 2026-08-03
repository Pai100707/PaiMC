package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FungusBlock extends VegetationBlock implements BonemealableBlock {
   public static final MapCodec<FungusBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ResourceKey.codec(Registries.CONFIGURED_FEATURE).fieldOf("feature").forGetter($$0x -> $$0x.feature),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("grows_on").forGetter($$0x -> $$0x.requiredBlock),
            propertiesCodec()
         )
         .apply($$0, FungusBlock::new)
   );
   private static final double BONEMEAL_SUCCESS_PROBABILITY = 0.4;
   private static final VoxelShape SHAPE = Block.column(8.0, 0.0, 9.0);
   private final Block requiredBlock;
   private final ResourceKey<ConfiguredFeature<?, ?>> feature;

   @Override
   public MapCodec<FungusBlock> codec() {
      return CODEC;
   }

   protected FungusBlock(ResourceKey<ConfiguredFeature<?, ?>> $$0, Block $$1, BlockBehaviour.Properties $$2) {
      super($$2);
      this.feature = $$0;
      this.requiredBlock = $$1;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(BlockTags.NYLIUM) || $$0.is(Blocks.MYCELIUM) || $$0.is(Blocks.SOUL_SOIL) || super.mayPlaceOn($$0, $$1, $$2);
   }

   private Optional<? extends Holder<ConfiguredFeature<?, ?>>> getFeature(net.minecraft.world.level.LevelReader $$0) {
      return $$0.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).get(this.feature);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      BlockState $$3 = $$0.getBlockState($$1.below());
      return $$3.is(this.requiredBlock);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return $$1.nextFloat() < 0.4;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      this.getFeature($$0).ifPresent($$3x -> ((ConfiguredFeature)$$3x.value()).place($$0, $$0.getChunkSource().getGenerator(), $$1, $$2));
   }
}
