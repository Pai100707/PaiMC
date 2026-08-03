package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class OptionsProgrammerArtFix extends DataFix {
   public OptionsProgrammerArtFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "OptionsProgrammerArtFix",
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.update("resourcePacks", this::fixList).update("incompatibleResourcePacks", this::fixList))
      );
   }

   private <T> Dynamic<T> fixList(Dynamic<T> $$0) {
      return $$0.asString().result().map($$1 -> $$0.createString($$1.replace("\"programer_art\"", "\"programmer_art\""))).orElse($$0);
   }
}
