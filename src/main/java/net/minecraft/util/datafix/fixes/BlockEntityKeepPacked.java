package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class BlockEntityKeepPacked extends NamedEntityFix {
   public BlockEntityKeepPacked(Schema $$0, boolean $$1) {
      super($$0, $$1, "BlockEntityKeepPacked", References.BLOCK_ENTITY, "DUMMY");
   }

   private static Dynamic<?> fixTag(Dynamic<?> $$0) {
      return $$0.set("keepPacked", $$0.createBoolean(true));
   }

   @Override
   protected Typed<?> fix(Typed<?> $$0) {
      return $$0.update(DSL.remainderFinder(), BlockEntityKeepPacked::fixTag);
   }
}
