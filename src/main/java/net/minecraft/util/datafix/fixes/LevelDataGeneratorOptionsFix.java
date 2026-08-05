package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LevelDataGeneratorOptionsFix extends DataFix {
   static final Map<String, String> MAP = net.minecraft.util.Util.make(Maps.newHashMap(), $$0 -> {
      $$0.put("0", "minecraft:ocean");
      $$0.put("1", "minecraft:plains");
      $$0.put("2", "minecraft:desert");
      $$0.put("3", "minecraft:mountains");
      $$0.put("4", "minecraft:forest");
      $$0.put("5", "minecraft:taiga");
      $$0.put("6", "minecraft:swamp");
      $$0.put("7", "minecraft:river");
      $$0.put("8", "minecraft:nether");
      $$0.put("9", "minecraft:the_end");
      $$0.put("10", "minecraft:frozen_ocean");
      $$0.put("11", "minecraft:frozen_river");
      $$0.put("12", "minecraft:snowy_tundra");
      $$0.put("13", "minecraft:snowy_mountains");
      $$0.put("14", "minecraft:mushroom_fields");
      $$0.put("15", "minecraft:mushroom_field_shore");
      $$0.put("16", "minecraft:beach");
      $$0.put("17", "minecraft:desert_hills");
      $$0.put("18", "minecraft:wooded_hills");
      $$0.put("19", "minecraft:taiga_hills");
      $$0.put("20", "minecraft:mountain_edge");
      $$0.put("21", "minecraft:jungle");
      $$0.put("22", "minecraft:jungle_hills");
      $$0.put("23", "minecraft:jungle_edge");
      $$0.put("24", "minecraft:deep_ocean");
      $$0.put("25", "minecraft:stone_shore");
      $$0.put("26", "minecraft:snowy_beach");
      $$0.put("27", "minecraft:birch_forest");
      $$0.put("28", "minecraft:birch_forest_hills");
      $$0.put("29", "minecraft:dark_forest");
      $$0.put("30", "minecraft:snowy_taiga");
      $$0.put("31", "minecraft:snowy_taiga_hills");
      $$0.put("32", "minecraft:giant_tree_taiga");
      $$0.put("33", "minecraft:giant_tree_taiga_hills");
      $$0.put("34", "minecraft:wooded_mountains");
      $$0.put("35", "minecraft:savanna");
      $$0.put("36", "minecraft:savanna_plateau");
      $$0.put("37", "minecraft:badlands");
      $$0.put("38", "minecraft:wooded_badlands_plateau");
      $$0.put("39", "minecraft:badlands_plateau");
      $$0.put("40", "minecraft:small_end_islands");
      $$0.put("41", "minecraft:end_midlands");
      $$0.put("42", "minecraft:end_highlands");
      $$0.put("43", "minecraft:end_barrens");
      $$0.put("44", "minecraft:warm_ocean");
      $$0.put("45", "minecraft:lukewarm_ocean");
      $$0.put("46", "minecraft:cold_ocean");
      $$0.put("47", "minecraft:deep_warm_ocean");
      $$0.put("48", "minecraft:deep_lukewarm_ocean");
      $$0.put("49", "minecraft:deep_cold_ocean");
      $$0.put("50", "minecraft:deep_frozen_ocean");
      $$0.put("127", "minecraft:the_void");
      $$0.put("129", "minecraft:sunflower_plains");
      $$0.put("130", "minecraft:desert_lakes");
      $$0.put("131", "minecraft:gravelly_mountains");
      $$0.put("132", "minecraft:flower_forest");
      $$0.put("133", "minecraft:taiga_mountains");
      $$0.put("134", "minecraft:swamp_hills");
      $$0.put("140", "minecraft:ice_spikes");
      $$0.put("149", "minecraft:modified_jungle");
      $$0.put("151", "minecraft:modified_jungle_edge");
      $$0.put("155", "minecraft:tall_birch_forest");
      $$0.put("156", "minecraft:tall_birch_hills");
      $$0.put("157", "minecraft:dark_forest_hills");
      $$0.put("158", "minecraft:snowy_taiga_mountains");
      $$0.put("160", "minecraft:giant_spruce_taiga");
      $$0.put("161", "minecraft:giant_spruce_taiga_hills");
      $$0.put("162", "minecraft:modified_gravelly_mountains");
      $$0.put("163", "minecraft:shattered_savanna");
      $$0.put("164", "minecraft:shattered_savanna_plateau");
      $$0.put("165", "minecraft:eroded_badlands");
      $$0.put("166", "minecraft:modified_wooded_badlands_plateau");
      $$0.put("167", "minecraft:modified_badlands_plateau");
   });
   public static final String GENERATOR_OPTIONS = "generatorOptions";

   public LevelDataGeneratorOptionsFix(Schema $$0, boolean $$1) {
      super($$0, $$1);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getOutputSchema().getType(References.LEVEL);
      return this.fixTypeEverywhereTyped(
         "LevelDataGeneratorOptionsFix",
         this.getInputSchema().getType(References.LEVEL),
         $$0,
         $$1 -> net.minecraft.util.Util.writeAndReadTypedOrThrow($$1, $$0, $$0xx -> {
            Optional<String> $$1x = $$0xx.get("generatorOptions").asString().result();
            if ("flat".equalsIgnoreCase($$0xx.get("generatorName").asString(""))) {
               String $$2 = $$1x.orElse("");
               return $$0xx.set("generatorOptions", convert($$2, $$0xx.getOps()));
            } else if ("buffet".equalsIgnoreCase($$0xx.get("generatorName").asString("")) && $$1x.isPresent()) {
               JsonElement $$3 = net.minecraft.util.LenientJsonParser.parse($$1x.get());
               return $$0xx.set("generatorOptions", new Dynamic(JsonOps.INSTANCE, $$3).convert($$0xx.getOps()));
            } else {
               return $$0xx;
            }
         })
      );
   }

   private static <T> Dynamic<T> convert(String $$0, DynamicOps<T> $$1) {
      Iterator<String> $$2 = Splitter.on(';').split($$0).iterator();
      String $$3 = "minecraft:plains";
      Map<String, Map<String, String>> $$4 = Maps.newHashMap();
      List<Pair<Integer, String>> $$5;
      if (!$$0.isEmpty() && $$2.hasNext()) {
         $$5 = getLayersInfoFromString($$2.next());
         if (!$$5.isEmpty()) {
            if ($$2.hasNext()) {
               $$3 = MAP.getOrDefault($$2.next(), "minecraft:plains");
            }

            if ($$2.hasNext()) {
               String[] $$6 = $$2.next().toLowerCase(Locale.ROOT).split(",");

               for (String $$7 : $$6) {
                  String[] $$8 = $$7.split("\\(", 2);
                  if (!$$8[0].isEmpty()) {
                     $$4.put($$8[0], Maps.newHashMap());
                     if ($$8.length > 1 && $$8[1].endsWith(")") && $$8[1].length() > 1) {
                        String[] $$9 = $$8[1].substring(0, $$8[1].length() - 1).split(" ");

                        for (String $$10 : $$9) {
                           String[] $$11 = $$10.split("=", 2);
                           if ($$11.length == 2) {
                              $$4.get($$8[0]).put($$11[0], $$11[1]);
                           }
                        }
                     }
                  }
               }
            } else {
               $$4.put("village", Maps.newHashMap());
            }
         }
      } else {
         $$5 = Lists.newArrayList();
         $$5.add(Pair.of(1, "minecraft:bedrock"));
         $$5.add(Pair.of(2, "minecraft:dirt"));
         $$5.add(Pair.of(1, "minecraft:grass_block"));
         $$4.put("village", Maps.newHashMap());
      }

      T $$13 = (T)$$1.createList(
         $$5.stream()
            .map(
               $$1x -> $$1.createMap(
                  ImmutableMap.of(
                     $$1.createString("height"), $$1.createInt((Integer)$$1x.getFirst()), $$1.createString("block"), $$1.createString((String)$$1x.getSecond())
                  )
               )
            )
      );
      T $$14 = (T)$$1.createMap(
         $$4.entrySet()
            .stream()
            .map(
               $$1x -> Pair.of(
                  $$1.createString(((String)$$1x.getKey()).toLowerCase(Locale.ROOT)),
                  $$1.createMap(
                     ((Map)$$1x.getValue())
                        .entrySet()
                        .stream()
                        .map($$1xx -> Pair.of($$1.createString((String)$$1xx.getKey()), $$1.createString((String)$$1xx.getValue())))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                  )
               )
            )
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
      );
      return new Dynamic(
         $$1,
         $$1.createMap(
            ImmutableMap.of($$1.createString("layers"), $$13, $$1.createString("biome"), $$1.createString($$3), $$1.createString("structures"), $$14)
         )
      );
   }

   
   private static Pair<Integer, String> getLayerInfoFromString(String $$0) {
      String[] $$1 = $$0.split("\\*", 2);
      int $$2;
      if ($$1.length == 2) {
         try {
            $$2 = Integer.parseInt($$1[0]);
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         $$2 = 1;
      }

      String $$5 = $$1[$$1.length - 1];
      return Pair.of($$2, $$5);
   }

   private static List<Pair<Integer, String>> getLayersInfoFromString(String $$0) {
      List<Pair<Integer, String>> $$1 = Lists.newArrayList();
      String[] $$2 = $$0.split(",");

      for (String $$3 : $$2) {
         Pair<Integer, String> $$4 = getLayerInfoFromString($$3);
         if ($$4 == null) {
            return Collections.emptyList();
         }

         $$1.add($$4);
      }

      return $$1;
   }
}
