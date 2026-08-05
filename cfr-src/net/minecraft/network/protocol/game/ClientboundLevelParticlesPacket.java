/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundLevelParticlesPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelParticlesPacket> STREAM_CODEC = Packet.codec(ClientboundLevelParticlesPacket::write, ClientboundLevelParticlesPacket::new);
    private final double x;
    private final double y;
    private final double z;
    private final float xDist;
    private final float yDist;
    private final float zDist;
    private final float maxSpeed;
    private final int count;
    private final boolean overrideLimiter;
    private final boolean alwaysShow;
    private final ParticleOptions particle;

    public <T extends ParticleOptions> ClientboundLevelParticlesPacket(T $$0, boolean $$1, boolean $$2, double $$3, double $$4, double $$5, float $$6, float $$7, float $$8, float $$9, int $$10) {
        this.particle = $$0;
        this.overrideLimiter = $$1;
        this.alwaysShow = $$2;
        this.x = $$3;
        this.y = $$4;
        this.z = $$5;
        this.xDist = $$6;
        this.yDist = $$7;
        this.zDist = $$8;
        this.maxSpeed = $$9;
        this.count = $$10;
    }

    private ClientboundLevelParticlesPacket(RegistryFriendlyByteBuf $$0) {
        this.overrideLimiter = $$0.readBoolean();
        this.alwaysShow = $$0.readBoolean();
        this.x = $$0.readDouble();
        this.y = $$0.readDouble();
        this.z = $$0.readDouble();
        this.xDist = $$0.readFloat();
        this.yDist = $$0.readFloat();
        this.zDist = $$0.readFloat();
        this.maxSpeed = $$0.readFloat();
        this.count = $$0.readInt();
        this.particle = (ParticleOptions)ParticleTypes.STREAM_CODEC.decode($$0);
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeBoolean(this.overrideLimiter);
        $$0.writeBoolean(this.alwaysShow);
        $$0.writeDouble(this.x);
        $$0.writeDouble(this.y);
        $$0.writeDouble(this.z);
        $$0.writeFloat(this.xDist);
        $$0.writeFloat(this.yDist);
        $$0.writeFloat(this.zDist);
        $$0.writeFloat(this.maxSpeed);
        $$0.writeInt(this.count);
        ParticleTypes.STREAM_CODEC.encode($$0, this.particle);
    }

    @Override
    public PacketType<ClientboundLevelParticlesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_LEVEL_PARTICLES;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleParticleEvent(this);
    }

    public boolean isOverrideLimiter() {
        return this.overrideLimiter;
    }

    public boolean alwaysShow() {
        return this.alwaysShow;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getXDist() {
        return this.xDist;
    }

    public float getYDist() {
        return this.yDist;
    }

    public float getZDist() {
        return this.zDist;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    public int getCount() {
        return this.count;
    }

    public ParticleOptions getParticle() {
        return this.particle;
    }
}

