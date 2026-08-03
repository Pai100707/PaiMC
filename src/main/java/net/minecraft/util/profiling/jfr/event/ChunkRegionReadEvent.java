package net.minecraft.util.profiling.jfr.event;

import jdk.jfr.EventType;
import jdk.jfr.Label;
import jdk.jfr.Name;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;

@Name("minecraft.ChunkRegionRead")
@Label("Region File Read")
@DontObfuscate
public class ChunkRegionReadEvent extends ChunkRegionIoEvent {
   public static final String EVENT_NAME = "minecraft.ChunkRegionRead";
   public static final EventType TYPE = EventType.getEventType(ChunkRegionReadEvent.class);

   public ChunkRegionReadEvent(RegionStorageInfo $$0, ChunkPos $$1, RegionFileVersion $$2, int $$3) {
      super($$0, $$1, $$2, $$3);
   }
}
