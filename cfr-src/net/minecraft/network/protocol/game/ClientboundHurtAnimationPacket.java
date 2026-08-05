/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundHurtAnimationPacket> STREAM_CODEC = Packet.codec(ClientboundHurtAnimationPacket::write, ClientboundHurtAnimationPacket::new);

    public ClientboundHurtAnimationPacket(LivingEntity $$0) {
        this($$0.getId(), $$0.getHurtDir());
    }

    private ClientboundHurtAnimationPacket(FriendlyByteBuf $$0) {
        this($$0.readVarInt(), $$0.readFloat());
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeVarInt(this.id);
        $$0.writeFloat(this.yaw);
    }

    @Override
    public PacketType<ClientboundHurtAnimationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_HURT_ANIMATION;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleHurtAnimation(this);
    }
}

