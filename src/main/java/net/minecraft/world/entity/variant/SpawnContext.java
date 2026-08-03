package net.minecraft.world.entity.variant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;

public record SpawnContext(BlockPos pos, ServerLevelAccessor level, EnvironmentAttributeReader environmentAttributes, Holder<Biome> biome) {
   public static SpawnContext create(ServerLevelAccessor $$0, BlockPos $$1) {
      Holder<Biome> $$2 = $$0.getBiome($$1);
      return new SpawnContext($$1, $$0, $$0.environmentAttributes(), $$2);
   }
}
