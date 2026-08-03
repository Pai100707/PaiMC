package net.minecraft.network.protocol.game;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class ServerboundSeenAdvancementsPacket implements Packet<ServerGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.FriendlyByteBuf, ServerboundSeenAdvancementsPacket> STREAM_CODEC = Packet.codec(
      ServerboundSeenAdvancementsPacket::write, ServerboundSeenAdvancementsPacket::new
   );
   private final ServerboundSeenAdvancementsPacket.Action action;
   @Nullable
   private final Identifier tab;

   public ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action $$0, @Nullable Identifier $$1) {
      this.action = $$0;
      this.tab = $$1;
   }

   public static ServerboundSeenAdvancementsPacket openedTab(AdvancementHolder $$0) {
      return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.OPENED_TAB, $$0.id());
   }

   public static ServerboundSeenAdvancementsPacket closedScreen() {
      return new ServerboundSeenAdvancementsPacket(ServerboundSeenAdvancementsPacket.Action.CLOSED_SCREEN, null);
   }

   private ServerboundSeenAdvancementsPacket(net.minecraft.network.FriendlyByteBuf $$0) {
      this.action = $$0.readEnum(ServerboundSeenAdvancementsPacket.Action.class);
      if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
         this.tab = $$0.readIdentifier();
      } else {
         this.tab = null;
      }
   }

   private void write(net.minecraft.network.FriendlyByteBuf $$0) {
      $$0.writeEnum(this.action);
      if (this.action == ServerboundSeenAdvancementsPacket.Action.OPENED_TAB) {
         $$0.writeIdentifier(this.tab);
      }
   }

   @Override
   public PacketType<ServerboundSeenAdvancementsPacket> type() {
      return GamePacketTypes.SERVERBOUND_SEEN_ADVANCEMENTS;
   }

   public void handle(ServerGamePacketListener $$0) {
      $$0.handleSeenAdvancements(this);
   }

   public ServerboundSeenAdvancementsPacket.Action getAction() {
      return this.action;
   }

   @Nullable
   public Identifier getTab() {
      return this.tab;
   }

   public static enum Action {
      OPENED_TAB,
      CLOSED_SCREEN;
   }
}
