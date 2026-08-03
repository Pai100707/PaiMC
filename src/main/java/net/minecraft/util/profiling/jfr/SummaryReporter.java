package net.minecraft.util.profiling.jfr;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SummaryReporter {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Runnable onDeregistration;

   protected SummaryReporter(Runnable $$0) {
      this.onDeregistration = $$0;
   }

   public void recordingStopped(@Nullable Path $$0) {
      if ($$0 != null) {
         this.onDeregistration.run();
         infoWithFallback(() -> "Dumped flight recorder profiling to " + $$0);

         JfrStatsResult $$1;
         try {
            $$1 = JfrStatsParser.parse($$0);
         } catch (Throwable var5) {
            warnWithFallback(() -> "Failed to parse JFR recording", var5);
            return;
         }

         try {
            infoWithFallback($$1::asJson);
            Path $$4 = $$0.resolveSibling("jfr-report-" + StringUtils.substringBefore($$0.getFileName().toString(), ".jfr") + ".json");
            Files.writeString($$4, $$1.asJson(), StandardOpenOption.CREATE);
            infoWithFallback(() -> "Dumped recording summary to " + $$4);
         } catch (Throwable var4) {
            warnWithFallback(() -> "Failed to output JFR report", var4);
         }
      }
   }

   private static void infoWithFallback(Supplier<String> $$0) {
      if (LogUtils.isLoggerActive()) {
         LOGGER.info($$0.get());
      } else {
         Bootstrap.realStdoutPrintln($$0.get());
      }
   }

   private static void warnWithFallback(Supplier<String> $$0, Throwable $$1) {
      if (LogUtils.isLoggerActive()) {
         LOGGER.warn($$0.get(), $$1);
      } else {
         Bootstrap.realStdoutPrintln($$0.get());
         $$1.printStackTrace(Bootstrap.STDOUT);
      }
   }
}
