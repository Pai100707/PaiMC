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
import java.util.Optional;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class PlayerHeadBlockProfileFix
extends NamedEntityFix {
    public PlayerHeadBlockProfileFix(Schema $$0) {
        super($$0, false, "PlayerHeadBlockProfileFix", References.BLOCK_ENTITY, "minecraft:skull");
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fix);
    }

    private <T> Dynamic<T> fix(Dynamic<T> $$0) {
        Optional $$2;
        Optional $$1 = $$0.get("SkullOwner").result();
        Optional $$3 = $$1.or(() -> PlayerHeadBlockProfileFix.lambda$fix$0($$2 = $$0.get("ExtraType").result()));
        if ($$3.isEmpty()) {
            return $$0;
        }
        $$0 = $$0.remove("SkullOwner").remove("ExtraType");
        $$0 = $$0.set("profile", ItemStackComponentizationFix.fixProfile((Dynamic)$$3.get()));
        return $$0;
    }

    private static /* synthetic */ Optional lambda$fix$0(Optional $$0) {
        return $$0;
    }
}

