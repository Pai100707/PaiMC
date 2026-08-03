package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.util.debug.DebugSubscription.Event;

public record ClientboundDebugEventPacket(Event<?> event) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundDebugEventPacket> STREAM_CODEC = StreamCodec.composite(
      Event.STREAM_CODEC, ClientboundDebugEventPacket::event, ClientboundDebugEventPacket::new
   );

   @Override
   public PacketType<ClientboundDebugEventPacket> type() {
      return GamePacketTypes.CLIENTBOUND_DEBUG_EVENT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleDebugEvent(this);
   }
}
