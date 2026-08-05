/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class EntityEquipmentToArmorAndHandFix
extends DataFix {
    public EntityEquipmentToArmorAndHandFix(Schema $$0) {
        super($$0, true);
    }

    public TypeRewriteRule makeRule() {
        return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK), this.getOutputSchema().getTypeRaw(References.ITEM_STACK));
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule cap(Type<ItemStackOld> $$02, Type<ItemStackNew> $$12) {
        Type $$2 = DSL.named((String)References.ENTITY_EQUIPMENT.typeName(), (Type)DSL.optional((Type)DSL.field((String)"Equipment", (Type)DSL.list($$02))));
        Type $$3 = DSL.named((String)References.ENTITY_EQUIPMENT.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"ArmorItems", (Type)DSL.list($$12))), (Type)DSL.optional((Type)DSL.field((String)"HandItems", (Type)DSL.list($$12))), (Type)DSL.optional((Type)DSL.field((String)"body_armor_item", $$12)), (Type)DSL.optional((Type)DSL.field((String)"saddle", $$12))));
        if (!$$2.equals((Object)this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!$$3.equals((Object)this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("EntityEquipmentToArmorAndHandFix - drop chances", this.getInputSchema().getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), EntityEquipmentToArmorAndHandFix::fixDropChances)), (TypeRewriteRule)this.fixTypeEverywhere("EntityEquipmentToArmorAndHandFix - equipment", $$2, $$3, $$1 -> {
            Object $$2 = ((Pair)$$12.read(new Dynamic($$1).emptyMap()).result().orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack."))).getFirst();
            Either $$3 = Either.right((Object)DSL.unit());
            return $$22 -> $$22.mapSecond($$2 -> {
                List $$3 = (List)$$2.map(Function.identity(), $$0 -> List.of());
                Either $$4 = Either.right((Object)DSL.unit());
                Either $$5 = Either.right((Object)DSL.unit());
                if (!$$3.isEmpty()) {
                    $$4 = Either.left((Object)Lists.newArrayList((Object[])new Object[]{$$3.getFirst(), $$2}));
                }
                if ($$3.size() > 1) {
                    ArrayList $$6 = Lists.newArrayList((Object[])new Object[]{$$2, $$2, $$2, $$2});
                    for (int $$7 = 1; $$7 < Math.min($$3.size(), 5); ++$$7) {
                        $$6.set($$7 - 1, $$3.get($$7));
                    }
                    $$5 = Either.left((Object)$$6);
                }
                return Pair.of((Object)$$5, (Object)Pair.of((Object)$$4, (Object)Pair.of((Object)$$3, (Object)$$3)));
            });
        }));
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> $$02) {
        Optional $$1 = $$02.get("DropChances").asStreamOpt().result();
        $$02 = $$02.remove("DropChances");
        if ($$1.isPresent()) {
            Iterator $$2 = Stream.concat(((Stream)$$1.get()).map($$0 -> Float.valueOf($$0.asFloat(0.0f))), Stream.generate(() -> Float.valueOf(0.0f))).iterator();
            float $$3 = ((Float)$$2.next()).floatValue();
            if ($$02.get("HandDropChances").result().isEmpty()) {
                $$02 = $$02.set("HandDropChances", $$02.createList(Stream.of(Float.valueOf($$3), Float.valueOf(0.0f)).map(arg_0 -> ((Dynamic)$$02).createFloat(arg_0))));
            }
            if ($$02.get("ArmorDropChances").result().isEmpty()) {
                $$02 = $$02.set("ArmorDropChances", $$02.createList(Stream.of((Float)$$2.next(), (Float)$$2.next(), (Float)$$2.next(), (Float)$$2.next()).map(arg_0 -> ((Dynamic)$$02).createFloat(arg_0))));
            }
        }
        return $$02;
    }
}

