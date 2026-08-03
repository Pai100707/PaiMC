package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFossilStructure extends Structure {
   public static final MapCodec<NetherFossilStructure> CODEC = RecordCodecBuilder.mapCodec(
      $$0 -> $$0.group(settingsCodec($$0), HeightProvider.CODEC.fieldOf("height").forGetter($$0x -> $$0x.height)).apply($$0, NetherFossilStructure::new)
   );
   public final HeightProvider height;

   public NetherFossilStructure(Structure.StructureSettings $$0, HeightProvider $$1) {
      super($$0);
      this.height = $$1;
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      WorldgenRandom $$1 = $$0.random();
      int $$2 = $$0.chunkPos().getMinBlockX() + $$1.nextInt(16);
      int $$3 = $$0.chunkPos().getMinBlockZ() + $$1.nextInt(16);
      int $$4 = $$0.chunkGenerator().getSeaLevel();
      WorldGenerationContext $$5 = new WorldGenerationContext($$0.chunkGenerator(), $$0.heightAccessor());
      int $$6 = this.height.sample($$1, $$5);
      net.minecraft.world.level.NoiseColumn $$7 = $$0.chunkGenerator().getBaseColumn($$2, $$3, $$0.heightAccessor(), $$0.randomState());
      MutableBlockPos $$8 = new MutableBlockPos($$2, $$6, $$3);

      while ($$6 > $$4) {
         BlockState $$9 = $$7.getBlock($$6);
         BlockState $$10 = $$7.getBlock(--$$6);
         if ($$9.isAir() && ($$10.is(Blocks.SOUL_SAND) || $$10.isFaceSturdy(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, $$8.setY($$6), Direction.UP))) {
            break;
         }
      }

      if ($$6 <= $$4) {
         return Optional.empty();
      } else {
         BlockPos $$11 = new BlockPos($$2, $$6, $$3);
         return Optional.of(
            new Structure.GenerationStub(
               $$11, (Consumer<StructurePiecesBuilder>)($$3x -> NetherFossilPieces.addPieces($$0.structureTemplateManager(), $$3x, $$1, $$11))
            )
         );
      }
   }

   @Override
   public StructureType<?> type() {
      return StructureType.NETHER_FOSSIL;
   }
}
