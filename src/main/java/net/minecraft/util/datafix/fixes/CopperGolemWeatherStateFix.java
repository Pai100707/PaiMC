package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class CopperGolemWeatherStateFix extends NamedEntityFix {
   public CopperGolemWeatherStateFix(Schema $$0) {
      super($$0, false, "CopperGolemWeatherStateFix", References.ENTITY, "minecraft:copper_golem");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> $$0x.update("weather_state", CopperGolemWeatherStateFix::fixWeatherState));
   }

   private static Dynamic<?> fixWeatherState(Dynamic<?> $$0) {
      return switch ($$0.asInt(0)) {
         case 1 -> $$0.createString("exposed");
         case 2 -> $$0.createString("weathered");
         case 3 -> $$0.createString("oxidized");
         default -> $$0.createString("unaffected");
      };
   }
}
