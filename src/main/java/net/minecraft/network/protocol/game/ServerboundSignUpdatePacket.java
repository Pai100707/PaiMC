package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSignUpdatePacket> STREAM_CODEC = Packet.codec(
      ServerboundSignUpdatePacket::write, ServerboundSignUpdatePacket::new
   );
   private static final int MAX_STRING_LENGTH = 384;
   private final BlockPos pos;
   private final String[] lines;
   private final boolean isFrontText;

   public ServerboundSignUpdatePacket(BlockPos $$0, boolean $$1, String $$2, String $$3, String $$4, String $$5) {
      this.pos = $$0;
      this.isFrontText = $$1;
      this.lines = new String[]{$$2, $$3, $$4, $$5};
   }

   private ServerboundSignUpdatePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.pos = $$0.readBlockPos();
      this.isFrontText = $$0.readBoolean();
      this.lines = new String[4];

      for (int $$1 = 0; $$1 < 4; $$1++) {
         this.lines[$$1] = $$0.readUtf(384);
      }
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeBlockPos(this.pos);
      $$0.writeBoolean(this.isFrontText);

      for (int $$1 = 0; $$1 < 4; $$1++) {
         $$0.writeUtf(this.lines[$$1]);
      }
   }

   @Override
   public PacketType<ServerboundSignUpdatePacket> type() {
      return GamePacketTypes.SERVERBOUND_SIGN_UPDATE;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSignUpdate(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public boolean isFrontText() {
      return this.isFrontText;
   }

   public String[] getLines() {
      return this.lines;
   }
}
