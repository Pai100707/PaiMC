/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class SavedDataFeaturePoolElementFix
extends DataFix {
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    private static final Set<String> PIECE_TYPE = Sets.newHashSet((Object[])new String[]{"minecraft:jigsaw", "minecraft:nvi", "minecraft:pcp", "minecraft:bastionremnant", "minecraft:runtime"});
    private static final Set<String> FEATURES = Sets.newHashSet((Object[])new String[]{"minecraft:tree", "minecraft:flower", "minecraft:block_pile", "minecraft:random_patch"});

    public SavedDataFeaturePoolElementFix(Schema $$0) {
        super($$0, false);
    }

    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead("SavedDataFeaturePoolElementFix", this.getInputSchema().getType(References.STRUCTURE_FEATURE), this.getOutputSchema().getType(References.STRUCTURE_FEATURE), SavedDataFeaturePoolElementFix::fixTag);
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> $$0) {
        return $$0.update("Children", SavedDataFeaturePoolElementFix::updateChildren);
    }

    private static <T> Dynamic<T> updateChildren(Dynamic<T> $$0) {
        return $$0.asStreamOpt().map(SavedDataFeaturePoolElementFix::updateChildren).map(arg_0 -> $$0.createList(arg_0)).result().orElse($$0);
    }

    private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> $$0) {
        return $$0.map($$02 -> {
            String $$1 = $$02.get("id").asString("");
            if (!PIECE_TYPE.contains($$1)) {
                return $$02;
            }
            OptionalDynamic $$2 = $$02.get("pool_element");
            if (!$$2.get("element_type").asString("").equals("minecraft:feature_pool_element")) {
                return $$02;
            }
            return $$02.update("pool_element", $$0 -> $$0.update("feature", SavedDataFeaturePoolElementFix::fixFeature));
        });
    }

    private static <T> OptionalDynamic<T> get(Dynamic<T> $$0, String ... $$1) {
        if ($$1.length == 0) {
            throw new IllegalArgumentException("Missing path");
        }
        OptionalDynamic $$2 = $$0.get($$1[0]);
        for (int $$3 = 1; $$3 < $$1.length; ++$$3) {
            String $$4 = $$1[$$3];
            Matcher $$5 = INDEX_PATTERN.matcher($$4);
            if ($$5.matches()) {
                int $$6 = Integer.parseInt($$5.group(1));
                List $$7 = $$2.asList(Function.identity());
                if ($$6 >= 0 && $$6 < $$7.size()) {
                    $$2 = new OptionalDynamic($$0.getOps(), DataResult.success((Object)((Dynamic)$$7.get($$6))));
                    continue;
                }
                $$2 = new OptionalDynamic($$0.getOps(), DataResult.error(() -> "Missing id:" + $$6));
                continue;
            }
            $$2 = $$2.get($$4);
        }
        return $$2;
    }

    @VisibleForTesting
    protected static Dynamic<?> fixFeature(Dynamic<?> $$0) {
        Optional<String> $$1 = SavedDataFeaturePoolElementFix.getReplacement(SavedDataFeaturePoolElementFix.get($$0, "type").asString(""), SavedDataFeaturePoolElementFix.get($$0, "name").asString(""), SavedDataFeaturePoolElementFix.get($$0, "config", "state_provider", "type").asString(""), SavedDataFeaturePoolElementFix.get($$0, "config", "state_provider", "state", "Name").asString(""), SavedDataFeaturePoolElementFix.get($$0, "config", "state_provider", "entries", "[0]", "data", "Name").asString(""), SavedDataFeaturePoolElementFix.get($$0, "config", "foliage_placer", "type").asString(""), SavedDataFeaturePoolElementFix.get($$0, "config", "leaves_provider", "state", "Name").asString(""));
        if ($$1.isPresent()) {
            return $$0.createString($$1.get());
        }
        return $$0;
    }

    /*
     * WARNING - void declaration
     */
    private static Optional<String> getReplacement(String $$0, String $$1, String $$2, String $$3, String $$4, String $$5, String $$6) {
        void $$10;
        if (!$$0.isEmpty()) {
            String $$7 = $$0;
        } else if (!$$1.isEmpty()) {
            if ("minecraft:normal_tree".equals($$1)) {
                String $$8 = "minecraft:tree";
            } else {
                String $$9 = $$1;
            }
        } else {
            return Optional.empty();
        }
        if (FEATURES.contains($$10)) {
            if ("minecraft:random_patch".equals($$10)) {
                if ("minecraft:simple_state_provider".equals($$2)) {
                    if ("minecraft:sweet_berry_bush".equals($$3)) {
                        return Optional.of("minecraft:patch_berry_bush");
                    }
                    if ("minecraft:cactus".equals($$3)) {
                        return Optional.of("minecraft:patch_cactus");
                    }
                } else if ("minecraft:weighted_state_provider".equals($$2) && ("minecraft:grass".equals($$4) || "minecraft:fern".equals($$4))) {
                    return Optional.of("minecraft:patch_taiga_grass");
                }
            } else if ("minecraft:block_pile".equals($$10)) {
                if ("minecraft:simple_state_provider".equals($$2) || "minecraft:rotated_block_provider".equals($$2)) {
                    if ("minecraft:hay_block".equals($$3)) {
                        return Optional.of("minecraft:pile_hay");
                    }
                    if ("minecraft:melon".equals($$3)) {
                        return Optional.of("minecraft:pile_melon");
                    }
                    if ("minecraft:snow".equals($$3)) {
                        return Optional.of("minecraft:pile_snow");
                    }
                } else if ("minecraft:weighted_state_provider".equals($$2)) {
                    if ("minecraft:packed_ice".equals($$4) || "minecraft:blue_ice".equals($$4)) {
                        return Optional.of("minecraft:pile_ice");
                    }
                    if ("minecraft:jack_o_lantern".equals($$4) || "minecraft:pumpkin".equals($$4)) {
                        return Optional.of("minecraft:pile_pumpkin");
                    }
                }
            } else {
                if ("minecraft:flower".equals($$10)) {
                    return Optional.of("minecraft:flower_plain");
                }
                if ("minecraft:tree".equals($$10)) {
                    if ("minecraft:acacia_foliage_placer".equals($$5)) {
                        return Optional.of("minecraft:acacia");
                    }
                    if ("minecraft:blob_foliage_placer".equals($$5) && "minecraft:oak_leaves".equals($$6)) {
                        return Optional.of("minecraft:oak");
                    }
                    if ("minecraft:pine_foliage_placer".equals($$5)) {
                        return Optional.of("minecraft:pine");
                    }
                    if ("minecraft:spruce_foliage_placer".equals($$5)) {
                        return Optional.of("minecraft:spruce");
                    }
                }
            }
        }
        return Optional.empty();
    }
}

