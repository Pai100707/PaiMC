/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PlayerSpawnFinder {
    private static final EntityDimensions PLAYER_DIMENSIONS = EntityType.PLAYER.getDimensions();
    private static final int ABSOLUTE_MAX_ATTEMPTS = 1024;
    private final ServerLevel level;
    private final BlockPos spawnSuggestion;
    private final int radius;
    private final int candidateCount;
    private final int coprime;
    private final int offset;
    private int nextCandidateIndex;
    private final CompletableFuture<Vec3> finishedFuture = new CompletableFuture();

    private PlayerSpawnFinder(ServerLevel $$0, BlockPos $$1, int $$2) {
        this.level = $$0;
        this.spawnSuggestion = $$1;
        this.radius = $$2;
        long $$3 = (long)$$2 * 2L + 1L;
        this.candidateCount = (int)Math.min(1024L, $$3 * $$3);
        this.coprime = PlayerSpawnFinder.getCoprime(this.candidateCount);
        this.offset = RandomSource.create().nextInt(this.candidateCount);
    }

    public static CompletableFuture<Vec3> findSpawn(ServerLevel $$0, BlockPos $$1) {
        if (!$$0.dimensionType().hasSkyLight() || $$0.getServer().getWorldData().getGameType() == GameType.ADVENTURE) {
            return CompletableFuture.completedFuture(PlayerSpawnFinder.fixupSpawnHeight($$0, $$1));
        }
        int $$2 = Math.max(0, $$0.getGameRules().get(GameRules.RESPAWN_RADIUS));
        int $$3 = Mth.floor($$0.getWorldBorder().getDistanceToBorder($$1.getX(), $$1.getZ()));
        if ($$3 < $$2) {
            $$2 = $$3;
        }
        if ($$3 <= 1) {
            $$2 = 1;
        }
        PlayerSpawnFinder $$4 = new PlayerSpawnFinder($$0, $$1, $$2);
        $$4.scheduleNext();
        return $$4.finishedFuture;
    }

    private void scheduleNext() {
        int $$0;
        if (($$0 = this.nextCandidateIndex++) < this.candidateCount) {
            int $$1 = (this.offset + this.coprime * $$0) % this.candidateCount;
            int $$2 = $$1 % (this.radius * 2 + 1);
            int $$3 = $$1 / (this.radius * 2 + 1);
            int $$4 = this.spawnSuggestion.getX() + $$2 - this.radius;
            int $$5 = this.spawnSuggestion.getZ() + $$3 - this.radius;
            this.scheduleCandidate($$4, $$5, $$0, () -> {
                BlockPos $$2 = PlayerSpawnFinder.getOverworldRespawnPos(this.level, $$4, $$5);
                if ($$2 != null && PlayerSpawnFinder.noCollisionNoLiquid(this.level, $$2)) {
                    return Optional.of(Vec3.atBottomCenterOf($$2));
                }
                return Optional.empty();
            });
        } else {
            this.scheduleCandidate(this.spawnSuggestion.getX(), this.spawnSuggestion.getZ(), $$0, () -> Optional.of(PlayerSpawnFinder.fixupSpawnHeight(this.level, this.spawnSuggestion)));
        }
    }

    private static Vec3 fixupSpawnHeight(CollisionGetter $$0, BlockPos $$1) {
        BlockPos.MutableBlockPos $$2 = $$1.mutable();
        while (!PlayerSpawnFinder.noCollisionNoLiquid($$0, $$2) && $$2.getY() < $$0.getMaxY()) {
            $$2.move(Direction.UP);
        }
        $$2.move(Direction.DOWN);
        while (PlayerSpawnFinder.noCollisionNoLiquid($$0, $$2) && $$2.getY() > $$0.getMinY()) {
            $$2.move(Direction.DOWN);
        }
        $$2.move(Direction.UP);
        return Vec3.atBottomCenterOf($$2);
    }

    private static boolean noCollisionNoLiquid(CollisionGetter $$0, BlockPos $$1) {
        return $$0.noCollision(null, PLAYER_DIMENSIONS.makeBoundingBox($$1.getBottomCenter()), true);
    }

    private static int getCoprime(int $$0) {
        return $$0 <= 16 ? $$0 - 1 : 17;
    }

    private void scheduleCandidate(int $$0, int $$1, int $$2, Supplier<Optional<Vec3>> $$3) {
        if (this.finishedFuture.isDone()) {
            return;
        }
        int $$42 = SectionPos.blockToSectionCoord($$0);
        int $$52 = SectionPos.blockToSectionCoord($$1);
        this.level.getChunkSource().addTicketAndLoadWithRadius(TicketType.SPAWN_SEARCH, new ChunkPos($$42, $$52), 0).whenCompleteAsync(($$4, $$5) -> {
            if ($$5 == null) {
                try {
                    Optional $$6 = (Optional)$$3.get();
                    if ($$6.isPresent()) {
                        this.finishedFuture.complete((Vec3)$$6.get());
                    } else {
                        this.scheduleNext();
                    }
                }
                catch (Throwable $$7) {
                    $$5 = $$7;
                }
            }
            if ($$5 != null) {
                CrashReport $$8 = CrashReport.forThrowable($$5, "Searching for spawn");
                CrashReportCategory $$9 = $$8.addCategory("Spawn Lookup");
                $$9.setDetail("Origin", this.spawnSuggestion::toString);
                $$9.setDetail("Radius", () -> Integer.toString(this.radius));
                $$9.setDetail("Candidate", () -> "[" + $$0 + "," + $$1 + "]");
                $$9.setDetail("Progress", () -> $$2 + " out of " + this.candidateCount);
                this.finishedFuture.completeExceptionally(new ReportedException($$8));
            }
        }, (Executor)this.level.getServer());
    }

    protected static @Nullable BlockPos getOverworldRespawnPos(ServerLevel $$0, int $$1, int $$2) {
        int $$5;
        boolean $$3 = $$0.dimensionType().hasCeiling();
        LevelChunk $$4 = $$0.getChunk(SectionPos.blockToSectionCoord($$1), SectionPos.blockToSectionCoord($$2));
        int n = $$5 = $$3 ? $$0.getChunkSource().getGenerator().getSpawnHeight($$0) : $$4.getHeight(Heightmap.Types.MOTION_BLOCKING, $$1 & 0xF, $$2 & 0xF);
        if ($$5 < $$0.getMinY()) {
            return null;
        }
        int $$6 = $$4.getHeight(Heightmap.Types.WORLD_SURFACE, $$1 & 0xF, $$2 & 0xF);
        if ($$6 <= $$5 && $$6 > $$4.getHeight(Heightmap.Types.OCEAN_FLOOR, $$1 & 0xF, $$2 & 0xF)) {
            return null;
        }
        BlockPos.MutableBlockPos $$7 = new BlockPos.MutableBlockPos();
        for (int $$8 = $$5 + 1; $$8 >= $$0.getMinY(); --$$8) {
            $$7.set($$1, $$8, $$2);
            BlockState $$9 = $$0.getBlockState($$7);
            if (!$$9.getFluidState().isEmpty()) break;
            if (!Block.isFaceFull($$9.getCollisionShape($$0, $$7), Direction.UP)) continue;
            return ((BlockPos)$$7.above()).immutable();
        }
        return null;
    }

    public static @Nullable BlockPos getSpawnPosInChunk(ServerLevel $$0, ChunkPos $$1) {
        if (SharedConstants.debugVoidTerrain($$1)) {
            return null;
        }
        for (int $$2 = $$1.getMinBlockX(); $$2 <= $$1.getMaxBlockX(); ++$$2) {
            for (int $$3 = $$1.getMinBlockZ(); $$3 <= $$1.getMaxBlockZ(); ++$$3) {
                BlockPos $$4 = PlayerSpawnFinder.getOverworldRespawnPos($$0, $$2, $$3);
                if ($$4 == null) continue;
                return $$4;
            }
        }
        return null;
    }
}

