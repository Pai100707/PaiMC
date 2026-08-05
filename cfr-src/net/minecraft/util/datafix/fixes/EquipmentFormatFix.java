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
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
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
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.datafix.fixes.References;

public class EquipmentFormatFix
extends DataFix {
    public EquipmentFormatFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
        Type $$1 = this.getOutputSchema().getTypeRaw(References.ITEM_STACK);
        OpticFinder $$2 = $$0.findField("id");
        return this.fix($$0, $$1, $$2);
    }

    private <ItemStackOld, ItemStackNew> TypeRewriteRule fix(Type<ItemStackOld> $$0, Type<ItemStackNew> $$1, OpticFinder<?> $$2) {
        Type $$3 = DSL.named((String)References.ENTITY_EQUIPMENT.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"ArmorItems", (Type)DSL.list($$0))), (Type)DSL.optional((Type)DSL.field((String)"HandItems", (Type)DSL.list($$0))), (Type)DSL.optional((Type)DSL.field((String)"body_armor_item", $$0)), (Type)DSL.optional((Type)DSL.field((String)"saddle", $$0))));
        Type $$4 = DSL.named((String)References.ENTITY_EQUIPMENT.typeName(), (Type)DSL.optional((Type)DSL.field((String)"equipment", (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"mainhand", $$1)), (Type)DSL.optional((Type)DSL.field((String)"offhand", $$1)), (Type)DSL.optional((Type)DSL.field((String)"feet", $$1)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"legs", $$1)), (Type)DSL.optional((Type)DSL.field((String)"chest", $$1)), (Type)DSL.optional((Type)DSL.field((String)"head", $$1)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"body", $$1)), (Type)DSL.optional((Type)DSL.field((String)"saddle", $$1)), (Type)DSL.remainderType()))))));
        if (!$$3.equals((Object)this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Input entity_equipment type does not match expected");
        }
        if (!$$4.equals((Object)this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
            throw new IllegalStateException("Output entity_equipment type does not match expected");
        }
        return this.fixTypeEverywhere("EquipmentFormatFix", $$3, $$4, $$22 -> {
            Predicate<Object> $$32 = $$3 -> {
                Typed $$4 = new Typed($$0, $$22, $$3);
                return $$4.getOptional($$2).isEmpty();
            };
            return $$2 -> {
                String $$3 = (String)$$2.getFirst();
                Pair $$4 = (Pair)$$2.getSecond();
                List $$5 = (List)((Either)$$4.getFirst()).map(Function.identity(), $$0 -> List.of());
                List $$6 = (List)((Either)((Pair)$$4.getSecond()).getFirst()).map(Function.identity(), $$0 -> List.of());
                Either $$7 = (Either)((Pair)((Pair)$$4.getSecond()).getSecond()).getFirst();
                Either $$8 = (Either)((Pair)((Pair)$$4.getSecond()).getSecond()).getSecond();
                Either $$9 = EquipmentFormatFix.getItemFromList(0, $$5, $$32);
                Either $$10 = EquipmentFormatFix.getItemFromList(1, $$5, $$32);
                Either $$11 = EquipmentFormatFix.getItemFromList(2, $$5, $$32);
                Either $$12 = EquipmentFormatFix.getItemFromList(3, $$5, $$32);
                Either $$13 = EquipmentFormatFix.getItemFromList(0, $$6, $$32);
                Either $$14 = EquipmentFormatFix.getItemFromList(1, $$6, $$32);
                if (EquipmentFormatFix.areAllEmpty($$7, $$8, $$9, $$10, $$11, $$12, $$13, $$14)) {
                    return Pair.of((Object)$$3, (Object)Either.right((Object)Unit.INSTANCE));
                }
                return Pair.of((Object)$$3, (Object)Either.left((Object)Pair.of($$13, (Object)Pair.of($$14, (Object)Pair.of($$9, (Object)Pair.of($$10, (Object)Pair.of($$11, (Object)Pair.of($$12, (Object)Pair.of((Object)$$7, (Object)Pair.of((Object)$$8, (Object)new Dynamic($$22)))))))))));
            };
        });
    }

    @SafeVarargs
    private static boolean areAllEmpty(Either<?, Unit> ... $$0) {
        for (Either<?, Unit> $$1 : $$0) {
            if (!$$1.right().isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static <ItemStack> Either<ItemStack, Unit> getItemFromList(int $$0, List<ItemStack> $$1, Predicate<ItemStack> $$2) {
        if ($$0 >= $$1.size()) {
            return Either.right((Object)Unit.INSTANCE);
        }
        ItemStack $$3 = $$1.get($$0);
        if ($$2.test($$3)) {
            return Either.right((Object)Unit.INSTANCE);
        }
        return Either.left($$3);
    }
}

