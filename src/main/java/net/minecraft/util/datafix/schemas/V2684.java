package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2684 extends NamespacedSchema {
   public V2684(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(false, References.GAME_EVENT_NAME, () -> DSL.constType(namespacedString()));
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      $$0.register(
         $$1,
         "minecraft:sculk_sensor",
         () -> DSL.optionalFields("listener", DSL.optionalFields("event", DSL.optionalFields("game_event", References.GAME_EVENT_NAME.in($$0))))
      );
      return $$1;
   }
}
