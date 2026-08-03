package net.minecraft.world.entity.monster.illager;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class Evoker extends SpellcasterIllager {
   @Nullable
   private Sheep wololoTarget;

   public Evoker(net.minecraft.world.entity.EntityType<? extends Evoker> $$0, Level $$1) {
      super($$0, $$1);
      this.xpReward = 10;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(0, new FloatGoal(this));
      this.goalSelector.addGoal(1, new Evoker.EvokerCastingSpellGoal());
      this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.6, 1.0));
      this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Creaking.class, 8.0F, 0.6, 1.0));
      this.goalSelector.addGoal(4, new Evoker.EvokerSummonSpellGoal());
      this.goalSelector.addGoal(5, new Evoker.EvokerAttackSpellGoal());
      this.goalSelector.addGoal(6, new Evoker.EvokerWololoSpellGoal());
      this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
      this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 8.0F));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 12.0).add(Attributes.MAX_HEALTH, 24.0);
   }

   @Override
   public SoundEvent getCelebrateSound() {
      return SoundEvents.EVOKER_CELEBRATE;
   }

   @Override
   protected boolean considersEntityAsAlly(net.minecraft.world.entity.Entity $$0) {
      if ($$0 == this) {
         return true;
      } else if (super.considersEntityAsAlly($$0)) {
         return true;
      } else {
         return $$0 instanceof Vex $$1 && $$1.getOwner() != null ? this.considersEntityAsAlly($$1.getOwner()) : false;
      }
   }

   @Override
   protected SoundEvent getAmbientSound() {
      return SoundEvents.EVOKER_AMBIENT;
   }

   @Override
   protected SoundEvent getDeathSound() {
      return SoundEvents.EVOKER_DEATH;
   }

   @Override
   protected SoundEvent getHurtSound(DamageSource $$0) {
      return SoundEvents.EVOKER_HURT;
   }

   void setWololoTarget(@Nullable Sheep $$0) {
      this.wololoTarget = $$0;
   }

   @Nullable
   Sheep getWololoTarget() {
      return this.wololoTarget;
   }

   @Override
   protected SoundEvent getCastingSoundEvent() {
      return SoundEvents.EVOKER_CAST_SPELL;
   }

   @Override
   public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
   }

   class EvokerAttackSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
      @Override
      protected int getCastingTime() {
         return 40;
      }

      @Override
      protected int getCastingInterval() {
         return 100;
      }

      @Override
      protected void performSpellCasting() {
         net.minecraft.world.entity.LivingEntity $$0 = Evoker.this.getTarget();
         double $$1 = Math.min($$0.getY(), Evoker.this.getY());
         double $$2 = Math.max($$0.getY(), Evoker.this.getY()) + 1.0;
         float $$3 = (float)Mth.atan2($$0.getZ() - Evoker.this.getZ(), $$0.getX() - Evoker.this.getX());
         if (Evoker.this.distanceToSqr($$0) < 9.0) {
            for (int $$4 = 0; $$4 < 5; $$4++) {
               float $$5 = $$3 + $$4 * (float) Math.PI * 0.4F;
               this.createSpellEntity(Evoker.this.getX() + Mth.cos($$5) * 1.5, Evoker.this.getZ() + Mth.sin($$5) * 1.5, $$1, $$2, $$5, 0);
            }

            for (int $$6 = 0; $$6 < 8; $$6++) {
               float $$7 = $$3 + $$6 * (float) Math.PI * 2.0F / 8.0F + (float) (Math.PI * 2.0 / 5.0);
               this.createSpellEntity(Evoker.this.getX() + Mth.cos($$7) * 2.5, Evoker.this.getZ() + Mth.sin($$7) * 2.5, $$1, $$2, $$7, 3);
            }
         } else {
            for (int $$8 = 0; $$8 < 16; $$8++) {
               double $$9 = 1.25 * ($$8 + 1);
               int $$10 = 1 * $$8;
               this.createSpellEntity(Evoker.this.getX() + Mth.cos($$3) * $$9, Evoker.this.getZ() + Mth.sin($$3) * $$9, $$1, $$2, $$3, $$10);
            }
         }
      }

      private void createSpellEntity(double $$0, double $$1, double $$2, double $$3, float $$4, int $$5) {
         BlockPos $$6 = BlockPos.containing($$0, $$3, $$1);
         boolean $$7 = false;
         double $$8 = 0.0;

         do {
            BlockPos $$9 = $$6.below();
            BlockState $$10 = Evoker.this.level().getBlockState($$9);
            if ($$10.isFaceSturdy(Evoker.this.level(), $$9, Direction.UP)) {
               if (!Evoker.this.level().isEmptyBlock($$6)) {
                  BlockState $$11 = Evoker.this.level().getBlockState($$6);
                  VoxelShape $$12 = $$11.getCollisionShape(Evoker.this.level(), $$6);
                  if (!$$12.isEmpty()) {
                     $$8 = $$12.max(Axis.Y);
                  }
               }

               $$7 = true;
               break;
            }

            $$6 = $$6.below();
         } while ($$6.getY() >= Mth.floor($$2) - 1);

         if ($$7) {
            Evoker.this.level().addFreshEntity(new EvokerFangs(Evoker.this.level(), $$0, $$6.getY() + $$8, $$1, $$4, $$5, Evoker.this));
            Evoker.this.level().gameEvent(GameEvent.ENTITY_PLACE, new Vec3($$0, $$6.getY() + $$8, $$1), Context.of(Evoker.this));
         }
      }

      @Override
      protected SoundEvent getSpellPrepareSound() {
         return SoundEvents.EVOKER_PREPARE_ATTACK;
      }

      @Override
      protected SpellcasterIllager.IllagerSpell getSpell() {
         return SpellcasterIllager.IllagerSpell.FANGS;
      }
   }

   class EvokerCastingSpellGoal extends SpellcasterIllager.SpellcasterCastingSpellGoal {
      @Override
      public void tick() {
         if (Evoker.this.getTarget() != null) {
            Evoker.this.getLookControl().setLookAt(Evoker.this.getTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
         } else if (Evoker.this.getWololoTarget() != null) {
            Evoker.this.getLookControl().setLookAt(Evoker.this.getWololoTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
         }
      }
   }

   class EvokerSummonSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
      private final TargetingConditions vexCountTargeting = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();

      @Override
      public boolean canUse() {
         if (!super.canUse()) {
            return false;
         } else {
            int $$0 = getServerLevel(Evoker.this.level())
               .getNearbyEntities(Vex.class, this.vexCountTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0))
               .size();
            return Evoker.this.random.nextInt(8) + 1 > $$0;
         }
      }

      @Override
      protected int getCastingTime() {
         return 100;
      }

      @Override
      protected int getCastingInterval() {
         return 340;
      }

      @Override
      protected void performSpellCasting() {
         ServerLevel $$0 = (ServerLevel)Evoker.this.level();
         PlayerTeam $$1 = Evoker.this.getTeam();

         for (int $$2 = 0; $$2 < 3; $$2++) {
            BlockPos $$3 = Evoker.this.blockPosition().offset(-2 + Evoker.this.random.nextInt(5), 1, -2 + Evoker.this.random.nextInt(5));
            Vex $$4 = net.minecraft.world.entity.EntityType.VEX.create(Evoker.this.level(), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED);
            if ($$4 != null) {
               $$4.snapTo($$3, 0.0F, 0.0F);
               $$4.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$3), net.minecraft.world.entity.EntitySpawnReason.MOB_SUMMONED, null);
               $$4.setOwner(Evoker.this);
               $$4.setBoundOrigin($$3);
               $$4.setLimitedLife(20 * (30 + Evoker.this.random.nextInt(90)));
               if ($$1 != null) {
                  $$0.getScoreboard().addPlayerToTeam($$4.getScoreboardName(), $$1);
               }

               $$0.addFreshEntityWithPassengers($$4);
               $$0.gameEvent(GameEvent.ENTITY_PLACE, $$3, Context.of(Evoker.this));
            }
         }
      }

      @Override
      protected SoundEvent getSpellPrepareSound() {
         return SoundEvents.EVOKER_PREPARE_SUMMON;
      }

      @Override
      protected SpellcasterIllager.IllagerSpell getSpell() {
         return SpellcasterIllager.IllagerSpell.SUMMON_VEX;
      }
   }

   public class EvokerWololoSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
      private final TargetingConditions wololoTargeting = TargetingConditions.forNonCombat()
         .range(16.0)
         .selector(($$0x, $$1) -> ((Sheep)$$0x).getColor() == DyeColor.BLUE);

      @Override
      public boolean canUse() {
         if (Evoker.this.getTarget() != null) {
            return false;
         } else if (Evoker.this.isCastingSpell()) {
            return false;
         } else if (Evoker.this.tickCount < this.nextAttackTickCount) {
            return false;
         } else {
            ServerLevel $$0 = getServerLevel(Evoker.this.level());
            if (!(Boolean)$$0.getGameRules().get(GameRules.MOB_GRIEFING)) {
               return false;
            } else {
               List<Sheep> $$1 = $$0.getNearbyEntities(Sheep.class, this.wololoTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0, 4.0, 16.0));
               if ($$1.isEmpty()) {
                  return false;
               } else {
                  Evoker.this.setWololoTarget($$1.get(Evoker.this.random.nextInt($$1.size())));
                  return true;
               }
            }
         }
      }

      @Override
      public boolean canContinueToUse() {
         return Evoker.this.getWololoTarget() != null && this.attackWarmupDelay > 0;
      }

      @Override
      public void stop() {
         super.stop();
         Evoker.this.setWololoTarget(null);
      }

      @Override
      protected void performSpellCasting() {
         Sheep $$0 = Evoker.this.getWololoTarget();
         if ($$0 != null && $$0.isAlive()) {
            $$0.setColor(DyeColor.RED);
         }
      }

      @Override
      protected int getCastWarmupTime() {
         return 40;
      }

      @Override
      protected int getCastingTime() {
         return 60;
      }

      @Override
      protected int getCastingInterval() {
         return 140;
      }

      @Override
      protected SoundEvent getSpellPrepareSound() {
         return SoundEvents.EVOKER_PREPARE_WOLOLO;
      }

      @Override
      protected SpellcasterIllager.IllagerSpell getSpell() {
         return SpellcasterIllager.IllagerSpell.WOLOLO;
      }
   }
}
