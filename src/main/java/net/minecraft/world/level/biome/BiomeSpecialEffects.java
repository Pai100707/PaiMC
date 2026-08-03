package net.minecraft.world.level.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

public record BiomeSpecialEffects(
   int waterColor,
   Optional<Integer> foliageColorOverride,
   Optional<Integer> dryFoliageColorOverride,
   Optional<Integer> grassColorOverride,
   BiomeSpecialEffects.GrassColorModifier grassColorModifier
) {
   public static final Codec<BiomeSpecialEffects> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            ExtraCodecs.STRING_RGB_COLOR.fieldOf("water_color").forGetter(BiomeSpecialEffects::waterColor),
            ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("foliage_color").forGetter(BiomeSpecialEffects::foliageColorOverride),
            ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("dry_foliage_color").forGetter(BiomeSpecialEffects::dryFoliageColorOverride),
            ExtraCodecs.STRING_RGB_COLOR.optionalFieldOf("grass_color").forGetter(BiomeSpecialEffects::grassColorOverride),
            BiomeSpecialEffects.GrassColorModifier.CODEC
               .optionalFieldOf("grass_color_modifier", BiomeSpecialEffects.GrassColorModifier.NONE)
               .forGetter(BiomeSpecialEffects::grassColorModifier)
         )
         .apply($$0, BiomeSpecialEffects::new)
   );

   public static class Builder {
      private OptionalInt waterColor = OptionalInt.empty();
      private Optional<Integer> foliageColorOverride = Optional.empty();
      private Optional<Integer> dryFoliageColorOverride = Optional.empty();
      private Optional<Integer> grassColorOverride = Optional.empty();
      private BiomeSpecialEffects.GrassColorModifier grassColorModifier = BiomeSpecialEffects.GrassColorModifier.NONE;

      public BiomeSpecialEffects.Builder waterColor(int $$0) {
         this.waterColor = OptionalInt.of($$0);
         return this;
      }

      public BiomeSpecialEffects.Builder foliageColorOverride(int $$0) {
         this.foliageColorOverride = Optional.of($$0);
         return this;
      }

      public BiomeSpecialEffects.Builder dryFoliageColorOverride(int $$0) {
         this.dryFoliageColorOverride = Optional.of($$0);
         return this;
      }

      public BiomeSpecialEffects.Builder grassColorOverride(int $$0) {
         this.grassColorOverride = Optional.of($$0);
         return this;
      }

      public BiomeSpecialEffects.Builder grassColorModifier(BiomeSpecialEffects.GrassColorModifier $$0) {
         this.grassColorModifier = $$0;
         return this;
      }

      public BiomeSpecialEffects build() {
         return new BiomeSpecialEffects(
            this.waterColor.orElseThrow(() -> new IllegalStateException("Missing 'water' color.")),
            this.foliageColorOverride,
            this.dryFoliageColorOverride,
            this.grassColorOverride,
            this.grassColorModifier
         );
      }
   }

   public static enum GrassColorModifier implements StringRepresentable {
      NONE("none") {
         @Override
         public int modifyColor(double $$0, double $$1, int $$2) {
            return $$2;
         }
      },
      DARK_FOREST("dark_forest") {
         @Override
         public int modifyColor(double $$0, double $$1, int $$2) {
            return ($$2 & 16711422) + 2634762 >> 1;
         }
      },
      SWAMP("swamp") {
         @Override
         public int modifyColor(double $$0, double $$1, int $$2) {
            double $$3 = Biome.BIOME_INFO_NOISE.getValue($$0 * 0.0225, $$1 * 0.0225, false);
            return $$3 < -0.1 ? 5011004 : 6975545;
         }
      };

      private final String name;
      public static final Codec<BiomeSpecialEffects.GrassColorModifier> CODEC = StringRepresentable.fromEnum(BiomeSpecialEffects.GrassColorModifier::values);

      public abstract int modifyColor(double var1, double var3, int var5);

      GrassColorModifier(final String $$0) {
         this.name = $$0;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
