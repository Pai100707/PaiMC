package net.minecraft.util;

public class Ease {
   public static float inBack(float $$0) {
      float $$1 = 1.70158F;
      float $$2 = 2.70158F;
      return net.minecraft.util.Mth.square($$0) * (2.70158F * $$0 - 1.70158F);
   }

   public static float inBounce(float $$0) {
      return 1.0F - outBounce(1.0F - $$0);
   }

   public static float inCubic(float $$0) {
      return net.minecraft.util.Mth.cube($$0);
   }

   public static float inElastic(float $$0) {
      if ($$0 == 0.0F) {
         return 0.0F;
      } else if ($$0 == 1.0F) {
         return 1.0F;
      } else {
         float $$1 = (float) (Math.PI * 2.0 / 3.0);
         return (float)(-Math.pow(2.0, 10.0 * $$0 - 10.0) * Math.sin(($$0 * 10.0 - 10.75) * (float) (Math.PI * 2.0 / 3.0)));
      }
   }

   public static float inExpo(float $$0) {
      return $$0 == 0.0F ? 0.0F : (float)Math.pow(2.0, 10.0 * $$0 - 10.0);
   }

   public static float inQuart(float $$0) {
      return net.minecraft.util.Mth.square(net.minecraft.util.Mth.square($$0));
   }

   public static float inQuint(float $$0) {
      return net.minecraft.util.Mth.square(net.minecraft.util.Mth.square($$0)) * $$0;
   }

   public static float inSine(float $$0) {
      return 1.0F - net.minecraft.util.Mth.cos($$0 * (float) (Math.PI / 2));
   }

   public static float inOutBounce(float $$0) {
      return $$0 < 0.5F ? (1.0F - outBounce(1.0F - 2.0F * $$0)) / 2.0F : (1.0F + outBounce(2.0F * $$0 - 1.0F)) / 2.0F;
   }

   public static float inOutCirc(float $$0) {
      return $$0 < 0.5F
         ? (float)((1.0 - Math.sqrt(1.0 - Math.pow(2.0 * $$0, 2.0))) / 2.0)
         : (float)((Math.sqrt(1.0 - Math.pow(-2.0 * $$0 + 2.0, 2.0)) + 1.0) / 2.0);
   }

   public static float inOutCubic(float $$0) {
      return $$0 < 0.5F ? 4.0F * net.minecraft.util.Mth.cube($$0) : (float)(1.0 - Math.pow(-2.0 * $$0 + 2.0, 3.0) / 2.0);
   }

   public static float inOutQuad(float $$0) {
      return $$0 < 0.5F ? 2.0F * net.minecraft.util.Mth.square($$0) : (float)(1.0 - Math.pow(-2.0 * $$0 + 2.0, 2.0) / 2.0);
   }

   public static float inOutQuart(float $$0) {
      return $$0 < 0.5F ? 8.0F * net.minecraft.util.Mth.square(net.minecraft.util.Mth.square($$0)) : (float)(1.0 - Math.pow(-2.0 * $$0 + 2.0, 4.0) / 2.0);
   }

   public static float inOutQuint(float $$0) {
      return $$0 < 0.5 ? 16.0F * $$0 * $$0 * $$0 * $$0 * $$0 : (float)(1.0 - Math.pow(-2.0 * $$0 + 2.0, 5.0) / 2.0);
   }

   public static float outBounce(float $$0) {
      float $$1 = 7.5625F;
      float $$2 = 2.75F;
      if ($$0 < 0.36363637F) {
         return 7.5625F * net.minecraft.util.Mth.square($$0);
      } else if ($$0 < 0.72727275F) {
         return 7.5625F * net.minecraft.util.Mth.square($$0 - 0.54545456F) + 0.75F;
      } else {
         return $$0 < 0.9090909090909091
            ? 7.5625F * net.minecraft.util.Mth.square($$0 - 0.8181818F) + 0.9375F
            : 7.5625F * net.minecraft.util.Mth.square($$0 - 0.95454544F) + 0.984375F;
      }
   }

   public static float outElastic(float $$0) {
      float $$1 = (float) (Math.PI * 2.0 / 3.0);
      if ($$0 == 0.0F) {
         return 0.0F;
      } else {
         return $$0 == 1.0F ? 1.0F : (float)(Math.pow(2.0, -10.0 * $$0) * Math.sin(($$0 * 10.0 - 0.75) * (float) (Math.PI * 2.0 / 3.0)) + 1.0);
      }
   }

   public static float outExpo(float $$0) {
      return $$0 == 1.0F ? 1.0F : 1.0F - (float)Math.pow(2.0, -10.0 * $$0);
   }

   public static float outQuad(float $$0) {
      return 1.0F - net.minecraft.util.Mth.square(1.0F - $$0);
   }

   public static float outQuint(float $$0) {
      return 1.0F - (float)Math.pow(1.0 - $$0, 5.0);
   }

   public static float outSine(float $$0) {
      return net.minecraft.util.Mth.sin($$0 * (float) (Math.PI / 2));
   }

   public static float inOutSine(float $$0) {
      return -(net.minecraft.util.Mth.cos((float) Math.PI * $$0) - 1.0F) / 2.0F;
   }

   public static float outBack(float $$0) {
      float $$1 = 1.70158F;
      float $$2 = 2.70158F;
      return 1.0F + 2.70158F * net.minecraft.util.Mth.cube($$0 - 1.0F) + 1.70158F * net.minecraft.util.Mth.square($$0 - 1.0F);
   }

   public static float outQuart(float $$0) {
      return 1.0F - net.minecraft.util.Mth.square(net.minecraft.util.Mth.square(1.0F - $$0));
   }

   public static float outCubic(float $$0) {
      return 1.0F - net.minecraft.util.Mth.cube(1.0F - $$0);
   }

   public static float inOutExpo(float $$0) {
      if ($$0 < 0.5F) {
         return $$0 == 0.0F ? 0.0F : (float)(Math.pow(2.0, 20.0 * $$0 - 10.0) / 2.0);
      } else {
         return $$0 == 1.0F ? 1.0F : (float)((2.0 - Math.pow(2.0, -20.0 * $$0 + 10.0)) / 2.0);
      }
   }

   public static float inQuad(float $$0) {
      return $$0 * $$0;
   }

   public static float outCirc(float $$0) {
      return (float)Math.sqrt(1.0F - net.minecraft.util.Mth.square($$0 - 1.0F));
   }

   public static float inOutElastic(float $$0) {
      float $$1 = (float) Math.PI * 4.0F / 9.0F;
      if ($$0 == 0.0F) {
         return 0.0F;
      } else if ($$0 == 1.0F) {
         return 1.0F;
      } else {
         double $$2 = Math.sin((20.0 * $$0 - 11.125) * (float) Math.PI * 4.0F / 9.0F);
         return $$0 < 0.5F ? (float)(-(Math.pow(2.0, 20.0 * $$0 - 10.0) * $$2) / 2.0) : (float)(Math.pow(2.0, -20.0 * $$0 + 10.0) * $$2 / 2.0 + 1.0);
      }
   }

   public static float inCirc(float $$0) {
      return (float)(-Math.sqrt(1.0F - $$0 * $$0)) + 1.0F;
   }

   public static float inOutBack(float $$0) {
      float $$1 = 1.70158F;
      float $$2 = 2.5949094F;
      if ($$0 < 0.5F) {
         return 4.0F * $$0 * $$0 * (7.189819F * $$0 - 2.5949094F) / 2.0F;
      } else {
         float $$3 = 2.0F * $$0 - 2.0F;
         return ($$3 * $$3 * (3.5949094F * $$3 + 2.5949094F) + 2.0F) / 2.0F;
      }
   }
}
