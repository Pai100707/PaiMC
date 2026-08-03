package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PoolElementStructurePiece extends StructurePiece {
   protected final StructurePoolElement element;
   protected BlockPos position;
   private final int groundLevelDelta;
   protected final Rotation rotation;
   private final List<JigsawJunction> junctions = Lists.newArrayList();
   private final StructureTemplateManager structureTemplateManager;
   private final LiquidSettings liquidSettings;

   public PoolElementStructurePiece(
      StructureTemplateManager $$0, StructurePoolElement $$1, BlockPos $$2, int $$3, Rotation $$4, BoundingBox $$5, LiquidSettings $$6
   ) {
      super(StructurePieceType.JIGSAW, 0, $$5);
      this.structureTemplateManager = $$0;
      this.element = $$1;
      this.position = $$2;
      this.groundLevelDelta = $$3;
      this.rotation = $$4;
      this.liquidSettings = $$6;
   }

   public PoolElementStructurePiece(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      super(StructurePieceType.JIGSAW, $$1);
      this.structureTemplateManager = $$0.structureTemplateManager();
      this.position = new BlockPos($$1.getIntOr("PosX", 0), $$1.getIntOr("PosY", 0), $$1.getIntOr("PosZ", 0));
      this.groundLevelDelta = $$1.getIntOr("ground_level_delta", 0);
      DynamicOps<Tag> $$2 = $$0.registryAccess().createSerializationContext(NbtOps.INSTANCE);
      this.element = (StructurePoolElement)$$1.read("pool_element", StructurePoolElement.CODEC, $$2)
         .orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
      this.rotation = (Rotation)$$1.read("rotation", Rotation.LEGACY_CODEC).orElseThrow();
      this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
      ListTag $$3 = $$1.getListOrEmpty("junctions");
      this.junctions.clear();
      $$3.forEach($$1x -> this.junctions.add(JigsawJunction.deserialize(new Dynamic($$2, $$1x))));
      this.liquidSettings = $$1.read("liquid_settings", LiquidSettings.CODEC).orElse(JigsawStructure.DEFAULT_LIQUID_SETTINGS);
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext $$0, CompoundTag $$1) {
      $$1.putInt("PosX", this.position.getX());
      $$1.putInt("PosY", this.position.getY());
      $$1.putInt("PosZ", this.position.getZ());
      $$1.putInt("ground_level_delta", this.groundLevelDelta);
      DynamicOps<Tag> $$2 = $$0.registryAccess().createSerializationContext(NbtOps.INSTANCE);
      $$1.store("pool_element", StructurePoolElement.CODEC, $$2, this.element);
      $$1.store("rotation", Rotation.LEGACY_CODEC, this.rotation);
      ListTag $$3 = new ListTag();

      for (JigsawJunction $$4 : this.junctions) {
         $$3.add((Tag)$$4.serialize($$2).getValue());
      }

      $$1.put("junctions", $$3);
      if (this.liquidSettings != JigsawStructure.DEFAULT_LIQUID_SETTINGS) {
         $$1.store("liquid_settings", LiquidSettings.CODEC, $$2, this.liquidSettings);
      }
   }

   @Override
   public void postProcess(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      net.minecraft.world.level.ChunkPos $$5,
      BlockPos $$6
   ) {
      this.place($$0, $$1, $$2, $$3, $$4, $$6, false);
   }

   public void place(
      net.minecraft.world.level.WorldGenLevel $$0,
      net.minecraft.world.level.StructureManager $$1,
      ChunkGenerator $$2,
      RandomSource $$3,
      BoundingBox $$4,
      BlockPos $$5,
      boolean $$6
   ) {
      this.element.place(this.structureTemplateManager, $$0, $$1, $$2, this.position, $$5, this.rotation, $$4, $$3, this.liquidSettings, $$6);
   }

   @Override
   public void move(int $$0, int $$1, int $$2) {
      super.move($$0, $$1, $$2);
      this.position = this.position.offset($$0, $$1, $$2);
   }

   @Override
   public Rotation getRotation() {
      return this.rotation;
   }

   @Override
   public String toString() {
      return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
   }

   public StructurePoolElement getElement() {
      return this.element;
   }

   public BlockPos getPosition() {
      return this.position;
   }

   public int getGroundLevelDelta() {
      return this.groundLevelDelta;
   }

   public void addJunction(JigsawJunction $$0) {
      this.junctions.add($$0);
   }

   public List<JigsawJunction> getJunctions() {
      return this.junctions;
   }
}
