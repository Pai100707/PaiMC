package net.minecraft.world.level.block;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class RedstoneTorchBlock extends BaseTorchBlock {
   public static final MapCodec<RedstoneTorchBlock> CODEC = simpleCodec(RedstoneTorchBlock::new);
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private static final Map<net.minecraft.world.level.BlockGetter, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap<>();
   public static final int RECENT_TOGGLE_TIMER = 60;
   public static final int MAX_RECENT_TOGGLES = 8;
   public static final int RESTART_DELAY = 160;
   private static final int TOGGLE_DELAY = 2;

   @Override
   public MapCodec<? extends RedstoneTorchBlock> codec() {
      return CODEC;
   }

   protected RedstoneTorchBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, true));
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      this.notifyNeighbors($$1, $$2, $$0);
   }

   private void notifyNeighbors(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      Orientation $$3 = this.randomOrientation($$0, $$2);

      for (Direction $$4 : Direction.values()) {
         $$0.updateNeighborsAt($$1.relative($$4), this, ExperimentalRedstoneUtils.withFront($$3, $$4));
      }
   }

   @Override
   protected void affectNeighborsAfterRemoval(BlockState $$0, ServerLevel $$1, BlockPos $$2, boolean $$3) {
      if (!$$3) {
         this.notifyNeighbors($$1, $$2, $$0);
      }
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(LIT) && Direction.UP != $$3 ? 15 : 0;
   }

   protected boolean hasNeighborSignal(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      return $$0.hasSignal($$1.below(), Direction.DOWN);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      boolean $$4 = this.hasNeighborSignal($$1, $$2, $$0);
      List<RedstoneTorchBlock.Toggle> $$5 = RECENT_TOGGLES.get($$1);

      while ($$5 != null && !$$5.isEmpty() && $$1.getGameTime() - $$5.get(0).when > 60L) {
         $$5.remove(0);
      }

      if ($$0.getValue(LIT)) {
         if ($$4) {
            $$1.setBlock($$2, $$0.setValue(LIT, false), 3);
            if (isToggledTooFrequently($$1, $$2, true)) {
               $$1.levelEvent(1502, $$2, 0);
               $$1.scheduleTick($$2, $$1.getBlockState($$2).getBlock(), 160);
            }
         }
      } else if (!$$4 && !isToggledTooFrequently($$1, $$2, false)) {
         $$1.setBlock($$2, $$0.setValue(LIT, true), 3);
      }
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, @Nullable Orientation $$4, boolean $$5) {
      if ($$0.getValue(LIT) == this.hasNeighborSignal($$1, $$2, $$0) && !$$1.getBlockTicks().willTickThisTick($$2, this)) {
         $$1.scheduleTick($$2, this, 2);
      }
   }

   @Override
   protected int getDirectSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$3 == Direction.DOWN ? $$0.getSignal($$1, $$2, $$3) : 0;
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         double $$4 = $$2.getX() + 0.5 + ($$3.nextDouble() - 0.5) * 0.2;
         double $$5 = $$2.getY() + 0.7 + ($$3.nextDouble() - 0.5) * 0.2;
         double $$6 = $$2.getZ() + 0.5 + ($$3.nextDouble() - 0.5) * 0.2;
         $$1.addParticle(DustParticleOptions.REDSTONE, $$4, $$5, $$6, 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LIT);
   }

   private static boolean isToggledTooFrequently(net.minecraft.world.level.Level $$0, BlockPos $$1, boolean $$2) {
      List<RedstoneTorchBlock.Toggle> $$3 = RECENT_TOGGLES.computeIfAbsent($$0, $$0x -> Lists.newArrayList());
      if ($$2) {
         $$3.add(new RedstoneTorchBlock.Toggle($$1.immutable(), $$0.getGameTime()));
      }

      int $$4 = 0;

      for (RedstoneTorchBlock.Toggle $$5 : $$3) {
         if ($$5.pos.equals($$1)) {
            if (++$$4 >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   @Nullable
   protected Orientation randomOrientation(net.minecraft.world.level.Level $$0, BlockState $$1) {
      return ExperimentalRedstoneUtils.initialOrientation($$0, null, Direction.UP);
   }

   public static class Toggle {
      final BlockPos pos;
      final long when;

      public Toggle(BlockPos $$0, long $$1) {
         this.pos = $$0;
         this.when = $$1;
      }
   }
}
