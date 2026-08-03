package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class Slime extends net.minecraft.world.entity.Mob implements Enemy {
   private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Slime.class, EntityDataSerializers.INT);
   public static final int MIN_SIZE = 1;
   public static final int MAX_SIZE = 127;
   public static final int MAX_NATURAL_SIZE = 4;
   private static final boolean DEFAULT_WAS_ON_GROUND = false;
   public float targetSquish;
   public float squish;
   public float oSquish;
   private boolean wasOnGround = false;

   public Slime(net.minecraft.world.entity.EntityType<? extends Slime> $$0, Level $$1) {
      super($$0, $$1);
      this.fixupDimensions();
      this.moveControl = new Slime.SlimeMoveControl(this);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new Slime.SlimeFloatGoal(this));
      this.goalSelector.addGoal(2, new Slime.SlimeAttackGoal(this));
      this.goalSelector.addGoal(3, new Slime.SlimeRandomDirectionGoal(this));
      this.goalSelector.addGoal(5, new Slime.SlimeKeepOnJumpingGoal(this));
      this.targetSelector
         .addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, ($$0, $$1) -> Math.abs($$0.getY() - this.getY()) <= 4.0));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(ID_SIZE, 1);
   }

   @VisibleForTesting
   public void setSize(int $$0, boolean $$1) {
      int $$2 = Mth.clamp($$0, 1, 127);
      this.entityData.set(ID_SIZE, $$2);
      this.reapplyPosition();
      this.refreshDimensions();
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue($$2 * $$2);
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.2F + 0.1F * $$2);
      this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue($$2);
      if ($$1) {
         this.setHealth(this.getMaxHealth());
      }

      this.xpReward = $$2;
   }

   public int getSize() {
      return (Integer)this.entityData.get(ID_SIZE);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("Size", this.getSize() - 1);
      $$0.putBoolean("wasOnGround", this.wasOnGround);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setSize($$0.getIntOr("Size", 0) + 1, false);
      super.readAdditionalSaveData($$0);
      this.wasOnGround = $$0.getBooleanOr("wasOnGround", false);
   }

   public boolean isTiny() {
      return this.getSize() <= 1;
   }

   protected ParticleOptions getParticleType() {
      return ParticleTypes.ITEM_SLIME;
   }

   @Override
   public void tick() {
      this.oSquish = this.squish;
      this.squish = this.squish + (this.targetSquish - this.squish) * 0.5F;
      super.tick();
      if (this.onGround() && !this.wasOnGround) {
         float $$0 = this.getDimensions(this.getPose()).width() * 2.0F;
         float $$1 = $$0 / 2.0F;

         for (int $$2 = 0; $$2 < $$0 * 16.0F; $$2++) {
            float $$3 = this.random.nextFloat() * (float) (Math.PI * 2);
            float $$4 = this.random.nextFloat() * 0.5F + 0.5F;
            float $$5 = Mth.sin($$3) * $$1 * $$4;
            float $$6 = Mth.cos($$3) * $$1 * $$4;
            this.level().addParticle(this.getParticleType(), this.getX() + $$5, this.getY(), this.getZ() + $$6, 0.0, 0.0, 0.0);
         }

         this.playSound(this.getSquishSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.targetSquish = -0.5F;
      } else if (!this.onGround() && this.wasOnGround) {
         this.targetSquish = 1.0F;
      }

      this.wasOnGround = this.onGround();
      this.decreaseSquish();
   }

   protected void decreaseSquish() {
      this.targetSquish *= 0.6F;
   }

   protected int getJumpDelay() {
      return this.random.nextInt(20) + 10;
   }

   @Override
   public void refreshDimensions() {
      double $$0 = this.getX();
      double $$1 = this.getY();
      double $$2 = this.getZ();
      super.refreshDimensions();
      this.setPos($$0, $$1, $$2);
   }

   @Override
   public void onSyncedDataUpdated(EntityDataAccessor<?> $$0) {
      if (ID_SIZE.equals($$0)) {
         this.refreshDimensions();
         this.setYRot(this.yHeadRot);
         this.yBodyRot = this.yHeadRot;
         if (this.isInWater() && this.random.nextInt(20) == 0) {
            this.doWaterSplashEffect();
         }
      }

      super.onSyncedDataUpdated($$0);
   }

   @Override
   public net.minecraft.world.entity.EntityType<? extends Slime> getType() {
      return (net.minecraft.world.entity.EntityType<? extends Slime>)super.getType();
   }

   @Override
   public void remove(net.minecraft.world.entity.Entity.RemovalReason $$0) {
      int $$1 = this.getSize();
      if (!this.level().isClientSide() && $$1 > 1 && this.isDeadOrDying()) {
         float $$2 = this.getDimensions(this.getPose()).width();
         float $$3 = $$2 / 2.0F;
         int $$4 = $$1 / 2;
         int $$5 = 2 + this.random.nextInt(3);
         PlayerTeam $$6 = this.getTeam();

         for (int $$7 = 0; $$7 < $$5; $$7++) {
            float $$8 = ($$7 % 2 - 0.5F) * $$3;
            float $$9 = ($$7 / 2 - 0.5F) * $$3;
            this.convertTo(
               this.getType(),
               new net.minecraft.world.entity.ConversionParams(net.minecraft.world.entity.ConversionType.SPLIT_ON_DEATH, false, false, $$6),
               net.minecraft.world.entity.EntitySpawnReason.TRIGGERED,
               $$3x -> {
                  $$3x.setSize($$4, true);
                  $$3x.snapTo(this.getX() + $$8, this.getY() + 0.5, this.getZ() + $$9, this.random.nextFloat() * 360.0F, 0.0F);
               }
            );
         }
      }

      super.remove($$0);
   }

   @Override
   public void push(net.minecraft.world.entity.Entity $$0) {
      super.push($$0);
      if ($$0 instanceof IronGolem && this.isDealsDamage()) {
         this.dealDamage((net.minecraft.world.entity.LivingEntity)$$0);
      }
   }

   @Override
   public void playerTouch(Player $$0) {
      if (this.isDealsDamage()) {
         this.dealDamage($$0);
      }
   }

   protected void dealDamage(net.minecraft.world.entity.LivingEntity $$0) {
      if (this.level() instanceof ServerLevel $$1 && this.isAlive() && this.isWithinMeleeAttackRange($$0) && this.hasLineOfSight($$0)) {
         DamageSource $$2 = this.damageSources().mobAttack(this);
         if ($$0.hurtServer($$1, $$2, this.getAttackDamage())) {
            this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            EnchantmentHelper.doPostAttackEffects($$1, $$0, $$2);
         }
      }
   }

   @Override
   protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.EntityDimensions $$1, float $$2) {
      return new Vec3(0.0, $$1.height() - 0.015625 * this.getSize() * $$2, 0.0);
   }

   protected boolean isDealsDamage() {
      return !this.isTiny() && this.isEffectiveAi();
   }

   protected float getAttackDamage() {
      return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return this.isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return this.isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
   }

   protected SoundEvent getSquishSound() {
      return this.isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
   }

   public static boolean checkSlimeSpawnRules(
      net.minecraft.world.entity.EntityType<Slime> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      if ($$1.getDifficulty() != Difficulty.PEACEFUL) {
         if (net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2)) {
            return checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
         }

         if ($$1.getBiome($$3).is(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS) && $$3.getY() > 50 && $$3.getY() < 70) {
            float $$5 = (Float)$$1.environmentAttributes().getValue(EnvironmentAttributes.SURFACE_SLIME_SPAWN_CHANCE, $$3);
            if ($$4.nextFloat() < $$5 && $$1.getMaxLocalRawBrightness($$3) <= $$4.nextInt(8)) {
               return checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
            }
         }

         if (!($$1 instanceof WorldGenLevel)) {
            return false;
         }

         ChunkPos $$6 = new ChunkPos($$3);
         boolean $$7 = WorldgenRandom.seedSlimeChunk($$6.x, $$6.z, ((WorldGenLevel)$$1).getSeed(), 987234911L).nextInt(10) == 0;
         if ($$4.nextInt(10) == 0 && $$7 && $$3.getY() < 40) {
            return checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
         }
      }

      return false;
   }

   @Override
   protected float getSoundVolume() {
      return 0.4F * this.getSize();
   }

   @Override
   public int getMaxHeadXRot() {
      return 0;
   }

   protected boolean doPlayJumpSound() {
      return this.getSize() > 0;
   }

   @Override
   public void jumpFromGround() {
      Vec3 $$0 = this.getDeltaMovement();
      this.setDeltaMovement($$0.x, this.getJumpPower(), $$0.z);
      this.needsSync = true;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      RandomSource $$4 = $$0.getRandom();
      int $$5 = $$4.nextInt(3);
      if ($$5 < 2 && $$4.nextFloat() < 0.5F * $$1.getSpecialMultiplier()) {
         $$5++;
      }

      int $$6 = 1 << $$5;
      this.setSize($$6, true);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   float getSoundPitch() {
      float $$0 = this.isTiny() ? 1.4F : 0.8F;
      return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * $$0;
   }

   protected SoundEvent getJumpSound() {
      return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return super.getDefaultDimensions($$0).scale(this.getSize());
   }

   static class SlimeAttackGoal extends Goal {
      private final Slime slime;
      private int growTiredTimer;

      public SlimeAttackGoal(Slime $$0) {
         this.slime = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         net.minecraft.world.entity.LivingEntity $$0 = this.slime.getTarget();
         if ($$0 == null) {
            return false;
         } else {
            return !this.slime.canAttack($$0) ? false : this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
         }
      }

      @Override
      public void start() {
         this.growTiredTimer = reducedTickDelay(300);
         super.start();
      }

      @Override
      public boolean canContinueToUse() {
         net.minecraft.world.entity.LivingEntity $$0 = this.slime.getTarget();
         if ($$0 == null) {
            return false;
         } else {
            return !this.slime.canAttack($$0) ? false : --this.growTiredTimer > 0;
         }
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         net.minecraft.world.entity.LivingEntity $$0 = this.slime.getTarget();
         if ($$0 != null) {
            this.slime.lookAt($$0, 10.0F, 10.0F);
         }

         if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl $$1) {
            $$1.setDirection(this.slime.getYRot(), this.slime.isDealsDamage());
         }
      }
   }

   static class SlimeFloatGoal extends Goal {
      private final Slime slime;

      public SlimeFloatGoal(Slime $$0) {
         this.slime = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
         $$0.getNavigation().setCanFloat(true);
      }

      @Override
      public boolean canUse() {
         return (this.slime.isInWater() || this.slime.isInLava()) && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
      }

      @Override
      public boolean requiresUpdateEveryTick() {
         return true;
      }

      @Override
      public void tick() {
         if (this.slime.getRandom().nextFloat() < 0.8F) {
            this.slime.getJumpControl().jump();
         }

         if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl $$0) {
            $$0.setWantedMovement(1.2);
         }
      }
   }

   static class SlimeKeepOnJumpingGoal extends Goal {
      private final Slime slime;

      public SlimeKeepOnJumpingGoal(Slime $$0) {
         this.slime = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         return !this.slime.isPassenger();
      }

      @Override
      public void tick() {
         if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl $$0) {
            $$0.setWantedMovement(1.0);
         }
      }
   }

   static class SlimeMoveControl extends MoveControl {
      private float yRot;
      private int jumpDelay;
      private final Slime slime;
      private boolean isAggressive;

      public SlimeMoveControl(Slime $$0) {
         super($$0);
         this.slime = $$0;
         this.yRot = 180.0F * $$0.getYRot() / (float) Math.PI;
      }

      public void setDirection(float $$0, boolean $$1) {
         this.yRot = $$0;
         this.isAggressive = $$1;
      }

      public void setWantedMovement(double $$0) {
         this.speedModifier = $$0;
         this.operation = MoveControl.Operation.MOVE_TO;
      }

      @Override
      public void tick() {
         this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0F));
         this.mob.yHeadRot = this.mob.getYRot();
         this.mob.yBodyRot = this.mob.getYRot();
         if (this.operation != MoveControl.Operation.MOVE_TO) {
            this.mob.setZza(0.0F);
         } else {
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.onGround()) {
               this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
               if (this.jumpDelay-- <= 0) {
                  this.jumpDelay = this.slime.getJumpDelay();
                  if (this.isAggressive) {
                     this.jumpDelay /= 3;
                  }

                  this.slime.getJumpControl().jump();
                  if (this.slime.doPlayJumpSound()) {
                     this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(), this.slime.getSoundPitch());
                  }
               } else {
                  this.slime.xxa = 0.0F;
                  this.slime.zza = 0.0F;
                  this.mob.setSpeed(0.0F);
               }
            } else {
               this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
         }
      }
   }

   static class SlimeRandomDirectionGoal extends Goal {
      private final Slime slime;
      private float chosenDegrees;
      private int nextRandomizeTime;

      public SlimeRandomDirectionGoal(Slime $$0) {
         this.slime = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         return this.slime.getTarget() == null
            && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava() || this.slime.hasEffect(MobEffects.LEVITATION))
            && this.slime.getMoveControl() instanceof Slime.SlimeMoveControl;
      }

      @Override
      public void tick() {
         if (--this.nextRandomizeTime <= 0) {
            this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
            this.chosenDegrees = this.slime.getRandom().nextInt(360);
         }

         if (this.slime.getMoveControl() instanceof Slime.SlimeMoveControl $$0) {
            $$0.setDirection(this.chosenDegrees, false);
         }
      }
   }
}
