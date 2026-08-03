package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import org.slf4j.Logger;

public class SavedDataUUIDFix extends AbstractUUIDFix {
   private static final Logger LOGGER = LogUtils.getLogger();

   public SavedDataUUIDFix(Schema $$0) {
      super($$0, References.SAVED_DATA_RAIDS);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "SavedDataUUIDFix",
         this.getInputSchema().getType(this.typeReference),
         $$0 -> $$0.update(
            DSL.remainderFinder(),
            $$0x -> $$0x.update(
               "data",
               $$0xx -> $$0xx.update(
                  "Raids",
                  $$0xxx -> $$0xxx.createList(
                     $$0xxx.asStream()
                        .map(
                           $$0xxxx -> $$0xxxx.update(
                              "HeroesOfTheVillage",
                              $$0xxxxx -> $$0xxxxx.createList(
                                 $$0xxxxx.asStream().map($$0xxxxxx -> (Dynamic)createUUIDFromLongs($$0xxxxxx, "UUIDMost", "UUIDLeast").orElseGet(() -> {
                                    LOGGER.warn("HeroesOfTheVillage contained invalid UUIDs.");
                                    return $$0xxxxxx;
                                 }))
                              )
                           )
                        )
                  )
               )
            )
         )
      );
   }
}
