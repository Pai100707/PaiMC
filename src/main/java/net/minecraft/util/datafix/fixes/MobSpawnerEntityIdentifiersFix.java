package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;

public class MobSpawnerEntityIdentifiersFix extends DataFix {
   public MobSpawnerEntityIdentifiersFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   private Dynamic<?> fix(Dynamic<?> $$0) {
      if (!"MobSpawner".equals($$0.get("id").asString(""))) {
         return $$0;
      } else {
         Optional<String> $$1 = $$0.get("EntityId").asString().result();
         if ($$1.isPresent()) {
            Dynamic<?> $$2 = (Dynamic<?>)DataFixUtils.orElse($$0.get("SpawnData").result(), $$0.emptyMap());
            $$2 = $$2.set("id", $$2.createString($$1.get().isEmpty() ? "Pig" : $$1.get()));
            $$0 = $$0.set("SpawnData", $$2);
            $$0 = $$0.remove("EntityId");
         }

         Optional<? extends Stream<? extends Dynamic<?>>> $$3 = $$0.get("SpawnPotentials").asStreamOpt().result();
         if ($$3.isPresent()) {
            $$0 = $$0.set("SpawnPotentials", $$0.createList($$3.get().map($$0x -> {
               Optional<String> $$1x = $$0x.get("Type").asString().result();
               if ($$1x.isPresent()) {
                  Dynamic<?> $$2 = ((Dynamic)DataFixUtils.orElse($$0x.get("Properties").result(), $$0x.emptyMap())).set("id", $$0x.createString($$1x.get()));
                  return $$0x.set("Entity", $$2).remove("Type").remove("Properties");
               } else {
                  return $$0x;
               }
            })));
         }

         return $$0;
      }
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
      return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), $$0, $$1 -> {
         Dynamic<?> $$2 = (Dynamic<?>)$$1.get(DSL.remainderFinder());
         $$2 = $$2.set("id", $$2.createString("MobSpawner"));
         DataResult<? extends Pair<? extends Typed<?>, ?>> $$3 = $$0.readTyped(this.fix($$2));
         return $$3.result().isEmpty() ? $$1 : (Typed)((Pair)$$3.result().get()).getFirst();
      });
   }
}
