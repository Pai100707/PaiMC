package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class TargetBlock extends Block {
   public static final MapCodec<TargetBlock> CODEC = simpleCodec(TargetBlock::new);
   private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;
   private static final int ACTIVATION_TICKS_ARROWS = 20;
   private static final int ACTIVATION_TICKS_OTHER = 8;

   @Override
   public MapCodec<TargetBlock> codec() {
      return CODEC;
   }

   public TargetBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(OUTPUT_POWER, 0));
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      int $$4 = updateRedstoneOutput($$0, $$1, $$2, $$3);
      if ($$3.getOwner() instanceof ServerPlayer $$6) {
         $$6.awardStat(Stats.TARGET_HIT);
         CriteriaTriggers.TARGET_BLOCK_HIT.trigger($$6, $$3, $$2.getLocation(), $$4);
      }
   }

   private static int updateRedstoneOutput(net.minecraft.world.level.LevelAccessor $$0, BlockState $$1, BlockHitResult $$2, Entity $$3) {
      int $$4 = getRedstoneStrength($$2, $$2.getLocation());
      int $$5 = $$3 instanceof AbstractArrow ? 20 : 8;
      if (!$$0.getBlockTicks().hasScheduledTick($$2.getBlockPos(), $$1.getBlock())) {
         setOutputPower($$0, $$1, $$4, $$2.getBlockPos(), $$5);
      }

      return $$4;
   }

   private static int getRedstoneStrength(BlockHitResult $$0, Vec3 $$1) {
      Direction $$2 = $$0.getDirection();
      double $$3 = Math.abs(Mth.frac($$1.x) - 0.5);
      double $$4 = Math.abs(Mth.frac($$1.y) - 0.5);
      double $$5 = Math.abs(Mth.frac($$1.z) - 0.5);
      Axis $$6 = $$2.getAxis();
      double $$7;
      if ($$6 == Axis.Y) {
         $$7 = Math.max($$3, $$5);
      } else if ($$6 == Axis.Z) {
         $$7 = Math.max($$3, $$4);
      } else {
         $$7 = Math.max($$4, $$5);
      }

      return Math.max(1, Mth.ceil(15.0 * Mth.clamp((0.5 - $$7) / 0.5, 0.0, 1.0)));
   }

   private static void setOutputPower(net.minecraft.world.level.LevelAccessor $$0, BlockState $$1, int $$2, BlockPos $$3, int $$4) {
      $$0.setBlock($$3, $$1.setValue(OUTPUT_POWER, $$2), 3);
      $$0.scheduleTick($$3, $$1.getBlock(), $$4);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(OUTPUT_POWER) != 0) {
         $$1.setBlock($$2, $$0.setValue(OUTPUT_POWER, 0), 3);
      }
   }

   @Override
   protected int getSignal(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(OUTPUT_POWER);
   }

   @Override
   protected boolean isSignalSource(BlockState $$0) {
      return true;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(OUTPUT_POWER);
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$1.isClientSide() && !$$0.is($$3.getBlock())) {
         if ($$0.getValue(OUTPUT_POWER) > 0 && !$$1.getBlockTicks().hasScheduledTick($$2, this)) {
            $$1.setBlock($$2, $$0.setValue(OUTPUT_POWER, 0), 18);
         }
      }
   }
}
