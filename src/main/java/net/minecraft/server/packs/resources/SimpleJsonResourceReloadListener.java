package net.minecraft.server.packs.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public abstract class SimpleJsonResourceReloadListener<T> extends SimplePreparableReloadListener<Map<Identifier, T>> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final DynamicOps<JsonElement> ops;
   private final Codec<T> codec;
   private final FileToIdConverter lister;

   protected SimpleJsonResourceReloadListener(Provider $$0, Codec<T> $$1, ResourceKey<? extends Registry<T>> $$2) {
      this($$0.createSerializationContext(JsonOps.INSTANCE), $$1, FileToIdConverter.registry($$2));
   }

   protected SimpleJsonResourceReloadListener(Codec<T> $$0, FileToIdConverter $$1) {
      this(JsonOps.INSTANCE, $$0, $$1);
   }

   private SimpleJsonResourceReloadListener(DynamicOps<JsonElement> $$0, Codec<T> $$1, FileToIdConverter $$2) {
      this.ops = $$0;
      this.codec = $$1;
      this.lister = $$2;
   }

   protected Map<Identifier, T> prepare(ResourceManager $$0, ProfilerFiller $$1) {
      Map<Identifier, T> $$2 = new HashMap<>();
      scanDirectory($$0, this.lister, this.ops, this.codec, $$2);
      return $$2;
   }

   public static <T> void scanDirectory(
      ResourceManager $$0, ResourceKey<? extends Registry<T>> $$1, DynamicOps<JsonElement> $$2, Codec<T> $$3, Map<Identifier, T> $$4
   ) {
      scanDirectory($$0, FileToIdConverter.registry($$1), $$2, $$3, $$4);
   }

   public static <T> void scanDirectory(ResourceManager $$0, FileToIdConverter $$1, DynamicOps<JsonElement> $$2, Codec<T> $$3, Map<Identifier, T> $$4) {
      for (Entry<Identifier, Resource> $$5 : $$1.listMatchingResources($$0).entrySet()) {
         Identifier $$6 = $$5.getKey();
         Identifier $$7 = $$1.fileToId($$6);

         try (Reader $$8 = $$5.getValue().openAsReader()) {
            $$3.parse($$2, StrictJsonParser.parse($$8)).ifSuccess($$2x -> {
               if ($$4.putIfAbsent($$7, (T)$$2x) != null) {
                  throw new IllegalStateException("Duplicate data file ignored with ID " + $$7);
               }
            }).ifError($$2x -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", new Object[]{$$7, $$6, $$2x}));
         } catch (IllegalArgumentException | IOException | JsonParseException var14) {
            LOGGER.error("Couldn't parse data file '{}' from '{}'", new Object[]{$$7, $$6, var14});
         }
      }
   }
}
