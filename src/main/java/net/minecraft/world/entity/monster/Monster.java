package net.minecraft.world.entity.monster;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;

public abstract class Monster extends net.minecraft.world.entity.PathfinderMob implements Enemy {
   protected Monster(net.minecraft.world.entity.EntityType<? extends Monster> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 5;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.HOSTILE;
   }

   @Override
   public void aiStep() {
      this.updateSwingTime();
      this.updateNoActionTime();
      super.aiStep();
   }

   protected void updateNoActionTime() {
      float $$0 = this.getLightLevelDependentMagicValue();
      if ($$0 > 0.5F) {
         this.noActionTime += 2;
      }
   }

   @Override
   protected SoundEvent getSwimSound() {
      return SoundEvents.HOSTILE_SWIM;
   }

   @Override
   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.HOSTILE_SPLASH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.HOSTILE_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.HOSTILE_DEATH;
   }

   @Override
   public net.minecraft.world.entity.LivingEntity.Fallsounds getFallSounds() {
      return new net.minecraft.world.entity.LivingEntity.Fallsounds(SoundEvents.HOSTILE_SMALL_FALL, SoundEvents.HOSTILE_BIG_FALL);
   }

   @Override
   public float getWalkTargetValue(BlockPos $$0, LevelReader $$1) {
      return -$$1.getPathfindingCostFromLightLevels($$0);
   }

   public static boolean isDarkEnoughToSpawn(ServerLevelAccessor $$0, BlockPos $$1, RandomSource $$2) {
      if ($$0.getBrightness(LightLayer.SKY, $$1) > $$2.nextInt(32)) {
         return false;
      } else {
         DimensionType $$3 = $$0.dimensionType();
         int $$4 = $$3.monsterSpawnBlockLightLimit();
         if ($$4 < 15 && $$0.getBrightness(LightLayer.BLOCK, $$1) > $$4) {
            return false;
         } else {
            int $$5 = $$0.getLevel().isThundering() ? $$0.getMaxLocalRawBrightness($$1, 10) : $$0.getMaxLocalRawBrightness($$1);
            return $$5 <= $$3.monsterSpawnLightTest().sample($$2);
         }
      }
   }

   public static boolean checkMonsterSpawnRules(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> $$0,
      ServerLevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getDifficulty() != Difficulty.PEACEFUL
         && (net.minecraft.world.entity.EntitySpawnReason.ignoresLightRequirements($$2) || isDarkEnoughToSpawn($$1, $$3, $$4))
         && checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
   }

   public static boolean checkAnyLightMonsterSpawnRules(
      net.minecraft.world.entity.EntityType<? extends Monster> $$0,
      LevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return $$1.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules($$0, $$1, $$2, $$3, $$4);
   }

   public static boolean checkSurfaceMonstersSpawnRules(
      net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> $$0,
      ServerLevelAccessor $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      BlockPos $$3,
      RandomSource $$4
   ) {
      return checkMonsterSpawnRules($$0, $$1, $$2, $$3, $$4) && (net.minecraft.world.entity.EntitySpawnReason.isSpawner($$2) || $$1.canSeeSky($$3));
   }

   public static AttributeSupplier.Builder createMonsterAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
   }

   @Override
   public boolean shouldDropExperience() {
      return true;
   }

   @Override
   protected boolean shouldDropLoot(ServerLevel $$0) {
      return (Boolean)$$0.getGameRules().get(GameRules.MOB_DROPS);
   }

   public boolean isPreventingPlayerRest(ServerLevel $$0, Player $$1) {
      return true;
   }

   @Override
   public ItemStack getProjectile(ItemStack $$0) {
      if ($$0.getItem() instanceof ProjectileWeaponItem) {
         Predicate<ItemStack> $$1 = ((ProjectileWeaponItem)$$0.getItem()).getSupportedHeldProjectiles();
         ItemStack $$2 = ProjectileWeaponItem.getHeldProjectile(this, $$1);
         return $$2.isEmpty() ? new ItemStack(Items.ARROW) : $$2;
      } else {
         return ItemStack.EMPTY;
      }
   }
}
