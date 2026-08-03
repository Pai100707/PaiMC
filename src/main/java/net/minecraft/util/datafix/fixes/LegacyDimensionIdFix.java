package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class LegacyDimensionIdFix extends DataFix {
   public LegacyDimensionIdFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      TypeRewriteRule $$0 = this.fixTypeEverywhereTyped(
         "PlayerLegacyDimensionFix", this.getInputSchema().getType(References.PLAYER), $$0x -> $$0x.update(DSL.remainderFinder(), this::fixPlayer)
      );
      Type<?> $$1 = this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA);
      OpticFinder<?> $$2 = $$1.findField("data");
      TypeRewriteRule $$3 = this.fixTypeEverywhereTyped(
         "MapLegacyDimensionFix", $$1, $$1x -> $$1x.updateTyped($$2, $$0xx -> $$0xx.update(DSL.remainderFinder(), this::fixMap))
      );
      return TypeRewriteRule.seq($$0, $$3);
   }

   private <T> Dynamic<T> fixMap(Dynamic<T> $$0) {
      return $$0.update("dimension", this::fixDimensionId);
   }

   private <T> Dynamic<T> fixPlayer(Dynamic<T> $$0) {
      return $$0.update("Dimension", this::fixDimensionId);
   }

   private <T> Dynamic<T> fixDimensionId(Dynamic<T> $$0) {
      return (Dynamic<T>)DataFixUtils.orElse($$0.asNumber().result().map($$1 -> {
         return switch ($$1.intValue()) {
            case -1 -> $$0.createString("minecraft:the_nether");
            case 1 -> $$0.createString("minecraft:the_end");
            default -> $$0.createString("minecraft:overworld");
         };
      }), $$0);
   }
}
