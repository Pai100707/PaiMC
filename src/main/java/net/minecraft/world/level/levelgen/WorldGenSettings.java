package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;

public record WorldGenSettings(WorldOptions options, WorldDimensions dimensions) {
   public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(WorldOptions.CODEC.forGetter(WorldGenSettings::options), WorldDimensions.CODEC.forGetter(WorldGenSettings::dimensions))
         .apply($$0, $$0.stable(WorldGenSettings::new))
   );

   public static <T> DataResult<T> encode(DynamicOps<T> $$0, WorldOptions $$1, WorldDimensions $$2) {
      return CODEC.encodeStart($$0, new WorldGenSettings($$1, $$2));
   }

   public static <T> DataResult<T> encode(DynamicOps<T> $$0, WorldOptions $$1, RegistryAccess $$2) {
      return encode($$0, $$1, new WorldDimensions($$2.lookupOrThrow(Registries.LEVEL_STEM)));
   }
}
