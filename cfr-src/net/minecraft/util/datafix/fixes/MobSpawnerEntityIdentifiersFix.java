/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class MobSpawnerEntityIdentifiersFix
extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    private Dynamic<?> fix(Dynamic<?> $$02) {
        Optional $$3;
        if (!"MobSpawner".equals($$02.get("id").asString(""))) {
            return $$02;
        }
        Optional $$1 = $$02.get("EntityId").asString().result();
        if ($$1.isPresent()) {
            Dynamic $$2 = (Dynamic)DataFixUtils.orElse((Optional)$$02.get("SpawnData").result(), (Object)$$02.emptyMap());
            $$2 = $$2.set("id", $$2.createString(((String)$$1.get()).isEmpty() ? "Pig" : (String)$$1.get()));
            $$02 = $$02.set("SpawnData", $$2);
            $$02 = $$02.remove("EntityId");
        }
        if (($$3 = $$02.get("SpawnPotentials").asStreamOpt().result()).isPresent()) {
            $$02 = $$02.set("SpawnPotentials", $$02.createList(((Stream)$$3.get()).map($$0 -> {
                Optional $$1 = $$0.get("Type").asString().result();
                if ($$1.isPresent()) {
                    Dynamic $$2 = ((Dynamic)DataFixUtils.orElse((Optional)$$0.get("Properties").result(), (Object)$$0.emptyMap())).set("id", $$0.createString((String)$$1.get()));
                    return $$0.set("Entity", $$2).remove("Type").remove("Properties");
                }
                return $$0;
            })));
        }
        return $$02;
    }

    public TypeRewriteRule makeRule() {
        Type $$0 = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), $$0, $$1 -> {
            Dynamic $$2 = (Dynamic)$$1.get(DSL.remainderFinder());
            DataResult $$3 = $$0.readTyped(this.fix($$2 = $$2.set("id", $$2.createString("MobSpawner"))));
            if ($$3.result().isEmpty()) {
                return $$1;
            }
            return (Typed)((Pair)$$3.result().get()).getFirst();
        });
    }
}

