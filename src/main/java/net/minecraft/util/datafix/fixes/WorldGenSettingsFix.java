package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class WorldGenSettingsFix extends DataFix {
   private static final String VILLAGE = "minecraft:village";
   private static final String DESERT_PYRAMID = "minecraft:desert_pyramid";
   private static final String IGLOO = "minecraft:igloo";
   private static final String JUNGLE_TEMPLE = "minecraft:jungle_pyramid";
   private static final String SWAMP_HUT = "minecraft:swamp_hut";
   private static final String PILLAGER_OUTPOST = "minecraft:pillager_outpost";
   private static final String END_CITY = "minecraft:endcity";
   private static final String WOODLAND_MANSION = "minecraft:mansion";
   private static final String OCEAN_MONUMENT = "minecraft:monument";
   private static final ImmutableMap<String, WorldGenSettingsFix.StructureFeatureConfiguration> DEFAULTS = ImmutableMap.builder()
      .put("minecraft:village", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 10387312))
      .put("minecraft:desert_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357617))
      .put("minecraft:igloo", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357618))
      .put("minecraft:jungle_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357619))
      .put("minecraft:swamp_hut", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357620))
      .put("minecraft:pillager_outpost", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 165745296))
      .put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 5, 10387313))
      .put("minecraft:endcity", new WorldGenSettingsFix.StructureFeatureConfiguration(20, 11, 10387313))
      .put("minecraft:mansion", new WorldGenSettingsFix.StructureFeatureConfiguration(80, 20, 10387319))
      .build();

   public WorldGenSettingsFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      return this.fixTypeEverywhereTyped(
         "WorldGenSettings building",
         this.getInputSchema().getType(References.WORLD_GEN_SETTINGS),
         $$0 -> $$0.update(DSL.remainderFinder(), WorldGenSettingsFix::fix)
      );
   }

   private static <T> Dynamic<T> noise(long $$0, DynamicLike<T> $$1, Dynamic<T> $$2, Dynamic<T> $$3) {
      return $$1.createMap(
         ImmutableMap.of(
            $$1.createString("type"),
            $$1.createString("minecraft:noise"),
            $$1.createString("biome_source"),
            $$3,
            $$1.createString("seed"),
            $$1.createLong($$0),
            $$1.createString("settings"),
            $$2
         )
      );
   }

   private static <T> Dynamic<T> vanillaBiomeSource(Dynamic<T> $$0, long $$1, boolean $$2, boolean $$3) {
      Builder<Dynamic<T>, Dynamic<T>> $$4 = ImmutableMap.builder()
         .put($$0.createString("type"), $$0.createString("minecraft:vanilla_layered"))
         .put($$0.createString("seed"), $$0.createLong($$1))
         .put($$0.createString("large_biomes"), $$0.createBoolean($$3));
      if ($$2) {
         $$4.put($$0.createString("legacy_biome_init_layer"), $$0.createBoolean($$2));
      }

      return $$0.createMap($$4.build());
   }

   private static <T> Dynamic<T> fix(Dynamic<T> $$0) {
      DynamicOps<T> $$1 = $$0.getOps();
      long $$2 = $$0.get("RandomSeed").asLong(0L);
      Optional<String> $$3 = $$0.get("generatorName").asString().map($$0x -> $$0x.toLowerCase(Locale.ROOT)).result();
      Optional<String> $$4 = $$0.get("legacy_custom_options")
         .asString()
         .result()
         .map(Optional::of)
         .orElseGet(() -> $$3.equals(Optional.of("customized")) ? $$0.get("generatorOptions").asString().result() : Optional.empty());
      boolean $$5 = false;
      Dynamic<T> $$6;
      if ($$3.equals(Optional.of("customized"))) {
         $$6 = defaultOverworld($$0, $$2);
      } else if ($$3.isEmpty()) {
         $$6 = defaultOverworld($$0, $$2);
      } else {
         String $$28 = $$3.get();
         switch ($$28) {
            case "flat":
               OptionalDynamic<T> $$8 = $$0.get("generatorOptions");
               Map<Dynamic<T>, Dynamic<T>> $$9 = fixFlatStructures($$1, $$8);
               $$6 = $$0.createMap(
                  ImmutableMap.of(
                     $$0.createString("type"),
                     $$0.createString("minecraft:flat"),
                     $$0.createString("settings"),
                     $$0.createMap(
                        ImmutableMap.of(
                           $$0.createString("structures"),
                           $$0.createMap($$9),
                           $$0.createString("layers"),
                           $$8.get("layers")
                              .result()
                              .orElseGet(
                                 () -> $$0.createList(
                                    Stream.of(
                                       $$0.createMap(
                                          ImmutableMap.of(
                                             $$0.createString("height"), $$0.createInt(1), $$0.createString("block"), $$0.createString("minecraft:bedrock")
                                          )
                                       ),
                                       $$0.createMap(
                                          ImmutableMap.of(
                                             $$0.createString("height"), $$0.createInt(2), $$0.createString("block"), $$0.createString("minecraft:dirt")
                                          )
                                       ),
                                       $$0.createMap(
                                          ImmutableMap.of(
                                             $$0.createString("height"), $$0.createInt(1), $$0.createString("block"), $$0.createString("minecraft:grass_block")
                                          )
                                       )
                                    )
                                 )
                              ),
                           $$0.createString("biome"),
                           $$0.createString($$8.get("biome").asString("minecraft:plains"))
                        )
                     )
                  )
               );
               break;
            case "debug_all_block_states":
               $$6 = $$0.createMap(ImmutableMap.of($$0.createString("type"), $$0.createString("minecraft:debug")));
               break;
            case "buffet":
               OptionalDynamic<T> $$12 = $$0.get("generatorOptions");
               OptionalDynamic<?> $$13 = $$12.get("chunk_generator");
               Optional<String> $$14 = $$13.get("type").asString().result();
               Dynamic<T> $$15;
               if (Objects.equals($$14, Optional.of("minecraft:caves"))) {
                  $$15 = $$0.createString("minecraft:caves");
                  $$5 = true;
               } else if (Objects.equals($$14, Optional.of("minecraft:floating_islands"))) {
                  $$15 = $$0.createString("minecraft:floating_islands");
               } else {
                  $$15 = $$0.createString("minecraft:overworld");
               }

               Dynamic<T> $$18 = $$12.get("biome_source")
                  .result()
                  .orElseGet(() -> $$0.createMap(ImmutableMap.of($$0.createString("type"), $$0.createString("minecraft:fixed"))));
               Dynamic<T> $$20;
               if ($$18.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
                  String $$19 = $$18.get("options").get("biomes").asStream().findFirst().flatMap($$0x -> $$0x.asString().result()).orElse("minecraft:ocean");
                  $$20 = $$18.remove("options").set("biome", $$0.createString($$19));
               } else {
                  $$20 = $$18;
               }

               $$6 = noise($$2, $$0, $$15, $$20);
               break;
            default:
               boolean $$23 = $$3.get().equals("default");
               boolean $$24 = $$3.get().equals("default_1_1") || $$23 && $$0.get("generatorVersion").asInt(0) == 0;
               boolean $$25 = $$3.get().equals("amplified");
               boolean $$26 = $$3.get().equals("largebiomes");
               $$6 = noise($$2, $$0, $$0.createString($$25 ? "minecraft:amplified" : "minecraft:overworld"), vanillaBiomeSource($$0, $$2, $$24, $$26));
         }
      }

      boolean $$28 = $$0.get("MapFeatures").asBoolean(true);
      boolean $$29 = $$0.get("BonusChest").asBoolean(false);
      Builder<T, T> $$30 = ImmutableMap.builder();
      $$30.put($$1.createString("seed"), $$1.createLong($$2));
      $$30.put($$1.createString("generate_features"), $$1.createBoolean($$28));
      $$30.put($$1.createString("bonus_chest"), $$1.createBoolean($$29));
      $$30.put($$1.createString("dimensions"), vanillaLevels($$0, $$2, $$6, $$5));
      $$4.ifPresent($$2x -> $$30.put($$1.createString("legacy_custom_options"), $$1.createString($$2x)));
      return new Dynamic($$1, $$1.createMap($$30.build()));
   }

   protected static <T> Dynamic<T> defaultOverworld(Dynamic<T> $$0, long $$1) {
      return noise($$1, $$0, $$0.createString("minecraft:overworld"), vanillaBiomeSource($$0, $$1, false, false));
   }

   protected static <T> T vanillaLevels(Dynamic<T> $$0, long $$1, Dynamic<T> $$2, boolean $$3) {
      DynamicOps<T> $$4 = $$0.getOps();
      return (T)$$4.createMap(
         ImmutableMap.of(
            $$4.createString("minecraft:overworld"),
            $$4.createMap(
               ImmutableMap.of(
                  $$4.createString("type"), $$4.createString("minecraft:overworld" + ($$3 ? "_caves" : "")), $$4.createString("generator"), $$2.getValue()
               )
            ),
            $$4.createString("minecraft:the_nether"),
            $$4.createMap(
               ImmutableMap.of(
                  $$4.createString("type"),
                  $$4.createString("minecraft:the_nether"),
                  $$4.createString("generator"),
                  noise(
                        $$1,
                        $$0,
                        $$0.createString("minecraft:nether"),
                        $$0.createMap(
                           ImmutableMap.of(
                              $$0.createString("type"),
                              $$0.createString("minecraft:multi_noise"),
                              $$0.createString("seed"),
                              $$0.createLong($$1),
                              $$0.createString("preset"),
                              $$0.createString("minecraft:nether")
                           )
                        )
                     )
                     .getValue()
               )
            ),
            $$4.createString("minecraft:the_end"),
            $$4.createMap(
               ImmutableMap.of(
                  $$4.createString("type"),
                  $$4.createString("minecraft:the_end"),
                  $$4.createString("generator"),
                  noise(
                        $$1,
                        $$0,
                        $$0.createString("minecraft:end"),
                        $$0.createMap(
                           ImmutableMap.of($$0.createString("type"), $$0.createString("minecraft:the_end"), $$0.createString("seed"), $$0.createLong($$1))
                        )
                     )
                     .getValue()
               )
            )
         )
      );
   }

   private static <T> Map<Dynamic<T>, Dynamic<T>> fixFlatStructures(DynamicOps<T> $$0, OptionalDynamic<T> $$1) {
      MutableInt $$2 = new MutableInt(32);
      MutableInt $$3 = new MutableInt(3);
      MutableInt $$4 = new MutableInt(128);
      MutableBoolean $$5 = new MutableBoolean(false);
      Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> $$6 = Maps.newHashMap();
      if ($$1.result().isEmpty()) {
         $$5.setTrue();
         $$6.put("minecraft:village", (WorldGenSettingsFix.StructureFeatureConfiguration)DEFAULTS.get("minecraft:village"));
      }

      $$1.get("structures")
         .flatMap(Dynamic::getMapValues)
         .ifSuccess(
            $$5x -> $$5x.forEach(
               ($$5xx, $$6x) -> $$6x.getMapValues()
                  .result()
                  .ifPresent(
                     $$6xx -> $$6xx.forEach(
                        ($$6xxx, $$7x) -> {
                           String $$8 = $$5xx.asString("");
                           String $$9 = $$6xxx.asString("");
                           String $$10 = $$7x.asString("");
                           if ("stronghold".equals($$8)) {
                              $$5.setTrue();
                              switch ($$9) {
                                 case "distance":
                                    $$2.setValue(getInt($$10, $$2.intValue(), 1));
                                    return;
                                 case "spread":
                                    $$3.setValue(getInt($$10, $$3.intValue(), 1));
                                    return;
                                 case "count":
                                    $$4.setValue(getInt($$10, $$4.intValue(), 1));
                                    return;
                              }
                           } else {
                              switch ($$9) {
                                 case "distance":
                                    switch ($$8) {
                                       case "village":
                                          setSpacing($$6, "minecraft:village", $$10, 9);
                                          return;
                                       case "biome_1":
                                          setSpacing($$6, "minecraft:desert_pyramid", $$10, 9);
                                          setSpacing($$6, "minecraft:igloo", $$10, 9);
                                          setSpacing($$6, "minecraft:jungle_pyramid", $$10, 9);
                                          setSpacing($$6, "minecraft:swamp_hut", $$10, 9);
                                          setSpacing($$6, "minecraft:pillager_outpost", $$10, 9);
                                          return;
                                       case "endcity":
                                          setSpacing($$6, "minecraft:endcity", $$10, 1);
                                          return;
                                       case "mansion":
                                          setSpacing($$6, "minecraft:mansion", $$10, 1);
                                          return;
                                       default:
                                          return;
                                    }
                                 case "separation":
                                    if ("oceanmonument".equals($$8)) {
                                       WorldGenSettingsFix.StructureFeatureConfiguration $$11 = $$6.getOrDefault(
                                          "minecraft:monument", (WorldGenSettingsFix.StructureFeatureConfiguration)DEFAULTS.get("minecraft:monument")
                                       );
                                       int $$12 = getInt($$10, $$11.separation, 1);
                                       $$6.put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration($$12, $$11.separation, $$11.salt));
                                    }

                                    return;
                                 case "spacing":
                                    if ("oceanmonument".equals($$8)) {
                                       setSpacing($$6, "minecraft:monument", $$10, 1);
                                    }

                                    return;
                              }
                           }
                        }
                     )
                  )
            )
         );
      Builder<Dynamic<T>, Dynamic<T>> $$7 = ImmutableMap.builder();
      $$7.put(
         $$1.createString("structures"),
         $$1.createMap(
            $$6.entrySet()
               .stream()
               .collect(
                  Collectors.toMap(
                     $$1x -> $$1.createString((String)$$1x.getKey()),
                     $$1x -> ((WorldGenSettingsFix.StructureFeatureConfiguration)$$1x.getValue()).serialize($$0)
                  )
               )
         )
      );
      if ($$5.isTrue()) {
         $$7.put(
            $$1.createString("stronghold"),
            $$1.createMap(
               ImmutableMap.of(
                  $$1.createString("distance"),
                  $$1.createInt($$2.intValue()),
                  $$1.createString("spread"),
                  $$1.createInt($$3.intValue()),
                  $$1.createString("count"),
                  $$1.createInt($$4.intValue())
               )
            )
         );
      }

      return $$7.build();
   }

   private static int getInt(String $$0, int $$1) {
      return NumberUtils.toInt($$0, $$1);
   }

   private static int getInt(String $$0, int $$1, int $$2) {
      return Math.max($$2, getInt($$0, $$1));
   }

   private static void setSpacing(Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> $$0, String $$1, String $$2, int $$3) {
      WorldGenSettingsFix.StructureFeatureConfiguration $$4 = $$0.getOrDefault($$1, (WorldGenSettingsFix.StructureFeatureConfiguration)DEFAULTS.get($$1));
      int $$5 = getInt($$2, $$4.spacing, $$3);
      $$0.put($$1, new WorldGenSettingsFix.StructureFeatureConfiguration($$5, $$4.separation, $$4.salt));
   }

   static final class StructureFeatureConfiguration {
      public static final Codec<WorldGenSettingsFix.StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               Codec.INT.fieldOf("spacing").forGetter($$0x -> $$0x.spacing),
               Codec.INT.fieldOf("separation").forGetter($$0x -> $$0x.separation),
               Codec.INT.fieldOf("salt").forGetter($$0x -> $$0x.salt)
            )
            .apply($$0, WorldGenSettingsFix.StructureFeatureConfiguration::new)
      );
      final int spacing;
      final int separation;
      final int salt;

      public StructureFeatureConfiguration(int $$0, int $$1, int $$2) {
         this.spacing = $$0;
         this.separation = $$1;
         this.salt = $$2;
      }

      public <T> Dynamic<T> serialize(DynamicOps<T> $$0) {
         return new Dynamic($$0, CODEC.encodeStart($$0, this).result().orElse($$0.emptyMap()));
      }
   }
}
