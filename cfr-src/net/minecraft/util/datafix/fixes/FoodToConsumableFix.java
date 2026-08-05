/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class FoodToConsumableFix
extends DataFix {
    public FoodToConsumableFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("Food to consumable fix", this.getInputSchema().getType(References.DATA_COMPONENTS), this.getOutputSchema().getType(References.DATA_COMPONENTS), $$02 -> {
            Optional $$1 = $$02.get("minecraft:food").result();
            if ($$1.isPresent()) {
                float $$2 = ((Dynamic)$$1.get()).get("eat_seconds").asFloat(1.6f);
                Stream $$3 = ((Dynamic)$$1.get()).get("effects").asStream();
                Stream<Dynamic> $$4 = $$3.map($$0 -> $$0.emptyMap().set("type", $$0.createString("minecraft:apply_effects")).set("effects", $$0.createList($$0.get("effect").result().stream())).set("probability", $$0.createFloat($$0.get("probability").asFloat(1.0f))));
                $$02 = Dynamic.copyField((Dynamic)((Dynamic)$$1.get()), (String)"using_converts_to", (Dynamic)$$02, (String)"minecraft:use_remainder");
                $$02 = $$02.set("minecraft:food", ((Dynamic)$$1.get()).remove("eat_seconds").remove("effects").remove("using_converts_to"));
                $$02 = $$02.set("minecraft:consumable", $$02.emptyMap().set("consume_seconds", $$02.createFloat($$2)).set("on_consume_effects", $$02.createList($$4)));
                return $$02;
            }
            return $$02;
        });
    }
}

