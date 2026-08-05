/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.minecraft.util;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ARGB {
    private static final int LINEAR_CHANNEL_DEPTH = 1024;
    private static final short[] SRGB_TO_LINEAR = Util.make(new short[256], $$0 -> {
        for (int $$1 = 0; $$1 < ((short[])$$0).length; ++$$1) {
            float $$2 = (float)$$1 / 255.0f;
            $$0[$$1] = (short)Math.round(ARGB.computeSrgbToLinear($$2) * 1023.0f);
        }
    });
    private static final byte[] LINEAR_TO_SRGB = Util.make(new byte[1024], $$0 -> {
        for (int $$1 = 0; $$1 < ((byte[])$$0).length; ++$$1) {
            float $$2 = (float)$$1 / 1023.0f;
            $$0[$$1] = (byte)Math.round(ARGB.computeLinearToSrgb($$2) * 255.0f);
        }
    });

    private static float computeSrgbToLinear(float $$0) {
        if ($$0 >= 0.04045f) {
            return (float)Math.pow(((double)$$0 + 0.055) / 1.055, 2.4);
        }
        return $$0 / 12.92f;
    }

    private static float computeLinearToSrgb(float $$0) {
        if ($$0 >= 0.0031308f) {
            return (float)(1.055 * Math.pow($$0, 0.4166666666666667) - 0.055);
        }
        return 12.92f * $$0;
    }

    public static float srgbToLinearChannel(int $$0) {
        return (float)SRGB_TO_LINEAR[$$0] / 1023.0f;
    }

    public static int linearToSrgbChannel(float $$0) {
        return LINEAR_TO_SRGB[Mth.floor($$0 * 1023.0f)] & 0xFF;
    }

    public static int meanLinear(int $$0, int $$1, int $$2, int $$3) {
        return ARGB.color((ARGB.alpha($$0) + ARGB.alpha($$1) + ARGB.alpha($$2) + ARGB.alpha($$3)) / 4, ARGB.linearChannelMean(ARGB.red($$0), ARGB.red($$1), ARGB.red($$2), ARGB.red($$3)), ARGB.linearChannelMean(ARGB.green($$0), ARGB.green($$1), ARGB.green($$2), ARGB.green($$3)), ARGB.linearChannelMean(ARGB.blue($$0), ARGB.blue($$1), ARGB.blue($$2), ARGB.blue($$3)));
    }

    private static int linearChannelMean(int $$0, int $$1, int $$2, int $$3) {
        int $$4 = (SRGB_TO_LINEAR[$$0] + SRGB_TO_LINEAR[$$1] + SRGB_TO_LINEAR[$$2] + SRGB_TO_LINEAR[$$3]) / 4;
        return LINEAR_TO_SRGB[$$4] & 0xFF;
    }

    public static int alpha(int $$0) {
        return $$0 >>> 24;
    }

    public static int red(int $$0) {
        return $$0 >> 16 & 0xFF;
    }

    public static int green(int $$0) {
        return $$0 >> 8 & 0xFF;
    }

    public static int blue(int $$0) {
        return $$0 & 0xFF;
    }

    public static int color(int $$0, int $$1, int $$2, int $$3) {
        return ($$0 & 0xFF) << 24 | ($$1 & 0xFF) << 16 | ($$2 & 0xFF) << 8 | $$3 & 0xFF;
    }

    public static int color(int $$0, int $$1, int $$2) {
        return ARGB.color(255, $$0, $$1, $$2);
    }

    public static int color(Vec3 $$0) {
        return ARGB.color(ARGB.as8BitChannel((float)$$0.x()), ARGB.as8BitChannel((float)$$0.y()), ARGB.as8BitChannel((float)$$0.z()));
    }

    public static int multiply(int $$0, int $$1) {
        if ($$0 == -1) {
            return $$1;
        }
        if ($$1 == -1) {
            return $$0;
        }
        return ARGB.color(ARGB.alpha($$0) * ARGB.alpha($$1) / 255, ARGB.red($$0) * ARGB.red($$1) / 255, ARGB.green($$0) * ARGB.green($$1) / 255, ARGB.blue($$0) * ARGB.blue($$1) / 255);
    }

    public static int addRgb(int $$0, int $$1) {
        return ARGB.color(ARGB.alpha($$0), Math.min(ARGB.red($$0) + ARGB.red($$1), 255), Math.min(ARGB.green($$0) + ARGB.green($$1), 255), Math.min(ARGB.blue($$0) + ARGB.blue($$1), 255));
    }

    public static int subtractRgb(int $$0, int $$1) {
        return ARGB.color(ARGB.alpha($$0), Math.max(ARGB.red($$0) - ARGB.red($$1), 0), Math.max(ARGB.green($$0) - ARGB.green($$1), 0), Math.max(ARGB.blue($$0) - ARGB.blue($$1), 0));
    }

    public static int multiplyAlpha(int $$0, float $$1) {
        if ($$0 == 0 || $$1 <= 0.0f) {
            return 0;
        }
        if ($$1 >= 1.0f) {
            return $$0;
        }
        return ARGB.color(ARGB.alphaFloat($$0) * $$1, $$0);
    }

    public static int scaleRGB(int $$0, float $$1) {
        return ARGB.scaleRGB($$0, $$1, $$1, $$1);
    }

    public static int scaleRGB(int $$0, float $$1, float $$2, float $$3) {
        return ARGB.color(ARGB.alpha($$0), Math.clamp((long)((int)((float)ARGB.red($$0) * $$1)), 0, 255), Math.clamp((long)((int)((float)ARGB.green($$0) * $$2)), 0, 255), Math.clamp((long)((int)((float)ARGB.blue($$0) * $$3)), 0, 255));
    }

    public static int scaleRGB(int $$0, int $$1) {
        return ARGB.color(ARGB.alpha($$0), Math.clamp((long)ARGB.red($$0) * (long)$$1 / 255L, 0, 255), Math.clamp((long)ARGB.green($$0) * (long)$$1 / 255L, 0, 255), Math.clamp((long)ARGB.blue($$0) * (long)$$1 / 255L, 0, 255));
    }

    public static int greyscale(int $$0) {
        int $$1 = (int)((float)ARGB.red($$0) * 0.3f + (float)ARGB.green($$0) * 0.59f + (float)ARGB.blue($$0) * 0.11f);
        return ARGB.color(ARGB.alpha($$0), $$1, $$1, $$1);
    }

    public static int alphaBlend(int $$0, int $$1) {
        int $$2 = ARGB.alpha($$0);
        int $$3 = ARGB.alpha($$1);
        if ($$3 == 255) {
            return $$1;
        }
        if ($$3 == 0) {
            return $$0;
        }
        int $$4 = $$3 + $$2 * (255 - $$3) / 255;
        return ARGB.color($$4, ARGB.alphaBlendChannel($$4, $$3, ARGB.red($$0), ARGB.red($$1)), ARGB.alphaBlendChannel($$4, $$3, ARGB.green($$0), ARGB.green($$1)), ARGB.alphaBlendChannel($$4, $$3, ARGB.blue($$0), ARGB.blue($$1)));
    }

    private static int alphaBlendChannel(int $$0, int $$1, int $$2, int $$3) {
        return ($$3 * $$1 + $$2 * ($$0 - $$1)) / $$0;
    }

    public static int srgbLerp(float $$0, int $$1, int $$2) {
        int $$3 = Mth.lerpInt($$0, ARGB.alpha($$1), ARGB.alpha($$2));
        int $$4 = Mth.lerpInt($$0, ARGB.red($$1), ARGB.red($$2));
        int $$5 = Mth.lerpInt($$0, ARGB.green($$1), ARGB.green($$2));
        int $$6 = Mth.lerpInt($$0, ARGB.blue($$1), ARGB.blue($$2));
        return ARGB.color($$3, $$4, $$5, $$6);
    }

    public static int linearLerp(float $$0, int $$1, int $$2) {
        return ARGB.color(Mth.lerpInt($$0, ARGB.alpha($$1), ARGB.alpha($$2)), LINEAR_TO_SRGB[Mth.lerpInt($$0, SRGB_TO_LINEAR[ARGB.red($$1)], SRGB_TO_LINEAR[ARGB.red($$2)])] & 0xFF, LINEAR_TO_SRGB[Mth.lerpInt($$0, SRGB_TO_LINEAR[ARGB.green($$1)], SRGB_TO_LINEAR[ARGB.green($$2)])] & 0xFF, LINEAR_TO_SRGB[Mth.lerpInt($$0, SRGB_TO_LINEAR[ARGB.blue($$1)], SRGB_TO_LINEAR[ARGB.blue($$2)])] & 0xFF);
    }

    public static int opaque(int $$0) {
        return $$0 | 0xFF000000;
    }

    public static int transparent(int $$0) {
        return $$0 & 0xFFFFFF;
    }

    public static int color(int $$0, int $$1) {
        return $$0 << 24 | $$1 & 0xFFFFFF;
    }

    public static int color(float $$0, int $$1) {
        return ARGB.as8BitChannel($$0) << 24 | $$1 & 0xFFFFFF;
    }

    public static int white(float $$0) {
        return ARGB.as8BitChannel($$0) << 24 | 0xFFFFFF;
    }

    public static int white(int $$0) {
        return $$0 << 24 | 0xFFFFFF;
    }

    public static int black(float $$0) {
        return ARGB.as8BitChannel($$0) << 24;
    }

    public static int black(int $$0) {
        return $$0 << 24;
    }

    public static int colorFromFloat(float $$0, float $$1, float $$2, float $$3) {
        return ARGB.color(ARGB.as8BitChannel($$0), ARGB.as8BitChannel($$1), ARGB.as8BitChannel($$2), ARGB.as8BitChannel($$3));
    }

    public static Vector3f vector3fFromRGB24(int $$0) {
        return new Vector3f(ARGB.redFloat($$0), ARGB.greenFloat($$0), ARGB.blueFloat($$0));
    }

    public static Vector4f vector4fFromARGB32(int $$0) {
        return new Vector4f(ARGB.redFloat($$0), ARGB.greenFloat($$0), ARGB.blueFloat($$0), ARGB.alphaFloat($$0));
    }

    public static int average(int $$0, int $$1) {
        return ARGB.color((ARGB.alpha($$0) + ARGB.alpha($$1)) / 2, (ARGB.red($$0) + ARGB.red($$1)) / 2, (ARGB.green($$0) + ARGB.green($$1)) / 2, (ARGB.blue($$0) + ARGB.blue($$1)) / 2);
    }

    public static int as8BitChannel(float $$0) {
        return Mth.floor($$0 * 255.0f);
    }

    public static float alphaFloat(int $$0) {
        return ARGB.from8BitChannel(ARGB.alpha($$0));
    }

    public static float redFloat(int $$0) {
        return ARGB.from8BitChannel(ARGB.red($$0));
    }

    public static float greenFloat(int $$0) {
        return ARGB.from8BitChannel(ARGB.green($$0));
    }

    public static float blueFloat(int $$0) {
        return ARGB.from8BitChannel(ARGB.blue($$0));
    }

    private static float from8BitChannel(int $$0) {
        return (float)$$0 / 255.0f;
    }

    public static int toABGR(int $$0) {
        return $$0 & 0xFF00FF00 | ($$0 & 0xFF0000) >> 16 | ($$0 & 0xFF) << 16;
    }

    public static int fromABGR(int $$0) {
        return ARGB.toABGR($$0);
    }

    public static int setBrightness(int $$0, float $$1) {
        float $$17;
        float $$10;
        int $$2 = ARGB.red($$0);
        int $$3 = ARGB.green($$0);
        int $$4 = ARGB.blue($$0);
        int $$5 = ARGB.alpha($$0);
        int $$6 = Math.max(Math.max($$2, $$3), $$4);
        int $$7 = Math.min(Math.min($$2, $$3), $$4);
        float $$8 = $$6 - $$7;
        if ($$6 != 0) {
            float $$9 = $$8 / (float)$$6;
        } else {
            $$10 = 0.0f;
        }
        if ($$10 == 0.0f) {
            float $$11 = 0.0f;
        } else {
            float $$12 = (float)($$6 - $$2) / $$8;
            float $$13 = (float)($$6 - $$3) / $$8;
            float $$14 = (float)($$6 - $$4) / $$8;
            if ($$2 == $$6) {
                float $$15 = $$14 - $$13;
            } else if ($$3 == $$6) {
                float $$16 = 2.0f + $$12 - $$14;
            } else {
                $$17 = 4.0f + $$13 - $$12;
            }
            $$17 /= 6.0f;
            if ($$17 < 0.0f) {
                $$17 += 1.0f;
            }
        }
        if ($$10 == 0.0f) {
            $$3 = $$4 = Math.round($$1 * 255.0f);
            $$2 = $$4;
            return ARGB.color($$5, $$2, $$3, $$4);
        }
        void $$18 = ($$17 - (float)Math.floor($$17)) * 6.0f;
        void $$19 = $$18 - (float)Math.floor((double)$$18);
        float $$20 = $$1 * (1.0f - $$10);
        float $$21 = $$1 * (1.0f - $$10 * $$19);
        float $$22 = $$1 * (1.0f - $$10 * (1.0f - $$19));
        switch ((int)$$18) {
            case 0: {
                $$2 = Math.round($$1 * 255.0f);
                $$3 = Math.round($$22 * 255.0f);
                $$4 = Math.round($$20 * 255.0f);
                break;
            }
            case 1: {
                $$2 = Math.round($$21 * 255.0f);
                $$3 = Math.round($$1 * 255.0f);
                $$4 = Math.round($$20 * 255.0f);
                break;
            }
            case 2: {
                $$2 = Math.round($$20 * 255.0f);
                $$3 = Math.round($$1 * 255.0f);
                $$4 = Math.round($$22 * 255.0f);
                break;
            }
            case 3: {
                $$2 = Math.round($$20 * 255.0f);
                $$3 = Math.round($$21 * 255.0f);
                $$4 = Math.round($$1 * 255.0f);
                break;
            }
            case 4: {
                $$2 = Math.round($$22 * 255.0f);
                $$3 = Math.round($$20 * 255.0f);
                $$4 = Math.round($$1 * 255.0f);
                break;
            }
            case 5: {
                $$2 = Math.round($$1 * 255.0f);
                $$3 = Math.round($$20 * 255.0f);
                $$4 = Math.round($$21 * 255.0f);
            }
        }
        return ARGB.color($$5, $$2, $$3, $$4);
    }
}

