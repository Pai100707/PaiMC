package net.minecraft.world.level.saveddata.maps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record MapFrame(BlockPos pos, int rotation, int entityId) {
   public static final Codec<MapFrame> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(MapFrame::pos),
            Codec.INT.fieldOf("rotation").forGetter(MapFrame::rotation),
            Codec.INT.fieldOf("entity_id").forGetter(MapFrame::entityId)
         )
         .apply($$0, MapFrame::new)
   );

   public String getId() {
      return frameId(this.pos);
   }

   public static String frameId(BlockPos $$0) {
      return "frame-" + $$0.getX() + "," + $$0.getY() + "," + $$0.getZ();
   }
}
