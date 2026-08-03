package net.minecraft.util.datafix.fixes;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public class LockComponentPredicateFix extends DataComponentRemainderFix {
   public static final Escaper ESCAPER = Escapers.builder().addEscape('"', "\\\"").addEscape('\\', "\\\\").build();

   public LockComponentPredicateFix(Schema $$0) {
      super($$0, "LockComponentPredicateFix", "minecraft:lock");
   }

   @Nullable
   @Override
   protected <T> Dynamic<T> fixComponent(Dynamic<T> $$0) {
      return fixLock($$0);
   }

   @Nullable
   public static <T> Dynamic<T> fixLock(Dynamic<T> $$0) {
      Optional<String> $$1 = $$0.asString().result();
      if ($$1.isEmpty()) {
         return null;
      } else if ($$1.get().isEmpty()) {
         return null;
      } else {
         Dynamic<T> $$2 = $$0.createString("\"" + ESCAPER.escape($$1.get()) + "\"");
         Dynamic<T> $$3 = $$0.emptyMap().set("minecraft:custom_name", $$2);
         return $$0.emptyMap().set("components", $$3);
      }
   }
}
