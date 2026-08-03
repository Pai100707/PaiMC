package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureFlagRemoveFix extends DataFix {
   private final String name;
   private final Set<String> flagsToRemove;

   public FeatureFlagRemoveFix(Schema $$0, String $$1, Set<String> $$2) {
      super($$0, false);
      this.name = $$1;
      this.flagsToRemove = $$2;
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         this.name, this.getInputSchema().getType(References.LIGHTWEIGHT_LEVEL), $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag)
      );
   }

   private <T> Dynamic<T> fixTag(Dynamic<T> $$0) {
      List<Dynamic<T>> $$1 = $$0.get("removed_features").asStream().collect(Collectors.toCollection(ArrayList::new));
      Dynamic<T> $$2 = $$0.update("enabled_features", $$2x -> (Dynamic)DataFixUtils.orElse($$2x.asStreamOpt().result().map($$2xx -> $$2xx.filter($$2xxx -> {
         Optional<String> $$3 = $$2xxx.asString().result();
         if ($$3.isEmpty()) {
            return true;
         } else {
            boolean $$4 = this.flagsToRemove.contains($$3.get());
            if ($$4) {
               $$1.add($$0.createString($$3.get()));
            }

            return !$$4;
         }
      })).map($$0::createList), $$2x));
      if (!$$1.isEmpty()) {
         $$2 = $$2.set("removed_features", $$0.createList($$1.stream()));
      }

      return $$2;
   }
}
