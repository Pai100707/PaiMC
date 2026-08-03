package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityBannerColorFix extends NamedEntityFix {
   public BlockEntityBannerColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "BlockEntityBannerColorFix", References.BLOCK_ENTITY, "minecraft:banner");
   }

   public Dynamic<?> fixTag(Dynamic<?> $$0) {
      $$0 = $$0.update("Base", $$0x -> $$0x.createInt(15 - $$0x.asInt(0)));
      return $$0.update(
         "Patterns",
         $$0x -> (Dynamic)DataFixUtils.orElse(
            $$0x.asStreamOpt()
               .map($$0xx -> $$0xx.map($$0xxx -> $$0xxx.update("Color", $$0xxxx -> $$0xxxx.createInt(15 - $$0xxxx.asInt(0)))))
               .map($$0x::createList)
               .result(),
            $$0x
         )
      );
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), this::fixTag);
   }
}
