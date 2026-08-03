package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsGraphicsModeSplitFix extends DataFix {
   private final String newFieldName;
   private final String valueIfFast;
   private final String valueIfFancy;
   private final String valueIfFabulous;

   public OptionsGraphicsModeSplitFix(Schema $$0, String $$1, String $$2, String $$3, String $$4) {
      super($$0, true);
      this.newFieldName = $$1;
      this.valueIfFast = $$2;
      this.valueIfFancy = $$3;
      this.valueIfFabulous = $$4;
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "graphicsMode split to " + this.newFieldName,
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> (Dynamic)DataFixUtils.orElseGet(
               $$0x.get("graphicsMode").asString().map($$1 -> $$0x.set(this.newFieldName, $$0x.createString(this.getValue($$1)))).result(),
               () -> $$0x.set(this.newFieldName, $$0x.createString(this.valueIfFancy))
            )
         )
      );
   }

   private String getValue(String $$0) {
      return switch ($$0) {
         case "2" -> this.valueIfFabulous;
         case "0" -> this.valueIfFast;
         default -> this.valueIfFancy;
      };
   }
}
