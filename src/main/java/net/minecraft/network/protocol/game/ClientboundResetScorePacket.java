package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundResetScorePacket(String owner, String objectiveName) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundResetScorePacket> STREAM_CODEC = Packet.codec(
      ClientboundResetScorePacket::write, ClientboundResetScorePacket::new
   );

   private ClientboundResetScorePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readUtf(), $$0.readNullable(net.minecraft.network.FriendlyByteBuf::readUtf));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeUtf(this.owner);
      $$0.writeNullable(this.objectiveName, net.minecraft.network.FriendlyByteBuf::writeUtf);
   }

   @Override
   public PacketType<ClientboundResetScorePacket> type() {
      return GamePacketTypes.CLIENTBOUND_RESET_SCORE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleResetScore(this);
   }
}
