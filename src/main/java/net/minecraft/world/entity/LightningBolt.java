package net.minecraft.world.entity;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LightningBolt extends net.minecraft.world.entity.Entity {
   private static final int START_LIFE = 2;
   private static final double DAMAGE_RADIUS = 3.0;
   private static final double DETECTION_RADIUS = 15.0;
   private int life;
   public long seed;
   private int flashes;
   private boolean visualOnly;
   @Nullable
   private ServerPlayer cause;
   private final Set<net.minecraft.world.entity.Entity> hitEntities = Sets.newHashSet();
   private int blocksSetOnFire;

   public LightningBolt(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LightningBolt> $$0, Level $$1) {
      super($$0, $$1);
      this.life = 2;
      this.seed = this.random.nextLong();
      this.flashes = this.random.nextInt(3) + 1;
   }

   public void setVisualOnly(boolean $$0) {
      this.visualOnly = $$0;
   }

   @Override
   public SoundSource getSoundSource() {
      return SoundSource.WEATHER;
   }

   @Nullable
   public ServerPlayer getCause() {
      return this.cause;
   }

   public void setCause(@Nullable ServerPlayer $$0) {
      this.cause = $$0;
   }

   private void powerLightningRod() {
      BlockPos $$0 = this.getStrikePosition();
      BlockState $$1 = this.level().getBlockState($$0);
      if ($$1.getBlock() instanceof LightningRodBlock $$2) {
         $$2.onLightningStrike($$1, this.level(), $$0);
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.life == 2) {
         if (this.level().isClientSide()) {
            this.level()
               .playLocalSound(
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  SoundEvents.LIGHTNING_BOLT_THUNDER,
                  SoundSource.WEATHER,
                  10000.0F,
                  0.8F + this.random.nextFloat() * 0.2F,
                  false
               );
            this.level()
               .playLocalSound(
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  SoundEvents.LIGHTNING_BOLT_IMPACT,
                  SoundSource.WEATHER,
                  2.0F,
                  0.5F + this.random.nextFloat() * 0.2F,
                  false
               );
         } else {
            Difficulty $$0 = this.level().getDifficulty();
            if ($$0 == Difficulty.NORMAL || $$0 == Difficulty.HARD) {
               this.spawnFire(4);
            }

            this.powerLightningRod();
            clearCopperOnLightningStrike(this.level(), this.getStrikePosition());
            this.gameEvent(GameEvent.LIGHTNING_STRIKE);
         }
      }

      this.life--;
      if (this.life < 0) {
         if (this.flashes == 0) {
            if (this.level() instanceof ServerLevel) {
               List<net.minecraft.world.entity.Entity> $$1 = this.level()
                  .getEntities(
                     this,
                     new AABB(this.getX() - 15.0, this.getY() - 15.0, this.getZ() - 15.0, this.getX() + 15.0, this.getY() + 6.0 + 15.0, this.getZ() + 15.0),
                     $$0 -> $$0.isAlive() && !this.hitEntities.contains($$0)
                  );

               for (ServerPlayer $$2 : ((ServerLevel)this.level()).getPlayers($$0 -> $$0.distanceTo(this) < 256.0F)) {
                  CriteriaTriggers.LIGHTNING_STRIKE.trigger($$2, this, $$1);
               }
            }

            this.discard();
         } else if (this.life < -this.random.nextInt(10)) {
            this.flashes--;
            this.life = 1;
            this.seed = this.random.nextLong();
            this.spawnFire(0);
         }
      }

      if (this.life >= 0) {
         if (!(this.level() instanceof ServerLevel)) {
            this.level().setSkyFlashTime(2);
         } else if (!this.visualOnly) {
            List<net.minecraft.world.entity.Entity> $$3 = this.level()
               .getEntities(
                  this,
                  new AABB(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0),
                  net.minecraft.world.entity.Entity::isAlive
               );

            for (net.minecraft.world.entity.Entity $$4 : $$3) {
               $$4.thunderHit((ServerLevel)this.level(), this);
            }

            this.hitEntities.addAll($$3);
            if (this.cause != null) {
               CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, $$3);
            }
         }
      }
   }

   private BlockPos getStrikePosition() {
      Vec3 $$0 = this.position();
      return BlockPos.containing($$0.x, $$0.y - 1.0E-6, $$0.z);
   }

   private void spawnFire(int $$0) {
      if (!this.visualOnly && this.level() instanceof ServerLevel $$1) {
         BlockPos var7 = this.blockPosition();
         if ($$1.canSpreadFireAround(var7)) {
            BlockState $$4 = BaseFireBlock.getState($$1, var7);
            if ($$1.getBlockState(var7).isAir() && $$4.canSurvive($$1, var7)) {
               $$1.setBlockAndUpdate(var7, $$4);
               this.blocksSetOnFire++;
            }

            for (int $$5 = 0; $$5 < $$0; $$5++) {
               BlockPos $$6 = var7.offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
               $$4 = BaseFireBlock.getState($$1, $$6);
               if ($$1.getBlockState($$6).isAir() && $$4.canSurvive($$1, $$6)) {
                  $$1.setBlockAndUpdate($$6, $$4);
                  this.blocksSetOnFire++;
               }
            }
         }
      }
   }

   private static void clearCopperOnLightningStrike(Level $$0, BlockPos $$1) {
      BlockState $$2 = $$0.getBlockState($$1);
      boolean $$3 = ((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).get($$2.getBlock()) != null;
      boolean $$4 = $$2.getBlock() instanceof WeatheringCopper;
      if ($$4 || $$3) {
         if ($$4) {
            $$0.setBlockAndUpdate($$1, WeatheringCopper.getFirst($$0.getBlockState($$1)));
         }

         MutableBlockPos $$5 = $$1.mutable();
         int $$6 = $$0.random.nextInt(3) + 3;

         for (int $$7 = 0; $$7 < $$6; $$7++) {
            int $$8 = $$0.random.nextInt(8) + 1;
            randomWalkCleaningCopper($$0, $$1, $$5, $$8);
         }
      }
   }

   private static void randomWalkCleaningCopper(Level $$0, BlockPos $$1, MutableBlockPos $$2, int $$3) {
      $$2.set($$1);

      for (int $$4 = 0; $$4 < $$3; $$4++) {
         Optional<BlockPos> $$5 = randomStepCleaningCopper($$0, $$2);
         if ($$5.isEmpty()) {
            break;
         }

         $$2.set((Vec3i)$$5.get());
      }
   }

   private static Optional<BlockPos> randomStepCleaningCopper(Level $$0, BlockPos $$1) {
      for (BlockPos $$2 : BlockPos.randomInCube($$0.random, 10, $$1, 1)) {
         BlockState $$3 = $$0.getBlockState($$2);
         if ($$3.getBlock() instanceof WeatheringCopper) {
            WeatheringCopper.getPrevious($$3).ifPresent($$2x -> $$0.setBlockAndUpdate($$2, $$2x));
            $$0.levelEvent(3002, $$2, -1);
            return Optional.of($$2);
         }
      }

      return Optional.empty();
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double $$0) {
      double $$1 = 64.0 * getViewScale();
      return $$0 < $$1 * $$1;
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
   }

   public int getBlocksSetOnFire() {
      return this.blocksSetOnFire;
   }

   public Stream<net.minecraft.world.entity.Entity> getHitEntities() {
      return this.hitEntities.stream().filter(net.minecraft.world.entity.Entity::isAlive);
   }

   @Override
   public final boolean hurtServer(ServerLevel $$0, DamageSource $$1, float $$2) {
      return false;
   }
}
