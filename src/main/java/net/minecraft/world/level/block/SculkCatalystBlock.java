package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jspecify.annotations.Nullable;

public class SculkCatalystBlock extends BaseEntityBlock {
   public static final MapCodec<SculkCatalystBlock> CODEC = simpleCodec(SculkCatalystBlock::new);
   public static final BooleanProperty PULSE = BlockStateProperties.BLOOM;
   private final IntProvider xpRange = ConstantInt.of(5);

   @Override
   public MapCodec<SculkCatalystBlock> codec() {
      return CODEC;
   }

   public SculkCatalystBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(PULSE, false));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(PULSE);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(PULSE)) {
         $$1.setBlock($$2, $$0.setValue(PULSE, false), 3);
      }
   }

   @Nullable
   @Override
   public BlockEntity newBlockEntity(BlockPos $$0, BlockState $$1) {
      return new SculkCatalystBlockEntity($$0, $$1);
   }

   @Nullable
   @Override
   public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level $$0, BlockState $$1, BlockEntityType<T> $$2) {
      return $$0.isClientSide() ? null : createTickerHelper($$2, BlockEntityType.SCULK_CATALYST, SculkCatalystBlockEntity::serverTick);
   }

   @Override
   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
      super.spawnAfterBreak($$0, $$1, $$2, $$3, $$4);
      if ($$4) {
         this.tryDropExperience($$1, $$2, $$3, this.xpRange);
      }
   }
}
