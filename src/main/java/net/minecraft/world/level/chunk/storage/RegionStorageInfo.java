package net.minecraft.world.level.chunk.storage;

import net.minecraft.resources.ResourceKey;

public record RegionStorageInfo(String level, ResourceKey<net.minecraft.world.level.Level> dimension, String type) {
   public RegionStorageInfo withTypeSuffix(String $$0) {
      return new RegionStorageInfo(this.level, this.dimension, this.type + $$0);
   }
}
