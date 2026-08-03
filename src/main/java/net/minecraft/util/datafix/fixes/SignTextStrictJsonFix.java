package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;

public class SignTextStrictJsonFix extends NamedEntityFix {
   private static final List<String> LINE_FIELDS = List.of("Text1", "Text2", "Text3", "Text4");

   public SignTextStrictJsonFix(Schema $$0) {
      super($$0, false, "SignTextStrictJsonFix", References.BLOCK_ENTITY, "Sign");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      for (String $$1 : LINE_FIELDS) {
         OpticFinder<?> $$2 = $$0.getType().findField($$1);
         OpticFinder<Pair<String, String>> $$3 = DSL.typeFinder(this.getInputSchema().getType(References.TEXT_COMPONENT));
         $$0 = $$0.updateTyped($$2, $$1x -> $$1x.update($$3, $$0xx -> $$0xx.mapSecond(LegacyComponentDataFixUtils::rewriteFromLenient)));
      }

      return $$0;
   }
}
