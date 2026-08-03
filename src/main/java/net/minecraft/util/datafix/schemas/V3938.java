package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3938 extends NamespacedSchema {
   public V3938(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   protected static TypeTemplate abstractArrow(Schema $$0) {
      return DSL.optionalFields("inBlockState", References.BLOCK_STATE.in($$0), "item", References.ITEM_STACK.in($$0), "weapon", References.ITEM_STACK.in($$0));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerEntities($$0);
      $$0.register($$1, "minecraft:spectral_arrow", () -> abstractArrow($$0));
      $$0.register($$1, "minecraft:arrow", () -> abstractArrow($$0));
      return $$1;
   }
}
