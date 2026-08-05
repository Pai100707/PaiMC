/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class ItemParser {
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.item.id.invalid", $$0));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.component.unknown", $$0));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("arguments.item.component.malformed", $$0, $$1));
    static final SimpleCommandExceptionType ERROR_EXPECTED_COMPONENT = new SimpleCommandExceptionType((Message)Component.translatable("arguments.item.component.expected"));
    static final DynamicCommandExceptionType ERROR_REPEATED_COMPONENT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.component.repeated", $$0));
    private static final DynamicCommandExceptionType ERROR_MALFORMED_ITEM = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.malformed", $$0));
    public static final char SYNTAX_START_COMPONENTS = '[';
    public static final char SYNTAX_END_COMPONENTS = ']';
    public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
    public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
    public static final char SYNTAX_REMOVED_COMPONENT = '!';
    static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    final HolderLookup.RegistryLookup<Item> items;
    final RegistryOps<Tag> registryOps;
    final TagParser<Tag> tagParser;

    public ItemParser(HolderLookup.Provider $$0) {
        this.items = $$0.lookupOrThrow(Registries.ITEM);
        this.registryOps = $$0.createSerializationContext(NbtOps.INSTANCE);
        this.tagParser = TagParser.create(this.registryOps);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public ItemResult parse(StringReader $$0) throws CommandSyntaxException {
        final @Nullable MutableObject $$1 = new MutableObject();
        final DataComponentPatch.Builder $$2 = DataComponentPatch.builder();
        this.parse($$0, new Visitor(){

            @Override
            public void visitItem(Holder<Item> $$0) {
                $$1.setValue($$0);
            }

            @Override
            public <T> void visitComponent(DataComponentType<T> $$0, T $$12) {
                $$2.set($$0, $$12);
            }

            @Override
            public <T> void visitRemovedComponent(DataComponentType<T> $$0) {
                $$2.remove($$0);
            }
        });
        Holder $$3 = Objects.requireNonNull((Holder)$$1.get(), "Parser gave no item");
        DataComponentPatch $$4 = $$2.build();
        ItemParser.validateComponents($$0, $$3, $$4);
        return new ItemResult($$3, $$4);
    }

    private static void validateComponents(StringReader $$0, Holder<Item> $$12, DataComponentPatch $$2) throws CommandSyntaxException {
        PatchedDataComponentMap $$3 = PatchedDataComponentMap.fromPatch($$12.value().components(), $$2);
        DataResult<Unit> $$4 = ItemStack.validateComponents($$3);
        $$4.getOrThrow($$1 -> ERROR_MALFORMED_ITEM.createWithContext((ImmutableStringReader)$$0, $$1));
    }

    public void parse(StringReader $$0, Visitor $$1) throws CommandSyntaxException {
        int $$2 = $$0.getCursor();
        try {
            new State($$0, $$1).parse();
        }
        catch (CommandSyntaxException $$3) {
            $$0.setCursor($$2);
            throw $$3;
        }
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder $$0) {
        StringReader $$1 = new StringReader($$0.getInput());
        $$1.setCursor($$0.getStart());
        SuggestionsVisitor $$2 = new SuggestionsVisitor();
        State $$3 = new State($$1, $$2);
        try {
            $$3.parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return $$2.resolveSuggestions($$0, $$1);
    }

    public static interface Visitor {
        default public void visitItem(Holder<Item> $$0) {
        }

        default public <T> void visitComponent(DataComponentType<T> $$0, T $$1) {
        }

        default public <T> void visitRemovedComponent(DataComponentType<T> $$0) {
        }

        default public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> $$0) {
        }
    }

    public record ItemResult(Holder<Item> item, DataComponentPatch components) {
    }

    class State {
        private final StringReader reader;
        private final Visitor visitor;

        State(StringReader $$0, Visitor $$1) {
            this.reader = $$0;
            this.visitor = $$1;
        }

        public void parse() throws CommandSyntaxException {
            this.visitor.visitSuggestions(this::suggestItem);
            this.readItem();
            this.visitor.visitSuggestions(this::suggestStartComponents);
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.visitor.visitSuggestions(SUGGEST_NOTHING);
                this.readComponents();
            }
        }

        private void readItem() throws CommandSyntaxException {
            int $$0 = this.reader.getCursor();
            Identifier $$1 = Identifier.read(this.reader);
            this.visitor.visitItem((Holder<Item>)ItemParser.this.items.get(ResourceKey.create(Registries.ITEM, $$1)).orElseThrow(() -> {
                this.reader.setCursor($$0);
                return ERROR_UNKNOWN_ITEM.createWithContext((ImmutableStringReader)this.reader, (Object)$$1);
            }));
        }

        private void readComponents() throws CommandSyntaxException {
            this.reader.expect('[');
            this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
            ReferenceArraySet $$0 = new ReferenceArraySet();
            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                if (this.reader.canRead() && this.reader.peek() == '!') {
                    this.reader.skip();
                    this.visitor.visitSuggestions(this::suggestComponent);
                    DataComponentType<?> $$1 = State.readComponentType(this.reader);
                    if (!$$0.add($$1)) {
                        throw ERROR_REPEATED_COMPONENT.create($$1);
                    }
                    this.visitor.visitRemovedComponent($$1);
                    this.visitor.visitSuggestions(SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                } else {
                    DataComponentType<?> $$2 = State.readComponentType(this.reader);
                    if (!$$0.add($$2)) {
                        throw ERROR_REPEATED_COMPONENT.create($$2);
                    }
                    this.visitor.visitSuggestions(this::suggestAssignment);
                    this.reader.skipWhitespace();
                    this.reader.expect('=');
                    this.visitor.visitSuggestions(SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                    this.readComponent(ItemParser.this.tagParser, ItemParser.this.registryOps, $$2);
                    this.reader.skipWhitespace();
                }
                this.visitor.visitSuggestions(this::suggestNextOrEndComponents);
                if (!this.reader.canRead() || this.reader.peek() != ',') break;
                this.reader.skip();
                this.reader.skipWhitespace();
                this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
                if (this.reader.canRead()) continue;
                throw ERROR_EXPECTED_COMPONENT.createWithContext((ImmutableStringReader)this.reader);
            }
            this.reader.expect(']');
            this.visitor.visitSuggestions(SUGGEST_NOTHING);
        }

        public static DataComponentType<?> readComponentType(StringReader $$0) throws CommandSyntaxException {
            if (!$$0.canRead()) {
                throw ERROR_EXPECTED_COMPONENT.createWithContext((ImmutableStringReader)$$0);
            }
            int $$1 = $$0.getCursor();
            Identifier $$2 = Identifier.read($$0);
            DataComponentType<?> $$3 = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue($$2);
            if ($$3 == null || $$3.isTransient()) {
                $$0.setCursor($$1);
                throw ERROR_UNKNOWN_COMPONENT.createWithContext((ImmutableStringReader)$$0, (Object)$$2);
            }
            return $$3;
        }

        private <T, O> void readComponent(TagParser<O> $$0, RegistryOps<O> $$1, DataComponentType<T> $$22) throws CommandSyntaxException {
            int $$3 = this.reader.getCursor();
            O $$4 = $$0.parseAsArgument(this.reader);
            DataResult $$5 = $$22.codecOrThrow().parse($$1, $$4);
            this.visitor.visitComponent($$22, $$5.getOrThrow($$2 -> {
                this.reader.setCursor($$3);
                return ERROR_MALFORMED_COMPONENT.createWithContext((ImmutableStringReader)this.reader, (Object)$$22.toString(), $$2);
            }));
        }

        private CompletableFuture<Suggestions> suggestStartComponents(SuggestionsBuilder $$0) {
            if ($$0.getRemaining().isEmpty()) {
                $$0.suggest(String.valueOf('['));
            }
            return $$0.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestNextOrEndComponents(SuggestionsBuilder $$0) {
            if ($$0.getRemaining().isEmpty()) {
                $$0.suggest(String.valueOf(','));
                $$0.suggest(String.valueOf(']'));
            }
            return $$0.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestAssignment(SuggestionsBuilder $$0) {
            if ($$0.getRemaining().isEmpty()) {
                $$0.suggest(String.valueOf('='));
            }
            return $$0.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder $$0) {
            return SharedSuggestionProvider.suggestResource(ItemParser.this.items.listElementIds().map(ResourceKey::identifier), $$0);
        }

        private CompletableFuture<Suggestions> suggestComponentAssignmentOrRemoval(SuggestionsBuilder $$0) {
            $$0.suggest(String.valueOf('!'));
            return this.suggestComponent($$0, String.valueOf('='));
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder $$0) {
            return this.suggestComponent($$0, "");
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder $$02, String $$1) {
            String $$22 = $$02.getRemaining().toLowerCase(Locale.ROOT);
            SharedSuggestionProvider.filterResources(BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), $$22, $$0 -> ((ResourceKey)$$0.getKey()).identifier(), $$2 -> {
                DataComponentType $$3 = (DataComponentType)$$2.getValue();
                if ($$3.codec() != null) {
                    Identifier $$4 = ((ResourceKey)$$2.getKey()).identifier();
                    $$02.suggest(String.valueOf($$4) + $$1);
                }
            });
            return $$02.buildFuture();
        }
    }

    static class SuggestionsVisitor
    implements Visitor {
        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

        SuggestionsVisitor() {
        }

        @Override
        public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> $$0) {
            this.suggestions = $$0;
        }

        public CompletableFuture<Suggestions> resolveSuggestions(SuggestionsBuilder $$0, StringReader $$1) {
            return this.suggestions.apply($$0.createOffset($$1.getCursor()));
        }
    }
}

