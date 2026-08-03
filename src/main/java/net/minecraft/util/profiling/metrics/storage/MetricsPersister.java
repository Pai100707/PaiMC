package net.minecraft.util.profiling.metrics.storage;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class MetricsPersister {
   public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
   public static final String METRICS_DIR_NAME = "metrics";
   public static final String DEVIATIONS_DIR_NAME = "deviations";
   public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
   private static final Logger LOGGER = LogUtils.getLogger();
   private final String rootFolderName;

   public MetricsPersister(String $$0) {
      this.rootFolderName = $$0;
   }

   public Path saveReports(Set<MetricSampler> $$0, Map<MetricSampler, List<RecordedDeviation>> $$1, ProfileResults $$2) {
      try {
         Files.createDirectories(PROFILING_RESULTS_DIR);
      } catch (IOException var8) {
         throw new UncheckedIOException(var8);
      }

      try {
         Path $$4 = Files.createTempDirectory("minecraft-profiling");
         $$4.toFile().deleteOnExit();
         Files.createDirectories(PROFILING_RESULTS_DIR);
         Path $$5 = $$4.resolve(this.rootFolderName);
         Path $$6 = $$5.resolve("metrics");
         this.saveMetrics($$0, $$6);
         if (!$$1.isEmpty()) {
            this.saveDeviations($$1, $$5.resolve("deviations"));
         }

         this.saveProfilingTaskExecutionResult($$2, $$5);
         return $$4;
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   private void saveMetrics(Set<MetricSampler> $$0, Path $$1) {
      if ($$0.isEmpty()) {
         throw new IllegalArgumentException("Expected at least one sampler to persist");
      } else {
         Map<MetricCategory, List<MetricSampler>> $$2 = $$0.stream().collect(Collectors.groupingBy(MetricSampler::getCategory));
         $$2.forEach(($$1x, $$2x) -> this.saveCategory($$1x, $$2x, $$1));
      }
   }

   private void saveCategory(MetricCategory $$0, List<MetricSampler> $$1, Path $$2) {
      Path $$3 = $$2.resolve(net.minecraft.util.Util.sanitizeName($$0.getDescription(), Identifier::validPathChar) + ".csv");
      Writer $$4 = null;

      try {
         Files.createDirectories($$3.getParent());
         $$4 = Files.newBufferedWriter($$3, StandardCharsets.UTF_8);
         net.minecraft.util.CsvOutput.Builder $$5 = net.minecraft.util.CsvOutput.builder();
         $$5.addColumn("@tick");

         for (MetricSampler $$6 : $$1) {
            $$5.addColumn($$6.getName());
         }

         net.minecraft.util.CsvOutput $$7 = $$5.build($$4);
         List<MetricSampler.SamplerResult> $$8 = $$1.stream().map(MetricSampler::result).collect(Collectors.toList());
         int $$9 = $$8.stream().mapToInt(MetricSampler.SamplerResult::getFirstTick).summaryStatistics().getMin();
         int $$10 = $$8.stream().mapToInt(MetricSampler.SamplerResult::getLastTick).summaryStatistics().getMax();

         for (int $$11 = $$9; $$11 <= $$10; $$11++) {
            int $$12 = $$11;
            Stream<String> $$13 = $$8.stream().map($$1x -> String.valueOf($$1x.valueAtTick($$12)));
            Object[] $$14 = Stream.concat(Stream.of(String.valueOf($$11)), $$13).toArray(String[]::new);
            $$7.writeRow($$14);
         }

         LOGGER.info("Flushed metrics to {}", $$3);
      } catch (Exception var18) {
         LOGGER.error("Could not save profiler results to {}", $$3, var18);
      } finally {
         IOUtils.closeQuietly($$4);
      }
   }

   private void saveDeviations(Map<MetricSampler, List<RecordedDeviation>> $$0, Path $$1) {
      DateTimeFormatter $$2 = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS", Locale.UK).withZone(ZoneId.systemDefault());
      $$0.forEach(
         ($$2x, $$3) -> $$3.forEach(
            $$3x -> {
               String $$4 = $$2.format($$3x.timestamp);
               Path $$5 = $$1.resolve(net.minecraft.util.Util.sanitizeName($$2x.getName(), Identifier::validPathChar))
                  .resolve(String.format(Locale.ROOT, "%d@%s.txt", $$3x.tick, $$4));
               $$3x.profilerResultAtTick.saveResults($$5);
            }
         )
      );
   }

   private void saveProfilingTaskExecutionResult(ProfileResults $$0, Path $$1) {
      $$0.saveResults($$1.resolve("profiling.txt"));
   }
}
