/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.stream.LongStream;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;

public class BitStorageAlignFix
extends DataFix {
    private static final int BIT_TO_LONG_SHIFT = 6;
    private static final int SECTION_WIDTH = 16;
    private static final int SECTION_HEIGHT = 16;
    private static final int SECTION_SIZE = 4096;
    private static final int HEIGHTMAP_BITS = 9;
    private static final int HEIGHTMAP_SIZE = 256;

    public BitStorageAlignFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        Type $$1 = $$0.findFieldType("Level");
        OpticFinder $$2 = DSL.fieldFinder((String)"Level", (Type)$$1);
        OpticFinder $$3 = $$2.type().findField("Sections");
        Type $$42 = ((List.ListType)$$3.type()).getElement();
        OpticFinder $$5 = DSL.typeFinder((Type)$$42);
        Type $$6 = DSL.named((String)References.BLOCK_STATE.typeName(), (Type)DSL.remainderType());
        OpticFinder $$7 = DSL.fieldFinder((String)"Palette", (Type)DSL.list((Type)$$6));
        return this.fixTypeEverywhereTyped("BitStorageAlignFix", $$0, this.getOutputSchema().getType(References.CHUNK), $$4 -> $$4.updateTyped($$2, $$3 -> this.updateHeightmaps(BitStorageAlignFix.updateSections($$3, $$5, $$7, $$3))));
    }

    private Typed<?> updateHeightmaps(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> $$0.update("Heightmaps", $$1 -> $$1.updateMapValues($$12 -> $$12.mapSecond($$1 -> BitStorageAlignFix.updateBitStorage($$0, $$1, 256, 9)))));
    }

    private static Typed<?> updateSections(OpticFinder<?> $$0, OpticFinder<?> $$1, OpticFinder<List<Pair<String, Dynamic<?>>>> $$22, Typed<?> $$3) {
        return $$3.updateTyped($$0, $$2 -> $$2.updateTyped($$1, $$12 -> {
            Object $$2 = $$12.getOptional($$22).map($$0 -> Math.max(4, DataFixUtils.ceillog2((int)$$0.size()))).orElse(0);
            if ($$2 == 0 || Mth.isPowerOfTwo($$2)) {
                return $$12;
            }
            return $$12.update(DSL.remainderFinder(), $$1 -> $$1.update("BlockStates", $$2 -> BitStorageAlignFix.updateBitStorage($$1, $$2, 4096, $$2)));
        }));
    }

    private static Dynamic<?> updateBitStorage(Dynamic<?> $$0, Dynamic<?> $$1, int $$2, int $$3) {
        long[] $$4 = $$1.asLongStream().toArray();
        long[] $$5 = BitStorageAlignFix.addPadding($$2, $$3, $$4);
        return $$0.createLongList(LongStream.of($$5));
    }

    public static long[] addPadding(int $$0, int $$1, long[] $$2) {
        int $$3 = $$2.length;
        if ($$3 == 0) {
            return $$2;
        }
        long $$4 = (1L << $$1) - 1L;
        int $$5 = 64 / $$1;
        int $$6 = ($$0 + $$5 - 1) / $$5;
        long[] $$7 = new long[$$6];
        int $$8 = 0;
        int $$9 = 0;
        long $$10 = 0L;
        int $$11 = 0;
        long $$12 = $$2[0];
        long $$13 = $$3 > 1 ? $$2[1] : 0L;
        for (int $$14 = 0; $$14 < $$0; ++$$14) {
            long $$21;
            int $$15 = $$14 * $$1;
            int $$16 = $$15 >> 6;
            int $$17 = ($$14 + 1) * $$1 - 1 >> 6;
            int $$18 = $$15 ^ $$16 << 6;
            if ($$16 != $$11) {
                $$12 = $$13;
                $$13 = $$16 + 1 < $$3 ? $$2[$$16 + 1] : 0L;
                $$11 = $$16;
            }
            if ($$16 == $$17) {
                long $$19 = $$12 >>> $$18 & $$4;
            } else {
                int $$20 = 64 - $$18;
                $$21 = ($$12 >>> $$18 | $$13 << $$20) & $$4;
            }
            int $$22 = $$9 + $$1;
            if ($$22 >= 64) {
                $$7[$$8++] = $$10;
                $$10 = $$21;
                $$9 = $$1;
                continue;
            }
            $$10 |= $$21 << $$9;
            $$9 = $$22;
        }
        if ($$10 != 0L) {
            $$7[$$8] = $$10;
        }
        return $$7;
    }
}

