package net.minecraft.world.level;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionGetter extends net.minecraft.world.level.BlockGetter {
   WorldBorder getWorldBorder();

   
   net.minecraft.world.level.BlockGetter getChunkForCollisions(int var1, int var2);

   default boolean isUnobstructed(Entity $$0, VoxelShape $$1) {
      return true;
   }

   default boolean isUnobstructed(BlockState $$0, BlockPos $$1, CollisionContext $$2) {
      VoxelShape $$3 = $$0.getCollisionShape(this, $$1, $$2);
      return $$3.isEmpty() || this.isUnobstructed(null, $$3.move($$1));
   }

   default boolean isUnobstructed(Entity $$0) {
      return this.isUnobstructed($$0, Shapes.create($$0.getBoundingBox()));
   }

   default boolean noCollision(AABB $$0) {
      return this.noCollision(null, $$0);
   }

   default boolean noCollision(Entity $$0) {
      return this.noCollision($$0, $$0.getBoundingBox());
   }

   default boolean noCollision(Entity $$0, AABB $$1) {
      return this.noCollision($$0, $$1, false);
   }

   default boolean noCollision(Entity $$0, AABB $$1, boolean $$2) {
      return this.noBlockCollision($$0, $$1, $$2) && this.noEntityCollision($$0, $$1) && this.noBorderCollision($$0, $$1);
   }

   default boolean noBlockCollision(Entity $$0, AABB $$1) {
      return this.noBlockCollision($$0, $$1, false);
   }

   default boolean noBlockCollision(Entity $$0, AABB $$1, boolean $$2) {
      for (VoxelShape $$4 : $$2 ? this.getBlockAndLiquidCollisions($$0, $$1) : this.getBlockCollisions($$0, $$1)) {
         if (!$$4.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   default boolean noEntityCollision(Entity $$0, AABB $$1) {
      return this.getEntityCollisions($$0, $$1).isEmpty();
   }

   default boolean noBorderCollision(Entity $$0, AABB $$1) {
      if ($$0 == null) {
         return true;
      } else {
         VoxelShape $$2 = this.borderCollision($$0, $$1);
         return $$2 == null || !Shapes.joinIsNotEmpty($$2, Shapes.create($$1), BooleanOp.AND);
      }
   }

   List<VoxelShape> getEntityCollisions(Entity var1, AABB var2);

   default Iterable<VoxelShape> getCollisions(Entity $$0, AABB $$1) {
      List<VoxelShape> $$2 = this.getEntityCollisions($$0, $$1);
      Iterable<VoxelShape> $$3 = this.getBlockCollisions($$0, $$1);
      return $$2.isEmpty() ? $$3 : Iterables.concat($$2, $$3);
   }

   default Iterable<VoxelShape> getPreMoveCollisions(Entity $$0, AABB $$1, Vec3 $$2) {
      List<VoxelShape> $$3 = this.getEntityCollisions($$0, $$1);
      Iterable<VoxelShape> $$4 = this.getBlockCollisionsFromContext(CollisionContext.withPosition($$0, $$2.y), $$1);
      return $$3.isEmpty() ? $$4 : Iterables.concat($$3, $$4);
   }

   default Iterable<VoxelShape> getBlockCollisions(Entity $$0, AABB $$1) {
      return this.getBlockCollisionsFromContext($$0 == null ? CollisionContext.empty() : CollisionContext.of($$0), $$1);
   }

   default Iterable<VoxelShape> getBlockAndLiquidCollisions(Entity $$0, AABB $$1) {
      return this.getBlockCollisionsFromContext($$0 == null ? CollisionContext.emptyWithFluidCollisions() : CollisionContext.of($$0, true), $$1);
   }

   private Iterable<VoxelShape> getBlockCollisionsFromContext(CollisionContext $$0, AABB $$1) {
      return () -> new net.minecraft.world.level.BlockCollisions<VoxelShape>(this, $$0, $$1, false, ($$0xx, $$1xx) -> $$1xx);
   }

   
   private VoxelShape borderCollision(Entity $$0, AABB $$1) {
      WorldBorder $$2 = this.getWorldBorder();
      return $$2.isInsideCloseToBorder($$0, $$1) ? $$2.getCollisionShape() : null;
   }

   default BlockHitResult clipIncludingBorder(net.minecraft.world.level.ClipContext $$0) {
      BlockHitResult $$1 = this.clip($$0);
      WorldBorder $$2 = this.getWorldBorder();
      if ($$2.isWithinBounds($$0.getFrom()) && !$$2.isWithinBounds($$1.getLocation())) {
         Vec3 $$3 = $$1.getLocation().subtract($$0.getFrom());
         Direction $$4 = Direction.getApproximateNearest($$3.x, $$3.y, $$3.z);
         Vec3 $$5 = $$2.clampVec3ToBound($$1.getLocation());
         return new BlockHitResult($$5, $$4, BlockPos.containing($$5), false, true);
      } else {
         return $$1;
      }
   }

   default boolean collidesWithSuffocatingBlock(Entity $$0, AABB $$1) {
      net.minecraft.world.level.BlockCollisions<VoxelShape> $$2 = new net.minecraft.world.level.BlockCollisions<>(this, $$0, $$1, true, ($$0x, $$1x) -> $$1x);

      while ($$2.hasNext()) {
         if (!((VoxelShape)$$2.next()).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   default Optional<BlockPos> findSupportingBlock(Entity $$0, AABB $$1) {
      BlockPos $$2 = null;
      double $$3 = Double.MAX_VALUE;
      net.minecraft.world.level.BlockCollisions<BlockPos> $$4 = new net.minecraft.world.level.BlockCollisions<>(this, $$0, $$1, false, ($$0x, $$1x) -> $$0x);

      while ($$4.hasNext()) {
         BlockPos $$5 = (BlockPos)$$4.next();
         double $$6 = $$5.distToCenterSqr($$0.position());
         if ($$6 < $$3 || $$6 == $$3 && ($$2 == null || $$2.compareTo($$5) < 0)) {
            $$2 = $$5.immutable();
            $$3 = $$6;
         }
      }

      return Optional.ofNullable($$2);
   }

   default Optional<Vec3> findFreePosition(Entity $$0, VoxelShape $$1, Vec3 $$2, double $$3, double $$4, double $$5) {
      if ($$1.isEmpty()) {
         return Optional.empty();
      } else {
         AABB $$6 = $$1.bounds().inflate($$3, $$4, $$5);
         VoxelShape $$7 = StreamSupport.stream(this.getBlockCollisions($$0, $$6).spliterator(), false)
            .filter($$0x -> this.getWorldBorder() == null || this.getWorldBorder().isWithinBounds($$0x.bounds()))
            .flatMap($$0x -> $$0x.toAabbs().stream())
            .map($$3x -> $$3x.inflate($$3 / 2.0, $$4 / 2.0, $$5 / 2.0))
            .<VoxelShape>map(Shapes::create)
            .reduce(Shapes.empty(), Shapes::or);
         VoxelShape $$8 = Shapes.join($$1, $$7, BooleanOp.ONLY_FIRST);
         return $$8.closestPointTo($$2);
      }
   }
}
