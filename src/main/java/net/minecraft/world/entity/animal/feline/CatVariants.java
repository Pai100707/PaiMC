package net.minecraft.world.entity.animal.feline;

import java.util.List;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.variant.MoonBrightnessCheck;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.entity.variant.StructureCheck;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface CatVariants {
   ResourceKey<CatVariant> TABBY = createKey("tabby");
   ResourceKey<CatVariant> BLACK = createKey("black");
   ResourceKey<CatVariant> RED = createKey("red");
   ResourceKey<CatVariant> SIAMESE = createKey("siamese");
   ResourceKey<CatVariant> BRITISH_SHORTHAIR = createKey("british_shorthair");
   ResourceKey<CatVariant> CALICO = createKey("calico");
   ResourceKey<CatVariant> PERSIAN = createKey("persian");
   ResourceKey<CatVariant> RAGDOLL = createKey("ragdoll");
   ResourceKey<CatVariant> WHITE = createKey("white");
   ResourceKey<CatVariant> JELLIE = createKey("jellie");
   ResourceKey<CatVariant> ALL_BLACK = createKey("all_black");

   private static ResourceKey<CatVariant> createKey(String $$0) {
      return ResourceKey.create(Registries.CAT_VARIANT, Identifier.withDefaultNamespace($$0));
   }

   static void bootstrap(BootstrapContext<CatVariant> $$0) {
      HolderGetter<Structure> $$1 = $$0.lookup(Registries.STRUCTURE);
      registerForAnyConditions($$0, TABBY, "entity/cat/tabby");
      registerForAnyConditions($$0, BLACK, "entity/cat/black");
      registerForAnyConditions($$0, RED, "entity/cat/red");
      registerForAnyConditions($$0, SIAMESE, "entity/cat/siamese");
      registerForAnyConditions($$0, BRITISH_SHORTHAIR, "entity/cat/british_shorthair");
      registerForAnyConditions($$0, CALICO, "entity/cat/calico");
      registerForAnyConditions($$0, PERSIAN, "entity/cat/persian");
      registerForAnyConditions($$0, RAGDOLL, "entity/cat/ragdoll");
      registerForAnyConditions($$0, WHITE, "entity/cat/white");
      registerForAnyConditions($$0, JELLIE, "entity/cat/jellie");
      register(
         $$0,
         ALL_BLACK,
         "entity/cat/all_black",
         new SpawnPrioritySelectors(
            List.of(
               new PriorityProvider.Selector<>(new StructureCheck($$1.getOrThrow(StructureTags.CATS_SPAWN_AS_BLACK)), 1),
               new PriorityProvider.Selector<>(new MoonBrightnessCheck(Doubles.atLeast(0.9)), 0)
            )
         )
      );
   }

   private static void registerForAnyConditions(BootstrapContext<CatVariant> $$0, ResourceKey<CatVariant> $$1, String $$2) {
      register($$0, $$1, $$2, SpawnPrioritySelectors.fallback(0));
   }

   private static void register(BootstrapContext<CatVariant> $$0, ResourceKey<CatVariant> $$1, String $$2, SpawnPrioritySelectors $$3) {
      $$0.register($$1, new CatVariant(new ResourceTexture(Identifier.withDefaultNamespace($$2)), $$3));
   }
}
