package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class MapBannerBlockPosFormatFix extends DataFix {
   public MapBannerBlockPosFormatFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA);
      OpticFinder<?> $$1 = $$0.findField("data");
      OpticFinder<?> $$2 = $$1.type().findField("banners");
      OpticFinder<?> $$3 = DSL.typeFinder(((ListType)$$2.type()).getElement());
      return this.fixTypeEverywhereTyped(
         "MapBannerBlockPosFormatFix",
         $$0,
         $$3x -> $$3x.updateTyped(
            $$1,
            $$2xx -> $$2xx.updateTyped(
               $$2,
               $$1xxx -> $$1xxx.updateTyped(
                  $$3, $$0xxxx -> $$0xxxx.update(DSL.remainderFinder(), $$0xxxxx -> $$0xxxxx.update("Pos", ExtraDataFixUtils::fixBlockPos))
               )
            )
         )
      );
   }
}
