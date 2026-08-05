/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.configuration;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.resources.Identifier;

public record ClientboundUpdateEnabledFeaturesPacket(Set<Identifier> features) implements Packet<ClientConfigurationPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateEnabledFeaturesPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateEnabledFeaturesPacket::write, ClientboundUpdateEnabledFeaturesPacket::new);

    private ClientboundUpdateEnabledFeaturesPacket(FriendlyByteBuf $$0) {
        this($$0.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier));
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeCollection(this.features, FriendlyByteBuf::writeIdentifier);
    }

    @Override
    public PacketType<ClientboundUpdateEnabledFeaturesPacket> type() {
        return ConfigurationPacketTypes.CLIENTBOUND_UPDATE_ENABLED_FEATURES;
    }

    @Override
    public void handle(ClientConfigurationPacketListener $$0) {
        $$0.handleEnabledFeatures(this);
    }
}

