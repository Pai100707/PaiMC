package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

public class FlatLevelGeneratorPresetTags {
   public static final net.minecraft.tags.TagKey<FlatLevelGeneratorPreset> VISIBLE = create("visible");

   private FlatLevelGeneratorPresetTags() {
   }

   private static net.minecraft.tags.TagKey<FlatLevelGeneratorPreset> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, Identifier.withDefaultNamespace($$0));
   }
}
