/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.slf4j.Logger
 */
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
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.slf4j.Logger;

public class FeatureFlagRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final FeatureFlagUniverse universe;
    private final Map<Identifier, FeatureFlag> names;
    private final FeatureFlagSet allFlags;

    FeatureFlagRegistry(FeatureFlagUniverse $$0, FeatureFlagSet $$1, Map<Identifier, FeatureFlag> $$2) {
        this.universe = $$0;
        this.names = $$2;
        this.allFlags = $$1;
    }

    public boolean isSubset(FeatureFlagSet $$0) {
        return $$0.isSubsetOf(this.allFlags);
    }

    public FeatureFlagSet allFlags() {
        return this.allFlags;
    }

    public FeatureFlagSet fromNames(Iterable<Identifier> $$02) {
        return this.fromNames($$02, $$0 -> LOGGER.warn("Unknown feature flag: {}", $$0));
    }

    public FeatureFlagSet subset(FeatureFlag ... $$0) {
        return FeatureFlagSet.create(this.universe, Arrays.asList($$0));
    }

    public FeatureFlagSet fromNames(Iterable<Identifier> $$0, Consumer<Identifier> $$1) {
        Set $$2 = Sets.newIdentityHashSet();
        for (Identifier $$3 : $$0) {
            FeatureFlag $$4 = this.names.get($$3);
            if ($$4 == null) {
                $$1.accept($$3);
                continue;
            }
            $$2.add($$4);
        }
        return FeatureFlagSet.create(this.universe, $$2);
    }

    public Set<Identifier> toNames(FeatureFlagSet $$0) {
        HashSet<Identifier> $$1 = new HashSet<Identifier>();
        this.names.forEach(($$2, $$3) -> {
            if ($$0.contains((FeatureFlag)$$3)) {
                $$1.add((Identifier)$$2);
            }
        });
        return $$1;
    }

    public Codec<FeatureFlagSet> codec() {
        return Identifier.CODEC.listOf().comapFlatMap($$0 -> {
            HashSet $$1 = new HashSet();
            FeatureFlagSet $$2 = this.fromNames((Iterable<Identifier>)$$0, $$1::add);
            if (!$$1.isEmpty()) {
                return DataResult.error(() -> "Unknown feature ids: " + String.valueOf($$1), (Object)$$2);
            }
            return DataResult.success((Object)$$2);
        }, $$0 -> List.copyOf(this.toNames((FeatureFlagSet)$$0)));
    }

    public static class Builder {
        private final FeatureFlagUniverse universe;
        private int id;
        private final Map<Identifier, FeatureFlag> flags = new LinkedHashMap<Identifier, FeatureFlag>();

        public Builder(String $$0) {
            this.universe = new FeatureFlagUniverse($$0);
        }

        public FeatureFlag createVanilla(String $$0) {
            return this.create(Identifier.withDefaultNamespace($$0));
        }

        public FeatureFlag create(Identifier $$0) {
            FeatureFlag $$1;
            FeatureFlag $$2;
            if (this.id >= 64) {
                throw new IllegalStateException("Too many feature flags");
            }
            if (($$2 = this.flags.put($$0, $$1 = new FeatureFlag(this.universe, this.id++))) != null) {
                throw new IllegalStateException("Duplicate feature flag " + String.valueOf($$0));
            }
            return $$1;
        }

        public FeatureFlagRegistry build() {
            FeatureFlagSet $$0 = FeatureFlagSet.create(this.universe, this.flags.values());
            return new FeatureFlagRegistry(this.universe, $$0, Map.copyOf(this.flags));
        }
    }
}

