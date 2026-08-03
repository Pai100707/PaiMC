package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction8;
import net.minecraft.core.SectionPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.ticks.SavedTick;
import org.slf4j.Logger;

public class UpgradeData {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final UpgradeData EMPTY = new UpgradeData(net.minecraft.world.level.EmptyBlockGetter.INSTANCE);
   private static final String TAG_INDICES = "Indices";
   private static final Direction8[] DIRECTIONS = Direction8.values();
   private static final Codec<List<SavedTick<Block>>> BLOCK_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.BLOCK.byNameCodec().orElse(Blocks.AIR)).listOf();
   private static final Codec<List<SavedTick<Fluid>>> FLUID_TICKS_CODEC = SavedTick.codec(BuiltInRegistries.FLUID.byNameCodec().orElse(Fluids.EMPTY)).listOf();
   private final EnumSet<Direction8> sides = EnumSet.noneOf(Direction8.class);
   private final List<SavedTick<Block>> neighborBlockTicks = Lists.newArrayList();
   private final List<SavedTick<Fluid>> neighborFluidTicks = Lists.newArrayList();
   private final int[][] index;
   static final Map<Block, UpgradeData.BlockFixer> MAP = new IdentityHashMap<>();
   static final Set<UpgradeData.BlockFixer> CHUNKY_FIXERS = Sets.newHashSet();

   private UpgradeData(net.minecraft.world.level.LevelHeightAccessor $$0) {
      this.index = new int[$$0.getSectionsCount()][];
   }

   public UpgradeData(CompoundTag $$0, net.minecraft.world.level.LevelHeightAccessor $$1) {
      this($$1);
      $$0.getCompound("Indices").ifPresent($$0x -> {
         for (int $$1x = 0; $$1x < this.index.length; $$1x++) {
            this.index[$$1x] = (int[])$$0x.getIntArray(String.valueOf($$1x)).orElse(null);
         }
      });
      int $$2 = $$0.getIntOr("Sides", 0);

      for (Direction8 $$3 : Direction8.values()) {
         if (($$2 & 1 << $$3.ordinal()) != 0) {
            this.sides.add($$3);
         }
      }

      $$0.read("neighbor_block_ticks", BLOCK_TICKS_CODEC).ifPresent(this.neighborBlockTicks::addAll);
      $$0.read("neighbor_fluid_ticks", FLUID_TICKS_CODEC).ifPresent(this.neighborFluidTicks::addAll);
   }

   private UpgradeData(UpgradeData $$0) {
      this.sides.addAll($$0.sides);
      this.neighborBlockTicks.addAll($$0.neighborBlockTicks);
      this.neighborFluidTicks.addAll($$0.neighborFluidTicks);
      this.index = new int[$$0.index.length][];

      for (int $$1 = 0; $$1 < $$0.index.length; $$1++) {
         int[] $$2 = $$0.index[$$1];
         this.index[$$1] = $$2 != null ? IntArrays.copy($$2) : null;
      }
   }

   public void upgrade(LevelChunk $$0) {
      this.upgradeInside($$0);

      for (Direction8 $$1 : DIRECTIONS) {
         upgradeSides($$0, $$1);
      }

      net.minecraft.world.level.Level $$2 = $$0.getLevel();
      this.neighborBlockTicks.forEach($$1x -> {
         Block $$2x = $$1x.type() == Blocks.AIR ? $$2.getBlockState($$1x.pos()).getBlock() : (Block)$$1x.type();
         $$2.scheduleTick($$1x.pos(), $$2x, $$1x.delay(), $$1x.priority());
      });
      this.neighborFluidTicks.forEach($$1x -> {
         Fluid $$2x = $$1x.type() == Fluids.EMPTY ? $$2.getFluidState($$1x.pos()).getType() : (Fluid)$$1x.type();
         $$2.scheduleTick($$1x.pos(), $$2x, $$1x.delay(), $$1x.priority());
      });
      CHUNKY_FIXERS.forEach($$1x -> $$1x.processChunk($$2));
   }

   private static void upgradeSides(LevelChunk $$0, Direction8 $$1) {
      net.minecraft.world.level.Level $$2 = $$0.getLevel();
      if ($$0.getUpgradeData().sides.remove($$1)) {
         Set<Direction> $$3 = $$1.getDirections();
         int $$4 = 0;
         int $$5 = 15;
         boolean $$6 = $$3.contains(Direction.EAST);
         boolean $$7 = $$3.contains(Direction.WEST);
         boolean $$8 = $$3.contains(Direction.SOUTH);
         boolean $$9 = $$3.contains(Direction.NORTH);
         boolean $$10 = $$3.size() == 1;
         net.minecraft.world.level.ChunkPos $$11 = $$0.getPos();
         int $$12 = $$11.getMinBlockX() + (!$$10 || !$$9 && !$$8 ? ($$7 ? 0 : 15) : 1);
         int $$13 = $$11.getMinBlockX() + (!$$10 || !$$9 && !$$8 ? ($$7 ? 0 : 15) : 14);
         int $$14 = $$11.getMinBlockZ() + (!$$10 || !$$6 && !$$7 ? ($$9 ? 0 : 15) : 1);
         int $$15 = $$11.getMinBlockZ() + (!$$10 || !$$6 && !$$7 ? ($$9 ? 0 : 15) : 14);
         Direction[] $$16 = Direction.values();
         MutableBlockPos $$17 = new MutableBlockPos();

         for (BlockPos $$18 : BlockPos.betweenClosed($$12, $$2.getMinY(), $$14, $$13, $$2.getMaxY(), $$15)) {
            BlockState $$19 = $$2.getBlockState($$18);
            BlockState $$20 = $$19;

            for (Direction $$21 : $$16) {
               $$17.setWithOffset($$18, $$21);
               $$20 = updateState($$20, $$21, $$2, $$18, $$17);
            }

            Block.updateOrDestroy($$19, $$20, $$2, $$18, 18);
         }
      }
   }

   private static BlockState updateState(BlockState $$0, Direction $$1, net.minecraft.world.level.LevelAccessor $$2, BlockPos $$3, BlockPos $$4) {
      return MAP.getOrDefault($$0.getBlock(), UpgradeData.BlockFixers.DEFAULT).updateShape($$0, $$1, $$2.getBlockState($$4), $$2, $$3, $$4);
   }

   private void upgradeInside(LevelChunk $$0) {
      MutableBlockPos $$1 = new MutableBlockPos();
      MutableBlockPos $$2 = new MutableBlockPos();
      net.minecraft.world.level.ChunkPos $$3 = $$0.getPos();
      net.minecraft.world.level.LevelAccessor $$4 = $$0.getLevel();

      for (int $$5 = 0; $$5 < this.index.length; $$5++) {
         LevelChunkSection $$6 = $$0.getSection($$5);
         int[] $$7 = this.index[$$5];
         this.index[$$5] = null;
         if ($$7 != null && $$7.length > 0) {
            Direction[] $$8 = Direction.values();
            PalettedContainer<BlockState> $$9 = $$6.getStates();
            int $$10 = $$0.getSectionYFromSectionIndex($$5);
            int $$11 = SectionPos.sectionToBlockCoord($$10);

            for (int $$12 : $$7) {
               int $$13 = $$12 & 15;
               int $$14 = $$12 >> 8 & 15;
               int $$15 = $$12 >> 4 & 15;
               $$1.set($$3.getMinBlockX() + $$13, $$11 + $$14, $$3.getMinBlockZ() + $$15);
               BlockState $$16 = $$9.get($$12);
               BlockState $$17 = $$16;

               for (Direction $$18 : $$8) {
                  $$2.setWithOffset($$1, $$18);
                  if (SectionPos.blockToSectionCoord($$1.getX()) == $$3.x && SectionPos.blockToSectionCoord($$1.getZ()) == $$3.z) {
                     $$17 = updateState($$17, $$18, $$4, $$1, $$2);
                  }
               }

               Block.updateOrDestroy($$16, $$17, $$4, $$1, 18);
            }
         }
      }

      for (int $$19 = 0; $$19 < this.index.length; $$19++) {
         if (this.index[$$19] != null) {
            LOGGER.warn("Discarding update data for section {} for chunk ({} {})", new Object[]{$$4.getSectionYFromSectionIndex($$19), $$3.x, $$3.z});
         }

         this.index[$$19] = null;
      }
   }

   public boolean isEmpty() {
      for (int[] $$0 : this.index) {
         if ($$0 != null) {
            return false;
         }
      }

      return this.sides.isEmpty();
   }

   public CompoundTag write() {
      CompoundTag $$0 = new CompoundTag();
      CompoundTag $$1 = new CompoundTag();

      for (int $$2 = 0; $$2 < this.index.length; $$2++) {
         String $$3 = String.valueOf($$2);
         if (this.index[$$2] != null && this.index[$$2].length != 0) {
            $$1.putIntArray($$3, this.index[$$2]);
         }
      }

      if (!$$1.isEmpty()) {
         $$0.put("Indices", $$1);
      }

      int $$4 = 0;

      for (Direction8 $$5 : this.sides) {
         $$4 |= 1 << $$5.ordinal();
      }

      $$0.putByte("Sides", (byte)$$4);
      if (!this.neighborBlockTicks.isEmpty()) {
         $$0.store("neighbor_block_ticks", BLOCK_TICKS_CODEC, this.neighborBlockTicks);
      }

      if (!this.neighborFluidTicks.isEmpty()) {
         $$0.store("neighbor_fluid_ticks", FLUID_TICKS_CODEC, this.neighborFluidTicks);
      }

      return $$0;
   }

   public UpgradeData copy() {
      return this == EMPTY ? EMPTY : new UpgradeData(this);
   }

   public interface BlockFixer {
      BlockState updateShape(BlockState var1, Direction var2, BlockState var3, net.minecraft.world.level.LevelAccessor var4, BlockPos var5, BlockPos var6);

      default void processChunk(net.minecraft.world.level.LevelAccessor $$0) {
      }
   }

   static enum BlockFixers implements UpgradeData.BlockFixer {
      BLACKLIST(
         Blocks.OBSERVER,
         Blocks.NETHER_PORTAL,
         Blocks.WHITE_CONCRETE_POWDER,
         Blocks.ORANGE_CONCRETE_POWDER,
         Blocks.MAGENTA_CONCRETE_POWDER,
         Blocks.LIGHT_BLUE_CONCRETE_POWDER,
         Blocks.YELLOW_CONCRETE_POWDER,
         Blocks.LIME_CONCRETE_POWDER,
         Blocks.PINK_CONCRETE_POWDER,
         Blocks.GRAY_CONCRETE_POWDER,
         Blocks.LIGHT_GRAY_CONCRETE_POWDER,
         Blocks.CYAN_CONCRETE_POWDER,
         Blocks.PURPLE_CONCRETE_POWDER,
         Blocks.BLUE_CONCRETE_POWDER,
         Blocks.BROWN_CONCRETE_POWDER,
         Blocks.GREEN_CONCRETE_POWDER,
         Blocks.RED_CONCRETE_POWDER,
         Blocks.BLACK_CONCRETE_POWDER,
         Blocks.ANVIL,
         Blocks.CHIPPED_ANVIL,
         Blocks.DAMAGED_ANVIL,
         Blocks.DRAGON_EGG,
         Blocks.GRAVEL,
         Blocks.SAND,
         Blocks.RED_SAND,
         Blocks.OAK_SIGN,
         Blocks.SPRUCE_SIGN,
         Blocks.BIRCH_SIGN,
         Blocks.ACACIA_SIGN,
         Blocks.CHERRY_SIGN,
         Blocks.JUNGLE_SIGN,
         Blocks.DARK_OAK_SIGN,
         Blocks.PALE_OAK_SIGN,
         Blocks.OAK_WALL_SIGN,
         Blocks.SPRUCE_WALL_SIGN,
         Blocks.BIRCH_WALL_SIGN,
         Blocks.ACACIA_WALL_SIGN,
         Blocks.JUNGLE_WALL_SIGN,
         Blocks.DARK_OAK_WALL_SIGN,
         Blocks.PALE_OAK_WALL_SIGN,
         Blocks.OAK_HANGING_SIGN,
         Blocks.SPRUCE_HANGING_SIGN,
         Blocks.BIRCH_HANGING_SIGN,
         Blocks.ACACIA_HANGING_SIGN,
         Blocks.JUNGLE_HANGING_SIGN,
         Blocks.DARK_OAK_HANGING_SIGN,
         Blocks.PALE_OAK_HANGING_SIGN,
         Blocks.OAK_WALL_HANGING_SIGN,
         Blocks.SPRUCE_WALL_HANGING_SIGN,
         Blocks.BIRCH_WALL_HANGING_SIGN,
         Blocks.ACACIA_WALL_HANGING_SIGN,
         Blocks.JUNGLE_WALL_HANGING_SIGN,
         Blocks.DARK_OAK_WALL_HANGING_SIGN,
         Blocks.PALE_OAK_WALL_HANGING_SIGN
      ) {
         @Override
         public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, net.minecraft.world.level.LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
            return $$0;
         }
      },
      DEFAULT {
         @Override
         public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, net.minecraft.world.level.LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
            return $$0.updateShape($$3, $$3, $$4, $$1, $$5, $$3.getBlockState($$5), $$3.getRandom());
         }
      },
      CHEST(Blocks.CHEST, Blocks.TRAPPED_CHEST) {
         @Override
         public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, net.minecraft.world.level.LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
            if ($$2.is($$0.getBlock())
               && $$1.getAxis().isHorizontal()
               && $$0.getValue(ChestBlock.TYPE) == ChestType.SINGLE
               && $$2.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
               Direction $$6 = $$0.getValue(ChestBlock.FACING);
               if ($$1.getAxis() != $$6.getAxis() && $$6 == $$2.getValue(ChestBlock.FACING)) {
                  ChestType $$7 = $$1 == $$6.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                  $$3.setBlock($$5, $$2.setValue(ChestBlock.TYPE, $$7.getOpposite()), 18);
                  if ($$6 == Direction.NORTH || $$6 == Direction.EAST) {
                     BlockEntity $$8 = $$3.getBlockEntity($$4);
                     BlockEntity $$9 = $$3.getBlockEntity($$5);
                     if ($$8 instanceof ChestBlockEntity && $$9 instanceof ChestBlockEntity) {
                        ChestBlockEntity.swapContents((ChestBlockEntity)$$8, (ChestBlockEntity)$$9);
                     }
                  }

                  return $$0.setValue(ChestBlock.TYPE, $$7);
               }
            }

            return $$0;
         }
      },
      LEAVES(
         true,
         Blocks.ACACIA_LEAVES,
         Blocks.CHERRY_LEAVES,
         Blocks.BIRCH_LEAVES,
         Blocks.PALE_OAK_LEAVES,
         Blocks.DARK_OAK_LEAVES,
         Blocks.JUNGLE_LEAVES,
         Blocks.OAK_LEAVES,
         Blocks.SPRUCE_LEAVES
      ) {
         private final ThreadLocal<List<ObjectSet<BlockPos>>> queue = ThreadLocal.withInitial(() -> Lists.newArrayListWithCapacity(7));

         @Override
         public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, net.minecraft.world.level.LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
            BlockState $$6 = $$0.updateShape($$3, $$3, $$4, $$1, $$5, $$3.getBlockState($$5), $$3.getRandom());
            if ($$0 != $$6) {
               int $$7 = $$6.getValue(BlockStateProperties.DISTANCE);
               List<ObjectSet<BlockPos>> $$8 = this.queue.get();
               if ($$8.isEmpty()) {
                  for (int $$9 = 0; $$9 < 7; $$9++) {
                     $$8.add(new ObjectOpenHashSet());
                  }
               }

               $$8.get($$7).add($$4.immutable());
            }

            return $$0;
         }

         @Override
         public void processChunk(net.minecraft.world.level.LevelAccessor $$0) {
            MutableBlockPos $$1 = new MutableBlockPos();
            List<ObjectSet<BlockPos>> $$2 = this.queue.get();

            for (int $$3 = 2; $$3 < $$2.size(); $$3++) {
               int $$4 = $$3 - 1;
               ObjectSet<BlockPos> $$5 = $$2.get($$4);
               ObjectSet<BlockPos> $$6 = $$2.get($$3);
               ObjectIterator var8 = $$5.iterator();

               while (var8.hasNext()) {
                  BlockPos $$7 = (BlockPos)var8.next();
                  BlockState $$8 = $$0.getBlockState($$7);
                  if ($$8.getValue(BlockStateProperties.DISTANCE) >= $$4) {
                     $$0.setBlock($$7, $$8.setValue(BlockStateProperties.DISTANCE, $$4), 18);
                     if ($$3 != 7) {
                        for (Direction $$9 : DIRECTIONS) {
                           $$1.setWithOffset($$7, $$9);
                           BlockState $$10 = $$0.getBlockState($$1);
                           if ($$10.hasProperty(BlockStateProperties.DISTANCE) && $$8.getValue(BlockStateProperties.DISTANCE) > $$3) {
                              $$6.add($$1.immutable());
                           }
                        }
                     }
                  }
               }
            }

            $$2.clear();
         }
      },
      STEM_BLOCK(Blocks.MELON_STEM, Blocks.PUMPKIN_STEM) {
         @Override
         public BlockState updateShape(BlockState $$0, Direction $$1, BlockState $$2, net.minecraft.world.level.LevelAccessor $$3, BlockPos $$4, BlockPos $$5) {
            if ($$0.getValue(StemBlock.AGE) == 7) {
               Block $$6 = $$0.is(Blocks.PUMPKIN_STEM) ? Blocks.PUMPKIN : Blocks.MELON;
               if ($$2.is($$6)) {
                  return ($$0.is(Blocks.PUMPKIN_STEM) ? Blocks.ATTACHED_PUMPKIN_STEM : Blocks.ATTACHED_MELON_STEM)
                     .defaultBlockState()
                     .setValue(HorizontalDirectionalBlock.FACING, $$1);
               }
            }

            return $$0;
         }
      };

      public static final Direction[] DIRECTIONS = Direction.values();

      BlockFixers(final Block... $$0) {
         this(false, $$0);
      }

      BlockFixers(final boolean $$0, final Block... $$1) {
         for (Block $$2 : $$1) {
            UpgradeData.MAP.put($$2, this);
         }

         if ($$0) {
            UpgradeData.CHUNKY_FIXERS.add(this);
         }
      }
   }
}
