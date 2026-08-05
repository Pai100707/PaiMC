package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Column;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.phys.Vec3;

public class LargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
   public LargeDripstoneFeature(Codec<LargeDripstoneConfiguration> $$0) {
      super($$0);
   }

   @Override
   public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> $$0) {
      net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      BlockPos $$2 = $$0.origin();
      LargeDripstoneConfiguration $$3 = $$0.config();
      RandomSource $$4 = $$0.random();
      if (!DripstoneUtils.isEmptyOrWater($$1, $$2)) {
         return false;
      } else {
         Optional<Column> $$5 = Column.scan($$1, $$2, $$3.floorToCeilingSearchRange, DripstoneUtils::isEmptyOrWater, DripstoneUtils::isDripstoneBaseOrLava);
         if (!$$5.isEmpty() && $$5.get() instanceof Column.Range) {
            Column.Range $$6 = (Column.Range)$$5.get();
            if ($$6.height() < 4) {
               return false;
            } else {
               int $$7 = (int)($$6.height() * $$3.maxColumnRadiusToCaveHeightRatio);
               int $$8 = Mth.clamp($$7, $$3.columnRadius.getMinValue(), $$3.columnRadius.getMaxValue());
               int $$9 = Mth.randomBetweenInclusive($$4, $$3.columnRadius.getMinValue(), $$8);
               LargeDripstoneFeature.LargeDripstone $$10 = makeDripstone($$2.atY($$6.ceiling() - 1), false, $$4, $$9, $$3.stalactiteBluntness, $$3.heightScale);
               LargeDripstoneFeature.LargeDripstone $$11 = makeDripstone($$2.atY($$6.floor() + 1), true, $$4, $$9, $$3.stalagmiteBluntness, $$3.heightScale);
               LargeDripstoneFeature.WindOffsetter $$12;
               if ($$10.isSuitableForWind($$3) && $$11.isSuitableForWind($$3)) {
                  $$12 = new LargeDripstoneFeature.WindOffsetter($$2.getY(), $$4, $$3.windSpeed);
               } else {
                  $$12 = LargeDripstoneFeature.WindOffsetter.noWind();
               }

               boolean $$14 = $$10.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary($$1, $$12);
               boolean $$15 = $$11.moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary($$1, $$12);
               if ($$14) {
                  $$10.placeBlocks($$1, $$4, $$12);
               }

               if ($$15) {
                  $$11.placeBlocks($$1, $$4, $$12);
               }

               if (SharedConstants.DEBUG_LARGE_DRIPSTONE) {
                  this.placeDebugMarkers($$1, $$2, $$6, $$12);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   private static LargeDripstoneFeature.LargeDripstone makeDripstone(BlockPos $$0, boolean $$1, RandomSource $$2, int $$3, FloatProvider $$4, FloatProvider $$5) {
      return new LargeDripstoneFeature.LargeDripstone($$0, $$1, $$3, $$4.sample($$2), $$5.sample($$2));
   }

   private void placeDebugMarkers(net.minecraft.world.level.WorldGenLevel $$0, BlockPos $$1, Column.Range $$2, LargeDripstoneFeature.WindOffsetter $$3) {
      $$0.setBlock($$3.offset($$1.atY($$2.ceiling() - 1)), Blocks.DIAMOND_BLOCK.defaultBlockState(), 2);
      $$0.setBlock($$3.offset($$1.atY($$2.floor() + 1)), Blocks.GOLD_BLOCK.defaultBlockState(), 2);

      for (MutableBlockPos $$4 = $$1.atY($$2.floor() + 2).mutable(); $$4.getY() < $$2.ceiling() - 1; $$4.move(Direction.UP)) {
         BlockPos $$5 = $$3.offset($$4);
         if (DripstoneUtils.isEmptyOrWater($$0, $$5) || $$0.getBlockState($$5).is(Blocks.DRIPSTONE_BLOCK)) {
            $$0.setBlock($$5, Blocks.CREEPER_HEAD.defaultBlockState(), 2);
         }
      }
   }

   static final class LargeDripstone {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      LargeDripstone(BlockPos $$0, boolean $$1, int $$2, double $$3, double $$4) {
         this.root = $$0;
         this.pointingUp = $$1;
         this.radius = $$2;
         this.bluntness = $$3;
         this.scale = $$4;
      }

      private int getHeight() {
         return this.getHeightAtRadius(0.0F);
      }

      private int getMinY() {
         return this.pointingUp ? this.root.getY() : this.root.getY() - this.getHeight();
      }

      private int getMaxY() {
         return !this.pointingUp ? this.root.getY() : this.root.getY() + this.getHeight();
      }

      boolean moveBackUntilBaseIsInsideStoneAndShrinkRadiusIfNecessary(net.minecraft.world.level.WorldGenLevel $$0, LargeDripstoneFeature.WindOffsetter $$1) {
         while (this.radius > 1) {
            MutableBlockPos $$2 = this.root.mutable();
            int $$3 = Math.min(10, this.getHeight());

            for (int $$4 = 0; $$4 < $$3; $$4++) {
               if ($$0.getBlockState($$2).is(Blocks.LAVA)) {
                  return false;
               }

               if (DripstoneUtils.isCircleMostlyEmbeddedInStone($$0, $$1.offset($$2), this.radius)) {
                  this.root = $$2;
                  return true;
               }

               $$2.move(this.pointingUp ? Direction.DOWN : Direction.UP);
            }

            this.radius /= 2;
         }

         return false;
      }

      private int getHeightAtRadius(float $$0) {
         return (int)DripstoneUtils.getDripstoneHeight($$0, this.radius, this.scale, this.bluntness);
      }

      void placeBlocks(net.minecraft.world.level.WorldGenLevel $$0, RandomSource $$1, LargeDripstoneFeature.WindOffsetter $$2) {
         for (int $$3 = -this.radius; $$3 <= this.radius; $$3++) {
            for (int $$4 = -this.radius; $$4 <= this.radius; $$4++) {
               float $$5 = Mth.sqrt($$3 * $$3 + $$4 * $$4);
               if (!($$5 > this.radius)) {
                  int $$6 = this.getHeightAtRadius($$5);
                  if ($$6 > 0) {
                     if ($$1.nextFloat() < 0.2) {
                        $$6 = (int)($$6 * Mth.randomBetween($$1, 0.8F, 1.0F));
                     }

                     MutableBlockPos $$7 = this.root.offset($$3, 0, $$4).mutable();
                     boolean $$8 = false;
                     int $$9 = this.pointingUp ? $$0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, $$7.getX(), $$7.getZ()) : Integer.MAX_VALUE;

                     for (int $$10 = 0; $$10 < $$6 && $$7.getY() < $$9; $$10++) {
                        BlockPos $$11 = $$2.offset($$7);
                        if (DripstoneUtils.isEmptyOrWaterOrLava($$0, $$11)) {
                           $$8 = true;
                           Block $$12 = SharedConstants.DEBUG_LARGE_DRIPSTONE ? Blocks.GLASS : Blocks.DRIPSTONE_BLOCK;
                           $$0.setBlock($$11, $$12.defaultBlockState(), 2);
                        } else if ($$8 && $$0.getBlockState($$11).is(BlockTags.BASE_STONE_OVERWORLD)) {
                           break;
                        }

                        $$7.move(this.pointingUp ? Direction.UP : Direction.DOWN);
                     }
                  }
               }
            }
         }
      }

      boolean isSuitableForWind(LargeDripstoneConfiguration $$0) {
         return this.radius >= $$0.minRadiusForWind && this.bluntness >= $$0.minBluntnessForWind;
      }
   }

   static final class WindOffsetter {
      private final int originY;
      
      private final Vec3 windSpeed;

      WindOffsetter(int $$0, RandomSource $$1, FloatProvider $$2) {
         this.originY = $$0;
         float $$3 = $$2.sample($$1);
         float $$4 = Mth.randomBetween($$1, 0.0F, (float) Math.PI);
         this.windSpeed = new Vec3(Mth.cos($$4) * $$3, 0.0, Mth.sin($$4) * $$3);
      }

      private WindOffsetter() {
         this.originY = 0;
         this.windSpeed = null;
      }

      static LargeDripstoneFeature.WindOffsetter noWind() {
         return new LargeDripstoneFeature.WindOffsetter();
      }

      BlockPos offset(BlockPos $$0) {
         if (this.windSpeed == null) {
            return $$0;
         } else {
            int $$1 = this.originY - $$0.getY();
            Vec3 $$2 = this.windSpeed.scale($$1);
            return $$0.offset(Mth.floor($$2.x), 0, Mth.floor($$2.z));
         }
      }
   }
}
