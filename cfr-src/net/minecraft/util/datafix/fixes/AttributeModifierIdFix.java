/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public class AttributeModifierIdFix
extends DataFix {
    private static final Map<UUID, String> ID_MAP = ImmutableMap.builder().put((Object)UUID.fromString("736565d2-e1a7-403d-a3f8-1aeb3e302542"), (Object)"minecraft:creative_mode_block_range").put((Object)UUID.fromString("98491ef6-97b1-4584-ae82-71a8cc85cf73"), (Object)"minecraft:creative_mode_entity_range").put((Object)UUID.fromString("91AEAA56-376B-4498-935B-2F7F68070635"), (Object)"minecraft:effect.speed").put((Object)UUID.fromString("7107DE5E-7CE8-4030-940E-514C1F160890"), (Object)"minecraft:effect.slowness").put((Object)UUID.fromString("AF8B6E3F-3328-4C0A-AA36-5BA2BB9DBEF3"), (Object)"minecraft:effect.haste").put((Object)UUID.fromString("55FCED67-E92A-486E-9800-B47F202C4386"), (Object)"minecraft:effect.mining_fatigue").put((Object)UUID.fromString("648D7064-6A60-4F59-8ABE-C2C23A6DD7A9"), (Object)"minecraft:effect.strength").put((Object)UUID.fromString("C0105BF3-AEF8-46B0-9EBC-92943757CCBE"), (Object)"minecraft:effect.jump_boost").put((Object)UUID.fromString("22653B89-116E-49DC-9B6B-9971489B5BE5"), (Object)"minecraft:effect.weakness").put((Object)UUID.fromString("5D6F0BA2-1186-46AC-B896-C61C5CEE99CC"), (Object)"minecraft:effect.health_boost").put((Object)UUID.fromString("EAE29CF0-701E-4ED6-883A-96F798F3DAB5"), (Object)"minecraft:effect.absorption").put((Object)UUID.fromString("03C3C89D-7037-4B42-869F-B146BCB64D2E"), (Object)"minecraft:effect.luck").put((Object)UUID.fromString("CC5AF142-2BD2-4215-B636-2605AED11727"), (Object)"minecraft:effect.unluck").put((Object)UUID.fromString("6555be74-63b3-41f1-a245-77833b3c2562"), (Object)"minecraft:evil").put((Object)UUID.fromString("1eaf83ff-7207-4596-b37a-d7a07b3ec4ce"), (Object)"minecraft:powder_snow").put((Object)UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D"), (Object)"minecraft:sprinting").put((Object)UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0"), (Object)"minecraft:attacking").put((Object)UUID.fromString("766bfa64-11f3-11ea-8d71-362b9e155667"), (Object)"minecraft:baby").put((Object)UUID.fromString("7E0292F2-9434-48D5-A29F-9583AF7DF27F"), (Object)"minecraft:covered").put((Object)UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174"), (Object)"minecraft:suffocating").put((Object)UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E"), (Object)"minecraft:drinking").put((Object)UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836"), (Object)"minecraft:baby").put((Object)UUID.fromString("49455A49-7EC5-45BA-B886-3B90B23A1718"), (Object)"minecraft:attacking").put((Object)UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), (Object)"minecraft:armor.boots").put((Object)UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), (Object)"minecraft:armor.leggings").put((Object)UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), (Object)"minecraft:armor.chestplate").put((Object)UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150"), (Object)"minecraft:armor.helmet").put((Object)UUID.fromString("C1C72771-8B8E-BA4A-ACE0-81A93C8928B2"), (Object)"minecraft:armor.body").put((Object)UUID.fromString("b572ecd2-ac0c-4071-abde-9594af072a37"), (Object)"minecraft:enchantment.fire_protection").put((Object)UUID.fromString("40a9968f-5c66-4e2f-b7f4-2ec2f4b3e450"), (Object)"minecraft:enchantment.blast_protection").put((Object)UUID.fromString("07a65791-f64d-4e79-86c7-f83932f007ec"), (Object)"minecraft:enchantment.respiration").put((Object)UUID.fromString("60b1b7db-fffd-4ad0-817c-d6c6a93d8a45"), (Object)"minecraft:enchantment.aqua_affinity").put((Object)UUID.fromString("11dc269a-4476-46c0-aff3-9e17d7eb6801"), (Object)"minecraft:enchantment.depth_strider").put((Object)UUID.fromString("87f46a96-686f-4796-b035-22e16ee9e038"), (Object)"minecraft:enchantment.soul_speed").put((Object)UUID.fromString("b9716dbd-50df-4080-850e-70347d24e687"), (Object)"minecraft:enchantment.soul_speed").put((Object)UUID.fromString("92437d00-c3a7-4f2e-8f6c-1f21585d5dd0"), (Object)"minecraft:enchantment.swift_sneak").put((Object)UUID.fromString("5d3d087b-debe-4037-b53e-d84f3ff51f17"), (Object)"minecraft:enchantment.sweeping_edge").put((Object)UUID.fromString("3ceb37c0-db62-46b5-bd02-785457b01d96"), (Object)"minecraft:enchantment.efficiency").put((Object)UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"), (Object)"minecraft:base_attack_damage").put((Object)UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), (Object)"minecraft:base_attack_speed").build();
    private static final Map<String, String> NAME_MAP = Map.of("Random spawn bonus", "minecraft:random_spawn_bonus", "Random zombie-spawn bonus", "minecraft:zombie_random_spawn_bonus", "Leader zombie bonus", "minecraft:leader_zombie_bonus", "Zombie reinforcement callee charge", "minecraft:reinforcement_callee_charge", "Zombie reinforcement caller charge", "minecraft:reinforcement_caller_charge");

    public AttributeModifierIdFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        Type $$0 = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder $$12 = $$0.findField("components");
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("AttributeIdFix (ItemStack)", $$0, $$1 -> $$1.updateTyped($$12, $$0 -> $$0.update(DSL.remainderFinder(), AttributeModifierIdFix::fixItemStackComponents))), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped("AttributeIdFix (Entity)", this.getInputSchema().getType(References.ENTITY), AttributeModifierIdFix::fixEntity), this.fixTypeEverywhereTyped("AttributeIdFix (Player)", this.getInputSchema().getType(References.PLAYER), AttributeModifierIdFix::fixEntity)});
    }

    private static Stream<Dynamic<?>> fixModifiersTypeWrapper(Stream<?> $$0) {
        return AttributeModifierIdFix.fixModifiers($$0);
    }

    private static Stream<Dynamic<?>> fixModifiers(Stream<Dynamic<?>> $$0) {
        Object2ObjectArrayMap $$1 = new Object2ObjectArrayMap();
        $$0.forEach(arg_0 -> AttributeModifierIdFix.lambda$fixModifiers$2((Map)$$1, arg_0));
        return $$1.values().stream();
    }

    private static Dynamic<?> convertModifierForEntity(Dynamic<?> $$02) {
        return $$02.renameField("UUID", "uuid").renameField("Name", "name").renameField("Amount", "amount").renameAndFixField("Operation", "operation", $$0 -> $$0.createString(switch ($$0.asInt(0)) {
            case 0 -> "add_value";
            case 1 -> "add_multiplied_base";
            case 2 -> "add_multiplied_total";
            default -> "invalid";
        }));
    }

    private static Dynamic<?> fixItemStackComponents(Dynamic<?> $$0) {
        return $$0.update("minecraft:attribute_modifiers", $$02 -> $$02.update("modifiers", $$0 -> (Dynamic)DataFixUtils.orElse($$0.asStreamOpt().result().map(AttributeModifierIdFix::fixModifiersTypeWrapper).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)), (Object)$$0)));
    }

    private static Dynamic<?> fixAttribute(Dynamic<?> $$0) {
        return $$0.renameField("Name", "id").renameField("Base", "base").renameAndFixField("Modifiers", "modifiers", $$1 -> (Dynamic)DataFixUtils.orElse($$1.asStreamOpt().result().map($$0 -> $$0.map(AttributeModifierIdFix::convertModifierForEntity)).map(AttributeModifierIdFix::fixModifiersTypeWrapper).map(arg_0 -> ((Dynamic)$$0).createList(arg_0)), (Object)$$1));
    }

    private static Typed<?> fixEntity(Typed<?> $$02) {
        return $$02.update(DSL.remainderFinder(), $$0 -> $$0.renameAndFixField("Attributes", "attributes", $$02 -> (Dynamic)DataFixUtils.orElse($$02.asStreamOpt().result().map($$0 -> $$0.map(AttributeModifierIdFix::fixAttribute)).map(arg_0 -> ((Dynamic)$$02).createList(arg_0)), (Object)$$02)));
    }

    public static @Nullable UUID uuidFromIntArray(int[] $$0) {
        if ($$0.length != 4) {
            return null;
        }
        return new UUID((long)$$0[0] << 32 | (long)$$0[1] & 0xFFFFFFFFL, (long)$$0[2] << 32 | (long)$$0[3] & 0xFFFFFFFFL);
    }

    private static /* synthetic */ void lambda$fixModifiers$2(Map $$0, Dynamic $$1) {
        UUID $$2 = AttributeModifierIdFix.uuidFromIntArray($$1.get("uuid").asIntStream().toArray());
        String $$3 = $$1.get("name").asString("");
        String $$4 = $$2 != null ? ID_MAP.get($$2) : null;
        String $$5 = NAME_MAP.get($$3);
        if ($$4 != null) {
            $$1 = $$1.set("id", $$1.createString($$4));
            $$0.put($$4, $$1.remove("uuid").remove("name"));
        } else if ($$5 != null) {
            Dynamic $$6 = (Dynamic)$$0.get($$5);
            if ($$6 == null) {
                $$1 = $$1.set("id", $$1.createString($$5));
                $$0.put($$5, $$1.remove("uuid").remove("name"));
            } else {
                double $$7 = $$6.get("amount").asDouble(0.0);
                double $$8 = $$1.get("amount").asDouble(0.0);
                $$0.put($$5, $$6.set("amount", $$1.createDouble($$7 + $$8)));
            }
        } else {
            String $$9 = "minecraft:" + ($$2 != null ? $$2.toString().toLowerCase(Locale.ROOT) : "unknown");
            $$1 = $$1.set("id", $$1.createString($$9));
            $$0.put($$9, $$1.remove("uuid").remove("name"));
        }
    }
}

