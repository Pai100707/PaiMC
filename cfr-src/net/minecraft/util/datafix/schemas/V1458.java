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
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1458
extends NamespacedSchema {
    public V1458(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(true, References.ENTITY, () -> DSL.and((TypeTemplate)References.ENTITY_EQUIPMENT.in($$0), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", V1458.namespacedString(), (Map)$$1))));
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        Map $$1 = super.registerBlockEntities($$0);
        $$0.register($$1, "minecraft:beacon", () -> V1458.nameable($$0));
        $$0.register($$1, "minecraft:banner", () -> V1458.nameable($$0));
        $$0.register($$1, "minecraft:brewing_stand", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:chest", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:trapped_chest", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:dispenser", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:dropper", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:enchanting_table", () -> V1458.nameable($$0));
        $$0.register($$1, "minecraft:furnace", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:hopper", () -> V1458.nameableInventory($$0));
        $$0.register($$1, "minecraft:shulker_box", () -> V1458.nameableInventory($$0));
        return $$1;
    }

    public static TypeTemplate nameableInventory(Schema $$0) {
        return DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0));
    }

    public static TypeTemplate nameable(Schema $$0) {
        return DSL.optionalFields((String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0));
    }
}

