package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ConcretePowderBlock extends FallingBlock {
   public static final MapCodec<ConcretePowderBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("concrete").forGetter($$0x -> $$0x.concrete), propertiesCodec())
         .apply($$0, ConcretePowderBlock::new)
   );
   private final Block concrete;

   @Override
   public MapCodec<ConcretePowderBlock> codec() {
      return CODEC;
   }

   public ConcretePowderBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.concrete = $$0;
   }

   @Override
   public void onLand(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, BlockState $$3, FallingBlockEntity $$4) {
      if (shouldSolidify($$0, $$1, $$3)) {
         $$0.setBlock($$1, this.concrete.defaultBlockState(), 3);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      net.minecraft.world.level.BlockGetter $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockState $$3 = $$1.getBlockState($$2);
      return shouldSolidify($$1, $$2, $$3) ? this.concrete.defaultBlockState() : super.getStateForPlacement($$0);
   }

   private static boolean shouldSolidify(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2) {
      return canSolidify($$2) || touchesLiquid($$0, $$1);
   }

   private static boolean touchesLiquid(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      boolean $$2 = false;
      MutableBlockPos $$3 = $$1.mutable();

      for (Direction $$4 : Direction.values()) {
         BlockState $$5 = $$0.getBlockState($$3);
         if ($$4 != Direction.DOWN || canSolidify($$5)) {
            $$3.setWithOffset($$1, $$4);
            $$5 = $$0.getBlockState($$3);
            if (canSolidify($$5) && !$$5.isFaceSturdy($$0, $$1, $$4.getOpposite())) {
               $$2 = true;
               break;
            }
         }
      }

      return $$2;
   }

   private static boolean canSolidify(BlockState $$0) {
      return $$0.getFluidState().is(FluidTags.WATER);
   }

   @Override
   protected BlockState updateShape(
      BlockState $$0,
      net.minecraft.world.level.LevelReader $$1,
      net.minecraft.world.level.ScheduledTickAccess $$2,
      BlockPos $$3,
      Direction $$4,
      BlockPos $$5,
      BlockState $$6,
      RandomSource $$7
   ) {
      return touchesLiquid($$1, $$3) ? this.concrete.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   public int getDustColor(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.getMapColor($$1, $$2).col;
   }
}
