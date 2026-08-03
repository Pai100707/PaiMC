package net.minecraft.world.entity.vehicle.boat;

import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.BlockUtil.FoundRectangle;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public abstract class AbstractBoat extends VehicleEntity implements net.minecraft.world.entity.Leashable {
   private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_LEFT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_ID_PADDLE_RIGHT = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_ID_BUBBLE_TIME = SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.INT);
   public static final int PADDLE_LEFT = 0;
   public static final int PADDLE_RIGHT = 1;
   private static final int TIME_TO_EJECT = 60;
   private static final float PADDLE_SPEED = (float) (Math.PI / 8);
   public static final double PADDLE_SOUND_TIME = (float) (Math.PI / 4);
   public static final int BUBBLE_TIME = 60;
   private final float[] paddlePositions = new float[2];
   private float outOfControlTicks;
   private float deltaRotation;
   private final net.minecraft.world.entity.InterpolationHandler interpolation = new net.minecraft.world.entity.InterpolationHandler(this, 3);
   private boolean inputLeft;
   private boolean inputRight;
   private boolean inputUp;
   private boolean inputDown;
   private double waterLevel;
   private float landFriction;
   private AbstractBoat.Status status;
   private AbstractBoat.Status oldStatus;
   private double lastYd;
   private boolean isAboveBubbleColumn;
   private boolean bubbleColumnDirectionIsDown;
   private float bubbleMultiplier;
   private float bubbleAngle;
   private float bubbleAngleO;
   @Nullable
   private net.minecraft.world.entity.Leashable.LeashData leashData;
   private final Supplier<Item> dropItem;

   public AbstractBoat(net.minecraft.world.entity.EntityType<? extends AbstractBoat> $$0, Level $$1, Supplier<Item> $$2) {
      super($$0, $$1);
      this.dropItem = $$2;
      this.blocksBuilding = true;
   }

   public void setInitialPos(double $$0, double $$1, double $$2) {
      this.setPos($$0, $$1, $$2);
      this.xo = $$0;
      this.yo = $$1;
      this.zo = $$2;
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.EVENTS;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_ID_PADDLE_LEFT, false);
      $$0.define(DATA_ID_PADDLE_RIGHT, false);
      $$0.define(DATA_ID_BUBBLE_TIME, 0);
   }

   @Override
   public boolean canCollideWith(net.minecraft.world.entity.Entity $$0) {
      return canVehicleCollide(this, $$0);
   }

   public static boolean canVehicleCollide(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Entity $$1) {
      return ($$1.canBeCollidedWith($$0) || $$1.isPushable()) && !$$0.isPassengerOfSameVehicle($$1);
   }

   @Override
   public boolean canBeCollidedWith(@Nullable net.minecraft.world.entity.Entity $$0) {
      return true;
   }

   @Override
   public boolean isPushable() {
      return true;
   }

   @Override
   public Vec3 getRelativePortalPosition(Axis $$0, FoundRectangle $$1) {
      return net.minecraft.world.entity.LivingEntity.resetForwardDirectionOfRelativePortalPosition(super.getRelativePortalPosition($$0, $$1));
   }

   protected abstract double rideHeight(net.minecraft.world.entity.EntityDimensions var1);

   @Override
   protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.EntityDimensions $$1, float $$2) {
      float $$3 = this.getSinglePassengerXOffset();
      if (this.getPassengers().size() > 1) {
         int $$4 = this.getPassengers().indexOf($$0);
         if ($$4 == 0) {
            $$3 = 0.2F;
         } else {
            $$3 = -0.6F;
         }

         if ($$0 instanceof Animal) {
            $$3 += 0.2F;
         }
      }

      return new Vec3(0.0, this.rideHeight($$1), $$3).yRot(-this.getYRot() * (float) (Math.PI / 180.0));
   }

   @Override
   public void onAboveBubbleColumn(boolean $$0, BlockPos $$1) {
      if (this.level() instanceof ServerLevel) {
         this.isAboveBubbleColumn = true;
         this.bubbleColumnDirectionIsDown = $$0;
         if (this.getBubbleTime() == 0) {
            this.setBubbleTime(60);
         }
      }

      if (!this.isUnderWater() && this.random.nextInt(100) == 0) {
         this.level()
            .playLocalSound(
               this.getX(), this.getY(), this.getZ(), this.getSwimSplashSound(), this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat(), false
            );
         this.level()
            .addParticle(ParticleTypes.SPLASH, this.getX() + this.random.nextFloat(), this.getY() + 0.7, this.getZ() + this.random.nextFloat(), 0.0, 0.0, 0.0);
         this.gameEvent(GameEvent.SPLASH, this.getControllingPassenger());
      }
   }

   @Override
   public void push(net.minecraft.world.entity.Entity $$0) {
      if ($$0 instanceof AbstractBoat) {
         if ($$0.getBoundingBox().minY < this.getBoundingBox().maxY) {
            super.push($$0);
         }
      } else if ($$0.getBoundingBox().minY <= this.getBoundingBox().minY) {
         super.push($$0);
      }
   }

   @Override
   public void animateHurt(float $$0) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() * 11.0F);
   }

   @Override
   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   public net.minecraft.world.entity.InterpolationHandler getInterpolation() {
      return this.interpolation;
   }

   @Override
   public Direction getMotionDirection() {
      return this.getDirection().getClockWise();
   }

   @Override
   public void tick() {
      this.oldStatus = this.status;
      this.status = this.getStatus();
      if (this.status != AbstractBoat.Status.UNDER_WATER && this.status != AbstractBoat.Status.UNDER_FLOWING_WATER) {
         this.outOfControlTicks = 0.0F;
      } else {
         this.outOfControlTicks++;
      }

      if (!this.level().isClientSide() && this.outOfControlTicks >= 60.0F) {
         this.ejectPassengers();
      }

      if (this.getHurtTime() > 0) {
         this.setHurtTime(this.getHurtTime() - 1);
      }

      if (this.getDamage() > 0.0F) {
         this.setDamage(this.getDamage() - 1.0F);
      }

      super.tick();
      this.interpolation.interpolate();
      if (this.isLocalInstanceAuthoritative()) {
         if (!(this.getFirstPassenger() instanceof Player)) {
            this.setPaddleState(false, false);
         }

         this.floatBoat();
         if (this.level().isClientSide()) {
            this.controlBoat();
            this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
         }

         this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      } else {
         this.setDeltaMovement(Vec3.ZERO);
      }

      this.applyEffectsFromBlocks();
      this.applyEffectsFromBlocks();
      this.tickBubbleColumn();

      for (int $$0 = 0; $$0 <= 1; $$0++) {
         if (this.getPaddleState($$0)) {
            if (!this.isSilent()
               && this.paddlePositions[$$0] % (float) (Math.PI * 2) <= (float) (Math.PI / 4)
               && (this.paddlePositions[$$0] + (float) (Math.PI / 8)) % (float) (Math.PI * 2) >= (float) (Math.PI / 4)) {
               SoundEvent $$1 = this.getPaddleSound();
               if ($$1 != null) {
                  Vec3 $$2 = this.getViewVector(1.0F);
                  double $$3 = $$0 == 1 ? -$$2.z : $$2.z;
                  double $$4 = $$0 == 1 ? $$2.x : -$$2.x;
                  this.level()
                     .playSound(
                        null, this.getX() + $$3, this.getY(), this.getZ() + $$4, $$1, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat()
                     );
               }
            }

            this.paddlePositions[$$0] = this.paddlePositions[$$0] + (float) (Math.PI / 8);
         } else {
            this.paddlePositions[$$0] = 0.0F;
         }
      }

      List<net.minecraft.world.entity.Entity> $$5 = this.level()
         .getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), net.minecraft.world.entity.EntitySelector.pushableBy(this));
      if (!$$5.isEmpty()) {
         boolean $$6 = !this.level().isClientSide() && !(this.getControllingPassenger() instanceof Player);

         for (net.minecraft.world.entity.Entity $$7 : $$5) {
            if (!$$7.hasPassenger(this)) {
               if ($$6
                  && this.getPassengers().size() < this.getMaxPassengers()
                  && !$$7.isPassenger()
                  && this.hasEnoughSpaceFor($$7)
                  && $$7 instanceof net.minecraft.world.entity.LivingEntity
                  && !$$7.getType().is(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS)) {
                  $$7.startRiding(this);
               } else {
                  this.push($$7);
               }
            }
         }
      }
   }

   private void tickBubbleColumn() {
      if (this.level().isClientSide()) {
         int $$0 = this.getBubbleTime();
         if ($$0 > 0) {
            this.bubbleMultiplier += 0.05F;
         } else {
            this.bubbleMultiplier -= 0.1F;
         }

         this.bubbleMultiplier = Mth.clamp(this.bubbleMultiplier, 0.0F, 1.0F);
         this.bubbleAngleO = this.bubbleAngle;
         this.bubbleAngle = 10.0F * (float)Math.sin(0.5 * this.tickCount) * this.bubbleMultiplier;
      } else {
         if (!this.isAboveBubbleColumn) {
            this.setBubbleTime(0);
         }

         int $$1 = this.getBubbleTime();
         if ($$1 > 0) {
            this.setBubbleTime(--$$1);
            int $$2 = 60 - $$1 - 1;
            if ($$2 > 0 && $$1 == 0) {
               this.setBubbleTime(0);
               Vec3 $$3 = this.getDeltaMovement();
               if (this.bubbleColumnDirectionIsDown) {
                  this.setDeltaMovement($$3.add(0.0, -0.7, 0.0));
                  this.ejectPassengers();
               } else {
                  this.setDeltaMovement($$3.x, this.hasPassenger($$0 -> $$0 instanceof Player) ? 2.7 : 0.6, $$3.z);
               }
            }

            this.isAboveBubbleColumn = false;
         }
      }
   }

   @Nullable
   protected SoundEvent getPaddleSound() {
      return switch (this.getStatus()) {
         case IN_WATER, UNDER_WATER, UNDER_FLOWING_WATER -> SoundEvents.BOAT_PADDLE_WATER;
         case ON_LAND -> SoundEvents.BOAT_PADDLE_LAND;
         default -> null;
      };
   }

   public void setPaddleState(boolean $$0, boolean $$1) {
      this.entityData.set(DATA_ID_PADDLE_LEFT, $$0);
      this.entityData.set(DATA_ID_PADDLE_RIGHT, $$1);
   }

   public float getRowingTime(int $$0, float $$1) {
      return this.getPaddleState($$0) ? Mth.clampedLerp($$1, this.paddlePositions[$$0] - (float) (Math.PI / 8), this.paddlePositions[$$0]) : 0.0F;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.Leashable.LeashData getLeashData() {
      return this.leashData;
   }

   @Override
   public void setLeashData(@Nullable net.minecraft.world.entity.Leashable.LeashData $$0) {
      this.leashData = $$0;
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.88F * this.getBbHeight(), 0.64F * this.getBbWidth());
   }

   @Override
   public boolean supportQuadLeash() {
      return true;
   }

   @Override
   public Vec3[] getQuadLeashOffsets() {
      return net.minecraft.world.entity.Leashable.createQuadLeashOffsets(this, 0.0, 0.64, 0.382, 0.88);
   }

   private AbstractBoat.Status getStatus() {
      AbstractBoat.Status $$0 = this.isUnderwater();
      if ($$0 != null) {
         this.waterLevel = this.getBoundingBox().maxY;
         return $$0;
      } else if (this.checkInWater()) {
         return AbstractBoat.Status.IN_WATER;
      } else {
         float $$1 = this.getGroundFriction();
         if ($$1 > 0.0F) {
            this.landFriction = $$1;
            return AbstractBoat.Status.ON_LAND;
         } else {
            return AbstractBoat.Status.IN_AIR;
         }
      }
   }

   public float getWaterLevelAbove() {
      AABB $$0 = this.getBoundingBox();
      int $$1 = Mth.floor($$0.minX);
      int $$2 = Mth.ceil($$0.maxX);
      int $$3 = Mth.floor($$0.maxY);
      int $$4 = Mth.ceil($$0.maxY - this.lastYd);
      int $$5 = Mth.floor($$0.minZ);
      int $$6 = Mth.ceil($$0.maxZ);
      MutableBlockPos $$7 = new MutableBlockPos();

      label39:
      for (int $$8 = $$3; $$8 < $$4; $$8++) {
         float $$9 = 0.0F;

         for (int $$10 = $$1; $$10 < $$2; $$10++) {
            for (int $$11 = $$5; $$11 < $$6; $$11++) {
               $$7.set($$10, $$8, $$11);
               FluidState $$12 = this.level().getFluidState($$7);
               if ($$12.is(FluidTags.WATER)) {
                  $$9 = Math.max($$9, $$12.getHeight(this.level(), $$7));
               }

               if ($$9 >= 1.0F) {
                  continue label39;
               }
            }
         }

         if ($$9 < 1.0F) {
            return $$7.getY() + $$9;
         }
      }

      return $$4 + 1;
   }

   public float getGroundFriction() {
      AABB $$0 = this.getBoundingBox();
      AABB $$1 = new AABB($$0.minX, $$0.minY - 0.001, $$0.minZ, $$0.maxX, $$0.minY, $$0.maxZ);
      int $$2 = Mth.floor($$1.minX) - 1;
      int $$3 = Mth.ceil($$1.maxX) + 1;
      int $$4 = Mth.floor($$1.minY) - 1;
      int $$5 = Mth.ceil($$1.maxY) + 1;
      int $$6 = Mth.floor($$1.minZ) - 1;
      int $$7 = Mth.ceil($$1.maxZ) + 1;
      VoxelShape $$8 = Shapes.create($$1);
      float $$9 = 0.0F;
      int $$10 = 0;
      MutableBlockPos $$11 = new MutableBlockPos();

      for (int $$12 = $$2; $$12 < $$3; $$12++) {
         for (int $$13 = $$6; $$13 < $$7; $$13++) {
            int $$14 = ($$12 != $$2 && $$12 != $$3 - 1 ? 0 : 1) + ($$13 != $$6 && $$13 != $$7 - 1 ? 0 : 1);
            if ($$14 != 2) {
               for (int $$15 = $$4; $$15 < $$5; $$15++) {
                  if ($$14 <= 0 || $$15 != $$4 && $$15 != $$5 - 1) {
                     $$11.set($$12, $$15, $$13);
                     BlockState $$16 = this.level().getBlockState($$11);
                     if (!($$16.getBlock() instanceof WaterlilyBlock)
                        && Shapes.joinIsNotEmpty($$16.getCollisionShape(this.level(), $$11).move($$11), $$8, BooleanOp.AND)) {
                        $$9 += $$16.getBlock().getFriction();
                        $$10++;
                     }
                  }
               }
            }
         }
      }

      return $$9 / $$10;
   }

   private boolean checkInWater() {
      AABB $$0 = this.getBoundingBox();
      int $$1 = Mth.floor($$0.minX);
      int $$2 = Mth.ceil($$0.maxX);
      int $$3 = Mth.floor($$0.minY);
      int $$4 = Mth.ceil($$0.minY + 0.001);
      int $$5 = Mth.floor($$0.minZ);
      int $$6 = Mth.ceil($$0.maxZ);
      boolean $$7 = false;
      this.waterLevel = -Double.MAX_VALUE;
      MutableBlockPos $$8 = new MutableBlockPos();

      for (int $$9 = $$1; $$9 < $$2; $$9++) {
         for (int $$10 = $$3; $$10 < $$4; $$10++) {
            for (int $$11 = $$5; $$11 < $$6; $$11++) {
               $$8.set($$9, $$10, $$11);
               FluidState $$12 = this.level().getFluidState($$8);
               if ($$12.is(FluidTags.WATER)) {
                  float $$13 = $$10 + $$12.getHeight(this.level(), $$8);
                  this.waterLevel = Math.max((double)$$13, this.waterLevel);
                  $$7 |= $$0.minY < $$13;
               }
            }
         }
      }

      return $$7;
   }

   @Nullable
   private AbstractBoat.Status isUnderwater() {
      AABB $$0 = this.getBoundingBox();
      double $$1 = $$0.maxY + 0.001;
      int $$2 = Mth.floor($$0.minX);
      int $$3 = Mth.ceil($$0.maxX);
      int $$4 = Mth.floor($$0.maxY);
      int $$5 = Mth.ceil($$1);
      int $$6 = Mth.floor($$0.minZ);
      int $$7 = Mth.ceil($$0.maxZ);
      boolean $$8 = false;
      MutableBlockPos $$9 = new MutableBlockPos();

      for (int $$10 = $$2; $$10 < $$3; $$10++) {
         for (int $$11 = $$4; $$11 < $$5; $$11++) {
            for (int $$12 = $$6; $$12 < $$7; $$12++) {
               $$9.set($$10, $$11, $$12);
               FluidState $$13 = this.level().getFluidState($$9);
               if ($$13.is(FluidTags.WATER) && $$1 < $$9.getY() + $$13.getHeight(this.level(), $$9)) {
                  if (!$$13.isSource()) {
                     return AbstractBoat.Status.UNDER_FLOWING_WATER;
                  }

                  $$8 = true;
               }
            }
         }
      }

      return $$8 ? AbstractBoat.Status.UNDER_WATER : null;
   }

   @Override
   protected double getDefaultGravity() {
      return 0.04;
   }

   private void floatBoat() {
      double $$0 = -this.getGravity();
      double $$1 = 0.0;
      float $$2 = 0.05F;
      if (this.oldStatus == AbstractBoat.Status.IN_AIR && this.status != AbstractBoat.Status.IN_AIR && this.status != AbstractBoat.Status.ON_LAND) {
         this.waterLevel = this.getY(1.0);
         double $$3 = this.getWaterLevelAbove() - this.getBbHeight() + 0.101;
         if (this.level().noCollision(this, this.getBoundingBox().move(0.0, $$3 - this.getY(), 0.0))) {
            this.setPos(this.getX(), $$3, this.getZ());
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.0, 1.0));
            this.lastYd = 0.0;
         }

         this.status = AbstractBoat.Status.IN_WATER;
      } else {
         if (this.status == AbstractBoat.Status.IN_WATER) {
            $$1 = (this.waterLevel - this.getY()) / this.getBbHeight();
            $$2 = 0.9F;
         } else if (this.status == AbstractBoat.Status.UNDER_FLOWING_WATER) {
            $$0 = -7.0E-4;
            $$2 = 0.9F;
         } else if (this.status == AbstractBoat.Status.UNDER_WATER) {
            $$1 = 0.01F;
            $$2 = 0.45F;
         } else if (this.status == AbstractBoat.Status.IN_AIR) {
            $$2 = 0.9F;
         } else if (this.status == AbstractBoat.Status.ON_LAND) {
            $$2 = this.landFriction;
            if (this.getControllingPassenger() instanceof Player) {
               this.landFriction /= 2.0F;
            }
         }

         Vec3 $$4 = this.getDeltaMovement();
         this.setDeltaMovement($$4.x * $$2, $$4.y + $$0, $$4.z * $$2);
         this.deltaRotation *= $$2;
         if ($$1 > 0.0) {
            Vec3 $$5 = this.getDeltaMovement();
            this.setDeltaMovement($$5.x, ($$5.y + $$1 * (this.getDefaultGravity() / 0.65)) * 0.75, $$5.z);
         }
      }
   }

   private void controlBoat() {
      if (this.isVehicle()) {
         float $$0 = 0.0F;
         if (this.inputLeft) {
            this.deltaRotation--;
         }

         if (this.inputRight) {
            this.deltaRotation++;
         }

         if (this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown) {
            $$0 += 0.005F;
         }

         this.setYRot(this.getYRot() + this.deltaRotation);
         if (this.inputUp) {
            $$0 += 0.04F;
         }

         if (this.inputDown) {
            $$0 -= 0.005F;
         }

         this.setDeltaMovement(
            this.getDeltaMovement()
               .add(Mth.sin(-this.getYRot() * (float) (Math.PI / 180.0)) * $$0, 0.0, Mth.cos(this.getYRot() * (float) (Math.PI / 180.0)) * $$0)
         );
         this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
      }
   }

   protected float getSinglePassengerXOffset() {
      return 0.0F;
   }

   public boolean hasEnoughSpaceFor(net.minecraft.world.entity.Entity $$0) {
      return $$0.getBbWidth() < this.getBbWidth();
   }

   @Override
   protected void positionRider(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.Entity.MoveFunction $$1) {
      super.positionRider($$0, $$1);
      if (!$$0.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
         $$0.setYRot($$0.getYRot() + this.deltaRotation);
         $$0.setYHeadRot($$0.getYHeadRot() + this.deltaRotation);
         this.clampRotation($$0);
         if ($$0 instanceof Animal && this.getPassengers().size() == this.getMaxPassengers()) {
            int $$2 = $$0.getId() % 2 == 0 ? 90 : 270;
            $$0.setYBodyRot(((Animal)$$0).yBodyRot + $$2);
            $$0.setYHeadRot($$0.getYHeadRot() + $$2);
         }
      }
   }

   @Override
   public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity $$0) {
      Vec3 $$1 = getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, $$0.getBbWidth(), $$0.getYRot());
      double $$2 = this.getX() + $$1.x;
      double $$3 = this.getZ() + $$1.z;
      BlockPos $$4 = BlockPos.containing($$2, this.getBoundingBox().maxY, $$3);
      BlockPos $$5 = $$4.below();
      if (!this.level().isWaterAt($$5)) {
         List<Vec3> $$6 = Lists.newArrayList();
         double $$7 = this.level().getBlockFloorHeight($$4);
         if (DismountHelper.isBlockFloorValid($$7)) {
            $$6.add(new Vec3($$2, $$4.getY() + $$7, $$3));
         }

         double $$8 = this.level().getBlockFloorHeight($$5);
         if (DismountHelper.isBlockFloorValid($$8)) {
            $$6.add(new Vec3($$2, $$5.getY() + $$8, $$3));
         }

         UnmodifiableIterator var14 = $$0.getDismountPoses().iterator();

         while (var14.hasNext()) {
            net.minecraft.world.entity.Pose $$9 = (net.minecraft.world.entity.Pose)var14.next();

            for (Vec3 $$10 : $$6) {
               if (DismountHelper.canDismountTo(this.level(), $$10, $$0, $$9)) {
                  $$0.setPose($$9);
                  return $$10;
               }
            }
         }
      }

      return super.getDismountLocationForPassenger($$0);
   }

   protected void clampRotation(net.minecraft.world.entity.Entity $$0) {
      $$0.setYBodyRot(this.getYRot());
      float $$1 = Mth.wrapDegrees($$0.getYRot() - this.getYRot());
      float $$2 = Mth.clamp($$1, -105.0F, 105.0F);
      $$0.yRotO += $$2 - $$1;
      $$0.setYRot($$0.getYRot() + $$2 - $$1);
      $$0.setYHeadRot($$0.getYRot());
   }

   @Override
   public void onPassengerTurned(net.minecraft.world.entity.Entity $$0) {
      this.clampRotation($$0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      this.writeLeashData($$0, this.leashData);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.readLeashData($$0);
   }

   @Override
   public InteractionResult interact(Player $$0, InteractionHand $$1) {
      InteractionResult $$2 = super.interact($$0, $$1);
      if ($$2 != InteractionResult.PASS) {
         return $$2;
      } else {
         return (InteractionResult)($$0.isSecondaryUseActive() || !(this.outOfControlTicks < 60.0F) || !this.level().isClientSide() && !$$0.startRiding(this)
            ? InteractionResult.PASS
            : InteractionResult.SUCCESS);
      }
   }

   @Override
   public void remove(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      if (!this.level().isClientSide() && $$0.shouldDestroy() && this.isLeashed()) {
         this.dropLeash();
      }

      super.remove($$0);
   }

   @Override
   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
      this.lastYd = this.getDeltaMovement().y;
      if (!this.isPassenger()) {
         if ($$1) {
            this.resetFallDistance();
         } else if (!this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER) && $$0 < 0.0) {
            this.fallDistance -= (float)$$0;
         }
      }
   }

   public boolean getPaddleState(int $$0) {
      return (Boolean)this.entityData.get($$0 == 0 ? DATA_ID_PADDLE_LEFT : DATA_ID_PADDLE_RIGHT) && this.getControllingPassenger() != null;
   }

   private void setBubbleTime(int $$0) {
      this.entityData.set(DATA_ID_BUBBLE_TIME, $$0);
   }

   private int getBubbleTime() {
      return (Integer)this.entityData.get(DATA_ID_BUBBLE_TIME);
   }

   public float getBubbleAngle(float $$0) {
      return Mth.lerp($$0, this.bubbleAngleO, this.bubbleAngle);
   }

   @Override
   protected boolean canAddPassenger(net.minecraft.world.entity.Entity $$0) {
      return this.getPassengers().size() < this.getMaxPassengers() && !this.isEyeInFluid(FluidTags.WATER);
   }

   protected int getMaxPassengers() {
      return 2;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
      return this.getFirstPassenger() instanceof net.minecraft.world.entity.LivingEntity $$0 ? $$0 : super.getControllingPassenger();
   }

   public void setInput(boolean $$0, boolean $$1, boolean $$2, boolean $$3) {
      this.inputLeft = $$0;
      this.inputRight = $$1;
      this.inputUp = $$2;
      this.inputDown = $$3;
   }

   @Override
   public boolean isUnderWater() {
      return this.status == AbstractBoat.Status.UNDER_WATER || this.status == AbstractBoat.Status.UNDER_FLOWING_WATER;
   }

   @Override
   protected final Item getDropItem() {
      return this.dropItem.get();
   }

   @Override
   public final ItemStack getPickResult() {
      return new ItemStack((ItemLike)this.dropItem.get());
   }

   public static enum Status {
      IN_WATER,
      UNDER_WATER,
      UNDER_FLOWING_WATER,
      ON_LAND,
      IN_AIR;
   }
}
