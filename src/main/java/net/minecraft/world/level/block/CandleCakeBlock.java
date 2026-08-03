package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CandleCakeBlock extends AbstractCandleBlock {
   public static final MapCodec<CandleCakeBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter($$0x -> $$0x.candleBlock), propertiesCodec())
         .apply($$0, CandleCakeBlock::new)
   );
   public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
   private static final VoxelShape SHAPE = Shapes.or(Block.column(2.0, 8.0, 14.0), Block.column(14.0, 0.0, 8.0));
   private static final Map<CandleBlock, CandleCakeBlock> BY_CANDLE = Maps.newHashMap();
   private static final Iterable<Vec3> PARTICLE_OFFSETS = List.of(new Vec3(8.0, 16.0, 8.0).scale(0.0625));
   private final CandleBlock candleBlock;

   @Override
   public MapCodec<CandleCakeBlock> codec() {
      return CODEC;
   }

   protected CandleCakeBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
      if ($$0 instanceof CandleBlock $$2) {
         BY_CANDLE.put($$2, this);
         this.candleBlock = $$2;
      } else {
         throw new IllegalArgumentException("Expected block to be of " + CandleBlock.class + " was " + $$0.getClass());
      }
   }

   @Override
   protected Iterable<Vec3> getParticleOffsets(BlockState $$0) {
      return PARTICLE_OFFSETS;
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      if ($$0.is(Items.FLINT_AND_STEEL) || $$0.is(Items.FIRE_CHARGE)) {
         return InteractionResult.PASS;
      } else if (candleHit($$6) && $$0.isEmpty() && $$1.getValue(LIT)) {
         extinguish($$4, $$1, $$2, $$3);
         return InteractionResult.SUCCESS;
      } else {
         return super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6);
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      InteractionResult $$5 = CakeBlock.eat($$1, $$2, Blocks.CAKE.defaultBlockState(), $$3);
      if ($$5.consumesAction()) {
         dropResources($$0, $$1, $$2);
      }

      return $$5;
   }

   private static boolean candleHit(BlockHitResult $$0) {
      return $$0.getLocation().y - $$0.getBlockPos().getY() > 0.5;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LIT);
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(Blocks.CAKE);
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
      return $$4 == Direction.DOWN && !$$0.canSurvive($$1, $$3) ? Blocks.AIR.defaultBlockState() : super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return $$1.getBlockState($$2.below()).isSolid();
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return CakeBlock.FULL_CAKE_SIGNAL;
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   public static BlockState byCandle(CandleBlock $$0) {
      return BY_CANDLE.get($$0).defaultBlockState();
   }

   public static boolean canLight(BlockState $$0) {
      return $$0.is(BlockTags.CANDLE_CAKES, $$1 -> $$1.hasProperty(LIT) && !$$0.getValue(LIT));
   }
}
