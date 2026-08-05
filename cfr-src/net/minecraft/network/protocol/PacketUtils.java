/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PacketUtils {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> $$0, T $$1, ServerLevel $$2) throws RunningOnDifferentThreadException {
        PacketUtils.ensureRunningOnSameThread($$0, $$1, $$2.getServer().packetProcessor());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> $$0, T $$1, PacketProcessor $$2) throws RunningOnDifferentThreadException {
        if (!$$2.isSameThread()) {
            $$2.scheduleIfPossible($$1, $$0);
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

    public static <T extends PacketListener> ReportedException makeReportedException(Exception $$0, Packet<T> $$1, T $$2) {
        if ($$0 instanceof ReportedException) {
            ReportedException $$3 = (ReportedException)$$0;
            PacketUtils.fillCrashReport($$3.getReport(), $$2, $$1);
            return $$3;
        }
        CrashReport $$4 = CrashReport.forThrowable($$0, "Main thread packet handler");
        PacketUtils.fillCrashReport($$4, $$2, $$1);
        return new ReportedException($$4);
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport $$0, T $$1, @Nullable Packet<T> $$2) {
        if ($$2 != null) {
            CrashReportCategory $$3 = $$0.addCategory("Incoming Packet");
            $$3.setDetail("Type", () -> $$2.type().toString());
            $$3.setDetail("Is Terminal", () -> Boolean.toString($$2.isTerminal()));
            $$3.setDetail("Is Skippable", () -> Boolean.toString($$2.isSkippable()));
        }
        $$1.fillCrashReport($$0);
    }
}

