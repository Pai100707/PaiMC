/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityArmorStandSilentFix
extends NamedEntityFix {
    public EntityArmorStandSilentFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "EntityArmorStandSilentFix", References.ENTITY, "ArmorStand");
    }

    public Dynamic<?> fixTag(Dynamic<?> $$0) {
        if ($$0.get("Silent").asBoolean(false) && !$$0.get("Marker").asBoolean(false)) {
            return $$0.remove("Silent");
        }
        return $$0;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixTag);
    }
}

