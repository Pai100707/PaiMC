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
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityMinecartIdentifiersFix
extends EntityRenameFix {
    public EntityMinecartIdentifiersFix(Schema $$0) {
        super("EntityMinecartIdentifiersFix", $$0, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String $$02, Typed<?> $$1) {
        if (!$$02.equals("Minecart")) {
            return Pair.of((Object)$$02, $$1);
        }
        int $$2 = ((Dynamic)$$1.getOrCreate(DSL.remainderFinder())).get("Type").asInt(0);
        String $$3 = switch ($$2) {
            default -> "MinecartRideable";
            case 1 -> "MinecartChest";
            case 2 -> "MinecartFurnace";
        };
        Type $$4 = (Type)this.getOutputSchema().findChoiceType(References.ENTITY).types().get($$3);
        return Pair.of((Object)$$3, Util.writeAndReadTypedOrThrow($$1, $$4, $$0 -> $$0.remove("Type")));
    }
}

