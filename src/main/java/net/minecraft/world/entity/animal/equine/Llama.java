package net.minecraft.world.entity.animal.equine;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Llama extends AbstractChestedHorse implements RangedAttackMob {
   private static final int MAX_STRENGTH = 5;
   private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
   private static final net.minecraft.world.entity.EntityDimensions BABY_DIMENSIONS = net.minecraft.world.entity.EntityType.LLAMA
      .getDimensions()
      .withAttachments(
         net.minecraft.world.entity.EntityAttachments.builder()
            .attach(net.minecraft.world.entity.EntityAttachment.PASSENGER, 0.0F, net.minecraft.world.entity.EntityType.LLAMA.getHeight() - 0.8125F, -0.3F)
      )
      .scale(0.5F);
   boolean didSpit;
   @Nullable
   private Llama caravanHead;
   @Nullable
   private Llama caravanTail;

   public Llama(net.minecraft.world.entity.EntityType<? extends Llama> $$0, Level $$1) {
      super($$0, $$1);
      this.getNavigation().setRequiredPathLength(40.0F);
   }

   public boolean isTraderLlama() {
      return false;
   }

   private void setStrength(int $$0) {
      this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, $$0)));
   }

   private void setRandomStrength(RandomSource $$0) {
      int $$1 = $$0.nextFloat() < 0.04F ? 5 : 3;
      this.setStrength(1 + $$0.nextInt($$1));
   }

   public int getStrength() {
      return (Integer)this.entityData.get(DATA_STRENGTH_ID);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("Variant", Llama.Variant.LEGACY_CODEC, this.getVariant());
      $$0.putInt("Strength", this.getStrength());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setStrength($$0.getIntOr("Strength", 0));
      super.readAdditionalSaveData($$0);
      this.setVariant($$0.read("Variant", Llama.Variant.LEGACY_CODEC).orElse(Llama.Variant.DEFAULT));
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
      this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1F));
      this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0F));
      this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
      this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
      this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, $$0 -> $$0.is(ItemTags.LLAMA_TEMPT_ITEMS), false));
      this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new Llama.LlamaHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new Llama.LlamaAttackWolfGoal(this));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return createBaseChestedHorseAttributes();
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_STRENGTH_ID, 0);
      $$0.define(DATA_VARIANT_ID, 0);
   }

   public Llama.Variant getVariant() {
      return Llama.Variant.byId((Integer)this.entityData.get(DATA_VARIANT_ID));
   }

   private void setVariant(Llama.Variant $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0.id);
   }

   @Nullable
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.LLAMA_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.LLAMA_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.LLAMA_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.LLAMA_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.LLAMA_FOOD);
   }

   @Override
   protected boolean handleEating(Player $$0, ItemStack $$1) {
      int $$2 = 0;
      int $$3 = 0;
      float $$4 = 0.0F;
      boolean $$5 = false;
      if ($$1.is(Items.WHEAT)) {
         $$2 = 10;
         $$3 = 3;
         $$4 = 2.0F;
      } else if ($$1.is(Blocks.HAY_BLOCK.asItem())) {
         $$2 = 90;
         $$3 = 6;
         $$4 = 10.0F;
         if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
            $$5 = true;
            this.setInLove($$0);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && $$4 > 0.0F) {
         this.heal($$4);
         $$5 = true;
      }

      if (this.isBaby() && $$2 > 0) {
         this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
         if (!this.level().isClientSide()) {
            this.ageUp($$2);
            $$5 = true;
         }
      }

      if ($$3 > 0 && ($$5 || !this.isTamed()) && this.getTemper() < this.getMaxTemper() && !this.level().isClientSide()) {
         this.modifyTemper($$3);
         $$5 = true;
      }

      if ($$5 && !this.isSilent()) {
         SoundEvent $$6 = this.getEatingSound();
         if ($$6 != null) {
            this.level()
               .playSound(
                  null,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  this.getEatingSound(),
                  this.getSoundSource(),
                  1.0F,
                  1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
               );
         }
      }

      return $$5;
   }

   @Override
   public boolean isImmobile() {
      return this.isDeadOrDying() || this.isEating();
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
      this.setRandomStrength($$4);
      Llama.Variant $$5;
      if ($$3 instanceof Llama.LlamaGroupData) {
         $$5 = ((Llama.LlamaGroupData)$$3).variant;
      } else {
         $$5 = (Llama.Variant)Util.getRandom(Llama.Variant.values(), $$4);
         $$3 = new Llama.LlamaGroupData($$5);
      }

      this.setVariant($$5);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   @Override
   protected boolean canPerformRearing() {
      return false;
   }

   @Override
   protected SoundEvent getAngrySound() {
      return SoundEvents.LLAMA_ANGRY;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.LLAMA_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.LLAMA_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.LLAMA_DEATH;
   }

   @Override
   protected SoundEvent getEatingSound() {
      return SoundEvents.LLAMA_EAT;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
   }

   @Override
   protected void playChestEquipsSound() {
      this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   @Override
   public int getInventoryColumns() {
      return this.hasChest() ? this.getStrength() : 0;
   }

   @Override
   public boolean canUseSlot(net.minecraft.world.entity.EquipmentSlot $$0) {
      return true;
   }

   @Override
   public int getMaxTemper() {
      return 30;
   }

   @Override
   public boolean canMate(Animal $$0) {
      return $$0 != this && $$0 instanceof Llama && this.canParent() && ((Llama)$$0).canParent();
   }

   @Nullable
   public Llama getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Llama $$2 = this.makeNewLlama();
      if ($$2 != null) {
         this.setOffspringAttributes($$1, $$2);
         Llama $$3 = (Llama)$$1;
         int $$4 = this.random.nextInt(Math.max(this.getStrength(), $$3.getStrength())) + 1;
         if (this.random.nextFloat() < 0.03F) {
            $$4++;
         }

         $$2.setStrength($$4);
         $$2.setVariant(this.random.nextBoolean() ? this.getVariant() : $$3.getVariant());
      }

      return $$2;
   }

   @Nullable
   protected Llama makeNewLlama() {
      return net.minecraft.world.entity.EntityType.LLAMA.create(this.level(), net.minecraft.world.entity.EntitySpawnReason.BREEDING);
   }

   private void spit(net.minecraft.world.entity.LivingEntity $$0) {
      LlamaSpit $$1 = new LlamaSpit(this.level(), this);
      double $$2 = $$0.getX() - this.getX();
      double $$3 = $$0.getY(0.3333333333333333) - $$1.getY();
      double $$4 = $$0.getZ() - this.getZ();
      double $$5 = Math.sqrt($$2 * $$2 + $$4 * $$4) * 0.2F;
      if (this.level() instanceof ServerLevel $$6) {
         Projectile.spawnProjectileUsingShoot($$1, $$6, ItemStack.EMPTY, $$2, $$3 + $$5, $$4, 1.5F, 10.0F);
      }

      if (!this.isSilent()) {
         this.level()
            .playSound(
               null,
               this.getX(),
               this.getY(),
               this.getZ(),
               SoundEvents.LLAMA_SPIT,
               this.getSoundSource(),
               1.0F,
               1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
            );
      }

      this.didSpit = true;
   }

   void setDidSpit(boolean $$0) {
      this.didSpit = $$0;
   }

   @Override
   public boolean causeFallDamage(double $$0, float $$1, DamageSource $$2) {
      int $$3 = this.calculateFallDamage($$0, $$1);
      if ($$3 <= 0) {
         return false;
      } else {
         if ($$0 >= 6.0) {
            this.hurt($$2, $$3);
            this.propagateFallToPassengers($$0, $$1, $$2);
         }

         this.playBlockFallSound();
         return true;
      }
   }

   public void leaveCaravan() {
      if (this.caravanHead != null) {
         this.caravanHead.caravanTail = null;
      }

      this.caravanHead = null;
   }

   public void joinCaravan(Llama $$0) {
      this.caravanHead = $$0;
      this.caravanHead.caravanTail = this;
   }

   public boolean hasCaravanTail() {
      return this.caravanTail != null;
   }

   public boolean inCaravan() {
      return this.caravanHead != null;
   }

   @Nullable
   public Llama getCaravanHead() {
      return this.caravanHead;
   }

   @Override
   protected double followLeashSpeed() {
      return 2.0;
   }

   @Override
   public boolean supportQuadLeash() {
      return false;
   }

   @Override
   protected void followMommy(ServerLevel $$0) {
      if (!this.inCaravan() && this.isBaby()) {
         super.followMommy($$0);
      }
   }

   @Override
   public boolean canEatGrass() {
      return false;
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity $$0, float $$1) {
      this.spit($$0);
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.75 * this.getEyeHeight(), this.getBbWidth() * 0.5);
   }

   @Override
   public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose $$0) {
      return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions($$0);
   }

   @Override
   protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity $$0, net.minecraft.world.entity.EntityDimensions $$1, float $$2) {
      return getDefaultPassengerAttachmentPoint(this, $$0, $$1.attachments());
   }

   static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
      public LlamaAttackWolfGoal(Llama $$0) {
         super($$0, Wolf.class, 16, false, true, ($$0x, $$1) -> !((Wolf)$$0x).isTame());
      }

      @Override
      protected double getFollowDistance() {
         return super.getFollowDistance() * 0.25;
      }
   }

   static class LlamaGroupData extends net.minecraft.world.entity.AgeableMob.AgeableMobGroupData {
      public final Llama.Variant variant;

      LlamaGroupData(Llama.Variant $$0) {
         super(true);
         this.variant = $$0;
      }
   }

   static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
      public LlamaHurtByTargetGoal(Llama $$0) {
         super($$0);
      }

      @Override
      public boolean canContinueToUse() {
         if (this.mob instanceof Llama $$0 && $$0.didSpit) {
            $$0.setDidSpit(false);
            return false;
         } else {
            return super.canContinueToUse();
         }
      }
   }

   public static enum Variant implements StringRepresentable {
      CREAMY(0, "creamy"),
      WHITE(1, "white"),
      BROWN(2, "brown"),
      GRAY(3, "gray");

      public static final Llama.Variant DEFAULT = CREAMY;
      private static final IntFunction<Llama.Variant> BY_ID = ByIdMap.continuous(Llama.Variant::getId, values(), OutOfBoundsStrategy.CLAMP);
      public static final Codec<Llama.Variant> CODEC = StringRepresentable.fromEnum(Llama.Variant::values);
      @Deprecated
      public static final Codec<Llama.Variant> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Llama.Variant::getId);
      public static final StreamCodec<ByteBuf, Llama.Variant> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Llama.Variant::getId);
      final int id;
      private final String name;

      private Variant(final int $$0, final String $$1) {
         this.id = $$0;
         this.name = $$1;
      }

      public int getId() {
         return this.id;
      }

      public static Llama.Variant byId(int $$0) {
         return BY_ID.apply($$0);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
