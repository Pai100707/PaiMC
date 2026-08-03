package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1470 extends NamespacedSchema {
   public V1470(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   protected static void registerMob(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.registerSimple($$1, $$2);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerEntities($$0);
      registerMob($$0, $$1, "minecraft:turtle");
      registerMob($$0, $$1, "minecraft:cod_mob");
      registerMob($$0, $$1, "minecraft:tropical_fish");
      registerMob($$0, $$1, "minecraft:salmon_mob");
      registerMob($$0, $$1, "minecraft:puffer_fish");
      registerMob($$0, $$1, "minecraft:phantom");
      registerMob($$0, $$1, "minecraft:dolphin");
      registerMob($$0, $$1, "minecraft:drowned");
      $$0.register(
         $$1, "minecraft:trident", $$1x -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in($$0), "Trident", References.ITEM_STACK.in($$0))
      );
      return $$1;
   }
}
