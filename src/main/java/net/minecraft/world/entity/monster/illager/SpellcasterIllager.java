package net.minecraft.world.entity.monster.illager;

import java.util.EnumSet;
import java.util.function.IntFunction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class SpellcasterIllager extends AbstractIllager {
   private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
   private static final int DEFAULT_SPELLCASTING_TICKS = 0;
   protected int spellCastingTickCount = 0;
   private SpellcasterIllager.IllagerSpell currentSpell = SpellcasterIllager.IllagerSpell.NONE;

   protected SpellcasterIllager(net.minecraft.world.entity.EntityType<? extends SpellcasterIllager> $$0, Level $$1) {
      super($$0, $$1);
   }

   @Override
   protected void defineSynchedData(Builder $$0) {
      super.defineSynchedData($$0);
      $$0.define(DATA_SPELL_CASTING_ID, (byte)0);
   }

   @Override
   protected void readAdditionalSaveData(ValueInput $$0) {
      super.readAdditionalSaveData($$0);
      this.spellCastingTickCount = $$0.getIntOr("SpellTicks", 0);
   }

   @Override
   protected void addAdditionalSaveData(ValueOutput $$0) {
      super.addAdditionalSaveData($$0);
      $$0.putInt("SpellTicks", this.spellCastingTickCount);
   }

   @Override
   public AbstractIllager.IllagerArmPose getArmPose() {
      if (this.isCastingSpell()) {
         return AbstractIllager.IllagerArmPose.SPELLCASTING;
      } else {
         return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
      }
   }

   public boolean isCastingSpell() {
      return this.level().isClientSide() ? (Byte)this.entityData.get(DATA_SPELL_CASTING_ID) > 0 : this.spellCastingTickCount > 0;
   }

   public void setIsCastingSpell(SpellcasterIllager.IllagerSpell $$0) {
      this.currentSpell = $$0;
      this.entityData.set(DATA_SPELL_CASTING_ID, (byte)$$0.id);
   }

   protected SpellcasterIllager.IllagerSpell getCurrentSpell() {
      return !this.level().isClientSide() ? this.currentSpell : SpellcasterIllager.IllagerSpell.byId((Byte)this.entityData.get(DATA_SPELL_CASTING_ID));
   }

   @Override
   protected void customServerAiStep(ServerLevel $$0) {
      super.customServerAiStep($$0);
      if (this.spellCastingTickCount > 0) {
         this.spellCastingTickCount--;
      }
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide() && this.isCastingSpell()) {
         SpellcasterIllager.IllagerSpell $$0 = this.getCurrentSpell();
         float $$1 = (float)$$0.spellColor[0];
         float $$2 = (float)$$0.spellColor[1];
         float $$3 = (float)$$0.spellColor[2];
         float $$4 = this.yBodyRot * (float) (Math.PI / 180.0) + Mth.cos(this.tickCount * 0.6662F) * 0.25F;
         float $$5 = Mth.cos($$4);
         float $$6 = Mth.sin($$4);
         double $$7 = 0.6 * this.getScale();
         double $$8 = 1.8 * this.getScale();
         this.level()
            .addParticle(
               ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$1, $$2, $$3),
               this.getX() + $$5 * $$7,
               this.getY() + $$8,
               this.getZ() + $$6 * $$7,
               0.0,
               0.0,
               0.0
            );
         this.level()
            .addParticle(
               ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, $$1, $$2, $$3),
               this.getX() - $$5 * $$7,
               this.getY() + $$8,
               this.getZ() - $$6 * $$7,
               0.0,
               0.0,
               0.0
            );
      }
   }

   protected int getSpellCastingTime() {
      return this.spellCastingTickCount;
   }

   protected abstract SoundEvent getCastingSoundEvent();

   protected static enum IllagerSpell {
      NONE(0, 0.0, 0.0, 0.0),
      SUMMON_VEX(1, 0.7, 0.7, 0.8),
      FANGS(2, 0.4, 0.3, 0.35),
      WOLOLO(3, 0.7, 0.5, 0.2),
      DISAPPEAR(4, 0.3, 0.3, 0.8),
      BLINDNESS(5, 0.1, 0.1, 0.2);

      private static final IntFunction<SpellcasterIllager.IllagerSpell> BY_ID = ByIdMap.continuous($$0 -> $$0.id, values(), OutOfBoundsStrategy.ZERO);
      final int id;
      final double[] spellColor;

      private IllagerSpell(final int $$0, final double $$1, final double $$2, final double $$3) {
         this.id = $$0;
         this.spellColor = new double[]{$$1, $$2, $$3};
      }

      public static SpellcasterIllager.IllagerSpell byId(int $$0) {
         return BY_ID.apply($$0);
      }
   }

   protected class SpellcasterCastingSpellGoal extends Goal {
      public SpellcasterCastingSpellGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      @Override
      public boolean canUse() {
         return SpellcasterIllager.this.getSpellCastingTime() > 0;
      }

      @Override
      public void start() {
         super.start();
         SpellcasterIllager.this.navigation.stop();
      }

      @Override
      public void stop() {
         super.stop();
         SpellcasterIllager.this.setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
      }

      @Override
      public void tick() {
         if (SpellcasterIllager.this.getTarget() != null) {
            SpellcasterIllager.this.getLookControl()
               .setLookAt(SpellcasterIllager.this.getTarget(), SpellcasterIllager.this.getMaxHeadYRot(), SpellcasterIllager.this.getMaxHeadXRot());
         }
      }
   }

   protected abstract class SpellcasterUseSpellGoal extends Goal {
      protected int attackWarmupDelay;
      protected int nextAttackTickCount;

      @Override
      public boolean canUse() {
         net.minecraft.world.entity.LivingEntity $$0 = SpellcasterIllager.this.getTarget();
         if ($$0 == null || !$$0.isAlive()) {
            return false;
         } else {
            return SpellcasterIllager.this.isCastingSpell() ? false : SpellcasterIllager.this.tickCount >= this.nextAttackTickCount;
         }
      }

      @Override
      public boolean canContinueToUse() {
         net.minecraft.world.entity.LivingEntity $$0 = SpellcasterIllager.this.getTarget();
         return $$0 != null && $$0.isAlive() && this.attackWarmupDelay > 0;
      }

      @Override
      public void start() {
         this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
         SpellcasterIllager.this.spellCastingTickCount = this.getCastingTime();
         this.nextAttackTickCount = SpellcasterIllager.this.tickCount + this.getCastingInterval();
         SoundEvent $$0 = this.getSpellPrepareSound();
         if ($$0 != null) {
            SpellcasterIllager.this.playSound($$0, 1.0F, 1.0F);
         }

         SpellcasterIllager.this.setIsCastingSpell(this.getSpell());
      }

      @Override
      public void tick() {
         this.attackWarmupDelay--;
         if (this.attackWarmupDelay == 0) {
            this.performSpellCasting();
            SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0F, 1.0F);
         }
      }

      protected abstract void performSpellCasting();

      protected int getCastWarmupTime() {
         return 20;
      }

      protected abstract int getCastingTime();

      protected abstract int getCastingInterval();

      
      protected abstract SoundEvent getSpellPrepareSound();

      protected abstract SpellcasterIllager.IllagerSpell getSpell();
   }
}
