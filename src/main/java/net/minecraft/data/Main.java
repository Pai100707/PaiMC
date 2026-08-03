package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistrySetBuilder.PatchedRegistries;
import net.minecraft.data.advancements.packs.VanillaAdvancementProvider;
import net.minecraft.data.info.BiomeParametersDumpReport;
import net.minecraft.data.info.BlockListReport;
import net.minecraft.data.info.CommandsReport;
import net.minecraft.data.info.DatapackStructureReport;
import net.minecraft.data.info.ItemListReport;
import net.minecraft.data.info.PacketReport;
import net.minecraft.data.info.RegistryDumpReport;
import net.minecraft.data.loot.packs.TradeRebalanceLootTableProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.TradeRebalanceRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.data.structures.SnbtToNbt;
import net.minecraft.data.structures.StructureUpdater;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.data.tags.DialogTagsProvider;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.FlatLevelGeneratorPresetTagsProvider;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.GameEventTagsProvider;
import net.minecraft.data.tags.InstrumentTagsProvider;
import net.minecraft.data.tags.PaintingVariantTagsProvider;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.data.tags.StructureTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.data.tags.TimelineTagsProvider;
import net.minecraft.data.tags.TradeRebalanceEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaBlockTagsProvider;
import net.minecraft.data.tags.VanillaEnchantmentTagsProvider;
import net.minecraft.data.tags.VanillaItemTagsProvider;
import net.minecraft.data.tags.WorldPresetTagsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.jsonrpc.dataprovider.JsonRpcApiSchema;
import net.minecraft.util.Util;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.levelgen.structure.Structure;

public class Main {
   @SuppressForbidden(
      reason = "System.out needed before bootstrap"
   )
   @DontObfuscate
   public static void main(String[] $$0) throws IOException {
      SharedConstants.tryDetectVersion();
      OptionParser $$1 = new OptionParser();
      OptionSpec<Void> $$2 = $$1.accepts("help", "Show the help menu").forHelp();
      OptionSpec<Void> $$3 = $$1.accepts("server", "Include server generators");
      OptionSpec<Void> $$4 = $$1.accepts("dev", "Include development tools");
      OptionSpec<Void> $$5 = $$1.accepts("reports", "Include data reports");
      $$1.accepts("validate", "Validate inputs");
      OptionSpec<Void> $$6 = $$1.accepts("all", "Include all generators");
      OptionSpec<String> $$7 = $$1.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated", new String[0]);
      OptionSpec<String> $$8 = $$1.accepts("input", "Input folder").withRequiredArg();
      OptionSet $$9 = $$1.parse($$0);
      if (!$$9.has($$2) && $$9.hasOptions()) {
         Path $$10 = Paths.get((String)$$7.value($$9));
         boolean $$11 = $$9.has($$6);
         boolean $$12 = $$11 || $$9.has($$3);
         boolean $$13 = $$11 || $$9.has($$4);
         boolean $$14 = $$11 || $$9.has($$5);
         Collection<Path> $$15 = $$9.valuesOf($$8).stream().map($$0x -> Paths.get($$0x)).toList();
         net.minecraft.data.DataGenerator $$16 = new net.minecraft.data.DataGenerator($$10, SharedConstants.getCurrentVersion(), true);
         addServerProviders($$16, $$15, $$12, $$13, $$14);
         $$16.run();
         Util.shutdownExecutors();
      } else {
         $$1.printHelpOn(System.out);
      }
   }

   private static <T extends net.minecraft.data.DataProvider> net.minecraft.data.DataProvider.Factory<T> bindRegistries(
      BiFunction<net.minecraft.data.PackOutput, CompletableFuture<Provider>, T> $$0, CompletableFuture<Provider> $$1
   ) {
      return $$2 -> $$0.apply($$2, $$1);
   }

   public static void addServerProviders(net.minecraft.data.DataGenerator $$0, Collection<Path> $$1, boolean $$2, boolean $$3, boolean $$4) {
      net.minecraft.data.DataGenerator.PackGenerator $$5 = $$0.getVanillaPack($$2);
      $$5.addProvider($$1x -> new SnbtToNbt($$1x, $$1).addFilter(new StructureUpdater()));
      CompletableFuture<Provider> $$6 = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
      net.minecraft.data.DataGenerator.PackGenerator $$7 = $$0.getVanillaPack($$2);
      $$7.addProvider(bindRegistries(RegistriesDatapackGenerator::new, $$6));
      $$7.addProvider(bindRegistries(VanillaAdvancementProvider::create, $$6));
      $$7.addProvider(bindRegistries(VanillaLootTableProvider::create, $$6));
      $$7.addProvider(bindRegistries(VanillaRecipeProvider.Runner::new, $$6));
      TagsProvider<Block> $$8 = $$7.addProvider(bindRegistries(VanillaBlockTagsProvider::new, $$6));
      TagsProvider<Item> $$9 = $$7.addProvider(bindRegistries(VanillaItemTagsProvider::new, $$6));
      TagsProvider<Biome> $$10 = $$7.addProvider(bindRegistries(BiomeTagsProvider::new, $$6));
      TagsProvider<BannerPattern> $$11 = $$7.addProvider(bindRegistries(BannerPatternTagsProvider::new, $$6));
      TagsProvider<Structure> $$12 = $$7.addProvider(bindRegistries(StructureTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(DamageTypeTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(DialogTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(EntityTypeTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(FluidTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(GameEventTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(InstrumentTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(PaintingVariantTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(PoiTypeTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(WorldPresetTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(VanillaEnchantmentTagsProvider::new, $$6));
      $$7.addProvider(bindRegistries(TimelineTagsProvider::new, $$6));
      $$7 = $$0.getVanillaPack($$3);
      $$7.addProvider($$1x -> new NbtToSnbt($$1x, $$1));
      $$7 = $$0.getVanillaPack($$4);
      $$7.addProvider(bindRegistries(BiomeParametersDumpReport::new, $$6));
      $$7.addProvider(bindRegistries(ItemListReport::new, $$6));
      $$7.addProvider(bindRegistries(BlockListReport::new, $$6));
      $$7.addProvider(bindRegistries(CommandsReport::new, $$6));
      $$7.addProvider(RegistryDumpReport::new);
      $$7.addProvider(PacketReport::new);
      $$7.addProvider(DatapackStructureReport::new);
      $$7.addProvider(JsonRpcApiSchema::new);
      CompletableFuture<PatchedRegistries> $$15 = TradeRebalanceRegistries.createLookup($$6);
      CompletableFuture<Provider> $$16 = $$15.thenApply(PatchedRegistries::patches);
      net.minecraft.data.DataGenerator.PackGenerator $$17 = $$0.getBuiltinDatapack($$2, "trade_rebalance");
      $$17.addProvider(bindRegistries(RegistriesDatapackGenerator::new, $$16));
      $$17.addProvider(
         $$0x -> PackMetadataGenerator.forFeaturePack(
            $$0x, Component.translatable("dataPack.trade_rebalance.description"), FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE)
         )
      );
      $$17.addProvider(bindRegistries(TradeRebalanceLootTableProvider::create, $$6));
      $$17.addProvider(bindRegistries(TradeRebalanceEnchantmentTagsProvider::new, $$6));
      $$7 = $$0.getBuiltinDatapack($$2, "redstone_experiments");
      $$7.addProvider(
         $$0x -> PackMetadataGenerator.forFeaturePack(
            $$0x, Component.translatable("dataPack.redstone_experiments.description"), FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)
         )
      );
      $$7 = $$0.getBuiltinDatapack($$2, "minecart_improvements");
      $$7.addProvider(
         $$0x -> PackMetadataGenerator.forFeaturePack(
            $$0x, Component.translatable("dataPack.minecart_improvements.description"), FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS)
         )
      );
   }
}
