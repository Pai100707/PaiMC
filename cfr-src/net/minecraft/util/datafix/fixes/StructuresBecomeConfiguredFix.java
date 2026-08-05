/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.LongStream;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class StructuresBecomeConfiguredFix
extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Conversion> CONVERSION_MAP = ImmutableMap.builder().put((Object)"mineshaft", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"), "minecraft:mineshaft")).put((Object)"shipwreck", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck")).put((Object)"ocean_ruin", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"), "minecraft:ocean_ruin_cold")).put((Object)"village", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:village_desert", List.of("minecraft:savanna"), "minecraft:village_savanna", List.of("minecraft:snowy_plains"), "minecraft:village_snowy", List.of("minecraft:taiga"), "minecraft:village_taiga"), "minecraft:village_plains")).put((Object)"ruined_portal", (Object)Conversion.biomeMapped(Map.of(List.of("minecraft:desert"), "minecraft:ruined_portal_desert", List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands", "minecraft:windswept_hills", "minecraft:windswept_forest", "minecraft:windswept_gravelly_hills", "minecraft:savanna_plateau", "minecraft:windswept_savanna", "minecraft:stony_shore", "minecraft:meadow", "minecraft:frozen_peaks", "minecraft:jagged_peaks", "minecraft:stony_peaks", "minecraft:snowy_slopes"), "minecraft:ruined_portal_mountain", List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"), "minecraft:ruined_portal_jungle", List.of("minecraft:deep_frozen_ocean", "minecraft:deep_cold_ocean", "minecraft:deep_ocean", "minecraft:deep_lukewarm_ocean", "minecraft:frozen_ocean", "minecraft:ocean", "minecraft:cold_ocean", "minecraft:lukewarm_ocean", "minecraft:warm_ocean"), "minecraft:ruined_portal_ocean"), "minecraft:ruined_portal")).put((Object)"pillager_outpost", (Object)Conversion.trivial("minecraft:pillager_outpost")).put((Object)"mansion", (Object)Conversion.trivial("minecraft:mansion")).put((Object)"jungle_pyramid", (Object)Conversion.trivial("minecraft:jungle_pyramid")).put((Object)"desert_pyramid", (Object)Conversion.trivial("minecraft:desert_pyramid")).put((Object)"igloo", (Object)Conversion.trivial("minecraft:igloo")).put((Object)"swamp_hut", (Object)Conversion.trivial("minecraft:swamp_hut")).put((Object)"stronghold", (Object)Conversion.trivial("minecraft:stronghold")).put((Object)"monument", (Object)Conversion.trivial("minecraft:monument")).put((Object)"fortress", (Object)Conversion.trivial("minecraft:fortress")).put((Object)"endcity", (Object)Conversion.trivial("minecraft:end_city")).put((Object)"buried_treasure", (Object)Conversion.trivial("minecraft:buried_treasure")).put((Object)"nether_fossil", (Object)Conversion.trivial("minecraft:nether_fossil")).put((Object)"bastion_remnant", (Object)Conversion.trivial("minecraft:bastion_remnant")).build();

    public StructuresBecomeConfiguredFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.CHUNK);
        Type $$1 = this.getInputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", $$0, $$1, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> $$0) {
        return $$0.update("structures", $$12 -> $$12.update("starts", $$1 -> this.updateStarts((Dynamic<?>)$$1, $$0)).update("References", $$1 -> this.updateReferences((Dynamic<?>)$$1, $$0)));
    }

    private Dynamic<?> updateStarts(Dynamic<?> $$0, Dynamic<?> $$1) {
        Map<Dynamic, Dynamic> $$2 = $$0.getMapValues().result().orElse(Map.of());
        HashMap $$32 = Maps.newHashMap();
        $$2.forEach(($$22, $$3) -> {
            if ($$3.get("id").asString("INVALID").equals("INVALID")) {
                return;
            }
            Dynamic<?> $$4 = this.findUpdatedStructureType((Dynamic<?>)$$22, $$1);
            if ($$4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: {}", (Object)$$22.asString("<missing key>"));
                return;
            }
            $$32.computeIfAbsent($$4, $$2 -> $$3.set("id", $$4));
        });
        return $$1.createMap((Map)$$32);
    }

    private Dynamic<?> updateReferences(Dynamic<?> $$0, Dynamic<?> $$1) {
        Map<Dynamic, Dynamic> $$2 = $$0.getMapValues().result().orElse(Map.of());
        HashMap $$32 = Maps.newHashMap();
        $$2.forEach(($$22, $$3) -> {
            if ($$3.asLongStream().count() == 0L) {
                return;
            }
            Dynamic<?> $$4 = this.findUpdatedStructureType((Dynamic<?>)$$22, $$1);
            if ($$4 == null) {
                LOGGER.warn("Encountered unknown structure in datafixer: {}", (Object)$$22.asString("<missing key>"));
                return;
            }
            $$32.compute($$4, ($$1, $$2) -> {
                if ($$2 == null) {
                    return $$3;
                }
                return $$3.createLongList(LongStream.concat($$2.asLongStream(), $$3.asLongStream()));
            });
        });
        return $$1.createMap((Map)$$32);
    }

    private @Nullable Dynamic<?> findUpdatedStructureType(Dynamic<?> $$0, Dynamic<?> $$1) {
        Optional<String> $$5;
        String $$2 = $$0.asString("UNKNOWN").toLowerCase(Locale.ROOT);
        Conversion $$3 = CONVERSION_MAP.get($$2);
        if ($$3 == null) {
            return null;
        }
        String $$4 = $$3.fallback;
        if (!$$3.biomeMapping().isEmpty() && ($$5 = this.guessConfiguration($$1, $$3)).isPresent()) {
            $$4 = $$5.get();
        }
        return $$1.createString($$4);
    }

    private Optional<String> guessConfiguration(Dynamic<?> $$0, Conversion $$1) {
        Object2IntArrayMap $$2 = new Object2IntArrayMap();
        $$0.get("sections").asList(Function.identity()).forEach($$22 -> $$22.get("biomes").get("palette").asList(Function.identity()).forEach($$2 -> {
            String $$3 = $$1.biomeMapping().get($$2.asString(""));
            if ($$3 != null) {
                $$2.mergeInt((Object)$$3, 1, Integer::sum);
            }
        }));
        return $$2.object2IntEntrySet().stream().max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue)).map(Map.Entry::getKey);
    }

    static final class Conversion
    extends Record {
        private final Map<String, String> biomeMapping;
        final String fallback;

        private Conversion(Map<String, String> $$0, String $$1) {
            this.biomeMapping = $$0;
            this.fallback = $$1;
        }

        public static Conversion trivial(String $$0) {
            return new Conversion(Map.of(), $$0);
        }

        public static Conversion biomeMapped(Map<List<String>, String> $$0, String $$1) {
            return new Conversion(Conversion.unpack($$0), $$1);
        }

        private static Map<String, String> unpack(Map<List<String>, String> $$0) {
            ImmutableMap.Builder $$1 = ImmutableMap.builder();
            for (Map.Entry<List<String>, String> $$22 : $$0.entrySet()) {
                $$22.getKey().forEach($$2 -> $$1.put($$2, (Object)((String)$$22.getValue())));
            }
            return $$1.build();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this);
        }

        @Override
        public final boolean equals(Object $$0) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Conversion.class, "biomeMapping;fallback", "biomeMapping", "fallback"}, this, $$0);
        }

        public Map<String, String> biomeMapping() {
            return this.biomeMapping;
        }

        public String fallback() {
            return this.fallback;
        }
    }
}

