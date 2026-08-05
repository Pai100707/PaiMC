package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.Optionull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public record SerializableChunkData(
   PalettedContainerFactory containerFactory,
   net.minecraft.world.level.ChunkPos chunkPos,
   int minSectionY,
   long lastUpdateTime,
   long inhabitedTime,
   ChunkStatus chunkStatus,
   BlendingData.Packed blendingData,
   BelowZeroRetrogen belowZeroRetrogen,
   UpgradeData upgradeData,
   long[] carvingMask,
   Map<Heightmap.Types, long[]> heightmaps,
   ChunkAccess.PackedTicks packedTicks,
   ShortList[] postProcessingSections,
   boolean lightCorrect,
   List<SerializableChunkData.SectionData> sectionData,
   List<CompoundTag> entities,
   List<CompoundTag> blockEntities,
   CompoundTag structureData
) {
   private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec()).listOf();
   private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec()).listOf();
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final String TAG_UPGRADE_DATA = "UpgradeData";
   private static final String BLOCK_TICKS_TAG = "block_ticks";
   private static final String FLUID_TICKS_TAG = "fluid_ticks";
   public static final String X_POS_TAG = "xPos";
   public static final String Z_POS_TAG = "zPos";
   public static final String HEIGHTMAPS_TAG = "Heightmaps";
   public static final String IS_LIGHT_ON_TAG = "isLightOn";
   public static final String SECTIONS_TAG = "sections";
   public static final String BLOCK_LIGHT_TAG = "BlockLight";
   public static final String SKY_LIGHT_TAG = "SkyLight";

   public static SerializableChunkData parse(net.minecraft.world.level.LevelHeightAccessor $$0, PalettedContainerFactory $$1, CompoundTag $$2) {
      if ($$2.getString("Status").isEmpty()) {
         return null;
      } else {
         net.minecraft.world.level.ChunkPos $$3 = new net.minecraft.world.level.ChunkPos($$2.getIntOr("xPos", 0), $$2.getIntOr("zPos", 0));
         long $$4 = $$2.getLongOr("LastUpdate", 0L);
         long $$5 = $$2.getLongOr("InhabitedTime", 0L);
         ChunkStatus $$6 = $$2.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY);
         UpgradeData $$7 = $$2.getCompound("UpgradeData").map($$1x -> new UpgradeData($$1x, $$0)).orElse(UpgradeData.EMPTY);
         boolean $$8 = $$2.getBooleanOr("isLightOn", false);
         BlendingData.Packed $$9 = (BlendingData.Packed)$$2.read("blending_data", BlendingData.Packed.CODEC).orElse(null);
         BelowZeroRetrogen $$10 = (BelowZeroRetrogen)$$2.read("below_zero_retrogen", BelowZeroRetrogen.CODEC).orElse(null);
         long[] $$11 = (long[])$$2.getLongArray("carving_mask").orElse(null);
         Map<Heightmap.Types, long[]> $$12 = new EnumMap<>(Heightmap.Types.class);
         $$2.getCompound("Heightmaps").ifPresent($$2x -> {
            for (Heightmap.Types $$3x : $$6.heightmapsAfter()) {
               $$2x.getLongArray($$3x.getSerializationKey()).ifPresent($$2xx -> $$12.put($$3x, $$2xx));
            }
         });
         List<SavedTick<Block>> $$13 = SavedTick.filterTickListForChunk($$2.read("block_ticks", BLOCK_TICKS_CODEC).orElse(List.of()), $$3);
         List<SavedTick<Fluid>> $$14 = SavedTick.filterTickListForChunk($$2.read("fluid_ticks", FLUID_TICKS_CODEC).orElse(List.of()), $$3);
         ChunkAccess.PackedTicks $$15 = new ChunkAccess.PackedTicks($$13, $$14);
         ListTag $$16 = $$2.getListOrEmpty("PostProcessing");
         ShortList[] $$17 = new ShortList[$$16.size()];

         for (int $$18 = 0; $$18 < $$16.size(); $$18++) {
            ListTag $$19 = (ListTag)$$16.getList($$18).orElse(null);
            if ($$19 != null && !$$19.isEmpty()) {
               ShortList $$20 = new ShortArrayList($$19.size());

               for (int $$21 = 0; $$21 < $$19.size(); $$21++) {
                  $$20.add($$19.getShortOr($$21, (short)0));
               }

               $$17[$$18] = $$20;
            }
         }

         List<CompoundTag> $$22 = $$2.getList("entities").stream().<CompoundTag>flatMap(ListTag::compoundStream).toList();
         List<CompoundTag> $$23 = $$2.getList("block_entities").stream().<CompoundTag>flatMap(ListTag::compoundStream).toList();
         CompoundTag $$24 = $$2.getCompoundOrEmpty("structures");
         ListTag $$25 = $$2.getListOrEmpty("sections");
         List<SerializableChunkData.SectionData> $$26 = new ArrayList<>($$25.size());
         Codec<PalettedContainerRO<Holder<Biome>>> $$27 = $$1.biomeContainerCodec();
         Codec<PalettedContainer<BlockState>> $$28 = $$1.blockStatesContainerCodec();

         for (int $$29 = 0; $$29 < $$25.size(); $$29++) {
            Optional<CompoundTag> $$30 = $$25.getCompound($$29);
            if (!$$30.isEmpty()) {
               CompoundTag $$31 = $$30.get();
               int $$32 = $$31.getByteOr("Y", (byte)0);
               LevelChunkSection $$35;
               if ($$32 >= $$0.getMinSectionY() && $$32 <= $$0.getMaxSectionY()) {
                  PalettedContainer<BlockState> $$33 = $$31.getCompound("block_states")
                     .map(
                        $$3x -> (PalettedContainer<BlockState>)$$28.parse(NbtOps.INSTANCE, $$3x)
                           .promotePartial($$2xx -> logErrors($$3, $$32, $$2xx))
                           .getOrThrow(SerializableChunkData.ChunkReadException::new)
                     )
                     .orElseGet($$1::createForBlockStates);
                  PalettedContainerRO<Holder<Biome>> $$34 = $$31.getCompound("biomes")
                     .map(
                        $$3x -> (PalettedContainerRO<Holder<Biome>>)$$27.parse(NbtOps.INSTANCE, $$3x)
                           .promotePartial($$2xx -> logErrors($$3, $$32, $$2xx))
                           .getOrThrow(SerializableChunkData.ChunkReadException::new)
                     )
                     .orElseGet($$1::createForBiomes);
                  $$35 = new LevelChunkSection($$33, $$34);
               } else {
                  $$35 = null;
               }

               DataLayer $$37 = $$31.getByteArray("BlockLight").map(DataLayer::new).orElse(null);
               DataLayer $$38 = $$31.getByteArray("SkyLight").map(DataLayer::new).orElse(null);
               $$26.add(new SerializableChunkData.SectionData($$32, $$35, $$37, $$38));
            }
         }

         return new SerializableChunkData($$1, $$3, $$0.getMinSectionY(), $$4, $$5, $$6, $$9, $$10, $$7, $$11, $$12, $$15, $$17, $$8, $$26, $$22, $$23, $$24);
      }
   }

   public ProtoChunk read(ServerLevel $$0, PoiManager $$1, RegionStorageInfo $$2, net.minecraft.world.level.ChunkPos $$3) {
      if (!Objects.equals($$3, this.chunkPos)) {
         LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", new Object[]{$$3, $$3, this.chunkPos});
         $$0.getServer().reportMisplacedChunk(this.chunkPos, $$3, $$2);
      }

      int $$4 = $$0.getSectionsCount();
      LevelChunkSection[] $$5 = new LevelChunkSection[$$4];
      boolean $$6 = $$0.dimensionType().hasSkyLight();
      ChunkSource $$7 = $$0.getChunkSource();
      LevelLightEngine $$8 = $$7.getLightEngine();
      PalettedContainerFactory $$9 = $$0.palettedContainerFactory();
      boolean $$10 = false;

      for (SerializableChunkData.SectionData $$11 : this.sectionData) {
         SectionPos $$12 = SectionPos.of($$3, $$11.y);
         if ($$11.chunkSection != null) {
            $$5[$$0.getSectionIndexFromSectionY($$11.y)] = $$11.chunkSection;
            $$1.checkConsistencyWithBlocks($$12, $$11.chunkSection);
         }

         boolean $$13 = $$11.blockLight != null;
         boolean $$14 = $$6 && $$11.skyLight != null;
         if ($$13 || $$14) {
            if (!$$10) {
               $$8.retainData($$3, true);
               $$10 = true;
            }

            if ($$13) {
               $$8.queueSectionData(net.minecraft.world.level.LightLayer.BLOCK, $$12, $$11.blockLight);
            }

            if ($$14) {
               $$8.queueSectionData(net.minecraft.world.level.LightLayer.SKY, $$12, $$11.skyLight);
            }
         }
      }

      ChunkType $$15 = this.chunkStatus.getChunkType();
      ChunkAccess $$18;
      if ($$15 == ChunkType.LEVELCHUNK) {
         LevelChunkTicks<Block> $$16 = new LevelChunkTicks(this.packedTicks.blocks());
         LevelChunkTicks<Fluid> $$17 = new LevelChunkTicks(this.packedTicks.fluids());
         $$18 = new LevelChunk(
            $$0.getLevel(),
            $$3,
            this.upgradeData,
            $$16,
            $$17,
            this.inhabitedTime,
            $$5,
            postLoadChunk($$0, this.entities, this.blockEntities),
            BlendingData.unpack(this.blendingData)
         );
      } else {
         ProtoChunkTicks<Block> $$19 = ProtoChunkTicks.load(this.packedTicks.blocks());
         ProtoChunkTicks<Fluid> $$20 = ProtoChunkTicks.load(this.packedTicks.fluids());
         ProtoChunk $$21 = new ProtoChunk($$3, this.upgradeData, $$5, $$19, $$20, $$0, $$9, BlendingData.unpack(this.blendingData));
         $$18 = $$21;
         $$21.setInhabitedTime(this.inhabitedTime);
         if (this.belowZeroRetrogen != null) {
            $$21.setBelowZeroRetrogen(this.belowZeroRetrogen);
         }

         $$21.setPersistedStatus(this.chunkStatus);
         if (this.chunkStatus.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
            $$21.setLightEngine($$8);
         }
      }

      $$18.setLightCorrect(this.lightCorrect);
      EnumSet<Heightmap.Types> $$23 = EnumSet.noneOf(Heightmap.Types.class);

      for (Heightmap.Types $$24 : $$18.getPersistedStatus().heightmapsAfter()) {
         long[] $$25 = this.heightmaps.get($$24);
         if ($$25 != null) {
            $$18.setHeightmap($$24, $$25);
         } else {
            $$23.add($$24);
         }
      }

      Heightmap.primeHeightmaps($$18, $$23);
      $$18.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel($$0), this.structureData, $$0.getSeed()));
      $$18.setAllReferences(unpackStructureReferences($$0.registryAccess(), $$3, this.structureData));

      for (int $$26 = 0; $$26 < this.postProcessingSections.length; $$26++) {
         ShortList $$27 = this.postProcessingSections[$$26];
         if ($$27 != null) {
            $$18.addPackedPostProcess($$27, $$26);
         }
      }

      if ($$15 == ChunkType.LEVELCHUNK) {
         return new ImposterProtoChunk((LevelChunk)$$18, false);
      } else {
         ProtoChunk $$28 = (ProtoChunk)$$18;

         for (CompoundTag $$29 : this.entities) {
            $$28.addEntity($$29);
         }

         for (CompoundTag $$30 : this.blockEntities) {
            $$28.setBlockEntityNbt($$30);
         }

         if (this.carvingMask != null) {
            $$28.setCarvingMask(new CarvingMask(this.carvingMask, $$18.getMinY()));
         }

         return $$28;
      }
   }

   private static void logErrors(net.minecraft.world.level.ChunkPos $$0, int $$1, String $$2) {
      LOGGER.error("Recoverable errors when loading section [{}, {}, {}]: {}", new Object[]{$$0.x, $$1, $$0.z, $$2});
   }

   public static SerializableChunkData copyOf(ServerLevel $$0, ChunkAccess $$1) {
      if (!$$1.canBeSerialized()) {
         throw new IllegalArgumentException("Chunk can't be serialized: " + $$1);
      } else {
         net.minecraft.world.level.ChunkPos $$2 = $$1.getPos();
         List<SerializableChunkData.SectionData> $$3 = new ArrayList<>();
         LevelChunkSection[] $$4 = $$1.getSections();
         LevelLightEngine $$5 = $$0.getChunkSource().getLightEngine();

         for (int $$6 = $$5.getMinLightSection(); $$6 < $$5.getMaxLightSection(); $$6++) {
            int $$7 = $$1.getSectionIndexFromSectionY($$6);
            boolean $$8 = $$7 >= 0 && $$7 < $$4.length;
            DataLayer $$9 = $$5.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK).getDataLayerData(SectionPos.of($$2, $$6));
            DataLayer $$10 = $$5.getLayerListener(net.minecraft.world.level.LightLayer.SKY).getDataLayerData(SectionPos.of($$2, $$6));
            DataLayer $$11 = $$9 != null && !$$9.isEmpty() ? $$9.copy() : null;
            DataLayer $$12 = $$10 != null && !$$10.isEmpty() ? $$10.copy() : null;
            if ($$8 || $$11 != null || $$12 != null) {
               LevelChunkSection $$13 = $$8 ? $$4[$$7].copy() : null;
               $$3.add(new SerializableChunkData.SectionData($$6, $$13, $$11, $$12));
            }
         }

         List<CompoundTag> $$14 = new ArrayList<>($$1.getBlockEntitiesPos().size());

         for (BlockPos $$15 : $$1.getBlockEntitiesPos()) {
            CompoundTag $$16 = $$1.getBlockEntityNbtForSaving($$15, $$0.registryAccess());
            if ($$16 != null) {
               $$14.add($$16);
            }
         }

         List<CompoundTag> $$17 = new ArrayList<>();
         long[] $$18 = null;
         if ($$1.getPersistedStatus().getChunkType() == ChunkType.PROTOCHUNK) {
            ProtoChunk $$19 = (ProtoChunk)$$1;
            $$17.addAll($$19.getEntities());
            CarvingMask $$20 = $$19.getCarvingMask();
            if ($$20 != null) {
               $$18 = $$20.toArray();
            }
         }

         Map<Heightmap.Types, long[]> $$21 = new EnumMap<>(Heightmap.Types.class);

         for (Entry<Heightmap.Types, Heightmap> $$22 : $$1.getHeightmaps()) {
            if ($$1.getPersistedStatus().heightmapsAfter().contains($$22.getKey())) {
               long[] $$23 = $$22.getValue().getRawData();
               $$21.put($$22.getKey(), (long[])$$23.clone());
            }
         }

         ChunkAccess.PackedTicks $$24 = $$1.getTicksForSerialization($$0.getGameTime());
         ShortList[] $$25 = Arrays.stream($$1.getPostProcessing())
            .map($$0x -> $$0x != null && !$$0x.isEmpty() ? new ShortArrayList($$0x) : null)
            .toArray(ShortList[]::new);
         CompoundTag $$26 = packStructureData(StructurePieceSerializationContext.fromLevel($$0), $$2, $$1.getAllStarts(), $$1.getAllReferences());
         return new SerializableChunkData(
            $$0.palettedContainerFactory(),
            $$2,
            $$1.getMinSectionY(),
            $$0.getGameTime(),
            $$1.getInhabitedTime(),
            $$1.getPersistedStatus(),
            (BlendingData.Packed)Optionull.map($$1.getBlendingData(), BlendingData::pack),
            $$1.getBelowZeroRetrogen(),
            $$1.getUpgradeData().copy(),
            $$18,
            $$21,
            $$24,
            $$25,
            $$1.isLightCorrect(),
            $$3,
            $$17,
            $$14,
            $$26
         );
      }
   }

   public CompoundTag write() {
      CompoundTag $$0 = NbtUtils.addCurrentDataVersion(new CompoundTag());
      $$0.putInt("xPos", this.chunkPos.x);
      $$0.putInt("yPos", this.minSectionY);
      $$0.putInt("zPos", this.chunkPos.z);
      $$0.putLong("LastUpdate", this.lastUpdateTime);
      $$0.putLong("InhabitedTime", this.inhabitedTime);
      $$0.putString("Status", BuiltInRegistries.CHUNK_STATUS.getKey(this.chunkStatus).toString());
      $$0.storeNullable("blending_data", BlendingData.Packed.CODEC, this.blendingData);
      $$0.storeNullable("below_zero_retrogen", BelowZeroRetrogen.CODEC, this.belowZeroRetrogen);
      if (!this.upgradeData.isEmpty()) {
         $$0.put("UpgradeData", this.upgradeData.write());
      }

      ListTag $$1 = new ListTag();
      Codec<PalettedContainer<BlockState>> $$2 = this.containerFactory.blockStatesContainerCodec();
      Codec<PalettedContainerRO<Holder<Biome>>> $$3 = this.containerFactory.biomeContainerCodec();

      for (SerializableChunkData.SectionData $$4 : this.sectionData) {
         CompoundTag $$5 = new CompoundTag();
         LevelChunkSection $$6 = $$4.chunkSection;
         if ($$6 != null) {
            $$5.store("block_states", $$2, $$6.getStates());
            $$5.store("biomes", $$3, $$6.getBiomes());
         }

         if ($$4.blockLight != null) {
            $$5.putByteArray("BlockLight", $$4.blockLight.getData());
         }

         if ($$4.skyLight != null) {
            $$5.putByteArray("SkyLight", $$4.skyLight.getData());
         }

         if (!$$5.isEmpty()) {
            $$5.putByte("Y", (byte)$$4.y);
            $$1.add($$5);
         }
      }

      $$0.put("sections", $$1);
      if (this.lightCorrect) {
         $$0.putBoolean("isLightOn", true);
      }

      ListTag $$7 = new ListTag();
      $$7.addAll(this.blockEntities);
      $$0.put("block_entities", $$7);
      if (this.chunkStatus.getChunkType() == ChunkType.PROTOCHUNK) {
         ListTag $$8 = new ListTag();
         $$8.addAll(this.entities);
         $$0.put("entities", $$8);
         if (this.carvingMask != null) {
            $$0.putLongArray("carving_mask", this.carvingMask);
         }
      }

      saveTicks($$0, this.packedTicks);
      $$0.put("PostProcessing", packOffsets(this.postProcessingSections));
      CompoundTag $$9 = new CompoundTag();
      this.heightmaps.forEach(($$1x, $$2x) -> $$9.put($$1x.getSerializationKey(), new LongArrayTag($$2x)));
      $$0.put("Heightmaps", $$9);
      $$0.put("structures", this.structureData);
      return $$0;
   }

   private static void saveTicks(CompoundTag $$0, ChunkAccess.PackedTicks $$1) {
      $$0.store("block_ticks", BLOCK_TICKS_CODEC, $$1.blocks());
      $$0.store("fluid_ticks", FLUID_TICKS_CODEC, $$1.fluids());
   }

   public static ChunkStatus getChunkStatusFromTag(CompoundTag $$0) {
      return $$0 != null ? $$0.read("Status", ChunkStatus.CODEC).orElse(ChunkStatus.EMPTY) : ChunkStatus.EMPTY;
   }

   
   private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel $$0, List<CompoundTag> $$1, List<CompoundTag> $$2) {
      return $$1.isEmpty() && $$2.isEmpty() ? null : $$3 -> {
         if (!$$1.isEmpty()) {
            ScopedCollector $$4 = new ScopedCollector($$3.problemPath(), LOGGER);

            try {
               $$0.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(TagValueInput.create($$4, $$0.registryAccess(), $$1), $$0, EntitySpawnReason.LOAD));
            } catch (Throwable var10) {
               try {
                  $$4.close();
               } catch (Throwable var9) {
                  var10.addSuppressed(var9);
               }

               throw var10;
            }

            $$4.close();
         }

         for (CompoundTag $$5 : $$2) {
            boolean $$6 = $$5.getBooleanOr("keepPacked", false);
            if ($$6) {
               $$3.setBlockEntityNbt($$5);
            } else {
               BlockPos $$7 = BlockEntity.getPosFromTag($$3.getPos(), $$5);
               BlockEntity $$8 = BlockEntity.loadStatic($$7, $$3.getBlockState($$7), $$5, $$0.registryAccess());
               if ($$8 != null) {
                  $$3.setBlockEntity($$8);
               }
            }
         }
      };
   }

   private static CompoundTag packStructureData(
      StructurePieceSerializationContext $$0, net.minecraft.world.level.ChunkPos $$1, Map<Structure, StructureStart> $$2, Map<Structure, LongSet> $$3
   ) {
      CompoundTag $$4 = new CompoundTag();
      CompoundTag $$5 = new CompoundTag();
      Registry<Structure> $$6 = $$0.registryAccess().lookupOrThrow(Registries.STRUCTURE);

      for (Entry<Structure, StructureStart> $$7 : $$2.entrySet()) {
         Identifier $$8 = $$6.getKey($$7.getKey());
         $$5.put($$8.toString(), $$7.getValue().createTag($$0, $$1));
      }

      $$4.put("starts", $$5);
      CompoundTag $$9 = new CompoundTag();

      for (Entry<Structure, LongSet> $$10 : $$3.entrySet()) {
         if (!$$10.getValue().isEmpty()) {
            Identifier $$11 = $$6.getKey($$10.getKey());
            $$9.putLongArray($$11.toString(), $$10.getValue().toLongArray());
         }
      }

      $$4.put("References", $$9);
      return $$4;
   }

   private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext $$0, CompoundTag $$1, long $$2) {
      Map<Structure, StructureStart> $$3 = Maps.newHashMap();
      Registry<Structure> $$4 = $$0.registryAccess().lookupOrThrow(Registries.STRUCTURE);
      CompoundTag $$5 = $$1.getCompoundOrEmpty("starts");

      for (String $$6 : $$5.keySet()) {
         Identifier $$7 = Identifier.tryParse($$6);
         Structure $$8 = (Structure)$$4.getValue($$7);
         if ($$8 == null) {
            LOGGER.error("Unknown structure start: {}", $$7);
         } else {
            StructureStart $$9 = StructureStart.loadStaticStart($$0, $$5.getCompoundOrEmpty($$6), $$2);
            if ($$9 != null) {
               $$3.put($$8, $$9);
            }
         }
      }

      return $$3;
   }

   private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess $$0, net.minecraft.world.level.ChunkPos $$1, CompoundTag $$2) {
      Map<Structure, LongSet> $$3 = Maps.newHashMap();
      Registry<Structure> $$4 = $$0.lookupOrThrow(Registries.STRUCTURE);
      CompoundTag $$5 = $$2.getCompoundOrEmpty("References");
      $$5.forEach(($$3x, $$4x) -> {
         Identifier $$5x = Identifier.tryParse($$3x);
         Structure $$6 = (Structure)$$4.getValue($$5x);
         if ($$6 == null) {
            LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", $$5x, $$1);
         } else {
            Optional<long[]> $$7 = $$4x.asLongArray();
            if (!$$7.isEmpty()) {
               $$3.put($$6, new LongOpenHashSet(Arrays.stream($$7.get()).filter($$2xx -> {
                  net.minecraft.world.level.ChunkPos $$3xx = new net.minecraft.world.level.ChunkPos($$2xx);
                  if ($$3xx.getChessboardDistance($$1) > 8) {
                     LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", new Object[]{$$5x, $$3xx, $$1});
                     return false;
                  } else {
                     return true;
                  }
               }).toArray()));
            }
         }
      });
      return $$3;
   }

   private static ListTag packOffsets(ShortList[] $$0) {
      ListTag $$1 = new ListTag();

      for (ShortList $$2 : $$0) {
         ListTag $$3 = new ListTag();
         if ($$2 != null) {
            for (int $$4 = 0; $$4 < $$2.size(); $$4++) {
               $$3.add(ShortTag.valueOf($$2.getShort($$4)));
            }
         }

         $$1.add($$3);
      }

      return $$1;
   }

   public static class ChunkReadException extends NbtException {
      public ChunkReadException(String $$0) {
         super($$0);
      }
   }

   public record SectionData(int y, LevelChunkSection chunkSection, DataLayer blockLight, DataLayer skyLight) {
   }
}
