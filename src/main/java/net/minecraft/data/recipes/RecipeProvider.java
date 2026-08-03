package net.minecraft.data.recipes;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.EnterBlockTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.Slots;
import net.minecraft.advancements.criterion.ItemPredicate.Builder;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.AbstractCookingRecipe.Factory;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import org.jspecify.annotations.Nullable;

public abstract class RecipeProvider {
   protected final Provider registries;
   private final HolderGetter<Item> items;
   protected final RecipeOutput output;
   private static final Map<net.minecraft.data.BlockFamily.Variant, RecipeProvider.FamilyRecipeProvider> SHAPE_BUILDERS = ImmutableMap.builder()
      .put(net.minecraft.data.BlockFamily.Variant.BUTTON, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.buttonBuilder($$1, Ingredient.of($$2)))
      .put(
         net.minecraft.data.BlockFamily.Variant.CHISELED,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.chiseledBuilder(RecipeCategory.BUILDING_BLOCKS, $$1, Ingredient.of($$2))
      )
      .put(
         net.minecraft.data.BlockFamily.Variant.CUT,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.cutBuilder(RecipeCategory.BUILDING_BLOCKS, $$1, Ingredient.of($$2))
      )
      .put(net.minecraft.data.BlockFamily.Variant.DOOR, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.doorBuilder($$1, Ingredient.of($$2)))
      .put(
         net.minecraft.data.BlockFamily.Variant.CUSTOM_FENCE, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.fenceBuilder($$1, Ingredient.of($$2))
      )
      .put(net.minecraft.data.BlockFamily.Variant.FENCE, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.fenceBuilder($$1, Ingredient.of($$2)))
      .put(
         net.minecraft.data.BlockFamily.Variant.CUSTOM_FENCE_GATE,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.fenceGateBuilder($$1, Ingredient.of($$2))
      )
      .put(
         net.minecraft.data.BlockFamily.Variant.FENCE_GATE,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.fenceGateBuilder($$1, Ingredient.of($$2))
      )
      .put(net.minecraft.data.BlockFamily.Variant.SIGN, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.signBuilder($$1, Ingredient.of($$2)))
      .put(
         net.minecraft.data.BlockFamily.Variant.SLAB,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.slabBuilder(RecipeCategory.BUILDING_BLOCKS, $$1, Ingredient.of($$2))
      )
      .put(net.minecraft.data.BlockFamily.Variant.STAIRS, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.stairBuilder($$1, Ingredient.of($$2)))
      .put(
         net.minecraft.data.BlockFamily.Variant.PRESSURE_PLATE,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.pressurePlateBuilder(RecipeCategory.REDSTONE, $$1, Ingredient.of($$2))
      )
      .put(
         net.minecraft.data.BlockFamily.Variant.POLISHED,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.polishedBuilder(RecipeCategory.BUILDING_BLOCKS, $$1, Ingredient.of($$2))
      )
      .put(
         net.minecraft.data.BlockFamily.Variant.TRAPDOOR, (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.trapdoorBuilder($$1, Ingredient.of($$2))
      )
      .put(
         net.minecraft.data.BlockFamily.Variant.WALL,
         (RecipeProvider.FamilyRecipeProvider)($$0, $$1, $$2) -> $$0.wallBuilder(RecipeCategory.DECORATIONS, $$1, Ingredient.of($$2))
      )
      .build();

   protected RecipeProvider(Provider $$0, RecipeOutput $$1) {
      this.registries = $$0;
      this.items = $$0.lookupOrThrow(Registries.ITEM);
      this.output = $$1;
   }

   protected abstract void buildRecipes();

   protected void generateForEnabledBlockFamilies(FeatureFlagSet $$0) {
      net.minecraft.data.BlockFamilies.getAllFamilies()
         .filter(net.minecraft.data.BlockFamily::shouldGenerateRecipe)
         .forEach($$1 -> this.generateRecipes($$1, $$0));
   }

   protected void oneToOneConversionRecipe(ItemLike $$0, ItemLike $$1, @Nullable String $$2) {
      this.oneToOneConversionRecipe($$0, $$1, $$2, 1);
   }

   protected void oneToOneConversionRecipe(ItemLike $$0, ItemLike $$1, @Nullable String $$2, int $$3) {
      this.shapeless(RecipeCategory.MISC, $$0, $$3)
         .requires($$1)
         .group($$2)
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output, getConversionRecipeName($$0, $$1));
   }

   protected void oreSmelting(List<ItemLike> $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4, String $$5) {
      this.oreCooking(RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, $$0, $$1, $$2, $$3, $$4, $$5, "_from_smelting");
   }

   protected void oreBlasting(List<ItemLike> $$0, RecipeCategory $$1, ItemLike $$2, float $$3, int $$4, String $$5) {
      this.oreCooking(RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, $$0, $$1, $$2, $$3, $$4, $$5, "_from_blasting");
   }

   private <T extends AbstractCookingRecipe> void oreCooking(
      RecipeSerializer<T> $$0, Factory<T> $$1, List<ItemLike> $$2, RecipeCategory $$3, ItemLike $$4, float $$5, int $$6, String $$7, String $$8
   ) {
      for (ItemLike $$9 : $$2) {
         SimpleCookingRecipeBuilder.generic(Ingredient.of($$9), $$3, $$4, $$5, $$6, $$0, $$1)
            .group($$7)
            .unlockedBy(getHasName($$9), this.has($$9))
            .save(this.output, getItemName($$4) + $$8 + "_" + getItemName($$9));
      }
   }

   protected void netheriteSmithing(Item $$0, RecipeCategory $$1, Item $$2) {
      SmithingTransformRecipeBuilder.smithing(
            Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of($$0), this.tag(ItemTags.NETHERITE_TOOL_MATERIALS), $$1, $$2
         )
         .unlocks("has_netherite_ingot", this.has(ItemTags.NETHERITE_TOOL_MATERIALS))
         .save(this.output, getItemName($$2) + "_smithing");
   }

   protected void trimSmithing(Item $$0, ResourceKey<TrimPattern> $$1, ResourceKey<Recipe<?>> $$2) {
      Reference<TrimPattern> $$3 = this.registries.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow($$1);
      SmithingTrimRecipeBuilder.smithingTrim(
            Ingredient.of($$0), this.tag(ItemTags.TRIMMABLE_ARMOR), this.tag(ItemTags.TRIM_MATERIALS), $$3, RecipeCategory.MISC
         )
         .unlocks("has_smithing_trim_template", this.has($$0))
         .save(this.output, $$2);
   }

   protected void twoByTwoPacker(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.shaped($$0, $$1, 1).define('#', $$2).pattern("##").pattern("##").unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   protected void threeByThreePacker(RecipeCategory $$0, ItemLike $$1, ItemLike $$2, String $$3) {
      this.shapeless($$0, $$1).requires($$2, 9).unlockedBy($$3, this.has($$2)).save(this.output);
   }

   protected void threeByThreePacker(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.threeByThreePacker($$0, $$1, $$2, getHasName($$2));
   }

   protected void planksFromLog(ItemLike $$0, TagKey<Item> $$1, int $$2) {
      this.shapeless(RecipeCategory.BUILDING_BLOCKS, $$0, $$2).requires($$1).group("planks").unlockedBy("has_log", this.has($$1)).save(this.output);
   }

   protected void planksFromLogs(ItemLike $$0, TagKey<Item> $$1, int $$2) {
      this.shapeless(RecipeCategory.BUILDING_BLOCKS, $$0, $$2).requires($$1).group("planks").unlockedBy("has_logs", this.has($$1)).save(this.output);
   }

   protected void woodFromLogs(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 3)
         .define('#', $$1)
         .pattern("##")
         .pattern("##")
         .group("bark")
         .unlockedBy("has_log", this.has($$1))
         .save(this.output);
   }

   protected void woodenBoat(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.TRANSPORTATION, $$0)
         .define('#', $$1)
         .pattern("# #")
         .pattern("###")
         .group("boat")
         .unlockedBy("in_water", insideOf(Blocks.WATER))
         .save(this.output);
   }

   protected void chestBoat(ItemLike $$0, ItemLike $$1) {
      this.shapeless(RecipeCategory.TRANSPORTATION, $$0)
         .requires(Blocks.CHEST)
         .requires($$1)
         .group("chest_boat")
         .unlockedBy("has_boat", this.has(ItemTags.BOATS))
         .save(this.output);
   }

   private RecipeBuilder buttonBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shapeless(RecipeCategory.REDSTONE, $$0).requires($$1);
   }

   protected RecipeBuilder doorBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shaped(RecipeCategory.REDSTONE, $$0, 3).define('#', $$1).pattern("##").pattern("##").pattern("##");
   }

   private RecipeBuilder fenceBuilder(ItemLike $$0, Ingredient $$1) {
      int $$2 = $$0 == Blocks.NETHER_BRICK_FENCE ? 6 : 3;
      Item $$3 = $$0 == Blocks.NETHER_BRICK_FENCE ? Items.NETHER_BRICK : Items.STICK;
      return this.shaped(RecipeCategory.DECORATIONS, $$0, $$2).define('W', $$1).define('#', $$3).pattern("W#W").pattern("W#W");
   }

   private RecipeBuilder fenceGateBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shaped(RecipeCategory.REDSTONE, $$0).define('#', Items.STICK).define('W', $$1).pattern("#W#").pattern("#W#");
   }

   protected void pressurePlate(ItemLike $$0, ItemLike $$1) {
      this.pressurePlateBuilder(RecipeCategory.REDSTONE, $$0, Ingredient.of($$1)).unlockedBy(getHasName($$1), this.has($$1)).save(this.output);
   }

   private RecipeBuilder pressurePlateBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1).define('#', $$2).pattern("##");
   }

   protected void slab(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.slabBuilder($$0, $$1, Ingredient.of($$2)).unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   protected void shelf(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0, 6)
         .define('#', $$1)
         .pattern("###")
         .pattern("   ")
         .pattern("###")
         .group("shelf")
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected RecipeBuilder slabBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1, 6).define('#', $$2).pattern("###");
   }

   protected RecipeBuilder stairBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 4).define('#', $$1).pattern("#  ").pattern("## ").pattern("###");
   }

   protected RecipeBuilder trapdoorBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shaped(RecipeCategory.REDSTONE, $$0, 2).define('#', $$1).pattern("###").pattern("###");
   }

   private RecipeBuilder signBuilder(ItemLike $$0, Ingredient $$1) {
      return this.shaped(RecipeCategory.DECORATIONS, $$0, 3)
         .group("sign")
         .define('#', $$1)
         .define('X', Items.STICK)
         .pattern("###")
         .pattern("###")
         .pattern(" X ");
   }

   protected void hangingSign(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0, 6)
         .group("hanging_sign")
         .define('#', $$1)
         .define('X', Items.IRON_CHAIN)
         .pattern("X X")
         .pattern("###")
         .pattern("###")
         .unlockedBy("has_stripped_logs", this.has($$1))
         .save(this.output);
   }

   protected void colorItemWithDye(List<Item> $$0, List<Item> $$1, String $$2, RecipeCategory $$3) {
      this.colorWithDye($$0, $$1, null, $$2, $$3);
   }

   protected void colorWithDye(List<Item> $$0, List<Item> $$1, @Nullable Item $$2, String $$3, RecipeCategory $$4) {
      for (int $$5 = 0; $$5 < $$0.size(); $$5++) {
         Item $$6 = $$0.get($$5);
         Item $$7 = $$1.get($$5);
         Stream<Item> $$8 = $$1.stream().filter($$1x -> !$$1x.equals($$7));
         if ($$2 != null) {
            $$8 = Stream.concat($$8, Stream.of($$2));
         }

         this.shapeless($$4, $$7)
            .requires($$6)
            .requires(Ingredient.of($$8))
            .group($$3)
            .unlockedBy("has_needed_dye", this.has($$6))
            .save(this.output, "dye_" + getItemName($$7));
      }
   }

   protected void carpet(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0, 3)
         .define('#', $$1)
         .pattern("##")
         .group("carpet")
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void bedFromPlanksAndWool(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0)
         .define('#', $$1)
         .define('X', ItemTags.PLANKS)
         .pattern("###")
         .pattern("XXX")
         .group("bed")
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void banner(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0)
         .define('#', $$1)
         .define('|', Items.STICK)
         .pattern("###")
         .pattern("###")
         .pattern(" | ")
         .group("banner")
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void stainedGlassFromGlassAndDye(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 8)
         .define('#', Blocks.GLASS)
         .define('X', $$1)
         .pattern("###")
         .pattern("#X#")
         .pattern("###")
         .group("stained_glass")
         .unlockedBy("has_glass", this.has(Blocks.GLASS))
         .save(this.output);
   }

   protected void dryGhast(ItemLike $$0) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 1)
         .define('#', Items.GHAST_TEAR)
         .define('X', Items.SOUL_SAND)
         .pattern("###")
         .pattern("#X#")
         .pattern("###")
         .group("dry_ghast")
         .unlockedBy(getHasName(Items.GHAST_TEAR), this.has(Items.GHAST_TEAR))
         .save(this.output);
   }

   protected void harness(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.COMBAT, $$0)
         .define('#', $$1)
         .define('G', Items.GLASS)
         .define('L', Items.LEATHER)
         .pattern("LLL")
         .pattern("G#G")
         .group("harness")
         .unlockedBy("has_dried_ghast", this.has(Blocks.DRIED_GHAST))
         .save(this.output);
   }

   protected void stainedGlassPaneFromStainedGlass(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0, 16)
         .define('#', $$1)
         .pattern("###")
         .pattern("###")
         .group("stained_glass_pane")
         .unlockedBy("has_glass", this.has($$1))
         .save(this.output);
   }

   protected void stainedGlassPaneFromGlassPaneAndDye(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.DECORATIONS, $$0, 8)
         .define('#', Blocks.GLASS_PANE)
         .define('$', $$1)
         .pattern("###")
         .pattern("#$#")
         .pattern("###")
         .group("stained_glass_pane")
         .unlockedBy("has_glass_pane", this.has(Blocks.GLASS_PANE))
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output, getConversionRecipeName($$0, Blocks.GLASS_PANE));
   }

   protected void coloredTerracottaFromTerracottaAndDye(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 8)
         .define('#', Blocks.TERRACOTTA)
         .define('X', $$1)
         .pattern("###")
         .pattern("#X#")
         .pattern("###")
         .group("stained_terracotta")
         .unlockedBy("has_terracotta", this.has(Blocks.TERRACOTTA))
         .save(this.output);
   }

   protected void concretePowder(ItemLike $$0, ItemLike $$1) {
      this.shapeless(RecipeCategory.BUILDING_BLOCKS, $$0, 8)
         .requires($$1)
         .requires(Blocks.SAND, 4)
         .requires(Blocks.GRAVEL, 4)
         .group("concrete_powder")
         .unlockedBy("has_sand", this.has(Blocks.SAND))
         .unlockedBy("has_gravel", this.has(Blocks.GRAVEL))
         .save(this.output);
   }

   protected void candle(ItemLike $$0, ItemLike $$1) {
      this.shapeless(RecipeCategory.DECORATIONS, $$0)
         .requires(Blocks.CANDLE)
         .requires($$1)
         .group("dyed_candle")
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void wall(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.wallBuilder($$0, $$1, Ingredient.of($$2)).unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   private RecipeBuilder wallBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1, 6).define('#', $$2).pattern("###").pattern("###");
   }

   protected void polished(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.polishedBuilder($$0, $$1, Ingredient.of($$2)).unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   private RecipeBuilder polishedBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1, 4).define('S', $$2).pattern("SS").pattern("SS");
   }

   protected void cut(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.cutBuilder($$0, $$1, Ingredient.of($$2)).unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   private ShapedRecipeBuilder cutBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1, 4).define('#', $$2).pattern("##").pattern("##");
   }

   protected void chiseled(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.chiseledBuilder($$0, $$1, Ingredient.of($$2)).unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   protected void mosaicBuilder(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.shaped($$0, $$1).define('#', $$2).pattern("#").pattern("#").unlockedBy(getHasName($$2), this.has($$2)).save(this.output);
   }

   protected ShapedRecipeBuilder chiseledBuilder(RecipeCategory $$0, ItemLike $$1, Ingredient $$2) {
      return this.shaped($$0, $$1).define('#', $$2).pattern("#").pattern("#");
   }

   protected void stonecutterResultFromBase(RecipeCategory $$0, ItemLike $$1, ItemLike $$2) {
      this.stonecutterResultFromBase($$0, $$1, $$2, 1);
   }

   protected void stonecutterResultFromBase(RecipeCategory $$0, ItemLike $$1, ItemLike $$2, int $$3) {
      SingleItemRecipeBuilder.stonecutting(Ingredient.of($$2), $$0, $$1, $$3)
         .unlockedBy(getHasName($$2), this.has($$2))
         .save(this.output, getConversionRecipeName($$1, $$2) + "_stonecutting");
   }

   private void smeltingResultFromBase(ItemLike $$0, ItemLike $$1) {
      SimpleCookingRecipeBuilder.smelting(Ingredient.of($$1), RecipeCategory.BUILDING_BLOCKS, $$0, 0.1F, 200)
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void nineBlockStorageRecipes(RecipeCategory $$0, ItemLike $$1, RecipeCategory $$2, ItemLike $$3) {
      this.nineBlockStorageRecipes($$0, $$1, $$2, $$3, getSimpleRecipeName($$3), null, getSimpleRecipeName($$1), null);
   }

   protected void nineBlockStorageRecipesWithCustomPacking(RecipeCategory $$0, ItemLike $$1, RecipeCategory $$2, ItemLike $$3, String $$4, String $$5) {
      this.nineBlockStorageRecipes($$0, $$1, $$2, $$3, $$4, $$5, getSimpleRecipeName($$1), null);
   }

   protected void nineBlockStorageRecipesRecipesWithCustomUnpacking(RecipeCategory $$0, ItemLike $$1, RecipeCategory $$2, ItemLike $$3, String $$4, String $$5) {
      this.nineBlockStorageRecipes($$0, $$1, $$2, $$3, getSimpleRecipeName($$3), null, $$4, $$5);
   }

   private void nineBlockStorageRecipes(
      RecipeCategory $$0, ItemLike $$1, RecipeCategory $$2, ItemLike $$3, String $$4, @Nullable String $$5, String $$6, @Nullable String $$7
   ) {
      this.shapeless($$0, $$1, 9)
         .requires($$3)
         .group($$7)
         .unlockedBy(getHasName($$3), this.has($$3))
         .save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse($$6)));
      this.shaped($$2, $$3)
         .define('#', $$1)
         .pattern("###")
         .pattern("###")
         .pattern("###")
         .group($$5)
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output, ResourceKey.create(Registries.RECIPE, Identifier.parse($$4)));
   }

   protected void copySmithingTemplate(ItemLike $$0, ItemLike $$1) {
      this.shaped(RecipeCategory.MISC, $$0, 2)
         .define('#', Items.DIAMOND)
         .define('C', $$1)
         .define('S', $$0)
         .pattern("#S#")
         .pattern("#C#")
         .pattern("###")
         .unlockedBy(getHasName($$0), this.has($$0))
         .save(this.output);
   }

   protected void copySmithingTemplate(ItemLike $$0, Ingredient $$1) {
      this.shaped(RecipeCategory.MISC, $$0, 2)
         .define('#', Items.DIAMOND)
         .define('C', $$1)
         .define('S', $$0)
         .pattern("#S#")
         .pattern("#C#")
         .pattern("###")
         .unlockedBy(getHasName($$0), this.has($$0))
         .save(this.output);
   }

   protected <T extends AbstractCookingRecipe> void cookRecipes(String $$0, RecipeSerializer<T> $$1, Factory<T> $$2, int $$3) {
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.BEEF, Items.COOKED_BEEF, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.CHICKEN, Items.COOKED_CHICKEN, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.COD, Items.COOKED_COD, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.KELP, Items.DRIED_KELP, 0.1F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.SALMON, Items.COOKED_SALMON, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.MUTTON, Items.COOKED_MUTTON, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.PORKCHOP, Items.COOKED_PORKCHOP, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.POTATO, Items.BAKED_POTATO, 0.35F);
      this.simpleCookingRecipe($$0, $$1, $$2, $$3, Items.RABBIT, Items.COOKED_RABBIT, 0.35F);
   }

   private <T extends AbstractCookingRecipe> void simpleCookingRecipe(
      String $$0, RecipeSerializer<T> $$1, Factory<T> $$2, int $$3, ItemLike $$4, ItemLike $$5, float $$6
   ) {
      SimpleCookingRecipeBuilder.generic(Ingredient.of($$4), RecipeCategory.FOOD, $$5, $$6, $$3, $$1, $$2)
         .unlockedBy(getHasName($$4), this.has($$4))
         .save(this.output, getItemName($$5) + "_from_" + $$0);
   }

   protected void waxRecipes(FeatureFlagSet $$0) {
      ((BiMap)HoneycombItem.WAXABLES.get())
         .forEach(
            ($$1, $$2) -> {
               if ($$2.requiredFeatures().isSubsetOf($$0)) {
                  Pair<RecipeCategory, String> $$3 = (Pair<RecipeCategory, String>)HoneycombItem.WAXED_RECIPES
                     .getOrDefault($$2, Pair.of(RecipeCategory.BUILDING_BLOCKS, getItemName($$2)));
                  RecipeCategory $$4 = (RecipeCategory)$$3.getFirst();
                  String $$5 = (String)$$3.getSecond();
                  this.shapeless($$4, $$2)
                     .requires($$1)
                     .requires(Items.HONEYCOMB)
                     .group($$5)
                     .unlockedBy(getHasName($$1), this.has($$1))
                     .save(this.output, getConversionRecipeName($$2, Items.HONEYCOMB));
               }
            }
         );
   }

   protected void grate(Block $$0, Block $$1) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0, 4)
         .define('M', $$1)
         .pattern(" M ")
         .pattern("M M")
         .pattern(" M ")
         .group(getItemName($$0))
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void copperBulb(Block $$0, Block $$1) {
      this.shaped(RecipeCategory.REDSTONE, $$0, 4)
         .define('C', $$1)
         .define('R', Items.REDSTONE)
         .define('B', Items.BLAZE_ROD)
         .pattern(" C ")
         .pattern("CBC")
         .pattern(" R ")
         .unlockedBy(getHasName($$1), this.has($$1))
         .group(getItemName($$0))
         .save(this.output);
   }

   protected void waxedChiseled(Block $$0, Block $$1) {
      this.shaped(RecipeCategory.BUILDING_BLOCKS, $$0)
         .define('M', $$1)
         .pattern(" M ")
         .pattern(" M ")
         .group(getItemName($$0))
         .unlockedBy(getHasName($$1), this.has($$1))
         .save(this.output);
   }

   protected void suspiciousStew(Item $$0, SuspiciousEffectHolder $$1) {
      ItemStack $$2 = new ItemStack(
         Items.SUSPICIOUS_STEW.builtInRegistryHolder(),
         1,
         DataComponentPatch.builder().set(DataComponents.SUSPICIOUS_STEW_EFFECTS, $$1.getSuspiciousEffects()).build()
      );
      this.shapeless(RecipeCategory.FOOD, $$2)
         .requires(Items.BOWL)
         .requires(Items.BROWN_MUSHROOM)
         .requires(Items.RED_MUSHROOM)
         .requires($$0)
         .group("suspicious_stew")
         .unlockedBy(getHasName($$0), this.has($$0))
         .save(this.output, getItemName($$2.getItem()) + "_from_" + getItemName($$0));
   }

   protected void generateRecipes(net.minecraft.data.BlockFamily $$0, FeatureFlagSet $$1) {
      $$0.getVariants()
         .forEach(
            ($$2, $$3) -> {
               if ($$3.requiredFeatures().isSubsetOf($$1)) {
                  RecipeProvider.FamilyRecipeProvider $$4 = SHAPE_BUILDERS.get($$2);
                  ItemLike $$5 = this.getBaseBlock($$0, $$2);
                  if ($$4 != null) {
                     RecipeBuilder $$6 = $$4.create(this, $$3, $$5);
                     $$0.getRecipeGroupPrefix()
                        .ifPresent($$2x -> $$6.group($$2x + ($$2 == net.minecraft.data.BlockFamily.Variant.CUT ? "" : "_" + $$2.getRecipeGroup())));
                     $$6.unlockedBy($$0.getRecipeUnlockedBy().orElseGet(() -> getHasName($$5)), this.has($$5));
                     $$6.save(this.output);
                  }

                  if ($$2 == net.minecraft.data.BlockFamily.Variant.CRACKED) {
                     this.smeltingResultFromBase($$3, $$5);
                  }
               }
            }
         );
   }

   private Block getBaseBlock(net.minecraft.data.BlockFamily $$0, net.minecraft.data.BlockFamily.Variant $$1) {
      if ($$1 == net.minecraft.data.BlockFamily.Variant.CHISELED) {
         if (!$$0.getVariants().containsKey(net.minecraft.data.BlockFamily.Variant.SLAB)) {
            throw new IllegalStateException("Slab is not defined for the family.");
         } else {
            return $$0.get(net.minecraft.data.BlockFamily.Variant.SLAB);
         }
      } else {
         return $$0.getBaseBlock();
      }
   }

   private static Criterion<TriggerInstance> insideOf(Block $$0) {
      return CriteriaTriggers.ENTER_BLOCK.createCriterion(new TriggerInstance(Optional.empty(), Optional.of($$0.builtInRegistryHolder()), Optional.empty()));
   }

   private Criterion<net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance> has(Ints $$0, ItemLike $$1) {
      return inventoryTrigger(Builder.item().of(this.items, new ItemLike[]{$$1}).withCount($$0));
   }

   protected Criterion<net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance> has(ItemLike $$0) {
      return inventoryTrigger(Builder.item().of(this.items, new ItemLike[]{$$0}));
   }

   protected Criterion<net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> $$0) {
      return inventoryTrigger(Builder.item().of(this.items, $$0));
   }

   private static Criterion<net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance> inventoryTrigger(Builder... $$0) {
      return inventoryTrigger(Arrays.stream($$0).map(Builder::build).toArray(ItemPredicate[]::new));
   }

   private static Criterion<net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... $$0) {
      return CriteriaTriggers.INVENTORY_CHANGED
         .createCriterion(new net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance(Optional.empty(), Slots.ANY, List.of($$0)));
   }

   protected static String getHasName(ItemLike $$0) {
      return "has_" + getItemName($$0);
   }

   protected static String getItemName(ItemLike $$0) {
      return BuiltInRegistries.ITEM.getKey($$0.asItem()).getPath();
   }

   protected static String getSimpleRecipeName(ItemLike $$0) {
      return getItemName($$0);
   }

   protected static String getConversionRecipeName(ItemLike $$0, ItemLike $$1) {
      return getItemName($$0) + "_from_" + getItemName($$1);
   }

   protected static String getSmeltingRecipeName(ItemLike $$0) {
      return getItemName($$0) + "_from_smelting";
   }

   protected static String getBlastingRecipeName(ItemLike $$0) {
      return getItemName($$0) + "_from_blasting";
   }

   protected Ingredient tag(TagKey<Item> $$0) {
      return Ingredient.of(this.items.getOrThrow($$0));
   }

   protected ShapedRecipeBuilder shaped(RecipeCategory $$0, ItemLike $$1) {
      return ShapedRecipeBuilder.shaped(this.items, $$0, $$1);
   }

   protected ShapedRecipeBuilder shaped(RecipeCategory $$0, ItemLike $$1, int $$2) {
      return ShapedRecipeBuilder.shaped(this.items, $$0, $$1, $$2);
   }

   protected ShapelessRecipeBuilder shapeless(RecipeCategory $$0, ItemStack $$1) {
      return ShapelessRecipeBuilder.shapeless(this.items, $$0, $$1);
   }

   protected ShapelessRecipeBuilder shapeless(RecipeCategory $$0, ItemLike $$1) {
      return ShapelessRecipeBuilder.shapeless(this.items, $$0, $$1);
   }

   protected ShapelessRecipeBuilder shapeless(RecipeCategory $$0, ItemLike $$1, int $$2) {
      return ShapelessRecipeBuilder.shapeless(this.items, $$0, $$1, $$2);
   }

   @FunctionalInterface
   interface FamilyRecipeProvider {
      RecipeBuilder create(RecipeProvider var1, ItemLike var2, ItemLike var3);
   }

   protected abstract static class Runner implements net.minecraft.data.DataProvider {
      private final net.minecraft.data.PackOutput packOutput;
      private final CompletableFuture<Provider> registries;

      protected Runner(net.minecraft.data.PackOutput $$0, CompletableFuture<Provider> $$1) {
         this.packOutput = $$0;
         this.registries = $$1;
      }

      @Override
      public final CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
         return this.registries
            .thenCompose(
               $$1 -> {
                  final net.minecraft.data.PackOutput.PathProvider $$2 = this.packOutput.createRegistryElementsPathProvider(Registries.RECIPE);
                  final net.minecraft.data.PackOutput.PathProvider $$3 = this.packOutput.createRegistryElementsPathProvider(Registries.ADVANCEMENT);
                  final Set<ResourceKey<Recipe<?>>> $$4 = Sets.newHashSet();
                  final List<CompletableFuture<?>> $$5 = new ArrayList<>();
                  RecipeOutput $$6 = new RecipeOutput() {
                     @Override
                     public void accept(ResourceKey<Recipe<?>> $$0x, Recipe<?> $$1x, @Nullable AdvancementHolder $$2x) {
                        if (!$$4.add($$0)) {
                           throw new IllegalStateException("Duplicate recipe " + $$0.identifier());
                        } else {
                           this.saveRecipe($$0, $$1);
                           if ($$2 != null) {
                              this.saveAdvancement($$2);
                           }
                        }
                     }

                     @Override
                     public net.minecraft.advancements.Advancement.Builder advancement() {
                        return net.minecraft.advancements.Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                     }

                     @Override
                     public void includeRootAdvancement() {
                        AdvancementHolder $$0x = net.minecraft.advancements.Advancement.Builder.recipeAdvancement()
                           .addCriterion(
                              "impossible",
                              CriteriaTriggers.IMPOSSIBLE.createCriterion(new net.minecraft.advancements.criterion.ImpossibleTrigger.TriggerInstance())
                           )
                           .build(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                        this.saveAdvancement($$0x);
                     }

                     private void saveRecipe(ResourceKey<Recipe<?>> $$0x, Recipe<?> $$1x) {
                        $$5.add(net.minecraft.data.DataProvider.saveStable($$0, $$1, Recipe.CODEC, $$1, $$2.json($$0.identifier())));
                     }

                     private void saveAdvancement(AdvancementHolder $$0x) {
                        $$5.add(net.minecraft.data.DataProvider.saveStable($$0, $$1, Advancement.CODEC, $$0.value(), $$3.json($$0.id())));
                     }
                  };
                  this.createRecipeProvider($$1, $$6).buildRecipes();
                  return CompletableFuture.allOf($$5.toArray(CompletableFuture[]::new));
               }
            );
      }

      protected abstract RecipeProvider createRecipeProvider(Provider var1, RecipeOutput var2);
   }
}
