/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class BlockFamily {
    private final Block baseBlock;
    final Map<Variant, Block> variants = Maps.newHashMap();
    boolean generateModel = true;
    boolean generateRecipe = true;
    @Nullable String recipeGroupPrefix;
    @Nullable String recipeUnlockedBy;

    BlockFamily(Block $$0) {
        this.baseBlock = $$0;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    public Map<Variant, Block> getVariants() {
        return this.variants;
    }

    public Block get(Variant $$0) {
        return this.variants.get((Object)$$0);
    }

    public boolean shouldGenerateModel() {
        return this.generateModel;
    }

    public boolean shouldGenerateRecipe() {
        return this.generateRecipe;
    }

    public Optional<String> getRecipeGroupPrefix() {
        if (StringUtil.isBlank(this.recipeGroupPrefix)) {
            return Optional.empty();
        }
        return Optional.of(this.recipeGroupPrefix);
    }

    public Optional<String> getRecipeUnlockedBy() {
        if (StringUtil.isBlank(this.recipeUnlockedBy)) {
            return Optional.empty();
        }
        return Optional.of(this.recipeUnlockedBy);
    }

    public static class Builder {
        private final BlockFamily family;

        public Builder(Block $$0) {
            this.family = new BlockFamily($$0);
        }

        public BlockFamily getFamily() {
            return this.family;
        }

        public Builder button(Block $$0) {
            this.family.variants.put(Variant.BUTTON, $$0);
            return this;
        }

        public Builder chiseled(Block $$0) {
            this.family.variants.put(Variant.CHISELED, $$0);
            return this;
        }

        public Builder mosaic(Block $$0) {
            this.family.variants.put(Variant.MOSAIC, $$0);
            return this;
        }

        public Builder cracked(Block $$0) {
            this.family.variants.put(Variant.CRACKED, $$0);
            return this;
        }

        public Builder cut(Block $$0) {
            this.family.variants.put(Variant.CUT, $$0);
            return this;
        }

        public Builder door(Block $$0) {
            this.family.variants.put(Variant.DOOR, $$0);
            return this;
        }

        public Builder customFence(Block $$0) {
            this.family.variants.put(Variant.CUSTOM_FENCE, $$0);
            return this;
        }

        public Builder fence(Block $$0) {
            this.family.variants.put(Variant.FENCE, $$0);
            return this;
        }

        public Builder customFenceGate(Block $$0) {
            this.family.variants.put(Variant.CUSTOM_FENCE_GATE, $$0);
            return this;
        }

        public Builder fenceGate(Block $$0) {
            this.family.variants.put(Variant.FENCE_GATE, $$0);
            return this;
        }

        public Builder sign(Block $$0, Block $$1) {
            this.family.variants.put(Variant.SIGN, $$0);
            this.family.variants.put(Variant.WALL_SIGN, $$1);
            return this;
        }

        public Builder slab(Block $$0) {
            this.family.variants.put(Variant.SLAB, $$0);
            return this;
        }

        public Builder stairs(Block $$0) {
            this.family.variants.put(Variant.STAIRS, $$0);
            return this;
        }

        public Builder pressurePlate(Block $$0) {
            this.family.variants.put(Variant.PRESSURE_PLATE, $$0);
            return this;
        }

        public Builder polished(Block $$0) {
            this.family.variants.put(Variant.POLISHED, $$0);
            return this;
        }

        public Builder trapdoor(Block $$0) {
            this.family.variants.put(Variant.TRAPDOOR, $$0);
            return this;
        }

        public Builder wall(Block $$0) {
            this.family.variants.put(Variant.WALL, $$0);
            return this;
        }

        public Builder dontGenerateModel() {
            this.family.generateModel = false;
            return this;
        }

        public Builder dontGenerateRecipe() {
            this.family.generateRecipe = false;
            return this;
        }

        public Builder recipeGroupPrefix(String $$0) {
            this.family.recipeGroupPrefix = $$0;
            return this;
        }

        public Builder recipeUnlockedBy(String $$0) {
            this.family.recipeUnlockedBy = $$0;
            return this;
        }
    }

    public static enum Variant {
        BUTTON("button"),
        CHISELED("chiseled"),
        CRACKED("cracked"),
        CUT("cut"),
        DOOR("door"),
        CUSTOM_FENCE("fence"),
        FENCE("fence"),
        CUSTOM_FENCE_GATE("fence_gate"),
        FENCE_GATE("fence_gate"),
        MOSAIC("mosaic"),
        SIGN("sign"),
        SLAB("slab"),
        STAIRS("stairs"),
        PRESSURE_PLATE("pressure_plate"),
        POLISHED("polished"),
        TRAPDOOR("trapdoor"),
        WALL("wall"),
        WALL_SIGN("wall_sign");

        private final String recipeGroup;

        private Variant(String $$0) {
            this.recipeGroup = $$0;
        }

        public String getRecipeGroup() {
            return this.recipeGroup;
        }
    }
}

