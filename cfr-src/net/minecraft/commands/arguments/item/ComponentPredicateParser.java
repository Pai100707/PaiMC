/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.IdentifierParseRule;
import net.minecraft.util.parsing.packrat.commands.ResourceLookupRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.TagParseRule;

public class ComponentPredicateParser {
    public static <T, C, P> Grammar<List<T>> createGrammar(Context<T, C, P> $$02) {
        Atom $$1 = Atom.of("top");
        Atom $$22 = Atom.of("type");
        Atom $$32 = Atom.of("any_type");
        Atom $$42 = Atom.of("element_type");
        Atom $$5 = Atom.of("tag_type");
        Atom $$6 = Atom.of("conditions");
        Atom $$7 = Atom.of("alternatives");
        Atom $$8 = Atom.of("term");
        Atom $$9 = Atom.of("negation");
        Atom $$10 = Atom.of("test");
        Atom $$11 = Atom.of("component_type");
        Atom $$12 = Atom.of("predicate_type");
        Atom $$13 = Atom.of("id");
        Atom $$14 = Atom.of("tag");
        Dictionary<StringReader> $$15 = new Dictionary<StringReader>();
        NamedRule<StringReader, Identifier> $$16 = $$15.put($$13, IdentifierParseRule.INSTANCE);
        NamedRule $$17 = $$15.put($$1, Term.alternative(Term.sequence($$15.named($$22), StringReaderTerms.character('['), Term.cut(), Term.optional($$15.named($$6)), StringReaderTerms.character(']')), $$15.named($$22)), $$2 -> {
            ImmutableList.Builder $$3 = ImmutableList.builder();
            ((Optional)$$2.getOrThrow($$22)).ifPresent(arg_0 -> ((ImmutableList.Builder)$$3).add(arg_0));
            List $$4 = (List)$$2.get($$6);
            if ($$4 != null) {
                $$3.addAll((Iterable)$$4);
            }
            return $$3.build();
        });
        $$15.put($$22, Term.alternative($$15.named($$42), Term.sequence(StringReaderTerms.character('#'), Term.cut(), $$15.named($$5)), $$15.named($$32)), $$2 -> Optional.ofNullable($$2.getAny($$42, $$5)));
        $$15.put($$32, StringReaderTerms.character('*'), $$0 -> Unit.INSTANCE);
        $$15.put($$42, new ElementLookupRule<T, C, P>($$16, $$02));
        $$15.put($$5, new TagLookupRule<T, C, P>($$16, $$02));
        $$15.put($$6, Term.sequence($$15.named($$7), Term.optional(Term.sequence(StringReaderTerms.character(','), $$15.named($$6)))), $$3 -> {
            Object $$4 = $$02.anyOf((List)$$3.getOrThrow($$7));
            return Optional.ofNullable((List)$$3.get($$6)).map($$1 -> Util.copyAndAdd($$4, $$1)).orElse(List.of($$4));
        });
        $$15.put($$7, Term.sequence($$15.named($$8), Term.optional(Term.sequence(StringReaderTerms.character('|'), $$15.named($$7)))), $$2 -> {
            Object $$3 = $$2.getOrThrow($$8);
            return Optional.ofNullable((List)$$2.get($$7)).map($$1 -> Util.copyAndAdd($$3, $$1)).orElse(List.of($$3));
        });
        $$15.put($$8, Term.alternative($$15.named($$10), Term.sequence(StringReaderTerms.character('!'), $$15.named($$9))), $$2 -> $$2.getAnyOrThrow($$10, $$9));
        $$15.put($$9, $$15.named($$10), $$2 -> $$02.negate($$2.getOrThrow($$10)));
        $$15.putComplex($$10, Term.alternative(Term.sequence($$15.named($$11), StringReaderTerms.character('='), Term.cut(), $$15.named($$14)), Term.sequence($$15.named($$12), StringReaderTerms.character('~'), Term.cut(), $$15.named($$14)), $$15.named($$11)), $$4 -> {
            Scope $$5 = $$4.scope();
            Object $$6 = $$5.get($$12);
            try {
                if ($$6 != null) {
                    Dynamic $$7 = (Dynamic)$$5.getOrThrow($$14);
                    return $$02.createPredicateTest((ImmutableStringReader)$$4.input(), $$6, $$7);
                }
                Object $$8 = $$5.getOrThrow($$11);
                Dynamic $$9 = (Dynamic)$$5.get($$14);
                return $$9 != null ? $$02.createComponentTest((ImmutableStringReader)$$4.input(), $$8, $$9) : $$02.createComponentTest((ImmutableStringReader)$$4.input(), $$8);
            }
            catch (CommandSyntaxException $$10) {
                $$4.errorCollector().store($$4.mark(), (Object)$$10);
                return null;
            }
        });
        $$15.put($$11, new ComponentLookupRule<T, C, P>($$16, $$02));
        $$15.put($$12, new PredicateLookupRule<T, C, P>($$16, $$02));
        $$15.put($$14, new TagParseRule<Tag>(NbtOps.INSTANCE));
        return new Grammar<List<T>>($$15, $$17);
    }

    static class ElementLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        ElementLookupRule(NamedRule<StringReader, Identifier> $$0, Context<T, C, P> $$1) {
            super($$0, $$1);
        }

        @Override
        protected T validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
            return ((Context)this.context).forElementType($$0, $$1);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listElementTypes();
        }
    }

    public static interface Context<T, C, P> {
        public T forElementType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listElementTypes();

        public T forTagType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listTagTypes();

        public C lookupComponentType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listComponentTypes();

        public T createComponentTest(ImmutableStringReader var1, C var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T createComponentTest(ImmutableStringReader var1, C var2);

        public P lookupPredicateType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

        public Stream<Identifier> listPredicateTypes();

        public T createPredicateTest(ImmutableStringReader var1, P var2, Dynamic<?> var3) throws CommandSyntaxException;

        public T negate(T var1);

        public T anyOf(List<T> var1);
    }

    static class TagLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, T> {
        TagLookupRule(NamedRule<StringReader, Identifier> $$0, Context<T, C, P> $$1) {
            super($$0, $$1);
        }

        @Override
        protected T validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
            return ((Context)this.context).forTagType($$0, $$1);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listTagTypes();
        }
    }

    static class ComponentLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, C> {
        ComponentLookupRule(NamedRule<StringReader, Identifier> $$0, Context<T, C, P> $$1) {
            super($$0, $$1);
        }

        @Override
        protected C validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
            return ((Context)this.context).lookupComponentType($$0, $$1);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listComponentTypes();
        }
    }

    static class PredicateLookupRule<T, C, P>
    extends ResourceLookupRule<Context<T, C, P>, P> {
        PredicateLookupRule(NamedRule<StringReader, Identifier> $$0, Context<T, C, P> $$1) {
            super($$0, $$1);
        }

        @Override
        protected P validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
            return ((Context)this.context).lookupPredicateType($$0, $$1);
        }

        @Override
        public Stream<Identifier> possibleResources() {
            return ((Context)this.context).listPredicateTypes();
        }
    }
}

