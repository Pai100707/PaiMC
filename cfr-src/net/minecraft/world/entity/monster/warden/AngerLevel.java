/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.monster.warden;

import java.util.Arrays;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Util;

public enum AngerLevel {
    CALM(0, SoundEvents.WARDEN_AMBIENT, SoundEvents.WARDEN_LISTENING),
    AGITATED(40, SoundEvents.WARDEN_AGITATED, SoundEvents.WARDEN_LISTENING_ANGRY),
    ANGRY(80, SoundEvents.WARDEN_ANGRY, SoundEvents.WARDEN_LISTENING_ANGRY);

    private static final AngerLevel[] SORTED_LEVELS;
    private final int minimumAnger;
    private final SoundEvent ambientSound;
    private final SoundEvent listeningSound;

    private AngerLevel(int $$0, SoundEvent $$1, SoundEvent $$2) {
        this.minimumAnger = $$0;
        this.ambientSound = $$1;
        this.listeningSound = $$2;
    }

    public int getMinimumAnger() {
        return this.minimumAnger;
    }

    public SoundEvent getAmbientSound() {
        return this.ambientSound;
    }

    public SoundEvent getListeningSound() {
        return this.listeningSound;
    }

    public static AngerLevel byAnger(int $$0) {
        for (AngerLevel $$1 : SORTED_LEVELS) {
            if ($$0 < $$1.minimumAnger) continue;
            return $$1;
        }
        return CALM;
    }

    public boolean isAngry() {
        return this == ANGRY;
    }

    static {
        SORTED_LEVELS = Util.make(AngerLevel.values(), $$02 -> Arrays.sort($$02, ($$0, $$1) -> Integer.compare($$1.minimumAnger, $$0.minimumAnger)));
    }
}

