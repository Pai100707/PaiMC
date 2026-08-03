package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ThrownPotionSplitFix extends EntityRenameFix {
   private final Supplier<ThrownPotionSplitFix.ItemIdFinder> itemIdFinder = Suppliers.memoize(() -> {
      Type<?> $$0x = this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:potion");
      Type<?> $$1 = ExtraDataFixUtils.patchSubType($$0x, this.getInputSchema().getType(References.ENTITY), this.getOutputSchema().getType(References.ENTITY));
      OpticFinder<?> $$2 = $$1.findField("Item");
      OpticFinder<Pair<String, String>> $$3 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      return new ThrownPotionSplitFix.ItemIdFinder($$2, $$3);
   });

   public ThrownPotionSplitFix(Schema $$0) {
      super("ThrownPotionSplitFix", $$0, true);
   }

   @Override
   protected Pair<String, Typed<?>> fix(String $$0, Typed<?> $$1) {
      if (!$$0.equals("minecraft:potion")) {
         return Pair.of($$0, $$1);
      } else {
         String $$2 = this.itemIdFinder.get().getItemId($$1);
         return "minecraft:lingering_potion".equals($$2) ? Pair.of("minecraft:lingering_potion", $$1) : Pair.of("minecraft:splash_potion", $$1);
      }
   }

   record ItemIdFinder(OpticFinder<?> itemFinder, OpticFinder<Pair<String, String>> itemIdFinder) {
      public String getItemId(Typed<?> $$0) {
         return $$0.getOptionalTyped(this.itemFinder)
            .flatMap($$0x -> $$0x.getOptional(this.itemIdFinder))
            .<String>map(Pair::getSecond)
            .map(NamespacedSchema::ensureNamespaced)
            .orElse("");
      }
   }
}
