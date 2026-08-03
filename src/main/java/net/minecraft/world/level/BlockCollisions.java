package net.minecraft.world.level;

import com.google.common.collect.AbstractIterator;
import java.util.function.BiFunction;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockCollisions<T> extends AbstractIterator<T> {
   private final AABB box;
   private final CollisionContext context;
   private final Cursor3D cursor;
   private final MutableBlockPos pos;
   private final VoxelShape entityShape;
   private final net.minecraft.world.level.CollisionGetter collisionGetter;
   private final boolean onlySuffocatingBlocks;
   @Nullable
   private net.minecraft.world.level.BlockGetter cachedBlockGetter;
   private long cachedBlockGetterPos;
   private final BiFunction<MutableBlockPos, VoxelShape, T> resultProvider;

   public BlockCollisions(
      net.minecraft.world.level.CollisionGetter $$0, @Nullable Entity $$1, AABB $$2, boolean $$3, BiFunction<MutableBlockPos, VoxelShape, T> $$4
   ) {
      this($$0, $$1 == null ? CollisionContext.empty() : CollisionContext.of($$1), $$2, $$3, $$4);
   }

   public BlockCollisions(
      net.minecraft.world.level.CollisionGetter $$0, CollisionContext $$1, AABB $$2, boolean $$3, BiFunction<MutableBlockPos, VoxelShape, T> $$4
   ) {
      this.context = $$1;
      this.pos = new MutableBlockPos();
      this.entityShape = Shapes.create($$2);
      this.collisionGetter = $$0;
      this.box = $$2;
      this.onlySuffocatingBlocks = $$3;
      this.resultProvider = $$4;
      int $$5 = Mth.floor($$2.minX - 1.0E-7) - 1;
      int $$6 = Mth.floor($$2.maxX + 1.0E-7) + 1;
      int $$7 = Mth.floor($$2.minY - 1.0E-7) - 1;
      int $$8 = Mth.floor($$2.maxY + 1.0E-7) + 1;
      int $$9 = Mth.floor($$2.minZ - 1.0E-7) - 1;
      int $$10 = Mth.floor($$2.maxZ + 1.0E-7) + 1;
      this.cursor = new Cursor3D($$5, $$7, $$9, $$6, $$8, $$10);
   }

   @Nullable
   private net.minecraft.world.level.BlockGetter getChunk(int $$0, int $$1) {
      int $$2 = SectionPos.blockToSectionCoord($$0);
      int $$3 = SectionPos.blockToSectionCoord($$1);
      long $$4 = net.minecraft.world.level.ChunkPos.asLong($$2, $$3);
      if (this.cachedBlockGetter != null && this.cachedBlockGetterPos == $$4) {
         return this.cachedBlockGetter;
      } else {
         net.minecraft.world.level.BlockGetter $$5 = this.collisionGetter.getChunkForCollisions($$2, $$3);
         this.cachedBlockGetter = $$5;
         this.cachedBlockGetterPos = $$4;
         return $$5;
      }
   }

   protected T computeNext() {
      while (this.cursor.advance()) {
         int $$0 = this.cursor.nextX();
         int $$1 = this.cursor.nextY();
         int $$2 = this.cursor.nextZ();
         int $$3 = this.cursor.getNextType();
         if ($$3 != 3) {
            net.minecraft.world.level.BlockGetter $$4 = this.getChunk($$0, $$2);
            if ($$4 != null) {
               this.pos.set($$0, $$1, $$2);
               BlockState $$5 = $$4.getBlockState(this.pos);
               if ((!this.onlySuffocatingBlocks || $$5.isSuffocating($$4, this.pos))
                  && ($$3 != 1 || $$5.hasLargeCollisionShape())
                  && ($$3 != 2 || $$5.is(Blocks.MOVING_PISTON))) {
                  VoxelShape $$6 = this.context.getCollisionShape($$5, this.collisionGetter, this.pos);
                  if ($$6 == Shapes.block()) {
                     if (this.box.intersects($$0, $$1, $$2, $$0 + 1.0, $$1 + 1.0, $$2 + 1.0)) {
                        return this.resultProvider.apply(this.pos, $$6.move(this.pos));
                     }
                  } else {
                     VoxelShape $$7 = $$6.move(this.pos);
                     if (!$$7.isEmpty() && Shapes.joinIsNotEmpty($$7, this.entityShape, BooleanOp.AND)) {
                        return this.resultProvider.apply(this.pos, $$7);
                     }
                  }
               }
            }
         }
      }

      return (T)this.endOfData();
   }
}
