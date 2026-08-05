package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class BoneMealItem extends net.minecraft.world.item.Item {
   public static final int GRASS_SPREAD_WIDTH = 3;
   public static final int GRASS_SPREAD_HEIGHT = 1;
   public static final int GRASS_COUNT_MULTIPLIER = 3;

   public BoneMealItem(net.minecraft.world.item.Item.Properties $$0) {
      super($$0);
   }

   @Override
   public InteractionResult useOn(UseOnContext $$0) {
      Level $$1 = $$0.getLevel();
      BlockPos $$2 = $$0.getClickedPos();
      BlockPos $$3 = $$2.relative($$0.getClickedFace());
      net.minecraft.world.item.ItemStack $$4 = $$0.getItemInHand();
      if (growCrop($$4, $$1, $$2)) {
         if (!$$1.isClientSide()) {
            $$4.causeUseVibration($$0.getPlayer(), GameEvent.ITEM_INTERACT_FINISH);
            $$1.levelEvent(1505, $$2, 15);
         }

         return InteractionResult.SUCCESS;
      } else {
         BlockState $$5 = $$1.getBlockState($$2);
         boolean $$6 = $$5.isFaceSturdy($$1, $$2, $$0.getClickedFace());
         if ($$6 && growWaterPlant($$4, $$1, $$3, $$0.getClickedFace())) {
            if (!$$1.isClientSide()) {
               $$4.causeUseVibration($$0.getPlayer(), GameEvent.ITEM_INTERACT_FINISH);
               $$1.levelEvent(1505, $$3, 15);
            }

            return InteractionResult.SUCCESS;
         } else {
            return InteractionResult.PASS;
         }
      }
   }

   public static boolean growCrop(net.minecraft.world.item.ItemStack $$0, Level $$1, BlockPos $$2) {
      BlockState $$3 = $$1.getBlockState($$2);
      if ($$3.getBlock() instanceof BonemealableBlock $$4 && $$4.isValidBonemealTarget($$1, $$2, $$3)) {
         if ($$1 instanceof ServerLevel) {
            if ($$4.isBonemealSuccess($$1, $$1.random, $$2, $$3)) {
               $$4.performBonemeal((ServerLevel)$$1, $$1.random, $$2, $$3);
            }

            $$0.shrink(1);
         }

         return true;
      } else {
         return false;
      }
   }

   public static boolean growWaterPlant(net.minecraft.world.item.ItemStack $$0, Level $$1, BlockPos $$2, Direction $$3) {
      if ($$1.getBlockState($$2).is(Blocks.WATER) && $$1.getFluidState($$2).getAmount() == 8) {
         if (!($$1 instanceof ServerLevel)) {
            return true;
         } else {
            RandomSource $$4 = $$1.getRandom();

            label80:
            for (int $$5 = 0; $$5 < 128; $$5++) {
               BlockPos $$6 = $$2;
               BlockState $$7 = Blocks.SEAGRASS.defaultBlockState();

               for (int $$8 = 0; $$8 < $$5 / 16; $$8++) {
                  $$6 = $$6.offset($$4.nextInt(3) - 1, ($$4.nextInt(3) - 1) * $$4.nextInt(3) / 2, $$4.nextInt(3) - 1);
                  if ($$1.getBlockState($$6).isCollisionShapeFullBlock($$1, $$6)) {
                     continue label80;
                  }
               }

               Holder<Biome> $$9 = $$1.getBiome($$6);
               if ($$9.is(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL)) {
                  if ($$5 == 0 && $$3 != null && $$3.getAxis().isHorizontal()) {
                     $$7 = BuiltInRegistries.BLOCK
                        .getRandomElementOf(BlockTags.WALL_CORALS, $$1.random)
                        .map($$0x -> ((Block)$$0x.value()).defaultBlockState())
                        .orElse($$7);
                     if ($$7.hasProperty(BaseCoralWallFanBlock.FACING)) {
                        $$7 = (BlockState)$$7.setValue(BaseCoralWallFanBlock.FACING, $$3);
                     }
                  } else if ($$4.nextInt(4) == 0) {
                     $$7 = BuiltInRegistries.BLOCK
                        .getRandomElementOf(BlockTags.UNDERWATER_BONEMEALS, $$1.random)
                        .map($$0x -> ((Block)$$0x.value()).defaultBlockState())
                        .orElse($$7);
                  }
               }

               if ($$7.is(BlockTags.WALL_CORALS, $$0x -> $$0x.hasProperty(BaseCoralWallFanBlock.FACING))) {
                  for (int $$10 = 0; !$$7.canSurvive($$1, $$6) && $$10 < 4; $$10++) {
                     $$7 = (BlockState)$$7.setValue(BaseCoralWallFanBlock.FACING, Plane.HORIZONTAL.getRandomDirection($$4));
                  }
               }

               if ($$7.canSurvive($$1, $$6)) {
                  BlockState $$11 = $$1.getBlockState($$6);
                  if ($$11.is(Blocks.WATER) && $$1.getFluidState($$6).getAmount() == 8) {
                     $$1.setBlock($$6, $$7, 3);
                  } else if ($$11.is(Blocks.SEAGRASS) && ((BonemealableBlock)Blocks.SEAGRASS).isValidBonemealTarget($$1, $$6, $$11) && $$4.nextInt(10) == 0) {
                     ((BonemealableBlock)Blocks.SEAGRASS).performBonemeal((ServerLevel)$$1, $$4, $$6, $$11);
                  }
               }
            }

            $$0.shrink(1);
            return true;
         }
      } else {
         return false;
      }
   }

   public static void addGrowthParticles(LevelAccessor $$0, BlockPos $$1, int $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      if ($$3.getBlock() instanceof BonemealableBlock $$4) {
         BlockPos $$5 = $$4.getParticlePos($$1);
         switch ($$4.getType()) {
            case NEIGHBOR_SPREADER:
               ParticleUtils.spawnParticles($$0, $$5, $$2 * 3, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
               break;
            case GROWER:
               ParticleUtils.spawnParticleInBlock($$0, $$5, $$2, ParticleTypes.HAPPY_VILLAGER);
         }
      } else if ($$3.is(Blocks.WATER)) {
         ParticleUtils.spawnParticles($$0, $$1, $$2 * 3, 3.0, 1.0, false, ParticleTypes.HAPPY_VILLAGER);
      }
   }
}
