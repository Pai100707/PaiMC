/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBlockEventPacket> STREAM_CODEC = Packet.codec(ClientboundBlockEventPacket::write, ClientboundBlockEventPacket::new);
    private final BlockPos pos;
    private final int b0;
    private final int b1;
    private final Block block;

    public ClientboundBlockEventPacket(BlockPos $$0, Block $$1, int $$2, int $$3) {
        this.pos = $$0;
        this.block = $$1;
        this.b0 = $$2;
        this.b1 = $$3;
    }

    private ClientboundBlockEventPacket(RegistryFriendlyByteBuf $$0) {
        this.pos = $$0.readBlockPos();
        this.b0 = $$0.readUnsignedByte();
        this.b1 = $$0.readUnsignedByte();
        this.block = (Block)ByteBufCodecs.registry(Registries.BLOCK).decode($$0);
    }

    private void write(RegistryFriendlyByteBuf $$0) {
        $$0.writeBlockPos(this.pos);
        $$0.writeByte(this.b0);
        $$0.writeByte(this.b1);
        ByteBufCodecs.registry(Registries.BLOCK).encode($$0, this.block);
    }

    @Override
    public PacketType<ClientboundBlockEventPacket> type() {
        return GamePacketTypes.CLIENTBOUND_BLOCK_EVENT;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleBlockEvent(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getB0() {
        return this.b0;
    }

    public int getB1() {
        return this.b1;
    }

    public Block getBlock() {
        return this.block;
    }
}

