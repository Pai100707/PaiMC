package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class PlaceOnGroundDecorator extends TreeDecorator {
   public static final MapCodec<PlaceOnGroundDecorator> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(128).forGetter($$0x -> $$0x.tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").orElse(2).forGetter($$0x -> $$0x.radius),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height").orElse(1).forGetter($$0x -> $$0x.height),
            BlockStateProvider.CODEC.fieldOf("block_state_provider").forGetter($$0x -> $$0x.blockStateProvider)
         )
         .apply($$0, PlaceOnGroundDecorator::new)
   );
   private final int tries;
   private final int radius;
   private final int height;
   private final BlockStateProvider blockStateProvider;

   public PlaceOnGroundDecorator(int $$0, int $$1, int $$2, BlockStateProvider $$3) {
      this.tries = $$0;
      this.radius = $$1;
      this.height = $$2;
      this.blockStateProvider = $$3;
   }

   @Override
   protected TreeDecoratorType<?> type() {
      return TreeDecoratorType.PLACE_ON_GROUND;
   }

   @Override
   public void place(TreeDecorator.Context $$0) {
      List<BlockPos> $$1 = TreeFeature.getLowestTrunkOrRootOfTree($$0);
      if (!$$1.isEmpty()) {
         BlockPos $$2 = $$1.getFirst();
         int $$3 = $$2.getY();
         int $$4 = $$2.getX();
         int $$5 = $$2.getX();
         int $$6 = $$2.getZ();
         int $$7 = $$2.getZ();

         for (BlockPos $$8 : $$1) {
            if ($$8.getY() == $$3) {
               $$4 = Math.min($$4, $$8.getX());
               $$5 = Math.max($$5, $$8.getX());
               $$6 = Math.min($$6, $$8.getZ());
               $$7 = Math.max($$7, $$8.getZ());
            }
         }

         RandomSource $$9 = $$0.random();
         BoundingBox $$10 = new BoundingBox($$4, $$3, $$6, $$5, $$3, $$7).inflatedBy(this.radius, this.height, this.radius);
         MutableBlockPos $$11 = new MutableBlockPos();

         for (int $$12 = 0; $$12 < this.tries; $$12++) {
            $$11.set(
               $$9.nextIntBetweenInclusive($$10.minX(), $$10.maxX()),
               $$9.nextIntBetweenInclusive($$10.minY(), $$10.maxY()),
               $$9.nextIntBetweenInclusive($$10.minZ(), $$10.maxZ())
            );
            this.attemptToPlaceBlockAbove($$0, $$11);
         }
      }
   }

   private void attemptToPlaceBlockAbove(TreeDecorator.Context $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.above();
      if ($$0.level().isStateAtPosition($$2, $$0x -> $$0x.isAir() || $$0x.is(Blocks.VINE))
         && $$0.checkBlock($$1, BlockBehaviour.BlockStateBase::isSolidRender)
         && $$0.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, $$1).getY() <= $$2.getY()) {
         $$0.setBlock($$2, this.blockStateProvider.getState($$0.random(), $$2));
      }
   }
}
