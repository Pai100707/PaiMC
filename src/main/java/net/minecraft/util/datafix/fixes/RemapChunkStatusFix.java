package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemapChunkStatusFix extends DataFix {
   private final String name;
   private final UnaryOperator<String> mapper;

   public RemapChunkStatusFix(Schema $$0, String $$1, UnaryOperator<String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.mapper = $$2;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         this.name,
         this.getInputSchema().getType(References.CHUNK),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.update("Status", this::fixStatus).update("below_zero_retrogen", $$0xx -> $$0xx.update("target_status", this::fixStatus))
         )
      );
   }

   private <T> Dynamic<T> fixStatus(Dynamic<T> $$0) {
      Optional<Dynamic<T>> $$1 = $$0.asString().result().map(NamespacedSchema::ensureNamespaced).map(this.mapper).map($$0::createString);
      return (Dynamic<T>)DataFixUtils.orElse($$1, $$0);
   }
}
