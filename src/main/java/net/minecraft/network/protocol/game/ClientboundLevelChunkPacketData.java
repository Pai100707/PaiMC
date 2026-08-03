package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.jspecify.annotations.Nullable;

public class ClientboundLevelChunkPacketData {
   private static final StreamCodec<ByteBuf, Map<Types, long[]>> HEIGHTMAPS_STREAM_CODEC = ByteBufCodecs.map(
      $$0 -> new EnumMap(Types.class), Types.STREAM_CODEC, ByteBufCodecs.LONG_ARRAY
   );
   private static final int TWO_MEGABYTES = 2097152;
   private final Map<Types, long[]> heightmaps;
   private final byte[] buffer;
   private final List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;

   public ClientboundLevelChunkPacketData(LevelChunk $$0) {
      this.heightmaps = $$0.getHeightmaps()
         .stream()
         .filter($$0x -> ((Types)$$0x.getKey()).sendToClient())
         .collect(Collectors.toMap(Entry::getKey, $$0x -> (long[])((Heightmap)$$0x.getValue()).getRawData().clone()));
      this.buffer = new byte[calculateChunkSize($$0)];
      extractChunkData(new net.minecraft.network.FriendlyByteBuf(this.getWriteBuffer()), $$0);
      this.blockEntitiesData = Lists.newArrayList();

      for (Entry<BlockPos, BlockEntity> $$1 : $$0.getBlockEntities().entrySet()) {
         this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create($$1.getValue()));
      }
   }

   public ClientboundLevelChunkPacketData(net.minecraft.network.RegistryFriendlyByteBuf $$0, int $$1, int $$2) {
      this.heightmaps = HEIGHTMAPS_STREAM_CODEC.decode($$0);
      int $$3 = $$0.readVarInt();
      if ($$3 > 2097152) {
         throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
      } else {
         this.buffer = new byte[$$3];
         $$0.readBytes(this.buffer);
         this.blockEntitiesData = ClientboundLevelChunkPacketData.BlockEntityInfo.LIST_STREAM_CODEC.decode($$0);
      }
   }

   public void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
      HEIGHTMAPS_STREAM_CODEC.encode($$0, this.heightmaps);
      $$0.writeVarInt(this.buffer.length);
      $$0.writeBytes(this.buffer);
      ClientboundLevelChunkPacketData.BlockEntityInfo.LIST_STREAM_CODEC.encode($$0, this.blockEntitiesData);
   }

   private static int calculateChunkSize(LevelChunk $$0) {
      int $$1 = 0;

      for (LevelChunkSection $$2 : $$0.getSections()) {
         $$1 += $$2.getSerializedSize();
      }

      return $$1;
   }

   private ByteBuf getWriteBuffer() {
      ByteBuf $$0 = Unpooled.wrappedBuffer(this.buffer);
      $$0.writerIndex(0);
      return $$0;
   }

   public static void extractChunkData(net.minecraft.network.FriendlyByteBuf $$0, LevelChunk $$1) {
      for (LevelChunkSection $$2 : $$1.getSections()) {
         $$2.write($$0);
      }

      if ($$0.writerIndex() != $$0.capacity()) {
         throw new IllegalStateException("Didn't fill chunk buffer: expected " + $$0.capacity() + " bytes, got " + $$0.writerIndex());
      }
   }

   public Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int $$0, int $$1) {
      return $$2 -> this.getBlockEntitiesTags($$2, $$0, $$1);
   }

   private void getBlockEntitiesTags(ClientboundLevelChunkPacketData.BlockEntityTagOutput $$0, int $$1, int $$2) {
      int $$3 = 16 * $$1;
      int $$4 = 16 * $$2;
      MutableBlockPos $$5 = new MutableBlockPos();

      for (ClientboundLevelChunkPacketData.BlockEntityInfo $$6 : this.blockEntitiesData) {
         int $$7 = $$3 + SectionPos.sectionRelative($$6.packedXZ >> 4);
         int $$8 = $$4 + SectionPos.sectionRelative($$6.packedXZ);
         $$5.set($$7, $$6.y, $$8);
         $$0.accept($$5, $$6.type, $$6.tag);
      }
   }

   public net.minecraft.network.FriendlyByteBuf getReadBuffer() {
      return new net.minecraft.network.FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
   }

   public Map<Types, long[]> getHeightmaps() {
      return this.heightmaps;
   }

   static class BlockEntityInfo {
      public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, ClientboundLevelChunkPacketData.BlockEntityInfo> STREAM_CODEC = StreamCodec.ofMember(
         ClientboundLevelChunkPacketData.BlockEntityInfo::write, ClientboundLevelChunkPacketData.BlockEntityInfo::new
      );
      public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, List<ClientboundLevelChunkPacketData.BlockEntityInfo>> LIST_STREAM_CODEC = STREAM_CODEC.apply(
         ByteBufCodecs.list()
      );
      final int packedXZ;
      final int y;
      final BlockEntityType<?> type;
      @Nullable
      final CompoundTag tag;

      private BlockEntityInfo(int $$0, int $$1, BlockEntityType<?> $$2, @Nullable CompoundTag $$3) {
         this.packedXZ = $$0;
         this.y = $$1;
         this.type = $$2;
         this.tag = $$3;
      }

      private BlockEntityInfo(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         this.packedXZ = $$0.readByte();
         this.y = $$0.readShort();
         this.type = (BlockEntityType<?>)ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).decode($$0);
         this.tag = $$0.readNbt();
      }

      private void write(net.minecraft.network.RegistryFriendlyByteBuf $$0) {
         $$0.writeByte(this.packedXZ);
         $$0.writeShort(this.y);
         ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).encode($$0, this.type);
         $$0.writeNbt(this.tag);
      }

      static ClientboundLevelChunkPacketData.BlockEntityInfo create(BlockEntity $$0) {
         CompoundTag $$1 = $$0.getUpdateTag($$0.getLevel().registryAccess());
         BlockPos $$2 = $$0.getBlockPos();
         int $$3 = SectionPos.sectionRelative($$2.getX()) << 4 | SectionPos.sectionRelative($$2.getZ());
         return new ClientboundLevelChunkPacketData.BlockEntityInfo($$3, $$2.getY(), $$0.getType(), $$1.isEmpty() ? null : $$1);
      }
   }

   @FunctionalInterface
   public interface BlockEntityTagOutput {
      void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
   }
}
