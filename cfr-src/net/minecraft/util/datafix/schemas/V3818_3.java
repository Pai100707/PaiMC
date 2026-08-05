/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3818_3
extends NamespacedSchema {
    public V3818_3(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema $$0) {
        LinkedHashMap<String, Supplier<TypeTemplate>> $$1 = new LinkedHashMap<String, Supplier<TypeTemplate>>();
        $$1.put("minecraft:bees", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"entity_data", (TypeTemplate)References.ENTITY_TREE.in($$0))));
        $$1.put("minecraft:block_entity_data", () -> References.BLOCK_ENTITY.in($$0));
        $$1.put("minecraft:bundle_contents", () -> DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)));
        $$1.put("minecraft:can_break", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in($$0)))))));
        $$1.put("minecraft:can_place_on", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in($$0)))))));
        $$1.put("minecraft:charged_projectiles", () -> DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)));
        $$1.put("minecraft:container", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"item", (TypeTemplate)References.ITEM_STACK.in($$0))));
        $$1.put("minecraft:entity_data", () -> References.ENTITY_TREE.in($$0));
        $$1.put("minecraft:pot_decorations", () -> DSL.list((TypeTemplate)References.ITEM_NAME.in($$0)));
        $$1.put("minecraft:food", () -> DSL.optionalFields((String)"using_converts_to", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$1.put("minecraft:custom_name", () -> References.TEXT_COMPONENT.in($$0));
        $$1.put("minecraft:item_name", () -> References.TEXT_COMPONENT.in($$0));
        $$1.put("minecraft:lore", () -> DSL.list((TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$1.put("minecraft:written_book_content", () -> DSL.optionalFields((String)"pages", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)DSL.optionalFields((String)"raw", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"filtered", (TypeTemplate)References.TEXT_COMPONENT.in($$0)), (TypeTemplate)References.TEXT_COMPONENT.in($$0)))));
        return $$1;
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V3818_3.components($$0)));
    }
}

