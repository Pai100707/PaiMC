package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LivingEntity.Fallsounds;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PowderSnowBlock extends Block implements BucketPickup {
   public static final MapCodec<PowderSnowBlock> CODEC = simpleCodec(PowderSnowBlock::new);
   private static final float HORIZONTAL_PARTICLE_MOMENTUM_FACTOR = 0.083333336F;
   private static final float IN_BLOCK_HORIZONTAL_SPEED_MULTIPLIER = 0.9F;
   private static final float IN_BLOCK_VERTICAL_SPEED_MULTIPLIER = 1.5F;
   private static final float NUM_BLOCKS_TO_FALL_INTO_BLOCK = 2.5F;
   private static final VoxelShape FALLING_COLLISION_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9F, 1.0);
   private static final double MINIMUM_FALL_DISTANCE_FOR_SOUND = 4.0;
   private static final double MINIMUM_FALL_DISTANCE_FOR_BIG_SOUND = 7.0;

   @Override
   public MapCodec<PowderSnowBlock> codec() {
      return CODEC;
   }

   public PowderSnowBlock(BlockBehaviour.Properties $$0) {
      super($$0);
   }

   @Override
   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      return $$1.is(this) ? true : super.skipRendering($$0, $$1, $$2);
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if (!($$3 instanceof LivingEntity) || $$3.getInBlockState().is(this)) {
         $$3.makeStuckInBlock($$0, new Vec3(0.9F, 1.5, 0.9F));
         if ($$1.isClientSide()) {
            RandomSource $$6 = $$1.getRandom();
            boolean $$7 = $$3.xOld != $$3.getX() || $$3.zOld != $$3.getZ();
            if ($$7 && $$6.nextBoolean()) {
               $$1.addParticle(
                  ParticleTypes.SNOWFLAKE,
                  $$3.getX(),
                  $$2.getY() + 1,
                  $$3.getZ(),
                  Mth.randomBetween($$6, -1.0F, 1.0F) * 0.083333336F,
                  0.05F,
                  Mth.randomBetween($$6, -1.0F, 1.0F) * 0.083333336F
               );
            }
         }
      }

      BlockPos $$8 = $$2.immutable();
      $$4.runBefore(
         InsideBlockEffectType.EXTINGUISH,
         $$2x -> {
            if ($$1 instanceof ServerLevel $$3x
               && $$2x.isOnFire()
               && ($$3x.getGameRules().get(GameRules.MOB_GRIEFING) || $$2x instanceof Player)
               && $$2x.mayInteract($$3x, $$8)) {
               $$1.destroyBlock($$8, false);
            }
         }
      );
      $$4.apply(InsideBlockEffectType.FREEZE);
      $$4.apply(InsideBlockEffectType.EXTINGUISH);
   }

   @Override
   public void fallOn(net.minecraft.world.level.Level $$0, BlockState $$1, BlockPos $$2, Entity $$3, double $$4) {
      if (!($$4 < 4.0) && $$3 instanceof LivingEntity $$5) {
         Fallsounds $$7 = $$5.getFallSounds();
         SoundEvent $$8 = $$4 < 7.0 ? $$7.small() : $$7.big();
         $$3.playSound($$8, 1.0F, 1.0F);
      }
   }

   @Override
   protected VoxelShape getEntityInsideCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, Entity $$3) {
      VoxelShape $$4 = this.getCollisionShape($$0, $$1, $$2, CollisionContext.of($$3));
      return $$4.isEmpty() ? Shapes.block() : $$4;
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      if (!$$3.isPlacement() && $$3 instanceof EntityCollisionContext $$4) {
         Entity $$5 = $$4.getEntity();
         if ($$5 != null) {
            if ($$5.fallDistance > 2.5) {
               return FALLING_COLLISION_SHAPE;
            }

            boolean $$6 = $$5 instanceof FallingBlockEntity;
            if ($$6 || canEntityWalkOnPowderSnow($$5) && $$3.isAbove(Shapes.block(), $$2, false) && !$$3.isDescending()) {
               return super.getCollisionShape($$0, $$1, $$2, $$3);
            }
         }
      }

      return Shapes.empty();
   }

   @Override
   protected VoxelShape getVisualShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return Shapes.empty();
   }

   public static boolean canEntityWalkOnPowderSnow(Entity $$0) {
      if ($$0.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
         return true;
      } else {
         return $$0 instanceof LivingEntity ? ((LivingEntity)$$0).getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS) : false;
      }
   }

   @Override
   public ItemStack pickupBlock(@Nullable LivingEntity $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      $$1.setBlock($$2, Blocks.AIR.defaultBlockState(), 11);
      if (!$$1.isClientSide()) {
         $$1.levelEvent(2001, $$2, Block.getId($$3));
      }

      return new ItemStack(Items.POWDER_SNOW_BUCKET);
   }

   @Override
   public Optional<SoundEvent> getPickupSound() {
      return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return true;
   }
}
