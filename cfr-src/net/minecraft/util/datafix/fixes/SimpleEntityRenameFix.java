/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.EntityRenameFix;

public abstract class SimpleEntityRenameFix
extends EntityRenameFix {
    public SimpleEntityRenameFix(String $$0, Schema $$1, boolean $$2) {
        super($$0, $$1, $$2);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String $$0, Typed<?> $$1) {
        Pair<String, Dynamic<?>> $$2 = this.getNewNameAndTag($$0, (Dynamic)$$1.getOrCreate(DSL.remainderFinder()));
        return Pair.of((Object)((String)$$2.getFirst()), (Object)$$1.set(DSL.remainderFinder(), (Object)((Dynamic)$$2.getSecond())));
    }

    protected abstract Pair<String, Dynamic<?>> getNewNameAndTag(String var1, Dynamic<?> var2);
}

