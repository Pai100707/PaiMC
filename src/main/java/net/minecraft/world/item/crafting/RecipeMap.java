package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RecipeMap {
   public static final RecipeMap EMPTY = new RecipeMap(ImmutableMultimap.of(), Map.of());
   private final Multimap<RecipeType<?>, RecipeHolder<?>> byType;
   private final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

   private RecipeMap(Multimap<RecipeType<?>, RecipeHolder<?>> $$0, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> $$1) {
      this.byType = $$0;
      this.byKey = $$1;
   }

   public static RecipeMap create(Iterable<RecipeHolder<?>> $$0) {
      Builder<RecipeType<?>, RecipeHolder<?>> $$1 = ImmutableMultimap.builder();
      com.google.common.collect.ImmutableMap.Builder<ResourceKey<Recipe<?>>, RecipeHolder<?>> $$2 = ImmutableMap.builder();

      for (RecipeHolder<?> $$3 : $$0) {
         $$1.put($$3.value().getType(), $$3);
         $$2.put($$3.id(), $$3);
      }

      return new RecipeMap($$1.build(), $$2.build());
   }

   public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> $$0) {
      return this.byType.get($$0);
   }

   public Collection<RecipeHolder<?>> values() {
      return this.byKey.values();
   }

   
   public RecipeHolder<?> byKey(ResourceKey<Recipe<?>> $$0) {
      return this.byKey.get($$0);
   }

   public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> $$0, I $$1, Level $$2) {
      return $$1.isEmpty() ? Stream.empty() : this.byType($$0).stream().filter($$2x -> $$2x.value().matches($$1, $$2));
   }
}
