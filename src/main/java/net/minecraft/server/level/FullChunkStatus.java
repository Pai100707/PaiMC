package net.minecraft.server.level;

public enum FullChunkStatus {
   INACCESSIBLE,
   FULL,
   BLOCK_TICKING,
   ENTITY_TICKING;

   public boolean isOrAfter(FullChunkStatus $$0) {
      return this.ordinal() >= $$0.ordinal();
   }
}
