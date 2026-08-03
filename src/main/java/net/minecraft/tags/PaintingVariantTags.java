package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public class PaintingVariantTags {
   public static final net.minecraft.tags.TagKey<PaintingVariant> PLACEABLE = create("placeable");

   private PaintingVariantTags() {
   }

   private static net.minecraft.tags.TagKey<PaintingVariant> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.PAINTING_VARIANT, Identifier.withDefaultNamespace($$0));
   }
}
