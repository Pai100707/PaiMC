package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public class SolidPredicate extends StateTestingPredicate {
   public static final MapCodec<SolidPredicate> CODEC = RecordCodecBuilder.mapCodec($$0 -> stateTestingCodec($$0).apply($$0, SolidPredicate::new));

   public SolidPredicate(Vec3i $$0) {
      super($$0);
   }

   @Override
   protected boolean test(BlockState $$0) {
      return $$0.isSolid();
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.SOLID;
   }
}
