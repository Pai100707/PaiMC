package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1466 extends NamespacedSchema {
   public V1466(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         false,
         References.CHUNK,
         () -> DSL.fields(
            "Level",
            DSL.optionalFields(
               "Entities",
               DSL.list(References.ENTITY_TREE.in($$0)),
               "TileEntities",
               DSL.list(DSL.or(References.BLOCK_ENTITY.in($$0), DSL.remainder())),
               "TileTicks",
               DSL.list(DSL.fields("i", References.BLOCK_NAME.in($$0))),
               "Sections",
               DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in($$0)))),
               "Structures",
               DSL.optionalFields("Starts", DSL.compoundList(References.STRUCTURE_FEATURE.in($$0)))
            )
         )
      );
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = super.registerBlockEntities($$0);
      $$1.put("DUMMY", DSL::remainder);
      return $$1;
   }
}
