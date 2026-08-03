package net.minecraft.util.context;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Set;

public class ContextKeySet {
   private final Set<ContextKey<?>> required;
   private final Set<ContextKey<?>> allowed;

   ContextKeySet(Set<ContextKey<?>> $$0, Set<ContextKey<?>> $$1) {
      this.required = Set.copyOf($$0);
      this.allowed = Set.copyOf(Sets.union($$0, $$1));
   }

   public Set<ContextKey<?>> required() {
      return this.required;
   }

   public Set<ContextKey<?>> allowed() {
      return this.allowed;
   }

   @Override
   public String toString() {
      return "[" + Joiner.on(", ").join(this.allowed.stream().map($$0 -> (this.required.contains($$0) ? "!" : "") + $$0.name()).iterator()) + "]";
   }

   public static class Builder {
      private final Set<ContextKey<?>> required = Sets.newIdentityHashSet();
      private final Set<ContextKey<?>> optional = Sets.newIdentityHashSet();

      public ContextKeySet.Builder required(ContextKey<?> $$0) {
         if (this.optional.contains($$0)) {
            throw new IllegalArgumentException("Parameter " + $$0.name() + " is already optional");
         } else {
            this.required.add($$0);
            return this;
         }
      }

      public ContextKeySet.Builder optional(ContextKey<?> $$0) {
         if (this.required.contains($$0)) {
            throw new IllegalArgumentException("Parameter " + $$0.name() + " is already required");
         } else {
            this.optional.add($$0);
            return this;
         }
      }

      public ContextKeySet build() {
         return new ContextKeySet(this.required, this.optional);
      }
   }
}
