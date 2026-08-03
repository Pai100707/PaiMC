package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;

public class DeltaFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<DeltaFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockState.CODEC.fieldOf("contents").forGetter($$0x -> $$0x.contents),
            BlockState.CODEC.fieldOf("rim").forGetter($$0x -> $$0x.rim),
            IntProvider.codec(0, 16).fieldOf("size").forGetter($$0x -> $$0x.size),
            IntProvider.codec(0, 16).fieldOf("rim_size").forGetter($$0x -> $$0x.rimSize)
         )
         .apply($$0, DeltaFeatureConfiguration::new)
   );
   private final BlockState contents;
   private final BlockState rim;
   private final IntProvider size;
   private final IntProvider rimSize;

   public DeltaFeatureConfiguration(BlockState $$0, BlockState $$1, IntProvider $$2, IntProvider $$3) {
      this.contents = $$0;
      this.rim = $$1;
      this.size = $$2;
      this.rimSize = $$3;
   }

   public BlockState contents() {
      return this.contents;
   }

   public BlockState rim() {
      return this.rim;
   }

   public IntProvider size() {
      return this.size;
   }

   public IntProvider rimSize() {
      return this.rimSize;
   }
}
