package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class ScoreboardDisplayNameFix extends DataFix {
   private final String name;
   private final TypeReference type;

   public ScoreboardDisplayNameFix(Schema $$0, String $$1, TypeReference $$2) {
      super($$0, false);
      this.name = $$1;
      this.type = $$2;
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(this.type);
      OpticFinder<?> $$1 = $$0.findField("DisplayName");
      OpticFinder<Pair<String, String>> $$2 = DSL.typeFinder(this.getInputSchema().getType(References.TEXT_COMPONENT));
      return this.fixTypeEverywhereTyped(
         this.name,
         $$0,
         $$2x -> $$2x.updateTyped($$1, $$1xx -> $$1xx.update($$2, $$0xxx -> $$0xxx.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson)))
      );
   }
}
