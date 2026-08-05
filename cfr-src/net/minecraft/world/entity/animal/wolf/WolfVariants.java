/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class WolfVariants {
    public static final ResourceKey<WolfVariant> PALE = WolfVariants.createKey("pale");
    public static final ResourceKey<WolfVariant> SPOTTED = WolfVariants.createKey("spotted");
    public static final ResourceKey<WolfVariant> SNOWY = WolfVariants.createKey("snowy");
    public static final ResourceKey<WolfVariant> BLACK = WolfVariants.createKey("black");
    public static final ResourceKey<WolfVariant> ASHEN = WolfVariants.createKey("ashen");
    public static final ResourceKey<WolfVariant> RUSTY = WolfVariants.createKey("rusty");
    public static final ResourceKey<WolfVariant> WOODS = WolfVariants.createKey("woods");
    public static final ResourceKey<WolfVariant> CHESTNUT = WolfVariants.createKey("chestnut");
    public static final ResourceKey<WolfVariant> STRIPED = WolfVariants.createKey("striped");
    public static final ResourceKey<WolfVariant> DEFAULT = PALE;

    private static ResourceKey<WolfVariant> createKey(String $$0) {
        return ResourceKey.create(Registries.WOLF_VARIANT, Identifier.withDefaultNamespace($$0));
    }

    private static void register(BootstrapContext<WolfVariant> $$0, ResourceKey<WolfVariant> $$1, String $$2, ResourceKey<Biome> $$3) {
        WolfVariants.register($$0, $$1, $$2, WolfVariants.highPrioBiome(HolderSet.direct($$0.lookup(Registries.BIOME).getOrThrow($$3))));
    }

    private static void register(BootstrapContext<WolfVariant> $$0, ResourceKey<WolfVariant> $$1, String $$2, TagKey<Biome> $$3) {
        WolfVariants.register($$0, $$1, $$2, WolfVariants.highPrioBiome($$0.lookup(Registries.BIOME).getOrThrow($$3)));
    }

    private static SpawnPrioritySelectors highPrioBiome(HolderSet<Biome> $$0) {
        return SpawnPrioritySelectors.single(new BiomeCheck($$0), 1);
    }

    private static void register(BootstrapContext<WolfVariant> $$0, ResourceKey<WolfVariant> $$1, String $$2, SpawnPrioritySelectors $$3) {
        Identifier $$4 = Identifier.withDefaultNamespace("entity/wolf/" + $$2);
        Identifier $$5 = Identifier.withDefaultNamespace("entity/wolf/" + $$2 + "_tame");
        Identifier $$6 = Identifier.withDefaultNamespace("entity/wolf/" + $$2 + "_angry");
        $$0.register($$1, new WolfVariant(new WolfVariant.AssetInfo(new ClientAsset.ResourceTexture($$4), new ClientAsset.ResourceTexture($$5), new ClientAsset.ResourceTexture($$6)), $$3));
    }

    public static void bootstrap(BootstrapContext<WolfVariant> $$0) {
        WolfVariants.register($$0, PALE, "wolf", SpawnPrioritySelectors.fallback(0));
        WolfVariants.register($$0, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
        WolfVariants.register($$0, SNOWY, "wolf_snowy", Biomes.GROVE);
        WolfVariants.register($$0, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
        WolfVariants.register($$0, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
        WolfVariants.register($$0, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
        WolfVariants.register($$0, WOODS, "wolf_woods", Biomes.FOREST);
        WolfVariants.register($$0, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        WolfVariants.register($$0, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
    }
}

