package net.minecraft.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Util;

public record Rotations(float x, float y, float z) {
   public static final Codec<net.minecraft.core.Rotations> CODEC = Codec.FLOAT
      .listOf()
      .comapFlatMap(
         $$0 -> Util.fixedSize($$0, 3).map($$0x -> new net.minecraft.core.Rotations((Float)$$0x.get(0), (Float)$$0x.get(1), (Float)$$0x.get(2))),
         $$0 -> List.of($$0.x(), $$0.y(), $$0.z())
      );
   public static final StreamCodec<ByteBuf, net.minecraft.core.Rotations> STREAM_CODEC = new StreamCodec<ByteBuf, net.minecraft.core.Rotations>() {
      public net.minecraft.core.Rotations decode(ByteBuf $$0) {
         return new net.minecraft.core.Rotations($$0.readFloat(), $$0.readFloat(), $$0.readFloat());
      }

      public void encode(ByteBuf $$0, net.minecraft.core.Rotations $$1) {
         $$0.writeFloat($$1.x);
         $$0.writeFloat($$1.y);
         $$0.writeFloat($$1.z);
      }
   };

   public Rotations(float x, float y, float z) {
      x = !Float.isInfinite(x) && !Float.isNaN(x) ? x % 360.0F : 0.0F;
      y = !Float.isInfinite(y) && !Float.isNaN(y) ? y % 360.0F : 0.0F;
      z = !Float.isInfinite(z) && !Float.isNaN(z) ? z % 360.0F : 0.0F;
      this.x = x;
      this.y = y;
      this.z = z;
   }
}
