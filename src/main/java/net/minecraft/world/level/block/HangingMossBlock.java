package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HangingMossBlock extends Block implements BonemealableBlock {
   public static final MapCodec<HangingMossBlock> CODEC = simpleCodec(HangingMossBlock::new);
   private static final VoxelShape SHAPE_BASE = Block.column(14.0, 0.0, 16.0);
   private static final VoxelShape SHAPE_TIP = Block.column(14.0, 2.0, 16.0);
   public static final BooleanProperty TIP = BlockStateProperties.TIP;

   @Override
   public MapCodec<HangingMossBlock> codec() {
      return CODEC;
   }

   public HangingMossBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(TIP, true));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$0.getValue(TIP) ? SHAPE_TIP : SHAPE_BASE;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(500) == 0) {
         BlockState $$4 = $$1.getBlockState($$2.above());
         if ($$4.is(BlockTags.PALE_OAK_LOGS) || $$4.is(Blocks.PALE_OAK_LEAVES)) {
            $$1.playLocalSound($$2.getX(), $$2.getY(), $$2.getZ(), SoundEvents.PALE_HANGING_MOSS_IDLE, SoundSource.AMBIENT, 1.0F, 1.0F, false);
         }
      }
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return this.canStayAtPosition($$1, $$2);
   }

   private boolean canStayAtPosition(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.relative(Direction.UP);
      BlockState $$3 = $$0.getBlockState($$2);
      return MultifaceBlock.canAttachTo($$0, Direction.UP, $$2, $$3) || $$3.is(Blocks.PALE_HANGING_MOSS);
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
      if (!this.canStayAtPosition($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      return $$0.setValue(TIP, !$$1.getBlockState($$3.below()).is(this));
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!this.canStayAtPosition($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(TIP);
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return this.canGrowInto($$0.getBlockState(this.getTip($$0, $$1).below()));
   }

   private boolean canGrowInto(BlockState $$0) {
      return $$0.isAir();
   }

   public BlockPos getTip(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      MutableBlockPos $$2 = $$1.mutable();

      BlockState $$3;
      do {
         $$2.move(Direction.DOWN);
         $$3 = $$0.getBlockState($$2);
      } while ($$3.is(this));

      return $$2.relative(Direction.UP).immutable();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      BlockPos $$4 = this.getTip($$0, $$2).below();
      if (this.canGrowInto($$0.getBlockState($$4))) {
         $$0.setBlockAndUpdate($$4, $$3.setValue(TIP, true));
      }
   }
}
