/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.util.profiling.jfr.event.ChunkRegionIoEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

@Name(value="minecraft.ChunkRegionWrite")
@Label(value="Region File Write")
@DontObfuscate
public class ChunkRegionWriteEvent
extends ChunkRegionIoEvent {
    public static final String EVENT_NAME = "minecraft.ChunkRegionWrite";
    public static final EventType TYPE = EventType.getEventType(ChunkRegionWriteEvent.class);

    public ChunkRegionWriteEvent(RegionStorageInfo $$0, ChunkPos $$1, RegionFileVersion $$2, int $$3) {
        super($$0, $$1, $$2, $$3);
    }
}

