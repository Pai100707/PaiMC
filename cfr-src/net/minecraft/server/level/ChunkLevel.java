/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.level;

import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public class ChunkLevel {
    private static final int FULL_CHUNK_LEVEL = 33;
    private static final int BLOCK_TICKING_LEVEL = 32;
    private static final int ENTITY_TICKING_LEVEL = 31;
    private static final ChunkStep FULL_CHUNK_STEP = ChunkPyramid.GENERATION_PYRAMID.getStepTo(ChunkStatus.FULL);
    public static final int RADIUS_AROUND_FULL_CHUNK = FULL_CHUNK_STEP.accumulatedDependencies().getRadius();
    public static final int MAX_LEVEL = 33 + RADIUS_AROUND_FULL_CHUNK;

    public static @Nullable ChunkStatus generationStatus(int $$0) {
        return ChunkLevel.getStatusAroundFullChunk($$0 - 33, null);
    }

    @Contract(value="_,!null->!null;_,_->_")
    public static @Nullable ChunkStatus getStatusAroundFullChunk(int $$0, @Nullable ChunkStatus $$1) {
        if ($$0 > RADIUS_AROUND_FULL_CHUNK) {
            return $$1;
        }
        if ($$0 <= 0) {
            return ChunkStatus.FULL;
        }
        return FULL_CHUNK_STEP.accumulatedDependencies().get($$0);
    }

    public static ChunkStatus getStatusAroundFullChunk(int $$0) {
        return ChunkLevel.getStatusAroundFullChunk($$0, ChunkStatus.EMPTY);
    }

    public static int byStatus(ChunkStatus $$0) {
        return 33 + FULL_CHUNK_STEP.getAccumulatedRadiusOf($$0);
    }

    public static FullChunkStatus fullStatus(int $$0) {
        if ($$0 <= 31) {
            return FullChunkStatus.ENTITY_TICKING;
        }
        if ($$0 <= 32) {
            return FullChunkStatus.BLOCK_TICKING;
        }
        if ($$0 <= 33) {
            return FullChunkStatus.FULL;
        }
        return FullChunkStatus.INACCESSIBLE;
    }

    public static int byStatus(FullChunkStatus $$0) {
        return switch ($$0) {
            default -> throw new MatchException(null, null);
            case FullChunkStatus.INACCESSIBLE -> MAX_LEVEL;
            case FullChunkStatus.FULL -> 33;
            case FullChunkStatus.BLOCK_TICKING -> 32;
            case FullChunkStatus.ENTITY_TICKING -> 31;
        };
    }

    public static boolean isEntityTicking(int $$0) {
        return $$0 <= 31;
    }

    public static boolean isBlockTicking(int $$0) {
        return $$0 <= 32;
    }

    public static boolean isLoaded(int $$0) {
        return $$0 <= MAX_LEVEL;
    }
}

