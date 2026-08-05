/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network.config;

import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkLoadCounter;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PrepareSpawnTask
implements ConfigurationTask {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final ConfigurationTask.Type TYPE = new ConfigurationTask.Type("prepare_spawn");
    public static final int PREPARE_CHUNK_RADIUS = 3;
    final MinecraftServer server;
    final NameAndId nameAndId;
    final LevelLoadListener loadListener;
    private @Nullable State state;

    public PrepareSpawnTask(MinecraftServer $$0, NameAndId $$1) {
        this.server = $$0;
        this.nameAndId = $$1;
        this.loadListener = $$0.getLevelLoadListener();
    }

    @Override
    public void start(Consumer<Packet<?>> $$02) {
        try (ProblemReporter.ScopedCollector $$12 = new ProblemReporter.ScopedCollector(LOGGER);){
            Optional<ValueInput> $$2 = this.server.getPlayerList().loadPlayerData(this.nameAndId).map($$1 -> TagValueInput.create((ProblemReporter)$$12, (HolderLookup.Provider)this.server.registryAccess(), $$1));
            ServerPlayer.SavedPosition $$3 = $$2.flatMap($$0 -> $$0.read(ServerPlayer.SavedPosition.MAP_CODEC)).orElse(ServerPlayer.SavedPosition.EMPTY);
            LevelData.RespawnData $$4 = this.server.getWorldData().overworldData().getRespawnData();
            ServerLevel $$5 = $$3.dimension().map(this.server::getLevel).orElseGet(() -> {
                ServerLevel $$1 = this.server.getLevel($$4.dimension());
                return $$1 != null ? $$1 : this.server.overworld();
            });
            CompletableFuture $$6 = $$3.position().map(CompletableFuture::completedFuture).orElseGet(() -> PlayerSpawnFinder.findSpawn($$5, $$4.pos()));
            Vec2 $$7 = $$3.rotation().orElse(new Vec2($$4.yaw(), $$4.pitch()));
            this.state = new Preparing($$5, $$6, $$7);
        }
    }

    @Override
    public boolean tick() {
        State state = this.state;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Preparing.class, Ready.class}, (Object)state, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                Preparing $$0 = (Preparing)state;
                Ready $$1 = $$0.tick();
                if ($$1 != null) {
                    this.state = $$1;
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                Ready $$2 = (Ready)state;
                yield true;
            }
            case -1 -> false;
        };
    }

    public ServerPlayer spawnPlayer(Connection $$0, CommonListenerCookie $$1) {
        State state = this.state;
        if (state instanceof Ready) {
            Ready $$2 = (Ready)state;
            return $$2.spawn($$0, $$1);
        }
        throw new IllegalStateException("Player spawn was not ready");
    }

    public void keepAlive() {
        State state = this.state;
        if (state instanceof Ready) {
            Ready $$0 = (Ready)state;
            $$0.keepAlive();
        }
    }

    public void close() {
        State state = this.state;
        if (state instanceof Preparing) {
            Preparing $$0 = (Preparing)state;
            $$0.cancel();
        }
        this.state = null;
    }

    @Override
    public ConfigurationTask.Type type() {
        return TYPE;
    }

    final class Preparing
    implements State {
        private final ServerLevel spawnLevel;
        private final CompletableFuture<Vec3> spawnPosition;
        private final Vec2 spawnAngle;
        private @Nullable CompletableFuture<?> chunkLoadFuture;
        private final ChunkLoadCounter chunkLoadCounter = new ChunkLoadCounter();

        Preparing(ServerLevel $$0, CompletableFuture<Vec3> $$1, Vec2 $$2) {
            this.spawnLevel = $$0;
            this.spawnPosition = $$1;
            this.spawnAngle = $$2;
        }

        public void cancel() {
            this.spawnPosition.cancel(false);
        }

        public @Nullable Ready tick() {
            if (!this.spawnPosition.isDone()) {
                return null;
            }
            Vec3 $$0 = this.spawnPosition.join();
            if (this.chunkLoadFuture == null) {
                ChunkPos $$1 = new ChunkPos(BlockPos.containing($$0));
                this.chunkLoadCounter.track(this.spawnLevel, () -> {
                    this.chunkLoadFuture = this.spawnLevel.getChunkSource().addTicketAndLoadWithRadius(TicketType.PLAYER_SPAWN, $$1, 3);
                });
                PrepareSpawnTask.this.loadListener.start(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS, this.chunkLoadCounter.totalChunks());
                PrepareSpawnTask.this.loadListener.updateFocus(this.spawnLevel.dimension(), $$1);
            }
            PrepareSpawnTask.this.loadListener.update(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS, this.chunkLoadCounter.readyChunks(), this.chunkLoadCounter.totalChunks());
            if (!this.chunkLoadFuture.isDone()) {
                return null;
            }
            PrepareSpawnTask.this.loadListener.finish(LevelLoadListener.Stage.LOAD_PLAYER_CHUNKS);
            return new Ready(this.spawnLevel, $$0, this.spawnAngle);
        }
    }

    static sealed interface State
    permits Preparing, Ready {
    }

    final class Ready
    implements State {
        private final ServerLevel spawnLevel;
        private final Vec3 spawnPosition;
        private final Vec2 spawnAngle;

        Ready(ServerLevel $$0, Vec3 $$1, Vec2 $$2) {
            this.spawnLevel = $$0;
            this.spawnPosition = $$1;
            this.spawnAngle = $$2;
        }

        public void keepAlive() {
            this.spawnLevel.getChunkSource().addTicketWithRadius(TicketType.PLAYER_SPAWN, new ChunkPos(BlockPos.containing(this.spawnPosition)), 3);
        }

        public ServerPlayer spawn(Connection $$0, CommonListenerCookie $$12) {
            ChunkPos $$2 = new ChunkPos(BlockPos.containing(this.spawnPosition));
            this.spawnLevel.waitForEntities($$2, 3);
            ServerPlayer $$3 = new ServerPlayer(PrepareSpawnTask.this.server, this.spawnLevel, $$12.gameProfile(), $$12.clientInformation());
            try (ProblemReporter.ScopedCollector $$4 = new ProblemReporter.ScopedCollector($$3.problemPath(), LOGGER);){
                Optional<ValueInput> $$5 = PrepareSpawnTask.this.server.getPlayerList().loadPlayerData(PrepareSpawnTask.this.nameAndId).map($$1 -> TagValueInput.create((ProblemReporter)$$4, (HolderLookup.Provider)PrepareSpawnTask.this.server.registryAccess(), $$1));
                $$5.ifPresent($$3::load);
                $$3.snapTo(this.spawnPosition, this.spawnAngle.x, this.spawnAngle.y);
                PrepareSpawnTask.this.server.getPlayerList().placeNewPlayer($$0, $$3, $$12);
                $$5.ifPresent($$1 -> {
                    $$3.loadAndSpawnEnderPearls((ValueInput)$$1);
                    $$3.loadAndSpawnParentVehicle((ValueInput)$$1);
                });
                ServerPlayer serverPlayer = $$3;
                return serverPlayer;
            }
        }
    }
}

