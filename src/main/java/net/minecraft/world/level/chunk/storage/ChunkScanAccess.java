package net.minecraft.world.level.chunk.storage;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.StreamTagVisitor;

public interface ChunkScanAccess {
   CompletableFuture<Void> scanChunk(net.minecraft.world.level.ChunkPos var1, StreamTagVisitor var2);
}
