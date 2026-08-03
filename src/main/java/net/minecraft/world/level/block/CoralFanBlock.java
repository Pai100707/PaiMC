package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class CoralFanBlock extends BaseCoralFanBlock {
   public static final MapCodec<CoralFanBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(CoralBlock.DEAD_CORAL_FIELD.forGetter($$0x -> $$0x.deadBlock), propertiesCodec()).apply($$0, CoralFanBlock::new)
   );
   private final Block deadBlock;

   @Override
   public MapCodec<CoralFanBlock> codec() {
      return CODEC;
   }

   protected CoralFanBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.deadBlock = $$0;
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      this.tryScheduleDieTick($$0, $$1, $$1, $$1.random, $$2);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!scanForWater($$0, $$1, $$2)) {
         $$1.setBlock($$2, this.deadBlock.defaultBlockState().setValue(WATERLOGGED, false), 2);
      }
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
      if ($$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         this.tryScheduleDieTick($$0, $$1, $$2, $$7, $$3);
         if ($$0.getValue(WATERLOGGED)) {
            $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
         }

         return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
      }
   }
}
