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
import java.util.stream.IntStream;
import net.minecraft.util.datafix.fixes.References;

public class ChunkTicketUnpackPosFix
extends DataFix {
    private static final long CHUNK_COORD_BITS = 32L;
    private static final long CHUNK_COORD_MASK = 0xFFFFFFFFL;

    public ChunkTicketUnpackPosFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("ChunkTicketUnpackPosFix", this.getInputSchema().getType(References.SAVED_DATA_TICKETS), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.update("data", $$02 -> $$02.update("tickets", $$0 -> $$0.createList($$0.asStream().map($$02 -> $$02.update("chunk_pos", $$0 -> {
            long $$1 = $$0.asLong(0L);
            int $$2 = (int)($$1 & 0xFFFFFFFFL);
            int $$3 = (int)($$1 >>> 32 & 0xFFFFFFFFL);
            return $$0.createIntList(IntStream.of($$2, $$3));
        })))))));
    }
}

