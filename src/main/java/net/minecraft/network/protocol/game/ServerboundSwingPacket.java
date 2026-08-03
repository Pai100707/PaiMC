package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;

public class ServerboundSwingPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSwingPacket> STREAM_CODEC = Packet.codec(
      ServerboundSwingPacket::write, ServerboundSwingPacket::new
   );
   private final InteractionHand hand;

   public ServerboundSwingPacket(InteractionHand $$0) {
      this.hand = $$0;
   }

   private ServerboundSwingPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.hand = $$0.readEnum(InteractionHand.class);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.hand);
   }

   @Override
   public PacketType<ServerboundSwingPacket> type() {
      return GamePacketTypes.SERVERBOUND_SWING;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleAnimate(this);
   }

   public InteractionHand getHand() {
      return this.hand;
   }
}
