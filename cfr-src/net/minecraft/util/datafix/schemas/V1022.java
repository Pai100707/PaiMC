/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1022
extends Schema {
    public V1022(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        super.registerTypes($$0, $$1, $$2);
        $$0.registerType(false, References.RECIPE, () -> DSL.constType(NamespacedSchema.namespacedString()));
        $$0.registerType(false, References.PLAYER, () -> DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"RootVehicle", (Object)DSL.optionalFields((String)"Entity", (TypeTemplate)References.ENTITY_TREE.in($$0))), Pair.of((Object)"ender_pearls", (Object)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0))), Pair.of((Object)"Inventory", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"EnderItems", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"ShoulderEntityLeft", (Object)References.ENTITY_TREE.in($$0)), Pair.of((Object)"ShoulderEntityRight", (Object)References.ENTITY_TREE.in($$0)), Pair.of((Object)"recipeBook", (Object)DSL.optionalFields((String)"recipes", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in($$0)), (String)"toBeDisplayed", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in($$0))))}));
        $$0.registerType(false, References.HOTBAR, () -> DSL.compoundList((TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
    }
}

