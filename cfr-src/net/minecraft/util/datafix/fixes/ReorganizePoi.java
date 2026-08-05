/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class ReorganizePoi
extends DataFix {
    public ReorganizePoi(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = DSL.named((String)References.POI_CHUNK.typeName(), (Type)DSL.remainderType());
        if (!Objects.equals($$0, this.getInputSchema().getType(References.POI_CHUNK))) {
            throw new IllegalStateException("Poi type is not what was expected.");
        }
        return this.fixTypeEverywhere("POI reorganization", $$0, $$02 -> $$0 -> $$0.mapSecond(ReorganizePoi::cap));
    }

    private static <T> Dynamic<T> cap(Dynamic<T> $$0) {
        HashMap $$1 = Maps.newHashMap();
        for (int $$2 = 0; $$2 < 16; ++$$2) {
            String $$3 = String.valueOf($$2);
            Optional $$4 = $$0.get($$3).result();
            if (!$$4.isPresent()) continue;
            Dynamic $$5 = (Dynamic)$$4.get();
            Dynamic $$6 = $$0.createMap((Map)ImmutableMap.of((Object)$$0.createString("Records"), (Object)$$5));
            $$1.put($$0.createString(Integer.toString($$2)), $$6);
            $$0 = $$0.remove($$3);
        }
        return $$0.set("Sections", $$0.createMap((Map)$$1));
    }
}

