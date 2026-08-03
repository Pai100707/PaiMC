package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.lighting.LevelLightEngine;

public interface BlockAndTintGetter extends net.minecraft.world.level.BlockGetter {
   float getShade(Direction var1, boolean var2);

   LevelLightEngine getLightEngine();

   int getBlockTint(BlockPos var1, net.minecraft.world.level.ColorResolver var2);

   default int getBrightness(net.minecraft.world.level.LightLayer $$0, BlockPos $$1) {
      return this.getLightEngine().getLayerListener($$0).getLightValue($$1);
   }

   default int getRawBrightness(BlockPos $$0, int $$1) {
      return this.getLightEngine().getRawBrightness($$0, $$1);
   }

   default boolean canSeeSky(BlockPos $$0) {
      return this.getBrightness(net.minecraft.world.level.LightLayer.SKY, $$0) >= 15;
   }
}
