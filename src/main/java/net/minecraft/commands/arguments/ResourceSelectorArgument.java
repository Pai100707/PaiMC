package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.io.FilenameUtils;

public class ResourceSelectorArgument<T> implements ArgumentType<Collection<Reference<T>>> {
   private static final Collection<String> EXAMPLES = List.of("minecraft:*", "*:asset", "*");
   public static final Dynamic2CommandExceptionType ERROR_NO_MATCHES = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("argument.resource_selector.not_found", new Object[]{$$0, $$1})
   );
   final ResourceKey<? extends Registry<T>> registryKey;
   private final HolderLookup<T> registryLookup;

   ResourceSelectorArgument(net.minecraft.commands.CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
      this.registryKey = $$1;
      this.registryLookup = $$0.lookupOrThrow($$1);
   }

   public Collection<Reference<T>> parse(StringReader $$0) throws CommandSyntaxException {
      String $$1 = ensureNamespaced(readPattern($$0));
      List<Reference<T>> $$2 = this.registryLookup.listElements().filter($$1x -> matches($$1, $$1x.key().identifier())).toList();
      if ($$2.isEmpty()) {
         throw ERROR_NO_MATCHES.createWithContext($$0, $$1, this.registryKey.identifier());
      } else {
         return $$2;
      }
   }

   public static <T> Collection<Reference<T>> parse(StringReader $$0, HolderLookup<T> $$1) {
      String $$2 = ensureNamespaced(readPattern($$0));
      return $$1.listElements().filter($$1x -> matches($$2, $$1x.key().identifier())).toList();
   }

   private static String readPattern(StringReader $$0) {
      int $$1 = $$0.getCursor();

      while ($$0.canRead() && isAllowedPatternCharacter($$0.peek())) {
         $$0.skip();
      }

      return $$0.getString().substring($$1, $$0.getCursor());
   }

   private static boolean isAllowedPatternCharacter(char $$0) {
      return Identifier.isAllowedInIdentifier($$0) || $$0 == '*' || $$0 == '?';
   }

   private static String ensureNamespaced(String $$0) {
      return !$$0.contains(":") ? "minecraft:" + $$0 : $$0;
   }

   private static boolean matches(String $$0, Identifier $$1) {
      return FilenameUtils.wildcardMatch($$1.toString(), $$0);
   }

   public static <T> ResourceSelectorArgument<T> resourceSelector(net.minecraft.commands.CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
      return new ResourceSelectorArgument<>($$0, $$1);
   }

   public static <T> Collection<Reference<T>> getSelectedResources(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Collection<Reference<T>>)$$0.getArgument($$1, Collection.class);
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.listSuggestions(
         $$0, $$1, this.registryKey, net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType.ELEMENTS
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceSelectorArgument<T>, ResourceSelectorArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceSelectorArgument.Info<T>.Template $$0, FriendlyByteBuf $$1) {
         $$1.writeResourceKey($$0.registryKey);
      }

      public ResourceSelectorArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         return new ResourceSelectorArgument.Info.Template($$0.readRegistryKey());
      }

      public void serializeToJson(ResourceSelectorArgument.Info<T>.Template $$0, JsonObject $$1) {
         $$1.addProperty("registry", $$0.registryKey.identifier().toString());
      }

      public ResourceSelectorArgument.Info<T>.Template unpack(ResourceSelectorArgument<T> $$0) {
         return new ResourceSelectorArgument.Info.Template($$0.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceSelectorArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(final ResourceKey<? extends Registry<T>> $$1) {
            this.registryKey = $$1;
         }

         public ResourceSelectorArgument<T> instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new ResourceSelectorArgument<>($$0, this.registryKey);
         }

         @Override
         public ArgumentTypeInfo<ResourceSelectorArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
