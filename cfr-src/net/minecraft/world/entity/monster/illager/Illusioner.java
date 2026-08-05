/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.illager;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Illusioner
extends SpellcasterIllager
implements RangedAttackMob {
    private static final int NUM_ILLUSIONS = 4;
    private static final int ILLUSION_TRANSITION_TICKS = 3;
    public static final int ILLUSION_SPREAD = 3;
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;

    public Illusioner(EntityType<? extends Illusioner> $$0, Level $$1) {
        super((EntityType<? extends SpellcasterIllager>)$$0, $$1);
        this.xpReward = 5;
        this.clientSideIllusionOffsets = new Vec3[2][4];
        for (int $$2 = 0; $$2 < 4; ++$$2) {
            this.clientSideIllusionOffsets[0][$$2] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][$$2] = Vec3.ZERO;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<Creaking>(this, Creaking.class, 8.0f, 1.0, 1.2));
        this.goalSelector.addGoal(4, new IllusionerMirrorSpellGoal());
        this.goalSelector.addGoal(5, new IllusionerBlindnessSpellGoal());
        this.goalSelector.addGoal(6, new RangedBowAttackGoal<Illusioner>(this, 0.5, 20, 15.0f));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.MAX_HEALTH, 32.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }
            if (this.hurtTime == 1 || this.tickCount % 1200 == 0) {
                this.clientSideIllusionTicks = 3;
                float $$0 = -6.0f;
                int $$1 = 13;
                for (int $$2 = 0; $$2 < 4; ++$$2) {
                    this.clientSideIllusionOffsets[0][$$2] = this.clientSideIllusionOffsets[1][$$2];
                    this.clientSideIllusionOffsets[1][$$2] = new Vec3((double)(-6.0f + (float)this.random.nextInt(13)) * 0.5, Math.max(0, this.random.nextInt(6) - 4), (double)(-6.0f + (float)this.random.nextInt(13)) * 0.5);
                }
                for (int $$3 = 0; $$3 < 16; ++$$3) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
                }
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0f, 1.0f, false);
            } else if (this.hurtTime == this.hurtDuration - 1) {
                this.clientSideIllusionTicks = 3;
                for (int $$4 = 0; $$4 < 4; ++$$4) {
                    this.clientSideIllusionOffsets[0][$$4] = this.clientSideIllusionOffsets[1][$$4];
                    this.clientSideIllusionOffsets[1][$$4] = new Vec3(0.0, 0.0, 0.0);
                }
            }
        }
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    public Vec3[] getIllusionOffsets(float $$0) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        }
        double $$1 = ((float)this.clientSideIllusionTicks - $$0) / 3.0f;
        $$1 = Math.pow($$1, 0.25);
        Vec3[] $$2 = new Vec3[4];
        for (int $$3 = 0; $$3 < 4; ++$$3) {
            $$2[$$3] = this.clientSideIllusionOffsets[1][$$3].scale(1.0 - $$1).add(this.clientSideIllusionOffsets[0][$$3].scale($$1));
        }
        return $$2;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    @Override
    public void applyRaidBuffs(ServerLevel $$0, int $$1, boolean $$2) {
    }

    @Override
    public void performRangedAttack(LivingEntity $$0, float $$1) {
        ItemStack $$2 = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
        ItemStack $$3 = this.getProjectile($$2);
        AbstractArrow $$4 = ProjectileUtil.getMobArrow(this, $$3, $$1, $$2);
        double $$5 = $$0.getX() - this.getX();
        double $$6 = $$0.getY(0.3333333333333333) - $$4.getY();
        double $$7 = $$0.getZ() - this.getZ();
        double $$8 = Math.sqrt($$5 * $$5 + $$7 * $$7);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel $$9 = (ServerLevel)level;
            Projectile.spawnProjectileUsingShoot($$4, $$9, $$3, $$5, $$6 + $$8 * (double)0.2f, $$7, 1.6f, 14 - $$9.getDifficulty().getId() * 4);
        }
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        }
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
        }
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    class IllusionerMirrorSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        IllusionerMirrorSpellGoal() {
            super(Illusioner.this);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            return !Illusioner.this.hasEffect(MobEffects.INVISIBILITY);
        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 340;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
        }

        @Override
        protected @Nullable SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.DISAPPEAR;
        }
    }

    class IllusionerBlindnessSpellGoal
    extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private int lastTargetId;

        IllusionerBlindnessSpellGoal() {
            super(Illusioner.this);
        }

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            }
            if (Illusioner.this.getTarget() == null) {
                return false;
            }
            if (Illusioner.this.getTarget().getId() == this.lastTargetId) {
                return false;
            }
            return IllusionerBlindnessSpellGoal.getServerLevel(Illusioner.this).getCurrentDifficultyAt(Illusioner.this.blockPosition()).isHarderThan(Difficulty.NORMAL.ordinal());
        }

        @Override
        public void start() {
            super.start();
            LivingEntity $$0 = Illusioner.this.getTarget();
            if ($$0 != null) {
                this.lastTargetId = $$0.getId();
            }
        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 180;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400), Illusioner.this);
        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.BLINDNESS;
        }
    }
}

