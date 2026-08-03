package net.minecraft.world.flag;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final net.minecraft.world.flag.FeatureFlagUniverse universe;
   private final Map<Identifier, net.minecraft.world.flag.FeatureFlag> names;
   private final net.minecraft.world.flag.FeatureFlagSet allFlags;

   FeatureFlagRegistry(
      net.minecraft.world.flag.FeatureFlagUniverse $$0, net.minecraft.world.flag.FeatureFlagSet $$1, Map<Identifier, net.minecraft.world.flag.FeatureFlag> $$2
   ) {
      this.universe = $$0;
      this.names = $$2;
      this.allFlags = $$1;
   }

   public boolean isSubset(net.minecraft.world.flag.FeatureFlagSet $$0) {
      return $$0.isSubsetOf(this.allFlags);
   }

   public net.minecraft.world.flag.FeatureFlagSet allFlags() {
      return this.allFlags;
   }

   public net.minecraft.world.flag.FeatureFlagSet fromNames(Iterable<Identifier> $$0) {
      return this.fromNames($$0, $$0x -> LOGGER.warn("Unknown feature flag: {}", $$0x));
   }

   public net.minecraft.world.flag.FeatureFlagSet subset(net.minecraft.world.flag.FeatureFlag... $$0) {
      return net.minecraft.world.flag.FeatureFlagSet.create(this.universe, Arrays.asList($$0));
   }

   public net.minecraft.world.flag.FeatureFlagSet fromNames(Iterable<Identifier> $$0, Consumer<Identifier> $$1) {
      Set<net.minecraft.world.flag.FeatureFlag> $$2 = Sets.newIdentityHashSet();

      for (Identifier $$3 : $$0) {
         net.minecraft.world.flag.FeatureFlag $$4 = this.names.get($$3);
         if ($$4 == null) {
            $$1.accept($$3);
         } else {
            $$2.add($$4);
         }
      }

      return net.minecraft.world.flag.FeatureFlagSet.create(this.universe, $$2);
   }

   public Set<Identifier> toNames(net.minecraft.world.flag.FeatureFlagSet $$0) {
      Set<Identifier> $$1 = new HashSet<>();
      this.names.forEach(($$2, $$3) -> {
         if ($$0.contains($$3)) {
            $$1.add($$2);
         }
      });
      return $$1;
   }

   public Codec<net.minecraft.world.flag.FeatureFlagSet> codec() {
      return Identifier.CODEC.listOf().comapFlatMap($$0 -> {
         Set<Identifier> $$1 = new HashSet<>();
         net.minecraft.world.flag.FeatureFlagSet $$2 = this.fromNames($$0, $$1::add);
         return !$$1.isEmpty() ? DataResult.error(() -> "Unknown feature ids: " + $$1, $$2) : DataResult.success($$2);
      }, $$0 -> List.copyOf(this.toNames($$0)));
   }

   public static class Builder {
      private final net.minecraft.world.flag.FeatureFlagUniverse universe;
      private int id;
      private final Map<Identifier, net.minecraft.world.flag.FeatureFlag> flags = new LinkedHashMap<>();

      public Builder(String $$0) {
         this.universe = new net.minecraft.world.flag.FeatureFlagUniverse($$0);
      }

      public net.minecraft.world.flag.FeatureFlag createVanilla(String $$0) {
         return this.create(Identifier.withDefaultNamespace($$0));
      }

      public net.minecraft.world.flag.FeatureFlag create(Identifier $$0) {
         if (this.id >= 64) {
            throw new IllegalStateException("Too many feature flags");
         } else {
            net.minecraft.world.flag.FeatureFlag $$1 = new net.minecraft.world.flag.FeatureFlag(this.universe, this.id++);
            net.minecraft.world.flag.FeatureFlag $$2 = this.flags.put($$0, $$1);
            if ($$2 != null) {
               throw new IllegalStateException("Duplicate feature flag " + $$0);
            } else {
               return $$1;
            }
         }
      }

      public net.minecraft.world.flag.FeatureFlagRegistry build() {
         net.minecraft.world.flag.FeatureFlagSet $$0 = net.minecraft.world.flag.FeatureFlagSet.create(this.universe, this.flags.values());
         return new net.minecraft.world.flag.FeatureFlagRegistry(this.universe, $$0, Map.copyOf(this.flags));
      }
   }
}
