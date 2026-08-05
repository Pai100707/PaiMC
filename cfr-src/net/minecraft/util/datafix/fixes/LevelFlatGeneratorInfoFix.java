/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  org.apache.commons.lang3.math.NumberUtils
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.EntityBlockStateFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.math.NumberUtils;

public class LevelFlatGeneratorInfoFix
extends DataFix {
    private static final String GENERATOR_OPTIONS = "generatorOptions";
    @VisibleForTesting
    static final String DEFAULT = "minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block;1;village";
    private static final Splitter SPLITTER = Splitter.on((char)';').limit(5);
    private static final Splitter LAYER_SPLITTER = Splitter.on((char)',');
    private static final Splitter OLD_AMOUNT_SPLITTER = Splitter.on((char)'x').limit(2);
    private static final Splitter AMOUNT_SPLITTER = Splitter.on((char)'*').limit(2);
    private static final Splitter BLOCK_SPLITTER = Splitter.on((char)':').limit(3);

    public LevelFlatGeneratorInfoFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("LevelFlatGeneratorInfoFix", this.getInputSchema().getType(References.LEVEL), $$0 -> $$0.update(DSL.remainderFinder(), this::fix));
    }

    private Dynamic<?> fix(Dynamic<?> $$02) {
        if ($$02.get("generatorName").asString("").equalsIgnoreCase("flat")) {
            return $$02.update(GENERATOR_OPTIONS, $$0 -> (Dynamic)DataFixUtils.orElse((Optional)$$0.asString().map(this::fixString).map(arg_0 -> ((Dynamic)$$0).createString(arg_0)).result(), (Object)$$0));
        }
        return $$02;
    }

    @VisibleForTesting
    String fixString(String $$0) {
        String $$6;
        int $$5;
        if ($$0.isEmpty()) {
            return DEFAULT;
        }
        Iterator $$1 = SPLITTER.split((CharSequence)$$0).iterator();
        String $$22 = (String)$$1.next();
        if ($$1.hasNext()) {
            int $$3 = NumberUtils.toInt((String)$$22, (int)0);
            String $$4 = (String)$$1.next();
        } else {
            $$5 = 0;
            $$6 = $$22;
        }
        if ($$5 < 0 || $$5 > 3) {
            return DEFAULT;
        }
        StringBuilder $$7 = new StringBuilder();
        Splitter $$8 = $$5 < 3 ? OLD_AMOUNT_SPLITTER : AMOUNT_SPLITTER;
        $$7.append(StreamSupport.stream(LAYER_SPLITTER.split((CharSequence)$$6).spliterator(), false).map($$2 -> {
            String $$7;
            int $$6;
            List $$3 = $$8.splitToList((CharSequence)$$2);
            if ($$3.size() == 2) {
                int $$4 = NumberUtils.toInt((String)((String)$$3.get(0)));
                String $$5 = (String)$$3.get(1);
            } else {
                $$6 = 1;
                $$7 = (String)$$3.get(0);
            }
            List $$8 = BLOCK_SPLITTER.splitToList((CharSequence)$$7);
            int $$9 = ((String)$$8.get(0)).equals("minecraft") ? 1 : 0;
            String $$10 = (String)$$8.get($$9);
            int $$11 = $$5 == 3 ? EntityBlockStateFix.getBlockId("minecraft:" + $$10) : NumberUtils.toInt((String)$$10, (int)0);
            int $$12 = $$9 + 1;
            int $$13 = $$8.size() > $$12 ? NumberUtils.toInt((String)((String)$$8.get($$12)), (int)0) : 0;
            return (String)($$6 == 1 ? "" : $$6 + "*") + BlockStateData.getTag($$11 << 4 | $$13).get("Name").asString("");
        }).collect(Collectors.joining(",")));
        while ($$1.hasNext()) {
            $$7.append(';').append((String)$$1.next());
        }
        return $$7.toString();
    }
}

