package net.minecraft.util.parsing.packrat;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public class Dictionary<S> {
   private final Map<Atom<?>, Dictionary.Entry<S, ?>> terms = new IdentityHashMap<>();

   public <T> NamedRule<S, T> put(Atom<T> $$0, Rule<S, T> $$1) {
      Dictionary.Entry<S, T> $$2 = (Dictionary.Entry<S, T>)this.terms.computeIfAbsent($$0, Dictionary.Entry::new);
      if ($$2.value != null) {
         throw new IllegalArgumentException("Trying to override rule: " + $$0);
      } else {
         $$2.value = $$1;
         return $$2;
      }
   }

   public <T> NamedRule<S, T> putComplex(Atom<T> $$0, Term<S> $$1, Rule.RuleAction<S, T> $$2) {
      return this.put($$0, Rule.fromTerm($$1, $$2));
   }

   public <T> NamedRule<S, T> put(Atom<T> $$0, Term<S> $$1, Rule.SimpleRuleAction<S, T> $$2) {
      return this.put($$0, Rule.fromTerm($$1, $$2));
   }

   public void checkAllBound() {
      List<? extends Atom<?>> $$0 = this.terms
         .entrySet()
         .stream()
         .filter($$0x -> ((Dictionary.Entry)$$0x.getValue()).value == null)
         .map(Map.Entry::getKey)
         .toList();
      if (!$$0.isEmpty()) {
         throw new IllegalStateException("Unbound names: " + $$0);
      }
   }

   public <T> NamedRule<S, T> getOrThrow(Atom<T> $$0) {
      return (NamedRule<S, T>)Objects.requireNonNull(this.terms.get($$0), () -> "No rule called " + $$0);
   }

   public <T> NamedRule<S, T> forward(Atom<T> $$0) {
      return this.getOrCreateEntry($$0);
   }

   private <T> Dictionary.Entry<S, T> getOrCreateEntry(Atom<T> $$0) {
      return (Dictionary.Entry<S, T>)this.terms.computeIfAbsent($$0, Dictionary.Entry::new);
   }

   public <T> Term<S> named(Atom<T> $$0) {
      return new Dictionary.Reference<>(this.getOrCreateEntry($$0), $$0);
   }

   public <T> Term<S> namedWithAlias(Atom<T> $$0, Atom<T> $$1) {
      return new Dictionary.Reference<>(this.getOrCreateEntry($$0), $$1);
   }

   static class Entry<S, T> implements NamedRule<S, T>, Supplier<String> {
      private final Atom<T> name;
      @Nullable
      Rule<S, T> value;

      private Entry(Atom<T> $$0) {
         this.name = $$0;
      }

      @Override
      public Atom<T> name() {
         return this.name;
      }

      @Override
      public Rule<S, T> value() {
         return Objects.requireNonNull(this.value, this);
      }

      public String get() {
         return "Unbound rule " + this.name;
      }
   }

   record Reference<S, T>(Dictionary.Entry<S, T> ruleToParse, Atom<T> nameToStore) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         T $$3 = $$0.parse(this.ruleToParse);
         if ($$3 == null) {
            return false;
         } else {
            $$1.put(this.nameToStore, $$3);
            return true;
         }
      }
   }
}
