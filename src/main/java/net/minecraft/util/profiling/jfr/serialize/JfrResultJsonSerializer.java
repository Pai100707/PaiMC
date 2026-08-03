package net.minecraft.util.profiling.jfr.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.LongSerializationPolicy;
import com.mojang.datafixers.util.Pair;
import java.time.Duration;
import java.util.DoubleSummaryStatistics;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import net.minecraft.util.profiling.jfr.Percentiles;
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
import net.minecraft.util.profiling.jfr.stats.TimedStatSummary;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public class JfrResultJsonSerializer {
   private static final String BYTES_PER_SECOND = "bytesPerSecond";
   private static final String COUNT = "count";
   private static final String DURATION_NANOS_TOTAL = "durationNanosTotal";
   private static final String TOTAL_BYTES = "totalBytes";
   private static final String COUNT_PER_SECOND = "countPerSecond";
   final Gson gson = new GsonBuilder().setPrettyPrinting().setLongSerializationPolicy(LongSerializationPolicy.DEFAULT).create();

   private static void serializePacketId(PacketIdentification $$0, JsonObject $$1) {
      $$1.addProperty("protocolId", $$0.protocolId());
      $$1.addProperty("packetId", $$0.packetId());
   }

   private static void serializeChunkId(ChunkIdentification $$0, JsonObject $$1) {
      $$1.addProperty("level", $$0.level());
      $$1.addProperty("dimension", $$0.dimension());
      $$1.addProperty("x", $$0.x());
      $$1.addProperty("z", $$0.z());
   }

   public String format(JfrStatsResult $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.addProperty("startedEpoch", $$0.recordingStarted().toEpochMilli());
      $$1.addProperty("endedEpoch", $$0.recordingEnded().toEpochMilli());
      $$1.addProperty("durationMs", $$0.recordingDuration().toMillis());
      Duration $$2 = $$0.worldCreationDuration();
      if ($$2 != null) {
         $$1.addProperty("worldGenDurationMs", $$2.toMillis());
      }

      $$1.add("heap", this.heap($$0.heapSummary()));
      $$1.add("cpuPercent", this.cpu($$0.cpuLoadStats()));
      $$1.add("network", this.network($$0));
      $$1.add("fileIO", this.fileIO($$0));
      $$1.add("fps", this.fps($$0.fps()));
      $$1.add("serverTick", this.serverTicks($$0.serverTickTimes()));
      $$1.add("threadAllocation", this.threadAllocations($$0.threadAllocationSummary()));
      $$1.add("chunkGen", this.chunkGen($$0.chunkGenSummary()));
      $$1.add("structureGen", this.structureGen($$0.structureGenStats()));
      return this.gson.toJson($$1);
   }

   private JsonElement heap(GcHeapStat.Summary $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.addProperty("allocationRateBytesPerSecond", $$0.allocationRateBytesPerSecond());
      $$1.addProperty("gcCount", $$0.totalGCs());
      $$1.addProperty("gcOverHeadPercent", $$0.gcOverHead());
      $$1.addProperty("gcTotalDurationMs", $$0.gcTotalDuration().toMillis());
      return $$1;
   }

   private JsonElement structureGen(List<StructureGenStat> $$0) {
      JsonObject $$1 = new JsonObject();
      Optional<TimedStatSummary<StructureGenStat>> $$2 = TimedStatSummary.summary($$0);
      if ($$2.isEmpty()) {
         return $$1;
      } else {
         TimedStatSummary<StructureGenStat> $$3 = $$2.get();
         JsonArray $$4 = new JsonArray();
         $$1.add("structure", $$4);
         $$0.stream().collect(Collectors.groupingBy(StructureGenStat::structureName)).forEach(($$3x, $$4x) -> {
            Optional<TimedStatSummary<StructureGenStat>> $$5 = TimedStatSummary.summary($$4x);
            if (!$$5.isEmpty()) {
               TimedStatSummary<StructureGenStat> $$6 = $$5.get();
               JsonObject $$7 = new JsonObject();
               $$4.add($$7);
               $$7.addProperty("name", $$3x);
               $$7.addProperty("count", $$6.count());
               $$7.addProperty("durationNanosTotal", $$6.totalDuration().toNanos());
               $$7.addProperty("durationNanosAvg", $$6.totalDuration().toNanos() / $$6.count());
               JsonObject $$8 = net.minecraft.util.Util.make(new JsonObject(), $$1xx -> $$7.add("durationNanosPercentiles", $$1xx));
               $$6.percentilesNanos().forEach(($$1xx, $$2xx) -> $$8.addProperty("p" + $$1xx, $$2xx));
               Function<StructureGenStat, JsonElement> $$9 = $$0xx -> {
                  JsonObject $$1xx = new JsonObject();
                  $$1xx.addProperty("durationNanos", $$0xx.duration().toNanos());
                  $$1xx.addProperty("chunkPosX", $$0xx.chunkPos().x);
                  $$1xx.addProperty("chunkPosZ", $$0xx.chunkPos().z);
                  $$1xx.addProperty("structureName", $$0xx.structureName());
                  $$1xx.addProperty("level", $$0xx.level());
                  $$1xx.addProperty("success", $$0xx.success());
                  return $$1xx;
               };
               $$1.add("fastest", $$9.apply($$3.fastest()));
               $$1.add("slowest", $$9.apply($$3.slowest()));
               $$1.add("secondSlowest", (JsonElement)($$3.secondSlowest() != null ? $$9.apply($$3.secondSlowest()) : JsonNull.INSTANCE));
            }
         });
         return $$1;
      }
   }

   private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> $$0) {
      JsonObject $$1 = new JsonObject();
      if ($$0.isEmpty()) {
         return $$1;
      } else {
         $$1.addProperty("durationNanosTotal", $$0.stream().mapToDouble($$0x -> ((TimedStatSummary)$$0x.getSecond()).totalDuration().toNanos()).sum());
         JsonArray $$2 = net.minecraft.util.Util.make(new JsonArray(), $$1x -> $$1.add("status", $$1x));

         for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> $$3 : $$0) {
            TimedStatSummary<ChunkGenStat> $$4 = (TimedStatSummary<ChunkGenStat>)$$3.getSecond();
            JsonObject $$5 = net.minecraft.util.Util.make(new JsonObject(), $$2::add);
            $$5.addProperty("state", ((ChunkStatus)$$3.getFirst()).toString());
            $$5.addProperty("count", $$4.count());
            $$5.addProperty("durationNanosTotal", $$4.totalDuration().toNanos());
            $$5.addProperty("durationNanosAvg", $$4.totalDuration().toNanos() / $$4.count());
            JsonObject $$6 = net.minecraft.util.Util.make(new JsonObject(), $$1x -> $$5.add("durationNanosPercentiles", $$1x));
            $$4.percentilesNanos().forEach(($$1x, $$2x) -> $$6.addProperty("p" + $$1x, $$2x));
            Function<ChunkGenStat, JsonElement> $$7 = $$0x -> {
               JsonObject $$1x = new JsonObject();
               $$1x.addProperty("durationNanos", $$0x.duration().toNanos());
               $$1x.addProperty("level", $$0x.level());
               $$1x.addProperty("chunkPosX", $$0x.chunkPos().x);
               $$1x.addProperty("chunkPosZ", $$0x.chunkPos().z);
               $$1x.addProperty("worldPosX", $$0x.worldPos().x());
               $$1x.addProperty("worldPosZ", $$0x.worldPos().z());
               return $$1x;
            };
            $$5.add("fastest", $$7.apply($$4.fastest()));
            $$5.add("slowest", $$7.apply($$4.slowest()));
            $$5.add("secondSlowest", (JsonElement)($$4.secondSlowest() != null ? $$7.apply($$4.secondSlowest()) : JsonNull.INSTANCE));
         }

         return $$1;
      }
   }

   private JsonElement threadAllocations(ThreadAllocationStat.Summary $$0) {
      JsonArray $$1 = new JsonArray();
      $$0.allocationsPerSecondByThread().forEach(($$1x, $$2) -> $$1.add(net.minecraft.util.Util.make(new JsonObject(), $$2x -> {
         $$2x.addProperty("thread", $$1x);
         $$2x.addProperty("bytesPerSecond", $$2);
      })));
      return $$1;
   }

   private JsonElement serverTicks(List<TickTimeStat> $$0) {
      if ($$0.isEmpty()) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject $$1 = new JsonObject();
         double[] $$2 = $$0.stream().mapToDouble($$0x -> $$0x.currentAverage().toNanos() / 1000000.0).toArray();
         DoubleSummaryStatistics $$3 = DoubleStream.of($$2).summaryStatistics();
         $$1.addProperty("minMs", $$3.getMin());
         $$1.addProperty("averageMs", $$3.getAverage());
         $$1.addProperty("maxMs", $$3.getMax());
         Map<Integer, Double> $$4 = Percentiles.evaluate($$2);
         $$4.forEach(($$1x, $$2x) -> $$1.addProperty("p" + $$1x, $$2x));
         return $$1;
      }
   }

   private JsonElement fps(List<FpsStat> $$0) {
      if ($$0.isEmpty()) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject $$1 = new JsonObject();
         int[] $$2 = $$0.stream().mapToInt(FpsStat::fps).toArray();
         IntSummaryStatistics $$3 = IntStream.of($$2).summaryStatistics();
         $$1.addProperty("minFPS", $$3.getMin());
         $$1.addProperty("averageFPS", $$3.getAverage());
         $$1.addProperty("maxFPS", $$3.getMax());
         Map<Integer, Double> $$4 = Percentiles.evaluate($$2);
         $$4.forEach(($$1x, $$2x) -> $$1.addProperty("p" + $$1x, $$2x));
         return $$1;
      }
   }

   private JsonElement fileIO(JfrStatsResult $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.add("write", this.fileIoSummary($$0.fileWrites()));
      $$1.add("read", this.fileIoSummary($$0.fileReads()));
      $$1.add("chunksRead", this.ioSummary($$0.readChunks(), JfrResultJsonSerializer::serializeChunkId));
      $$1.add("chunksWritten", this.ioSummary($$0.writtenChunks(), JfrResultJsonSerializer::serializeChunkId));
      return $$1;
   }

   private JsonElement fileIoSummary(FileIOStat.Summary $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.addProperty("totalBytes", $$0.totalBytes());
      $$1.addProperty("count", $$0.counts());
      $$1.addProperty("bytesPerSecond", $$0.bytesPerSecond());
      $$1.addProperty("countPerSecond", $$0.countsPerSecond());
      JsonArray $$2 = new JsonArray();
      $$1.add("topContributors", $$2);
      $$0.topTenContributorsByTotalBytes().forEach($$1x -> {
         JsonObject $$2x = new JsonObject();
         $$2.add($$2x);
         $$2x.addProperty("path", (String)$$1x.getFirst());
         $$2x.addProperty("totalBytes", (Number)$$1x.getSecond());
      });
      return $$1;
   }

   private JsonElement network(JfrStatsResult $$0) {
      JsonObject $$1 = new JsonObject();
      $$1.add("sent", this.ioSummary($$0.sentPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
      $$1.add("received", this.ioSummary($$0.receivedPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
      return $$1;
   }

   private <T> JsonElement ioSummary(IoSummary<T> $$0, BiConsumer<T, JsonObject> $$1) {
      JsonObject $$2 = new JsonObject();
      $$2.addProperty("totalBytes", $$0.getTotalSize());
      $$2.addProperty("count", $$0.getTotalCount());
      $$2.addProperty("bytesPerSecond", $$0.getSizePerSecond());
      $$2.addProperty("countPerSecond", $$0.getCountsPerSecond());
      JsonArray $$3 = new JsonArray();
      $$2.add("topContributors", $$3);
      $$0.largestSizeContributors().forEach($$2x -> {
         JsonObject $$3x = new JsonObject();
         $$3.add($$3x);
         T $$4 = (T)$$2x.getFirst();
         IoSummary.CountAndSize $$5 = (IoSummary.CountAndSize)$$2x.getSecond();
         $$1.accept($$4, $$3x);
         $$3x.addProperty("totalBytes", $$5.totalSize());
         $$3x.addProperty("count", $$5.totalCount());
         $$3x.addProperty("averageSize", $$5.averageSize());
      });
      return $$2;
   }

   private JsonElement cpu(List<CpuLoadStat> $$0) {
      JsonObject $$1 = new JsonObject();
      BiFunction<List<CpuLoadStat>, ToDoubleFunction<CpuLoadStat>, JsonObject> $$2 = ($$0x, $$1x) -> {
         JsonObject $$2x = new JsonObject();
         DoubleSummaryStatistics $$3 = $$0x.stream().mapToDouble($$1x).summaryStatistics();
         $$2x.addProperty("min", $$3.getMin());
         $$2x.addProperty("average", $$3.getAverage());
         $$2x.addProperty("max", $$3.getMax());
         return $$2x;
      };
      $$1.add("jvm", (JsonElement)$$2.apply($$0, CpuLoadStat::jvm));
      $$1.add("userJvm", (JsonElement)$$2.apply($$0, CpuLoadStat::userJvm));
      $$1.add("system", (JsonElement)$$2.apply($$0, CpuLoadStat::system));
      return $$1;
   }
}
