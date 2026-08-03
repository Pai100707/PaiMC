package net.minecraft.world.level.entity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface EntityPersistentStorage<T> extends AutoCloseable {
   CompletableFuture<ChunkEntities<T>> loadEntities(net.minecraft.world.level.ChunkPos var1);

   void storeEntities(ChunkEntities<T> var1);

   void flush(boolean var1);

   @Override
   default void close() throws IOException {
   }
}
