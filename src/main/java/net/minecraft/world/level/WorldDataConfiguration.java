package net.minecraft.world.level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public record WorldDataConfiguration(net.minecraft.world.level.DataPackConfig dataPacks, FeatureFlagSet enabledFeatures) {
   public static final String ENABLED_FEATURES_ID = "enabled_features";
   public static final MapCodec<net.minecraft.world.level.WorldDataConfiguration> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            net.minecraft.world.level.DataPackConfig.CODEC
               .lenientOptionalFieldOf("DataPacks", net.minecraft.world.level.DataPackConfig.DEFAULT)
               .forGetter(net.minecraft.world.level.WorldDataConfiguration::dataPacks),
            FeatureFlags.CODEC
               .lenientOptionalFieldOf("enabled_features", FeatureFlags.DEFAULT_FLAGS)
               .forGetter(net.minecraft.world.level.WorldDataConfiguration::enabledFeatures)
         )
         .apply($$0, net.minecraft.world.level.WorldDataConfiguration::new)
   );
   public static final Codec<net.minecraft.world.level.WorldDataConfiguration> CODEC = MAP_CODEC.codec();
   public static final net.minecraft.world.level.WorldDataConfiguration DEFAULT = new net.minecraft.world.level.WorldDataConfiguration(
      net.minecraft.world.level.DataPackConfig.DEFAULT, FeatureFlags.DEFAULT_FLAGS
   );

   public net.minecraft.world.level.WorldDataConfiguration expandFeatures(FeatureFlagSet $$0) {
      return new net.minecraft.world.level.WorldDataConfiguration(this.dataPacks, this.enabledFeatures.join($$0));
   }
}
