package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

public class InvalidLockComponentFix extends DataComponentRemainderFix {
   private static final Optional<String> INVALID_LOCK_CUSTOM_NAME = Optional.of("\"\"");

   public InvalidLockComponentFix(Schema $$0) {
      super($$0, "InvalidLockComponentPredicateFix", "minecraft:lock");
   }

   @Nullable
   @Override
   protected <T> Dynamic<T> fixComponent(Dynamic<T> $$0) {
      return fixLock($$0);
   }

   @Nullable
   public static <T> Dynamic<T> fixLock(Dynamic<T> $$0) {
      return isBrokenLock($$0) ? null : $$0;
   }

   private static <T> boolean isBrokenLock(Dynamic<T> $$0) {
      return isMapWithOneField(
         $$0, "components", $$0x -> isMapWithOneField($$0x, "minecraft:custom_name", $$0xx -> $$0xx.asString().result().equals(INVALID_LOCK_CUSTOM_NAME))
      );
   }

   private static <T> boolean isMapWithOneField(Dynamic<T> $$0, String $$1, Predicate<Dynamic<T>> $$2) {
      Optional<Map<Dynamic<T>, Dynamic<T>>> $$3 = $$0.getMapValues().result();
      return !$$3.isEmpty() && $$3.get().size() == 1 ? $$0.get($$1).result().filter($$2).isPresent() : false;
   }
}
