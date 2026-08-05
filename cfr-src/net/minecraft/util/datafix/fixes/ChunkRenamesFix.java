/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class ChunkRenamesFix
extends DataFix {
    public ChunkRenamesFix(Schema $$0) {
        super($$0, true);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        OpticFinder $$1 = $$0.findField("Level");
        OpticFinder $$2 = $$1.type().findField("Structures");
        Type $$32 = this.getOutputSchema().getType(References.CHUNK);
        Type $$4 = $$32.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", $$0, $$32, $$3 -> {
            Typed $$4 = $$3.getTyped($$1);
            Typed<?> $$5 = ChunkRenamesFix.appendChunkName($$4);
            $$5 = $$5.set(DSL.remainderFinder(), ChunkRenamesFix.mergeRemainders($$3, (Dynamic)$$4.get(DSL.remainderFinder())));
            $$5 = ChunkRenamesFix.renameField($$5, "TileEntities", "block_entities");
            $$5 = ChunkRenamesFix.renameField($$5, "TileTicks", "block_ticks");
            $$5 = ChunkRenamesFix.renameField($$5, "Entities", "entities");
            $$5 = ChunkRenamesFix.renameField($$5, "Sections", "sections");
            $$5 = $$5.updateTyped($$2, $$4, $$0 -> ChunkRenamesFix.renameField($$0, "Starts", "starts"));
            $$5 = ChunkRenamesFix.renameField($$5, "Structures", "structures");
            return $$5.update(DSL.remainderFinder(), $$0 -> $$0.remove("Level"));
        });
    }

    private static Typed<?> renameField(Typed<?> $$0, String $$12, String $$2) {
        return ChunkRenamesFix.renameFieldHelper($$0, $$12, $$2, $$0.getType().findFieldType($$12)).update(DSL.remainderFinder(), $$1 -> $$1.remove($$12));
    }

    private static <A> Typed<?> renameFieldHelper(Typed<?> $$0, String $$1, String $$2, Type<A> $$3) {
        Type $$4 = DSL.optional((Type)DSL.field((String)$$1, $$3));
        Type $$5 = DSL.optional((Type)DSL.field((String)$$2, $$3));
        return $$0.update($$4.finder(), $$5, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> $$0) {
        return new Typed(DSL.named((String)"chunk", (Type)$$0.getType()), $$0.getOps(), (Object)Pair.of((Object)"chunk", (Object)$$0.getValue()));
    }

    private static <T> Dynamic<T> mergeRemainders(Typed<?> $$0, Dynamic<T> $$12) {
        DynamicOps $$22 = $$12.getOps();
        Dynamic $$3 = ((Dynamic)$$0.get(DSL.remainderFinder())).convert($$22);
        DataResult $$4 = $$22.getMap($$12.getValue()).flatMap($$2 -> $$22.mergeToMap($$3.getValue(), $$2));
        return $$4.result().map($$1 -> new Dynamic($$22, $$1)).orElse($$12);
    }
}

