package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;

public record BiomeCheck(HolderSet<Biome> requiredBiomes) implements SpawnCondition {
   public static final MapCodec<BiomeCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(BiomeCheck::requiredBiomes)).apply($$0, BiomeCheck::new)
   );

   public boolean test(SpawnContext $$0) {
      return this.requiredBiomes.contains($$0.biome());
   }

   @Override
   public MapCodec<BiomeCheck> codec() {
      return MAP_CODEC;
   }
}
