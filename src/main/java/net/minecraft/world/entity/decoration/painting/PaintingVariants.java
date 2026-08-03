package net.minecraft.world.entity.decoration.painting;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class PaintingVariants {
   public static final ResourceKey<PaintingVariant> KEBAB = create("kebab");
   public static final ResourceKey<PaintingVariant> AZTEC = create("aztec");
   public static final ResourceKey<PaintingVariant> ALBAN = create("alban");
   public static final ResourceKey<PaintingVariant> AZTEC2 = create("aztec2");
   public static final ResourceKey<PaintingVariant> BOMB = create("bomb");
   public static final ResourceKey<PaintingVariant> PLANT = create("plant");
   public static final ResourceKey<PaintingVariant> WASTELAND = create("wasteland");
   public static final ResourceKey<PaintingVariant> POOL = create("pool");
   public static final ResourceKey<PaintingVariant> COURBET = create("courbet");
   public static final ResourceKey<PaintingVariant> SEA = create("sea");
   public static final ResourceKey<PaintingVariant> SUNSET = create("sunset");
   public static final ResourceKey<PaintingVariant> CREEBET = create("creebet");
   public static final ResourceKey<PaintingVariant> WANDERER = create("wanderer");
   public static final ResourceKey<PaintingVariant> GRAHAM = create("graham");
   public static final ResourceKey<PaintingVariant> MATCH = create("match");
   public static final ResourceKey<PaintingVariant> BUST = create("bust");
   public static final ResourceKey<PaintingVariant> STAGE = create("stage");
   public static final ResourceKey<PaintingVariant> VOID = create("void");
   public static final ResourceKey<PaintingVariant> SKULL_AND_ROSES = create("skull_and_roses");
   public static final ResourceKey<PaintingVariant> WITHER = create("wither");
   public static final ResourceKey<PaintingVariant> FIGHTERS = create("fighters");
   public static final ResourceKey<PaintingVariant> POINTER = create("pointer");
   public static final ResourceKey<PaintingVariant> PIGSCENE = create("pigscene");
   public static final ResourceKey<PaintingVariant> BURNING_SKULL = create("burning_skull");
   public static final ResourceKey<PaintingVariant> SKELETON = create("skeleton");
   public static final ResourceKey<PaintingVariant> DONKEY_KONG = create("donkey_kong");
   public static final ResourceKey<PaintingVariant> EARTH = create("earth");
   public static final ResourceKey<PaintingVariant> WIND = create("wind");
   public static final ResourceKey<PaintingVariant> WATER = create("water");
   public static final ResourceKey<PaintingVariant> FIRE = create("fire");
   public static final ResourceKey<PaintingVariant> BAROQUE = create("baroque");
   public static final ResourceKey<PaintingVariant> HUMBLE = create("humble");
   public static final ResourceKey<PaintingVariant> MEDITATIVE = create("meditative");
   public static final ResourceKey<PaintingVariant> PRAIRIE_RIDE = create("prairie_ride");
   public static final ResourceKey<PaintingVariant> UNPACKED = create("unpacked");
   public static final ResourceKey<PaintingVariant> BACKYARD = create("backyard");
   public static final ResourceKey<PaintingVariant> BOUQUET = create("bouquet");
   public static final ResourceKey<PaintingVariant> CAVEBIRD = create("cavebird");
   public static final ResourceKey<PaintingVariant> CHANGING = create("changing");
   public static final ResourceKey<PaintingVariant> COTAN = create("cotan");
   public static final ResourceKey<PaintingVariant> ENDBOSS = create("endboss");
   public static final ResourceKey<PaintingVariant> FERN = create("fern");
   public static final ResourceKey<PaintingVariant> FINDING = create("finding");
   public static final ResourceKey<PaintingVariant> LOWMIST = create("lowmist");
   public static final ResourceKey<PaintingVariant> ORB = create("orb");
   public static final ResourceKey<PaintingVariant> OWLEMONS = create("owlemons");
   public static final ResourceKey<PaintingVariant> PASSAGE = create("passage");
   public static final ResourceKey<PaintingVariant> POND = create("pond");
   public static final ResourceKey<PaintingVariant> SUNFLOWERS = create("sunflowers");
   public static final ResourceKey<PaintingVariant> TIDES = create("tides");
   public static final ResourceKey<PaintingVariant> DENNIS = create("dennis");

   public static void bootstrap(BootstrapContext<PaintingVariant> $$0) {
      register($$0, KEBAB, 1, 1);
      register($$0, AZTEC, 1, 1);
      register($$0, ALBAN, 1, 1);
      register($$0, AZTEC2, 1, 1);
      register($$0, BOMB, 1, 1);
      register($$0, PLANT, 1, 1);
      register($$0, WASTELAND, 1, 1);
      register($$0, POOL, 2, 1);
      register($$0, COURBET, 2, 1);
      register($$0, SEA, 2, 1);
      register($$0, SUNSET, 2, 1);
      register($$0, CREEBET, 2, 1);
      register($$0, WANDERER, 1, 2);
      register($$0, GRAHAM, 1, 2);
      register($$0, MATCH, 2, 2);
      register($$0, BUST, 2, 2);
      register($$0, STAGE, 2, 2);
      register($$0, VOID, 2, 2);
      register($$0, SKULL_AND_ROSES, 2, 2);
      register($$0, WITHER, 2, 2, false);
      register($$0, FIGHTERS, 4, 2);
      register($$0, POINTER, 4, 4);
      register($$0, PIGSCENE, 4, 4);
      register($$0, BURNING_SKULL, 4, 4);
      register($$0, SKELETON, 4, 3);
      register($$0, EARTH, 2, 2, false);
      register($$0, WIND, 2, 2, false);
      register($$0, WATER, 2, 2, false);
      register($$0, FIRE, 2, 2, false);
      register($$0, DONKEY_KONG, 4, 3);
      register($$0, BAROQUE, 2, 2);
      register($$0, HUMBLE, 2, 2);
      register($$0, MEDITATIVE, 1, 1);
      register($$0, PRAIRIE_RIDE, 1, 2);
      register($$0, UNPACKED, 4, 4);
      register($$0, BACKYARD, 3, 4);
      register($$0, BOUQUET, 3, 3);
      register($$0, CAVEBIRD, 3, 3);
      register($$0, CHANGING, 4, 2);
      register($$0, COTAN, 3, 3);
      register($$0, ENDBOSS, 3, 3);
      register($$0, FERN, 3, 3);
      register($$0, FINDING, 4, 2);
      register($$0, LOWMIST, 4, 2);
      register($$0, ORB, 4, 4);
      register($$0, OWLEMONS, 3, 3);
      register($$0, PASSAGE, 4, 2);
      register($$0, POND, 3, 4);
      register($$0, SUNFLOWERS, 3, 3);
      register($$0, TIDES, 3, 3);
      register($$0, DENNIS, 3, 3);
   }

   private static void register(BootstrapContext<PaintingVariant> $$0, ResourceKey<PaintingVariant> $$1, int $$2, int $$3) {
      register($$0, $$1, $$2, $$3, true);
   }

   private static void register(BootstrapContext<PaintingVariant> $$0, ResourceKey<PaintingVariant> $$1, int $$2, int $$3, boolean $$4) {
      $$0.register(
         $$1,
         new PaintingVariant(
            $$2,
            $$3,
            $$1.identifier(),
            Optional.of(Component.translatable($$1.identifier().toLanguageKey("painting", "title")).withStyle(ChatFormatting.YELLOW)),
            $$4 ? Optional.of(Component.translatable($$1.identifier().toLanguageKey("painting", "author")).withStyle(ChatFormatting.GRAY)) : Optional.empty()
         )
      );
   }

   private static ResourceKey<PaintingVariant> create(String $$0) {
      return ResourceKey.create(Registries.PAINTING_VARIANT, Identifier.withDefaultNamespace($$0));
   }
}
