package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CarverConfiguration extends ProbabilityFeatureConfiguration {
   public static final MapCodec<CarverConfiguration> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter($$0x -> $$0x.probability),
            HeightProvider.CODEC.fieldOf("y").forGetter($$0x -> $$0x.y),
            FloatProvider.CODEC.fieldOf("yScale").forGetter($$0x -> $$0x.yScale),
            VerticalAnchor.CODEC.fieldOf("lava_level").forGetter($$0x -> $$0x.lavaLevel),
            CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter($$0x -> $$0x.debugSettings),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("replaceable").forGetter($$0x -> $$0x.replaceable)
         )
         .apply($$0, CarverConfiguration::new)
   );
   public final HeightProvider y;
   public final FloatProvider yScale;
   public final VerticalAnchor lavaLevel;
   public final CarverDebugSettings debugSettings;
   public final HolderSet<Block> replaceable;

   public CarverConfiguration(float $$0, HeightProvider $$1, FloatProvider $$2, VerticalAnchor $$3, CarverDebugSettings $$4, HolderSet<Block> $$5) {
      super($$0);
      this.y = $$1;
      this.yScale = $$2;
      this.lavaLevel = $$3;
      this.debugSettings = $$4;
      this.replaceable = $$5;
   }
}
