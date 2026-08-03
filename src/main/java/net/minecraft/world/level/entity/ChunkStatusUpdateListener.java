package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;

@FunctionalInterface
public interface ChunkStatusUpdateListener {
   void onChunkStatusChange(net.minecraft.world.level.ChunkPos var1, FullChunkStatus var2);
}
