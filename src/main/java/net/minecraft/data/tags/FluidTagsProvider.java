package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends IntrinsicHolderTagsProvider<Fluid> {
   public FluidTagsProvider(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      super($$0, Registries.FLUID, $$1, $$0x -> $$0x.builtInRegistryHolder().key());
   }

   @Override
   protected void addTags(Provider $$0) {
      this.tag(FluidTags.WATER).add(Fluids.WATER, Fluids.FLOWING_WATER);
      this.tag(FluidTags.LAVA).add(Fluids.LAVA, Fluids.FLOWING_LAVA);
   }
}
