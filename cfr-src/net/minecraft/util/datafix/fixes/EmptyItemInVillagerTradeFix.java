/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EmptyItemInVillagerTradeFix
extends DataFix {
    public EmptyItemInVillagerTradeFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        Type $$02 = this.getInputSchema().getType(References.VILLAGER_TRADE);
        return this.writeFixAndRead("EmptyItemInVillagerTradeFix", $$02, $$02, $$0 -> {
            Dynamic $$1 = $$0.get("buyB").orElseEmptyMap();
            String $$2 = NamespacedSchema.ensureNamespaced($$1.get("id").asString("minecraft:air"));
            int $$3 = $$1.get("count").asInt(0);
            if ($$2.equals("minecraft:air") || $$3 == 0) {
                return $$0.remove("buyB");
            }
            return $$0;
        });
    }
}

