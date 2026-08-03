package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;

public interface Term<S> {
   boolean parse(ParseState<S> var1, Scope var2, Control var3);

   static <S, T> Term<S> marker(Atom<T> $$0, T $$1) {
      return new Term.Marker<>($$0, $$1);
   }

   @SafeVarargs
   static <S> Term<S> sequence(Term<S>... $$0) {
      return new Term.Sequence<>($$0);
   }

   @SafeVarargs
   static <S> Term<S> alternative(Term<S>... $$0) {
      return new Term.Alternative<>($$0);
   }

   static <S> Term<S> optional(Term<S> $$0) {
      return new Term.Maybe<>($$0);
   }

   static <S, T> Term<S> repeated(NamedRule<S, T> $$0, Atom<List<T>> $$1) {
      return repeated($$0, $$1, 0);
   }

   static <S, T> Term<S> repeated(NamedRule<S, T> $$0, Atom<List<T>> $$1, int $$2) {
      return new Term.Repeated<>($$0, $$1, $$2);
   }

   static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2) {
      return repeatedWithTrailingSeparator($$0, $$1, $$2, 0);
   }

   static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2, int $$3) {
      return new Term.RepeatedWithSeparator<>($$0, $$1, $$2, $$3, true);
   }

   static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2) {
      return repeatedWithoutTrailingSeparator($$0, $$1, $$2, 0);
   }

   static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> $$0, Atom<List<T>> $$1, Term<S> $$2, int $$3) {
      return new Term.RepeatedWithSeparator<>($$0, $$1, $$2, $$3, false);
   }

   static <S> Term<S> positiveLookahead(Term<S> $$0) {
      return new Term.LookAhead<>($$0, true);
   }

   static <S> Term<S> negativeLookahead(Term<S> $$0) {
      return new Term.LookAhead<>($$0, false);
   }

   static <S> Term<S> cut() {
      return new Term<S>() {
         @Override
         public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            $$2.cut();
            return true;
         }

         @Override
         public String toString() {
            return "↑";
         }
      };
   }

   static <S> Term<S> empty() {
      return new Term<S>() {
         @Override
         public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
            return true;
         }

         @Override
         public String toString() {
            return "ε";
         }
      };
   }

   static <S> Term<S> fail(final Object $$0) {
      return new Term<S>() {
         @Override
         public boolean parse(ParseState<S> $$0x, Scope $$1, Control $$2) {
            $$0.errorCollector().store($$0.mark(), $$0);
            return false;
         }

         @Override
         public String toString() {
            return "fail";
         }
      };
   }

   public record Alternative<S>(Term<S>[] elements) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         Control $$3 = $$0.acquireControl();

         try {
            int $$4 = $$0.mark();
            $$1.splitFrame();

            for (Term<S> $$5 : this.elements) {
               if ($$5.parse($$0, $$1, $$3)) {
                  $$1.mergeFrame();
                  return true;
               }

               $$1.clearFrameValues();
               $$0.restore($$4);
               if ($$3.hasCut()) {
                  break;
               }
            }

            $$1.popFrame();
            return false;
         } finally {
            $$0.releaseControl();
         }
      }
   }

   public record LookAhead<S>(Term<S> term, boolean positive) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         int $$3 = $$0.mark();
         boolean $$4 = this.term.parse($$0.silent(), $$1, $$2);
         $$0.restore($$3);
         return this.positive == $$4;
      }
   }

   public record Marker<S, T>(Atom<T> name, T value) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         $$1.put(this.name, this.value);
         return true;
      }
   }

   public record Maybe<S>(Term<S> term) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         int $$3 = $$0.mark();
         if (!this.term.parse($$0, $$1, $$2)) {
            $$0.restore($$3);
         }

         return true;
      }
   }

   public record Repeated<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, int minRepetitions) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         int $$3 = $$0.mark();
         List<T> $$4 = new ArrayList<>(this.minRepetitions);

         while (true) {
            int $$5 = $$0.mark();
            T $$6 = $$0.parse(this.element);
            if ($$6 == null) {
               $$0.restore($$5);
               if ($$4.size() < this.minRepetitions) {
                  $$0.restore($$3);
                  return false;
               } else {
                  $$1.put(this.listName, $$4);
                  return true;
               }
            }

            $$4.add($$6);
         }
      }
   }

   public record RepeatedWithSeparator<S, T>(
      NamedRule<S, T> element, Atom<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator
   ) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         int $$3 = $$0.mark();
         List<T> $$4 = new ArrayList<>(this.minRepetitions);
         boolean $$5 = true;

         while (true) {
            int $$6 = $$0.mark();
            if (!$$5 && !this.separator.parse($$0, $$1, $$2)) {
               $$0.restore($$6);
               break;
            }

            int $$7 = $$0.mark();
            T $$8 = $$0.parse(this.element);
            if ($$8 == null) {
               if ($$5) {
                  $$0.restore($$7);
               } else {
                  if (!this.allowTrailingSeparator) {
                     $$0.restore($$3);
                     return false;
                  }

                  $$0.restore($$7);
               }
               break;
            }

            $$4.add($$8);
            $$5 = false;
         }

         if ($$4.size() < this.minRepetitions) {
            $$0.restore($$3);
            return false;
         } else {
            $$1.put(this.listName, $$4);
            return true;
         }
      }
   }

   public record Sequence<S>(Term<S>[] elements) implements Term<S> {
      @Override
      public boolean parse(ParseState<S> $$0, Scope $$1, Control $$2) {
         int $$3 = $$0.mark();

         for (Term<S> $$4 : this.elements) {
            if (!$$4.parse($$0, $$1, $$2)) {
               $$0.restore($$3);
               return false;
            }
         }

         return true;
      }
   }
}
