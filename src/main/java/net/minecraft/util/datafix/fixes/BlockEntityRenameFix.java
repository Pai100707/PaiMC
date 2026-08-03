package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import java.util.function.UnaryOperator;

public class BlockEntityRenameFix extends DataFix {
   private final String name;
   private final UnaryOperator<String> nameChangeLookup;

   private BlockEntityRenameFix(Schema $$0, String $$1, UnaryOperator<String> $$2) {
      super($$0, true);
      this.name = $$1;
      this.nameChangeLookup = $$2;
   }

   public TypeRewriteRule makeRule() {
      TaggedChoiceType<String> $$0 = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
      TaggedChoiceType<String> $$1 = this.getOutputSchema().findChoiceType(References.BLOCK_ENTITY);
      return this.fixTypeEverywhere(this.name, $$0, $$1, $$0x -> $$0xx -> $$0xx.mapFirst(this.nameChangeLookup));
   }

   public static DataFix create(Schema $$0, String $$1, UnaryOperator<String> $$2) {
      return new BlockEntityRenameFix($$0, $$1, $$2);
   }
}
