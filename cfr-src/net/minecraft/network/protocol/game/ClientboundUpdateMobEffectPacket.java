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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateMobEffectPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateMobEffectPacket::write, ClientboundUpdateMobEffectPacket::new);
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private static final int FLAG_BLEND = 8;
    private final int entityId;
    private final Holder<MobEffect> effect;
    private final int effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;

    public ClientboundUpdateMobEffectPacket(int $$0, MobEffectInstance $$1, boolean $$2) {
        this.entityId = $$0;
        this.effect = $$1.getEffect();
        this.effectAmplifier = $$1.getAmplifier();
        this.effectDurationTicks = $$1.getDuration();
        byte $$3 = 0;
        if ($$1.isAmbient()) {
            $$3 = (byte)($$3 | 1);
        }
        if ($$1.isVisible()) {
            $$3 = (byte)($$3 | 2);
        }
        if ($$1.showIcon()) {
            $$3 = (byte)($$3 | 4);
        }
        if ($$2) {
            $$3 = (byte)($$3 | 8);
        }
        this.flags = $$3;
    }

    private ClientboundUpdateMobEffectPacket(RegistryFriendlyByteBuf $$0) {
        this.entityId = $$0.readVarInt();
        this.effect = (Holder)MobEffect.STREAM_CODEC.decode($$0);
        this.effectAmplifier = $$0.readVarInt();
        this.effectDurationTicks = $$0.readVarInt();
        this.flags = $$0.readByte();
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeVarInt(this.entityId);
        MobEffect.STREAM_CODEC.encode($$0, this.effect);
        $$0.writeVarInt(this.effectAmplifier);
        $$0.writeVarInt(this.effectDurationTicks);
        $$0.writeByte(this.flags);
    }

    @Override
    public PacketType<ClientboundUpdateMobEffectPacket> type() {
        return GamePacketTypes.CLIENTBOUND_UPDATE_MOB_EFFECT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Holder<MobEffect> getEffect() {
        return this.effect;
    }

    public int getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    public boolean isEffectVisible() {
        return (this.flags & 2) != 0;
    }

    public boolean isEffectAmbient() {
        return (this.flags & 1) != 0;
    }

    public boolean effectShowsIcon() {
        return (this.flags & 4) != 0;
    }

    public boolean shouldBlend() {
        return (this.flags & 8) != 0;
    }
}

