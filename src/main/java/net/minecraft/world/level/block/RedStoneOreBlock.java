package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class RedStoneOreBlock extends Block {
   public static final MapCodec<RedStoneOreBlock> CODEC = simpleCodec(RedStoneOreBlock::new);
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   @Override
   public MapCodec<RedStoneOreBlock> codec() {
      return CODEC;
   }

   public RedStoneOreBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, false));
   }

   @Override
   protected void attack(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3) {
      interact($$0, $$1, $$2);
      super.attack($$0, $$1, $$2, $$3);
   }

   @Override
   public void stepOn(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Entity $$3) {
      if (!$$3.isSteppingCarefully()) {
         interact($$2, $$0, $$1);
      }

      super.stepOn($$0, $$1, $$2, $$3);
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$2.isClientSide()) {
         spawnParticles($$2, $$3);
      } else {
         interact($$1, $$2, $$3);
      }

      return (InteractionResult)($$0.getItem() instanceof BlockItem && new BlockPlaceContext($$4, $$5, $$0, $$6).canPlace()
         ? InteractionResult.PASS
         : InteractionResult.SUCCESS);
   }

   private static void interact(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2) {
      spawnParticles($$1, $$2);
      if (!$$0.getValue(LIT)) {
         $$1.setBlock($$2, $$0.setValue(LIT, true), 3);
      }
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(LIT);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         $$1.setBlock($$2, $$0.setValue(LIT, false), 3);
      }
   }

   @Override
   protected void spawnAfterBreak(BlockState $$0, ServerLevel $$1, BlockPos $$2, ItemStack $$3, boolean $$4) {
      super.spawnAfterBreak($$0, $$1, $$2, $$3, $$4);
      if ($$4) {
         this.tryDropExperience($$1, $$2, $$3, UniformInt.of(1, 5));
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LIT)) {
         spawnParticles($$1, $$2);
      }
   }

   private static void spawnParticles(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      double $$2 = 0.5625;
      RandomSource $$3 = $$0.random;

      for (Direction $$4 : Direction.values()) {
         BlockPos $$5 = $$1.relative($$4);
         if (!$$0.getBlockState($$5).isSolidRender()) {
            Axis $$6 = $$4.getAxis();
            double $$7 = $$6 == Axis.X ? 0.5 + 0.5625 * $$4.getStepX() : $$3.nextFloat();
            double $$8 = $$6 == Axis.Y ? 0.5 + 0.5625 * $$4.getStepY() : $$3.nextFloat();
            double $$9 = $$6 == Axis.Z ? 0.5 + 0.5625 * $$4.getStepZ() : $$3.nextFloat();
            $$0.addParticle(DustParticleOptions.REDSTONE, $$1.getX() + $$7, $$1.getY() + $$8, $$1.getZ() + $$9, 0.0, 0.0, 0.0);
         }
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LIT);
   }
}
