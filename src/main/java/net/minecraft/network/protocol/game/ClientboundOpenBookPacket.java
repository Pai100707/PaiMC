package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundOpenBookPacket> STREAM_CODEC = Packet.codec(
      ClientboundOpenBookPacket::write, ClientboundOpenBookPacket::new
   );
   private final InteractionHand hand;

   public ClientboundOpenBookPacket(InteractionHand $$0) {
      this.hand = $$0;
   }

   private ClientboundOpenBookPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.hand = $$0.readEnum(InteractionHand.class);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.hand);
   }

   @Override
   public PacketType<ClientboundOpenBookPacket> type() {
      return GamePacketTypes.CLIENTBOUND_OPEN_BOOK;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleOpenBook(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }
}
