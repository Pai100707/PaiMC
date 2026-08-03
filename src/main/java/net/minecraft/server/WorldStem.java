package net.minecraft.server;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.storage.WorldData;

public record WorldStem(
   CloseableResourceManager resourceManager,
   net.minecraft.server.ReloadableServerResources dataPackResources,
   LayeredRegistryAccess<net.minecraft.server.RegistryLayer> registries,
   WorldData worldData
) implements AutoCloseable {
   @Override
   public void close() {
      this.resourceManager.close();
   }
}
