package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class BaseFireBlock extends Block {
   private static final int SECONDS_ON_FIRE = 8;
   private static final int MIN_FIRE_TICKS_TO_ADD = 1;
   private static final int MAX_FIRE_TICKS_TO_ADD = 3;
   private final float fireDamage;
   protected static final VoxelShape SHAPE = Block.column(16.0, 0.0, 1.0);

   public BaseFireBlock(BlockBehaviour.Properties $$0, float $$1) {
      super($$0);
      this.fireDamage = $$1;
   }

   @Override
   protected abstract MapCodec<? extends BaseFireBlock> codec();

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return getState($$0.getLevel(), $$0.getClickedPos());
   }

   public static BlockState getState(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.below();
      BlockState $$3 = $$0.getBlockState($$2);
      return SoulFireBlock.canSurviveOnBlock($$3) ? Blocks.SOUL_FIRE.defaultBlockState() : ((FireBlock)Blocks.FIRE).getStateForPlacement($$0, $$1);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$3.nextInt(24) == 0) {
         $$1.playLocalSound(
            $$2.getX() + 0.5,
            $$2.getY() + 0.5,
            $$2.getZ() + 0.5,
            SoundEvents.FIRE_AMBIENT,
            SoundSource.BLOCKS,
            1.0F + $$3.nextFloat(),
            $$3.nextFloat() * 0.7F + 0.3F,
            false
         );
      }

      BlockPos $$4 = $$2.below();
      BlockState $$5 = $$1.getBlockState($$4);
      if (!this.canBurn($$5) && !$$5.isFaceSturdy($$1, $$4, Direction.UP)) {
         if (this.canBurn($$1.getBlockState($$2.west()))) {
            for (int $$10 = 0; $$10 < 2; $$10++) {
               double $$11 = $$2.getX() + $$3.nextDouble() * 0.1F;
               double $$12 = $$2.getY() + $$3.nextDouble();
               double $$13 = $$2.getZ() + $$3.nextDouble();
               $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$11, $$12, $$13, 0.0, 0.0, 0.0);
            }
         }

         if (this.canBurn($$1.getBlockState($$2.east()))) {
            for (int $$14 = 0; $$14 < 2; $$14++) {
               double $$15 = $$2.getX() + 1 - $$3.nextDouble() * 0.1F;
               double $$16 = $$2.getY() + $$3.nextDouble();
               double $$17 = $$2.getZ() + $$3.nextDouble();
               $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$15, $$16, $$17, 0.0, 0.0, 0.0);
            }
         }

         if (this.canBurn($$1.getBlockState($$2.north()))) {
            for (int $$18 = 0; $$18 < 2; $$18++) {
               double $$19 = $$2.getX() + $$3.nextDouble();
               double $$20 = $$2.getY() + $$3.nextDouble();
               double $$21 = $$2.getZ() + $$3.nextDouble() * 0.1F;
               $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$19, $$20, $$21, 0.0, 0.0, 0.0);
            }
         }

         if (this.canBurn($$1.getBlockState($$2.south()))) {
            for (int $$22 = 0; $$22 < 2; $$22++) {
               double $$23 = $$2.getX() + $$3.nextDouble();
               double $$24 = $$2.getY() + $$3.nextDouble();
               double $$25 = $$2.getZ() + 1 - $$3.nextDouble() * 0.1F;
               $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$23, $$24, $$25, 0.0, 0.0, 0.0);
            }
         }

         if (this.canBurn($$1.getBlockState($$2.above()))) {
            for (int $$26 = 0; $$26 < 2; $$26++) {
               double $$27 = $$2.getX() + $$3.nextDouble();
               double $$28 = $$2.getY() + 1 - $$3.nextDouble() * 0.1F;
               double $$29 = $$2.getZ() + $$3.nextDouble();
               $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$27, $$28, $$29, 0.0, 0.0, 0.0);
            }
         }
      } else {
         for (int $$6 = 0; $$6 < 3; $$6++) {
            double $$7 = $$2.getX() + $$3.nextDouble();
            double $$8 = $$2.getY() + $$3.nextDouble() * 0.5 + 0.5;
            double $$9 = $$2.getZ() + $$3.nextDouble();
            $$1.addParticle(ParticleTypes.LARGE_SMOKE, $$7, $$8, $$9, 0.0, 0.0, 0.0);
         }
      }
   }

   protected abstract boolean canBurn(BlockState var1);

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      $$4.apply(InsideBlockEffectType.CLEAR_FREEZE);
      $$4.apply(InsideBlockEffectType.FIRE_IGNITE);
      $$4.runAfter(InsideBlockEffectType.FIRE_IGNITE, $$0x -> $$0x.hurt($$0x.level().damageSources().inFire(), this.fireDamage));
   }

   public static void fireIgnite(Entity $$0) {
      if (!$$0.fireImmune()) {
         if ($$0.getRemainingFireTicks() < 0) {
            $$0.setRemainingFireTicks($$0.getRemainingFireTicks() + 1);
         } else if ($$0 instanceof ServerPlayer) {
            int $$1 = $$0.level().getRandom().nextInt(1, 3);
            $$0.setRemainingFireTicks($$0.getRemainingFireTicks() + $$1);
         }

         if ($$0.getRemainingFireTicks() >= 0) {
            $$0.igniteForSeconds(8.0F);
         }
      }
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (!$$3.is($$0.getBlock())) {
         if (inPortalDimension($$1)) {
            Optional<PortalShape> $$5 = PortalShape.findEmptyPortalShape($$1, $$2, Axis.X);
            if ($$5.isPresent()) {
               $$5.get().createPortalBlocks($$1);
               return;
            }
         }

         if (!$$0.canSurvive($$1, $$2)) {
            $$1.removeBlock($$2, false);
         }
      }
   }

   private static boolean inPortalDimension(net.minecraft.world.level.Level $$0) {
      return $$0.dimension() == net.minecraft.world.level.Level.OVERWORLD || $$0.dimension() == net.minecraft.world.level.Level.NETHER;
   }

   @Override
   protected void spawnDestroyParticles(net.minecraft.world.level.Level $$0, Player $$1, BlockPos $$2, BlockState $$3) {
   }

   @Override
   public BlockState playerWillDestroy(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, Player $$3) {
      if (!$$0.isClientSide()) {
         $$0.levelEvent(null, 1009, $$1, 0);
      }

      return super.playerWillDestroy($$0, $$1, $$2, $$3);
   }

   public static boolean canBePlacedAt(net.minecraft.world.level.Level $$0, BlockPos $$1, Direction $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      return !$$3.isAir() ? false : getState($$0, $$1).canSurvive($$0, $$1) || isPortal($$0, $$1, $$2);
   }

   private static boolean isPortal(net.minecraft.world.level.Level $$0, BlockPos $$1, Direction $$2) {
      if (!inPortalDimension($$0)) {
         return false;
      } else {
         MutableBlockPos $$3 = $$1.mutable();
         boolean $$4 = false;

         for (Direction $$5 : Direction.values()) {
            if ($$0.getBlockState($$3.set($$1).move($$5)).is(Blocks.OBSIDIAN)) {
               $$4 = true;
               break;
            }
         }

         if (!$$4) {
            return false;
         } else {
            Axis $$6 = $$2.getAxis().isHorizontal() ? $$2.getCounterClockWise().getAxis() : Plane.HORIZONTAL.getRandomAxis($$0.random);
            return PortalShape.findEmptyPortalShape($$0, $$1, $$6).isPresent();
         }
      }
   }
}
