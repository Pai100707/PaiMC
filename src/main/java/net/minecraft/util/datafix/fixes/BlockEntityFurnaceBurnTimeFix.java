package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityFurnaceBurnTimeFix extends NamedEntityFix {
   public BlockEntityFurnaceBurnTimeFix(Schema $$0, String $$1) {
      super($$0, false, "BlockEntityFurnaceBurnTimeFix" + $$1, References.BLOCK_ENTITY, $$1);
   }

   public Dynamic<?> fixBurnTime(Dynamic<?> $$0) {
      $$0 = $$0.renameField("CookTime", "cooking_time_spent");
      $$0 = $$0.renameField("CookTimeTotal", "cooking_total_time");
      $$0 = $$0.renameField("BurnTime", "lit_time_remaining");
      return $$0.setFieldIfPresent("lit_total_time", $$0.get("lit_time_remaining").result());
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixBurnTime);
   }
}
