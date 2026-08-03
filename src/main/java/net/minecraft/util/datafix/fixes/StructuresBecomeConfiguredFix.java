package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.LongStream;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix extends DataFix {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<String, StructuresBecomeConfiguredFix.Conversion> CONVERSION_MAP = ImmutableMap.builder()
      .put(
         "mineshaft",
         StructuresBecomeConfiguredFix.Conversion.biomeMapped(
            Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft"
         )
      )
      .put(
         "shipwreck",
         StructuresBecomeConfiguredFix.Conversion.biomeMapped(
            Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck"
         )
      )
      .put(
         "ocean_ruin",
         StructuresBecomeConfiguredFix.Conversion.biomeMapped(
            Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"),
            "minecraft:ocean_ruin_cold"
         )
      )
      .put(
         "village",
         StructuresBecomeConfiguredFix.Conversion.biomeMapped(
            Map.of(
               List.of("minecraft:desert"),
               "minecraft:village_desert",
               List.of("minecraft:savanna"),
               "minecraft:village_savanna",
               List.of("minecraft:snowy_plains"),
               "minecraft:village_snowy",
               List.of("minecraft:taiga"),
               "minecraft:village_taiga"
            ),
            "minecraft:village_plains"
         )
      )
      .put(
         "ruined_portal",
         StructuresBecomeConfiguredFix.Conversion.biomeMapped(
            Map.of(
               List.of("minecraft:desert"),
               "minecraft:ruined_portal_desert",
               List.of(
                  "minecraft:badlands",
                  "minecraft:eroded_badlands",
                  "minecraft:wooded_badlands",
                  "minecraft:windswept_hills",
                  "minecraft:windswept_forest",
                  "minecraft:windswept_gravelly_hills",
                  "minecraft:savanna_plateau",
                  "minecraft:windswept_savanna",
                  "minecraft:stony_shore",
                  "minecraft:meadow",
                  "minecraft:frozen_peaks",
                  "minecraft:jagged_peaks",
                  "minecraft:stony_peaks",
                  "minecraft:snowy_slopes"
               ),
               "minecraft:ruined_portal_mountain",
               List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"),
               "minecraft:ruined_portal_jungle",
               List.of(
                  "minecraft:deep_frozen_ocean",
                  "minecraft:deep_cold_ocean",
                  "minecraft:deep_ocean",
                  "minecraft:deep_lukewarm_ocean",
                  "minecraft:frozen_ocean",
                  "minecraft:ocean",
                  "minecraft:cold_ocean",
                  "minecraft:lukewarm_ocean",
                  "minecraft:warm_ocean"
               ),
               "minecraft:ruined_portal_ocean"
            ),
            "minecraft:ruined_portal"
         )
      )
      .put("pillager_outpost", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:pillager_outpost"))
      .put("mansion", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:mansion"))
      .put("jungle_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:jungle_pyramid"))
      .put("desert_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:desert_pyramid"))
      .put("igloo", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:igloo"))
      .put("swamp_hut", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:swamp_hut"))
      .put("stronghold", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:stronghold"))
      .put("monument", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:monument"))
      .put("fortress", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:fortress"))
      .put("endcity", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:end_city"))
      .put("buried_treasure", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:buried_treasure"))
      .put("nether_fossil", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:nether_fossil"))
      .put("bastion_remnant", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:bastion_remnant"))
      .build();

   public StructuresBecomeConfiguredFix(Schema $$0) {
      super($$0, false);
   }

   protected TypeRewriteRule makeRule() {
      Type<?> $$0 = this.getInputSchema().getType(References.CHUNK);
      Type<?> $$1 = this.getInputSchema().getType(References.CHUNK);
      return this.writeFixAndRead("StucturesToConfiguredStructures", $$0, $$1, this::fix);
   }

   private Dynamic<?> fix(Dynamic<?> $$0) {
      return $$0.update(
         "structures", $$1 -> $$1.update("starts", $$1x -> this.updateStarts($$1x, $$0)).update("References", $$1x -> this.updateReferences($$1x, $$0))
      );
   }

   private Dynamic<?> updateStarts(Dynamic<?> $$0, Dynamic<?> $$1) {
      Map<? extends Dynamic<?>, ? extends Dynamic<?>> $$2 = $$0.getMapValues().result().orElse(Map.of());
      HashMap<Dynamic<?>, Dynamic<?>> $$3 = Maps.newHashMap();
      $$2.forEach(($$2x, $$3x) -> {
         if (!$$3x.get("id").asString("INVALID").equals("INVALID")) {
            Dynamic<?> $$4 = this.findUpdatedStructureType($$2x, $$1);
            if ($$4 == null) {
               LOGGER.warn("Encountered unknown structure in datafixer: {}", $$2x.asString("<missing key>"));
            } else {
               $$3.computeIfAbsent($$4, $$2xx -> $$3x.set("id", $$4));
            }
         }
      });
      return $$1.createMap($$3);
   }

   private Dynamic<?> updateReferences(Dynamic<?> $$0, Dynamic<?> $$1) {
      Map<? extends Dynamic<?>, ? extends Dynamic<?>> $$2 = $$0.getMapValues().result().orElse(Map.of());
      HashMap<Dynamic<?>, Dynamic<?>> $$3 = Maps.newHashMap();
      $$2.forEach(($$2x, $$3x) -> {
         if ($$3x.asLongStream().count() != 0L) {
            Dynamic<?> $$4 = this.findUpdatedStructureType($$2x, $$1);
            if ($$4 == null) {
               LOGGER.warn("Encountered unknown structure in datafixer: {}", $$2x.asString("<missing key>"));
            } else {
               $$3.compute($$4, ($$1xx, $$2xx) -> $$2xx == null ? $$3x : $$3x.createLongList(LongStream.concat($$2xx.asLongStream(), $$3x.asLongStream())));
            }
         }
      });
      return $$1.createMap($$3);
   }

   @Nullable
   private Dynamic<?> findUpdatedStructureType(Dynamic<?> $$0, Dynamic<?> $$1) {
      String $$2 = $$0.asString("UNKNOWN").toLowerCase(Locale.ROOT);
      StructuresBecomeConfiguredFix.Conversion $$3 = CONVERSION_MAP.get($$2);
      if ($$3 == null) {
         return null;
      } else {
         String $$4 = $$3.fallback;
         if (!$$3.biomeMapping().isEmpty()) {
            Optional<String> $$5 = this.guessConfiguration($$1, $$3);
            if ($$5.isPresent()) {
               $$4 = $$5.get();
            }
         }

         return $$1.createString($$4);
      }
   }

   private Optional<String> guessConfiguration(Dynamic<?> $$0, StructuresBecomeConfiguredFix.Conversion $$1) {
      Object2IntArrayMap<String> $$2 = new Object2IntArrayMap();
      $$0.get("sections").asList(Function.identity()).forEach($$2x -> $$2x.get("biomes").get("palette").asList(Function.identity()).forEach($$2xx -> {
         String $$3 = $$1.biomeMapping().get($$2xx.asString(""));
         if ($$3 != null) {
            $$2.mergeInt($$3, 1, Integer::sum);
         }
      }));
      return $$2.object2IntEntrySet().stream().max(Comparator.comparingInt(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry::getIntValue)).map(Entry::getKey);
   }

   record Conversion(Map<String, String> biomeMapping, String fallback) {

      public static StructuresBecomeConfiguredFix.Conversion trivial(String $$0) {
         return new StructuresBecomeConfiguredFix.Conversion(Map.of(), $$0);
      }

      public static StructuresBecomeConfiguredFix.Conversion biomeMapped(Map<List<String>, String> $$0, String $$1) {
         return new StructuresBecomeConfiguredFix.Conversion(unpack($$0), $$1);
      }

      private static Map<String, String> unpack(Map<List<String>, String> $$0) {
         Builder<String, String> $$1 = ImmutableMap.builder();

         for (Entry<List<String>, String> $$2 : $$0.entrySet()) {
            $$2.getKey().forEach($$2x -> $$1.put($$2x, $$2.getValue()));
         }

         return $$1.build();
      }
   }
}
