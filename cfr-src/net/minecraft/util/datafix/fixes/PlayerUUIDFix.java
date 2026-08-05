/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.EntityUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class PlayerUUIDFix
extends AbstractUUIDFix {
    public PlayerUUIDFix(Schema $$0) {
        super($$0, References.PLAYER);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("PlayerUUIDFix", this.getInputSchema().getType(this.typeReference), $$03 -> {
            OpticFinder $$1 = $$03.getType().findField("RootVehicle");
            return $$03.updateTyped($$1, $$1.type(), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> PlayerUUIDFix.replaceUUIDLeastMost($$0, "Attach", "Attach").orElse((Dynamic<?>)$$0))).update(DSL.remainderFinder(), $$0 -> EntityUUIDFix.updateEntityUUID(EntityUUIDFix.updateLivingEntity($$0)));
        });
    }
}

