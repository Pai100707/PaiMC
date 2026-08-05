/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.shorts.ShortOpenHashSet
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jspecify.annotations.Nullable;

public class ChunkHolder
extends GenerationChunkHolder {
    public static final ChunkResult<LevelChunk> UNLOADED_LEVEL_CHUNK = ChunkResult.error("Unloaded level chunk");
    private static final CompletableFuture<ChunkResult<LevelChunk>> UNLOADED_LEVEL_CHUNK_FUTURE = CompletableFuture.completedFuture(UNLOADED_LEVEL_CHUNK);
    private final LevelHeightAccessor levelHeightAccessor;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private volatile CompletableFuture<ChunkResult<LevelChunk>> entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
    private int oldTicketLevel;
    private int ticketLevel;
    private int queueLevel;
    private boolean hasChangedSections;
    private final @Nullable ShortSet[] changedBlocksPerSection;
    private final BitSet blockChangedLightSectionFilter = new BitSet();
    private final BitSet skyChangedLightSectionFilter = new BitSet();
    private final LevelLightEngine lightEngine;
    private final LevelChangeListener onLevelChange;
    private final PlayerProvider playerProvider;
    private boolean wasAccessibleSinceLastSave;
    private CompletableFuture<?> pendingFullStateConfirmation = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> sendSync = CompletableFuture.completedFuture(null);
    private CompletableFuture<?> saveSync = CompletableFuture.completedFuture(null);

    public ChunkHolder(ChunkPos $$0, int $$1, LevelHeightAccessor $$2, LevelLightEngine $$3, LevelChangeListener $$4, PlayerProvider $$5) {
        super($$0);
        this.levelHeightAccessor = $$2;
        this.lightEngine = $$3;
        this.onLevelChange = $$4;
        this.playerProvider = $$5;
        this.ticketLevel = this.oldTicketLevel = ChunkLevel.MAX_LEVEL + 1;
        this.queueLevel = this.oldTicketLevel;
        this.setTicketLevel($$1);
        this.changedBlocksPerSection = new ShortSet[$$2.getSectionsCount()];
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getTickingChunkFuture() {
        return this.tickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getEntityTickingChunkFuture() {
        return this.entityTickingChunkFuture;
    }

    public CompletableFuture<ChunkResult<LevelChunk>> getFullChunkFuture() {
        return this.fullChunkFuture;
    }

    public @Nullable LevelChunk getTickingChunk() {
        return this.getTickingChunkFuture().getNow(UNLOADED_LEVEL_CHUNK).orElse(null);
    }

    public @Nullable LevelChunk getChunkToSend() {
        if (!this.sendSync.isDone()) {
            return null;
        }
        return this.getTickingChunk();
    }

    public CompletableFuture<?> getSendSyncFuture() {
        return this.sendSync;
    }

    public void addSendDependency(CompletableFuture<?> $$02) {
        this.sendSync = this.sendSync.isDone() ? $$02 : this.sendSync.thenCombine($$02, ($$0, $$1) -> null);
    }

    public CompletableFuture<?> getSaveSyncFuture() {
        return this.saveSync;
    }

    public boolean isReadyForSaving() {
        return this.saveSync.isDone();
    }

    @Override
    protected void addSaveDependency(CompletableFuture<?> $$02) {
        this.saveSync = this.saveSync.isDone() ? $$02 : this.saveSync.thenCombine($$02, ($$0, $$1) -> null);
    }

    public boolean blockChanged(BlockPos $$0) {
        LevelChunk $$1 = this.getTickingChunk();
        if ($$1 == null) {
            return false;
        }
        boolean $$2 = this.hasChangedSections;
        int $$3 = this.levelHeightAccessor.getSectionIndex($$0.getY());
        ShortSet $$4 = this.changedBlocksPerSection[$$3];
        if ($$4 == null) {
            this.hasChangedSections = true;
            this.changedBlocksPerSection[$$3] = $$4 = new ShortOpenHashSet();
        }
        $$4.add(SectionPos.sectionRelativePos($$0));
        return !$$2;
    }

    public boolean sectionLightChanged(LightLayer $$0, int $$1) {
        int $$7;
        ChunkAccess $$2 = this.getChunkIfPresent(ChunkStatus.INITIALIZE_LIGHT);
        if ($$2 == null) {
            return false;
        }
        $$2.markUnsaved();
        LevelChunk $$3 = this.getTickingChunk();
        if ($$3 == null) {
            return false;
        }
        int $$4 = this.lightEngine.getMinLightSection();
        int $$5 = this.lightEngine.getMaxLightSection();
        if ($$1 < $$4 || $$1 > $$5) {
            return false;
        }
        BitSet $$6 = $$0 == LightLayer.SKY ? this.skyChangedLightSectionFilter : this.blockChangedLightSectionFilter;
        if (!$$6.get($$7 = $$1 - $$4)) {
            $$6.set($$7);
            return true;
        }
        return false;
    }

    public boolean hasChangesToBroadcast() {
        return this.hasChangedSections || !this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty();
    }

    public void broadcastChanges(LevelChunk $$0) {
        if (!this.hasChangesToBroadcast()) {
            return;
        }
        Level $$1 = $$0.getLevel();
        if (!this.skyChangedLightSectionFilter.isEmpty() || !this.blockChangedLightSectionFilter.isEmpty()) {
            List<ServerPlayer> $$22 = this.playerProvider.getPlayers(this.pos, true);
            if (!$$22.isEmpty()) {
                ClientboundLightUpdatePacket $$32 = new ClientboundLightUpdatePacket($$0.getPos(), this.lightEngine, this.skyChangedLightSectionFilter, this.blockChangedLightSectionFilter);
                this.broadcast($$22, $$32);
            }
            this.skyChangedLightSectionFilter.clear();
            this.blockChangedLightSectionFilter.clear();
        }
        if (!this.hasChangedSections) {
            return;
        }
        List<ServerPlayer> $$4 = this.playerProvider.getPlayers(this.pos, false);
        for (int $$5 = 0; $$5 < this.changedBlocksPerSection.length; ++$$5) {
            ShortSet $$6 = this.changedBlocksPerSection[$$5];
            if ($$6 == null) continue;
            this.changedBlocksPerSection[$$5] = null;
            if ($$4.isEmpty()) continue;
            int $$7 = this.levelHeightAccessor.getSectionYFromSectionIndex($$5);
            SectionPos $$8 = SectionPos.of($$0.getPos(), $$7);
            if ($$6.size() == 1) {
                BlockPos $$9 = $$8.relativeToBlockPos($$6.iterator().nextShort());
                BlockState $$10 = $$1.getBlockState($$9);
                this.broadcast($$4, new ClientboundBlockUpdatePacket($$9, $$10));
                this.broadcastBlockEntityIfNeeded($$4, $$1, $$9, $$10);
                continue;
            }
            LevelChunkSection $$11 = $$0.getSection($$5);
            ClientboundSectionBlocksUpdatePacket $$12 = new ClientboundSectionBlocksUpdatePacket($$8, $$6, $$11);
            this.broadcast($$4, $$12);
            $$12.runUpdates(($$2, $$3) -> this.broadcastBlockEntityIfNeeded($$4, $$1, (BlockPos)$$2, (BlockState)$$3));
        }
        this.hasChangedSections = false;
    }

    private void broadcastBlockEntityIfNeeded(List<ServerPlayer> $$0, Level $$1, BlockPos $$2, BlockState $$3) {
        if ($$3.hasBlockEntity()) {
            this.broadcastBlockEntity($$0, $$1, $$2);
        }
    }

    private void broadcastBlockEntity(List<ServerPlayer> $$0, Level $$1, BlockPos $$2) {
        Packet<ClientGamePacketListener> $$4;
        BlockEntity $$3 = $$1.getBlockEntity($$2);
        if ($$3 != null && ($$4 = $$3.getUpdatePacket()) != null) {
            this.broadcast($$0, $$4);
        }
    }

    private void broadcast(List<ServerPlayer> $$0, Packet<?> $$12) {
        $$0.forEach($$1 -> $$1.connection.send($$12));
    }

    @Override
    public int getTicketLevel() {
        return this.ticketLevel;
    }

    @Override
    public int getQueueLevel() {
        return this.queueLevel;
    }

    private void setQueueLevel(int $$0) {
        this.queueLevel = $$0;
    }

    public void setTicketLevel(int $$0) {
        this.ticketLevel = $$0;
    }

    private void scheduleFullChunkPromotion(ChunkMap $$0, CompletableFuture<ChunkResult<LevelChunk>> $$1, Executor $$2, FullChunkStatus $$3) {
        this.pendingFullStateConfirmation.cancel(false);
        CompletableFuture $$4 = new CompletableFuture();
        $$4.thenRunAsync(() -> $$0.onFullChunkStatusChange(this.pos, $$3), $$2);
        this.pendingFullStateConfirmation = $$4;
        $$1.thenAccept($$12 -> $$12.ifSuccess($$1 -> $$4.complete(null)));
    }

    private void demoteFullChunk(ChunkMap $$0, FullChunkStatus $$1) {
        this.pendingFullStateConfirmation.cancel(false);
        $$0.onFullChunkStatusChange(this.pos, $$1);
    }

    protected void updateFutures(ChunkMap $$0, Executor $$1) {
        FullChunkStatus $$2 = ChunkLevel.fullStatus(this.oldTicketLevel);
        FullChunkStatus $$3 = ChunkLevel.fullStatus(this.ticketLevel);
        boolean $$4 = $$2.isOrAfter(FullChunkStatus.FULL);
        boolean $$5 = $$3.isOrAfter(FullChunkStatus.FULL);
        this.wasAccessibleSinceLastSave |= $$5;
        if (!$$4 && $$5) {
            this.fullChunkFuture = $$0.prepareAccessibleChunk(this);
            this.scheduleFullChunkPromotion($$0, this.fullChunkFuture, $$1, FullChunkStatus.FULL);
            this.addSaveDependency(this.fullChunkFuture);
        }
        if ($$4 && !$$5) {
            this.fullChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.fullChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean $$6 = $$2.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        boolean $$7 = $$3.isOrAfter(FullChunkStatus.BLOCK_TICKING);
        if (!$$6 && $$7) {
            this.tickingChunkFuture = $$0.prepareTickingChunk(this);
            this.scheduleFullChunkPromotion($$0, this.tickingChunkFuture, $$1, FullChunkStatus.BLOCK_TICKING);
            this.addSaveDependency(this.tickingChunkFuture);
        }
        if ($$6 && !$$7) {
            this.tickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.tickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        boolean $$8 = $$2.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        boolean $$9 = $$3.isOrAfter(FullChunkStatus.ENTITY_TICKING);
        if (!$$8 && $$9) {
            if (this.entityTickingChunkFuture != UNLOADED_LEVEL_CHUNK_FUTURE) {
                throw Util.pauseInIde(new IllegalStateException());
            }
            this.entityTickingChunkFuture = $$0.prepareEntityTickingChunk(this);
            this.scheduleFullChunkPromotion($$0, this.entityTickingChunkFuture, $$1, FullChunkStatus.ENTITY_TICKING);
            this.addSaveDependency(this.entityTickingChunkFuture);
        }
        if ($$8 && !$$9) {
            this.entityTickingChunkFuture.complete(UNLOADED_LEVEL_CHUNK);
            this.entityTickingChunkFuture = UNLOADED_LEVEL_CHUNK_FUTURE;
        }
        if (!$$3.isOrAfter($$2)) {
            this.demoteFullChunk($$0, $$3);
        }
        this.onLevelChange.onLevelChange(this.pos, this::getQueueLevel, this.ticketLevel, this::setQueueLevel);
        this.oldTicketLevel = this.ticketLevel;
    }

    public boolean wasAccessibleSinceLastSave() {
        return this.wasAccessibleSinceLastSave;
    }

    public void refreshAccessibility() {
        this.wasAccessibleSinceLastSave = ChunkLevel.fullStatus(this.ticketLevel).isOrAfter(FullChunkStatus.FULL);
    }

    @FunctionalInterface
    public static interface LevelChangeListener {
        public void onLevelChange(ChunkPos var1, IntSupplier var2, int var3, IntConsumer var4);
    }

    public static interface PlayerProvider {
        public List<ServerPlayer> getPlayers(ChunkPos var1, boolean var2);
    }
}

