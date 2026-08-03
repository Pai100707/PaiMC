package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CropBlock extends VegetationBlock implements BonemealableBlock {
   public static final MapCodec<CropBlock> CODEC = simpleCodec(CropBlock::new);
   public static final int MAX_AGE = 7;
   public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
   private static final VoxelShape[] SHAPES = Block.boxes(7, $$0 -> Block.column(16.0, 0.0, 2 + $$0 * 2));

   @Override
   public MapCodec<? extends CropBlock> codec() {
      return CODEC;
   }

   protected CropBlock(BlockBehaviour.Properties $$0) {
      super($$0);
      this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
   }

   @Override
   protected VoxelShape getShape(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, CollisionContext $$3) {
      return SHAPES[this.getAge($$0)];
   }

   @Override
   protected boolean mayPlaceOn(BlockState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.is(Blocks.FARMLAND);
   }

   protected IntegerProperty getAgeProperty() {
      return AGE;
   }

   public int getMaxAge() {
      return 7;
   }

   public int getAge(BlockState $$0) {
      return $$0.getValue(this.getAgeProperty());
   }

   public BlockState getStateForAge(int $$0) {
      return this.defaultBlockState().setValue(this.getAgeProperty(), $$0);
   }

   public final boolean isMaxAge(BlockState $$0) {
      return this.getAge($$0) >= this.getMaxAge();
   }

   @Override
   protected boolean isRandomlyTicking(BlockState $$0) {
      return !this.isMaxAge($$0);
   }

   @Override
   protected void randomTick(BlockState $$0, ServerLevel $$1, BlockPos $$2, RandomSource $$3) {
      if ($$1.getRawBrightness($$2, 0) >= 9) {
         int $$4 = this.getAge($$0);
         if ($$4 < this.getMaxAge()) {
            float $$5 = getGrowthSpeed(this, $$1, $$2);
            if ($$3.nextInt((int)(25.0F / $$5) + 1) == 0) {
               $$1.setBlock($$2, this.getStateForAge($$4 + 1), 2);
            }
         }
      }
   }

   public void growCrops(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = Math.min(this.getMaxAge(), this.getAge($$2) + this.getBonemealAgeIncrease($$0));
      $$0.setBlock($$1, this.getStateForAge($$3), 2);
   }

   protected int getBonemealAgeIncrease(net.minecraft.world.level.Level $$0) {
      return Mth.nextInt($$0.random, 2, 5);
   }

   protected static float getGrowthSpeed(Block $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      float $$3 = 1.0F;
      BlockPos $$4 = $$2.below();

      for (int $$5 = -1; $$5 <= 1; $$5++) {
         for (int $$6 = -1; $$6 <= 1; $$6++) {
            float $$7 = 0.0F;
            BlockState $$8 = $$1.getBlockState($$4.offset($$5, 0, $$6));
            if ($$8.is(Blocks.FARMLAND)) {
               $$7 = 1.0F;
               if ($$8.getValue(FarmBlock.MOISTURE) > 0) {
                  $$7 = 3.0F;
               }
            }

            if ($$5 != 0 || $$6 != 0) {
               $$7 /= 4.0F;
            }

            $$3 += $$7;
         }
      }

      BlockPos $$9 = $$2.north();
      BlockPos $$10 = $$2.south();
      BlockPos $$11 = $$2.west();
      BlockPos $$12 = $$2.east();
      boolean $$13 = $$1.getBlockState($$11).is($$0) || $$1.getBlockState($$12).is($$0);
      boolean $$14 = $$1.getBlockState($$9).is($$0) || $$1.getBlockState($$10).is($$0);
      if ($$13 && $$14) {
         $$3 /= 2.0F;
      } else {
         boolean $$15 = $$1.getBlockState($$11.north()).is($$0)
            || $$1.getBlockState($$12.north()).is($$0)
            || $$1.getBlockState($$12.south()).is($$0)
            || $$1.getBlockState($$11.south()).is($$0);
         if ($$15) {
            $$3 /= 2.0F;
         }
      }

      return $$3;
   }

   @Override
   protected boolean canSurvive(BlockState $$0, net.minecraft.world.level.LevelReader $$1, BlockPos $$2) {
      return hasSufficientLight($$1, $$2) && super.canSurvive($$0, $$1, $$2);
   }

   protected static boolean hasSufficientLight(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      return $$0.getRawBrightness($$1, 0) >= 8;
   }

   @Override
   protected void entityInside(BlockState $$0, net.minecraft.world.level.Level $$1, BlockPos $$2, Entity $$3, InsideBlockEffectApplier $$4, boolean $$5) {
      if ($$1 instanceof ServerLevel $$6 && $$3 instanceof Ravager && $$6.getGameRules().get(GameRules.MOB_GRIEFING)) {
         $$6.destroyBlock($$2, true, $$3);
      }

      super.entityInside($$0, $$1, $$2, $$3, $$4, $$5);
   }

   protected net.minecraft.world.level.ItemLike getBaseSeedId() {
      return Items.WHEAT_SEEDS;
   }

   @Override
   protected ItemStack getCloneItemStack(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2, boolean $$3) {
      return new ItemStack(this.getBaseSeedId());
   }

   @Override
   public boolean isValidBonemealTarget(net.minecraft.world.level.LevelReader $$0, BlockPos $$1, BlockState $$2) {
      return !this.isMaxAge($$2);
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      return true;
   }

   @Override
   public void performBonemeal(ServerLevel $$0, RandomSource $$1, BlockPos $$2, BlockState $$3) {
      this.growCrops($$0, $$2, $$3);
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> $$0) {
      $$0.add(AGE);
   }
}
