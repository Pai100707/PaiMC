package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.util.datafix.ExtraDataFixUtils;

public class FixProjectileStoredItem extends DataFix {
   private static final String EMPTY_POTION = "minecraft:empty";

   public FixProjectileStoredItem(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ENTITY);
      Type<?> $$1 = this.getOutputSchema().getType(References.ENTITY);
      return this.fixTypeEverywhereTyped(
         "Fix AbstractArrow item type",
         $$0,
         $$1,
         ExtraDataFixUtils.chainAllFilters(
            this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked),
            this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow),
            this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)
         )
      );
   }

   private Function<Typed<?>, Typed<?>> fixChoice(String $$0, FixProjectileStoredItem.SubFixer<?> $$1) {
      Type<?> $$2 = this.getInputSchema().getChoiceType(References.ENTITY, $$0);
      Type<?> $$3 = this.getOutputSchema().getChoiceType(References.ENTITY, $$0);
      return fixChoiceCap($$0, $$1, $$2, $$3);
   }

   private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String $$0, FixProjectileStoredItem.SubFixer<?> $$1, Type<?> $$2, Type<T> $$3) {
      OpticFinder<?> $$4 = DSL.namedChoice($$0, $$2);
      return $$3x -> $$3x.updateTyped($$4, $$3, $$2xx -> $$1.fix($$2xx, $$3));
   }

   private static <T> Typed<T> fixArrow(Typed<?> $$0, Type<T> $$1) {
      return net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, $$1, $$0x -> $$0x.set("item", createItemStack($$0x, getArrowType($$0x))));
   }

   private static String getArrowType(Dynamic<?> $$0) {
      return $$0.get("Potion").asString("minecraft:empty").equals("minecraft:empty") ? "minecraft:arrow" : "minecraft:tipped_arrow";
   }

   private static <T> Typed<T> fixSpectralArrow(Typed<?> $$0, Type<T> $$1) {
      return net.minecraft.util.Util.writeAndReadTypedOrThrow($$0, $$1, $$0x -> $$0x.set("item", createItemStack($$0x, "minecraft:spectral_arrow")));
   }

   private static Dynamic<?> createItemStack(Dynamic<?> $$0, String $$1) {
      return $$0.createMap(ImmutableMap.of($$0.createString("id"), $$0.createString($$1), $$0.createString("Count"), $$0.createInt(1)));
   }

   private static <T> Typed<T> castUnchecked(Typed<?> $$0, Type<T> $$1) {
      return new Typed($$1, $$0.getOps(), $$0.getValue());
   }

   interface SubFixer<F> {
      Typed<F> fix(Typed<?> var1, Type<F> var2);
   }
}
