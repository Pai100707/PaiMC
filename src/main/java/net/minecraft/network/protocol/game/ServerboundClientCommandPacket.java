package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundClientCommandPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundClientCommandPacket> STREAM_CODEC = Packet.codec(
      ServerboundClientCommandPacket::write, ServerboundClientCommandPacket::new
   );
   private final ServerboundClientCommandPacket.Action action;

   public ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action $$0) {
      this.action = $$0;
   }

   private ServerboundClientCommandPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.action = $$0.readEnum(ServerboundClientCommandPacket.Action.class);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.action);
   }

   @Override
   public PacketType<ServerboundClientCommandPacket> type() {
      return GamePacketTypes.SERVERBOUND_CLIENT_COMMAND;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleClientCommand(this);
   }

   public ServerboundClientCommandPacket.Action getAction() {
      return this.action;
   }

   public static enum Action {
      PERFORM_RESPAWN,
      REQUEST_STATS;
   }
}
