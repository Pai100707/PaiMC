package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

public class RecipeManager extends SimplePreparableReloadListener<RecipeMap> implements RecipeAccess {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<ResourceKey<RecipePropertySet>, RecipeManager.IngredientExtractor> RECIPE_PROPERTY_SETS = Map.of(
      RecipePropertySet.SMITHING_ADDITION,
      $$0 -> $$0 instanceof SmithingRecipe $$1 ? $$1.additionIngredient() : Optional.empty(),
      RecipePropertySet.SMITHING_BASE,
      $$0 -> $$0 instanceof SmithingRecipe $$1 ? Optional.of($$1.baseIngredient()) : Optional.empty(),
      RecipePropertySet.SMITHING_TEMPLATE,
      $$0 -> $$0 instanceof SmithingRecipe $$1 ? $$1.templateIngredient() : Optional.empty(),
      RecipePropertySet.FURNACE_INPUT,
      forSingleInput(RecipeType.SMELTING),
      RecipePropertySet.BLAST_FURNACE_INPUT,
      forSingleInput(RecipeType.BLASTING),
      RecipePropertySet.SMOKER_INPUT,
      forSingleInput(RecipeType.SMOKING),
      RecipePropertySet.CAMPFIRE_INPUT,
      forSingleInput(RecipeType.CAMPFIRE_COOKING)
   );
   private static final FileToIdConverter RECIPE_LISTER = FileToIdConverter.registry(Registries.RECIPE);
   private final Provider registries;
   private RecipeMap recipes = RecipeMap.EMPTY;
   private Map<ResourceKey<RecipePropertySet>, RecipePropertySet> propertySets = Map.of();
   private SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes = SelectableRecipe.SingleInputSet.empty();
   private List<RecipeManager.ServerDisplayInfo> allDisplays = List.of();
   private Map<ResourceKey<Recipe<?>>, List<RecipeManager.ServerDisplayInfo>> recipeToDisplay = Map.of();

   public RecipeManager(Provider $$0) {
      this.registries = $$0;
   }

   protected RecipeMap prepare(ResourceManager $$0, ProfilerFiller $$1) {
      SortedMap<Identifier, Recipe<?>> $$2 = new TreeMap<>();
      SimpleJsonResourceReloadListener.scanDirectory($$0, RECIPE_LISTER, this.registries.createSerializationContext(JsonOps.INSTANCE), Recipe.CODEC, $$2);
      List<RecipeHolder<?>> $$3 = new ArrayList<>($$2.size());
      $$2.forEach(($$1x, $$2x) -> {
         ResourceKey<Recipe<?>> $$3x = ResourceKey.create(Registries.RECIPE, $$1x);
         RecipeHolder<?> $$4 = new RecipeHolder($$3x, $$2x);
         $$3.add($$4);
      });
      return RecipeMap.create($$3);
   }

   protected void apply(RecipeMap $$0, ResourceManager $$1, ProfilerFiller $$2) {
      this.recipes = $$0;
      LOGGER.info("Loaded {} recipes", $$0.values().size());
   }

   public void finalizeRecipeLoading(FeatureFlagSet $$0) {
      List<SelectableRecipe.SingleInputEntry<StonecutterRecipe>> $$1 = new ArrayList<>();
      List<RecipeManager.IngredientCollector> $$2 = RECIPE_PROPERTY_SETS.entrySet()
         .stream()
         .map($$0x -> new RecipeManager.IngredientCollector((ResourceKey<RecipePropertySet>)$$0x.getKey(), (RecipeManager.IngredientExtractor)$$0x.getValue()))
         .toList();
      this.recipes
         .values()
         .forEach(
            $$3 -> {
               Recipe<?> $$4 = $$3.value();
               if (!$$4.isSpecial() && $$4.placementInfo().isImpossibleToPlace()) {
                  LOGGER.warn("Recipe {} can't be placed due to empty ingredients and will be ignored", $$3.id().identifier());
               } else {
                  $$2.forEach($$1xx -> $$1xx.accept($$4));
                  if ($$4 instanceof StonecutterRecipe $$5 && isIngredientEnabled($$0, $$5.input()) && $$5.resultDisplay().isEnabled($$0)) {
                     $$1.add(
                        new SelectableRecipe.SingleInputEntry<>(
                           $$5.input(), new SelectableRecipe<>($$5.resultDisplay(), Optional.of((RecipeHolder<StonecutterRecipe>)$$3))
                        )
                     );
                  }
               }
            }
         );
      this.propertySets = $$2.stream().collect(Collectors.toUnmodifiableMap($$0x -> $$0x.key, $$1x -> $$1x.asPropertySet($$0)));
      this.stonecutterRecipes = new SelectableRecipe.SingleInputSet<>($$1);
      this.allDisplays = unpackRecipeInfo(this.recipes.values(), $$0);
      this.recipeToDisplay = this.allDisplays.stream().collect(Collectors.groupingBy($$0x -> $$0x.parent.id(), IdentityHashMap::new, Collectors.toList()));
   }

   static List<Ingredient> filterDisabled(FeatureFlagSet $$0, List<Ingredient> $$1) {
      $$1.removeIf($$1x -> !isIngredientEnabled($$0, $$1x));
      return $$1;
   }

   private static boolean isIngredientEnabled(FeatureFlagSet $$0, Ingredient $$1) {
      return $$1.items().allMatch($$1x -> ((net.minecraft.world.item.Item)$$1x.value()).isEnabled($$0));
   }

   public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
      RecipeType<T> $$0, I $$1, Level $$2, ResourceKey<Recipe<?>> $$3
   ) {
      RecipeHolder<T> $$4 = $$3 != null ? this.byKeyTyped($$0, $$3) : null;
      return this.getRecipeFor($$0, $$1, $$2, $$4);
   }

   public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(
      RecipeType<T> $$0, I $$1, Level $$2, RecipeHolder<T> $$3
   ) {
      return $$3 != null && $$3.value().matches($$1, $$2) ? Optional.of($$3) : this.getRecipeFor($$0, $$1, $$2);
   }

   public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getRecipeFor(RecipeType<T> $$0, I $$1, Level $$2) {
      return this.recipes.getRecipesFor($$0, $$1, $$2).findFirst();
   }

   public Optional<RecipeHolder<?>> byKey(ResourceKey<Recipe<?>> $$0) {
      return Optional.ofNullable(this.recipes.byKey($$0));
   }

   
   private <T extends Recipe<?>> RecipeHolder<T> byKeyTyped(RecipeType<T> $$0, ResourceKey<Recipe<?>> $$1) {
      RecipeHolder<?> $$2 = this.recipes.byKey($$1);
      return (RecipeHolder<T>)($$2 != null && $$2.value().getType().equals($$0) ? $$2 : null);
   }

   public Map<ResourceKey<RecipePropertySet>, RecipePropertySet> getSynchronizedItemProperties() {
      return this.propertySets;
   }

   public SelectableRecipe.SingleInputSet<StonecutterRecipe> getSynchronizedStonecutterRecipes() {
      return this.stonecutterRecipes;
   }

   @Override
   public RecipePropertySet propertySet(ResourceKey<RecipePropertySet> $$0) {
      return this.propertySets.getOrDefault($$0, RecipePropertySet.EMPTY);
   }

   @Override
   public SelectableRecipe.SingleInputSet<StonecutterRecipe> stonecutterRecipes() {
      return this.stonecutterRecipes;
   }

   public Collection<RecipeHolder<?>> getRecipes() {
      return this.recipes.values();
   }

   
   public RecipeManager.ServerDisplayInfo getRecipeFromDisplay(RecipeDisplayId $$0) {
      int $$1 = $$0.index();
      return $$1 >= 0 && $$1 < this.allDisplays.size() ? this.allDisplays.get($$1) : null;
   }

   public void listDisplaysForRecipe(ResourceKey<Recipe<?>> $$0, Consumer<RecipeDisplayEntry> $$1) {
      List<RecipeManager.ServerDisplayInfo> $$2 = this.recipeToDisplay.get($$0);
      if ($$2 != null) {
         $$2.forEach($$1x -> $$1.accept($$1x.display));
      }
   }

   @VisibleForTesting
   protected static RecipeHolder<?> fromJson(ResourceKey<Recipe<?>> $$0, JsonObject $$1, Provider $$2) {
      Recipe<?> $$3 = (Recipe<?>)Recipe.CODEC.parse($$2.createSerializationContext(JsonOps.INSTANCE), $$1).getOrThrow(JsonParseException::new);
      return new RecipeHolder<>($$0, $$3);
   }

   public static <I extends RecipeInput, T extends Recipe<I>> RecipeManager.CachedCheck<I, T> createCheck(final RecipeType<T> $$0) {
      return new RecipeManager.CachedCheck<I, T>() {
         
         private ResourceKey<Recipe<?>> lastRecipe;

         @Override
         public Optional<RecipeHolder<T>> getRecipeFor(I $$0x, ServerLevel $$1) {
            RecipeManager $$2 = $$1.recipeAccess();
            Optional<RecipeHolder<T>> $$3 = $$2.getRecipeFor($$0, $$0, $$1, this.lastRecipe);
            if ($$3.isPresent()) {
               RecipeHolder<T> $$4 = $$3.get();
               this.lastRecipe = $$4.id();
               return Optional.of($$4);
            } else {
               return Optional.empty();
            }
         }
      };
   }

   private static List<RecipeManager.ServerDisplayInfo> unpackRecipeInfo(Iterable<RecipeHolder<?>> $$0, FeatureFlagSet $$1) {
      List<RecipeManager.ServerDisplayInfo> $$2 = new ArrayList<>();
      Object2IntMap<String> $$3 = new Object2IntOpenHashMap();

      for (RecipeHolder<?> $$4 : $$0) {
         Recipe<?> $$5 = $$4.value();
         OptionalInt $$6;
         if ($$5.group().isEmpty()) {
            $$6 = OptionalInt.empty();
         } else {
            $$6 = OptionalInt.of($$3.computeIfAbsent($$5.group(), $$1x -> $$3.size()));
         }

         Optional<List<Ingredient>> $$8;
         if ($$5.isSpecial()) {
            $$8 = Optional.empty();
         } else {
            $$8 = Optional.of($$5.placementInfo().ingredients());
         }

         for (RecipeDisplay $$10 : $$5.display()) {
            if ($$10.isEnabled($$1)) {
               int $$11 = $$2.size();
               RecipeDisplayId $$12 = new RecipeDisplayId($$11);
               RecipeDisplayEntry $$13 = new RecipeDisplayEntry($$12, $$10, $$6, $$5.recipeBookCategory(), $$8);
               $$2.add(new RecipeManager.ServerDisplayInfo($$13, $$4));
            }
         }
      }

      return $$2;
   }

   private static RecipeManager.IngredientExtractor forSingleInput(RecipeType<? extends SingleItemRecipe> $$0) {
      return $$1 -> $$1.getType() == $$0 && $$1 instanceof SingleItemRecipe $$2 ? Optional.of($$2.input()) : Optional.empty();
   }

   public interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
      Optional<RecipeHolder<T>> getRecipeFor(I var1, ServerLevel var2);
   }

   public static class IngredientCollector implements Consumer<Recipe<?>> {
      final ResourceKey<RecipePropertySet> key;
      private final RecipeManager.IngredientExtractor extractor;
      private final List<Ingredient> ingredients = new ArrayList<>();

      protected IngredientCollector(ResourceKey<RecipePropertySet> $$0, RecipeManager.IngredientExtractor $$1) {
         this.key = $$0;
         this.extractor = $$1;
      }

      public void accept(Recipe<?> $$0) {
         this.extractor.apply($$0).ifPresent(this.ingredients::add);
      }

      public RecipePropertySet asPropertySet(FeatureFlagSet $$0) {
         return RecipePropertySet.create(RecipeManager.filterDisabled($$0, this.ingredients));
      }
   }

   @FunctionalInterface
   public interface IngredientExtractor {
      Optional<Ingredient> apply(Recipe<?> var1);
   }

   public record ServerDisplayInfo(RecipeDisplayEntry display, RecipeHolder<?> parent) {
   }
}
