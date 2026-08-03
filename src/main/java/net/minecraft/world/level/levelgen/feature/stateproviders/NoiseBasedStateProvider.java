package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public abstract class NoiseBasedStateProvider extends BlockStateProvider {
   protected final long seed;
   protected final NormalNoise.NoiseParameters parameters;
   protected final float scale;
   protected final NormalNoise noise;

   protected static <P extends NoiseBasedStateProvider> P3<Mu<P>, Long, NormalNoise.NoiseParameters, Float> noiseCodec(Instance<P> $$0) {
      return $$0.group(
         Codec.LONG.fieldOf("seed").forGetter($$0x -> $$0x.seed),
         NormalNoise.NoiseParameters.DIRECT_CODEC.fieldOf("noise").forGetter($$0x -> $$0x.parameters),
         ExtraCodecs.POSITIVE_FLOAT.fieldOf("scale").forGetter($$0x -> $$0x.scale)
      );
   }

   protected NoiseBasedStateProvider(long $$0, NormalNoise.NoiseParameters $$1, float $$2) {
      this.seed = $$0;
      this.parameters = $$1;
      this.scale = $$2;
      this.noise = NormalNoise.create(new WorldgenRandom(new LegacyRandomSource($$0)), $$1);
   }

   protected double getNoiseValue(BlockPos $$0, double $$1) {
      return this.noise.getValue($$0.getX() * $$1, $$0.getY() * $$1, $$0.getZ() * $$1);
   }
}
