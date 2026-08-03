package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class RandomSequenceSettingsFix extends DataFix {
   public RandomSequenceSettingsFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "RandomSequenceSettingsFix",
         this.getInputSchema().getType(References.SAVED_DATA_RANDOM_SEQUENCES),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.update("data", $$0xx -> $$0xx.emptyMap().set("sequences", $$0xx)))
      );
   }
}
