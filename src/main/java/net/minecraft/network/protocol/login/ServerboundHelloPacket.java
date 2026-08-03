package net.minecraft.network.protocol.login;

import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundHelloPacket(String name, UUID profileId) implements Packet<ServerLoginPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundHelloPacket> STREAM_CODEC = Packet.codec(
      ServerboundHelloPacket::write, ServerboundHelloPacket::new
   );

   private ServerboundHelloPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf(16), $$0.readUUID());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.name, 16);
      $$0.writeUUID(this.profileId);
   }

   @Override
   public PacketType<ServerboundHelloPacket> type() {
      return LoginPacketTypes.SERVERBOUND_HELLO;
   }

   public void handle(ServerLoginPacketListener $$0) {
      $$0.handleHello(this);
   }
}
