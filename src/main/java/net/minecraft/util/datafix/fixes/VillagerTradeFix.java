package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends DataFix {
   public VillagerTradeFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.VILLAGER_TRADE);
      OpticFinder<?> $$1 = $$0.findField("buy");
      OpticFinder<?> $$2 = $$0.findField("buyB");
      OpticFinder<?> $$3 = $$0.findField("sell");
      OpticFinder<Pair<String, String>> $$4 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      Function<Typed<?>, Typed<?>> $$5 = $$1x -> this.updateItemStack($$4, $$1x);
      return this.fixTypeEverywhereTyped("Villager trade fix", $$0, $$4x -> $$4x.updateTyped($$1, $$5).updateTyped($$2, $$5).updateTyped($$3, $$5));
   }

   private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> $$0, Typed<?> $$1) {
      return $$1.update($$0, $$0x -> $$0x.mapSecond($$0xx -> Objects.equals($$0xx, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : $$0xx));
   }
}
