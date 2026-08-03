package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.SectionPos;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlendingDataFix extends DataFix {
   private final String name;
   private static final Set<String> STATUSES_TO_SKIP_BLENDING = Set.of(
      "minecraft:empty", "minecraft:structure_starts", "minecraft:structure_references", "minecraft:biomes"
   );

   public BlendingDataFix(Schema $$0) {
      super($$0, false);
      this.name = "Blending Data Fix v" + $$0.getVersionKey();
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getOutputSchema().getType(References.CHUNK);
      return this.fixTypeEverywhereTyped(this.name, $$0, $$0x -> $$0x.update(DSL.remainderFinder(), $$0xx -> updateChunkTag($$0xx, $$0xx.get("__context"))));
   }

   private static Dynamic<?> updateChunkTag(Dynamic<?> $$0, OptionalDynamic<?> $$1) {
      $$0 = $$0.remove("blending_data");
      boolean $$2 = "minecraft:overworld".equals($$1.get("dimension").asString().result().orElse(""));
      Optional<? extends Dynamic<?>> $$3 = $$0.get("Status").result();
      if ($$2 && $$3.isPresent()) {
         String $$4 = NamespacedSchema.ensureNamespaced($$3.get().asString("empty"));
         Optional<? extends Dynamic<?>> $$5 = $$0.get("below_zero_retrogen").result();
         if (!STATUSES_TO_SKIP_BLENDING.contains($$4)) {
            $$0 = updateBlendingData($$0, 384, -64);
         } else if ($$5.isPresent()) {
            Dynamic<?> $$6 = (Dynamic<?>)$$5.get();
            String $$7 = NamespacedSchema.ensureNamespaced($$6.get("target_status").asString("empty"));
            if (!STATUSES_TO_SKIP_BLENDING.contains($$7)) {
               $$0 = updateBlendingData($$0, 256, 0);
            }
         }
      }

      return $$0;
   }

   private static Dynamic<?> updateBlendingData(Dynamic<?> $$0, int $$1, int $$2) {
      return $$0.set(
         "blending_data",
         $$0.createMap(
            Map.of(
               $$0.createString("min_section"),
               $$0.createInt(SectionPos.blockToSectionCoord($$2)),
               $$0.createString("max_section"),
               $$0.createInt(SectionPos.blockToSectionCoord($$2 + $$1))
            )
         )
      );
   }
}
