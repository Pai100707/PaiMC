/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 */
package net.minecraft.data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.SuppressForbidden;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
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

public class Main {
    @SuppressForbidden(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] $$02) throws IOException {
        SharedConstants.tryDetectVersion();
        OptionParser $$1 = new OptionParser();
        AbstractOptionSpec $$2 = $$1.accepts("help", "Show the help menu").forHelp();
        OptionSpecBuilder $$3 = $$1.accepts("server", "Include server generators");
        OptionSpecBuilder $$4 = $$1.accepts("dev", "Include development tools");
        OptionSpecBuilder $$5 = $$1.accepts("reports", "Include data reports");
        $$1.accepts("validate", "Validate inputs");
        OptionSpecBuilder $$6 = $$1.accepts("all", "Include all generators");
        ArgumentAcceptingOptionSpec $$7 = $$1.accepts("output", "Output folder").withRequiredArg().defaultsTo((Object)"generated", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec $$8 = $$1.accepts("input", "Input folder").withRequiredArg();
        OptionSet $$9 = $$1.parse($$02);
        if ($$9.has((OptionSpec)$$2) || !$$9.hasOptions()) {
            $$1.printHelpOn((OutputStream)System.out);
            return;
        }
        Path $$10 = Paths.get((String)$$7.value($$9), new String[0]);
        boolean $$11 = $$9.has((OptionSpec)$$6);
        boolean $$12 = $$11 || $$9.has((OptionSpec)$$3);
        boolean $$13 = $$11 || $$9.has((OptionSpec)$$4);
        boolean $$14 = $$11 || $$9.has((OptionSpec)$$5);
        List<Path> $$15 = $$9.valuesOf((OptionSpec)$$8).stream().map($$0 -> Paths.get($$0, new String[0])).toList();
        DataGenerator $$16 = new DataGenerator($$10, SharedConstants.getCurrentVersion(), true);
        Main.addServerProviders($$16, $$15, $$12, $$13, $$14);
        $$16.run();
        Util.shutdownExecutors();
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        return $$2 -> (DataProvider)$$0.apply($$2, $$1);
    }

    public static void addServerProviders(DataGenerator $$02, Collection<Path> $$12, boolean $$2, boolean $$3, boolean $$4) {
        DataGenerator.PackGenerator $$5 = $$02.getVanillaPack($$2);
        $$5.addProvider($$1 -> new SnbtToNbt($$1, $$12).addFilter(new StructureUpdater()));
        CompletableFuture<HolderLookup.Provider> $$6 = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        DataGenerator.PackGenerator $$7 = $$02.getVanillaPack($$2);
        $$7.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, $$6));
        $$7.addProvider(Main.bindRegistries(VanillaAdvancementProvider::create, $$6));
        $$7.addProvider(Main.bindRegistries(VanillaLootTableProvider::create, $$6));
        $$7.addProvider(Main.bindRegistries(VanillaRecipeProvider.Runner::new, $$6));
        TagsProvider $$8 = $$7.addProvider(Main.bindRegistries(VanillaBlockTagsProvider::new, $$6));
        TagsProvider $$9 = $$7.addProvider(Main.bindRegistries(VanillaItemTagsProvider::new, $$6));
        TagsProvider $$10 = $$7.addProvider(Main.bindRegistries(BiomeTagsProvider::new, $$6));
        TagsProvider $$11 = $$7.addProvider(Main.bindRegistries(BannerPatternTagsProvider::new, $$6));
        TagsProvider $$122 = $$7.addProvider(Main.bindRegistries(StructureTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(DamageTypeTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(DialogTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(EntityTypeTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(FlatLevelGeneratorPresetTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(FluidTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(GameEventTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(InstrumentTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(PaintingVariantTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(PoiTypeTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(WorldPresetTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(VanillaEnchantmentTagsProvider::new, $$6));
        $$7.addProvider(Main.bindRegistries(TimelineTagsProvider::new, $$6));
        DataGenerator.PackGenerator $$13 = $$02.getVanillaPack($$3);
        $$13.addProvider($$1 -> new NbtToSnbt($$1, $$12));
        DataGenerator.PackGenerator $$14 = $$02.getVanillaPack($$4);
        $$14.addProvider(Main.bindRegistries(BiomeParametersDumpReport::new, $$6));
        $$14.addProvider(Main.bindRegistries(ItemListReport::new, $$6));
        $$14.addProvider(Main.bindRegistries(BlockListReport::new, $$6));
        $$14.addProvider(Main.bindRegistries(CommandsReport::new, $$6));
        $$14.addProvider(RegistryDumpReport::new);
        $$14.addProvider(PacketReport::new);
        $$14.addProvider(DatapackStructureReport::new);
        $$14.addProvider(JsonRpcApiSchema::new);
        CompletableFuture<RegistrySetBuilder.PatchedRegistries> $$15 = TradeRebalanceRegistries.createLookup($$6);
        CompletionStage $$16 = $$15.thenApply(RegistrySetBuilder.PatchedRegistries::patches);
        DataGenerator.PackGenerator $$17 = $$02.getBuiltinDatapack($$2, "trade_rebalance");
        $$17.addProvider(Main.bindRegistries(RegistriesDatapackGenerator::new, (CompletableFuture<HolderLookup.Provider>)$$16));
        $$17.addProvider($$0 -> PackMetadataGenerator.forFeaturePack($$0, Component.translatable("dataPack.trade_rebalance.description"), FeatureFlagSet.of(FeatureFlags.TRADE_REBALANCE)));
        $$17.addProvider(Main.bindRegistries(TradeRebalanceLootTableProvider::create, $$6));
        $$17.addProvider(Main.bindRegistries(TradeRebalanceEnchantmentTagsProvider::new, $$6));
        DataGenerator.PackGenerator $$18 = $$02.getBuiltinDatapack($$2, "redstone_experiments");
        $$18.addProvider($$0 -> PackMetadataGenerator.forFeaturePack($$0, Component.translatable("dataPack.redstone_experiments.description"), FeatureFlagSet.of(FeatureFlags.REDSTONE_EXPERIMENTS)));
        DataGenerator.PackGenerator $$19 = $$02.getBuiltinDatapack($$2, "minecart_improvements");
        $$19.addProvider($$0 -> PackMetadataGenerator.forFeaturePack($$0, Component.translatable("dataPack.minecart_improvements.description"), FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS)));
    }
}

