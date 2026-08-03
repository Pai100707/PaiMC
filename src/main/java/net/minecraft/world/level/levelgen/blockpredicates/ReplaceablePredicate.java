package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

class ReplaceablePredicate extends StateTestingPredicate {
   public static final MapCodec<ReplaceablePredicate> CODEC = RecordCodecBuilder.mapCodec($$0 -> stateTestingCodec($$0).apply($$0, ReplaceablePredicate::new));

   public ReplaceablePredicate(Vec3i $$0) {
      super($$0);
   }

   @Override
   protected boolean test(BlockState $$0) {
      return $$0.canBeReplaced();
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.REPLACEABLE;
   }
}
