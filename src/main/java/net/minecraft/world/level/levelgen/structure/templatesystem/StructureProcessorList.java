package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;

public class StructureProcessorList {
   private final List<StructureProcessor> list;

   public StructureProcessorList(List<StructureProcessor> $$0) {
      this.list = $$0;
   }

   public List<StructureProcessor> list() {
      return this.list;
   }

   @Override
   public String toString() {
      return "ProcessorList[" + this.list + "]";
   }
}
