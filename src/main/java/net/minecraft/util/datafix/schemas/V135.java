package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V135 extends Schema {
   public V135(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         false,
         References.PLAYER,
         () -> DSL.optionalFields(
            "RootVehicle",
            DSL.optionalFields("Entity", References.ENTITY_TREE.in($$0)),
            "ender_pearls",
            DSL.list(References.ENTITY_TREE.in($$0)),
            "Inventory",
            DSL.list(References.ITEM_STACK.in($$0)),
            "EnderItems",
            DSL.list(References.ITEM_STACK.in($$0))
         )
      );
      $$0.registerType(
         true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in($$0)), References.ENTITY.in($$0))
      );
   }
}
