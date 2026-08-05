/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityGoatMissingStateFix
extends NamedEntityFix {
    public EntityGoatMissingStateFix(Schema $$0) {
        super($$0, false, "EntityGoatMissingStateFix", References.ENTITY, "minecraft:goat");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> $$0.set("HasLeftHorn", $$0.createBoolean(true)).set("HasRightHorn", $$0.createBoolean(true)));
    }
}

