package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid extends Fluid {
   public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
   private static final int CACHE_SIZE = 200;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey> $$0 = new Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey>(200) {
         protected void rehash(int $$0) {
         }
      };
      $$0.defaultReturnValue((byte)127);
      return $$0;
   });
   private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

   @Override
   protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> $$0) {
      $$0.add(FALLING);
   }

   @Override
   public Vec3 getFlow(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, FluidState $$2) {
      double $$3 = 0.0;
      double $$4 = 0.0;
      MutableBlockPos $$5 = new MutableBlockPos();

      for (Direction $$6 : Plane.HORIZONTAL) {
         $$5.setWithOffset($$1, $$6);
         FluidState $$7 = $$0.getFluidState($$5);
         if (this.affectsFlow($$7)) {
            float $$8 = $$7.getOwnHeight();
            float $$9 = 0.0F;
            if ($$8 == 0.0F) {
               if (!$$0.getBlockState($$5).blocksMotion()) {
                  BlockPos $$10 = $$5.below();
                  FluidState $$11 = $$0.getFluidState($$10);
                  if (this.affectsFlow($$11)) {
                     $$8 = $$11.getOwnHeight();
                     if ($$8 > 0.0F) {
                        $$9 = $$2.getOwnHeight() - ($$8 - 0.8888889F);
                     }
                  }
               }
            } else if ($$8 > 0.0F) {
               $$9 = $$2.getOwnHeight() - $$8;
            }

            if ($$9 != 0.0F) {
               $$3 += $$6.getStepX() * $$9;
               $$4 += $$6.getStepZ() * $$9;
            }
         }
      }

      Vec3 $$12 = new Vec3($$3, 0.0, $$4);
      if ($$2.getValue(FALLING)) {
         for (Direction $$13 : Plane.HORIZONTAL) {
            $$5.setWithOffset($$1, $$13);
            if (this.isSolidFace($$0, $$5, $$13) || this.isSolidFace($$0, $$5.above(), $$13)) {
               $$12 = $$12.normalize().add(0.0, -6.0, 0.0);
               break;
            }
         }
      }

      return $$12.normalize();
   }

   private boolean affectsFlow(FluidState $$0) {
      return $$0.isEmpty() || $$0.getType().isSame(this);
   }

   protected boolean isSolidFace(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, Direction $$2) {
      BlockState $$3 = $$0.getBlockState($$1);
      FluidState $$4 = $$0.getFluidState($$1);
      if ($$4.getType().isSame(this)) {
         return false;
      } else if ($$2 == Direction.UP) {
         return true;
      } else {
         return $$3.getBlock() instanceof IceBlock ? false : $$3.isFaceSturdy($$0, $$1, $$2);
      }
   }

   protected void spread(ServerLevel $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      if (!$$3.isEmpty()) {
         BlockPos $$4 = $$1.below();
         BlockState $$5 = $$0.getBlockState($$4);
         FluidState $$6 = $$5.getFluidState();
         if (this.canMaybePassThrough($$0, $$1, $$2, Direction.DOWN, $$4, $$5, $$6)) {
            FluidState $$7 = this.getNewLiquid($$0, $$4, $$5);
            Fluid $$8 = $$7.getType();
            if ($$6.canBeReplacedWith($$0, $$4, $$8, Direction.DOWN) && canHoldSpecificFluid($$0, $$4, $$5, $$8)) {
               this.spreadTo($$0, $$4, $$5, Direction.DOWN, $$7);
               if (this.sourceNeighborCount($$0, $$1) >= 3) {
                  this.spreadToSides($$0, $$1, $$3, $$2);
               }

               return;
            }
         }

         if ($$3.isSource() || !this.isWaterHole($$0, $$1, $$2, $$4, $$5)) {
            this.spreadToSides($$0, $$1, $$3, $$2);
         }
      }
   }

   private void spreadToSides(ServerLevel $$0, BlockPos $$1, FluidState $$2, BlockState $$3) {
      int $$4 = $$2.getAmount() - this.getDropOff($$0);
      if ($$2.getValue(FALLING)) {
         $$4 = 7;
      }

      if ($$4 > 0) {
         Map<Direction, FluidState> $$5 = this.getSpread($$0, $$1, $$3);

         for (Entry<Direction, FluidState> $$6 : $$5.entrySet()) {
            Direction $$7 = $$6.getKey();
            FluidState $$8 = $$6.getValue();
            BlockPos $$9 = $$1.relative($$7);
            this.spreadTo($$0, $$9, $$0.getBlockState($$9), $$7, $$8);
         }
      }
   }

   protected FluidState getNewLiquid(ServerLevel $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = 0;
      int $$4 = 0;
      MutableBlockPos $$5 = new MutableBlockPos();

      for (Direction $$6 : Plane.HORIZONTAL) {
         BlockPos $$7 = $$5.setWithOffset($$1, $$6);
         BlockState $$8 = $$0.getBlockState($$7);
         FluidState $$9 = $$8.getFluidState();
         if ($$9.getType().isSame(this) && canPassThroughWall($$6, $$0, $$1, $$2, $$7, $$8)) {
            if ($$9.isSource()) {
               $$4++;
            }

            $$3 = Math.max($$3, $$9.getAmount());
         }
      }

      if ($$4 >= 2 && this.canConvertToSource($$0)) {
         BlockState $$10 = $$0.getBlockState($$5.setWithOffset($$1, Direction.DOWN));
         FluidState $$11 = $$10.getFluidState();
         if ($$10.isSolid() || this.isSourceBlockOfThisType($$11)) {
            return this.getSource(false);
         }
      }

      BlockPos $$12 = $$5.setWithOffset($$1, Direction.UP);
      BlockState $$13 = $$0.getBlockState($$12);
      FluidState $$14 = $$13.getFluidState();
      if (!$$14.isEmpty() && $$14.getType().isSame(this) && canPassThroughWall(Direction.UP, $$0, $$1, $$2, $$12, $$13)) {
         return this.getFlowing(8, true);
      } else {
         int $$15 = $$3 - this.getDropOff($$0);
         return $$15 <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing($$15, false);
      }
   }

   private static boolean canPassThroughWall(
      Direction $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2, BlockState $$3, BlockPos $$4, BlockState $$5
   ) {
      if (!SharedConstants.DEBUG_DISABLE_LIQUID_SPREADING && (!SharedConstants.DEBUG_ONLY_GENERATE_HALF_THE_WORLD || $$4.getZ() >= 0)) {
         VoxelShape $$6 = $$5.getCollisionShape($$1, $$4);
         if ($$6 == Shapes.block()) {
            return false;
         } else {
            VoxelShape $$7 = $$3.getCollisionShape($$1, $$2);
            if ($$7 == Shapes.block()) {
               return false;
            } else if ($$7 == Shapes.empty() && $$6 == Shapes.empty()) {
               return true;
            } else {
               Object2ByteLinkedOpenHashMap<FlowingFluid.BlockStatePairKey> $$9;
               if (!$$3.getBlock().hasDynamicShape() && !$$5.getBlock().hasDynamicShape()) {
                  $$9 = OCCLUSION_CACHE.get();
               } else {
                  $$9 = null;
               }

               FlowingFluid.BlockStatePairKey $$10;
               if ($$9 != null) {
                  $$10 = new FlowingFluid.BlockStatePairKey($$3, $$5, $$0);
                  byte $$11 = $$9.getAndMoveToFirst($$10);
                  if ($$11 != 127) {
                     return $$11 != 0;
                  }
               } else {
                  $$10 = null;
               }

               boolean $$13 = !Shapes.mergedFaceOccludes($$7, $$6, $$0);
               if ($$9 != null) {
                  if ($$9.size() == 200) {
                     $$9.removeLastByte();
                  }

                  $$9.putAndMoveToFirst($$10, (byte)($$13 ? 1 : 0));
               }

               return $$13;
            }
         }
      } else {
         return false;
      }
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int $$0, boolean $$1) {
      return this.getFlowing().defaultFluidState().setValue(LEVEL, $$0).setValue(FALLING, $$1);
   }

   public abstract Fluid getSource();

   public FluidState getSource(boolean $$0) {
      return this.getSource().defaultFluidState().setValue(FALLING, $$0);
   }

   protected abstract boolean canConvertToSource(ServerLevel var1);

   protected void spreadTo(net.minecraft.world.level.LevelAccessor $$0, BlockPos $$1, BlockState $$2, Direction $$3, FluidState $$4) {
      if ($$2.getBlock() instanceof LiquidBlockContainer $$5) {
         $$5.placeLiquid($$0, $$1, $$2, $$4);
      } else {
         if (!$$2.isAir()) {
            this.beforeDestroyingBlock($$0, $$1, $$2);
         }

         $$0.setBlock($$1, $$4.createLegacyBlock(), 3);
      }
   }

   protected abstract void beforeDestroyingBlock(net.minecraft.world.level.LevelAccessor var1, BlockPos var2, BlockState var3);

   protected int getSlopeDistance(
      net.minecraft.world.level.LevelReader $$0, BlockPos $$1, int $$2, Direction $$3, BlockState $$4, FlowingFluid.SpreadContext $$5
   ) {
      int $$6 = 1000;

      for (Direction $$7 : Plane.HORIZONTAL) {
         if ($$7 != $$3) {
            BlockPos $$8 = $$1.relative($$7);
            BlockState $$9 = $$5.getBlockState($$8);
            FluidState $$10 = $$9.getFluidState();
            if (this.canPassThrough($$0, this.getFlowing(), $$1, $$4, $$7, $$8, $$9, $$10)) {
               if ($$5.isHole($$8)) {
                  return $$2;
               }

               if ($$2 < this.getSlopeFindDistance($$0)) {
                  int $$11 = this.getSlopeDistance($$0, $$8, $$2 + 1, $$7.getOpposite(), $$9, $$5);
                  if ($$11 < $$6) {
                     $$6 = $$11;
                  }
               }
            }
         }
      }

      return $$6;
   }

   boolean isWaterHole(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2, BlockPos $$3, BlockState $$4) {
      if (!canPassThroughWall(Direction.DOWN, $$0, $$1, $$2, $$3, $$4)) {
         return false;
      } else {
         return $$4.getFluidState().getType().isSame(this) ? true : canHoldFluid($$0, $$3, $$4, this.getFlowing());
      }
   }

   private boolean canPassThrough(
      net.minecraft.world.level.BlockGetter $$0, Fluid $$1, BlockPos $$2, BlockState $$3, Direction $$4, BlockPos $$5, BlockState $$6, FluidState $$7
   ) {
      return this.canMaybePassThrough($$0, $$2, $$3, $$4, $$5, $$6, $$7) && canHoldSpecificFluid($$0, $$5, $$6, $$1);
   }

   private boolean canMaybePassThrough(
      net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2, Direction $$3, BlockPos $$4, BlockState $$5, FluidState $$6
   ) {
      return !this.isSourceBlockOfThisType($$6) && canHoldAnyFluid($$5) && canPassThroughWall($$3, $$0, $$1, $$2, $$4, $$5);
   }

   private boolean isSourceBlockOfThisType(FluidState $$0) {
      return $$0.getType().isSame(this) && $$0.isSource();
   }

   protected abstract int getSlopeFindDistance(net.minecraft.world.level.LevelReader var1);

   private int sourceNeighborCount(net.minecraft.world.level.LevelReader $$0, BlockPos $$1) {
      int $$2 = 0;

      for (Direction $$3 : Plane.HORIZONTAL) {
         BlockPos $$4 = $$1.relative($$3);
         FluidState $$5 = $$0.getFluidState($$4);
         if (this.isSourceBlockOfThisType($$5)) {
            $$2++;
         }
      }

      return $$2;
   }

   protected Map<Direction, FluidState> getSpread(ServerLevel $$0, BlockPos $$1, BlockState $$2) {
      int $$3 = 1000;
      Map<Direction, FluidState> $$4 = Maps.newEnumMap(Direction.class);
      FlowingFluid.SpreadContext $$5 = null;

      for (Direction $$6 : Plane.HORIZONTAL) {
         BlockPos $$7 = $$1.relative($$6);
         BlockState $$8 = $$0.getBlockState($$7);
         FluidState $$9 = $$8.getFluidState();
         if (this.canMaybePassThrough($$0, $$1, $$2, $$6, $$7, $$8, $$9)) {
            FluidState $$10 = this.getNewLiquid($$0, $$7, $$8);
            if (canHoldSpecificFluid($$0, $$7, $$8, $$10.getType())) {
               if ($$5 == null) {
                  $$5 = new FlowingFluid.SpreadContext($$0, $$1);
               }

               int $$11;
               if ($$5.isHole($$7)) {
                  $$11 = 0;
               } else {
                  $$11 = this.getSlopeDistance($$0, $$7, 1, $$6.getOpposite(), $$8, $$5);
               }

               if ($$11 < $$3) {
                  $$4.clear();
               }

               if ($$11 <= $$3) {
                  if ($$9.canBeReplacedWith($$0, $$7, $$10.getType(), $$6)) {
                     $$4.put($$6, $$10);
                  }

                  $$3 = $$11;
               }
            }
         }
      }

      return $$4;
   }

   private static boolean canHoldAnyFluid(BlockState $$0) {
      Block $$1 = $$0.getBlock();
      if ($$1 instanceof LiquidBlockContainer) {
         return true;
      } else {
         return $$0.blocksMotion()
            ? false
            : !($$1 instanceof DoorBlock)
               && !$$0.is(BlockTags.SIGNS)
               && !$$0.is(Blocks.LADDER)
               && !$$0.is(Blocks.SUGAR_CANE)
               && !$$0.is(Blocks.BUBBLE_COLUMN)
               && !$$0.is(Blocks.NETHER_PORTAL)
               && !$$0.is(Blocks.END_PORTAL)
               && !$$0.is(Blocks.END_GATEWAY)
               && !$$0.is(Blocks.STRUCTURE_VOID);
      }
   }

   private static boolean canHoldFluid(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2, Fluid $$3) {
      return canHoldAnyFluid($$2) && canHoldSpecificFluid($$0, $$1, $$2, $$3);
   }

   private static boolean canHoldSpecificFluid(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1, BlockState $$2, Fluid $$3) {
      return $$2.getBlock() instanceof LiquidBlockContainer $$5 ? $$5.canPlaceLiquid(null, $$0, $$1, $$2, $$3) : true;
   }

   protected abstract int getDropOff(net.minecraft.world.level.LevelReader var1);

   protected int getSpreadDelay(net.minecraft.world.level.Level $$0, BlockPos $$1, FluidState $$2, FluidState $$3) {
      return this.getTickDelay($$0);
   }

   @Override
   public void tick(ServerLevel $$0, BlockPos $$1, BlockState $$2, FluidState $$3) {
      if (!$$3.isSource()) {
         FluidState $$4 = this.getNewLiquid($$0, $$1, $$0.getBlockState($$1));
         int $$5 = this.getSpreadDelay($$0, $$1, $$3, $$4);
         if ($$4.isEmpty()) {
            $$3 = $$4;
            $$2 = Blocks.AIR.defaultBlockState();
            $$0.setBlock($$1, $$2, 3);
         } else if ($$4 != $$3) {
            $$3 = $$4;
            $$2 = $$4.createLegacyBlock();
            $$0.setBlock($$1, $$2, 3);
            $$0.scheduleTick($$1, $$4.getType(), $$5);
         }
      }

      this.spread($$0, $$1, $$2, $$3);
   }

   protected static int getLegacyLevel(FluidState $$0) {
      return $$0.isSource() ? 0 : 8 - Math.min($$0.getAmount(), 8) + ($$0.getValue(FALLING) ? 8 : 0);
   }

   private static boolean hasSameAbove(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.getType().isSame($$1.getFluidState($$2.above()).getType());
   }

   @Override
   public float getHeight(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return hasSameAbove($$0, $$1, $$2) ? 1.0F : $$0.getOwnHeight();
   }

   @Override
   public float getOwnHeight(FluidState $$0) {
      return $$0.getAmount() / 9.0F;
   }

   @Override
   public abstract int getAmount(FluidState var1);

   @Override
   public VoxelShape getShape(FluidState $$0, net.minecraft.world.level.BlockGetter $$1, BlockPos $$2) {
      return $$0.getAmount() == 9 && hasSameAbove($$0, $$1, $$2)
         ? Shapes.block()
         : this.shapes.computeIfAbsent($$0, $$2x -> Shapes.box(0.0, 0.0, 0.0, 1.0, $$2x.getHeight($$1, $$2), 1.0));
   }

   record BlockStatePairKey(BlockState first, BlockState second, Direction direction) {
      @Override
      public boolean equals(Object $$0) {
         return $$0 instanceof FlowingFluid.BlockStatePairKey $$1 && this.first == $$1.first && this.second == $$1.second && this.direction == $$1.direction;
      }

      @Override
      public int hashCode() {
         int $$0 = System.identityHashCode(this.first);
         $$0 = 31 * $$0 + System.identityHashCode(this.second);
         return 31 * $$0 + this.direction.hashCode();
      }
   }

   protected class SpreadContext {
      private final net.minecraft.world.level.BlockGetter level;
      private final BlockPos origin;
      private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap();
      private final Short2BooleanMap holeCache = new Short2BooleanOpenHashMap();

      SpreadContext(final net.minecraft.world.level.BlockGetter $$1, final BlockPos $$2) {
         this.level = $$1;
         this.origin = $$2;
      }

      public BlockState getBlockState(BlockPos $$0) {
         return this.getBlockState($$0, this.getCacheKey($$0));
      }

      private BlockState getBlockState(BlockPos $$0, short $$1) {
         return (BlockState)this.stateCache.computeIfAbsent($$1, $$1x -> this.level.getBlockState($$0));
      }

      public boolean isHole(BlockPos $$0) {
         return this.holeCache.computeIfAbsent(this.getCacheKey($$0), $$1 -> {
            BlockState $$2 = this.getBlockState($$0, $$1);
            BlockPos $$3 = $$0.below();
            BlockState $$4 = this.level.getBlockState($$3);
            return FlowingFluid.this.isWaterHole(this.level, $$0, $$2, $$3, $$4);
         });
      }

      private short getCacheKey(BlockPos $$0) {
         int $$1 = $$0.getX() - this.origin.getX();
         int $$2 = $$0.getZ() - this.origin.getZ();
         return (short)(($$1 + 128 & 0xFF) << 8 | $$2 + 128 & 0xFF);
      }
   }
}
