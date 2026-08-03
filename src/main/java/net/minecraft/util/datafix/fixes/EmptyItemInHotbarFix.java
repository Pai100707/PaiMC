package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;

public class EmptyItemInHotbarFix extends DataFix {
   public EmptyItemInHotbarFix(Schema $$0) {
      super($$0, false);
   }

   public TypeRewriteRule makeRule() {
      OpticFinder<Pair<String, Pair<Either<Pair<String, String>, com.mojang.datafixers.util.Unit>, Pair<Either<?, com.mojang.datafixers.util.Unit>, Dynamic<?>>>>> $$0 = DSL.typeFinder(
         this.getInputSchema().getType(References.ITEM_STACK)
      );
      return this.fixTypeEverywhereTyped(
         "EmptyItemInHotbarFix",
         this.getInputSchema().getType(References.HOTBAR),
         $$1 -> $$1.update(
            $$0,
            $$0xx -> $$0xx.mapSecond(
               $$0xxx -> {
                  Optional<String> $$1x = ((Either)$$0xxx.getFirst()).left().map(Pair::getSecond);
                  Dynamic<?> $$2 = (Dynamic<?>)((Pair)$$0xxx.getSecond()).getSecond();
                  boolean $$3 = $$1x.isEmpty() || $$1x.get().equals("minecraft:air");
                  boolean $$4 = $$2.get("Count").asInt(0) <= 0;
                  return !$$3 && !$$4
                     ? $$0xxx
                     : Pair.of(
                        Either.right(com.mojang.datafixers.util.Unit.INSTANCE), Pair.of(Either.right(com.mojang.datafixers.util.Unit.INSTANCE), $$2.emptyMap())
                     );
               }
            )
         )
      );
   }
}
