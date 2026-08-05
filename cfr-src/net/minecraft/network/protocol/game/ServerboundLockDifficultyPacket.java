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

public class ServerboundLockDifficultyPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundLockDifficultyPacket> STREAM_CODEC = Packet.codec(ServerboundLockDifficultyPacket::write, ServerboundLockDifficultyPacket::new);
    private final boolean locked;

    public ServerboundLockDifficultyPacket(boolean $$0) {
        this.locked = $$0;
    }

    private ServerboundLockDifficultyPacket(FriendlyByteBuf $$0) {
        this.locked = $$0.readBoolean();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBoolean(this.locked);
    }

    @Override
    public PacketType<ServerboundLockDifficultyPacket> type() {
        return GamePacketTypes.SERVERBOUND_LOCK_DIFFICULTY;
    }

    @Override
    public void handle(ServerGamePacketListener $$0) {
        $$0.handleLockDifficulty(this);
    }

    public boolean isLocked() {
        return this.locked;
    }
}

