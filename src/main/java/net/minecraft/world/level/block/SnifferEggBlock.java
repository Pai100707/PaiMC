package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SnifferEggBlock extends Block {
   public static final MapCodec<SnifferEggBlock> CODEC = simpleCodec(SnifferEggBlock::new);
   public static final int MAX_HATCH_LEVEL = 2;
   public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
   private static final int REGULAR_HATCH_TIME_TICKS = 24000;
   private static final int BOOSTED_HATCH_TIME_TICKS = 12000;
   private static final int RANDOM_HATCH_OFFSET_TICKS = 300;
   private static final VoxelShape SHAPE = Block.column(14.0, 12.0, 0.0, 16.0);

   @Override
   public MapCodec<SnifferEggBlock> codec() {
      return CODEC;
   }

   public SnifferEggBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(HATCH);
   }

   @Override
   public VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPE;
   }

   public int getHatchLevel(BlockState $$0) {
      return $$0.getValue(HATCH);
   }

   private boolean isReadyToHatch(BlockState $$0) {
      return this.getHatchLevel($$0) == 2;
   }

   @Override
   public void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!this.isReadyToHatch($$0)) {
         $$1.playSound(null, $$2, SoundEvents.SNIFFER_EGG_CRACK, SoundSource.BLOCKS, 0.7F, 0.9F + $$3.nextFloat() * 0.2F);
         $$1.setBlock($$2, $$0.setValue(HATCH, this.getHatchLevel($$0) + 1), 2);
      } else {
         $$1.playSound(null, $$2, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.BLOCKS, 0.7F, 0.9F + $$3.nextFloat() * 0.2F);
         $$1.destroyBlock($$2, false);
         Sniffer $$4 = (Sniffer)EntityType.SNIFFER.create($$1, EntitySpawnReason.BREEDING);
         if ($$4 != null) {
            Vec3 $$5 = $$2.getCenter();
            $$4.setBaby(true);
            $$4.snapTo($$5.x(), $$5.y(), $$5.z(), Mth.wrapDegrees($$1.random.nextFloat() * 360.0F), 0.0F);
            $$1.addFreshEntity($$4);
         }
      }
   }

   @Override
   public void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      boolean $$5 = hatchBoost($$1, $$2);
      if (!$$1.isClientSide() && $$5) {
         $$1.levelEvent(3009, $$2, 0);
      }

      int $$6 = $$5 ? 12000 : 24000;
      int $$7 = $$6 / 3;
      $$1.gameEvent(GameEvent.BLOCK_PLACE, $$2, GameEvent.Context.of($$0));
      $$1.scheduleTick($$2, this, $$7 + $$1.random.nextInt(300));
   }

   @Override
   public boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   public static boolean hatchBoost(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      return $$0.getBlockState($$1.below()).is(BlockTags.SNIFFER_EGG_HATCH_BOOST);
   }
}
