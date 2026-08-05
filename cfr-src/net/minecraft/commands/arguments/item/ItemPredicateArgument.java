/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.item.ComponentPredicateParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.commands.ParserBasedArgument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemPredicateArgument
extends ParserBasedArgument<Result> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick", "#stick", "#stick{foo:'bar'}");
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("argument.item.id.invalid", $$0));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.tag.unknown", $$0));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.component.unknown", $$0));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("arguments.item.component.malformed", $$0, $$1));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_PREDICATE = new DynamicCommandExceptionType($$0 -> Component.translatableEscape("arguments.item.predicate.unknown", $$0));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_PREDICATE = new Dynamic2CommandExceptionType(($$0, $$1) -> Component.translatableEscape("arguments.item.predicate.malformed", $$0, $$1));
    private static final Identifier COUNT_ID = Identifier.withDefaultNamespace("count");
    static final Map<Identifier, ComponentWrapper> PSEUDO_COMPONENTS = Stream.of(new ComponentWrapper(COUNT_ID, $$0 -> true, (Decoder<? extends Predicate<ItemStack>>)MinMaxBounds.Ints.CODEC.map($$0 -> $$1 -> $$0.matches($$1.getCount())))).collect(Collectors.toUnmodifiableMap(ComponentWrapper::id, $$0 -> $$0));
    static final Map<Identifier, PredicateWrapper> PSEUDO_PREDICATES = Stream.of(new PredicateWrapper(COUNT_ID, (Decoder<? extends Predicate<ItemStack>>)MinMaxBounds.Ints.CODEC.map($$0 -> $$1 -> $$0.matches($$1.getCount())))).collect(Collectors.toUnmodifiableMap(PredicateWrapper::id, $$0 -> $$0));

    private static PredicateWrapper createComponentExistencePredicate(Holder.Reference<DataComponentType<?>> $$0) {
        Predicate<ItemStack> $$12 = $$1 -> $$1.has((DataComponentType)$$0.value());
        return new PredicateWrapper($$0.key().identifier(), (Decoder<? extends Predicate<ItemStack>>)Unit.CODEC.map($$1 -> $$12));
    }

    public ItemPredicateArgument(CommandBuildContext $$02) {
        super(ComponentPredicateParser.createGrammar(new Context($$02)).mapResult($$0 -> Util.allOf($$0)::test));
    }

    public static ItemPredicateArgument itemPredicate(CommandBuildContext $$0) {
        return new ItemPredicateArgument($$0);
    }

    public static Result getItemPredicate(CommandContext<CommandSourceStack> $$0, String $$1) {
        return (Result)$$0.getArgument($$1, Result.class);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    record PredicateWrapper(Identifier id, Decoder<? extends Predicate<ItemStack>> type) {
        public PredicateWrapper(Holder.Reference<DataComponentPredicate.Type<?>> $$02) {
            this($$02.key().identifier(), (Decoder<? extends Predicate<ItemStack>>)$$02.value().codec().map($$0 -> $$0::matches));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader $$0, Dynamic<?> $$12) throws CommandSyntaxException {
            DataResult $$2 = this.type.parse($$12);
            return (Predicate)$$2.getOrThrow($$1 -> ERROR_MALFORMED_PREDICATE.createWithContext($$0, (Object)this.id.toString(), $$1));
        }
    }

    static class Context
    implements ComponentPredicateParser.Context<Predicate<ItemStack>, ComponentWrapper, PredicateWrapper> {
        private final HolderLookup.Provider registries;
        private final HolderLookup.RegistryLookup<Item> items;
        private final HolderLookup.RegistryLookup<DataComponentType<?>> components;
        private final HolderLookup.RegistryLookup<DataComponentPredicate.Type<?>> predicates;

        Context(HolderLookup.Provider $$0) {
            this.registries = $$0;
            this.items = $$0.lookupOrThrow(Registries.ITEM);
            this.components = $$0.lookupOrThrow(Registries.DATA_COMPONENT_TYPE);
            this.predicates = $$0.lookupOrThrow(Registries.DATA_COMPONENT_PREDICATE_TYPE);
        }

        @Override
        public Predicate<ItemStack> forElementType(ImmutableStringReader $$0, Identifier $$12) throws CommandSyntaxException {
            Holder.Reference<Item> $$2 = this.items.get(ResourceKey.create(Registries.ITEM, $$12)).orElseThrow(() -> ERROR_UNKNOWN_ITEM.createWithContext($$0, (Object)$$12));
            return $$1 -> $$1.is($$2);
        }

        @Override
        public Predicate<ItemStack> forTagType(ImmutableStringReader $$0, Identifier $$12) throws CommandSyntaxException {
            HolderSet $$2 = this.items.get(TagKey.create(Registries.ITEM, $$12)).orElseThrow(() -> ERROR_UNKNOWN_TAG.createWithContext($$0, (Object)$$12));
            return $$1 -> $$1.is($$2);
        }

        @Override
        public ComponentWrapper lookupComponentType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
            ComponentWrapper $$2 = PSEUDO_COMPONENTS.get($$1);
            if ($$2 != null) {
                return $$2;
            }
            DataComponentType $$3 = this.components.get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, $$1)).map(Holder::value).orElseThrow(() -> ERROR_UNKNOWN_COMPONENT.createWithContext($$0, (Object)$$1));
            return ComponentWrapper.create($$0, $$1, $$3);
        }

        @Override
        public Predicate<ItemStack> createComponentTest(ImmutableStringReader $$0, ComponentWrapper $$1, Dynamic<?> $$2) throws CommandSyntaxException {
            return $$1.decode($$0, RegistryOps.injectRegistryContext($$2, this.registries));
        }

        @Override
        public Predicate<ItemStack> createComponentTest(ImmutableStringReader $$0, ComponentWrapper $$1) {
            return $$1.presenceChecker;
        }

        @Override
        public PredicateWrapper lookupPredicateType(ImmutableStringReader $$0, Identifier $$1) throws CommandSyntaxException {
            PredicateWrapper $$2 = PSEUDO_PREDICATES.get($$1);
            if ($$2 != null) {
                return $$2;
            }
            return this.predicates.get(ResourceKey.create(Registries.DATA_COMPONENT_PREDICATE_TYPE, $$1)).map(PredicateWrapper::new).or(() -> this.components.get(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, $$1)).map(ItemPredicateArgument::createComponentExistencePredicate)).orElseThrow(() -> ERROR_UNKNOWN_PREDICATE.createWithContext($$0, (Object)$$1));
        }

        @Override
        public Predicate<ItemStack> createPredicateTest(ImmutableStringReader $$0, PredicateWrapper $$1, Dynamic<?> $$2) throws CommandSyntaxException {
            return $$1.decode($$0, RegistryOps.injectRegistryContext($$2, this.registries));
        }

        @Override
        public Stream<Identifier> listElementTypes() {
            return this.items.listElementIds().map(ResourceKey::identifier);
        }

        @Override
        public Stream<Identifier> listTagTypes() {
            return this.items.listTagIds().map(TagKey::location);
        }

        @Override
        public Stream<Identifier> listComponentTypes() {
            return Stream.concat(PSEUDO_COMPONENTS.keySet().stream(), this.components.listElements().filter($$0 -> !((DataComponentType)$$0.value()).isTransient()).map($$0 -> $$0.key().identifier()));
        }

        @Override
        public Stream<Identifier> listPredicateTypes() {
            return Stream.concat(PSEUDO_PREDICATES.keySet().stream(), this.predicates.listElementIds().map(ResourceKey::identifier));
        }

        @Override
        public Predicate<ItemStack> negate(Predicate<ItemStack> $$0) {
            return $$0.negate();
        }

        @Override
        public Predicate<ItemStack> anyOf(List<Predicate<ItemStack>> $$0) {
            return Util.anyOf($$0);
        }

        @Override
        public /* synthetic */ Object anyOf(List list) {
            return this.anyOf(list);
        }

        @Override
        public /* synthetic */ Object createPredicateTest(ImmutableStringReader immutableStringReader, Object object, Dynamic dynamic) throws CommandSyntaxException {
            return this.createPredicateTest(immutableStringReader, (PredicateWrapper)object, dynamic);
        }

        @Override
        public /* synthetic */ Object lookupPredicateType(ImmutableStringReader immutableStringReader, Identifier identifier) throws CommandSyntaxException {
            return this.lookupPredicateType(immutableStringReader, identifier);
        }

        @Override
        public /* synthetic */ Object lookupComponentType(ImmutableStringReader immutableStringReader, Identifier identifier) throws CommandSyntaxException {
            return this.lookupComponentType(immutableStringReader, identifier);
        }

        @Override
        public /* synthetic */ Object forTagType(ImmutableStringReader immutableStringReader, Identifier identifier) throws CommandSyntaxException {
            return this.forTagType(immutableStringReader, identifier);
        }

        @Override
        public /* synthetic */ Object forElementType(ImmutableStringReader immutableStringReader, Identifier identifier) throws CommandSyntaxException {
            return this.forElementType(immutableStringReader, identifier);
        }
    }

    public static interface Result
    extends Predicate<ItemStack> {
    }

    static final class ComponentWrapper
    extends Record {
        private final Identifier id;
        final Predicate<ItemStack> presenceChecker;
        private final Decoder<? extends Predicate<ItemStack>> valueChecker;

        ComponentWrapper(Identifier $$0, Predicate<ItemStack> $$1, Decoder<? extends Predicate<ItemStack>> $$2) {
            this.id = $$0;
            this.presenceChecker = $$1;
            this.valueChecker = $$2;
        }

        public static <T> ComponentWrapper create(ImmutableStringReader $$0, Identifier $$12, DataComponentType<T> $$2) throws CommandSyntaxException {
            Codec<T> $$3 = $$2.codec();
            if ($$3 == null) {
                throw ERROR_UNKNOWN_COMPONENT.createWithContext($$0, (Object)$$12);
            }
            return new ComponentWrapper($$12, $$1 -> $$1.has($$2), (Decoder<? extends Predicate<ItemStack>>)$$3.map($$1 -> $$2 -> {
                Object $$3 = $$2.get($$2);
                return Objects.equals($$1, $$3);
            }));
        }

        public Predicate<ItemStack> decode(ImmutableStringReader $$0, Dynamic<?> $$12) throws CommandSyntaxException {
            DataResult $$2 = this.valueChecker.parse($$12);
            return (Predicate)$$2.getOrThrow($$1 -> ERROR_MALFORMED_COMPONENT.createWithContext($$0, (Object)this.id.toString(), $$1));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ComponentWrapper.class, "id;presenceChecker;valueChecker", "id", "presenceChecker", "valueChecker"}, this, $$0);
        }

        public Identifier id() {
            return this.id;
        }

        public Predicate<ItemStack> presenceChecker() {
            return this.presenceChecker;
        }

        public Decoder<? extends Predicate<ItemStack>> valueChecker() {
            return this.valueChecker;
        }
    }
}

