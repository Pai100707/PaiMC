package net.minecraft.server.packs;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class AbstractPackResources implements PackResources {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final PackLocationInfo location;

   protected AbstractPackResources(PackLocationInfo $$0) {
      this.location = $$0;
   }

   @Nullable
   @Override
   public <T> T getMetadataSection(MetadataSectionType<T> $$0) throws IOException {
      IoSupplier<InputStream> $$1 = this.getRootResource(new String[]{"pack.mcmeta"});
      if ($$1 == null) {
         return null;
      } else {
         Object var4;
         try (InputStream $$2 = $$1.get()) {
            var4 = getMetadataFromStream($$0, $$2, this.location);
         }

         return (T)var4;
      }
   }

   @Nullable
   public static <T> T getMetadataFromStream(MetadataSectionType<T> $$0, InputStream $$1, PackLocationInfo $$2) {
      JsonObject $$4;
      try (BufferedReader $$3 = new BufferedReader(new InputStreamReader($$1, StandardCharsets.UTF_8))) {
         $$4 = GsonHelper.parse($$3);
      } catch (Exception var9) {
         LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{$$2.id(), $$0.name(), var9.getMessage()});
         return null;
      }

      return (T)(!$$4.has($$0.name())
         ? null
         : $$0.codec()
            .parse(JsonOps.INSTANCE, $$4.get($$0.name()))
            .ifError($$2x -> LOGGER.error("Couldn't load {} {} metadata: {}", new Object[]{$$2.id(), $$0.name(), $$2x.message()}))
            .result()
            .orElse(null));
   }

   @Override
   public PackLocationInfo location() {
      return this.location;
   }
}
