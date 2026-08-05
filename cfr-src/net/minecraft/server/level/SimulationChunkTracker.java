/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 */
package net.minecraft.server.level;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import net.minecraft.server.level.ChunkTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;

public class SimulationChunkTracker
extends ChunkTracker {
    public static final int MAX_LEVEL = 33;
    protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
    private final TicketStorage ticketStorage;

    public SimulationChunkTracker(TicketStorage $$0) {
        super(34, 16, 256);
        this.ticketStorage = $$0;
        $$0.setSimulationChunkUpdatedListener(this::update);
        this.chunks.defaultReturnValue((byte)33);
    }

    @Override
    protected int getLevelFromSource(long $$0) {
        return this.ticketStorage.getTicketLevelAt($$0, true);
    }

    public int getLevel(ChunkPos $$0) {
        return this.getLevel($$0.toLong());
    }

    @Override
    protected int getLevel(long $$0) {
        return this.chunks.get($$0);
    }

    @Override
    protected void setLevel(long $$0, int $$1) {
        if ($$1 >= 33) {
            this.chunks.remove($$0);
        } else {
            this.chunks.put($$0, (byte)$$1);
        }
    }

    public void runAllUpdates() {
        this.runUpdates(Integer.MAX_VALUE);
    }
}

