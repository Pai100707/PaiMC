/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.data.loot;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.slf4j.Logger;

public class LootTableProvider
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput.PathProvider pathProvider;
    private final Set<ResourceKey<LootTable>> requiredTables;
    private final List<SubProviderEntry> subProviders;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public LootTableProvider(PackOutput $$0, Set<ResourceKey<LootTable>> $$1, List<SubProviderEntry> $$2, CompletableFuture<HolderLookup.Provider> $$3) {
        this.pathProvider = $$0.createRegistryElementsPathProvider(Registries.LOOT_TABLE);
        this.subProviders = $$2;
        this.requiredTables = $$1;
        this.registries = $$3;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        return this.registries.thenCompose($$1 -> this.run($$0, (HolderLookup.Provider)$$1));
    }

    private CompletableFuture<?> run(CachedOutput $$02, HolderLookup.Provider $$12) {
        MappedRegistry<LootTable> $$22 = new MappedRegistry<LootTable>(Registries.LOOT_TABLE, Lifecycle.experimental());
        Object2ObjectOpenHashMap $$3 = new Object2ObjectOpenHashMap();
        this.subProviders.forEach(arg_0 -> LootTableProvider.lambda$run$2($$12, (Map)$$3, $$22, arg_0));
        $$22.freeze();
        ProblemReporter.Collector $$4 = new ProblemReporter.Collector();
        RegistryAccess.Frozen $$5 = new RegistryAccess.ImmutableRegistryAccess(List.of($$22)).freeze();
        ValidationContext $$6 = new ValidationContext($$4, LootContextParamSets.ALL_PARAMS, $$5);
        Sets.SetView $$7 = Sets.difference(this.requiredTables, $$22.registryKeySet());
        for (ResourceKey $$8 : $$7) {
            $$4.report(new MissingTableProblem($$8));
        }
        $$22.listElements().forEach($$1 -> ((LootTable)$$1.value()).validate($$6.setContextKeySet(((LootTable)$$1.value()).getParamSet()).enterElement(new ProblemReporter.RootElementPathElement($$1.key()), $$1.key())));
        if (!$$4.isEmpty()) {
            $$4.forEach(($$0, $$1) -> LOGGER.warn("Found validation problem in {}: {}", $$0, (Object)$$1.description()));
            throw new IllegalStateException("Failed to validate loot tables, see logs");
        }
        return CompletableFuture.allOf((CompletableFuture[])$$22.entrySet().stream().map($$2 -> {
            ResourceKey $$3 = (ResourceKey)$$2.getKey();
            LootTable $$4 = (LootTable)$$2.getValue();
            Path $$5 = this.pathProvider.json($$3.identifier());
            return DataProvider.saveStable($$02, $$12, LootTable.DIRECT_CODEC, $$4, $$5);
        }).toArray(CompletableFuture[]::new));
    }

    private static Identifier sequenceIdForLootTable(ResourceKey<LootTable> $$0) {
        return $$0.identifier();
    }

    @Override
    public final String getName() {
        return "Loot Tables";
    }

    private static /* synthetic */ void lambda$run$2(HolderLookup.Provider $$0, Map $$1, WritableRegistry $$2, SubProviderEntry $$32) {
        $$32.provider().apply($$0).generate(($$3, $$4) -> {
            Identifier $$5 = LootTableProvider.sequenceIdForLootTable($$3);
            Identifier $$6 = $$1.put(RandomSequence.seedForKey($$5), $$5);
            if ($$6 != null) {
                Util.logAndPauseIfInIde("Loot table random sequence seed collision on " + String.valueOf($$6) + " and " + String.valueOf($$3.identifier()));
            }
            $$4.setRandomSequence($$5);
            LootTable $$7 = $$4.setParamSet($$1.paramSet).build();
            $$2.register($$3, $$7, RegistrationInfo.BUILT_IN);
        });
    }

    public record MissingTableProblem(ResourceKey<LootTable> id) implements ProblemReporter.Problem
    {
        @Override
        public String description() {
            return "Missing built-in table: " + String.valueOf(this.id.identifier());
        }
    }

    public static final class SubProviderEntry
    extends Record {
        private final Function<HolderLookup.Provider, LootTableSubProvider> provider;
        final ContextKeySet paramSet;

        public SubProviderEntry(Function<HolderLookup.Provider, LootTableSubProvider> $$0, ContextKeySet $$1) {
            this.provider = $$0;
            this.paramSet = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SubProviderEntry.class, "provider;paramSet", "provider", "paramSet"}, this, $$0);
        }

        public Function<HolderLookup.Provider, LootTableSubProvider> provider() {
            return this.provider;
        }

        public ContextKeySet paramSet() {
            return this.paramSet;
        }
    }
}

