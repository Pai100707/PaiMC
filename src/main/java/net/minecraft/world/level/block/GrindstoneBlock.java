package net.minecraft.world.level.block;

import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GrindstoneBlock extends FaceAttachedHorizontalDirectionalBlock {
   public static final MapCodec<GrindstoneBlock> CODEC = simpleCodec(GrindstoneBlock::new);
   private static final Component CONTAINER_TITLE = Component.translatable("container.grindstone_title");
   private final Function<BlockState, VoxelShape> shapes;

   @Override
   public MapCodec<GrindstoneBlock> codec() {
      return CODEC;
   }

   protected GrindstoneBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      VoxelShape $$0 = Shapes.or(Block.box(2.0, 6.0, 7.0, 4.0, 10.0, 16.0), Block.box(2.0, 5.0, 3.0, 4.0, 11.0, 9.0));
      VoxelShape $$1 = Shapes.rotate($$0, OctahedralGroup.INVERT_X);
      VoxelShape $$2 = Shapes.or(Block.boxZ(8.0, 2.0, 14.0, 0.0, 12.0), new VoxelShape[]{$$0, $$1});
      Map<AttachFace, Map<Direction, VoxelShape>> $$3 = Shapes.rotateAttachFace($$2);
      return this.getShapeForEachState($$1x -> $$3.get($$1x.getValue(FACE)).get($$1x.getValue(FACING)));
   }

   private VoxelShape getVoxelShape(BlockState $$0) {
      return this.shapes.apply($$0);
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getVoxelShape($$0);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.getVoxelShape($$0);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return true;
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         $$3.openMenu($$0.getMenuProvider($$1, $$2));
         $$3.awardStat(Stats.INTERACT_WITH_GRINDSTONE);
      }

      return InteractionResult.SUCCESS;
   }

   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      return new SimpleMenuProvider(($$2x, $$3, $$4) -> new GrindstoneMenu($$2x, $$3, ContainerLevelAccess.create($$1, $$2)), CONTAINER_TITLE);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING, FACE);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
