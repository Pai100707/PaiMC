package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundSetCarriedItemPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSetCarriedItemPacket> STREAM_CODEC = Packet.codec(
      ServerboundSetCarriedItemPacket::write, ServerboundSetCarriedItemPacket::new
   );
   private final int slot;

   public ServerboundSetCarriedItemPacket(int $$0) {
      this.slot = $$0;
   }

   private ServerboundSetCarriedItemPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.slot = $$0.readShort();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeShort(this.slot);
   }

   @Override
   public PacketType<ServerboundSetCarriedItemPacket> type() {
      return GamePacketTypes.SERVERBOUND_SET_CARRIED_ITEM;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSetCarriedItem(this);
   }

   public int getSlot() {
      return this.slot;
   }
}
