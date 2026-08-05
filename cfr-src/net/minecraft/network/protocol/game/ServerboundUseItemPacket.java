/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundUseItemPacket> STREAM_CODEC = Packet.codec(ServerboundUseItemPacket::write, ServerboundUseItemPacket::new);
    private final InteractionHand hand;
    private final int sequence;
    private final float yRot;
    private final float xRot;

    public ServerboundUseItemPacket(InteractionHand $$0, int $$1, float $$2, float $$3) {
        this.hand = $$0;
        this.sequence = $$1;
        this.yRot = $$2;
        this.xRot = $$3;
    }

    private ServerboundUseItemPacket(FriendlyByteBuf $$0) {
        this.hand = $$0.readEnum(InteractionHand.class);
        this.sequence = $$0.readVarInt();
        this.yRot = $$0.readFloat();
        this.xRot = $$0.readFloat();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeEnum(this.hand);
        $$0.writeVarInt(this.sequence);
        $$0.writeFloat(this.yRot);
        $$0.writeFloat(this.xRot);
    }

    @Override
    public PacketType<ServerboundUseItemPacket> type() {
        return GamePacketTypes.SERVERBOUND_USE_ITEM;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleUseItem(this);
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public int getSequence() {
        return this.sequence;
    }

    public float getYRot() {
        return this.yRot;
    }

    public float getXRot() {
        return this.xRot;
    }
}

