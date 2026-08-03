package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3078 extends NamespacedSchema {
   public V3078(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   protected static void registerMob(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.registerSimple($$1, $$2);
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerEntities($$0);
      registerMob($$0, $$1, "minecraft:frog");
      registerMob($$0, $$1, "minecraft:tadpole");
      return $$1;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      $$0.register(
         $$1,
         "minecraft:sculk_shrieker",
         () -> DSL.optionalFields("listener", DSL.optionalFields("event", DSL.optionalFields("game_event", References.GAME_EVENT_NAME.in($$0))))
      );
      return $$1;
   }
}
