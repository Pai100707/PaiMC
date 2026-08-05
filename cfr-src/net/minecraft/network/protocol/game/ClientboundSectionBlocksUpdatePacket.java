/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.shorts.ShortIterator
 *  it.unimi.dsi.fastutil.shorts.ShortSet
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class ClientboundSectionBlocksUpdatePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSectionBlocksUpdatePacket> STREAM_CODEC = Packet.codec(ClientboundSectionBlocksUpdatePacket::write, ClientboundSectionBlocksUpdatePacket::new);
    private static final int POS_IN_SECTION_BITS = 12;
    private final SectionPos sectionPos;
    private final short[] positions;
    private final BlockState[] states;

    public ClientboundSectionBlocksUpdatePacket(SectionPos $$0, ShortSet $$1, LevelChunkSection $$2) {
        this.sectionPos = $$0;
        int $$3 = $$1.size();
        this.positions = new short[$$3];
        this.states = new BlockState[$$3];
        int $$4 = 0;
        ShortIterator shortIterator = $$1.iterator();
        while (shortIterator.hasNext()) {
            short $$5;
            this.positions[$$4] = $$5 = ((Short)shortIterator.next()).shortValue();
            this.states[$$4] = $$2.getBlockState(SectionPos.sectionRelativeX($$5), SectionPos.sectionRelativeY($$5), SectionPos.sectionRelativeZ($$5));
            ++$$4;
        }
    }

    private ClientboundSectionBlocksUpdatePacket(FriendlyByteBuf $$0) {
        this.sectionPos = (SectionPos)SectionPos.STREAM_CODEC.decode($$0);
        int $$1 = $$0.readVarInt();
        this.positions = new short[$$1];
        this.states = new BlockState[$$1];
        for (int $$2 = 0; $$2 < $$1; ++$$2) {
            long $$3 = $$0.readVarLong();
            this.positions[$$2] = (short)($$3 & 0xFFFL);
            this.states[$$2] = Block.BLOCK_STATE_REGISTRY.byId((int)($$3 >>> 12));
        }
    }

    private void write(FriendlyByteBuf $$0) {
        SectionPos.STREAM_CODEC.encode($$0, this.sectionPos);
        $$0.writeVarInt(this.positions.length);
        for (int $$1 = 0; $$1 < this.positions.length; ++$$1) {
            $$0.writeVarLong((long)Block.getId(this.states[$$1]) << 12 | (long)this.positions[$$1]);
        }
    }

    @Override
    public PacketType<ClientboundSectionBlocksUpdatePacket> type() {
        return GamePacketTypes.CLIENTBOUND_SECTION_BLOCKS_UPDATE;
    }

    @Override
    public void handle(ClientGamePacketListener $$0) {
        $$0.handleChunkBlocksUpdate(this);
    }

    public void runUpdates(BiConsumer<BlockPos, BlockState> $$0) {
        BlockPos.MutableBlockPos $$1 = new BlockPos.MutableBlockPos();
        for (int $$2 = 0; $$2 < this.positions.length; ++$$2) {
            short $$3 = this.positions[$$2];
            $$1.set(this.sectionPos.relativeToBlockX($$3), this.sectionPos.relativeToBlockY($$3), this.sectionPos.relativeToBlockZ($$3));
            $$0.accept($$1, this.states[$$2]);
        }
    }
}

