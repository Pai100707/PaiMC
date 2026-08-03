package net.minecraft.world.entity;

public enum EntitySpawnReason {
   NATURAL,
   CHUNK_GENERATION,
   SPAWNER,
   STRUCTURE,
   BREEDING,
   MOB_SUMMONED,
   JOCKEY,
   EVENT,
   CONVERSION,
   REINFORCEMENT,
   TRIGGERED,
   BUCKET,
   SPAWN_ITEM_USE,
   COMMAND,
   DISPENSER,
   PATROL,
   TRIAL_SPAWNER,
   LOAD,
   DIMENSION_TRAVEL;

   public static boolean isSpawner(net.minecraft.world.entity.EntitySpawnReason $$0) {
      return $$0 == SPAWNER || $$0 == TRIAL_SPAWNER;
   }

   public static boolean ignoresLightRequirements(net.minecraft.world.entity.EntitySpawnReason $$0) {
      return $$0 == TRIAL_SPAWNER;
   }
}
