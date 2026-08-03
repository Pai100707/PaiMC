package net.minecraft.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ARGB {
   private static final int LINEAR_CHANNEL_DEPTH = 1024;
   private static final short[] SRGB_TO_LINEAR = net.minecraft.util.Util.make(new short[256], $$0 -> {
      for (int $$1 = 0; $$1 < $$0.length; $$1++) {
         float $$2 = $$1 / 255.0F;
         $$0[$$1] = (short)Math.round(computeSrgbToLinear($$2) * 1023.0F);
      }
   });
   private static final byte[] LINEAR_TO_SRGB = net.minecraft.util.Util.make(new byte[1024], $$0 -> {
      for (int $$1 = 0; $$1 < $$0.length; $$1++) {
         float $$2 = $$1 / 1023.0F;
         $$0[$$1] = (byte)Math.round(computeLinearToSrgb($$2) * 255.0F);
      }
   });

   private static float computeSrgbToLinear(float $$0) {
      return $$0 >= 0.04045F ? (float)Math.pow(($$0 + 0.055) / 1.055, 2.4) : $$0 / 12.92F;
   }

   private static float computeLinearToSrgb(float $$0) {
      return $$0 >= 0.0031308F ? (float)(1.055 * Math.pow($$0, 0.4166666666666667) - 0.055) : 12.92F * $$0;
   }

   public static float srgbToLinearChannel(int $$0) {
      return SRGB_TO_LINEAR[$$0] / 1023.0F;
   }

   public static int linearToSrgbChannel(float $$0) {
      return LINEAR_TO_SRGB[net.minecraft.util.Mth.floor($$0 * 1023.0F)] & 0xFF;
   }

   public static int meanLinear(int $$0, int $$1, int $$2, int $$3) {
      return color(
         (alpha($$0) + alpha($$1) + alpha($$2) + alpha($$3)) / 4,
         linearChannelMean(red($$0), red($$1), red($$2), red($$3)),
         linearChannelMean(green($$0), green($$1), green($$2), green($$3)),
         linearChannelMean(blue($$0), blue($$1), blue($$2), blue($$3))
      );
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
      return color(255, $$0, $$1, $$2);
   }

   public static int color(Vec3 $$0) {
      return color(as8BitChannel((float)$$0.x()), as8BitChannel((float)$$0.y()), as8BitChannel((float)$$0.z()));
   }

   public static int multiply(int $$0, int $$1) {
      if ($$0 == -1) {
         return $$1;
      } else {
         return $$1 == -1 ? $$0 : color(alpha($$0) * alpha($$1) / 255, red($$0) * red($$1) / 255, green($$0) * green($$1) / 255, blue($$0) * blue($$1) / 255);
      }
   }

   public static int addRgb(int $$0, int $$1) {
      return color(alpha($$0), Math.min(red($$0) + red($$1), 255), Math.min(green($$0) + green($$1), 255), Math.min(blue($$0) + blue($$1), 255));
   }

   public static int subtractRgb(int $$0, int $$1) {
      return color(alpha($$0), Math.max(red($$0) - red($$1), 0), Math.max(green($$0) - green($$1), 0), Math.max(blue($$0) - blue($$1), 0));
   }

   public static int multiplyAlpha(int $$0, float $$1) {
      if ($$0 == 0 || $$1 <= 0.0F) {
         return 0;
      } else {
         return $$1 >= 1.0F ? $$0 : color(alphaFloat($$0) * $$1, $$0);
      }
   }

   public static int scaleRGB(int $$0, float $$1) {
      return scaleRGB($$0, $$1, $$1, $$1);
   }

   public static int scaleRGB(int $$0, float $$1, float $$2, float $$3) {
      return color(
         alpha($$0),
         Math.clamp((long)((int)(red($$0) * $$1)), 0, 255),
         Math.clamp((long)((int)(green($$0) * $$2)), 0, 255),
         Math.clamp((long)((int)(blue($$0) * $$3)), 0, 255)
      );
   }

   public static int scaleRGB(int $$0, int $$1) {
      return color(
         alpha($$0),
         Math.clamp((long)red($$0) * $$1 / 255L, 0, 255),
         Math.clamp((long)green($$0) * $$1 / 255L, 0, 255),
         Math.clamp((long)blue($$0) * $$1 / 255L, 0, 255)
      );
   }

   public static int greyscale(int $$0) {
      int $$1 = (int)(red($$0) * 0.3F + green($$0) * 0.59F + blue($$0) * 0.11F);
      return color(alpha($$0), $$1, $$1, $$1);
   }

   public static int alphaBlend(int $$0, int $$1) {
      int $$2 = alpha($$0);
      int $$3 = alpha($$1);
      if ($$3 == 255) {
         return $$1;
      } else if ($$3 == 0) {
         return $$0;
      } else {
         int $$4 = $$3 + $$2 * (255 - $$3) / 255;
         return color(
            $$4,
            alphaBlendChannel($$4, $$3, red($$0), red($$1)),
            alphaBlendChannel($$4, $$3, green($$0), green($$1)),
            alphaBlendChannel($$4, $$3, blue($$0), blue($$1))
         );
      }
   }

   private static int alphaBlendChannel(int $$0, int $$1, int $$2, int $$3) {
      return ($$3 * $$1 + $$2 * ($$0 - $$1)) / $$0;
   }

   public static int srgbLerp(float $$0, int $$1, int $$2) {
      int $$3 = net.minecraft.util.Mth.lerpInt($$0, alpha($$1), alpha($$2));
      int $$4 = net.minecraft.util.Mth.lerpInt($$0, red($$1), red($$2));
      int $$5 = net.minecraft.util.Mth.lerpInt($$0, green($$1), green($$2));
      int $$6 = net.minecraft.util.Mth.lerpInt($$0, blue($$1), blue($$2));
      return color($$3, $$4, $$5, $$6);
   }

   public static int linearLerp(float $$0, int $$1, int $$2) {
      return color(
         net.minecraft.util.Mth.lerpInt($$0, alpha($$1), alpha($$2)),
         LINEAR_TO_SRGB[net.minecraft.util.Mth.lerpInt($$0, SRGB_TO_LINEAR[red($$1)], SRGB_TO_LINEAR[red($$2)])] & 0xFF,
         LINEAR_TO_SRGB[net.minecraft.util.Mth.lerpInt($$0, SRGB_TO_LINEAR[green($$1)], SRGB_TO_LINEAR[green($$2)])] & 0xFF,
         LINEAR_TO_SRGB[net.minecraft.util.Mth.lerpInt($$0, SRGB_TO_LINEAR[blue($$1)], SRGB_TO_LINEAR[blue($$2)])] & 0xFF
      );
   }

   public static int opaque(int $$0) {
      return $$0 | 0xFF000000;
   }

   public static int transparent(int $$0) {
      return $$0 & 16777215;
   }

   public static int color(int $$0, int $$1) {
      return $$0 << 24 | $$1 & 16777215;
   }

   public static int color(float $$0, int $$1) {
      return as8BitChannel($$0) << 24 | $$1 & 16777215;
   }

   public static int white(float $$0) {
      return as8BitChannel($$0) << 24 | 16777215;
   }

   public static int white(int $$0) {
      return $$0 << 24 | 16777215;
   }

   public static int black(float $$0) {
      return as8BitChannel($$0) << 24;
   }

   public static int black(int $$0) {
      return $$0 << 24;
   }

   public static int colorFromFloat(float $$0, float $$1, float $$2, float $$3) {
      return color(as8BitChannel($$0), as8BitChannel($$1), as8BitChannel($$2), as8BitChannel($$3));
   }

   public static Vector3f vector3fFromRGB24(int $$0) {
      return new Vector3f(redFloat($$0), greenFloat($$0), blueFloat($$0));
   }

   public static Vector4f vector4fFromARGB32(int $$0) {
      return new Vector4f(redFloat($$0), greenFloat($$0), blueFloat($$0), alphaFloat($$0));
   }

   public static int average(int $$0, int $$1) {
      return color((alpha($$0) + alpha($$1)) / 2, (red($$0) + red($$1)) / 2, (green($$0) + green($$1)) / 2, (blue($$0) + blue($$1)) / 2);
   }

   public static int as8BitChannel(float $$0) {
      return net.minecraft.util.Mth.floor($$0 * 255.0F);
   }

   public static float alphaFloat(int $$0) {
      return from8BitChannel(alpha($$0));
   }

   public static float redFloat(int $$0) {
      return from8BitChannel(red($$0));
   }

   public static float greenFloat(int $$0) {
      return from8BitChannel(green($$0));
   }

   public static float blueFloat(int $$0) {
      return from8BitChannel(blue($$0));
   }

   private static float from8BitChannel(int $$0) {
      return $$0 / 255.0F;
   }

   public static int toABGR(int $$0) {
      return $$0 & -16711936 | ($$0 & 0xFF0000) >> 16 | ($$0 & 0xFF) << 16;
   }

   public static int fromABGR(int $$0) {
      return toABGR($$0);
   }

   public static int setBrightness(int $$0, float $$1) {
      int $$2 = red($$0);
      int $$3 = green($$0);
      int $$4 = blue($$0);
      int $$5 = alpha($$0);
      int $$6 = Math.max(Math.max($$2, $$3), $$4);
      int $$7 = Math.min(Math.min($$2, $$3), $$4);
      float $$8 = $$6 - $$7;
      float $$9;
      if ($$6 != 0) {
         $$9 = $$8 / $$6;
      } else {
         $$9 = 0.0F;
      }

      float $$11;
      if ($$9 == 0.0F) {
         $$11 = 0.0F;
      } else {
         float $$12 = ($$6 - $$2) / $$8;
         float $$13 = ($$6 - $$3) / $$8;
         float $$14 = ($$6 - $$4) / $$8;
         if ($$2 == $$6) {
            $$11 = $$14 - $$13;
         } else if ($$3 == $$6) {
            $$11 = 2.0F + $$12 - $$14;
         } else {
            $$11 = 4.0F + $$13 - $$12;
         }

         $$11 /= 6.0F;
         if ($$11 < 0.0F) {
            $$11++;
         }
      }

      if ($$9 == 0.0F) {
         $$2 = $$3 = $$4 = Math.round($$1 * 255.0F);
         return color($$5, $$2, $$3, $$4);
      } else {
         float $$18 = ($$11 - (float)Math.floor($$11)) * 6.0F;
         float $$19 = $$18 - (float)Math.floor($$18);
         float $$20 = $$1 * (1.0F - $$9);
         float $$21 = $$1 * (1.0F - $$9 * $$19);
         float $$22 = $$1 * (1.0F - $$9 * (1.0F - $$19));
         switch ((int)$$18) {
            case 0:
               $$2 = Math.round($$1 * 255.0F);
               $$3 = Math.round($$22 * 255.0F);
               $$4 = Math.round($$20 * 255.0F);
               break;
            case 1:
               $$2 = Math.round($$21 * 255.0F);
               $$3 = Math.round($$1 * 255.0F);
               $$4 = Math.round($$20 * 255.0F);
               break;
            case 2:
               $$2 = Math.round($$20 * 255.0F);
               $$3 = Math.round($$1 * 255.0F);
               $$4 = Math.round($$22 * 255.0F);
               break;
            case 3:
               $$2 = Math.round($$20 * 255.0F);
               $$3 = Math.round($$21 * 255.0F);
               $$4 = Math.round($$1 * 255.0F);
               break;
            case 4:
               $$2 = Math.round($$22 * 255.0F);
               $$3 = Math.round($$20 * 255.0F);
               $$4 = Math.round($$1 * 255.0F);
               break;
            case 5:
               $$2 = Math.round($$1 * 255.0F);
               $$3 = Math.round($$20 * 255.0F);
               $$4 = Math.round($$21 * 255.0F);
         }

         return color($$5, $$2, $$3, $$4);
      }
   }
}
