package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class AttachedToLeavesDecorator extends TreeDecorator {
   public static final MapCodec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter($$0x -> $$0x.probability),
            Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter($$0x -> $$0x.exclusionRadiusXZ),
            Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter($$0x -> $$0x.exclusionRadiusY),
            BlockStateProvider.CODEC.fieldOf("block_provider").forGetter($$0x -> $$0x.blockProvider),
            Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter($$0x -> $$0x.requiredEmptyBlocks),
            ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter($$0x -> $$0x.directions)
         )
         .apply($$0, AttachedToLeavesDecorator::new)
   );
   protected final float probability;
   protected final int exclusionRadiusXZ;
   protected final int exclusionRadiusY;
   protected final BlockStateProvider blockProvider;
   protected final int requiredEmptyBlocks;
   protected final List<Direction> directions;

   public AttachedToLeavesDecorator(float $$0, int $$1, int $$2, BlockStateProvider $$3, int $$4, List<Direction> $$5) {
      this.probability = $$0;
      this.exclusionRadiusXZ = $$1;
      this.exclusionRadiusY = $$2;
      this.blockProvider = $$3;
      this.requiredEmptyBlocks = $$4;
      this.directions = $$5;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      Set<BlockPos> $$1 = new HashSet<>();
      RandomSource $$2 = $$0.random();

      for (BlockPos $$3 : Util.shuffledCopy($$0.leaves(), $$2)) {
         Direction $$4 = (Direction)Util.getRandom(this.directions, $$2);
         BlockPos $$5 = $$3.relative($$4);
         if (!$$1.contains($$5) && $$2.nextFloat() < this.probability && this.hasRequiredEmptyBlocks($$0, $$3, $$4)) {
            BlockPos $$6 = $$5.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos $$7 = $$5.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

            for (BlockPos $$8 : BlockPos.betweenClosed($$6, $$7)) {
               $$1.add($$8.immutable());
            }

            $$0.setBlock($$5, this.blockProvider.getState($$2, $$5));
         }
      }
   }

   private boolean hasRequiredEmptyBlocks(TreeDecorator.Context $$0, BlockPos $$1, Direction $$2) {
      for (int $$3 = 1; $$3 <= this.requiredEmptyBlocks; $$3++) {
         BlockPos $$4 = $$1.relative($$2, $$3);
         if (!$$0.isAir($$4)) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.ATTACHED_TO_LEAVES;
   }
}
