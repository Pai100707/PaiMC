package net.minecraft.world.level.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.EquipmentDispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DispenserBlock extends BaseEntityBlock {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final MapCodec<DispenserBlock> CODEC = simpleCodec(DispenserBlock::new);
   public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   private static final DefaultDispenseItemBehavior DEFAULT_BEHAVIOR = new DefaultDispenseItemBehavior();
   public static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = new IdentityHashMap<>();
   private static final int TRIGGER_DURATION = 4;

   @Override
   public MapCodec<? extends DispenserBlock> codec() {
      return CODEC;
   }

   public static void registerBehavior(net.minecraft.world.level.ItemLike $$0, DispenseItemBehavior $$1) {
      DISPENSER_REGISTRY.put($$0.asItem(), $$1);
   }

   public static void registerProjectileBehavior(net.minecraft.world.level.ItemLike $$0) {
      DISPENSER_REGISTRY.put($$0.asItem(), new ProjectileDispenseBehavior($$0.asItem()));
   }

   protected DispenserBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if (!$$1.isClientSide() && $$1.getBlockEntity($$2) instanceof DispenserBlockEntity $$5) {
         $$3.openMenu($$5);
         $$3.awardStat($$5 instanceof DropperBlockEntity ? Stats.INSPECT_DROPPER : Stats.INSPECT_DISPENSER);
      }

      return InteractionResult.SUCCESS;
   }

   protected void dispenseFrom(ServerLevel $$0, BlockState $$1, BlockPos $$2) {
      DispenserBlockEntity $$3 = (DispenserBlockEntity)$$0.getBlockEntity($$2, BlockEntityType.DISPENSER).orElse(null);
      if ($$3 == null) {
         LOGGER.warn("Ignoring dispensing attempt for Dispenser without matching block entity at {}", $$2);
      } else {
         BlockSource $$4 = new BlockSource($$0, $$2, $$1, $$3);
         int $$5 = $$3.getRandomSlot($$0.random);
         if ($$5 < 0) {
            $$0.levelEvent(1001, $$2, 0);
            $$0.gameEvent(GameEvent.BLOCK_ACTIVATE, $$2, GameEvent.Context.of($$3.getBlockState()));
         } else {
            ItemStack $$6 = $$3.getItem($$5);
            DispenseItemBehavior $$7 = this.getDispenseMethod($$0, $$6);
            if ($$7 != DispenseItemBehavior.NOOP) {
               $$3.setItem($$5, $$7.dispense($$4, $$6));
            }
         }
      }
   }

   protected DispenseItemBehavior getDispenseMethod(net.minecraft.world.level.Level $$0, ItemStack $$1) {
      if (!$$1.isItemEnabled($$0.enabledFeatures())) {
         return DEFAULT_BEHAVIOR;
      } else {
         DispenseItemBehavior $$2 = DISPENSER_REGISTRY.get($$1.getItem());
         return $$2 != null ? $$2 : getDefaultDispenseMethod($$1);
      }
   }

   private static DispenseItemBehavior getDefaultDispenseMethod(ItemStack $$0) {
      return (DispenseItemBehavior)($$0.has(DataComponents.EQUIPPABLE) ? EquipmentDispenseItemBehavior.INSTANCE : DEFAULT_BEHAVIOR);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      boolean $$6 = $$1.hasNeighborSignal($$2) || $$1.hasNeighborSignal($$2.above());
      boolean $$7 = $$0.getValue(TRIGGERED);
      if ($$6 && !$$7) {
         $$1.scheduleTick($$2, this, 4);
         $$1.setBlock($$2, $$0.setValue(TRIGGERED, true), 2);
      } else if (!$$6 && $$7) {
         $$1.setBlock($$2, $$0.setValue(TRIGGERED, false), 2);
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      this.dispenseFrom($$1, $$0, $$2);
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new DispenserBlockEntity($$0, $$1);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getNearestLookingDirection().getOpposite());
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   public static Position getDispensePosition(BlockSource $$0) {
      return getDispensePosition($$0, 0.7, Vec3.ZERO);
   }

   public static Position getDispensePosition(BlockSource $$0, double $$1, Vec3 $$2) {
      Direction $$3 = $$0.state().getValue(FACING);
      return $$0.center().add($$1 * $$3.getStepX() + $$2.x(), $$1 * $$3.getStepY() + $$2.y(), $$1 * $$3.getStepZ() + $$2.z());
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity($$1.getBlockEntity($$2));
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
      $$0.add(FACING, TRIGGERED);
   }
}
