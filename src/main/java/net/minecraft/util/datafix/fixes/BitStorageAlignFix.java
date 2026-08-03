package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.stream.LongStream;

public class BitStorageAlignFix extends DataFix {
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
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      Type<?> $$1 = $$0.findFieldType("Level");
      OpticFinder<?> $$2 = DSL.fieldFinder("Level", $$1);
      OpticFinder<?> $$3 = $$2.type().findField("Sections");
      Type<?> $$4 = ((ListType)$$3.type()).getElement();
      OpticFinder<?> $$5 = DSL.typeFinder($$4);
      Type<Pair<String, Dynamic<?>>> $$6 = DSL.named(References.BLOCK_STATE.typeName(), DSL.remainderType());
      OpticFinder<List<Pair<String, Dynamic<?>>>> $$7 = DSL.fieldFinder("Palette", DSL.list($$6));
      return this.fixTypeEverywhereTyped(
         "BitStorageAlignFix",
         $$0,
         this.getOutputSchema().getType(References.CHUNK),
         $$4x -> $$4x.updateTyped($$2, $$3xx -> this.updateHeightmaps(updateSections($$3, $$5, $$7, $$3xx)))
      );
   }

   private Typed<?> updateHeightmaps(Typed<?> $$0) {
      return $$0.update(
         DSL.remainderFinder(),
         $$0x -> $$0x.update("Heightmaps", $$1 -> $$1.updateMapValues($$1x -> $$1x.mapSecond($$1xx -> updateBitStorage($$0x, $$1xx, 256, 9))))
      );
   }

   private static Typed<?> updateSections(OpticFinder<?> $$0, OpticFinder<?> $$1, OpticFinder<List<Pair<String, Dynamic<?>>>> $$2, Typed<?> $$3) {
      return $$3.updateTyped(
         $$0,
         $$2x -> $$2x.updateTyped(
            $$1,
            $$1xx -> {
               int $$2xx = $$1xx.getOptional($$2).map($$0xxx -> Math.max(4, DataFixUtils.ceillog2($$0xxx.size()))).orElse(0);
               return $$2xx != 0 && !net.minecraft.util.Mth.isPowerOfTwo($$2xx)
                  ? $$1xx.update(DSL.remainderFinder(), $$1xxx -> $$1xxx.update("BlockStates", $$2xxx -> updateBitStorage($$1xxx, $$2xxx, 4096, $$2xx)))
                  : $$1xx;
            }
         )
      );
   }

   private static Dynamic<?> updateBitStorage(Dynamic<?> $$0, Dynamic<?> $$1, int $$2, int $$3) {
      long[] $$4 = $$1.asLongStream().toArray();
      long[] $$5 = addPadding($$2, $$3, $$4);
      return $$0.createLongList(LongStream.of($$5));
   }

   public static long[] addPadding(int $$0, int $$1, long[] $$2) {
      int $$3 = $$2.length;
      if ($$3 == 0) {
         return $$2;
      } else {
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

         for (int $$14 = 0; $$14 < $$0; $$14++) {
            int $$15 = $$14 * $$1;
            int $$16 = $$15 >> 6;
            int $$17 = ($$14 + 1) * $$1 - 1 >> 6;
            int $$18 = $$15 ^ $$16 << 6;
            if ($$16 != $$11) {
               $$12 = $$13;
               $$13 = $$16 + 1 < $$3 ? $$2[$$16 + 1] : 0L;
               $$11 = $$16;
            }

            long $$19;
            if ($$16 == $$17) {
               $$19 = $$12 >>> $$18 & $$4;
            } else {
               int $$20 = 64 - $$18;
               $$19 = ($$12 >>> $$18 | $$13 << $$20) & $$4;
            }

            int $$22 = $$9 + $$1;
            if ($$22 >= 64) {
               $$7[$$8++] = $$10;
               $$10 = $$19;
               $$9 = $$1;
            } else {
               $$10 |= $$19 << $$9;
               $$9 = $$22;
            }
         }

         if ($$10 != 0L) {
            $$7[$$8] = $$10;
         }

         return $$7;
      }
   }
}
