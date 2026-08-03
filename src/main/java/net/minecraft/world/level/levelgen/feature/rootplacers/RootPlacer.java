package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public abstract class RootPlacer {
   public static final Codec<RootPlacer> CODEC = BuiltInRegistries.ROOT_PLACER_TYPE.byNameCodec().dispatch(RootPlacer::type, RootPlacerType::codec);
   protected final IntProvider trunkOffsetY;
   protected final BlockStateProvider rootProvider;
   protected final Optional<AboveRootPlacement> aboveRootPlacement;

   protected static <P extends RootPlacer> P3<Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> rootPlacerParts(Instance<P> $$0) {
      return $$0.group(
         IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter($$0x -> $$0x.trunkOffsetY),
         BlockStateProvider.CODEC.fieldOf("root_provider").forGetter($$0x -> $$0x.rootProvider),
         AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter($$0x -> $$0x.aboveRootPlacement)
      );
   }

   public RootPlacer(IntProvider $$0, BlockStateProvider $$1, Optional<AboveRootPlacement> $$2) {
      this.trunkOffsetY = $$0;
      this.rootProvider = $$1;
      this.aboveRootPlacement = $$2;
   }

   protected abstract RootPlacerType<?> type();

   public abstract boolean placeRoots(
      net.minecraft.world.level.LevelSimulatedReader var1,
      BiConsumer<BlockPos, BlockState> var2,
      RandomSource var3,
      BlockPos var4,
      BlockPos var5,
      TreeConfiguration var6
   );

   protected boolean canPlaceRoot(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return TreeFeature.validTreePos($$0, $$1);
   }

   protected void placeRoot(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, BlockPos $$3, TreeConfiguration $$4
   ) {
      if (this.canPlaceRoot($$0, $$3)) {
         $$1.accept($$3, this.getPotentiallyWaterloggedState($$0, $$3, this.rootProvider.getState($$2, $$3)));
         if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement $$5 = this.aboveRootPlacement.get();
            BlockPos $$6 = $$3.above();
            if ($$2.nextFloat() < $$5.aboveRootPlacementChance() && $$0.isStateAtPosition($$6, BlockBehaviour.BlockStateBase::isAir)) {
               $$1.accept($$6, this.getPotentiallyWaterloggedState($$0, $$6, $$5.aboveRootProvider().getState($$2, $$6)));
            }
         }
      }
   }

   protected BlockState getPotentiallyWaterloggedState(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1, BlockState $$2) {
      if ($$2.hasProperty(BlockStateProperties.WATERLOGGED)) {
         boolean $$3 = $$0.isFluidAtPosition($$1, $$0x -> $$0x.is(FluidTags.WATER));
         return $$2.setValue(BlockStateProperties.WATERLOGGED, $$3);
      } else {
         return $$2;
      }
   }

   public BlockPos getTrunkOrigin(BlockPos $$0, RandomSource $$1) {
      return $$0.above(this.trunkOffsetY.sample($$1));
   }
}
