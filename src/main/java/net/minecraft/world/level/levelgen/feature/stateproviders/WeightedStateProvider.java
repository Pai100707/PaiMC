package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.WeightedList.Builder;
import net.minecraft.world.level.block.state.BlockState;

public class WeightedStateProvider extends BlockStateProvider {
   public static final MapCodec<WeightedStateProvider> CODEC = WeightedList.nonEmptyCodec(BlockState.CODEC)
      .comapFlatMap(WeightedStateProvider::create, $$0 -> $$0.weightedList)
      .fieldOf("entries");
   private final WeightedList<BlockState> weightedList;

   private static DataResult<WeightedStateProvider> create(WeightedList<BlockState> $$0) {
      return $$0.isEmpty() ? DataResult.error(() -> "WeightedStateProvider with no states") : DataResult.success(new WeightedStateProvider($$0));
   }

   public WeightedStateProvider(WeightedList<BlockState> $$0) {
      this.weightedList = $$0;
   }

   public WeightedStateProvider(Builder<BlockState> $$0) {
      this($$0.build());
   }

   @Override
   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
   }

   @Override
   public BlockState getState(RandomSource $$0, BlockPos $$1) {
      return (BlockState)this.weightedList.getRandomOrThrow($$0);
   }
}
