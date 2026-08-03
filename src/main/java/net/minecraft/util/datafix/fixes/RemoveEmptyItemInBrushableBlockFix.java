package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemoveEmptyItemInBrushableBlockFix extends NamedEntityWriteReadFix {
   public RemoveEmptyItemInBrushableBlockFix(Schema $$0) {
      super($$0, false, "RemoveEmptyItemInSuspiciousBlockFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
   }

   @Override
   protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
      Optional<Dynamic<T>> $$1 = $$0.get("item").result();
      return $$1.isPresent() && isEmptyStack($$1.get()) ? $$0.remove("item") : $$0;
   }

   private static boolean isEmptyStack(Dynamic<?> $$0) {
      String $$1 = NamespacedSchema.ensureNamespaced($$0.get("id").asString("minecraft:air"));
      int $$2 = $$0.get("count").asInt(0);
      return $$1.equals("minecraft:air") || $$2 == 0;
   }
}
