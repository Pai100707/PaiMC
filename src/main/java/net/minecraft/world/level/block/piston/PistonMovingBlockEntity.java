package net.minecraft.world.level.block.piston;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonMovingBlockEntity extends BlockEntity {
   private static final int TICKS_TO_EXTEND = 2;
   private static final double PUSH_OFFSET = 0.01;
   public static final double TICK_MOVEMENT = 0.51;
   private static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
   private static final float DEFAULT_PROGRESS = 0.0F;
   private static final boolean DEFAULT_EXTENDING = false;
   private static final boolean DEFAULT_SOURCE = false;
   private BlockState movedState = DEFAULT_BLOCK_STATE;
   private Direction direction;
   private boolean extending = false;
   private boolean isSourcePiston = false;
   private static final ThreadLocal<Direction> NOCLIP = ThreadLocal.withInitial(() -> null);
   private float progress = 0.0F;
   private float progressO = 0.0F;
   private long lastTicked;
   private int deathTicks;

   public PistonMovingBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.PISTON, $$0, $$1);
   }

   public PistonMovingBlockEntity(BlockPos $$0, BlockState $$1, BlockState $$2, Direction $$3, boolean $$4, boolean $$5) {
      this($$0, $$1);
      this.movedState = $$2;
      this.direction = $$3;
      this.extending = $$4;
      this.isSourcePiston = $$5;
   }

   @Override
   public CompoundTag getUpdateTag(Provider $$0) {
      return this.saveCustomOnly($$0);
   }

   public boolean isExtending() {
      return this.extending;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public boolean isSourcePiston() {
      return this.isSourcePiston;
   }

   public float getProgress(float $$0) {
      if ($$0 > 1.0F) {
         $$0 = 1.0F;
      }

      return Mth.lerp($$0, this.progressO, this.progress);
   }

   public float getXOff(float $$0) {
      return this.direction.getStepX() * this.getExtendedProgress(this.getProgress($$0));
   }

   public float getYOff(float $$0) {
      return this.direction.getStepY() * this.getExtendedProgress(this.getProgress($$0));
   }

   public float getZOff(float $$0) {
      return this.direction.getStepZ() * this.getExtendedProgress(this.getProgress($$0));
   }

   private float getExtendedProgress(float $$0) {
      return this.extending ? $$0 - 1.0F : 1.0F - $$0;
   }

   private BlockState getCollisionRelatedBlockState() {
      return !this.isExtending() && this.isSourcePiston() && this.movedState.getBlock() instanceof PistonBaseBlock
         ? Blocks.PISTON_HEAD
            .defaultBlockState()
            .setValue(PistonHeadBlock.SHORT, this.progress > 0.25F)
            .setValue(PistonHeadBlock.TYPE, this.movedState.is(Blocks.STICKY_PISTON) ? PistonType.STICKY : PistonType.DEFAULT)
            .setValue(PistonHeadBlock.FACING, (Direction)this.movedState.getValue(PistonBaseBlock.FACING))
         : this.movedState;
   }

   private static void moveCollidedEntities(net.minecraft.world.level.Level $$0, BlockPos $$1, float $$2, PistonMovingBlockEntity $$3) {
      Direction $$4 = $$3.getMovementDirection();
      double $$5 = $$2 - $$3.progress;
      VoxelShape $$6 = $$3.getCollisionRelatedBlockState().getCollisionShape($$0, $$1);
      if (!$$6.isEmpty()) {
         AABB $$7 = moveByPositionAndProgress($$1, $$6.bounds(), $$3);
         List<Entity> $$8 = $$0.getEntities(null, PistonMath.getMovementArea($$7, $$4, $$5).minmax($$7));
         if (!$$8.isEmpty()) {
            List<AABB> $$9 = $$6.toAabbs();
            boolean $$10 = $$3.movedState.is(Blocks.SLIME_BLOCK);
            Iterator var12 = $$8.iterator();

            while (true) {
               Entity $$11;
               while (true) {
                  if (!var12.hasNext()) {
                     return;
                  }

                  $$11 = (Entity)var12.next();
                  if ($$11.getPistonPushReaction() != PushReaction.IGNORE) {
                     if (!$$10) {
                        break;
                     }

                     if (!($$11 instanceof ServerPlayer)) {
                        Vec3 $$12 = $$11.getDeltaMovement();
                        double $$13 = $$12.x;
                        double $$14 = $$12.y;
                        double $$15 = $$12.z;
                        switch ($$4.getAxis()) {
                           case X:
                              $$13 = $$4.getStepX();
                              break;
                           case Y:
                              $$14 = $$4.getStepY();
                              break;
                           case Z:
                              $$15 = $$4.getStepZ();
                        }

                        $$11.setDeltaMovement($$13, $$14, $$15);
                        break;
                     }
                  }
               }

               double $$16 = 0.0;

               for (AABB $$17 : $$9) {
                  AABB $$18 = PistonMath.getMovementArea(moveByPositionAndProgress($$1, $$17, $$3), $$4, $$5);
                  AABB $$19 = $$11.getBoundingBox();
                  if ($$18.intersects($$19)) {
                     $$16 = Math.max($$16, getMovement($$18, $$4, $$19));
                     if ($$16 >= $$5) {
                        break;
                     }
                  }
               }

               if (!($$16 <= 0.0)) {
                  $$16 = Math.min($$16, $$5) + 0.01;
                  moveEntityByPiston($$4, $$11, $$16, $$4);
                  if (!$$3.extending && $$3.isSourcePiston) {
                     fixEntityWithinPistonBase($$1, $$11, $$4, $$5);
                  }
               }
            }
         }
      }
   }

   private static void moveEntityByPiston(Direction $$0, Entity $$1, double $$2, Direction $$3) {
      NOCLIP.set($$0);
      Vec3 $$4 = $$1.position();
      $$1.move(MoverType.PISTON, new Vec3($$2 * $$3.getStepX(), $$2 * $$3.getStepY(), $$2 * $$3.getStepZ()));
      $$1.applyEffectsFromBlocks($$4, $$1.position());
      $$1.removeLatestMovementRecording();
      NOCLIP.set(null);
   }

   private static void moveStuckEntities(net.minecraft.world.level.Level $$0, BlockPos $$1, float $$2, PistonMovingBlockEntity $$3) {
      if ($$3.isStickyForEntities()) {
         Direction $$4 = $$3.getMovementDirection();
         if ($$4.getAxis().isHorizontal()) {
            double $$5 = $$3.movedState.getCollisionShape($$0, $$1).max(Axis.Y);
            AABB $$6 = moveByPositionAndProgress($$1, new AABB(0.0, $$5, 0.0, 1.0, 1.5000010000000001, 1.0), $$3);
            double $$7 = $$2 - $$3.progress;

            for (Entity $$9 : $$0.getEntities((Entity)null, $$6, $$2x -> matchesStickyCritera($$6, $$2x, $$1))) {
               moveEntityByPiston($$4, $$9, $$7, $$4);
            }
         }
      }
   }

   private static boolean matchesStickyCritera(AABB $$0, Entity $$1, BlockPos $$2) {
      return $$1.getPistonPushReaction() == PushReaction.NORMAL
         && $$1.onGround()
         && ($$1.isSupportedBy($$2) || $$1.getX() >= $$0.minX && $$1.getX() <= $$0.maxX && $$1.getZ() >= $$0.minZ && $$1.getZ() <= $$0.maxZ);
   }

   private boolean isStickyForEntities() {
      return this.movedState.is(Blocks.HONEY_BLOCK);
   }

   public Direction getMovementDirection() {
      return this.extending ? this.direction : this.direction.getOpposite();
   }

   private static double getMovement(AABB $$0, Direction $$1, AABB $$2) {
      switch ($$1) {
         case EAST:
            return $$0.maxX - $$2.minX;
         case WEST:
            return $$2.maxX - $$0.minX;
         case UP:
         default:
            return $$0.maxY - $$2.minY;
         case DOWN:
            return $$2.maxY - $$0.minY;
         case SOUTH:
            return $$0.maxZ - $$2.minZ;
         case NORTH:
            return $$2.maxZ - $$0.minZ;
      }
   }

   private static AABB moveByPositionAndProgress(BlockPos $$0, AABB $$1, PistonMovingBlockEntity $$2) {
      double $$3 = $$2.getExtendedProgress($$2.progress);
      return $$1.move($$0.getX() + $$3 * $$2.direction.getStepX(), $$0.getY() + $$3 * $$2.direction.getStepY(), $$0.getZ() + $$3 * $$2.direction.getStepZ());
   }

   private static void fixEntityWithinPistonBase(BlockPos $$0, Entity $$1, Direction $$2, double $$3) {
      AABB $$4 = $$1.getBoundingBox();
      AABB $$5 = Shapes.block().bounds().move($$0);
      if ($$4.intersects($$5)) {
         Direction $$6 = $$2.getOpposite();
         double $$7 = getMovement($$5, $$6, $$4) + 0.01;
         double $$8 = getMovement($$5, $$6, $$4.intersect($$5)) + 0.01;
         if (Math.abs($$7 - $$8) < 0.01) {
            $$7 = Math.min($$7, $$3) + 0.01;
            moveEntityByPiston($$2, $$1, $$7, $$6);
         }
      }
   }

   public BlockState getMovedState() {
      return this.movedState;
   }

   public void finalTick() {
      if (this.level != null && (this.progressO < 1.0F || this.level.isClientSide())) {
         this.progress = 1.0F;
         this.progressO = this.progress;
         this.level.removeBlockEntity(this.worldPosition);
         this.setRemoved();
         if (this.level.getBlockState(this.worldPosition).is(Blocks.MOVING_PISTON)) {
            BlockState $$0;
            if (this.isSourcePiston) {
               $$0 = Blocks.AIR.defaultBlockState();
            } else {
               $$0 = Block.updateFromNeighbourShapes(this.movedState, this.level, this.worldPosition);
            }

            this.level.setBlock(this.worldPosition, $$0, 3);
            this.level
               .neighborChanged(this.worldPosition, $$0.getBlock(), ExperimentalRedstoneUtils.initialOrientation(this.level, this.getPushDirection(), null));
         }
      }
   }

   @Override
   public void preRemoveSideEffects(BlockPos $$0, BlockState $$1) {
      this.finalTick();
   }

   public Direction getPushDirection() {
      return this.extending ? this.direction : this.direction.getOpposite();
   }

   public static void tick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, PistonMovingBlockEntity $$3) {
      $$3.lastTicked = $$0.getGameTime();
      $$3.progressO = $$3.progress;
      if ($$3.progressO >= 1.0F) {
         if ($$0.isClientSide() && $$3.deathTicks < 5) {
            $$3.deathTicks++;
         } else {
            $$0.removeBlockEntity($$1);
            $$3.setRemoved();
            if ($$0.getBlockState($$1).is(Blocks.MOVING_PISTON)) {
               BlockState $$4 = Block.updateFromNeighbourShapes($$3.movedState, $$0, $$1);
               if ($$4.isAir()) {
                  $$0.setBlock($$1, $$3.movedState, 340);
                  Block.updateOrDestroy($$3.movedState, $$4, $$0, $$1, 3);
               } else {
                  if ($$4.hasProperty(BlockStateProperties.WATERLOGGED) && $$4.getValue(BlockStateProperties.WATERLOGGED)) {
                     $$4 = $$4.setValue(BlockStateProperties.WATERLOGGED, false);
                  }

                  $$0.setBlock($$1, $$4, 67);
                  $$0.neighborChanged($$1, $$4.getBlock(), ExperimentalRedstoneUtils.initialOrientation($$0, $$3.getPushDirection(), null));
               }
            }
         }
      } else {
         float $$5 = $$3.progress + 0.5F;
         moveCollidedEntities($$0, $$1, $$5, $$3);
         moveStuckEntities($$0, $$1, $$5, $$3);
         $$3.progress = $$5;
         if ($$3.progress >= 1.0F) {
            $$3.progress = 1.0F;
         }
      }
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.movedState = $$0.<BlockState>read("blockState", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE);
      this.direction = $$0.<Direction>read("facing", Direction.LEGACY_ID_CODEC).orElse(Direction.DOWN);
      this.progress = $$0.getFloatOr("progress", 0.0F);
      this.progressO = this.progress;
      this.extending = $$0.getBooleanOr("extending", false);
      this.isSourcePiston = $$0.getBooleanOr("source", false);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      super.saveAdditional($$0);
      $$0.store("blockState", BlockState.CODEC, this.movedState);
      $$0.store("facing", Direction.LEGACY_ID_CODEC, this.direction);
      $$0.putFloat("progress", this.progressO);
      $$0.putBoolean("extending", this.extending);
      $$0.putBoolean("source", this.isSourcePiston);
   }

   public VoxelShape getCollisionShape(net.minecraft.world.level.BlockGetter $$0, BlockPos $$1) {
      VoxelShape $$2;
      if (!this.extending && this.isSourcePiston && this.movedState.getBlock() instanceof PistonBaseBlock) {
         $$2 = this.movedState.setValue(PistonBaseBlock.EXTENDED, true).getCollisionShape($$0, $$1);
      } else {
         $$2 = Shapes.empty();
      }

      Direction $$4 = NOCLIP.get();
      if (this.progress < 1.0 && $$4 == this.getMovementDirection()) {
         return $$2;
      } else {
         BlockState $$5;
         if (this.isSourcePiston()) {
            $$5 = Blocks.PISTON_HEAD
               .defaultBlockState()
               .setValue(PistonHeadBlock.FACING, this.direction)
               .setValue(PistonHeadBlock.SHORT, this.extending != 1.0F - this.progress < 0.25F);
         } else {
            $$5 = this.movedState;
         }

         float $$7 = this.getExtendedProgress(this.progress);
         double $$8 = this.direction.getStepX() * $$7;
         double $$9 = this.direction.getStepY() * $$7;
         double $$10 = this.direction.getStepZ() * $$7;
         return Shapes.or($$2, $$5.getCollisionShape($$0, $$1).move($$8, $$9, $$10));
      }
   }

   public long getLastTicked() {
      return this.lastTicked;
   }

   @Override
   public void setLevel(net.minecraft.world.level.Level $$0) {
      super.setLevel($$0);
      if ($$0.holderLookup(Registries.BLOCK).get(this.movedState.getBlock().builtInRegistryHolder().key()).isEmpty()) {
         this.movedState = Blocks.AIR.defaultBlockState();
      }
   }
}
