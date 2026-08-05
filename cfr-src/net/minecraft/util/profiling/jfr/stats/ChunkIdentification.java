/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.stats;

import jdk.jfr.consumer.RecordedEvent;

public record ChunkIdentification(String level, String dimension, int x, int z) {
    public static ChunkIdentification from(RecordedEvent $$0) {
        return new ChunkIdentification($$0.getString("level"), $$0.getString("dimension"), $$0.getInt("chunkPosX"), $$0.getInt("chunkPosZ"));
    }
}

