package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.IdentifierParseRule;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ResourceOrIdArgument<T> implements ArgumentType<Holder<T>> {
   private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
   public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType(
      $$0 -> Component.translatableEscape("argument.resource_or_id.failed_to_parse", new Object[]{$$0})
   );
   public static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ELEMENT = new Dynamic2CommandExceptionType(
      ($$0, $$1) -> Component.translatableEscape("argument.resource_or_id.no_such_element", new Object[]{$$0, $$1})
   );
   public static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
   private final Provider registryLookup;
   private final Optional<? extends RegistryLookup<T>> elementLookup;
   private final Codec<T> codec;
   private final Grammar<ResourceOrIdArgument.Result<T, Tag>> grammar;
   private final ResourceKey<? extends Registry<T>> registryKey;

   protected ResourceOrIdArgument(net.minecraft.commands.CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1, Codec<T> $$2) {
      this.registryLookup = $$0;
      this.elementLookup = $$0.lookup($$1);
      this.registryKey = $$1;
      this.codec = $$2;
      this.grammar = createGrammar($$1, OPS);
   }

   public static <T, O> Grammar<ResourceOrIdArgument.Result<T, O>> createGrammar(ResourceKey<? extends Registry<T>> $$0, DynamicOps<O> $$1) {
      Grammar<O> $$2 = SnbtGrammar.createParser($$1);
      Dictionary<StringReader> $$3 = new Dictionary();
      Atom<ResourceOrIdArgument.Result<T, O>> $$4 = Atom.of("result");
      Atom<Identifier> $$5 = Atom.of("id");
      Atom<O> $$6 = Atom.of("value");
      $$3.put($$5, IdentifierParseRule.INSTANCE);
      $$3.put($$6, $$2.top().value());
      NamedRule<StringReader, ResourceOrIdArgument.Result<T, O>> $$7 = $$3.put($$4, Term.alternative(new Term[]{$$3.named($$5), $$3.named($$6)}), $$3x -> {
         Identifier $$4x = (Identifier)$$3x.get($$5);
         if ($$4x != null) {
            return new ResourceOrIdArgument.ReferenceResult(ResourceKey.create($$0, $$4x));
         } else {
            O $$5x = (O)$$3x.getOrThrow($$6);
            return new ResourceOrIdArgument.InlineResult($$5x);
         }
      });
      return new Grammar($$3, $$7);
   }

   public static ResourceOrIdArgument.LootTableArgument lootTable(net.minecraft.commands.CommandBuildContext $$0) {
      return new ResourceOrIdArgument.LootTableArgument($$0);
   }

   public static Holder<LootTable> getLootTable(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
      return getResource($$0, $$1);
   }

   public static ResourceOrIdArgument.LootModifierArgument lootModifier(net.minecraft.commands.CommandBuildContext $$0) {
      return new ResourceOrIdArgument.LootModifierArgument($$0);
   }

   public static Holder<LootItemFunction> getLootModifier(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return getResource($$0, $$1);
   }

   public static ResourceOrIdArgument.LootPredicateArgument lootPredicate(net.minecraft.commands.CommandBuildContext $$0) {
      return new ResourceOrIdArgument.LootPredicateArgument($$0);
   }

   public static Holder<LootItemCondition> getLootPredicate(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return getResource($$0, $$1);
   }

   public static ResourceOrIdArgument.DialogArgument dialog(net.minecraft.commands.CommandBuildContext $$0) {
      return new ResourceOrIdArgument.DialogArgument($$0);
   }

   public static Holder<Dialog> getDialog(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return getResource($$0, $$1);
   }

   private static <T> Holder<T> getResource(CommandContext<net.minecraft.commands.CommandSourceStack> $$0, String $$1) {
      return (Holder<T>)$$0.getArgument($$1, Holder.class);
   }

   
   public Holder<T> parse(StringReader $$0) throws CommandSyntaxException {
      return this.parse($$0, this.grammar, OPS);
   }

   
   private <O> Holder<T> parse(StringReader $$0, Grammar<ResourceOrIdArgument.Result<T, O>> $$1, DynamicOps<O> $$2) throws CommandSyntaxException {
      ResourceOrIdArgument.Result<T, O> $$3 = (ResourceOrIdArgument.Result<T, O>)$$1.parseForCommands($$0);
      return this.elementLookup.isEmpty() ? null : $$3.parse($$0, this.registryLookup, $$2, this.codec, (RegistryLookup<T>)this.elementLookup.get());
   }

   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
      return net.minecraft.commands.SharedSuggestionProvider.listSuggestions(
         $$0, $$1, this.registryKey, net.minecraft.commands.SharedSuggestionProvider.ElementSuggestionType.ELEMENTS
      );
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class DialogArgument extends ResourceOrIdArgument<Dialog> {
      protected DialogArgument(net.minecraft.commands.CommandBuildContext $$0) {
         super($$0, Registries.DIALOG, Dialog.DIRECT_CODEC);
      }
   }

   public record InlineResult<T, O>(O value) implements ResourceOrIdArgument.Result<T, O> {
      @Override
      public Holder<T> parse(ImmutableStringReader $$0, Provider $$1, DynamicOps<O> $$2, Codec<T> $$3, RegistryLookup<T> $$4) throws CommandSyntaxException {
         return Holder.direct(
            $$3.parse($$1.createSerializationContext($$2), this.value)
               .getOrThrow($$1x -> ResourceOrIdArgument.ERROR_FAILED_TO_PARSE.createWithContext($$0, $$1x))
         );
      }
   }

   public static class LootModifierArgument extends ResourceOrIdArgument<LootItemFunction> {
      protected LootModifierArgument(net.minecraft.commands.CommandBuildContext $$0) {
         super($$0, Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC);
      }
   }

   public static class LootPredicateArgument extends ResourceOrIdArgument<LootItemCondition> {
      protected LootPredicateArgument(net.minecraft.commands.CommandBuildContext $$0) {
         super($$0, Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);
      }
   }

   public static class LootTableArgument extends ResourceOrIdArgument<LootTable> {
      protected LootTableArgument(net.minecraft.commands.CommandBuildContext $$0) {
         super($$0, Registries.LOOT_TABLE, LootTable.DIRECT_CODEC);
      }
   }

   public record ReferenceResult<T, O>(ResourceKey<T> key) implements ResourceOrIdArgument.Result<T, O> {
      @Override
      public Holder<T> parse(ImmutableStringReader $$0, Provider $$1, DynamicOps<O> $$2, Codec<T> $$3, RegistryLookup<T> $$4) throws CommandSyntaxException {
         return (Holder<T>)$$4.get(this.key)
            .orElseThrow(() -> ResourceOrIdArgument.ERROR_NO_SUCH_ELEMENT.createWithContext($$0, this.key.identifier(), this.key.registry()));
      }
   }

   public sealed interface Result<T, O> permits ResourceOrIdArgument.InlineResult, ResourceOrIdArgument.ReferenceResult {
      Holder<T> parse(ImmutableStringReader var1, Provider var2, DynamicOps<O> var3, Codec<T> var4, RegistryLookup<T> var5) throws CommandSyntaxException;
   }
}
