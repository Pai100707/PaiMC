package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1125 extends NamespacedSchema {
   public V1125(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      $$0.registerSimple($$1, "minecraft:bed");
      return $$1;
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         false,
         References.ADVANCEMENTS,
         () -> DSL.optionalFields(
            "minecraft:adventure/adventuring_time",
            DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:adventure/kill_a_mob",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:adventure/kill_all_mobs",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:husbandry/bred_all_animals",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string())))
         )
      );
      $$0.registerType(false, References.BIOME, () -> DSL.constType(namespacedString()));
      $$0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(namespacedString()));
   }
}
