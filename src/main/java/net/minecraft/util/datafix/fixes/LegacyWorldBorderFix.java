package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class LegacyWorldBorderFix extends DataFix {
   public LegacyWorldBorderFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "LegacyWorldBorderFix",
         this.getInputSchema().getType(References.LEVEL),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> {
               Dynamic<?> $$1 = $$0x.emptyMap()
                  .set("center_x", $$0x.createDouble($$0x.get("BorderCenterX").asDouble(0.0)))
                  .set("center_z", $$0x.createDouble($$0x.get("BorderCenterZ").asDouble(0.0)))
                  .set("size", $$0x.createDouble($$0x.get("BorderSize").asDouble(5.999997E7F)))
                  .set("lerp_time", $$0x.createLong($$0x.get("BorderSizeLerpTime").asLong(0L)))
                  .set("lerp_target", $$0x.createDouble($$0x.get("BorderSizeLerpTarget").asDouble(0.0)))
                  .set("safe_zone", $$0x.createDouble($$0x.get("BorderSafeZone").asDouble(5.0)))
                  .set("damage_per_block", $$0x.createDouble($$0x.get("BorderDamagePerBlock").asDouble(0.2)))
                  .set("warning_blocks", $$0x.createInt($$0x.get("BorderWarningBlocks").asInt(5)))
                  .set("warning_time", $$0x.createInt($$0x.get("BorderWarningTime").asInt(15)));
               $$0x = $$0x.remove("BorderCenterX")
                  .remove("BorderCenterZ")
                  .remove("BorderSize")
                  .remove("BorderSizeLerpTime")
                  .remove("BorderSizeLerpTarget")
                  .remove("BorderSafeZone")
                  .remove("BorderDamagePerBlock")
                  .remove("BorderWarningBlocks")
                  .remove("BorderWarningTime");
               return $$0x.set("world_border", $$1);
            }
         )
      );
   }
}
