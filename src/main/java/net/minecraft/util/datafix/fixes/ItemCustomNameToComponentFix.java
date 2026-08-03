package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class ItemCustomNameToComponentFix extends DataFix {
   public ItemCustomNameToComponentFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      Type<Pair<String, String>> $$1 = this.getInputSchema().getType(References.TEXT_COMPONENT);
      OpticFinder<?> $$2 = $$0.findField("tag");
      OpticFinder<?> $$3 = $$2.type().findField("display");
      OpticFinder<?> $$4 = $$3.type().findField("Name");
      OpticFinder<Pair<String, String>> $$5 = DSL.typeFinder($$1);
      return this.fixTypeEverywhereTyped(
         "ItemCustomNameToComponentFix",
         $$0,
         $$4x -> $$4x.updateTyped(
            $$2,
            $$3xx -> $$3xx.updateTyped(
               $$3,
               $$2xxx -> $$2xxx.updateTyped(
                  $$4, $$1xxxx -> $$1xxxx.update($$5, $$0xxxxx -> $$0xxxxx.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson))
               )
            )
         )
      );
   }
}
