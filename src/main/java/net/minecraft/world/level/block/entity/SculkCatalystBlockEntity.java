package net.minecraft.world.level.block.entity;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.Optionull;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSpreader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class SculkCatalystBlockEntity extends BlockEntity implements GameEventListener.Provider<SculkCatalystBlockEntity.CatalystListener> {
   private final SculkCatalystBlockEntity.CatalystListener catalystListener;

   public SculkCatalystBlockEntity(BlockPos $$0, BlockState $$1) {
      super(BlockEntityType.SCULK_CATALYST, $$0, $$1);
      this.catalystListener = new SculkCatalystBlockEntity.CatalystListener($$1, new BlockPositionSource($$0));
   }

   public static void serverTick(net.minecraft.world.level.Level $$0, BlockPos $$1, BlockState $$2, SculkCatalystBlockEntity $$3) {
      $$3.catalystListener.getSculkSpreader().updateCursors($$0, $$1, $$0.getRandom(), true);
   }

   @Override
   protected void loadAdditional(ValueInput $$0) {
      super.loadAdditional($$0);
      this.catalystListener.sculkSpreader.load($$0);
   }

   @Override
   protected void saveAdditional(ValueOutput $$0) {
      this.catalystListener.sculkSpreader.save($$0);
      super.saveAdditional($$0);
   }

   public SculkCatalystBlockEntity.CatalystListener getListener() {
      return this.catalystListener;
   }

   public static class CatalystListener implements GameEventListener {
      public static final int PULSE_TICKS = 8;
      final SculkSpreader sculkSpreader;
      private final BlockState blockState;
      private final PositionSource positionSource;

      public CatalystListener(BlockState $$0, PositionSource $$1) {
         this.blockState = $$0;
         this.positionSource = $$1;
         this.sculkSpreader = SculkSpreader.createLevelSpreader();
      }

      @Override
      public PositionSource getListenerSource() {
         return this.positionSource;
      }

      @Override
      public int getListenerRadius() {
         return 8;
      }

      @Override
      public GameEventListener.DeliveryMode getDeliveryMode() {
         return GameEventListener.DeliveryMode.BY_DISTANCE;
      }

      @Override
      public boolean handleGameEvent(ServerLevel $$0, Holder<GameEvent> $$1, GameEvent.Context $$2, Vec3 $$3) {
         if ($$1.is(GameEvent.ENTITY_DIE) && $$2.sourceEntity() instanceof LivingEntity $$4) {
            if (!$$4.wasExperienceConsumed()) {
               DamageSource $$5 = $$4.getLastDamageSource();
               int $$6 = $$4.getExperienceReward($$0, (Entity)Optionull.map($$5, DamageSource::getEntity));
               if ($$4.shouldDropExperience() && $$6 > 0) {
                  this.sculkSpreader.addCursors(BlockPos.containing($$3.relative(Direction.UP, 0.5)), $$6);
                  this.tryAwardItSpreadsAdvancement($$0, $$4);
               }

               $$4.skipDropExperience();
               this.positionSource.getPosition($$0).ifPresent($$1x -> this.bloom($$0, BlockPos.containing($$1x), this.blockState, $$0.getRandom()));
            }

            return true;
         } else {
            return false;
         }
      }

      @VisibleForTesting
      public SculkSpreader getSculkSpreader() {
         return this.sculkSpreader;
      }

      private void bloom(ServerLevel $$0, BlockPos $$1, BlockState $$2, RandomSource $$3) {
         $$0.setBlock($$1, $$2.setValue(SculkCatalystBlock.PULSE, true), 3);
         $$0.scheduleTick($$1, $$2.getBlock(), 8);
         $$0.sendParticles(ParticleTypes.SCULK_SOUL, $$1.getX() + 0.5, $$1.getY() + 1.15, $$1.getZ() + 0.5, 2, 0.2, 0.0, 0.2, 0.0);
         $$0.playSound(null, $$1, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + $$3.nextFloat() * 0.4F);
      }

      private void tryAwardItSpreadsAdvancement(net.minecraft.world.level.Level $$0, LivingEntity $$1) {
         if ($$1.getLastHurtByMob() instanceof ServerPlayer $$3) {
            DamageSource $$4 = $$1.getLastDamageSource() == null ? $$0.damageSources().playerAttack($$3) : $$1.getLastDamageSource();
            CriteriaTriggers.KILL_MOB_NEAR_SCULK_CATALYST.trigger($$3, $$1, $$4);
         }
      }
   }
}
