/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.collect.Streams;
import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Util;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;

public class ServerWatchdog
implements Runnable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long MAX_SHUTDOWN_TIME = 10000L;
    private static final int SHUTDOWN_STATUS = 1;
    private final DedicatedServer server;
    private final long maxTickTimeNanos;

    public ServerWatchdog(DedicatedServer $$0) {
        this.server = $$0;
        this.maxTickTimeNanos = $$0.getMaxTickLength() * TimeUtil.NANOSECONDS_PER_MILLISECOND;
    }

    @Override
    public void run() {
        while (this.server.isRunning()) {
            long $$0 = this.server.getNextTickTime();
            long $$1 = Util.getNanos();
            long $$2 = $$1 - $$0;
            if ($$2 > this.maxTickTimeNanos) {
                LOGGER.error(LogUtils.FATAL_MARKER, "A single server tick took {} seconds (should be max {})", (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf((float)$$2 / (float)TimeUtil.NANOSECONDS_PER_SECOND)), (Object)String.format(Locale.ROOT, "%.2f", Float.valueOf(this.server.tickRateManager().millisecondsPerTick() / (float)TimeUtil.MILLISECONDS_PER_SECOND)));
                LOGGER.error(LogUtils.FATAL_MARKER, "Considering it to be crashed, server will forcibly shutdown.");
                CrashReport $$3 = ServerWatchdog.createWatchdogCrashReport("Watching Server", this.server.getRunningThread().threadId());
                this.server.fillSystemReport($$3.getSystemReport());
                CrashReportCategory $$4 = $$3.addCategory("Performance stats");
                $$4.setDetail("Random tick rate", () -> this.server.getWorldData().getGameRules().getAsString(GameRules.RANDOM_TICK_SPEED));
                $$4.setDetail("Level stats", () -> Streams.stream(this.server.getAllLevels()).map($$0 -> String.valueOf($$0.dimension().identifier()) + ": " + $$0.getWatchdogStats()).collect(Collectors.joining(",\n")));
                Bootstrap.realStdoutPrintln("Crash report:\n" + $$3.getFriendlyReport(ReportType.CRASH));
                Path $$5 = this.server.getServerDirectory().resolve("crash-reports").resolve("crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                if ($$3.saveToFile($$5, ReportType.CRASH)) {
                    LOGGER.error("This crash report has been saved to: {}", (Object)$$5.toAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.exit();
            }
            try {
                Thread.sleep(($$0 + this.maxTickTimeNanos - $$1) / TimeUtil.NANOSECONDS_PER_MILLISECOND);
            }
            catch (InterruptedException interruptedException) {}
        }
    }

    public static CrashReport createWatchdogCrashReport(String $$0, long $$1) {
        ThreadMXBean $$2 = ManagementFactory.getThreadMXBean();
        ThreadInfo[] $$3 = $$2.dumpAllThreads(true, true);
        StringBuilder $$4 = new StringBuilder();
        Error $$5 = new Error("Watchdog");
        for (ThreadInfo $$6 : $$3) {
            if ($$6.getThreadId() == $$1) {
                $$5.setStackTrace($$6.getStackTrace());
            }
            $$4.append($$6);
            $$4.append("\n");
        }
        CrashReport $$7 = new CrashReport($$0, $$5);
        CrashReportCategory $$8 = $$7.addCategory("Thread Dump");
        $$8.setDetail("Threads", $$4);
        return $$7;
    }

    private void exit() {
        try {
            Timer $$0 = new Timer();
            $$0.schedule(new TimerTask(this){

                @Override
                public void run() {
                    Runtime.getRuntime().halt(1);
                }
            }, 10000L);
            System.exit(1);
        }
        catch (Throwable $$1) {
            Runtime.getRuntime().halt(1);
        }
    }
}

