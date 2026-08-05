/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V2501
extends NamespacedSchema {
    public V2501(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    private static void registerFurnace(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.register($$1, $$2, () -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"RecipesUsed", (TypeTemplate)DSL.compoundList((TypeTemplate)References.RECIPE.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))));
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        Map $$1 = super.registerBlockEntities($$0);
        V2501.registerFurnace($$0, $$1, "minecraft:furnace");
        V2501.registerFurnace($$0, $$1, "minecraft:smoker");
        V2501.registerFurnace($$0, $$1, "minecraft:blast_furnace");
        return $$1;
    }
}

