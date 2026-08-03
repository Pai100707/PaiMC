package net.minecraft.util;

import java.util.function.Supplier;
import org.apache.commons.lang3.ObjectUtils;

public record ModCheck(net.minecraft.util.ModCheck.Confidence confidence, String description) {
   public static net.minecraft.util.ModCheck identify(String $$0, Supplier<String> $$1, String $$2, Class<?> $$3) {
      String $$4 = $$1.get();
      if (!$$0.equals($$4)) {
         return new net.minecraft.util.ModCheck(net.minecraft.util.ModCheck.Confidence.DEFINITELY, $$2 + " brand changed to '" + $$4 + "'");
      } else {
         return $$3.getSigners() == null
            ? new net.minecraft.util.ModCheck(net.minecraft.util.ModCheck.Confidence.VERY_LIKELY, $$2 + " jar signature invalidated")
            : new net.minecraft.util.ModCheck(net.minecraft.util.ModCheck.Confidence.PROBABLY_NOT, $$2 + " jar signature and brand is untouched");
      }
   }

   public boolean shouldReportAsModified() {
      return this.confidence.shouldReportAsModified;
   }

   public net.minecraft.util.ModCheck merge(net.minecraft.util.ModCheck $$0) {
      return new net.minecraft.util.ModCheck(
         (net.minecraft.util.ModCheck.Confidence)ObjectUtils.max(new net.minecraft.util.ModCheck.Confidence[]{this.confidence, $$0.confidence}),
         this.description + "; " + $$0.description
      );
   }

   public String fullDescription() {
      return this.confidence.description + " " + this.description;
   }

   public static enum Confidence {
      PROBABLY_NOT("Probably not.", false),
      VERY_LIKELY("Very likely;", true),
      DEFINITELY("Definitely;", true);

      final String description;
      final boolean shouldReportAsModified;

      private Confidence(final String $$0, final boolean $$1) {
         this.description = $$0;
         this.shouldReportAsModified = $$1;
      }
   }
}
