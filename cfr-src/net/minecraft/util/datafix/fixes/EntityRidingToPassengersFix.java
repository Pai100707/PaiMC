/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class EntityRidingToPassengersFix
extends DataFix {
    public EntityRidingToPassengersFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        Schema $$0 = this.getInputSchema();
        Schema $$1 = this.getOutputSchema();
        Type $$2 = $$0.getTypeRaw(References.ENTITY_TREE);
        Type $$3 = $$1.getTypeRaw(References.ENTITY_TREE);
        Type $$4 = $$0.getTypeRaw(References.ENTITY);
        return this.cap($$0, $$1, $$2, $$3, $$4);
    }

    private <OldEntityTree, NewEntityTree, Entity> TypeRewriteRule cap(Schema $$0, Schema $$1, Type<OldEntityTree> $$2, Type<NewEntityTree> $$3, Type<Entity> $$4) {
        Type $$52 = DSL.named((String)References.ENTITY_TREE.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Riding", $$2)), $$4));
        Type $$6 = DSL.named((String)References.ENTITY_TREE.typeName(), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"Passengers", (Type)DSL.list($$3))), $$4));
        Type $$7 = $$0.getType(References.ENTITY_TREE);
        Type $$8 = $$1.getType(References.ENTITY_TREE);
        if (!Objects.equals($$7, $$52)) {
            throw new IllegalStateException("Old entity type is not what was expected.");
        }
        if (!$$8.equals((Object)$$6, true, true)) {
            throw new IllegalStateException("New entity type is not what was expected.");
        }
        OpticFinder $$9 = DSL.typeFinder((Type)$$52);
        OpticFinder $$10 = DSL.typeFinder((Type)$$6);
        OpticFinder $$11 = DSL.typeFinder($$3);
        Type $$12 = $$0.getType(References.PLAYER);
        Type $$13 = $$1.getType(References.PLAYER);
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhere("EntityRidingToPassengerFix", $$52, $$6, $$5 -> $$6 -> {
            Optional<Object> $$7 = Optional.empty();
            Pair $$8 = $$6;
            while (true) {
                Either $$9 = (Either)DataFixUtils.orElse($$7.map($$4 -> {
                    OpticFinder $$9 = (Typed)$$3.pointTyped($$5).orElseThrow(() -> new IllegalStateException("Could not create new entity tree"));
                    Object $$6 = $$9.set($$10, $$4).getOptional($$11).orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
                    return Either.left((Object)ImmutableList.of($$6));
                }), (Object)Either.right((Object)DSL.unit()));
                $$7 = Optional.of(Pair.of((Object)References.ENTITY_TREE.typeName(), (Object)Pair.of((Object)$$9, (Object)((Pair)$$8.getSecond()).getSecond())));
                Optional $$10 = ((Either)((Pair)$$8.getSecond()).getFirst()).left();
                if ($$10.isEmpty()) break;
                $$8 = (Pair)new Typed($$2, $$5, $$10.get()).getOptional($$9).orElseThrow(() -> new IllegalStateException("Should always have an entity here"));
            }
            return (Pair)$$7.orElseThrow(() -> new IllegalStateException("Should always have an entity tree here"));
        }), (TypeRewriteRule)this.writeAndRead("player RootVehicle injecter", $$12, $$13));
    }
}

