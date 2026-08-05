/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonNull
 *  com.google.gson.JsonObject
 *  com.google.gson.LongSerializationPolicy
 *  com.mojang.datafixers.util.Pair
 */
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
import net.minecraft.util.Util;
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
        $$1.addProperty("x", (Number)$$0.x());
        $$1.addProperty("z", (Number)$$0.z());
    }

    public String format(JfrStatsResult $$0) {
        JsonObject $$1 = new JsonObject();
        $$1.addProperty("startedEpoch", (Number)$$0.recordingStarted().toEpochMilli());
        $$1.addProperty("endedEpoch", (Number)$$0.recordingEnded().toEpochMilli());
        $$1.addProperty("durationMs", (Number)$$0.recordingDuration().toMillis());
        Duration $$2 = $$0.worldCreationDuration();
        if ($$2 != null) {
            $$1.addProperty("worldGenDurationMs", (Number)$$2.toMillis());
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
        return this.gson.toJson((JsonElement)$$1);
    }

    private JsonElement heap(GcHeapStat.Summary $$0) {
        JsonObject $$1 = new JsonObject();
        $$1.addProperty("allocationRateBytesPerSecond", (Number)$$0.allocationRateBytesPerSecond());
        $$1.addProperty("gcCount", (Number)$$0.totalGCs());
        $$1.addProperty("gcOverHeadPercent", (Number)Float.valueOf($$0.gcOverHead()));
        $$1.addProperty("gcTotalDurationMs", (Number)$$0.gcTotalDuration().toMillis());
        return $$1;
    }

    private JsonElement structureGen(List<StructureGenStat> $$0) {
        JsonObject $$1 = new JsonObject();
        Optional<TimedStatSummary<StructureGenStat>> $$2 = TimedStatSummary.summary($$0);
        if ($$2.isEmpty()) {
            return $$1;
        }
        TimedStatSummary<StructureGenStat> $$32 = $$2.get();
        JsonArray $$42 = new JsonArray();
        $$1.add("structure", (JsonElement)$$42);
        $$0.stream().collect(Collectors.groupingBy(StructureGenStat::structureName)).forEach(($$3, $$4) -> {
            Optional $$5 = TimedStatSummary.summary($$4);
            if ($$5.isEmpty()) {
                return;
            }
            TimedStatSummary $$6 = $$5.get();
            JsonObject $$7 = new JsonObject();
            $$42.add((JsonElement)$$7);
            $$7.addProperty("name", $$3);
            $$7.addProperty(COUNT, (Number)$$6.count());
            $$7.addProperty(DURATION_NANOS_TOTAL, (Number)$$6.totalDuration().toNanos());
            $$7.addProperty("durationNanosAvg", (Number)($$6.totalDuration().toNanos() / (long)$$6.count()));
            JsonObject $$8 = Util.make(new JsonObject(), $$1 -> $$7.add("durationNanosPercentiles", (JsonElement)$$1));
            $$6.percentilesNanos().forEach(($$1, $$2) -> $$8.addProperty("p" + $$1, (Number)$$2));
            Function<StructureGenStat, JsonElement> $$9 = $$0 -> {
                JsonObject $$1 = new JsonObject();
                $$1.addProperty("durationNanos", (Number)$$0.duration().toNanos());
                $$1.addProperty("chunkPosX", (Number)$$0.chunkPos().x);
                $$1.addProperty("chunkPosZ", (Number)$$0.chunkPos().z);
                $$1.addProperty("structureName", $$0.structureName());
                $$1.addProperty("level", $$0.level());
                $$1.addProperty("success", Boolean.valueOf($$0.success()));
                return $$1;
            };
            $$1.add("fastest", $$9.apply((StructureGenStat)$$32.fastest()));
            $$1.add("slowest", $$9.apply((StructureGenStat)$$32.slowest()));
            $$1.add("secondSlowest", (JsonElement)($$32.secondSlowest() != null ? $$9.apply((StructureGenStat)$$32.secondSlowest()) : JsonNull.INSTANCE));
        });
        return $$1;
    }

    private JsonElement chunkGen(List<Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>>> $$02) {
        JsonObject $$12 = new JsonObject();
        if ($$02.isEmpty()) {
            return $$12;
        }
        $$12.addProperty(DURATION_NANOS_TOTAL, (Number)$$02.stream().mapToDouble($$0 -> ((TimedStatSummary)$$0.getSecond()).totalDuration().toNanos()).sum());
        JsonArray $$22 = Util.make(new JsonArray(), $$1 -> $$12.add("status", (JsonElement)$$1));
        for (Pair<ChunkStatus, TimedStatSummary<ChunkGenStat>> $$3 : $$02) {
            TimedStatSummary $$4 = (TimedStatSummary)$$3.getSecond();
            JsonObject $$5 = Util.make(new JsonObject(), arg_0 -> ((JsonArray)$$22).add(arg_0));
            $$5.addProperty("state", ((ChunkStatus)$$3.getFirst()).toString());
            $$5.addProperty(COUNT, (Number)$$4.count());
            $$5.addProperty(DURATION_NANOS_TOTAL, (Number)$$4.totalDuration().toNanos());
            $$5.addProperty("durationNanosAvg", (Number)($$4.totalDuration().toNanos() / (long)$$4.count()));
            JsonObject $$6 = Util.make(new JsonObject(), $$1 -> $$5.add("durationNanosPercentiles", (JsonElement)$$1));
            $$4.percentilesNanos().forEach(($$1, $$2) -> $$6.addProperty("p" + $$1, (Number)$$2));
            Function<ChunkGenStat, JsonElement> $$7 = $$0 -> {
                JsonObject $$1 = new JsonObject();
                $$1.addProperty("durationNanos", (Number)$$0.duration().toNanos());
                $$1.addProperty("level", $$0.level());
                $$1.addProperty("chunkPosX", (Number)$$0.chunkPos().x);
                $$1.addProperty("chunkPosZ", (Number)$$0.chunkPos().z);
                $$1.addProperty("worldPosX", (Number)$$0.worldPos().x());
                $$1.addProperty("worldPosZ", (Number)$$0.worldPos().z());
                return $$1;
            };
            $$5.add("fastest", $$7.apply((ChunkGenStat)$$4.fastest()));
            $$5.add("slowest", $$7.apply((ChunkGenStat)$$4.slowest()));
            $$5.add("secondSlowest", (JsonElement)($$4.secondSlowest() != null ? $$7.apply((ChunkGenStat)$$4.secondSlowest()) : JsonNull.INSTANCE));
        }
        return $$12;
    }

    private JsonElement threadAllocations(ThreadAllocationStat.Summary $$0) {
        JsonArray $$12 = new JsonArray();
        $$0.allocationsPerSecondByThread().forEach(($$1, $$22) -> $$12.add((JsonElement)Util.make(new JsonObject(), $$2 -> {
            $$2.addProperty("thread", $$1);
            $$2.addProperty(BYTES_PER_SECOND, (Number)$$22);
        })));
        return $$12;
    }

    private JsonElement serverTicks(List<TickTimeStat> $$02) {
        if ($$02.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject $$12 = new JsonObject();
        double[] $$22 = $$02.stream().mapToDouble($$0 -> (double)$$0.currentAverage().toNanos() / 1000000.0).toArray();
        DoubleSummaryStatistics $$3 = DoubleStream.of($$22).summaryStatistics();
        $$12.addProperty("minMs", (Number)$$3.getMin());
        $$12.addProperty("averageMs", (Number)$$3.getAverage());
        $$12.addProperty("maxMs", (Number)$$3.getMax());
        Map<Integer, Double> $$4 = Percentiles.evaluate($$22);
        $$4.forEach(($$1, $$2) -> $$12.addProperty("p" + $$1, (Number)$$2));
        return $$12;
    }

    private JsonElement fps(List<FpsStat> $$0) {
        if ($$0.isEmpty()) {
            return JsonNull.INSTANCE;
        }
        JsonObject $$12 = new JsonObject();
        int[] $$22 = $$0.stream().mapToInt(FpsStat::fps).toArray();
        IntSummaryStatistics $$3 = IntStream.of($$22).summaryStatistics();
        $$12.addProperty("minFPS", (Number)$$3.getMin());
        $$12.addProperty("averageFPS", (Number)$$3.getAverage());
        $$12.addProperty("maxFPS", (Number)$$3.getMax());
        Map<Integer, Double> $$4 = Percentiles.evaluate($$22);
        $$4.forEach(($$1, $$2) -> $$12.addProperty("p" + $$1, (Number)$$2));
        return $$12;
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
        JsonObject $$12 = new JsonObject();
        $$12.addProperty(TOTAL_BYTES, (Number)$$0.totalBytes());
        $$12.addProperty(COUNT, (Number)$$0.counts());
        $$12.addProperty(BYTES_PER_SECOND, (Number)$$0.bytesPerSecond());
        $$12.addProperty(COUNT_PER_SECOND, (Number)$$0.countsPerSecond());
        JsonArray $$2 = new JsonArray();
        $$12.add("topContributors", (JsonElement)$$2);
        $$0.topTenContributorsByTotalBytes().forEach($$1 -> {
            JsonObject $$2 = new JsonObject();
            $$2.add((JsonElement)$$2);
            $$2.addProperty("path", (String)$$1.getFirst());
            $$2.addProperty(TOTAL_BYTES, (Number)$$1.getSecond());
        });
        return $$12;
    }

    private JsonElement network(JfrStatsResult $$0) {
        JsonObject $$1 = new JsonObject();
        $$1.add("sent", this.ioSummary($$0.sentPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        $$1.add("received", this.ioSummary($$0.receivedPacketsSummary(), JfrResultJsonSerializer::serializePacketId));
        return $$1;
    }

    private <T> JsonElement ioSummary(IoSummary<T> $$0, BiConsumer<T, JsonObject> $$1) {
        JsonObject $$22 = new JsonObject();
        $$22.addProperty(TOTAL_BYTES, (Number)$$0.getTotalSize());
        $$22.addProperty(COUNT, (Number)$$0.getTotalCount());
        $$22.addProperty(BYTES_PER_SECOND, (Number)$$0.getSizePerSecond());
        $$22.addProperty(COUNT_PER_SECOND, (Number)$$0.getCountsPerSecond());
        JsonArray $$3 = new JsonArray();
        $$22.add("topContributors", (JsonElement)$$3);
        $$0.largestSizeContributors().forEach($$2 -> {
            JsonObject $$3 = new JsonObject();
            $$3.add((JsonElement)$$3);
            Object $$4 = $$2.getFirst();
            IoSummary.CountAndSize $$5 = (IoSummary.CountAndSize)$$2.getSecond();
            $$1.accept($$4, $$3);
            $$3.addProperty(TOTAL_BYTES, (Number)$$5.totalSize());
            $$3.addProperty(COUNT, (Number)$$5.totalCount());
            $$3.addProperty("averageSize", (Number)Float.valueOf($$5.averageSize()));
        });
        return $$22;
    }

    private JsonElement cpu(List<CpuLoadStat> $$02) {
        JsonObject $$12 = new JsonObject();
        BiFunction<List, ToDoubleFunction, JsonObject> $$2 = ($$0, $$1) -> {
            JsonObject $$2 = new JsonObject();
            DoubleSummaryStatistics $$3 = $$0.stream().mapToDouble($$1).summaryStatistics();
            $$2.addProperty("min", (Number)$$3.getMin());
            $$2.addProperty("average", (Number)$$3.getAverage());
            $$2.addProperty("max", (Number)$$3.getMax());
            return $$2;
        };
        $$12.add("jvm", (JsonElement)$$2.apply($$02, CpuLoadStat::jvm));
        $$12.add("userJvm", (JsonElement)$$2.apply($$02, CpuLoadStat::userJvm));
        $$12.add("system", (JsonElement)$$2.apply($$02, CpuLoadStat::system));
        return $$12;
    }
}

