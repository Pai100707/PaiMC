package net.minecraft.world.level.levelgen.feature.trunkplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

public class FancyTrunkPlacer extends TrunkPlacer {
   public static final MapCodec<FancyTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec($$0 -> trunkPlacerParts($$0).apply($$0, FancyTrunkPlacer::new));
   private static final double TRUNK_HEIGHT_SCALE = 0.618;
   private static final double CLUSTER_DENSITY_MAGIC = 1.382;
   private static final double BRANCH_SLOPE = 0.381;
   private static final double BRANCH_LENGTH_MAGIC = 0.328;

   public FancyTrunkPlacer(int $$0, int $$1, int $$2) {
      super($$0, $$1, $$2);
   }

   @Override
   protected TrunkPlacerType<?> type() {
      return TrunkPlacerType.FANCY_TRUNK_PLACER;
   }

   @Override
   public List<FoliagePlacer.FoliageAttachment> placeTrunk(
      net.minecraft.world.level.LevelSimulatedReader $$0, BiConsumer<BlockPos, BlockState> $$1, RandomSource $$2, int $$3, BlockPos $$4, TreeConfiguration $$5
   ) {
      int $$6 = 5;
      int $$7 = $$3 + 2;
      int $$8 = Mth.floor($$7 * 0.618);
      setDirtAt($$0, $$1, $$2, $$4.below(), $$5);
      double $$9 = 1.0;
      int $$10 = Math.min(1, Mth.floor(1.382 + Math.pow(1.0 * $$7 / 13.0, 2.0)));
      int $$11 = $$4.getY() + $$8;
      int $$12 = $$7 - 5;
      List<FancyTrunkPlacer.FoliageCoords> $$13 = Lists.newArrayList();
      $$13.add(new FancyTrunkPlacer.FoliageCoords($$4.above($$12), $$11));

      for (; $$12 >= 0; $$12--) {
         float $$14 = treeShape($$7, $$12);
         if (!($$14 < 0.0F)) {
            for (int $$15 = 0; $$15 < $$10; $$15++) {
               double $$16 = 1.0;
               double $$17 = 1.0 * $$14 * ($$2.nextFloat() + 0.328);
               double $$18 = $$2.nextFloat() * 2.0F * Math.PI;
               double $$19 = $$17 * Math.sin($$18) + 0.5;
               double $$20 = $$17 * Math.cos($$18) + 0.5;
               BlockPos $$21 = $$4.offset(Mth.floor($$19), $$12 - 1, Mth.floor($$20));
               BlockPos $$22 = $$21.above(5);
               if (this.makeLimb($$0, $$1, $$2, $$21, $$22, false, $$5)) {
                  int $$23 = $$4.getX() - $$21.getX();
                  int $$24 = $$4.getZ() - $$21.getZ();
                  double $$25 = $$21.getY() - Math.sqrt($$23 * $$23 + $$24 * $$24) * 0.381;
                  int $$26 = $$25 > $$11 ? $$11 : (int)$$25;
                  BlockPos $$27 = new BlockPos($$4.getX(), $$26, $$4.getZ());
                  if (this.makeLimb($$0, $$1, $$2, $$27, $$21, false, $$5)) {
                     $$13.add(new FancyTrunkPlacer.FoliageCoords($$21, $$27.getY()));
                  }
               }
            }
         }
      }

      this.makeLimb($$0, $$1, $$2, $$4, $$4.above($$8), true, $$5);
      this.makeBranches($$0, $$1, $$2, $$7, $$4, $$13, $$5);
      List<FoliagePlacer.FoliageAttachment> $$28 = Lists.newArrayList();

      for (FancyTrunkPlacer.FoliageCoords $$29 : $$13) {
         if (this.trimBranches($$7, $$29.getBranchBase() - $$4.getY())) {
            $$28.add($$29.attachment);
         }
      }

      return $$28;
   }

   private boolean makeLimb(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      BlockPos $$3,
      BlockPos $$4,
      boolean $$5,
      TreeConfiguration $$6
   ) {
      if (!$$5 && Objects.equals($$3, $$4)) {
         return true;
      } else {
         BlockPos $$7 = $$4.offset(-$$3.getX(), -$$3.getY(), -$$3.getZ());
         int $$8 = this.getSteps($$7);
         float $$9 = (float)$$7.getX() / $$8;
         float $$10 = (float)$$7.getY() / $$8;
         float $$11 = (float)$$7.getZ() / $$8;

         for (int $$12 = 0; $$12 <= $$8; $$12++) {
            BlockPos $$13 = $$3.offset(Mth.floor(0.5F + $$12 * $$9), Mth.floor(0.5F + $$12 * $$10), Mth.floor(0.5F + $$12 * $$11));
            if ($$5) {
               this.placeLog($$0, $$1, $$2, $$13, $$6, $$2x -> $$2x.trySetValue(RotatedPillarBlock.AXIS, this.getLogAxis($$3, $$13)));
            } else if (!this.isFree($$0, $$13)) {
               return false;
            }
         }

         return true;
      }
   }

   private int getSteps(BlockPos $$0) {
      int $$1 = Mth.abs($$0.getX());
      int $$2 = Mth.abs($$0.getY());
      int $$3 = Mth.abs($$0.getZ());
      return Math.max($$1, Math.max($$2, $$3));
   }

   private Axis getLogAxis(BlockPos $$0, BlockPos $$1) {
      Axis $$2 = Axis.Y;
      int $$3 = Math.abs($$1.getX() - $$0.getX());
      int $$4 = Math.abs($$1.getZ() - $$0.getZ());
      int $$5 = Math.max($$3, $$4);
      if ($$5 > 0) {
         if ($$3 == $$5) {
            $$2 = Axis.X;
         } else {
            $$2 = Axis.Z;
         }
      }

      return $$2;
   }

   private boolean trimBranches(int $$0, int $$1) {
      return $$1 >= $$0 * 0.2;
   }

   private void makeBranches(
      net.minecraft.world.level.LevelSimulatedReader $$0,
      BiConsumer<BlockPos, BlockState> $$1,
      RandomSource $$2,
      int $$3,
      BlockPos $$4,
      List<FancyTrunkPlacer.FoliageCoords> $$5,
      TreeConfiguration $$6
   ) {
      for (FancyTrunkPlacer.FoliageCoords $$7 : $$5) {
         int $$8 = $$7.getBranchBase();
         BlockPos $$9 = new BlockPos($$4.getX(), $$8, $$4.getZ());
         if (!$$9.equals($$7.attachment.pos()) && this.trimBranches($$3, $$8 - $$4.getY())) {
            this.makeLimb($$0, $$1, $$2, $$9, $$7.attachment.pos(), true, $$6);
         }
      }
   }

   private static float treeShape(int $$0, int $$1) {
      if ($$1 < $$0 * 0.3F) {
         return -1.0F;
      } else {
         float $$2 = $$0 / 2.0F;
         float $$3 = $$2 - $$1;
         float $$4 = Mth.sqrt($$2 * $$2 - $$3 * $$3);
         if ($$3 == 0.0F) {
            $$4 = $$2;
         } else if (Math.abs($$3) >= $$2) {
            return 0.0F;
         }

         return $$4 * 0.5F;
      }
   }

   static class FoliageCoords {
      final FoliagePlacer.FoliageAttachment attachment;
      private final int branchBase;

      public FoliageCoords(BlockPos $$0, int $$1) {
         this.attachment = new FoliagePlacer.FoliageAttachment($$0, 0, false);
         this.branchBase = $$1;
      }

      public int getBranchBase() {
         return this.branchBase;
      }
   }
}
