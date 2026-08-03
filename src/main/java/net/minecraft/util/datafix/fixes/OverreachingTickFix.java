package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;

public class OverreachingTickFix extends DataFix {
   public OverreachingTickFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("block_ticks");
      return this.fixTypeEverywhereTyped("Handle ticks saved in the wrong chunk", $$0, $$1x -> {
         Optional<? extends Typed<?>> $$2 = $$1x.getOptionalTyped($$1);
         Optional<? extends Dynamic<?>> $$3 = $$2.isPresent() ? $$2.get().write().result() : Optional.empty();
         return $$1x.update(DSL.remainderFinder(), $$1xx -> {
            int $$2x = $$1xx.get("xPos").asInt(0);
            int $$3x = $$1xx.get("zPos").asInt(0);
            Optional<? extends Dynamic<?>> $$4 = $$1xx.get("fluid_ticks").get().result();
            $$1xx = extractOverreachingTicks($$1xx, $$2x, $$3x, $$3, "neighbor_block_ticks");
            return extractOverreachingTicks($$1xx, $$2x, $$3x, $$4, "neighbor_fluid_ticks");
         });
      });
   }

   private static Dynamic<?> extractOverreachingTicks(Dynamic<?> $$0, int $$1, int $$2, Optional<? extends Dynamic<?>> $$3, String $$4) {
      if ($$3.isPresent()) {
         List<? extends Dynamic<?>> $$5 = $$3.get().asStream().filter($$2x -> {
            int $$3x = $$2x.get("x").asInt(0);
            int $$4x = $$2x.get("z").asInt(0);
            int $$5x = Math.abs($$1 - ($$3x >> 4));
            int $$6 = Math.abs($$2 - ($$4x >> 4));
            return ($$5x != 0 || $$6 != 0) && $$5x <= 1 && $$6 <= 1;
         }).toList();
         if (!$$5.isEmpty()) {
            $$0 = $$0.set("UpgradeData", $$0.get("UpgradeData").orElseEmptyMap().set($$4, $$0.createList($$5.stream())));
         }
      }

      return $$0;
   }
}
