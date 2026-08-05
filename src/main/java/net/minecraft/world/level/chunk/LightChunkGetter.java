package net.minecraft.world.level.chunk;

import net.minecraft.core.SectionPos;

public interface LightChunkGetter {
   
   LightChunk getChunkForLighting(int var1, int var2);

   default void onLightUpdate(net.minecraft.world.level.LightLayer $$0, SectionPos $$1) {
   }

   net.minecraft.world.level.BlockGetter getLevel();
}
