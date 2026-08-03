package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;

public class ColumnFeatureConfiguration implements FeatureConfiguration {
   public static final Codec<ColumnFeatureConfiguration> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            IntProvider.codec(0, 3).fieldOf("reach").forGetter($$0x -> $$0x.reach), IntProvider.codec(1, 10).fieldOf("height").forGetter($$0x -> $$0x.height)
         )
         .apply($$0, ColumnFeatureConfiguration::new)
   );
   private final IntProvider reach;
   private final IntProvider height;

   public ColumnFeatureConfiguration(IntProvider $$0, IntProvider $$1) {
      this.reach = $$0;
      this.height = $$1;
   }

   public IntProvider reach() {
      return this.reach;
   }

   public IntProvider height() {
      return this.height;
   }
}
