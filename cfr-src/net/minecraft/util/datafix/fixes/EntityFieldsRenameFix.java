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
import java.util.Map;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityFieldsRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public EntityFieldsRenameFix(Schema $$0, String $$1, String $$2, Map<String, String> $$3) {
        super($$0, false, $$1, References.ENTITY, $$2);
        this.renames = $$3;
    }

    public Dynamic<?> fixTag(Dynamic<?> $$0) {
        for (Map.Entry<String, String> $$1 : this.renames.entrySet()) {
            $$0 = $$0.renameField($$1.getKey(), $$1.getValue());
        }
        return $$0;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixTag);
    }
}

