package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeLayerSettings {
   private static final Codec<Double> LAYER_RANGE = Codec.doubleRange(0.01, 50.0);
   public static final Codec<GeodeLayerSettings> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            LAYER_RANGE.fieldOf("filling").orElse(1.7).forGetter($$0x -> $$0x.filling),
            LAYER_RANGE.fieldOf("inner_layer").orElse(2.2).forGetter($$0x -> $$0x.innerLayer),
            LAYER_RANGE.fieldOf("middle_layer").orElse(3.2).forGetter($$0x -> $$0x.middleLayer),
            LAYER_RANGE.fieldOf("outer_layer").orElse(4.2).forGetter($$0x -> $$0x.outerLayer)
         )
         .apply($$0, GeodeLayerSettings::new)
   );
   public final double filling;
   public final double innerLayer;
   public final double middleLayer;
   public final double outerLayer;

   public GeodeLayerSettings(double $$0, double $$1, double $$2, double $$3) {
      this.filling = $$0;
      this.innerLayer = $$1;
      this.middleLayer = $$2;
      this.outerLayer = $$3;
   }
}
