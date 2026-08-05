/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.illager;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class Evoker
extends SpellcasterIllager {
    private @Nullable Sheep wololoTarget;

    public Evoker(EntityType<? extends Evoker> $$0, Level $$1) {
        super((EntityType<? extends SpellcasterIllager>)$$0, $$1);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new EvokerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 8.0f, 0.6, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Creaking>(this, Creaking.class, 8.0f, 0.6, 1.0));
        this.goalSelector.addGoal(4, new EvokerSummonSpellGoal());
        this.goalSelector.addGoal(5, new EvokerAttackSpellGoal());
        this.goalSelector.addGoal(6, new EvokerWololoSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 12.0).add(Attributes.MAX_HEALTH, 24.0);
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.EVOKER_CELEBRATE;
    }

    @Override
    protected boolean considersEntityAsAlly(Entity $$0) {
        Vex $$1;
        if ($$0 == this) {
            return true;
        }
        if (super.considersEntityAsAlly($$0)) {
            return true;
        }
        if ($$0 instanceof Vex && ($$1 = (Vex)$$0).getOwner() != null) {
            return this.considersEntityAsAlly($$1.getOwner());
        }
        return false;
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

    @Nullable Sheep getWololoTarget() {
        return this.wololoTarget;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
    }

    class EvokerCastingSpellGoal
    extends SpellcasterIllager.SpellcasterCastingSpellGoal {
        EvokerCastingSpellGoal() {
            super(Evoker.this);
        }

        @Override
        public void tick() {
            if (Evoker.this.getTarget() != null) {
                Evoker.this.getLookControl().setLookAt(Evoker.this.getTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
            } else if (Evoker.this.getWololoTarget() != null) {
                Evoker.this.getLookControl().setLookAt(Evoker.this.getWololoTarget(), Evoker.this.getMaxHeadYRot(), Evoker.this.getMaxHeadXRot());
            }
        }
    }

    class EvokerSummonSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions vexCountTargeting;

        EvokerSummonSpellGoal() {
            super(Evoker.this);
            this.vexCountTargeting = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            int $$0 = EvokerSummonSpellGoal.getServerLevel(Evoker.this.level()).getNearbyEntities(Vex.class, this.vexCountTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0)).size();
            return Evoker.this.random.nextInt(8) + 1 > $$0;
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
            for (int $$2 = 0; $$2 < 3; ++$$2) {
                BlockPos $$3 = Evoker.this.blockPosition().offset(-2 + Evoker.this.random.nextInt(5), 1, -2 + Evoker.this.random.nextInt(5));
                Vex $$4 = EntityType.VEX.create(Evoker.this.level(), EntitySpawnReason.MOB_SUMMONED);
                if ($$4 == null) continue;
                $$4.snapTo($$3, 0.0f, 0.0f);
                $$4.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$3), EntitySpawnReason.MOB_SUMMONED, null);
                $$4.setOwner(Evoker.this);
                $$4.setBoundOrigin($$3);
                $$4.setLimitedLife(20 * (30 + Evoker.this.random.nextInt(90)));
                if ($$1 != null) {
                    $$0.getScoreboard().addPlayerToTeam($$4.getScoreboardName(), $$1);
                }
                $$0.addFreshEntityWithPassengers($$4);
                $$0.gameEvent(GameEvent.ENTITY_PLACE, $$3, GameEvent.Context.of(Evoker.this));
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

    class EvokerAttackSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        EvokerAttackSpellGoal() {
            super(Evoker.this);
        }

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
            LivingEntity $$0 = Evoker.this.getTarget();
            double $$1 = Math.min($$0.getY(), Evoker.this.getY());
            double $$2 = Math.max($$0.getY(), Evoker.this.getY()) + 1.0;
            float $$3 = (float)Mth.atan2($$0.getZ() - Evoker.this.getZ(), $$0.getX() - Evoker.this.getX());
            if (Evoker.this.distanceToSqr($$0) < 9.0) {
                for (int $$4 = 0; $$4 < 5; ++$$4) {
                    float $$5 = $$3 + (float)$$4 * (float)Math.PI * 0.4f;
                    this.createSpellEntity(Evoker.this.getX() + (double)Mth.cos($$5) * 1.5, Evoker.this.getZ() + (double)Mth.sin($$5) * 1.5, $$1, $$2, $$5, 0);
                }
                for (int $$6 = 0; $$6 < 8; ++$$6) {
                    float $$7 = $$3 + (float)$$6 * (float)Math.PI * 2.0f / 8.0f + 1.2566371f;
                    this.createSpellEntity(Evoker.this.getX() + (double)Mth.cos($$7) * 2.5, Evoker.this.getZ() + (double)Mth.sin($$7) * 2.5, $$1, $$2, $$7, 3);
                }
            } else {
                for (int $$8 = 0; $$8 < 16; ++$$8) {
                    double $$9 = 1.25 * (double)($$8 + 1);
                    int $$10 = 1 * $$8;
                    this.createSpellEntity(Evoker.this.getX() + (double)Mth.cos($$3) * $$9, Evoker.this.getZ() + (double)Mth.sin($$3) * $$9, $$1, $$2, $$3, $$10);
                }
            }
        }

        private void createSpellEntity(double $$0, double $$1, double $$2, double $$3, float $$4, int $$5) {
            BlockPos $$6 = BlockPos.containing($$0, $$3, $$1);
            boolean $$7 = false;
            double $$8 = 0.0;
            do {
                BlockState $$11;
                VoxelShape $$12;
                BlockPos $$9 = $$6.below();
                BlockState $$10 = Evoker.this.level().getBlockState($$9);
                if (!$$10.isFaceSturdy(Evoker.this.level(), $$9, Direction.UP)) continue;
                if (!Evoker.this.level().isEmptyBlock($$6) && !($$12 = ($$11 = Evoker.this.level().getBlockState($$6)).getCollisionShape(Evoker.this.level(), $$6)).isEmpty()) {
                    $$8 = $$12.max(Direction.Axis.Y);
                }
                $$7 = true;
                break;
            } while (($$6 = $$6.below()).getY() >= Mth.floor($$2) - 1);
            if ($$7) {
                Evoker.this.level().addFreshEntity(new EvokerFangs(Evoker.this.level(), $$0, (double)$$6.getY() + $$8, $$1, $$4, $$5, Evoker.this));
                Evoker.this.level().gameEvent(GameEvent.ENTITY_PLACE, new Vec3($$0, (double)$$6.getY() + $$8, $$1), GameEvent.Context.of(Evoker.this));
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

    public class EvokerWololoSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions wololoTargeting;

        public EvokerWololoSpellGoal() {
            super(Evoker.this);
            this.wololoTargeting = TargetingConditions.forNonCombat().range(16.0).selector(($$0, $$1) -> ((Sheep)$$0).getColor() == DyeColor.BLUE);
        }

        @Override
        public boolean canUse() {
            if (Evoker.this.getTarget() != null) {
                return false;
            }
            if (Evoker.this.isCastingSpell()) {
                return false;
            }
            if (Evoker.this.tickCount < this.nextAttackTickCount) {
                return false;
            }
            ServerLevel $$0 = EvokerWololoSpellGoal.getServerLevel(Evoker.this.level());
            if (!$$0.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                return false;
            }
            List<Sheep> $$1 = $$0.getNearbyEntities(Sheep.class, this.wololoTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0, 4.0, 16.0));
            if ($$1.isEmpty()) {
                return false;
            }
            Evoker.this.setWololoTarget($$1.get(Evoker.this.random.nextInt($$1.size())));
            return true;
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

