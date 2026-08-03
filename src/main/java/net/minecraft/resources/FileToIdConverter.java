package net.minecraft.resources;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class FileToIdConverter {
   private final String prefix;
   private final String extension;

   public FileToIdConverter(String $$0, String $$1) {
      this.prefix = $$0;
      this.extension = $$1;
   }

   public static net.minecraft.resources.FileToIdConverter json(String $$0) {
      return new net.minecraft.resources.FileToIdConverter($$0, ".json");
   }

   public static net.minecraft.resources.FileToIdConverter registry(net.minecraft.resources.ResourceKey<? extends Registry<?>> $$0) {
      return json(Registries.elementsDirPath($$0));
   }

   public net.minecraft.resources.Identifier idToFile(net.minecraft.resources.Identifier $$0) {
      return $$0.withPath(this.prefix + "/" + $$0.getPath() + this.extension);
   }

   public net.minecraft.resources.Identifier fileToId(net.minecraft.resources.Identifier $$0) {
      String $$1 = $$0.getPath();
      return $$0.withPath($$1.substring(this.prefix.length() + 1, $$1.length() - this.extension.length()));
   }

   public Map<net.minecraft.resources.Identifier, Resource> listMatchingResources(ResourceManager $$0) {
      return $$0.listResources(this.prefix, $$0x -> $$0x.getPath().endsWith(this.extension));
   }

   public Map<net.minecraft.resources.Identifier, List<Resource>> listMatchingResourceStacks(ResourceManager $$0) {
      return $$0.listResourceStacks(this.prefix, $$0x -> $$0x.getPath().endsWith(this.extension));
   }
}
