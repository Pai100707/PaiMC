/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerAdvancementManager
extends SimpleJsonResourceReloadListener<Advancement> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private Map<Identifier, AdvancementHolder> advancements = Map.of();
    private AdvancementTree tree = new AdvancementTree();
    private final HolderLookup.Provider registries;

    public ServerAdvancementManager(HolderLookup.Provider $$0) {
        super($$0, Advancement.CODEC, Registries.ADVANCEMENT);
        this.registries = $$0;
    }

    @Override
    protected void apply(Map<Identifier, Advancement> $$0, ResourceManager $$12, ProfilerFiller $$22) {
        ImmutableMap.Builder $$3 = ImmutableMap.builder();
        $$0.forEach(($$1, $$2) -> {
            this.validate((Identifier)$$1, (Advancement)$$2);
            $$3.put($$1, (Object)new AdvancementHolder((Identifier)$$1, (Advancement)$$2));
        });
        this.advancements = $$3.buildOrThrow();
        AdvancementTree $$4 = new AdvancementTree();
        $$4.addAll(this.advancements.values());
        for (AdvancementNode $$5 : $$4.roots()) {
            if (!$$5.holder().value().display().isPresent()) continue;
            TreeNodePosition.run($$5);
        }
        this.tree = $$4;
    }

    private void validate(Identifier $$0, Advancement $$1) {
        ProblemReporter.Collector $$2 = new ProblemReporter.Collector();
        $$1.validate($$2, this.registries);
        if (!$$2.isEmpty()) {
            LOGGER.warn("Found validation problems in advancement {}: \n{}", (Object)$$0, (Object)$$2.getReport());
        }
    }

    public @Nullable AdvancementHolder get(Identifier $$0) {
        return this.advancements.get($$0);
    }

    public AdvancementTree tree() {
        return this.tree;
    }

    public Collection<AdvancementHolder> getAllAdvancements() {
        return this.advancements.values();
    }
}

