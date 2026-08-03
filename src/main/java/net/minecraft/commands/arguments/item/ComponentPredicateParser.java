package net.minecraft.commands.arguments.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
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
   public static <T, C, P> Grammar<List<T>> createGrammar(ComponentPredicateParser.Context<T, C, P> $$0) {
      Atom<List<T>> $$1 = Atom.of("top");
      Atom<Optional<T>> $$2 = Atom.of("type");
      Atom<Unit> $$3 = Atom.of("any_type");
      Atom<T> $$4 = Atom.of("element_type");
      Atom<T> $$5 = Atom.of("tag_type");
      Atom<List<T>> $$6 = Atom.of("conditions");
      Atom<List<T>> $$7 = Atom.of("alternatives");
      Atom<T> $$8 = Atom.of("term");
      Atom<T> $$9 = Atom.of("negation");
      Atom<T> $$10 = Atom.of("test");
      Atom<C> $$11 = Atom.of("component_type");
      Atom<P> $$12 = Atom.of("predicate_type");
      Atom<Identifier> $$13 = Atom.of("id");
      Atom<Dynamic<?>> $$14 = Atom.of("tag");
      Dictionary<StringReader> $$15 = new Dictionary();
      NamedRule<StringReader, Identifier> $$16 = $$15.put($$13, IdentifierParseRule.INSTANCE);
      NamedRule<StringReader, List<T>> $$17 = $$15.put(
         $$1,
         Term.alternative(
            new Term[]{
               Term.sequence(
                  new Term[]{$$15.named($$2), StringReaderTerms.character('['), Term.cut(), Term.optional($$15.named($$6)), StringReaderTerms.character(']')}
               ),
               $$15.named($$2)
            }
         ),
         $$2x -> {
            Builder<T> $$3x = ImmutableList.builder();
            ((Optional)$$2x.getOrThrow($$2)).ifPresent($$3x::add);
            List<T> $$4x = (List<T>)$$2x.get($$6);
            if ($$4x != null) {
               $$3x.addAll($$4x);
            }

            return $$3x.build();
         }
      );
      $$15.put(
         $$2,
         Term.alternative(
            new Term[]{$$15.named($$4), Term.sequence(new Term[]{StringReaderTerms.character('#'), Term.cut(), $$15.named($$5)}), $$15.named($$3)}
         ),
         $$2x -> Optional.ofNullable($$2x.getAny(new Atom[]{$$4, $$5}))
      );
      $$15.put($$3, StringReaderTerms.character('*'), $$0x -> Unit.INSTANCE);
      $$15.put($$4, new ComponentPredicateParser.ElementLookupRule<T, C, P>($$16, $$0));
      $$15.put($$5, new ComponentPredicateParser.TagLookupRule<T, C, P>($$16, $$0));
      $$15.put(
         $$6,
         Term.sequence(new Term[]{$$15.named($$7), Term.optional(Term.sequence(new Term[]{StringReaderTerms.character(','), $$15.named($$6)}))}),
         $$3x -> {
            T $$4x = $$0.anyOf((List<T>)$$3x.getOrThrow($$7));
            return Optional.ofNullable((List)$$3x.get($$6)).map($$1xx -> Util.copyAndAdd($$4x, $$1xx)).orElse(List.of($$4x));
         }
      );
      $$15.put(
         $$7,
         Term.sequence(new Term[]{$$15.named($$8), Term.optional(Term.sequence(new Term[]{StringReaderTerms.character('|'), $$15.named($$7)}))}),
         $$2x -> {
            T $$3x = (T)$$2x.getOrThrow($$8);
            return Optional.ofNullable((List)$$2x.get($$7)).map($$1xx -> Util.copyAndAdd($$3x, $$1xx)).orElse(List.of($$3x));
         }
      );
      $$15.put(
         $$8,
         Term.alternative(new Term[]{$$15.named($$10), Term.sequence(new Term[]{StringReaderTerms.character('!'), $$15.named($$9)})}),
         $$2x -> $$2x.getAnyOrThrow(new Atom[]{$$10, $$9})
      );
      $$15.put($$9, $$15.named($$10), $$2x -> $$0.negate((T)$$2x.getOrThrow($$10)));
      $$15.putComplex(
         $$10,
         Term.alternative(
            new Term[]{
               Term.sequence(new Term[]{$$15.named($$11), StringReaderTerms.character('='), Term.cut(), $$15.named($$14)}),
               Term.sequence(new Term[]{$$15.named($$12), StringReaderTerms.character('~'), Term.cut(), $$15.named($$14)}),
               $$15.named($$11)
            }
         ),
         $$4x -> {
            Scope $$5x = $$4x.scope();
            P $$6x = (P)$$5x.get($$12);

            try {
               if ($$6x != null) {
                  Dynamic<?> $$7x = (Dynamic<?>)$$5x.getOrThrow($$14);
                  return $$0.createPredicateTest((ImmutableStringReader)$$4x.input(), $$6x, $$7x);
               } else {
                  C $$8x = (C)$$5x.getOrThrow($$11);
                  Dynamic<?> $$9x = (Dynamic<?>)$$5x.get($$14);
                  return $$9x != null
                     ? $$0.createComponentTest((ImmutableStringReader)$$4x.input(), $$8x, $$9x)
                     : $$0.createComponentTest((ImmutableStringReader)$$4x.input(), $$8x);
               }
            } catch (CommandSyntaxException var9x) {
               $$4x.errorCollector().store($$4x.mark(), var9x);
               return null;
            }
         }
      );
      $$15.put($$11, new ComponentPredicateParser.ComponentLookupRule<T, C, P>($$16, $$0));
      $$15.put($$12, new ComponentPredicateParser.PredicateLookupRule<T, C, P>($$16, $$0));
      $$15.put($$14, new TagParseRule(NbtOps.INSTANCE));
      return new Grammar($$15, $$17);
   }

   static class ComponentLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, C> {
      ComponentLookupRule(NamedRule<StringReader, Identifier> $$0, ComponentPredicateParser.Context<T, C, P> $$1) {
         super($$0, $$1);
      }

      protected C validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
         return (C)((ComponentPredicateParser.Context)this.context).lookupComponentType($$0, $$1);
      }

      public Stream<Identifier> possibleResources() {
         return ((ComponentPredicateParser.Context)this.context).listComponentTypes();
      }
   }

   public interface Context<T, C, P> {
      T forElementType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

      Stream<Identifier> listElementTypes();

      T forTagType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

      Stream<Identifier> listTagTypes();

      C lookupComponentType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

      Stream<Identifier> listComponentTypes();

      T createComponentTest(ImmutableStringReader var1, C var2, Dynamic<?> var3) throws CommandSyntaxException;

      T createComponentTest(ImmutableStringReader var1, C var2);

      P lookupPredicateType(ImmutableStringReader var1, Identifier var2) throws CommandSyntaxException;

      Stream<Identifier> listPredicateTypes();

      T createPredicateTest(ImmutableStringReader var1, P var2, Dynamic<?> var3) throws CommandSyntaxException;

      T negate(T var1);

      T anyOf(List<T> var1);
   }

   static class ElementLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, T> {
      ElementLookupRule(NamedRule<StringReader, Identifier> $$0, ComponentPredicateParser.Context<T, C, P> $$1) {
         super($$0, $$1);
      }

      protected T validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
         return (T)((ComponentPredicateParser.Context)this.context).forElementType($$0, $$1);
      }

      public Stream<Identifier> possibleResources() {
         return ((ComponentPredicateParser.Context)this.context).listElementTypes();
      }
   }

   static class PredicateLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, P> {
      PredicateLookupRule(NamedRule<StringReader, Identifier> $$0, ComponentPredicateParser.Context<T, C, P> $$1) {
         super($$0, $$1);
      }

      protected P validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
         return (P)((ComponentPredicateParser.Context)this.context).lookupPredicateType($$0, $$1);
      }

      public Stream<Identifier> possibleResources() {
         return ((ComponentPredicateParser.Context)this.context).listPredicateTypes();
      }
   }

   static class TagLookupRule<T, C, P> extends ResourceLookupRule<ComponentPredicateParser.Context<T, C, P>, T> {
      TagLookupRule(NamedRule<StringReader, Identifier> $$0, ComponentPredicateParser.Context<T, C, P> $$1) {
         super($$0, $$1);
      }

      protected T validateElement(ImmutableStringReader $$0, Identifier $$1) throws Exception {
         return (T)((ComponentPredicateParser.Context)this.context).forTagType($$0, $$1);
      }

      public Stream<Identifier> possibleResources() {
         return ((ComponentPredicateParser.Context)this.context).listTagTypes();
      }
   }
}
