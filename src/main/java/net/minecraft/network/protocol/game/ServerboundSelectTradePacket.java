package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundSelectTradePacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSelectTradePacket> STREAM_CODEC = Packet.codec(
      ServerboundSelectTradePacket::write, ServerboundSelectTradePacket::new
   );
   private final int item;

   public ServerboundSelectTradePacket(int $$0) {
      this.item = $$0;
   }

   private ServerboundSelectTradePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.item = $$0.readVarInt();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeVarInt(this.item);
   }

   @Override
   public PacketType<ServerboundSelectTradePacket> type() {
      return GamePacketTypes.SERVERBOUND_SELECT_TRADE;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSelectTrade(this);
   }

   public int getItem() {
      return this.item;
   }
}
