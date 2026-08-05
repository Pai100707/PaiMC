/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;

public class ClientboundSoundEntityPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSoundEntityPacket> STREAM_CODEC = Packet.codec(ClientboundSoundEntityPacket::write, ClientboundSoundEntityPacket::new);
    private final Holder<SoundEvent> sound;
    private final SoundSource source;
    private final int id;
    private final float volume;
    private final float pitch;
    private final long seed;

    public ClientboundSoundEntityPacket(Holder<SoundEvent> $$0, SoundSource $$1, Entity $$2, float $$3, float $$4, long $$5) {
        this.sound = $$0;
        this.source = $$1;
        this.id = $$2.getId();
        this.volume = $$3;
        this.pitch = $$4;
        this.seed = $$5;
    }

    private ClientboundSoundEntityPacket(RegistryFriendlyByteBuf $$0) {
        this.sound = (Holder)SoundEvent.STREAM_CODEC.decode($$0);
        this.source = $$0.readEnum(SoundSource.class);
        this.id = $$0.readVarInt();
        this.volume = $$0.readFloat();
        this.pitch = $$0.readFloat();
        this.seed = $$0.readLong();
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        SoundEvent.STREAM_CODEC.encode($$0, this.sound);
        $$0.writeEnum(this.source);
        $$0.writeVarInt(this.id);
        $$0.writeFloat(this.volume);
        $$0.writeFloat(this.pitch);
        $$0.writeLong(this.seed);
    }

    @Override
    public PacketType<ClientboundSoundEntityPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SOUND_ENTITY;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleSoundEntityEvent(this);
    }

    public Holder<SoundEvent> getSound() {
        return this.sound;
    }

    public SoundSource getSource() {
        return this.source;
    }

    public int getId() {
        return this.id;
    }

    public float getVolume() {
        return this.volume;
    }

    public float getPitch() {
        return this.pitch;
    }

    public long getSeed() {
        return this.seed;
    }
}

