package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ComposterBlock extends Block implements WorldlyContainerHolder {
   public static final MapCodec<ComposterBlock> CODEC = simpleCodec(ComposterBlock::new);
   public static final int READY = 8;
   public static final int MIN_LEVEL = 0;
   public static final int MAX_LEVEL = 7;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
   public static final Object2FloatMap<net.minecraft.world.level.ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap();
   private static final int HOLE_WIDTH = 12;
   private static final VoxelShape[] SHAPES = (VoxelShape[])Util.make(
      () -> {
         VoxelShape[] $$0 = Block.boxes(
            8, $$0x -> Shapes.join(Shapes.block(), Block.column(12.0, Math.clamp((long)(1 + $$0x * 2), 2, 16), 16.0), BooleanOp.ONLY_FIRST)
         );
         $$0[8] = $$0[7];
         return $$0;
      }
   );

   @Override
   public MapCodec<ComposterBlock> codec() {
      return CODEC;
   }

   public static void bootStrap() {
      COMPOSTABLES.defaultReturnValue(-1.0F);
      float $$0 = 0.3F;
      float $$1 = 0.5F;
      float $$2 = 0.65F;
      float $$3 = 0.85F;
      float $$4 = 1.0F;
      add(0.3F, Items.JUNGLE_LEAVES);
      add(0.3F, Items.OAK_LEAVES);
      add(0.3F, Items.SPRUCE_LEAVES);
      add(0.3F, Items.DARK_OAK_LEAVES);
      add(0.3F, Items.PALE_OAK_LEAVES);
      add(0.3F, Items.ACACIA_LEAVES);
      add(0.3F, Items.CHERRY_LEAVES);
      add(0.3F, Items.BIRCH_LEAVES);
      add(0.3F, Items.AZALEA_LEAVES);
      add(0.3F, Items.MANGROVE_LEAVES);
      add(0.3F, Items.OAK_SAPLING);
      add(0.3F, Items.SPRUCE_SAPLING);
      add(0.3F, Items.BIRCH_SAPLING);
      add(0.3F, Items.JUNGLE_SAPLING);
      add(0.3F, Items.ACACIA_SAPLING);
      add(0.3F, Items.CHERRY_SAPLING);
      add(0.3F, Items.DARK_OAK_SAPLING);
      add(0.3F, Items.PALE_OAK_SAPLING);
      add(0.3F, Items.MANGROVE_PROPAGULE);
      add(0.3F, Items.BEETROOT_SEEDS);
      add(0.3F, Items.DRIED_KELP);
      add(0.3F, Items.SHORT_GRASS);
      add(0.3F, Items.KELP);
      add(0.3F, Items.MELON_SEEDS);
      add(0.3F, Items.PUMPKIN_SEEDS);
      add(0.3F, Items.SEAGRASS);
      add(0.3F, Items.SWEET_BERRIES);
      add(0.3F, Items.GLOW_BERRIES);
      add(0.3F, Items.WHEAT_SEEDS);
      add(0.3F, Items.MOSS_CARPET);
      add(0.3F, Items.PALE_MOSS_CARPET);
      add(0.3F, Items.PALE_HANGING_MOSS);
      add(0.3F, Items.PINK_PETALS);
      add(0.3F, Items.WILDFLOWERS);
      add(0.3F, Items.LEAF_LITTER);
      add(0.3F, Items.SMALL_DRIPLEAF);
      add(0.3F, Items.HANGING_ROOTS);
      add(0.3F, Items.MANGROVE_ROOTS);
      add(0.3F, Items.TORCHFLOWER_SEEDS);
      add(0.3F, Items.PITCHER_POD);
      add(0.3F, Items.FIREFLY_BUSH);
      add(0.3F, Items.BUSH);
      add(0.3F, Items.CACTUS_FLOWER);
      add(0.3F, Items.DRY_SHORT_GRASS);
      add(0.3F, Items.DRY_TALL_GRASS);
      add(0.5F, Items.DRIED_KELP_BLOCK);
      add(0.5F, Items.TALL_GRASS);
      add(0.5F, Items.FLOWERING_AZALEA_LEAVES);
      add(0.5F, Items.CACTUS);
      add(0.5F, Items.SUGAR_CANE);
      add(0.5F, Items.VINE);
      add(0.5F, Items.NETHER_SPROUTS);
      add(0.5F, Items.WEEPING_VINES);
      add(0.5F, Items.TWISTING_VINES);
      add(0.5F, Items.MELON_SLICE);
      add(0.5F, Items.GLOW_LICHEN);
      add(0.65F, Items.SEA_PICKLE);
      add(0.65F, Items.LILY_PAD);
      add(0.65F, Items.PUMPKIN);
      add(0.65F, Items.CARVED_PUMPKIN);
      add(0.65F, Items.MELON);
      add(0.65F, Items.APPLE);
      add(0.65F, Items.BEETROOT);
      add(0.65F, Items.CARROT);
      add(0.65F, Items.COCOA_BEANS);
      add(0.65F, Items.POTATO);
      add(0.65F, Items.WHEAT);
      add(0.65F, Items.BROWN_MUSHROOM);
      add(0.65F, Items.RED_MUSHROOM);
      add(0.65F, Items.MUSHROOM_STEM);
      add(0.65F, Items.CRIMSON_FUNGUS);
      add(0.65F, Items.WARPED_FUNGUS);
      add(0.65F, Items.NETHER_WART);
      add(0.65F, Items.CRIMSON_ROOTS);
      add(0.65F, Items.WARPED_ROOTS);
      add(0.65F, Items.SHROOMLIGHT);
      add(0.65F, Items.DANDELION);
      add(0.65F, Items.POPPY);
      add(0.65F, Items.BLUE_ORCHID);
      add(0.65F, Items.ALLIUM);
      add(0.65F, Items.AZURE_BLUET);
      add(0.65F, Items.RED_TULIP);
      add(0.65F, Items.ORANGE_TULIP);
      add(0.65F, Items.WHITE_TULIP);
      add(0.65F, Items.PINK_TULIP);
      add(0.65F, Items.OXEYE_DAISY);
      add(0.65F, Items.CORNFLOWER);
      add(0.65F, Items.LILY_OF_THE_VALLEY);
      add(0.65F, Items.WITHER_ROSE);
      add(0.65F, Items.OPEN_EYEBLOSSOM);
      add(0.65F, Items.CLOSED_EYEBLOSSOM);
      add(0.65F, Items.FERN);
      add(0.65F, Items.SUNFLOWER);
      add(0.65F, Items.LILAC);
      add(0.65F, Items.ROSE_BUSH);
      add(0.65F, Items.PEONY);
      add(0.65F, Items.LARGE_FERN);
      add(0.65F, Items.SPORE_BLOSSOM);
      add(0.65F, Items.AZALEA);
      add(0.65F, Items.MOSS_BLOCK);
      add(0.65F, Items.PALE_MOSS_BLOCK);
      add(0.65F, Items.BIG_DRIPLEAF);
      add(0.85F, Items.HAY_BLOCK);
      add(0.85F, Items.BROWN_MUSHROOM_BLOCK);
      add(0.85F, Items.RED_MUSHROOM_BLOCK);
      add(0.85F, Items.NETHER_WART_BLOCK);
      add(0.85F, Items.WARPED_WART_BLOCK);
      add(0.85F, Items.FLOWERING_AZALEA);
      add(0.85F, Items.BREAD);
      add(0.85F, Items.BAKED_POTATO);
      add(0.85F, Items.COOKIE);
      add(0.85F, Items.TORCHFLOWER);
      add(0.85F, Items.PITCHER_PLANT);
      add(1.0F, Items.CAKE);
      add(1.0F, Items.PUMPKIN_PIE);
   }

   private static void add(float $$0, net.minecraft.world.level.ItemLike $$1) {
      COMPOSTABLES.put($$1.asItem(), $$0);
   }

   public ComposterBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
   }

   public static void handleFill(net.minecraft.world.level.Level $$0, BlockPos $$1, boolean $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      $$0.playLocalSound($$1, $$2 ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      double $$4 = $$3.getShape($$0, $$1).max(Axis.Y, 0.5, 0.5) + 0.03125;
      double $$5 = 2.0;
      double $$6 = 0.1875;
      double $$7 = 0.625;
      RandomSource $$8 = $$0.getRandom();

      for (int $$9 = 0; $$9 < 10; $$9++) {
         double $$10 = $$8.nextGaussian() * 0.02;
         double $$11 = $$8.nextGaussian() * 0.02;
         double $$12 = $$8.nextGaussian() * 0.02;
         $$0.addParticle(
            ParticleTypes.COMPOSTER,
            $$1.getX() + 0.1875 + 0.625 * $$8.nextFloat(),
            $$1.getY() + $$4 + $$8.nextFloat() * (1.0 - $$4),
            $$1.getZ() + 0.1875 + 0.625 * $$8.nextFloat(),
            $$10,
            $$11,
            $$12
         );
      }
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[$$0.getValue(LEVEL)];
   }

   @Override
   protected VoxelShape getInteractionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return Shapes.block();
   }

   @Override
   protected VoxelShape getCollisionShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[0];
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      if ($$0.getValue(LEVEL) == 7) {
         $$1.scheduleTick($$2, $$0.getBlock(), 20);
      }
   }

   @Override
   protected InteractionResult useItemOn(
      ItemStack $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3, Player $$4, InteractionHand $$5, BlockHitResult $$6
   ) {
      int $$7 = $$1.getValue(LEVEL);
      if ($$7 < 8 && COMPOSTABLES.containsKey($$0.getItem())) {
         if ($$7 < 7 && !$$2.isClientSide()) {
            BlockState $$8 = addItem($$4, $$1, $$2, $$3, $$0);
            $$2.levelEvent(1500, $$3, $$1 != $$8 ? 1 : 0);
            $$4.awardStat(Stats.ITEM_USED.get($$0.getItem()));
            $$0.consume(1, $$4);
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.useItemOn($$0, $$1, $$2, $$3, $$4, $$5, $$6);
      }
   }

   @Override
   protected InteractionResult useWithoutItem(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Player $$3, BlockHitResult $$4) {
      int $$5 = $$0.getValue(LEVEL);
      if ($$5 == 8) {
         extractProduce($$3, $$0, $$1, $$2);
         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   public static BlockState insertItem(Entity $$0, BlockState $$1, ServerLevel $$2, ItemStack $$3, BlockPos $$4) {
      int $$5 = $$1.getValue(LEVEL);
      if ($$5 < 7 && COMPOSTABLES.containsKey($$3.getItem())) {
         BlockState $$6 = addItem($$0, $$1, $$2, $$4, $$3);
         $$3.shrink(1);
         return $$6;
      } else {
         return $$1;
      }
   }

   public static BlockState extractProduce(Entity $$0, BlockState $$1, net.minecraft.world.level.Level $$2, BlockPos $$3) {
      if (!$$2.isClientSide()) {
         Vec3 $$4 = Vec3.atLowerCornerWithOffset($$3, 0.5, 1.01, 0.5).offsetRandomXZ($$2.random, 0.7F);
         ItemEntity $$5 = new ItemEntity($$2, $$4.x(), $$4.y(), $$4.z(), new ItemStack(Items.BONE_MEAL));
         $$5.setDefaultPickUpDelay();
         $$2.addFreshEntity($$5);
      }

      BlockState $$6 = empty($$0, $$1, $$2, $$3);
      $$2.playSound(null, $$3, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
      return $$6;
   }

   static BlockState empty(Entity $$0, BlockState $$1, net.minecraft.world.level.LevelAccessor $$2, BlockPos $$3) {
      BlockState $$4 = $$1.setValue(LEVEL, 0);
      $$2.setBlock($$3, $$4, 3);
      $$2.gameEvent(GameEvent.BLOCK_CHANGE, $$3, GameEvent.Context.of($$0, $$4));
      return $$4;
   }

   static BlockState addItem(Entity $$0, BlockState $$1, net.minecraft.world.level.LevelAccessor $$2, BlockPos $$3, ItemStack $$4) {
      int $$5 = $$1.getValue(LEVEL);
      float $$6 = COMPOSTABLES.getFloat($$4.getItem());
      if (($$5 != 0 || !($$6 > 0.0F)) && !($$2.getRandom().nextDouble() < $$6)) {
         return $$1;
      } else {
         int $$7 = $$5 + 1;
         BlockState $$8 = $$1.setValue(LEVEL, $$7);
         $$2.setBlock($$3, $$8, 3);
         $$2.gameEvent(GameEvent.BLOCK_CHANGE, $$3, GameEvent.Context.of($$0, $$8));
         if ($$7 == 7) {
            $$2.scheduleTick($$3, $$1.getBlock(), 20);
         }

         return $$8;
      }
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$0.getValue(LEVEL) == 7) {
         $$1.setBlock($$2, $$0.cycle(LEVEL), 3);
         $$1.playSound(null, $$2, SoundEvents.COMPOSTER_READY, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   @Override
   protected boolean hasAnalogOutputSignal(BlockState $$0) {
      return true;
   }

   @Override
   protected int getAnalogOutputSignal(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Direction $$3) {
      return $$0.getValue(LEVEL);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(LEVEL);
   }

   @Override
   protected boolean isPathfindable(BlockState $$0, PathComputationType $$1) {
      return false;
   }

   public WorldlyContainer getContainer(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
      int $$3 = $$0.getValue(LEVEL);
      if ($$3 == 8) {
         return new ComposterBlock.OutputContainer($$0, $$1, $$2, new ItemStack(Items.BONE_MEAL));
      } else {
         return (WorldlyContainer)($$3 < 7 ? new ComposterBlock.InputContainer($$0, $$1, $$2) : new ComposterBlock.EmptyContainer());
      }
   }

   static class EmptyContainer extends SimpleContainer implements WorldlyContainer {
      public EmptyContainer() {
         super(0);
      }

      public int[] getSlotsForFace(Direction $$0) {
         return new int[0];
      }

      public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return false;
      }

      public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return false;
      }
   }

   static class InputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final net.minecraft.world.level.LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public InputContainer(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2) {
         super(1);
         this.state = $$0;
         this.level = $$1;
         this.pos = $$2;
      }

      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction $$0) {
         return $$0 == Direction.UP ? new int[]{0} : new int[0];
      }

      public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return !this.changed && $$2 == Direction.UP && ComposterBlock.COMPOSTABLES.containsKey($$1.getItem());
      }

      public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return false;
      }

      public void setChanged() {
         ItemStack $$0 = this.getItem(0);
         if (!$$0.isEmpty()) {
            this.changed = true;
            BlockState $$1 = ComposterBlock.addItem(null, this.state, this.level, this.pos, $$0);
            this.level.levelEvent(1500, this.pos, $$1 != this.state ? 1 : 0);
            this.removeItemNoUpdate(0);
         }
      }
   }

   static class OutputContainer extends SimpleContainer implements WorldlyContainer {
      private final BlockState state;
      private final net.minecraft.world.level.LevelAccessor level;
      private final BlockPos pos;
      private boolean changed;

      public OutputContainer(BlockState $$0, net.minecraft.world.level.LevelAccessor $$1, BlockPos $$2, ItemStack $$3) {
         super(new ItemStack[]{$$3});
         this.state = $$0;
         this.level = $$1;
         this.pos = $$2;
      }

      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction $$0) {
         return $$0 == Direction.DOWN ? new int[]{0} : new int[0];
      }

      public boolean canPlaceItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return false;
      }

      public boolean canTakeItemThroughFace(int $$0, ItemStack $$1, Direction $$2) {
         return !this.changed && $$2 == Direction.DOWN && $$1.is(Items.BONE_MEAL);
      }

      public void setChanged() {
         ComposterBlock.empty(null, this.state, this.level, this.pos);
         this.changed = true;
      }
   }
}
