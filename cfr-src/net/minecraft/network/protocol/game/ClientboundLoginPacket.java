/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record ClientboundLoginPacket(int playerId, boolean hardcore, Set<ResourceKey<Level>> levels, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting, CommonPlayerSpawnInfo commonPlayerSpawnInfo, boolean enforcesSecureChat) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLoginPacket> STREAM_CODEC = Packet.codec(ClientboundLoginPacket::write, ClientboundLoginPacket::new);

    private ClientboundLoginPacket(RegistryFriendlyByteBuf $$02) {
        this($$02.readInt(), $$02.readBoolean(), $$02.readCollection(Sets::newHashSetWithExpectedSize, $$0 -> $$0.readResourceKey(Registries.DIMENSION)), $$02.readVarInt(), $$02.readVarInt(), $$02.readVarInt(), $$02.readBoolean(), $$02.readBoolean(), $$02.readBoolean(), new CommonPlayerSpawnInfo($$02), $$02.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeInt(this.playerId);
        $$0.writeBoolean(this.hardcore);
        $$0.writeCollection(this.levels, FriendlyByteBuf::writeResourceKey);
        $$0.writeVarInt(this.maxPlayers);
        $$0.writeVarInt(this.chunkRadius);
        $$0.writeVarInt(this.simulationDistance);
        $$0.writeBoolean(this.reducedDebugInfo);
        $$0.writeBoolean(this.showDeathScreen);
        $$0.writeBoolean(this.doLimitedCrafting);
        this.commonPlayerSpawnInfo.write($$0);
        $$0.writeBoolean(this.enforcesSecureChat);
    }

    @Override
    public PacketType<ClientboundLoginPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LOGIN;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleLogin(this);
    }
}

