package net.minecraft.world.entity.animal.golem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SnowGolem extends AbstractGolem implements net.minecraft.world.entity.Shearable, RangedAttackMob {
   private static final EntityDataAccessor<Byte> DATA_PUMPKIN_ID = SynchedEntityData.defineId(SnowGolem.class, EntityDataSerializers.BYTE);
   private static final byte PUMPKIN_FLAG = 16;
   private static final boolean DEFAULT_PUMPKIN = true;

   public SnowGolem(net.minecraft.world.entity.EntityType<? extends SnowGolem> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new RangedAttackGoal(this, 1.25, 20, 10.0F));
      this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0, 1.0000001E-5F));
      this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
      this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
      this.targetSelector
         .addGoal(1, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, ($$0, $$1) -> $$0 instanceof Enemy));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return net.minecraft.world.entity.Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_PUMPKIN_ID, (byte)16);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putBoolean("Pumpkin", this.hasPumpkin());
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.setPumpkin($$0.getBooleanOr("Pumpkin", true));
   }

   @Override
   public boolean isSensitiveToWater() {
      return true;
   }

   @Override
   public void aiStep() {
      super.aiStep();
      if (this.level() instanceof ServerLevel $$0) {
         if ((Boolean)$$0.environmentAttributes().getValue(EnvironmentAttributes.SNOW_GOLEM_MELTS, this.position())) {
            this.hurtServer($$0, this.damageSources().onFire(), 1.0F);
         }

         if (!(Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING)) {
            return;
         }

         BlockState $$1 = Blocks.SNOW.defaultBlockState();

         for (int $$2 = 0; $$2 < 4; $$2++) {
            int $$3 = Mth.floor(this.getX() + ($$2 % 2 * 2 - 1) * 0.25F);
            int $$4 = Mth.floor(this.getY());
            int $$5 = Mth.floor(this.getZ() + ($$2 / 2 % 2 * 2 - 1) * 0.25F);
            BlockPos $$6 = new BlockPos($$3, $$4, $$5);
            if (this.level().getBlockState($$6).isAir() && $$1.canSurvive(this.level(), $$6)) {
               this.level().setBlockAndUpdate($$6, $$1);
               this.level().gameEvent(GameEvent.BLOCK_PLACE, $$6, Context.of(this, $$1));
            }
         }
      }
   }

   @Override
   public void performRangedAttack(net.minecraft.world.entity.LivingEntity $$0, float $$1) {
      double $$2 = $$0.getX() - this.getX();
      double $$3 = $$0.getEyeY() - 1.1F;
      double $$4 = $$0.getZ() - this.getZ();
      double $$5 = Math.sqrt($$2 * $$2 + $$4 * $$4) * 0.2F;
      if (this.level() instanceof ServerLevel $$6) {
         ItemStack $$7 = new ItemStack(Items.SNOWBALL);
         Projectile.spawnProjectile(new Snowball($$6, this, $$7), $$6, $$7, $$4x -> $$4x.shoot($$2, $$3 + $$5 - $$4x.getY(), $$4, 1.6F, 12.0F));
      }

      this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
   }

   @Override
   protected InteractionResult mobInteract(Player $$0, InteractionHand $$1) {
      ItemStack $$2 = $$0.getItemInHand($$1);
      if ($$2.is(Items.SHEARS) && this.readyForShearing()) {
         if (this.level() instanceof ServerLevel $$3) {
            this.shear($$3, SoundSource.PLAYERS, $$2);
            this.gameEvent(GameEvent.SHEAR, $$0);
            $$2.hurtAndBreak(1, $$0, $$1.asEquipmentSlot());
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Override
   public void shear(ServerLevel $$0, SoundSource $$1, ItemStack $$2) {
      $$0.playSound(null, this, SoundEvents.SNOW_GOLEM_SHEAR, $$1, 1.0F, 1.0F);
      this.setPumpkin(false);
      this.dropFromShearingLootTable($$0, BuiltInLootTables.SHEAR_SNOW_GOLEM, $$2, ($$0x, $$1x) -> this.spawnAtLocation($$0x, $$1x, this.getEyeHeight()));
   }

   @Override
   public boolean readyForShearing() {
      return this.isAlive() && this.hasPumpkin();
   }

   public boolean hasPumpkin() {
      return ((Byte)this.entityData.get(DATA_PUMPKIN_ID) & 16) != 0;
   }

   public void setPumpkin(boolean $$0) {
      byte $$1 = (Byte)this.entityData.get(DATA_PUMPKIN_ID);
      if ($$0) {
         this.entityData.set(DATA_PUMPKIN_ID, (byte)($$1 | 16));
      } else {
         this.entityData.set(DATA_PUMPKIN_ID, (byte)($$1 & -17));
      }
   }

   @Nullable
   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.SNOW_GOLEM_AMBIENT;
   }

   @Nullable
   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.SNOW_GOLEM_HURT;
   }

   @Nullable
   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.SNOW_GOLEM_DEATH;
   }

   @Override
   public Vec3 getLeashOffset() {
      return new Vec3(0.0, 0.75F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
   }
}
