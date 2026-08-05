package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChorusFlowerBlock extends Block {
   public static final MapCodec<ChorusFlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(BuiltInRegistries.BLOCK.byNameCodec().fieldOf("plant").forGetter($$0x -> $$0x.plant), propertiesCodec())
         .apply($$0, ChorusFlowerBlock::new)
   );
   public static final int DEAD_AGE = 5;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
   private static final VoxelShape SHAPE_BLOCK_SUPPORT = Block.column(14.0, 0.0, 15.0);
   private final Block plant;

   @Override
   public MapCodec<ChorusFlowerBlock> codec() {
      return CODEC;
   }

   protected ChorusFlowerBlock(Block $$0, BlockBehaviour.Properties $$1) {
      super($$1);
      this.plant = $$0;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
   }

   @Override
   protected void tick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if (!$$0.canSurvive($$1, $$2)) {
         $$1.destroyBlock($$2, true);
      }
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return $$0.getValue(AGE) < 5;
   }

   @Override
   public VoxelShape getBlockSupportShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return SHAPE_BLOCK_SUPPORT;
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      BlockPos $$4 = $$2.above();
      if ($$1.isEmptyBlock($$4) && $$4.getY() <= $$1.getMaxY()) {
         int $$5 = $$0.getValue(AGE);
         if ($$5 < 5) {
            boolean $$6 = false;
            boolean $$7 = false;
            BlockState $$8 = $$1.getBlockState($$2.below());
            if ($$8.is(Blocks.END_STONE)) {
               $$6 = true;
            } else if ($$8.is(this.plant)) {
               int $$9 = 1;

               for (int $$10 = 0; $$10 < 4; $$10++) {
                  BlockState $$11 = $$1.getBlockState($$2.below($$9 + 1));
                  if (!$$11.is(this.plant)) {
                     if ($$11.is(Blocks.END_STONE)) {
                        $$7 = true;
                     }
                     break;
                  }

                  $$9++;
               }

               if ($$9 < 2 || $$9 <= $$3.nextInt($$7 ? 5 : 4)) {
                  $$6 = true;
               }
            } else if ($$8.isAir()) {
               $$6 = true;
            }

            if ($$6 && allNeighborsEmpty($$1, $$4, null) && $$1.isEmptyBlock($$2.above(2))) {
               $$1.setBlock($$2, ChorusPlantBlock.getStateWithConnections($$1, $$2, this.plant.defaultBlockState()), 2);
               this.placeGrownFlower($$1, $$4, $$5);
            } else if ($$5 < 4) {
               int $$12 = $$3.nextInt(4);
               if ($$7) {
                  $$12++;
               }

               boolean $$13 = false;

               for (int $$14 = 0; $$14 < $$12; $$14++) {
                  Direction $$15 = Plane.HORIZONTAL.getRandomDirection($$3);
                  BlockPos $$16 = $$2.relative($$15);
                  if ($$1.isEmptyBlock($$16) && $$1.isEmptyBlock($$16.below()) && allNeighborsEmpty($$1, $$16, $$15.getOpposite())) {
                     this.placeGrownFlower($$1, $$16, $$5 + 1);
                     $$13 = true;
                  }
               }

               if ($$13) {
                  $$1.setBlock($$2, ChorusPlantBlock.getStateWithConnections($$1, $$2, this.plant.defaultBlockState()), 2);
               } else {
                  this.placeDeadFlower($$1, $$2);
               }
            } else {
               this.placeDeadFlower($$1, $$2);
            }
         }
      }
   }

   private void placeGrownFlower(net.minecraft.world.level.Level $$0, BlockPos $$1, int $$2) {
      $$0.setBlock($$1, this.defaultBlockState().setValue(AGE, $$2), 2);
      $$0.levelEvent(1033, $$1, 0);
   }

   private void placeDeadFlower(net.minecraft.world.level.Level $$0, BlockPos $$1) {
      $$0.setBlock($$1, this.defaultBlockState().setValue(AGE, 5), 2);
      $$0.levelEvent(1034, $$1, 0);
   }

   private static boolean allNeighborsEmpty(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, Direction $$2) {
      for (Direction $$3 : Plane.HORIZONTAL) {
         if ($$3 != $$2 && !$$0.isEmptyBlock($$1.relative($$3))) {
            return false;
         }
      }

      return true;
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
      if ($$4 != Direction.UP && !$$0.canSurvive($$1, $$3)) {
         $$2.scheduleTick($$3, this, 1);
      }

      return super.updateShape($$0, $$1, $$2, $$3, $$4, $$5, $$6, $$7);
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2.below());
      if (!$$3.is(this.plant) && !$$3.is(Blocks.END_STONE)) {
         if (!$$3.isAir()) {
            return false;
         } else {
            boolean $$4 = false;

            for (Direction $$5 : Plane.HORIZONTAL) {
               BlockState $$6 = $$1.getBlockState($$2.relative($$5));
               if ($$6.is(this.plant)) {
                  if ($$4) {
                     return false;
                  }

                  $$4 = true;
               } else if (!$$6.isAir()) {
                  return false;
               }
            }

            return $$4;
         }
      } else {
         return true;
      }
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }

   public static void generatePlant(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, int $$3) {
      $$0.setBlock($$1, ChorusPlantBlock.getStateWithConnections($$0, $$1, Blocks.CHORUS_PLANT.defaultBlockState()), 2);
      growTreeRecursive($$0, $$1, $$2, $$1, $$3, 0);
   }

   private static void growTreeRecursive(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, BlockPos $$3, int $$4, int $$5) {
      Block $$6 = Blocks.CHORUS_PLANT;
      int $$7 = $$2.nextInt(4) + 1;
      if ($$5 == 0) {
         $$7++;
      }

      for (int $$8 = 0; $$8 < $$7; $$8++) {
         BlockPos $$9 = $$1.above($$8 + 1);
         if (!allNeighborsEmpty($$0, $$9, null)) {
            return;
         }

         $$0.setBlock($$9, ChorusPlantBlock.getStateWithConnections($$0, $$9, $$6.defaultBlockState()), 2);
         $$0.setBlock($$9.below(), ChorusPlantBlock.getStateWithConnections($$0, $$9.below(), $$6.defaultBlockState()), 2);
      }

      boolean $$10 = false;
      if ($$5 < 4) {
         int $$11 = $$2.nextInt(4);
         if ($$5 == 0) {
            $$11++;
         }

         for (int $$12 = 0; $$12 < $$11; $$12++) {
            Direction $$13 = Plane.HORIZONTAL.getRandomDirection($$2);
            BlockPos $$14 = $$1.above($$7).relative($$13);
            if (Math.abs($$14.getX() - $$3.getX()) < $$4
               && Math.abs($$14.getZ() - $$3.getZ()) < $$4
               && $$0.isEmptyBlock($$14)
               && $$0.isEmptyBlock($$14.below())
               && allNeighborsEmpty($$0, $$14, $$13.getOpposite())) {
               $$10 = true;
               $$0.setBlock($$14, ChorusPlantBlock.getStateWithConnections($$0, $$14, $$6.defaultBlockState()), 2);
               $$0.setBlock(
                  $$14.relative($$13.getOpposite()),
                  ChorusPlantBlock.getStateWithConnections($$0, $$14.relative($$13.getOpposite()), $$6.defaultBlockState()),
                  2
               );
               growTreeRecursive($$0, $$14, $$2, $$3, $$4, $$5 + 1);
            }
         }
      }

      if (!$$10) {
         $$0.setBlock($$1.above($$7), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, 5), 2);
      }
   }

   @Override
   protected void onProjectileHit(net.minecraft.world.level.Level $$0, BlockState $$1, BlockHitResult $$2, Projectile $$3) {
      BlockPos $$4 = $$2.getBlockPos();
      if ($$0 instanceof ServerLevel $$5 && $$3.mayInteract($$5, $$4) && $$3.mayBreak($$5)) {
         $$0.destroyBlock($$4, true, $$3);
      }
   }
}
