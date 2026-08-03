package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class AttributesRenameFix extends DataFix {
   private final String name;
   private final UnaryOperator<String> renames;

   public AttributesRenameFix(Schema $$0, String $$1, UnaryOperator<String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.renames = $$2;
   }

   protected TypeRewriteRule makeRule() {
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(this.name + " (Components)", this.getInputSchema().getType(References.DATA_COMPONENTS), this::fixDataComponents),
         new TypeRewriteRule[]{
            this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity),
            this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)
         }
      );
   }

   private Typed<?> fixDataComponents(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(),
         $$0x -> $$0x.update(
            "minecraft:attribute_modifiers",
            $$0xx -> $$0xx.update(
               "modifiers",
               $$0xxx -> (Dynamic)DataFixUtils.orElse(
                  $$0xxx.asStreamOpt().result().map($$0xxxx -> $$0xxxx.map(this::fixTypeField)).map($$0xxx::createList), $$0xxx
               )
            )
         )
      );
   }

   private Typed<?> fixEntity(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(),
         $$0x -> $$0x.update(
            "attributes",
            $$0xx -> (Dynamic)DataFixUtils.orElse($$0xx.asStreamOpt().result().map($$0xxx -> $$0xxx.map(this::fixIdField)).map($$0xx::createList), $$0xx)
         )
      );
   }

   private Dynamic<?> fixIdField(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixStringField($$0, "id", this.renames);
   }

   private Dynamic<?> fixTypeField(Dynamic<?> $$0) {
      return ExtraDataFixUtils.fixStringField($$0, "type", this.renames);
   }
}
