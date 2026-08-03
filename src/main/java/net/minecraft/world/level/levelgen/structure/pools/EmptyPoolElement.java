package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.MapCodec;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class EmptyPoolElement extends StructurePoolElement {
   public static final MapCodec<EmptyPoolElement> CODEC = MapCodec.unit(() -> EmptyPoolElement.INSTANCE);
   public static final EmptyPoolElement INSTANCE = new EmptyPoolElement();

   private EmptyPoolElement() {
      super(StructureTemplatePool.Projection.TERRAIN_MATCHING);
   }

   @Override
   public Vec3i getSize(StructureTemplateManager $$0, Rotation $$1) {
      return Vec3i.ZERO;
   }

   @Override
   public List<StructureTemplate.JigsawBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2, RandomSource $$3) {
      return Collections.emptyList();
   }

   @Override
   public BoundingBox getBoundingBox(StructureTemplateManager $$0, BlockPos $$1, Rotation $$2) {
      throw new IllegalStateException("Invalid call to EmptyPoolElement.getBoundingBox, filter me!");
   }

   @Override
   public boolean place(
      StructureTemplateManager $$0,
      net.minecraft.world.level.WorldGenLevel $$1,
      net.minecraft.world.level.StructureManager $$2,
      ChunkGenerator $$3,
      BlockPos $$4,
      BlockPos $$5,
      Rotation $$6,
      BoundingBox $$7,
      RandomSource $$8,
      LiquidSettings $$9,
      boolean $$10
   ) {
      return true;
   }

   @Override
   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.EMPTY;
   }

   @Override
   public String toString() {
      return "Empty";
   }
}
