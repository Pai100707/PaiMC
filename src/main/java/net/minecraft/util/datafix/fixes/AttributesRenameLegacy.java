package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;

public class AttributesRenameLegacy extends DataFix {
   private final String name;
   private final UnaryOperator<String> renames;

   public AttributesRenameLegacy(Schema $$0, String $$1, UnaryOperator<String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.renames = $$2;
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<?> $$1 = $$0.findField("tag");
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(this.name + " (ItemStack)", $$0, $$1x -> $$1x.updateTyped($$1, this::fixItemStackTag)),
         new TypeRewriteRule[]{
            this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity),
            this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)
         }
      );
   }

   private Dynamic<?> fixName(Dynamic<?> $$0) {
      return (Dynamic<?>)DataFixUtils.orElse($$0.asString().result().map(this.renames).map($$0::createString), $$0);
   }

   private Typed<?> fixItemStackTag(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(),
         $$0x -> $$0x.update(
            "AttributeModifiers",
            $$0xx -> (Dynamic)DataFixUtils.orElse(
               $$0xx.asStreamOpt().result().map($$0xxx -> $$0xxx.map($$0xxxx -> $$0xxxx.update("AttributeName", this::fixName))).map($$0xx::createList), $$0xx
            )
         )
      );
   }

   private Typed<?> fixEntity(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(),
         $$0x -> $$0x.update(
            "Attributes",
            $$0xx -> (Dynamic)DataFixUtils.orElse(
               $$0xx.asStreamOpt().result().map($$0xxx -> $$0xxx.map($$0xxxx -> $$0xxxx.update("Name", this::fixName))).map($$0xx::createList), $$0xx
            )
         )
      );
   }
}
