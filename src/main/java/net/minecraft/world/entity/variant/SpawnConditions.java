package net.minecraft.world.entity.variant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;

public class SpawnConditions {
   public static MapCodec<? extends SpawnCondition> bootstrap(Registry<MapCodec<? extends SpawnCondition>> $$0) {
      Registry.register($$0, "structure", StructureCheck.MAP_CODEC);
      Registry.register($$0, "moon_brightness", MoonBrightnessCheck.MAP_CODEC);
      return (MapCodec<? extends SpawnCondition>)Registry.register($$0, "biome", BiomeCheck.MAP_CODEC);
   }
}
