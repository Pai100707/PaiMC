package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;

class TrueBlockPredicate implements BlockPredicate {
   public static TrueBlockPredicate INSTANCE = new TrueBlockPredicate();
   public static final MapCodec<TrueBlockPredicate> CODEC = MapCodec.unit(() -> INSTANCE);

   private TrueBlockPredicate() {
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return true;
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.TRUE;
   }
}
