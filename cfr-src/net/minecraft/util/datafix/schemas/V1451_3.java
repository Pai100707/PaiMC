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

public class V1451_3
extends NamespacedSchema {
    public V1451_3(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        Map $$12 = super.registerEntities($$0);
        $$0.registerSimple($$12, "minecraft:egg");
        $$0.registerSimple($$12, "minecraft:ender_pearl");
        $$0.registerSimple($$12, "minecraft:fireball");
        $$0.register($$12, "minecraft:potion", $$1 -> DSL.optionalFields((String)"Potion", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple($$12, "minecraft:small_fireball");
        $$0.registerSimple($$12, "minecraft:snowball");
        $$0.registerSimple($$12, "minecraft:wither_skull");
        $$0.registerSimple($$12, "minecraft:xp_bottle");
        $$0.register($$12, "minecraft:arrow", () -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:enderman", () -> DSL.optionalFields((String)"carriedBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:falling_block", () -> DSL.optionalFields((String)"BlockState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"TileEntityData", (TypeTemplate)References.BLOCK_ENTITY.in($$0)));
        $$0.register($$12, "minecraft:spectral_arrow", () -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:chest_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.register($$12, "minecraft:commandblock_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.register($$12, "minecraft:furnace_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:hopper_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.register($$12, "minecraft:minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        $$0.register($$12, "minecraft:spawner_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (TypeTemplate)References.UNTAGGED_SPAWNER.in($$0)));
        $$0.register($$12, "minecraft:tnt_minecart", () -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        return $$12;
    }
}

