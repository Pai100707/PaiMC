package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntityHorseSaddleFix extends NamedEntityFix {
   public EntityHorseSaddleFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "EntityHorseSaddleFix", References.ENTITY, "EntityHorse");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      OpticFinder<Pair<String, String>> $$1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      Type<?> $$2 = this.getInputSchema().getTypeRaw(References.ITEM_STACK);
      OpticFinder<?> $$3 = DSL.fieldFinder("SaddleItem", $$2);
      Optional<? extends Typed<?>> $$4 = $$0.getOptionalTyped($$3);
      Dynamic<?> $$5 = (Dynamic<?>)$$0.get(DSL.remainderFinder());
      if ($$4.isEmpty() && $$5.get("Saddle").asBoolean(false)) {
         Typed<?> $$6 = (Typed<?>)$$2.pointTyped($$0.getOps()).orElseThrow(IllegalStateException::new);
         $$6 = $$6.set($$1, Pair.of(References.ITEM_NAME.typeName(), "minecraft:saddle"));
         Dynamic<?> $$7 = $$5.emptyMap();
         $$7 = $$7.set("Count", $$7.createByte((byte)1));
         $$7 = $$7.set("Damage", $$7.createShort((short)0));
         $$6 = $$6.set(DSL.remainderFinder(), $$7);
         $$5.remove("Saddle");
         return $$0.set($$3, $$6).set(DSL.remainderFinder(), $$5);
      } else {
         return $$0;
      }
   }
}
