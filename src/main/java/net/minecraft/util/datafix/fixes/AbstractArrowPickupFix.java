package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;

public class AbstractArrowPickupFix extends DataFix {
   public AbstractArrowPickupFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Schema $$0 = this.getInputSchema();
      return this.fixTypeEverywhereTyped("AbstractArrowPickupFix", $$0.getType(References.ENTITY), this::updateProjectiles);
   }

   private Typed<?> updateProjectiles(Typed<?> $$0) {
      $$0 = this.updateEntity($$0, "minecraft:arrow", AbstractArrowPickupFix::updatePickup);
      $$0 = this.updateEntity($$0, "minecraft:spectral_arrow", AbstractArrowPickupFix::updatePickup);
      return this.updateEntity($$0, "minecraft:trident", AbstractArrowPickupFix::updatePickup);
   }

   private static Dynamic<?> updatePickup(Dynamic<?> $$0) {
      if ($$0.get("pickup").result().isPresent()) {
         return $$0;
      } else {
         boolean $$1 = $$0.get("player").asBoolean(true);
         return $$0.set("pickup", $$0.createByte((byte)($$1 ? 1 : 0))).remove("player");
      }
   }

   private Typed<?> updateEntity(Typed<?> $$0, String $$1, Function<Dynamic<?>, Dynamic<?>> $$2) {
      Type<?> $$3 = this.getInputSchema().getChoiceType(References.ENTITY, $$1);
      Type<?> $$4 = this.getOutputSchema().getChoiceType(References.ENTITY, $$1);
      return $$0.updateTyped(DSL.namedChoice($$1, $$3), $$4, $$1x -> $$1x.update(DSL.remainderFinder(), $$2));
   }
}
