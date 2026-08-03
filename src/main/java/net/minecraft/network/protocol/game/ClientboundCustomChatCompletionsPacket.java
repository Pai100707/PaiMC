package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundCustomChatCompletionsPacket(ClientboundCustomChatCompletionsPacket.Action action, List<String> entries)
   implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ClientboundCustomChatCompletionsPacket> STREAM_CODEC = Packet.codec(
      ClientboundCustomChatCompletionsPacket::write, ClientboundCustomChatCompletionsPacket::new
   );

   private ClientboundCustomChatCompletionsPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this($$0.readEnum(ClientboundCustomChatCompletionsPacket.Action.class), $$0.readList(net.minecraft.network.FriendlyByteBuf::readUtf));
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.action);
      $$0.writeCollection(this.entries, net.minecraft.network.FriendlyByteBuf::writeUtf);
   }

   @Override
   public PacketType<ClientboundCustomChatCompletionsPacket> type() {
      return GamePacketTypes.CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleCustomChatCompletions(this);
   }

   public static enum Action {
      ADD,
      REMOVE,
      SET;
   }
}
