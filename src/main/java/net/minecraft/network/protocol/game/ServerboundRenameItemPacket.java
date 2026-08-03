package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundRenameItemPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundRenameItemPacket> STREAM_CODEC = Packet.codec(
      ServerboundRenameItemPacket::write, ServerboundRenameItemPacket::new
   );
   private final String name;

   public ServerboundRenameItemPacket(String $$0) {
      this.name = $$0;
   }

   private ServerboundRenameItemPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.name = $$0.readUtf();
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.name);
   }

   @Override
   public PacketType<ServerboundRenameItemPacket> type() {
      return GamePacketTypes.SERVERBOUND_RENAME_ITEM;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleRenameItem(this);
   }

   public String getName() {
      return this.name;
   }
}
