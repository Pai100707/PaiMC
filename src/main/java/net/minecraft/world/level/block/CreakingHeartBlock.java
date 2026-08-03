package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class CreakingHeartBlock extends BaseEntityBlock {
   public static final MapCodec<CreakingHeartBlock> CODEC = simpleCodec(CreakingHeartBlock::new);
   public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;
   public static final EnumProperty<CreakingHeartState> STATE = BlockStateProperties.CREAKING_HEART_STATE;
   public static final BooleanProperty NATURAL = BlockStateProperties.NATURAL;

   @Override
   public MapCodec<CreakingHeartBlock> codec() {
      return CODEC;
   }

   protected CreakingHeartBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Axis.Y).setValue(STATE, CreakingHeartState.UPROOTED).setValue(NATURAL, false));
   }

   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new CreakingHeartBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      if ($$0.isClientSide()) {
         return null;
      } else {
         return $$1.getValue(STATE) != CreakingHeartState.UPROOTED
            ? createTickerHelper($$2, BlockEntityType.CREAKING_HEART, CreakingHeartBlockEntity::serverTick)
            : null;
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ((Boolean)$$1.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, $$2)) {
         if ($$0.getValue(STATE) != CreakingHeartState.UPROOTED) {
            if ($$3.nextInt(16) == 0 && isSurroundedByLogs($$1, $$2)) {
               $$1.playLocalSound($$2.getX(), $$2.getY(), $$2.getZ(), SoundEvents.CREAKING_HEART_IDLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
         }
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
      $$2.scheduleTick($$3, this, 1);
      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockState $$4 = updateState($$0, $$1, $$2);
      if ($$4 != $$0) {
         $$1.setBlock($$2, $$4, 3);
      }
   }

   private static BlockState updateState(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      boolean $$3 = hasRequiredLogs($$0, $$1, $$2);
      boolean $$4 = $$0.getValue(STATE) == CreakingHeartState.UPROOTED;
      return $$3 && $$4
         ? $$0.setValue(
            STATE, $$1.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, $$2) ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT
         )
         : $$0;
   }

   public static boolean hasRequiredLogs(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      Axis $$3 = $$0.getValue(AXIS);

      for (Direction $$4 : $$3.getDirections()) {
         BlockState $$5 = $$1.getBlockState($$2.relative($$4));
         if (!$$5.is(BlockTags.PALE_OAK_LOGS) || $$5.getValue(AXIS) != $$3) {
            return false;
         }
      }

      return true;
   }

   private static boolean isSurroundedByLogs(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      for (Direction $$2 : Direction.values()) {
         BlockPos $$3 = $$1.relative($$2);
         BlockState $$4 = $$0.getBlockState($$3);
         if (!$$4.is(BlockTags.PALE_OAK_LOGS)) {
            return false;
         }
      }

      return true;
   }

   @Nullable
   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return updateState(this.defaultBlockState().setValue(AXIS, $$0.getClickedFace().getAxis()), $$0.getLevel(), $$0.getClickedPos());
   }

   @Override
   protected BlockState rotate(BlockState $$0, Rotation $$1) {
      return RotatedPillarBlock.rotatePillar($$0, $$1);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AXIS, STATE, NATURAL);
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      Containers.updateNeighboursAfterDestroy($$0, $$1, $$2);
   }

   @Override
   protected void onExplosionHit(BlockState $$0, ServerLevel $$1, BlockPos $$2, net.minecraft.world.level.Explosion $$3, BiConsumer<ItemStack, BlockPos> $$4) {
      if ($$1.getBlockEntity($$2) instanceof CreakingHeartBlockEntity $$5
         && $$3 instanceof net.minecraft.world.level.ServerExplosion $$6
         && $$3.getBlockInteraction().shouldAffectBlocklikeEntities()) {
         $$5.removeProtector($$6.getDamageSource());
         if ($$3.getIndirectSourceEntity() instanceof Player $$7 && $$3.getBlockInteraction().shouldAffectBlocklikeEntities()) {
            this.tryAwardExperience($$7, $$0, $$1, $$2);
         }
      }

      super.onExplosionHit($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if ($$0.getBlockEntity($$1) instanceof CreakingHeartBlockEntity $$4) {
         $$4.removeProtector($$3.damageSources().playerAttack($$3));
         this.tryAwardExperience($$3, $$2, $$0, $$1);
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   private void tryAwardExperience(Player $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3) {
      if (!$$0.preventsBlockDrops() && !$$0.isSpectator() && $$1.getValue(NATURAL) && $$2 instanceof ServerLevel $$4) {
         this.popExperience($$4, $$3, $$2.random.nextIntBetweenInclusive(20, 24));
      }
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      if ($$0.getValue(STATE) == CreakingHeartState.UPROOTED) {
         return 0;
      } else {
         return $$1.getBlockEntity($$2) instanceof CreakingHeartBlockEntity $$4 ? $$4.getAnalogOutputSignal() : 0;
      }
   }
}
