package net.minecraft.network.protocol.game;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ClientboundSelectAdvancementsTabPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundSelectAdvancementsTabPacket> STREAM_CODEC = Packet.codec(
      ClientboundSelectAdvancementsTabPacket::write, ClientboundSelectAdvancementsTabPacket::new
   );
   @Nullable
   private final Identifier tab;

   public ClientboundSelectAdvancementsTabPacket(@Nullable Identifier $$0) {
      this.tab = $$0;
   }

   private ClientboundSelectAdvancementsTabPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.tab = $$0.readNullable(net.minecraft.network.FriendlyByteBuf::readIdentifier);
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeNullable(this.tab, net.minecraft.network.FriendlyByteBuf::writeIdentifier);
   }

   @Override
   public PacketType<ClientboundSelectAdvancementsTabPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SELECT_ADVANCEMENTS_TAB;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSelectAdvancementsTab(this);
   }

   @Nullable
   public Identifier getTab() {
      return this.tab;
   }
}
