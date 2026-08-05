package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
   private static final Codec<FlowingFluid> FLOWING_FLUID = BuiltInRegistries.FLUID
      .byNameCodec()
      .comapFlatMap($$0 -> $$0 instanceof FlowingFluid $$1 ? DataResult.success($$1) : DataResult.error(() -> "Not a flowing fluid: " + $$0), $$0 -> $$0);
   public static final MapCodec<LiquidBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(FLOWING_FLUID.fieldOf("fluid").forGetter($$0x -> $$0x.fluid), propertiesCodec()).apply($$0, LiquidBlock::new)
   );
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   protected final FlowingFluid fluid;
   private final List<FluidState> stateCache;
   public static final VoxelShape SHAPE_STABLE = Block.column(16.0, 0.0, 8.0);
   public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(
      Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST
   );

   @Override
   public MapCodec<LiquidBlock> codec() {
      return CODEC;
   }

   protected LiquidBlock(FlowingFluid $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.fluid = $$0;
      this.stateCache = Lists.newArrayList();
      this.stateCache.add($$0.getSource(false));

      for (int $$2 = 1; $$2 < 8; $$2++) {
         this.stateCache.add($$0.getFlowing(8 - $$2, false));
      }

      this.stateCache.add($$0.getFlowing(8, true));
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      if ($$3.alwaysCollideWithFluid()) {
         return Shapes.block();
      } else {
         return $$3.isAbove(SHAPE_STABLE, $$2, true) && $$0.getValue(LEVEL) == 0 && $$3.canStandOnFluid($$1.getFluidState($$2.above()), $$0.getFluidState())
            ? SHAPE_STABLE
            : Shapes.empty();
      }
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getFluidState().isRandomlyTicking();
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      $$0.getFluidState().randomTick($$1, $$2, $$3);
   }

   @Override
   protected boolean propagatesSkylightDown(BlockState $$0) {
      return false;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return !this.fluid.is(FluidTags.LAVA);
   }

   @Override
   protected FluidState getFluidState(BlockState $$0) {
      int $$1 = $$0.getValue(LEVEL);
      return this.stateCache.get(Math.min($$1, 8));
   }

   @Override
   protected boolean skipRendering(BlockState $$0, BlockState $$1, Direction $$2) {
      return $$1.getFluidState().getType().isSame(this.fluid);
   }

   @Override
   protected RenderShape getRenderShape(BlockState $$0) {
      return RenderShape.INVISIBLE;
   }

   @Override
   protected List<ItemStack> getDrops(BlockState $$0, LootParams.Builder $$1) {
      return Collections.emptyList();
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return Shapes.empty();
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if (this.shouldSpreadLiquid($$1, $$2, $$0)) {
         $$1.scheduleTick($$2, $$0.getFluidState().getType(), this.fluid.getTickDelay($$1));
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
      if ($$0.getFluidState().isSource() || $$6.getFluidState().isSource()) {
         $$2.scheduleTick($$3, $$0.getFluidState().getType(), this.fluid.getTickDelay($$1));
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected void neighborChanged(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Block $$3, Orientation $$4, boolean $$5) {
      if (this.shouldSpreadLiquid($$1, $$2, $$0)) {
         $$1.scheduleTick($$2, $$0.getFluidState().getType(), this.fluid.getTickDelay($$1));
      }
   }

   private boolean shouldSpreadLiquid(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      if (this.fluid.is(FluidTags.LAVA)) {
         boolean $$3 = $$0.getBlockState($$1.below()).is(Blocks.SOUL_SOIL);
         UnmodifiableIterator var5 = POSSIBLE_FLOW_DIRECTIONS.iterator();

         while (var5.hasNext()) {
            Direction $$4 = (Direction)var5.next();
            BlockPos $$5 = $$1.relative($$4.getOpposite());
            if ($$0.getFluidState($$5).is(FluidTags.WATER)) {
               Block $$6 = $$0.getFluidState($$1).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
               $$0.setBlockAndUpdate($$1, $$6.defaultBlockState());
               this.fizz($$0, $$1);
               return false;
            }

            if ($$3 && $$0.getBlockState($$5).is(Blocks.BLUE_ICE)) {
               $$0.setBlockAndUpdate($$1, Blocks.BASALT.defaultBlockState());
               this.fizz($$0, $$1);
               return false;
            }
         }
      }

      return true;
   }

   private void fizz(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1) {
      $$0.levelEvent(1501, $$1, 0);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LEVEL);
   }

   @Override
   public ItemStack pickupBlock(LivingEntity $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, BlockState $$3) {
      if ($$3.getValue(LEVEL) == 0) {
         $$1.setBlock($$2, Blocks.AIR.defaultBlockState(), 11);
         return new ItemStack(this.fluid.getBucket());
      } else {
         return ItemStack.EMPTY;
      }
   }

   @Override
   public Optional<SoundEvent> getPickupSound() {
      return this.fluid.getPickupSound();
   }
}
