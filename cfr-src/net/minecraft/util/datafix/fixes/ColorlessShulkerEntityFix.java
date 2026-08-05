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

public class ColorlessShulkerEntityFix
extends NamedEntityFix {
    public ColorlessShulkerEntityFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "Colorless shulker entity fix", References.ENTITY, "minecraft:shulker");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> {
            if ($$0.get("Color").asInt(0) == 10) {
                return $$0.set("Color", $$0.createByte((byte)16));
            }
            return $$0;
        });
    }
}

