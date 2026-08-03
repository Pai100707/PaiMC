package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
   private final HolderSet<Block> blocks;
   public static final MapCodec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> stateTestingCodec($$0)
         .and(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter($$0x -> $$0x.blocks))
         .apply($$0, MatchingBlocksPredicate::new)
   );

   public MatchingBlocksPredicate(Vec3i $$0, HolderSet<Block> $$1) {
      super($$0);
      this.blocks = $$1;
   }

   @Override
   protected boolean test(BlockState $$0) {
      return $$0.is(this.blocks);
   }

   @Override
   public BlockPredicateType<?> type() {
      return BlockPredicateType.MATCHING_BLOCKS;
   }
}
