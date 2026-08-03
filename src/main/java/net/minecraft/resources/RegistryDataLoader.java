package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.RegistrySynchronization.PackedRegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization.NetworkPayload;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilusVariant;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise.NoiseParameters;
import net.minecraft.world.timeline.Timeline;
import org.slf4j.Logger;

public class RegistryDataLoader {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Comparator<net.minecraft.resources.ResourceKey<?>> ERROR_KEY_COMPARATOR = Comparator.comparing(
         net.minecraft.resources.ResourceKey::registry
      )
      .thenComparing(net.minecraft.resources.ResourceKey::identifier);
   private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
   private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize($$0 -> {
      Lifecycle $$1 = $$0.<Boolean>map(KnownPack::isVanilla).map($$0x -> Lifecycle.stable()).orElse(Lifecycle.experimental());
      return new RegistrationInfo($$0, $$1);
   });
   public static final List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> WORLDGEN_REGISTRIES = List.of(
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.BIOME, Biome.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.STRUCTURE, Structure.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.NOISE, NoiseParameters.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TRIAL_SPAWNER_CONFIG, TrialSpawnerConfig.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PIG_VARIANT, PigVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.FROG_VARIANT, FrogVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CAT_VARIANT, CatVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.COW_VARIANT, CowVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CHICKEN_VARIANT, ChickenVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(
         Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC
      ),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.ENCHANTMENT_PROVIDER, EnchantmentProvider.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.INSTRUMENT, Instrument.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DIALOG, Dialog.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TIMELINE, Timeline.DIRECT_CODEC)
   );
   public static final List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> DIMENSION_REGISTRIES = List.of(
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.LEVEL_STEM, LevelStem.CODEC)
   );
   public static final List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> SYNCHRONIZED_REGISTRIES = List.of(
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.BIOME, Biome.NETWORK_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.WOLF_VARIANT, WolfVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PIG_VARIANT, PigVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.FROG_VARIANT, FrogVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CAT_VARIANT, CatVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.COW_VARIANT, CowVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.CHICKEN_VARIANT, ChickenVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.NETWORK_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DIMENSION_TYPE, DimensionType.NETWORK_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.INSTRUMENT, Instrument.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.DIALOG, Dialog.DIRECT_CODEC),
      new net.minecraft.resources.RegistryDataLoader.RegistryData(Registries.TIMELINE, Timeline.NETWORK_CODEC)
   );

   public static Frozen load(ResourceManager $$0, List<RegistryLookup<?>> $$1, List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> $$2) {
      return load((net.minecraft.resources.RegistryDataLoader.LoadingFunction)(($$1x, $$2x) -> $$1x.loadFromResources($$0, $$2x)), $$1, $$2);
   }

   public static Frozen load(
      Map<net.minecraft.resources.ResourceKey<? extends Registry<?>>, net.minecraft.resources.RegistryDataLoader.NetworkedRegistryData> $$0,
      ResourceProvider $$1,
      List<RegistryLookup<?>> $$2,
      List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> $$3
   ) {
      return load((net.minecraft.resources.RegistryDataLoader.LoadingFunction)(($$2x, $$3x) -> $$2x.loadFromNetwork($$0, $$1, $$3x)), $$2, $$3);
   }

   private static Frozen load(
      net.minecraft.resources.RegistryDataLoader.LoadingFunction $$0,
      List<RegistryLookup<?>> $$1,
      List<net.minecraft.resources.RegistryDataLoader.RegistryData<?>> $$2
   ) {
      Map<net.minecraft.resources.ResourceKey<?>, Exception> $$3 = new HashMap<>();
      List<net.minecraft.resources.RegistryDataLoader.Loader<?>> $$4 = $$2.stream()
         .map($$1x -> $$1x.create(Lifecycle.stable(), $$3))
         .collect(Collectors.toUnmodifiableList());
      net.minecraft.resources.RegistryOps.RegistryInfoLookup $$5 = createContext($$1, $$4);
      $$4.forEach($$2x -> $$0.apply($$2x, $$5));
      $$4.forEach($$1x -> {
         Registry<?> $$2x = $$1x.registry();

         try {
            $$2x.freeze();
         } catch (Exception var4x) {
            $$3.put($$2x.key(), var4x);
         }

         if ($$1x.data.requiredNonEmpty && $$2x.size() == 0) {
            $$3.put($$2x.key(), new IllegalStateException("Registry must be non-empty: " + $$2x.key().identifier()));
         }
      });
      if (!$$3.isEmpty()) {
         throw logErrors($$3);
      } else {
         return new ImmutableRegistryAccess($$4.stream().map(net.minecraft.resources.RegistryDataLoader.Loader::registry).toList()).freeze();
      }
   }

   private static net.minecraft.resources.RegistryOps.RegistryInfoLookup createContext(
      List<RegistryLookup<?>> $$0, List<net.minecraft.resources.RegistryDataLoader.Loader<?>> $$1
   ) {
      final Map<net.minecraft.resources.ResourceKey<? extends Registry<?>>, net.minecraft.resources.RegistryOps.RegistryInfo<?>> $$2 = new HashMap<>();
      $$0.forEach($$1x -> $$2.put($$1x.key(), createInfoForContextRegistry($$1x)));
      $$1.forEach($$1x -> $$2.put($$1x.registry.key(), createInfoForNewRegistry($$1x.registry)));
      return new net.minecraft.resources.RegistryOps.RegistryInfoLookup() {
         @Override
         public <T> Optional<net.minecraft.resources.RegistryOps.RegistryInfo<T>> lookup(
            net.minecraft.resources.ResourceKey<? extends Registry<? extends T>> $$0
         ) {
            return Optional.ofNullable((net.minecraft.resources.RegistryOps.RegistryInfo<T>)$$2.get($$0));
         }
      };
   }

   private static <T> net.minecraft.resources.RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> $$0) {
      return new net.minecraft.resources.RegistryOps.RegistryInfo<>($$0, $$0.createRegistrationLookup(), $$0.registryLifecycle());
   }

   private static <T> net.minecraft.resources.RegistryOps.RegistryInfo<T> createInfoForContextRegistry(RegistryLookup<T> $$0) {
      return new net.minecraft.resources.RegistryOps.RegistryInfo<>($$0, $$0, $$0.registryLifecycle());
   }

   private static ReportedException logErrors(Map<net.minecraft.resources.ResourceKey<?>, Exception> $$0) {
      printFullDetailsToLog($$0);
      return createReportWithBriefInfo($$0);
   }

   private static void printFullDetailsToLog(Map<net.minecraft.resources.ResourceKey<?>, Exception> $$0) {
      StringWriter $$1 = new StringWriter();
      PrintWriter $$2 = new PrintWriter($$1);
      Map<net.minecraft.resources.Identifier, Map<net.minecraft.resources.Identifier, Exception>> $$3 = $$0.entrySet()
         .stream()
         .collect(
            Collectors.groupingBy(
               $$0x -> ((net.minecraft.resources.ResourceKey)$$0x.getKey()).registry(),
               Collectors.toMap($$0x -> ((net.minecraft.resources.ResourceKey)$$0x.getKey()).identifier(), Entry::getValue)
            )
         );
      $$3.entrySet().stream().sorted(Entry.comparingByKey()).forEach($$1x -> {
         $$2.printf(Locale.ROOT, "> Errors in registry %s:%n", $$1x.getKey());
         ((Map)$$1x.getValue()).entrySet().stream().sorted(Entry.comparingByKey()).forEach($$1xx -> {
            $$2.printf(Locale.ROOT, ">> Errors in element %s:%n", $$1xx.getKey());
            ((Exception)$$1xx.getValue()).printStackTrace($$2);
         });
      });
      $$2.flush();
      LOGGER.error("Registry loading errors:\n{}", $$1);
   }

   private static ReportedException createReportWithBriefInfo(Map<net.minecraft.resources.ResourceKey<?>, Exception> $$0) {
      CrashReport $$1 = CrashReport.forThrowable(new IllegalStateException("Failed to load registries due to errors"), "Registry Loading");
      CrashReportCategory $$2 = $$1.addCategory("Loading info");
      $$2.setDetail(
         "Errors",
         () -> {
            StringBuilder $$1x = new StringBuilder();
            $$0.entrySet()
               .stream()
               .sorted(Entry.comparingByKey(ERROR_KEY_COMPARATOR))
               .forEach(
                  $$1xx -> $$1x.append("\n\t\t")
                     .append(((net.minecraft.resources.ResourceKey)$$1xx.getKey()).registry())
                     .append("/")
                     .append(((net.minecraft.resources.ResourceKey)$$1xx.getKey()).identifier())
                     .append(": ")
                     .append(((Exception)$$1xx.getValue()).getMessage())
               );
            return $$1x.toString();
         }
      );
      return new ReportedException($$1);
   }

   private static <E> void loadElementFromResource(
      WritableRegistry<E> $$0,
      Decoder<E> $$1,
      net.minecraft.resources.RegistryOps<JsonElement> $$2,
      net.minecraft.resources.ResourceKey<E> $$3,
      Resource $$4,
      RegistrationInfo $$5
   ) throws IOException {
      try (Reader $$6 = $$4.openAsReader()) {
         JsonElement $$7 = StrictJsonParser.parse($$6);
         DataResult<E> $$8 = $$1.parse($$2, $$7);
         E $$9 = (E)$$8.getOrThrow();
         $$0.register($$3, $$9, $$5);
      }
   }

   static <E> void loadContentsFromManager(
      ResourceManager $$0,
      net.minecraft.resources.RegistryOps.RegistryInfoLookup $$1,
      WritableRegistry<E> $$2,
      Decoder<E> $$3,
      Map<net.minecraft.resources.ResourceKey<?>, Exception> $$4
   ) {
      net.minecraft.resources.FileToIdConverter $$5 = net.minecraft.resources.FileToIdConverter.registry($$2.key());
      net.minecraft.resources.RegistryOps<JsonElement> $$6 = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, $$1);

      for (Entry<net.minecraft.resources.Identifier, Resource> $$7 : $$5.listMatchingResources($$0).entrySet()) {
         net.minecraft.resources.Identifier $$8 = $$7.getKey();
         net.minecraft.resources.ResourceKey<E> $$9 = net.minecraft.resources.ResourceKey.create($$2.key(), $$5.fileToId($$8));
         Resource $$10 = $$7.getValue();
         RegistrationInfo $$11 = REGISTRATION_INFO_CACHE.apply($$10.knownPackInfo());

         try {
            loadElementFromResource($$2, $$3, $$6, $$9, $$10, $$11);
         } catch (Exception var14) {
            $$4.put($$9, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", $$8, $$10.sourcePackId()), var14));
         }
      }

      TagLoader.loadTagsForRegistry($$0, $$2);
   }

   static <E> void loadContentsFromNetwork(
      Map<net.minecraft.resources.ResourceKey<? extends Registry<?>>, net.minecraft.resources.RegistryDataLoader.NetworkedRegistryData> $$0,
      ResourceProvider $$1,
      net.minecraft.resources.RegistryOps.RegistryInfoLookup $$2,
      WritableRegistry<E> $$3,
      Decoder<E> $$4,
      Map<net.minecraft.resources.ResourceKey<?>, Exception> $$5
   ) {
      net.minecraft.resources.RegistryDataLoader.NetworkedRegistryData $$6 = $$0.get($$3.key());
      if ($$6 != null) {
         net.minecraft.resources.RegistryOps<Tag> $$7 = net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, $$2);
         net.minecraft.resources.RegistryOps<JsonElement> $$8 = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, $$2);
         net.minecraft.resources.FileToIdConverter $$9 = net.minecraft.resources.FileToIdConverter.registry($$3.key());

         for (PackedRegistryEntry $$10 : $$6.elements) {
            net.minecraft.resources.ResourceKey<E> $$11 = net.minecraft.resources.ResourceKey.create($$3.key(), $$10.id());
            Optional<Tag> $$12 = $$10.data();
            if ($$12.isPresent()) {
               try {
                  DataResult<E> $$13 = $$4.parse($$7, $$12.get());
                  E $$14 = (E)$$13.getOrThrow();
                  $$3.register($$11, $$14, NETWORK_REGISTRATION_INFO);
               } catch (Exception var16) {
                  $$5.put($$11, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", $$12.get()), var16));
               }
            } else {
               net.minecraft.resources.Identifier $$16 = $$9.idToFile($$10.id());

               try {
                  Resource $$17 = $$1.getResourceOrThrow($$16);
                  loadElementFromResource($$3, $$4, $$8, $$11, $$17, NETWORK_REGISTRATION_INFO);
               } catch (Exception var17) {
                  $$5.put($$11, new IllegalStateException("Failed to parse local data", var17));
               }
            }
         }

         TagLoader.loadTagsFromNetwork($$6.tags, $$3);
      }
   }

   record Loader<T>(
      net.minecraft.resources.RegistryDataLoader.RegistryData<T> data,
      WritableRegistry<T> registry,
      Map<net.minecraft.resources.ResourceKey<?>, Exception> loadingErrors
   ) {

      public void loadFromResources(ResourceManager $$0, net.minecraft.resources.RegistryOps.RegistryInfoLookup $$1) {
         net.minecraft.resources.RegistryDataLoader.loadContentsFromManager($$0, $$1, this.registry, this.data.elementCodec, this.loadingErrors);
      }

      public void loadFromNetwork(
         Map<net.minecraft.resources.ResourceKey<? extends Registry<?>>, net.minecraft.resources.RegistryDataLoader.NetworkedRegistryData> $$0,
         ResourceProvider $$1,
         net.minecraft.resources.RegistryOps.RegistryInfoLookup $$2
      ) {
         net.minecraft.resources.RegistryDataLoader.loadContentsFromNetwork($$0, $$1, $$2, this.registry, this.data.elementCodec, this.loadingErrors);
      }
   }

   @FunctionalInterface
   interface LoadingFunction {
      void apply(net.minecraft.resources.RegistryDataLoader.Loader<?> var1, net.minecraft.resources.RegistryOps.RegistryInfoLookup var2);
   }

   public record NetworkedRegistryData(List<PackedRegistryEntry> elements, NetworkPayload tags) {
   }

   public record RegistryData<T>(net.minecraft.resources.ResourceKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {

      RegistryData(net.minecraft.resources.ResourceKey<? extends Registry<T>> $$0, Codec<T> $$1) {
         this($$0, $$1, false);
      }

      net.minecraft.resources.RegistryDataLoader.Loader<T> create(Lifecycle $$0, Map<net.minecraft.resources.ResourceKey<?>, Exception> $$1) {
         WritableRegistry<T> $$2 = new MappedRegistry(this.key, $$0);
         return new net.minecraft.resources.RegistryDataLoader.Loader<>(this, $$2, $$1);
      }

      public void runWithArguments(BiConsumer<net.minecraft.resources.ResourceKey<? extends Registry<T>>, Codec<T>> $$0) {
         $$0.accept(this.key, this.elementCodec);
      }
   }
}
