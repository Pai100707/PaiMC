package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2831 extends NamespacedSchema {
   public V2831(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         true,
         References.UNTAGGED_SPAWNER,
         () -> DSL.optionalFields(
            "SpawnPotentials",
            DSL.list(DSL.fields("data", DSL.fields("entity", References.ENTITY_TREE.in($$0)))),
            "SpawnData",
            DSL.fields("entity", References.ENTITY_TREE.in($$0))
         )
      );
   }
}
