package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;

public class DetectorRailBlock extends BaseRailBlock {
   public static final MapCodec<DetectorRailBlock> CODEC = simpleCodec(DetectorRailBlock::new);
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private static final int PRESSED_CHECK_PERIOD = 20;

   @Override
   public MapCodec<DetectorRailBlock> codec() {
      return CODEC;
   }

   public DetectorRailBlock(BlockBehaviour.Properties $$0) {
      super(true, $$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false).setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(WATERLOGGED, false));
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (!$$1.isClientSide()) {
         if (!$$0.getValue(POWERED)) {
            this.checkPressed($$1, $$2, $$0);
         }
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(POWERED)) {
         this.checkPressed($$1, $$2, $$0);
      }
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(POWERED) ? 15 : 0;
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      if (!$$0.getValue(POWERED)) {
         return 0;
      } else {
         return $$3 == Direction.UP ? 15 : 0;
      }
   }

   private void checkPressed(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      if (this.canSurvive($$2, $$0, $$1)) {
         boolean $$3 = $$2.getValue(POWERED);
         boolean $$4 = false;
         List<AbstractMinecart> $$5 = this.getInteractingMinecartOfType($$0, $$1, AbstractMinecart.class, $$0x -> true);
         if (!$$5.isEmpty()) {
            $$4 = true;
         }

         if ($$4 && !$$3) {
            BlockState $$6 = $$2.setValue(POWERED, true);
            $$0.setBlock($$1, $$6, 3);
            this.updatePowerToConnected($$0, $$1, $$6, true);
            $$0.updateNeighborsAt($$1, this);
            $$0.updateNeighborsAt($$1.below(), this);
            $$0.setBlocksDirty($$1, $$2, $$6);
         }

         if (!$$4 && $$3) {
            BlockState $$7 = $$2.setValue(POWERED, false);
            $$0.setBlock($$1, $$7, 3);
            this.updatePowerToConnected($$0, $$1, $$7, false);
            $$0.updateNeighborsAt($$1, this);
            $$0.updateNeighborsAt($$1.below(), this);
            $$0.setBlocksDirty($$1, $$2, $$7);
         }

         if ($$4) {
            $$0.scheduleTick($$1, this, 20);
         }

         $$0.updateNeighbourForOutputSignal($$1, this);
      }
   }

   protected void updatePowerToConnected(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      RailState $$4 = new RailState($$0, $$1, $$2);

      for (BlockPos $$6 : $$4.getConnections()) {
         BlockState $$7 = $$0.getBlockState($$6);
         $$0.neighborChanged($$7, $$6, $$7.getBlock(), null, false);
      }
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         BlockState $$5 = this.updateState($$0, $$1, $$2, $$4);
         this.checkPressed($$1, $$2, $$5);
      }
   }

   @Override
   public Property<RailShape> getShapeProperty() {
      return SHAPE;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      if ($$0.getValue(POWERED)) {
         List<MinecartCommandBlock> $$4 = this.getInteractingMinecartOfType($$1, $$2, MinecartCommandBlock.class, $$0x -> true);
         if (!$$4.isEmpty()) {
            return $$4.get(0).getCommandBlock().getSuccessCount();
         }

         List<AbstractMinecart> $$5 = this.getInteractingMinecartOfType($$1, $$2, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
         if (!$$5.isEmpty()) {
            return AbstractContainerMenu.getRedstoneSignalFromContainer((Container)$$5.get(0));
         }
      }

      return 0;
   }

   private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(
      net.minecraft.world.level.Level $$0, BlockPos $$1, Class<T> $$2, Predicate<Entity> $$3
   ) {
      return (List<T>)$$0.getEntitiesOfClass($$2, this.getSearchBB($$1), $$3);
   }

   private AABB getSearchBB(BlockPos $$0) {
      double $$1 = 0.2;
      return new AABB($$0.getX() + 0.2, $$0.getY(), $$0.getZ() + 0.2, $$0.getX() + 1 - 0.2, $$0.getY() + 1 - 0.2, $$0.getZ() + 1 - 0.2);
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      RailShape $$2 = $$0.getValue(SHAPE);
      RailShape $$3 = this.rotate($$2, $$1);
      return $$0.setValue(SHAPE, $$3);
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      RailShape $$2 = $$0.getValue(SHAPE);
      RailShape $$3 = this.mirror($$2, $$1);
      return $$0.setValue(SHAPE, $$3);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(SHAPE, POWERED, WATERLOGGED);
   }
}
