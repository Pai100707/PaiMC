/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.feline;

import java.util.List;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.variant.MoonBrightnessCheck;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.entity.variant.StructureCheck;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface CatVariants {
    public static final ResourceKey<CatVariant> TABBY = CatVariants.createKey("tabby");
    public static final ResourceKey<CatVariant> BLACK = CatVariants.createKey("black");
    public static final ResourceKey<CatVariant> RED = CatVariants.createKey("red");
    public static final ResourceKey<CatVariant> SIAMESE = CatVariants.createKey("siamese");
    public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = CatVariants.createKey("british_shorthair");
    public static final ResourceKey<CatVariant> CALICO = CatVariants.createKey("calico");
    public static final ResourceKey<CatVariant> PERSIAN = CatVariants.createKey("persian");
    public static final ResourceKey<CatVariant> RAGDOLL = CatVariants.createKey("ragdoll");
    public static final ResourceKey<CatVariant> WHITE = CatVariants.createKey("white");
    public static final ResourceKey<CatVariant> JELLIE = CatVariants.createKey("jellie");
    public static final ResourceKey<CatVariant> ALL_BLACK = CatVariants.createKey("all_black");

    private static ResourceKey<CatVariant> createKey(String $$0) {
        return ResourceKey.create(Registries.CAT_VARIANT, Identifier.withDefaultNamespace($$0));
    }

    public static void bootstrap(BootstrapContext<CatVariant> $$0) {
        HolderGetter<Structure> $$1 = $$0.lookup(Registries.STRUCTURE);
        CatVariants.registerForAnyConditions($$0, TABBY, "entity/cat/tabby");
        CatVariants.registerForAnyConditions($$0, BLACK, "entity/cat/black");
        CatVariants.registerForAnyConditions($$0, RED, "entity/cat/red");
        CatVariants.registerForAnyConditions($$0, SIAMESE, "entity/cat/siamese");
        CatVariants.registerForAnyConditions($$0, BRITISH_SHORTHAIR, "entity/cat/british_shorthair");
        CatVariants.registerForAnyConditions($$0, CALICO, "entity/cat/calico");
        CatVariants.registerForAnyConditions($$0, PERSIAN, "entity/cat/persian");
        CatVariants.registerForAnyConditions($$0, RAGDOLL, "entity/cat/ragdoll");
        CatVariants.registerForAnyConditions($$0, WHITE, "entity/cat/white");
        CatVariants.registerForAnyConditions($$0, JELLIE, "entity/cat/jellie");
        CatVariants.register($$0, ALL_BLACK, "entity/cat/all_black", new SpawnPrioritySelectors(List.of(new PriorityProvider.Selector(new StructureCheck($$1.getOrThrow(StructureTags.CATS_SPAWN_AS_BLACK)), 1), new PriorityProvider.Selector(new MoonBrightnessCheck(MinMaxBounds.Doubles.atLeast(0.9)), 0))));
    }

    private static void registerForAnyConditions(BootstrapContext<CatVariant> $$0, ResourceKey<CatVariant> $$1, String $$2) {
        CatVariants.register($$0, $$1, $$2, SpawnPrioritySelectors.fallback(0));
    }

    private static void register(BootstrapContext<CatVariant> $$0, ResourceKey<CatVariant> $$1, String $$2, SpawnPrioritySelectors $$3) {
        $$0.register($$1, new CatVariant(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace($$2)), $$3));
    }
}

