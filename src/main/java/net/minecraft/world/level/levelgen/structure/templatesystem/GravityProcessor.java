package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public class GravityProcessor extends StructureProcessor {
   public static final MapCodec<GravityProcessor> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            Heightmap.Types.CODEC.fieldOf("heightmap").orElse(Heightmap.Types.WORLD_SURFACE_WG).forGetter($$0x -> $$0x.heightmap),
            Codec.INT.fieldOf("offset").orElse(0).forGetter($$0x -> $$0x.offset)
         )
         .apply($$0, GravityProcessor::new)
   );
   private final Heightmap.Types heightmap;
   private final int offset;

   public GravityProcessor(Heightmap.Types $$0, int $$1) {
      this.heightmap = $$0;
      this.offset = $$1;
   }

   
   @Override
   public StructureTemplate.StructureBlockInfo processBlock(
      net.minecraft.world.level.LevelReader $$0,
      BlockPos $$1,
      BlockPos $$2,
      StructureTemplate.StructureBlockInfo $$3,
      StructureTemplate.StructureBlockInfo $$4,
      StructurePlaceSettings $$5
   ) {
      Heightmap.Types $$6;
      if ($$0 instanceof ServerLevel) {
         if (this.heightmap == Heightmap.Types.WORLD_SURFACE_WG) {
            $$6 = Heightmap.Types.WORLD_SURFACE;
         } else if (this.heightmap == Heightmap.Types.OCEAN_FLOOR_WG) {
            $$6 = Heightmap.Types.OCEAN_FLOOR;
         } else {
            $$6 = this.heightmap;
         }
      } else {
         $$6 = this.heightmap;
      }

      BlockPos $$10 = $$4.pos();
      int $$11 = $$0.getHeight($$6, $$10.getX(), $$10.getZ()) + this.offset;
      int $$12 = $$3.pos().getY();
      return new StructureTemplate.StructureBlockInfo(new BlockPos($$10.getX(), $$11 + $$12, $$10.getZ()), $$4.state(), $$4.nbt());
   }

   @Override
   protected StructureProcessorType<?> getType() {
      return StructureProcessorType.GRAVITY;
   }
}
