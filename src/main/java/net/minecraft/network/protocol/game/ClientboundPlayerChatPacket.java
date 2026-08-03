package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.jspecify.annotations.Nullable;

public record ClientboundPlayerChatPacket(
   int globalIndex,
   UUID sender,
   int index,
   @Nullable MessageSignature signature,
   SignedMessageBody.Packed body,
   @Nullable Component unsignedContent,
   FilterMask filterMask,
   ChatType.Bound chatType
) implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundPlayerChatPacket> STREAM_CODEC = Packet.codec(
      ClientboundPlayerChatPacket::write, ClientboundPlayerChatPacket::new
   );

   private ClientboundPlayerChatPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this(
         $$0.readVarInt(),
         $$0.readUUID(),
         $$0.readVarInt(),
         $$0.readNullable(MessageSignature::read),
         new SignedMessageBody.Packed($$0),
         net.minecraft.network.FriendlyByteBuf.readNullable($$0, ComponentSerialization.TRUSTED_STREAM_CODEC),
         FilterMask.read($$0),
         ChatType.Bound.STREAM_CODEC.decode($$0)
      );
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeVarInt(this.globalIndex);
      $$0.writeUUID(this.sender);
      $$0.writeVarInt(this.index);
      $$0.writeNullable(this.signature, MessageSignature::write);
      this.body.write($$0);
      net.minecraft.network.FriendlyByteBuf.writeNullable($$0, this.unsignedContent, ComponentSerialization.TRUSTED_STREAM_CODEC);
      FilterMask.write($$0, this.filterMask);
      ChatType.Bound.STREAM_CODEC.encode($$0, this.chatType);
   }

   @Override
   public PacketType<ClientboundPlayerChatPacket> type() {
      return GamePacketTypes.CLIENTBOUND_PLAYER_CHAT;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handlePlayerChat(this);
   }

   @Override
   public boolean isSkippable() {
      return true;
   }
}
