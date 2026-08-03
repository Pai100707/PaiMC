package net.minecraft.world.level.entity;

import java.util.List;
import java.util.stream.Stream;

public class ChunkEntities<T> {
   private final net.minecraft.world.level.ChunkPos pos;
   private final List<T> entities;

   public ChunkEntities(net.minecraft.world.level.ChunkPos $$0, List<T> $$1) {
      this.pos = $$0;
      this.entities = $$1;
   }

   public net.minecraft.world.level.ChunkPos getPos() {
      return this.pos;
   }

   public Stream<T> getEntities() {
      return this.entities.stream();
   }

   public boolean isEmpty() {
      return this.entities.isEmpty();
   }
}
