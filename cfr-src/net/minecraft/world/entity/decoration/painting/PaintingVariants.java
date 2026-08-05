/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.decoration.painting;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

public class PaintingVariants {
    public static final ResourceKey<PaintingVariant> KEBAB = PaintingVariants.create("kebab");
    public static final ResourceKey<PaintingVariant> AZTEC = PaintingVariants.create("aztec");
    public static final ResourceKey<PaintingVariant> ALBAN = PaintingVariants.create("alban");
    public static final ResourceKey<PaintingVariant> AZTEC2 = PaintingVariants.create("aztec2");
    public static final ResourceKey<PaintingVariant> BOMB = PaintingVariants.create("bomb");
    public static final ResourceKey<PaintingVariant> PLANT = PaintingVariants.create("plant");
    public static final ResourceKey<PaintingVariant> WASTELAND = PaintingVariants.create("wasteland");
    public static final ResourceKey<PaintingVariant> POOL = PaintingVariants.create("pool");
    public static final ResourceKey<PaintingVariant> COURBET = PaintingVariants.create("courbet");
    public static final ResourceKey<PaintingVariant> SEA = PaintingVariants.create("sea");
    public static final ResourceKey<PaintingVariant> SUNSET = PaintingVariants.create("sunset");
    public static final ResourceKey<PaintingVariant> CREEBET = PaintingVariants.create("creebet");
    public static final ResourceKey<PaintingVariant> WANDERER = PaintingVariants.create("wanderer");
    public static final ResourceKey<PaintingVariant> GRAHAM = PaintingVariants.create("graham");
    public static final ResourceKey<PaintingVariant> MATCH = PaintingVariants.create("match");
    public static final ResourceKey<PaintingVariant> BUST = PaintingVariants.create("bust");
    public static final ResourceKey<PaintingVariant> STAGE = PaintingVariants.create("stage");
    public static final ResourceKey<PaintingVariant> VOID = PaintingVariants.create("void");
    public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = PaintingVariants.create("skull_and_roses");
    public static final ResourceKey<PaintingVariant> WITHER = PaintingVariants.create("wither");
    public static final ResourceKey<PaintingVariant> FIGHTERS = PaintingVariants.create("fighters");
    public static final ResourceKey<PaintingVariant> POINTER = PaintingVariants.create("pointer");
    public static final ResourceKey<PaintingVariant> PIGSCENE = PaintingVariants.create("pigscene");
    public static final ResourceKey<PaintingVariant> BURNING_SKULL = PaintingVariants.create("burning_skull");
    public static final ResourceKey<PaintingVariant> SKELETON = PaintingVariants.create("skeleton");
    public static final ResourceKey<PaintingVariant> DONKEY_KONG = PaintingVariants.create("donkey_kong");
    public static final ResourceKey<PaintingVariant> EARTH = PaintingVariants.create("earth");
    public static final ResourceKey<PaintingVariant> WIND = PaintingVariants.create("wind");
    public static final ResourceKey<PaintingVariant> WATER = PaintingVariants.create("water");
    public static final ResourceKey<PaintingVariant> FIRE = PaintingVariants.create("fire");
    public static final ResourceKey<PaintingVariant> BAROQUE = PaintingVariants.create("baroque");
    public static final ResourceKey<PaintingVariant> HUMBLE = PaintingVariants.create("humble");
    public static final ResourceKey<PaintingVariant> MEDITATIVE = PaintingVariants.create("meditative");
    public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = PaintingVariants.create("prairie_ride");
    public static final ResourceKey<PaintingVariant> UNPACKED = PaintingVariants.create("unpacked");
    public static final ResourceKey<PaintingVariant> BACKYARD = PaintingVariants.create("backyard");
    public static final ResourceKey<PaintingVariant> BOUQUET = PaintingVariants.create("bouquet");
    public static final ResourceKey<PaintingVariant> CAVEBIRD = PaintingVariants.create("cavebird");
    public static final ResourceKey<PaintingVariant> CHANGING = PaintingVariants.create("changing");
    public static final ResourceKey<PaintingVariant> COTAN = PaintingVariants.create("cotan");
    public static final ResourceKey<PaintingVariant> ENDBOSS = PaintingVariants.create("endboss");
    public static final ResourceKey<PaintingVariant> FERN = PaintingVariants.create("fern");
    public static final ResourceKey<PaintingVariant> FINDING = PaintingVariants.create("finding");
    public static final ResourceKey<PaintingVariant> LOWMIST = PaintingVariants.create("lowmist");
    public static final ResourceKey<PaintingVariant> ORB = PaintingVariants.create("orb");
    public static final ResourceKey<PaintingVariant> OWLEMONS = PaintingVariants.create("owlemons");
    public static final ResourceKey<PaintingVariant> PASSAGE = PaintingVariants.create("passage");
    public static final ResourceKey<PaintingVariant> POND = PaintingVariants.create("pond");
    public static final ResourceKey<PaintingVariant> SUNFLOWERS = PaintingVariants.create("sunflowers");
    public static final ResourceKey<PaintingVariant> TIDES = PaintingVariants.create("tides");
    public static final ResourceKey<PaintingVariant> DENNIS = PaintingVariants.create("dennis");

    public static void bootstrap(BootstrapContext<PaintingVariant> $$0) {
        PaintingVariants.register($$0, KEBAB, 1, 1);
        PaintingVariants.register($$0, AZTEC, 1, 1);
        PaintingVariants.register($$0, ALBAN, 1, 1);
        PaintingVariants.register($$0, AZTEC2, 1, 1);
        PaintingVariants.register($$0, BOMB, 1, 1);
        PaintingVariants.register($$0, PLANT, 1, 1);
        PaintingVariants.register($$0, WASTELAND, 1, 1);
        PaintingVariants.register($$0, POOL, 2, 1);
        PaintingVariants.register($$0, COURBET, 2, 1);
        PaintingVariants.register($$0, SEA, 2, 1);
        PaintingVariants.register($$0, SUNSET, 2, 1);
        PaintingVariants.register($$0, CREEBET, 2, 1);
        PaintingVariants.register($$0, WANDERER, 1, 2);
        PaintingVariants.register($$0, GRAHAM, 1, 2);
        PaintingVariants.register($$0, MATCH, 2, 2);
        PaintingVariants.register($$0, BUST, 2, 2);
        PaintingVariants.register($$0, STAGE, 2, 2);
        PaintingVariants.register($$0, VOID, 2, 2);
        PaintingVariants.register($$0, SKULL_AND_ROSES, 2, 2);
        PaintingVariants.register($$0, WITHER, 2, 2, false);
        PaintingVariants.register($$0, FIGHTERS, 4, 2);
        PaintingVariants.register($$0, POINTER, 4, 4);
        PaintingVariants.register($$0, PIGSCENE, 4, 4);
        PaintingVariants.register($$0, BURNING_SKULL, 4, 4);
        PaintingVariants.register($$0, SKELETON, 4, 3);
        PaintingVariants.register($$0, EARTH, 2, 2, false);
        PaintingVariants.register($$0, WIND, 2, 2, false);
        PaintingVariants.register($$0, WATER, 2, 2, false);
        PaintingVariants.register($$0, FIRE, 2, 2, false);
        PaintingVariants.register($$0, DONKEY_KONG, 4, 3);
        PaintingVariants.register($$0, BAROQUE, 2, 2);
        PaintingVariants.register($$0, HUMBLE, 2, 2);
        PaintingVariants.register($$0, MEDITATIVE, 1, 1);
        PaintingVariants.register($$0, PRAIRIE_RIDE, 1, 2);
        PaintingVariants.register($$0, UNPACKED, 4, 4);
        PaintingVariants.register($$0, BACKYARD, 3, 4);
        PaintingVariants.register($$0, BOUQUET, 3, 3);
        PaintingVariants.register($$0, CAVEBIRD, 3, 3);
        PaintingVariants.register($$0, CHANGING, 4, 2);
        PaintingVariants.register($$0, COTAN, 3, 3);
        PaintingVariants.register($$0, ENDBOSS, 3, 3);
        PaintingVariants.register($$0, FERN, 3, 3);
        PaintingVariants.register($$0, FINDING, 4, 2);
        PaintingVariants.register($$0, LOWMIST, 4, 2);
        PaintingVariants.register($$0, ORB, 4, 4);
        PaintingVariants.register($$0, OWLEMONS, 3, 3);
        PaintingVariants.register($$0, PASSAGE, 4, 2);
        PaintingVariants.register($$0, POND, 3, 4);
        PaintingVariants.register($$0, SUNFLOWERS, 3, 3);
        PaintingVariants.register($$0, TIDES, 3, 3);
        PaintingVariants.register($$0, DENNIS, 3, 3);
    }

    private static void register(BootstrapContext<PaintingVariant> $$0, ResourceKey<PaintingVariant> $$1, int $$2, int $$3) {
        PaintingVariants.register($$0, $$1, $$2, $$3, true);
    }

    private static void register(BootstrapContext<PaintingVariant> $$0, ResourceKey<PaintingVariant> $$1, int $$2, int $$3, boolean $$4) {
        $$0.register($$1, new PaintingVariant($$2, $$3, $$1.identifier(), Optional.of(Component.translatable($$1.identifier().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW)), $$4 ? Optional.of(Component.translatable($$1.identifier().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY)) : Optional.empty()));
    }

    private static ResourceKey<PaintingVariant> create(String $$0) {
        return ResourceKey.create(Registries.PAINTING_VARIANT, Identifier.withDefaultNamespace($$0));
    }
}

