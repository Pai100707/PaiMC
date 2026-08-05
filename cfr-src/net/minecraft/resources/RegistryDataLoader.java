/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.resources;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.tags.TagLoader;
import net.minecraft.tags.TagNetworkSerialization;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.timeline.Timeline;
import org.slf4j.Logger;

public class RegistryDataLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Comparator<ResourceKey<?>> ERROR_KEY_COMPARATOR = Comparator.comparing(ResourceKey::registry).thenComparing(ResourceKey::identifier);
    private static final RegistrationInfo NETWORK_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<KnownPack>, RegistrationInfo> REGISTRATION_INFO_CACHE = Util.memoize($$02 -> {
        Lifecycle $$1 = $$02.map(KnownPack::isVanilla).map($$0 -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistrationInfo((Optional<KnownPack>)$$02, $$1);
    });
    public static final List<RegistryData<?>> WORLDGEN_REGISTRIES = List.of(new RegistryData<DimensionType>(Registries.DIMENSION_TYPE, DimensionType.DIRECT_CODEC), new RegistryData<Biome>(Registries.BIOME, Biome.DIRECT_CODEC), new RegistryData<ChatType>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC), new RegistryData(Registries.CONFIGURED_CARVER, ConfiguredWorldCarver.DIRECT_CODEC), new RegistryData(Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC), new RegistryData<PlacedFeature>(Registries.PLACED_FEATURE, PlacedFeature.DIRECT_CODEC), new RegistryData<Structure>(Registries.STRUCTURE, Structure.DIRECT_CODEC), new RegistryData<StructureSet>(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC), new RegistryData<StructureProcessorList>(Registries.PROCESSOR_LIST, StructureProcessorType.DIRECT_CODEC), new RegistryData<StructureTemplatePool>(Registries.TEMPLATE_POOL, StructureTemplatePool.DIRECT_CODEC), new RegistryData<NoiseGeneratorSettings>(Registries.NOISE_SETTINGS, NoiseGeneratorSettings.DIRECT_CODEC), new RegistryData<NormalNoise.NoiseParameters>(Registries.NOISE, NormalNoise.NoiseParameters.DIRECT_CODEC), new RegistryData<DensityFunction>(Registries.DENSITY_FUNCTION, DensityFunction.DIRECT_CODEC), new RegistryData<WorldPreset>(Registries.WORLD_PRESET, WorldPreset.DIRECT_CODEC), new RegistryData<FlatLevelGeneratorPreset>(Registries.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.DIRECT_CODEC), new RegistryData<TrimPattern>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryData<TrimMaterial>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryData<TrialSpawnerConfig>(Registries.TRIAL_SPAWNER_CONFIG, TrialSpawnerConfig.DIRECT_CODEC), new RegistryData<WolfVariant>(Registries.WOLF_VARIANT, WolfVariant.DIRECT_CODEC, true), new RegistryData<WolfSoundVariant>(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.DIRECT_CODEC, true), new RegistryData<PigVariant>(Registries.PIG_VARIANT, PigVariant.DIRECT_CODEC, true), new RegistryData<FrogVariant>(Registries.FROG_VARIANT, FrogVariant.DIRECT_CODEC, true), new RegistryData<CatVariant>(Registries.CAT_VARIANT, CatVariant.DIRECT_CODEC, true), new RegistryData<CowVariant>(Registries.COW_VARIANT, CowVariant.DIRECT_CODEC, true), new RegistryData<ChickenVariant>(Registries.CHICKEN_VARIANT, ChickenVariant.DIRECT_CODEC, true), new RegistryData<ZombieNautilusVariant>(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.DIRECT_CODEC, true), new RegistryData<PaintingVariant>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true), new RegistryData<DamageType>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryData<MultiNoiseBiomeSourceParameterList>(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.DIRECT_CODEC), new RegistryData<BannerPattern>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC), new RegistryData<Enchantment>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC), new RegistryData<EnchantmentProvider>(Registries.ENCHANTMENT_PROVIDER, EnchantmentProvider.DIRECT_CODEC), new RegistryData<JukeboxSong>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC), new RegistryData<Instrument>(Registries.INSTRUMENT, Instrument.DIRECT_CODEC), new RegistryData<TestEnvironmentDefinition>(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC), new RegistryData<GameTestInstance>(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC), new RegistryData<Dialog>(Registries.DIALOG, Dialog.DIRECT_CODEC), new RegistryData<Timeline>(Registries.TIMELINE, Timeline.DIRECT_CODEC));
    public static final List<RegistryData<?>> DIMENSION_REGISTRIES = List.of(new RegistryData<LevelStem>(Registries.LEVEL_STEM, LevelStem.CODEC));
    public static final List<RegistryData<?>> SYNCHRONIZED_REGISTRIES = List.of(new RegistryData<Biome>(Registries.BIOME, Biome.NETWORK_CODEC), new RegistryData<ChatType>(Registries.CHAT_TYPE, ChatType.DIRECT_CODEC), new RegistryData<TrimPattern>(Registries.TRIM_PATTERN, TrimPattern.DIRECT_CODEC), new RegistryData<TrimMaterial>(Registries.TRIM_MATERIAL, TrimMaterial.DIRECT_CODEC), new RegistryData<WolfVariant>(Registries.WOLF_VARIANT, WolfVariant.NETWORK_CODEC, true), new RegistryData<WolfSoundVariant>(Registries.WOLF_SOUND_VARIANT, WolfSoundVariant.NETWORK_CODEC, true), new RegistryData<PigVariant>(Registries.PIG_VARIANT, PigVariant.NETWORK_CODEC, true), new RegistryData<FrogVariant>(Registries.FROG_VARIANT, FrogVariant.NETWORK_CODEC, true), new RegistryData<CatVariant>(Registries.CAT_VARIANT, CatVariant.NETWORK_CODEC, true), new RegistryData<CowVariant>(Registries.COW_VARIANT, CowVariant.NETWORK_CODEC, true), new RegistryData<ChickenVariant>(Registries.CHICKEN_VARIANT, ChickenVariant.NETWORK_CODEC, true), new RegistryData<ZombieNautilusVariant>(Registries.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.NETWORK_CODEC, true), new RegistryData<PaintingVariant>(Registries.PAINTING_VARIANT, PaintingVariant.DIRECT_CODEC, true), new RegistryData<DimensionType>(Registries.DIMENSION_TYPE, DimensionType.NETWORK_CODEC), new RegistryData<DamageType>(Registries.DAMAGE_TYPE, DamageType.DIRECT_CODEC), new RegistryData<BannerPattern>(Registries.BANNER_PATTERN, BannerPattern.DIRECT_CODEC), new RegistryData<Enchantment>(Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC), new RegistryData<JukeboxSong>(Registries.JUKEBOX_SONG, JukeboxSong.DIRECT_CODEC), new RegistryData<Instrument>(Registries.INSTRUMENT, Instrument.DIRECT_CODEC), new RegistryData<TestEnvironmentDefinition>(Registries.TEST_ENVIRONMENT, TestEnvironmentDefinition.DIRECT_CODEC), new RegistryData<GameTestInstance>(Registries.TEST_INSTANCE, GameTestInstance.DIRECT_CODEC), new RegistryData<Dialog>(Registries.DIALOG, Dialog.DIRECT_CODEC), new RegistryData<Timeline>(Registries.TIMELINE, Timeline.NETWORK_CODEC));

    public static RegistryAccess.Frozen load(ResourceManager $$0, List<HolderLookup.RegistryLookup<?>> $$12, List<RegistryData<?>> $$22) {
        return RegistryDataLoader.load((Loader<?> $$1, RegistryOps.RegistryInfoLookup $$2) -> $$1.loadFromResources($$0, $$2), $$12, $$22);
    }

    public static RegistryAccess.Frozen load(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> $$0, ResourceProvider $$1, List<HolderLookup.RegistryLookup<?>> $$22, List<RegistryData<?>> $$32) {
        return RegistryDataLoader.load((Loader<?> $$2, RegistryOps.RegistryInfoLookup $$3) -> $$2.loadFromNetwork($$0, $$1, $$3), $$22, $$32);
    }

    private static RegistryAccess.Frozen load(LoadingFunction $$0, List<HolderLookup.RegistryLookup<?>> $$12, List<RegistryData<?>> $$22) {
        HashMap $$3 = new HashMap();
        List<Loader<?>> $$4 = $$22.stream().map($$1 -> $$1.create(Lifecycle.stable(), $$3)).collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoLookup $$5 = RegistryDataLoader.createContext($$12, $$4);
        $$4.forEach($$2 -> $$0.apply((Loader<?>)$$2, $$5));
        $$4.forEach($$1 -> {
            WritableRegistry $$2 = $$1.registry();
            try {
                $$2.freeze();
            }
            catch (Exception $$3) {
                $$3.put($$2.key(), $$3);
            }
            if ($$1.data.requiredNonEmpty && $$2.size() == 0) {
                $$3.put($$2.key(), new IllegalStateException("Registry must be non-empty: " + String.valueOf($$2.key().identifier())));
            }
        });
        if (!$$3.isEmpty()) {
            throw RegistryDataLoader.logErrors($$3);
        }
        return new RegistryAccess.ImmutableRegistryAccess($$4.stream().map(Loader::registry).toList()).freeze();
    }

    private static RegistryOps.RegistryInfoLookup createContext(List<HolderLookup.RegistryLookup<?>> $$0, List<Loader<?>> $$12) {
        final HashMap $$2 = new HashMap();
        $$0.forEach($$1 -> $$2.put($$1.key(), RegistryDataLoader.createInfoForContextRegistry($$1)));
        $$12.forEach($$1 -> $$2.put($$1.registry.key(), RegistryDataLoader.createInfoForNewRegistry($$1.registry)));
        return new RegistryOps.RegistryInfoLookup(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> $$0) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)$$2.get($$0));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForNewRegistry(WritableRegistry<T> $$0) {
        return new RegistryOps.RegistryInfo<T>($$0, $$0.createRegistrationLookup(), $$0.registryLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfoForContextRegistry(HolderLookup.RegistryLookup<T> $$0) {
        return new RegistryOps.RegistryInfo<T>($$0, $$0, $$0.registryLifecycle());
    }

    private static ReportedException logErrors(Map<ResourceKey<?>, Exception> $$0) {
        RegistryDataLoader.printFullDetailsToLog($$0);
        return RegistryDataLoader.createReportWithBriefInfo($$0);
    }

    private static void printFullDetailsToLog(Map<ResourceKey<?>, Exception> $$02) {
        StringWriter $$1 = new StringWriter();
        PrintWriter $$2 = new PrintWriter($$1);
        Map<Identifier, Map<Identifier, Exception>> $$3 = $$02.entrySet().stream().collect(Collectors.groupingBy($$0 -> ((ResourceKey)$$0.getKey()).registry(), Collectors.toMap($$0 -> ((ResourceKey)$$0.getKey()).identifier(), Map.Entry::getValue)));
        $$3.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach($$12 -> {
            $$2.printf(Locale.ROOT, "> Errors in registry %s:%n", $$12.getKey());
            ((Map)$$12.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach($$1 -> {
                $$2.printf(Locale.ROOT, ">> Errors in element %s:%n", $$1.getKey());
                ((Exception)$$1.getValue()).printStackTrace($$2);
            });
        });
        $$2.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)$$1);
    }

    private static ReportedException createReportWithBriefInfo(Map<ResourceKey<?>, Exception> $$0) {
        CrashReport $$1 = CrashReport.forThrowable(new IllegalStateException("Failed to load registries due to errors"), "Registry Loading");
        CrashReportCategory $$2 = $$1.addCategory("Loading info");
        $$2.setDetail("Errors", () -> {
            StringBuilder $$12 = new StringBuilder();
            $$0.entrySet().stream().sorted(Map.Entry.comparingByKey(ERROR_KEY_COMPARATOR)).forEach($$1 -> $$12.append("\n\t\t").append(((ResourceKey)$$1.getKey()).registry()).append("/").append(((ResourceKey)$$1.getKey()).identifier()).append(": ").append(((Exception)$$1.getValue()).getMessage()));
            return $$12.toString();
        });
        return new ReportedException($$1);
    }

    private static <E> void loadElementFromResource(WritableRegistry<E> $$0, Decoder<E> $$1, RegistryOps<JsonElement> $$2, ResourceKey<E> $$3, Resource $$4, RegistrationInfo $$5) throws IOException {
        try (BufferedReader $$6 = $$4.openAsReader();){
            JsonElement $$7 = StrictJsonParser.parse($$6);
            DataResult $$8 = $$1.parse($$2, (Object)$$7);
            Object $$9 = $$8.getOrThrow();
            $$0.register($$3, $$9, $$5);
        }
    }

    static <E> void loadContentsFromManager(ResourceManager $$0, RegistryOps.RegistryInfoLookup $$1, WritableRegistry<E> $$2, Decoder<E> $$3, Map<ResourceKey<?>, Exception> $$4) {
        FileToIdConverter $$5 = FileToIdConverter.registry($$2.key());
        RegistryOps<JsonElement> $$6 = RegistryOps.create(JsonOps.INSTANCE, $$1);
        for (Map.Entry<Identifier, Resource> $$7 : $$5.listMatchingResources($$0).entrySet()) {
            Identifier $$8 = $$7.getKey();
            ResourceKey $$9 = ResourceKey.create($$2.key(), $$5.fileToId($$8));
            Resource $$10 = $$7.getValue();
            RegistrationInfo $$11 = REGISTRATION_INFO_CACHE.apply($$10.knownPackInfo());
            try {
                RegistryDataLoader.loadElementFromResource($$2, $$3, $$6, $$9, $$10, $$11);
            }
            catch (Exception $$12) {
                $$4.put($$9, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", $$8, $$10.sourcePackId()), $$12));
            }
        }
        TagLoader.loadTagsForRegistry($$0, $$2);
    }

    static <E> void loadContentsFromNetwork(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> $$0, ResourceProvider $$1, RegistryOps.RegistryInfoLookup $$2, WritableRegistry<E> $$3, Decoder<E> $$4, Map<ResourceKey<?>, Exception> $$5) {
        NetworkedRegistryData $$6 = $$0.get($$3.key());
        if ($$6 == null) {
            return;
        }
        RegistryOps<Tag> $$7 = RegistryOps.create(NbtOps.INSTANCE, $$2);
        RegistryOps<JsonElement> $$8 = RegistryOps.create(JsonOps.INSTANCE, $$2);
        FileToIdConverter $$9 = FileToIdConverter.registry($$3.key());
        for (RegistrySynchronization.PackedRegistryEntry $$10 : $$6.elements) {
            ResourceKey $$11 = ResourceKey.create($$3.key(), $$10.id());
            Optional<Tag> $$12 = $$10.data();
            if ($$12.isPresent()) {
                try {
                    DataResult $$13 = $$4.parse($$7, (Object)$$12.get());
                    Object $$14 = $$13.getOrThrow();
                    $$3.register($$11, $$14, NETWORK_REGISTRATION_INFO);
                }
                catch (Exception $$15) {
                    $$5.put($$11, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", $$12.get()), $$15));
                }
                continue;
            }
            Identifier $$16 = $$9.idToFile($$10.id());
            try {
                Resource $$17 = $$1.getResourceOrThrow($$16);
                RegistryDataLoader.loadElementFromResource($$3, $$4, $$8, $$11, $$17, NETWORK_REGISTRATION_INFO);
            }
            catch (Exception $$18) {
                $$5.put($$11, new IllegalStateException("Failed to parse local data", $$18));
            }
        }
        TagLoader.loadTagsFromNetwork($$6.tags, $$3);
    }

    @FunctionalInterface
    static interface LoadingFunction {
        public void apply(Loader<?> var1, RegistryOps.RegistryInfoLookup var2);
    }

    public static final class NetworkedRegistryData
    extends Record {
        final List<RegistrySynchronization.PackedRegistryEntry> elements;
        final TagNetworkSerialization.NetworkPayload tags;

        public NetworkedRegistryData(List<RegistrySynchronization.PackedRegistryEntry> $$0, TagNetworkSerialization.NetworkPayload $$1) {
            this.elements = $$0;
            this.tags = $$1;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{NetworkedRegistryData.class, "elements;tags", "elements", "tags"}, this, $$0);
        }

        public List<RegistrySynchronization.PackedRegistryEntry> elements() {
            return this.elements;
        }

        public TagNetworkSerialization.NetworkPayload tags() {
            return this.tags;
        }
    }

    static final class Loader<T>
    extends Record {
        final RegistryData<T> data;
        final WritableRegistry<T> registry;
        private final Map<ResourceKey<?>, Exception> loadingErrors;

        Loader(RegistryData<T> $$0, WritableRegistry<T> $$1, Map<ResourceKey<?>, Exception> $$2) {
            this.data = $$0;
            this.registry = $$1;
            this.loadingErrors = $$2;
        }

        public void loadFromResources(ResourceManager $$0, RegistryOps.RegistryInfoLookup $$1) {
            RegistryDataLoader.loadContentsFromManager($$0, $$1, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(Map<ResourceKey<? extends Registry<?>>, NetworkedRegistryData> $$0, ResourceProvider $$1, RegistryOps.RegistryInfoLookup $$2) {
            RegistryDataLoader.loadContentsFromNetwork($$0, $$1, $$2, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this, $$0);
        }

        public RegistryData<T> data() {
            return this.data;
        }

        public WritableRegistry<T> registry() {
            return this.registry;
        }

        public Map<ResourceKey<?>, Exception> loadingErrors() {
            return this.loadingErrors;
        }
    }

    public static final class RegistryData<T>
    extends Record {
        private final ResourceKey<? extends Registry<T>> key;
        final Codec<T> elementCodec;
        final boolean requiredNonEmpty;

        RegistryData(ResourceKey<? extends Registry<T>> $$0, Codec<T> $$1) {
            this($$0, $$1, false);
        }

        public RegistryData(ResourceKey<? extends Registry<T>> $$0, Codec<T> $$1, boolean $$2) {
            this.key = $$0;
            this.elementCodec = $$1;
            this.requiredNonEmpty = $$2;
        }

        Loader<T> create(Lifecycle $$0, Map<ResourceKey<?>, Exception> $$1) {
            MappedRegistry $$2 = new MappedRegistry(this.key, $$0);
            return new Loader(this, $$2, $$1);
        }

        public void runWithArguments(BiConsumer<ResourceKey<? extends Registry<T>>, Codec<T>> $$0) {
            $$0.accept(this.key, this.elementCodec);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryData.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this, $$0);
        }

        public ResourceKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Codec<T> elementCodec() {
            return this.elementCodec;
        }

        public boolean requiredNonEmpty() {
            return this.requiredNonEmpty;
        }
    }
}

