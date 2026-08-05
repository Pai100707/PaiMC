package net.minecraft.world.level.portal;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.BlockUtil.FoundRectangle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableInt;

public class PortalShape {
   private static final int MIN_WIDTH = 2;
   public static final int MAX_WIDTH = 21;
   private static final int MIN_HEIGHT = 3;
   public static final int MAX_HEIGHT = 21;
   private static final BlockBehaviour.StatePredicate FRAME = ($$0, $$1, $$2) -> $$0.is(Blocks.OBSIDIAN);
   private static final float SAFE_TRAVEL_MAX_ENTITY_XY = 4.0F;
   private static final double SAFE_TRAVEL_MAX_VERTICAL_DELTA = 1.0;
   private final Axis axis;
   private final Direction rightDir;
   private final int numPortalBlocks;
   private final BlockPos bottomLeft;
   private final int height;
   private final int width;

   private PortalShape(Axis $$0, int $$1, Direction $$2, BlockPos $$3, int $$4, int $$5) {
      this.axis = $$0;
      this.numPortalBlocks = $$1;
      this.rightDir = $$2;
      this.bottomLeft = $$3;
      this.width = $$4;
      this.height = $$5;
   }

   public static Optional<PortalShape> findEmptyPortalShape(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Axis $$2) {
      return findPortalShape($$0, $$1, $$0x -> $$0x.isValid() && $$0x.numPortalBlocks == 0, $$2);
   }

   public static Optional<PortalShape> findPortalShape(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Predicate<PortalShape> $$2, Axis $$3) {
      Optional<PortalShape> $$4 = Optional.of(findAnyShape($$0, $$1, $$3)).filter($$2);
      if ($$4.isPresent()) {
         return $$4;
      } else {
         Axis $$5 = $$3 == Axis.X ? Axis.Z : Axis.X;
         return Optional.of(findAnyShape($$0, $$1, $$5)).filter($$2);
      }
   }

   public static PortalShape findAnyShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Axis $$2) {
      Direction $$3 = $$2 == Axis.X ? Direction.WEST : Direction.SOUTH;
      BlockPos $$4 = calculateBottomLeft($$0, $$3, $$1);
      if ($$4 == null) {
         return new PortalShape($$2, 0, $$3, $$1, 0, 0);
      } else {
         int $$5 = calculateWidth($$0, $$4, $$3);
         if ($$5 == 0) {
            return new PortalShape($$2, 0, $$3, $$4, 0, 0);
         } else {
            MutableInt $$6 = new MutableInt();
            int $$7 = calculateHeight($$0, $$4, $$3, $$5, $$6);
            return new PortalShape($$2, $$6.intValue(), $$3, $$4, $$5, $$7);
         }
      }
   }

   
   private static BlockPos calculateBottomLeft(net.minecraft.world.level.BlockGetter $$0, Direction $$1, BlockPos $$2) {
      int $$3 = Math.max($$0.getMinY(), $$2.getY() - 21);

      while ($$2.getY() > $$3 && isEmpty($$0.getBlockState($$2.below()))) {
         $$2 = $$2.below();
      }

      Direction $$4 = $$1.getOpposite();
      int $$5 = getDistanceUntilEdgeAboveFrame($$0, $$2, $$4) - 1;
      return $$5 < 0 ? null : $$2.relative($$4, $$5);
   }

   private static int calculateWidth(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      int $$3 = getDistanceUntilEdgeAboveFrame($$0, $$1, $$2);
      return $$3 >= 2 && $$3 <= 21 ? $$3 : 0;
   }

   private static int getDistanceUntilEdgeAboveFrame(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      MutableBlockPos $$3 = new MutableBlockPos();

      for (int $$4 = 0; $$4 <= 21; $$4++) {
         $$3.set($$1).move($$2, $$4);
         BlockState $$5 = $$0.getBlockState($$3);
         if (!isEmpty($$5)) {
            if (FRAME.test($$5, $$0, $$3)) {
               return $$4;
            }
            break;
         }

         BlockState $$6 = $$0.getBlockState($$3.move(Direction.DOWN));
         if (!FRAME.test($$6, $$0, $$3)) {
            break;
         }
      }

      return 0;
   }

   private static int calculateHeight(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2, int $$3, MutableInt $$4) {
      MutableBlockPos $$5 = new MutableBlockPos();
      int $$6 = getDistanceUntilTop($$0, $$1, $$2, $$5, $$3, $$4);
      return $$6 >= 3 && $$6 <= 21 && hasTopFrame($$0, $$1, $$2, $$5, $$3, $$6) ? $$6 : 0;
   }

   private static boolean hasTopFrame(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2, MutableBlockPos $$3, int $$4, int $$5) {
      for (int $$6 = 0; $$6 < $$4; $$6++) {
         MutableBlockPos $$7 = $$3.set($$1).move(Direction.UP, $$5).move($$2, $$6);
         if (!FRAME.test($$0.getBlockState($$7), $$0, $$7)) {
            return false;
         }
      }

      return true;
   }

   private static int getDistanceUntilTop(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2, MutableBlockPos $$3, int $$4, MutableInt $$5) {
      for (int $$6 = 0; $$6 < 21; $$6++) {
         $$3.set($$1).move(Direction.UP, $$6).move($$2, -1);
         if (!FRAME.test($$0.getBlockState($$3), $$0, $$3)) {
            return $$6;
         }

         $$3.set($$1).move(Direction.UP, $$6).move($$2, $$4);
         if (!FRAME.test($$0.getBlockState($$3), $$0, $$3)) {
            return $$6;
         }

         for (int $$7 = 0; $$7 < $$4; $$7++) {
            $$3.set($$1).move(Direction.UP, $$6).move($$2, $$7);
            BlockState $$8 = $$0.getBlockState($$3);
            if (!isEmpty($$8)) {
               return $$6;
            }

            if ($$8.is(Blocks.NETHER_PORTAL)) {
               $$5.increment();
            }
         }
      }

      return 21;
   }

   private static boolean isEmpty(BlockState $$0) {
      return $$0.isAir() || $$0.is(BlockTags.FIRE) || $$0.is(Blocks.NETHER_PORTAL);
   }

   public boolean isValid() {
      return this.width >= 2 && this.width <= 21 && this.height >= 3 && this.height <= 21;
   }

   public void createPortalBlocks(net.minecraft.world.level.LevelAccessor $$0) {
      BlockState $$1 = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, this.axis);
      BlockPos.betweenClosed(this.bottomLeft, this.bottomLeft.relative(Direction.UP, this.height - 1).relative(this.rightDir, this.width - 1))
         .forEach($$2 -> $$0.setBlock($$2, $$1, 18));
   }

   public boolean isComplete() {
      return this.isValid() && this.numPortalBlocks == this.width * this.height;
   }

   public static Vec3 getRelativePosition(FoundRectangle $$0, Axis $$1, Vec3 $$2, EntityDimensions $$3) {
      double $$4 = (double)$$0.axis1Size - $$3.width();
      double $$5 = (double)$$0.axis2Size - $$3.height();
      BlockPos $$6 = $$0.minCorner;
      double $$8;
      if ($$4 > 0.0) {
         double $$7 = $$6.get($$1) + $$3.width() / 2.0;
         $$8 = Mth.clamp(Mth.inverseLerp($$2.get($$1) - $$7, 0.0, $$4), 0.0, 1.0);
      } else {
         $$8 = 0.5;
      }

      double $$11;
      if ($$5 > 0.0) {
         Axis $$10 = Axis.Y;
         $$11 = Mth.clamp(Mth.inverseLerp($$2.get($$10) - $$6.get($$10), 0.0, $$5), 0.0, 1.0);
      } else {
         $$11 = 0.0;
      }

      Axis $$13 = $$1 == Axis.X ? Axis.Z : Axis.X;
      double $$14 = $$2.get($$13) - ($$6.get($$13) + 0.5);
      return new Vec3($$8, $$11, $$14);
   }

   public static Vec3 findCollisionFreePosition(Vec3 $$0, ServerLevel $$1, Entity $$2, EntityDimensions $$3) {
      if (!($$3.width() > 4.0F) && !($$3.height() > 4.0F)) {
         double $$4 = $$3.height() / 2.0;
         Vec3 $$5 = $$0.add(0.0, $$4, 0.0);
         VoxelShape $$6 = Shapes.create(AABB.ofSize($$5, $$3.width(), 0.0, $$3.width()).expandTowards(0.0, 1.0, 0.0).inflate(1.0E-6));
         Optional<Vec3> $$7 = $$1.findFreePosition($$2, $$6, $$5, $$3.width(), $$3.height(), $$3.width());
         Optional<Vec3> $$8 = $$7.map($$1x -> $$1x.subtract(0.0, $$4, 0.0));
         return $$8.orElse($$0);
      } else {
         return $$0;
      }
   }
}
