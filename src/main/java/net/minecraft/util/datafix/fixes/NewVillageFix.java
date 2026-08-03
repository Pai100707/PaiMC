package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class NewVillageFix extends DataFix {
   public NewVillageFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      CompoundListType<String, ?> $$0 = DSL.compoundList(DSL.string(), this.getInputSchema().getType(References.STRUCTURE_FEATURE));
      OpticFinder<? extends List<? extends Pair<String, ?>>> $$1 = $$0.finder();
      return this.cap($$0);
   }

   private <SF> TypeRewriteRule cap(CompoundListType<String, SF> $$0) {
      Type<?> $$1 = this.getInputSchema().getType(References.CHUNK);
      Type<?> $$2 = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
      OpticFinder<?> $$3 = $$1.findField("Level");
      OpticFinder<?> $$4 = $$3.type().findField("Structures");
      OpticFinder<?> $$5 = $$4.type().findField("Starts");
      OpticFinder<List<Pair<String, SF>>> $$6 = $$0.finder();
      return TypeRewriteRule.seq(
         this.fixTypeEverywhereTyped(
            "NewVillageFix",
            $$1,
            $$4x -> $$4x.updateTyped(
               $$3,
               $$3xx -> $$3xx.updateTyped(
                  $$4,
                  $$2xxx -> $$2xxx.updateTyped(
                        $$5,
                        $$1xxxx -> $$1xxxx.update(
                           $$6,
                           $$0xxxxx -> $$0xxxxx.stream()
                              .filter($$0xxxxxx -> !Objects.equals($$0xxxxxx.getFirst(), "Village"))
                              .map($$0xxxxxx -> $$0xxxxxx.mapFirst($$0xxxxxxx -> $$0xxxxxxx.equals("New_Village") ? "Village" : $$0xxxxxxx))
                              .collect(Collectors.toList())
                        )
                     )
                     .update(
                        DSL.remainderFinder(),
                        $$0xxxx -> $$0xxxx.update(
                           "References",
                           $$0xxxxx -> {
                              Optional<? extends Dynamic<?>> $$1xxxx = $$0xxxxx.get("New_Village").result();
                              return ((Dynamic)DataFixUtils.orElse($$1xxxx.map($$1xxxxx -> $$0xxxxx.remove("New_Village").set("Village", $$1xxxxx)), $$0xxxxx))
                                 .remove("Village");
                           }
                        )
                     )
               )
            )
         ),
         this.fixTypeEverywhereTyped(
            "NewVillageStartFix",
            $$2,
            $$0x -> $$0x.update(
               DSL.remainderFinder(),
               $$0xx -> $$0xx.update(
                  "id",
                  $$0xxx -> Objects.equals(NamespacedSchema.ensureNamespaced($$0xxx.asString("")), "minecraft:new_village")
                     ? $$0xxx.createString("minecraft:village")
                     : $$0xxx
               )
            )
         )
      );
   }
}
