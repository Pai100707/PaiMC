package net.minecraft.world.level.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PathfindingContext {
   private final net.minecraft.world.level.CollisionGetter level;
   @Nullable
   private final PathTypeCache cache;
   private final BlockPos mobPosition;
   private final MutableBlockPos mutablePos = new MutableBlockPos();

   public PathfindingContext(net.minecraft.world.level.CollisionGetter $$0, Mob $$1) {
      this.level = $$0;
      if ($$1.level() instanceof ServerLevel $$2) {
         this.cache = $$2.getPathTypeCache();
      } else {
         this.cache = null;
      }

      this.mobPosition = $$1.blockPosition();
   }

   public PathType getPathTypeFromState(int $$0, int $$1, int $$2) {
      BlockPos $$3 = this.mutablePos.set($$0, $$1, $$2);
      return this.cache == null ? WalkNodeEvaluator.getPathTypeFromState(this.level, $$3) : this.cache.getOrCompute(this.level, $$3);
   }

   public BlockState getBlockState(BlockPos $$0) {
      return this.level.getBlockState($$0);
   }

   public net.minecraft.world.level.CollisionGetter level() {
      return this.level;
   }

   public BlockPos mobPosition() {
      return this.mobPosition;
   }
}
