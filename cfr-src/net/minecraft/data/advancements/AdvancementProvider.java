/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;

public class AdvancementProvider
implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final List<AdvancementSubProvider> subProviders;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public AdvancementProvider(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1, List<AdvancementSubProvider> $$2) {
        this.pathProvider = $$0.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
        this.subProviders = $$2;
        this.registries = $$1;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput $$0) {
        return this.registries.thenCompose($$1 -> {
            HashSet $$2 = new HashSet();
            ArrayList $$3 = new ArrayList();
            Consumer<AdvancementHolder> $$42 = $$4 -> {
                if (!$$2.add($$4.id())) {
                    throw new IllegalStateException("Duplicate advancement " + String.valueOf($$4.id()));
                }
                Path $$5 = this.pathProvider.json($$4.id());
                $$3.add(DataProvider.saveStable($$0, $$1, Advancement.CODEC, $$4.value(), $$5));
            };
            for (AdvancementSubProvider $$5 : this.subProviders) {
                $$5.generate((HolderLookup.Provider)$$1, $$42);
            }
            return CompletableFuture.allOf((CompletableFuture[])$$3.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public final String getName() {
        return "Advancements";
    }
}

