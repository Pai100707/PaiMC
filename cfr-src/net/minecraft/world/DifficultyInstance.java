/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.concurrent.Immutable
 */
package net.minecraft.world;

import javax.annotation.concurrent.Immutable;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;

@Immutable
public class DifficultyInstance {
    private static final float DIFFICULTY_TIME_GLOBAL_OFFSET = -72000.0f;
    private static final float MAX_DIFFICULTY_TIME_GLOBAL = 1440000.0f;
    private static final float MAX_DIFFICULTY_TIME_LOCAL = 3600000.0f;
    private final Difficulty base;
    private final float effectiveDifficulty;

    public DifficultyInstance(Difficulty $$0, long $$1, long $$2, float $$3) {
        this.base = $$0;
        this.effectiveDifficulty = this.calculateDifficulty($$0, $$1, $$2, $$3);
    }

    public Difficulty getDifficulty() {
        return this.base;
    }

    public float getEffectiveDifficulty() {
        return this.effectiveDifficulty;
    }

    public boolean isHard() {
        return this.effectiveDifficulty >= (float)Difficulty.HARD.ordinal();
    }

    public boolean isHarderThan(float $$0) {
        return this.effectiveDifficulty > $$0;
    }

    public float getSpecialMultiplier() {
        if (this.effectiveDifficulty < 2.0f) {
            return 0.0f;
        }
        if (this.effectiveDifficulty > 4.0f) {
            return 1.0f;
        }
        return (this.effectiveDifficulty - 2.0f) / 2.0f;
    }

    private float calculateDifficulty(Difficulty $$0, long $$1, long $$2, float $$3) {
        if ($$0 == Difficulty.PEACEFUL) {
            return 0.0f;
        }
        boolean $$4 = $$0 == Difficulty.HARD;
        float $$5 = 0.75f;
        float $$6 = Mth.clamp(((float)$$1 + -72000.0f) / 1440000.0f, 0.0f, 1.0f) * 0.25f;
        $$5 += $$6;
        float $$7 = 0.0f;
        $$7 += Mth.clamp((float)$$2 / 3600000.0f, 0.0f, 1.0f) * ($$4 ? 1.0f : 0.75f);
        $$7 += Mth.clamp($$3 * 0.25f, 0.0f, $$6);
        if ($$0 == Difficulty.EASY) {
            $$7 *= 0.5f;
        }
        return (float)$$0.getId() * ($$5 += $$7);
    }
}

