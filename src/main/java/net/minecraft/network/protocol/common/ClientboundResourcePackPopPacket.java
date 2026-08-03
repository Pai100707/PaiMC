package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundResourcePackPopPacket> STREAM_CODEC = Packet.codec(
      ClientboundResourcePackPopPacket::write, ClientboundResourcePackPopPacket::new
   );

   private ClientboundResourcePackPopPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readOptional(UUIDUtil.STREAM_CODEC));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeOptional(this.id, UUIDUtil.STREAM_CODEC);
   }

   @Override
   public PacketType<ClientboundResourcePackPopPacket> type() {
      return CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_POP;
   }

   public void handle(ClientCommonPacketListener $$0) {
      $$0.handleResourcePackPop(this);
   }
}
