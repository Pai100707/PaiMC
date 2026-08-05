/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Sets;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class EntityUUIDFix
extends AbstractUUIDFix {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<String> ABSTRACT_HORSES = Sets.newHashSet();
    private static final Set<String> TAMEABLE_ANIMALS = Sets.newHashSet();
    private static final Set<String> ANIMALS = Sets.newHashSet();
    private static final Set<String> MOBS = Sets.newHashSet();
    private static final Set<String> LIVING_ENTITIES = Sets.newHashSet();
    private static final Set<String> PROJECTILES = Sets.newHashSet();

    public EntityUUIDFix(Schema $$0) {
        super($$0, References.ENTITY);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("EntityUUIDFixes", this.getInputSchema().getType(this.typeReference), $$0 -> {
            $$0 = $$0.update(DSL.remainderFinder(), EntityUUIDFix::updateEntityUUID);
            for (String $$1 : ABSTRACT_HORSES) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$1, EntityUUIDFix::updateAnimalOwner);
            }
            for (String $$2 : TAMEABLE_ANIMALS) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$2, EntityUUIDFix::updateAnimalOwner);
            }
            for (String $$3 : ANIMALS) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$3, EntityUUIDFix::updateAnimal);
            }
            for (String $$4 : MOBS) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$4, EntityUUIDFix::updateMob);
            }
            for (String $$5 : LIVING_ENTITIES) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$5, EntityUUIDFix::updateLivingEntity);
            }
            for (String $$6 : PROJECTILES) {
                $$0 = this.updateNamedChoice((Typed<?>)$$0, $$6, EntityUUIDFix::updateProjectile);
            }
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:bee", EntityUUIDFix::updateHurtBy);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:zombified_piglin", EntityUUIDFix::updateHurtBy);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:fox", EntityUUIDFix::updateFox);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:item", EntityUUIDFix::updateItem);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:shulker_bullet", EntityUUIDFix::updateShulkerBullet);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:area_effect_cloud", EntityUUIDFix::updateAreaEffectCloud);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:zombie_villager", EntityUUIDFix::updateZombieVillager);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:evoker_fangs", EntityUUIDFix::updateEvokerFangs);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, "minecraft:piglin", EntityUUIDFix::updatePiglin);
            return $$0;
        });
    }

    private static Dynamic<?> updatePiglin(Dynamic<?> $$02) {
        return $$02.update("Brain", $$0 -> $$0.update("memories", $$02 -> $$02.update("minecraft:angry_at", $$0 -> EntityUUIDFix.replaceUUIDString($$0, "value", "value").orElseGet(() -> {
            LOGGER.warn("angry_at has no value.");
            return $$0;
        }))));
    }

    private static Dynamic<?> updateEvokerFangs(Dynamic<?> $$0) {
        return EntityUUIDFix.replaceUUIDLeastMost($$0, "OwnerUUID", "Owner").orElse($$0);
    }

    private static Dynamic<?> updateZombieVillager(Dynamic<?> $$0) {
        return EntityUUIDFix.replaceUUIDLeastMost($$0, "ConversionPlayer", "ConversionPlayer").orElse($$0);
    }

    private static Dynamic<?> updateAreaEffectCloud(Dynamic<?> $$0) {
        return EntityUUIDFix.replaceUUIDLeastMost($$0, "OwnerUUID", "Owner").orElse($$0);
    }

    private static Dynamic<?> updateShulkerBullet(Dynamic<?> $$0) {
        $$0 = EntityUUIDFix.replaceUUIDMLTag($$0, "Owner", "Owner").orElse($$0);
        return EntityUUIDFix.replaceUUIDMLTag($$0, "Target", "Target").orElse($$0);
    }

    private static Dynamic<?> updateItem(Dynamic<?> $$0) {
        $$0 = EntityUUIDFix.replaceUUIDMLTag($$0, "Owner", "Owner").orElse($$0);
        return EntityUUIDFix.replaceUUIDMLTag($$0, "Thrower", "Thrower").orElse($$0);
    }

    private static Dynamic<?> updateFox(Dynamic<?> $$0) {
        Optional<Dynamic> $$12 = $$0.get("TrustedUUIDs").result().map($$1 -> $$0.createList($$1.asStream().map($$0 -> EntityUUIDFix.createUUIDFromML($$0).orElseGet(() -> {
            LOGGER.warn("Trusted contained invalid data.");
            return $$0;
        }))));
        return (Dynamic)DataFixUtils.orElse($$12.map($$1 -> $$0.remove("TrustedUUIDs").set("Trusted", $$1)), $$0);
    }

    private static Dynamic<?> updateHurtBy(Dynamic<?> $$0) {
        return EntityUUIDFix.replaceUUIDString($$0, "HurtBy", "HurtBy").orElse($$0);
    }

    private static Dynamic<?> updateAnimalOwner(Dynamic<?> $$0) {
        Dynamic<?> $$1 = EntityUUIDFix.updateAnimal($$0);
        return EntityUUIDFix.replaceUUIDString($$1, "OwnerUUID", "Owner").orElse($$1);
    }

    private static Dynamic<?> updateAnimal(Dynamic<?> $$0) {
        Dynamic<?> $$1 = EntityUUIDFix.updateMob($$0);
        return EntityUUIDFix.replaceUUIDLeastMost($$1, "LoveCause", "LoveCause").orElse($$1);
    }

    private static Dynamic<?> updateMob(Dynamic<?> $$02) {
        return EntityUUIDFix.updateLivingEntity($$02).update("Leash", $$0 -> EntityUUIDFix.replaceUUIDLeastMost($$0, "UUID", "UUID").orElse((Dynamic<?>)$$0));
    }

    public static Dynamic<?> updateLivingEntity(Dynamic<?> $$0) {
        return $$0.update("Attributes", $$1 -> $$0.createList($$1.asStream().map($$0 -> $$0.update("Modifiers", $$1 -> $$0.createList($$1.asStream().map($$0 -> EntityUUIDFix.replaceUUIDLeastMost($$0, "UUID", "UUID").orElse((Dynamic<?>)$$0)))))));
    }

    private static Dynamic<?> updateProjectile(Dynamic<?> $$0) {
        return (Dynamic)DataFixUtils.orElse($$0.get("OwnerUUID").result().map($$1 -> $$0.remove("OwnerUUID").set("Owner", $$1)), $$0);
    }

    public static Dynamic<?> updateEntityUUID(Dynamic<?> $$0) {
        return EntityUUIDFix.replaceUUIDLeastMost($$0, "UUID", "UUID").orElse($$0);
    }

    static {
        ABSTRACT_HORSES.add("minecraft:donkey");
        ABSTRACT_HORSES.add("minecraft:horse");
        ABSTRACT_HORSES.add("minecraft:llama");
        ABSTRACT_HORSES.add("minecraft:mule");
        ABSTRACT_HORSES.add("minecraft:skeleton_horse");
        ABSTRACT_HORSES.add("minecraft:trader_llama");
        ABSTRACT_HORSES.add("minecraft:zombie_horse");
        TAMEABLE_ANIMALS.add("minecraft:cat");
        TAMEABLE_ANIMALS.add("minecraft:parrot");
        TAMEABLE_ANIMALS.add("minecraft:wolf");
        ANIMALS.add("minecraft:bee");
        ANIMALS.add("minecraft:chicken");
        ANIMALS.add("minecraft:cow");
        ANIMALS.add("minecraft:fox");
        ANIMALS.add("minecraft:mooshroom");
        ANIMALS.add("minecraft:ocelot");
        ANIMALS.add("minecraft:panda");
        ANIMALS.add("minecraft:pig");
        ANIMALS.add("minecraft:polar_bear");
        ANIMALS.add("minecraft:rabbit");
        ANIMALS.add("minecraft:sheep");
        ANIMALS.add("minecraft:turtle");
        ANIMALS.add("minecraft:hoglin");
        MOBS.add("minecraft:bat");
        MOBS.add("minecraft:blaze");
        MOBS.add("minecraft:cave_spider");
        MOBS.add("minecraft:cod");
        MOBS.add("minecraft:creeper");
        MOBS.add("minecraft:dolphin");
        MOBS.add("minecraft:drowned");
        MOBS.add("minecraft:elder_guardian");
        MOBS.add("minecraft:ender_dragon");
        MOBS.add("minecraft:enderman");
        MOBS.add("minecraft:endermite");
        MOBS.add("minecraft:evoker");
        MOBS.add("minecraft:ghast");
        MOBS.add("minecraft:giant");
        MOBS.add("minecraft:guardian");
        MOBS.add("minecraft:husk");
        MOBS.add("minecraft:illusioner");
        MOBS.add("minecraft:magma_cube");
        MOBS.add("minecraft:pufferfish");
        MOBS.add("minecraft:zombified_piglin");
        MOBS.add("minecraft:salmon");
        MOBS.add("minecraft:shulker");
        MOBS.add("minecraft:silverfish");
        MOBS.add("minecraft:skeleton");
        MOBS.add("minecraft:slime");
        MOBS.add("minecraft:snow_golem");
        MOBS.add("minecraft:spider");
        MOBS.add("minecraft:squid");
        MOBS.add("minecraft:stray");
        MOBS.add("minecraft:tropical_fish");
        MOBS.add("minecraft:vex");
        MOBS.add("minecraft:villager");
        MOBS.add("minecraft:iron_golem");
        MOBS.add("minecraft:vindicator");
        MOBS.add("minecraft:pillager");
        MOBS.add("minecraft:wandering_trader");
        MOBS.add("minecraft:witch");
        MOBS.add("minecraft:wither");
        MOBS.add("minecraft:wither_skeleton");
        MOBS.add("minecraft:zombie");
        MOBS.add("minecraft:zombie_villager");
        MOBS.add("minecraft:phantom");
        MOBS.add("minecraft:ravager");
        MOBS.add("minecraft:piglin");
        LIVING_ENTITIES.add("minecraft:armor_stand");
        PROJECTILES.add("minecraft:arrow");
        PROJECTILES.add("minecraft:dragon_fireball");
        PROJECTILES.add("minecraft:firework_rocket");
        PROJECTILES.add("minecraft:fireball");
        PROJECTILES.add("minecraft:llama_spit");
        PROJECTILES.add("minecraft:small_fireball");
        PROJECTILES.add("minecraft:snowball");
        PROJECTILES.add("minecraft:spectral_arrow");
        PROJECTILES.add("minecraft:egg");
        PROJECTILES.add("minecraft:ender_pearl");
        PROJECTILES.add("minecraft:experience_bottle");
        PROJECTILES.add("minecraft:potion");
        PROJECTILES.add("minecraft:trident");
        PROJECTILES.add("minecraft:wither_skull");
    }
}

