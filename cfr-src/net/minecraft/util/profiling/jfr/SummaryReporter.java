/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
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

    /*
     * WARNING - void declaration
     */
    public void recordingStopped(@Nullable Path $$0) {
        if ($$0 == null) {
            return;
        }
        this.onDeregistration.run();
        SummaryReporter.infoWithFallback(() -> "Dumped flight recorder profiling to " + String.valueOf($$0));
        try {
            JfrStatsResult $$1 = JfrStatsParser.parse($$0);
        }
        catch (Throwable $$2) {
            SummaryReporter.warnWithFallback(() -> "Failed to parse JFR recording", $$2);
            return;
        }
        try {
            void $$3;
            SummaryReporter.infoWithFallback(((JfrStatsResult)$$3)::asJson);
            Path $$4 = $$0.resolveSibling("jfr-report-" + StringUtils.substringBefore((String)$$0.getFileName().toString(), (String)".jfr") + ".json");
            Files.writeString($$4, (CharSequence)$$3.asJson(), StandardOpenOption.CREATE);
            SummaryReporter.infoWithFallback(() -> "Dumped recording summary to " + String.valueOf($$4));
        }
        catch (Throwable $$5) {
            SummaryReporter.warnWithFallback(() -> "Failed to output JFR report", $$5);
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

