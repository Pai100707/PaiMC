/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class FixWolfHealth
extends NamedEntityFix {
    private static final String WOLF_ID = "minecraft:wolf";
    private static final String WOLF_HEALTH = "minecraft:generic.max_health";

    public FixWolfHealth(Schema $$0) {
        super($$0, false, "FixWolfHealth", References.ENTITY, WOLF_ID);
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), $$02 -> {
            MutableBoolean $$12 = new MutableBoolean(false);
            $$02 = $$02.update("Attributes", $$1 -> $$1.createList($$1.asStream().map($$12 -> {
                if (WOLF_HEALTH.equals(NamespacedSchema.ensureNamespaced($$12.get("Name").asString("")))) {
                    return $$12.update("Base", $$1 -> {
                        if ($$1.asDouble(0.0) == 20.0) {
                            $$12.setTrue();
                            return $$1.createDouble(40.0);
                        }
                        return $$1;
                    });
                }
                return $$12;
            })));
            if ($$12.isTrue()) {
                $$02 = $$02.update("Health", $$0 -> $$0.createFloat($$0.asFloat(0.0f) * 2.0f));
            }
            return $$02;
        });
    }
}

