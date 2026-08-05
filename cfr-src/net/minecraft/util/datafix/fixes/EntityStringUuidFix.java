/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.datafix.fixes.References;

public class EntityStringUuidFix
extends DataFix {
    public EntityStringUuidFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityStringUuidFix", this.getInputSchema().getType(References.ENTITY), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> {
            Optional $$1 = $$0.get("UUID").asString().result();
            if ($$1.isPresent()) {
                UUID $$2 = UUID.fromString((String)$$1.get());
                return $$0.remove("UUID").set("UUIDMost", $$0.createLong($$2.getMostSignificantBits())).set("UUIDLeast", $$0.createLong($$2.getLeastSignificantBits()));
            }
            return $$0;
        }));
    }
}

