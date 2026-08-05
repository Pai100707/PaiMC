package net.minecraft.world.level.levelgen.structure;


public interface StructurePieceAccessor {
   void addPiece(StructurePiece var1);

   
   StructurePiece findCollisionPiece(BoundingBox var1);
}
