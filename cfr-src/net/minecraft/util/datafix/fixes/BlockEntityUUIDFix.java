/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntityUUIDFix
extends AbstractUUIDFix {
    public BlockEntityUUIDFix(Schema $$0) {
        super($$0, References.BLOCK_ENTITY);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("BlockEntityUUIDFix", this.getInputSchema().getType(this.typeReference), $$0 -> {
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:conduit", this::updateConduit);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:skull", this::updateSkull);
            return $$0;
        });
    }

    private Dynamic<?> updateSkull(Dynamic<?> $$02) {
        return $$02.get("Owner").get().map($$0 -> BlockEntityUUIDFix.replaceUUIDString($$0, "Id", "Id").orElse((Dynamic<?>)$$0)).map($$1 -> $$02.remove("Owner").set("SkullOwner", $$1)).result().orElse($$02);
    }

    private Dynamic<?> updateConduit(Dynamic<?> $$0) {
        return BlockEntityUUIDFix.replaceUUIDMLTag($$0, "target_uuid", "Target").orElse($$0);
    }
}

