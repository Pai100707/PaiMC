package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2501 extends NamespacedSchema {
   public V2501(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   private static void registerFurnace(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register(
         $$1,
         $$2,
         () -> DSL.optionalFields(
            "Items",
            DSL.list(References.ITEM_STACK.in($$0)),
            "CustomName",
            References.TEXT_COMPONENT.in($$0),
            "RecipesUsed",
            DSL.compoundList(References.RECIPE.in($$0), DSL.constType(DSL.intType()))
         )
      );
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      registerFurnace($$0, $$1, "minecraft:furnace");
      registerFurnace($$0, $$1, "minecraft:smoker");
      registerFurnace($$0, $$1, "minecraft:blast_furnace");
      return $$1;
   }
}
