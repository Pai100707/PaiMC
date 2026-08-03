package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SaddleEquipmentSlotFix extends DataFix {
   private static final Set<String> ENTITIES_WITH_SADDLE_ITEM = Set.of(
      "minecraft:horse",
      "minecraft:skeleton_horse",
      "minecraft:zombie_horse",
      "minecraft:donkey",
      "minecraft:mule",
      "minecraft:camel",
      "minecraft:llama",
      "minecraft:trader_llama"
   );
   private static final Set<String> ENTITIES_WITH_SADDLE_FLAG = Set.of("minecraft:pig", "minecraft:strider");
   private static final String SADDLE_FLAG = "Saddle";
   private static final String NEW_SADDLE = "saddle";

   public SaddleEquipmentSlotFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      TaggedChoiceType<String> $$0 = this.getInputSchema().findChoiceType(References.ENTITY);
      OpticFinder<Pair<String, ?>> $$1 = DSL.typeFinder($$0);
      Type<?> $$2 = this.getInputSchema().getType(References.ENTITY);
      Type<?> $$3 = this.getOutputSchema().getType(References.ENTITY);
      Type<?> $$4 = ExtraDataFixUtils.patchSubType($$2, $$2, $$3);
      return this.fixTypeEverywhereTyped(
         "SaddleEquipmentSlotFix",
         $$2,
         $$3,
         $$3x -> {
            String $$4x = $$3x.getOptional($$1).<String>map(Pair::getFirst).map(NamespacedSchema::ensureNamespaced).orElse("");
            Typed<?> $$5 = ExtraDataFixUtils.cast($$4, $$3x);
            if (ENTITIES_WITH_SADDLE_ITEM.contains($$4x)) {
               return net.minecraft.util.Util.writeAndReadTypedOrThrow($$5, $$3, SaddleEquipmentSlotFix::fixEntityWithSaddleItem);
            } else {
               return ENTITIES_WITH_SADDLE_FLAG.contains($$4x)
                  ? net.minecraft.util.Util.writeAndReadTypedOrThrow($$5, $$3, SaddleEquipmentSlotFix::fixEntityWithSaddleFlag)
                  : ExtraDataFixUtils.cast($$3, $$3x);
            }
         }
      );
   }

   private static Dynamic<?> fixEntityWithSaddleItem(Dynamic<?> $$0) {
      return $$0.get("SaddleItem").result().isEmpty() ? $$0 : fixDropChances($$0.renameField("SaddleItem", "saddle"));
   }

   private static Dynamic<?> fixEntityWithSaddleFlag(Dynamic<?> $$0) {
      boolean $$1 = $$0.get("Saddle").asBoolean(false);
      $$0 = $$0.remove("Saddle");
      if (!$$1) {
         return $$0;
      } else {
         Dynamic<?> $$2 = $$0.emptyMap().set("id", $$0.createString("minecraft:saddle")).set("count", $$0.createInt(1));
         return fixDropChances($$0.set("saddle", $$2));
      }
   }

   private static Dynamic<?> fixDropChances(Dynamic<?> $$0) {
      Dynamic<?> $$1 = $$0.get("drop_chances").orElseEmptyMap().set("saddle", $$0.createFloat(2.0F));
      return $$0.set("drop_chances", $$1);
   }
}
