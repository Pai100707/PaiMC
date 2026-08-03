package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MatchingBlockTagPredicate extends StateTestingPredicate {
   final TagKey<Block> tag;
   public static final MapCodec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> stateTestingCodec($$0).and(TagKey.codec(Registries.BLOCK).fieldOf("tag").forGetter($$0x -> $$0x.tag)).apply($$0, MatchingBlockTagPredicate::new)
   );

   protected MatchingBlockTagPredicate(Vec3i $$0, TagKey<Block> $$1) {
      super($$0);
      this.tag = $$1;
   }

   @Override
   protected boolean test(BlockState $$0) {
      return $$0.is(this.tag);
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_BLOCK_TAG;
   }
}
