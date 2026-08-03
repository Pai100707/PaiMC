package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ShulkerBoxBlock extends BaseEntityBlock {
   public static final MapCodec<ShulkerBoxBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(DyeColor.CODEC.optionalFieldOf("color").forGetter($$0x -> Optional.ofNullable($$0x.color)), propertiesCodec())
         .apply($$0, ($$0x, $$1) -> new ShulkerBoxBlock((DyeColor)$$0x.orElse(null), $$1))
   );
   public static final Map<Direction, VoxelShape> SHAPES_OPEN_SUPPORT = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
   public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
   public static final Identifier CONTENTS = Identifier.withDefaultNamespace("contents");
   @Nullable
   private final DyeColor color;

   @Override
   public MapCodec<ShulkerBoxBlock> codec() {
      return CODEC;
   }

   public ShulkerBoxBlock(@Nullable DyeColor $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.color = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new ShulkerBoxBlockEntity(this.color, $$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return createTickerHelper($$2, BlockEntityType.SHULKER_BOX, ShulkerBoxBlockEntity::tick);
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$1 instanceof ServerLevel $$5 && $$1.getBlockEntity($$2) instanceof ShulkerBoxBlockEntity $$6 && canOpen($$0, $$1, $$2, $$6)) {
         $$3.openMenu($$6);
         $$3.awardStat(Stats.OPEN_SHULKER_BOX);
         PiglinAi.angerNearbyPiglins($$5, $$3, true);
      }

      return InteractionResult.SUCCESS;
   }

   private static boolean canOpen(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, ShulkerBoxBlockEntity $$3) {
      if ($$3.getAnimationStatus() != ShulkerBoxBlockEntity.AnimationStatus.CLOSED) {
         return true;
      } else {
         AABB $$4 = Shulker.getProgressDeltaAabb(1.0F, $$0.getValue(FACING), 0.0F, 0.5F, $$2.getBottomCenter()).deflate(1.0E-6);
         return $$1.noCollision($$4);
      }
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.defaultBlockState().setValue(FACING, $$0.getClickedFace());
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(FACING);
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      BlockEntity $$4 = $$0.getBlockEntity($$1);
      if ($$4 instanceof ShulkerBoxBlockEntity $$5) {
         if (!$$0.isClientSide() && $$3.preventsBlockDrops() && !$$5.isEmpty()) {
            ItemStack $$6 = getColoredItemStack(this.getColor());
            $$6.applyComponents($$4.collectComponents());
            ItemEntity $$7 = new ItemEntity($$0, $$1.getX() + 0.5, $$1.getY() + 0.5, $$1.getZ() + 0.5, $$6);
            $$7.setDefaultPickUpDelay();
            $$0.addFreshEntity($$7);
         } else {
            $$5.unpackLootTable($$3);
         }
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   @Override
   protected List<ItemStack> getDrops(BlockState $$0, LootParams.Builder $$1) {
      BlockEntity $$2 = $$1.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
      if ($$2 instanceof ShulkerBoxBlockEntity $$3) {
         $$1 = $$1.withDynamicDrop(CONTENTS, $$1x -> {
            for (int $$2x = 0; $$2x < $$3.getContainerSize(); $$2x++) {
               $$1x.accept($$3.getItem($$2x));
            }
         });
      }

      return super.getDrops($$0, $$1);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$1.getBlockEntity($$2) instanceof ShulkerBoxBlockEntity $$4 && !$$4.isClosed()
         ? SHAPES_OPEN_SUPPORT.get(((Direction)$$0.getValue(FACING)).getOpposite())
         : Shapes.block();
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return $$1.getBlockEntity($$2) instanceof ShulkerBoxBlockEntity $$5 ? Shapes.create($$5.getBoundingBox($$0)) : Shapes.block();
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return false;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return AbstractContainerMenu.getRedstoneSignalFromBlockEntity($$1.getBlockEntity($$2));
   }

   public static Block getBlockByColor(@Nullable DyeColor $$0) {
      if ($$0 == null) {
         return Blocks.SHULKER_BOX;
      } else {
         return switch ($$0) {
            case WHITE -> Blocks.WHITE_SHULKER_BOX;
            case ORANGE -> Blocks.ORANGE_SHULKER_BOX;
            case MAGENTA -> Blocks.MAGENTA_SHULKER_BOX;
            case LIGHT_BLUE -> Blocks.LIGHT_BLUE_SHULKER_BOX;
            case YELLOW -> Blocks.YELLOW_SHULKER_BOX;
            case LIME -> Blocks.LIME_SHULKER_BOX;
            case PINK -> Blocks.PINK_SHULKER_BOX;
            case GRAY -> Blocks.GRAY_SHULKER_BOX;
            case LIGHT_GRAY -> Blocks.LIGHT_GRAY_SHULKER_BOX;
            case CYAN -> Blocks.CYAN_SHULKER_BOX;
            case BLUE -> Blocks.BLUE_SHULKER_BOX;
            case BROWN -> Blocks.BROWN_SHULKER_BOX;
            case GREEN -> Blocks.GREEN_SHULKER_BOX;
            case RED -> Blocks.RED_SHULKER_BOX;
            case BLACK -> Blocks.BLACK_SHULKER_BOX;
            case PURPLE -> Blocks.PURPLE_SHULKER_BOX;
            default -> throw new MatchException(null, null);
         };
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getColoredItemStack(@Nullable DyeColor $$0) {
      return new ItemStack(getBlockByColor($$0));
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return $$0.setValue(FACING, $$1.rotate($$0.getValue(FACING)));
   }

   @Override
   protected BlockState mirror(BlockState $$0, Mirror $$1) {
      return $$0.rotate($$1.getRotation($$0.getValue(FACING)));
   }
}
