package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;

class AnyOfPredicate extends CombiningPredicate {
   public static final MapCodec<AnyOfPredicate> CODEC = codec(AnyOfPredicate::new);

   public AnyOfPredicate(List<BlockPredicate> $$0) {
      super($$0);
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      for (BlockPredicate $$2 : this.predicates) {
         if ($$2.test($$0, $$1)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.ANY_OF;
   }
}
