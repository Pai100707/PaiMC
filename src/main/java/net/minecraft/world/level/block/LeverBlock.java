package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LeverBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final MapCodec<LeverBlock> CODEC = simpleCodec(LeverBlock::new);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<LeverBlock> codec() {
      return CODEC;
   }

   protected LeverBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(FACE, AttachFace.WALL));
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      Map<AttachFace, Map<Direction, VoxelShape>> $$0 = Shapes.rotateAttachFace(Block.boxZ(6.0, 8.0, 10.0, 16.0));
      return this.getShapeForEachState($$1 -> $$0.get($$1.getValue(FACE)).get($$1.getValue(FACING)), new Property[]{POWERED});
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1.isClientSide()) {
         BlockState $$5 = $$0.cycle(POWERED);
         if ($$5.getValue(POWERED)) {
            makeParticle($$5, $$1, $$2, 1.0F);
         }
      } else {
         this.pull($$0, $$1, $$2, null);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$3.canTriggerBlocks()) {
         this.pull($$0, $$1, $$2, null);
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }

   public void pull(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3) {
      $$0 = $$0.cycle(POWERED);
      $$1.setBlock($$2, $$0, 3);
      this.updateNeighbours($$0, $$1, $$2);
      playSound($$3, $$1, $$2, $$0);
      $$1.gameEvent($$3, $$0.getValue(POWERED) ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, $$2);
   }

   protected static void playSound(Player $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      float $$4 = $$3.getValue(POWERED) ? 0.6F : 0.5F;
      $$1.playSound($$0, $$2, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, $$4);
   }

   private static void makeParticle(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, float $$3) {
      Direction $$4 = ((Direction)$$0.getValue(FACING)).getOpposite();
      Direction $$5 = getConnectedDirection($$0).getOpposite();
      double $$6 = $$2.getX() + 0.5 + 0.1 * $$4.getStepX() + 0.2 * $$5.getStepX();
      double $$7 = $$2.getY() + 0.5 + 0.1 * $$4.getStepY() + 0.2 * $$5.getStepY();
      double $$8 = $$2.getZ() + 0.5 + 0.1 * $$4.getStepZ() + 0.2 * $$5.getStepZ();
      $$1.addParticle(new DustParticleOptions(16711680, $$3), $$6, $$7, $$8, 0.0, 0.0, 0.0);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(POWERED) && $$3.nextFloat() < 0.25F) {
         makeParticle($$0, $$1, $$2, 0.5F);
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3 && $$0.getValue(POWERED)) {
         this.updateNeighbours($$0, $$1, $$2);
      }
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWERED) ? 15 : 0;
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWERED) && getConnectedDirection($$0) == $$3 ? 15 : 0;
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   private void updateNeighbours(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      Direction $$3 = getConnectedDirection($$0).getOpposite();
      Orientation $$4 = ExperimentalRedstoneUtils.initialOrientation($$1, $$3, $$3.getAxis().isHorizontal() ? Direction.UP : $$0.getValue(FACING));
      $$1.updateNeighborsAt($$2, this, $$4);
      $$1.updateNeighborsAt($$2.relative($$3), this, $$4);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACE, FACING, POWERED);
   }
}
