package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SculkSpreader {
   public static final int MAX_GROWTH_RATE_RADIUS = 24;
   public static final int MAX_CHARGE = 1000;
   public static final float MAX_DECAY_FACTOR = 0.5F;
   private static final int MAX_CURSORS = 32;
   public static final int SHRIEKER_PLACEMENT_RATE = 11;
   public static final int MAX_CURSOR_DISTANCE = 1024;
   final boolean isWorldGeneration;
   private final TagKey<Block> replaceableBlocks;
   private final int growthSpawnCost;
   private final int noGrowthRadius;
   private final int chargeDecayRate;
   private final int additionalDecayRate;
   private List<SculkSpreader.ChargeCursor> cursors = new ArrayList<>();

   public SculkSpreader(boolean $$0, TagKey<Block> $$1, int $$2, int $$3, int $$4, int $$5) {
      this.isWorldGeneration = $$0;
      this.replaceableBlocks = $$1;
      this.growthSpawnCost = $$2;
      this.noGrowthRadius = $$3;
      this.chargeDecayRate = $$4;
      this.additionalDecayRate = $$5;
   }

   public static SculkSpreader createLevelSpreader() {
      return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
   }

   public static SculkSpreader createWorldGenSpreader() {
      return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
   }

   public TagKey<Block> replaceableBlocks() {
      return this.replaceableBlocks;
   }

   public int growthSpawnCost() {
      return this.growthSpawnCost;
   }

   public int noGrowthRadius() {
      return this.noGrowthRadius;
   }

   public int chargeDecayRate() {
      return this.chargeDecayRate;
   }

   public int additionalDecayRate() {
      return this.additionalDecayRate;
   }

   public boolean isWorldGeneration() {
      return this.isWorldGeneration;
   }

   @VisibleForTesting
   public List<SculkSpreader.ChargeCursor> getCursors() {
      return this.cursors;
   }

   public void clear() {
      this.cursors.clear();
   }

   public void load(ValueInput $$0) {
      this.cursors.clear();
      $$0.<List>read("cursors", SculkSpreader.ChargeCursor.CODEC.sizeLimitedListOf(32)).orElse(List.of()).forEach(this::addCursor);
   }

   public void save(ValueOutput $$0) {
      $$0.store("cursors", SculkSpreader.ChargeCursor.CODEC.listOf(), this.cursors);
      if (SharedConstants.DEBUG_SCULK_CATALYST) {
         int $$1 = this.getCursors().stream().map(SculkSpreader.ChargeCursor::getCharge).reduce(0, Integer::sum);
         int $$2 = this.getCursors().stream().map($$0x -> 1).reduce(0, Integer::sum);
         int $$3 = this.getCursors().stream().map(SculkSpreader.ChargeCursor::getCharge).reduce(0, Math::max);
         $$0.putInt("stats.total", $$1);
         $$0.putInt("stats.count", $$2);
         $$0.putInt("stats.max", $$3);
         $$0.putInt("stats.avg", $$1 / ($$2 + 1));
      }
   }

   public void addCursors(BlockPos $$0, int $$1) {
      while ($$1 > 0) {
         int $$2 = Math.min($$1, 1000);
         this.addCursor(new SculkSpreader.ChargeCursor($$0, $$2));
         $$1 -= $$2;
      }
   }

   private void addCursor(SculkSpreader.ChargeCursor $$0) {
      if (this.cursors.size() < 32) {
         this.cursors.add($$0);
      }
   }

   public void updateCursors(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, boolean $$3) {
      if (!this.cursors.isEmpty()) {
         List<SculkSpreader.ChargeCursor> $$4 = new ArrayList<>();
         Map<BlockPos, SculkSpreader.ChargeCursor> $$5 = new HashMap<>();
         Object2IntMap<BlockPos> $$6 = new Object2IntOpenHashMap();

         for (SculkSpreader.ChargeCursor $$7 : this.cursors) {
            if (!$$7.isPosUnreasonable($$1)) {
               $$7.update($$0, $$1, $$2, this, $$3);
               if ($$7.charge <= 0) {
                  $$0.levelEvent(3006, $$7.getPos(), 0);
               } else {
                  BlockPos $$8 = $$7.getPos();
                  $$6.computeInt($$8, ($$1x, $$2x) -> ($$2x == null ? 0 : $$2x) + $$7.charge);
                  SculkSpreader.ChargeCursor $$9 = $$5.get($$8);
                  if ($$9 == null) {
                     $$5.put($$8, $$7);
                     $$4.add($$7);
                  } else if (!this.isWorldGeneration() && $$7.charge + $$9.charge <= 1000) {
                     $$9.mergeWith($$7);
                  } else {
                     $$4.add($$7);
                     if ($$7.charge < $$9.charge) {
                        $$5.put($$8, $$7);
                     }
                  }
               }
            }
         }

         ObjectIterator var16 = $$6.object2IntEntrySet().iterator();

         while (var16.hasNext()) {
            Entry<BlockPos> $$10 = (Entry<BlockPos>)var16.next();
            BlockPos $$11 = (BlockPos)$$10.getKey();
            int $$12 = $$10.getIntValue();
            SculkSpreader.ChargeCursor $$13 = $$5.get($$11);
            Collection<Direction> $$14 = $$13 == null ? null : $$13.getFacingData();
            if ($$12 > 0 && $$14 != null) {
               int $$15 = (int)(Math.log1p($$12) / 2.3F) + 1;
               int $$16 = ($$15 << 6) + MultifaceBlock.pack($$14);
               $$0.levelEvent(3006, $$11, $$16);
            }
         }

         this.cursors = $$4;
      }
   }

   public static class ChargeCursor {
      private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = (ObjectArrayList<Vec3i>)Util.make(
         new ObjectArrayList(18),
         $$0 -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
            .filter($$0x -> ($$0x.getX() == 0 || $$0x.getY() == 0 || $$0x.getZ() == 0) && !$$0x.equals(BlockPos.ZERO))
            .map(BlockPos::immutable)
            .forEach($$0::add)
      );
      public static final int MAX_CURSOR_DECAY_DELAY = 1;
      private BlockPos pos;
      int charge;
      private int updateDelay;
      private int decayDelay;
      
      private Set<Direction> facings;
      private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC
         .listOf()
         .xmap($$0 -> Sets.newEnumSet($$0, Direction.class), Lists::newArrayList);
      public static final Codec<SculkSpreader.ChargeCursor> CODEC = RecordCodecBuilder.create(
         $$0 -> $$0.group(
               BlockPos.CODEC.fieldOf("pos").forGetter(SculkSpreader.ChargeCursor::getPos),
               Codec.intRange(0, 1000).fieldOf("charge").orElse(0).forGetter(SculkSpreader.ChargeCursor::getCharge),
               Codec.intRange(0, 1).fieldOf("decay_delay").orElse(1).forGetter(SculkSpreader.ChargeCursor::getDecayDelay),
               Codec.intRange(0, Integer.MAX_VALUE).fieldOf("update_delay").orElse(0).forGetter($$0x -> $$0x.updateDelay),
               DIRECTION_SET.lenientOptionalFieldOf("facings").forGetter($$0x -> Optional.ofNullable($$0x.getFacingData()))
            )
            .apply($$0, SculkSpreader.ChargeCursor::new)
      );

      private ChargeCursor(BlockPos $$0, int $$1, int $$2, int $$3, Optional<Set<Direction>> $$4) {
         this.pos = $$0;
         this.charge = $$1;
         this.decayDelay = $$2;
         this.updateDelay = $$3;
         this.facings = $$4.orElse(null);
      }

      public ChargeCursor(BlockPos $$0, int $$1) {
         this($$0, $$1, 1, 0, Optional.empty());
      }

      public BlockPos getPos() {
         return this.pos;
      }

      boolean isPosUnreasonable(BlockPos $$0) {
         return this.pos.distChessboard($$0) > 1024;
      }

      public int getCharge() {
         return this.charge;
      }

      public int getDecayDelay() {
         return this.decayDelay;
      }

      
      public Set<Direction> getFacingData() {
         return this.facings;
      }

      private boolean shouldUpdate(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, boolean $$2) {
         if (this.charge <= 0) {
            return false;
         } else if ($$2) {
            return true;
         } else {
            return $$0 instanceof ServerLevel $$3 ? $$3.shouldTickBlocksAt($$1) : false;
         }
      }

      public void update(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2, SculkSpreader $$3, boolean $$4) {
         if (this.shouldUpdate($$0, $$1, $$3.isWorldGeneration)) {
            if (this.updateDelay > 0) {
               this.updateDelay--;
            } else {
               BlockState $$5 = $$0.getBlockState(this.pos);
               SculkBehaviour $$6 = getBlockBehaviour($$5);
               if ($$4 && $$6.attemptSpreadVein($$0, this.pos, $$5, this.facings, $$3.isWorldGeneration())) {
                  if ($$6.canChangeBlockStateOnSpread()) {
                     $$5 = $$0.getBlockState(this.pos);
                     $$6 = getBlockBehaviour($$5);
                  }

                  $$0.playSound(null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0F, 1.0F);
               }

               this.charge = $$6.attemptUseCharge(this, $$0, $$1, $$2, $$3, $$4);
               if (this.charge <= 0) {
                  $$6.onDischarged($$0, $$5, this.pos, $$2);
               } else {
                  BlockPos $$7 = getValidMovementPos($$0, this.pos, $$2);
                  if ($$7 != null) {
                     $$6.onDischarged($$0, $$5, this.pos, $$2);
                     this.pos = $$7.immutable();
                     if ($$3.isWorldGeneration() && !this.pos.closerThan(new Vec3i($$1.getX(), this.pos.getY(), $$1.getZ()), 15.0)) {
                        this.charge = 0;
                        return;
                     }

                     $$5 = $$0.getBlockState($$7);
                  }

                  if ($$5.getBlock() instanceof SculkBehaviour) {
                     this.facings = MultifaceBlock.availableFaces($$5);
                  }

                  this.decayDelay = $$6.updateDecayDelay(this.decayDelay);
                  this.updateDelay = $$6.getSculkSpreadDelay();
               }
            }
         }
      }

      void mergeWith(SculkSpreader.ChargeCursor $$0) {
         this.charge = this.charge + $$0.charge;
         $$0.charge = 0;
         this.updateDelay = Math.min(this.updateDelay, $$0.updateDelay);
      }

      private static SculkBehaviour getBlockBehaviour(BlockState $$0) {
         return $$0.getBlock() instanceof SculkBehaviour $$1 ? $$1 : SculkBehaviour.DEFAULT;
      }

      private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource $$0) {
         return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, $$0);
      }

      
      private static BlockPos getValidMovementPos(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, RandomSource $$2) {
         MutableBlockPos $$3 = $$1.mutable();
         MutableBlockPos $$4 = $$1.mutable();

         for (Vec3i $$5 : getRandomizedNonCornerNeighbourOffsets($$2)) {
            $$4.setWithOffset($$1, $$5);
            BlockState $$6 = $$0.getBlockState($$4);
            if ($$6.getBlock() instanceof SculkBehaviour && isMovementUnobstructed($$0, $$1, $$4)) {
               $$3.set($$4);
               if (SculkVeinBlock.hasSubstrateAccess($$0, $$6, $$4)) {
                  break;
               }
            }
         }

         return $$3.equals($$1) ? null : $$3;
      }

      private static boolean isMovementUnobstructed(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockPos $$2) {
         if ($$1.distManhattan($$2) == 1) {
            return true;
         } else {
            BlockPos $$3 = $$2.subtract($$1);
            Direction $$4 = Direction.fromAxisAndDirection(Axis.X, $$3.getX() < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
            Direction $$5 = Direction.fromAxisAndDirection(Axis.Y, $$3.getY() < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
            Direction $$6 = Direction.fromAxisAndDirection(Axis.Z, $$3.getZ() < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
            if ($$3.getX() == 0) {
               return isUnobstructed($$0, $$1, $$5) || isUnobstructed($$0, $$1, $$6);
            } else {
               return $$3.getY() == 0
                  ? isUnobstructed($$0, $$1, $$4) || isUnobstructed($$0, $$1, $$6)
                  : isUnobstructed($$0, $$1, $$4) || isUnobstructed($$0, $$1, $$5);
            }
         }
      }

      private static boolean isUnobstructed(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, Direction $$2) {
         BlockPos $$3 = $$1.relative($$2);
         return !$$0.getBlockState($$3).isFaceSturdy($$0, $$3, $$2.getOpposite());
      }
   }
}
