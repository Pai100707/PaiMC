package net.minecraft.network.protocol.game;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundCommandSuggestionsPacket(int id, int start, int length, List<ClientboundCommandSuggestionsPacket.Entry> suggestions)
   implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket> STREAM_CODEC = StreamCodec.composite(
      ByteBufCodecs.VAR_INT,
      ClientboundCommandSuggestionsPacket::id,
      ByteBufCodecs.VAR_INT,
      ClientboundCommandSuggestionsPacket::start,
      ByteBufCodecs.VAR_INT,
      ClientboundCommandSuggestionsPacket::length,
      ClientboundCommandSuggestionsPacket.Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
      ClientboundCommandSuggestionsPacket::suggestions,
      ClientboundCommandSuggestionsPacket::new
   );

   public ClientboundCommandSuggestionsPacket(int $$0, Suggestions $$1) {
      this(
         $$0,
         $$1.getRange().getStart(),
         $$1.getRange().getLength(),
         $$1.getList()
            .stream()
            .map($$0x -> new ClientboundCommandSuggestionsPacket.Entry($$0x.getText(), Optional.ofNullable($$0x.getTooltip()).map(ComponentUtils::fromMessage)))
            .toList()
      );
   }

   @Override
   public PacketType<ClientboundCommandSuggestionsPacket> type() {
      return GamePacketTypes.CLIENTBOUND_COMMAND_SUGGESTIONS;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleCommandSuggestions(this);
   }

   public Suggestions toSuggestions() {
      StringRange $$0 = StringRange.between(this.start, this.start + this.length);
      return new Suggestions($$0, this.suggestions.stream().map($$1 -> new Suggestion($$0, $$1.text(), $$1.tooltip().orElse(null))).toList());
   }

   public record Entry(String text, Optional<Component> tooltip) {
      public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundCommandSuggestionsPacket.Entry> STREAM_CODEC = StreamCodec.composite(
         ByteBufCodecs.STRING_UTF8,
         ClientboundCommandSuggestionsPacket.Entry::text,
         ComponentSerialization.TRUSTED_OPTIONAL_STREAM_CODEC,
         ClientboundCommandSuggestionsPacket.Entry::tooltip,
         ClientboundCommandSuggestionsPacket.Entry::new
      );
   }
}
