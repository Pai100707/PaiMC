package net.minecraft.network.protocol.game;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.Optionull;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

public class ClientboundPlayerInfoUpdatePacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundPlayerInfoUpdatePacket> STREAM_CODEC = Packet.codec(
      ClientboundPlayerInfoUpdatePacket::write, ClientboundPlayerInfoUpdatePacket::new
   );
   private final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions;
   private final List<ClientboundPlayerInfoUpdatePacket.Entry> entries;

   public ClientboundPlayerInfoUpdatePacket(EnumSet<ClientboundPlayerInfoUpdatePacket.Action> $$0, Collection<ServerPlayer> $$1) {
      this.actions = $$0;
      this.entries = $$1.stream().map(ClientboundPlayerInfoUpdatePacket.Entry::new).toList();
   }

   public ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action $$0, ServerPlayer $$1) {
      this.actions = EnumSet.of($$0);
      this.entries = List.of(new ClientboundPlayerInfoUpdatePacket.Entry($$1));
   }

   public static ClientboundPlayerInfoUpdatePacket createPlayerInitializing(Collection<ServerPlayer> $$0) {
      EnumSet<ClientboundPlayerInfoUpdatePacket.Action> $$1 = EnumSet.of(
         ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
         ClientboundPlayerInfoUpdatePacket.Action.INITIALIZE_CHAT,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_HAT,
         ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LIST_ORDER
      );
      return new ClientboundPlayerInfoUpdatePacket($$1, $$0);
   }

   private ClientboundPlayerInfoUpdatePacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.actions = $$0.readEnumSet(ClientboundPlayerInfoUpdatePacket.Action.class);
      this.entries = $$0.readList($$0x -> {
         ClientboundPlayerInfoUpdatePacket.EntryBuilder $$1 = new ClientboundPlayerInfoUpdatePacket.EntryBuilder($$0x.readUUID());

         for (ClientboundPlayerInfoUpdatePacket.Action $$2 : this.actions) {
            $$2.reader.read($$1, (net.minecraft.network.RegistryFriendlyByteBuf)$$0x);
         }

         return $$1.build();
      });
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeEnumSet(this.actions, ClientboundPlayerInfoUpdatePacket.Action.class);
      $$0.writeCollection(this.entries, ($$0x, $$1) -> {
         $$0x.writeUUID($$1.profileId());

         for (ClientboundPlayerInfoUpdatePacket.Action $$2 : this.actions) {
            $$2.writer.write((net.minecraft.network.RegistryFriendlyByteBuf)$$0x, $$1);
         }
      });
   }

   @Override
   public PacketType<ClientboundPlayerInfoUpdatePacket> type() {
      return GamePacketTypes.CLIENTBOUND_PLAYER_INFO_UPDATE;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handlePlayerInfoUpdate(this);
   }

   public EnumSet<ClientboundPlayerInfoUpdatePacket.Action> actions() {
      return this.actions;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> entries() {
      return this.entries;
   }

   public List<ClientboundPlayerInfoUpdatePacket.Entry> newEntries() {
      return this.actions.contains(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) ? this.entries : List.of();
   }

   @Override
   public String toString() {
      return MoreObjects.toStringHelper(this).add("actions", this.actions).add("entries", this.entries).toString();
   }

   public static enum Action {
      ADD_PLAYER(($$0, $$1) -> {
         String $$2 = ByteBufCodecs.PLAYER_NAME.decode($$1);
         PropertyMap $$3 = ByteBufCodecs.GAME_PROFILE_PROPERTIES.decode($$1);
         $$0.profile = new GameProfile($$0.profileId, $$2, $$3);
      }, ($$0, $$1) -> {
         GameProfile $$2 = Objects.requireNonNull($$1.profile());
         ByteBufCodecs.PLAYER_NAME.encode($$0, $$2.name());
         ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode($$0, $$2.properties());
      }),
      INITIALIZE_CHAT(
         ($$0, $$1) -> $$0.chatSession = $$1.readNullable(RemoteChatSession.Data::read),
         ($$0, $$1) -> $$0.writeNullable($$1.chatSession, RemoteChatSession.Data::write)
      ),
      UPDATE_GAME_MODE(($$0, $$1) -> $$0.gameMode = GameType.byId($$1.readVarInt()), ($$0, $$1) -> $$0.writeVarInt($$1.gameMode().getId())),
      UPDATE_LISTED(($$0, $$1) -> $$0.listed = $$1.readBoolean(), ($$0, $$1) -> $$0.writeBoolean($$1.listed())),
      UPDATE_LATENCY(($$0, $$1) -> $$0.latency = $$1.readVarInt(), ($$0, $$1) -> $$0.writeVarInt($$1.latency())),
      UPDATE_DISPLAY_NAME(
         ($$0, $$1) -> $$0.displayName = net.minecraft.network.FriendlyByteBuf.readNullable($$1, ComponentSerialization.TRUSTED_STREAM_CODEC),
         ($$0, $$1) -> net.minecraft.network.FriendlyByteBuf.writeNullable($$0, $$1.displayName(), ComponentSerialization.TRUSTED_STREAM_CODEC)
      ),
      UPDATE_LIST_ORDER(($$0, $$1) -> $$0.listOrder = $$1.readVarInt(), ($$0, $$1) -> $$0.writeVarInt($$1.listOrder)),
      UPDATE_HAT(($$0, $$1) -> $$0.showHat = $$1.readBoolean(), ($$0, $$1) -> $$0.writeBoolean($$1.showHat));

      final ClientboundPlayerInfoUpdatePacket.Action.Reader reader;
      final ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

      private Action(final ClientboundPlayerInfoUpdatePacket.Action.Reader $$0, final ClientboundPlayerInfoUpdatePacket.Action.Writer $$1) {
         this.reader = $$0;
         this.writer = $$1;
      }

      public interface Reader {
         void read(ClientboundPlayerInfoUpdatePacket.EntryBuilder var1, net.minecraft.network.RegistryFriendlyByteBuf var2);
      }

      public interface Writer {
         void write(net.minecraft.network.RegistryFriendlyByteBuf var1, ClientboundPlayerInfoUpdatePacket.Entry var2);
      }
   }

   public record Entry(
      UUID profileId,
      @Nullable GameProfile profile,
      boolean listed,
      int latency,
      GameType gameMode,
      @Nullable Component displayName,
      boolean showHat,
      int listOrder,
      @Nullable RemoteChatSession.Data chatSession
   ) {

      Entry(ServerPlayer $$0) {
         this(
            $$0.getUUID(),
            $$0.getGameProfile(),
            true,
            $$0.connection.latency(),
            $$0.gameMode(),
            $$0.getTabListDisplayName(),
            $$0.isModelPartShown(PlayerModelPart.HAT),
            $$0.getTabListOrder(),
            (RemoteChatSession.Data)Optionull.map($$0.getChatSession(), RemoteChatSession::asData)
         );
      }
   }

   static class EntryBuilder {
      final UUID profileId;
      @Nullable
      GameProfile profile;
      boolean listed;
      int latency;
      GameType gameMode = GameType.DEFAULT_MODE;
      @Nullable
      Component displayName;
      boolean showHat;
      int listOrder;
      @Nullable
      RemoteChatSession.Data chatSession;

      EntryBuilder(UUID $$0) {
         this.profileId = $$0;
      }

      ClientboundPlayerInfoUpdatePacket.Entry build() {
         return new ClientboundPlayerInfoUpdatePacket.Entry(
            this.profileId, this.profile, this.listed, this.latency, this.gameMode, this.displayName, this.showHat, this.listOrder, this.chatSession
         );
      }
   }
}
