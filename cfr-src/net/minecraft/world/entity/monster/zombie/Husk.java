/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.zombie;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class Husk
extends Zombie {
    public Husk(EntityType<? extends Husk> $$0, Level $$1) {
        super((EntityType<? extends Zombie>)$$0, $$1);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HUSK_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.HUSK_STEP;
    }

    @Override
    public boolean doHurtTarget(ServerLevel $$0, Entity $$1) {
        boolean $$2 = super.doHurtTarget($$0, $$1);
        if ($$2 && this.getMainHandItem().isEmpty() && $$1 instanceof LivingEntity) {
            float $$3 = $$0.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            ((LivingEntity)$$1).addEffect(new MobEffectInstance(MobEffects.HUNGER, 140 * (int)$$3), this);
        }
        return $$2;
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion(ServerLevel $$0) {
        this.convertToZombieType($$0, EntityType.ZOMBIE);
        if (!this.isSilent()) {
            $$0.levelEvent(null, 1041, this.blockPosition(), 0);
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor $$0, DifficultyInstance $$1, EntitySpawnReason $$2, @Nullable SpawnGroupData $$3) {
        RandomSource $$4 = $$0.getRandom();
        $$3 = super.finalizeSpawn($$0, $$1, $$2, $$3);
        float $$5 = $$1.getSpecialMultiplier();
        if ($$2 != EntitySpawnReason.CONVERSION) {
            this.setCanPickUpLoot($$4.nextFloat() < 0.55f * $$5);
        }
        if ($$3 != null) {
            $$3 = new HuskGroupData((Zombie.ZombieGroupData)$$3);
            boolean bl = ((HuskGroupData)$$3).triedToSpawnCamelHusk = $$2 != EntitySpawnReason.NATURAL;
        }
        if ($$3 instanceof HuskGroupData) {
            BlockPos $$7;
            HuskGroupData $$6 = (HuskGroupData)$$3;
            if (!$$6.triedToSpawnCamelHusk && $$0.noCollision(EntityType.CAMEL_HUSK.getSpawnAABB((double)($$7 = this.blockPosition()).getX() + 0.5, $$7.getY(), (double)$$7.getZ() + 0.5))) {
                $$6.triedToSpawnCamelHusk = true;
                if ($$4.nextFloat() < 0.1f) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
                    CamelHusk $$8 = EntityType.CAMEL_HUSK.create(this.level(), EntitySpawnReason.NATURAL);
                    if ($$8 != null) {
                        $$8.setPos(this.getX(), this.getY(), this.getZ());
                        $$8.finalizeSpawn($$0, $$1, $$2, null);
                        this.startRiding($$8, true, true);
                        $$0.addFreshEntity($$8);
                        Parched $$9 = EntityType.PARCHED.create(this.level(), EntitySpawnReason.NATURAL);
                        if ($$9 != null) {
                            $$9.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
                            $$9.finalizeSpawn($$0, $$1, $$2, null);
                            $$9.startRiding($$8, false, false);
                            $$0.addFreshEntityWithPassengers($$9);
                        }
                    }
                }
            }
        }
        return $$3;
    }

    public static class HuskGroupData
    extends Zombie.ZombieGroupData {
        public boolean triedToSpawnCamelHusk = false;

        public HuskGroupData(Zombie.ZombieGroupData $$0) {
            super($$0.isBaby, $$0.canSpawnJockey);
        }
    }
}

