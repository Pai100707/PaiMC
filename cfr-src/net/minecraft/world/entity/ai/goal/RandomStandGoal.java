/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

public class RandomStandGoal
extends Goal {
    private final AbstractHorse horse;
    private int nextStand;

    public RandomStandGoal(AbstractHorse $$0) {
        this.horse = $$0;
        this.resetStandInterval($$0);
    }

    @Override
    public void start() {
        this.horse.standIfPossible();
        this.playStandSound();
    }

    private void playStandSound() {
        SoundEvent $$0 = this.horse.getAmbientStandSound();
        if ($$0 != null) {
            this.horse.playSound($$0);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean canUse() {
        ++this.nextStand;
        if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
            this.resetStandInterval(this.horse);
            return !this.horse.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
        }
        return false;
    }

    private void resetStandInterval(AbstractHorse $$0) {
        this.nextStand = -$$0.getAmbientStandInterval();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}

