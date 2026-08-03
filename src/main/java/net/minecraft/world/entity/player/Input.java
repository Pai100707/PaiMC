package net.minecraft.world.entity.player;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Input(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint) {
   private static final byte FLAG_FORWARD = 1;
   private static final byte FLAG_BACKWARD = 2;
   private static final byte FLAG_LEFT = 4;
   private static final byte FLAG_RIGHT = 8;
   private static final byte FLAG_JUMP = 16;
   private static final byte FLAG_SHIFT = 32;
   private static final byte FLAG_SPRINT = 64;
   public static final StreamCodec<FriendlyByteBuf, Input> STREAM_CODEC = new StreamCodec<FriendlyByteBuf, Input>() {
      public void encode(FriendlyByteBuf $$0, Input $$1) {
         byte $$2 = 0;
         $$2 = (byte)($$2 | ($$1.forward() ? 1 : 0));
         $$2 = (byte)($$2 | ($$1.backward() ? 2 : 0));
         $$2 = (byte)($$2 | ($$1.left() ? 4 : 0));
         $$2 = (byte)($$2 | ($$1.right() ? 8 : 0));
         $$2 = (byte)($$2 | ($$1.jump() ? 16 : 0));
         $$2 = (byte)($$2 | ($$1.shift() ? 32 : 0));
         $$2 = (byte)($$2 | ($$1.sprint() ? 64 : 0));
         $$0.writeByte($$2);
      }

      public Input decode(FriendlyByteBuf $$0) {
         byte $$1 = $$0.readByte();
         boolean $$2 = ($$1 & 1) != 0;
         boolean $$3 = ($$1 & 2) != 0;
         boolean $$4 = ($$1 & 4) != 0;
         boolean $$5 = ($$1 & 8) != 0;
         boolean $$6 = ($$1 & 16) != 0;
         boolean $$7 = ($$1 & 32) != 0;
         boolean $$8 = ($$1 & 64) != 0;
         return new Input($$2, $$3, $$4, $$5, $$6, $$7, $$8);
      }
   };
   public static Input EMPTY = new Input(false, false, false, false, false, false, false);
}
