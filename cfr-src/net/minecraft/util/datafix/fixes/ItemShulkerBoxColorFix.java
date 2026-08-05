/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemShulkerBoxColorFix
extends DataFix {
    public static final String[] NAMES_BY_COLOR = new String[]{"minecraft:white_shulker_box", "minecraft:orange_shulker_box", "minecraft:magenta_shulker_box", "minecraft:light_blue_shulker_box", "minecraft:yellow_shulker_box", "minecraft:lime_shulker_box", "minecraft:pink_shulker_box", "minecraft:gray_shulker_box", "minecraft:silver_shulker_box", "minecraft:cyan_shulker_box", "minecraft:purple_shulker_box", "minecraft:blue_shulker_box", "minecraft:brown_shulker_box", "minecraft:green_shulker_box", "minecraft:red_shulker_box", "minecraft:black_shulker_box"};

    public ItemShulkerBoxColorFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$1 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$2 = $$0.findField("tag");
        OpticFinder $$32 = $$2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped("ItemShulkerBoxColorFix", $$0, $$3 -> {
            Typed $$6;
            Optional $$7;
            Optional $$5;
            Optional $$4 = $$3.getOptional($$1);
            if ($$4.isPresent() && Objects.equals(((Pair)$$4.get()).getSecond(), "minecraft:shulker_box") && ($$5 = $$3.getOptionalTyped($$2)).isPresent() && ($$7 = ($$6 = (Typed)$$5.get()).getOptionalTyped($$32)).isPresent()) {
                Typed $$8 = (Typed)$$7.get();
                Dynamic $$9 = (Dynamic)$$8.get(DSL.remainderFinder());
                int $$10 = $$9.get("Color").asInt(0);
                $$9.remove("Color");
                return $$3.set($$2, $$6.set($$32, $$8.set(DSL.remainderFinder(), (Object)$$9))).set($$1, (Object)Pair.of((Object)References.ITEM_NAME.typeName(), (Object)NAMES_BY_COLOR[$$10 % 16]));
            }
            return $$3;
        });
    }
}

