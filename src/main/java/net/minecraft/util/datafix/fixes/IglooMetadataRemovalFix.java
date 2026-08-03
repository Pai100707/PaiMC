package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;

public class IglooMetadataRemovalFix extends DataFix {
   public IglooMetadataRemovalFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
      return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", $$0, $$0x -> $$0x.update(DSL.remainderFinder(), IglooMetadataRemovalFix::fixTag));
   }

   private static <T> Dynamic<T> fixTag(Dynamic<T> $$0) {
      boolean $$1 = $$0.get("Children").asStreamOpt().map($$0x -> $$0x.allMatch(IglooMetadataRemovalFix::isIglooPiece)).result().orElse(false);
      return $$1 ? $$0.set("id", $$0.createString("Igloo")).remove("Children") : $$0.update("Children", IglooMetadataRemovalFix::removeIglooPieces);
   }

   private static <T> Dynamic<T> removeIglooPieces(Dynamic<T> $$0) {
      return $$0.asStreamOpt().map($$0x -> $$0x.filter($$0xx -> !isIglooPiece($$0xx))).map($$0::createList).result().orElse($$0);
   }

   private static boolean isIglooPiece(Dynamic<?> $$0) {
      return $$0.get("id").asString("").equals("Iglu");
   }
}
