package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;

public class BlockEntityShulkerBoxColorFix extends NamedEntityFix {
   public BlockEntityShulkerBoxColorFix(Schema $$0, boolean $$1) {
      super($$0, $$1, "BlockEntityShulkerBoxColorFix", References.BLOCK_ENTITY, "minecraft:shulker_box");
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), $$0x -> $$0x.remove("Color"));
   }
}
