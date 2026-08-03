package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class OceanRuinStructure extends Structure {
   public static final MapCodec<OceanRuinStructure> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(
            settingsCodec($$0),
            OceanRuinStructure.Type.CODEC.fieldOf("biome_temp").forGetter($$0x -> $$0x.biomeTemp),
            Codec.floatRange(0.0F, 1.0F).fieldOf("large_probability").forGetter($$0x -> $$0x.largeProbability),
            Codec.floatRange(0.0F, 1.0F).fieldOf("cluster_probability").forGetter($$0x -> $$0x.clusterProbability)
         )
         .apply($$0, OceanRuinStructure::new)
   );
   public final OceanRuinStructure.Type biomeTemp;
   public final float largeProbability;
   public final float clusterProbability;

   public OceanRuinStructure(Structure.StructureSettings $$0, OceanRuinStructure.Type $$1, float $$2, float $$3) {
      super($$0);
      this.biomeTemp = $$1;
      this.largeProbability = $$2;
      this.clusterProbability = $$3;
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      return onTopOfChunkCenter($$0, Heightmap.Types.OCEAN_FLOOR_WG, $$1 -> this.generatePieces($$1, $$0));
   }

   private void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      BlockPos $$2 = new BlockPos($$1.chunkPos().getMinBlockX(), 90, $$1.chunkPos().getMinBlockZ());
      Rotation $$3 = Rotation.getRandom($$1.random());
      OceanRuinPieces.addPieces($$1.structureTemplateManager(), $$2, $$3, $$0, $$1.random(), this);
   }

   @Override
   public StructureType<?> type() {
      return StructureType.OCEAN_RUIN;
   }

   public static enum Type implements StringRepresentable {
      WARM("warm"),
      COLD("cold");

      public static final Codec<OceanRuinStructure.Type> CODEC = StringRepresentable.fromEnum(OceanRuinStructure.Type::values);
      @Deprecated
      public static final Codec<OceanRuinStructure.Type> LEGACY_CODEC = ExtraCodecs.legacyEnum(OceanRuinStructure.Type::valueOf);
      private final String name;

      private Type(final String $$0) {
         this.name = $$0;
      }

      public String getName() {
         return this.name;
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
