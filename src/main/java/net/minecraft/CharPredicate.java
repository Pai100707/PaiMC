package net.minecraft;

import java.util.Objects;

@FunctionalInterface
public interface CharPredicate {
   boolean test(char var1);

   default CharPredicate and(CharPredicate $$0) {
      Objects.requireNonNull($$0);
      return $$1 -> this.test($$1) && $$0.test($$1);
   }

   default CharPredicate negate() {
      return $$0 -> !this.test($$0);
   }

   default CharPredicate or(CharPredicate $$0) {
      Objects.requireNonNull($$0);
      return $$1 -> this.test($$1) || $$0.test($$1);
   }
}
