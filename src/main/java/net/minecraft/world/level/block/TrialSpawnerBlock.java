package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jspecify.annotations.Nullable;

public class TrialSpawnerBlock extends BaseEntityBlock {
   public static final MapCodec<TrialSpawnerBlock> CODEC = simpleCodec(TrialSpawnerBlock::new);
   public static final EnumProperty<TrialSpawnerState> STATE = BlockStateProperties.TRIAL_SPAWNER_STATE;
   public static final BooleanProperty OMINOUS = BlockStateProperties.OMINOUS;

   @Override
   public MapCodec<TrialSpawnerBlock> codec() {
      return CODEC;
   }

   public TrialSpawnerBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(STATE, TrialSpawnerState.INACTIVE).setValue(OMINOUS, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(STATE, OMINOUS);
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new TrialSpawnerBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0 instanceof ServerLevel $$3
         ? createTickerHelper(
            $$2,
            BlockEntityType.TRIAL_SPAWNER,
            ($$1x, $$2x, $$3x, $$4) -> $$4.getTrialSpawner().tickServer($$3, $$2x, $$3x.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
         )
         : createTickerHelper(
            $$2,
            BlockEntityType.TRIAL_SPAWNER,
            ($$0x, $$1x, $$2x, $$3x) -> $$3x.getTrialSpawner().tickClient($$0x, $$1x, $$2x.getOptionalValue(BlockStateProperties.OMINOUS).orElse(false))
         );
   }
}
