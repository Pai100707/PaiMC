/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.annotations.VisibleForTesting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class AbstractPiglin
extends Monster {
    protected static final EntityDataAccessor<Boolean> DATA_IMMUNE_TO_ZOMBIFICATION = SynchedEntityData.defineId(AbstractPiglin.class, EntityDataSerializers.BOOLEAN);
    public static final int CONVERSION_TIME = 300;
    private static final boolean DEFAULT_IMMUNE_TO_ZOMBIFICATION = false;
    private static final boolean DEFAULT_PICK_UP_LOOT = true;
    private static final int DEFAULT_TIME_IN_OVERWORLD = 0;
    protected int timeInOverworld = 0;

    public AbstractPiglin(EntityType<? extends AbstractPiglin> $$0, Level $$1) {
        super((EntityType<? extends Monster>)$$0, $$1);
        this.setCanPickUpLoot(true);
        this.applyOpenDoorsAbility();
        this.setPathfindingMalus(PathType.DANGER_FIRE, 16.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
    }

    private void applyOpenDoorsAbility() {
        if (GoalUtils.hasGroundPathNavigation(this)) {
            this.getNavigation().setCanOpenDoors(true);
        }
    }

    protected abstract boolean canHunt();

    public void setImmuneToZombification(boolean $$0) {
        this.getEntityData().set(DATA_IMMUNE_TO_ZOMBIFICATION, $$0);
    }

    protected boolean isImmuneToZombification() {
        return this.getEntityData().get(DATA_IMMUNE_TO_ZOMBIFICATION);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder $$0) {
        super.defineSynchedData($$0);
        $$0.define(DATA_IMMUNE_TO_ZOMBIFICATION, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput $$0) {
        super.addAdditionalSaveData($$0);
        $$0.putBoolean("IsImmuneToZombification", this.isImmuneToZombification());
        $$0.putInt("TimeInOverworld", this.timeInOverworld);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput $$0) {
        super.readAdditionalSaveData($$0);
        this.setCanPickUpLoot($$0.getBooleanOr("CanPickUpLoot", true));
        this.setImmuneToZombification($$0.getBooleanOr("IsImmuneToZombification", false));
        this.timeInOverworld = $$0.getIntOr("TimeInOverworld", 0);
    }

    @Override
    protected void customServerAiStep(ServerLevel $$0) {
        super.customServerAiStep($$0);
        this.timeInOverworld = this.isConverting() ? ++this.timeInOverworld : 0;
        if (this.timeInOverworld > 300) {
            this.playConvertedSound();
            this.finishConversion($$0);
        }
    }

    @VisibleForTesting
    public void setTimeInOverworld(int $$0) {
        this.timeInOverworld = $$0;
    }

    public boolean isConverting() {
        return !this.isImmuneToZombification() && !this.isNoAi() && this.level().environmentAttributes().getValue(EnvironmentAttributes.PIGLINS_ZOMBIFY, this.position()) != false;
    }

    protected void finishConversion(ServerLevel $$02) {
        this.convertTo(EntityType.ZOMBIFIED_PIGLIN, ConversionParams.single(this, true, true), $$0 -> $$0.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)));
    }

    public boolean isAdult() {
        return !this.isBaby();
    }

    public abstract PiglinArmPose getArmPose();

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    protected boolean isHoldingMeleeWeapon() {
        return this.getMainHandItem().has(DataComponents.TOOL);
    }

    @Override
    public void playAmbientSound() {
        if (PiglinAi.isIdle(this)) {
            super.playAmbientSound();
        }
    }

    protected abstract void playConvertedSound();
}

