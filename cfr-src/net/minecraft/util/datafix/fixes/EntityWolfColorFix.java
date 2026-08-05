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

public class EntityWolfColorFix
extends NamedEntityFix {
    public EntityWolfColorFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "EntityWolfColorFix", References.ENTITY, "minecraft:wolf");
    }

    public Dynamic<?> fixTag(Dynamic<?> $$02) {
        return $$02.update("CollarColor", $$0 -> $$0.createByte((byte)(15 - $$0.asInt(0))));
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixTag);
    }
}

