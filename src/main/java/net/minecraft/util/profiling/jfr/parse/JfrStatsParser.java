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

public class JfrStatsParser {
   private Instant recordingStarted = Instant.EPOCH;
   private Instant recordingEnded = Instant.EPOCH;
   private final List<ChunkGenStat> chunkGenStats = new ArrayList<>();
   private final List<StructureGenStat> structureGenStats = new ArrayList<>();
   private final List<CpuLoadStat> cpuLoadStat = new ArrayList<>();
   private final Map<PacketIdentification, JfrStatsParser.MutableCountAndSize> receivedPackets = new HashMap<>();
   private final Map<PacketIdentification, JfrStatsParser.MutableCountAndSize> sentPackets = new HashMap<>();
   private final Map<ChunkIdentification, JfrStatsParser.MutableCountAndSize> readChunks = new HashMap<>();
   private final Map<ChunkIdentification, JfrStatsParser.MutableCountAndSize> writtenChunks = new HashMap<>();
   private final List<FileIOStat> fileWrites = new ArrayList<>();
   private final List<FileIOStat> fileReads = new ArrayList<>();
   private int garbageCollections;
   private Duration gcTotalDuration = Duration.ZERO;
   private final List<GcHeapStat> gcHeapStats = new ArrayList<>();
   private final List<ThreadAllocationStat> threadAllocationStats = new ArrayList<>();
   private final List<FpsStat> fps = new ArrayList<>();
   private final List<TickTimeStat> serverTickTimes = new ArrayList<>();
   
   private Duration worldCreationDuration = null;

   private JfrStatsParser(Stream<RecordedEvent> $$0) {
      this.capture($$0);
   }

   public static JfrStatsResult parse(Path $$0) {
      try {
         JfrStatsResult var4;
         try (final RecordingFile $$1 = new RecordingFile($$0)) {
            Iterator<RecordedEvent> $$2 = new Iterator<RecordedEvent>() {
               @Override
               public boolean hasNext() {
                  return $$1.hasMoreEvents();
               }

               public RecordedEvent next() {
                  if (!this.hasNext()) {
                     throw new NoSuchElementException();
                  } else {
                     try {
                        return $$1.readEvent();
                     } catch (IOException var2) {
                        throw new UncheckedIOException(var2);
                     }
                  }
               }
            };
            Stream<RecordedEvent> $$3 = StreamSupport.stream(Spliterators.spliteratorUnknownSize($$2, 1297), false);
            var4 = new JfrStatsParser($$3).results();
         }

         return var4;
      } catch (IOException var7) {
         throw new UncheckedIOException(var7);
      }
   }

   private JfrStatsResult results() {
      Duration $$0 = Duration.between(this.recordingStarted, this.recordingEnded);
      return new JfrStatsResult(
         this.recordingStarted,
         this.recordingEnded,
         $$0,
         this.worldCreationDuration,
         this.fps,
         this.serverTickTimes,
         this.cpuLoadStat,
         GcHeapStat.summary($$0, this.gcHeapStats, this.gcTotalDuration, this.garbageCollections),
         ThreadAllocationStat.summary(this.threadAllocationStats),
         collectIoStats($$0, this.receivedPackets),
         collectIoStats($$0, this.sentPackets),
         collectIoStats($$0, this.writtenChunks),
         collectIoStats($$0, this.readChunks),
         FileIOStat.summary($$0, this.fileWrites),
         FileIOStat.summary($$0, this.fileReads),
         this.chunkGenStats,
         this.structureGenStats
      );
   }

   private void capture(Stream<RecordedEvent> $$0) {
      $$0.forEach($$0x -> {
         if ($$0x.getEndTime().isAfter(this.recordingEnded) || this.recordingEnded.equals(Instant.EPOCH)) {
            this.recordingEnded = $$0x.getEndTime();
         }

         if ($$0x.getStartTime().isBefore(this.recordingStarted) || this.recordingStarted.equals(Instant.EPOCH)) {
            this.recordingStarted = $$0x.getStartTime();
         }

         String var2 = $$0x.getEventType().getName();
         switch (var2) {
            case "minecraft.ChunkGeneration":
               this.chunkGenStats.add(ChunkGenStat.from($$0x));
               break;
            case "minecraft.StructureGeneration":
               this.structureGenStats.add(StructureGenStat.from($$0x));
               break;
            case "minecraft.LoadWorld":
               this.worldCreationDuration = $$0x.getDuration();
               break;
            case "minecraft.ClientFps":
               this.fps.add(FpsStat.from($$0x, "fps"));
               break;
            case "minecraft.ServerTickTime":
               this.serverTickTimes.add(TickTimeStat.from($$0x));
               break;
            case "minecraft.PacketReceived":
               this.incrementPacket($$0x, $$0x.getInt("bytes"), this.receivedPackets);
               break;
            case "minecraft.PacketSent":
               this.incrementPacket($$0x, $$0x.getInt("bytes"), this.sentPackets);
               break;
            case "minecraft.ChunkRegionRead":
               this.incrementChunk($$0x, $$0x.getInt("bytes"), this.readChunks);
               break;
            case "minecraft.ChunkRegionWrite":
               this.incrementChunk($$0x, $$0x.getInt("bytes"), this.writtenChunks);
               break;
            case "jdk.ThreadAllocationStatistics":
               this.threadAllocationStats.add(ThreadAllocationStat.from($$0x));
               break;
            case "jdk.GCHeapSummary":
               this.gcHeapStats.add(GcHeapStat.from($$0x));
               break;
            case "jdk.CPULoad":
               this.cpuLoadStat.add(CpuLoadStat.from($$0x));
               break;
            case "jdk.FileWrite":
               this.appendFileIO($$0x, this.fileWrites, "bytesWritten");
               break;
            case "jdk.FileRead":
               this.appendFileIO($$0x, this.fileReads, "bytesRead");
               break;
            case "jdk.GarbageCollection":
               this.garbageCollections++;
               this.gcTotalDuration = this.gcTotalDuration.plus($$0x.getDuration());
         }
      });
   }

   private void incrementPacket(RecordedEvent $$0, int $$1, Map<PacketIdentification, JfrStatsParser.MutableCountAndSize> $$2) {
      $$2.computeIfAbsent(PacketIdentification.from($$0), $$0x -> new JfrStatsParser.MutableCountAndSize()).increment($$1);
   }

   private void incrementChunk(RecordedEvent $$0, int $$1, Map<ChunkIdentification, JfrStatsParser.MutableCountAndSize> $$2) {
      $$2.computeIfAbsent(ChunkIdentification.from($$0), $$0x -> new JfrStatsParser.MutableCountAndSize()).increment($$1);
   }

   private void appendFileIO(RecordedEvent $$0, List<FileIOStat> $$1, String $$2) {
      $$1.add(new FileIOStat($$0.getDuration(), $$0.getString("path"), $$0.getLong($$2)));
   }

   private static <T> IoSummary<T> collectIoStats(Duration $$0, Map<T, JfrStatsParser.MutableCountAndSize> $$1) {
      List<Pair<T, IoSummary.CountAndSize>> $$2 = $$1.entrySet()
         .stream()
         .map($$0x -> Pair.of($$0x.getKey(), ((JfrStatsParser.MutableCountAndSize)$$0x.getValue()).toCountAndSize()))
         .toList();
      return new IoSummary<>($$0, $$2);
   }

   public static final class MutableCountAndSize {
      private long count;
      private long totalSize;

      public void increment(int $$0) {
         this.totalSize += $$0;
         this.count++;
      }

      public IoSummary.CountAndSize toCountAndSize() {
         return new IoSummary.CountAndSize(this.count, this.totalSize);
      }
   }
}
