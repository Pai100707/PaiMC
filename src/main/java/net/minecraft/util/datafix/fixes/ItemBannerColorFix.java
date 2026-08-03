package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix extends DataFix {
   public ItemBannerColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.ITEM_STACK);
      OpticFinder<Pair<String, String>> $$1 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
      OpticFinder<?> $$2 = $$0.findField("tag");
      OpticFinder<?> $$3 = $$2.type().findField("BlockEntityTag");
      return this.fixTypeEverywhereTyped("ItemBannerColorFix", $$0, $$3x -> {
         Optional<Pair<String, String>> $$4 = $$3x.getOptional($$1);
         if ($$4.isPresent() && Objects.equals($$4.get().getSecond(), "minecraft:banner")) {
            Dynamic<?> $$5 = (Dynamic<?>)$$3x.get(DSL.remainderFinder());
            Optional<? extends Typed<?>> $$6 = $$3x.getOptionalTyped($$2);
            if ($$6.isPresent()) {
               Typed<?> $$7 = (Typed<?>)$$6.get();
               Optional<? extends Typed<?>> $$8 = $$7.getOptionalTyped($$3);
               if ($$8.isPresent()) {
                  Typed<?> $$9 = (Typed<?>)$$8.get();
                  Dynamic<?> $$10 = (Dynamic<?>)$$7.get(DSL.remainderFinder());
                  Dynamic<?> $$11 = (Dynamic<?>)$$9.getOrCreate(DSL.remainderFinder());
                  if ($$11.get("Base").asNumber().result().isPresent()) {
                     $$5 = $$5.set("Damage", $$5.createShort((short)($$11.get("Base").asInt(0) & 15)));
                     Optional<? extends Dynamic<?>> $$12 = $$10.get("display").result();
                     if ($$12.isPresent()) {
                        Dynamic<?> $$13 = (Dynamic<?>)$$12.get();
                        Dynamic<?> $$14 = $$13.createMap(ImmutableMap.of($$13.createString("Lore"), $$13.createList(Stream.of($$13.createString("(+NBT")))));
                        if (Objects.equals($$13, $$14)) {
                           return $$3x.set(DSL.remainderFinder(), $$5);
                        }
                     }

                     $$11.remove("Base");
                     return $$3x.set(DSL.remainderFinder(), $$5).set($$2, $$7.set($$3, $$9.set(DSL.remainderFinder(), $$11)));
                  }
               }
            }

            return $$3x.set(DSL.remainderFinder(), $$5);
         } else {
            return $$3x;
         }
      });
   }
}
