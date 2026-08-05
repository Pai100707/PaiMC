package net.minecraft.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Pair;
import io.netty.buffer.ByteBuf;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NewMinecartBehavior extends MinecartBehavior {
   public static final int POS_ROT_LERP_TICKS = 3;
   public static final double ON_RAIL_Y_OFFSET = 0.1;
   public static final double OPPOSING_SLOPES_REST_AT_SPEED_THRESHOLD = 0.005;
   
   private NewMinecartBehavior.StepPartialTicks cacheIndexAlpha;
   private int cachedLerpDelay;
   private float cachedPartialTick;
   private int lerpDelay = 0;
   public final List<NewMinecartBehavior.MinecartStep> lerpSteps = new LinkedList<>();
   public final List<NewMinecartBehavior.MinecartStep> currentLerpSteps = new LinkedList<>();
   public double currentLerpStepsTotalWeight = 0.0;
   public NewMinecartBehavior.MinecartStep oldLerp = NewMinecartBehavior.MinecartStep.ZERO;

   public NewMinecartBehavior(AbstractMinecart $$0) {
      super($$0);
   }

   @Override
   public void tick() {
      if (this.level() instanceof ServerLevel $$0) {
         BlockPos var5 = this.minecart.getCurrentBlockPosOrRailBelow();
         BlockState $$4 = this.level().getBlockState(var5);
         if (this.minecart.isFirstTick()) {
            this.minecart.setOnRails(BaseRailBlock.isRail($$4));
            this.adjustToRails(var5, $$4, true);
         }

         this.minecart.applyGravity();
         this.minecart.moveAlongTrack($$0);
      } else {
         this.lerpClientPositionAndRotation();
         boolean $$1 = BaseRailBlock.isRail(this.level().getBlockState(this.minecart.getCurrentBlockPosOrRailBelow()));
         this.minecart.setOnRails($$1);
      }
   }

   private void lerpClientPositionAndRotation() {
      if (--this.lerpDelay <= 0) {
         this.setOldLerpValues();
         this.currentLerpSteps.clear();
         if (!this.lerpSteps.isEmpty()) {
            this.currentLerpSteps.addAll(this.lerpSteps);
            this.lerpSteps.clear();
            this.currentLerpStepsTotalWeight = 0.0;

            for (NewMinecartBehavior.MinecartStep $$0 : this.currentLerpSteps) {
               this.currentLerpStepsTotalWeight = this.currentLerpStepsTotalWeight + $$0.weight;
            }

            this.lerpDelay = this.currentLerpStepsTotalWeight == 0.0 ? 0 : 3;
         }
      }

      if (this.cartHasPosRotLerp()) {
         this.setPos(this.getCartLerpPosition(1.0F));
         this.setDeltaMovement(this.getCartLerpMovements(1.0F));
         this.setXRot(this.getCartLerpXRot(1.0F));
         this.setYRot(this.getCartLerpYRot(1.0F));
      }
   }

   public void setOldLerpValues() {
      this.oldLerp = new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), 0.0F);
   }

   public boolean cartHasPosRotLerp() {
      return !this.currentLerpSteps.isEmpty();
   }

   public float getCartLerpXRot(float $$0) {
      NewMinecartBehavior.StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
      return Mth.rotLerp($$1.partialTicksInStep, $$1.previousStep.xRot, $$1.currentStep.xRot);
   }

   public float getCartLerpYRot(float $$0) {
      NewMinecartBehavior.StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
      return Mth.rotLerp($$1.partialTicksInStep, $$1.previousStep.yRot, $$1.currentStep.yRot);
   }

   public Vec3 getCartLerpPosition(float $$0) {
      NewMinecartBehavior.StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
      return Mth.lerp($$1.partialTicksInStep, $$1.previousStep.position, $$1.currentStep.position);
   }

   public Vec3 getCartLerpMovements(float $$0) {
      NewMinecartBehavior.StepPartialTicks $$1 = this.getCurrentLerpStep($$0);
      return Mth.lerp($$1.partialTicksInStep, $$1.previousStep.movement, $$1.currentStep.movement);
   }

   private NewMinecartBehavior.StepPartialTicks getCurrentLerpStep(float $$0) {
      if ($$0 == this.cachedPartialTick && this.lerpDelay == this.cachedLerpDelay && this.cacheIndexAlpha != null) {
         return this.cacheIndexAlpha;
      } else {
         float $$1 = (3 - this.lerpDelay + $$0) / 3.0F;
         float $$2 = 0.0F;
         float $$3 = 1.0F;
         boolean $$4 = false;

         int $$5;
         for ($$5 = 0; $$5 < this.currentLerpSteps.size(); $$5++) {
            float $$6 = this.currentLerpSteps.get($$5).weight;
            if (!($$6 <= 0.0F)) {
               $$2 += $$6;
               if ($$2 >= this.currentLerpStepsTotalWeight * $$1) {
                  float $$7 = $$2 - $$6;
                  $$3 = (float)(($$1 * this.currentLerpStepsTotalWeight - $$7) / $$6);
                  $$4 = true;
                  break;
               }
            }
         }

         if (!$$4) {
            $$5 = this.currentLerpSteps.size() - 1;
         }

         NewMinecartBehavior.MinecartStep $$8 = this.currentLerpSteps.get($$5);
         NewMinecartBehavior.MinecartStep $$9 = $$5 > 0 ? this.currentLerpSteps.get($$5 - 1) : this.oldLerp;
         this.cacheIndexAlpha = new NewMinecartBehavior.StepPartialTicks($$3, $$8, $$9);
         this.cachedLerpDelay = this.lerpDelay;
         this.cachedPartialTick = $$0;
         return this.cacheIndexAlpha;
      }
   }

   public void adjustToRails(BlockPos $$0, BlockState $$1, boolean $$2) {
      if (BaseRailBlock.isRail($$1)) {
         RailShape $$3 = (RailShape)$$1.getValue(((BaseRailBlock)$$1.getBlock()).getShapeProperty());
         Pair<Vec3i, Vec3i> $$4 = AbstractMinecart.exits($$3);
         Vec3 $$5 = new Vec3((Vec3i)$$4.getFirst()).scale(0.5);
         Vec3 $$6 = new Vec3((Vec3i)$$4.getSecond()).scale(0.5);
         Vec3 $$7 = $$5.horizontal();
         Vec3 $$8 = $$6.horizontal();
         if (this.getDeltaMovement().length() > 1.0E-5F && this.getDeltaMovement().dot($$7) < this.getDeltaMovement().dot($$8) || this.isDecending($$8, $$3)) {
            Vec3 $$9 = $$7;
            $$7 = $$8;
            $$8 = $$9;
         }

         float $$10 = 180.0F - (float)(Math.atan2($$7.z, $$7.x) * 180.0 / Math.PI);
         $$10 += this.minecart.isFlipped() ? 180.0F : 0.0F;
         Vec3 $$11 = this.position();
         boolean $$12 = $$5.x() != $$6.x() && $$5.z() != $$6.z();
         Vec3 $$16;
         if ($$12) {
            Vec3 $$13 = $$6.subtract($$5);
            Vec3 $$14 = $$11.subtract($$0.getBottomCenter()).subtract($$5);
            Vec3 $$15 = $$13.scale($$13.dot($$14) / $$13.dot($$13));
            $$16 = $$0.getBottomCenter().add($$5).add($$15);
            $$10 = 180.0F - (float)(Math.atan2($$15.z, $$15.x) * 180.0 / Math.PI);
            $$10 += this.minecart.isFlipped() ? 180.0F : 0.0F;
         } else {
            boolean $$17 = $$5.subtract($$6).x != 0.0;
            boolean $$18 = $$5.subtract($$6).z != 0.0;
            $$16 = new Vec3($$18 ? $$0.getCenter().x : $$11.x, $$0.getY(), $$17 ? $$0.getCenter().z : $$11.z);
         }

         Vec3 $$20 = $$16.subtract($$11);
         this.setPos($$11.add($$20));
         float $$21 = 0.0F;
         boolean $$22 = $$5.y() != $$6.y();
         if ($$22) {
            Vec3 $$23 = $$0.getBottomCenter().add($$8);
            double $$24 = $$23.distanceTo(this.position());
            this.setPos(this.position().add(0.0, $$24 + 0.1, 0.0));
            $$21 = this.minecart.isFlipped() ? 45.0F : -45.0F;
         } else {
            this.setPos(this.position().add(0.0, 0.1, 0.0));
         }

         this.setRotation($$10, $$21);
         double $$25 = $$11.distanceTo(this.position());
         if ($$25 > 0.0) {
            this.lerpSteps
               .add(new NewMinecartBehavior.MinecartStep(this.position(), this.getDeltaMovement(), this.getYRot(), this.getXRot(), $$2 ? 0.0F : (float)$$25));
         }
      }
   }

   private void setRotation(float $$0, float $$1) {
      double $$2 = Math.abs($$0 - this.getYRot());
      if ($$2 >= 175.0 && $$2 <= 185.0) {
         this.minecart.setFlipped(!this.minecart.isFlipped());
         $$0 -= 180.0F;
         $$1 *= -1.0F;
      }

      $$1 = Math.clamp($$1, -45.0F, 45.0F);
      this.setXRot($$1 % 360.0F);
      this.setYRot($$0 % 360.0F);
   }

   @Override
   public void moveAlongTrack(ServerLevel $$0) {
      for (NewMinecartBehavior.TrackIteration $$1 = new NewMinecartBehavior.TrackIteration();
         $$1.shouldIterate() && this.minecart.isAlive();
         $$1.firstIteration = false
      ) {
         Vec3 $$2 = this.getDeltaMovement();
         BlockPos $$3 = this.minecart.getCurrentBlockPosOrRailBelow();
         BlockState $$4 = this.level().getBlockState($$3);
         boolean $$5 = BaseRailBlock.isRail($$4);
         if (this.minecart.isOnRails() != $$5) {
            this.minecart.setOnRails($$5);
            this.adjustToRails($$3, $$4, false);
         }

         if ($$5) {
            this.minecart.resetFallDistance();
            this.minecart.setOldPosAndRot();
            if ($$4.is(Blocks.ACTIVATOR_RAIL)) {
               this.minecart.activateMinecart($$0, $$3.getX(), $$3.getY(), $$3.getZ(), (Boolean)$$4.getValue(PoweredRailBlock.POWERED));
            }

            RailShape $$6 = (RailShape)$$4.getValue(((BaseRailBlock)$$4.getBlock()).getShapeProperty());
            Vec3 $$7 = this.calculateTrackSpeed($$0, $$2.horizontal(), $$1, $$3, $$4, $$6);
            if ($$1.firstIteration) {
               $$1.movementLeft = $$7.horizontalDistance();
            } else {
               $$1.movementLeft = $$1.movementLeft + ($$7.horizontalDistance() - $$2.horizontalDistance());
            }

            this.setDeltaMovement($$7);
            $$1.movementLeft = this.minecart.makeStepAlongTrack($$3, $$6, $$1.movementLeft);
         } else {
            this.minecart.comeOffTrack($$0);
            $$1.movementLeft = 0.0;
         }

         Vec3 $$8 = this.position();
         Vec3 $$9 = $$8.subtract(this.minecart.oldPosition());
         double $$10 = $$9.length();
         if ($$10 > 1.0E-5F) {
            if (!($$9.horizontalDistanceSqr() > 1.0E-5F)) {
               if (!this.minecart.isOnRails()) {
                  this.setXRot(this.minecart.onGround() ? 0.0F : Mth.rotLerp(0.2F, this.getXRot(), 0.0F));
               }
            } else {
               float $$11 = 180.0F - (float)(Math.atan2($$9.z, $$9.x) * 180.0 / Math.PI);
               float $$12 = this.minecart.onGround() && !this.minecart.isOnRails()
                  ? 0.0F
                  : 90.0F - (float)(Math.atan2($$9.horizontalDistance(), $$9.y) * 180.0 / Math.PI);
               $$11 += this.minecart.isFlipped() ? 180.0F : 0.0F;
               $$12 *= this.minecart.isFlipped() ? -1.0F : 1.0F;
               this.setRotation($$11, $$12);
            }

            this.lerpSteps
               .add(
                  new NewMinecartBehavior.MinecartStep(
                     $$8, this.getDeltaMovement(), this.getYRot(), this.getXRot(), (float)Math.min($$10, this.getMaxSpeed($$0))
                  )
               );
         } else if ($$2.horizontalDistanceSqr() > 0.0) {
            this.lerpSteps.add(new NewMinecartBehavior.MinecartStep($$8, this.getDeltaMovement(), this.getYRot(), this.getXRot(), 1.0F));
         }

         if ($$10 > 1.0E-5F || $$1.firstIteration) {
            this.minecart.applyEffectsFromBlocks();
            this.minecart.applyEffectsFromBlocks();
         }
      }
   }

   private Vec3 calculateTrackSpeed(ServerLevel $$0, Vec3 $$1, NewMinecartBehavior.TrackIteration $$2, BlockPos $$3, BlockState $$4, RailShape $$5) {
      Vec3 $$6 = $$1;
      if (!$$2.hasGainedSlopeSpeed) {
         Vec3 $$7 = this.calculateSlopeSpeed($$1, $$5);
         if ($$7.horizontalDistanceSqr() != $$1.horizontalDistanceSqr()) {
            $$2.hasGainedSlopeSpeed = true;
            $$6 = $$7;
         }
      }

      if ($$2.firstIteration) {
         Vec3 $$8 = this.calculatePlayerInputSpeed($$6);
         if ($$8.horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasHalted = true;
            $$6 = $$8;
         }
      }

      if (!$$2.hasHalted) {
         Vec3 $$9 = this.calculateHaltTrackSpeed($$6, $$4);
         if ($$9.horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasHalted = true;
            $$6 = $$9;
         }
      }

      if ($$2.firstIteration) {
         $$6 = this.minecart.applyNaturalSlowdown($$6);
         if ($$6.lengthSqr() > 0.0) {
            double $$10 = Math.min($$6.length(), this.minecart.getMaxSpeed($$0));
            $$6 = $$6.normalize().scale($$10);
         }
      }

      if (!$$2.hasBoosted) {
         Vec3 $$11 = this.calculateBoostTrackSpeed($$6, $$3, $$4);
         if ($$11.horizontalDistanceSqr() != $$6.horizontalDistanceSqr()) {
            $$2.hasBoosted = true;
            $$6 = $$11;
         }
      }

      return $$6;
   }

   private Vec3 calculateSlopeSpeed(Vec3 $$0, RailShape $$1) {
      double $$2 = Math.max(0.0078125, $$0.horizontalDistance() * 0.02);
      if (this.minecart.isInWater()) {
         $$2 *= 0.2;
      }
      return switch ($$1) {
         case ASCENDING_EAST -> $$0.add(-$$2, 0.0, 0.0);
         case ASCENDING_WEST -> $$0.add($$2, 0.0, 0.0);
         case ASCENDING_NORTH -> $$0.add(0.0, 0.0, $$2);
         case ASCENDING_SOUTH -> $$0.add(0.0, 0.0, -$$2);
         default -> $$0;
      };
   }

   private Vec3 calculatePlayerInputSpeed(Vec3 $$0) {
      if (this.minecart.getFirstPassenger() instanceof ServerPlayer $$1) {
         Vec3 $$3 = $$1.getLastClientMoveIntent();
         if ($$3.lengthSqr() > 0.0) {
            Vec3 $$4 = $$3.normalize();
            double $$5 = $$0.horizontalDistanceSqr();
            if ($$4.lengthSqr() > 0.0 && $$5 < 0.01) {
               return $$0.add(new Vec3($$4.x, 0.0, $$4.z).normalize().scale(0.001));
            }
         }

         return $$0;
      } else {
         return $$0;
      }
   }

   private Vec3 calculateHaltTrackSpeed(Vec3 $$0, BlockState $$1) {
      if ($$1.is(Blocks.POWERED_RAIL) && !(Boolean)$$1.getValue(PoweredRailBlock.POWERED)) {
         return $$0.length() < 0.03 ? Vec3.ZERO : $$0.scale(0.5);
      } else {
         return $$0;
      }
   }

   private Vec3 calculateBoostTrackSpeed(Vec3 $$0, BlockPos $$1, BlockState $$2) {
      if ($$2.is(Blocks.POWERED_RAIL) && (Boolean)$$2.getValue(PoweredRailBlock.POWERED)) {
         if ($$0.length() > 0.01) {
            return $$0.normalize().scale($$0.length() + 0.06);
         } else {
            Vec3 $$3 = this.minecart.getRedstoneDirection($$1);
            return $$3.lengthSqr() <= 0.0 ? $$0 : $$3.scale($$0.length() + 0.2);
         }
      } else {
         return $$0;
      }
   }

   @Override
   public double stepAlongTrack(BlockPos $$0, RailShape $$1, double $$2) {
      if ($$2 < 1.0E-5F) {
         return 0.0;
      } else {
         Vec3 $$3 = this.position();
         Pair<Vec3i, Vec3i> $$4 = AbstractMinecart.exits($$1);
         Vec3i $$5 = (Vec3i)$$4.getFirst();
         Vec3i $$6 = (Vec3i)$$4.getSecond();
         Vec3 $$7 = this.getDeltaMovement().horizontal();
         if ($$7.length() < 1.0E-5F) {
            this.setDeltaMovement(Vec3.ZERO);
            return 0.0;
         } else {
            boolean $$8 = $$5.getY() != $$6.getY();
            Vec3 $$9 = new Vec3($$6).scale(0.5).horizontal();
            Vec3 $$10 = new Vec3($$5).scale(0.5).horizontal();
            if ($$7.dot($$10) < $$7.dot($$9)) {
               $$10 = $$9;
            }

            Vec3 $$11 = $$0.getBottomCenter().add($$10).add(0.0, 0.1, 0.0).add($$10.normalize().scale(1.0E-5F));
            if ($$8 && !this.isDecending($$7, $$1)) {
               $$11 = $$11.add(0.0, 1.0, 0.0);
            }

            Vec3 $$12 = $$11.subtract(this.position()).normalize();
            $$7 = $$12.scale($$7.length() / $$12.horizontalDistance());
            Vec3 $$13 = $$3.add($$7.normalize().scale($$2 * ($$8 ? Mth.SQRT_OF_TWO : 1.0F)));
            if ($$3.distanceToSqr($$11) <= $$3.distanceToSqr($$13)) {
               $$2 = $$11.subtract($$13).horizontalDistance();
               $$13 = $$11;
            } else {
               $$2 = 0.0;
            }

            this.minecart.move(net.minecraft.world.entity.MoverType.SELF, $$13.subtract($$3));
            BlockState $$14 = this.level().getBlockState(BlockPos.containing($$13));
            if ($$8) {
               if (BaseRailBlock.isRail($$14)) {
                  RailShape $$15 = (RailShape)$$14.getValue(((BaseRailBlock)$$14.getBlock()).getShapeProperty());
                  if (this.restAtVShape($$1, $$15)) {
                     return 0.0;
                  }
               }

               double $$16 = $$11.horizontal().distanceTo(this.position().horizontal());
               double $$17 = $$11.y + (this.isDecending($$7, $$1) ? $$16 : -$$16);
               if (this.position().y < $$17) {
                  this.setPos(this.position().x, $$17, this.position().z);
               }
            }

            if (this.position().distanceTo($$3) < 1.0E-5F && $$13.distanceTo($$3) > 1.0E-5F) {
               this.setDeltaMovement(Vec3.ZERO);
               return 0.0;
            } else {
               this.setDeltaMovement($$7);
               return $$2;
            }
         }
      }
   }

   private boolean restAtVShape(RailShape $$0, RailShape $$1) {
      if (this.getDeltaMovement().lengthSqr() < 0.005
         && $$1.isSlope()
         && this.isDecending(this.getDeltaMovement(), $$0)
         && !this.isDecending(this.getDeltaMovement(), $$1)) {
         this.setDeltaMovement(Vec3.ZERO);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public double getMaxSpeed(ServerLevel $$0) {
      return ((Integer)$$0.getGameRules().get(GameRules.MAX_MINECART_SPEED)).intValue() * (this.minecart.isInWater() ? 0.5 : 1.0) / 20.0;
   }

   private boolean isDecending(Vec3 $$0, RailShape $$1) {
      return switch ($$1) {
         case ASCENDING_EAST -> $$0.x < 0.0;
         case ASCENDING_WEST -> $$0.x > 0.0;
         case ASCENDING_NORTH -> $$0.z > 0.0;
         case ASCENDING_SOUTH -> $$0.z < 0.0;
         default -> false;
      };
   }

   @Override
   public double getSlowdownFactor() {
      return this.minecart.isVehicle() ? 0.997 : 0.975;
   }

   @Override
   public boolean pushAndPickupEntities() {
      boolean $$0 = this.pickupEntities(this.minecart.getBoundingBox().inflate(0.2, 0.0, 0.2));
      if (!this.minecart.horizontalCollision && !this.minecart.verticalCollision) {
         return false;
      } else {
         boolean $$1 = this.pushEntities(this.minecart.getBoundingBox().inflate(1.0E-7));
         return $$0 && !$$1;
      }
   }

   public boolean pickupEntities(AABB $$0) {
      if (this.minecart.isRideable() && !this.minecart.isVehicle()) {
         List<net.minecraft.world.entity.Entity> $$1 = this.level()
            .getEntities(this.minecart, $$0, net.minecraft.world.entity.EntitySelector.pushableBy(this.minecart));
         if (!$$1.isEmpty()) {
            for (net.minecraft.world.entity.Entity $$2 : $$1) {
               if (!($$2 instanceof Player)
                  && !($$2 instanceof IronGolem)
                  && !($$2 instanceof AbstractMinecart)
                  && !this.minecart.isVehicle()
                  && !$$2.isPassenger()) {
                  boolean $$3 = $$2.startRiding(this.minecart);
                  if ($$3) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean pushEntities(AABB $$0) {
      boolean $$1 = false;
      if (this.minecart.isRideable()) {
         List<net.minecraft.world.entity.Entity> $$2 = this.level()
            .getEntities(this.minecart, $$0, net.minecraft.world.entity.EntitySelector.pushableBy(this.minecart));
         if (!$$2.isEmpty()) {
            for (net.minecraft.world.entity.Entity $$3 : $$2) {
               if ($$3 instanceof Player || $$3 instanceof IronGolem || $$3 instanceof AbstractMinecart || this.minecart.isVehicle() || $$3.isPassenger()) {
                  $$3.push(this.minecart);
                  $$1 = true;
               }
            }
         }
      } else {
         for (net.minecraft.world.entity.Entity $$4 : this.level().getEntities(this.minecart, $$0)) {
            if (!this.minecart.hasPassenger($$4) && $$4.isPushable() && $$4 instanceof AbstractMinecart) {
               $$4.push(this.minecart);
               $$1 = true;
            }
         }
      }

      return $$1;
   }

   public record MinecartStep(Vec3 position, Vec3 movement, float yRot, float xRot, float weight) {
      public static final StreamCodec<ByteBuf, NewMinecartBehavior.MinecartStep> STREAM_CODEC = StreamCodec.composite(
         Vec3.STREAM_CODEC,
         NewMinecartBehavior.MinecartStep::position,
         Vec3.STREAM_CODEC,
         NewMinecartBehavior.MinecartStep::movement,
         ByteBufCodecs.ROTATION_BYTE,
         NewMinecartBehavior.MinecartStep::yRot,
         ByteBufCodecs.ROTATION_BYTE,
         NewMinecartBehavior.MinecartStep::xRot,
         ByteBufCodecs.FLOAT,
         NewMinecartBehavior.MinecartStep::weight,
         NewMinecartBehavior.MinecartStep::new
      );
      public static NewMinecartBehavior.MinecartStep ZERO = new NewMinecartBehavior.MinecartStep(Vec3.ZERO, Vec3.ZERO, 0.0F, 0.0F, 0.0F);
   }

   record StepPartialTicks(float partialTicksInStep, NewMinecartBehavior.MinecartStep currentStep, NewMinecartBehavior.MinecartStep previousStep) {
   }

   static class TrackIteration {
      double movementLeft = 0.0;
      boolean firstIteration = true;
      boolean hasGainedSlopeSpeed = false;
      boolean hasHalted = false;
      boolean hasBoosted = false;

      public boolean shouldIterate() {
         return this.firstIteration || this.movementLeft > 1.0E-5F;
      }
   }
}
