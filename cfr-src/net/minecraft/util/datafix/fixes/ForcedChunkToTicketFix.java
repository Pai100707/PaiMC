/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.util.datafix.fixes.References;

public class ForcedChunkToTicketFix
extends DataFix {
    public ForcedChunkToTicketFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ForcedChunkToTicketFix", this.getInputSchema().getType(References.SAVED_DATA_TICKETS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.update("data", $$1 -> $$1.renameAndFixField("Forced", "tickets", $$12 -> $$12.createList($$12.asLongStream().mapToObj($$1 -> $$0.emptyMap().set("type", $$0.createString("minecraft:forced")).set("level", $$0.createInt(31)).set("ticks_left", $$0.createLong(0L)).set("chunk_pos", $$0.createLong($$1))))))));
    }
}

