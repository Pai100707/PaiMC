package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;

public class DatapackStructureReport implements net.minecraft.data.DataProvider {
   private final net.minecraft.data.PackOutput output;
   private static final DatapackStructureReport.Entry PSEUDO_REGISTRY = new DatapackStructureReport.Entry(true, false, true);
   private static final DatapackStructureReport.Entry STABLE_DYNAMIC_REGISTRY = new DatapackStructureReport.Entry(true, true, true);
   private static final DatapackStructureReport.Entry UNSTABLE_DYNAMIC_REGISTRY = new DatapackStructureReport.Entry(true, true, false);
   private static final DatapackStructureReport.Entry BUILT_IN_REGISTRY = new DatapackStructureReport.Entry(false, true, true);
   private static final Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> MANUAL_ENTRIES = Map.of(
      Registries.RECIPE,
      PSEUDO_REGISTRY,
      Registries.ADVANCEMENT,
      PSEUDO_REGISTRY,
      Registries.LOOT_TABLE,
      STABLE_DYNAMIC_REGISTRY,
      Registries.ITEM_MODIFIER,
      STABLE_DYNAMIC_REGISTRY,
      Registries.PREDICATE,
      STABLE_DYNAMIC_REGISTRY
   );
   private static final Map<String, DatapackStructureReport.CustomPackEntry> NON_REGISTRY_ENTRIES = Map.of(
      "structure",
      new DatapackStructureReport.CustomPackEntry(DatapackStructureReport.Format.STRUCTURE, new DatapackStructureReport.Entry(true, false, true)),
      "function",
      new DatapackStructureReport.CustomPackEntry(DatapackStructureReport.Format.MCFUNCTION, new DatapackStructureReport.Entry(true, true, true))
   );
   static final Codec<ResourceKey<? extends Registry<?>>> REGISTRY_KEY_CODEC = Identifier.CODEC.xmap(ResourceKey::createRegistryKey, ResourceKey::identifier);

   public DatapackStructureReport(net.minecraft.data.PackOutput $$0) {
      this.output = $$0;
   }

   @Override
   public CompletableFuture<?> run(net.minecraft.data.CachedOutput $$0) {
      DatapackStructureReport.Report $$1 = new DatapackStructureReport.Report(this.listRegistries(), NON_REGISTRY_ENTRIES);
      Path $$2 = this.output.getOutputFolder(net.minecraft.data.PackOutput.Target.REPORTS).resolve("datapack.json");
      return net.minecraft.data.DataProvider.saveStable(
         $$0, (JsonElement)DatapackStructureReport.Report.CODEC.encodeStart(JsonOps.INSTANCE, $$1).getOrThrow(), $$2
      );
   }

   @Override
   public String getName() {
      return "Datapack Structure";
   }

   private void putIfNotPresent(
      Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> $$0, ResourceKey<? extends Registry<?>> $$1, DatapackStructureReport.Entry $$2
   ) {
      DatapackStructureReport.Entry $$3 = $$0.putIfAbsent($$1, $$2);
      if ($$3 != null) {
         throw new IllegalStateException("Duplicate entry for key " + $$1.identifier());
      }
   }

   private Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> listRegistries() {
      Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> $$0 = new HashMap<>();
      BuiltInRegistries.REGISTRY.forEach($$1 -> this.putIfNotPresent($$0, $$1.key(), BUILT_IN_REGISTRY));
      RegistryDataLoader.WORLDGEN_REGISTRIES.forEach($$1 -> this.putIfNotPresent($$0, $$1.key(), UNSTABLE_DYNAMIC_REGISTRY));
      RegistryDataLoader.DIMENSION_REGISTRIES.forEach($$1 -> this.putIfNotPresent($$0, $$1.key(), UNSTABLE_DYNAMIC_REGISTRY));
      MANUAL_ENTRIES.forEach(($$1, $$2) -> this.putIfNotPresent($$0, (ResourceKey<? extends Registry<?>>)$$1, $$2));
      return $$0;
   }

   record CustomPackEntry(DatapackStructureReport.Format format, DatapackStructureReport.Entry entry) {
      public static final Codec<DatapackStructureReport.CustomPackEntry> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               DatapackStructureReport.Format.CODEC.fieldOf("format").forGetter(DatapackStructureReport.CustomPackEntry::format),
               DatapackStructureReport.Entry.MAP_CODEC.forGetter(DatapackStructureReport.CustomPackEntry::entry)
            )
            .apply($$0, DatapackStructureReport.CustomPackEntry::new)
      );
   }

   record Entry(boolean elements, boolean tags, boolean stable) {
      public static final MapCodec<DatapackStructureReport.Entry> MAP_CODEC = RecordCodecBuilder.mapCodec(
         $$0 -> $$0.group(
               Codec.BOOL.fieldOf("elements").forGetter(DatapackStructureReport.Entry::elements),
               Codec.BOOL.fieldOf("tags").forGetter(DatapackStructureReport.Entry::tags),
               Codec.BOOL.fieldOf("stable").forGetter(DatapackStructureReport.Entry::stable)
            )
            .apply($$0, DatapackStructureReport.Entry::new)
      );
      public static final Codec<DatapackStructureReport.Entry> CODEC = MAP_CODEC.codec();
   }

   static enum Format implements StringRepresentable {
      STRUCTURE("structure"),
      MCFUNCTION("mcfunction");

      public static final Codec<DatapackStructureReport.Format> CODEC = StringRepresentable.fromEnum(DatapackStructureReport.Format::values);
      private final String name;

      private Format(final String $$0) {
         this.name = $$0;
      }

      public String getSerializedName() {
         return this.name;
      }
   }

   record Report(Map<ResourceKey<? extends Registry<?>>, DatapackStructureReport.Entry> registries, Map<String, DatapackStructureReport.CustomPackEntry> others) {
      public static final Codec<DatapackStructureReport.Report> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.unboundedMap(DatapackStructureReport.REGISTRY_KEY_CODEC, DatapackStructureReport.Entry.CODEC)
                  .fieldOf("registries")
                  .forGetter(DatapackStructureReport.Report::registries),
               Codec.unboundedMap(Codec.STRING, DatapackStructureReport.CustomPackEntry.CODEC)
                  .fieldOf("others")
                  .forGetter(DatapackStructureReport.Report::others)
            )
            .apply($$0, DatapackStructureReport.Report::new)
      );
   }
}
