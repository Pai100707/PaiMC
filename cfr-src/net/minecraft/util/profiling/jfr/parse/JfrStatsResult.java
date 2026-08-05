/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.util.profiling.jfr.serialize.JfrResultJsonSerializer;
import net.minecraft.util.profiling.jfr.stats.ChunkGenStat;
import net.minecraft.util.profiling.jfr.stats.ChunkIdentification;
import net.minecraft.util.profiling.jfr.stats.CpuLoadStat;
import net.minecraft.util.profiling.jfr.stats.FileIOStat;
import net.minecraft.util.profiling.jfr.stats.FpsStat;
import net.minecraft.util.profiling.jfr.stats.GcHeapStat;
import net.minecraft.util.profiling.jfr.stats.IoSummary;
import net.minecraft.util.profiling.jfr.stats.PacketIdentification;
import net.minecraft.util.profiling.jfr.stats.StructureGenStat;
import net.minecraft.util.profiling.jfr.stats.ThreadAllocationStat;
import net.minecraft.util.profiling.jfr.stats.TickTimeStat;
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;

public record JfrStatsResult(Instant recordingStarted, Instant recordingEnded, Duration recordingDuration, @Nullable Duration worldCreationDuration, List<FpsStat> fps, List<TickTimeStat> serverTickTimes, List<CpuLoadStat> cpuLoadStats, GcHeapStat.Summary heapSummary, ThreadAllocationStat.Summary threadAllocationSummary, IoSummary<PacketIdentification> receivedPacketsSummary, IoSummary<PacketIdentification> sentPacketsSummary, IoSummary<ChunkIdentification> writtenChunks, IoSummary<ChunkIdentification> readChunks, FileIOStat.Summary fileWrites, FileIOStat.Summary fileReads, List<ChunkGenStat> chunkGenStats, List<StructureGenStat> structureGenStats) {
    public List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> chunkGenSummary() {
        Map<ChunkStatus, List<ChunkGenStat>> $$02 = this.chunkGenStats.stream().collect(Collectors.groupingBy(ChunkGenStat::status));
        return $$02.entrySet().stream().map($$0 -> Pair.of((Object)((ChunkStatus)$$0.getKey()), TimedStatSummary.summary((List)$$0.getValue()))).filter($$0 -> ((Optional)$$0.getSecond()).isPresent()).map($$0 -> Pair.of((Object)((ChunkStatus)$$0.getFirst()), (Object)((TimedStatSummary)((Optional)$$0.getSecond()).get()))).sorted(Comparator.comparing($$0 -> ((TimedStatSummary)$$0.getSecond()).totalDuration()).reversed()).toList();
    }

    public String asJson() {
        return new JfrResultJsonSerializer().format(this);
    }
}

