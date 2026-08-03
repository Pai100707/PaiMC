package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class TreeFeature extends Feature<TreeConfiguration> {
   @Block.UpdateFlags
   private static final int BLOCK_UPDATE_FLAGS = 19;

   public TreeFeature(Codec<TreeConfiguration> $$0) {
      super($$0);
   }

   public static boolean isVine(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, $$0x -> $$0x.is(Blocks.VINE));
   }

   public static boolean isAirOrLeaves(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, $$0x -> $$0x.isAir() || $$0x.is(BlockTags.LEAVES));
   }

   private static void setBlockKnownShape(net.minecraft.world.level.LevelWriter $$0, BlockPos $$1, BlockState $$2) {
      $$0.setBlock($$1, $$2, 19);
   }

   public static boolean validTreePos(net.minecraft.world.level.LevelSimulatedReader $$0, BlockPos $$1) {
      return $$0.isStateAtPosition($$1, $$0x -> $$0x.isAir() || $$0x.is(BlockTags.REPLACEABLE_BY_TREES));
   }

   private boolean doPlace(
      net.minecraft.world.level.WorldGenLevel $$0,
      RandomSource $$1,
      BlockPos $$2,
      BiConsumer<BlockPos, BlockState> $$3,
      BiConsumer<BlockPos, BlockState> $$4,
      FoliagePlacer.FoliageSetter $$5,
      TreeConfiguration $$6
   ) {
      int $$7 = $$6.trunkPlacer.getTreeHeight($$1);
      int $$8 = $$6.foliagePlacer.foliageHeight($$1, $$7, $$6);
      int $$9 = $$7 - $$8;
      int $$10 = $$6.foliagePlacer.foliageRadius($$1, $$9);
      BlockPos $$11 = $$6.rootPlacer.<BlockPos>map($$2x -> $$2x.getTrunkOrigin($$2, $$1)).orElse($$2);
      int $$12 = Math.min($$2.getY(), $$11.getY());
      int $$13 = Math.max($$2.getY(), $$11.getY()) + $$7 + 1;
      if ($$12 >= $$0.getMinY() + 1 && $$13 <= $$0.getMaxY() + 1) {
         OptionalInt $$14 = $$6.minimumSize.minClippedHeight();
         int $$15 = this.getMaxFreeTreeHeight($$0, $$7, $$11, $$6);
         if ($$15 >= $$7 || !$$14.isEmpty() && $$15 >= $$14.getAsInt()) {
            if ($$6.rootPlacer.isPresent() && !$$6.rootPlacer.get().placeRoots($$0, $$3, $$1, $$2, $$11, $$6)) {
               return false;
            } else {
               List<FoliagePlacer.FoliageAttachment> $$16 = $$6.trunkPlacer.placeTrunk($$0, $$4, $$1, $$15, $$11, $$6);
               $$16.forEach($$7x -> $$6.foliagePlacer.createFoliage($$0, $$5, $$1, $$6, $$15, $$7x, $$8, $$10));
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int getMaxFreeTreeHeight(net.minecraft.world.level.LevelSimulatedReader $$0, int $$1, BlockPos $$2, TreeConfiguration $$3) {
      MutableBlockPos $$4 = new MutableBlockPos();

      for (int $$5 = 0; $$5 <= $$1 + 1; $$5++) {
         int $$6 = $$3.minimumSize.getSizeAtHeight($$1, $$5);

         for (int $$7 = -$$6; $$7 <= $$6; $$7++) {
            for (int $$8 = -$$6; $$8 <= $$6; $$8++) {
               $$4.setWithOffset($$2, $$7, $$5, $$8);
               if (!$$3.trunkPlacer.isFree($$0, $$4) || !$$3.ignoreVines && isVine($$0, $$4)) {
                  return $$5 - 2;
               }
            }
         }
      }

      return $$1;
   }

   @Override
   protected void setBlock(net.minecraft.world.level.LevelWriter $$0, BlockPos $$1, BlockState $$2) {
      setBlockKnownShape($$0, $$1, $$2);
   }

   @Override
   public final boolean place(FeaturePlaceContext<TreeConfiguration> $$0) {
      final net.minecraft.world.level.WorldGenLevel $$1 = $$0.level();
      RandomSource $$2 = $$0.random();
      BlockPos $$3 = $$0.origin();
      TreeConfiguration $$4 = $$0.config();
      Set<BlockPos> $$5 = Sets.newHashSet();
      Set<BlockPos> $$6 = Sets.newHashSet();
      final Set<BlockPos> $$7 = Sets.newHashSet();
      Set<BlockPos> $$8 = Sets.newHashSet();
      BiConsumer<BlockPos, BlockState> $$9 = ($$2x, $$3x) -> {
         $$5.add($$2x.immutable());
         $$1.setBlock($$2x, $$3x, 19);
      };
      BiConsumer<BlockPos, BlockState> $$10 = ($$2x, $$3x) -> {
         $$6.add($$2x.immutable());
         $$1.setBlock($$2x, $$3x, 19);
      };
      FoliagePlacer.FoliageSetter $$11 = new FoliagePlacer.FoliageSetter() {
         @Override
         public void set(BlockPos $$0, BlockState $$1x) {
            $$7.add($$0.immutable());
            $$1.setBlock($$0, $$1, 19);
         }

         @Override
         public boolean isSet(BlockPos $$0) {
            return $$7.contains($$0);
         }
      };
      BiConsumer<BlockPos, BlockState> $$12 = ($$2x, $$3x) -> {
         $$8.add($$2x.immutable());
         $$1.setBlock($$2x, $$3x, 19);
      };
      boolean $$13 = this.doPlace($$1, $$2, $$3, $$9, $$10, $$11, $$4);
      if ($$13 && (!$$6.isEmpty() || !$$7.isEmpty())) {
         if (!$$4.decorators.isEmpty()) {
            TreeDecorator.Context $$14 = new TreeDecorator.Context($$1, $$12, $$2, $$6, $$7, $$5);
            $$4.decorators.forEach($$1x -> $$1x.place($$14));
         }

         return BoundingBox.encapsulatingPositions(Iterables.concat($$5, $$6, $$7, $$8)).map($$4x -> {
            DiscreteVoxelShape $$5x = updateLeaves($$1, $$4x, $$6, $$8, $$5);
            StructureTemplate.updateShapeAtEdge($$1, 3, $$5x, $$4x.minX(), $$4x.minY(), $$4x.minZ());
            return true;
         }).orElse(false);
      } else {
         return false;
      }
   }

   private static DiscreteVoxelShape updateLeaves(
      net.minecraft.world.level.LevelAccessor $$0, BoundingBox $$1, Set<BlockPos> $$2, Set<BlockPos> $$3, Set<BlockPos> $$4
   ) {
      DiscreteVoxelShape $$5 = new BitSetDiscreteVoxelShape($$1.getXSpan(), $$1.getYSpan(), $$1.getZSpan());
      int $$6 = 7;
      List<Set<BlockPos>> $$7 = Lists.newArrayList();

      for (int $$8 = 0; $$8 < 7; $$8++) {
         $$7.add(Sets.newHashSet());
      }

      for (BlockPos $$9 : Lists.newArrayList(Sets.union($$3, $$4))) {
         if ($$1.isInside($$9)) {
            $$5.fill($$9.getX() - $$1.minX(), $$9.getY() - $$1.minY(), $$9.getZ() - $$1.minZ());
         }
      }

      MutableBlockPos $$10 = new MutableBlockPos();
      int $$11 = 0;
      $$7.get(0).addAll($$2);

      while (true) {
         while ($$11 >= 7 || !$$7.get($$11).isEmpty()) {
            if ($$11 >= 7) {
               return $$5;
            }

            Iterator<BlockPos> $$12 = $$7.get($$11).iterator();
            BlockPos $$13 = $$12.next();
            $$12.remove();
            if ($$1.isInside($$13)) {
               if ($$11 != 0) {
                  BlockState $$14 = $$0.getBlockState($$13);
                  setBlockKnownShape($$0, $$13, $$14.setValue(BlockStateProperties.DISTANCE, $$11));
               }

               $$5.fill($$13.getX() - $$1.minX(), $$13.getY() - $$1.minY(), $$13.getZ() - $$1.minZ());

               for (Direction $$15 : Direction.values()) {
                  $$10.setWithOffset($$13, $$15);
                  if ($$1.isInside($$10)) {
                     int $$16 = $$10.getX() - $$1.minX();
                     int $$17 = $$10.getY() - $$1.minY();
                     int $$18 = $$10.getZ() - $$1.minZ();
                     if (!$$5.isFull($$16, $$17, $$18)) {
                        BlockState $$19 = $$0.getBlockState($$10);
                        OptionalInt $$20 = LeavesBlock.getOptionalDistanceAt($$19);
                        if (!$$20.isEmpty()) {
                           int $$21 = Math.min($$20.getAsInt(), $$11 + 1);
                           if ($$21 < 7) {
                              $$7.get($$21).add($$10.immutable());
                              $$11 = Math.min($$11, $$21);
                           }
                        }
                     }
                  }
               }
            }
         }

         $$11++;
      }
   }

   public static List<BlockPos> getLowestTrunkOrRootOfTree(TreeDecorator.Context $$0) {
      List<BlockPos> $$1 = Lists.newArrayList();
      List<BlockPos> $$2 = $$0.roots();
      List<BlockPos> $$3 = $$0.logs();
      if ($$2.isEmpty()) {
         $$1.addAll($$3);
      } else if (!$$3.isEmpty() && $$2.get(0).getY() == $$3.get(0).getY()) {
         $$1.addAll($$3);
         $$1.addAll($$2);
      } else {
         $$1.addAll($$2);
      }

      return $$1;
   }
}
