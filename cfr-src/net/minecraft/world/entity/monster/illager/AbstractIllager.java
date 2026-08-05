/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.illager;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager
extends Raider {
    protected AbstractIllager(EntityType<? extends AbstractIllager> $$0, Level $$1) {
        super((EntityType<? extends Raider>)$$0, $$1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    public IllagerArmPose getArmPose() {
        return IllagerArmPose.CROSSED;
    }

    @Override
    public boolean canAttack(LivingEntity $$0) {
        if ($$0 instanceof AbstractVillager && $$0.isBaby()) {
            return false;
        }
        return super.canAttack($$0);
    }

    @Override
    protected boolean considersEntityAsAlly(Entity $$0) {
        if (super.considersEntityAsAlly($$0)) {
            return true;
        }
        if ($$0.getType().is(EntityTypeTags.ILLAGER_FRIENDS)) {
            return this.getTeam() == null && $$0.getTeam() == null;
        }
        return false;
    }

    public static enum IllagerArmPose {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING,
        NEUTRAL;

    }

    protected class RaiderOpenDoorGoal
    extends OpenDoorGoal {
        public RaiderOpenDoorGoal(Raider $$1) {
            super($$1, false);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }
}

