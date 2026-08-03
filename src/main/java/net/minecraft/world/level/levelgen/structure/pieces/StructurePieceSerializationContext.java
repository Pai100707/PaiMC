package net.minecraft.world.level.levelgen.structure.pieces;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public record StructurePieceSerializationContext(
   ResourceManager resourceManager, RegistryAccess registryAccess, StructureTemplateManager structureTemplateManager
) {
   public static StructurePieceSerializationContext fromLevel(ServerLevel $$0) {
      MinecraftServer $$1 = $$0.getServer();
      return new StructurePieceSerializationContext($$1.getResourceManager(), $$1.registryAccess(), $$1.getStructureManager());
   }
}
