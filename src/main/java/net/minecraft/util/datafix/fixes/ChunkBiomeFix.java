package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class ChunkBiomeFix extends DataFix {
   public ChunkBiomeFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("Level");
      return this.fixTypeEverywhereTyped("Leaves fix", $$0, $$1x -> $$1x.updateTyped($$1, $$0xx -> $$0xx.update(DSL.remainderFinder(), $$0xxx -> {
         Optional<IntStream> $$1xx = $$0xxx.get("Biomes").asIntStreamOpt().result();
         if ($$1xx.isEmpty()) {
            return $$0xxx;
         } else {
            int[] $$2 = $$1xx.get().toArray();
            if ($$2.length != 256) {
               return $$0xxx;
            } else {
               int[] $$3 = new int[1024];

               for (int $$4 = 0; $$4 < 4; $$4++) {
                  for (int $$5 = 0; $$5 < 4; $$5++) {
                     int $$6 = ($$5 << 2) + 2;
                     int $$7 = ($$4 << 2) + 2;
                     int $$8 = $$7 << 4 | $$6;
                     $$3[$$4 << 2 | $$5] = $$2[$$8];
                  }
               }

               for (int $$9 = 1; $$9 < 64; $$9++) {
                  System.arraycopy($$3, 0, $$3, $$9 * 16, 16);
               }

               return $$0xxx.set("Biomes", $$0xxx.createIntList(Arrays.stream($$3)));
            }
         }
      })));
   }
}
