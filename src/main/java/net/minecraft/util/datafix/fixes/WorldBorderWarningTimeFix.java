package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class WorldBorderWarningTimeFix extends DataFix {
   public WorldBorderWarningTimeFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.writeFixAndRead(
         "WorldBorderWarningTimeFix",
         this.getInputSchema().getType(References.SAVED_DATA_WORLD_BORDER),
         this.getOutputSchema().getType(References.SAVED_DATA_WORLD_BORDER),
         $$0 -> $$0.update("data", $$0x -> $$0x.update("warning_time", $$1 -> $$0x.createInt($$1.asInt(15) * 20)))
      );
   }
}
