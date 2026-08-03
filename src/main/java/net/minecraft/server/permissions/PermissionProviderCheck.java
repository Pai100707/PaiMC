package net.minecraft.server.permissions;

import java.util.function.Predicate;

public record PermissionProviderCheck<T extends PermissionSetSupplier>(PermissionCheck test) implements Predicate<T> {
   public boolean test(T $$0) {
      return this.test.check($$0.permissions());
   }
}
