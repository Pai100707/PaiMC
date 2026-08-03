package net.minecraft.util.parsing.packrat;

import java.util.Optional;
import org.jspecify.annotations.Nullable;

public interface ParseState<S> {
   Scope scope();

   ErrorCollector<S> errorCollector();

   default <T> Optional<T> parseTopRule(NamedRule<S, T> $$0) {
      T $$1 = this.parse($$0);
      if ($$1 != null) {
         this.errorCollector().finish(this.mark());
      }

      if (!this.scope().hasOnlySingleFrame()) {
         throw new IllegalStateException("Malformed scope: " + this.scope());
      } else {
         return Optional.ofNullable($$1);
      }
   }

   @Nullable
   <T> T parse(NamedRule<S, T> var1);

   S input();

   int mark();

   void restore(int var1);

   Control acquireControl();

   void releaseControl();

   ParseState<S> silent();
}
