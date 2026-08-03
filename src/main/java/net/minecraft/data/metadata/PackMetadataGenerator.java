package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;
   private final Map<String, Supplier<JsonElement>> elements = new HashMap<>();

   public PackMetadataGenerator(net.minecraft.data.PackOutput $$0) {
      this.output = $$0;
   }

   public <T> PackMetadataGenerator add(MetadataSectionType<T> $$0, T $$1) {
      this.elements
         .put($$0.name(), () -> ((JsonElement)$$0.codec().encodeStart(JsonOps.INSTANCE, $$1).getOrThrow(IllegalArgumentException::new)).getAsJsonObject());
      return this;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      JsonObject $$1 = new JsonObject();
      this.elements.forEach(($$1x, $$2) -> $$1.add($$1x, $$2.get()));
      return net.minecraft.data.DataProvider.saveStable($$0, $$1, this.output.getOutputFolder().resolve("pack.mcmeta"));
   }

   @Override
   public final String getName() {
      return "Pack Metadata";
   }

   public static PackMetadataGenerator forFeaturePack(net.minecraft.data.PackOutput $$0, Component $$1) {
      return new PackMetadataGenerator($$0)
         .add(PackMetadataSection.SERVER_TYPE, new PackMetadataSection($$1, DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA).minorRange()));
   }

   public static PackMetadataGenerator forFeaturePack(net.minecraft.data.PackOutput $$0, Component $$1, FeatureFlagSet $$2) {
      return forFeaturePack($$0, $$1).add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection($$2));
   }
}
