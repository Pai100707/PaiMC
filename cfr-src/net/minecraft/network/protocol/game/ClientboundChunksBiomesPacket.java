/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public record ClientboundChunksBiomesPacket(List<ChunkBiomeData> chunkBiomeData) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundChunksBiomesPacket> STREAM_CODEC = Packet.codec(ClientboundChunksBiomesPacket::write, ClientboundChunksBiomesPacket::new);
    private static final int TWO_MEGABYTES = 0x200000;

    private ClientboundChunksBiomesPacket(FriendlyByteBuf $$0) {
        this($$0.readList(ChunkBiomeData::new));
    }

    public static ClientboundChunksBiomesPacket forChunks(List<LevelChunk> $$0) {
        return new ClientboundChunksBiomesPacket($$0.stream().map(ChunkBiomeData::new).toList());
    }

    private void write(FriendlyByteBuf $$02) {
        $$02.writeCollection(this.chunkBiomeData, ($$0, $$1) -> $$1.write((FriendlyByteBuf)((Object)$$0)));
    }

    @Override
    public PacketType<ClientboundChunksBiomesPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CHUNKS_BIOMES;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleChunksBiomes(this);
    }

    public record ChunkBiomeData(ChunkPos pos, byte[] buffer) {
        public ChunkBiomeData(LevelChunk $$0) {
            this($$0.getPos(), new byte[ChunkBiomeData.calculateChunkSize($$0)]);
            ChunkBiomeData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), $$0);
        }

        public ChunkBiomeData(FriendlyByteBuf $$0) {
            this($$0.readChunkPos(), $$0.readByteArray(0x200000));
        }

        private static int calculateChunkSize(LevelChunk $$0) {
            int $$1 = 0;
            for (LevelChunkSection $$2 : $$0.getSections()) {
                $$1 += $$2.getBiomes().getSerializedSize();
            }
            return $$1;
        }

        public FriendlyByteBuf getReadBuffer() {
            return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.buffer));
        }

        private ByteBuf getWriteBuffer() {
            ByteBuf $$0 = Unpooled.wrappedBuffer((byte[])this.buffer);
            $$0.writerIndex(0);
            return $$0;
        }

        public static void extractChunkData(FriendlyByteBuf $$0, LevelChunk $$1) {
            for (LevelChunkSection $$2 : $$1.getSections()) {
                $$2.getBiomes().write($$0);
            }
            if ($$0.writerIndex() != $$0.capacity()) {
                throw new IllegalStateException("Didn't fill biome buffer: expected " + $$0.capacity() + " bytes, got " + $$0.writerIndex());
            }
        }

        public void write(FriendlyByteBuf $$0) {
            $$0.writeChunkPos(this.pos);
            $$0.writeByteArray(this.buffer);
        }
    }
}

