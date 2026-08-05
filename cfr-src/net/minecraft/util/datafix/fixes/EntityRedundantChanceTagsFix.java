/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import net.minecraft.util.datafix.fixes.References;

public class EntityRedundantChanceTagsFix
extends DataFix {
    private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

    public EntityRedundantChanceTagsFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityRedundantChanceTagsFix", this.getInputSchema().getType(References.ENTITY), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            if (EntityRedundantChanceTagsFix.isZeroList($$0.get("HandDropChances"), 2)) {
                $$0 = $$0.remove("HandDropChances");
            }
            if (EntityRedundantChanceTagsFix.isZeroList($$0.get("ArmorDropChances"), 4)) {
                $$0 = $$0.remove("ArmorDropChances");
            }
            return $$0;
        }));
    }

    private static boolean isZeroList(OptionalDynamic<?> $$0, int $$12) {
        return $$0.flatMap(arg_0 -> FLOAT_LIST_CODEC.parse(arg_0)).map($$1 -> $$1.size() == $$12 && $$1.stream().allMatch($$0 -> $$0.floatValue() == 0.0f)).result().orElse(false);
    }
}

