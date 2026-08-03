package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnConfig;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RespawnAnchorBlock extends Block {
   public static final MapCodec<RespawnAnchorBlock> CODEC = simpleCodec(RespawnAnchorBlock::new);
   public static final int MIN_CHARGES = 0;
   public static final int MAX_CHARGES = 4;
   public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
   private static final ImmutableList<Vec3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(
      new Vec3i(0, 0, -1),
      new Vec3i(-1, 0, 0),
      new Vec3i(0, 0, 1),
      new Vec3i(1, 0, 0),
      new Vec3i(-1, 0, -1),
      new Vec3i(1, 0, -1),
      new Vec3i(-1, 0, 1),
      new Vec3i(1, 0, 1)
   );
   private static final ImmutableList<Vec3i> RESPAWN_OFFSETS = new Builder()
      .addAll(RESPAWN_HORIZONTAL_OFFSETS)
      .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::below).iterator())
      .addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vec3i::above).iterator())
      .add(new Vec3i(0, 1, 0))
      .build();

   @Override
   public MapCodec<RespawnAnchorBlock> codec() {
      return CODEC;
   }

   public RespawnAnchorBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, 0));
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if (isRespawnFuel($$0) && canBeCharged($$1)) {
         charge($$4, $$2, $$3, $$1);
         $$0.consume(1, $$4);
         return InteractionResult.SUCCESS;
      } else {
         return (InteractionResult)($$5 == InteractionHand.MAIN_HAND && isRespawnFuel($$4.getItemInHand(InteractionHand.OFF_HAND)) && canBeCharged($$1)
            ? InteractionResult.PASS
            : InteractionResult.TRY_WITH_EMPTY_HAND);
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      if ($$0.getValue(CHARGE) == 0) {
         return InteractionResult.PASS;
      } else if ($$1 instanceof ServerLevel $$5) {
         if (!canSetSpawn($$5, $$2)) {
            this.explode($$0, $$5, $$2);
            return InteractionResult.SUCCESS_SERVER;
         } else {
            if ($$3 instanceof ServerPlayer $$7) {
               RespawnConfig $$8 = $$7.getRespawnConfig();
               RespawnConfig $$9 = new RespawnConfig(LevelData.RespawnData.of($$5.dimension(), $$2, 0.0F, 0.0F), false);
               if ($$8 == null || !$$8.isSamePosition($$9)) {
                  $$7.setRespawnPosition($$9, true);
                  $$5.playSound(
                     null, $$2.getX() + 0.5, $$2.getY() + 0.5, $$2.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F
                  );
                  return InteractionResult.SUCCESS_SERVER;
               }
            }

            return InteractionResult.CONSUME;
         }
      } else {
         return InteractionResult.CONSUME;
      }
   }

   private static boolean isRespawnFuel(ItemStack $$0) {
      return $$0.is(Items.GLOWSTONE);
   }

   private static boolean canBeCharged(BlockState $$0) {
      return $$0.getValue(CHARGE) < 4;
   }

   private static boolean isWaterThatWouldFlow(BlockPos $$0, net.minecraft.world.level.Level $$1) {
      FluidState $$2 = $$1.getFluidState($$0);
      if (!$$2.is(FluidTags.WATER)) {
         return false;
      } else if ($$2.isSource()) {
         return true;
      } else {
         float $$3 = $$2.getAmount();
         if ($$3 < 2.0F) {
            return false;
         } else {
            FluidState $$4 = $$1.getFluidState($$0.below());
            return !$$4.is(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState $$0, ServerLevel $$1, final BlockPos $$2) {
      $$1.removeBlock($$2, false);
      boolean $$3 = Plane.HORIZONTAL.stream().<BlockPos>map($$2::relative).anyMatch($$1x -> isWaterThatWouldFlow($$1x, $$1));
      final boolean $$4 = $$3 || $$1.getFluidState($$2.above()).is(FluidTags.WATER);
      net.minecraft.world.level.ExplosionDamageCalculator $$5 = new net.minecraft.world.level.ExplosionDamageCalculator() {
         @Override
         public Optional<Float> getBlockExplosionResistance(
            net.minecraft.world.level.Explosion $$0, net.minecraft.world.level.BlockGetter $$1x, BlockPos $$2x, BlockState $$3x, FluidState $$4x
         ) {
            return $$2.equals($$2) && $$4 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance($$0, $$1x, $$2, $$3x, $$4);
         }
      };
      Vec3 $$6 = $$2.getCenter();
      $$1.explode(null, $$1.damageSources().badRespawnPointExplosion($$6), $$5, $$6, 5.0F, true, net.minecraft.world.level.Level.ExplosionInteraction.BLOCK);
   }

   public static boolean canSetSpawn(ServerLevel $$0, BlockPos $$1) {
      return (Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, $$1);
   }

   public static void charge(@Nullable Entity $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3) {
      BlockState $$4 = $$3.setValue(CHARGE, $$3.getValue(CHARGE) + 1);
      $$1.setBlock($$2, $$4, 3);
      $$1.gameEvent(GameEvent.BLOCK_CHANGE, $$2, GameEvent.Context.of($$0, $$4));
      $$1.playSound(null, $$2.getX() + 0.5, $$2.getY() + 0.5, $$2.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.0F);
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(CHARGE) != 0) {
         if ($$3.nextInt(100) == 0) {
            $$1.playLocalSound($$2, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }

         double $$4 = $$2.getX() + 0.5 + (0.5 - $$3.nextDouble());
         double $$5 = $$2.getY() + 1.0;
         double $$6 = $$2.getZ() + 0.5 + (0.5 - $$3.nextDouble());
         double $$7 = $$3.nextFloat() * 0.04;
         $$1.addParticle(ParticleTypes.REVERSE_PORTAL, $$4, $$5, $$6, 0.0, $$7, 0.0);
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(CHARGE);
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   public static int getScaledChargeLevel(BlockState $$0, int $$1) {
      return Mth.floor(($$0.getValue(CHARGE) - 0) / 4.0F * $$1);
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return getScaledChargeLevel($$0, 15);
   }

   public static Optional<Vec3> findStandUpPosition(EntityType<?> $$0, net.minecraft.world.level.CollisionGetter $$1, BlockPos $$2) {
      Optional<Vec3> $$3 = findStandUpPosition($$0, $$1, $$2, true);
      return $$3.isPresent() ? $$3 : findStandUpPosition($$0, $$1, $$2, false);
   }

   private static Optional<Vec3> findStandUpPosition(EntityType<?> $$0, net.minecraft.world.level.CollisionGetter $$1, BlockPos $$2, boolean $$3) {
      MutableBlockPos $$4 = new MutableBlockPos();
      UnmodifiableIterator var5 = RESPAWN_OFFSETS.iterator();

      while (var5.hasNext()) {
         Vec3i $$5 = (Vec3i)var5.next();
         $$4.set($$2).move($$5);
         Vec3 $$6 = DismountHelper.findSafeDismountLocation($$0, $$1, $$4, $$3);
         if ($$6 != null) {
            return Optional.of($$6);
         }
      }

      return Optional.empty();
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }
}
