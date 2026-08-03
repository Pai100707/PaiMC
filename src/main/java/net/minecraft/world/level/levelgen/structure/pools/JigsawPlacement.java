package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
   static final Logger LOGGER = LogUtils.getLogger();
   private static final int UNSET_HEIGHT = Integer.MIN_VALUE;

   public static Optional<Structure.GenerationStub> addPieces(
      Structure.GenerationContext $$0,
      Holder<StructureTemplatePool> $$1,
      Optional<Identifier> $$2,
      int $$3,
      BlockPos $$4,
      boolean $$5,
      Optional<Heightmap.Types> $$6,
      JigsawStructure.MaxDistance $$7,
      PoolAliasLookup $$8,
      DimensionPadding $$9,
      LiquidSettings $$10
   ) {
      RegistryAccess $$11 = $$0.registryAccess();
      ChunkGenerator $$12 = $$0.chunkGenerator();
      StructureTemplateManager $$13 = $$0.structureTemplateManager();
      net.minecraft.world.level.LevelHeightAccessor $$14 = $$0.heightAccessor();
      WorldgenRandom $$15 = $$0.random();
      Registry<StructureTemplatePool> $$16 = $$11.lookupOrThrow(Registries.TEMPLATE_POOL);
      Rotation $$17 = Rotation.getRandom($$15);
      StructureTemplatePool $$18 = $$1.unwrapKey().flatMap($$2x -> $$16.getOptional($$8.lookup($$2x))).orElse((StructureTemplatePool)$$1.value());
      StructurePoolElement $$19 = $$18.getRandomTemplate($$15);
      if ($$19 == EmptyPoolElement.INSTANCE) {
         return Optional.empty();
      } else {
         BlockPos $$22;
         if ($$2.isPresent()) {
            Identifier $$20 = $$2.get();
            Optional<BlockPos> $$21 = getRandomNamedJigsaw($$19, $$20, $$4, $$17, $$13, $$15);
            if ($$21.isEmpty()) {
               LOGGER.error(
                  "No starting jigsaw {} found in start pool {}", $$20, $$1.unwrapKey().map($$0x -> $$0x.identifier().toString()).orElse("<unregistered>")
               );
               return Optional.empty();
            }

            $$22 = $$21.get();
         } else {
            $$22 = $$4;
         }

         Vec3i $$24 = $$22.subtract($$4);
         BlockPos $$25 = $$4.subtract($$24);
         PoolElementStructurePiece $$26 = new PoolElementStructurePiece(
            $$13, $$19, $$25, $$19.getGroundLevelDelta(), $$17, $$19.getBoundingBox($$13, $$25, $$17), $$10
         );
         BoundingBox $$27 = $$26.getBoundingBox();
         int $$28 = ($$27.maxX() + $$27.minX()) / 2;
         int $$29 = ($$27.maxZ() + $$27.minZ()) / 2;
         int $$30 = $$6.isEmpty() ? $$25.getY() : $$4.getY() + $$12.getFirstFreeHeight($$28, $$29, $$6.get(), $$14, $$0.randomState());
         int $$31 = $$27.minY() + $$26.getGroundLevelDelta();
         $$26.move(0, $$30 - $$31, 0);
         if (isStartTooCloseToWorldHeightLimits($$14, $$9, $$26.getBoundingBox())) {
            LOGGER.debug("Center piece {} with bounding box {} does not fit dimension padding {}", new Object[]{$$19, $$26.getBoundingBox(), $$9});
            return Optional.empty();
         } else {
            int $$32 = $$30 + $$24.getY();
            return Optional.of(
               new Structure.GenerationStub(
                  new BlockPos($$28, $$32, $$29),
                  (Consumer<StructurePiecesBuilder>)($$17x -> {
                     List<PoolElementStructurePiece> $$18x = Lists.newArrayList();
                     $$18x.add($$26);
                     if ($$3 > 0) {
                        AABB $$19x = new AABB(
                           $$28 - $$7.horizontal(),
                           Math.max($$32 - $$7.vertical(), $$14.getMinY() + $$9.bottom()),
                           $$29 - $$7.horizontal(),
                           $$28 + $$7.horizontal() + 1,
                           Math.min($$32 + $$7.vertical() + 1, $$14.getMaxY() + 1 - $$9.top()),
                           $$29 + $$7.horizontal() + 1
                        );
                        VoxelShape $$20 = Shapes.join(Shapes.create($$19x), Shapes.create(AABB.of($$27)), BooleanOp.ONLY_FIRST);
                        addPieces($$0.randomState(), $$3, $$5, $$12, $$13, $$14, $$15, $$16, $$26, $$18x, $$20, $$8, $$10);
                        $$18x.forEach($$17x::addPiece);
                     }
                  })
               )
            );
         }
      }
   }

   private static boolean isStartTooCloseToWorldHeightLimits(net.minecraft.world.level.LevelHeightAccessor $$0, DimensionPadding $$1, BoundingBox $$2) {
      if ($$1 == DimensionPadding.ZERO) {
         return false;
      } else {
         int $$3 = $$0.getMinY() + $$1.bottom();
         int $$4 = $$0.getMaxY() - $$1.top();
         return $$2.minY() < $$3 || $$2.maxY() > $$4;
      }
   }

   private static Optional<BlockPos> getRandomNamedJigsaw(
      StructurePoolElement $$0, Identifier $$1, BlockPos $$2, Rotation $$3, StructureTemplateManager $$4, WorldgenRandom $$5
   ) {
      for (StructureTemplate.JigsawBlockInfo $$7 : $$0.getShuffledJigsawBlocks($$4, $$2, $$3, $$5)) {
         if ($$1.equals($$7.name())) {
            return Optional.of($$7.info().pos());
         }
      }

      return Optional.empty();
   }

   private static void addPieces(
      RandomState $$0,
      int $$1,
      boolean $$2,
      ChunkGenerator $$3,
      StructureTemplateManager $$4,
      net.minecraft.world.level.LevelHeightAccessor $$5,
      RandomSource $$6,
      Registry<StructureTemplatePool> $$7,
      PoolElementStructurePiece $$8,
      List<PoolElementStructurePiece> $$9,
      VoxelShape $$10,
      PoolAliasLookup $$11,
      LiquidSettings $$12
   ) {
      JigsawPlacement.Placer $$13 = new JigsawPlacement.Placer($$7, $$1, $$3, $$4, $$9, $$6);
      $$13.tryPlacingChildren($$8, new MutableObject($$10), 0, $$2, $$5, $$0, $$11, $$12);

      while ($$13.placing.hasNext()) {
         JigsawPlacement.PieceState $$14 = (JigsawPlacement.PieceState)$$13.placing.next();
         $$13.tryPlacingChildren($$14.piece, $$14.free, $$14.depth, $$2, $$5, $$0, $$11, $$12);
      }
   }

   public static boolean generateJigsaw(ServerLevel $$0, Holder<StructureTemplatePool> $$1, Identifier $$2, int $$3, BlockPos $$4, boolean $$5) {
      ChunkGenerator $$6 = $$0.getChunkSource().getGenerator();
      StructureTemplateManager $$7 = $$0.getStructureManager();
      net.minecraft.world.level.StructureManager $$8 = $$0.structureManager();
      RandomSource $$9 = $$0.getRandom();
      Structure.GenerationContext $$10 = new Structure.GenerationContext(
         $$0.registryAccess(),
         $$6,
         $$6.getBiomeSource(),
         $$0.getChunkSource().randomState(),
         $$7,
         $$0.getSeed(),
         new net.minecraft.world.level.ChunkPos($$4),
         $$0,
         $$0x -> true
      );
      Optional<Structure.GenerationStub> $$11 = addPieces(
         $$10,
         $$1,
         Optional.of($$2),
         $$3,
         $$4,
         false,
         Optional.empty(),
         new JigsawStructure.MaxDistance(128),
         PoolAliasLookup.EMPTY,
         JigsawStructure.DEFAULT_DIMENSION_PADDING,
         JigsawStructure.DEFAULT_LIQUID_SETTINGS
      );
      if ($$11.isPresent()) {
         StructurePiecesBuilder $$12 = $$11.get().getPiecesBuilder();

         for (StructurePiece $$13 : $$12.build().pieces()) {
            if ($$13 instanceof PoolElementStructurePiece $$14) {
               $$14.place($$0, $$8, $$6, $$9, BoundingBox.infinite(), $$4, $$5);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   record PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth) {
   }

   static final class Placer {
      private final Registry<StructureTemplatePool> pools;
      private final int maxDepth;
      private final ChunkGenerator chunkGenerator;
      private final StructureTemplateManager structureTemplateManager;
      private final List<? super PoolElementStructurePiece> pieces;
      private final RandomSource random;
      final SequencedPriorityIterator<JigsawPlacement.PieceState> placing = new SequencedPriorityIterator();

      Placer(
         Registry<StructureTemplatePool> $$0,
         int $$1,
         ChunkGenerator $$2,
         StructureTemplateManager $$3,
         List<? super PoolElementStructurePiece> $$4,
         RandomSource $$5
      ) {
         this.pools = $$0;
         this.maxDepth = $$1;
         this.chunkGenerator = $$2;
         this.structureTemplateManager = $$3;
         this.pieces = $$4;
         this.random = $$5;
      }

      void tryPlacingChildren(
         PoolElementStructurePiece $$0,
         MutableObject<VoxelShape> $$1,
         int $$2,
         boolean $$3,
         net.minecraft.world.level.LevelHeightAccessor $$4,
         RandomState $$5,
         PoolAliasLookup $$6,
         LiquidSettings $$7
      ) {
         StructurePoolElement $$8 = $$0.getElement();
         BlockPos $$9 = $$0.getPosition();
         Rotation $$10 = $$0.getRotation();
         StructureTemplatePool.Projection $$11 = $$8.getProjection();
         boolean $$12 = $$11 == StructureTemplatePool.Projection.RIGID;
         MutableObject<VoxelShape> $$13 = new MutableObject();
         BoundingBox $$14 = $$0.getBoundingBox();
         int $$15 = $$14.minY();

         label129:
         for (StructureTemplate.JigsawBlockInfo $$16 : $$8.getShuffledJigsawBlocks(this.structureTemplateManager, $$9, $$10, this.random)) {
            StructureTemplate.StructureBlockInfo $$17 = $$16.info();
            Direction $$18 = JigsawBlock.getFrontFacing($$17.state());
            BlockPos $$19 = $$17.pos();
            BlockPos $$20 = $$19.relative($$18);
            int $$21 = $$19.getY() - $$15;
            int $$22 = Integer.MIN_VALUE;
            ResourceKey<StructureTemplatePool> $$23 = $$6.lookup($$16.pool());
            Optional<? extends Holder<StructureTemplatePool>> $$24 = this.pools.get($$23);
            if ($$24.isEmpty()) {
               JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", $$23.identifier());
            } else {
               Holder<StructureTemplatePool> $$25 = (Holder<StructureTemplatePool>)$$24.get();
               if (((StructureTemplatePool)$$25.value()).size() == 0 && !$$25.is(Pools.EMPTY)) {
                  JigsawPlacement.LOGGER.warn("Empty or non-existent pool: {}", $$23.identifier());
               } else {
                  Holder<StructureTemplatePool> $$26 = ((StructureTemplatePool)$$25.value()).getFallback();
                  if (((StructureTemplatePool)$$26.value()).size() == 0 && !$$26.is(Pools.EMPTY)) {
                     JigsawPlacement.LOGGER
                        .warn("Empty or non-existent fallback pool: {}", $$26.unwrapKey().map($$0x -> $$0x.identifier().toString()).orElse("<unregistered>"));
                  } else {
                     boolean $$27 = $$14.isInside($$20);
                     MutableObject<VoxelShape> $$28;
                     if ($$27) {
                        $$28 = $$13;
                        if ($$13.get() == null) {
                           $$13.setValue(Shapes.create(AABB.of($$14)));
                        }
                     } else {
                        $$28 = $$1;
                     }

                     List<StructurePoolElement> $$30 = Lists.newArrayList();
                     if ($$2 != this.maxDepth) {
                        $$30.addAll(((StructureTemplatePool)$$25.value()).getShuffledTemplates(this.random));
                     }

                     $$30.addAll(((StructureTemplatePool)$$26.value()).getShuffledTemplates(this.random));
                     int $$31 = $$16.placementPriority();

                     for (StructurePoolElement $$32 : $$30) {
                        if ($$32 == EmptyPoolElement.INSTANCE) {
                           break;
                        }

                        for (Rotation $$33 : Rotation.getShuffled(this.random)) {
                           List<StructureTemplate.JigsawBlockInfo> $$34 = $$32.getShuffledJigsawBlocks(
                              this.structureTemplateManager, BlockPos.ZERO, $$33, this.random
                           );
                           BoundingBox $$35 = $$32.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, $$33);
                           int $$37;
                           if ($$3 && $$35.getYSpan() <= 16) {
                              $$37 = $$34.stream()
                                 .mapToInt(
                                    $$2x -> {
                                       StructureTemplate.StructureBlockInfo $$3x = $$2x.info();
                                       if (!$$35.isInside($$3x.pos().relative(JigsawBlock.getFrontFacing($$3x.state())))) {
                                          return 0;
                                       } else {
                                          ResourceKey<StructureTemplatePool> $$4x = $$6.lookup($$2x.pool());
                                          Optional<? extends Holder<StructureTemplatePool>> $$5x = this.pools.get($$4x);
                                          Optional<Holder<StructureTemplatePool>> $$6x = $$5x.map($$0xx -> ((StructureTemplatePool)$$0xx.value()).getFallback());
                                          int $$7x = $$5x.<Integer>map(
                                                $$0xx -> ((StructureTemplatePool)$$0xx.value()).getMaxSize(this.structureTemplateManager)
                                             )
                                             .orElse(0);
                                          int $$8x = $$6x.<Integer>map(
                                                $$0xx -> ((StructureTemplatePool)$$0xx.value()).getMaxSize(this.structureTemplateManager)
                                             )
                                             .orElse(0);
                                          return Math.max($$7x, $$8x);
                                       }
                                    }
                                 )
                                 .max()
                                 .orElse(0);
                           } else {
                              $$37 = 0;
                           }

                           for (StructureTemplate.JigsawBlockInfo $$38 : $$34) {
                              if (JigsawBlock.canAttach($$16, $$38)) {
                                 BlockPos $$39 = $$38.info().pos();
                                 BlockPos $$40 = $$20.subtract($$39);
                                 BoundingBox $$41 = $$32.getBoundingBox(this.structureTemplateManager, $$40, $$33);
                                 int $$42 = $$41.minY();
                                 StructureTemplatePool.Projection $$43 = $$32.getProjection();
                                 boolean $$44 = $$43 == StructureTemplatePool.Projection.RIGID;
                                 int $$45 = $$39.getY();
                                 int $$46 = $$21 - $$45 + JigsawBlock.getFrontFacing($$17.state()).getStepY();
                                 int $$47;
                                 if ($$12 && $$44) {
                                    $$47 = $$15 + $$46;
                                 } else {
                                    if ($$22 == Integer.MIN_VALUE) {
                                       $$22 = this.chunkGenerator.getFirstFreeHeight($$19.getX(), $$19.getZ(), Heightmap.Types.WORLD_SURFACE_WG, $$4, $$5);
                                    }

                                    $$47 = $$22 - $$45;
                                 }

                                 int $$49 = $$47 - $$42;
                                 BoundingBox $$50 = $$41.moved(0, $$49, 0);
                                 BlockPos $$51 = $$40.offset(0, $$49, 0);
                                 if ($$37 > 0) {
                                    int $$52 = Math.max($$37 + 1, $$50.maxY() - $$50.minY());
                                    $$50.encapsulate(new BlockPos($$50.minX(), $$50.minY() + $$52, $$50.minZ()));
                                 }

                                 if (!Shapes.joinIsNotEmpty((VoxelShape)$$28.get(), Shapes.create(AABB.of($$50).deflate(0.25)), BooleanOp.ONLY_SECOND)) {
                                    $$28.setValue(Shapes.joinUnoptimized((VoxelShape)$$28.get(), Shapes.create(AABB.of($$50)), BooleanOp.ONLY_FIRST));
                                    int $$53 = $$0.getGroundLevelDelta();
                                    int $$54;
                                    if ($$44) {
                                       $$54 = $$53 - $$46;
                                    } else {
                                       $$54 = $$32.getGroundLevelDelta();
                                    }

                                    PoolElementStructurePiece $$56 = new PoolElementStructurePiece(
                                       this.structureTemplateManager, $$32, $$51, $$54, $$33, $$50, $$7
                                    );
                                    int $$57;
                                    if ($$12) {
                                       $$57 = $$15 + $$21;
                                    } else if ($$44) {
                                       $$57 = $$47 + $$45;
                                    } else {
                                       if ($$22 == Integer.MIN_VALUE) {
                                          $$22 = this.chunkGenerator.getFirstFreeHeight($$19.getX(), $$19.getZ(), Heightmap.Types.WORLD_SURFACE_WG, $$4, $$5);
                                       }

                                       $$57 = $$22 + $$46 / 2;
                                    }

                                    $$0.addJunction(new JigsawJunction($$20.getX(), $$57 - $$21 + $$53, $$20.getZ(), $$46, $$43));
                                    $$56.addJunction(new JigsawJunction($$19.getX(), $$57 - $$45 + $$54, $$19.getZ(), -$$46, $$11));
                                    this.pieces.add($$56);
                                    if ($$2 + 1 <= this.maxDepth) {
                                       JigsawPlacement.PieceState $$60 = new JigsawPlacement.PieceState($$56, $$28, $$2 + 1);
                                       this.placing.add($$60, $$31);
                                    }
                                    continue label129;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
