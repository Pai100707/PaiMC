package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class VillagerDataFix extends NamedEntityFix {
   public VillagerDataFix(Schema $$0, String $$1) {
      super($$0, false, "Villager profession data fix (" + $$1 + ")", References.ENTITY, $$1);
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      Dynamic<?> $$1 = (Dynamic<?>)$$0.get(DSL.remainderFinder());
      return $$0.set(
         DSL.remainderFinder(),
         $$1.remove("Profession")
            .remove("Career")
            .remove("CareerLevel")
            .set(
               "VillagerData",
               $$1.createMap(
                  ImmutableMap.of(
                     $$1.createString("type"),
                     $$1.createString("minecraft:plains"),
                     $$1.createString("profession"),
                     $$1.createString(upgradeData($$1.get("Profession").asInt(0), $$1.get("Career").asInt(0))),
                     $$1.createString("level"),
                     (Dynamic)DataFixUtils.orElse($$1.get("CareerLevel").result(), $$1.createInt(1))
                  )
               )
            )
      );
   }

   private static String upgradeData(int $$0, int $$1) {
      if ($$0 == 0) {
         if ($$1 == 2) {
            return "minecraft:fisherman";
         } else if ($$1 == 3) {
            return "minecraft:shepherd";
         } else {
            return $$1 == 4 ? "minecraft:fletcher" : "minecraft:farmer";
         }
      } else if ($$0 == 1) {
         return $$1 == 2 ? "minecraft:cartographer" : "minecraft:librarian";
      } else if ($$0 == 2) {
         return "minecraft:cleric";
      } else if ($$0 == 3) {
         if ($$1 == 2) {
            return "minecraft:weaponsmith";
         } else {
            return $$1 == 3 ? "minecraft:toolsmith" : "minecraft:armorer";
         }
      } else if ($$0 == 4) {
         return $$1 == 2 ? "minecraft:leatherworker" : "minecraft:butcher";
      } else {
         return $$0 == 5 ? "minecraft:nitwit" : "minecraft:none";
      }
   }
}
