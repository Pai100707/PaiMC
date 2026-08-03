package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P4;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseProvider extends NoiseBasedStateProvider {
   public static final MapCodec<NoiseProvider> CODEC = RecordCodecBuilder.mapCodec($$0 -> noiseProviderCodec($$0).apply($$0, NoiseProvider::new));
   protected final List<BlockState> states;

   protected static <P extends NoiseProvider> P4<Mu<P>, Long, NormalNoise.NoiseParameters, Float, List<BlockState>> noiseProviderCodec(Instance<P> $$0) {
      return noiseCodec($$0).and(ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("states").forGetter($$0x -> $$0x.states));
   }

   public NoiseProvider(long $$0, NormalNoise.NoiseParameters $$1, float $$2, List<BlockState> $$3) {
      super($$0, $$1, $$2);
      this.states = $$3;
   }

   @Override
   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.NOISE_PROVIDER;
   }

   @Override
   public BlockState getState(RandomSource $$0, BlockPos $$1) {
      return this.getRandomState(this.states, $$1, this.scale);
   }

   protected BlockState getRandomState(List<BlockState> $$0, BlockPos $$1, double $$2) {
      double $$3 = this.getNoiseValue($$1, $$2);
      return this.getRandomState($$0, $$3);
   }

   protected BlockState getRandomState(List<BlockState> $$0, double $$1) {
      double $$2 = Mth.clamp((1.0 + $$1) / 2.0, 0.0, 0.9999);
      return $$0.get((int)($$2 * $$0.size()));
   }
}
