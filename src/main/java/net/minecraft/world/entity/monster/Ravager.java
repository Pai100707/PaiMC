package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Ravager extends Raider {
   private static final Predicate<net.minecraft.world.entity.Entity> ROAR_TARGET_WITH_GRIEFING = $$0 -> !($$0 instanceof Ravager) && $$0.isAlive();
   private static final Predicate<net.minecraft.world.entity.Entity> ROAR_TARGET_WITHOUT_GRIEFING = $$0 -> ROAR_TARGET_WITH_GRIEFING.test($$0)
      && !$$0.getType().equals(net.minecraft.world.entity.EntityType.ARMOR_STAND);
   private static final Predicate<net.minecraft.world.entity.LivingEntity> ROAR_TARGET_ON_CLIENT = $$0 -> !($$0 instanceof Ravager)
      && $$0.isAlive()
      && $$0.isLocalInstanceAuthoritative();
   private static final double BASE_MOVEMENT_SPEED = 0.3;
   private static final double ATTACK_MOVEMENT_SPEED = 0.35;
   private static final int STUNNED_COLOR = 8356754;
   private static final float STUNNED_COLOR_BLUE = 0.57254905F;
   private static final float STUNNED_COLOR_GREEN = 0.5137255F;
   private static final float STUNNED_COLOR_RED = 0.49803922F;
   public static final int ATTACK_DURATION = 10;
   public static final int STUN_DURATION = 40;
   private static final int DEFAULT_ATTACK_TICK = 0;
   private static final int DEFAULT_STUN_TICK = 0;
   private static final int DEFAULT_ROAR_TICK = 0;
   private int attackTick = 0;
   private int stunnedTick = 0;
   private int roarTick = 0;

   public Ravager(net.minecraft.world.entity.EntityType<? extends Ravager> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 20;
      this.setPathfindingMalus(PathType.LEAVES, 0.0F);
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, true));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true, ($$0, $$1) -> !$$0.isBaby()));
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
   }

   @Override
   protected void updateControlFlags() {
      boolean $$0 = !(this.getControllingPassenger() instanceof net.minecraft.world.entity.Mob)
         || this.getControllingPassenger().getType().is(EntityTypeTags.RAIDERS);
      boolean $$1 = !(this.getVehicle() instanceof AbstractBoat);
      this.goalSelector.setControlFlag(Goal.Flag.MOVE, $$0);
      this.goalSelector.setControlFlag(Goal.Flag.JUMP, $$0 && $$1);
      this.goalSelector.setControlFlag(Goal.Flag.LOOK, $$0);
      this.goalSelector.setControlFlag(Goal.Flag.TARGET, $$0);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes()
         .add(Attributes.MAX_HEALTH, 100.0)
         .add(Attributes.MOVEMENT_SPEED, 0.3)
         .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
         .add(Attributes.ATTACK_DAMAGE, 12.0)
         .add(Attributes.ATTACK_KNOCKBACK, 1.5)
         .add(Attributes.FOLLOW_RANGE, 32.0)
         .add(Attributes.STEP_HEIGHT, 1.0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("AttackTick", this.attackTick);
      $$0.putInt("StunTick", this.stunnedTick);
      $$0.putInt("RoarTick", this.roarTick);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.attackTick = $$0.getIntOr("AttackTick", 0);
      this.stunnedTick = $$0.getIntOr("StunTick", 0);
      this.roarTick = $$0.getIntOr("RoarTick", 0);
   }

   @Override
   public SoundEvent getCelebrateSound() {
      return SoundEvents.RAVAGER_CELEBRATE;
   }

   @Override
   public int getMaxHeadYRot() {
      return 45;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.isAlive()) {
         if (this.isImmobile()) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
         } else {
            double $$0 = this.getTarget() != null ? 0.35 : 0.3;
            double $$1 = this.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue();
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Mth.lerp(0.1, $$1, $$0));
         }

         if (this.level() instanceof ServerLevel $$2 && this.horizontalCollision && (Boolean)$$2.getGameRules().get(GameRules.MOB_GRIEFING)) {
            boolean $$3 = false;
            AABB $$4 = this.getBoundingBox().inflate(0.2);

            for (BlockPos $$5 : BlockPos.betweenClosed(
               Mth.floor($$4.minX), Mth.floor($$4.minY), Mth.floor($$4.minZ), Mth.floor($$4.maxX), Mth.floor($$4.maxY), Mth.floor($$4.maxZ)
            )) {
               BlockState $$6 = $$2.getBlockState($$5);
               Block $$7 = $$6.getBlock();
               if ($$7 instanceof LeavesBlock) {
                  $$3 = $$2.destroyBlock($$5, true, this) || $$3;
               }
            }

            if (!$$3 && this.onGround()) {
               this.jumpFromGround();
            }
         }

         if (this.roarTick > 0) {
            this.roarTick--;
            if (this.roarTick == 10) {
               this.roar();
            }
         }

         if (this.attackTick > 0) {
            this.attackTick--;
         }

         if (this.stunnedTick > 0) {
            this.stunnedTick--;
            this.stunEffect();
            if (this.stunnedTick == 0) {
               this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.0F);
               this.roarTick = 20;
            }
         }
      }
   }

   private void stunEffect() {
      if (this.random.nextInt(6) == 0) {
         double $$0 = this.getX() - this.getBbWidth() * Math.sin(this.yBodyRot * (float) (Math.PI / 180.0)) + (this.random.nextDouble() * 0.6 - 0.3);
         double $$1 = this.getY() + this.getBbHeight() - 0.3;
         double $$2 = this.getZ() + this.getBbWidth() * Math.cos(this.yBodyRot * (float) (Math.PI / 180.0)) + (this.random.nextDouble() * 0.6 - 0.3);
         this.level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0.49803922F, 0.5137255F, 0.57254905F), $$0, $$1, $$2, 0.0, 0.0, 0.0);
      }
   }

   @Override
   protected boolean isImmobile() {
      return super.isImmobile() || this.attackTick > 0 || this.stunnedTick > 0 || this.roarTick > 0;
   }

   @Override
   public boolean hasLineOfSight(net.minecraft.world.entity.Entity $$0) {
      return this.stunnedTick <= 0 && this.roarTick <= 0 ? super.hasLineOfSight($$0) : false;
   }

   @Override
   protected void blockedByItem(net.minecraft.world.entity.LivingEntity $$0) {
      if (this.roarTick == 0) {
         if (this.random.nextDouble() < 0.5) {
            this.stunnedTick = 40;
            this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0F, 1.0F);
            this.level().broadcastEntityEvent(this, (byte)39);
            $$0.push(this);
         } else {
            this.strongKnockback($$0);
         }

         $$0.hurtMarked = true;
      }
   }

   private void roar() {
      if (this.isAlive() && this.level() instanceof ServerLevel $$0) {
         Predicate<net.minecraft.world.entity.Entity> $$1 = $$0.getGameRules().get(GameRules.MOB_GRIEFING)
            ? ROAR_TARGET_WITH_GRIEFING
            : ROAR_TARGET_WITHOUT_GRIEFING;

         for (net.minecraft.world.entity.LivingEntity $$3 : this.level()
            .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(4.0), $$1)) {
            if (!($$3 instanceof AbstractIllager)) {
               $$3.hurtServer($$0, this.damageSources().mobAttack(this), 6.0F);
            }

            if (!($$3 instanceof Player)) {
               this.strongKnockback($$3);
            }
         }

         this.gameEvent(GameEvent.ENTITY_ACTION);
         $$0.broadcastEntityEvent(this, (byte)69);
      }
   }

   private void applyRoarKnockbackClient() {
      for (net.minecraft.world.entity.LivingEntity $$1 : this.level()
         .getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(4.0), ROAR_TARGET_ON_CLIENT)) {
         this.strongKnockback($$1);
      }
   }

   private void strongKnockback(net.minecraft.world.entity.Entity $$0) {
      double $$1 = $$0.getX() - this.getX();
      double $$2 = $$0.getZ() - this.getZ();
      double $$3 = Math.max($$1 * $$1 + $$2 * $$2, 0.001);
      $$0.push($$1 / $$3 * 4.0, 0.2, $$2 / $$3 * 4.0);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 4) {
         this.attackTick = 10;
         this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      } else if ($$0 == 39) {
         this.stunnedTick = 40;
      } else if ($$0 == 69) {
         this.addRoarParticleEffects();
         this.applyRoarKnockbackClient();
      }

      super.handleEntityEvent($$0);
   }

   private void addRoarParticleEffects() {
      Vec3 $$0 = this.getBoundingBox().getCenter();

      for (int $$1 = 0; $$1 < 40; $$1++) {
         double $$2 = this.random.nextGaussian() * 0.2;
         double $$3 = this.random.nextGaussian() * 0.2;
         double $$4 = this.random.nextGaussian() * 0.2;
         this.level().addParticle(ParticleTypes.POOF, $$0.x, $$0.y, $$0.z, $$2, $$3, $$4);
      }
   }

   public int getAttackTick() {
      return this.attackTick;
   }

   public int getStunnedTick() {
      return this.stunnedTick;
   }

   public int getRoarTick() {
      return this.roarTick;
   }

   @Override
   public boolean doHurtTarget(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      this.attackTick = 10;
      $$0.broadcastEntityEvent(this, (byte)4);
      this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
      return super.doHurtTarget($$0, $$1);
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.RAVAGER_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.RAVAGER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.RAVAGER_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.RAVAGER_STEP, 0.15F, 1.0F);
   }

   @Override
   public boolean checkSpawnObstruction(LevelReader $$0) {
      return !$$0.containsAnyLiquid(this.getBoundingBox());
   }

   @Override
   public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
   }

   @Override
   public boolean canBeLeader() {
      return false;
   }

   @Override
   protected AABB getAttackBoundingBox(double $$0) {
      AABB $$1 = super.getAttackBoundingBox($$0);
      return $$1.deflate(0.05, 0.0, 0.05);
   }
}
