package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributeMap.Builder;
import net.minecraft.world.attribute.modifier.AttributeModifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class Biome {
   public static final Codec<Biome> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Biome.ClimateSettings.CODEC.forGetter($$0x -> $$0x.climateSettings),
            EnvironmentAttributeMap.CODEC_ONLY_POSITIONAL.optionalFieldOf("attributes", EnvironmentAttributeMap.EMPTY).forGetter($$0x -> $$0x.attributes),
            BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter($$0x -> $$0x.specialEffects),
            BiomeGenerationSettings.CODEC.forGetter($$0x -> $$0x.generationSettings),
            MobSpawnSettings.CODEC.forGetter($$0x -> $$0x.mobSettings)
         )
         .apply($$0, Biome::new)
   );
   public static final Codec<Biome> NETWORK_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            Biome.ClimateSettings.CODEC.forGetter($$0x -> $$0x.climateSettings),
            EnvironmentAttributeMap.NETWORK_CODEC.optionalFieldOf("attributes", EnvironmentAttributeMap.EMPTY).forGetter($$0x -> $$0x.attributes),
            BiomeSpecialEffects.CODEC.fieldOf("effects").forGetter($$0x -> $$0x.specialEffects)
         )
         .apply($$0, ($$0x, $$1, $$2) -> new Biome($$0x, $$1, $$2, BiomeGenerationSettings.EMPTY, MobSpawnSettings.EMPTY))
   );
   public static final Codec<Holder<Biome>> CODEC = RegistryFileCodec.create(Registries.BIOME, DIRECT_CODEC);
   public static final Codec<HolderSet<Biome>> LIST_CODEC = RegistryCodecs.homogeneousList(Registries.BIOME, DIRECT_CODEC);
   private static final PerlinSimplexNoise TEMPERATURE_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(1234L)), ImmutableList.of(0));
   static final PerlinSimplexNoise FROZEN_TEMPERATURE_NOISE = new PerlinSimplexNoise(
      new WorldgenRandom(new LegacyRandomSource(3456L)), ImmutableList.of(-2, -1, 0)
   );
   @Deprecated(
      forRemoval = true
   )
   public static final PerlinSimplexNoise BIOME_INFO_NOISE = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), ImmutableList.of(0));
   private static final int TEMPERATURE_CACHE_SIZE = 1024;
   private final Biome.ClimateSettings climateSettings;
   private final BiomeGenerationSettings generationSettings;
   private final MobSpawnSettings mobSettings;
   private final EnvironmentAttributeMap attributes;
   private final BiomeSpecialEffects specialEffects;
   private final ThreadLocal<Long2FloatLinkedOpenHashMap> temperatureCache = ThreadLocal.withInitial(() -> {
      Long2FloatLinkedOpenHashMap $$0x = new Long2FloatLinkedOpenHashMap(1024, 0.25F) {
         protected void rehash(int $$0) {
         }
      };
      $$0x.defaultReturnValue(Float.NaN);
      return $$0x;
   });

   Biome(Biome.ClimateSettings $$0, EnvironmentAttributeMap $$1, BiomeSpecialEffects $$2, BiomeGenerationSettings $$3, MobSpawnSettings $$4) {
      this.climateSettings = $$0;
      this.generationSettings = $$3;
      this.mobSettings = $$4;
      this.attributes = $$1;
      this.specialEffects = $$2;
   }

   public MobSpawnSettings getMobSettings() {
      return this.mobSettings;
   }

   public boolean hasPrecipitation() {
      return this.climateSettings.hasPrecipitation();
   }

   public Biome.Precipitation getPrecipitationAt(BlockPos $$0, int $$1) {
      if (!this.hasPrecipitation()) {
         return Biome.Precipitation.NONE;
      } else {
         return this.coldEnoughToSnow($$0, $$1) ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN;
      }
   }

   private float getHeightAdjustedTemperature(BlockPos $$0, int $$1) {
      float $$2 = this.climateSettings.temperatureModifier.modifyTemperature($$0, this.getBaseTemperature());
      int $$3 = $$1 + 17;
      if ($$0.getY() > $$3) {
         float $$4 = (float)(TEMPERATURE_NOISE.getValue($$0.getX() / 8.0F, $$0.getZ() / 8.0F, false) * 8.0);
         return $$2 - ($$4 + $$0.getY() - $$3) * 0.05F / 40.0F;
      } else {
         return $$2;
      }
   }

   @Deprecated
   private float getTemperature(BlockPos $$0, int $$1) {
      long $$2 = $$0.asLong();
      Long2FloatLinkedOpenHashMap $$3 = this.temperatureCache.get();
      float $$4 = $$3.get($$2);
      if (!Float.isNaN($$4)) {
         return $$4;
      } else {
         float $$5 = this.getHeightAdjustedTemperature($$0, $$1);
         if ($$3.size() == 1024) {
            $$3.removeFirstFloat();
         }

         $$3.put($$2, $$5);
         return $$5;
      }
   }

   public boolean shouldFreeze(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      return this.shouldFreeze($$0, $$1, true);
   }

   public boolean shouldFreeze(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, boolean $$2) {
      if (this.warmEnoughToRain($$1, $$0.getSeaLevel())) {
         return false;
      } else {
         if ($$0.isInsideBuildHeight($$1.getY()) && $$0.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, $$1) < 10) {
            BlockState $$3 = $$0.getBlockState($$1);
            FluidState $$4 = $$0.getFluidState($$1);
            if ($$4.getType() == Fluids.WATER && $$3.getBlock() instanceof LiquidBlock) {
               if (!$$2) {
                  return true;
               }

               boolean $$5 = $$0.isWaterAt($$1.west()) && $$0.isWaterAt($$1.east()) && $$0.isWaterAt($$1.north()) && $$0.isWaterAt($$1.south());
               if (!$$5) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean coldEnoughToSnow(BlockPos $$0, int $$1) {
      return !this.warmEnoughToRain($$0, $$1);
   }

   public boolean warmEnoughToRain(BlockPos $$0, int $$1) {
      return this.getTemperature($$0, $$1) >= 0.15F;
   }

   public boolean shouldMeltFrozenOceanIcebergSlightly(BlockPos $$0, int $$1) {
      return this.getTemperature($$0, $$1) > 0.1F;
   }

   public boolean shouldSnow(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      if (this.getPrecipitationAt($$1, $$0.getSeaLevel()) != Biome.Precipitation.SNOW) {
         return false;
      } else {
         if ($$0.isInsideBuildHeight($$1.getY()) && $$0.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, $$1) < 10) {
            BlockState $$2 = $$0.getBlockState($$1);
            if (($$2.isAir() || $$2.is(Blocks.SNOW)) && Blocks.SNOW.defaultBlockState().canSurvive($$0, $$1)) {
               return true;
            }
         }

         return false;
      }
   }

   public BiomeGenerationSettings getGenerationSettings() {
      return this.generationSettings;
   }

   public int getGrassColor(double $$0, double $$1) {
      int $$2 = this.getBaseGrassColor();
      return this.specialEffects.grassColorModifier().modifyColor($$0, $$1, $$2);
   }

   private int getBaseGrassColor() {
      Optional<Integer> $$0 = this.specialEffects.grassColorOverride();
      return $$0.isPresent() ? $$0.get() : this.getGrassColorFromTexture();
   }

   private int getGrassColorFromTexture() {
      double $$0 = Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double $$1 = Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return net.minecraft.world.level.GrassColor.get($$0, $$1);
   }

   public int getFoliageColor() {
      return this.specialEffects.foliageColorOverride().orElseGet(this::getFoliageColorFromTexture);
   }

   private int getFoliageColorFromTexture() {
      double $$0 = Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double $$1 = Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return net.minecraft.world.level.FoliageColor.get($$0, $$1);
   }

   public int getDryFoliageColor() {
      return this.specialEffects.dryFoliageColorOverride().orElseGet(this::getDryFoliageColorFromTexture);
   }

   private int getDryFoliageColorFromTexture() {
      double $$0 = Mth.clamp(this.climateSettings.temperature, 0.0F, 1.0F);
      double $$1 = Mth.clamp(this.climateSettings.downfall, 0.0F, 1.0F);
      return net.minecraft.world.level.DryFoliageColor.get($$0, $$1);
   }

   public float getBaseTemperature() {
      return this.climateSettings.temperature;
   }

   public EnvironmentAttributeMap getAttributes() {
      return this.attributes;
   }

   public BiomeSpecialEffects getSpecialEffects() {
      return this.specialEffects;
   }

   public int getWaterColor() {
      return this.specialEffects.waterColor();
   }

   public static class BiomeBuilder {
      private boolean hasPrecipitation = true;
      @Nullable
      private Float temperature;
      private Biome.TemperatureModifier temperatureModifier = Biome.TemperatureModifier.NONE;
      @Nullable
      private Float downfall;
      private final Builder attributes = EnvironmentAttributeMap.builder();
      @Nullable
      private BiomeSpecialEffects specialEffects;
      @Nullable
      private MobSpawnSettings mobSpawnSettings;
      @Nullable
      private BiomeGenerationSettings generationSettings;

      public Biome.BiomeBuilder hasPrecipitation(boolean $$0) {
         this.hasPrecipitation = $$0;
         return this;
      }

      public Biome.BiomeBuilder temperature(float $$0) {
         this.temperature = $$0;
         return this;
      }

      public Biome.BiomeBuilder downfall(float $$0) {
         this.downfall = $$0;
         return this;
      }

      public Biome.BiomeBuilder putAttributes(EnvironmentAttributeMap $$0) {
         this.attributes.putAll($$0);
         return this;
      }

      public Biome.BiomeBuilder putAttributes(Builder $$0) {
         return this.putAttributes($$0.build());
      }

      public <Value> Biome.BiomeBuilder setAttribute(EnvironmentAttribute<Value> $$0, Value $$1) {
         this.attributes.set($$0, $$1);
         return this;
      }

      public <Value, Parameter> Biome.BiomeBuilder modifyAttribute(EnvironmentAttribute<Value> $$0, AttributeModifier<Value, Parameter> $$1, Parameter $$2) {
         this.attributes.modify($$0, $$1, $$2);
         return this;
      }

      public Biome.BiomeBuilder specialEffects(BiomeSpecialEffects $$0) {
         this.specialEffects = $$0;
         return this;
      }

      public Biome.BiomeBuilder mobSpawnSettings(MobSpawnSettings $$0) {
         this.mobSpawnSettings = $$0;
         return this;
      }

      public Biome.BiomeBuilder generationSettings(BiomeGenerationSettings $$0) {
         this.generationSettings = $$0;
         return this;
      }

      public Biome.BiomeBuilder temperatureAdjustment(Biome.TemperatureModifier $$0) {
         this.temperatureModifier = $$0;
         return this;
      }

      public Biome build() {
         if (this.temperature != null
            && this.downfall != null
            && this.specialEffects != null
            && this.mobSpawnSettings != null
            && this.generationSettings != null) {
            return new Biome(
               new Biome.ClimateSettings(this.hasPrecipitation, this.temperature, this.temperatureModifier, this.downfall),
               this.attributes.build(),
               this.specialEffects,
               this.generationSettings,
               this.mobSpawnSettings
            );
         } else {
            throw new IllegalStateException("You are missing parameters to build a proper biome\n" + this);
         }
      }

      @Override
      public String toString() {
         return "BiomeBuilder{\nhasPrecipitation="
            + this.hasPrecipitation
            + ",\ntemperature="
            + this.temperature
            + ",\ntemperatureModifier="
            + this.temperatureModifier
            + ",\ndownfall="
            + this.downfall
            + ",\nspecialEffects="
            + this.specialEffects
            + ",\nmobSpawnSettings="
            + this.mobSpawnSettings
            + ",\ngenerationSettings="
            + this.generationSettings
            + ",\n}";
      }
   }

   record ClimateSettings(boolean hasPrecipitation, float temperature, Biome.TemperatureModifier temperatureModifier, float downfall) {
      public static final MapCodec<Biome.ClimateSettings> CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.BOOL.fieldOf("has_precipitation").forGetter($$0x -> $$0x.hasPrecipitation),
               Codec.FLOAT.fieldOf("temperature").forGetter($$0x -> $$0x.temperature),
               Biome.TemperatureModifier.CODEC
                  .optionalFieldOf("temperature_modifier", Biome.TemperatureModifier.NONE)
                  .forGetter($$0x -> $$0x.temperatureModifier),
               Codec.FLOAT.fieldOf("downfall").forGetter($$0x -> $$0x.downfall)
            )
            .apply($$0, Biome.ClimateSettings::new)
      );
   }

   public static enum Precipitation implements StringRepresentable {
      NONE("none"),
      RAIN("rain"),
      SNOW("snow");

      public static final Codec<Biome.Precipitation> CODEC = StringRepresentable.fromEnum(Biome.Precipitation::values);
      private final String name;

      private Precipitation(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   public static enum TemperatureModifier implements StringRepresentable {
      NONE("none") {
         @Override
         public float modifyTemperature(BlockPos $$0, float $$1) {
            return $$1;
         }
      },
      FROZEN("frozen") {
         @Override
         public float modifyTemperature(BlockPos $$0, float $$1) {
            double $$2 = Biome.FROZEN_TEMPERATURE_NOISE.getValue($$0.getX() * 0.05, $$0.getZ() * 0.05, false) * 7.0;
            double $$3 = Biome.BIOME_INFO_NOISE.getValue($$0.getX() * 0.2, $$0.getZ() * 0.2, false);
            double $$4 = $$2 + $$3;
            if ($$4 < 0.3) {
               double $$5 = Biome.BIOME_INFO_NOISE.getValue($$0.getX() * 0.09, $$0.getZ() * 0.09, false);
               if ($$5 < 0.8) {
                  return 0.2F;
               }
            }

            return $$1;
         }
      };

      private final String name;
      public static final Codec<Biome.TemperatureModifier> CODEC = StringRepresentable.fromEnum(Biome.TemperatureModifier::values);

      public abstract float modifyTemperature(BlockPos var1, float var2);

      TemperatureModifier(final String $$0) {
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
