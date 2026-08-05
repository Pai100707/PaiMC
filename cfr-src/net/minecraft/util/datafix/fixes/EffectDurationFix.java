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
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EffectDurationFix
extends DataFix {
    private static final Set<String> POTION_ITEMS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public EffectDurationFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Schema $$02 = this.getInputSchema();
        Type $$1 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$22 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder $$3 = $$1.findField("tag");
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("EffectDurationEntity", $$02.getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), this::updateEntity)), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped("EffectDurationPlayer", $$02.getType(References.PLAYER), $$0 -> $$0.update(DSL.remainderFinder(), this::updateEntity)), this.fixTypeEverywhereTyped("EffectDurationItem", $$1, $$2 -> {
            Optional $$3;
            if ($$2.getOptional($$22).filter($$0 -> POTION_ITEMS.contains($$0.getSecond())).isPresent() && ($$3 = $$2.getOptionalTyped($$3)).isPresent()) {
                Dynamic $$4 = (Dynamic)((Typed)$$3.get()).get(DSL.remainderFinder());
                Typed $$5 = ((Typed)$$3.get()).set(DSL.remainderFinder(), (Object)$$4.update("CustomPotionEffects", this::fix));
                return $$2.set($$3, $$5);
            }
            return $$2;
        })});
    }

    private Dynamic<?> fixEffect(Dynamic<?> $$0) {
        return $$0.update("FactorCalculationData", $$1 -> {
            int $$2 = $$1.get("effect_changed_timestamp").asInt(-1);
            $$1 = $$1.remove("effect_changed_timestamp");
            int $$3 = $$0.get("Duration").asInt(-1);
            int $$4 = $$2 - $$3;
            return $$1.set("ticks_active", $$1.createInt($$4));
        });
    }

    private Dynamic<?> fix(Dynamic<?> $$0) {
        return $$0.createList($$0.asStream().map(this::fixEffect));
    }

    private Dynamic<?> updateEntity(Dynamic<?> $$0) {
        $$0 = $$0.update("Effects", this::fix);
        $$0 = $$0.update("ActiveEffects", this::fix);
        $$0 = $$0.update("CustomPotionEffects", this::fix);
        return $$0;
    }
}

