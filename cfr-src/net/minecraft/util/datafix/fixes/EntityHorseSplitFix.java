/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityHorseSplitFix
extends EntityRenameFix {
    public EntityHorseSplitFix(Schema $$0, boolean $$1) {
        super("EntityHorseSplitFix", $$0, $$1);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String $$02, Typed<?> $$1) {
        if (Objects.equals("EntityHorse", $$02)) {
            Dynamic $$2 = (Dynamic)$$1.get(DSL.remainderFinder());
            int $$3 = $$2.get("Type").asInt(0);
            String $$4 = switch ($$3) {
                default -> "Horse";
                case 1 -> "Donkey";
                case 2 -> "Mule";
                case 3 -> "ZombieHorse";
                case 4 -> "SkeletonHorse";
            };
            Type $$5 = (Type)this.getOutputSchema().findChoiceType(References.ENTITY).types().get($$4);
            return Pair.of((Object)$$4, Util.writeAndReadTypedOrThrow($$1, $$5, $$0 -> $$0.remove("Type")));
        }
        return Pair.of((Object)$$02, $$1);
    }
}

