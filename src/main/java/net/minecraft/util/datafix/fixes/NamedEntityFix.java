package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;

public abstract class NamedEntityFix extends DataFix {
   private final String name;
   protected final String entityName;
   protected final TypeReference type;

   public NamedEntityFix(Schema $$0, boolean $$1, String $$2, TypeReference $$3, String $$4) {
      super($$0, $$1);
      this.name = $$2;
      this.type = $$3;
      this.entityName = $$4;
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<?> $$0 = DSL.namedChoice(this.entityName, this.getInputSchema().getChoiceType(this.type, this.entityName));
      return this.fixTypeEverywhereTyped(
         this.name,
         this.getInputSchema().getType(this.type),
         this.getOutputSchema().getType(this.type),
         $$1 -> $$1.updateTyped($$0, this.getOutputSchema().getChoiceType(this.type, this.entityName), this::fix)
      );
   }

   protected abstract Typed<?> fix(Typed<?> var1);
}
