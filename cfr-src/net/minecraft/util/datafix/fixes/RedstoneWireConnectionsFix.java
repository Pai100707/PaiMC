/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class RedstoneWireConnectionsFix
extends DataFix {
    public RedstoneWireConnectionsFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Schema $$02 = this.getInputSchema();
        return this.fixTypeEverywhereTyped("RedstoneConnectionsFix", $$02.getType(References.BLOCK_STATE), $$0 -> $$0.update(DSL.remainderFinder(), this::updateRedstoneConnections));
    }

    private <T> Dynamic<T> updateRedstoneConnections(Dynamic<T> $$02) {
        boolean $$1 = $$02.get("Name").asString().result().filter("minecraft:redstone_wire"::equals).isPresent();
        if (!$$1) {
            return $$02;
        }
        return $$02.update("Properties", $$0 -> {
            String $$12 = $$0.get("east").asString("none");
            String $$2 = $$0.get("west").asString("none");
            String $$3 = $$0.get("north").asString("none");
            String $$4 = $$0.get("south").asString("none");
            boolean $$5 = RedstoneWireConnectionsFix.isConnected($$12) || RedstoneWireConnectionsFix.isConnected($$2);
            boolean $$6 = RedstoneWireConnectionsFix.isConnected($$3) || RedstoneWireConnectionsFix.isConnected($$4);
            String $$7 = !RedstoneWireConnectionsFix.isConnected($$12) && !$$6 ? "side" : $$12;
            String $$8 = !RedstoneWireConnectionsFix.isConnected($$2) && !$$6 ? "side" : $$2;
            String $$9 = !RedstoneWireConnectionsFix.isConnected($$3) && !$$5 ? "side" : $$3;
            String $$10 = !RedstoneWireConnectionsFix.isConnected($$4) && !$$5 ? "side" : $$4;
            return $$0.update("east", $$1 -> $$1.createString($$7)).update("west", $$1 -> $$1.createString($$8)).update("north", $$1 -> $$1.createString($$9)).update("south", $$1 -> $$1.createString($$10));
        });
    }

    private static boolean isConnected(String $$0) {
        return !"none".equals($$0);
    }
}

