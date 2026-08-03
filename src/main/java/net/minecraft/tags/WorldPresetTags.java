package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

public class WorldPresetTags {
   public static final net.minecraft.tags.TagKey<WorldPreset> NORMAL = create("normal");
   public static final net.minecraft.tags.TagKey<WorldPreset> EXTENDED = create("extended");

   private WorldPresetTags() {
   }

   private static net.minecraft.tags.TagKey<WorldPreset> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.WORLD_PRESET, Identifier.withDefaultNamespace($$0));
   }
}
