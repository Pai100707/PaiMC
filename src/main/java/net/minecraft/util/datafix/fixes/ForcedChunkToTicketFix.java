package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class ForcedChunkToTicketFix extends DataFix {
   public ForcedChunkToTicketFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "ForcedChunkToTicketFix",
         this.getInputSchema().getType(References.SAVED_DATA_TICKETS),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.update(
               "data",
               $$1 -> $$1.renameAndFixField(
                  "Forced",
                  "tickets",
                  $$1x -> $$1x.createList(
                     $$1x.asLongStream()
                        .mapToObj(
                           $$1xx -> $$0x.emptyMap()
                              .set("type", $$0x.createString("minecraft:forced"))
                              .set("level", $$0x.createInt(31))
                              .set("ticks_left", $$0x.createLong(0L))
                              .set("chunk_pos", $$0x.createLong($$1xx))
                        )
                  )
               )
            )
         )
      );
   }
}
