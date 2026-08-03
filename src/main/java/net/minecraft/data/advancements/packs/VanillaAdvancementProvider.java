package net.minecraft.data.advancements.packs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.advancements.AdvancementProvider;

public class VanillaAdvancementProvider {
   public static AdvancementProvider create(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
      return new AdvancementProvider(
         $$0,
         $$1,
         List.of(
            new VanillaTheEndAdvancements(),
            new VanillaHusbandryAdvancements(),
            new VanillaAdventureAdvancements(),
            new VanillaNetherAdvancements(),
            new VanillaStoryAdvancements()
         )
      );
   }
}
