package net.minecraft.world.entity.ambient;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Bat extends AmbientCreature {
   public static final float FLAP_LENGTH_SECONDS = 0.5F;
   public static final float TICKS_PER_FLAP = 10.0F;
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Bat.class, EntityDataSerializers.BYTE);
   private static final int FLAG_RESTING = 1;
   private static final TargetingConditions BAT_RESTING_TARGETING = TargetingConditions.forNonCombat().range(4.0);
   private static final byte DEFAULT_FLAGS = 0;
   public final net.minecraft.world.entity.AnimationState flyAnimationState = new net.minecraft.world.entity.AnimationState();
   public final net.minecraft.world.entity.AnimationState restAnimationState = new net.minecraft.world.entity.AnimationState();
   @Nullable
   private BlockPos targetPosition;

   public Bat(net.minecraft.world.entity.EntityType<? extends Bat> $$0, Level $$1) {
      super($$0, $$1);
      if (!$$1.isClientSide()) {
         this.setResting(true);
      }
   }

   @Override
   public boolean isFlapping() {
      return !this.isResting() && this.tickCount % 10.0F == 0.0F;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_ID_FLAGS, (byte)0);
   }

   @Override
   protected float getSoundVolume() {
      return 0.1F;
   }

   @Override
   public float getVoicePitch() {
      return super.getVoicePitch() * 0.95F;
   }

   @Nullable
   @Override
   public SoundEvent getAmbientSound() {
      return this.isResting() && this.random.nextInt(4) != 0 ? null : SoundEvents.BAT_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.BAT_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.BAT_DEATH;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(net.minecraft.world.entity.Entity $$0) {
   }

   @Override
   protected void pushEntities() {
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0);
   }

   public boolean isResting() {
      return ((Byte)this.entityData.get(DATA_ID_FLAGS) & 1) != 0;
   }

   public void setResting(boolean $$0) {
      byte $$1 = (Byte)this.entityData.get(DATA_ID_FLAGS);
      if ($$0) {
         this.entityData.set(DATA_ID_FLAGS, (byte)($$1 | 1));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)($$1 & -2));
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isResting()) {
         this.setDeltaMovement(Vec3.ZERO);
         this.setPosRaw(this.getX(), Mth.floor(this.getY()) + 1.0 - this.getBbHeight(), this.getZ());
      } else {
         this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0));
      }

      this.setupAnimationStates();
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      super.customServerAiStep($$0);
      BlockPos $$1 = this.blockPosition();
      BlockPos $$2 = $$1.above();
      if (this.isResting()) {
         boolean $$3 = this.isSilent();
         if ($$0.getBlockState($$2).isRedstoneConductor($$0, $$1)) {
            if (this.random.nextInt(200) == 0) {
               this.yHeadRot = this.random.nextInt(360);
            }

            if ($$0.getNearestPlayer(BAT_RESTING_TARGETING, this) != null) {
               this.setResting(false);
               if (!$$3) {
                  $$0.levelEvent(null, 1025, $$1, 0);
               }
            }
         } else {
            this.setResting(false);
            if (!$$3) {
               $$0.levelEvent(null, 1025, $$1, 0);
            }
         }
      } else {
         if (this.targetPosition != null && (!$$0.isEmptyBlock(this.targetPosition) || this.targetPosition.getY() <= $$0.getMinY())) {
            this.targetPosition = null;
         }

         if (this.targetPosition == null || this.random.nextInt(30) == 0 || this.targetPosition.closerToCenterThan(this.position(), 2.0)) {
            this.targetPosition = BlockPos.containing(
               this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
               this.getY() + this.random.nextInt(6) - 2.0,
               this.getZ() + this.random.nextInt(7) - this.random.nextInt(7)
            );
         }

         double $$4 = this.targetPosition.getX() + 0.5 - this.getX();
         double $$5 = this.targetPosition.getY() + 0.1 - this.getY();
         double $$6 = this.targetPosition.getZ() + 0.5 - this.getZ();
         Vec3 $$7 = this.getDeltaMovement();
         Vec3 $$8 = $$7.add((Math.signum($$4) * 0.5 - $$7.x) * 0.1F, (Math.signum($$5) * 0.7F - $$7.y) * 0.1F, (Math.signum($$6) * 0.5 - $$7.z) * 0.1F);
         this.setDeltaMovement($$8);
         float $$9 = (float)(Mth.atan2($$8.z, $$8.x) * 180.0F / (float)Math.PI) - 90.0F;
         float $$10 = Mth.wrapDegrees($$9 - this.getYRot());
         this.zza = 0.5F;
         this.setYRot(this.getYRot() + $$10);
         if (this.random.nextInt(100) == 0 && $$0.getBlockState($$2).isRedstoneConductor($$0, $$2)) {
            this.setResting(true);
         }
      }
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.EVENTS;
   }

   @Override
   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
   }

   @Override
   public boolean isIgnoringBlockTriggers() {
      return true;
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isInvulnerableTo($$0, $$1)) {
         return false;
      } else {
         if (this.isResting()) {
            this.setResting(false);
         }

         return super.hurtServer($$0, $$1, $$2);
      }
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.entityData.set(DATA_ID_FLAGS, $$0.getByteOr("BatFlags", (byte)0));
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putByte("BatFlags", (Byte)this.entityData.get(DATA_ID_FLAGS));
   }

   public static boolean checkBatSpawnRules(
      net.minecraft.world.entity.EntityType<Bat> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      if ($$3.getY() >= $$1.getHeightmapPos(Types.WORLD_SURFACE, $$3).getY()) {
         return false;
      } else if ($$4.nextBoolean()) {
         return false;
      } else if ($$1.getMaxLocalRawBrightness($$3) > $$4.nextInt(4)) {
         return false;
      } else {
         return !$$1.getBlockState($$3.below()).is(BlockTags.BATS_SPAWNABLE_ON) ? false : checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
      }
   }

   private void setupAnimationStates() {
      if (this.isResting()) {
         this.flyAnimationState.stop();
         this.restAnimationState.startIfStopped(this.tickCount);
      } else {
         this.restAnimationState.stop();
         this.flyAnimationState.startIfStopped(this.tickCount);
      }
   }
}
