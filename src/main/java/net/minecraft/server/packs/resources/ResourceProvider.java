package net.minecraft.server.packs.resources;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

@FunctionalInterface
public interface ResourceProvider {
   ResourceProvider EMPTY = $$0 -> Optional.empty();

   Optional<Resource> getResource(Identifier var1);

   default Resource getResourceOrThrow(Identifier $$0) throws FileNotFoundException {
      return this.getResource($$0).orElseThrow(() -> new FileNotFoundException($$0.toString()));
   }

   default InputStream open(Identifier $$0) throws IOException {
      return this.getResourceOrThrow($$0).open();
   }

   default BufferedReader openAsReader(Identifier $$0) throws IOException {
      return this.getResourceOrThrow($$0).openAsReader();
   }

   static ResourceProvider fromMap(Map<Identifier, Resource> $$0) {
      return $$1 -> Optional.ofNullable($$0.get($$1));
   }
}
