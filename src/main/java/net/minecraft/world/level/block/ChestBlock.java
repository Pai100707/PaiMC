package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChestBlock extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {
   public static final MapCodec<ChestBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound),
            BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound),
            propertiesCodec()
         )
         .apply($$0, ($$0x, $$1, $$2) -> new ChestBlock(() -> BlockEntityType.CHEST, $$0x, $$1, $$2))
   );
   public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
   public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final int EVENT_SET_OPEN_COUNT = 1;
   private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);
   private static final Map<Direction, VoxelShape> HALF_SHAPES = Shapes.rotateHorizontal(Block.boxZ(14.0, 0.0, 14.0, 0.0, 15.0));
   private final SoundEvent openSound;
   private final SoundEvent closeSound;
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>() {
      public Optional<Container> acceptDouble(ChestBlockEntity $$0, ChestBlockEntity $$1) {
         return Optional.of(new CompoundContainer($$0, $$1));
      }

      public Optional<Container> acceptSingle(ChestBlockEntity $$0) {
         return Optional.of($$0);
      }

      public Optional<Container> acceptNone() {
         return Optional.empty();
      }
   };
   private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>() {
      public Optional<MenuProvider> acceptDouble(final ChestBlockEntity $$0, final ChestBlockEntity $$1) {
         final Container $$2 = new CompoundContainer($$0, $$1);
         return Optional.of(new MenuProvider() {
            
            public AbstractContainerMenu createMenu(int $$0x, Inventory $$1x, Player $$2x) {
               if ($$0.canOpen($$2) && $$1.canOpen($$2)) {
                  $$0.unpackLootTable($$1.player);
                  $$1.unpackLootTable($$1.player);
                  return ChestMenu.sixRows($$0, $$1, $$2);
               } else {
                  Direction $$3 = ChestBlock.getConnectedDirection($$0.getBlockState());
                  Vec3 $$4 = $$0.getBlockPos().getCenter();
                  Vec3 $$5 = $$4.add($$3.getStepX() / 2.0, 0.0, $$3.getStepZ() / 2.0);
                  BaseContainerBlockEntity.sendChestLockedNotifications($$5, $$2, this.getDisplayName());
                  return null;
               }
            }

            public Component getDisplayName() {
               if ($$0.hasCustomName()) {
                  return $$0.getDisplayName();
               } else {
                  return (Component)($$1.hasCustomName() ? $$1.getDisplayName() : Component.translatable("container.chestDouble"));
               }
            }
         });
      }

      public Optional<MenuProvider> acceptSingle(ChestBlockEntity $$0) {
         return Optional.of($$0);
      }

      public Optional<MenuProvider> acceptNone() {
         return Optional.empty();
      }
   };

   @Override
   public MapCodec<? extends ChestBlock> codec() {
      return CODEC;
   }

   protected ChestBlock(Supplier<BlockEntityType<? extends ChestBlockEntity>> $$0, SoundEvent $$1, SoundEvent $$2, BlockBehaviour.Properties $$3) {
      super($$3, $$0);
      this.openSound = $$1;
      this.closeSound = $$2;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, false));
   }

   public static DoubleBlockCombiner.BlockType getBlockType(BlockState $$0) {
      ChestType $$1 = $$0.getValue(TYPE);
      if ($$1 == ChestType.SINGLE) {
         return DoubleBlockCombiner.BlockType.SINGLE;
      } else {
         return $$1 == ChestType.RIGHT ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
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
      if ($$0.getValue(WATERLOGGED)) {
         $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      }

      if (this.chestCanConnectTo($$6) && $$4.getAxis().isHorizontal()) {
         ChestType $$8 = $$6.getValue(TYPE);
         if ($$0.getValue(TYPE) == ChestType.SINGLE
            && $$8 != ChestType.SINGLE
            && $$0.getValue(FACING) == $$6.getValue(FACING)
            && getConnectedDirection($$6) == $$4.getOpposite()) {
            return $$0.setValue(TYPE, $$8.getOpposite());
         }
      } else if (getConnectedDirection($$0) == $$4) {
         return $$0.setValue(TYPE, ChestType.SINGLE);
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   public boolean chestCanConnectTo(BlockState $$0) {
      return $$0.is(this);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return switch ((ChestType)$$0.getValue(TYPE)) {
         case SINGLE -> SHAPE;
         case LEFT, RIGHT -> (VoxelShape)HALF_SHAPES.get(getConnectedDirection($$0));
      };
   }

   public static Direction getConnectedDirection(BlockState $$0) {
      Direction $$1 = $$0.getValue(FACING);
      return $$0.getValue(TYPE) == ChestType.LEFT ? $$1.getClockWise() : $$1.getCounterClockWise();
   }

   public static BlockPos getConnectedBlockPos(BlockPos $$0, BlockState $$1) {
      Direction $$2 = getConnectedDirection($$1);
      return $$0.relative($$2);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      ChestType $$1 = ChestType.SINGLE;
      Direction $$2 = $$0.getHorizontalDirection().getOpposite();
      FluidState $$3 = $$0.getLevel().getFluidState($$0.getClickedPos());
      boolean $$4 = $$0.isSecondaryUseActive();
      Direction $$5 = $$0.getClickedFace();
      if ($$5.getAxis().isHorizontal() && $$4) {
         Direction $$6 = this.candidatePartnerFacing($$0.getLevel(), $$0.getClickedPos(), $$5.getOpposite());
         if ($$6 != null && $$6.getAxis() != $$5.getAxis()) {
            $$2 = $$6;
            $$1 = $$6.getCounterClockWise() == $$5.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if ($$1 == ChestType.SINGLE && !$$4) {
         $$1 = this.getChestType($$0.getLevel(), $$0.getClickedPos(), $$2);
      }

      return this.defaultBlockState().setValue(FACING, $$2).setValue(TYPE, $$1).setValue(WATERLOGGED, $$3.getType() == Fluids.WATER);
   }

   protected ChestType getChestType(net.minecraft.world.level.Level $$0, BlockPos $$1, Direction $$2) {
      if ($$2 == this.candidatePartnerFacing($$0, $$1, $$2.getClockWise())) {
         return ChestType.LEFT;
      } else {
         return $$2 == this.candidatePartnerFacing($$0, $$1, $$2.getCounterClockWise()) ? ChestType.RIGHT : ChestType.SINGLE;
      }
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return $$0.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState($$0);
   }

   
   private Direction candidatePartnerFacing(net.minecraft.world.level.Level $$0, BlockPos $$1, Direction $$2) {
      BlockState $$3 = $$0.getBlockState($$1.relative($$2));
      return this.chestCanConnectTo($$3) && $$3.getValue(TYPE) == ChestType.SINGLE ? $$3.getValue(FACING) : null;
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1 instanceof ServerLevel $$5) {
         MenuProvider $$6 = this.getMenuProvider($$0, $$1, $$2);
         if ($$6 != null) {
            $$3.openMenu($$6);
            $$3.awardStat(this.getOpenChestStat());
            PiglinAi.angerNearbyPiglins($$5, $$3, true);
         }
      }

      return InteractionResult.SUCCESS;
   }

   protected Stat<Identifier> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.OPEN_CHEST);
   }

   public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
      return this.blockEntityType.get();
   }

   
   public static Container getContainer(ChestBlock $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, boolean $$4) {
      return $$0.combine($$1, $$2, $$3, $$4).apply(CHEST_COMBINER).orElse(null);
   }

   @Override
   public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(
      BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, boolean $$3
   ) {
      BiPredicate<net.minecraft.world.level.LevelAccessor, BlockPos> $$4;
      if ($$3) {
         $$4 = ($$0x, $$1x) -> false;
      } else {
         $$4 = ChestBlock::isChestBlockedAt;
      }

      return DoubleBlockCombiner.combineWithNeigbour(
         this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, $$0, $$1, $$2, $$4
      );
   }

   
   @Override
   protected MenuProvider getMenuProvider(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      return this.combine($$0, $$1, $$2, false).apply(MENU_PROVIDER_COMBINER).orElse(null);
   }

   public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity $$0) {
      return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>() {
         public Float2FloatFunction acceptDouble(ChestBlockEntity $$0x, ChestBlockEntity $$1) {
            return $$2 -> Math.max($$0.getOpenNess($$2), $$1.getOpenNess($$2));
         }

         public Float2FloatFunction acceptSingle(ChestBlockEntity $$0x) {
            return $$0::getOpenNess;
         }

         public Float2FloatFunction acceptNone() {
            return $$0::getOpenNess;
         }
      };
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new ChestBlockEntity($$0, $$1);
   }

   
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0.isClientSide() ? createTickerHelper($$2, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) : null;
   }

   public static boolean isChestBlockedAt(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      return isBlockedChestByBlock($$0, $$1) || isCatSittingOnChest($$0, $$1);
   }

   private static boolean isBlockedChestByBlock(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.above();
      return $$0.getBlockState($$2).isRedstoneConductor($$0, $$2);
   }

   private static boolean isCatSittingOnChest(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      List<Cat> $$2 = $$0.getEntitiesOfClass(Cat.class, new AABB($$1.getX(), $$1.getY() + 1, $$1.getZ(), $$1.getX() + 1, $$1.getY() + 2, $$1.getZ() + 1));
      if (!$$2.isEmpty()) {
         for (Cat $$3 : $$2) {
            if ($$3.isInSittingPose()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return AbstractContainerMenu.getRedstoneSignalFromContainer(getContainer(this, $$0, $$1, $$2, false));
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
      $$0.add(FACING, TYPE, WATERLOGGED);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockEntity $$4 = $$1.getBlockEntity($$2);
      if ($$4 instanceof ChestBlockEntity) {
         ((ChestBlockEntity)$$4).recheckOpen();
      }
   }

   public SoundEvent getOpenChestSound() {
      return this.openSound;
   }

   public SoundEvent getCloseChestSound() {
      return this.closeSound;
   }
}
