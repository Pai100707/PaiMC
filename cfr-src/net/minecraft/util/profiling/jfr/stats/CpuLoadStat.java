/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record CpuLoadStat(double jvm, double userJvm, double system) {
    public static CpuLoadStat from(RecordedEvent $$0) {
        return new CpuLoadStat($$0.getFloat("jvmSystem"), $$0.getFloat("jvmUser"), $$0.getFloat("machineTotal"));
    }
}

