package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3818_3 extends NamespacedSchema {
   public V3818_3(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema $$0) {
      SequencedMap<String, Supplier<TypeTemplate>> $$1 = new LinkedHashMap<>();
      $$1.put("minecraft:bees", () -> DSL.list(DSL.optionalFields("entity_data", References.ENTITY_TREE.in($$0))));
      $$1.put("minecraft:block_entity_data", () -> References.BLOCK_ENTITY.in($$0));
      $$1.put("minecraft:bundle_contents", () -> DSL.list(References.ITEM_STACK.in($$0)));
      $$1.put(
         "minecraft:can_break",
         () -> DSL.optionalFields(
            "predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in($$0), DSL.list(References.BLOCK_NAME.in($$0)))))
         )
      );
      $$1.put(
         "minecraft:can_place_on",
         () -> DSL.optionalFields(
            "predicates", DSL.list(DSL.optionalFields("blocks", DSL.or(References.BLOCK_NAME.in($$0), DSL.list(References.BLOCK_NAME.in($$0)))))
         )
      );
      $$1.put("minecraft:charged_projectiles", () -> DSL.list(References.ITEM_STACK.in($$0)));
      $$1.put("minecraft:container", () -> DSL.list(DSL.optionalFields("item", References.ITEM_STACK.in($$0))));
      $$1.put("minecraft:entity_data", () -> References.ENTITY_TREE.in($$0));
      $$1.put("minecraft:pot_decorations", () -> DSL.list(References.ITEM_NAME.in($$0)));
      $$1.put("minecraft:food", () -> DSL.optionalFields("using_converts_to", References.ITEM_STACK.in($$0)));
      $$1.put("minecraft:custom_name", () -> References.TEXT_COMPONENT.in($$0));
      $$1.put("minecraft:item_name", () -> References.TEXT_COMPONENT.in($$0));
      $$1.put("minecraft:lore", () -> DSL.list(References.TEXT_COMPONENT.in($$0)));
      $$1.put(
         "minecraft:written_book_content",
         () -> DSL.optionalFields(
            "pages",
            DSL.list(
               DSL.or(
                  DSL.optionalFields("raw", References.TEXT_COMPONENT.in($$0), "filtered", References.TEXT_COMPONENT.in($$0)),
                  References.TEXT_COMPONENT.in($$0)
               )
            )
         )
      );
      return $$1;
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(components($$0)));
   }
}
