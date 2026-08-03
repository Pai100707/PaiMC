package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public interface BlockGetter extends net.minecraft.world.level.LevelHeightAccessor {
   @Nullable
   BlockEntity getBlockEntity(BlockPos var1);

   default <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos $$0, BlockEntityType<T> $$1) {
      BlockEntity $$2 = this.getBlockEntity($$0);
      return $$2 != null && $$2.getType() == $$1 ? Optional.of((T)$$2) : Optional.empty();
   }

   BlockState getBlockState(BlockPos var1);

   FluidState getFluidState(BlockPos var1);

   default int getLightEmission(BlockPos $$0) {
      return this.getBlockState($$0).getLightEmission();
   }

   default Stream<BlockState> getBlockStates(AABB $$0) {
      return BlockPos.betweenClosedStream($$0).map(this::getBlockState);
   }

   default BlockHitResult isBlockInLine(net.minecraft.world.level.ClipBlockStateContext $$0) {
      return traverseBlocks(
         $$0.getFrom(),
         $$0.getTo(),
         $$0,
         ($$0x, $$1) -> {
            BlockState $$2 = this.getBlockState($$1);
            Vec3 $$3 = $$0x.getFrom().subtract($$0x.getTo());
            return $$0x.isTargetBlock().test($$2)
               ? new BlockHitResult($$0x.getTo(), Direction.getApproximateNearest($$3.x, $$3.y, $$3.z), BlockPos.containing($$0x.getTo()), false)
               : null;
         },
         $$0x -> {
            Vec3 $$1 = $$0x.getFrom().subtract($$0x.getTo());
            return BlockHitResult.miss($$0x.getTo(), Direction.getApproximateNearest($$1.x, $$1.y, $$1.z), BlockPos.containing($$0x.getTo()));
         }
      );
   }

   default BlockHitResult clip(net.minecraft.world.level.ClipContext $$0) {
      return traverseBlocks($$0.getFrom(), $$0.getTo(), $$0, ($$0x, $$1) -> {
         BlockState $$2 = this.getBlockState($$1);
         FluidState $$3 = this.getFluidState($$1);
         Vec3 $$4 = $$0x.getFrom();
         Vec3 $$5 = $$0x.getTo();
         VoxelShape $$6 = $$0x.getBlockShape($$2, this, $$1);
         BlockHitResult $$7 = this.clipWithInteractionOverride($$4, $$5, $$1, $$6, $$2);
         VoxelShape $$8 = $$0x.getFluidShape($$3, this, $$1);
         BlockHitResult $$9 = $$8.clip($$4, $$5, $$1);
         double $$10 = $$7 == null ? Double.MAX_VALUE : $$0x.getFrom().distanceToSqr($$7.getLocation());
         double $$11 = $$9 == null ? Double.MAX_VALUE : $$0x.getFrom().distanceToSqr($$9.getLocation());
         return $$10 <= $$11 ? $$7 : $$9;
      }, $$0x -> {
         Vec3 $$1 = $$0x.getFrom().subtract($$0x.getTo());
         return BlockHitResult.miss($$0x.getTo(), Direction.getApproximateNearest($$1.x, $$1.y, $$1.z), BlockPos.containing($$0x.getTo()));
      });
   }

   @Nullable
   default BlockHitResult clipWithInteractionOverride(Vec3 $$0, Vec3 $$1, BlockPos $$2, VoxelShape $$3, BlockState $$4) {
      BlockHitResult $$5 = $$3.clip($$0, $$1, $$2);
      if ($$5 != null) {
         BlockHitResult $$6 = $$4.getInteractionShape(this, $$2).clip($$0, $$1, $$2);
         if ($$6 != null && $$6.getLocation().subtract($$0).lengthSqr() < $$5.getLocation().subtract($$0).lengthSqr()) {
            return $$5.withDirection($$6.getDirection());
         }
      }

      return $$5;
   }

   default double getBlockFloorHeight(VoxelShape $$0, Supplier<VoxelShape> $$1) {
      if (!$$0.isEmpty()) {
         return $$0.max(Axis.Y);
      } else {
         double $$2 = $$1.get().max(Axis.Y);
         return $$2 >= 1.0 ? $$2 - 1.0 : Double.NEGATIVE_INFINITY;
      }
   }

   default double getBlockFloorHeight(BlockPos $$0) {
      return this.getBlockFloorHeight(this.getBlockState($$0).getCollisionShape(this, $$0), () -> {
         BlockPos $$1 = $$0.below();
         return this.getBlockState($$1).getCollisionShape(this, $$1);
      });
   }

   static <T, C> T traverseBlocks(Vec3 $$0, Vec3 $$1, C $$2, BiFunction<C, BlockPos, T> $$3, Function<C, T> $$4) {
      if ($$0.equals($$1)) {
         return $$4.apply($$2);
      } else {
         double $$5 = Mth.lerp(-1.0E-7, $$1.x, $$0.x);
         double $$6 = Mth.lerp(-1.0E-7, $$1.y, $$0.y);
         double $$7 = Mth.lerp(-1.0E-7, $$1.z, $$0.z);
         double $$8 = Mth.lerp(-1.0E-7, $$0.x, $$1.x);
         double $$9 = Mth.lerp(-1.0E-7, $$0.y, $$1.y);
         double $$10 = Mth.lerp(-1.0E-7, $$0.z, $$1.z);
         int $$11 = Mth.floor($$8);
         int $$12 = Mth.floor($$9);
         int $$13 = Mth.floor($$10);
         MutableBlockPos $$14 = new MutableBlockPos($$11, $$12, $$13);
         T $$15 = $$3.apply($$2, $$14);
         if ($$15 != null) {
            return $$15;
         } else {
            double $$16 = $$5 - $$8;
            double $$17 = $$6 - $$9;
            double $$18 = $$7 - $$10;
            int $$19 = Mth.sign($$16);
            int $$20 = Mth.sign($$17);
            int $$21 = Mth.sign($$18);
            double $$22 = $$19 == 0 ? Double.MAX_VALUE : $$19 / $$16;
            double $$23 = $$20 == 0 ? Double.MAX_VALUE : $$20 / $$17;
            double $$24 = $$21 == 0 ? Double.MAX_VALUE : $$21 / $$18;
            double $$25 = $$22 * ($$19 > 0 ? 1.0 - Mth.frac($$8) : Mth.frac($$8));
            double $$26 = $$23 * ($$20 > 0 ? 1.0 - Mth.frac($$9) : Mth.frac($$9));
            double $$27 = $$24 * ($$21 > 0 ? 1.0 - Mth.frac($$10) : Mth.frac($$10));

            while ($$25 <= 1.0 || $$26 <= 1.0 || $$27 <= 1.0) {
               if ($$25 < $$26) {
                  if ($$25 < $$27) {
                     $$11 += $$19;
                     $$25 += $$22;
                  } else {
                     $$13 += $$21;
                     $$27 += $$24;
                  }
               } else if ($$26 < $$27) {
                  $$12 += $$20;
                  $$26 += $$23;
               } else {
                  $$13 += $$21;
                  $$27 += $$24;
               }

               T $$28 = $$3.apply($$2, $$14.set($$11, $$12, $$13));
               if ($$28 != null) {
                  return $$28;
               }
            }

            return $$4.apply($$2);
         }
      }
   }

   static boolean forEachBlockIntersectedBetween(Vec3 $$0, Vec3 $$1, AABB $$2, net.minecraft.world.level.BlockGetter.BlockStepVisitor $$3) {
      Vec3 $$4 = $$1.subtract($$0);
      if ($$4.lengthSqr() < Mth.square(1.0E-5F)) {
         for (BlockPos $$5 : BlockPos.betweenClosed($$2)) {
            if (!$$3.visit($$5, 0)) {
               return false;
            }
         }

         return true;
      } else {
         LongSet $$6 = new LongOpenHashSet();

         for (BlockPos $$7 : BlockPos.betweenCornersInDirection($$2.move($$4.scale(-1.0)), $$4)) {
            if (!$$3.visit($$7, 0)) {
               return false;
            }

            $$6.add($$7.asLong());
         }

         int $$8 = addCollisionsAlongTravel($$6, $$4, $$2, $$3);
         if ($$8 < 0) {
            return false;
         } else {
            for (BlockPos $$9 : BlockPos.betweenCornersInDirection($$2, $$4)) {
               if ($$6.add($$9.asLong()) && !$$3.visit($$9, $$8 + 1)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private static int addCollisionsAlongTravel(LongSet $$0, Vec3 $$1, AABB $$2, net.minecraft.world.level.BlockGetter.BlockStepVisitor $$3) {
      double $$4 = $$2.getXsize();
      double $$5 = $$2.getYsize();
      double $$6 = $$2.getZsize();
      Vec3i $$7 = getFurthestCorner($$1);
      Vec3 $$8 = $$2.getCenter();
      Vec3 $$9 = new Vec3($$8.x() + $$4 * 0.5 * $$7.getX(), $$8.y() + $$5 * 0.5 * $$7.getY(), $$8.z() + $$6 * 0.5 * $$7.getZ());
      Vec3 $$10 = $$9.subtract($$1);
      int $$11 = Mth.floor($$10.x);
      int $$12 = Mth.floor($$10.y);
      int $$13 = Mth.floor($$10.z);
      int $$14 = Mth.sign($$1.x);
      int $$15 = Mth.sign($$1.y);
      int $$16 = Mth.sign($$1.z);
      double $$17 = $$14 == 0 ? Double.MAX_VALUE : $$14 / $$1.x;
      double $$18 = $$15 == 0 ? Double.MAX_VALUE : $$15 / $$1.y;
      double $$19 = $$16 == 0 ? Double.MAX_VALUE : $$16 / $$1.z;
      double $$20 = $$17 * ($$14 > 0 ? 1.0 - Mth.frac($$10.x) : Mth.frac($$10.x));
      double $$21 = $$18 * ($$15 > 0 ? 1.0 - Mth.frac($$10.y) : Mth.frac($$10.y));
      double $$22 = $$19 * ($$16 > 0 ? 1.0 - Mth.frac($$10.z) : Mth.frac($$10.z));
      int $$23 = 0;

      while ($$20 <= 1.0 || $$21 <= 1.0 || $$22 <= 1.0) {
         if ($$20 < $$21) {
            if ($$20 < $$22) {
               $$11 += $$14;
               $$20 += $$17;
            } else {
               $$13 += $$16;
               $$22 += $$19;
            }
         } else if ($$21 < $$22) {
            $$12 += $$15;
            $$21 += $$18;
         } else {
            $$13 += $$16;
            $$22 += $$19;
         }

         Optional<Vec3> $$24 = AABB.clip($$11, $$12, $$13, $$11 + 1, $$12 + 1, $$13 + 1, $$10, $$9);
         if (!$$24.isEmpty()) {
            $$23++;
            Vec3 $$25 = $$24.get();
            double $$26 = Mth.clamp($$25.x, $$11 + 1.0E-5F, $$11 + 1.0 - 1.0E-5F);
            double $$27 = Mth.clamp($$25.y, $$12 + 1.0E-5F, $$12 + 1.0 - 1.0E-5F);
            double $$28 = Mth.clamp($$25.z, $$13 + 1.0E-5F, $$13 + 1.0 - 1.0E-5F);
            int $$29 = Mth.floor($$26 - $$4 * $$7.getX());
            int $$30 = Mth.floor($$27 - $$5 * $$7.getY());
            int $$31 = Mth.floor($$28 - $$6 * $$7.getZ());
            int $$32 = $$23;

            for (BlockPos $$33 : BlockPos.betweenCornersInDirection($$11, $$12, $$13, $$29, $$30, $$31, $$1)) {
               if ($$0.add($$33.asLong()) && !$$3.visit($$33, $$32)) {
                  return -1;
               }
            }
         }
      }

      return $$23;
   }

   private static Vec3i getFurthestCorner(Vec3 $$0) {
      double $$1 = Math.abs(Vec3.X_AXIS.dot($$0));
      double $$2 = Math.abs(Vec3.Y_AXIS.dot($$0));
      double $$3 = Math.abs(Vec3.Z_AXIS.dot($$0));
      int $$4 = $$0.x >= 0.0 ? 1 : -1;
      int $$5 = $$0.y >= 0.0 ? 1 : -1;
      int $$6 = $$0.z >= 0.0 ? 1 : -1;
      if ($$1 <= $$2 && $$1 <= $$3) {
         return new Vec3i(-$$4, -$$6, $$5);
      } else {
         return $$2 <= $$3 ? new Vec3i($$6, -$$5, -$$4) : new Vec3i(-$$5, $$4, -$$6);
      }
   }

   @FunctionalInterface
   public interface BlockStepVisitor {
      boolean visit(BlockPos var1, int var2);
   }
}
