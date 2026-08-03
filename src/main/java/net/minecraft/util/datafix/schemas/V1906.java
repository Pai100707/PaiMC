package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1906 extends NamespacedSchema {
   public V1906(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      registerInventory($$0, $$1, "minecraft:barrel");
      registerInventory($$0, $$1, "minecraft:smoker");
      registerInventory($$0, $$1, "minecraft:blast_furnace");
      $$0.register($$1, "minecraft:lectern", $$1x -> DSL.optionalFields("Book", References.ITEM_STACK.in($$0)));
      $$0.registerSimple($$1, "minecraft:bell");
      return $$1;
   }

   protected static void registerInventory(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register($$1, $$2, () -> V1458.nameableInventory($$0));
   }
}
