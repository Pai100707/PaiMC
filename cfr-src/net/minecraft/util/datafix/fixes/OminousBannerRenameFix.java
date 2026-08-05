/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public class OminousBannerRenameFix
extends ItemStackTagFix {
    public OminousBannerRenameFix(Schema $$02) {
        super($$02, "OminousBannerRenameFix", $$0 -> $$0.equals("minecraft:white_banner"));
    }

    private <T> Dynamic<T> fixItemStackTag(Dynamic<T> $$0) {
        return $$0.update("display", $$02 -> $$02.update("Name", $$0 -> {
            Optional $$1 = $$0.asString().result();
            if ($$1.isPresent()) {
                return $$0.createString(((String)$$1.get()).replace("\"translate\":\"block.minecraft.illager_banner\"", "\"translate\":\"block.minecraft.ominous_banner\""));
            }
            return $$0;
        }));
    }

    @Override
    protected Typed<?> fixItemStackTag(Typed<?> $$0) {
        return Util.writeAndReadTypedOrThrow($$0, $$0.getType(), this::fixItemStackTag);
    }
}

