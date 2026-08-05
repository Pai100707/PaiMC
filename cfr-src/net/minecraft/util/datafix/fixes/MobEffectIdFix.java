/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MobEffectIdFix
extends DataFix {
    private static final Int2ObjectMap<String> ID_MAP = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(), $$0 -> {
        $$0.put(1, (Object)"minecraft:speed");
        $$0.put(2, (Object)"minecraft:slowness");
        $$0.put(3, (Object)"minecraft:haste");
        $$0.put(4, (Object)"minecraft:mining_fatigue");
        $$0.put(5, (Object)"minecraft:strength");
        $$0.put(6, (Object)"minecraft:instant_health");
        $$0.put(7, (Object)"minecraft:instant_damage");
        $$0.put(8, (Object)"minecraft:jump_boost");
        $$0.put(9, (Object)"minecraft:nausea");
        $$0.put(10, (Object)"minecraft:regeneration");
        $$0.put(11, (Object)"minecraft:resistance");
        $$0.put(12, (Object)"minecraft:fire_resistance");
        $$0.put(13, (Object)"minecraft:water_breathing");
        $$0.put(14, (Object)"minecraft:invisibility");
        $$0.put(15, (Object)"minecraft:blindness");
        $$0.put(16, (Object)"minecraft:night_vision");
        $$0.put(17, (Object)"minecraft:hunger");
        $$0.put(18, (Object)"minecraft:weakness");
        $$0.put(19, (Object)"minecraft:poison");
        $$0.put(20, (Object)"minecraft:wither");
        $$0.put(21, (Object)"minecraft:health_boost");
        $$0.put(22, (Object)"minecraft:absorption");
        $$0.put(23, (Object)"minecraft:saturation");
        $$0.put(24, (Object)"minecraft:glowing");
        $$0.put(25, (Object)"minecraft:levitation");
        $$0.put(26, (Object)"minecraft:luck");
        $$0.put(27, (Object)"minecraft:unluck");
        $$0.put(28, (Object)"minecraft:slow_falling");
        $$0.put(29, (Object)"minecraft:conduit_power");
        $$0.put(30, (Object)"minecraft:dolphins_grace");
        $$0.put(31, (Object)"minecraft:bad_omen");
        $$0.put(32, (Object)"minecraft:hero_of_the_village");
        $$0.put(33, (Object)"minecraft:darkness");
    });
    private static final Set<String> MOB_EFFECT_INSTANCE_CARRIER_ITEMS = Set.of("minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow");

    public MobEffectIdFix(Schema $$0) {
        super($$0, false);
    }

    private static <T> Optional<Dynamic<T>> getAndConvertMobEffectId(Dynamic<T> $$02, String $$1) {
        return $$02.get($$1).asNumber().result().map($$0 -> (String)ID_MAP.get($$0.intValue())).map(arg_0 -> $$02.createString(arg_0));
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> $$0, String $$1, Dynamic<T> $$2, String $$3) {
        Optional<Dynamic<T>> $$4 = MobEffectIdFix.getAndConvertMobEffectId($$0, $$1);
        return $$2.replaceField($$1, $$3, $$4);
    }

    private static <T> Dynamic<T> updateMobEffectIdField(Dynamic<T> $$0, String $$1, String $$2) {
        return MobEffectIdFix.updateMobEffectIdField($$0, $$1, $$0, $$2);
    }

    private static <T> Dynamic<T> updateMobEffectInstance(Dynamic<T> $$0) {
        $$0 = MobEffectIdFix.updateMobEffectIdField($$0, "Id", "id");
        $$0 = $$0.renameField("Ambient", "ambient");
        $$0 = $$0.renameField("Amplifier", "amplifier");
        $$0 = $$0.renameField("Duration", "duration");
        $$0 = $$0.renameField("ShowParticles", "show_particles");
        $$0 = $$0.renameField("ShowIcon", "show_icon");
        Optional<Dynamic> $$1 = $$0.get("HiddenEffect").result().map(MobEffectIdFix::updateMobEffectInstance);
        return $$0.replaceField("HiddenEffect", "hidden_effect", $$1);
    }

    private static <T> Dynamic<T> updateMobEffectInstanceList(Dynamic<T> $$0, String $$12, String $$2) {
        Optional<Dynamic> $$3 = $$0.get($$12).asStreamOpt().result().map($$1 -> $$0.createList($$1.map(MobEffectIdFix::updateMobEffectInstance)));
        return $$0.replaceField($$12, $$2, $$3);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> $$0, Dynamic<T> $$1) {
        $$1 = MobEffectIdFix.updateMobEffectIdField($$0, "EffectId", $$1, "id");
        Optional $$2 = $$0.get("EffectDuration").result();
        return $$1.replaceField("EffectDuration", "duration", $$2);
    }

    private static <T> Dynamic<T> updateSuspiciousStewEntry(Dynamic<T> $$0) {
        return MobEffectIdFix.updateSuspiciousStewEntry($$0, $$0);
    }

    private Typed<?> updateNamedChoice(Typed<?> $$0, DSL.TypeReference $$12, String $$2, Function<Dynamic<?>, Dynamic<?>> $$3) {
        Type $$4 = this.getInputSchema().getChoiceType($$12, $$2);
        Type $$5 = this.getOutputSchema().getChoiceType($$12, $$2);
        return $$0.updateTyped(DSL.namedChoice((String)$$2, (Type)$$4), $$5, $$1 -> $$1.update(DSL.remainderFinder(), $$3));
    }

    private TypeRewriteRule blockEntityFixer() {
        Type $$0 = this.getInputSchema().getType(References.BLOCK_ENTITY);
        return this.fixTypeEverywhereTyped("BlockEntityMobEffectIdFix", $$0, $$02 -> {
            $$02 = this.updateNamedChoice((Typed<?>)$$02, References.BLOCK_ENTITY, "minecraft:beacon", $$0 -> {
                $$0 = MobEffectIdFix.updateMobEffectIdField($$0, "Primary", "primary_effect");
                return MobEffectIdFix.updateMobEffectIdField($$0, "Secondary", "secondary_effect");
            });
            return $$02;
        });
    }

    private static <T> Dynamic<T> fixMooshroomTag(Dynamic<T> $$0) {
        Dynamic $$1 = $$0.emptyMap();
        Dynamic<T> $$2 = MobEffectIdFix.updateSuspiciousStewEntry($$0, $$1);
        if (!$$2.equals((Object)$$1)) {
            $$0 = $$0.set("stew_effects", $$0.createList(Stream.of($$2)));
        }
        return $$0.remove("EffectId").remove("EffectDuration");
    }

    private static <T> Dynamic<T> fixArrowTag(Dynamic<T> $$0) {
        return MobEffectIdFix.updateMobEffectInstanceList($$0, "CustomPotionEffects", "custom_potion_effects");
    }

    private static <T> Dynamic<T> fixAreaEffectCloudTag(Dynamic<T> $$0) {
        return MobEffectIdFix.updateMobEffectInstanceList($$0, "Effects", "effects");
    }

    private static Dynamic<?> updateLivingEntityTag(Dynamic<?> $$0) {
        return MobEffectIdFix.updateMobEffectInstanceList($$0, "ActiveEffects", "active_effects");
    }

    private TypeRewriteRule entityFixer() {
        Type $$02 = this.getInputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("EntityMobEffectIdFix", $$02, $$0 -> {
            $$0 = this.updateNamedChoice((Typed<?>)$$0, References.ENTITY, "minecraft:mooshroom", MobEffectIdFix::fixMooshroomTag);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, References.ENTITY, "minecraft:arrow", MobEffectIdFix::fixArrowTag);
            $$0 = this.updateNamedChoice((Typed<?>)$$0, References.ENTITY, "minecraft:area_effect_cloud", MobEffectIdFix::fixAreaEffectCloudTag);
            $$0 = $$0.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag);
            return $$0;
        });
    }

    private TypeRewriteRule playerFixer() {
        Type $$02 = this.getInputSchema().getType(References.PLAYER);
        return this.fixTypeEverywhereTyped("PlayerMobEffectIdFix", $$02, $$0 -> $$0.update(DSL.remainderFinder(), MobEffectIdFix::updateLivingEntityTag));
    }

    private static <T> Dynamic<T> fixSuspiciousStewTag(Dynamic<T> $$0) {
        Optional<Dynamic> $$12 = $$0.get("Effects").asStreamOpt().result().map($$1 -> $$0.createList($$1.map(MobEffectIdFix::updateSuspiciousStewEntry)));
        return $$0.replaceField("Effects", "effects", $$12);
    }

    private TypeRewriteRule itemStackFixer() {
        OpticFinder $$0 = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        Type $$1 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$22 = $$1.findField("tag");
        return this.fixTypeEverywhereTyped("ItemStackMobEffectIdFix", $$1, $$2 -> {
            Optional $$3 = $$2.getOptional($$0);
            if ($$3.isPresent()) {
                String $$4 = (String)((Pair)$$3.get()).getSecond();
                if ($$4.equals("minecraft:suspicious_stew")) {
                    return $$2.updateTyped($$22, $$0 -> $$0.update(DSL.remainderFinder(), MobEffectIdFix::fixSuspiciousStewTag));
                }
                if (MOB_EFFECT_INSTANCE_CARRIER_ITEMS.contains($$4)) {
                    return $$2.updateTyped($$22, $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> MobEffectIdFix.updateMobEffectInstanceList($$0, "CustomPotionEffects", "custom_potion_effects")));
                }
            }
            return $$2;
        });
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.blockEntityFixer(), (TypeRewriteRule[])new TypeRewriteRule[]{this.entityFixer(), this.playerFixer(), this.itemStackFixer()});
    }
}

