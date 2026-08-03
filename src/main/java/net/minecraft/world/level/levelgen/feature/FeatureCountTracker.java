package net.minecraft.world.level.levelgen.feature;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class FeatureCountTracker {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final LoadingCache<ServerLevel, FeatureCountTracker.LevelData> data = CacheBuilder.newBuilder()
      .weakKeys()
      .expireAfterAccess(5L, TimeUnit.MINUTES)
      .build(new CacheLoader<ServerLevel, FeatureCountTracker.LevelData>() {
         public FeatureCountTracker.LevelData load(ServerLevel $$0) {
            return new FeatureCountTracker.LevelData(Object2IntMaps.synchronize(new Object2IntOpenHashMap()), new MutableInt(0));
         }
      });

   public static void chunkDecorated(ServerLevel $$0) {
      try {
         ((FeatureCountTracker.LevelData)data.get($$0)).chunksWithFeatures().increment();
      } catch (Exception var2) {
         LOGGER.error("Failed to increment chunk count", var2);
      }
   }

   public static void featurePlaced(ServerLevel $$0, ConfiguredFeature<?, ?> $$1, Optional<PlacedFeature> $$2) {
      try {
         ((FeatureCountTracker.LevelData)data.get($$0))
            .featureData()
            .computeInt(new FeatureCountTracker.FeatureData($$1, $$2), ($$0x, $$1x) -> $$1x == null ? 1 : $$1x + 1);
      } catch (Exception var4) {
         LOGGER.error("Failed to increment feature count", var4);
      }
   }

   public static void clearCounts() {
      data.invalidateAll();
      LOGGER.debug("Cleared feature counts");
   }

   public static void logCounts() {
      LOGGER.debug("Logging feature counts:");
      data.asMap()
         .forEach(
            ($$0, $$1) -> {
               String $$2 = $$0.dimension().identifier().toString();
               boolean $$3 = $$0.getServer().isRunning();
               Registry<PlacedFeature> $$4 = $$0.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE);
               String $$5 = ($$3 ? "running" : "dead") + " " + $$2;
               int $$6 = $$1.chunksWithFeatures().intValue();
               LOGGER.debug("{} total_chunks: {}", $$5, $$6);
               $$1.featureData()
                  .forEach(
                     ($$3x, $$4x) -> LOGGER.debug(
                        "{} {} {} {} {} {}",
                        new Object[]{
                           $$5,
                           String.format(Locale.ROOT, "%10d", $$4x),
                           String.format(Locale.ROOT, "%10f", (double)$$4x / $$6),
                           $$3x.topFeature().flatMap($$4::getResourceKey).map(ResourceKey::identifier),
                           $$3x.feature().feature(),
                           $$3x.feature()
                        }
                     )
                  );
            }
         );
   }

   record FeatureData(ConfiguredFeature<?, ?> feature, Optional<PlacedFeature> topFeature) {
   }

   record LevelData(Object2IntMap<FeatureCountTracker.FeatureData> featureData, MutableInt chunksWithFeatures) {
   }
}
