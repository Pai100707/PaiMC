package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

public class MultiNoiseBiomeSourceParameterList {
   public static final Codec<MultiNoiseBiomeSourceParameterList> DIRECT_CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            MultiNoiseBiomeSourceParameterList.Preset.CODEC.fieldOf("preset").forGetter($$0x -> $$0x.preset), RegistryOps.retrieveGetter(Registries.BIOME)
         )
         .apply($$0, MultiNoiseBiomeSourceParameterList::new)
   );
   public static final Codec<Holder<MultiNoiseBiomeSourceParameterList>> CODEC = RegistryFileCodec.create(
      Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, DIRECT_CODEC
   );
   private final MultiNoiseBiomeSourceParameterList.Preset preset;
   private final Climate.ParameterList<Holder<Biome>> parameters;

   public MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset $$0, HolderGetter<Biome> $$1) {
      this.preset = $$0;
      this.parameters = $$0.provider.apply($$1::getOrThrow);
   }

   public Climate.ParameterList<Holder<Biome>> parameters() {
      return this.parameters;
   }

   public static Map<MultiNoiseBiomeSourceParameterList.Preset, Climate.ParameterList<ResourceKey<Biome>>> knownPresets() {
      return MultiNoiseBiomeSourceParameterList.Preset.BY_NAME
         .values()
         .stream()
         .collect(Collectors.toMap($$0 -> (MultiNoiseBiomeSourceParameterList.Preset)$$0, $$0 -> $$0.provider().apply($$0x -> $$0x)));
   }

   public record Preset(Identifier id, MultiNoiseBiomeSourceParameterList.Preset.SourceProvider provider) {
      public static final MultiNoiseBiomeSourceParameterList.Preset NETHER = new MultiNoiseBiomeSourceParameterList.Preset(
         Identifier.withDefaultNamespace("nether"),
         new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> $$0) {
               return new Climate.ParameterList<>(
                  List.of(
                     Pair.of(Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), $$0.apply(Biomes.NETHER_WASTES)),
                     Pair.of(Climate.parameters(0.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), $$0.apply(Biomes.SOUL_SAND_VALLEY)),
                     Pair.of(Climate.parameters(0.4F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), $$0.apply(Biomes.CRIMSON_FOREST)),
                     Pair.of(Climate.parameters(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.375F), $$0.apply(Biomes.WARPED_FOREST)),
                     Pair.of(Climate.parameters(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.175F), $$0.apply(Biomes.BASALT_DELTAS))
                  )
               );
            }
         }
      );
      public static final MultiNoiseBiomeSourceParameterList.Preset OVERWORLD = new MultiNoiseBiomeSourceParameterList.Preset(
         Identifier.withDefaultNamespace("overworld"), new MultiNoiseBiomeSourceParameterList.Preset.SourceProvider() {
            @Override
            public <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> $$0) {
               return MultiNoiseBiomeSourceParameterList.Preset.generateOverworldBiomes($$0);
            }
         }
      );
      static final Map<Identifier, MultiNoiseBiomeSourceParameterList.Preset> BY_NAME = Stream.of(NETHER, OVERWORLD)
         .collect(Collectors.toMap(MultiNoiseBiomeSourceParameterList.Preset::id, $$0 -> (MultiNoiseBiomeSourceParameterList.Preset)$$0));
      public static final Codec<MultiNoiseBiomeSourceParameterList.Preset> CODEC = Identifier.CODEC
         .flatXmap(
            $$0 -> Optional.ofNullable(BY_NAME.get($$0)).<DataResult>map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown preset: " + $$0)),
            $$0 -> DataResult.success($$0.id)
         );

      static <T> Climate.ParameterList<T> generateOverworldBiomes(Function<ResourceKey<Biome>, T> $$0) {
         Builder<Pair<Climate.ParameterPoint, T>> $$1 = ImmutableList.builder();
         new OverworldBiomeBuilder().addBiomes($$2 -> $$1.add($$2.mapSecond($$0)));
         return new Climate.ParameterList<>($$1.build());
      }

      public Stream<ResourceKey<Biome>> usedBiomes() {
         return this.provider.apply($$0 -> $$0).values().stream().<ResourceKey<Biome>>map(Pair::getSecond).distinct();
      }

      @FunctionalInterface
      interface SourceProvider {
         <T> Climate.ParameterList<T> apply(Function<ResourceKey<Biome>, T> var1);
      }
   }
}
