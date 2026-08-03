package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public abstract class DataComponentRemainderFix extends DataFix {
   private final String name;
   private final String componentId;
   private final String newComponentId;

   public DataComponentRemainderFix(Schema $$0, String $$1, String $$2) {
      this($$0, $$1, $$2, $$2);
   }

   public DataComponentRemainderFix(Schema $$0, String $$1, String $$2, String $$3) {
      super($$0, false);
      this.name = $$1;
      this.componentId = $$2;
      this.newComponentId = $$3;
   }

   public final TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.DATA_COMPONENTS);
      return this.fixTypeEverywhereTyped(this.name, $$0, $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> {
         Optional<? extends Dynamic<?>> $$1 = $$0xx.get(this.componentId).result();
         if ($$1.isEmpty()) {
            return $$0xx;
         } else {
            Dynamic<?> $$2 = this.fixComponent($$1.get());
            return $$0xx.remove(this.componentId).setFieldIfPresent(this.newComponentId, Optional.ofNullable($$2));
         }
      }));
   }

   @Nullable
   protected abstract <T> Dynamic<T> fixComponent(Dynamic<T> var1);
}
