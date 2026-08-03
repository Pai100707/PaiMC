package net.minecraft.world.level.pathfinder;

import it.unimi.dsi.fastutil.HashCommon;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

public class PathTypeCache {
   private static final int SIZE = 4096;
   private static final int MASK = 4095;
   private final long[] positions = new long[4096];
   private final PathType[] pathTypes = new PathType[4096];

   public PathType getOrCompute(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      long $$2 = $$1.asLong();
      int $$3 = index($$2);
      PathType $$4 = this.get($$3, $$2);
      return $$4 != null ? $$4 : this.compute($$0, $$1, $$3, $$2);
   }

   @Nullable
   private PathType get(int $$0, long $$1) {
      return this.positions[$$0] == $$1 ? this.pathTypes[$$0] : null;
   }

   private PathType compute(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, int $$2, long $$3) {
      PathType $$4 = WalkNodeEvaluator.getPathTypeFromState($$0, $$1);
      this.positions[$$2] = $$3;
      this.pathTypes[$$2] = $$4;
      return $$4;
   }

   public void invalidate(BlockPos $$0) {
      long $$1 = $$0.asLong();
      int $$2 = index($$1);
      if (this.positions[$$2] == $$1) {
         this.pathTypes[$$2] = null;
      }
   }

   private static int index(long $$0) {
      return (int)HashCommon.mix($$0) & 4095;
   }
}
