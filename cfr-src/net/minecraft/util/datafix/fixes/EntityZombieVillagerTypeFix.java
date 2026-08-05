/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityZombieVillagerTypeFix
extends NamedEntityFix {
    private static final int PROFESSION_MAX = 6;

    public EntityZombieVillagerTypeFix(Schema $$0, boolean $$1) {
        super($$0, $$1, "EntityZombieVillagerTypeFix", References.ENTITY, "Zombie");
    }

    public Dynamic<?> fixTag(Dynamic<?> $$0) {
        if ($$0.get("IsVillager").asBoolean(false)) {
            if ($$0.get("ZombieType").result().isEmpty()) {
                int $$1 = this.getVillagerProfession($$0.get("VillagerProfession").asInt(-1));
                if ($$1 == -1) {
                    $$1 = this.getVillagerProfession(RandomSource.create().nextInt(6));
                }
                $$0 = $$0.set("ZombieType", $$0.createInt($$1));
            }
            $$0 = $$0.remove("IsVillager");
        }
        return $$0;
    }

    private int getVillagerProfession(int $$0) {
        if ($$0 < 0 || $$0 >= 6) {
            return -1;
        }
        return $$0;
    }

    @Override
    protected Typed<?> fix(Typed<?> $$0) {
        return $$0.update(DSL.remainderFinder(), this::fixTag);
    }
}

