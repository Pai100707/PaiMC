package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

public class ChunkHeightAndBiomeFix extends DataFix {
   public static final String DATAFIXER_CONTEXT_TAG = "__context";
   private static final String NAME = "ChunkHeightAndBiomeFix";
   private static final int OLD_SECTION_COUNT = 16;
   private static final int NEW_SECTION_COUNT = 24;
   private static final int NEW_MIN_SECTION_Y = -4;
   public static final int BLOCKS_PER_SECTION = 4096;
   private static final int LONGS_PER_SECTION = 64;
   private static final int HEIGHTMAP_BITS = 9;
   private static final long HEIGHTMAP_MASK = 511L;
   private static final int HEIGHTMAP_OFFSET = 64;
   private static final String[] HEIGHTMAP_TYPES = new String[]{
      "WORLD_SURFACE_WG", "WORLD_SURFACE", "WORLD_SURFACE_IGNORE_SNOW", "OCEAN_FLOOR_WG", "OCEAN_FLOOR", "MOTION_BLOCKING", "MOTION_BLOCKING_NO_LEAVES"
   };
   private static final Set<String> STATUS_IS_OR_AFTER_SURFACE = Set.of(
      "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full"
   );
   private static final Set<String> STATUS_IS_OR_AFTER_NOISE = Set.of(
      "noise", "surface", "carvers", "liquid_carvers", "features", "light", "spawn", "heightmaps", "full"
   );
   private static final Set<String> BLOCKS_BEFORE_FEATURE_STATUS = Set.of(
      "minecraft:air",
      "minecraft:basalt",
      "minecraft:bedrock",
      "minecraft:blackstone",
      "minecraft:calcite",
      "minecraft:cave_air",
      "minecraft:coarse_dirt",
      "minecraft:crimson_nylium",
      "minecraft:dirt",
      "minecraft:end_stone",
      "minecraft:grass_block",
      "minecraft:gravel",
      "minecraft:ice",
      "minecraft:lava",
      "minecraft:mycelium",
      "minecraft:nether_wart_block",
      "minecraft:netherrack",
      "minecraft:orange_terracotta",
      "minecraft:packed_ice",
      "minecraft:podzol",
      "minecraft:powder_snow",
      "minecraft:red_sand",
      "minecraft:red_sandstone",
      "minecraft:sand",
      "minecraft:sandstone",
      "minecraft:snow_block",
      "minecraft:soul_sand",
      "minecraft:soul_soil",
      "minecraft:stone",
      "minecraft:terracotta",
      "minecraft:warped_nylium",
      "minecraft:warped_wart_block",
      "minecraft:water",
      "minecraft:white_terracotta"
   );
   private static final int BIOME_CONTAINER_LAYER_SIZE = 16;
   private static final int BIOME_CONTAINER_SIZE = 64;
   private static final int BIOME_CONTAINER_TOP_LAYER_OFFSET = 1008;
   public static final String DEFAULT_BIOME = "minecraft:plains";
   private static final Int2ObjectMap<String> BIOMES_BY_ID = new Int2ObjectOpenHashMap();

   public ChunkHeightAndBiomeFix(Schema $$0) {
      super($$0, true);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      OpticFinder<?> $$1 = $$0.findField("Level");
      OpticFinder<?> $$2 = $$1.type().findField("Sections");
      Schema $$3 = this.getOutputSchema();
      Type<?> $$4 = $$3.getType(References.CHUNK);
      Type<?> $$5 = $$4.findField("Level").type();
      Type<?> $$6 = $$5.findField("Sections").type();
      return this.fixTypeEverywhereTyped(
         "ChunkHeightAndBiomeFix",
         $$0,
         $$4,
         $$4x -> $$4x.updateTyped(
            $$1,
            $$5,
            $$3xx -> {
               Dynamic<?> $$4xx = (Dynamic<?>)$$3xx.get(DSL.remainderFinder());
               OptionalDynamic<?> $$5x = ((Dynamic)$$4x.get(DSL.remainderFinder())).get("__context");
               String $$6x = $$5x.get("dimension").asString().result().orElse("");
               String $$7 = $$5x.get("generator").asString().result().orElse("");
               boolean $$8 = "minecraft:overworld".equals($$6x);
               MutableBoolean $$9 = new MutableBoolean();
               int $$10 = $$8 ? -4 : 0;
               Dynamic<?>[] $$11 = getBiomeContainers($$4xx, $$8, $$10, $$9);
               Dynamic<?> $$12 = makePalettedContainer(
                  $$4xx.createList(Stream.of($$4xx.createMap(ImmutableMap.of($$4xx.createString("Name"), $$4xx.createString("minecraft:air")))))
               );
               Set<String> $$13 = Sets.newHashSet();
               MutableObject<Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>> $$14 = new MutableObject(
                  (Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>)() -> null
               );
               $$3xx = $$3xx.updateTyped($$2, $$6, $$7x -> {
                  IntSet $$8x = new IntOpenHashSet();
                  Dynamic<?> $$9x = (Dynamic<?>)$$7x.write().result().orElseThrow(() -> new IllegalStateException("Malformed Chunk.Level.Sections"));
                  List<Dynamic<?>> $$10x = $$9x.asStream().map($$6xxx -> {
                     int $$7xx = $$6xxx.get("Y").asInt(0);
                     Dynamic<?> $$8xx = (Dynamic<?>)DataFixUtils.orElse($$6xxx.get("Palette").result().flatMap($$2xxxxx -> {
                        $$2xxxxx.asStream().map($$0xxxxxx -> $$0xxxxxx.get("Name").asString("minecraft:air")).forEach($$13::add);
                        return $$6xxx.get("BlockStates").result().map($$1xxxxxx -> makeOptimizedPalettedContainer($$2xxxxx, $$1xxxxxx));
                     }), $$12);
                     Dynamic<?> $$9xx = $$6xxx;
                     int $$10xx = $$7xx - $$10;
                     if ($$10xx >= 0 && $$10xx < $$11.length) {
                        $$9xx = $$6xxx.set("biomes", $$11[$$10xx]);
                     }

                     $$8x.add($$7xx);
                     if ($$6xxx.get("Y").asInt(Integer.MAX_VALUE) == 0) {
                        $$14.setValue((Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>)() -> {
                           List<? extends Dynamic<?>> $$1xxxxx = $$8xx.get("palette").asList(Function.identity());
                           long[] $$2xxxxx = $$8xx.get("data").asLongStream().toArray();
                           return new ChunkProtoTickListFix.PoorMansPalettedContainer($$1xxxxx, $$2xxxxx);
                        });
                     }

                     return $$9xx.set("block_states", $$8xx).remove("Palette").remove("BlockStates");
                  }).collect(Collectors.toCollection(ArrayList::new));

                  for (int $$11x = 0; $$11x < $$11.length; $$11x++) {
                     int $$12x = $$11x + $$10;
                     if ($$8x.add($$12x)) {
                        Dynamic<?> $$13x = $$4xx.createMap(Map.of($$4xx.createString("Y"), $$4xx.createInt($$12x)));
                        $$13x = $$13x.set("block_states", $$12);
                        $$13x = $$13x.set("biomes", $$11[$$11x]);
                        $$10x.add($$13x);
                     }
                  }

                  return net.minecraft.util.Util.readTypedOrThrow($$6, $$4xx.createList($$10x.stream()));
               });
               return $$3xx.update(
                  DSL.remainderFinder(),
                  $$5xx -> {
                     if ($$8) {
                        $$5xx = this.predictChunkStatusBeforeSurface($$5xx, $$13);
                     }

                     return updateChunkTag(
                        $$5xx, $$8, $$9.booleanValue(), "minecraft:noise".equals($$7), (Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer>)$$14.get()
                     );
                  }
               );
            }
         )
      );
   }

   private Dynamic<?> predictChunkStatusBeforeSurface(Dynamic<?> $$0, Set<String> $$1) {
      return $$0.update("Status", $$1x -> {
         String $$2 = $$1x.asString("empty");
         if (STATUS_IS_OR_AFTER_SURFACE.contains($$2)) {
            return $$1x;
         } else {
            $$1.remove("minecraft:air");
            boolean $$3 = !$$1.isEmpty();
            $$1.removeAll(BLOCKS_BEFORE_FEATURE_STATUS);
            boolean $$4 = !$$1.isEmpty();
            if ($$4) {
               return $$1x.createString("liquid_carvers");
            } else if ("noise".equals($$2) || $$3) {
               return $$1x.createString("noise");
            } else {
               return "biomes".equals($$2) ? $$1x.createString("structure_references") : $$1x;
            }
         }
      });
   }

   private static Dynamic<?>[] getBiomeContainers(Dynamic<?> $$0, boolean $$1, int $$2, MutableBoolean $$3) {
      Dynamic<?>[] $$4 = new Dynamic[$$1 ? 24 : 16];
      int[] $$5 = $$0.get("Biomes").asIntStreamOpt().result().map(IntStream::toArray).orElse(null);
      if ($$5 != null && $$5.length == 1536) {
         $$3.setValue(true);

         for (int $$6 = 0; $$6 < 24; $$6++) {
            int $$7 = $$6;
            $$4[$$6] = makeBiomeContainer($$0, $$2x -> getOldBiome($$5, $$7 * 64 + $$2x));
         }
      } else if ($$5 != null && $$5.length == 1024) {
         for (int $$8 = 0; $$8 < 16; $$8++) {
            int $$9 = $$8 - $$2;
            $$4[$$9] = makeBiomeContainer($$0, $$2x -> getOldBiome($$5, $$8 * 64 + $$2x));
         }

         if ($$1) {
            Dynamic<?> $$11 = makeBiomeContainer($$0, $$1x -> getOldBiome($$5, $$1x % 16));
            Dynamic<?> $$12 = makeBiomeContainer($$0, $$1x -> getOldBiome($$5, $$1x % 16 + 1008));

            for (int $$13 = 0; $$13 < 4; $$13++) {
               $$4[$$13] = $$11;
            }

            for (int $$14 = 20; $$14 < 24; $$14++) {
               $$4[$$14] = $$12;
            }
         }
      } else {
         Arrays.fill($$4, makePalettedContainer($$0.createList(Stream.of($$0.createString("minecraft:plains")))));
      }

      return $$4;
   }

   private static int getOldBiome(int[] $$0, int $$1) {
      return $$0[$$1] & 0xFF;
   }

   private static Dynamic<?> updateChunkTag(
      Dynamic<?> $$0, boolean $$1, boolean $$2, boolean $$3, Supplier<ChunkProtoTickListFix.PoorMansPalettedContainer> $$4
   ) {
      $$0 = $$0.remove("Biomes");
      if (!$$1) {
         return updateCarvingMasks($$0, 16, 0);
      } else if ($$2) {
         return updateCarvingMasks($$0, 24, 0);
      } else {
         $$0 = updateHeightmaps($$0);
         $$0 = addPaddingEntries($$0, "LiquidsToBeTicked");
         $$0 = addPaddingEntries($$0, "PostProcessing");
         $$0 = addPaddingEntries($$0, "ToBeTicked");
         $$0 = updateCarvingMasks($$0, 24, 4);
         $$0 = $$0.update("UpgradeData", ChunkHeightAndBiomeFix::shiftUpgradeData);
         if (!$$3) {
            return $$0;
         } else {
            Optional<? extends Dynamic<?>> $$5 = $$0.get("Status").result();
            if ($$5.isPresent()) {
               Dynamic<?> $$6 = (Dynamic<?>)$$5.get();
               String $$7 = $$6.asString("");
               if (!"empty".equals($$7)) {
                  $$0 = $$0.set(
                     "blending_data", $$0.createMap(ImmutableMap.of($$0.createString("old_noise"), $$0.createBoolean(STATUS_IS_OR_AFTER_NOISE.contains($$7))))
                  );
                  if (!SharedConstants.DEBUG_DISABLE_BELOW_ZERO_RETROGENERATION) {
                     ChunkProtoTickListFix.PoorMansPalettedContainer $$8 = $$4.get();
                     if ($$8 != null) {
                        BitSet $$9 = new BitSet(256);
                        boolean $$10 = $$7.equals("noise");

                        for (int $$11 = 0; $$11 < 16; $$11++) {
                           for (int $$12 = 0; $$12 < 16; $$12++) {
                              Dynamic<?> $$13 = $$8.get($$12, 0, $$11);
                              boolean $$14 = $$13 != null && "minecraft:bedrock".equals($$13.get("Name").asString(""));
                              boolean $$15 = $$13 != null && "minecraft:air".equals($$13.get("Name").asString(""));
                              if ($$15) {
                                 $$9.set($$11 * 16 + $$12);
                              }

                              $$10 |= $$14;
                           }
                        }

                        if ($$10 && $$9.cardinality() != $$9.size()) {
                           Dynamic<?> $$16 = "full".equals($$7) ? $$0.createString("heightmaps") : $$6;
                           $$0 = $$0.set(
                              "below_zero_retrogen",
                              $$0.createMap(
                                 ImmutableMap.of(
                                    $$0.createString("target_status"),
                                    $$16,
                                    $$0.createString("missing_bedrock"),
                                    $$0.createLongList(LongStream.of($$9.toLongArray()))
                                 )
                              )
                           );
                           $$0 = $$0.set("Status", $$0.createString("empty"));
                        }

                        $$0 = $$0.set("isLightOn", $$0.createBoolean(false));
                     }
                  }
               }
            }

            return $$0;
         }
      }
   }

   private static <T> Dynamic<T> shiftUpgradeData(Dynamic<T> $$0) {
      return $$0.update("Indices", $$0x -> {
         Map<Dynamic<?>, Dynamic<?>> $$1 = new HashMap<>();
         $$0x.getMapValues().ifSuccess($$1x -> $$1x.forEach(($$1xx, $$2) -> {
            try {
               $$1xx.asString().result().map(Integer::parseInt).ifPresent($$3 -> {
                  int $$4 = $$3 - -4;
                  $$1.put($$1xx.createString(Integer.toString($$4)), (Dynamic<?>)$$2);
               });
            } catch (NumberFormatException var4) {
            }
         }));
         return $$0x.createMap($$1);
      });
   }

   private static Dynamic<?> updateCarvingMasks(Dynamic<?> $$0, int $$1, int $$2) {
      Dynamic<?> $$3 = $$0.get("CarvingMasks").orElseEmptyMap();
      $$3 = $$3.updateMapValues($$3x -> {
         long[] $$4 = BitSet.valueOf(((Dynamic)$$3x.getSecond()).asByteBuffer().array()).toLongArray();
         long[] $$5 = new long[64 * $$1];
         System.arraycopy($$4, 0, $$5, 64 * $$2, $$4.length);
         return Pair.of((Dynamic)$$3x.getFirst(), $$0.createLongList(LongStream.of($$5)));
      });
      return $$0.set("CarvingMasks", $$3);
   }

   private static Dynamic<?> addPaddingEntries(Dynamic<?> $$0, String $$1) {
      List<Dynamic<?>> $$2 = $$0.get($$1).orElseEmptyList().asStream().collect(Collectors.toCollection(ArrayList::new));
      if ($$2.size() == 24) {
         return $$0;
      } else {
         Dynamic<?> $$3 = $$0.emptyList();

         for (int $$4 = 0; $$4 < 4; $$4++) {
            $$2.add(0, $$3);
            $$2.add($$3);
         }

         return $$0.set($$1, $$0.createList($$2.stream()));
      }
   }

   private static Dynamic<?> updateHeightmaps(Dynamic<?> $$0) {
      return $$0.update("Heightmaps", $$0x -> {
         for (String $$1 : HEIGHTMAP_TYPES) {
            $$0x = $$0x.update($$1, ChunkHeightAndBiomeFix::getFixedHeightmap);
         }

         return $$0x;
      });
   }

   private static Dynamic<?> getFixedHeightmap(Dynamic<?> $$0) {
      return $$0.createLongList($$0.asLongStream().map($$0x -> {
         long $$1 = 0L;

         for (int $$2 = 0; $$2 + 9 <= 64; $$2 += 9) {
            long $$3 = $$0x >> $$2 & 511L;
            long $$4;
            if ($$3 == 0L) {
               $$4 = 0L;
            } else {
               $$4 = Math.min($$3 + 64L, 511L);
            }

            $$1 |= $$4 << $$2;
         }

         return $$1;
      }));
   }

   private static Dynamic<?> makeBiomeContainer(Dynamic<?> $$0, Int2IntFunction $$1) {
      Int2IntMap $$2 = new Int2IntLinkedOpenHashMap();

      for (int $$3 = 0; $$3 < 64; $$3++) {
         int $$4 = $$1.applyAsInt($$3);
         if (!$$2.containsKey($$4)) {
            $$2.put($$4, $$2.size());
         }
      }

      Dynamic<?> $$5 = $$0.createList($$2.keySet().stream().map($$1x -> $$0.createString((String)BIOMES_BY_ID.getOrDefault($$1x, "minecraft:plains"))));
      int $$6 = ceillog2($$2.size());
      if ($$6 == 0) {
         return makePalettedContainer($$5);
      } else {
         int $$7 = 64 / $$6;
         int $$8 = (64 + $$7 - 1) / $$7;
         long[] $$9 = new long[$$8];
         int $$10 = 0;
         int $$11 = 0;

         for (int $$12 = 0; $$12 < 64; $$12++) {
            int $$13 = $$1.applyAsInt($$12);
            $$9[$$10] |= (long)$$2.get($$13) << $$11;
            $$11 += $$6;
            if ($$11 + $$6 > 64) {
               $$10++;
               $$11 = 0;
            }
         }

         Dynamic<?> $$14 = $$0.createLongList(Arrays.stream($$9));
         return makePalettedContainer($$5, $$14);
      }
   }

   private static Dynamic<?> makePalettedContainer(Dynamic<?> $$0) {
      return $$0.createMap(ImmutableMap.of($$0.createString("palette"), $$0));
   }

   private static Dynamic<?> makePalettedContainer(Dynamic<?> $$0, Dynamic<?> $$1) {
      return $$0.createMap(ImmutableMap.of($$0.createString("palette"), $$0, $$0.createString("data"), $$1));
   }

   private static Dynamic<?> makeOptimizedPalettedContainer(Dynamic<?> $$0, Dynamic<?> $$1) {
      List<Dynamic<?>> $$2 = $$0.asStream().collect(Collectors.toCollection(ArrayList::new));
      if ($$2.size() == 1) {
         return makePalettedContainer($$0);
      } else {
         $$0 = padPaletteEntries($$0, $$1, $$2);
         return makePalettedContainer($$0, $$1);
      }
   }

   private static Dynamic<?> padPaletteEntries(Dynamic<?> $$0, Dynamic<?> $$1, List<Dynamic<?>> $$2) {
      long $$3 = $$1.asLongStream().count() * 64L;
      long $$4 = $$3 / 4096L;
      int $$5 = $$2.size();
      int $$6 = ceillog2($$5);
      if ($$4 <= $$6) {
         return $$0;
      } else {
         Dynamic<?> $$7 = $$0.createMap(ImmutableMap.of($$0.createString("Name"), $$0.createString("minecraft:air")));
         int $$8 = (1 << (int)($$4 - 1L)) + 1;
         int $$9 = $$8 - $$5;

         for (int $$10 = 0; $$10 < $$9; $$10++) {
            $$2.add($$7);
         }

         return $$0.createList($$2.stream());
      }
   }

   public static int ceillog2(int $$0) {
      return $$0 == 0 ? 0 : (int)Math.ceil(Math.log($$0) / Math.log(2.0));
   }

   static {
      BIOMES_BY_ID.put(0, "minecraft:ocean");
      BIOMES_BY_ID.put(1, "minecraft:plains");
      BIOMES_BY_ID.put(2, "minecraft:desert");
      BIOMES_BY_ID.put(3, "minecraft:mountains");
      BIOMES_BY_ID.put(4, "minecraft:forest");
      BIOMES_BY_ID.put(5, "minecraft:taiga");
      BIOMES_BY_ID.put(6, "minecraft:swamp");
      BIOMES_BY_ID.put(7, "minecraft:river");
      BIOMES_BY_ID.put(8, "minecraft:nether_wastes");
      BIOMES_BY_ID.put(9, "minecraft:the_end");
      BIOMES_BY_ID.put(10, "minecraft:frozen_ocean");
      BIOMES_BY_ID.put(11, "minecraft:frozen_river");
      BIOMES_BY_ID.put(12, "minecraft:snowy_tundra");
      BIOMES_BY_ID.put(13, "minecraft:snowy_mountains");
      BIOMES_BY_ID.put(14, "minecraft:mushroom_fields");
      BIOMES_BY_ID.put(15, "minecraft:mushroom_field_shore");
      BIOMES_BY_ID.put(16, "minecraft:beach");
      BIOMES_BY_ID.put(17, "minecraft:desert_hills");
      BIOMES_BY_ID.put(18, "minecraft:wooded_hills");
      BIOMES_BY_ID.put(19, "minecraft:taiga_hills");
      BIOMES_BY_ID.put(20, "minecraft:mountain_edge");
      BIOMES_BY_ID.put(21, "minecraft:jungle");
      BIOMES_BY_ID.put(22, "minecraft:jungle_hills");
      BIOMES_BY_ID.put(23, "minecraft:jungle_edge");
      BIOMES_BY_ID.put(24, "minecraft:deep_ocean");
      BIOMES_BY_ID.put(25, "minecraft:stone_shore");
      BIOMES_BY_ID.put(26, "minecraft:snowy_beach");
      BIOMES_BY_ID.put(27, "minecraft:birch_forest");
      BIOMES_BY_ID.put(28, "minecraft:birch_forest_hills");
      BIOMES_BY_ID.put(29, "minecraft:dark_forest");
      BIOMES_BY_ID.put(30, "minecraft:snowy_taiga");
      BIOMES_BY_ID.put(31, "minecraft:snowy_taiga_hills");
      BIOMES_BY_ID.put(32, "minecraft:giant_tree_taiga");
      BIOMES_BY_ID.put(33, "minecraft:giant_tree_taiga_hills");
      BIOMES_BY_ID.put(34, "minecraft:wooded_mountains");
      BIOMES_BY_ID.put(35, "minecraft:savanna");
      BIOMES_BY_ID.put(36, "minecraft:savanna_plateau");
      BIOMES_BY_ID.put(37, "minecraft:badlands");
      BIOMES_BY_ID.put(38, "minecraft:wooded_badlands_plateau");
      BIOMES_BY_ID.put(39, "minecraft:badlands_plateau");
      BIOMES_BY_ID.put(40, "minecraft:small_end_islands");
      BIOMES_BY_ID.put(41, "minecraft:end_midlands");
      BIOMES_BY_ID.put(42, "minecraft:end_highlands");
      BIOMES_BY_ID.put(43, "minecraft:end_barrens");
      BIOMES_BY_ID.put(44, "minecraft:warm_ocean");
      BIOMES_BY_ID.put(45, "minecraft:lukewarm_ocean");
      BIOMES_BY_ID.put(46, "minecraft:cold_ocean");
      BIOMES_BY_ID.put(47, "minecraft:deep_warm_ocean");
      BIOMES_BY_ID.put(48, "minecraft:deep_lukewarm_ocean");
      BIOMES_BY_ID.put(49, "minecraft:deep_cold_ocean");
      BIOMES_BY_ID.put(50, "minecraft:deep_frozen_ocean");
      BIOMES_BY_ID.put(127, "minecraft:the_void");
      BIOMES_BY_ID.put(129, "minecraft:sunflower_plains");
      BIOMES_BY_ID.put(130, "minecraft:desert_lakes");
      BIOMES_BY_ID.put(131, "minecraft:gravelly_mountains");
      BIOMES_BY_ID.put(132, "minecraft:flower_forest");
      BIOMES_BY_ID.put(133, "minecraft:taiga_mountains");
      BIOMES_BY_ID.put(134, "minecraft:swamp_hills");
      BIOMES_BY_ID.put(140, "minecraft:ice_spikes");
      BIOMES_BY_ID.put(149, "minecraft:modified_jungle");
      BIOMES_BY_ID.put(151, "minecraft:modified_jungle_edge");
      BIOMES_BY_ID.put(155, "minecraft:tall_birch_forest");
      BIOMES_BY_ID.put(156, "minecraft:tall_birch_hills");
      BIOMES_BY_ID.put(157, "minecraft:dark_forest_hills");
      BIOMES_BY_ID.put(158, "minecraft:snowy_taiga_mountains");
      BIOMES_BY_ID.put(160, "minecraft:giant_spruce_taiga");
      BIOMES_BY_ID.put(161, "minecraft:giant_spruce_taiga_hills");
      BIOMES_BY_ID.put(162, "minecraft:modified_gravelly_mountains");
      BIOMES_BY_ID.put(163, "minecraft:shattered_savanna");
      BIOMES_BY_ID.put(164, "minecraft:shattered_savanna_plateau");
      BIOMES_BY_ID.put(165, "minecraft:eroded_badlands");
      BIOMES_BY_ID.put(166, "minecraft:modified_wooded_badlands_plateau");
      BIOMES_BY_ID.put(167, "minecraft:modified_badlands_plateau");
      BIOMES_BY_ID.put(168, "minecraft:bamboo_jungle");
      BIOMES_BY_ID.put(169, "minecraft:bamboo_jungle_hills");
      BIOMES_BY_ID.put(170, "minecraft:soul_sand_valley");
      BIOMES_BY_ID.put(171, "minecraft:crimson_forest");
      BIOMES_BY_ID.put(172, "minecraft:warped_forest");
      BIOMES_BY_ID.put(173, "minecraft:basalt_deltas");
      BIOMES_BY_ID.put(174, "minecraft:dripstone_caves");
      BIOMES_BY_ID.put(175, "minecraft:lush_caves");
      BIOMES_BY_ID.put(177, "minecraft:meadow");
      BIOMES_BY_ID.put(178, "minecraft:grove");
      BIOMES_BY_ID.put(179, "minecraft:snowy_slopes");
      BIOMES_BY_ID.put(180, "minecraft:snowcapped_peaks");
      BIOMES_BY_ID.put(181, "minecraft:lofty_peaks");
      BIOMES_BY_ID.put(182, "minecraft:stony_peaks");
   }
}
