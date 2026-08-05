/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonIOException
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class ServerStatsCounter
extends StatsCounter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Map<Stat<?>, Integer>> STATS_CODEC = Codec.dispatchedMap(BuiltInRegistries.STAT_TYPE.byNameCodec(), Util.memoize(ServerStatsCounter::createTypedStatsCodec)).xmap($$0 -> {
        HashMap $$12 = new HashMap();
        $$0.forEach(($$1, $$2) -> $$12.putAll($$2));
        return $$12;
    }, $$02 -> $$02.entrySet().stream().collect(Collectors.groupingBy($$0 -> ((Stat)$$0.getKey()).getType(), Util.toMap())));
    private final Path file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    private static <T> Codec<Map<Stat<?>, Integer>> createTypedStatsCodec(StatType<T> $$0) {
        Codec<T> $$12 = $$0.getRegistry().byNameCodec();
        Codec $$2 = $$12.flatComapMap($$0::get, $$1 -> {
            if ($$1.getType() == $$0) {
                return DataResult.success($$1.getValue());
            }
            return DataResult.error(() -> "Expected type " + String.valueOf($$0) + ", but got " + String.valueOf($$1.getType()));
        });
        return Codec.unboundedMap((Codec)$$2, (Codec)Codec.INT);
    }

    public ServerStatsCounter(MinecraftServer $$0, Path $$1) {
        this.file = $$1;
        if (Files.isRegularFile($$1, new LinkOption[0])) {
            try (BufferedReader $$2 = Files.newBufferedReader($$1, StandardCharsets.UTF_8);){
                JsonElement $$3 = StrictJsonParser.parse($$2);
                this.parse($$0.getFixerUpper(), $$3);
            }
            catch (IOException $$4) {
                LOGGER.error("Couldn't read statistics file {}", (Object)$$1, (Object)$$4);
            }
            catch (JsonParseException $$5) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)$$1, (Object)$$5);
            }
        }
    }

    public void save() {
        try {
            FileUtil.createDirectoriesSafe(this.file.getParent());
            try (BufferedWriter $$0 = Files.newBufferedWriter(this.file, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson(this.toJson(), GSON.newJsonWriter((Writer)$$0));
            }
        }
        catch (JsonIOException | IOException $$1) {
            LOGGER.error("Couldn't save stats to {}", (Object)this.file, (Object)$$1);
        }
    }

    @Override
    public void setValue(Player $$0, Stat<?> $$1, int $$2) {
        super.setValue($$0, $$1, $$2);
        this.dirty.add($$1);
    }

    private Set<Stat<?>> getDirty() {
        HashSet $$0 = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return $$0;
    }

    public void parse(DataFixer $$02, JsonElement $$1) {
        Dynamic $$2 = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)$$1);
        $$2 = DataFixTypes.STATS.updateToCurrentVersion($$02, $$2, NbtUtils.getDataVersion($$2, 1343));
        this.stats.putAll(STATS_CODEC.parse($$2.get("stats").orElseEmptyMap()).resultOrPartial($$0 -> LOGGER.error("Failed to parse statistics for {}: {}", (Object)this.file, $$0)).orElse(Map.of()));
    }

    protected JsonElement toJson() {
        JsonObject $$0 = new JsonObject();
        $$0.add("stats", (JsonElement)STATS_CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this.stats).getOrThrow());
        $$0.addProperty("DataVersion", (Number)SharedConstants.getCurrentVersion().dataVersion().version());
        return $$0;
    }

    public void markAllDirty() {
        this.dirty.addAll((Collection<Stat<?>>)this.stats.keySet());
    }

    public void sendStats(ServerPlayer $$0) {
        Object2IntOpenHashMap $$1 = new Object2IntOpenHashMap();
        for (Stat<?> $$2 : this.getDirty()) {
            $$1.put($$2, this.getValue($$2));
        }
        $$0.connection.send(new ClientboundAwardStatsPacket((Object2IntMap<Stat<?>>)$$1));
    }
}

