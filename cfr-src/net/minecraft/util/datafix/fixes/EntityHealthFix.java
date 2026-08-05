/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.References;

public class EntityHealthFix
extends DataFix {
    private static final Set<String> ENTITIES = Sets.newHashSet((Object[])new String[]{"ArmorStand", "Bat", "Blaze", "CaveSpider", "Chicken", "Cow", "Creeper", "EnderDragon", "Enderman", "Endermite", "EntityHorse", "Ghast", "Giant", "Guardian", "LavaSlime", "MushroomCow", "Ozelot", "Pig", "PigZombie", "Rabbit", "Sheep", "Shulker", "Silverfish", "Skeleton", "Slime", "SnowMan", "Spider", "Squid", "Villager", "VillagerGolem", "Witch", "WitherBoss", "Wolf", "Zombie"});

    public EntityHealthFix(Schema $$0, boolean $$1) {
        super($$0, $$1);
    }

    /*
     * WARNING - void declaration
     */
    public Dynamic<?> fixTag(Dynamic<?> $$0) {
        void $$5;
        Optional $$1 = $$0.get("HealF").asNumber().result();
        Optional $$2 = $$0.get("Health").asNumber().result();
        if ($$1.isPresent()) {
            float $$3 = ((Number)$$1.get()).floatValue();
            $$0 = $$0.remove("HealF");
        } else if ($$2.isPresent()) {
            float $$4 = ((Number)$$2.get()).floatValue();
        } else {
            return $$0;
        }
        return $$0.set("Health", $$0.createFloat((float)$$5));
    }

    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityHealthFix", this.getInputSchema().getType(References.ENTITY), $$0 -> $$0.update(DSL.remainderFinder(), this::fixTag));
    }
}

