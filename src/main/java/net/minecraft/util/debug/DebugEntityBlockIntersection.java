package net.minecraft.util.debug;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum DebugEntityBlockIntersection {
   IN_BLOCK(0, 1610678016),
   IN_FLUID(1, 1610612991),
   IN_AIR(2, 1613968179);

   private static final IntFunction<DebugEntityBlockIntersection> BY_ID = net.minecraft.util.ByIdMap.continuous(
      $$0 -> $$0.id, values(), net.minecraft.util.ByIdMap.OutOfBoundsStrategy.ZERO
   );
   public static final StreamCodec<ByteBuf, DebugEntityBlockIntersection> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, $$0 -> $$0.id);
   private final int id;
   private final int color;

   private DebugEntityBlockIntersection(final int $$0, final int $$1) {
      this.id = $$0;
      this.color = $$1;
   }

   public int color() {
      return this.color;
   }
}
