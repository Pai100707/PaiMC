package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V4302 extends NamespacedSchema {
   public V4302(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      $$0.registerSimple($$1, "minecraft:test_block");
      $$0.register(
         $$1,
         "minecraft:test_instance_block",
         () -> DSL.optionalFields(
            "data",
            DSL.optionalFields("error_message", References.TEXT_COMPONENT.in($$0)),
            "errors",
            DSL.list(DSL.optionalFields("text", References.TEXT_COMPONENT.in($$0)))
         )
      );
      return $$1;
   }
}
