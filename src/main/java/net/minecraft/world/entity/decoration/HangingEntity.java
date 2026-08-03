package net.minecraft.world.entity.decoration;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.Validate;

public abstract class HangingEntity extends BlockAttachedEntity {
   private static final EntityDataAccessor<Direction> DATA_DIRECTION = SynchedEntityData.defineId(HangingEntity.class, EntityDataSerializers.DIRECTION);
   private static final Direction DEFAULT_DIRECTION = Direction.SOUTH;

   protected HangingEntity(net.minecraft.world.entity.EntityType<? extends HangingEntity> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected HangingEntity(net.minecraft.world.entity.EntityType<? extends HangingEntity> $$0, Level $$1, BlockPos $$2) {
      this($$0, $$1);
      this.pos = $$2;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_DIRECTION, DEFAULT_DIRECTION);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      super.onSyncedDataUpdated($$0);
      if ($$0.equals(DATA_DIRECTION)) {
         this.setDirection(this.getDirection());
      }
   }

   @Override
   public Direction getDirection() {
      return (Direction)this.entityData.get(DATA_DIRECTION);
   }

   protected void setDirectionRaw(Direction $$0) {
      this.entityData.set(DATA_DIRECTION, $$0);
   }

   protected void setDirection(Direction $$0) {
      Objects.requireNonNull($$0);
      Validate.isTrue($$0.getAxis().isHorizontal());
      this.setDirectionRaw($$0);
      this.setYRot($$0.get2DDataValue() * 90);
      this.yRotO = this.getYRot();
      this.recalculateBoundingBox();
   }

   @Override
   protected void recalculateBoundingBox() {
      if (this.getDirection() != null) {
         AABB $$0 = this.calculateBoundingBox(this.pos, this.getDirection());
         Vec3 $$1 = $$0.getCenter();
         this.setPosRaw($$1.x, $$1.y, $$1.z);
         this.setBoundingBox($$0);
      }
   }

   protected abstract AABB calculateBoundingBox(BlockPos var1, Direction var2);

   @Override
   public boolean survives() {
      if (this.hasLevelCollision(this.getPopBox())) {
         return false;
      } else {
         boolean $$0 = BlockPos.betweenClosedStream(this.calculateSupportBox()).allMatch($$0x -> {
            BlockState $$1 = this.level().getBlockState($$0x);
            return $$1.isSolid() || DiodeBlock.isDiode($$1);
         });
         return $$0 && this.canCoexist(false);
      }
   }

   protected AABB calculateSupportBox() {
      return this.getBoundingBox().move(this.getDirection().step().mul(-0.5F)).deflate(1.0E-7);
   }

   protected boolean canCoexist(boolean $$0) {
      Predicate<HangingEntity> $$1 = $$1x -> {
         boolean $$2 = !$$0 && $$1x.getType() == this.getType();
         boolean $$3 = $$1x.getDirection() == this.getDirection();
         return $$1x != this && ($$2 || $$3);
      };
      return !this.level().hasEntities(EntityTypeTest.forClass(HangingEntity.class), this.getPopBox(), $$1);
   }

   protected boolean hasLevelCollision(AABB $$0) {
      Level $$1 = this.level();
      return !$$1.noBlockCollision(this, $$0) || !$$1.noBorderCollision(this, $$0);
   }

   protected AABB getPopBox() {
      return this.getBoundingBox();
   }

   public abstract void playPlacementSound();

   @Override
   public ItemEntity spawnAtLocation(ServerLevel $$0, ItemStack $$1, float $$2) {
      ItemEntity $$3 = new ItemEntity(
         this.level(), this.getX() + this.getDirection().getStepX() * 0.15F, this.getY() + $$2, this.getZ() + this.getDirection().getStepZ() * 0.15F, $$1
      );
      $$3.setDefaultPickUpDelay();
      this.level().addFreshEntity($$3);
      return $$3;
   }

   @Override
   public float rotate(Rotation $$0) {
      Direction $$1 = this.getDirection();
      if ($$1.getAxis() != Axis.Y) {
         switch ($$0) {
            case CLOCKWISE_180:
               $$1 = $$1.getOpposite();
               break;
            case COUNTERCLOCKWISE_90:
               $$1 = $$1.getCounterClockWise();
               break;
            case CLOCKWISE_90:
               $$1 = $$1.getClockWise();
         }

         this.setDirection($$1);
      }

      float $$2 = Mth.wrapDegrees(this.getYRot());

      return switch ($$0) {
         case CLOCKWISE_180 -> $$2 + 180.0F;
         case COUNTERCLOCKWISE_90 -> $$2 + 90.0F;
         case CLOCKWISE_90 -> $$2 + 270.0F;
         default -> $$2;
      };
   }

   @Override
   public float mirror(Mirror $$0) {
      return this.rotate($$0.getRotation(this.getDirection()));
   }
}
