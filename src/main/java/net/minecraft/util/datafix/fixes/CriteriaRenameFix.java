package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class CriteriaRenameFix extends DataFix {
   private final String name;
   private final String advancementId;
   private final UnaryOperator<String> conversions;

   public CriteriaRenameFix(Schema $$0, String $$1, String $$2, UnaryOperator<String> $$3) {
      super($$0, false);
      this.name = $$1;
      this.advancementId = $$2;
      this.conversions = $$3;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         this.name, this.getInputSchema().getType(References.ADVANCEMENTS), $$0 -> $$0.update(DSL.remainderFinder(), this::fixAdvancements)
      );
   }

   private Dynamic<?> fixAdvancements(Dynamic<?> $$0) {
      return $$0.update(
         this.advancementId,
         $$0x -> $$0x.update(
            "criteria",
            $$0xx -> $$0xx.updateMapValues(
               $$0xxx -> $$0xxx.mapFirst(
                  $$0xxxx -> (Dynamic)DataFixUtils.orElse($$0xxxx.asString().map($$1 -> $$0xxxx.createString(this.conversions.apply($$1))).result(), $$0xxxx)
               )
            )
         )
      );
   }
}
