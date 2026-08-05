/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.references;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public class Blocks {
    public static final ResourceKey<Block> PUMPKIN = Blocks.createKey("pumpkin");
    public static final ResourceKey<Block> PUMPKIN_STEM = Blocks.createKey("pumpkin_stem");
    public static final ResourceKey<Block> ATTACHED_PUMPKIN_STEM = Blocks.createKey("attached_pumpkin_stem");
    public static final ResourceKey<Block> MELON = Blocks.createKey("melon");
    public static final ResourceKey<Block> MELON_STEM = Blocks.createKey("melon_stem");
    public static final ResourceKey<Block> ATTACHED_MELON_STEM = Blocks.createKey("attached_melon_stem");

    private static ResourceKey<Block> createKey(String $$0) {
        return ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace($$0));
    }
}

