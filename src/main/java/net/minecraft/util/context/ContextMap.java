package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jetbrains.annotations.Contract;

public class ContextMap {
   private final Map<ContextKey<?>, Object> params;

   ContextMap(Map<ContextKey<?>, Object> $$0) {
      this.params = $$0;
   }

   public boolean has(ContextKey<?> $$0) {
      return this.params.containsKey($$0);
   }

   public <T> T getOrThrow(ContextKey<T> $$0) {
      T $$1 = (T)this.params.get($$0);
      if ($$1 == null) {
         throw new NoSuchElementException($$0.name().toString());
      } else {
         return $$1;
      }
   }

   
   public <T> T getOptional(ContextKey<T> $$0) {
      return (T)this.params.get($$0);
   }

   @Contract("_,!null->!null; _,_->_")
   
   public <T> T getOrDefault(ContextKey<T> $$0, T $$1) {
      return (T)this.params.getOrDefault($$0, $$1);
   }

   public static class Builder {
      private final Map<ContextKey<?>, Object> params = new IdentityHashMap<>();

      public <T> ContextMap.Builder withParameter(ContextKey<T> $$0, T $$1) {
         this.params.put($$0, $$1);
         return this;
      }

      public <T> ContextMap.Builder withOptionalParameter(ContextKey<T> $$0, T $$1) {
         if ($$1 == null) {
            this.params.remove($$0);
         } else {
            this.params.put($$0, $$1);
         }

         return this;
      }

      public <T> T getParameter(ContextKey<T> $$0) {
         T $$1 = (T)this.params.get($$0);
         if ($$1 == null) {
            throw new NoSuchElementException($$0.name().toString());
         } else {
            return $$1;
         }
      }

      
      public <T> T getOptionalParameter(ContextKey<T> $$0) {
         return (T)this.params.get($$0);
      }

      public ContextMap create(ContextKeySet $$0) {
         Set<ContextKey<?>> $$1 = Sets.difference(this.params.keySet(), $$0.allowed());
         if (!$$1.isEmpty()) {
            throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + $$1);
         } else {
            Set<ContextKey<?>> $$2 = Sets.difference($$0.required(), this.params.keySet());
            if (!$$2.isEmpty()) {
               throw new IllegalArgumentException("Missing required parameters: " + $$2);
            } else {
               return new ContextMap(this.params);
            }
         }
      }
   }
}
