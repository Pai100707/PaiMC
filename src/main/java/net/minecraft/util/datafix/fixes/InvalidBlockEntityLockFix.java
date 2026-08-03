package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class InvalidBlockEntityLockFix extends DataFix {
   public InvalidBlockEntityLockFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "BlockEntityLockToComponentFix", this.getInputSchema().getType(References.BLOCK_ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> {
            Optional<? extends Dynamic<?>> $$1 = $$0x.get("lock").result();
            if ($$1.isEmpty()) {
               return $$0x;
            } else {
               Dynamic<?> $$2 = InvalidLockComponentFix.fixLock($$1.get());
               return $$2 != null ? $$0x.set("lock", $$2) : $$0x.remove("lock");
            }
         })
      );
   }
}
