/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.util.datafix.fixes.References;

public class DropChancesFormatFix
extends DataFix {
    private static final List<String> ARMOR_SLOT_NAMES = List.of("feet", "legs", "chest", "head");
    private static final List<String> HAND_SLOT_NAMES = List.of("mainhand", "offhand");
    private static final float DEFAULT_CHANCE = 0.085f;

    public DropChancesFormatFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DropChancesFormatFix", this.getInputSchema().getType(References.ENTITY), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            List<Float> $$1 = DropChancesFormatFix.parseDropChances($$0.get("ArmorDropChances"));
            List<Float> $$2 = DropChancesFormatFix.parseDropChances($$0.get("HandDropChances"));
            float $$3 = $$0.get("body_armor_drop_chance").asNumber().result().map(Number::floatValue).orElse(Float.valueOf(0.085f)).floatValue();
            $$0 = $$0.remove("ArmorDropChances").remove("HandDropChances").remove("body_armor_drop_chance");
            Dynamic $$4 = $$0.emptyMap();
            $$4 = DropChancesFormatFix.addSlotChances($$4, $$1, ARMOR_SLOT_NAMES);
            $$4 = DropChancesFormatFix.addSlotChances($$4, $$2, HAND_SLOT_NAMES);
            if ($$3 != 0.085f) {
                $$4 = $$4.set("body", $$0.createFloat($$3));
            }
            if (!$$4.equals((Object)$$0.emptyMap())) {
                return $$0.set("drop_chances", $$4);
            }
            return $$0;
        }));
    }

    private static Dynamic<?> addSlotChances(Dynamic<?> $$0, List<Float> $$1, List<String> $$2) {
        for (int $$3 = 0; $$3 < $$2.size() && $$3 < $$1.size(); ++$$3) {
            String $$4 = $$2.get($$3);
            float $$5 = $$1.get($$3).floatValue();
            if ($$5 == 0.085f) continue;
            $$0 = $$0.set($$4, $$0.createFloat($$5));
        }
        return $$0;
    }

    private static List<Float> parseDropChances(OptionalDynamic<?> $$02) {
        return $$02.asStream().map($$0 -> Float.valueOf($$0.asFloat(0.085f))).toList();
    }
}

