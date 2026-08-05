/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.util.Mth;

public class Ease {
    public static float inBack(float $$0) {
        float $$1 = 1.70158f;
        float $$2 = 2.70158f;
        return Mth.square($$0) * (2.70158f * $$0 - 1.70158f);
    }

    public static float inBounce(float $$0) {
        return 1.0f - Ease.outBounce(1.0f - $$0);
    }

    public static float inCubic(float $$0) {
        return Mth.cube($$0);
    }

    public static float inElastic(float $$0) {
        if ($$0 == 0.0f) {
            return 0.0f;
        }
        if ($$0 == 1.0f) {
            return 1.0f;
        }
        float $$1 = 2.0943952f;
        return (float)(-Math.pow(2.0, 10.0 * (double)$$0 - 10.0) * Math.sin(((double)$$0 * 10.0 - 10.75) * 2.094395160675049));
    }

    public static float inExpo(float $$0) {
        return $$0 == 0.0f ? 0.0f : (float)Math.pow(2.0, 10.0 * (double)$$0 - 10.0);
    }

    public static float inQuart(float $$0) {
        return Mth.square(Mth.square($$0));
    }

    public static float inQuint(float $$0) {
        return Mth.square(Mth.square($$0)) * $$0;
    }

    public static float inSine(float $$0) {
        return 1.0f - Mth.cos($$0 * 1.5707964f);
    }

    public static float inOutBounce(float $$0) {
        if ($$0 < 0.5f) {
            return (1.0f - Ease.outBounce(1.0f - 2.0f * $$0)) / 2.0f;
        }
        return (1.0f + Ease.outBounce(2.0f * $$0 - 1.0f)) / 2.0f;
    }

    public static float inOutCirc(float $$0) {
        if ($$0 < 0.5f) {
            return (float)((1.0 - Math.sqrt(1.0 - Math.pow(2.0 * (double)$$0, 2.0))) / 2.0);
        }
        return (float)((Math.sqrt(1.0 - Math.pow(-2.0 * (double)$$0 + 2.0, 2.0)) + 1.0) / 2.0);
    }

    public static float inOutCubic(float $$0) {
        if ($$0 < 0.5f) {
            return 4.0f * Mth.cube($$0);
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)$$0 + 2.0, 3.0) / 2.0);
    }

    public static float inOutQuad(float $$0) {
        if ($$0 < 0.5f) {
            return 2.0f * Mth.square($$0);
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)$$0 + 2.0, 2.0) / 2.0);
    }

    public static float inOutQuart(float $$0) {
        if ($$0 < 0.5f) {
            return 8.0f * Mth.square(Mth.square($$0));
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)$$0 + 2.0, 4.0) / 2.0);
    }

    public static float inOutQuint(float $$0) {
        if ((double)$$0 < 0.5) {
            return 16.0f * $$0 * $$0 * $$0 * $$0 * $$0;
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)$$0 + 2.0, 5.0) / 2.0);
    }

    public static float outBounce(float $$0) {
        float $$1 = 7.5625f;
        float $$2 = 2.75f;
        if ($$0 < 0.36363637f) {
            return 7.5625f * Mth.square($$0);
        }
        if ($$0 < 0.72727275f) {
            return 7.5625f * Mth.square($$0 - 0.54545456f) + 0.75f;
        }
        if ((double)$$0 < 0.9090909090909091) {
            return 7.5625f * Mth.square($$0 - 0.8181818f) + 0.9375f;
        }
        return 7.5625f * Mth.square($$0 - 0.95454544f) + 0.984375f;
    }

    public static float outElastic(float $$0) {
        float $$1 = 2.0943952f;
        if ($$0 == 0.0f) {
            return 0.0f;
        }
        if ($$0 == 1.0f) {
            return 1.0f;
        }
        return (float)(Math.pow(2.0, -10.0 * (double)$$0) * Math.sin(((double)$$0 * 10.0 - 0.75) * 2.094395160675049) + 1.0);
    }

    public static float outExpo(float $$0) {
        if ($$0 == 1.0f) {
            return 1.0f;
        }
        return 1.0f - (float)Math.pow(2.0, -10.0 * (double)$$0);
    }

    public static float outQuad(float $$0) {
        return 1.0f - Mth.square(1.0f - $$0);
    }

    public static float outQuint(float $$0) {
        return 1.0f - (float)Math.pow(1.0 - (double)$$0, 5.0);
    }

    public static float outSine(float $$0) {
        return Mth.sin($$0 * 1.5707964f);
    }

    public static float inOutSine(float $$0) {
        return -(Mth.cos((float)Math.PI * $$0) - 1.0f) / 2.0f;
    }

    public static float outBack(float $$0) {
        float $$1 = 1.70158f;
        float $$2 = 2.70158f;
        return 1.0f + 2.70158f * Mth.cube($$0 - 1.0f) + 1.70158f * Mth.square($$0 - 1.0f);
    }

    public static float outQuart(float $$0) {
        return 1.0f - Mth.square(Mth.square(1.0f - $$0));
    }

    public static float outCubic(float $$0) {
        return 1.0f - Mth.cube(1.0f - $$0);
    }

    public static float inOutExpo(float $$0) {
        if ($$0 < 0.5f) {
            return $$0 == 0.0f ? 0.0f : (float)(Math.pow(2.0, 20.0 * (double)$$0 - 10.0) / 2.0);
        }
        return $$0 == 1.0f ? 1.0f : (float)((2.0 - Math.pow(2.0, -20.0 * (double)$$0 + 10.0)) / 2.0);
    }

    public static float inQuad(float $$0) {
        return $$0 * $$0;
    }

    public static float outCirc(float $$0) {
        return (float)Math.sqrt(1.0f - Mth.square($$0 - 1.0f));
    }

    public static float inOutElastic(float $$0) {
        float $$1 = 1.3962635f;
        if ($$0 == 0.0f) {
            return 0.0f;
        }
        if ($$0 == 1.0f) {
            return 1.0f;
        }
        double $$2 = Math.sin((20.0 * (double)$$0 - 11.125) * 1.3962634801864624);
        if ($$0 < 0.5f) {
            return (float)(-(Math.pow(2.0, 20.0 * (double)$$0 - 10.0) * $$2) / 2.0);
        }
        return (float)(Math.pow(2.0, -20.0 * (double)$$0 + 10.0) * $$2 / 2.0 + 1.0);
    }

    public static float inCirc(float $$0) {
        return (float)(-Math.sqrt(1.0f - $$0 * $$0)) + 1.0f;
    }

    public static float inOutBack(float $$0) {
        float $$1 = 1.70158f;
        float $$2 = 2.5949094f;
        if ($$0 < 0.5f) {
            return 4.0f * $$0 * $$0 * (7.189819f * $$0 - 2.5949094f) / 2.0f;
        }
        float $$3 = 2.0f * $$0 - 2.0f;
        return ($$3 * $$3 * (3.5949094f * $$3 + 2.5949094f) + 2.0f) / 2.0f;
    }
}

