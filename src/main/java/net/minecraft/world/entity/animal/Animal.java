package net.minecraft.world.entity.animal;

import com.google.common.collect.UnmodifiableIterator;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public abstract class Animal extends net.minecraft.world.entity.AgeableMob {
   protected static final int PARENT_AGE_AFTER_BREEDING = 6000;
   private static final int DEFAULT_IN_LOVE_TIME = 0;
   private int inLove = 0;
   
   private net.minecraft.world.entity.EntityReference<ServerPlayer> loveCause;

   protected Animal(net.minecraft.world.entity.EntityType<? extends Animal> $$0, Level $$1) {
      super($$0, $$1);
      this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
   }

   public static AttributeSupplier.Builder createAnimalAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.TEMPT_RANGE, 10.0);
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      super.customServerAiStep($$0);
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.getAge() != 0) {
         this.inLove = 0;
      }

      if (this.inLove > 0) {
         this.inLove--;
         if (this.inLove % 10 == 0) {
            double $$0 = this.random.nextGaussian() * 0.02;
            double $$1 = this.random.nextGaussian() * 0.02;
            double $$2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), $$0, $$1, $$2);
         }
      }
   }

   @Override
   protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
      this.resetLove();
      super.actuallyHurt($$0, $$1, $$2);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return $$1.getBlockState($$0.below()).is(Blocks.GRASS_BLOCK) ? 10.0F : $$1.getPathfindingCostFromLightLevels($$0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("InLove", this.inLove);
      net.minecraft.world.entity.EntityReference.store(this.loveCause, $$0, "LoveCause");
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.inLove = $$0.getIntOr("InLove", 0);
      this.loveCause = net.minecraft.world.entity.EntityReference.read($$0, "LoveCause");
   }

   public static boolean checkAnimalSpawnRules(
      net.minecraft.world.entity.EntityType<? extends Animal> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      boolean $$5 = net.minecraft.world.entity.EntitySpawnReason.ignoresLightRequirements($$2) || isBrightEnoughToSpawn($$1, $$3);
      return $$1.getBlockState($$3.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && $$5;
   }

   protected static boolean isBrightEnoughToSpawn(BlockAndTintGetter $$0, BlockPos $$1) {
      return $$0.getRawBrightness($$1, 0) > 8;
   }

   @Override
   public int getAmbientSoundInterval() {
      return 120;
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return false;
   }

   @Override
   protected int getBaseExperienceReward(ServerLevel $$0) {
      return 1 + this.random.nextInt(3);
   }

   public abstract boolean isFood(ItemStack var1);

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (this.isFood($$2)) {
         int $$3 = this.getAge();
         if ($$0 instanceof ServerPlayer $$4 && $$3 == 0 && this.canFallInLove()) {
            this.usePlayerItem($$0, $$1, $$2);
            this.setInLove($$4);
            this.playEatingSound();
            return InteractionResult.SUCCESS_SERVER;
         }

         if (this.isBaby()) {
            this.usePlayerItem($$0, $$1, $$2);
            this.ageUp(getSpeedUpSecondsWhenFeeding(-$$3), true);
            this.playEatingSound();
            return InteractionResult.SUCCESS;
         }

         if (this.level().isClientSide()) {
            return InteractionResult.CONSUME;
         }
      }

      return super.mobInteract($$0, $$1);
   }

   protected void playEatingSound() {
   }

   public boolean canFallInLove() {
      return this.inLove <= 0;
   }

   public void setInLove(Player $$0) {
      this.inLove = 600;
      if ($$0 instanceof ServerPlayer $$1) {
         this.loveCause = net.minecraft.world.entity.EntityReference.of($$1);
      }

      this.level().broadcastEntityEvent(this, (byte)18);
   }

   public void setInLoveTime(int $$0) {
      this.inLove = $$0;
   }

   public int getInLoveTime() {
      return this.inLove;
   }

   
   public ServerPlayer getLoveCause() {
      return net.minecraft.world.entity.EntityReference.get(this.loveCause, this.level(), ServerPlayer.class);
   }

   public boolean isInLove() {
      return this.inLove > 0;
   }

   public void resetLove() {
      this.inLove = 0;
   }

   public boolean canMate(Animal $$0) {
      if ($$0 == this) {
         return false;
      } else {
         return $$0.getClass() != this.getClass() ? false : this.isInLove() && $$0.isInLove();
      }
   }

   public void spawnChildFromBreeding(ServerLevel $$0, Animal $$1) {
      net.minecraft.world.entity.AgeableMob $$2 = this.getBreedOffspring($$0, $$1);
      if ($$2 != null) {
         $$2.setBaby(true);
         $$2.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
         this.finalizeSpawnChildFromBreeding($$0, $$1, $$2);
         $$0.addFreshEntityWithPassengers($$2);
      }
   }

   public void finalizeSpawnChildFromBreeding(ServerLevel $$0, Animal $$1, net.minecraft.world.entity.AgeableMob $$2) {
      Optional.ofNullable(this.getLoveCause()).or(() -> Optional.ofNullable($$1.getLoveCause())).ifPresent($$2x -> {
         $$2x.awardStat(Stats.ANIMALS_BRED);
         CriteriaTriggers.BRED_ANIMALS.trigger($$2x, this, $$1, $$2);
      });
      this.setAge(6000);
      $$1.setAge(6000);
      this.resetLove();
      $$1.resetLove();
      $$0.broadcastEntityEvent(this, (byte)18);
      if ((Boolean)$$0.getGameRules().get(GameRules.MOB_DROPS)) {
         $$0.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb($$0, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
      }
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 18) {
         for (int $$1 = 0; $$1 < 7; $$1++) {
            double $$2 = this.random.nextGaussian() * 0.02;
            double $$3 = this.random.nextGaussian() * 0.02;
            double $$4 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), $$2, $$3, $$4);
         }
      } else {
         super.handleEntityEvent($$0);
      }
   }

   @Override
   public Vec3 getDismountLocationForPassenger(net.minecraft.world.entity.LivingEntity $$0) {
      Direction $$1 = this.getMotionDirection();
      if ($$1.getAxis() == Axis.Y) {
         return super.getDismountLocationForPassenger($$0);
      } else {
         int[][] $$2 = DismountHelper.offsetsForDirection($$1);
         BlockPos $$3 = this.blockPosition();
         MutableBlockPos $$4 = new MutableBlockPos();
         UnmodifiableIterator var6 = $$0.getDismountPoses().iterator();

         while (var6.hasNext()) {
            net.minecraft.world.entity.Pose $$5 = (net.minecraft.world.entity.Pose)var6.next();
            AABB $$6 = $$0.getLocalBoundsForPose($$5);

            for (int[] $$7 : $$2) {
               $$4.set($$3.getX() + $$7[0], $$3.getY(), $$3.getZ() + $$7[1]);
               double $$8 = this.level().getBlockFloorHeight($$4);
               if (DismountHelper.isBlockFloorValid($$8)) {
                  Vec3 $$9 = Vec3.upFromBottomCenterOf($$4, $$8);
                  if (DismountHelper.canDismountTo(this.level(), $$0, $$6.move($$9))) {
                     $$0.setPose($$5);
                     return $$9;
                  }
               }
            }
         }

         return super.getDismountLocationForPassenger($$0);
      }
   }
}
