package net.minecraft.world.flag;

import it.unimi.dsi.fastutil.HashCommon;
import java.util.Arrays;
import java.util.Collection;
import org.jspecify.annotations.Nullable;

public final class FeatureFlagSet {
   private static final net.minecraft.world.flag.FeatureFlagSet EMPTY = new net.minecraft.world.flag.FeatureFlagSet(null, 0L);
   public static final int MAX_CONTAINER_SIZE = 64;
   @Nullable
   private final net.minecraft.world.flag.FeatureFlagUniverse universe;
   private final long mask;

   private FeatureFlagSet(@Nullable net.minecraft.world.flag.FeatureFlagUniverse $$0, long $$1) {
      this.universe = $$0;
      this.mask = $$1;
   }

   static net.minecraft.world.flag.FeatureFlagSet create(net.minecraft.world.flag.FeatureFlagUniverse $$0, Collection<net.minecraft.world.flag.FeatureFlag> $$1) {
      if ($$1.isEmpty()) {
         return EMPTY;
      } else {
         long $$2 = computeMask($$0, 0L, $$1);
         return new net.minecraft.world.flag.FeatureFlagSet($$0, $$2);
      }
   }

   public static net.minecraft.world.flag.FeatureFlagSet of() {
      return EMPTY;
   }

   public static net.minecraft.world.flag.FeatureFlagSet of(net.minecraft.world.flag.FeatureFlag $$0) {
      return new net.minecraft.world.flag.FeatureFlagSet($$0.universe, $$0.mask);
   }

   public static net.minecraft.world.flag.FeatureFlagSet of(net.minecraft.world.flag.FeatureFlag $$0, net.minecraft.world.flag.FeatureFlag... $$1) {
      long $$2 = $$1.length == 0 ? $$0.mask : computeMask($$0.universe, $$0.mask, Arrays.asList($$1));
      return new net.minecraft.world.flag.FeatureFlagSet($$0.universe, $$2);
   }

   private static long computeMask(net.minecraft.world.flag.FeatureFlagUniverse $$0, long $$1, Iterable<net.minecraft.world.flag.FeatureFlag> $$2) {
      for (net.minecraft.world.flag.FeatureFlag $$3 : $$2) {
         if ($$0 != $$3.universe) {
            throw new IllegalStateException("Mismatched feature universe, expected '" + $$0 + "', but got '" + $$3.universe + "'");
         }

         $$1 |= $$3.mask;
      }

      return $$1;
   }

   public boolean contains(net.minecraft.world.flag.FeatureFlag $$0) {
      return this.universe != $$0.universe ? false : (this.mask & $$0.mask) != 0L;
   }

   public boolean isEmpty() {
      return this.equals(EMPTY);
   }

   public boolean isSubsetOf(net.minecraft.world.flag.FeatureFlagSet $$0) {
      if (this.universe == null) {
         return true;
      } else {
         return this.universe != $$0.universe ? false : (this.mask & ~$$0.mask) == 0L;
      }
   }

   public boolean intersects(net.minecraft.world.flag.FeatureFlagSet $$0) {
      return this.universe != null && $$0.universe != null && this.universe == $$0.universe ? (this.mask & $$0.mask) != 0L : false;
   }

   public net.minecraft.world.flag.FeatureFlagSet join(net.minecraft.world.flag.FeatureFlagSet $$0) {
      if (this.universe == null) {
         return $$0;
      } else if ($$0.universe == null) {
         return this;
      } else if (this.universe != $$0.universe) {
         throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + $$0.universe + "'");
      } else {
         return new net.minecraft.world.flag.FeatureFlagSet(this.universe, this.mask | $$0.mask);
      }
   }

   public net.minecraft.world.flag.FeatureFlagSet subtract(net.minecraft.world.flag.FeatureFlagSet $$0) {
      if (this.universe == null || $$0.universe == null) {
         return this;
      } else if (this.universe != $$0.universe) {
         throw new IllegalArgumentException("Mismatched set elements: '" + this.universe + "' != '" + $$0.universe + "'");
      } else {
         long $$1 = this.mask & ~$$0.mask;
         return $$1 == 0L ? EMPTY : new net.minecraft.world.flag.FeatureFlagSet(this.universe, $$1);
      }
   }

   @Override
   public boolean equals(Object $$0) {
      return this == $$0 ? true : $$0 instanceof net.minecraft.world.flag.FeatureFlagSet $$1 && this.universe == $$1.universe && this.mask == $$1.mask;
   }

   @Override
   public int hashCode() {
      return (int)HashCommon.mix(this.mask);
   }
}
