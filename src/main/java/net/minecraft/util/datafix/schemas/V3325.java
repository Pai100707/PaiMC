package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3325 extends NamespacedSchema {
   public V3325(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerEntities($$0);
      $$0.register($$1, "minecraft:item_display", $$1x -> DSL.optionalFields("item", References.ITEM_STACK.in($$0)));
      $$0.register($$1, "minecraft:block_display", $$1x -> DSL.optionalFields("block_state", References.BLOCK_STATE.in($$0)));
      $$0.register($$1, "minecraft:text_display", () -> DSL.optionalFields("text", References.TEXT_COMPONENT.in($$0)));
      return $$1;
   }
}
