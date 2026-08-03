package net.minecraft.world.entity;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ExperienceOrb extends net.minecraft.world.entity.Entity {
   protected static final EntityDataAccessor<Integer> DATA_VALUE = SynchedEntityData.defineId(
      net.minecraft.world.entity.ExperienceOrb.class, EntityDataSerializers.INT
   );
   private static final int LIFETIME = 6000;
   private static final int ENTITY_SCAN_PERIOD = 20;
   private static final int MAX_FOLLOW_DIST = 8;
   private static final int ORB_GROUPS_PER_AREA = 40;
   private static final double ORB_MERGE_DISTANCE = 0.5;
   private static final short DEFAULT_HEALTH = 5;
   private static final short DEFAULT_AGE = 0;
   private static final short DEFAULT_VALUE = 0;
   private static final int DEFAULT_COUNT = 1;
   private int age = 0;
   private int health = 5;
   private int count = 1;
   @Nullable
   private Player followingPlayer;
   private final net.minecraft.world.entity.InterpolationHandler interpolation = new net.minecraft.world.entity.InterpolationHandler(this);

   public ExperienceOrb(Level $$0, double $$1, double $$2, double $$3, int $$4) {
      this($$0, new Vec3($$1, $$2, $$3), Vec3.ZERO, $$4);
   }

   public ExperienceOrb(Level $$0, Vec3 $$1, Vec3 $$2, int $$3) {
      this(net.minecraft.world.entity.EntityType.EXPERIENCE_ORB, $$0);
      this.setPos($$1);
      if (!$$0.isClientSide()) {
         this.setYRot(this.random.nextFloat() * 360.0F);
         Vec3 $$4 = new Vec3((this.random.nextDouble() * 0.2 - 0.1) * 2.0, this.random.nextDouble() * 0.2 * 2.0, (this.random.nextDouble() * 0.2 - 0.1) * 2.0);
         if ($$2.lengthSqr() > 0.0 && $$2.dot($$4) < 0.0) {
            $$4 = $$4.scale(-1.0);
         }

         double $$5 = this.getBoundingBox().getSize();
         this.setPos($$1.add($$2.normalize().scale($$5 * 0.5)));
         this.setDeltaMovement($$4);
         if (!$$0.noCollision(this.getBoundingBox())) {
            this.unstuckIfPossible($$5);
         }
      }

      this.setValue($$3);
   }

   public ExperienceOrb(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.ExperienceOrb> $$0, Level $$1) {
      super($$0, $$1);
   }

   protected void unstuckIfPossible(double $$0) {
      Vec3 $$1 = this.position().add(0.0, this.getBbHeight() / 2.0, 0.0);
      VoxelShape $$2 = Shapes.create(AABB.ofSize($$1, $$0, $$0, $$0));
      this.level()
         .findFreePosition(this, $$2, $$1, this.getBbWidth(), this.getBbHeight(), this.getBbWidth())
         .ifPresent($$0x -> this.setPos($$0x.add(0.0, -this.getBbHeight() / 2.0, 0.0)));
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.NONE;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_VALUE, 0);
   }

   @Override
   protected double getDefaultGravity() {
      return 0.03;
   }

   @Override
   public void tick() {
      this.interpolation.interpolate();
      if (this.firstTick && this.level().isClientSide()) {
         this.firstTick = false;
      } else {
         super.tick();
         boolean $$0 = !this.level().noCollision(this.getBoundingBox());
         if (this.isEyeInFluid(FluidTags.WATER)) {
            this.setUnderwaterMovement();
         } else if (!$$0) {
            this.applyGravity();
         }

         if (this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA)) {
            this.setDeltaMovement((this.random.nextFloat() - this.random.nextFloat()) * 0.2F, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }

         if (this.tickCount % 20 == 1) {
            this.scanForMerges();
         }

         this.followNearbyPlayer();
         if (this.followingPlayer == null && !this.level().isClientSide() && $$0) {
            boolean $$1 = !this.level().noCollision(this.getBoundingBox().move(this.getDeltaMovement()));
            if ($$1) {
               this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
               this.needsSync = true;
            }
         }

         double $$2 = this.getDeltaMovement().y;
         this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
         this.applyEffectsFromBlocks();
         float $$3 = 0.98F;
         if (this.onGround()) {
            $$3 = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.98F;
         }

         this.setDeltaMovement(this.getDeltaMovement().scale($$3));
         if (this.verticalCollisionBelow && $$2 < -this.getGravity()) {
            this.setDeltaMovement(new Vec3(this.getDeltaMovement().x, -$$2 * 0.4, this.getDeltaMovement().z));
         }

         this.age++;
         if (this.age >= 6000) {
            this.discard();
         }
      }
   }

   private void followNearbyPlayer() {
      if (this.followingPlayer == null || this.followingPlayer.isSpectator() || this.followingPlayer.distanceToSqr(this) > 64.0) {
         Player $$0 = this.level().getNearestPlayer(this, 8.0);
         if ($$0 != null && !$$0.isSpectator() && !$$0.isDeadOrDying()) {
            this.followingPlayer = $$0;
         } else {
            this.followingPlayer = null;
         }
      }

      if (this.followingPlayer != null) {
         Vec3 $$1 = new Vec3(
            this.followingPlayer.getX() - this.getX(),
            this.followingPlayer.getY() + this.followingPlayer.getEyeHeight() / 2.0 - this.getY(),
            this.followingPlayer.getZ() - this.getZ()
         );
         double $$2 = $$1.lengthSqr();
         double $$3 = 1.0 - Math.sqrt($$2) / 8.0;
         this.setDeltaMovement(this.getDeltaMovement().add($$1.normalize().scale($$3 * $$3 * 0.1)));
      }
   }

   @Override
   public BlockPos getBlockPosBelowThatAffectsMyMovement() {
      return this.getOnPos(0.999999F);
   }

   private void scanForMerges() {
      if (this.level() instanceof ServerLevel) {
         for (net.minecraft.world.entity.ExperienceOrb $$1 : this.level()
            .getEntities(EntityTypeTest.forClass(net.minecraft.world.entity.ExperienceOrb.class), this.getBoundingBox().inflate(0.5), this::canMerge)) {
            this.merge($$1);
         }
      }
   }

   public static void award(ServerLevel $$0, Vec3 $$1, int $$2) {
      awardWithDirection($$0, $$1, Vec3.ZERO, $$2);
   }

   public static void awardWithDirection(ServerLevel $$0, Vec3 $$1, Vec3 $$2, int $$3) {
      while ($$3 > 0) {
         int $$4 = getExperienceValue($$3);
         $$3 -= $$4;
         if (!tryMergeToExisting($$0, $$1, $$4)) {
            $$0.addFreshEntity(new net.minecraft.world.entity.ExperienceOrb($$0, $$1, $$2, $$4));
         }
      }
   }

   private static boolean tryMergeToExisting(ServerLevel $$0, Vec3 $$1, int $$2) {
      AABB $$3 = AABB.ofSize($$1, 1.0, 1.0, 1.0);
      int $$4 = $$0.getRandom().nextInt(40);
      List<net.minecraft.world.entity.ExperienceOrb> $$5 = $$0.getEntities(
         EntityTypeTest.forClass(net.minecraft.world.entity.ExperienceOrb.class), $$3, $$2x -> canMerge($$2x, $$4, $$2)
      );
      if (!$$5.isEmpty()) {
         net.minecraft.world.entity.ExperienceOrb $$6 = $$5.get(0);
         $$6.count++;
         $$6.age = 0;
         return true;
      } else {
         return false;
      }
   }

   private boolean canMerge(net.minecraft.world.entity.ExperienceOrb $$0) {
      return $$0 != this && canMerge($$0, this.getId(), this.getValue());
   }

   private static boolean canMerge(net.minecraft.world.entity.ExperienceOrb $$0, int $$1, int $$2) {
      return !$$0.isRemoved() && ($$0.getId() - $$1) % 40 == 0 && $$0.getValue() == $$2;
   }

   private void merge(net.minecraft.world.entity.ExperienceOrb $$0) {
      this.count = this.count + $$0.count;
      this.age = Math.min(this.age, $$0.age);
      $$0.discard();
   }

   private void setUnderwaterMovement() {
      Vec3 $$0 = this.getDeltaMovement();
      this.setDeltaMovement($$0.x * 0.99F, Math.min($$0.y + 5.0E-4F, 0.06F), $$0.z * 0.99F);
   }

   @Override
   protected void doWaterSplashEffect() {
   }

   @Override
   public final boolean hurtClient(DamageSource $$0) {
      return !this.isInvulnerableToBase($$0);
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      if (this.isInvulnerableToBase($$1)) {
         return false;
      } else {
         this.markHurt();
         this.health = (int)(this.health - $$2);
         if (this.health <= 0) {
            this.discard();
         }

         return true;
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.putShort("Health", (short)this.health);
      $$0.putShort("Age", (short)this.age);
      $$0.putShort("Value", (short)this.getValue());
      $$0.putInt("Count", this.count);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.health = $$0.getShortOr("Health", (short)5);
      this.age = $$0.getShortOr("Age", (short)0);
      this.setValue($$0.getShortOr("Value", (short)0));
      this.count = $$0.read("Count", ExtraCodecs.POSITIVE_INT).orElse(1);
   }

   @Override
   public void playerTouch(Player $$0) {
      if ($$0 instanceof ServerPlayer $$1) {
         if ($$0.takeXpDelay == 0) {
            $$0.takeXpDelay = 2;
            $$0.take(this, 1);
            int $$3 = this.repairPlayerItems($$1, this.getValue());
            if ($$3 > 0) {
               $$0.giveExperiencePoints($$3);
            }

            this.count--;
            if (this.count == 0) {
               this.discard();
            }
         }
      }
   }

   private int repairPlayerItems(ServerPlayer $$0, int $$1) {
      Optional<EnchantedItemInUse> $$2 = EnchantmentHelper.getRandomItemWith(EnchantmentEffectComponents.REPAIR_WITH_XP, $$0, ItemStack::isDamaged);
      if ($$2.isPresent()) {
         ItemStack $$3 = $$2.get().itemStack();
         int $$4 = EnchantmentHelper.modifyDurabilityToRepairFromXp($$0.level(), $$3, $$1);
         int $$5 = Math.min($$4, $$3.getDamageValue());
         $$3.setDamageValue($$3.getDamageValue() - $$5);
         if ($$5 > 0) {
            int $$6 = $$1 - $$5 * $$1 / $$4;
            if ($$6 > 0) {
               return this.repairPlayerItems($$0, $$6);
            }
         }

         return 0;
      } else {
         return $$1;
      }
   }

   public int getValue() {
      return (Integer)this.entityData.get(DATA_VALUE);
   }

   private void setValue(int $$0) {
      this.entityData.set(DATA_VALUE, $$0);
   }

   public int getIcon() {
      int $$0 = this.getValue();
      if ($$0 >= 2477) {
         return 10;
      } else if ($$0 >= 1237) {
         return 9;
      } else if ($$0 >= 617) {
         return 8;
      } else if ($$0 >= 307) {
         return 7;
      } else if ($$0 >= 149) {
         return 6;
      } else if ($$0 >= 73) {
         return 5;
      } else if ($$0 >= 37) {
         return 4;
      } else if ($$0 >= 17) {
         return 3;
      } else if ($$0 >= 7) {
         return 2;
      } else {
         return $$0 >= 3 ? 1 : 0;
      }
   }

   public static int getExperienceValue(int $$0) {
      if ($$0 >= 2477) {
         return 2477;
      } else if ($$0 >= 1237) {
         return 1237;
      } else if ($$0 >= 617) {
         return 617;
      } else if ($$0 >= 307) {
         return 307;
      } else if ($$0 >= 149) {
         return 149;
      } else if ($$0 >= 73) {
         return 73;
      } else if ($$0 >= 37) {
         return 37;
      } else if ($$0 >= 17) {
         return 17;
      } else if ($$0 >= 7) {
         return 7;
      } else {
         return $$0 >= 3 ? 3 : 1;
      }
   }

   @Override
   public boolean isAttackable() {
      return false;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.AMBIENT;
   }

   @Override
   public net.minecraft.world.entity.InterpolationHandler getInterpolation() {
      return this.interpolation;
   }
}
