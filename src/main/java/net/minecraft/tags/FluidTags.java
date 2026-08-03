package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

public final class FluidTags {
   public static final net.minecraft.tags.TagKey<Fluid> WATER = create("water");
   public static final net.minecraft.tags.TagKey<Fluid> LAVA = create("lava");

   private FluidTags() {
   }

   private static net.minecraft.tags.TagKey<Fluid> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.FLUID, Identifier.withDefaultNamespace($$0));
   }
}
