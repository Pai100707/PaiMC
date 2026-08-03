package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FireBlock extends BaseFireBlock {
   public static final MapCodec<FireBlock> CODEC = simpleCodec(FireBlock::new);
   public static final int MAX_AGE = 15;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
   public static final BooleanProperty NORTH = PipeBlock.NORTH;
   public static final BooleanProperty EAST = PipeBlock.EAST;
   public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
   public static final BooleanProperty WEST = PipeBlock.WEST;
   public static final BooleanProperty UP = PipeBlock.UP;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION
      .entrySet()
      .stream()
      .filter($$0 -> $$0.getKey() != Direction.DOWN)
      .collect(Util.toMap());
   private final Function<BlockState, VoxelShape> shapes;
   private static final int IGNITE_INSTANT = 60;
   private static final int IGNITE_EASY = 30;
   private static final int IGNITE_MEDIUM = 15;
   private static final int IGNITE_HARD = 5;
   private static final int BURN_INSTANT = 100;
   private static final int BURN_EASY = 60;
   private static final int BURN_MEDIUM = 20;
   private static final int BURN_HARD = 5;
   private final Object2IntMap<Block> igniteOdds = new Object2IntOpenHashMap();
   private final Object2IntMap<Block> burnOdds = new Object2IntOpenHashMap();

   @Override
   public MapCodec<FireBlock> codec() {
      return CODEC;
   }

   public FireBlock(BlockBehaviour.Properties $$0) {
      super($$0, 1.0F);
      this.registerDefaultState(
         this.stateDefinition
            .any()
            .setValue(AGE, 0)
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
      );
      this.shapes = this.makeShapes();
   }

   private Function<BlockState, VoxelShape> makeShapes() {
      Map<Direction, VoxelShape> $$0 = Shapes.rotateAll(Block.boxZ(16.0, 0.0, 1.0));
      return this.getShapeForEachState($$1 -> {
         VoxelShape $$2 = Shapes.empty();

         for (Entry<Direction, BooleanProperty> $$3 : PROPERTY_BY_DIRECTION.entrySet()) {
            if ($$1.getValue($$3.getValue())) {
               $$2 = Shapes.or($$2, $$0.get($$3.getKey()));
            }
         }

         return $$2.isEmpty() ? SHAPE : $$2;
      }, new Property[]{AGE});
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
      return this.canSurvive($$0, $$1, $$3) ? this.getStateWithAge($$1, $$3, $$0.getValue(AGE)) : Blocks.AIR.defaultBlockState();
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return this.shapes.apply($$0);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext $$0) {
      return this.getStateForPlacement($$0.getLevel(), $$0.getClickedPos());
   }

   protected BlockState getStateForPlacement(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      BlockPos $$2 = $$1.below();
      BlockState $$3 = $$0.getBlockState($$2);
      if (!this.canBurn($$3) && !$$3.isFaceSturdy($$0, $$2, Direction.UP)) {
         BlockState $$4 = this.defaultBlockState();

         for (Direction $$5 : Direction.values()) {
            BooleanProperty $$6 = PROPERTY_BY_DIRECTION.get($$5);
            if ($$6 != null) {
               $$4 = $$4.setValue($$6, this.canBurn($$0.getBlockState($$1.relative($$5))));
            }
         }

         return $$4;
      } else {
         return this.defaultBlockState();
      }
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockPos $$3 = $$2.below();
      return $$1.getBlockState($$3).isFaceSturdy($$1, $$3, Direction.UP) || this.isValidFireLocation($$1, $$2);
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      $$1.scheduleTick($$2, this, getFireTickDelay($$1.random));
      if ($$1.canSpreadFireAround($$2)) {
         if (!$$0.canSurvive($$1, $$2)) {
            $$1.removeBlock($$2, false);
         }

         BlockState $$4 = $$1.getBlockState($$2.below());
         boolean $$5 = $$4.is($$1.dimensionType().infiniburn());
         int $$6 = $$0.getValue(AGE);
         if (!$$5 && $$1.isRaining() && this.isNearRain($$1, $$2) && $$3.nextFloat() < 0.2F + $$6 * 0.03F) {
            $$1.removeBlock($$2, false);
         } else {
            int $$7 = Math.min(15, $$6 + $$3.nextInt(3) / 2);
            if ($$6 != $$7) {
               $$0 = $$0.setValue(AGE, $$7);
               $$1.setBlock($$2, $$0, 260);
            }

            if (!$$5) {
               if (!this.isValidFireLocation($$1, $$2)) {
                  BlockPos $$8 = $$2.below();
                  if (!$$1.getBlockState($$8).isFaceSturdy($$1, $$8, Direction.UP) || $$6 > 3) {
                     $$1.removeBlock($$2, false);
                  }

                  return;
               }

               if ($$6 == 15 && $$3.nextInt(4) == 0 && !this.canBurn($$1.getBlockState($$2.below()))) {
                  $$1.removeBlock($$2, false);
                  return;
               }
            }

            boolean $$9 = (Boolean)$$1.environmentAttributes().getValue(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, $$2);
            int $$10 = $$9 ? -50 : 0;
            this.checkBurnOut($$1, $$2.east(), 300 + $$10, $$3, $$6);
            this.checkBurnOut($$1, $$2.west(), 300 + $$10, $$3, $$6);
            this.checkBurnOut($$1, $$2.below(), 250 + $$10, $$3, $$6);
            this.checkBurnOut($$1, $$2.above(), 250 + $$10, $$3, $$6);
            this.checkBurnOut($$1, $$2.north(), 300 + $$10, $$3, $$6);
            this.checkBurnOut($$1, $$2.south(), 300 + $$10, $$3, $$6);
            MutableBlockPos $$11 = new MutableBlockPos();

            for (int $$12 = -1; $$12 <= 1; $$12++) {
               for (int $$13 = -1; $$13 <= 1; $$13++) {
                  for (int $$14 = -1; $$14 <= 4; $$14++) {
                     if ($$12 != 0 || $$14 != 0 || $$13 != 0) {
                        int $$15 = 100;
                        if ($$14 > 1) {
                           $$15 += ($$14 - 1) * 100;
                        }

                        $$11.setWithOffset($$2, $$12, $$14, $$13);
                        int $$16 = this.getIgniteOdds($$1, $$11);
                        if ($$16 > 0) {
                           int $$17 = ($$16 + 40 + $$1.getDifficulty().getId() * 7) / ($$6 + 30);
                           if ($$9) {
                              $$17 /= 2;
                           }

                           if ($$17 > 0 && $$3.nextInt($$15) <= $$17 && (!$$1.isRaining() || !this.isNearRain($$1, $$11))) {
                              int $$18 = Math.min(15, $$6 + $$3.nextInt(5) / 4);
                              $$1.setBlock($$11, this.getStateWithAge($$1, $$11, $$18), 3);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected boolean isNearRain(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      return $$0.isRainingAt($$1) || $$0.isRainingAt($$1.west()) || $$0.isRainingAt($$1.east()) || $$0.isRainingAt($$1.north()) || $$0.isRainingAt($$1.south());
   }

   private int getBurnOdds(BlockState $$0) {
      return $$0.hasProperty(BlockStateProperties.WATERLOGGED) && $$0.getValue(BlockStateProperties.WATERLOGGED) ? 0 : this.burnOdds.getInt($$0.getBlock());
   }

   private int getIgniteOdds(BlockState $$0) {
      return $$0.hasProperty(BlockStateProperties.WATERLOGGED) && $$0.getValue(BlockStateProperties.WATERLOGGED) ? 0 : this.igniteOdds.getInt($$0.getBlock());
   }

   private void checkBurnOut(net.minecraft.world.level.Level $$0, BlockPos $$1, int $$2, RandomSource $$3, int $$4) {
      int $$5 = this.getBurnOdds($$0.getBlockState($$1));
      if ($$3.nextInt($$2) < $$5) {
         BlockState $$6 = $$0.getBlockState($$1);
         if ($$3.nextInt($$4 + 10) < 5 && !$$0.isRainingAt($$1)) {
            int $$7 = Math.min($$4 + $$3.nextInt(5) / 4, 15);
            $$0.setBlock($$1, this.getStateWithAge($$0, $$1, $$7), 3);
         } else {
            $$0.removeBlock($$1, false);
         }

         Block $$8 = $$6.getBlock();
         if ($$8 instanceof TntBlock) {
            TntBlock.prime($$0, $$1);
         }
      }
   }

   private BlockState getStateWithAge(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, int $$2) {
      BlockState $$3 = getState($$0, $$1);
      return $$3.is(Blocks.FIRE) ? $$3.setValue(AGE, $$2) : $$3;
   }

   private boolean isValidFireLocation(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      for (Direction $$2 : Direction.values()) {
         if (this.canBurn($$0.getBlockState($$1.relative($$2)))) {
            return true;
         }
      }

      return false;
   }

   private int getIgniteOdds(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      if (!$$0.isEmptyBlock($$1)) {
         return 0;
      } else {
         int $$2 = 0;

         for (Direction $$3 : Direction.values()) {
            BlockState $$4 = $$0.getBlockState($$1.relative($$3));
            $$2 = Math.max(this.getIgniteOdds($$4), $$2);
         }

         return $$2;
      }
   }

   @Override
   protected boolean canBurn(BlockState $$0) {
      return this.getIgniteOdds($$0) > 0;
   }

   @Override
   protected void onPlace(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, BlockState $$3, boolean $$4) {
      super.onPlace($$0, $$1, $$2, $$3, $$4);
      $$1.scheduleTick($$2, this, getFireTickDelay($$1.random));
   }

   private static int getFireTickDelay(RandomSource $$0) {
      return 30 + $$0.nextInt(10);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
   }

   public void setFlammable(Block $$0, int $$1, int $$2) {
      this.igniteOdds.put($$0, $$1);
      this.burnOdds.put($$0, $$2);
   }

   public static void bootStrap() {
      FireBlock $$0 = (FireBlock)Blocks.FIRE;
      $$0.setFlammable(Blocks.OAK_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.SPRUCE_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.BIRCH_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.JUNGLE_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.ACACIA_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.CHERRY_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.DARK_OAK_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.PALE_OAK_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.MANGROVE_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_PLANKS, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_MOSAIC, 5, 20);
      $$0.setFlammable(Blocks.OAK_SLAB, 5, 20);
      $$0.setFlammable(Blocks.SPRUCE_SLAB, 5, 20);
      $$0.setFlammable(Blocks.BIRCH_SLAB, 5, 20);
      $$0.setFlammable(Blocks.JUNGLE_SLAB, 5, 20);
      $$0.setFlammable(Blocks.ACACIA_SLAB, 5, 20);
      $$0.setFlammable(Blocks.CHERRY_SLAB, 5, 20);
      $$0.setFlammable(Blocks.DARK_OAK_SLAB, 5, 20);
      $$0.setFlammable(Blocks.PALE_OAK_SLAB, 5, 20);
      $$0.setFlammable(Blocks.MANGROVE_SLAB, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_SLAB, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_MOSAIC_SLAB, 5, 20);
      $$0.setFlammable(Blocks.OAK_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.SPRUCE_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.BIRCH_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.JUNGLE_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.ACACIA_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.CHERRY_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.DARK_OAK_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.PALE_OAK_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.MANGROVE_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_FENCE_GATE, 5, 20);
      $$0.setFlammable(Blocks.OAK_FENCE, 5, 20);
      $$0.setFlammable(Blocks.SPRUCE_FENCE, 5, 20);
      $$0.setFlammable(Blocks.BIRCH_FENCE, 5, 20);
      $$0.setFlammable(Blocks.JUNGLE_FENCE, 5, 20);
      $$0.setFlammable(Blocks.ACACIA_FENCE, 5, 20);
      $$0.setFlammable(Blocks.CHERRY_FENCE, 5, 20);
      $$0.setFlammable(Blocks.DARK_OAK_FENCE, 5, 20);
      $$0.setFlammable(Blocks.PALE_OAK_FENCE, 5, 20);
      $$0.setFlammable(Blocks.MANGROVE_FENCE, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_FENCE, 5, 20);
      $$0.setFlammable(Blocks.OAK_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.BIRCH_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.SPRUCE_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.JUNGLE_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.ACACIA_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.CHERRY_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.DARK_OAK_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.PALE_OAK_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.MANGROVE_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.BAMBOO_MOSAIC_STAIRS, 5, 20);
      $$0.setFlammable(Blocks.OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.SPRUCE_LOG, 5, 5);
      $$0.setFlammable(Blocks.BIRCH_LOG, 5, 5);
      $$0.setFlammable(Blocks.JUNGLE_LOG, 5, 5);
      $$0.setFlammable(Blocks.ACACIA_LOG, 5, 5);
      $$0.setFlammable(Blocks.CHERRY_LOG, 5, 5);
      $$0.setFlammable(Blocks.PALE_OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.DARK_OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.MANGROVE_LOG, 5, 5);
      $$0.setFlammable(Blocks.BAMBOO_BLOCK, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_SPRUCE_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_BIRCH_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_JUNGLE_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_ACACIA_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_CHERRY_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_DARK_OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_PALE_OAK_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_MANGROVE_LOG, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_BAMBOO_BLOCK, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_SPRUCE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_BIRCH_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_JUNGLE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_ACACIA_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_CHERRY_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_DARK_OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_PALE_OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.STRIPPED_MANGROVE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.SPRUCE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.BIRCH_WOOD, 5, 5);
      $$0.setFlammable(Blocks.JUNGLE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.ACACIA_WOOD, 5, 5);
      $$0.setFlammable(Blocks.CHERRY_WOOD, 5, 5);
      $$0.setFlammable(Blocks.PALE_OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.DARK_OAK_WOOD, 5, 5);
      $$0.setFlammable(Blocks.MANGROVE_WOOD, 5, 5);
      $$0.setFlammable(Blocks.MANGROVE_ROOTS, 5, 20);
      $$0.setFlammable(Blocks.OAK_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.SPRUCE_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.BIRCH_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.JUNGLE_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.ACACIA_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.CHERRY_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.DARK_OAK_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.PALE_OAK_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.MANGROVE_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.BOOKSHELF, 30, 20);
      $$0.setFlammable(Blocks.TNT, 15, 100);
      $$0.setFlammable(Blocks.SHORT_GRASS, 60, 100);
      $$0.setFlammable(Blocks.FERN, 60, 100);
      $$0.setFlammable(Blocks.DEAD_BUSH, 60, 100);
      $$0.setFlammable(Blocks.SHORT_DRY_GRASS, 60, 100);
      $$0.setFlammable(Blocks.TALL_DRY_GRASS, 60, 100);
      $$0.setFlammable(Blocks.SUNFLOWER, 60, 100);
      $$0.setFlammable(Blocks.LILAC, 60, 100);
      $$0.setFlammable(Blocks.ROSE_BUSH, 60, 100);
      $$0.setFlammable(Blocks.PEONY, 60, 100);
      $$0.setFlammable(Blocks.TALL_GRASS, 60, 100);
      $$0.setFlammable(Blocks.LARGE_FERN, 60, 100);
      $$0.setFlammable(Blocks.DANDELION, 60, 100);
      $$0.setFlammable(Blocks.POPPY, 60, 100);
      $$0.setFlammable(Blocks.OPEN_EYEBLOSSOM, 60, 100);
      $$0.setFlammable(Blocks.CLOSED_EYEBLOSSOM, 60, 100);
      $$0.setFlammable(Blocks.BLUE_ORCHID, 60, 100);
      $$0.setFlammable(Blocks.ALLIUM, 60, 100);
      $$0.setFlammable(Blocks.AZURE_BLUET, 60, 100);
      $$0.setFlammable(Blocks.RED_TULIP, 60, 100);
      $$0.setFlammable(Blocks.ORANGE_TULIP, 60, 100);
      $$0.setFlammable(Blocks.WHITE_TULIP, 60, 100);
      $$0.setFlammable(Blocks.PINK_TULIP, 60, 100);
      $$0.setFlammable(Blocks.OXEYE_DAISY, 60, 100);
      $$0.setFlammable(Blocks.CORNFLOWER, 60, 100);
      $$0.setFlammable(Blocks.LILY_OF_THE_VALLEY, 60, 100);
      $$0.setFlammable(Blocks.TORCHFLOWER, 60, 100);
      $$0.setFlammable(Blocks.PITCHER_PLANT, 60, 100);
      $$0.setFlammable(Blocks.WITHER_ROSE, 60, 100);
      $$0.setFlammable(Blocks.PINK_PETALS, 60, 100);
      $$0.setFlammable(Blocks.WILDFLOWERS, 60, 100);
      $$0.setFlammable(Blocks.LEAF_LITTER, 60, 100);
      $$0.setFlammable(Blocks.CACTUS_FLOWER, 60, 100);
      $$0.setFlammable(Blocks.WHITE_WOOL, 30, 60);
      $$0.setFlammable(Blocks.ORANGE_WOOL, 30, 60);
      $$0.setFlammable(Blocks.MAGENTA_WOOL, 30, 60);
      $$0.setFlammable(Blocks.LIGHT_BLUE_WOOL, 30, 60);
      $$0.setFlammable(Blocks.YELLOW_WOOL, 30, 60);
      $$0.setFlammable(Blocks.LIME_WOOL, 30, 60);
      $$0.setFlammable(Blocks.PINK_WOOL, 30, 60);
      $$0.setFlammable(Blocks.GRAY_WOOL, 30, 60);
      $$0.setFlammable(Blocks.LIGHT_GRAY_WOOL, 30, 60);
      $$0.setFlammable(Blocks.CYAN_WOOL, 30, 60);
      $$0.setFlammable(Blocks.PURPLE_WOOL, 30, 60);
      $$0.setFlammable(Blocks.BLUE_WOOL, 30, 60);
      $$0.setFlammable(Blocks.BROWN_WOOL, 30, 60);
      $$0.setFlammable(Blocks.GREEN_WOOL, 30, 60);
      $$0.setFlammable(Blocks.RED_WOOL, 30, 60);
      $$0.setFlammable(Blocks.BLACK_WOOL, 30, 60);
      $$0.setFlammable(Blocks.VINE, 15, 100);
      $$0.setFlammable(Blocks.COAL_BLOCK, 5, 5);
      $$0.setFlammable(Blocks.HAY_BLOCK, 60, 20);
      $$0.setFlammable(Blocks.TARGET, 15, 20);
      $$0.setFlammable(Blocks.WHITE_CARPET, 60, 20);
      $$0.setFlammable(Blocks.ORANGE_CARPET, 60, 20);
      $$0.setFlammable(Blocks.MAGENTA_CARPET, 60, 20);
      $$0.setFlammable(Blocks.LIGHT_BLUE_CARPET, 60, 20);
      $$0.setFlammable(Blocks.YELLOW_CARPET, 60, 20);
      $$0.setFlammable(Blocks.LIME_CARPET, 60, 20);
      $$0.setFlammable(Blocks.PINK_CARPET, 60, 20);
      $$0.setFlammable(Blocks.GRAY_CARPET, 60, 20);
      $$0.setFlammable(Blocks.LIGHT_GRAY_CARPET, 60, 20);
      $$0.setFlammable(Blocks.CYAN_CARPET, 60, 20);
      $$0.setFlammable(Blocks.PURPLE_CARPET, 60, 20);
      $$0.setFlammable(Blocks.BLUE_CARPET, 60, 20);
      $$0.setFlammable(Blocks.BROWN_CARPET, 60, 20);
      $$0.setFlammable(Blocks.GREEN_CARPET, 60, 20);
      $$0.setFlammable(Blocks.RED_CARPET, 60, 20);
      $$0.setFlammable(Blocks.BLACK_CARPET, 60, 20);
      $$0.setFlammable(Blocks.PALE_MOSS_BLOCK, 5, 100);
      $$0.setFlammable(Blocks.PALE_MOSS_CARPET, 5, 100);
      $$0.setFlammable(Blocks.PALE_HANGING_MOSS, 5, 100);
      $$0.setFlammable(Blocks.DRIED_KELP_BLOCK, 30, 60);
      $$0.setFlammable(Blocks.BAMBOO, 60, 60);
      $$0.setFlammable(Blocks.SCAFFOLDING, 60, 60);
      $$0.setFlammable(Blocks.LECTERN, 30, 20);
      $$0.setFlammable(Blocks.COMPOSTER, 5, 20);
      $$0.setFlammable(Blocks.SWEET_BERRY_BUSH, 60, 100);
      $$0.setFlammable(Blocks.BEEHIVE, 5, 20);
      $$0.setFlammable(Blocks.BEE_NEST, 30, 20);
      $$0.setFlammable(Blocks.AZALEA_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.FLOWERING_AZALEA_LEAVES, 30, 60);
      $$0.setFlammable(Blocks.CAVE_VINES, 15, 60);
      $$0.setFlammable(Blocks.CAVE_VINES_PLANT, 15, 60);
      $$0.setFlammable(Blocks.SPORE_BLOSSOM, 60, 100);
      $$0.setFlammable(Blocks.AZALEA, 30, 60);
      $$0.setFlammable(Blocks.FLOWERING_AZALEA, 30, 60);
      $$0.setFlammable(Blocks.BIG_DRIPLEAF, 60, 100);
      $$0.setFlammable(Blocks.BIG_DRIPLEAF_STEM, 60, 100);
      $$0.setFlammable(Blocks.SMALL_DRIPLEAF, 60, 100);
      $$0.setFlammable(Blocks.HANGING_ROOTS, 30, 60);
      $$0.setFlammable(Blocks.GLOW_LICHEN, 15, 100);
      $$0.setFlammable(Blocks.FIREFLY_BUSH, 60, 100);
      $$0.setFlammable(Blocks.BUSH, 60, 100);
      $$0.setFlammable(Blocks.ACACIA_SHELF, 30, 20);
      $$0.setFlammable(Blocks.BAMBOO_SHELF, 30, 20);
      $$0.setFlammable(Blocks.BIRCH_SHELF, 30, 20);
      $$0.setFlammable(Blocks.CHERRY_SHELF, 30, 20);
      $$0.setFlammable(Blocks.DARK_OAK_SHELF, 30, 20);
      $$0.setFlammable(Blocks.JUNGLE_SHELF, 30, 20);
      $$0.setFlammable(Blocks.MANGROVE_SHELF, 30, 20);
      $$0.setFlammable(Blocks.OAK_SHELF, 30, 20);
      $$0.setFlammable(Blocks.PALE_OAK_SHELF, 30, 20);
      $$0.setFlammable(Blocks.SPRUCE_SHELF, 30, 20);
   }
}
