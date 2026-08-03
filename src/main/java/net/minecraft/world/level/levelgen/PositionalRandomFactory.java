package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public interface PositionalRandomFactory {
   default RandomSource at(BlockPos $$0) {
      return this.at($$0.getX(), $$0.getY(), $$0.getZ());
   }

   default RandomSource fromHashOf(Identifier $$0) {
      return this.fromHashOf($$0.toString());
   }

   RandomSource fromHashOf(String var1);

   RandomSource fromSeed(long var1);

   RandomSource at(int var1, int var2, int var3);

   @VisibleForTesting
   void parityConfigString(StringBuilder var1);
}
