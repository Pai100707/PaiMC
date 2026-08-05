/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class CarvingStepRemoveFix
extends DataFix {
    public CarvingStepRemoveFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("CarvingStepRemoveFix", this.getInputSchema().getType(References.CHUNK), CarvingStepRemoveFix::fixChunk);
    }

    private static Typed<?> fixChunk(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> {
            Optional $$3;
            Dynamic $$1 = $$0;
            Optional $$2 = $$1.get("CarvingMasks").result();
            if ($$2.isPresent() && ($$3 = ((Dynamic)$$2.get()).get("AIR").result()).isPresent()) {
                $$1 = $$1.set("carving_mask", (Dynamic)$$3.get());
            }
            return $$1.remove("CarvingMasks");
        });
    }
}

