package net.minecraft.world.level.material;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.gamerules.GameRules;

public abstract class LavaFluid extends FlowingFluid {
   public static final float MIN_LEVEL_CUTOFF = 0.44444445F;

   @Override
   public Fluid getFlowing() {
      return Fluids.FLOWING_LAVA;
   }

   @Override
   public Fluid getSource() {
      return Fluids.LAVA;
   }

   @Override
   public Item getBucket() {
      return Items.LAVA_BUCKET;
   }

   @Override
   public void animateTick(net.minecraft.world.level.Level $$0, BlockPos $$1, FluidState $$2, RandomSource $$3) {
      BlockPos $$4 = $$1.above();
      if ($$0.getBlockState($$4).isAir() && !$$0.getBlockState($$4).isSolidRender()) {
         if ($$3.nextInt(100) == 0) {
            double $$5 = $$1.getX() + $$3.nextDouble();
            double $$6 = $$1.getY() + 1.0;
            double $$7 = $$1.getZ() + $$3.nextDouble();
            $$0.addParticle(ParticleTypes.LAVA, $$5, $$6, $$7, 0.0, 0.0, 0.0);
            $$0.playLocalSound($$5, $$6, $$7, SoundEvents.LAVA_POP, SoundSource.AMBIENT, 0.2F + $$3.nextFloat() * 0.2F, 0.9F + $$3.nextFloat() * 0.15F, false);
         }

         if ($$3.nextInt(200) == 0) {
            $$0.playLocalSound(
               $$1.getX(),
               $$1.getY(),
               $$1.getZ(),
               SoundEvents.LAVA_AMBIENT,
               SoundSource.AMBIENT,
               0.2F + $$3.nextFloat() * 0.2F,
               0.9F + $$3.nextFloat() * 0.15F,
               false
            );
         }
      }
   }

   @Override
   public void randomTick(ServerLevel $$0, BlockPos $$1, FluidState $$2, RandomSource $$3) {
      if ($$0.canSpreadFireAround($$1)) {
         int $$4 = $$3.nextInt(3);
         if ($$4 > 0) {
            BlockPos $$5 = $$1;

            for (int $$6 = 0; $$6 < $$4; $$6++) {
               $$5 = $$5.offset($$3.nextInt(3) - 1, 1, $$3.nextInt(3) - 1);
               if (!$$0.isLoaded($$5)) {
                  return;
               }

               BlockState $$7 = $$0.getBlockState($$5);
               if ($$7.isAir()) {
                  if (this.hasFlammableNeighbours($$0, $$5)) {
                     $$0.setBlockAndUpdate($$5, BaseFireBlock.getState($$0, $$5));
                     return;
                  }
               } else if ($$7.blocksMotion()) {
                  return;
               }
            }
         } else {
            for (int $$8 = 0; $$8 < 3; $$8++) {
               BlockPos $$9 = $$1.offset($$3.nextInt(3) - 1, 0, $$3.nextInt(3) - 1);
               if (!$$0.isLoaded($$9)) {
                  return;
               }

               if ($$0.isEmptyBlock($$9.above()) && this.isFlammable($$0, $$9)) {
                  $$0.setBlockAndUpdate($$9.above(), BaseFireBlock.getState($$0, $$9));
               }
            }
         }
      }
   }

   @Override
   protected void entityInside(net.minecraft.world.level.Level $$0, BlockPos $$1, Entity $$2, InsideBlockEffectApplier $$3) {
      $$3.apply(InsideBlockEffectType.CLEAR_FREEZE);
      $$3.apply(InsideBlockEffectType.LAVA_IGNITE);
      $$3.runAfter(InsideBlockEffectType.LAVA_IGNITE, Entity::lavaHurt);
   }

   private boolean hasFlammableNeighbours(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      for (Direction $$2 : Direction.values()) {
         if (this.isFlammable($$0, $$1.relative($$2))) {
            return true;
         }
      }

      return false;
   }

   private boolean isFlammable(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      return $$0.isInsideBuildHeight($$1.getY()) && !$$0.hasChunkAt($$1) ? false : $$0.getBlockState($$1).ignitedByLava();
   }

   
   @Override
   public ParticleOptions getDripParticle() {
      return ParticleTypes.DRIPPING_LAVA;
   }

   @Override
   protected void beforeDestroyingBlock(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2) {
      this.fizz($$0, $$1);
   }

   @Override
   public int getSlopeFindDistance(net.minecraft.world.level.LevelReader $$0) {
      return isFastLava($$0) ? 4 : 2;
   }

   @Override
   public BlockState createLegacyBlock(FluidState $$0) {
      return Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel($$0));
   }

   @Override
   public boolean isSame(Fluid $$0) {
      return $$0 == Fluids.LAVA || $$0 == Fluids.FLOWING_LAVA;
   }

   @Override
   public int getDropOff(net.minecraft.world.level.LevelReader $$0) {
      return isFastLava($$0) ? 1 : 2;
   }

   @Override
   public boolean canBeReplacedWith(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Fluid $$3, Direction $$4) {
      return $$0.getHeight($$1, $$2) >= 0.44444445F && $$3.is(FluidTags.WATER);
   }

   @Override
   public int getTickDelay(net.minecraft.world.level.LevelReader $$0) {
      return isFastLava($$0) ? 10 : 30;
   }

   @Override
   public int getSpreadDelay(net.minecraft.world.level.Level $$0, BlockPos $$1, FluidState $$2, FluidState $$3) {
      int $$4 = this.getTickDelay($$0);
      if (!$$2.isEmpty()
         && !$$3.isEmpty()
         && !$$2.getValue(FALLING)
         && !$$3.getValue(FALLING)
         && $$3.getHeight($$0, $$1) > $$2.getHeight($$0, $$1)
         && $$0.getRandom().nextInt(4) != 0) {
         $$4 *= 4;
      }

      return $$4;
   }

   private void fizz(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      $$0.levelEvent(1501, $$1, 0);
   }

   @Override
   protected boolean canConvertToSource(ServerLevel $$0) {
      return $$0.getGameRules().get(GameRules.LAVA_SOURCE_CONVERSION);
   }

   @Override
   protected void spreadTo(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, Direction $$3, FluidState $$4) {
      if ($$3 == Direction.DOWN) {
         FluidState $$5 = $$0.getFluidState($$1);
         if (this.is(FluidTags.LAVA) && $$5.is(FluidTags.WATER)) {
            if ($$2.getBlock() instanceof LiquidBlock) {
               $$0.setBlock($$1, Blocks.STONE.defaultBlockState(), 3);
            }

            this.fizz($$0, $$1);
            return;
         }
      }

      super.spreadTo($$0, $$1, $$2, $$3, $$4);
   }

   @Override
   protected boolean isRandomlyTicking() {
      return true;
   }

   @Override
   protected float getExplosionResistance() {
      return 100.0F;
   }

   @Override
   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
   }

   private static boolean isFastLava(net.minecraft.world.level.LevelReader $$0) {
      return (Boolean)$$0.environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA);
   }

   public static class Flowing extends LavaFluid {
      @Override
      protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> $$0) {
         super.createFluidStateDefinition($$0);
         $$0.add(LEVEL);
      }

      @Override
      public int getAmount(FluidState $$0) {
         return $$0.getValue(LEVEL);
      }

      @Override
      public boolean isSource(FluidState $$0) {
         return false;
      }
   }

   public static class Source extends LavaFluid {
      @Override
      public int getAmount(FluidState $$0) {
         return 8;
      }

      @Override
      public boolean isSource(FluidState $$0) {
         return true;
      }
   }
}
