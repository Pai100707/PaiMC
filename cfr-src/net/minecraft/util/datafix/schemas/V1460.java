/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V1451_6;
import net.minecraft.util.datafix.schemas.V1458;
import net.minecraft.util.datafix.schemas.V705;
import net.minecraft.util.datafix.schemas.V99;

public class V1460
extends NamespacedSchema {
    public V1460(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    protected static void registerMob(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.registerSimple($$1, $$2);
    }

    protected static void registerInventory(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.register($$1, $$2, () -> V1458.nameableInventory($$0));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        HashMap $$12 = Maps.newHashMap();
        $$0.register((Map)$$12, "minecraft:area_effect_cloud", $$1 -> DSL.optionalFields((String)"Particle", (TypeTemplate)References.PARTICLE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:armor_stand");
        $$0.register((Map)$$12, "minecraft:arrow", $$1 -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:bat");
        V1460.registerMob($$0, $$12, "minecraft:blaze");
        $$0.registerSimple((Map)$$12, "minecraft:boat");
        V1460.registerMob($$0, $$12, "minecraft:cave_spider");
        $$0.register((Map)$$12, "minecraft:chest_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        V1460.registerMob($$0, $$12, "minecraft:chicken");
        $$0.register((Map)$$12, "minecraft:commandblock_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:cow");
        V1460.registerMob($$0, $$12, "minecraft:creeper");
        $$0.register((Map)$$12, "minecraft:donkey", $$1 -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "minecraft:dragon_fireball");
        $$0.registerSimple((Map)$$12, "minecraft:egg");
        V1460.registerMob($$0, $$12, "minecraft:elder_guardian");
        $$0.registerSimple((Map)$$12, "minecraft:ender_crystal");
        V1460.registerMob($$0, $$12, "minecraft:ender_dragon");
        $$0.register((Map)$$12, "minecraft:enderman", $$1 -> DSL.optionalFields((String)"carriedBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:endermite");
        $$0.registerSimple((Map)$$12, "minecraft:ender_pearl");
        $$0.registerSimple((Map)$$12, "minecraft:evocation_fangs");
        V1460.registerMob($$0, $$12, "minecraft:evocation_illager");
        $$0.registerSimple((Map)$$12, "minecraft:eye_of_ender_signal");
        $$0.register((Map)$$12, "minecraft:falling_block", $$1 -> DSL.optionalFields((String)"BlockState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"TileEntityData", (TypeTemplate)References.BLOCK_ENTITY.in($$0)));
        $$0.registerSimple((Map)$$12, "minecraft:fireball");
        $$0.register((Map)$$12, "minecraft:fireworks_rocket", $$1 -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.register((Map)$$12, "minecraft:furnace_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:ghast");
        V1460.registerMob($$0, $$12, "minecraft:giant");
        V1460.registerMob($$0, $$12, "minecraft:guardian");
        $$0.register((Map)$$12, "minecraft:hopper_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.register((Map)$$12, "minecraft:horse", $$1 -> DSL.optionalFields((String)"ArmorItem", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:husk");
        V1460.registerMob($$0, $$12, "minecraft:illusion_illager");
        $$0.register((Map)$$12, "minecraft:item", $$1 -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.register((Map)$$12, "minecraft:item_frame", $$1 -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "minecraft:leash_knot");
        $$0.register((Map)$$12, "minecraft:llama", $$1 -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"DecorItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "minecraft:llama_spit");
        V1460.registerMob($$0, $$12, "minecraft:magma_cube");
        $$0.register((Map)$$12, "minecraft:minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:mooshroom");
        $$0.register((Map)$$12, "minecraft:mule", $$1 -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:ocelot");
        $$0.registerSimple((Map)$$12, "minecraft:painting");
        V1460.registerMob($$0, $$12, "minecraft:parrot");
        V1460.registerMob($$0, $$12, "minecraft:pig");
        V1460.registerMob($$0, $$12, "minecraft:polar_bear");
        $$0.register((Map)$$12, "minecraft:potion", $$1 -> DSL.optionalFields((String)"Potion", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:rabbit");
        V1460.registerMob($$0, $$12, "minecraft:sheep");
        V1460.registerMob($$0, $$12, "minecraft:shulker");
        $$0.registerSimple((Map)$$12, "minecraft:shulker_bullet");
        V1460.registerMob($$0, $$12, "minecraft:silverfish");
        V1460.registerMob($$0, $$12, "minecraft:skeleton");
        $$0.register((Map)$$12, "minecraft:skeleton_horse", $$1 -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:slime");
        $$0.registerSimple((Map)$$12, "minecraft:small_fireball");
        $$0.registerSimple((Map)$$12, "minecraft:snowball");
        V1460.registerMob($$0, $$12, "minecraft:snowman");
        $$0.register((Map)$$12, "minecraft:spawner_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0), (TypeTemplate)References.UNTAGGED_SPAWNER.in($$0)));
        $$0.register((Map)$$12, "minecraft:spectral_arrow", $$1 -> DSL.optionalFields((String)"inBlockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:spider");
        V1460.registerMob($$0, $$12, "minecraft:squid");
        V1460.registerMob($$0, $$12, "minecraft:stray");
        $$0.registerSimple((Map)$$12, "minecraft:tnt");
        $$0.register((Map)$$12, "minecraft:tnt_minecart", $$1 -> DSL.optionalFields((String)"DisplayState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:vex");
        $$0.register((Map)$$12, "minecraft:villager", $$1 -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in($$0)))));
        V1460.registerMob($$0, $$12, "minecraft:villager_golem");
        V1460.registerMob($$0, $$12, "minecraft:vindication_illager");
        V1460.registerMob($$0, $$12, "minecraft:witch");
        V1460.registerMob($$0, $$12, "minecraft:wither");
        V1460.registerMob($$0, $$12, "minecraft:wither_skeleton");
        $$0.registerSimple((Map)$$12, "minecraft:wither_skull");
        V1460.registerMob($$0, $$12, "minecraft:wolf");
        $$0.registerSimple((Map)$$12, "minecraft:xp_bottle");
        $$0.registerSimple((Map)$$12, "minecraft:xp_orb");
        V1460.registerMob($$0, $$12, "minecraft:zombie");
        $$0.register((Map)$$12, "minecraft:zombie_horse", $$1 -> DSL.optionalFields((String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerMob($$0, $$12, "minecraft:zombie_pigman");
        $$0.register((Map)$$12, "minecraft:zombie_villager", $$1 -> DSL.optionalFields((String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in($$0)))));
        return $$12;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        HashMap $$12 = Maps.newHashMap();
        V1460.registerInventory($$0, $$12, "minecraft:furnace");
        V1460.registerInventory($$0, $$12, "minecraft:chest");
        V1460.registerInventory($$0, $$12, "minecraft:trapped_chest");
        $$0.registerSimple((Map)$$12, "minecraft:ender_chest");
        $$0.register((Map)$$12, "minecraft:jukebox", $$1 -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V1460.registerInventory($$0, $$12, "minecraft:dispenser");
        V1460.registerInventory($$0, $$12, "minecraft:dropper");
        $$0.register((Map)$$12, "minecraft:sign", () -> V99.sign($$0));
        $$0.register((Map)$$12, "minecraft:mob_spawner", $$1 -> References.UNTAGGED_SPAWNER.in($$0));
        $$0.register((Map)$$12, "minecraft:piston", $$1 -> DSL.optionalFields((String)"blockState", (TypeTemplate)References.BLOCK_STATE.in($$0)));
        V1460.registerInventory($$0, $$12, "minecraft:brewing_stand");
        $$0.register((Map)$$12, "minecraft:enchanting_table", () -> V1458.nameable($$0));
        $$0.registerSimple((Map)$$12, "minecraft:end_portal");
        $$0.register((Map)$$12, "minecraft:beacon", () -> V1458.nameable($$0));
        $$0.register((Map)$$12, "minecraft:skull", () -> DSL.optionalFields((String)"custom_name", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerSimple((Map)$$12, "minecraft:daylight_detector");
        V1460.registerInventory($$0, $$12, "minecraft:hopper");
        $$0.registerSimple((Map)$$12, "minecraft:comparator");
        $$0.register((Map)$$12, "minecraft:banner", () -> V1458.nameable($$0));
        $$0.registerSimple((Map)$$12, "minecraft:structure_block");
        $$0.registerSimple((Map)$$12, "minecraft:end_gateway");
        $$0.register((Map)$$12, "minecraft:command_block", () -> DSL.optionalFields((String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        V1460.registerInventory($$0, $$12, "minecraft:shulker_box");
        $$0.registerSimple((Map)$$12, "minecraft:bed");
        return $$12;
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        $$0.registerType(false, References.LEVEL, () -> DSL.optionalFields((String)"CustomBossEvents", (TypeTemplate)DSL.compoundList((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in($$0))), (TypeTemplate)References.LIGHTWEIGHT_LEVEL.in($$0)));
        $$0.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
        $$0.registerType(false, References.RECIPE, () -> DSL.constType(V1460.namespacedString()));
        $$0.registerType(false, References.PLAYER, () -> DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"RootVehicle", (Object)DSL.optionalFields((String)"Entity", (TypeTemplate)References.ENTITY_TREE.in($$0))), Pair.of((Object)"ender_pearls", (Object)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0))), Pair.of((Object)"Inventory", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"EnderItems", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"ShoulderEntityLeft", (Object)References.ENTITY_TREE.in($$0)), Pair.of((Object)"ShoulderEntityRight", (Object)References.ENTITY_TREE.in($$0)), Pair.of((Object)"recipeBook", (Object)DSL.optionalFields((String)"recipes", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in($$0)), (String)"toBeDisplayed", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in($$0))))}));
        $$0.registerType(false, References.CHUNK, () -> DSL.fields((String)"Level", (TypeTemplate)DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0)), (String)"TileEntities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_ENTITY.in($$0), (TypeTemplate)DSL.remainder())), (String)"TileTicks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)References.BLOCK_NAME.in($$0))), (String)"Sections", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"Palette", (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_STATE.in($$0)))))));
        $$0.registerType(true, References.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)References.DATA_COMPONENTS.in($$0), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", V1460.namespacedString(), (Map)$$2)));
        $$0.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields((String)"Passengers", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0)), (TypeTemplate)References.ENTITY.in($$0)));
        $$0.registerType(true, References.ENTITY, () -> DSL.and((TypeTemplate)References.ENTITY_EQUIPMENT.in($$0), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", V1460.namespacedString(), (Map)$$1))));
        $$0.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)References.ITEM_NAME.in($$0), (String)"tag", (TypeTemplate)V99.itemStackTag($$0)), (Hook.HookFunction)V705.ADD_NAMES, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
        $$0.registerType(false, References.HOTBAR, () -> DSL.compoundList((TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.registerType(false, References.OPTIONS, DSL::remainder);
        $$0.registerType(false, References.STRUCTURE, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.ENTITY_TREE.in($$0))), (String)"blocks", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.BLOCK_ENTITY.in($$0))), (String)"palette", (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_STATE.in($$0))));
        $$0.registerType(false, References.BLOCK_NAME, () -> DSL.constType(V1460.namespacedString()));
        $$0.registerType(false, References.ITEM_NAME, () -> DSL.constType(V1460.namespacedString()));
        $$0.registerType(false, References.BLOCK_STATE, DSL::remainder);
        $$0.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
        Supplier<TypeTemplate> $$3 = () -> DSL.compoundList((TypeTemplate)References.ITEM_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()));
        $$0.registerType(false, References.STATS, () -> DSL.optionalFields((String)"stats", (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"minecraft:mined", (Object)DSL.compoundList((TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:crafted", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:used", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:broken", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:picked_up", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:dropped", (Object)((TypeTemplate)$$3.get())), Pair.of((Object)"minecraft:killed", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:killed_by", (Object)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.intType()))), Pair.of((Object)"minecraft:custom", (Object)DSL.compoundList((TypeTemplate)DSL.constType(V1460.namespacedString()), (TypeTemplate)DSL.constType((Type)DSL.intType())))})));
        $$0.registerType(false, References.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_TICKETS, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_MAP_DATA, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"banners", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in($$0))))));
        $$0.registerType(false, References.SAVED_DATA_MAP_INDEX, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_RAIDS, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_SCOREBOARD, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Objectives", (TypeTemplate)DSL.list((TypeTemplate)References.OBJECTIVE.in($$0)), (String)"Teams", (TypeTemplate)DSL.list((TypeTemplate)References.TEAM.in($$0)), (String)"PlayerScores", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"display", (TypeTemplate)References.TEXT_COMPONENT.in($$0))))));
        $$0.registerType(false, References.SAVED_DATA_STOPWATCHES, DSL::remainder);
        $$0.registerType(false, References.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> DSL.optionalFields((String)"data", (TypeTemplate)DSL.optionalFields((String)"Features", (TypeTemplate)DSL.compoundList((TypeTemplate)References.STRUCTURE_FEATURE.in($$0)))));
        $$0.registerType(false, References.SAVED_DATA_WORLD_BORDER, DSL::remainder);
        $$0.registerType(false, References.DEBUG_PROFILE, DSL::remainder);
        $$0.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        Map<String, Supplier<TypeTemplate>> $$4 = V1451_6.createCriterionTypes($$0);
        $$0.registerType(false, References.OBJECTIVE, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"CriteriaType", (TypeTemplate)DSL.taggedChoiceLazy((String)"type", (Type)DSL.string(), (Map)$$4), (String)"DisplayName", (TypeTemplate)References.TEXT_COMPONENT.in($$0)), (Hook.HookFunction)V1451_6.UNPACK_OBJECTIVE_ID, (Hook.HookFunction)V1451_6.REPACK_OBJECTIVE_ID));
        $$0.registerType(false, References.TEAM, () -> DSL.optionalFields((String)"MemberNamePrefix", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"MemberNameSuffix", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"DisplayName", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerType(true, References.UNTAGGED_SPAWNER, () -> DSL.optionalFields((String)"SpawnPotentials", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"Entity", (TypeTemplate)References.ENTITY_TREE.in($$0))), (String)"SpawnData", (TypeTemplate)References.ENTITY_TREE.in($$0)));
        $$0.registerType(false, References.ADVANCEMENTS, () -> DSL.optionalFields((String)"minecraft:adventure/adventuring_time", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)References.BIOME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:adventure/kill_a_mob", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:adventure/kill_all_mobs", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.string()))), (String)"minecraft:husbandry/bred_all_animals", (TypeTemplate)DSL.optionalFields((String)"criteria", (TypeTemplate)DSL.compoundList((TypeTemplate)References.ENTITY_NAME.in($$0), (TypeTemplate)DSL.constType((Type)DSL.string())))));
        $$0.registerType(false, References.BIOME, () -> DSL.constType(V1460.namespacedString()));
        $$0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(V1460.namespacedString()));
        $$0.registerType(false, References.POI_CHUNK, DSL::remainder);
        $$0.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
        $$0.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0))));
        $$0.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
        $$0.registerType(true, References.VILLAGER_TRADE, () -> DSL.optionalFields((String)"buy", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"buyB", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"sell", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerType(true, References.PARTICLE, () -> DSL.constType((Type)DSL.string()));
        $$0.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType((Type)DSL.string()));
        $$0.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.and((TypeTemplate)DSL.optional((TypeTemplate)DSL.field((String)"ArmorItems", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)))), (TypeTemplate[])new TypeTemplate[]{DSL.optional((TypeTemplate)DSL.field((String)"HandItems", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)))), DSL.optional((TypeTemplate)DSL.field((String)"body_armor_item", (TypeTemplate)References.ITEM_STACK.in($$0))), DSL.optional((TypeTemplate)DSL.field((String)"saddle", (TypeTemplate)References.ITEM_STACK.in($$0)))}));
    }
}

