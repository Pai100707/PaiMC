package net.minecraft.world.entity.animal.wolf;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Wolf extends net.minecraft.world.entity.TamableAnimal implements net.minecraft.world.entity.NeutralMob {
   private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Long> DATA_ANGER_END_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.LONG);
   private static final EntityDataAccessor<Holder<WolfVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_VARIANT);
   private static final EntityDataAccessor<Holder<WolfSoundVariant>> DATA_SOUND_VARIANT_ID = SynchedEntityData.defineId(
      Wolf.class, EntityDataSerializers.WOLF_SOUND_VARIANT
   );
   public static final TargetingConditions.Selector PREY_SELECTOR = ($$0, $$1) -> {
      net.minecraft.world.entity.EntityType<?> $$2 = $$0.getType();
      return $$2 == net.minecraft.world.entity.EntityType.SHEEP
         || $$2 == net.minecraft.world.entity.EntityType.RABBIT
         || $$2 == net.minecraft.world.entity.EntityType.FOX;
   };
   private static final float START_HEALTH = 8.0F;
   private static final float TAME_HEALTH = 40.0F;
   private static final float ARMOR_REPAIR_UNIT = 0.125F;
   public static final float DEFAULT_TAIL_ANGLE = (float) (Math.PI / 5);
   private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
   private float interestedAngle;
   private float interestedAngleO;
   private boolean isWet;
   private boolean isShaking;
   private float shakeAnim;
   private float shakeAnimO;
   private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
   @Nullable
   private net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> persistentAngerTarget;

   public Wolf(net.minecraft.world.entity.EntityType<? extends Wolf> $$0, Level $$1) {
      super($$0, $$1);
      this.setTame(false, false);
      this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0F);
      this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0F);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new net.minecraft.world.entity.TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(3, new Wolf.WolfAvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5, 1.5));
      this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4F));
      this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
      this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
      this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
      this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
      this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
      this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
      this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
      this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
      this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
      this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
   }

   public Identifier getTexture() {
      WolfVariant $$0 = (WolfVariant)this.getVariant().value();
      if (this.isTame()) {
         return $$0.assetInfo().tame().texturePath();
      } else {
         return this.isAngry() ? $$0.assetInfo().angry().texturePath() : $$0.assetInfo().wild().texturePath();
      }
   }

   private Holder<WolfVariant> getVariant() {
      return (Holder<WolfVariant>)this.entityData.get(DATA_VARIANT_ID);
   }

   private void setVariant(Holder<WolfVariant> $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0);
   }

   private Holder<WolfSoundVariant> getSoundVariant() {
      return (Holder<WolfSoundVariant>)this.entityData.get(DATA_SOUND_VARIANT_ID);
   }

   private void setSoundVariant(Holder<WolfSoundVariant> $$0) {
      this.entityData.set(DATA_SOUND_VARIANT_ID, $$0);
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      if ($$0 == DataComponents.WOLF_VARIANT) {
         return castComponentValue((DataComponentType<T>)$$0, this.getVariant());
      } else if ($$0 == DataComponents.WOLF_SOUND_VARIANT) {
         return castComponentValue((DataComponentType<T>)$$0, this.getSoundVariant());
      } else {
         return $$0 == DataComponents.WOLF_COLLAR ? castComponentValue((DataComponentType<T>)$$0, this.getCollarColor()) : super.get($$0);
      }
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_VARIANT);
      this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_SOUND_VARIANT);
      this.applyImplicitComponentIfPresent($$0, DataComponents.WOLF_COLLAR);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.WOLF_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.WOLF_VARIANT, $$1));
         return true;
      } else if ($$0 == DataComponents.WOLF_SOUND_VARIANT) {
         this.setSoundVariant(castComponentValue(DataComponents.WOLF_SOUND_VARIANT, $$1));
         return true;
      } else if ($$0 == DataComponents.WOLF_COLLAR) {
         this.setCollarColor(castComponentValue(DataComponents.WOLF_COLLAR, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 4.0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      Registry<WolfSoundVariant> $$1 = this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT);
      $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), WolfVariants.DEFAULT));
      $$0.define(DATA_SOUND_VARIANT_ID, (Holder)$$1.get(WolfSoundVariants.CLASSIC).or($$1::getAny).orElseThrow());
      $$0.define(DATA_INTERESTED_ID, false);
      $$0.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
      $$0.define(DATA_ANGER_END_TIME, -1L);
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.WOLF_STEP, 0.15F, 1.0F);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("CollarColor", DyeColor.LEGACY_ID_CODEC, this.getCollarColor());
      VariantUtils.writeVariant($$0, this.getVariant());
      this.addPersistentAngerSaveData($$0);
      this.getSoundVariant().unwrapKey().ifPresent($$1 -> $$0.store("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT), $$1));
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      VariantUtils.<WolfVariant>readVariant($$0, Registries.WOLF_VARIANT).ifPresent(this::setVariant);
      this.setCollarColor($$0.read("CollarColor", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLLAR_COLOR));
      this.readPersistentAngerSaveData(this.level(), $$0);
      $$0.read("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT))
         .flatMap($$0x -> this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT).get($$0x))
         .ifPresent(this::setSoundVariant);
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      @Nullable net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      if ($$3 instanceof Wolf.WolfPackData $$4) {
         this.setVariant($$4.type);
      } else {
         Optional<? extends Holder<WolfVariant>> $$5 = VariantUtils.selectVariantToSpawn(
            SpawnContext.create($$0, this.blockPosition()), Registries.WOLF_VARIANT
         );
         if ($$5.isPresent()) {
            this.setVariant((Holder<WolfVariant>)$$5.get());
            $$3 = new Wolf.WolfPackData((Holder<WolfVariant>)$$5.get());
         }
      }

      this.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), $$0.getRandom()));
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   @Override
   protected SoundEvent getAmbientSound() {
      if (this.isAngry()) {
         return (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).growlSound().value();
      } else if (this.random.nextInt(3) == 0) {
         return this.isTame() && this.getHealth() < 20.0F
            ? (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).whineSound().value()
            : (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).pantSound().value();
      } else {
         return (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).ambientSound().value();
      }
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return this.canArmorAbsorb($$0) ? SoundEvents.WOLF_ARMOR_DAMAGE : (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).hurtSound().value();
   }

   @Override
   protected SoundEvent getDeathSound() {
      return (SoundEvent)((WolfSoundVariant)this.getSoundVariant().value()).deathSound().value();
   }

   @Override
   protected float getSoundVolume() {
      return 0.4F;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (!this.level().isClientSide() && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
         this.isShaking = true;
         this.shakeAnim = 0.0F;
         this.shakeAnimO = 0.0F;
         this.level().broadcastEntityEvent(this, (byte)8);
      }

      if (!this.level().isClientSide()) {
         this.updatePersistentAnger((ServerLevel)this.level(), true);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isAlive()) {
         this.interestedAngleO = this.interestedAngle;
         if (this.isInterested()) {
            this.interestedAngle = this.interestedAngle + (1.0F - this.interestedAngle) * 0.4F;
         } else {
            this.interestedAngle = this.interestedAngle + (0.0F - this.interestedAngle) * 0.4F;
         }

         if (this.isInWaterOrRain()) {
            this.isWet = true;
            if (this.isShaking && !this.level().isClientSide()) {
               this.level().broadcastEntityEvent(this, (byte)56);
               this.cancelShake();
            }
         } else if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0F) {
               this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
               this.gameEvent(GameEvent.ENTITY_ACTION);
            }

            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05F;
            if (this.shakeAnimO >= 2.0F) {
               this.isWet = false;
               this.isShaking = false;
               this.shakeAnimO = 0.0F;
               this.shakeAnim = 0.0F;
            }

            if (this.shakeAnim > 0.4F) {
               float $$0 = (float)this.getY();
               int $$1 = (int)(Mth.sin((this.shakeAnim - 0.4F) * (float) Math.PI) * 7.0F);
               Vec3 $$2 = this.getDeltaMovement();

               for (int $$3 = 0; $$3 < $$1; $$3++) {
                  float $$4 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                  float $$5 = (this.random.nextFloat() * 2.0F - 1.0F) * this.getBbWidth() * 0.5F;
                  this.level().addParticle(ParticleTypes.SPLASH, this.getX() + $$4, $$0 + 0.8F, this.getZ() + $$5, $$2.x, $$2.y, $$2.z);
               }
            }
         }
      }
   }

   private void cancelShake() {
      this.isShaking = false;
      this.shakeAnim = 0.0F;
      this.shakeAnimO = 0.0F;
   }

   @Override
   public void die(DamageSource $$0) {
      this.isWet = false;
      this.isShaking = false;
      this.shakeAnimO = 0.0F;
      this.shakeAnim = 0.0F;
      super.die($$0);
   }

   public float getWetShade(float $$0) {
      return !this.isWet ? 1.0F : Math.min(0.75F + Mth.lerp($$0, this.shakeAnimO, this.shakeAnim) / 2.0F * 0.25F, 1.0F);
   }

   public float getShakeAnim(float $$0) {
      return Mth.lerp($$0, this.shakeAnimO, this.shakeAnim);
   }

   public float getHeadRollAngle(float $$0) {
      return Mth.lerp($$0, this.interestedAngleO, this.interestedAngle) * 0.15F * (float) Math.PI;
   }

   @Override
   public int getMaxHeadXRot() {
      return this.isInSittingPose() ? 20 : super.getMaxHeadXRot();
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isInvulnerableTo($$0, $$1)) {
         return false;
      } else {
         this.setOrderedToSit(false);
         return super.hurtServer($$0, $$1, $$2);
      }
   }

   @Override
   protected void actuallyHurt(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (!this.canArmorAbsorb($$1)) {
         super.actuallyHurt($$0, $$1, $$2);
      } else {
         ItemStack $$3 = this.getBodyArmorItem();
         int $$4 = $$3.getDamageValue();
         int $$5 = $$3.getMaxDamage();
         $$3.hurtAndBreak(Mth.ceil($$2), this, net.minecraft.world.entity.EquipmentSlot.BODY);
         if (net.minecraft.world.entity.Crackiness.WOLF_ARMOR.byDamage($$4, $$5)
            != net.minecraft.world.entity.Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
            this.playSound(SoundEvents.WOLF_ARMOR_CRACK);
            $$0.sendParticles(
               new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()),
               this.getX(),
               this.getY() + 1.0,
               this.getZ(),
               20,
               0.2,
               0.1,
               0.2,
               0.1
            );
         }
      }
   }

   private boolean canArmorAbsorb(DamageSource $$0) {
      return this.getBodyArmorItem().is(Items.WOLF_ARMOR) && !$$0.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
   }

   @Override
   protected void applyTamingSideEffects() {
      if (this.isTame()) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
         this.setHealth(40.0F);
      } else {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0);
      }
   }

   @Override
   protected void hurtArmor(DamageSource $$0, float $$1) {
      this.doHurtEquipment($$0, $$1, new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.BODY});
   }

   @Override
   protected boolean canShearEquipment(Player $$0) {
      return this.isOwnedBy($$0);
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      Item $$3 = $$2.getItem();
      if (this.isTame()) {
         if (this.isFood($$2) && this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem($$0, $$1, $$2);
            FoodProperties $$4 = (FoodProperties)$$2.get(DataComponents.FOOD);
            float $$5 = $$4 != null ? $$4.nutrition() : 1.0F;
            this.heal(2.0F * $$5);
            return InteractionResult.SUCCESS;
         }

         if (!($$3 instanceof DyeItem $$6 && this.isOwnedBy($$0))) {
            if (this.isEquippableInSlot($$2, net.minecraft.world.entity.EquipmentSlot.BODY)
               && !this.isWearingBodyArmor()
               && this.isOwnedBy($$0)
               && !this.isBaby()) {
               this.setBodyArmorItem($$2.copyWithCount(1));
               $$2.consume(1, $$0);
               return InteractionResult.SUCCESS;
            }

            if (this.isInSittingPose()
               && this.isWearingBodyArmor()
               && this.isOwnedBy($$0)
               && this.getBodyArmorItem().isDamaged()
               && this.getBodyArmorItem().isValidRepairItem($$2)) {
               $$2.shrink(1);
               this.playSound(SoundEvents.WOLF_ARMOR_REPAIR);
               ItemStack $$8 = this.getBodyArmorItem();
               int $$9 = (int)($$8.getMaxDamage() * 0.125F);
               $$8.setDamageValue(Math.max(0, $$8.getDamageValue() - $$9));
               return InteractionResult.SUCCESS;
            }

            InteractionResult $$10 = super.mobInteract($$0, $$1);
            if (!$$10.consumesAction() && this.isOwnedBy($$0)) {
               this.setOrderedToSit(!this.isOrderedToSit());
               this.jumping = false;
               this.navigation.stop();
               this.setTarget(null);
               return InteractionResult.SUCCESS.withoutItem();
            }

            return $$10;
         }

         DyeColor $$7 = $$6.getDyeColor();
         if ($$7 != this.getCollarColor()) {
            this.setCollarColor($$7);
            $$2.consume(1, $$0);
            return InteractionResult.SUCCESS;
         }
      } else if (!this.level().isClientSide() && $$2.is(Items.BONE) && !this.isAngry()) {
         $$2.consume(1, $$0);
         this.tryToTame($$0);
         return InteractionResult.SUCCESS_SERVER;
      }

      return super.mobInteract($$0, $$1);
   }

   private void tryToTame(Player $$0) {
      if (this.random.nextInt(3) == 0) {
         this.tame($$0);
         this.navigation.stop();
         this.setTarget(null);
         this.setOrderedToSit(true);
         this.level().broadcastEntityEvent(this, (byte)7);
      } else {
         this.level().broadcastEntityEvent(this, (byte)6);
      }
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 8) {
         this.isShaking = true;
         this.shakeAnim = 0.0F;
         this.shakeAnimO = 0.0F;
      } else if ($$0 == 56) {
         this.cancelShake();
      } else {
         super.handleEntityEvent($$0);
      }
   }

   public float getTailAngle() {
      if (this.isAngry()) {
         return 1.5393804F;
      } else if (this.isTame()) {
         float $$0 = this.getMaxHealth();
         float $$1 = ($$0 - this.getHealth()) / $$0;
         return (0.55F - $$1 * 0.4F) * (float) Math.PI;
      } else {
         return (float) (Math.PI / 5);
      }
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.WOLF_FOOD);
   }

   @Override
   public int getMaxSpawnClusterSize() {
      return 8;
   }

   @Override
   public long getPersistentAngerEndTime() {
      return (Long)this.entityData.get(DATA_ANGER_END_TIME);
   }

   @Override
   public void setPersistentAngerEndTime(long $$0) {
      this.entityData.set(DATA_ANGER_END_TIME, $$0);
   }

   @Override
   public void startPersistentAngerTimer() {
      this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   @Override
   public void setPersistentAngerTarget(@Nullable net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> $$0) {
      this.persistentAngerTarget = $$0;
   }

   public DyeColor getCollarColor() {
      return DyeColor.byId((Integer)this.entityData.get(DATA_COLLAR_COLOR));
   }

   private void setCollarColor(DyeColor $$0) {
      this.entityData.set(DATA_COLLAR_COLOR, $$0.getId());
   }

   @Nullable
   public Wolf getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Wolf $$2 = net.minecraft.world.entity.EntityType.WOLF.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null && $$1 instanceof Wolf $$3) {
         if (this.random.nextBoolean()) {
            $$2.setVariant(this.getVariant());
         } else {
            $$2.setVariant($$3.getVariant());
         }

         if (this.isTame()) {
            $$2.setOwnerReference(this.getOwnerReference());
            $$2.setTame(true, true);
            DyeColor $$4 = this.getCollarColor();
            DyeColor $$5 = $$3.getCollarColor();
            $$2.setCollarColor(DyeColor.getMixedColor($$0, $$4, $$5));
         }

         $$2.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), this.random));
      }

      return $$2;
   }

   public void setIsInterested(boolean $$0) {
      this.entityData.set(DATA_INTERESTED_ID, $$0);
   }

   @Override
   public boolean canMate(Animal $$0) {
      if ($$0 == this) {
         return false;
      } else if (!this.isTame()) {
         return false;
      } else if (!($$0 instanceof Wolf $$1)) {
         return false;
      } else if (!$$1.isTame()) {
         return false;
      } else {
         return $$1.isInSittingPose() ? false : this.isInLove() && $$1.isInLove();
      }
   }

   public boolean isInterested() {
      return (Boolean)this.entityData.get(DATA_INTERESTED_ID);
   }

   @Override
   public boolean wantsToAttack(net.minecraft.world.entity.LivingEntity $$0, net.minecraft.world.entity.LivingEntity $$1) {
      if ($$0 instanceof Creeper || $$0 instanceof Ghast || $$0 instanceof ArmorStand) {
         return false;
      } else if ($$0 instanceof Wolf $$2) {
         return !$$2.isTame() || $$2.getOwner() != $$1;
      } else if ($$0 instanceof Player $$3 && $$1 instanceof Player $$4 && !$$4.canHarmPlayer($$3)) {
         return false;
      } else {
         return $$0 instanceof AbstractHorse $$5 && $$5.isTamed() ? false : !($$0 instanceof net.minecraft.world.entity.TamableAnimal $$6 && $$6.isTame());
      }
   }

   @Override
   public boolean canBeLeashed() {
      return !this.isAngry();
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
   }

   public static boolean checkWolfSpawnRules(
      net.minecraft.world.entity.EntityType<Wolf> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      return $$1.getBlockState($$3.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && isBrightEnoughToSpawn($$1, $$3);
   }

   class WolfAvoidEntityGoal<T extends net.minecraft.world.entity.LivingEntity> extends AvoidEntityGoal<T> {
      private final Wolf wolf;

      public WolfAvoidEntityGoal(final Wolf $$0, final Class<T> $$1, final float $$2, final double $$3, final double $$4) {
         super($$0, $$1, $$2, $$3, $$4);
         this.wolf = $$0;
      }

      @Override
      public boolean canUse() {
         return super.canUse() && this.toAvoid instanceof Llama ? !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid) : false;
      }

      private boolean avoidLlama(Llama $$0) {
         return $$0.getStrength() >= Wolf.this.random.nextInt(5);
      }

      @Override
      public void start() {
         Wolf.this.setTarget(null);
         super.start();
      }

      @Override
      public void tick() {
         Wolf.this.setTarget(null);
         super.tick();
      }
   }

   public static class WolfPackData extends net.minecraft.world.entity.AgeableMob.AgeableMobGroupData {
      public final Holder<WolfVariant> type;

      public WolfPackData(Holder<WolfVariant> $$0) {
         super(false);
         this.type = $$0;
      }
   }
}
