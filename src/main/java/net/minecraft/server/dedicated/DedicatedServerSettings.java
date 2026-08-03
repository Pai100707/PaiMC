package net.minecraft.server.dedicated;

import java.nio.file.Path;
import java.util.function.UnaryOperator;

public class DedicatedServerSettings {
   private final Path source;
   private DedicatedServerProperties properties;

   public DedicatedServerSettings(Path $$0) {
      this.source = $$0;
      this.properties = DedicatedServerProperties.fromFile($$0);
   }

   public DedicatedServerProperties getProperties() {
      return this.properties;
   }

   public void forceSave() {
      this.properties.store(this.source);
   }

   public DedicatedServerSettings update(UnaryOperator<DedicatedServerProperties> $$0) {
      (this.properties = $$0.apply(this.properties)).store(this.source);
      return this;
   }
}
