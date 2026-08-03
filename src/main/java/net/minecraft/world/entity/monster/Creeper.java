package net.minecraft.world.entity.monster;

import java.util.Collection;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SwellGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class Creeper extends Monster {
   private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Boolean> DATA_IS_POWERED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(Creeper.class, EntityDataSerializers.BOOLEAN);
   private static final boolean DEFAULT_IGNITED = false;
   private static final boolean DEFAULT_POWERED = false;
   private static final short DEFAULT_MAX_SWELL = 30;
   private static final byte DEFAULT_EXPLOSION_RADIUS = 3;
   private int oldSwell;
   private int swell;
   private int maxSwell = 30;
   private int explosionRadius = 3;
   private boolean droppedSkulls;

   public Creeper(net.minecraft.world.entity.EntityType<? extends Creeper> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new SwellGoal(this));
      this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Ocelot.class, 6.0F, 1.0, 1.2));
      this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Cat.class, 6.0F, 1.0, 1.2));
      this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
      this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.25);
   }

   @Override
   public int getMaxFallDistance() {
      return this.getTarget() == null ? this.getComfortableFallDistance(0.0F) : this.getComfortableFallDistance(this.getHealth() - 1.0F);
   }

   @Override
   public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
      boolean $$3 = super.causeFallDamage($$0, $$1, $$2);
      this.swell += (int)($$0 * 1.5);
      if (this.swell > this.maxSwell - 5) {
         this.swell = this.maxSwell - 5;
      }

      return $$3;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_SWELL_DIR, -1);
      $$0.define(DATA_IS_POWERED, false);
      $$0.define(DATA_IS_IGNITED, false);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("powered", this.isPowered());
      $$0.putShort("Fuse", (short)this.maxSwell);
      $$0.putByte("ExplosionRadius", (byte)this.explosionRadius);
      $$0.putBoolean("ignited", this.isIgnited());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.entityData.set(DATA_IS_POWERED, $$0.getBooleanOr("powered", false));
      this.maxSwell = $$0.getShortOr("Fuse", (short)30);
      this.explosionRadius = $$0.getByteOr("ExplosionRadius", (byte)3);
      if ($$0.getBooleanOr("ignited", false)) {
         this.ignite();
      }
   }

   @Override
   public void tick() {
      if (this.isAlive()) {
         this.oldSwell = this.swell;
         if (this.isIgnited()) {
            this.setSwellDir(1);
         }

         int $$0 = this.getSwellDir();
         if ($$0 > 0 && this.swell == 0) {
            this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            this.gameEvent(GameEvent.PRIME_FUSE);
         }

         this.swell += $$0;
         if (this.swell < 0) {
            this.swell = 0;
         }

         if (this.swell >= this.maxSwell) {
            this.swell = this.maxSwell;
            this.explodeCreeper();
         }
      }

      super.tick();
   }

   @Override
   public void setTarget(@Nullable net.minecraft.world.entity.LivingEntity $$0) {
      if (!($$0 instanceof Goat)) {
         super.setTarget($$0);
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.CREEPER_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.CREEPER_DEATH;
   }

   @Override
   public boolean killedEntity(ServerLevel $$0, net.minecraft.world.entity.LivingEntity $$1, DamageSource $$2) {
      if (this.shouldDropLoot($$0) && this.isPowered() && !this.droppedSkulls) {
         $$1.dropFromLootTable($$0, $$2, false, BuiltInLootTables.CHARGED_CREEPER, $$2x -> {
            $$1.spawnAtLocation($$0, $$2x);
            this.droppedSkulls = true;
         });
      }

      return super.killedEntity($$0, $$1, $$2);
   }

   @Override
   public boolean doHurtTarget(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      return true;
   }

   public boolean isPowered() {
      return (Boolean)this.entityData.get(DATA_IS_POWERED);
   }

   public float getSwelling(float $$0) {
      return Mth.lerp($$0, this.oldSwell, this.swell) / (this.maxSwell - 2);
   }

   public int getSwellDir() {
      return (Integer)this.entityData.get(DATA_SWELL_DIR);
   }

   public void setSwellDir(int $$0) {
      this.entityData.set(DATA_SWELL_DIR, $$0);
   }

   @Override
   public void thunderHit(ServerLevel $$0, net.minecraft.world.entity.LightningBolt $$1) {
      super.thunderHit($$0, $$1);
      this.entityData.set(DATA_IS_POWERED, true);
   }

   @Override
   protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(ItemTags.CREEPER_IGNITERS)) {
         SoundEvent $$3 = $$2.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
         this.level().playSound($$0, this.getX(), this.getY(), this.getZ(), $$3, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
         if (!this.level().isClientSide()) {
            this.ignite();
            if (!$$2.isDamageableItem()) {
               $$2.shrink(1);
            } else {
               $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
            }
         }

         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   private void explodeCreeper() {
      if (this.level() instanceof ServerLevel $$0) {
         float $$1 = this.isPowered() ? 2.0F : 1.0F;
         this.dead = true;
         $$0.explode(this, this.getX(), this.getY(), this.getZ(), this.explosionRadius * $$1, ExplosionInteraction.MOB);
         this.spawnLingeringCloud();
         this.triggerOnDeathMobEffects($$0, net.minecraft.world.entity.Entity.RemovalReason.KILLED);
         this.discard();
      }
   }

   private void spawnLingeringCloud() {
      Collection<MobEffectInstance> $$0 = this.getActiveEffects();
      if (!$$0.isEmpty()) {
         net.minecraft.world.entity.AreaEffectCloud $$1 = new net.minecraft.world.entity.AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
         $$1.setRadius(2.5F);
         $$1.setRadiusOnUse(-0.5F);
         $$1.setWaitTime(10);
         $$1.setDuration(300);
         $$1.setPotionDurationScale(0.25F);
         $$1.setRadiusPerTick(-$$1.getRadius() / $$1.getDuration());

         for (MobEffectInstance $$2 : $$0) {
            $$1.addEffect(new MobEffectInstance($$2));
         }

         this.level().addFreshEntity($$1);
      }
   }

   public boolean isIgnited() {
      return (Boolean)this.entityData.get(DATA_IS_IGNITED);
   }

   public void ignite() {
      this.entityData.set(DATA_IS_IGNITED, true);
   }
}
