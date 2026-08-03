package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseThresholdProvider extends NoiseBasedStateProvider {
   public static final MapCodec<NoiseThresholdProvider> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> noiseCodec($$0)
         .and(
            $$0.group(
               Codec.floatRange(-1.0F, 1.0F).fieldOf("threshold").forGetter($$0x -> $$0x.threshold),
               Codec.floatRange(0.0F, 1.0F).fieldOf("high_chance").forGetter($$0x -> $$0x.highChance),
               BlockState.CODEC.fieldOf("default_state").forGetter($$0x -> $$0x.defaultState),
               ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("low_states").forGetter($$0x -> $$0x.lowStates),
               ExtraCodecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("high_states").forGetter($$0x -> $$0x.highStates)
            )
         )
         .apply($$0, NoiseThresholdProvider::new)
   );
   private final float threshold;
   private final float highChance;
   private final BlockState defaultState;
   private final List<BlockState> lowStates;
   private final List<BlockState> highStates;

   public NoiseThresholdProvider(
      long $$0, NormalNoise.NoiseParameters $$1, float $$2, float $$3, float $$4, BlockState $$5, List<BlockState> $$6, List<BlockState> $$7
   ) {
      super($$0, $$1, $$2);
      this.threshold = $$3;
      this.highChance = $$4;
      this.defaultState = $$5;
      this.lowStates = $$6;
      this.highStates = $$7;
   }

   @Override
   protected BlockStateProviderType<?> type() {
      return BlockStateProviderType.NOISE_THRESHOLD_PROVIDER;
   }

   @Override
   public BlockState getState(RandomSource $$0, BlockPos $$1) {
      double $$2 = this.getNoiseValue($$1, this.scale);
      if ($$2 < this.threshold) {
         return (BlockState)Util.getRandom(this.lowStates, $$0);
      } else {
         return $$0.nextFloat() < this.highChance ? (BlockState)Util.getRandom(this.highStates, $$0) : this.defaultState;
      }
   }
}
