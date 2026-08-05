/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public class ClientboundLevelChunkPacketData {
    private static final StreamCodec<ByteBuf, Map<Heightmap.Types, long[]>> HEIGHTMAPS_STREAM_CODEC = ByteBufCodecs.map($$0 -> new EnumMap(Heightmap.Types.class), Heightmap.Types.STREAM_CODEC, ByteBufCodecs.LONG_ARRAY);
    private static final int TWO_MEGABYTES = 0x200000;
    private final Map<Heightmap.Types, long[]> heightmaps;
    private final byte[] buffer;
    private final List<BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk $$02) {
        this.heightmaps = $$02.getHeightmaps().stream().filter($$0 -> ((Heightmap.Types)$$0.getKey()).sendToClient()).collect(Collectors.toMap(Map.Entry::getKey, $$0 -> (long[])((Heightmap)$$0.getValue()).getRawData().clone()));
        this.buffer = new byte[ClientboundLevelChunkPacketData.calculateChunkSize($$02)];
        ClientboundLevelChunkPacketData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), $$02);
        this.blockEntitiesData = Lists.newArrayList();
        for (Map.Entry<BlockPos, BlockEntity> $$1 : $$02.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(BlockEntityInfo.create($$1.getValue()));
        }
    }

    public ClientboundLevelChunkPacketData(RegistryFriendlyByteBuf $$0, int $$1, int $$2) {
        this.heightmaps = (Map)HEIGHTMAPS_STREAM_CODEC.decode($$0);
        int $$3 = $$0.readVarInt();
        if ($$3 > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.buffer = new byte[$$3];
        $$0.readBytes(this.buffer);
        this.blockEntitiesData = (List)BlockEntityInfo.LIST_STREAM_CODEC.decode($$0);
    }

    public void write(RegistryFriendlyByteBuf $$0) {
        HEIGHTMAPS_STREAM_CODEC.encode($$0, this.heightmaps);
        $$0.writeVarInt(this.buffer.length);
        $$0.writeBytes(this.buffer);
        BlockEntityInfo.LIST_STREAM_CODEC.encode($$0, this.blockEntitiesData);
    }

    private static int calculateChunkSize(LevelChunk $$0) {
        int $$1 = 0;
        for (LevelChunkSection $$2 : $$0.getSections()) {
            $$1 += $$2.getSerializedSize();
        }
        return $$1;
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf $$0 = Unpooled.wrappedBuffer((byte[])this.buffer);
        $$0.writerIndex(0);
        return $$0;
    }

    public static void extractChunkData(FriendlyByteBuf $$0, LevelChunk $$1) {
        for (LevelChunkSection $$2 : $$1.getSections()) {
            $$2.write($$0);
        }
        if ($$0.writerIndex() != $$0.capacity()) {
            throw new IllegalStateException("Didn't fill chunk buffer: expected " + $$0.capacity() + " bytes, got " + $$0.writerIndex());
        }
    }

    public Consumer<BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int $$0, int $$1) {
        return $$2 -> this.getBlockEntitiesTags((BlockEntityTagOutput)$$2, $$0, $$1);
    }

    private void getBlockEntitiesTags(BlockEntityTagOutput $$0, int $$1, int $$2) {
        int $$3 = 16 * $$1;
        int $$4 = 16 * $$2;
        BlockPos.MutableBlockPos $$5 = new BlockPos.MutableBlockPos();
        for (BlockEntityInfo $$6 : this.blockEntitiesData) {
            int $$7 = $$3 + SectionPos.sectionRelative($$6.packedXZ >> 4);
            int $$8 = $$4 + SectionPos.sectionRelative($$6.packedXZ);
            $$5.set($$7, $$6.y, $$8);
            $$0.accept($$5, $$6.type, $$6.tag);
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.buffer));
    }

    public Map<Heightmap.Types, long[]> getHeightmaps() {
        return this.heightmaps;
    }

    static class BlockEntityInfo {
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityInfo> STREAM_CODEC = StreamCodec.ofMember(BlockEntityInfo::write, BlockEntityInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, List<BlockEntityInfo>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
        final int packedXZ;
        final int y;
        final BlockEntityType<?> type;
        final @Nullable CompoundTag tag;

        private BlockEntityInfo(int $$0, int $$1, BlockEntityType<?> $$2, @Nullable CompoundTag $$3) {
            this.packedXZ = $$0;
            this.y = $$1;
            this.type = $$2;
            this.tag = $$3;
        }

        private BlockEntityInfo(RegistryFriendlyByteBuf $$0) {
            this.packedXZ = $$0.readByte();
            this.y = $$0.readShort();
            this.type = (BlockEntityType)ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).decode($$0);
            this.tag = $$0.readNbt();
        }

        private void write(RegistryFriendlyByteBuf $$0) {
            $$0.writeByte(this.packedXZ);
            $$0.writeShort(this.y);
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).encode($$0, this.type);
            $$0.writeNbt(this.tag);
        }

        static BlockEntityInfo create(BlockEntity $$0) {
            CompoundTag $$1 = $$0.getUpdateTag($$0.getLevel().registryAccess());
            BlockPos $$2 = $$0.getBlockPos();
            int $$3 = SectionPos.sectionRelative($$2.getX()) << 4 | SectionPos.sectionRelative($$2.getZ());
            return new BlockEntityInfo($$3, $$2.getY(), $$0.getType(), $$1.isEmpty() ? null : $$1);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityTagOutput {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}

