package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier implements DensityFunctions.BeardifierOrMarker {
   public static final int BEARD_KERNEL_RADIUS = 12;
   private static final int BEARD_KERNEL_SIZE = 24;
   private static final float[] BEARD_KERNEL = (float[])Util.make(new float[13824], $$0 -> {
      for (int $$1 = 0; $$1 < 24; $$1++) {
         for (int $$2 = 0; $$2 < 24; $$2++) {
            for (int $$3 = 0; $$3 < 24; $$3++) {
               $$0[$$1 * 24 * 24 + $$2 * 24 + $$3] = (float)computeBeardContribution($$2 - 12, $$3 - 12, $$1 - 12);
            }
         }
      }
   });
   public static final Beardifier EMPTY = new Beardifier(List.of(), List.of(), null);
   private final List<Beardifier.Rigid> pieces;
   private final List<JigsawJunction> junctions;
   
   private final BoundingBox affectedBox;

   public static Beardifier forStructuresInChunk(net.minecraft.world.level.StructureManager $$0, net.minecraft.world.level.ChunkPos $$1) {
      List<StructureStart> $$2 = $$0.startsForStructure($$1, $$0x -> $$0x.terrainAdaptation() != TerrainAdjustment.NONE);
      if ($$2.isEmpty()) {
         return EMPTY;
      } else {
         int $$3 = $$1.getMinBlockX();
         int $$4 = $$1.getMinBlockZ();
         List<Beardifier.Rigid> $$5 = new ArrayList<>();
         List<JigsawJunction> $$6 = new ArrayList<>();
         BoundingBox $$7 = null;

         for (StructureStart $$8 : $$2) {
            TerrainAdjustment $$9 = $$8.getStructure().terrainAdaptation();

            for (StructurePiece $$10 : $$8.getPieces()) {
               if ($$10.isCloseToChunk($$1, 12)) {
                  if ($$10 instanceof PoolElementStructurePiece $$11) {
                     StructureTemplatePool.Projection $$12 = $$11.getElement().getProjection();
                     if ($$12 == StructureTemplatePool.Projection.RIGID) {
                        $$5.add(new Beardifier.Rigid($$11.getBoundingBox(), $$9, $$11.getGroundLevelDelta()));
                        $$7 = includeBoundingBox($$7, $$10.getBoundingBox());
                     }

                     for (JigsawJunction $$13 : $$11.getJunctions()) {
                        int $$14 = $$13.getSourceX();
                        int $$15 = $$13.getSourceZ();
                        if ($$14 > $$3 - 12 && $$15 > $$4 - 12 && $$14 < $$3 + 15 + 12 && $$15 < $$4 + 15 + 12) {
                           $$6.add($$13);
                           BoundingBox $$16 = new BoundingBox(new BlockPos($$14, $$13.getSourceGroundY(), $$15));
                           $$7 = includeBoundingBox($$7, $$16);
                        }
                     }
                  } else {
                     $$5.add(new Beardifier.Rigid($$10.getBoundingBox(), $$9, 0));
                     $$7 = includeBoundingBox($$7, $$10.getBoundingBox());
                  }
               }
            }
         }

         if ($$7 == null) {
            return EMPTY;
         } else {
            BoundingBox $$17 = $$7.inflatedBy(24);
            return new Beardifier(List.copyOf($$5), List.copyOf($$6), $$17);
         }
      }
   }

   private static BoundingBox includeBoundingBox(BoundingBox $$0, BoundingBox $$1) {
      return $$0 == null ? $$1 : BoundingBox.encapsulating($$0, $$1);
   }

   @VisibleForTesting
   public Beardifier(List<Beardifier.Rigid> $$0, List<JigsawJunction> $$1, BoundingBox $$2) {
      this.pieces = $$0;
      this.junctions = $$1;
      this.affectedBox = $$2;
   }

   @Override
   public void fillArray(double[] $$0, DensityFunction.ContextProvider $$1) {
      if (this.affectedBox == null) {
         Arrays.fill($$0, 0.0);
      } else {
         DensityFunctions.BeardifierOrMarker.super.fillArray($$0, $$1);
      }
   }

   @Override
   public double compute(DensityFunction.FunctionContext $$0) {
      if (this.affectedBox == null) {
         return 0.0;
      } else {
         int $$1 = $$0.blockX();
         int $$2 = $$0.blockY();
         int $$3 = $$0.blockZ();
         if (!this.affectedBox.isInside($$1, $$2, $$3)) {
            return 0.0;
         } else {
            double $$4 = 0.0;

            for (Beardifier.Rigid $$5 : this.pieces) {
               BoundingBox $$6 = $$5.box();
               int $$7 = $$5.groundLevelDelta();
               int $$8 = Math.max(0, Math.max($$6.minX() - $$1, $$1 - $$6.maxX()));
               int $$9 = Math.max(0, Math.max($$6.minZ() - $$3, $$3 - $$6.maxZ()));
               int $$10 = $$6.minY() + $$7;
               int $$11 = $$2 - $$10;

               int $$12 = switch ($$5.terrainAdjustment()) {
                  case NONE -> 0;
                  case BURY, BEARD_THIN -> $$11;
                  case BEARD_BOX -> Math.max(0, Math.max($$10 - $$2, $$2 - $$6.maxY()));
                  case ENCAPSULATE -> Math.max(0, Math.max($$6.minY() - $$2, $$2 - $$6.maxY()));
               };

               $$4 += switch ($$5.terrainAdjustment()) {
                  case NONE -> 0.0;
                  case BURY -> getBuryContribution($$8, $$12 / 2.0, $$9);
                  case BEARD_THIN, BEARD_BOX -> getBeardContribution($$8, $$12, $$9, $$11) * 0.8;
                  case ENCAPSULATE -> getBuryContribution($$8 / 2.0, $$12 / 2.0, $$9 / 2.0) * 0.8;
               };
            }

            for (JigsawJunction $$13 : this.junctions) {
               int $$14 = $$1 - $$13.getSourceX();
               int $$15 = $$2 - $$13.getSourceGroundY();
               int $$16 = $$3 - $$13.getSourceZ();
               $$4 += getBeardContribution($$14, $$15, $$16, $$15) * 0.4;
            }

            return $$4;
         }
      }
   }

   @Override
   public double minValue() {
      return Double.NEGATIVE_INFINITY;
   }

   @Override
   public double maxValue() {
      return Double.POSITIVE_INFINITY;
   }

   private static double getBuryContribution(double $$0, double $$1, double $$2) {
      double $$3 = Mth.length($$0, $$1, $$2);
      return Mth.clampedMap($$3, 0.0, 6.0, 1.0, 0.0);
   }

   private static double getBeardContribution(int $$0, int $$1, int $$2, int $$3) {
      int $$4 = $$0 + 12;
      int $$5 = $$1 + 12;
      int $$6 = $$2 + 12;
      if (isInKernelRange($$4) && isInKernelRange($$5) && isInKernelRange($$6)) {
         double $$7 = $$3 + 0.5;
         double $$8 = Mth.lengthSquared($$0, $$7, $$2);
         double $$9 = -$$7 * Mth.fastInvSqrt($$8 / 2.0) / 2.0;
         return $$9 * BEARD_KERNEL[$$6 * 24 * 24 + $$4 * 24 + $$5];
      } else {
         return 0.0;
      }
   }

   private static boolean isInKernelRange(int $$0) {
      return $$0 >= 0 && $$0 < 24;
   }

   private static double computeBeardContribution(int $$0, int $$1, int $$2) {
      return computeBeardContribution($$0, $$1 + 0.5, $$2);
   }

   private static double computeBeardContribution(int $$0, double $$1, int $$2) {
      double $$3 = Mth.lengthSquared($$0, $$1, $$2);
      return Math.pow(Math.E, -$$3 / 16.0);
   }

   @VisibleForTesting
   public record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
   }
}
