/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

public class ClientboundSetPlayerTeamPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetPlayerTeamPacket> STREAM_CODEC = Packet.codec(ClientboundSetPlayerTeamPacket::write, ClientboundSetPlayerTeamPacket::new);
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
    private final Optional<Parameters> parameters;

    private ClientboundSetPlayerTeamPacket(String $$0, int $$1, Optional<Parameters> $$2, Collection<String> $$3) {
        this.name = $$0;
        this.method = $$1;
        this.parameters = $$2;
        this.players = ImmutableList.copyOf($$3);
    }

    public static ClientboundSetPlayerTeamPacket createAddOrModifyPacket(PlayerTeam $$0, boolean $$1) {
        return new ClientboundSetPlayerTeamPacket($$0.getName(), $$1 ? 0 : 2, Optional.of(new Parameters($$0)), $$1 ? $$0.getPlayers() : ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createRemovePacket(PlayerTeam $$0) {
        return new ClientboundSetPlayerTeamPacket($$0.getName(), 1, Optional.empty(), (Collection<String>)ImmutableList.of());
    }

    public static ClientboundSetPlayerTeamPacket createPlayerPacket(PlayerTeam $$0, String $$1, Action $$2) {
        return new ClientboundSetPlayerTeamPacket($$0.getName(), $$2 == Action.ADD ? 3 : 4, Optional.empty(), (Collection<String>)ImmutableList.of((Object)$$1));
    }

    private ClientboundSetPlayerTeamPacket(RegistryFriendlyByteBuf $$0) {
        this.name = $$0.readUtf();
        this.method = $$0.readByte();
        this.parameters = ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method) ? Optional.of(new Parameters($$0)) : Optional.empty();
        this.players = ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method) ? $$0.readList(FriendlyByteBuf::readUtf) : ImmutableList.of();
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeUtf(this.name);
        $$0.writeByte(this.method);
        if (ClientboundSetPlayerTeamPacket.shouldHaveParameters(this.method)) {
            this.parameters.orElseThrow(() -> new IllegalStateException("Parameters not present, but method is" + this.method)).write($$0);
        }
        if (ClientboundSetPlayerTeamPacket.shouldHavePlayerList(this.method)) {
            $$0.writeCollection(this.players, FriendlyByteBuf::writeUtf);
        }
    }

    private static boolean shouldHavePlayerList(int $$0) {
        return $$0 == 0 || $$0 == 3 || $$0 == 4;
    }

    private static boolean shouldHaveParameters(int $$0) {
        return $$0 == 0 || $$0 == 2;
    }

    public @Nullable Action getPlayerAction() {
        return switch (this.method) {
            case 0, 3 -> Action.ADD;
            case 4 -> Action.REMOVE;
            default -> null;
        };
    }

    public @Nullable Action getTeamAction() {
        return switch (this.method) {
            case 0 -> Action.ADD;
            case 1 -> Action.REMOVE;
            default -> null;
        };
    }

    @Override
    public PacketType<ClientboundSetPlayerTeamPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_PLAYER_TEAM;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSetPlayerTeamPacket(this);
    }

    public String getName() {
        return this.name;
    }

    public Collection<String> getPlayers() {
        return this.players;
    }

    public Optional<Parameters> getParameters() {
        return this.parameters;
    }

    public static class Parameters {
        private final Component displayName;
        private final Component playerPrefix;
        private final Component playerSuffix;
        private final Team.Visibility nametagVisibility;
        private final Team.CollisionRule collisionRule;
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

        public Parameters(RegistryFriendlyByteBuf $$0) {
            this.displayName = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
            this.options = $$0.readByte();
            this.nametagVisibility = (Team.Visibility)Team.Visibility.STREAM_CODEC.decode($$0);
            this.collisionRule = (Team.CollisionRule)Team.CollisionRule.STREAM_CODEC.decode($$0);
            this.color = $$0.readEnum(ChatFormatting.class);
            this.playerPrefix = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
            this.playerSuffix = (Component)ComponentSerialization.TRUSTED_STREAM_CODEC.decode($$0);
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

        public Team.Visibility getNametagVisibility() {
            return this.nametagVisibility;
        }

        public Team.CollisionRule getCollisionRule() {
            return this.collisionRule;
        }

        public Component getPlayerPrefix() {
            return this.playerPrefix;
        }

        public Component getPlayerSuffix() {
            return this.playerSuffix;
        }

        public void write(RegistryFriendlyByteBuf $$0) {
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.displayName);
            $$0.writeByte(this.options);
            Team.Visibility.STREAM_CODEC.encode($$0, this.nametagVisibility);
            Team.CollisionRule.STREAM_CODEC.encode($$0, this.collisionRule);
            $$0.writeEnum(this.color);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.playerPrefix);
            ComponentSerialization.TRUSTED_STREAM_CODEC.encode($$0, this.playerSuffix);
        }
    }

    public static enum Action {
        ADD,
        REMOVE;

    }
}

