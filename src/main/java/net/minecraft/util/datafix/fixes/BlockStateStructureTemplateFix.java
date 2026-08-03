package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class BlockStateStructureTemplateFix extends DataFix {
   public BlockStateStructureTemplateFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "BlockStateStructureTemplateFix",
         this.getInputSchema().getType(References.BLOCK_STATE),
         $$0 -> $$0.update(DSL.remainderFinder(), BlockStateData::upgradeBlockStateTag)
      );
   }
}
