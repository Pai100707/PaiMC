/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.camel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class CamelHusk
extends Camel {
    public CamelHusk(EntityType<? extends Camel> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public boolean removeWhenFarAway(double $$0) {
        return true;
    }

    @Override
    public boolean isMobControlled() {
        return this.getFirstPassenger() instanceof Mob;
    }

    @Override
    public InteractionResult interact(Player $$0, InteractionHand $$1) {
        this.setPersistenceRequired();
        return super.interact($$0, $$1);
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isMobControlled();
    }

    @Override
    public boolean isFood(ItemStack $$0) {
        return $$0.is(ItemTags.CAMEL_HUSK_FOOD);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMEL_HUSK_AMBIENT;
    }

    @Override
    public boolean canMate(Animal $$0) {
        return false;
    }

    @Override
    public @Nullable Camel getBreedOffspring(ServerLevel $$0, AgeableMob $$1) {
        return null;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_HUSK_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource $$0) {
        return SoundEvents.CAMEL_HUSK_HURT;
    }

    @Override
    protected void playStepSound(BlockPos $$0, BlockState $$1) {
        if ($$1.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEvents.CAMEL_HUSK_STEP_SAND, 0.4f, 1.0f);
        } else {
            this.playSound(SoundEvents.CAMEL_HUSK_STEP, 0.4f, 1.0f);
        }
    }

    @Override
    protected SoundEvent getDashingSound() {
        return SoundEvents.CAMEL_HUSK_DASH;
    }

    @Override
    protected SoundEvent getDashReadySound() {
        return SoundEvents.CAMEL_HUSK_DASH_READY;
    }

    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.CAMEL_HUSK_EAT;
    }

    @Override
    protected SoundEvent getStandUpSound() {
        return SoundEvents.CAMEL_HUSK_STAND;
    }

    @Override
    protected SoundEvent getSitDownSound() {
        return SoundEvents.CAMEL_HUSK_SIT;
    }

    @Override
    protected Holder.Reference<SoundEvent> getSaddleSound() {
        return SoundEvents.CAMEL_HUSK_SADDLE;
    }

    @Override
    public float chargeSpeedModifier() {
        return 4.0f;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

