/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundOpenSignEditorPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundOpenSignEditorPacket> STREAM_CODEC = Packet.codec(ClientboundOpenSignEditorPacket::write, ClientboundOpenSignEditorPacket::new);
    private final BlockPos pos;
    private final boolean isFrontText;

    public ClientboundOpenSignEditorPacket(BlockPos $$0, boolean $$1) {
        this.pos = $$0;
        this.isFrontText = $$1;
    }

    private ClientboundOpenSignEditorPacket(FriendlyByteBuf $$0) {
        this.pos = $$0.readBlockPos();
        this.isFrontText = $$0.readBoolean();
    }

    private void write(FriendlyByteBuf $$0) {
        $$0.writeBlockPos(this.pos);
        $$0.writeBoolean(this.isFrontText);
    }

    @Override
    public PacketType<ClientboundOpenSignEditorPacket> type() {
        return GamePacketTypes.CLIENTBOUND_OPEN_SIGN_EDITOR;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleOpenSignEditor(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isFrontText() {
        return this.isFrontText;
    }
}

