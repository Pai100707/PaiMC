/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.parse;

import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import net.minecraft.util.profiling.jfr.parse.JfrStatsResult;
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
import org.jspecify.annotations.Nullable;

public class JfrStatsParser {
    private Instant recordingStarted = Instant.EPOCH;
    private Instant recordingEnded = Instant.EPOCH;
    private final List<ChunkGenStat> chunkGenStats = new ArrayList<ChunkGenStat>();
    private final List<StructureGenStat> structureGenStats = new ArrayList<StructureGenStat>();
    private final List<CpuLoadStat> cpuLoadStat = new ArrayList<CpuLoadStat>();
    private final Map<PacketIdentification, MutableCountAndSize> receivedPackets = new HashMap<PacketIdentification, MutableCountAndSize>();
    private final Map<PacketIdentification, MutableCountAndSize> sentPackets = new HashMap<PacketIdentification, MutableCountAndSize>();
    private final Map<ChunkIdentification, MutableCountAndSize> readChunks = new HashMap<ChunkIdentification, MutableCountAndSize>();
    private final Map<ChunkIdentification, MutableCountAndSize> writtenChunks = new HashMap<ChunkIdentification, MutableCountAndSize>();
    private final List<FileIOStat> fileWrites = new ArrayList<FileIOStat>();
    private final List<FileIOStat> fileReads = new ArrayList<FileIOStat>();
    private int garbageCollections;
    private Duration gcTotalDuration = Duration.ZERO;
    private final List<GcHeapStat> gcHeapStats = new ArrayList<GcHeapStat>();
    private final List<ThreadAllocationStat> threadAllocationStats = new ArrayList<ThreadAllocationStat>();
    private final List<FpsStat> fps = new ArrayList<FpsStat>();
    private final List<TickTimeStat> serverTickTimes = new ArrayList<TickTimeStat>();
    private @Nullable Duration worldCreationDuration = null;

    private JfrStatsParser(Stream<RecordedEvent> $$0) {
        this.capture($$0);
    }

    public static JfrStatsResult parse(Path $$0) {
        JfrStatsResult jfrStatsResult;
        final RecordingFile $$1 = new RecordingFile($$0);
        try {
            Iterator<RecordedEvent> $$2 = new Iterator<RecordedEvent>(){

                @Override
                public boolean hasNext() {
                    return $$1.hasMoreEvents();
                }

                @Override
                public RecordedEvent next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    try {
                        return $$1.readEvent();
                    }
                    catch (IOException $$0) {
                        throw new UncheckedIOException($$0);
                    }
                }

                @Override
                public /* synthetic */ Object next() {
                    return this.next();
                }
            };
            Stream<RecordedEvent> $$3 = StreamSupport.stream(Spliterators.spliteratorUnknownSize($$2, 1297), false);
            jfrStatsResult = new JfrStatsParser($$3).results();
        }
        catch (Throwable throwable) {
            try {
                try {
                    $$1.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException $$4) {
                throw new UncheckedIOException($$4);
            }
        }
        $$1.close();
        return jfrStatsResult;
    }

    private JfrStatsResult results() {
        Duration $$0 = Duration.between(this.recordingStarted, this.recordingEnded);
        return new JfrStatsResult(this.recordingStarted, this.recordingEnded, $$0, this.worldCreationDuration, this.fps, this.serverTickTimes, this.cpuLoadStat, GcHeapStat.summary($$0, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections), ThreadAllocationStat.summary(this.threadAllocationStats), JfrStatsParser.collectIoStats($$0, this.receivedPackets), JfrStatsParser.collectIoStats($$0, this.sentPackets), JfrStatsParser.collectIoStats($$0, this.writtenChunks), JfrStatsParser.collectIoStats($$0, this.readChunks), FileIOStat.summary($$0, this.fileWrites), FileIOStat.summary($$0, this.fileReads), this.chunkGenStats, this.structureGenStats);
    }

    private void capture(Stream<RecordedEvent> $$02) {
        $$02.forEach($$0 -> {
            if ($$0.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
                this.recordingEnded = $$0.getEndTime();
            }
            if ($$0.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
                this.recordingStarted = $$0.getStartTime();
            }
            switch ($$0.getEventType().getName()) {
                case "minecraft.ChunkGeneration": {
                    this.chunkGenStats.add(ChunkGenStat.from($$0));
                    break;
                }
                case "minecraft.StructureGeneration": {
                    this.structureGenStats.add(StructureGenStat.from($$0));
                    break;
                }
                case "minecraft.LoadWorld": {
                    this.worldCreationDuration = $$0.getDuration();
                    break;
                }
                case "minecraft.ClientFps": {
                    this.fps.add(FpsStat.from($$0, "fps"));
                    break;
                }
                case "minecraft.ServerTickTime": {
                    this.serverTickTimes.add(TickTimeStat.from($$0));
                    break;
                }
                case "minecraft.PacketReceived": {
                    this.incrementPacket((RecordedEvent)$$0, $$0.getInt("bytes"), this.receivedPackets);
                    break;
                }
                case "minecraft.PacketSent": {
                    this.incrementPacket((RecordedEvent)$$0, $$0.getInt("bytes"), this.sentPackets);
                    break;
                }
                case "minecraft.ChunkRegionRead": {
                    this.incrementChunk((RecordedEvent)$$0, $$0.getInt("bytes"), this.readChunks);
                    break;
                }
                case "minecraft.ChunkRegionWrite": {
                    this.incrementChunk((RecordedEvent)$$0, $$0.getInt("bytes"), this.writtenChunks);
                    break;
                }
                case "jdk.ThreadAllocationStatistics": {
                    this.threadAllocationStats.add(ThreadAllocationStat.from($$0));
                    break;
                }
                case "jdk.GCHeapSummary": {
                    this.gcHeapStats.add(GcHeapStat.from($$0));
                    break;
                }
                case "jdk.CPULoad": {
                    this.cpuLoadStat.add(CpuLoadStat.from($$0));
                    break;
                }
                case "jdk.FileWrite": {
                    this.appendFileIO((RecordedEvent)$$0, this.fileWrites, "bytesWritten");
                    break;
                }
                case "jdk.FileRead": {
                    this.appendFileIO((RecordedEvent)$$0, this.fileReads, "bytesRead");
                    break;
                }
                case "jdk.GarbageCollection": {
                    ++this.garbageCollections;
                    this.gcTotalDuration = this.gcTotalDuration.plus($$0.getDuration());
                    break;
                }
            }
        });
    }

    private void incrementPacket(RecordedEvent $$02, int $$1, Map<PacketIdentification, MutableCountAndSize> $$2) {
        $$2.computeIfAbsent(PacketIdentification.from($$02), $$0 -> new MutableCountAndSize()).increment($$1);
    }

    private void incrementChunk(RecordedEvent $$02, int $$1, Map<ChunkIdentification, MutableCountAndSize> $$2) {
        $$2.computeIfAbsent(ChunkIdentification.from($$02), $$0 -> new MutableCountAndSize()).increment($$1);
    }

    private void appendFileIO(RecordedEvent $$0, List<FileIOStat> $$1, String $$2) {
        $$1.add(new FileIOStat($$0.getDuration(), $$0.getString("path"), $$0.getLong($$2)));
    }

    private static <T> IoSummary<T> collectIoStats(Duration $$02, Map<T, MutableCountAndSize> $$1) {
        List $$2 = $$1.entrySet().stream().map($$0 -> Pair.of($$0.getKey(), (Object)((MutableCountAndSize)$$0.getValue()).toCountAndSize())).toList();
        return new IoSummary($$02, $$2);
    }

    public static final class MutableCountAndSize {
        private long count;
        private long totalSize;

        public void increment(int $$0) {
            this.totalSize += (long)$$0;
            ++this.count;
        }

        public IoSummary.CountAndSize toCountAndSize() {
            return new IoSummary.CountAndSize(this.count, this.totalSize);
        }
    }
}

