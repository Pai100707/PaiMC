/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
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

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix
extends DataFix {
    public ItemBannerColorFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$1 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$2 = $$0.findField("tag");
        OpticFinder $$32 = $$2.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped("ItemBannerColorFix", $$0, $$3 -> {
            Optional $$4 = $$3.getOptional($$1);
            if ($$4.isPresent() && Objects.equals(((Pair)$$4.get()).getSecond(), "minecraft:banner")) {
                Typed $$7;
                Optional $$8;
                Dynamic $$5 = (Dynamic)$$3.get(DSL.remainderFinder());
                Optional $$6 = $$3.getOptionalTyped($$2);
                if ($$6.isPresent() && ($$8 = ($$7 = (Typed)$$6.get()).getOptionalTyped($$32)).isPresent()) {
                    Typed $$9 = (Typed)$$8.get();
                    Dynamic $$10 = (Dynamic)$$7.get(DSL.remainderFinder());
                    Dynamic $$11 = (Dynamic)$$9.getOrCreate(DSL.remainderFinder());
                    if ($$11.get("Base").asNumber().result().isPresent()) {
                        Dynamic $$14;
                        Dynamic $$13;
                        $$5 = $$5.set("Damage", $$5.createShort((short)($$11.get("Base").asInt(0) & 0xF)));
                        Optional $$12 = $$10.get("display").result();
                        if ($$12.isPresent() && Objects.equals($$13 = (Dynamic)$$12.get(), $$14 = $$13.createMap((Map)ImmutableMap.of((Object)$$13.createString("Lore"), (Object)$$13.createList(Stream.of($$13.createString("(+NBT"))))))) {
                            return $$3.set(DSL.remainderFinder(), (Object)$$5);
                        }
                        $$11.remove("Base");
                        return $$3.set(DSL.remainderFinder(), (Object)$$5).set($$2, $$7.set($$32, $$9.set(DSL.remainderFinder(), (Object)$$11)));
                    }
                }
                return $$3.set(DSL.remainderFinder(), (Object)$$5);
            }
            return $$3;
        });
    }
}

