package net.minecraft.world.entity.projectile;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class FishingHook extends Projectile {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final RandomSource syncronizedRandom = RandomSource.create();
   private boolean biting;
   private int outOfWaterTime;
   private static final int MAX_OUT_OF_WATER_TIME = 10;
   private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.BOOLEAN);
   private int life;
   private int nibble;
   private int timeUntilLured;
   private int timeUntilHooked;
   private float fishAngle;
   private boolean openWater = true;
   @Nullable
   private net.minecraft.world.entity.Entity hookedIn;
   private FishingHook.FishHookState currentState = FishingHook.FishHookState.FLYING;
   private final int luck;
   private final int lureSpeed;
   private final net.minecraft.world.entity.InterpolationHandler interpolationHandler = new net.minecraft.world.entity.InterpolationHandler(this);

   private FishingHook(net.minecraft.world.entity.EntityType<? extends FishingHook> $$0, Level $$1, int $$2, int $$3) {
      super($$0, $$1);
      this.luck = Math.max(0, $$2);
      this.lureSpeed = Math.max(0, $$3);
   }

   public FishingHook(net.minecraft.world.entity.EntityType<? extends FishingHook> $$0, Level $$1) {
      this($$0, $$1, 0, 0);
   }

   public FishingHook(Player $$0, Level $$1, int $$2, int $$3) {
      this(net.minecraft.world.entity.EntityType.FISHING_BOBBER, $$1, $$2, $$3);
      this.setOwner($$0);
      float $$4 = $$0.getXRot();
      float $$5 = $$0.getYRot();
      float $$6 = Mth.cos(-$$5 * (float) (Math.PI / 180.0) - (float) Math.PI);
      float $$7 = Mth.sin(-$$5 * (float) (Math.PI / 180.0) - (float) Math.PI);
      float $$8 = -Mth.cos(-$$4 * (float) (Math.PI / 180.0));
      float $$9 = Mth.sin(-$$4 * (float) (Math.PI / 180.0));
      double $$10 = $$0.getX() - $$7 * 0.3;
      double $$11 = $$0.getEyeY();
      double $$12 = $$0.getZ() - $$6 * 0.3;
      this.snapTo($$10, $$11, $$12, $$5, $$4);
      Vec3 $$13 = new Vec3(-$$7, Mth.clamp(-($$9 / $$8), -5.0F, 5.0F), -$$6);
      double $$14 = $$13.length();
      $$13 = $$13.multiply(
         0.6 / $$14 + this.random.triangle(0.5, 0.0103365),
         0.6 / $$14 + this.random.triangle(0.5, 0.0103365),
         0.6 / $$14 + this.random.triangle(0.5, 0.0103365)
      );
      this.setDeltaMovement($$13);
      this.setYRot((float)(Mth.atan2($$13.x, $$13.z) * 180.0F / (float)Math.PI));
      this.setXRot((float)(Mth.atan2($$13.y, $$13.horizontalDistance()) * 180.0F / (float)Math.PI));
      this.yRotO = this.getYRot();
      this.xRotO = this.getXRot();
   }

   @Override
   public net.minecraft.world.entity.InterpolationHandler getInterpolation() {
      return this.interpolationHandler;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_HOOKED_ENTITY, 0);
      $$0.define(DATA_BITING, false);
   }

   @Override
   protected boolean shouldBounceOnWorldBorder() {
      return true;
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (DATA_HOOKED_ENTITY.equals($$0)) {
         int $$1 = (Integer)this.getEntityData().get(DATA_HOOKED_ENTITY);
         this.hookedIn = $$1 > 0 ? this.level().getEntity($$1 - 1) : null;
      }

      if (DATA_BITING.equals($$0)) {
         this.biting = (Boolean)this.getEntityData().get(DATA_BITING);
         if (this.biting) {
            this.setDeltaMovement(this.getDeltaMovement().x, -0.4F * Mth.nextFloat(this.syncronizedRandom, 0.6F, 1.0F), this.getDeltaMovement().z);
         }
      }

      super.onSyncedDataUpdated($$0);
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = 64.0;
      return $$0 < 4096.0;
   }

   @Override
   public void tick() {
      this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
      this.getInterpolation().interpolate();
      super.tick();
      Player $$0 = this.getPlayerOwner();
      if ($$0 == null) {
         this.discard();
      } else if (this.level().isClientSide() || !this.shouldStopFishing($$0)) {
         if (this.onGround()) {
            this.life++;
            if (this.life >= 1200) {
               this.discard();
               return;
            }
         } else {
            this.life = 0;
         }

         float $$1 = 0.0F;
         BlockPos $$2 = this.blockPosition();
         FluidState $$3 = this.level().getFluidState($$2);
         if ($$3.is(FluidTags.WATER)) {
            $$1 = $$3.getHeight(this.level(), $$2);
         }

         boolean $$4 = $$1 > 0.0F;
         if (this.currentState == FishingHook.FishHookState.FLYING) {
            if (this.hookedIn != null) {
               this.setDeltaMovement(Vec3.ZERO);
               this.currentState = FishingHook.FishHookState.HOOKED_IN_ENTITY;
               return;
            }

            if ($$4) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
               this.currentState = FishingHook.FishHookState.BOBBING;
               return;
            }

            this.checkCollision();
         } else {
            if (this.currentState == FishingHook.FishHookState.HOOKED_IN_ENTITY) {
               if (this.hookedIn != null) {
                  if (!this.hookedIn.isRemoved() && this.hookedIn.canInteractWithLevel() && this.hookedIn.level().dimension() == this.level().dimension()) {
                     this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                  } else {
                     this.setHookedEntity(null);
                     this.currentState = FishingHook.FishHookState.FLYING;
                  }
               }

               return;
            }

            if (this.currentState == FishingHook.FishHookState.BOBBING) {
               Vec3 $$5 = this.getDeltaMovement();
               double $$6 = this.getY() + $$5.y - $$2.getY() - $$1;
               if (Math.abs($$6) < 0.01) {
                  $$6 += Math.signum($$6) * 0.1;
               }

               this.setDeltaMovement($$5.x * 0.9, $$5.y - $$6 * this.random.nextFloat() * 0.2, $$5.z * 0.9);
               if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
                  this.openWater = true;
               } else {
                  this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater($$2);
               }

               if ($$4) {
                  this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                  if (this.biting) {
                     this.setDeltaMovement(
                        this.getDeltaMovement().add(0.0, -0.1 * this.syncronizedRandom.nextFloat() * this.syncronizedRandom.nextFloat(), 0.0)
                     );
                  }

                  if (!this.level().isClientSide()) {
                     this.catchingFish($$2);
                  }
               } else {
                  this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
               }
            }
         }

         if (!$$3.is(FluidTags.WATER) && !this.onGround() && this.hookedIn == null) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
         }

         this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
         this.applyEffectsFromBlocks();
         this.updateRotation();
         if (this.currentState == FishingHook.FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
         }

         double $$7 = 0.92;
         this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
         this.reapplyPosition();
      }
   }

   private boolean shouldStopFishing(Player $$0) {
      if ($$0.canInteractWithLevel()) {
         ItemStack $$1 = $$0.getMainHandItem();
         ItemStack $$2 = $$0.getOffhandItem();
         boolean $$3 = $$1.is(Items.FISHING_ROD);
         boolean $$4 = $$2.is(Items.FISHING_ROD);
         if (($$3 || $$4) && this.distanceToSqr($$0) <= 1024.0) {
            return false;
         }
      }

      this.discard();
      return true;
   }

   private void checkCollision() {
      HitResult $$0 = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      this.hitTargetOrDeflectSelf($$0);
   }

   @Override
   protected boolean canHitEntity(net.minecraft.world.entity.Entity $$0) {
      return super.canHitEntity($$0) || $$0.isAlive() && $$0 instanceof ItemEntity;
   }

   @Override
   protected void onHitEntity(EntityHitResult $$0) {
      super.onHitEntity($$0);
      if (!this.level().isClientSide()) {
         this.setHookedEntity($$0.getEntity());
      }
   }

   @Override
   protected void onHitBlock(BlockHitResult $$0) {
      super.onHitBlock($$0);
      this.setDeltaMovement(this.getDeltaMovement().normalize().scale($$0.distanceTo(this)));
   }

   private void setHookedEntity(@Nullable net.minecraft.world.entity.Entity $$0) {
      this.hookedIn = $$0;
      this.getEntityData().set(DATA_HOOKED_ENTITY, $$0 == null ? 0 : $$0.getId() + 1);
   }

   private void catchingFish(BlockPos $$0) {
      ServerLevel $$1 = (ServerLevel)this.level();
      int $$2 = 1;
      BlockPos $$3 = $$0.above();
      if (this.random.nextFloat() < 0.25F && this.level().isRainingAt($$3)) {
         $$2++;
      }

      if (this.random.nextFloat() < 0.5F && !this.level().canSeeSky($$3)) {
         $$2--;
      }

      if (this.nibble > 0) {
         this.nibble--;
         if (this.nibble <= 0) {
            this.timeUntilLured = 0;
            this.timeUntilHooked = 0;
            this.getEntityData().set(DATA_BITING, false);
         }
      } else if (this.timeUntilHooked > 0) {
         this.timeUntilHooked -= $$2;
         if (this.timeUntilHooked > 0) {
            this.fishAngle = this.fishAngle + (float)this.random.triangle(0.0, 9.188);
            float $$4 = this.fishAngle * (float) (Math.PI / 180.0);
            float $$5 = Mth.sin($$4);
            float $$6 = Mth.cos($$4);
            double $$7 = this.getX() + $$5 * this.timeUntilHooked * 0.1F;
            double $$8 = Mth.floor(this.getY()) + 1.0F;
            double $$9 = this.getZ() + $$6 * this.timeUntilHooked * 0.1F;
            BlockState $$10 = $$1.getBlockState(BlockPos.containing($$7, $$8 - 1.0, $$9));
            if ($$10.is(Blocks.WATER)) {
               if (this.random.nextFloat() < 0.15F) {
                  $$1.sendParticles(ParticleTypes.BUBBLE, $$7, $$8 - 0.1F, $$9, 1, $$5, 0.1, $$6, 0.0);
               }

               float $$11 = $$5 * 0.04F;
               float $$12 = $$6 * 0.04F;
               $$1.sendParticles(ParticleTypes.FISHING, $$7, $$8, $$9, 0, $$12, 0.01, -$$11, 1.0);
               $$1.sendParticles(ParticleTypes.FISHING, $$7, $$8, $$9, 0, -$$12, 0.01, $$11, 1.0);
            }
         } else {
            this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
            double $$13 = this.getY() + 0.5;
            $$1.sendParticles(
               ParticleTypes.BUBBLE, this.getX(), $$13, this.getZ(), (int)(1.0F + this.getBbWidth() * 20.0F), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2F
            );
            $$1.sendParticles(
               ParticleTypes.FISHING, this.getX(), $$13, this.getZ(), (int)(1.0F + this.getBbWidth() * 20.0F), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2F
            );
            this.nibble = Mth.nextInt(this.random, 20, 40);
            this.getEntityData().set(DATA_BITING, true);
         }
      } else if (this.timeUntilLured > 0) {
         this.timeUntilLured -= $$2;
         float $$14 = 0.15F;
         if (this.timeUntilLured < 20) {
            $$14 += (20 - this.timeUntilLured) * 0.05F;
         } else if (this.timeUntilLured < 40) {
            $$14 += (40 - this.timeUntilLured) * 0.02F;
         } else if (this.timeUntilLured < 60) {
            $$14 += (60 - this.timeUntilLured) * 0.01F;
         }

         if (this.random.nextFloat() < $$14) {
            float $$15 = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
            float $$16 = Mth.nextFloat(this.random, 25.0F, 60.0F);
            double $$17 = this.getX() + Mth.sin($$15) * $$16 * 0.1;
            double $$18 = Mth.floor(this.getY()) + 1.0F;
            double $$19 = this.getZ() + Mth.cos($$15) * $$16 * 0.1;
            BlockState $$20 = $$1.getBlockState(BlockPos.containing($$17, $$18 - 1.0, $$19));
            if ($$20.is(Blocks.WATER)) {
               $$1.sendParticles(ParticleTypes.SPLASH, $$17, $$18, $$19, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
            }
         }

         if (this.timeUntilLured <= 0) {
            this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
            this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
         }
      } else {
         this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
         this.timeUntilLured = this.timeUntilLured - this.lureSpeed;
      }
   }

   private boolean calculateOpenWater(BlockPos $$0) {
      FishingHook.OpenWaterType $$1 = FishingHook.OpenWaterType.INVALID;

      for (int $$2 = -1; $$2 <= 2; $$2++) {
         FishingHook.OpenWaterType $$3 = this.getOpenWaterTypeForArea($$0.offset(-2, $$2, -2), $$0.offset(2, $$2, 2));
         switch ($$3) {
            case ABOVE_WATER:
               if ($$1 == FishingHook.OpenWaterType.INVALID) {
                  return false;
               }
               break;
            case INSIDE_WATER:
               if ($$1 == FishingHook.OpenWaterType.ABOVE_WATER) {
                  return false;
               }
               break;
            case INVALID:
               return false;
         }

         $$1 = $$3;
      }

      return true;
   }

   private FishingHook.OpenWaterType getOpenWaterTypeForArea(BlockPos $$0, BlockPos $$1) {
      return BlockPos.betweenClosedStream($$0, $$1)
         .map(this::getOpenWaterTypeForBlock)
         .reduce(($$0x, $$1x) -> $$0x == $$1x ? $$0x : FishingHook.OpenWaterType.INVALID)
         .orElse(FishingHook.OpenWaterType.INVALID);
   }

   private FishingHook.OpenWaterType getOpenWaterTypeForBlock(BlockPos $$0) {
      BlockState $$1 = this.level().getBlockState($$0);
      if (!$$1.isAir() && !$$1.is(Blocks.LILY_PAD)) {
         FluidState $$2 = $$1.getFluidState();
         return $$2.is(FluidTags.WATER) && $$2.isSource() && $$1.getCollisionShape(this.level(), $$0).isEmpty()
            ? FishingHook.OpenWaterType.INSIDE_WATER
            : FishingHook.OpenWaterType.INVALID;
      } else {
         return FishingHook.OpenWaterType.ABOVE_WATER;
      }
   }

   public boolean isOpenWaterFishing() {
      return this.openWater;
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
   }

   public int retrieve(ItemStack $$0) {
      Player $$1 = this.getPlayerOwner();
      if (!this.level().isClientSide() && $$1 != null && !this.shouldStopFishing($$1)) {
         int $$2 = 0;
         if (this.hookedIn != null) {
            this.pullEntity(this.hookedIn);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)$$1, $$0, this, Collections.emptyList());
            this.level().broadcastEntityEvent(this, (byte)31);
            $$2 = this.hookedIn instanceof ItemEntity ? 3 : 5;
         } else if (this.nibble > 0) {
            LootParams $$3 = new net.minecraft.world.level.storage.loot.LootParams.Builder((ServerLevel)this.level())
               .withParameter(LootContextParams.ORIGIN, this.position())
               .withParameter(LootContextParams.TOOL, $$0)
               .withParameter(LootContextParams.THIS_ENTITY, this)
               .withLuck(this.luck + $$1.getLuck())
               .create(LootContextParamSets.FISHING);
            LootTable $$4 = this.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
            List<ItemStack> $$5 = $$4.getRandomItems($$3);
            CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayer)$$1, $$0, this, $$5);

            for (ItemStack $$6 : $$5) {
               ItemEntity $$7 = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), $$6);
               double $$8 = $$1.getX() - this.getX();
               double $$9 = $$1.getY() - this.getY();
               double $$10 = $$1.getZ() - this.getZ();
               double $$11 = 0.1;
               $$7.setDeltaMovement($$8 * 0.1, $$9 * 0.1 + Math.sqrt(Math.sqrt($$8 * $$8 + $$9 * $$9 + $$10 * $$10)) * 0.08, $$10 * 0.1);
               this.level().addFreshEntity($$7);
               $$1.level()
                  .addFreshEntity(
                     new net.minecraft.world.entity.ExperienceOrb($$1.level(), $$1.getX(), $$1.getY() + 0.5, $$1.getZ() + 0.5, this.random.nextInt(6) + 1)
                  );
               if ($$6.is(ItemTags.FISHES)) {
                  $$1.awardStat(Stats.FISH_CAUGHT, 1);
               }
            }

            $$2 = 1;
         }

         if (this.onGround()) {
            $$2 = 2;
         }

         this.discard();
         return $$2;
      } else {
         return 0;
      }
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 31 && this.level().isClientSide() && this.hookedIn instanceof Player $$1 && $$1.isLocalPlayer()) {
         this.pullEntity(this.hookedIn);
      }

      super.handleEntityEvent($$0);
   }

   protected void pullEntity(net.minecraft.world.entity.Entity $$0) {
      net.minecraft.world.entity.Entity $$1 = this.getOwner();
      if ($$1 != null) {
         Vec3 $$2 = new Vec3($$1.getX() - this.getX(), $$1.getY() - this.getY(), $$1.getZ() - this.getZ()).scale(0.1);
         $$0.setDeltaMovement($$0.getDeltaMovement().add($$2));
      }
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.NONE;
   }

   @Override
   public void remove(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      this.updateOwnerInfo(null);
      super.remove($$0);
   }

   @Override
   public void onClientRemoval() {
      this.updateOwnerInfo(null);
   }

   @Override
   public void setOwner(@Nullable net.minecraft.world.entity.Entity $$0) {
      super.setOwner($$0);
      this.updateOwnerInfo(this);
   }

   private void updateOwnerInfo(@Nullable FishingHook $$0) {
      Player $$1 = this.getPlayerOwner();
      if ($$1 != null) {
         $$1.fishing = $$0;
      }
   }

   @Nullable
   public Player getPlayerOwner() {
      return this.getOwner() instanceof Player $$1 ? $$1 : null;
   }

   @Nullable
   public net.minecraft.world.entity.Entity getHookedIn() {
      return this.hookedIn;
   }

   @Override
   public boolean canUsePortal(boolean $$0) {
      return false;
   }

   @Override
   public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity $$0) {
      net.minecraft.world.entity.Entity $$1 = this.getOwner();
      return new ClientboundAddEntityPacket(this, $$0, $$1 == null ? this.getId() : $$1.getId());
   }

   @Override
   public void recreateFromPacket(ClientboundAddEntityPacket $$0) {
      super.recreateFromPacket($$0);
      if (this.getPlayerOwner() == null) {
         int $$1 = $$0.getData();
         LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", this.level().getEntity($$1), $$1);
         this.discard();
      }
   }

   static enum FishHookState {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }

   static enum OpenWaterType {
      ABOVE_WATER,
      INSIDE_WATER,
      INVALID;
   }
}
