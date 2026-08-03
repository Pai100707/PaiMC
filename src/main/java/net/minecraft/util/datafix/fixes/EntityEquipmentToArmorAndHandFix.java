package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class EntityEquipmentToArmorAndHandFix extends DataFix {
   public EntityEquipmentToArmorAndHandFix(Schema $$0) {
      super($$0, true);
   }

   public TypeRewriteRule makeRule() {
      return this.cap(this.getInputSchema().getTypeRaw(References.ITEM_STACK), this.getOutputSchema().getTypeRaw(References.ITEM_STACK));
   }

   private <ItemStackOld, ItemStackNew> TypeRewriteRule cap(Type<ItemStackOld> $$0, Type<ItemStackNew> $$1) {
      Type<Pair<String, Either<List<ItemStackOld>, com.mojang.datafixers.util.Unit>>> $$2 = DSL.named(
         References.ENTITY_EQUIPMENT.typeName(), DSL.optional(DSL.field("Equipment", DSL.list($$0)))
      );
      Type<Pair<String, Pair<Either<List<ItemStackNew>, com.mojang.datafixers.util.Unit>, Pair<Either<List<ItemStackNew>, com.mojang.datafixers.util.Unit>, Pair<Either<ItemStackNew, com.mojang.datafixers.util.Unit>, Either<ItemStackNew, com.mojang.datafixers.util.Unit>>>>>> $$3 = DSL.named(
         References.ENTITY_EQUIPMENT.typeName(),
         DSL.and(
            DSL.optional(DSL.field("ArmorItems", DSL.list($$1))),
            DSL.optional(DSL.field("HandItems", DSL.list($$1))),
            DSL.optional(DSL.field("body_armor_item", $$1)),
            DSL.optional(DSL.field("saddle", $$1))
         )
      );
      if (!$$2.equals(this.getInputSchema().getType(References.ENTITY_EQUIPMENT))) {
         throw new IllegalStateException("Input entity_equipment type does not match expected");
      } else if (!$$3.equals(this.getOutputSchema().getType(References.ENTITY_EQUIPMENT))) {
         throw new IllegalStateException("Output entity_equipment type does not match expected");
      } else {
         return TypeRewriteRule.seq(
            this.fixTypeEverywhereTyped(
               "EntityEquipmentToArmorAndHandFix - drop chances",
               this.getInputSchema().getType(References.ENTITY),
               $$0x -> $$0x.update(DSL.remainderFinder(), EntityEquipmentToArmorAndHandFix::fixDropChances)
            ),
            this.fixTypeEverywhere(
               "EntityEquipmentToArmorAndHandFix - equipment",
               $$2,
               $$3,
               $$1x -> {
                  ItemStackNew $$2x = (ItemStackNew)((Pair)$$1.read(new Dynamic($$1x).emptyMap())
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Could not parse newly created empty itemstack.")))
                     .getFirst();
                  Either<ItemStackNew, com.mojang.datafixers.util.Unit> $$3x = Either.right(DSL.unit());
                  return $$2xx -> $$2xx.mapSecond($$2xxx -> {
                     List<ItemStackOld> $$3xx = (List<ItemStackOld>)$$2xxx.map(Function.identity(), $$0xxxx -> List.of());
                     Either<List<ItemStackNew>, com.mojang.datafixers.util.Unit> $$4 = Either.right(DSL.unit());
                     Either<List<ItemStackNew>, com.mojang.datafixers.util.Unit> $$5 = Either.right(DSL.unit());
                     if (!$$3xx.isEmpty()) {
                        $$4 = Either.left(Lists.newArrayList(new Object[]{$$3xx.getFirst(), $$2x}));
                     }

                     if ($$3xx.size() > 1) {
                        List<ItemStackNew> $$6 = Lists.newArrayList(new Object[]{$$2x, $$2x, $$2x, $$2x});

                        for (int $$7 = 1; $$7 < Math.min($$3xx.size(), 5); $$7++) {
                           $$6.set($$7 - 1, (ItemStackNew)$$3xx.get($$7));
                        }

                        $$5 = Either.left($$6);
                     }

                     return Pair.of($$5, Pair.of($$4, Pair.of($$3x, $$3x)));
                  });
               }
            )
         );
      }
   }

   private static Dynamic<?> fixDropChances(Dynamic<?> $$0) {
      Optional<? extends Stream<? extends Dynamic<?>>> $$1 = $$0.get("DropChances").asStreamOpt().result();
      $$0 = $$0.remove("DropChances");
      if ($$1.isPresent()) {
         Iterator<Float> $$2 = Stream.concat($$1.get().map($$0x -> $$0x.asFloat(0.0F)), Stream.generate(() -> 0.0F)).iterator();
         float $$3 = $$2.next();
         if ($$0.get("HandDropChances").result().isEmpty()) {
            $$0 = $$0.set("HandDropChances", $$0.createList(Stream.of($$3, 0.0F).map($$0::createFloat)));
         }

         if ($$0.get("ArmorDropChances").result().isEmpty()) {
            $$0 = $$0.set("ArmorDropChances", $$0.createList(Stream.of($$2.next(), $$2.next(), $$2.next(), $$2.next()).map($$0::createFloat)));
         }
      }

      return $$0;
   }
}
