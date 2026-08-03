package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerFollowRangeFix extends NamedEntityFix {
   private static final double ORIGINAL_VALUE = 16.0;
   private static final double NEW_BASE_VALUE = 48.0;

   public VillagerFollowRangeFix(Schema $$0) {
      super($$0, false, "Villager Follow Range Fix", References.ENTITY, "minecraft:villager");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), VillagerFollowRangeFix::fixValue);
   }

   private static Dynamic<?> fixValue(Dynamic<?> $$0) {
      return $$0.update(
         "Attributes",
         $$1 -> $$0.createList(
            $$1.asStream()
               .map(
                  $$0xx -> $$0xx.get("Name").asString("").equals("generic.follow_range") && $$0xx.get("Base").asDouble(0.0) == 16.0
                     ? $$0xx.set("Base", $$0xx.createDouble(48.0))
                     : $$0xx
               )
         )
      );
   }
}
