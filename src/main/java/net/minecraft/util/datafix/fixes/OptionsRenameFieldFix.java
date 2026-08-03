package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;

public class OptionsRenameFieldFix extends DataFix {
   private final String fixName;
   private final String fieldFrom;
   private final String fieldTo;

   public OptionsRenameFieldFix(Schema $$0, boolean $$1, String $$2, String $$3, String $$4) {
      super($$0, $$1);
      this.fixName = $$2;
      this.fieldFrom = $$3;
      this.fieldTo = $$4;
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         this.fixName,
         this.getInputSchema().getType(References.OPTIONS),
         $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> $$0x.renameField(this.fieldFrom, this.fieldTo))
      );
   }
}
