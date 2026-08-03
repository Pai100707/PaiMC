package net.minecraft.util.parsing.packrat;

import org.jspecify.annotations.Nullable;

public interface Rule<S, T> {
   @Nullable
   T parse(ParseState<S> var1);

   static <S, T> Rule<S, T> fromTerm(Term<S> $$0, Rule.RuleAction<S, T> $$1) {
      return new Rule.WrappedTerm<>($$1, $$0);
   }

   static <S, T> Rule<S, T> fromTerm(Term<S> $$0, Rule.SimpleRuleAction<S, T> $$1) {
      return new Rule.WrappedTerm<>($$1, $$0);
   }

   @FunctionalInterface
   public interface RuleAction<S, T> {
      @Nullable
      T run(ParseState<S> var1);
   }

   @FunctionalInterface
   public interface SimpleRuleAction<S, T> extends Rule.RuleAction<S, T> {
      T run(Scope var1);

      @Override
      default T run(ParseState<S> $$0) {
         return this.run($$0.scope());
      }
   }

   public record WrappedTerm<S, T>(Rule.RuleAction<S, T> action, Term<S> child) implements Rule<S, T> {
      @Nullable
      @Override
      public T parse(ParseState<S> $$0) {
         Scope $$1 = $$0.scope();
         $$1.pushFrame();

         Object var3;
         try {
            if (!this.child.parse($$0, $$1, Control.UNBOUND)) {
               return null;
            }

            var3 = this.action.run($$0);
         } finally {
            $$1.popFrame();
         }

         return (T)var3;
      }
   }
}
