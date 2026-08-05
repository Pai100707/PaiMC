package net.minecraft.world.entity.animal.feline;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;

public class Cat extends net.minecraft.world.entity.TamableAnimal {
   public static final double TEMPT_SPEED_MOD = 0.6;
   public static final double WALK_SPEED_MOD = 0.8;
   public static final double SPRINT_SPEED_MOD = 1.33;
   private static final EntityDataAccessor<Holder<CatVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.CAT_VARIANT);
   private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
   private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
   private static final ResourceKey<CatVariant> DEFAULT_VARIANT = CatVariants.BLACK;
   private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
   
   private Cat.CatAvoidEntityGoal<Player> avoidPlayersGoal;
   
   private TemptGoal temptGoal;
   private float lieDownAmount;
   private float lieDownAmountO;
   private float lieDownAmountTail;
   private float lieDownAmountOTail;
   private boolean isLyingOnTopOfSleepingPlayer;
   private float relaxStateOneAmount;
   private float relaxStateOneAmountO;

   public Cat(net.minecraft.world.entity.EntityType<? extends Cat> $$0, Level $$1) {
      super($$0, $$1);
      this.reassessTameGoals();
   }

   @Override
   protected void registerGoals() {
      this.temptGoal = new Cat.CatTemptGoal(this, 0.6, $$0 -> $$0.is(ItemTags.CAT_FOOD), true);
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new net.minecraft.world.entity.TamableAnimal.TamableAnimalPanicGoal(1.5));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(3, new Cat.CatRelaxOnOwnerGoal(this));
      this.goalSelector.addGoal(4, this.temptGoal);
      this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
      this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 5.0F));
      this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
      this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
      this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
      this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
      this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
      this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
      this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Rabbit.class, false, null));
      this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
   }

   public Holder<CatVariant> getVariant() {
      return (Holder<CatVariant>)this.entityData.get(DATA_VARIANT_ID);
   }

   private void setVariant(Holder<CatVariant> $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0);
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      if ($$0 == DataComponents.CAT_VARIANT) {
         return castComponentValue((DataComponentType<T>)$$0, this.getVariant());
      } else {
         return $$0 == DataComponents.CAT_COLLAR ? castComponentValue((DataComponentType<T>)$$0, this.getCollarColor()) : super.get($$0);
      }
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.CAT_VARIANT);
      this.applyImplicitComponentIfPresent($$0, DataComponents.CAT_COLLAR);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.CAT_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.CAT_VARIANT, $$1));
         return true;
      } else if ($$0 == DataComponents.CAT_COLLAR) {
         this.setCollarColor(castComponentValue(DataComponents.CAT_COLLAR, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   public void setLying(boolean $$0) {
      this.entityData.set(IS_LYING, $$0);
   }

   public boolean isLying() {
      return (Boolean)this.entityData.get(IS_LYING);
   }

   void setRelaxStateOne(boolean $$0) {
      this.entityData.set(RELAX_STATE_ONE, $$0);
   }

   boolean isRelaxStateOne() {
      return (Boolean)this.entityData.get(RELAX_STATE_ONE);
   }

   public DyeColor getCollarColor() {
      return DyeColor.byId((Integer)this.entityData.get(DATA_COLLAR_COLOR));
   }

   private void setCollarColor(DyeColor $$0) {
      this.entityData.set(DATA_COLLAR_COLOR, $$0.getId());
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), DEFAULT_VARIANT));
      $$0.define(IS_LYING, false);
      $$0.define(RELAX_STATE_ONE, false);
      $$0.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      VariantUtils.writeVariant($$0, this.getVariant());
      $$0.store("CollarColor", DyeColor.LEGACY_ID_CODEC, this.getCollarColor());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      VariantUtils.<CatVariant>readVariant($$0, Registries.CAT_VARIANT).ifPresent(this::setVariant);
      this.setCollarColor($$0.read("CollarColor", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLLAR_COLOR));
   }

   @Override
   public void customServerAiStep(ServerLevel $$0) {
      if (this.getMoveControl().hasWanted()) {
         double $$1 = this.getMoveControl().getSpeedModifier();
         if ($$1 == 0.6) {
            this.setPose(net.minecraft.world.entity.Pose.CROUCHING);
            this.setSprinting(false);
         } else if ($$1 == 1.33) {
            this.setPose(net.minecraft.world.entity.Pose.STANDING);
            this.setSprinting(true);
         } else {
            this.setPose(net.minecraft.world.entity.Pose.STANDING);
            this.setSprinting(false);
         }
      } else {
         this.setPose(net.minecraft.world.entity.Pose.STANDING);
         this.setSprinting(false);
      }
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      if (this.isTame()) {
         if (this.isInLove()) {
            return SoundEvents.CAT_PURR;
         } else {
            return this.random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
         }
      } else {
         return SoundEvents.CAT_STRAY_AMBIENT;
      }
   }

   @Override
   public int getAmbientSoundInterval() {
      return 120;
   }

   public void hiss() {
      this.makeSound(SoundEvents.CAT_HISS);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.CAT_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.CAT_DEATH;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   @Override
   protected void playEatingSound() {
      this.playSound(SoundEvents.CAT_EAT, 1.0F, 1.0F);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
         this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0F, 1.0F);
      }

      this.handleLieDown();
   }

   private void handleLieDown() {
      if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
         this.playSound(SoundEvents.CAT_PURR, 0.6F + 0.4F * (this.random.nextFloat() - this.random.nextFloat()), 1.0F);
      }

      this.updateLieDownAmount();
      this.updateRelaxStateOneAmount();
      this.isLyingOnTopOfSleepingPlayer = false;
      if (this.isLying()) {
         BlockPos $$0 = this.blockPosition();

         for (Player $$2 : this.level().getEntitiesOfClass(Player.class, new AABB($$0).inflate(2.0, 2.0, 2.0))) {
            if ($$2.isSleeping()) {
               this.isLyingOnTopOfSleepingPlayer = true;
               break;
            }
         }
      }
   }

   public boolean isLyingOnTopOfSleepingPlayer() {
      return this.isLyingOnTopOfSleepingPlayer;
   }

   private void updateLieDownAmount() {
      this.lieDownAmountO = this.lieDownAmount;
      this.lieDownAmountOTail = this.lieDownAmountTail;
      if (this.isLying()) {
         this.lieDownAmount = Math.min(1.0F, this.lieDownAmount + 0.15F);
         this.lieDownAmountTail = Math.min(1.0F, this.lieDownAmountTail + 0.08F);
      } else {
         this.lieDownAmount = Math.max(0.0F, this.lieDownAmount - 0.22F);
         this.lieDownAmountTail = Math.max(0.0F, this.lieDownAmountTail - 0.13F);
      }
   }

   private void updateRelaxStateOneAmount() {
      this.relaxStateOneAmountO = this.relaxStateOneAmount;
      if (this.isRelaxStateOne()) {
         this.relaxStateOneAmount = Math.min(1.0F, this.relaxStateOneAmount + 0.1F);
      } else {
         this.relaxStateOneAmount = Math.max(0.0F, this.relaxStateOneAmount - 0.13F);
      }
   }

   public float getLieDownAmount(float $$0) {
      return Mth.lerp($$0, this.lieDownAmountO, this.lieDownAmount);
   }

   public float getLieDownAmountTail(float $$0) {
      return Mth.lerp($$0, this.lieDownAmountOTail, this.lieDownAmountTail);
   }

   public float getRelaxStateOneAmount(float $$0) {
      return Mth.lerp($$0, this.relaxStateOneAmountO, this.relaxStateOneAmount);
   }

   
   public Cat getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Cat $$2 = net.minecraft.world.entity.EntityType.CAT.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null && $$1 instanceof Cat $$3) {
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
      }

      return $$2;
   }

   @Override
   public boolean canMate(Animal $$0) {
      if (!this.isTame()) {
         return false;
      } else {
         return !($$0 instanceof Cat $$1) ? false : $$1.isTame() && super.canMate($$0);
      }
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
      VariantUtils.<CatVariant>selectVariantToSpawn(SpawnContext.create($$0, this.blockPosition()), Registries.CAT_VARIANT).ifPresent(this::setVariant);
      return $$3;
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      Item $$3 = $$2.getItem();
      if (this.isTame()) {
         if (this.isOwnedBy($$0)) {
            if ($$3 instanceof DyeItem $$4) {
               DyeColor $$5 = $$4.getDyeColor();
               if ($$5 != this.getCollarColor()) {
                  if (!this.level().isClientSide()) {
                     this.setCollarColor($$5);
                     $$2.consume(1, $$0);
                     this.setPersistenceRequired();
                  }

                  return InteractionResult.SUCCESS;
               }
            } else if (this.isFood($$2) && this.getHealth() < this.getMaxHealth()) {
               if (!this.level().isClientSide()) {
                  this.usePlayerItem($$0, $$1, $$2);
                  FoodProperties $$6 = (FoodProperties)$$2.get(DataComponents.FOOD);
                  this.heal($$6 != null ? $$6.nutrition() : 1.0F);
                  this.playEatingSound();
               }

               return InteractionResult.SUCCESS;
            }

            InteractionResult $$7 = super.mobInteract($$0, $$1);
            if (!$$7.consumesAction()) {
               this.setOrderedToSit(!this.isOrderedToSit());
               return InteractionResult.SUCCESS;
            }

            return $$7;
         }
      } else if (this.isFood($$2)) {
         if (!this.level().isClientSide()) {
            this.usePlayerItem($$0, $$1, $$2);
            this.tryToTame($$0);
            this.setPersistenceRequired();
            this.playEatingSound();
         }

         return InteractionResult.SUCCESS;
      }

      InteractionResult $$8 = super.mobInteract($$0, $$1);
      if ($$8.consumesAction()) {
         this.setPersistenceRequired();
      }

      return $$8;
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.CAT_FOOD);
   }

   @Override
   public boolean removeWhenFarAway(double $$0) {
      return !this.isTame() && this.tickCount > 2400;
   }

   @Override
   public void setTame(boolean $$0, boolean $$1) {
      super.setTame($$0, $$1);
      this.reassessTameGoals();
   }

   protected void reassessTameGoals() {
      if (this.avoidPlayersGoal == null) {
         this.avoidPlayersGoal = new Cat.CatAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
      }

      this.goalSelector.removeGoal(this.avoidPlayersGoal);
      if (!this.isTame()) {
         this.goalSelector.addGoal(4, this.avoidPlayersGoal);
      }
   }

   private void tryToTame(Player $$0) {
      if (this.random.nextInt(3) == 0) {
         this.tame($$0);
         this.setOrderedToSit(true);
         this.level().broadcastEntityEvent(this, (byte)7);
      } else {
         this.level().broadcastEntityEvent(this, (byte)6);
      }
   }

   @Override
   public boolean isSteppingCarefully() {
      return this.isCrouching() || super.isSteppingCarefully();
   }

   static class CatAvoidEntityGoal<T extends net.minecraft.world.entity.LivingEntity> extends AvoidEntityGoal<T> {
      private final Cat cat;

      public CatAvoidEntityGoal(Cat $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
         super($$0, $$1, $$2, $$3, $$4, net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR);
         this.cat = $$0;
      }

      @Override
      public boolean canUse() {
         return !this.cat.isTame() && super.canUse();
      }

      @Override
      public boolean canContinueToUse() {
         return !this.cat.isTame() && super.canContinueToUse();
      }
   }

   static class CatRelaxOnOwnerGoal extends Goal {
      private final Cat cat;
      
      private Player ownerPlayer;
      
      private BlockPos goalPos;
      private int onBedTicks;

      public CatRelaxOnOwnerGoal(Cat $$0) {
         this.cat = $$0;
      }

      @Override
      public boolean canUse() {
         if (!this.cat.isTame()) {
            return false;
         } else if (this.cat.isOrderedToSit()) {
            return false;
         } else {
            net.minecraft.world.entity.LivingEntity $$0 = this.cat.getOwner();
            if ($$0 instanceof Player $$1) {
               this.ownerPlayer = $$1;
               if (!$$0.isSleeping()) {
                  return false;
               }

               if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0) {
                  return false;
               }

               BlockPos $$2 = this.ownerPlayer.blockPosition();
               BlockState $$3 = this.cat.level().getBlockState($$2);
               if ($$3.is(BlockTags.BEDS)) {
                  this.goalPos = $$3.getOptionalValue(BedBlock.FACING).map($$1x -> $$2.relative($$1x.getOpposite())).orElseGet(() -> new BlockPos($$2));
                  return !this.spaceIsOccupied();
               }
            }

            return false;
         }
      }

      private boolean spaceIsOccupied() {
         for (Cat $$1 : this.cat.level().getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0))) {
            if ($$1 != this.cat && ($$1.isLying() || $$1.isRelaxStateOne())) {
               return true;
            }
         }

         return false;
      }

      @Override
      public boolean canContinueToUse() {
         return this.cat.isTame()
            && !this.cat.isOrderedToSit()
            && this.ownerPlayer != null
            && this.ownerPlayer.isSleeping()
            && this.goalPos != null
            && !this.spaceIsOccupied();
      }

      @Override
      public void start() {
         if (this.goalPos != null) {
            this.cat.setInSittingPose(false);
            this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1F);
         }
      }

      @Override
      public void stop() {
         this.cat.setLying(false);
         if (this.ownerPlayer.getSleepTimer() >= 100
            && this.cat.level().getRandom().nextFloat()
               < (Float)this.cat.level().environmentAttributes().getValue(EnvironmentAttributes.CAT_WAKING_UP_GIFT_CHANCE, this.cat.position())) {
            this.giveMorningGift();
         }

         this.onBedTicks = 0;
         this.cat.setRelaxStateOne(false);
         this.cat.getNavigation().stop();
      }

      private void giveMorningGift() {
         RandomSource $$0 = this.cat.getRandom();
         MutableBlockPos $$1 = new MutableBlockPos();
         $$1.set(this.cat.isLeashed() ? this.cat.getLeashHolder().blockPosition() : this.cat.blockPosition());
         this.cat.randomTeleport($$1.getX() + $$0.nextInt(11) - 5, $$1.getY() + $$0.nextInt(5) - 2, $$1.getZ() + $$0.nextInt(11) - 5, false);
         $$1.set(this.cat.blockPosition());
         this.cat
            .dropFromGiftLootTable(
               getServerLevel(this.cat),
               BuiltInLootTables.CAT_MORNING_GIFT,
               ($$1x, $$2) -> $$1x.addFreshEntity(
                  new ItemEntity(
                     $$1x,
                     (double)$$1.getX() - Mth.sin(this.cat.yBodyRot * (float) (Math.PI / 180.0)),
                     $$1.getY(),
                     (double)$$1.getZ() + Mth.cos(this.cat.yBodyRot * (float) (Math.PI / 180.0)),
                     $$2
                  )
               )
            );
      }

      @Override
      public void tick() {
         if (this.ownerPlayer != null && this.goalPos != null) {
            this.cat.setInSittingPose(false);
            this.cat.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.1F);
            if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5) {
               this.onBedTicks++;
               if (this.onBedTicks > this.adjustedTickDelay(16)) {
                  this.cat.setLying(true);
                  this.cat.setRelaxStateOne(false);
               } else {
                  this.cat.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                  this.cat.setRelaxStateOne(true);
               }
            } else {
               this.cat.setLying(false);
            }
         }
      }
   }

   static class CatTemptGoal extends TemptGoal {
      
      private Player selectedPlayer;
      private final Cat cat;

      public CatTemptGoal(Cat $$0, double $$1, Predicate<ItemStack> $$2, boolean $$3) {
         super($$0, $$1, $$2, $$3);
         this.cat = $$0;
      }

      @Override
      public void tick() {
         super.tick();
         if (this.selectedPlayer == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
            this.selectedPlayer = this.player;
         } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
            this.selectedPlayer = null;
         }
      }

      @Override
      protected boolean canScare() {
         return this.selectedPlayer != null && this.selectedPlayer.equals(this.player) ? false : super.canScare();
      }

      @Override
      public boolean canUse() {
         return super.canUse() && !this.cat.isTame();
      }
   }
}
