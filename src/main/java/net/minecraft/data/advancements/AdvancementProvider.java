package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public class AdvancementProvider implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput.PathProvider pathProvider;
   private final List<AdvancementSubProvider> subProviders;
   private final CompletableFuture<Provider> registries;

   public AdvancementProvider(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1, List<AdvancementSubProvider> $$2) {
      this.pathProvider = $$0.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
      this.subProviders = $$2;
      this.registries = $$1;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      return this.registries.thenCompose($$1 -> {
         Set<Identifier> $$2 = new HashSet<>();
         List<CompletableFuture<?>> $$3 = new ArrayList<>();
         Consumer<AdvancementHolder> $$4 = $$4x -> {
            if (!$$2.add($$4x.id())) {
               throw new IllegalStateException("Duplicate advancement " + $$4x.id());
            } else {
               Path $$5x = this.pathProvider.json($$4x.id());
               $$3.add(net.minecraft.data.DataProvider.saveStable($$0, $$1, Advancement.CODEC, $$4x.value(), $$5x));
            }
         };

         for (AdvancementSubProvider $$5 : this.subProviders) {
            $$5.generate($$1, $$4);
         }

         return CompletableFuture.allOf($$3.toArray(CompletableFuture[]::new));
      });
   }

   @Override
   public final String getName() {
      return "Advancements";
   }
}
