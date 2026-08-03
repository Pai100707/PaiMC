package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public class ResourceOrTagKeyArgument<T> implements ArgumentType<ResourceOrTagKeyArgument.Result<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012", "#skeletons", "#minecraft:skeletons");
   final ResourceKey<? extends Registry<T>> registryKey;

   public ResourceOrTagKeyArgument(ResourceKey<? extends Registry<T>> $$0) {
      this.registryKey = $$0;
   }

   public static <T> ResourceOrTagKeyArgument<T> resourceOrTagKey(ResourceKey<? extends Registry<T>> $$0) {
      return new ResourceOrTagKeyArgument<>($$0);
   }

   public static <T> ResourceOrTagKeyArgument.Result<T> getResourceOrTagKey(
      CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2, DynamicCommandExceptionType $$3
   ) throws CommandSyntaxException {
      ResourceOrTagKeyArgument.Result<?> $$4 = (ResourceOrTagKeyArgument.Result<?>)$$0.getArgument($$1, ResourceOrTagKeyArgument.Result.class);
      Optional<ResourceOrTagKeyArgument.Result<T>> $$5 = $$4.cast($$2);
      return $$5.orElseThrow(() -> $$3.create($$4));
   }

   public ResourceOrTagKeyArgument.Result<T> parse(StringReader $$0) throws CommandSyntaxException {
      if ($$0.canRead() && $$0.peek() == '#') {
         int $$1 = $$0.getCursor();

         try {
            $$0.skip();
            Identifier $$2 = Identifier.read($$0);
            return new ResourceOrTagKeyArgument.TagResult<>(TagKey.create(this.registryKey, $$2));
         } catch (CommandSyntaxException var4) {
            $$0.setCursor($$1);
            throw var4;
         }
      } else {
         Identifier $$4 = Identifier.read($$0);
         return new ResourceOrTagKeyArgument.ResourceResult<>(ResourceKey.create(this.registryKey, $$4));
      }
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.listSuggestions(
         $$0, $$1, this.registryKey, net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType.ALL
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ResourceOrTagKeyArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceOrTagKeyArgument.Info<T>.Template $$0, FriendlyByteBuf $$1) {
         $$1.writeResourceKey($$0.registryKey);
      }

      public ResourceOrTagKeyArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         return new ResourceOrTagKeyArgument.Info.Template($$0.readRegistryKey());
      }

      public void serializeToJson(ResourceOrTagKeyArgument.Info<T>.Template $$0, JsonObject $$1) {
         $$1.addProperty("registry", $$0.registryKey.identifier().toString());
      }

      public ResourceOrTagKeyArgument.Info<T>.Template unpack(ResourceOrTagKeyArgument<T> $$0) {
         return new ResourceOrTagKeyArgument.Info.Template($$0.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceOrTagKeyArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(final ResourceKey<? extends Registry<T>> $$1) {
            this.registryKey = $$1;
         }

         public ResourceOrTagKeyArgument<T> instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new ResourceOrTagKeyArgument<>(this.registryKey);
         }

         @Override
         public ArgumentTypeInfo<ResourceOrTagKeyArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }

   record ResourceResult<T>(ResourceKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
      @Override
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.left(this.key);
      }

      @Override
      public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
         return this.key.cast($$0).map(ResourceOrTagKeyArgument.ResourceResult::new);
      }

      public boolean test(Holder<T> $$0) {
         return $$0.is(this.key);
      }

      @Override
      public String asPrintable() {
         return this.key.identifier().toString();
      }
   }

   public interface Result<T> extends Predicate<Holder<T>> {
      Either<ResourceKey<T>, TagKey<T>> unwrap();

      <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> var1);

      String asPrintable();
   }

   record TagResult<T>(TagKey<T> key) implements ResourceOrTagKeyArgument.Result<T> {
      @Override
      public Either<ResourceKey<T>, TagKey<T>> unwrap() {
         return Either.right(this.key);
      }

      @Override
      public <E> Optional<ResourceOrTagKeyArgument.Result<E>> cast(ResourceKey<? extends Registry<E>> $$0) {
         return this.key.cast($$0).map(ResourceOrTagKeyArgument.TagResult::new);
      }

      public boolean test(Holder<T> $$0) {
         return $$0.is(this.key);
      }

      @Override
      public String asPrintable() {
         return "#" + this.key.location();
      }
   }
}
