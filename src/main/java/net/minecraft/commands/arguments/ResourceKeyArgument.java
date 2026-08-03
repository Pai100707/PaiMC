package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ResourceKeyArgument<T> implements ArgumentType<ResourceKey<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_INVALID_FEATURE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.place.feature.invalid", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INVALID_STRUCTURE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.place.structure.invalid", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INVALID_TEMPLATE_POOL = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("commands.place.jigsaw.invalid", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INVALID_RECIPE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("recipe.notFound", new Object[]{$$0})
   );
   private static final DynamicCommandExceptionType ERROR_INVALID_ADVANCEMENT = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("advancement.advancementNotFound", new Object[]{$$0})
   );
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceKeyArgument(ResourceKey<? extends Registry<T>> $$0) {
      this.registryKey = $$0;
   }

   public static <T> ResourceKeyArgument<T> key(ResourceKey<? extends Registry<T>> $$0) {
      return new ResourceKeyArgument<>($$0);
   }

   public static <T> ResourceKey<T> getRegistryKey(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2, DynamicCommandExceptionType $$3
   ) throws CommandSyntaxException {
      ResourceKey<?> $$4 = (ResourceKey<?>)$$0.getArgument($$1, ResourceKey.class);
      Optional<ResourceKey<T>> $$5 = $$4.cast($$2);
      return $$5.orElseThrow(() -> $$3.create($$4.identifier()));
   }

   private static <T> Registry<T> getRegistry(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, ResourceKey<? extends Registry<T>> $$1) {
      return ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getServer().registryAccess().lookupOrThrow($$1);
   }

   private static <T> Reference<T> resolveKey(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2, DynamicCommandExceptionType $$3
   ) throws CommandSyntaxException {
      ResourceKey<T> $$4 = getRegistryKey($$0, $$1, $$2, $$3);
      return (Reference<T>)getRegistry($$0, $$2).get($$4).orElseThrow(() -> $$3.create($$4.identifier()));
   }

   public static Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return resolveKey($$0, $$1, Registries.CONFIGURED_FEATURE, ERROR_INVALID_FEATURE);
   }

   public static Reference<Structure> getStructure(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return resolveKey($$0, $$1, Registries.STRUCTURE, ERROR_INVALID_STRUCTURE);
   }

   public static Reference<StructureTemplatePool> getStructureTemplatePool(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return resolveKey($$0, $$1, Registries.TEMPLATE_POOL, ERROR_INVALID_TEMPLATE_POOL);
   }

   public static RecipeHolder<?> getRecipe(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      RecipeManager $$2 = ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getServer().getRecipeManager();
      ResourceKey<Recipe<?>> $$3 = getRegistryKey($$0, $$1, Registries.RECIPE, ERROR_INVALID_RECIPE);
      return (RecipeHolder<?>)$$2.byKey($$3).orElseThrow(() -> ERROR_INVALID_RECIPE.create($$3.identifier()));
   }

   public static AdvancementHolder getAdvancement(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      ResourceKey<Advancement> $$2 = getRegistryKey($$0, $$1, Registries.ADVANCEMENT, ERROR_INVALID_ADVANCEMENT);
      AdvancementHolder $$3 = ((net.minecraft.commands.CommandSourceStack)$$0.getSource()).getServer().getAdvancements().get($$2.identifier());
      if ($$3 == null) {
         throw ERROR_INVALID_ADVANCEMENT.create($$2.identifier());
      } else {
         return $$3;
      }
   }

   public ResourceKey<T> parse(StringReader $$0) throws CommandSyntaxException {
      Identifier $$1 = Identifier.read($$0);
      return ResourceKey.create(this.registryKey, $$1);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.listSuggestions(
         $$0, $$1, this.registryKey, net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType.ELEMENTS
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceKeyArgument<T>, ResourceKeyArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceKeyArgument.Info<T>.Template $$0, FriendlyByteBuf $$1) {
         $$1.writeResourceKey($$0.registryKey);
      }

      public ResourceKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         return new ResourceKeyArgument.Info.Template($$0.readRegistryKey());
      }

      public void serializeToJson(ResourceKeyArgument.Info<T>.Template $$0, JsonObject $$1) {
         $$1.addProperty("registry", $$0.registryKey.identifier().toString());
      }

      public ResourceKeyArgument.Info<T>.Template unpack(ResourceKeyArgument<T> $$0) {
         return new ResourceKeyArgument.Info.Template($$0.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceKeyArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(final ResourceKey<? extends Registry<T>> $$1) {
            this.registryKey = $$1;
         }

         public ResourceKeyArgument<T> instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new ResourceKeyArgument<>(this.registryKey);
         }

         @Override
         public ArgumentTypeInfo<ResourceKeyArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
