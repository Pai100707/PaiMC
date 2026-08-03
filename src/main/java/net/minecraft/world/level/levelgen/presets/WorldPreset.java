package net.minecraft.world.level.levelgen.presets;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

public class WorldPreset {
   public static final Codec<WorldPreset> DIRECT_CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.unboundedMap(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC).fieldOf("dimensions").forGetter($$0x -> $$0x.dimensions)
            )
            .apply($$0, WorldPreset::new)
      )
      .validate(WorldPreset::requireOverworld);
   public static final Codec<Holder<WorldPreset>> CODEC = RegistryFileCodec.create(Registries.WORLD_PRESET, DIRECT_CODEC);
   private final Map<ResourceKey<LevelStem>, LevelStem> dimensions;

   public WorldPreset(Map<ResourceKey<LevelStem>, LevelStem> $$0) {
      this.dimensions = $$0;
   }

   private ImmutableMap<ResourceKey<LevelStem>, LevelStem> dimensionsInOrder() {
      Builder<ResourceKey<LevelStem>, LevelStem> $$0 = ImmutableMap.builder();
      WorldDimensions.keysInOrder(this.dimensions.keySet().stream()).forEach($$1 -> {
         LevelStem $$2 = this.dimensions.get($$1);
         if ($$2 != null) {
            $$0.put($$1, $$2);
         }
      });
      return $$0.build();
   }

   public WorldDimensions createWorldDimensions() {
      return new WorldDimensions(this.dimensionsInOrder());
   }

   public Optional<LevelStem> overworld() {
      return Optional.ofNullable(this.dimensions.get(LevelStem.OVERWORLD));
   }

   private static DataResult<WorldPreset> requireOverworld(WorldPreset $$0) {
      return $$0.overworld().isEmpty() ? DataResult.error(() -> "Missing overworld dimension") : DataResult.success($$0, Lifecycle.stable());
   }
}
