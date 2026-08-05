package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import net.minecraft.core.BlockPos;

public abstract class StructureProcessor {
   
   public StructureTemplate.StructureBlockInfo processBlock(
      net.minecraft.world.level.LevelReader $$0,
      BlockPos $$1,
      BlockPos $$2,
      StructureTemplate.StructureBlockInfo $$3,
      StructureTemplate.StructureBlockInfo $$4,
      StructurePlaceSettings $$5
   ) {
      return $$4;
   }

   protected abstract StructureProcessorType<?> getType();

   public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
      net.minecraft.world.level.ServerLevelAccessor $$0,
      BlockPos $$1,
      BlockPos $$2,
      List<StructureTemplate.StructureBlockInfo> $$3,
      List<StructureTemplate.StructureBlockInfo> $$4,
      StructurePlaceSettings $$5
   ) {
      return $$4;
   }
}
