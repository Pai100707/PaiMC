/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class Vex
extends Monster
implements TraceableEntity {
    public static final float FLAP_DEGREES_PER_TICK = 45.836624f;
    public static final int TICKS_PER_FLAP = Mth.ceil(3.9269907f);
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Vex.class, EntityDataSerializers.BYTE);
    private static final int FLAG_IS_CHARGING = 1;
    private @Nullable EntityReference<Mob> owner;
    private @Nullable BlockPos boundOrigin;
    private boolean hasLimitedLife;
    private int limitedLifeTicks;

    public Vex(EntityType<? extends Vex> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.moveControl = new VexMoveControl(this);
        this.xpReward = 3;
    }

    @Override
    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    protected boolean isAffectedByBlocks() {
        return !this.isRemoved();
    }

    @Override
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20;
            this.hurt(this.damageSources().starve(), 1.0f);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(4, new VexChargeAttackGoal());
        this.goalSelector.addGoal(8, new VexRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0f, 1.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new VexCopyOwnerTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 14.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_FLAGS_ID, (byte)0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.boundOrigin = $$0.read("bound_pos", BlockPos.CODEC).orElse(null);
        $$0.getInt("life_ticks").ifPresentOrElse(this::setLimitedLife, () -> {
            this.hasLimitedLife = false;
        });
        this.owner = EntityReference.read($$0, "owner");
    }

    @Override
    public void restoreFrom(Entity $$0) {
        super.restoreFrom($$0);
        if ($$0 instanceof Vex) {
            Vex $$1 = (Vex)$$0;
            this.owner = $$1.owner;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.storeNullable("bound_pos", BlockPos.CODEC, this.boundOrigin);
        if (this.hasLimitedLife) {
            $$0.putInt("life_ticks", this.limitedLifeTicks);
        }
        EntityReference.store(this.owner, $$0, "owner");
    }

    @Override
    public @Nullable Mob getOwner() {
        return EntityReference.get(this.owner, this.level(), Mob.class);
    }

    public @Nullable BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos $$0) {
        this.boundOrigin = $$0;
    }

    private boolean getVexFlag(int $$0) {
        byte $$1 = this.entityData.get(DATA_FLAGS_ID);
        return ($$1 & $$0) != 0;
    }

    private void setVexFlag(int $$0, boolean $$1) {
        int $$2 = this.entityData.get(DATA_FLAGS_ID).byteValue();
        $$2 = $$1 ? ($$2 |= $$0) : ($$2 &= ~$$0);
        this.entityData.set(DATA_FLAGS_ID, (byte)($$2 & 0xFF));
    }

    public boolean isCharging() {
        return this.getVexFlag(1);
    }

    public void setIsCharging(boolean $$0) {
        this.setVexFlag(1, $$0);
    }

    public void setOwner(Mob $$0) {
        this.owner = EntityReference.of($$0);
    }

    public void setLimitedLife(int $$0) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = $$0;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        this.populateDefaultEquipmentSlots($$4, $$1);
        this.populateDefaultEquipmentEnchantments($$0, $$4, $$1);
        return super.finalizeSpawn($$0, $$1, $$2, $$3);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource $$0, DifficultyInstance $$1) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
    }

    @Override
    public /* synthetic */ @Nullable Entity getOwner() {
        return this.getOwner();
    }

    class VexMoveControl
    extends MoveControl {
        public VexMoveControl(Vex $$0) {
            super($$0);
        }

        @Override
        public void tick() {
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                return;
            }
            Vec3 $$0 = new Vec3(this.wantedX - Vex.this.getX(), this.wantedY - Vex.this.getY(), this.wantedZ - Vex.this.getZ());
            double $$1 = $$0.length();
            if ($$1 < Vex.this.getBoundingBox().getSize()) {
                this.operation = MoveControl.Operation.WAIT;
                Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().scale(0.5));
            } else {
                Vex.this.setDeltaMovement(Vex.this.getDeltaMovement().add($$0.scale(this.speedModifier * 0.05 / $$1)));
                if (Vex.this.getTarget() == null) {
                    Vec3 $$2 = Vex.this.getDeltaMovement();
                    Vex.this.setYRot(-((float)Mth.atan2($$2.x, $$2.z)) * 57.295776f);
                    Vex.this.yBodyRot = Vex.this.getYRot();
                } else {
                    double $$3 = Vex.this.getTarget().getX() - Vex.this.getX();
                    double $$4 = Vex.this.getTarget().getZ() - Vex.this.getZ();
                    Vex.this.setYRot(-((float)Mth.atan2($$3, $$4)) * 57.295776f);
                    Vex.this.yBodyRot = Vex.this.getYRot();
                }
            }
        }
    }

    class VexChargeAttackGoal
    extends Goal {
        public VexChargeAttackGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity $$0 = Vex.this.getTarget();
            if ($$0 != null && $$0.isAlive() && !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(VexChargeAttackGoal.reducedTickDelay(7)) == 0) {
                return Vex.this.distanceToSqr($$0) > 4.0;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return Vex.this.getMoveControl().hasWanted() && Vex.this.isCharging() && Vex.this.getTarget() != null && Vex.this.getTarget().isAlive();
        }

        @Override
        public void start() {
            LivingEntity $$0 = Vex.this.getTarget();
            if ($$0 != null) {
                Vec3 $$1 = $$0.getEyePosition();
                Vex.this.moveControl.setWantedPosition($$1.x, $$1.y, $$1.z, 1.0);
            }
            Vex.this.setIsCharging(true);
            Vex.this.playSound(SoundEvents.VEX_CHARGE, 1.0f, 1.0f);
        }

        @Override
        public void stop() {
            Vex.this.setIsCharging(false);
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity $$0 = Vex.this.getTarget();
            if ($$0 == null) {
                return;
            }
            if (Vex.this.getBoundingBox().intersects($$0.getBoundingBox())) {
                Vex.this.doHurtTarget(VexChargeAttackGoal.getServerLevel(Vex.this.level()), $$0);
                Vex.this.setIsCharging(false);
            } else {
                double $$1 = Vex.this.distanceToSqr($$0);
                if ($$1 < 9.0) {
                    Vec3 $$2 = $$0.getEyePosition();
                    Vex.this.moveControl.setWantedPosition($$2.x, $$2.y, $$2.z, 1.0);
                }
            }
        }
    }

    class VexRandomMoveGoal
    extends Goal {
        public VexRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !Vex.this.getMoveControl().hasWanted() && Vex.this.random.nextInt(VexRandomMoveGoal.reducedTickDelay(7)) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void tick() {
            BlockPos $$0 = Vex.this.getBoundOrigin();
            if ($$0 == null) {
                $$0 = Vex.this.blockPosition();
            }
            for (int $$1 = 0; $$1 < 3; ++$$1) {
                BlockPos $$2 = $$0.offset(Vex.this.random.nextInt(15) - 7, Vex.this.random.nextInt(11) - 5, Vex.this.random.nextInt(15) - 7);
                if (!Vex.this.level().isEmptyBlock($$2)) continue;
                Vex.this.moveControl.setWantedPosition((double)$$2.getX() + 0.5, (double)$$2.getY() + 0.5, (double)$$2.getZ() + 0.5, 0.25);
                if (Vex.this.getTarget() != null) break;
                Vex.this.getLookControl().setLookAt((double)$$2.getX() + 0.5, (double)$$2.getY() + 0.5, (double)$$2.getZ() + 0.5, 180.0f, 20.0f);
                break;
            }
        }
    }

    class VexCopyOwnerTargetGoal
    extends TargetGoal {
        private final TargetingConditions copyOwnerTargeting;

        public VexCopyOwnerTargetGoal(PathfinderMob $$0) {
            super($$0, false);
            this.copyOwnerTargeting = TargetingConditions.forNonCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
        }

        @Override
        public boolean canUse() {
            Mob $$0 = Vex.this.getOwner();
            return $$0 != null && $$0.getTarget() != null && this.canAttack($$0.getTarget(), this.copyOwnerTargeting);
        }

        @Override
        public void start() {
            Mob $$0 = Vex.this.getOwner();
            Vex.this.setTarget($$0 != null ? $$0.getTarget() : null);
            super.start();
        }
    }
}

