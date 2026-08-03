package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;

public class EntityRedundantChanceTagsFix extends DataFix {
   private static final Codec<List<Float>> FLOAT_LIST_CODEC = Codec.FLOAT.listOf();

   public EntityRedundantChanceTagsFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   public TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "EntityRedundantChanceTagsFix", this.getInputSchema().getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), $$0x -> {
            if (isZeroList($$0x.get("HandDropChances"), 2)) {
               $$0x = $$0x.remove("HandDropChances");
            }

            if (isZeroList($$0x.get("ArmorDropChances"), 4)) {
               $$0x = $$0x.remove("ArmorDropChances");
            }

            return $$0x;
         })
      );
   }

   private static boolean isZeroList(OptionalDynamic<?> $$0, int $$1) {
      return $$0.flatMap(FLOAT_LIST_CODEC::parse).map($$1x -> $$1x.size() == $$1 && $$1x.stream().allMatch($$0xx -> $$0xx == 0.0F)).result().orElse(false);
   }
}
