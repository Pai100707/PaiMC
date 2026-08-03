package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class LegacyDragonFightFix extends DataFix {
   public LegacyDragonFightFix(Schema $$0) {
      super($$0, false);
   }

   private static <T> Dynamic<T> fixDragonFight(Dynamic<T> $$0) {
      return $$0.update("ExitPortalLocation", ExtraDataFixUtils::fixBlockPos);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "LegacyDragonFightFix", this.getInputSchema().getType(References.LEVEL), $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> {
            OptionalDynamic<?> $$1 = $$0x.get("DragonFight");
            if ($$1.result().isPresent()) {
               return $$0x;
            } else {
               Dynamic<?> $$2 = $$0x.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap();
               return $$0x.set("DragonFight", fixDragonFight($$2));
            }
         })
      );
   }
}
