package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock extends Block implements EntityBlock, GameMasterBlock {
   public static final MapCodec<JigsawBlock> CODEC = simpleCodec(JigsawBlock::new);
   public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

   @Override
   public MapCodec<JigsawBlock> codec() {
      return CODEC;
   }

   protected JigsawBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, FrontAndTop.NORTH_UP));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(ORIENTATION);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(ORIENTATION, $$1.rotation().rotate($$0.getValue(ORIENTATION)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.setValue(ORIENTATION, $$1.rotation().rotate($$0.getValue(ORIENTATION)));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      Direction $$1 = $$0.getClickedFace();
      Direction $$2;
      if ($$1.getAxis() == Axis.Y) {
         $$2 = $$0.getHorizontalDirection().getOpposite();
      } else {
         $$2 = Direction.UP;
      }

      return this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop($$1, $$2));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new JigsawBlockEntity($$0, $$1);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      BlockEntity $$5 = $$1.getBlockEntity($$2);
      if ($$5 instanceof JigsawBlockEntity && $$3.canUseGameMasterBlocks()) {
         $$3.openJigsawBlock((JigsawBlockEntity)$$5);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static boolean canAttach(StructureTemplate.JigsawBlockInfo $$0, StructureTemplate.JigsawBlockInfo $$1) {
      Direction $$2 = getFrontFacing($$0.info().state());
      Direction $$3 = getFrontFacing($$1.info().state());
      Direction $$4 = getTopFacing($$0.info().state());
      Direction $$5 = getTopFacing($$1.info().state());
      JigsawBlockEntity.JointType $$6 = $$0.jointType();
      boolean $$7 = $$6 == JigsawBlockEntity.JointType.ROLLABLE;
      return $$2 == $$3.getOpposite() && ($$7 || $$4 == $$5) && $$0.target().equals($$1.name());
   }

   public static Direction getFrontFacing(BlockState $$0) {
      return ((FrontAndTop)$$0.getValue(ORIENTATION)).front();
   }

   public static Direction getTopFacing(BlockState $$0) {
      return ((FrontAndTop)$$0.getValue(ORIENTATION)).top();
   }
}
