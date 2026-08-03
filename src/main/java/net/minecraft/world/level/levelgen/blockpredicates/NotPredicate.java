package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

class NotPredicate implements BlockPredicate {
   public static final MapCodec<NotPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BlockPredicate.CODEC.fieldOf("predicate").forGetter($$0x -> $$0x.predicate)).apply($$0, NotPredicate::new)
   );
   private final BlockPredicate predicate;

   public NotPredicate(BlockPredicate $$0) {
      this.predicate = $$0;
   }

   public boolean test(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1) {
      return !this.predicate.test($$0, $$1);
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.NOT;
   }
}
