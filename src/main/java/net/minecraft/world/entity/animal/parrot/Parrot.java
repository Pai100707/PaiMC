package net.minecraft.world.entity.animal.parrot;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Parrot extends ShoulderRidingEntity implements FlyingAnimal {
   private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
   private static final Predicate<net.minecraft.world.entity.Mob> NOT_PARROT_PREDICATE = new Predicate<net.minecraft.world.entity.Mob>() {
      public boolean test(net.minecraft.world.entity.Mob $$0) {
         return $$0 != null && Parrot.MOB_SOUND_MAP.containsKey($$0.getType());
      }
   };
   static final Map<net.minecraft.world.entity.EntityType<?>, SoundEvent> MOB_SOUND_MAP = (Map<net.minecraft.world.entity.EntityType<?>, SoundEvent>)Util.make(
      Maps.newHashMap(), $$0 -> {
         $$0.put(net.minecraft.world.entity.EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
         $$0.put(net.minecraft.world.entity.EntityType.BOGGED, SoundEvents.PARROT_IMITATE_BOGGED);
         $$0.put(net.minecraft.world.entity.EntityType.BREEZE, SoundEvents.PARROT_IMITATE_BREEZE);
         $$0.put(net.minecraft.world.entity.EntityType.CAMEL_HUSK, SoundEvents.PARROT_IMITATE_CAMEL_HUSK);
         $$0.put(net.minecraft.world.entity.EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
         $$0.put(net.minecraft.world.entity.EntityType.CREAKING, SoundEvents.PARROT_IMITATE_CREAKING);
         $$0.put(net.minecraft.world.entity.EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
         $$0.put(net.minecraft.world.entity.EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
         $$0.put(net.minecraft.world.entity.EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
         $$0.put(net.minecraft.world.entity.EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
         $$0.put(net.minecraft.world.entity.EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
         $$0.put(net.minecraft.world.entity.EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
         $$0.put(net.minecraft.world.entity.EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
         $$0.put(net.minecraft.world.entity.EntityType.HAPPY_GHAST, SoundEvents.EMPTY);
         $$0.put(net.minecraft.world.entity.EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
         $$0.put(net.minecraft.world.entity.EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
         $$0.put(net.minecraft.world.entity.EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
         $$0.put(net.minecraft.world.entity.EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
         $$0.put(net.minecraft.world.entity.EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
         $$0.put(net.minecraft.world.entity.EntityType.PARCHED, SoundEvents.PARROT_IMITATE_PARCHED);
         $$0.put(net.minecraft.world.entity.EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
         $$0.put(net.minecraft.world.entity.EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
         $$0.put(net.minecraft.world.entity.EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
         $$0.put(net.minecraft.world.entity.EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
         $$0.put(net.minecraft.world.entity.EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
         $$0.put(net.minecraft.world.entity.EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
         $$0.put(net.minecraft.world.entity.EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
         $$0.put(net.minecraft.world.entity.EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
         $$0.put(net.minecraft.world.entity.EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
         $$0.put(net.minecraft.world.entity.EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
         $$0.put(net.minecraft.world.entity.EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
         $$0.put(net.minecraft.world.entity.EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
         $$0.put(net.minecraft.world.entity.EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
         $$0.put(net.minecraft.world.entity.EntityType.WARDEN, SoundEvents.PARROT_IMITATE_WARDEN);
         $$0.put(net.minecraft.world.entity.EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
         $$0.put(net.minecraft.world.entity.EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
         $$0.put(net.minecraft.world.entity.EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
         $$0.put(net.minecraft.world.entity.EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
         $$0.put(net.minecraft.world.entity.EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
         $$0.put(net.minecraft.world.entity.EntityType.ZOMBIE_HORSE, SoundEvents.PARROT_IMITATE_ZOMBIE_HORSE);
         $$0.put(net.minecraft.world.entity.EntityType.ZOMBIE_NAUTILUS, SoundEvents.PARROT_IMITATE_ZOMBIE_NAUTILUS);
         $$0.put(net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
      }
   );
   public float flap;
   public float flapSpeed;
   public float oFlapSpeed;
   public float oFlap;
   private float flapping = 1.0F;
   private float nextFlap = 1.0F;
   private boolean partyParrot;
   
   private BlockPos jukebox;

   public Parrot(net.minecraft.world.entity.EntityType<? extends Parrot> $$0, Level $$1) {
      super($$0, $$1);
      this.moveControl = new FlyingMoveControl(this, 10, false);
      this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
      this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
      this.setPathfindingMalus(PathType.COCOA, -1.0F);
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      this.setVariant((Parrot.Variant)Util.getRandom(Parrot.Variant.values(), $$0.getRandom()));
      if ($$3 == null) {
         $$3 = new net.minecraft.world.entity.AgeableMob.AgeableMobGroupData(false);
      }

      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   @Override
   public boolean isBaby() {
      return false;
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(0, new net.minecraft.world.entity.TamableAnimal.TamableAnimalPanicGoal(1.25));
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
      this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0F, 1.0F));
      this.goalSelector.addGoal(2, new Parrot.ParrotWanderGoal(this, 1.0));
      this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
      this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0, 3.0F, 7.0F));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes()
         .add(Attributes.MAX_HEALTH, 6.0)
         .add(Attributes.FLYING_SPEED, 0.4F)
         .add(Attributes.MOVEMENT_SPEED, 0.2F)
         .add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   @Override
   protected PathNavigation createNavigation(Level $$0) {
      FlyingPathNavigation $$1 = new FlyingPathNavigation(this, $$0);
      $$1.setCanOpenDoors(false);
      $$1.setCanFloat(true);
      return $$1;
   }

   @Override
   public void aiStep() {
      if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46) || !this.level().getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
         this.partyParrot = false;
         this.jukebox = null;
      }

      if (this.level().random.nextInt(400) == 0) {
         imitateNearbyMobs(this.level(), this);
      }

      super.aiStep();
      this.calculateFlapping();
   }

   @Override
   public void setRecordPlayingNearby(BlockPos $$0, boolean $$1) {
      this.jukebox = $$0;
      this.partyParrot = $$1;
   }

   public boolean isPartyParrot() {
      return this.partyParrot;
   }

   private void calculateFlapping() {
      this.oFlap = this.flap;
      this.oFlapSpeed = this.flapSpeed;
      this.flapSpeed = this.flapSpeed + (!this.onGround() && !this.isPassenger() ? 4 : -1) * 0.3F;
      this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
      if (!this.onGround() && this.flapping < 1.0F) {
         this.flapping = 1.0F;
      }

      this.flapping *= 0.9F;
      Vec3 $$0 = this.getDeltaMovement();
      if (!this.onGround() && $$0.y < 0.0) {
         this.setDeltaMovement($$0.multiply(1.0, 0.6, 1.0));
      }

      this.flap = this.flap + this.flapping * 2.0F;
   }

   public static boolean imitateNearbyMobs(Level $$0, net.minecraft.world.entity.Entity $$1) {
      if ($$1.isAlive() && !$$1.isSilent() && $$0.random.nextInt(2) == 0) {
         List<net.minecraft.world.entity.Mob> $$2 = $$0.getEntitiesOfClass(
            net.minecraft.world.entity.Mob.class, $$1.getBoundingBox().inflate(20.0), NOT_PARROT_PREDICATE
         );
         if (!$$2.isEmpty()) {
            net.minecraft.world.entity.Mob $$3 = $$2.get($$0.random.nextInt($$2.size()));
            if (!$$3.isSilent()) {
               SoundEvent $$4 = getImitatedSound($$3.getType());
               $$0.playSound(null, $$1.getX(), $$1.getY(), $$1.getZ(), $$4, $$1.getSoundSource(), 0.7F, getPitch($$0.random));
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if (!this.isTame() && $$2.is(ItemTags.PARROT_FOOD)) {
         this.usePlayerItem($$0, $$1, $$2);
         if (!this.isSilent()) {
            this.level()
               .playSound(
                  null,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  SoundEvents.PARROT_EAT,
                  this.getSoundSource(),
                  1.0F,
                  1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
               );
         }

         if (!this.level().isClientSide()) {
            if (this.random.nextInt(10) == 0) {
               this.tame($$0);
               this.level().broadcastEntityEvent(this, (byte)7);
            } else {
               this.level().broadcastEntityEvent(this, (byte)6);
            }
         }

         return InteractionResult.SUCCESS;
      } else if (!$$2.is(ItemTags.PARROT_POISONOUS_FOOD)) {
         if (!this.isFlying() && this.isTame() && this.isOwnedBy($$0)) {
            if (!this.level().isClientSide()) {
               this.setOrderedToSit(!this.isOrderedToSit());
            }

            return InteractionResult.SUCCESS;
         } else {
            return super.mobInteract($$0, $$1);
         }
      } else {
         this.usePlayerItem($$0, $$1, $$2);
         this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
         if ($$0.isCreative() || !this.isInvulnerable()) {
            this.hurt(this.damageSources().playerAttack($$0), Float.MAX_VALUE);
         }

         return InteractionResult.SUCCESS;
      }
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return false;
   }

   public static boolean checkParrotSpawnRules(
      net.minecraft.world.entity.EntityType<Parrot> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      return $$1.getBlockState($$3.below()).is(BlockTags.PARROTS_SPAWNABLE_ON) && isBrightEnoughToSpawn($$1, $$3);
   }

   @Override
   protected void checkFallDamage(double $$0, boolean $$1, BlockState $$2, BlockPos $$3) {
   }

   @Override
   public boolean canMate(Animal $$0) {
      return false;
   }

   
   @Override
   public net.minecraft.world.entity.AgeableMob getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      return null;
   }

   
   @Override
   public SoundEvent getAmbientSound() {
      return getAmbient(this.level(), this.level().random);
   }

   public static SoundEvent getAmbient(Level $$0, RandomSource $$1) {
      if ($$0.getDifficulty() != Difficulty.PEACEFUL && $$1.nextInt(1000) == 0) {
         List<net.minecraft.world.entity.EntityType<?>> $$2 = Lists.newArrayList(MOB_SOUND_MAP.keySet());
         return getImitatedSound($$2.get($$1.nextInt($$2.size())));
      } else {
         return SoundEvents.PARROT_AMBIENT;
      }
   }

   private static SoundEvent getImitatedSound(net.minecraft.world.entity.EntityType<?> $$0) {
      return MOB_SOUND_MAP.getOrDefault($$0, SoundEvents.PARROT_AMBIENT);
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.PARROT_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.PARROT_DEATH;
   }

   @Override
   protected void playStepSound(BlockPos $$0, BlockState $$1) {
      this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
   }

   @Override
   protected boolean isFlapping() {
      return this.flyDist > this.nextFlap;
   }

   @Override
   protected void onFlap() {
      this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
      this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
   }

   @Override
   public float getVoicePitch() {
      return getPitch(this.random);
   }

   public static float getPitch(RandomSource $$0) {
      return ($$0.nextFloat() - $$0.nextFloat()) * 0.2F + 1.0F;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.NEUTRAL;
   }

   @Override
   public boolean isPushable() {
      return true;
   }

   @Override
   protected void doPush(net.minecraft.world.entity.Entity $$0) {
      if (!($$0 instanceof Player)) {
         super.doPush($$0);
      }
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

   public Parrot.Variant getVariant() {
      return Parrot.Variant.byId((Integer)this.entityData.get(DATA_VARIANT_ID));
   }

   private void setVariant(Parrot.Variant $$0) {
      this.entityData.set(DATA_VARIANT_ID, $$0.id);
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.PARROT_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.PARROT_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.PARROT_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.PARROT_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_VARIANT_ID, Parrot.Variant.DEFAULT.id);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("Variant", Parrot.Variant.LEGACY_CODEC, this.getVariant());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setVariant($$0.read("Variant", Parrot.Variant.LEGACY_CODEC).orElse(Parrot.Variant.DEFAULT));
   }

   @Override
   public boolean isFlying() {
      return !this.onGround();
   }

   @Override
   protected boolean canFlyToOwner() {
      return true;
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.5F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
   }

   static class ParrotWanderGoal extends WaterAvoidingRandomFlyingGoal {
      public ParrotWanderGoal(net.minecraft.world.entity.PathfinderMob $$0, double $$1) {
         super($$0, $$1);
      }

      
      @Override
      protected Vec3 getPosition() {
         Vec3 $$0 = null;
         if (this.mob.isInWater()) {
            $$0 = LandRandomPos.getPos(this.mob, 15, 15);
         }

         if (this.mob.getRandom().nextFloat() >= this.probability) {
            $$0 = this.getTreePos();
         }

         return $$0 == null ? super.getPosition() : $$0;
      }

      
      private Vec3 getTreePos() {
         BlockPos $$0 = this.mob.blockPosition();
         MutableBlockPos $$1 = new MutableBlockPos();
         MutableBlockPos $$2 = new MutableBlockPos();

         for (BlockPos $$4 : BlockPos.betweenClosed(
            Mth.floor(this.mob.getX() - 3.0),
            Mth.floor(this.mob.getY() - 6.0),
            Mth.floor(this.mob.getZ() - 3.0),
            Mth.floor(this.mob.getX() + 3.0),
            Mth.floor(this.mob.getY() + 6.0),
            Mth.floor(this.mob.getZ() + 3.0)
         )) {
            if (!$$0.equals($$4)) {
               BlockState $$5 = this.mob.level().getBlockState($$2.setWithOffset($$4, Direction.DOWN));
               boolean $$6 = $$5.getBlock() instanceof LeavesBlock || $$5.is(BlockTags.LOGS);
               if ($$6 && this.mob.level().isEmptyBlock($$4) && this.mob.level().isEmptyBlock($$1.setWithOffset($$4, Direction.UP))) {
                  return Vec3.atBottomCenterOf($$4);
               }
            }
         }

         return null;
      }
   }

   public static enum Variant implements StringRepresentable {
      RED_BLUE(0, "red_blue"),
      BLUE(1, "blue"),
      GREEN(2, "green"),
      YELLOW_BLUE(3, "yellow_blue"),
      GRAY(4, "gray");

      public static final Parrot.Variant DEFAULT = RED_BLUE;
      private static final IntFunction<Parrot.Variant> BY_ID = ByIdMap.continuous(Parrot.Variant::getId, values(), OutOfBoundsStrategy.CLAMP);
      public static final Codec<Parrot.Variant> CODEC = StringRepresentable.fromEnum(Parrot.Variant::values);
      @Deprecated
      public static final Codec<Parrot.Variant> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Parrot.Variant::getId);
      public static final StreamCodec<ByteBuf, Parrot.Variant> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Parrot.Variant::getId);
      final int id;
      private final String name;

      private Variant(final int $$0, final String $$1) {
         this.id = $$0;
         this.name = $$1;
      }

      public int getId() {
         return this.id;
      }

      public static Parrot.Variant byId(int $$0) {
         return BY_ID.apply($$0);
      }

      public String getSerializedName() {
         return this.name;
      }
   }
}
