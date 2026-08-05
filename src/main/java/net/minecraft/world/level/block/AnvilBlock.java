package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AnvilBlock extends FallingBlock {
   public static final MapCodec<AnvilBlock> CODEC = simpleCodec(AnvilBlock::new);
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   private static final Map<Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(
      Shapes.or(
         Block.column(12.0, 0.0, 4.0),
         new VoxelShape[]{Block.column(8.0, 10.0, 4.0, 5.0), Block.column(4.0, 8.0, 5.0, 10.0), Block.column(10.0, 16.0, 10.0, 16.0)}
      )
   );
   private static final Component CONTAINER_TITLE = Component.translatable("container.repair");
   private static final float FALL_DAMAGE_PER_DISTANCE = 2.0F;
   private static final int FALL_DAMAGE_MAX = 40;

   @Override
   public MapCodec<AnvilBlock> codec() {
      return CODEC;
   }

   public AnvilBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getHorizontalDirection().getClockWise());
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide()) {
         $$3.openMenu($$0.getMenuProvider($$1, $$2));
         $$3.awardStat(Stats.INTERACT_WITH_ANVIL);
      }

      return InteractionResult.SUCCESS;
   }

   
   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      return new SimpleMenuProvider(($$2x, $$3, $$4) -> new AnvilMenu($$2x, $$3, ContainerLevelAccess.create($$1, $$2)), CONTAINER_TITLE);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES.get(((Direction)$$0.getValue(FACING)).getAxis());
   }

   @Override
   protected void falling(FallingBlockEntity $$0) {
      $$0.setHurtsEntities(2.0F, 40);
   }

   @Override
   public void onLand(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, BlockState $$3, FallingBlockEntity $$4) {
      if (!$$4.isSilent()) {
         $$0.levelEvent(1031, $$1, 0);
      }
   }

   @Override
   public void onBrokenAfterFall(net.minecraft.world.level.Level $$0, BlockPos $$1, FallingBlockEntity $$2) {
      if (!$$2.isSilent()) {
         $$0.levelEvent(1029, $$1, 0);
      }
   }

   @Override
   public DamageSource getFallDamageSource(Entity $$0) {
      return $$0.damageSources().anvil($$0);
   }

   
   public static BlockState damage(BlockState $$0) {
      if ($$0.is(Blocks.ANVIL)) {
         return Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(FACING, (Direction)$$0.getValue(FACING));
      } else {
         return $$0.is(Blocks.CHIPPED_ANVIL) ? Blocks.DAMAGED_ANVIL.defaultBlockState().setValue(FACING, (Direction)$$0.getValue(FACING)) : null;
      }
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   public int getDustColor(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.getMapColor($$1, $$2).col;
   }
}
