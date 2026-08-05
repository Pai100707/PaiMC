/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
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
import org.jspecify.annotations.Nullable;

public class ResourceOrIdArgument<T>
implements ArgumentType<Holder<T>> {
    private static final Collection<String> EXAMPLES = List.of("foo", "foo:bar", "012", "{}", "true");
    public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.resource_or_id.failed_to_parse", $$0));
    public static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ELEMENT = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("argument.resource_or_id.no_such_element", $$0, $$1));
    public static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
    private final HolderLookup.Provider registryLookup;
    private final Optional<? extends HolderLookup.RegistryLookup<T>> elementLookup;
    private final Codec<T> codec;
    private final Grammar<Result<T, Tag>> grammar;
    private final ResourceKey<? extends Registry<T>> registryKey;

    protected ResourceOrIdArgument(CommandBuildContext $$0, ResourceKey<? extends Registry<T>> $$1, Codec<T> $$2) {
        this.registryLookup = $$0;
        this.elementLookup = $$0.lookup($$1);
        this.registryKey = $$1;
        this.codec = $$2;
        this.grammar = ResourceOrIdArgument.createGrammar($$1, OPS);
    }

    public static <T, O> Grammar<Result<T, O>> createGrammar(ResourceKey<? extends Registry<T>> $$0, DynamicOps<O> $$1) {
        Grammar<O> $$2 = SnbtGrammar.createParser($$1);
        Dictionary<StringReader> $$32 = new Dictionary<StringReader>();
        Atom $$4 = Atom.of("result");
        Atom $$5 = Atom.of("id");
        Atom $$6 = Atom.of("value");
        $$32.put($$5, IdentifierParseRule.INSTANCE);
        $$32.put($$6, $$2.top().value());
        NamedRule $$7 = $$32.put($$4, Term.alternative($$32.named($$5), $$32.named($$6)), $$3 -> {
            Identifier $$4 = (Identifier)$$3.get($$5);
            if ($$4 != null) {
                return new ReferenceResult(ResourceKey.create($$0, $$4));
            }
            Object $$5 = $$3.getOrThrow($$6);
            return new InlineResult($$5);
        });
        return new Grammar<Result<T, O>>($$32, $$7);
    }

    public static LootTableArgument lootTable(CommandBuildContext $$0) {
        return new LootTableArgument($$0);
    }

    public static Holder<LootTable> getLootTable(CommandContext<CommandSourceStack> $$0, String $$1) throws CommandSyntaxException {
        return ResourceOrIdArgument.getResource($$0, $$1);
    }

    public static LootModifierArgument lootModifier(CommandBuildContext $$0) {
        return new LootModifierArgument($$0);
    }

    public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandSourceStack> $$0, String $$1) {
        return ResourceOrIdArgument.getResource($$0, $$1);
    }

    public static LootPredicateArgument lootPredicate(CommandBuildContext $$0) {
        return new LootPredicateArgument($$0);
    }

    public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandSourceStack> $$0, String $$1) {
        return ResourceOrIdArgument.getResource($$0, $$1);
    }

    public static DialogArgument dialog(CommandBuildContext $$0) {
        return new DialogArgument($$0);
    }

    public static Holder<Dialog> getDialog(CommandContext<CommandSourceStack> $$0, String $$1) {
        return ResourceOrIdArgument.getResource($$0, $$1);
    }

    private static <T> Holder<T> getResource(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Holder)$$0.getArgument($$1, Holder.class);
    }

    public @Nullable Holder<T> parse(StringReader $$0) throws CommandSyntaxException {
        return this.parse($$0, this.grammar, OPS);
    }

    private <O> @Nullable Holder<T> parse(StringReader $$0, Grammar<Result<T, O>> $$1, DynamicOps<O> $$2) throws CommandSyntaxException {
        Result<T, O> $$3 = $$1.parseForCommands($$0);
        if (this.elementLookup.isEmpty()) {
            return null;
        }
        return $$3.parse((ImmutableStringReader)$$0, this.registryLookup, $$2, this.codec, this.elementLookup.get());
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> $$0, SuggestionsBuilder $$1) {
        return SharedSuggestionProvider.listSuggestions($$0, $$1, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class LootTableArgument
    extends ResourceOrIdArgument<LootTable> {
        protected LootTableArgument(CommandBuildContext $$0) {
            super($$0, Registries.LOOT_TABLE, LootTable.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootModifierArgument
    extends ResourceOrIdArgument<LootItemFunction> {
        protected LootModifierArgument(CommandBuildContext $$0) {
            super($$0, Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootPredicateArgument
    extends ResourceOrIdArgument<LootItemCondition> {
        protected LootPredicateArgument(CommandBuildContext $$0) {
            super($$0, Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class DialogArgument
    extends ResourceOrIdArgument<Dialog> {
        protected DialogArgument(CommandBuildContext $$0) {
            super($$0, Registries.DIALOG, Dialog.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static sealed interface Result<T, O>
    permits InlineResult, ReferenceResult {
        public Holder<T> parse(ImmutableStringReader var1, HolderLookup.Provider var2, DynamicOps<O> var3, Codec<T> var4, HolderLookup.RegistryLookup<T> var5) throws CommandSyntaxException;
    }

    public record ReferenceResult<T, O>(ResourceKey<T> key) implements Result<T, O>
    {
        @Override
        public Holder<T> parse(ImmutableStringReader $$0, HolderLookup.Provider $$1, DynamicOps<O> $$2, Codec<T> $$3, HolderLookup.RegistryLookup<T> $$4) throws CommandSyntaxException {
            return $$4.get(this.key).orElseThrow(() -> ERROR_NO_SUCH_ELEMENT.createWithContext($$0, (Object)this.key.identifier(), (Object)this.key.registry()));
        }
    }

    public record InlineResult<T, O>(O value) implements Result<T, O>
    {
        @Override
        public Holder<T> parse(ImmutableStringReader $$0, HolderLookup.Provider $$12, DynamicOps<O> $$2, Codec<T> $$3, HolderLookup.RegistryLookup<T> $$4) throws CommandSyntaxException {
            return Holder.direct($$3.parse($$12.createSerializationContext($$2), this.value).getOrThrow($$1 -> ERROR_FAILED_TO_PARSE.createWithContext($$0, $$1)));
        }
    }
}

