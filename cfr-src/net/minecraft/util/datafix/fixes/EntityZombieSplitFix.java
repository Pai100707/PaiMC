/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.function.Supplier;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.EntityRenameFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityZombieSplitFix
extends EntityRenameFix {
    private final Supplier<Type<?>> zombieVillagerType = Suppliers.memoize(() -> this.getOutputSchema().getChoiceType(References.ENTITY, "ZombieVillager"));

    public EntityZombieSplitFix(Schema $$0) {
        super("EntityZombieSplitFix", $$0, true);
    }

    @Override
    protected Pair<String, Typed<?>> fix(String $$02, Typed<?> $$1) {
        Typed<?> $$9;
        String $$8;
        if (!$$02.equals("Zombie")) {
            return Pair.of((Object)$$02, $$1);
        }
        Dynamic $$2 = (Dynamic)$$1.getOptional(DSL.remainderFinder()).orElseThrow();
        int $$3 = $$2.get("ZombieType").asInt(0);
        switch ($$3) {
            default: {
                String $$4 = "Zombie";
                Typed<?> $$5 = $$1;
                break;
            }
            case 1: 
            case 2: 
            case 3: 
            case 4: 
            case 5: {
                String $$6 = "ZombieVillager";
                Typed<?> $$7 = this.changeSchemaToZombieVillager($$1, $$3 - 1);
                break;
            }
            case 6: {
                $$8 = "Husk";
                $$9 = $$1;
            }
        }
        return Pair.of((Object)$$8, (Object)$$9.update(DSL.remainderFinder(), $$0 -> $$0.remove("ZombieType")));
    }

    private Typed<?> changeSchemaToZombieVillager(Typed<?> $$0, int $$12) {
        return Util.writeAndReadTypedOrThrow($$0, this.zombieVillagerType.get(), $$1 -> $$1.set("Profession", $$1.createInt($$12)));
    }
}

