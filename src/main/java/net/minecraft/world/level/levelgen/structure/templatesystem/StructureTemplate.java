package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructureTemplate {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String PALETTE_TAG = "palette";
   public static final String PALETTE_LIST_TAG = "palettes";
   public static final String ENTITIES_TAG = "entities";
   public static final String BLOCKS_TAG = "blocks";
   public static final String BLOCK_TAG_POS = "pos";
   public static final String BLOCK_TAG_STATE = "state";
   public static final String BLOCK_TAG_NBT = "nbt";
   public static final String ENTITY_TAG_POS = "pos";
   public static final String ENTITY_TAG_BLOCKPOS = "blockPos";
   public static final String ENTITY_TAG_NBT = "nbt";
   public static final String SIZE_TAG = "size";
   private final List<StructureTemplate.Palette> palettes = Lists.newArrayList();
   private final List<StructureTemplate.StructureEntityInfo> entityInfoList = Lists.newArrayList();
   private Vec3i size = Vec3i.ZERO;
   private String author = "?";

   public Vec3i getSize() {
      return this.size;
   }

   public void setAuthor(String $$0) {
      this.author = $$0;
   }

   public String getAuthor() {
      return this.author;
   }

   public void fillFromWorld(net.minecraft.world.level.Level $$0, BlockPos $$1, Vec3i $$2, boolean $$3, List<Block> $$4) {
      if ($$2.getX() >= 1 && $$2.getY() >= 1 && $$2.getZ() >= 1) {
         BlockPos $$5 = $$1.offset($$2).offset(-1, -1, -1);
         List<StructureTemplate.StructureBlockInfo> $$6 = Lists.newArrayList();
         List<StructureTemplate.StructureBlockInfo> $$7 = Lists.newArrayList();
         List<StructureTemplate.StructureBlockInfo> $$8 = Lists.newArrayList();
         BlockPos $$9 = new BlockPos(Math.min($$1.getX(), $$5.getX()), Math.min($$1.getY(), $$5.getY()), Math.min($$1.getZ(), $$5.getZ()));
         BlockPos $$10 = new BlockPos(Math.max($$1.getX(), $$5.getX()), Math.max($$1.getY(), $$5.getY()), Math.max($$1.getZ(), $$5.getZ()));
         this.size = $$2;
         ScopedCollector $$11 = new ScopedCollector(LOGGER);

         try {
            for (BlockPos $$12 : BlockPos.betweenClosed($$9, $$10)) {
               BlockPos $$13 = $$12.subtract($$9);
               BlockState $$14 = $$0.getBlockState($$12);
               if (!$$4.stream().anyMatch($$14::is)) {
                  BlockEntity $$15 = $$0.getBlockEntity($$12);
                  StructureTemplate.StructureBlockInfo $$17;
                  if ($$15 != null) {
                     TagValueOutput $$16 = TagValueOutput.createWithContext($$11, $$0.registryAccess());
                     $$15.saveWithId($$16);
                     $$17 = new StructureTemplate.StructureBlockInfo($$13, $$14, $$16.buildResult());
                  } else {
                     $$17 = new StructureTemplate.StructureBlockInfo($$13, $$14, null);
                  }

                  addToLists($$17, $$6, $$7, $$8);
               }
            }

            List<StructureTemplate.StructureBlockInfo> $$19 = buildInfoList($$6, $$7, $$8);
            this.palettes.clear();
            this.palettes.add(new StructureTemplate.Palette($$19));
            if ($$3) {
               this.fillEntityList($$0, $$9, $$10, $$11);
            } else {
               this.entityInfoList.clear();
            }
         } catch (Throwable var21) {
            try {
               $$11.close();
            } catch (Throwable var20) {
               var21.addSuppressed(var20);
            }

            throw var21;
         }

         $$11.close();
      }
   }

   private static void addToLists(
      StructureTemplate.StructureBlockInfo $$0,
      List<StructureTemplate.StructureBlockInfo> $$1,
      List<StructureTemplate.StructureBlockInfo> $$2,
      List<StructureTemplate.StructureBlockInfo> $$3
   ) {
      if ($$0.nbt != null) {
         $$2.add($$0);
      } else if (!$$0.state.getBlock().hasDynamicShape()
         && $$0.state.isCollisionShapeFullBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
         $$1.add($$0);
      } else {
         $$3.add($$0);
      }
   }

   private static List<StructureTemplate.StructureBlockInfo> buildInfoList(
      List<StructureTemplate.StructureBlockInfo> $$0, List<StructureTemplate.StructureBlockInfo> $$1, List<StructureTemplate.StructureBlockInfo> $$2
   ) {
      Comparator<StructureTemplate.StructureBlockInfo> $$3 = Comparator.<StructureTemplate.StructureBlockInfo>comparingInt($$0x -> $$0x.pos.getY())
         .thenComparingInt($$0x -> $$0x.pos.getX())
         .thenComparingInt($$0x -> $$0x.pos.getZ());
      $$0.sort($$3);
      $$2.sort($$3);
      $$1.sort($$3);
      List<StructureTemplate.StructureBlockInfo> $$4 = Lists.newArrayList();
      $$4.addAll($$0);
      $$4.addAll($$2);
      $$4.addAll($$1);
      return $$4;
   }

   private void fillEntityList(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockPos $$2, ProblemReporter $$3) {
      List<Entity> $$4 = $$0.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks($$1, $$2), $$0x -> !($$0x instanceof Player));
      this.entityInfoList.clear();

      for (Entity $$5 : $$4) {
         Vec3 $$6 = new Vec3($$5.getX() - $$1.getX(), $$5.getY() - $$1.getY(), $$5.getZ() - $$1.getZ());
         TagValueOutput $$7 = TagValueOutput.createWithContext($$3.forChild($$5.problemPath()), $$5.registryAccess());
         $$5.save($$7);
         BlockPos $$9;
         if ($$5 instanceof Painting $$8) {
            $$9 = $$8.getPos().subtract($$1);
         } else {
            $$9 = BlockPos.containing($$6);
         }

         this.entityInfoList.add(new StructureTemplate.StructureEntityInfo($$6, $$9, $$7.buildResult().copy()));
      }
   }

   public List<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos $$0, StructurePlaceSettings $$1, Block $$2) {
      return this.filterBlocks($$0, $$1, $$2, true);
   }

   public List<StructureTemplate.JigsawBlockInfo> getJigsaws(BlockPos $$0, Rotation $$1) {
      if (this.palettes.isEmpty()) {
         return new ArrayList<>();
      } else {
         StructurePlaceSettings $$2 = new StructurePlaceSettings().setRotation($$1);
         List<StructureTemplate.JigsawBlockInfo> $$3 = $$2.getRandomPalette(this.palettes, $$0).jigsaws();
         List<StructureTemplate.JigsawBlockInfo> $$4 = new ArrayList<>($$3.size());

         for (StructureTemplate.JigsawBlockInfo $$5 : $$3) {
            StructureTemplate.StructureBlockInfo $$6 = $$5.info;
            $$4.add(
               $$5.withInfo(
                  new StructureTemplate.StructureBlockInfo(calculateRelativePosition($$2, $$6.pos()).offset($$0), $$6.state.rotate($$2.getRotation()), $$6.nbt)
               )
            );
         }

         return $$4;
      }
   }

   public ObjectArrayList<StructureTemplate.StructureBlockInfo> filterBlocks(BlockPos $$0, StructurePlaceSettings $$1, Block $$2, boolean $$3) {
      ObjectArrayList<StructureTemplate.StructureBlockInfo> $$4 = new ObjectArrayList();
      BoundingBox $$5 = $$1.getBoundingBox();
      if (this.palettes.isEmpty()) {
         return $$4;
      } else {
         for (StructureTemplate.StructureBlockInfo $$6 : $$1.getRandomPalette(this.palettes, $$0).blocks($$2)) {
            BlockPos $$7 = $$3 ? calculateRelativePosition($$1, $$6.pos).offset($$0) : $$6.pos;
            if ($$5 == null || $$5.isInside($$7)) {
               $$4.add(new StructureTemplate.StructureBlockInfo($$7, $$6.state.rotate($$1.getRotation()), $$6.nbt));
            }
         }

         return $$4;
      }
   }

   public BlockPos calculateConnectedPosition(StructurePlaceSettings $$0, BlockPos $$1, StructurePlaceSettings $$2, BlockPos $$3) {
      BlockPos $$4 = calculateRelativePosition($$0, $$1);
      BlockPos $$5 = calculateRelativePosition($$2, $$3);
      return $$4.subtract($$5);
   }

   public static BlockPos calculateRelativePosition(StructurePlaceSettings $$0, BlockPos $$1) {
      return transform($$1, $$0.getMirror(), $$0.getRotation(), $$0.getRotationPivot());
   }

   public boolean placeInWorld(
      net.minecraft.world.level.ServerLevelAccessor $$0, BlockPos $$1, BlockPos $$2, StructurePlaceSettings $$3, RandomSource $$4, @Block.UpdateFlags int $$5
   ) {
      if (this.palettes.isEmpty()) {
         return false;
      } else {
         List<StructureTemplate.StructureBlockInfo> $$6 = $$3.getRandomPalette(this.palettes, $$1).blocks();
         if ((!$$6.isEmpty() || !$$3.isIgnoreEntities() && !this.entityInfoList.isEmpty())
            && this.size.getX() >= 1
            && this.size.getY() >= 1
            && this.size.getZ() >= 1) {
            BoundingBox $$7 = $$3.getBoundingBox();
            List<BlockPos> $$8 = Lists.newArrayListWithCapacity($$3.shouldApplyWaterlogging() ? $$6.size() : 0);
            List<BlockPos> $$9 = Lists.newArrayListWithCapacity($$3.shouldApplyWaterlogging() ? $$6.size() : 0);
            List<Pair<BlockPos, CompoundTag>> $$10 = Lists.newArrayListWithCapacity($$6.size());
            int $$11 = Integer.MAX_VALUE;
            int $$12 = Integer.MAX_VALUE;
            int $$13 = Integer.MAX_VALUE;
            int $$14 = Integer.MIN_VALUE;
            int $$15 = Integer.MIN_VALUE;
            int $$16 = Integer.MIN_VALUE;
            List<StructureTemplate.StructureBlockInfo> $$17 = processBlockInfos($$0, $$1, $$2, $$3, $$6);
            ScopedCollector $$18 = new ScopedCollector(LOGGER);

            try {
               for (StructureTemplate.StructureBlockInfo $$19 : $$17) {
                  BlockPos $$20 = $$19.pos;
                  if ($$7 == null || $$7.isInside($$20)) {
                     FluidState $$21 = $$3.shouldApplyWaterlogging() ? $$0.getFluidState($$20) : null;
                     BlockState $$22 = $$19.state.mirror($$3.getMirror()).rotate($$3.getRotation());
                     if ($$19.nbt != null) {
                        $$0.setBlock($$20, Blocks.BARRIER.defaultBlockState(), 820);
                     }

                     if ($$0.setBlock($$20, $$22, $$5)) {
                        $$11 = Math.min($$11, $$20.getX());
                        $$12 = Math.min($$12, $$20.getY());
                        $$13 = Math.min($$13, $$20.getZ());
                        $$14 = Math.max($$14, $$20.getX());
                        $$15 = Math.max($$15, $$20.getY());
                        $$16 = Math.max($$16, $$20.getZ());
                        $$10.add(Pair.of($$20, $$19.nbt));
                        if ($$19.nbt != null) {
                           BlockEntity $$23 = $$0.getBlockEntity($$20);
                           if ($$23 != null) {
                              if (!SharedConstants.DEBUG_STRUCTURE_EDIT_MODE && $$23 instanceof RandomizableContainer) {
                                 $$19.nbt.putLong("LootTableSeed", $$4.nextLong());
                              }

                              $$23.loadWithComponents(TagValueInput.create($$18.forChild($$23.problemPath()), $$0.registryAccess(), $$19.nbt));
                           }
                        }

                        if ($$21 != null) {
                           if ($$22.getFluidState().isSource()) {
                              $$9.add($$20);
                           } else if ($$22.getBlock() instanceof LiquidBlockContainer) {
                              ((LiquidBlockContainer)$$22.getBlock()).placeLiquid($$0, $$20, $$22, $$21);
                              if (!$$21.isSource()) {
                                 $$8.add($$20);
                              }
                           }
                        }
                     }
                  }
               }

               boolean $$24 = true;
               Direction[] $$25 = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

               while ($$24 && !$$8.isEmpty()) {
                  $$24 = false;
                  Iterator<BlockPos> $$26 = $$8.iterator();

                  while ($$26.hasNext()) {
                     BlockPos $$27 = $$26.next();
                     FluidState $$28 = $$0.getFluidState($$27);

                     for (int $$29 = 0; $$29 < $$25.length && !$$28.isSource(); $$29++) {
                        BlockPos $$30 = $$27.relative($$25[$$29]);
                        FluidState $$31 = $$0.getFluidState($$30);
                        if ($$31.isSource() && !$$9.contains($$30)) {
                           $$28 = $$31;
                        }
                     }

                     if ($$28.isSource()) {
                        BlockState $$32 = $$0.getBlockState($$27);
                        Block $$33 = $$32.getBlock();
                        if ($$33 instanceof LiquidBlockContainer) {
                           ((LiquidBlockContainer)$$33).placeLiquid($$0, $$27, $$32, $$28);
                           $$24 = true;
                           $$26.remove();
                        }
                     }
                  }
               }

               if ($$11 <= $$14) {
                  if (!$$3.getKnownShape()) {
                     DiscreteVoxelShape $$34 = new BitSetDiscreteVoxelShape($$14 - $$11 + 1, $$15 - $$12 + 1, $$16 - $$13 + 1);
                     int $$35 = $$11;
                     int $$36 = $$12;
                     int $$37 = $$13;

                     for (Pair<BlockPos, CompoundTag> $$38 : $$10) {
                        BlockPos $$39 = (BlockPos)$$38.getFirst();
                        $$34.fill($$39.getX() - $$35, $$39.getY() - $$36, $$39.getZ() - $$37);
                     }

                     updateShapeAtEdge($$0, $$5, $$34, $$35, $$36, $$37);
                  }

                  for (Pair<BlockPos, CompoundTag> $$40 : $$10) {
                     BlockPos $$41 = (BlockPos)$$40.getFirst();
                     if (!$$3.getKnownShape()) {
                        BlockState $$42 = $$0.getBlockState($$41);
                        BlockState $$43 = Block.updateFromNeighbourShapes($$42, $$0, $$41);
                        if ($$42 != $$43) {
                           $$0.setBlock($$41, $$43, $$5 & -2 | 16);
                        }

                        $$0.updateNeighborsAt($$41, $$43.getBlock());
                     }

                     if ($$40.getSecond() != null) {
                        BlockEntity $$44 = $$0.getBlockEntity($$41);
                        if ($$44 != null) {
                           $$44.setChanged();
                        }
                     }
                  }
               }

               if (!$$3.isIgnoreEntities()) {
                  this.placeEntities($$0, $$1, $$3.getMirror(), $$3.getRotation(), $$3.getRotationPivot(), $$7, $$3.shouldFinalizeEntities(), $$18);
               }
            } catch (Throwable var30) {
               try {
                  $$18.close();
               } catch (Throwable var29) {
                  var30.addSuppressed(var29);
               }

               throw var30;
            }

            $$18.close();
            return true;
         } else {
            return false;
         }
      }
   }

   public static void updateShapeAtEdge(net.minecraft.world.level.LevelAccessor $$0, @Block.UpdateFlags int $$1, DiscreteVoxelShape $$2, BlockPos $$3) {
      updateShapeAtEdge($$0, $$1, $$2, $$3.getX(), $$3.getY(), $$3.getZ());
   }

   public static void updateShapeAtEdge(
      net.minecraft.world.level.LevelAccessor $$0, @Block.UpdateFlags int $$1, DiscreteVoxelShape $$2, int $$3, int $$4, int $$5
   ) {
      MutableBlockPos $$6 = new MutableBlockPos();
      MutableBlockPos $$7 = new MutableBlockPos();
      $$2.forAllFaces(($$7x, $$8, $$9, $$10) -> {
         $$6.set($$3 + $$8, $$4 + $$9, $$5 + $$10);
         $$7.setWithOffset($$6, $$7x);
         BlockState $$11 = $$0.getBlockState($$6);
         BlockState $$12 = $$0.getBlockState($$7);
         BlockState $$13 = $$11.updateShape($$0, $$0, $$6, $$7x, $$7, $$12, $$0.getRandom());
         if ($$11 != $$13) {
            $$0.setBlock($$6, $$13, $$1 & -2);
         }

         BlockState $$14 = $$12.updateShape($$0, $$0, $$7, $$7x.getOpposite(), $$6, $$13, $$0.getRandom());
         if ($$12 != $$14) {
            $$0.setBlock($$7, $$14, $$1 & -2);
         }
      });
   }

   public static List<StructureTemplate.StructureBlockInfo> processBlockInfos(
      net.minecraft.world.level.ServerLevelAccessor $$0, BlockPos $$1, BlockPos $$2, StructurePlaceSettings $$3, List<StructureTemplate.StructureBlockInfo> $$4
   ) {
      List<StructureTemplate.StructureBlockInfo> $$5 = new ArrayList<>();
      List<StructureTemplate.StructureBlockInfo> $$6 = new ArrayList<>();

      for (StructureTemplate.StructureBlockInfo $$7 : $$4) {
         BlockPos $$8 = calculateRelativePosition($$3, $$7.pos).offset($$1);
         StructureTemplate.StructureBlockInfo $$9 = new StructureTemplate.StructureBlockInfo($$8, $$7.state, $$7.nbt != null ? $$7.nbt.copy() : null);
         Iterator<StructureProcessor> $$10 = $$3.getProcessors().iterator();

         while ($$9 != null && $$10.hasNext()) {
            $$9 = $$10.next().processBlock($$0, $$1, $$2, $$7, $$9, $$3);
         }

         if ($$9 != null) {
            $$6.add($$9);
            $$5.add($$7);
         }
      }

      for (StructureProcessor $$11 : $$3.getProcessors()) {
         $$6 = $$11.finalizeProcessing($$0, $$1, $$2, $$5, $$6, $$3);
      }

      return $$6;
   }

   private void placeEntities(
      net.minecraft.world.level.ServerLevelAccessor $$0,
      BlockPos $$1,
      Mirror $$2,
      Rotation $$3,
      BlockPos $$4,
      @Nullable BoundingBox $$5,
      boolean $$6,
      ProblemReporter $$7
   ) {
      for (StructureTemplate.StructureEntityInfo $$8 : this.entityInfoList) {
         BlockPos $$9 = transform($$8.blockPos, $$2, $$3, $$4).offset($$1);
         if ($$5 == null || $$5.isInside($$9)) {
            CompoundTag $$10 = $$8.nbt.copy();
            Vec3 $$11 = transform($$8.pos, $$2, $$3, $$4);
            Vec3 $$12 = $$11.add($$1.getX(), $$1.getY(), $$1.getZ());
            ListTag $$13 = new ListTag();
            $$13.add(DoubleTag.valueOf($$12.x));
            $$13.add(DoubleTag.valueOf($$12.y));
            $$13.add(DoubleTag.valueOf($$12.z));
            $$10.put("Pos", $$13);
            $$10.remove("UUID");
            createEntityIgnoreException($$7, $$0, $$10).ifPresent($$5x -> {
               float $$6x = $$5x.rotate($$3);
               $$6x += $$5x.mirror($$2) - $$5x.getYRot();
               $$5x.snapTo($$12.x, $$12.y, $$12.z, $$6x, $$5x.getXRot());
               $$5x.setYBodyRot($$6x);
               $$5x.setYHeadRot($$6x);
               if ($$6 && $$5x instanceof Mob $$7x) {
                  $$7x.finalizeSpawn($$0, $$0.getCurrentDifficultyAt(BlockPos.containing($$12)), EntitySpawnReason.STRUCTURE, null);
               }

               $$0.addFreshEntityWithPassengers($$5x);
            });
         }
      }
   }

   private static Optional<Entity> createEntityIgnoreException(ProblemReporter $$0, net.minecraft.world.level.ServerLevelAccessor $$1, CompoundTag $$2) {
      try {
         return EntityType.create(TagValueInput.create($$0, $$1.registryAccess(), $$2), $$1.getLevel(), EntitySpawnReason.STRUCTURE);
      } catch (Exception var4) {
         return Optional.empty();
      }
   }

   public Vec3i getSize(Rotation $$0) {
      switch ($$0) {
         case COUNTERCLOCKWISE_90:
         case CLOCKWISE_90:
            return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
         default:
            return this.size;
      }
   }

   public static BlockPos transform(BlockPos $$0, Mirror $$1, Rotation $$2, BlockPos $$3) {
      int $$4 = $$0.getX();
      int $$5 = $$0.getY();
      int $$6 = $$0.getZ();
      boolean $$7 = true;
      switch ($$1) {
         case LEFT_RIGHT:
            $$6 = -$$6;
            break;
         case FRONT_BACK:
            $$4 = -$$4;
            break;
         default:
            $$7 = false;
      }

      int $$8 = $$3.getX();
      int $$9 = $$3.getZ();
      switch ($$2) {
         case COUNTERCLOCKWISE_90:
            return new BlockPos($$8 - $$9 + $$6, $$5, $$8 + $$9 - $$4);
         case CLOCKWISE_90:
            return new BlockPos($$8 + $$9 - $$6, $$5, $$9 - $$8 + $$4);
         case CLOCKWISE_180:
            return new BlockPos($$8 + $$8 - $$4, $$5, $$9 + $$9 - $$6);
         default:
            return $$7 ? new BlockPos($$4, $$5, $$6) : $$0;
      }
   }

   public static Vec3 transform(Vec3 $$0, Mirror $$1, Rotation $$2, BlockPos $$3) {
      double $$4 = $$0.x;
      double $$5 = $$0.y;
      double $$6 = $$0.z;
      boolean $$7 = true;
      switch ($$1) {
         case LEFT_RIGHT:
            $$6 = 1.0 - $$6;
            break;
         case FRONT_BACK:
            $$4 = 1.0 - $$4;
            break;
         default:
            $$7 = false;
      }

      int $$8 = $$3.getX();
      int $$9 = $$3.getZ();
      switch ($$2) {
         case COUNTERCLOCKWISE_90:
            return new Vec3($$8 - $$9 + $$6, $$5, $$8 + $$9 + 1 - $$4);
         case CLOCKWISE_90:
            return new Vec3($$8 + $$9 + 1 - $$6, $$5, $$9 - $$8 + $$4);
         case CLOCKWISE_180:
            return new Vec3($$8 + $$8 + 1 - $$4, $$5, $$9 + $$9 + 1 - $$6);
         default:
            return $$7 ? new Vec3($$4, $$5, $$6) : $$0;
      }
   }

   public BlockPos getZeroPositionWithTransform(BlockPos $$0, Mirror $$1, Rotation $$2) {
      return getZeroPositionWithTransform($$0, $$1, $$2, this.getSize().getX(), this.getSize().getZ());
   }

   public static BlockPos getZeroPositionWithTransform(BlockPos $$0, Mirror $$1, Rotation $$2, int $$3, int $$4) {
      $$3--;
      $$4--;
      int $$5 = $$1 == Mirror.FRONT_BACK ? $$3 : 0;
      int $$6 = $$1 == Mirror.LEFT_RIGHT ? $$4 : 0;
      BlockPos $$7 = $$0;
      switch ($$2) {
         case COUNTERCLOCKWISE_90:
            $$7 = $$0.offset($$6, 0, $$3 - $$5);
            break;
         case CLOCKWISE_90:
            $$7 = $$0.offset($$4 - $$6, 0, $$5);
            break;
         case CLOCKWISE_180:
            $$7 = $$0.offset($$3 - $$5, 0, $$4 - $$6);
            break;
         case NONE:
            $$7 = $$0.offset($$5, 0, $$6);
      }

      return $$7;
   }

   public BoundingBox getBoundingBox(StructurePlaceSettings $$0, BlockPos $$1) {
      return this.getBoundingBox($$1, $$0.getRotation(), $$0.getRotationPivot(), $$0.getMirror());
   }

   public BoundingBox getBoundingBox(BlockPos $$0, Rotation $$1, BlockPos $$2, Mirror $$3) {
      return getBoundingBox($$0, $$1, $$2, $$3, this.size);
   }

   @VisibleForTesting
   protected static BoundingBox getBoundingBox(BlockPos $$0, Rotation $$1, BlockPos $$2, Mirror $$3, Vec3i $$4) {
      Vec3i $$5 = $$4.offset(-1, -1, -1);
      BlockPos $$6 = transform(BlockPos.ZERO, $$3, $$1, $$2);
      BlockPos $$7 = transform(BlockPos.ZERO.offset($$5), $$3, $$1, $$2);
      return BoundingBox.fromCorners($$6, $$7).move($$0);
   }

   public CompoundTag save(CompoundTag $$0) {
      if (this.palettes.isEmpty()) {
         $$0.put("blocks", new ListTag());
         $$0.put("palette", new ListTag());
      } else {
         List<StructureTemplate.SimplePalette> $$1 = Lists.newArrayList();
         StructureTemplate.SimplePalette $$2 = new StructureTemplate.SimplePalette();
         $$1.add($$2);

         for (int $$3 = 1; $$3 < this.palettes.size(); $$3++) {
            $$1.add(new StructureTemplate.SimplePalette());
         }

         ListTag $$4 = new ListTag();
         List<StructureTemplate.StructureBlockInfo> $$5 = this.palettes.get(0).blocks();

         for (int $$6 = 0; $$6 < $$5.size(); $$6++) {
            StructureTemplate.StructureBlockInfo $$7 = $$5.get($$6);
            CompoundTag $$8 = new CompoundTag();
            $$8.put("pos", this.newIntegerList($$7.pos.getX(), $$7.pos.getY(), $$7.pos.getZ()));
            int $$9 = $$2.idFor($$7.state);
            $$8.putInt("state", $$9);
            if ($$7.nbt != null) {
               $$8.put("nbt", $$7.nbt);
            }

            $$4.add($$8);

            for (int $$10 = 1; $$10 < this.palettes.size(); $$10++) {
               StructureTemplate.SimplePalette $$11 = $$1.get($$10);
               $$11.addMapping(this.palettes.get($$10).blocks().get($$6).state, $$9);
            }
         }

         $$0.put("blocks", $$4);
         if ($$1.size() == 1) {
            ListTag $$12 = new ListTag();

            for (BlockState $$13 : $$2) {
               $$12.add(NbtUtils.writeBlockState($$13));
            }

            $$0.put("palette", $$12);
         } else {
            ListTag $$14 = new ListTag();

            for (StructureTemplate.SimplePalette $$15 : $$1) {
               ListTag $$16 = new ListTag();

               for (BlockState $$17 : $$15) {
                  $$16.add(NbtUtils.writeBlockState($$17));
               }

               $$14.add($$16);
            }

            $$0.put("palettes", $$14);
         }
      }

      ListTag $$18 = new ListTag();

      for (StructureTemplate.StructureEntityInfo $$19 : this.entityInfoList) {
         CompoundTag $$20 = new CompoundTag();
         $$20.put("pos", this.newDoubleList($$19.pos.x, $$19.pos.y, $$19.pos.z));
         $$20.put("blockPos", this.newIntegerList($$19.blockPos.getX(), $$19.blockPos.getY(), $$19.blockPos.getZ()));
         if ($$19.nbt != null) {
            $$20.put("nbt", $$19.nbt);
         }

         $$18.add($$20);
      }

      $$0.put("entities", $$18);
      $$0.put("size", this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
      return NbtUtils.addCurrentDataVersion($$0);
   }

   public void load(HolderGetter<Block> $$0, CompoundTag $$1) {
      this.palettes.clear();
      this.entityInfoList.clear();
      ListTag $$2 = $$1.getListOrEmpty("size");
      this.size = new Vec3i($$2.getIntOr(0, 0), $$2.getIntOr(1, 0), $$2.getIntOr(2, 0));
      ListTag $$3 = $$1.getListOrEmpty("blocks");
      Optional<ListTag> $$4 = $$1.getList("palettes");
      if ($$4.isPresent()) {
         for (int $$5 = 0; $$5 < $$4.get().size(); $$5++) {
            this.loadPalette($$0, $$4.get().getListOrEmpty($$5), $$3);
         }
      } else {
         this.loadPalette($$0, $$1.getListOrEmpty("palette"), $$3);
      }

      $$1.getListOrEmpty("entities").compoundStream().forEach($$0x -> {
         ListTag $$1x = $$0x.getListOrEmpty("pos");
         Vec3 $$2x = new Vec3($$1x.getDoubleOr(0, 0.0), $$1x.getDoubleOr(1, 0.0), $$1x.getDoubleOr(2, 0.0));
         ListTag $$3x = $$0x.getListOrEmpty("blockPos");
         BlockPos $$4x = new BlockPos($$3x.getIntOr(0, 0), $$3x.getIntOr(1, 0), $$3x.getIntOr(2, 0));
         $$0x.getCompound("nbt").ifPresent($$2xx -> this.entityInfoList.add(new StructureTemplate.StructureEntityInfo($$2x, $$4x, $$2xx)));
      });
   }

   private void loadPalette(HolderGetter<Block> $$0, ListTag $$1, ListTag $$2) {
      StructureTemplate.SimplePalette $$3 = new StructureTemplate.SimplePalette();

      for (int $$4 = 0; $$4 < $$1.size(); $$4++) {
         $$3.addMapping(NbtUtils.readBlockState($$0, $$1.getCompoundOrEmpty($$4)), $$4);
      }

      List<StructureTemplate.StructureBlockInfo> $$5 = Lists.newArrayList();
      List<StructureTemplate.StructureBlockInfo> $$6 = Lists.newArrayList();
      List<StructureTemplate.StructureBlockInfo> $$7 = Lists.newArrayList();
      $$2.compoundStream().forEach($$4 -> {
         ListTag $$5x = $$4.getListOrEmpty("pos");
         BlockPos $$6x = new BlockPos($$5x.getIntOr(0, 0), $$5x.getIntOr(1, 0), $$5x.getIntOr(2, 0));
         BlockState $$7x = $$3.stateFor($$4.getIntOr("state", 0));
         CompoundTag $$8x = (CompoundTag)$$4.getCompound("nbt").orElse(null);
         StructureTemplate.StructureBlockInfo $$9 = new StructureTemplate.StructureBlockInfo($$6x, $$7x, $$8x);
         addToLists($$9, $$5, $$6, $$7);
      });
      List<StructureTemplate.StructureBlockInfo> $$8 = buildInfoList($$5, $$6, $$7);
      this.palettes.add(new StructureTemplate.Palette($$8));
   }

   private ListTag newIntegerList(int... $$0) {
      ListTag $$1 = new ListTag();

      for (int $$2 : $$0) {
         $$1.add(IntTag.valueOf($$2));
      }

      return $$1;
   }

   private ListTag newDoubleList(double... $$0) {
      ListTag $$1 = new ListTag();

      for (double $$2 : $$0) {
         $$1.add(DoubleTag.valueOf($$2));
      }

      return $$1;
   }

   public static JigsawBlockEntity.JointType getJointType(CompoundTag $$0, BlockState $$1) {
      return $$0.read("joint", JigsawBlockEntity.JointType.CODEC).orElseGet(() -> getDefaultJointType($$1));
   }

   public static JigsawBlockEntity.JointType getDefaultJointType(BlockState $$0) {
      return JigsawBlock.getFrontFacing($$0).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
   }

   public record JigsawBlockInfo(
      StructureTemplate.StructureBlockInfo info,
      JigsawBlockEntity.JointType jointType,
      Identifier name,
      ResourceKey<StructureTemplatePool> pool,
      Identifier target,
      int placementPriority,
      int selectionPriority
   ) {

      public static StructureTemplate.JigsawBlockInfo of(StructureTemplate.StructureBlockInfo $$0) {
         CompoundTag $$1 = Objects.requireNonNull($$0.nbt(), () -> $$0 + " nbt was null");
         return new StructureTemplate.JigsawBlockInfo(
            $$0,
            StructureTemplate.getJointType($$1, $$0.state()),
            $$1.read("name", Identifier.CODEC).orElse(JigsawBlockEntity.EMPTY_ID),
            $$1.read("pool", JigsawBlockEntity.POOL_CODEC).orElse(Pools.EMPTY),
            $$1.read("target", Identifier.CODEC).orElse(JigsawBlockEntity.EMPTY_ID),
            $$1.getIntOr("placement_priority", 0),
            $$1.getIntOr("selection_priority", 0)
         );
      }

      @Override
      public String toString() {
         return String.format(
            Locale.ROOT,
            "<JigsawBlockInfo | %s | %s | name: %s | pool: %s | target: %s | placement: %d | selection: %d | %s>",
            this.info.pos,
            this.info.state,
            this.name,
            this.pool.identifier(),
            this.target,
            this.placementPriority,
            this.selectionPriority,
            this.info.nbt
         );
      }

      public StructureTemplate.JigsawBlockInfo withInfo(StructureTemplate.StructureBlockInfo $$0) {
         return new StructureTemplate.JigsawBlockInfo($$0, this.jointType, this.name, this.pool, this.target, this.placementPriority, this.selectionPriority);
      }
   }

   public static final class Palette {
      private final List<StructureTemplate.StructureBlockInfo> blocks;
      private final Map<Block, List<StructureTemplate.StructureBlockInfo>> cache = Maps.newHashMap();
      @Nullable
      private List<StructureTemplate.JigsawBlockInfo> cachedJigsaws;

      Palette(List<StructureTemplate.StructureBlockInfo> $$0) {
         this.blocks = $$0;
      }

      public List<StructureTemplate.JigsawBlockInfo> jigsaws() {
         if (this.cachedJigsaws == null) {
            this.cachedJigsaws = this.blocks(Blocks.JIGSAW).stream().map(StructureTemplate.JigsawBlockInfo::of).toList();
         }

         return this.cachedJigsaws;
      }

      public List<StructureTemplate.StructureBlockInfo> blocks() {
         return this.blocks;
      }

      public List<StructureTemplate.StructureBlockInfo> blocks(Block $$0) {
         return this.cache.computeIfAbsent($$0, $$0x -> this.blocks.stream().filter($$1 -> $$1.state.is($$0x)).collect(Collectors.toList()));
      }
   }

   static class SimplePalette implements Iterable<BlockState> {
      public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
      private final IdMapper<BlockState> ids = new IdMapper(16);
      private int lastId;

      public int idFor(BlockState $$0) {
         int $$1 = this.ids.getId($$0);
         if ($$1 == -1) {
            $$1 = this.lastId++;
            this.ids.addMapping($$0, $$1);
         }

         return $$1;
      }

      @Nullable
      public BlockState stateFor(int $$0) {
         BlockState $$1 = (BlockState)this.ids.byId($$0);
         return $$1 == null ? DEFAULT_BLOCK_STATE : $$1;
      }

      @Override
      public Iterator<BlockState> iterator() {
         return this.ids.iterator();
      }

      public void addMapping(BlockState $$0, int $$1) {
         this.ids.addMapping($$0, $$1);
      }
   }

   public record StructureBlockInfo(BlockPos pos, BlockState state, @Nullable CompoundTag nbt) {

      @Override
      public String toString() {
         return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
      }
   }

   public static class StructureEntityInfo {
      public final Vec3 pos;
      public final BlockPos blockPos;
      public final CompoundTag nbt;

      public StructureEntityInfo(Vec3 $$0, BlockPos $$1, CompoundTag $$2) {
         this.pos = $$0;
         this.blockPos = $$1;
         this.nbt = $$2;
      }
   }
}
