package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class AddFlagIfNotPresentFix extends DataFix {
   private final String name;
   private final boolean flagValue;
   private final String flagKey;
   private final TypeReference typeReference;

   public AddFlagIfNotPresentFix(Schema $$0, TypeReference $$1, String $$2, boolean $$3) {
      super($$0, true);
      this.flagValue = $$3;
      this.flagKey = $$2;
      this.name = "AddFlagIfNotPresentFix_" + this.flagKey + "=" + this.flagValue + " for " + $$0.getVersionKey();
      this.typeReference = $$1;
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(this.typeReference);
      return this.fixTypeEverywhereTyped(
         this.name,
         $$0,
         $$0x -> $$0x.update(
            DSL.remainderFinder(),
            $$0xx -> $$0xx.set(this.flagKey, (Dynamic)DataFixUtils.orElseGet($$0xx.get(this.flagKey).result(), () -> $$0xx.createBoolean(this.flagValue)))
         )
      );
   }
}
