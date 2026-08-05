package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BubbleColumnBlock extends Block implements BucketPickup {
   public static final MapCodec<BubbleColumnBlock> CODEC = simpleCodec(BubbleColumnBlock::new);
   public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;
   private static final int CHECK_PERIOD = 5;

   @Override
   public MapCodec<BubbleColumnBlock> codec() {
      return CODEC;
   }

   public BubbleColumnBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, true));
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$5) {
         BlockState $$6 = $$1.getBlockState($$2.above());
         boolean $$7 = $$6.getCollisionShape($$1, $$2).isEmpty() && $$6.getFluidState().isEmpty();
         if ($$7) {
            $$3.onAboveBubbleColumn($$0.getValue(DRAG_DOWN), $$2);
         } else {
            $$3.onInsideBubbleColumn($$0.getValue(DRAG_DOWN));
         }
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      updateColumn($$1, $$2, $$0, $$1.getBlockState($$2.below()));
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      return Fluids.WATER.getSource(false);
   }

   public static void updateColumn(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2) {
      updateColumn($$0, $$1, $$0.getBlockState($$1), $$2);
   }

   public static void updateColumn(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, BlockState $$3) {
      if (canExistIn($$2)) {
         BlockState $$4 = getColumnState($$3);
         $$0.setBlock($$1, $$4, 2);
         MutableBlockPos $$5 = $$1.mutable().move(Direction.UP);

         while (canExistIn($$0.getBlockState($$5))) {
            if (!$$0.setBlock($$5, $$4, 2)) {
               return;
            }

            $$5.move(Direction.UP);
         }
      }
   }

   private static boolean canExistIn(BlockState $$0) {
      return $$0.is(Blocks.BUBBLE_COLUMN) || $$0.is(Blocks.WATER) && $$0.getFluidState().getAmount() >= 8 && $$0.getFluidState().isSource();
   }

   private static BlockState getColumnState(BlockState $$0) {
      if ($$0.is(Blocks.BUBBLE_COLUMN)) {
         return $$0;
      } else if ($$0.is(Blocks.SOUL_SAND)) {
         return Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, false);
      } else {
         return $$0.is(Blocks.MAGMA_BLOCK) ? Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, true) : Blocks.WATER.defaultBlockState();
      }
   }

   @Override
   public void animateTick(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, RandomSource $$3) {
      double $$4 = $$2.getX();
      double $$5 = $$2.getY();
      double $$6 = $$2.getZ();
      if ($$0.getValue(DRAG_DOWN)) {
         $$1.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, $$4 + 0.5, $$5 + 0.8, $$6, 0.0, 0.0, 0.0);
         if ($$3.nextInt(200) == 0) {
            $$1.playLocalSound(
               $$4,
               $$5,
               $$6,
               SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
               SoundSource.BLOCKS,
               0.2F + $$3.nextFloat() * 0.2F,
               0.9F + $$3.nextFloat() * 0.15F,
               false
            );
         }
      } else {
         $$1.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, $$4 + 0.5, $$5, $$6 + 0.5, 0.0, 0.04, 0.0);
         $$1.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, $$4 + $$3.nextFloat(), $$5 + $$3.nextFloat(), $$6 + $$3.nextFloat(), 0.0, 0.04, 0.0);
         if ($$3.nextInt(200) == 0) {
            $$1.playLocalSound(
               $$4,
               $$5,
               $$6,
               SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
               SoundSource.BLOCKS,
               0.2F + $$3.nextFloat() * 0.2F,
               0.9F + $$3.nextFloat() * 0.15F,
               false
            );
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
      $$2.scheduleTick($$3, Fluids.WATER, Fluids.WATER.getTickDelay($$1));
      if (!$$0.canSurvive($$1, $$3) || $$4 == Direction.DOWN || $$4 == Direction.UP && !$$6.is(Blocks.BUBBLE_COLUMN) && canExistIn($$6)) {
         $$2.scheduleTick($$3, this, 5);
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.below());
      return $$3.is(Blocks.BUBBLE_COLUMN) || $$3.is(Blocks.MAGMA_BLOCK) || $$3.is(Blocks.SOUL_SAND);
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return Shapes.empty();
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(DRAG_DOWN);
   }

   @Override
   public ItemStack pickupBlock(LivingEntity $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      $$1.setBlock($$2, Blocks.AIR.defaultBlockState(), 11);
      return new ItemStack(Items.WATER_BUCKET);
   }

   @Override
   public Optional<SoundEvent> getPickupSound() {
      return Fluids.WATER.getPickupSound();
   }
}
