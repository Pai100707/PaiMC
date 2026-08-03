package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class NetherFortressStructure extends Structure {
   public static final WeightedList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedList.builder()
      .add(new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 2, 3), 10)
      .add(new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 4, 4), 5)
      .add(new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 5, 5), 8)
      .add(new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 5, 5), 2)
      .add(new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 4, 4), 3)
      .build();
   public static final MapCodec<NetherFortressStructure> CODEC = simpleCodec(NetherFortressStructure::new);

   public NetherFortressStructure(Structure.StructureSettings $$0) {
      super($$0);
   }

   @Override
   public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext $$0) {
      net.minecraft.world.level.ChunkPos $$1 = $$0.chunkPos();
      BlockPos $$2 = new BlockPos($$1.getMinBlockX(), 64, $$1.getMinBlockZ());
      return Optional.of(new Structure.GenerationStub($$2, (Consumer<StructurePiecesBuilder>)($$1x -> generatePieces($$1x, $$0))));
   }

   private static void generatePieces(StructurePiecesBuilder $$0, Structure.GenerationContext $$1) {
      NetherFortressPieces.StartPiece $$2 = new NetherFortressPieces.StartPiece($$1.random(), $$1.chunkPos().getBlockX(2), $$1.chunkPos().getBlockZ(2));
      $$0.addPiece($$2);
      $$2.addChildren($$2, $$0, $$1.random());
      List<StructurePiece> $$3 = $$2.pendingChildren;

      while (!$$3.isEmpty()) {
         int $$4 = $$1.random().nextInt($$3.size());
         StructurePiece $$5 = $$3.remove($$4);
         $$5.addChildren($$2, $$0, $$1.random());
      }

      $$0.moveInsideHeights($$1.random(), 48, 70);
   }

   @Override
   public StructureType<?> type() {
      return StructureType.FORTRESS;
   }
}
