package net.minecraft.world.level.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class MultiNoiseBiomeSourceParameterLists {
   public static final ResourceKey<MultiNoiseBiomeSourceParameterList> NETHER = register("nether");
   public static final ResourceKey<MultiNoiseBiomeSourceParameterList> OVERWORLD = register("overworld");

   public static void bootstrap(BootstrapContext<MultiNoiseBiomeSourceParameterList> $$0) {
      HolderGetter<Biome> $$1 = $$0.lookup(Registries.BIOME);
      $$0.register(NETHER, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.NETHER, $$1));
      $$0.register(OVERWORLD, new MultiNoiseBiomeSourceParameterList(MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD, $$1));
   }

   private static ResourceKey<MultiNoiseBiomeSourceParameterList> register(String $$0) {
      return ResourceKey.create(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, Identifier.withDefaultNamespace($$0));
   }
}
