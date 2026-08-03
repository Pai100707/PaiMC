package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class MapIndex extends SavedData {
   private static final int NO_MAP_ID = -1;
   public static final Codec<MapIndex> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(Codec.INT.optionalFieldOf("map", -1).forGetter($$0x -> $$0x.lastMapId)).apply($$0, MapIndex::new)
   );
   public static final SavedDataType<MapIndex> TYPE = new SavedDataType<>("idcounts", MapIndex::new, CODEC, DataFixTypes.SAVED_DATA_MAP_INDEX);
   private int lastMapId;

   public MapIndex() {
      this(-1);
   }

   public MapIndex(int $$0) {
      this.lastMapId = $$0;
   }

   public MapId getNextMapId() {
      MapId $$0 = new MapId(++this.lastMapId);
      this.setDirty();
      return $$0;
   }
}
