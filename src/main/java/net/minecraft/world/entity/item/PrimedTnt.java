package net.minecraft.world.entity.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class PrimedTnt extends net.minecraft.world.entity.Entity implements net.minecraft.world.entity.TraceableEntity {
   private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.BLOCK_STATE);
   private static final short DEFAULT_FUSE_TIME = 80;
   private static final float DEFAULT_EXPLOSION_POWER = 4.0F;
   private static final BlockState DEFAULT_BLOCK_STATE = Blocks.TNT.defaultBlockState();
   private static final String TAG_BLOCK_STATE = "block_state";
   public static final String TAG_FUSE = "fuse";
   private static final String TAG_EXPLOSION_POWER = "explosion_power";
   private static final ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
      public boolean shouldBlockExplode(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, float $$4) {
         return $$3.is(Blocks.NETHER_PORTAL) ? false : super.shouldBlockExplode($$0, $$1, $$2, $$3, $$4);
      }

      public Optional<Float> getBlockExplosionResistance(Explosion $$0, BlockGetter $$1, BlockPos $$2, BlockState $$3, FluidState $$4) {
         return $$3.is(Blocks.NETHER_PORTAL) ? Optional.empty() : super.getBlockExplosionResistance($$0, $$1, $$2, $$3, $$4);
      }
   };
   @Nullable
   private net.minecraft.world.entity.EntityReference<net.minecraft.world.entity.LivingEntity> owner;
   private boolean usedPortal;
   private float explosionPower = 4.0F;

   public PrimedTnt(net.minecraft.world.entity.EntityType<? extends PrimedTnt> $$0, Level $$1) {
      super($$0, $$1);
      this.blocksBuilding = true;
   }

   public PrimedTnt(Level $$0, double $$1, double $$2, double $$3, @Nullable net.minecraft.world.entity.LivingEntity $$4) {
      this(net.minecraft.world.entity.EntityType.TNT, $$0);
      this.setPos($$1, $$2, $$3);
      double $$5 = $$0.random.nextDouble() * (float) (Math.PI * 2);
      this.setDeltaMovement(-Math.sin($$5) * 0.02, 0.2F, -Math.cos($$5) * 0.02);
      this.setFuse(80);
      this.xo = $$1;
      this.yo = $$2;
      this.zo = $$3;
      this.owner = net.minecraft.world.entity.EntityReference.of($$4);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      $$0.define(DATA_FUSE_ID, 80);
      $$0.define(DATA_BLOCK_STATE_ID, DEFAULT_BLOCK_STATE);
   }

   @Override
   protected net.minecraft.world.entity.Entity.MovementEmission getMovementEmission() {
      return net.minecraft.world.entity.Entity.MovementEmission.NONE;
   }

   @Override
   public boolean isPickable() {
      return !this.isRemoved();
   }

   @Override
   protected double getDefaultGravity() {
      return 0.04;
   }

   @Override
   public void tick() {
      this.handlePortal();
      this.applyGravity();
      this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
      this.applyEffectsFromBlocks();
      this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
      if (this.onGround()) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
      }

      int $$0 = this.getFuse() - 1;
      this.setFuse($$0);
      if ($$0 <= 0) {
         this.discard();
         if (!this.level().isClientSide()) {
            this.explode();
         }
      } else {
         this.updateInWaterStateAndDoFluidPushing();
         if (this.level().isClientSide()) {
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
         }
      }
   }

   private void explode() {
      if (this.level() instanceof ServerLevel $$0 && (Boolean)$$0.getGameRules().get(GameRules.TNT_EXPLODES)) {
         this.level()
            .explode(
               this,
               Explosion.getDefaultDamageSource(this.level(), this),
               this.usedPortal ? USED_PORTAL_DAMAGE_CALCULATOR : null,
               this.getX(),
               this.getY(0.0625),
               this.getZ(),
               this.explosionPower,
               false,
               ExplosionInteraction.TNT
            );
      }
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      $$0.putShort("fuse", (short)this.getFuse());
      $$0.store("block_state", BlockState.CODEC, this.getBlockState());
      if (this.explosionPower != 4.0F) {
         $$0.putFloat("explosion_power", this.explosionPower);
      }

      net.minecraft.world.entity.EntityReference.store(this.owner, $$0, "owner");
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      this.setFuse($$0.getShortOr("fuse", (short)80));
      this.setBlockState($$0.read("block_state", BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE));
      this.explosionPower = Mth.clamp($$0.getFloatOr("explosion_power", 4.0F), 0.0F, 128.0F);
      this.owner = net.minecraft.world.entity.EntityReference.read($$0, "owner");
   }

   @Nullable
   public net.minecraft.world.entity.LivingEntity getOwner() {
      return net.minecraft.world.entity.EntityReference.getLivingEntity(this.owner, this.level());
   }

   @Override
   public void restoreFrom(net.minecraft.world.entity.Entity $$0) {
      super.restoreFrom($$0);
      if ($$0 instanceof PrimedTnt $$1) {
         this.owner = $$1.owner;
      }
   }

   public void setFuse(int $$0) {
      this.entityData.set(DATA_FUSE_ID, $$0);
   }

   public int getFuse() {
      return (Integer)this.entityData.get(DATA_FUSE_ID);
   }

   public void setBlockState(BlockState $$0) {
      this.entityData.set(DATA_BLOCK_STATE_ID, $$0);
   }

   public BlockState getBlockState() {
      return (BlockState)this.entityData.get(DATA_BLOCK_STATE_ID);
   }

   private void setUsedPortal(boolean $$0) {
      this.usedPortal = $$0;
   }

   @Nullable
   @Override
   public net.minecraft.world.entity.Entity teleport(TeleportTransition $$0) {
      net.minecraft.world.entity.Entity $$1 = super.teleport($$0);
      if ($$1 instanceof PrimedTnt $$2) {
         $$2.setUsedPortal(true);
      }

      return $$1;
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return false;
   }
}
