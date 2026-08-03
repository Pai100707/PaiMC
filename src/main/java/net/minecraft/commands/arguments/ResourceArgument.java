package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ResourceArgument<T> implements ArgumentType<Reference<T>> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");
   private static final DynamicCommandExceptionType ERROR_NOT_SUMMONABLE_ENTITY = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("entity.not_summonable", new Object[]{$$0})
   );
   public static final Dynamic2CommandExceptionType ERROR_UNKNOWN_RESOURCE = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("argument.resource.not_found", new Object[]{$$0, $$1})
   );
   public static final Dynamic3CommandExceptionType ERROR_INVALID_RESOURCE_TYPE = new Dynamic3CommandExceptionType(
      ($$0, $$1, $$2) -> Component.translatableEscape("argument.resource.invalid_type", new Object[]{$$0, $$1, $$2})
   );
   final ResourceKey<? extends Registry<T>> registryKey;
   private final HolderLookup<T> registryLookup;

   public ResourceArgument(net.minecraft.commands.CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
      this.registryKey = $$1;
      this.registryLookup = $$0.lookupOrThrow($$1);
   }

   public static <T> ResourceArgument<T> resource(net.minecraft.commands.CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1) {
      return new ResourceArgument<>($$0, $$1);
   }

   public static <T> Reference<T> getResource(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1, ResourceKey<Registry<T>> $$2) throws CommandSyntaxException {
      Reference<T> $$3 = (Reference<T>)$$0.getArgument($$1, Reference.class);
      ResourceKey<?> $$4 = $$3.key();
      if ($$4.isFor($$2)) {
         return $$3;
      } else {
         throw ERROR_INVALID_RESOURCE_TYPE.create($$4.identifier(), $$4.registry(), $$2.identifier());
      }
   }

   public static Reference<Attribute> getAttribute(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.ATTRIBUTE);
   }

   public static Reference<ConfiguredFeature<?, ?>> getConfiguredFeature(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.CONFIGURED_FEATURE);
   }

   public static Reference<Structure> getStructure(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.STRUCTURE);
   }

   public static Reference<EntityType<?>> getEntityType(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.ENTITY_TYPE);
   }

   public static Reference<EntityType<?>> getSummonableEntityType(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      Reference<EntityType<?>> $$2 = getResource($$0, $$1, Registries.ENTITY_TYPE);
      if (!((EntityType)$$2.value()).canSummon()) {
         throw ERROR_NOT_SUMMONABLE_ENTITY.create($$2.key().identifier().toString());
      } else {
         return $$2;
      }
   }

   public static Reference<MobEffect> getMobEffect(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.MOB_EFFECT);
   }

   public static Reference<Enchantment> getEnchantment(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1, Registries.ENCHANTMENT);
   }

   public Reference<T> parse(StringReader $$0) throws CommandSyntaxException {
      Identifier $$1 = Identifier.read($$0);
      ResourceKey<T> $$2 = ResourceKey.create(this.registryKey, $$1);
      return (Reference<T>)this.registryLookup.get($$2).orElseThrow(() -> ERROR_UNKNOWN_RESOURCE.createWithContext($$0, $$1, this.registryKey.identifier()));
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.listSuggestions(
         $$0, $$1, this.registryKey, net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType.ELEMENTS
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Info<T> implements ArgumentTypeInfo<ResourceArgument<T>, ResourceArgument.Info<T>.Template> {
      public void serializeToNetwork(ResourceArgument.Info<T>.Template $$0, FriendlyByteBuf $$1) {
         $$1.writeResourceKey($$0.registryKey);
      }

      public ResourceArgument.Info<T>.Template deserializeFromNetwork(FriendlyByteBuf $$0) {
         return new ResourceArgument.Info.Template($$0.readRegistryKey());
      }

      public void serializeToJson(ResourceArgument.Info<T>.Template $$0, JsonObject $$1) {
         $$1.addProperty("registry", $$0.registryKey.identifier().toString());
      }

      public ResourceArgument.Info<T>.Template unpack(ResourceArgument<T> $$0) {
         return new ResourceArgument.Info.Template($$0.registryKey);
      }

      public final class Template implements ArgumentTypeInfo.Template<ResourceArgument<T>> {
         final ResourceKey<? extends Registry<T>> registryKey;

         Template(final ResourceKey<? extends Registry<T>> $$1) {
            this.registryKey = $$1;
         }

         public ResourceArgument<T> instantiate(net.minecraft.commands.CommandBuildContext $$0) {
            return new ResourceArgument<>($$0, this.registryKey);
         }

         @Override
         public ArgumentTypeInfo<ResourceArgument<T>, ?> type() {
            return Info.this;
         }
      }
   }
}
