package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ReplaceSphereConfiguration implements FeatureConfiguration {
   public static final Codec<ReplaceSphereConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockState.CODEC.fieldOf("target").forGetter($$0x -> $$0x.targetState),
            BlockState.CODEC.fieldOf("state").forGetter($$0x -> $$0x.replaceState),
            IntProvider.codec(0, 12).fieldOf("radius").forGetter($$0x -> $$0x.radius)
         )
         .apply($$0, ReplaceSphereConfiguration::new)
   );
   public final BlockState targetState;
   public final BlockState replaceState;
   private final IntProvider radius;

   public ReplaceSphereConfiguration(BlockState $$0, BlockState $$1, IntProvider $$2) {
      this.targetState = $$0;
      this.replaceState = $$1;
      this.radius = $$2;
   }

   public IntProvider radius() {
      return this.radius;
   }
}
