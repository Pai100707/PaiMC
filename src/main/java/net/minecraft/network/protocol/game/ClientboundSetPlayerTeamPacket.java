package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team.CollisionRule;
import net.minecraft.world.scores.Team.Visibility;
import org.jspecify.annotations.Nullable;

public class ClientboundSetPlayerTeamPacket implements Packet<ClientGamePacketListener> {
   public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundSetPlayerTeamPacket> STREAM_CODEC = Packet.codec(
      ClientboundSetPlayerTeamPacket::write, ClientboundSetPlayerTeamPacket::new
   );
   private static final int METHOD_ADD = 0;
   private static final int METHOD_REMOVE = 1;
   private static final int METHOD_CHANGE = 2;
   private static final int METHOD_JOIN = 3;
   private static final int METHOD_LEAVE = 4;
   private static final int MAX_VISIBILITY_LENGTH = 40;
   private static final int MAX_COLLISION_LENGTH = 40;
   private final int method;
   private final String name;
   private final Collection<String> players;
   private final Optional<ClientboundSetPlayerTeamPacket.Parameters> parameters;

   private ClientboundSetPlayerTeamPacket(String $$0, int $$1, Optional<ClientboundSetPlayerTeamPacket.Parameters> $$2, Collection<String> $$3) {
      this.name = $$0;
      this.method = $$1;
      this.parameters = $$2;
      this.players = ImmutableList.copyOf($$3);
   }

   public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam $$0, boolean $$1) {
      return new ClientboundSetPlayerTeamPacket(
         $$0.getName(),
         $$1 ? 0 : 2,
         Optional.of(new ClientboundSetPlayerTeamPacket.Parameters($$0)),
         (Collection<String>)($$1 ? $$0.getPlayers() : ImmutableList.of())
      );
   }

   public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam $$0) {
      return new ClientboundSetPlayerTeamPacket($$0.getName(), 1, Optional.empty(), ImmutableList.of());
   }

   public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam $$0, String $$1, ClientboundSetPlayerTeamPacket.Action $$2) {
      return new ClientboundSetPlayerTeamPacket(
         $$0.getName(), $$2 == ClientboundSetPlayerTeamPacket.Action.ADD ? 3 : 4, Optional.empty(), ImmutableList.of($$1)
      );
   }

   private ClientboundSetPlayerTeamPacket(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      this.name = $$0.readUtf();
      this.method = $$0.readByte();
      if (shouldHaveParameters(this.method)) {
         this.parameters = Optional.of(new ClientboundSetPlayerTeamPacket.Parameters($$0));
      } else {
         this.parameters = Optional.empty();
      }

      if (shouldHavePlayerList(this.method)) {
         this.players = $$0.readList(net.minecraft.network.FriendlyByteBuf::readUtf);
      } else {
         this.players = ImmutableList.of();
      }
   }

   private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      $$0.writeUtf(this.name);
      $$0.writeByte(this.method);
      if (shouldHaveParameters(this.method)) {
         this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write($$0);
      }

      if (shouldHavePlayerList(this.method)) {
         $$0.writeCollection(this.players, net.minecraft.network.FriendlyByteBuf::writeUtf);
      }
   }

   private static boolean shouldHavePlayerList(int $$0) {
      return $$0 == 0 || $$0 == 3 || $$0 == 4;
   }

   private static boolean shouldHaveParameters(int $$0) {
      return $$0 == 0 || $$0 == 2;
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getPlayerAction() {
      return switch (this.method) {
         case 0, 3 -> ClientboundSetPlayerTeamPacket.Action.ADD;
         default -> null;
         case 4 -> ClientboundSetPlayerTeamPacket.Action.REMOVE;
      };
   }

   @Nullable
   public ClientboundSetPlayerTeamPacket.Action getTeamAction() {
      return switch (this.method) {
         case 0 -> ClientboundSetPlayerTeamPacket.Action.ADD;
         case 1 -> ClientboundSetPlayerTeamPacket.Action.REMOVE;
         default -> null;
      };
   }

   @Override
   public PacketType<ClientboundSetPlayerTeamPacket> type() {
      return GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM;
   }

   public void handle(ClientGamePacketListener $$0) {
      $$0.handleSetPlayerTeamPacket(this);
   }

   public String getName() {
      return this.name;
   }

   public Collection<String> getPlayers() {
      return this.players;
   }

   public Optional<ClientboundSetPlayerTeamPacket.Parameters> getParameters() {
      return this.parameters;
   }

   public static enum Action {
      ADD,
      REMOVE;
   }

   public static class Parameters {
      private final Component displayName;
      private final Component playerPrefix;
      private final Component playerSuffix;
      private final Visibility nametagVisibility;
      private final CollisionRule collisionRule;
      private final ChatFormatting color;
      private final int options;

      public Parameters(PlayerTeam $$0) {
         this.displayName = $$0.getDisplayName();
         this.options = $$0.packOptions();
         this.nametagVisibility = $$0.getNameTagVisibility();
         this.collisionRule = $$0.getCollisionRule();
         this.color = $$0.getColor();
         this.playerPrefix = $$0.getPlayerPrefix();
         this.playerSuffix = $$0.getPlayerSuffix();
      }

      public Parameters(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this.displayName = ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
         this.options = $$0.readByte();
         this.nametagVisibility = (Visibility)Visibility.STREAM_CODEC.decode($$0);
         this.collisionRule = (CollisionRule)CollisionRule.STREAM_CODEC.decode($$0);
         this.color = $$0.readEnum(ChatFormatting.class);
         this.playerPrefix = ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
         this.playerSuffix = ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
      }

      public Component getDisplayName() {
         return this.displayName;
      }

      public int getOptions() {
         return this.options;
      }

      public ChatFormatting getColor() {
         return this.color;
      }

      public Visibility getNametagVisibility() {
         return this.nametagVisibility;
      }

      public CollisionRule getCollisionRule() {
         return this.collisionRule;
      }

      public Component getPlayerPrefix() {
         return this.playerPrefix;
      }

      public Component getPlayerSuffix() {
         return this.playerSuffix;
      }

      public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.displayName);
         $$0.writeByte(this.options);
         Visibility.STREAM_CODEC.encode($$0, this.nametagVisibility);
         CollisionRule.STREAM_CODEC.encode($$0, this.collisionRule);
         $$0.writeEnum(this.color);
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.playerPrefix);
         ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.playerSuffix);
      }
   }
}
