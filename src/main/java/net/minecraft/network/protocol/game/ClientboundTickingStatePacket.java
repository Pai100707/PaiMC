package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.TickRateManager;

public record ClientboundTickingStatePacket(float tickRate, boolean isFrozen) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundTickingStatePacket> STREAM_CODEC = Packet.codec(
      ClientboundTickingStatePacket::write, ClientboundTickingStatePacket::new
   );

   private ClientboundTickingStatePacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readFloat(), $$0.readBoolean());
   }

   public static ClientboundTickingStatePacket from(TickRateManager $$0) {
      return new ClientboundTickingStatePacket($$0.tickrate(), $$0.isFrozen());
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeFloat(this.tickRate);
      $$0.writeBoolean(this.isFrozen);
   }

   @Override
   public PacketType<ClientboundTickingStatePacket> type() {
      return GamePacketTypes.CLIENTBOUND_TICKING_STATE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleTickingState(this);
   }
}
