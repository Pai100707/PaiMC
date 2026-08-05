/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldPresetTagsProvider
extends KeyTagProvider<WorldPreset> {
    public WorldPresetTagsProvider(PackOutput $$0, CompletableFuture<HolderLookup.Provider> $$1) {
        super($$0, Registries.WORLD_PRESET, $$1);
    }

    @Override
    protected void addTags(HolderLookup.Provider $$0) {
        this.tag(WorldPresetTags.NORMAL).add(WorldPresets.NORMAL).add(WorldPresets.FLAT).add(WorldPresets.LARGE_BIOMES).add(WorldPresets.AMPLIFIED).add(WorldPresets.SINGLE_BIOME_SURFACE);
        this.tag(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).add(WorldPresets.DEBUG);
    }
}

