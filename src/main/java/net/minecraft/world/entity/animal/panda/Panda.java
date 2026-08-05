package net.minecraft.world.entity.animal.panda;

import com.mojang.serialization.Codec;
import java.util.EnumSet;
import java.util.List;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

public class Panda extends Animal {
   private static final EntityDataAccessor<Integer> UNHAPPY_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> SNEEZE_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Byte> MAIN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> HIDDEN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
   static final TargetingConditions BREED_TARGETING = TargetingConditions.forNonCombat().range(8.0);
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.PANDA
      .getDimensions()
      .scale(0.5F)
      .withAttachments(
         net.minecraft.world.entity.EntityAttachments.builder().attach(net.minecraft.world.entity.EntityAttachment.PASSENGER, 0.0F, 0.40625F, 0.0F)
      );
   private static final int FLAG_SNEEZE = 2;
   private static final int FLAG_ROLL = 4;
   private static final int FLAG_SIT = 8;
   private static final int FLAG_ON_BACK = 16;
   private static final int EAT_TICK_INTERVAL = 5;
   public static final int TOTAL_ROLL_STEPS = 32;
   private static final int TOTAL_UNHAPPY_TIME = 32;
   boolean gotBamboo;
   boolean didBite;
   public int rollCounter;
   private Vec3 rollDelta;
   private float sitAmount;
   private float sitAmountO;
   private float onBackAmount;
   private float onBackAmountO;
   private float rollAmount;
   private float rollAmountO;
   Panda.PandaLookAtPlayerGoal lookAtPlayerGoal;

   public Panda(net.minecraft.world.entity.EntityType<? extends Panda> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new Panda.PandaMoveControl(this);
      if (!this.isBaby()) {
         this.setCanPickUpLoot(true);
      }
   }

   @Override
   protected boolean canDispenserEquipIntoSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return $$0 == net.minecraft.world.entity.EquipmentSlot.MAINHAND && this.canPickUpLoot();
   }

   public int getUnhappyCounter() {
      return (Integer)this.entityData.get(UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int $$0) {
      this.entityData.set(UNHAPPY_COUNTER, $$0);
   }

   public boolean isSneezing() {
      return this.getFlag(2);
   }

   public boolean isSitting() {
      return this.getFlag(8);
   }

   public void sit(boolean $$0) {
      this.setFlag(8, $$0);
   }

   public boolean isOnBack() {
      return this.getFlag(16);
   }

   public void setOnBack(boolean $$0) {
      this.setFlag(16, $$0);
   }

   public boolean isEating() {
      return (Integer)this.entityData.get(EAT_COUNTER) > 0;
   }

   public void eat(boolean $$0) {
      this.entityData.set(EAT_COUNTER, $$0 ? 1 : 0);
   }

   private int getEatCounter() {
      return (Integer)this.entityData.get(EAT_COUNTER);
   }

   private void setEatCounter(int $$0) {
      this.entityData.set(EAT_COUNTER, $$0);
   }

   public void sneeze(boolean $$0) {
      this.setFlag(2, $$0);
      if (!$$0) {
         this.setSneezeCounter(0);
      }
   }

   public int getSneezeCounter() {
      return (Integer)this.entityData.get(SNEEZE_COUNTER);
   }

   public void setSneezeCounter(int $$0) {
      this.entityData.set(SNEEZE_COUNTER, $$0);
   }

   public Panda.Gene getMainGene() {
      return Panda.Gene.byId((Byte)this.entityData.get(MAIN_GENE_ID));
   }

   public void setMainGene(Panda.Gene $$0) {
      if ($$0.getId() > 6) {
         $$0 = Panda.Gene.getRandom(this.random);
      }

      this.entityData.set(MAIN_GENE_ID, (byte)$$0.getId());
   }

   public Panda.Gene getHiddenGene() {
      return Panda.Gene.byId((Byte)this.entityData.get(HIDDEN_GENE_ID));
   }

   public void setHiddenGene(Panda.Gene $$0) {
      if ($$0.getId() > 6) {
         $$0 = Panda.Gene.getRandom(this.random);
      }

      this.entityData.set(HIDDEN_GENE_ID, (byte)$$0.getId());
   }

   public boolean isRolling() {
      return this.getFlag(4);
   }

   public void roll(boolean $$0) {
      this.setFlag(4, $$0);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(UNHAPPY_COUNTER, 0);
      $$0.define(SNEEZE_COUNTER, 0);
      $$0.define(MAIN_GENE_ID, (byte)0);
      $$0.define(HIDDEN_GENE_ID, (byte)0);
      $$0.define(DATA_ID_FLAGS, (byte)0);
      $$0.define(EAT_COUNTER, 0);
   }

   private boolean getFlag(int $$0) {
      return ((Byte)this.entityData.get(DATA_ID_FLAGS) & $$0) != 0;
   }

   private void setFlag(int $$0, boolean $$1) {
      byte $$2 = (Byte)this.entityData.get(DATA_ID_FLAGS);
      if ($$1) {
         this.entityData.set(DATA_ID_FLAGS, (byte)($$2 | $$0));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)($$2 & ~$$0));
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("MainGene", Panda.Gene.CODEC, this.getMainGene());
      $$0.store("HiddenGene", Panda.Gene.CODEC, this.getHiddenGene());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setMainGene($$0.read("MainGene", Panda.Gene.CODEC).orElse(Panda.Gene.NORMAL));
      this.setHiddenGene($$0.read("HiddenGene", Panda.Gene.CODEC).orElse(Panda.Gene.NORMAL));
   }

   
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Panda $$2 = net.minecraft.world.entity.EntityType.PANDA.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null) {
         if ($$1 instanceof Panda $$3) {
            $$2.setGeneFromParents(this, $$3);
         }

         $$2.setAttributes();
      }

      return $$2;
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(2, new Panda.PandaPanicGoal(this, 2.0));
      this.goalSelector.addGoal(2, new Panda.PandaBreedGoal(this, 1.0));
      this.goalSelector.addGoal(3, new Panda.PandaAttackGoal(this, 1.2F, true));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, $$0 -> $$0.is(ItemTags.PANDA_FOOD), false));
      this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Player.class, 8.0F, 2.0, 2.0));
      this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Monster.class, 4.0F, 2.0, 2.0));
      this.goalSelector.addGoal(7, new Panda.PandaSitGoal());
      this.goalSelector.addGoal(8, new Panda.PandaLieOnBackGoal(this));
      this.goalSelector.addGoal(8, new Panda.PandaSneezeGoal(this));
      this.lookAtPlayerGoal = new Panda.PandaLookAtPlayerGoal(this, Player.class, 6.0F);
      this.goalSelector.addGoal(9, this.lookAtPlayerGoal);
      this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
      this.goalSelector.addGoal(12, new Panda.PandaRollGoal(this));
      this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25));
      this.goalSelector.addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.targetSelector.addGoal(1, new Panda.PandaHurtByTargetGoal(this).setAlertOthers(new Class[0]));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.15F).add(Attributes.ATTACK_DAMAGE, 6.0);
   }

   public Panda.Gene getVariant() {
      return Panda.Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
   }

   public boolean isLazy() {
      return this.getVariant() == Panda.Gene.LAZY;
   }

   public boolean isWorried() {
      return this.getVariant() == Panda.Gene.WORRIED;
   }

   public boolean isPlayful() {
      return this.getVariant() == Panda.Gene.PLAYFUL;
   }

   public boolean isBrown() {
      return this.getVariant() == Panda.Gene.BROWN;
   }

   public boolean isWeak() {
      return this.getVariant() == Panda.Gene.WEAK;
   }

   @Override
   public boolean isAggressive() {
      return this.getVariant() == Panda.Gene.AGGRESSIVE;
   }

   @Override
   public boolean canBeLeashed() {
      return false;
   }

   @Override
   public boolean doHurtTarget(ServerLevel $$0, net.minecraft.world.entity.Entity $$1) {
      if (!this.isAggressive()) {
         this.didBite = true;
      }

      return super.doHurtTarget($$0, $$1);
   }

   @Override
   public void playAttackSound() {
      this.playSound(SoundEvents.PANDA_BITE, 1.0F, 1.0F);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isWorried()) {
         if (this.level().isThundering() && !this.isInWater()) {
            this.sit(true);
            this.eat(false);
         } else if (!this.isEating()) {
            this.sit(false);
         }
      }

      net.minecraft.world.entity.LivingEntity $$0 = this.getTarget();
      if ($$0 == null) {
         this.gotBamboo = false;
         this.didBite = false;
      }

      if (this.getUnhappyCounter() > 0) {
         if ($$0 != null) {
            this.lookAt($$0, 90.0F, 90.0F);
         }

         if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
            this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0F, 1.0F);
         }

         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      if (this.isSneezing()) {
         this.setSneezeCounter(this.getSneezeCounter() + 1);
         if (this.getSneezeCounter() > 20) {
            this.sneeze(false);
            this.afterSneeze();
         } else if (this.getSneezeCounter() == 1) {
            this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0F, 1.0F);
         }
      }

      if (this.isRolling()) {
         this.handleRoll();
      } else {
         this.rollCounter = 0;
      }

      if (this.isSitting()) {
         this.setXRot(0.0F);
      }

      this.updateSitAmount();
      this.handleEating();
      this.updateOnBackAnimation();
      this.updateRollAmount();
   }

   public boolean isScared() {
      return this.isWorried() && this.level().isThundering();
   }

   private void handleEating() {
      if (!this.isEating()
         && this.isSitting()
         && !this.isScared()
         && !this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty()
         && this.random.nextInt(80) == 1) {
         this.eat(true);
      } else if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
         this.eat(false);
      }

      if (this.isEating()) {
         this.addEatingParticles();
         if (!this.level().isClientSide() && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
            if (this.getEatCounter() > 100 && this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).is(ItemTags.PANDA_EATS_FROM_GROUND)) {
               if (!this.level().isClientSide()) {
                  this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                  this.gameEvent(GameEvent.EAT);
               }

               this.sit(false);
            }

            this.eat(false);
            return;
         }

         this.setEatCounter(this.getEatCounter() + 1);
      }
   }

   private void addEatingParticles() {
      if (this.getEatCounter() % 5 == 0) {
         this.playSound(SoundEvents.PANDA_EAT, 0.5F + 0.5F * this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

         for (int $$0 = 0; $$0 < 6; $$0++) {
            Vec3 $$1 = new Vec3((this.random.nextFloat() - 0.5) * 0.1, this.random.nextFloat() * 0.1 + 0.1, (this.random.nextFloat() - 0.5) * 0.1);
            $$1 = $$1.xRot(-this.getXRot() * (float) (Math.PI / 180.0));
            $$1 = $$1.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
            double $$2 = -this.random.nextFloat() * 0.6 - 0.3;
            Vec3 $$3 = new Vec3((this.random.nextFloat() - 0.5) * 0.8, $$2, 1.0 + (this.random.nextFloat() - 0.5) * 0.4);
            $$3 = $$3.yRot(-this.yBodyRot * (float) (Math.PI / 180.0));
            $$3 = $$3.add(this.getX(), this.getEyeY() + 1.0, this.getZ());
            this.level()
               .addParticle(
                  new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND)),
                  $$3.x,
                  $$3.y,
                  $$3.z,
                  $$1.x,
                  $$1.y + 0.05,
                  $$1.z
               );
         }
      }
   }

   private void updateSitAmount() {
      this.sitAmountO = this.sitAmount;
      if (this.isSitting()) {
         this.sitAmount = Math.min(1.0F, this.sitAmount + 0.15F);
      } else {
         this.sitAmount = Math.max(0.0F, this.sitAmount - 0.19F);
      }
   }

   private void updateOnBackAnimation() {
      this.onBackAmountO = this.onBackAmount;
      if (this.isOnBack()) {
         this.onBackAmount = Math.min(1.0F, this.onBackAmount + 0.15F);
      } else {
         this.onBackAmount = Math.max(0.0F, this.onBackAmount - 0.19F);
      }
   }

   private void updateRollAmount() {
      this.rollAmountO = this.rollAmount;
      if (this.isRolling()) {
         this.rollAmount = Math.min(1.0F, this.rollAmount + 0.15F);
      } else {
         this.rollAmount = Math.max(0.0F, this.rollAmount - 0.19F);
      }
   }

   public float getSitAmount(float $$0) {
      return Mth.lerp($$0, this.sitAmountO, this.sitAmount);
   }

   public float getLieOnBackAmount(float $$0) {
      return Mth.lerp($$0, this.onBackAmountO, this.onBackAmount);
   }

   public float getRollAmount(float $$0) {
      return Mth.lerp($$0, this.rollAmountO, this.rollAmount);
   }

   private void handleRoll() {
      this.rollCounter++;
      if (this.rollCounter > 32) {
         this.roll(false);
      } else {
         if (!this.level().isClientSide()) {
            Vec3 $$0 = this.getDeltaMovement();
            if (this.rollCounter == 1) {
               float $$1 = this.getYRot() * (float) (Math.PI / 180.0);
               float $$2 = this.isBaby() ? 0.1F : 0.2F;
               this.rollDelta = new Vec3($$0.x + -Mth.sin($$1) * $$2, 0.0, $$0.z + Mth.cos($$1) * $$2);
               this.setDeltaMovement(this.rollDelta.add(0.0, 0.27, 0.0));
            } else if (this.rollCounter != 7.0F && this.rollCounter != 15.0F && this.rollCounter != 23.0F) {
               this.setDeltaMovement(this.rollDelta.x, $$0.y, this.rollDelta.z);
            } else {
               this.setDeltaMovement(0.0, this.onGround() ? 0.27 : $$0.y, 0.0);
            }
         }
      }
   }

   private void afterSneeze() {
      Vec3 $$0 = this.getDeltaMovement();
      Level $$1 = this.level();
      $$1.addParticle(
         ParticleTypes.SNEEZE,
         this.getX() - (this.getBbWidth() + 1.0F) * 0.5 * Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0)),
         this.getEyeY() - 0.1F,
         this.getZ() + (this.getBbWidth() + 1.0F) * 0.5 * Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0)),
         $$0.x,
         0.0,
         $$0.z
      );
      this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);

      for (Panda $$3 : $$1.getEntitiesOfClass(Panda.class, this.getBoundingBox().inflate(10.0))) {
         if (!$$3.isBaby() && $$3.onGround() && !$$3.isInWater() && $$3.canPerformAction()) {
            $$3.jumpFromGround();
         }
      }

      if (this.level() instanceof ServerLevel $$4 && (Boolean)$$4.getGameRules().get(GameRules.MOB_DROPS)) {
         this.dropFromGiftLootTable($$4, BuiltInLootTables.PANDA_SNEEZE, this::spawnAtLocation);
      }
   }

   @Override
   protected void pickUpItem(ServerLevel $$0, ItemEntity $$1) {
      if (this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty() && canPickUpAndEat($$1)) {
         this.onItemPickup($$1);
         ItemStack $$2 = $$1.getItem();
         this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, $$2);
         this.setGuaranteedDrop(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         this.take($$1, $$2.getCount());
         $$1.discard();
      }
   }

   @Override
   public boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      this.sit(false);
      return super.hurtServer($$0, $$1, $$2);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      RandomSource $$4 = $$0.getRandom();
      this.setMainGene(Panda.Gene.getRandom($$4));
      this.setHiddenGene(Panda.Gene.getRandom($$4));
      this.setAttributes();
      if ($$3 == null) {
         $$3 = new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(0.2F);
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   public void setGeneFromParents(Panda $$0, Panda $$1) {
      if ($$1 == null) {
         if (this.random.nextBoolean()) {
            this.setMainGene($$0.getOneOfGenesRandomly());
            this.setHiddenGene(Panda.Gene.getRandom(this.random));
         } else {
            this.setMainGene(Panda.Gene.getRandom(this.random));
            this.setHiddenGene($$0.getOneOfGenesRandomly());
         }
      } else if (this.random.nextBoolean()) {
         this.setMainGene($$0.getOneOfGenesRandomly());
         this.setHiddenGene($$1.getOneOfGenesRandomly());
      } else {
         this.setMainGene($$1.getOneOfGenesRandomly());
         this.setHiddenGene($$0.getOneOfGenesRandomly());
      }

      if (this.random.nextInt(32) == 0) {
         this.setMainGene(Panda.Gene.getRandom(this.random));
      }

      if (this.random.nextInt(32) == 0) {
         this.setHiddenGene(Panda.Gene.getRandom(this.random));
      }
   }

   private Panda.Gene getOneOfGenesRandomly() {
      return this.random.nextBoolean() ? this.getMainGene() : this.getHiddenGene();
   }

   public void setAttributes() {
      if (this.isWeak()) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0);
      }

      if (this.isLazy()) {
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.07F);
      }
   }

   void tryToSit() {
      if (!this.isInWater()) {
         this.setZza(0.0F);
         this.getNavigation().stop();
         this.sit(true);
      }
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (this.isScared()) {
         return InteractionResult.PASS;
      } else if (this.isOnBack()) {
         this.setOnBack(false);
         return InteractionResult.SUCCESS;
      } else if (this.isFood($$2)) {
         if (this.getTarget() != null) {
            this.gotBamboo = true;
         }

         if (this.isBaby()) {
            this.usePlayerItem($$0, $$1, $$2);
            this.ageUp((int)(-this.getAge() / 20 * 0.1F), true);
         } else if (!this.level().isClientSide() && this.getAge() == 0 && this.canFallInLove()) {
            this.usePlayerItem($$0, $$1, $$2);
            this.setInLove($$0);
         } else {
            if (!(this.level() instanceof ServerLevel $$3) || this.isSitting() || this.isInWater()) {
               return InteractionResult.PASS;
            }

            this.tryToSit();
            this.eat(true);
            ItemStack $$4 = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (!$$4.isEmpty() && !$$0.hasInfiniteMaterials()) {
               this.spawnAtLocation($$3, $$4);
            }

            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new ItemStack($$2.getItem(), 1));
            this.usePlayerItem($$0, $$1, $$2);
         }

         return InteractionResult.SUCCESS_SERVER;
      } else {
         return InteractionResult.PASS;
      }
   }

   
   @Override
   protected SoundEvent getAmbientSound() {
      if (this.isAggressive()) {
         return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
      } else {
         return this.isWorried() ? SoundEvents.PANDA_WORRIED_AMBIENT : SoundEvents.PANDA_AMBIENT;
      }
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.0F);
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.PANDA_FOOD);
   }

   
   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.PANDA_DEATH;
   }

   
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.PANDA_HURT;
   }

   public boolean canPerformAction() {
      return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
   }

   private static boolean canPickUpAndEat(ItemEntity $$0) {
      return $$0.getItem().is(ItemTags.PANDA_EATS_FROM_GROUND) && $$0.isAlive() && !$$0.hasPickUpDelay();
   }

   public static enum Gene implements StringRepresentable {
      NORMAL(0, "normal", false),
      LAZY(1, "lazy", false),
      WORRIED(2, "worried", false),
      PLAYFUL(3, "playful", false),
      BROWN(4, "brown", true),
      WEAK(5, "weak", true),
      AGGRESSIVE(6, "aggressive", false);

      public static final Codec<Panda.Gene> CODEC = StringRepresentable.fromEnum(Panda.Gene::values);
      private static final IntFunction<Panda.Gene> BY_ID = ByIdMap.continuous(Panda.Gene::getId, values(), OutOfBoundsStrategy.ZERO);
      private static final int MAX_GENE = 6;
      private final int id;
      private final String name;
      private final boolean isRecessive;

      private Gene(final int $$0, final String $$1, final boolean $$2) {
         this.id = $$0;
         this.name = $$1;
         this.isRecessive = $$2;
      }

      public int getId() {
         return this.id;
      }

      public String getSerializedName() {
         return this.name;
      }

      public boolean isRecessive() {
         return this.isRecessive;
      }

      static Panda.Gene getVariantFromGenes(Panda.Gene $$0, Panda.Gene $$1) {
         if ($$0.isRecessive()) {
            return $$0 == $$1 ? $$0 : NORMAL;
         } else {
            return $$0;
         }
      }

      public static Panda.Gene byId(int $$0) {
         return BY_ID.apply($$0);
      }

      public static Panda.Gene getRandom(RandomSource $$0) {
         int $$1 = $$0.nextInt(16);
         if ($$1 == 0) {
            return LAZY;
         } else if ($$1 == 1) {
            return WORRIED;
         } else if ($$1 == 2) {
            return PLAYFUL;
         } else if ($$1 == 4) {
            return AGGRESSIVE;
         } else if ($$1 < 9) {
            return WEAK;
         } else {
            return $$1 < 11 ? BROWN : NORMAL;
         }
      }
   }

   static class PandaAttackGoal extends MeleeAttackGoal {
      private final Panda panda;

      public PandaAttackGoal(Panda $$0, double $$1, boolean $$2) {
         super($$0, $$1, $$2);
         this.panda = $$0;
      }

      @Override
      public boolean canUse() {
         return this.panda.canPerformAction() && super.canUse();
      }
   }

   static class PandaAvoidGoal<T extends net.minecraft.world.entity.LivingEntity> extends AvoidEntityGoal<T> {
      private final Panda panda;

      public PandaAvoidGoal(Panda $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
         super($$0, $$1, $$2, $$3, $$4, net.minecraft.world.entity.EntitySelector.NO_SPECTATORS);
         this.panda = $$0;
      }

      @Override
      public boolean canUse() {
         return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
      }
   }

   static class PandaBreedGoal extends BreedGoal {
      private final Panda panda;
      private int unhappyCooldown;

      public PandaBreedGoal(Panda $$0, double $$1) {
         super($$0, $$1);
         this.panda = $$0;
      }

      @Override
      public boolean canUse() {
         if (!super.canUse() || this.panda.getUnhappyCounter() != 0) {
            return false;
         } else if (!this.canFindBamboo()) {
            if (this.unhappyCooldown <= this.panda.tickCount) {
               this.panda.setUnhappyCounter(32);
               this.unhappyCooldown = this.panda.tickCount + 600;
               if (this.panda.isEffectiveAi()) {
                  Player $$0 = this.level.getNearestPlayer(Panda.BREED_TARGETING, this.panda);
                  this.panda.lookAtPlayerGoal.setTarget($$0);
               }
            }

            return false;
         } else {
            return true;
         }
      }

      private boolean canFindBamboo() {
         BlockPos $$0 = this.panda.blockPosition();
         MutableBlockPos $$1 = new MutableBlockPos();

         for (int $$2 = 0; $$2 < 3; $$2++) {
            for (int $$3 = 0; $$3 < 8; $$3++) {
               for (int $$4 = 0; $$4 <= $$3; $$4 = $$4 > 0 ? -$$4 : 1 - $$4) {
                  for (int $$5 = $$4 < $$3 && $$4 > -$$3 ? $$3 : 0; $$5 <= $$3; $$5 = $$5 > 0 ? -$$5 : 1 - $$5) {
                     $$1.setWithOffset($$0, $$4, $$2, $$5);
                     if (this.level.getBlockState($$1).is(Blocks.BAMBOO)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   static class PandaHurtByTargetGoal extends HurtByTargetGoal {
      private final Panda panda;

      public PandaHurtByTargetGoal(Panda $$0, Class<?>... $$1) {
         super($$0, $$1);
         this.panda = $$0;
      }

      @Override
      public boolean canContinueToUse() {
         if (!this.panda.gotBamboo && !this.panda.didBite) {
            return super.canContinueToUse();
         } else {
            this.panda.setTarget(null);
            return false;
         }
      }

      @Override
      protected void alertOther(net.minecraft.world.entity.Mob $$0, net.minecraft.world.entity.LivingEntity $$1) {
         if ($$0 instanceof Panda && $$0.isAggressive()) {
            $$0.setTarget($$1);
         }
      }
   }

   static class PandaLieOnBackGoal extends Goal {
      private final Panda panda;
      private int cooldown;

      public PandaLieOnBackGoal(Panda $$0) {
         this.panda = $$0;
      }

      @Override
      public boolean canUse() {
         return this.cooldown < this.panda.tickCount
            && this.panda.isLazy()
            && this.panda.canPerformAction()
            && this.panda.random.nextInt(reducedTickDelay(400)) == 1;
      }

      @Override
      public boolean canContinueToUse() {
         return !this.panda.isInWater() && (this.panda.isLazy() || this.panda.random.nextInt(reducedTickDelay(600)) != 1)
            ? this.panda.random.nextInt(reducedTickDelay(2000)) != 1
            : false;
      }

      @Override
      public void start() {
         this.panda.setOnBack(true);
         this.cooldown = 0;
      }

      @Override
      public void stop() {
         this.panda.setOnBack(false);
         this.cooldown = this.panda.tickCount + 200;
      }
   }

   static class PandaLookAtPlayerGoal extends LookAtPlayerGoal {
      private final Panda panda;

      public PandaLookAtPlayerGoal(Panda $$0, Class<? extends net.minecraft.world.entity.LivingEntity> $$1, float $$2) {
         super($$0, $$1, $$2);
         this.panda = $$0;
      }

      public void setTarget(net.minecraft.world.entity.LivingEntity $$0) {
         this.lookAt = $$0;
      }

      @Override
      public boolean canContinueToUse() {
         return this.lookAt != null && super.canContinueToUse();
      }

      @Override
      public boolean canUse() {
         if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
         } else {
            if (this.lookAt == null) {
               ServerLevel $$0 = getServerLevel(this.mob);
               if (this.lookAtType == Player.class) {
                  this.lookAt = $$0.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
               } else {
                  this.lookAt = $$0.getNearestEntity(
                     this.mob
                        .level()
                        .getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0, this.lookDistance), $$0x -> true),
                     this.lookAtContext,
                     this.mob,
                     this.mob.getX(),
                     this.mob.getEyeY(),
                     this.mob.getZ()
                  );
               }
            }

            return this.panda.canPerformAction() && this.lookAt != null;
         }
      }

      @Override
      public void tick() {
         if (this.lookAt != null) {
            super.tick();
         }
      }
   }

   static class PandaMoveControl extends MoveControl {
      private final Panda panda;

      public PandaMoveControl(Panda $$0) {
         super($$0);
         this.panda = $$0;
      }

      @Override
      public void tick() {
         if (this.panda.canPerformAction()) {
            super.tick();
         }
      }
   }

   static class PandaPanicGoal extends PanicGoal {
      private final Panda panda;

      public PandaPanicGoal(Panda $$0, double $$1) {
         super($$0, $$1, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES);
         this.panda = $$0;
      }

      @Override
      public boolean canContinueToUse() {
         if (this.panda.isSitting()) {
            this.panda.getNavigation().stop();
            return false;
         } else {
            return super.canContinueToUse();
         }
      }
   }

   static class PandaRollGoal extends Goal {
      private final Panda panda;

      public PandaRollGoal(Panda $$0) {
         this.panda = $$0;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
      }

      @Override
      public boolean canUse() {
         if ((this.panda.isBaby() || this.panda.isPlayful()) && this.panda.onGround()) {
            if (!this.panda.canPerformAction()) {
               return false;
            } else {
               float $$0 = this.panda.getYRot() * (float) (Math.PI / 180.0);
               float $$1 = -Mth.sin($$0);
               float $$2 = Mth.cos($$0);
               int $$3 = Math.abs($$1) > 0.5 ? Mth.sign($$1) : 0;
               int $$4 = Math.abs($$2) > 0.5 ? Mth.sign($$2) : 0;
               if (this.panda.level().getBlockState(this.panda.blockPosition().offset($$3, -1, $$4)).isAir()) {
                  return true;
               } else {
                  return this.panda.isPlayful() && this.panda.random.nextInt(reducedTickDelay(60)) == 1
                     ? true
                     : this.panda.random.nextInt(reducedTickDelay(500)) == 1;
               }
            }
         } else {
            return false;
         }
      }

      @Override
      public boolean canContinueToUse() {
         return false;
      }

      @Override
      public void start() {
         this.panda.roll(true);
      }

      @Override
      public boolean isInterruptable() {
         return false;
      }
   }

   class PandaSitGoal extends Goal {
      private int cooldown;

      public PandaSitGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      @Override
      public boolean canUse() {
         if (this.cooldown > Panda.this.tickCount
            || Panda.this.isBaby()
            || Panda.this.isInWater()
            || !Panda.this.canPerformAction()
            || Panda.this.getUnhappyCounter() > 0) {
            return false;
         } else {
            return !Panda.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty()
               ? true
               : !Panda.this.level().getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(6.0, 6.0, 6.0), Panda::canPickUpAndEat).isEmpty();
         }
      }

      @Override
      public boolean canContinueToUse() {
         return !Panda.this.isInWater() && (Panda.this.isLazy() || Panda.this.random.nextInt(reducedTickDelay(600)) != 1)
            ? Panda.this.random.nextInt(reducedTickDelay(2000)) != 1
            : false;
      }

      @Override
      public void tick() {
         if (!Panda.this.isSitting() && !Panda.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty()) {
            Panda.this.tryToSit();
         }
      }

      @Override
      public void start() {
         if (Panda.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND).isEmpty()) {
            List<ItemEntity> $$0 = Panda.this.level()
               .getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Panda::canPickUpAndEat);
            if (!$$0.isEmpty()) {
               Panda.this.getNavigation().moveTo($$0.getFirst(), 1.2F);
            }
         } else {
            Panda.this.tryToSit();
         }

         this.cooldown = 0;
      }

      @Override
      public void stop() {
         ItemStack $$0 = Panda.this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
         if (!$$0.isEmpty()) {
            Panda.this.spawnAtLocation(getServerLevel(Panda.this.level()), $$0);
            Panda.this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            int $$1 = Panda.this.isLazy() ? Panda.this.random.nextInt(50) + 10 : Panda.this.random.nextInt(150) + 10;
            this.cooldown = Panda.this.tickCount + $$1 * 20;
         }

         Panda.this.sit(false);
      }
   }

   static class PandaSneezeGoal extends Goal {
      private final Panda panda;

      public PandaSneezeGoal(Panda $$0) {
         this.panda = $$0;
      }

      @Override
      public boolean canUse() {
         if (this.panda.isBaby() && this.panda.canPerformAction()) {
            return this.panda.isWeak() && this.panda.random.nextInt(reducedTickDelay(500)) == 1 ? true : this.panda.random.nextInt(reducedTickDelay(6000)) == 1;
         } else {
            return false;
         }
      }

      @Override
      public boolean canContinueToUse() {
         return false;
      }

      @Override
      public void start() {
         this.panda.sneeze(true);
      }
   }
}
