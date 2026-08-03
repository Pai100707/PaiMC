package net.minecraft.data.loot;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.Collector;
import net.minecraft.util.ProblemReporter.Problem;
import net.minecraft.util.ProblemReporter.RootElementPathElement;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.RandomSupport.Seed128bit;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider implements net.minecraft.data.DataProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final net.minecraft.data.PackOutput.PathProvider pathProvider;
   private final Set<ResourceKey<LootTable>> requiredTables;
   private final List<LootTableProvider.SubProviderEntry> subProviders;
   private final CompletableFuture<Provider> registries;

   public LootTableProvider(
      net.minecraft.data.PackOutput $$0, Set<ResourceKey<LootTable>> $$1, List<LootTableProvider.SubProviderEntry> $$2, CompletableFuture<Provider> $$3
   ) {
      this.pathProvider = $$0.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
      this.subProviders = $$2;
      this.requiredTables = $$1;
      this.registries = $$3;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      return this.registries.thenCompose($$1 -> this.run($$0, $$1));
   }

   private CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0, Provider $$1) {
      WritableRegistry<LootTable> $$2 = new MappedRegistry(Registries.LOOT_TABLE, Lifecycle.experimental());
      Map<Seed128bit, Identifier> $$3 = new Object2ObjectOpenHashMap();
      this.subProviders.forEach($$3x -> $$3x.provider().apply($$1).generate(($$3xx, $$4x) -> {
         Identifier $$5x = sequenceIdForLootTable($$3xx);
         Identifier $$6x = $$3.put(RandomSequence.seedForKey($$5x), $$5x);
         if ($$6x != null) {
            Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + $$6x + " and " + $$3xx.identifier());
         }

         $$4x.setRandomSequence($$5x);
         LootTable $$7 = $$4x.setParamSet($$3x.paramSet).build();
         $$2.register($$3xx, $$7, RegistrationInfo.BUILT_IN);
      }));
      $$2.freeze();
      Collector $$4 = new Collector();
      net.minecraft.core.HolderGetter.Provider $$5 = new ImmutableRegistryAccess(List.of($$2)).freeze();
      ValidationContext $$6 = new ValidationContext($$4, LootContextParamSets.ALL_PARAMS, $$5);

      for (ResourceKey<LootTable> $$8 : Sets.difference(this.requiredTables, $$2.registryKeySet())) {
         $$4.report(new LootTableProvider.MissingTableProblem($$8));
      }

      $$2.listElements()
         .forEach(
            $$1x -> ((LootTable)$$1x.value())
               .validate($$6.setContextKeySet(((LootTable)$$1x.value()).getParamSet()).enterElement(new RootElementPathElement($$1x.key()), $$1x.key()))
         );
      if (!$$4.isEmpty()) {
         $$4.forEach(($$0x, $$1x) -> LOGGER.warn("Found validation problem in {}: {}", $$0x, $$1x.description()));
         throw new IllegalStateException("Failed to validate loot tables, see logs");
      } else {
         return CompletableFuture.allOf($$2.entrySet().stream().map($$2x -> {
            ResourceKey<LootTable> $$3x = (ResourceKey<LootTable>)$$2x.getKey();
            LootTable $$4x = (LootTable)$$2x.getValue();
            Path $$5x = this.pathProvider.json($$3x.identifier());
            return net.minecraft.data.DataProvider.saveStable($$0, $$1, LootTable.DIRECT_CODEC, $$4x, $$5x);
         }).toArray(CompletableFuture[]::new));
      }
   }

   private static Identifier sequenceIdForLootTable(ResourceKey<LootTable> $$0) {
      return $$0.identifier();
   }

   @Override
   public final String getName() {
      return "Loot Tables";
   }

   public record MissingTableProblem(ResourceKey<LootTable> id) implements Problem {
      public String description() {
         return "Missing built-in table: " + this.id.identifier();
      }
   }

   public record SubProviderEntry(Function<Provider, LootTableSubProvider> provider, ContextKeySet paramSet) {
   }
}
