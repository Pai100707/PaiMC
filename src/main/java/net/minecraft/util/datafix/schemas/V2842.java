package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2842 extends NamespacedSchema {
   public V2842(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         false,
         References.CHUNK,
         () -> DSL.optionalFields(
            "entities",
            DSL.list(References.ENTITY_TREE.in($$0)),
            "block_entities",
            DSL.list(DSL.or(References.BLOCK_ENTITY.in($$0), DSL.remainder())),
            "block_ticks",
            DSL.list(DSL.fields("i", References.BLOCK_NAME.in($$0))),
            "sections",
            DSL.list(
               DSL.optionalFields(
                  "biomes",
                  DSL.optionalFields("palette", DSL.list(References.BIOME.in($$0))),
                  "block_states",
                  DSL.optionalFields("palette", DSL.list(References.BLOCK_STATE.in($$0)))
               )
            ),
            "structures",
            DSL.optionalFields("starts", DSL.compoundList(References.STRUCTURE_FEATURE.in($$0)))
         )
      );
   }
}
