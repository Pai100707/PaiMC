/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemoveEmptyItemInBrushableBlockFix
extends NamedEntityWriteReadFix {
    public RemoveEmptyItemInBrushableBlockFix(Schema $$0) {
        super($$0, false, "RemoveEmptyItemInSuspiciousBlockFix", References.BLOCK_ENTITY, "minecraft:brushable_block");
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> $$0) {
        Optional $$1 = $$0.get("item").result();
        if ($$1.isPresent() && RemoveEmptyItemInBrushableBlockFix.isEmptyStack((Dynamic)$$1.get())) {
            return $$0.remove("item");
        }
        return $$0;
    }

    private static boolean isEmptyStack(Dynamic<?> $$0) {
        String $$1 = NamespacedSchema.ensureNamespaced($$0.get("id").asString("minecraft:air"));
        int $$2 = $$0.get("count").asInt(0);
        return $$1.equals("minecraft:air") || $$2 == 0;
    }
}

