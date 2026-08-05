package net.minecraft.world.entity.animal.rabbit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Rabbit extends Animal {
   public static final double STROLL_SPEED_MOD = 0.6;
   public static final double BREED_SPEED_MOD = 0.8;
   public static final double FOLLOW_SPEED_MOD = 1.0;
   public static final double FLEE_SPEED_MOD = 2.2;
   public static final double ATTACK_SPEED_MOD = 1.4;
   private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
   private static final int DEFAULT_MORE_CARROT_TICKS = 0;
   private static final Identifier KILLER_BUNNY = Identifier.withDefaultNamespace("killer_bunny");
   private static final int DEFAULT_ATTACK_POWER = 3;
   private static final int EVIL_ATTACK_POWER_INCREMENT = 5;
   private static final Identifier EVIL_ATTACK_POWER_MODIFIER = Identifier.withDefaultNamespace("evil");
   private static final int EVIL_ARMOR_VALUE = 8;
   private static final int MORE_CARROTS_DELAY = 40;
   private int jumpTicks;
   private int jumpDuration;
   private boolean wasOnGround;
   private int jumpDelayTicks;
   int moreCarrotTicks = 0;

   public Rabbit(net.minecraft.world.entity.EntityType<? extends Rabbit> $$0, Level $$1) {
      super($$0, $$1);
      this.jumpControl = new Rabbit.RabbitJumpControl(this);
      this.moveControl = new Rabbit.RabbitMoveControl(this);
      this.setSpeedModifier(0.0);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
      this.goalSelector.addGoal(1, new Rabbit.RabbitPanicGoal(this, 2.2));
      this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, $$0 -> $$0.is(ItemTags.RABBIT_FOOD), false));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Player.class, 8.0F, 2.2, 2.2));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Wolf.class, 10.0F, 2.2, 2.2));
      this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Monster.class, 4.0F, 2.2, 2.2));
      this.goalSelector.addGoal(5, new Rabbit.RaidGardenGoal(this));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
   }

   @Override
   protected float getJumpPower() {
      float $$0 = 0.3F;
      if (this.moveControl.getSpeedModifier() <= 0.6) {
         $$0 = 0.2F;
      }

      Path $$1 = this.navigation.getPath();
      if ($$1 != null && !$$1.isDone()) {
         Vec3 $$2 = $$1.getNextEntityPos(this);
         if ($$2.y > this.getY() + 0.5) {
            $$0 = 0.5F;
         }
      }

      if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + 0.5) {
         $$0 = 0.5F;
      }

      return super.getJumpPower($$0 / 0.42F);
   }

   @Override
   public void jumpFromGround() {
      super.jumpFromGround();
      double $$0 = this.moveControl.getSpeedModifier();
      if ($$0 > 0.0) {
         double $$1 = this.getDeltaMovement().horizontalDistanceSqr();
         if ($$1 < 0.01) {
            this.moveRelative(0.1F, new Vec3(0.0, 0.0, 1.0));
         }
      }

      if (!this.level().isClientSide()) {
         this.level().broadcastEntityEvent(this, (byte)1);
      }
   }

   public float getJumpCompletion(float $$0) {
      return this.jumpDuration == 0 ? 0.0F : (this.jumpTicks + $$0) / this.jumpDuration;
   }

   public void setSpeedModifier(double $$0) {
      this.getNavigation().setSpeedModifier($$0);
      this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), $$0);
   }

   @Override
   public void setJumping(boolean $$0) {
      super.setJumping($$0);
      if ($$0) {
         this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
      }
   }

   public void startJumping() {
      this.setJumping(true);
      this.jumpDuration = 10;
      this.jumpTicks = 0;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_TYPE_ID, Rabbit.Variant.DEFAULT.id);
   }

   @Override
   public void customServerAiStep(ServerLevel $$0) {
      if (this.jumpDelayTicks > 0) {
         this.jumpDelayTicks--;
      }

      if (this.moreCarrotTicks > 0) {
         this.moreCarrotTicks = this.moreCarrotTicks - this.random.nextInt(3);
         if (this.moreCarrotTicks < 0) {
            this.moreCarrotTicks = 0;
         }
      }

      if (this.onGround()) {
         if (!this.wasOnGround) {
            this.setJumping(false);
            this.checkLandingDelay();
         }

         if (this.getVariant() == Rabbit.Variant.EVIL && this.jumpDelayTicks == 0) {
            net.minecraft.world.entity.LivingEntity $$1 = this.getTarget();
            if ($$1 != null && this.distanceToSqr($$1) < 16.0) {
               this.facePoint($$1.getX(), $$1.getZ());
               this.moveControl.setWantedPosition($$1.getX(), $$1.getY(), $$1.getZ(), this.moveControl.getSpeedModifier());
               this.startJumping();
               this.wasOnGround = true;
            }
         }

         Rabbit.RabbitJumpControl $$2 = (Rabbit.RabbitJumpControl)this.jumpControl;
         if (!$$2.wantJump()) {
            if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
               Path $$3 = this.navigation.getPath();
               Vec3 $$4 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
               if ($$3 != null && !$$3.isDone()) {
                  $$4 = $$3.getNextEntityPos(this);
               }

               this.facePoint($$4.x, $$4.z);
               this.startJumping();
            }
         } else if (!$$2.canJump()) {
            this.enableJumpControl();
         }
      }

      this.wasOnGround = this.onGround();
   }

   @Override
   public boolean canSpawnSprintParticle() {
      return false;
   }

   private void facePoint(double $$0, double $$1) {
      this.setYRot((float)(Mth.atan2($$1 - this.getZ(), $$0 - this.getX()) * 180.0F / (float)Math.PI) - 90.0F);
   }

   private void enableJumpControl() {
      ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(true);
   }

   private void disableJumpControl() {
      ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(false);
   }

   private void setLandingDelay() {
      if (this.moveControl.getSpeedModifier() < 2.2) {
         this.jumpDelayTicks = 10;
      } else {
         this.jumpDelayTicks = 1;
      }
   }

   private void checkLandingDelay() {
      this.setLandingDelay();
      this.disableJumpControl();
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.jumpTicks != this.jumpDuration) {
         this.jumpTicks++;
      } else if (this.jumpDuration != 0) {
         this.jumpTicks = 0;
         this.jumpDuration = 0;
         this.setJumping(false);
      }
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.store("RabbitType", Rabbit.Variant.LEGACY_CODEC, this.getVariant());
      $$0.putInt("MoreCarrotTicks", this.moreCarrotTicks);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setVariant($$0.read("RabbitType", Rabbit.Variant.LEGACY_CODEC).orElse(Rabbit.Variant.DEFAULT));
      this.moreCarrotTicks = $$0.getIntOr("MoreCarrotTicks", 0);
   }

   protected SoundEvent getJumpSound() {
      return SoundEvents.RABBIT_JUMP;
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.RABBIT_AMBIENT;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.RABBIT_HURT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.RABBIT_DEATH;
   }

   @Override
   public void playAttackSound() {
      if (this.getVariant() == Rabbit.Variant.EVIL) {
         this.playSound(SoundEvents.RABBIT_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
      }
   }

   @Override
   public SoundSource getSoundSource() {
      return this.getVariant() == Rabbit.Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
   }

   
   public Rabbit getBreedOffspring(ServerLevel $$0, net.minecraft.world.entity.AgeableMob $$1) {
      Rabbit $$2 = net.minecraft.world.entity.EntityType.RABBIT.create($$0, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
      if ($$2 != null) {
         Rabbit.Variant $$3 = getRandomRabbitVariant($$0, this.blockPosition());
         if (this.random.nextInt(20) != 0) {
            if ($$1 instanceof Rabbit $$4 && this.random.nextBoolean()) {
               $$3 = $$4.getVariant();
            } else {
               $$3 = this.getVariant();
            }
         }

         $$2.setVariant($$3);
      }

      return $$2;
   }

   @Override
   public boolean isFood(ItemStack $$0) {
      return $$0.is(ItemTags.RABBIT_FOOD);
   }

   public Rabbit.Variant getVariant() {
      return Rabbit.Variant.byId((Integer)this.entityData.get(DATA_TYPE_ID));
   }

   private void setVariant(Rabbit.Variant $$0) {
      if ($$0 == Rabbit.Variant.EVIL) {
         this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
         this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.4, true));
         this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
         this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
         this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
         this.getAttribute(Attributes.ATTACK_DAMAGE)
            .addOrUpdateTransientModifier(new AttributeModifier(EVIL_ATTACK_POWER_MODIFIER, 5.0, AttributeModifier.Operation.ADD_VALUE));
         if (!this.hasCustomName()) {
            this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
         }
      } else {
         this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(EVIL_ATTACK_POWER_MODIFIER);
      }

      this.entityData.set(DATA_TYPE_ID, $$0.id);
   }

   
   @Override
   public <T> T get(DataComponentType<? extends T> $$0) {
      return $$0 == DataComponents.RABBIT_VARIANT ? castComponentValue((DataComponentType<T>)$$0, this.getVariant()) : super.get($$0);
   }

   @Override
   protected void applyImplicitComponents(DataComponentGetter $$0) {
      this.applyImplicitComponentIfPresent($$0, DataComponents.RABBIT_VARIANT);
      super.applyImplicitComponents($$0);
   }

   @Override
   protected <T> boolean applyImplicitComponent(DataComponentType<T> $$0, T $$1) {
      if ($$0 == DataComponents.RABBIT_VARIANT) {
         this.setVariant(castComponentValue(DataComponents.RABBIT_VARIANT, $$1));
         return true;
      } else {
         return super.applyImplicitComponent($$0, $$1);
      }
   }

   
   @Override
   public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
      ServerLevelAccessor $$0,
      DifficultyInstance $$1,
      net.minecraft.world.entity.EntitySpawnReason $$2,
      net.minecraft.world.entity.SpawnGroupData $$3
   ) {
      Rabbit.Variant $$4 = getRandomRabbitVariant($$0, this.blockPosition());
      if ($$3 instanceof Rabbit.RabbitGroupData) {
         $$4 = ((Rabbit.RabbitGroupData)$$3).variant;
      } else {
         $$3 = new Rabbit.RabbitGroupData($$4);
      }

      this.setVariant($$4);
      return super.finalizeSpawn($$0, $$1, $$2, $$3);
   }

   private static Rabbit.Variant getRandomRabbitVariant(LevelAccessor $$0, BlockPos $$1) {
      Holder<Biome> $$2 = $$0.getBiome($$1);
      int $$3 = $$0.getRandom().nextInt(100);
      if ($$2.is(BiomeTags.SPAWNS_WHITE_RABBITS)) {
         return $$3 < 80 ? Rabbit.Variant.WHITE : Rabbit.Variant.WHITE_SPLOTCHED;
      } else if ($$2.is(BiomeTags.SPAWNS_GOLD_RABBITS)) {
         return Rabbit.Variant.GOLD;
      } else {
         return $$3 < 50 ? Rabbit.Variant.BROWN : ($$3 < 90 ? Rabbit.Variant.SALT : Rabbit.Variant.BLACK);
      }
   }

   public static boolean checkRabbitSpawnRules(
      net.minecraft.world.entity.EntityType<Rabbit> $$0, LevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      return $$1.getBlockState($$3.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && isBrightEnoughToSpawn($$1, $$3);
   }

   boolean wantsMoreFood() {
      return this.moreCarrotTicks <= 0;
   }

   @Override
   public void handleEntityEvent(byte $$0) {
      if ($$0 == 1) {
         this.spawnSprintParticle();
         this.jumpDuration = 10;
         this.jumpTicks = 0;
      } else {
         super.handleEntityEvent($$0);
      }
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.6F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
   }

   static class RabbitAvoidEntityGoal<T extends net.minecraft.world.entity.LivingEntity> extends AvoidEntityGoal<T> {
      private final Rabbit rabbit;

      public RabbitAvoidEntityGoal(Rabbit $$0, Class<T> $$1, float $$2, double $$3, double $$4) {
         super($$0, $$1, $$2, $$3, $$4);
         this.rabbit = $$0;
      }

      @Override
      public boolean canUse() {
         return this.rabbit.getVariant() != Rabbit.Variant.EVIL && super.canUse();
      }
   }

   public static class RabbitGroupData extends net.minecraft.world.entity.AgeableMob.AgeableMobGroupData {
      public final Rabbit.Variant variant;

      public RabbitGroupData(Rabbit.Variant $$0) {
         super(1.0F);
         this.variant = $$0;
      }
   }

   public static class RabbitJumpControl extends JumpControl {
      private final Rabbit rabbit;
      private boolean canJump;

      public RabbitJumpControl(Rabbit $$0) {
         super($$0);
         this.rabbit = $$0;
      }

      public boolean wantJump() {
         return this.jump;
      }

      public boolean canJump() {
         return this.canJump;
      }

      public void setCanJump(boolean $$0) {
         this.canJump = $$0;
      }

      @Override
      public void tick() {
         if (this.jump) {
            this.rabbit.startJumping();
            this.jump = false;
         }
      }
   }

   static class RabbitMoveControl extends MoveControl {
      private final Rabbit rabbit;
      private double nextJumpSpeed;

      public RabbitMoveControl(Rabbit $$0) {
         super($$0);
         this.rabbit = $$0;
      }

      @Override
      public void tick() {
         if (this.rabbit.onGround() && !this.rabbit.jumping && !((Rabbit.RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
            this.rabbit.setSpeedModifier(0.0);
         } else if (this.hasWanted() || this.operation == MoveControl.Operation.JUMPING) {
            this.rabbit.setSpeedModifier(this.nextJumpSpeed);
         }

         super.tick();
      }

      @Override
      public void setWantedPosition(double $$0, double $$1, double $$2, double $$3) {
         if (this.rabbit.isInWater()) {
            $$3 = 1.5;
         }

         super.setWantedPosition($$0, $$1, $$2, $$3);
         if ($$3 > 0.0) {
            this.nextJumpSpeed = $$3;
         }
      }
   }

   static class RabbitPanicGoal extends PanicGoal {
      private final Rabbit rabbit;

      public RabbitPanicGoal(Rabbit $$0, double $$1) {
         super($$0, $$1);
         this.rabbit = $$0;
      }

      @Override
      public void tick() {
         super.tick();
         this.rabbit.setSpeedModifier(this.speedModifier);
      }
   }

   static class RaidGardenGoal extends MoveToBlockGoal {
      private final Rabbit rabbit;
      private boolean wantsToRaid;
      private boolean canRaid;

      public RaidGardenGoal(Rabbit $$0) {
         super($$0, 0.7F, 16);
         this.rabbit = $$0;
      }

      @Override
      public boolean canUse() {
         if (this.nextStartTick <= 0) {
            if (!(Boolean)getServerLevel(this.rabbit).getGameRules().get(GameRules.MOB_GRIEFING)) {
               return false;
            }

            this.canRaid = false;
            this.wantsToRaid = this.rabbit.wantsMoreFood();
         }

         return super.canUse();
      }

      @Override
      public boolean canContinueToUse() {
         return this.canRaid && super.canContinueToUse();
      }

      @Override
      public void tick() {
         super.tick();
         this.rabbit
            .getLookControl()
            .setLookAt(this.blockPos.getX() + 0.5, this.blockPos.getY() + 1, this.blockPos.getZ() + 0.5, 10.0F, this.rabbit.getMaxHeadXRot());
         if (this.isReachedTarget()) {
            Level $$0 = this.rabbit.level();
            BlockPos $$1 = this.blockPos.above();
            BlockState $$2 = $$0.getBlockState($$1);
            Block $$3 = $$2.getBlock();
            if (this.canRaid && $$3 instanceof CarrotBlock) {
               int $$4 = (Integer)$$2.getValue(CarrotBlock.AGE);
               if ($$4 == 0) {
                  $$0.setBlock($$1, Blocks.AIR.defaultBlockState(), 2);
                  $$0.destroyBlock($$1, true, this.rabbit);
               } else {
                  $$0.setBlock($$1, (BlockState)$$2.setValue(CarrotBlock.AGE, $$4 - 1), 2);
                  $$0.gameEvent(GameEvent.BLOCK_CHANGE, $$1, Context.of(this.rabbit));
                  $$0.levelEvent(2001, $$1, Block.getId($$2));
               }

               this.rabbit.moreCarrotTicks = 40;
            }

            this.canRaid = false;
            this.nextStartTick = 10;
         }
      }

      @Override
      protected boolean isValidTarget(LevelReader $$0, BlockPos $$1) {
         BlockState $$2 = $$0.getBlockState($$1);
         if ($$2.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid) {
            $$2 = $$0.getBlockState($$1.above());
            if ($$2.getBlock() instanceof CarrotBlock && ((CarrotBlock)$$2.getBlock()).isMaxAge($$2)) {
               this.canRaid = true;
               return true;
            }
         }

         return false;
      }
   }

   public static enum Variant implements StringRepresentable {
      BROWN(0, "brown"),
      WHITE(1, "white"),
      BLACK(2, "black"),
      WHITE_SPLOTCHED(3, "white_splotched"),
      GOLD(4, "gold"),
      SALT(5, "salt"),
      EVIL(99, "evil");

      public static final Rabbit.Variant DEFAULT = BROWN;
      private static final IntFunction<Rabbit.Variant> BY_ID = ByIdMap.sparse(Rabbit.Variant::id, values(), DEFAULT);
      public static final Codec<Rabbit.Variant> CODEC = StringRepresentable.fromEnum(Rabbit.Variant::values);
      @Deprecated
      public static final Codec<Rabbit.Variant> LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, Rabbit.Variant::id);
      public static final StreamCodec<ByteBuf, Rabbit.Variant> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Rabbit.Variant::id);
      final int id;
      private final String name;

      private Variant(final int $$0, final String $$1) {
         this.id = $$0;
         this.name = $$1;
      }

      public String getSerializedName() {
         return this.name;
      }

      public int id() {
         return this.id;
      }

      public static Rabbit.Variant byId(int $$0) {
         return BY_ID.apply($$0);
      }
   }
}
