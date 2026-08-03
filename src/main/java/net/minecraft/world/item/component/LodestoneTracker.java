package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public record LodestoneTracker(Optional<GlobalPos> target, boolean tracked) {
   public static final Codec<LodestoneTracker> CODEC = RecordCodecBuilder.create(
      $$0 -> $$0.group(
            GlobalPos.CODEC.optionalFieldOf("target").forGetter(LodestoneTracker::target),
            Codec.BOOL.optionalFieldOf("tracked", true).forGetter(LodestoneTracker::tracked)
         )
         .apply($$0, LodestoneTracker::new)
   );
   public static final StreamCodec<ByteBuf, LodestoneTracker> STREAM_CODEC = StreamCodec.composite(
      GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional), LodestoneTracker::target, ByteBufCodecs.BOOL, LodestoneTracker::tracked, LodestoneTracker::new
   );

   public LodestoneTracker tick(ServerLevel $$0) {
      if (this.tracked && !this.target.isEmpty()) {
         if (this.target.get().dimension() != $$0.dimension()) {
            return this;
         } else {
            BlockPos $$1 = this.target.get().pos();
            return $$0.isInWorldBounds($$1) && $$0.getPoiManager().existsAtPosition(PoiTypes.LODESTONE, $$1)
               ? this
               : new LodestoneTracker(Optional.empty(), true);
         }
      } else {
         return this;
      }
   }
}
