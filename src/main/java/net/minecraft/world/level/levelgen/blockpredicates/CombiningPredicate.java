package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;

abstract class CombiningPredicate implements BlockPredicate {
   protected final List<BlockPredicate> predicates;

   protected CombiningPredicate(List<BlockPredicate> $$0) {
      this.predicates = $$0;
   }

   public static <T extends CombiningPredicate> MapCodec<T> codec(Function<List<BlockPredicate>, T> $$0) {
      return RecordCodecBuilder.mapCodec(
         $$1 -> $$1.group(BlockPredicate.CODEC.listOf().fieldOf("predicates").forGetter($$0xx -> $$0xx.predicates)).apply($$1, $$0)
      );
   }
}
