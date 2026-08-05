package net.minecraft.world.entity.animal.sheep;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class Sheep extends Animal implements net.minecraft.world.entity.Shearable {
   private static final int EAT_ANIMATION_TICKS = 40;
   private static final EntityDataAccessor<Byte> DATA_WOOL_ID = SynchedEntityData.defineId(Sheep.class, EntityDataSerializers.BYTE);
   private static final DyeColor DEFAULT_COLOR = DyeColor.WHITE;
   private static final boolean DEFAULT_SHEARED = false;
   private int eatAnimationTick;
   private EatBlockGoal eatBlockGoal;

   public Sheep(net.minecraft.world.entity.EntityType<? extends Sheep> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      this.eatBlockGoal = new EatBlockGoal(this);
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, $$0 -> $$0.is(ItemTags.SHEEP_FOOD), false));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
      this.goalSelector.addGoal(5, this.eatBlockGoal);
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
      this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.SHEEP_FOOD);
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
      super.customServerAiStep($$0);
   }

   @Override
   public void aiStep() {
      if (this.level().isClientSide()) {
         this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
      }

      super.aiStep();
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.23F);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_WOOL_ID, (byte)0);
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 10) {
         this.eatAnimationTick = 40;
      } else {
         super.handleEntityEvent($$0);
      }
   }

   public float getHeadEatPositionScale(float $$0) {
      if (this.eatAnimationTick <= 0) {
         return 0.0F;
      } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
         return 1.0F;
      } else {
         return this.eatAnimationTick < 4 ? (this.eatAnimationTick - $$0) / 4.0F : -(this.eatAnimationTick - 40 - $$0) / 4.0F;
      }
   }

   public float getHeadEatAngleScale(float $$0) {
      if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
         float $$1 = (this.eatAnimationTick - 4 - $$0) / 32.0F;
         return (float) (Math.PI / 5) + 0.21991149F * Mth.sin($$1 * 28.7F);
      } else {
         return this.eatAnimationTick > 0 ? (float) (Math.PI / 5) : this.getXRot($$0) * (float) (Math.PI / 180.0);
      }
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.SHEARS)) {
         if (this.level() instanceof ServerLevel $$3 && this.readyForShearing()) {
            this.shear($$3, SoundSource.PLAYERS, $$2);
            this.gameEvent(GameEvent.SHEAR, $$0);
            $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
            return InteractionResult.SUCCESS_SERVER;
         } else {
            return InteractionResult.CONSUME;
         }
      } else {
         return super.mobInteract($$0, $$1);
      }
   }

   @Override
   public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$2) {
      $$0.playSound(null, this, SoundEvents.SHEEP_SHEAR, $$1, 1.0F, 1.0F);
      this.dropFromShearingLootTable(
         $$0,
         BuiltInLootTables.SHEAR_SHEEP,
         $$2,
         ($$0x, $$1x) -> {
            for (int $$2x = 0; $$2x < $$1x.getCount(); $$2x++) {
               ItemEntity $$3 = this.spawnAtLocation($$0x, $$1x.copyWithCount(1), 1.0F);
               if ($$3 != null) {
                  $$3.setDeltaMovement(
                     $$3.getDeltaMovement()
                        .add(
                           (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                           this.random.nextFloat() * 0.05F,
                           (this.random.nextFloat() - this.random.nextFloat()) * 0.1F
                        )
                  );
               }
            }
         }
      );
      this.setSheared(true);
   }

   @Override
   public boolean readyForShearing() {
      return this.isAlive() && !this.isSheared() && !this.isBaby();
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("Sheared", this.isSheared());
      $$0.store("Color", DyeColor.LEGACY_ID_CODEC, this.getColor());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setSheared($$0.getBooleanOr("Sheared", false));
      this.setColor($$0.read("Color", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLOR));
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SHEEP_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SHEEP_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SHEEP_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
   }

   public DyeColor getColor() {
      return DyeColor.byId((Byte)this.entityData.get(DATA_WOOL_ID) & 15);
   }

   public void setColor(DyeColor $$0) {
      byte $$1 = (Byte)this.entityData.get(DATA_WOOL_ID);
      this.entityData.set(DATA_WOOL_ID, (byte)($$1 & 240 | $$0.getId() & 15));
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.SHEEP_COLOR ? castComponentValue((DataComponentType<T>)$$0, this.getColor()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.SHEEP_COLOR);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.SHEEP_COLOR) {
         this.setColor(castComponentValue(DataComponents.SHEEP_COLOR, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   public boolean isSheared() {
      return ((Byte)this.entityData.get(DATA_WOOL_ID) & 16) != 0;
   }

   public void setSheared(boolean $$0) {
      byte $$1 = (Byte)this.entityData.get(DATA_WOOL_ID);
      if ($$0) {
         this.entityData.set(DATA_WOOL_ID, (byte)($$1 | 16));
      } else {
         this.entityData.set(DATA_WOOL_ID, (byte)($$1 & -17));
      }
   }

   public static DyeColor getRandomSheepColor(ServerLevelAccessor $$0, BlockPos $$1) {
      Holder<Biome> $$2 = $$0.getBiome($$1);
      return SheepColorSpawnRules.getSheepColor($$2, $$0.getRandom());
   }

   
   public Sheep getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Sheep $$2 = net.minecraft.world.entity.EntityType.SHEEP.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null) {
         DyeColor $$3 = this.getColor();
         DyeColor $$4 = ((Sheep)$$1).getColor();
         $$2.setColor(DyeColor.getMixedColor($$0, $$3, $$4));
      }

      return $$2;
   }

   @Override
   public void ate() {
      super.ate();
      this.setSheared(false);
      if (this.isBaby()) {
         this.ageUp(60);
      }
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.setColor(getRandomSheepColor($$0, this.blockPosition()));
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }
}
