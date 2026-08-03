package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class PlayerRespawnDataFix extends DataFix {
   public PlayerRespawnDataFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "PlayerRespawnDataFix",
         this.getInputSchema().getType(References.PLAYER),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.update(
               "respawn",
               $$0xx -> $$0xx.set("dimension", $$0xx.createString($$0xx.get("dimension").asString("minecraft:overworld")))
                  .set("yaw", $$0xx.createFloat($$0xx.get("angle").asFloat(0.0F)))
                  .set("pitch", $$0xx.createFloat(0.0F))
                  .remove("angle")
            )
         )
      );
   }
}
