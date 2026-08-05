/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.Hook$HookFunction
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class V99
extends Schema {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Map<String, String> ITEM_TO_BLOCKENTITY = (Map)DataFixUtils.make((Object)Maps.newHashMap(), $$0 -> {
        $$0.put("minecraft:furnace", "Furnace");
        $$0.put("minecraft:lit_furnace", "Furnace");
        $$0.put("minecraft:chest", "Chest");
        $$0.put("minecraft:trapped_chest", "Chest");
        $$0.put("minecraft:ender_chest", "EnderChest");
        $$0.put("minecraft:jukebox", "RecordPlayer");
        $$0.put("minecraft:dispenser", "Trap");
        $$0.put("minecraft:dropper", "Dropper");
        $$0.put("minecraft:sign", "Sign");
        $$0.put("minecraft:mob_spawner", "MobSpawner");
        $$0.put("minecraft:noteblock", "Music");
        $$0.put("minecraft:brewing_stand", "Cauldron");
        $$0.put("minecraft:enhanting_table", "EnchantTable");
        $$0.put("minecraft:command_block", "CommandBlock");
        $$0.put("minecraft:beacon", "Beacon");
        $$0.put("minecraft:skull", "Skull");
        $$0.put("minecraft:daylight_detector", "DLDetector");
        $$0.put("minecraft:hopper", "Hopper");
        $$0.put("minecraft:banner", "Banner");
        $$0.put("minecraft:flower_pot", "FlowerPot");
        $$0.put("minecraft:repeating_command_block", "CommandBlock");
        $$0.put("minecraft:chain_command_block", "CommandBlock");
        $$0.put("minecraft:standing_sign", "Sign");
        $$0.put("minecraft:wall_sign", "Sign");
        $$0.put("minecraft:piston_head", "Piston");
        $$0.put("minecraft:daylight_detector_inverted", "DLDetector");
        $$0.put("minecraft:unpowered_comparator", "Comparator");
        $$0.put("minecraft:powered_comparator", "Comparator");
        $$0.put("minecraft:wall_banner", "Banner");
        $$0.put("minecraft:standing_banner", "Banner");
        $$0.put("minecraft:structure_block", "Structure");
        $$0.put("minecraft:end_portal", "Airportal");
        $$0.put("minecraft:end_gateway", "EndGateway");
        $$0.put("minecraft:shield", "Banner");
    });
    public static final Map<String, String> ITEM_TO_ENTITY = Map.of("minecraft:armor_stand", "ArmorStand", "minecraft:painting", "Painting");
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        public <T> T apply(DynamicOps<T> $$0, T $$1) {
            return V99.addNames(new Dynamic($$0, $$1), ITEM_TO_BLOCKENTITY, ITEM_TO_ENTITY);
        }
    };

    public V99(int $$0, Schema $$1) {
        super($$0, $$1);
    }

    protected static void registerThrowableProjectile(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.register($$1, $$2, () -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in($$0)));
    }

    protected static void registerMinecart(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.register($$1, $$2, () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0)));
    }

    protected static void registerInventory(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
        $$0.register($$1, $$2, () -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
        HashMap $$12 = Maps.newHashMap();
        $$0.register((Map)$$12, "Item", $$1 -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "XPOrb");
        V99.registerThrowableProjectile($$0, $$12, "ThrownEgg");
        $$0.registerSimple((Map)$$12, "LeashKnot");
        $$0.registerSimple((Map)$$12, "Painting");
        $$0.register((Map)$$12, "Arrow", $$1 -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in($$0)));
        $$0.register((Map)$$12, "TippedArrow", $$1 -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in($$0)));
        $$0.register((Map)$$12, "SpectralArrow", $$1 -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in($$0)));
        V99.registerThrowableProjectile($$0, $$12, "Snowball");
        V99.registerThrowableProjectile($$0, $$12, "Fireball");
        V99.registerThrowableProjectile($$0, $$12, "SmallFireball");
        V99.registerThrowableProjectile($$0, $$12, "ThrownEnderpearl");
        $$0.registerSimple((Map)$$12, "EyeOfEnderSignal");
        $$0.register((Map)$$12, "ThrownPotion", $$1 -> DSL.optionalFields((String)"inTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"Potion", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V99.registerThrowableProjectile($$0, $$12, "ThrownExpBottle");
        $$0.register((Map)$$12, "ItemFrame", $$1 -> DSL.optionalFields((String)"Item", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V99.registerThrowableProjectile($$0, $$12, "WitherSkull");
        $$0.registerSimple((Map)$$12, "PrimedTnt");
        $$0.register((Map)$$12, "FallingSand", $$1 -> DSL.optionalFields((String)"Block", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"TileEntityData", (TypeTemplate)References.BLOCK_ENTITY.in($$0)));
        $$0.register((Map)$$12, "FireworksRocketEntity", $$1 -> DSL.optionalFields((String)"FireworksItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "Boat");
        $$0.register((Map)$$12, "Minecart", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        V99.registerMinecart($$0, $$12, "MinecartRideable");
        $$0.register((Map)$$12, "MinecartChest", $$1 -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        V99.registerMinecart($$0, $$12, "MinecartFurnace");
        V99.registerMinecart($$0, $$12, "MinecartTNT");
        $$0.register((Map)$$12, "MinecartSpawner", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (TypeTemplate)References.UNTAGGED_SPAWNER.in($$0)));
        $$0.register((Map)$$12, "MinecartHopper", $$1 -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.register((Map)$$12, "MinecartCommandBlock", () -> DSL.optionalFields((String)"DisplayTile", (TypeTemplate)References.BLOCK_NAME.in($$0), (String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerSimple((Map)$$12, "ArmorStand");
        $$0.registerSimple((Map)$$12, "Creeper");
        $$0.registerSimple((Map)$$12, "Skeleton");
        $$0.registerSimple((Map)$$12, "Spider");
        $$0.registerSimple((Map)$$12, "Giant");
        $$0.registerSimple((Map)$$12, "Zombie");
        $$0.registerSimple((Map)$$12, "Slime");
        $$0.registerSimple((Map)$$12, "Ghast");
        $$0.registerSimple((Map)$$12, "PigZombie");
        $$0.register((Map)$$12, "Enderman", $$1 -> DSL.optionalFields((String)"carried", (TypeTemplate)References.BLOCK_NAME.in($$0)));
        $$0.registerSimple((Map)$$12, "CaveSpider");
        $$0.registerSimple((Map)$$12, "Silverfish");
        $$0.registerSimple((Map)$$12, "Blaze");
        $$0.registerSimple((Map)$$12, "LavaSlime");
        $$0.registerSimple((Map)$$12, "EnderDragon");
        $$0.registerSimple((Map)$$12, "WitherBoss");
        $$0.registerSimple((Map)$$12, "Bat");
        $$0.registerSimple((Map)$$12, "Witch");
        $$0.registerSimple((Map)$$12, "Endermite");
        $$0.registerSimple((Map)$$12, "Guardian");
        $$0.registerSimple((Map)$$12, "Pig");
        $$0.registerSimple((Map)$$12, "Sheep");
        $$0.registerSimple((Map)$$12, "Cow");
        $$0.registerSimple((Map)$$12, "Chicken");
        $$0.registerSimple((Map)$$12, "Squid");
        $$0.registerSimple((Map)$$12, "Wolf");
        $$0.registerSimple((Map)$$12, "MushroomCow");
        $$0.registerSimple((Map)$$12, "SnowMan");
        $$0.registerSimple((Map)$$12, "Ozelot");
        $$0.registerSimple((Map)$$12, "VillagerGolem");
        $$0.register((Map)$$12, "EntityHorse", $$1 -> DSL.optionalFields((String)"Items", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"ArmorItem", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"SaddleItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerSimple((Map)$$12, "Rabbit");
        $$0.register((Map)$$12, "Villager", $$1 -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"Offers", (TypeTemplate)DSL.optionalFields((String)"Recipes", (TypeTemplate)DSL.list((TypeTemplate)References.VILLAGER_TRADE.in($$0)))));
        $$0.registerSimple((Map)$$12, "EnderCrystal");
        $$0.register((Map)$$12, "AreaEffectCloud", $$1 -> DSL.optionalFields((String)"Particle", (TypeTemplate)References.PARTICLE.in($$0)));
        $$0.registerSimple((Map)$$12, "ShulkerBullet");
        $$0.registerSimple((Map)$$12, "DragonFireball");
        $$0.registerSimple((Map)$$12, "Shulker");
        return $$12;
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
        HashMap $$12 = Maps.newHashMap();
        V99.registerInventory($$0, $$12, "Furnace");
        V99.registerInventory($$0, $$12, "Chest");
        $$0.registerSimple((Map)$$12, "EnderChest");
        $$0.register((Map)$$12, "RecordPlayer", $$1 -> DSL.optionalFields((String)"RecordItem", (TypeTemplate)References.ITEM_STACK.in($$0)));
        V99.registerInventory($$0, $$12, "Trap");
        V99.registerInventory($$0, $$12, "Dropper");
        $$0.register((Map)$$12, "Sign", () -> V99.sign($$0));
        $$0.register((Map)$$12, "MobSpawner", $$1 -> References.UNTAGGED_SPAWNER.in($$0));
        $$0.registerSimple((Map)$$12, "Music");
        $$0.registerSimple((Map)$$12, "Piston");
        V99.registerInventory($$0, $$12, "Cauldron");
        $$0.registerSimple((Map)$$12, "EnchantTable");
        $$0.registerSimple((Map)$$12, "Airportal");
        $$0.register((Map)$$12, "Control", () -> DSL.optionalFields((String)"LastOutput", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerSimple((Map)$$12, "Beacon");
        $$0.register((Map)$$12, "Skull", () -> DSL.optionalFields((String)"custom_name", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerSimple((Map)$$12, "DLDetector");
        V99.registerInventory($$0, $$12, "Hopper");
        $$0.registerSimple((Map)$$12, "Comparator");
        $$0.register((Map)$$12, "FlowerPot", $$1 -> DSL.optionalFields((String)"Item", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)References.ITEM_NAME.in($$0))));
        $$0.register((Map)$$12, "Banner", () -> DSL.optionalFields((String)"CustomName", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerSimple((Map)$$12, "Structure");
        $$0.registerSimple((Map)$$12, "EndGateway");
        return $$12;
    }

    public static TypeTemplate sign(Schema $$0) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"Text1", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"Text2", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"Text3", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"Text4", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"FilteredText1", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"FilteredText2", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"FilteredText3", (Object)References.TEXT_COMPONENT.in($$0)), Pair.of((Object)"FilteredText4", (Object)References.TEXT_COMPONENT.in($$0))});
    }

    public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
        $$0.registerType(false, References.LEVEL, () -> DSL.optionalFields((String)"CustomBossEvents", (TypeTemplate)DSL.compoundList((TypeTemplate)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in($$0))), (TypeTemplate)References.LIGHTWEIGHT_LEVEL.in($$0)));
        $$0.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
        $$0.registerType(false, References.PLAYER, () -> DSL.optionalFields((String)"Inventory", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)), (String)"EnderItems", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))));
        $$0.registerType(false, References.CHUNK, () -> DSL.fields((String)"Level", (TypeTemplate)DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0)), (String)"TileEntities", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_ENTITY.in($$0), (TypeTemplate)DSL.remainder())), (String)"TileTicks", (TypeTemplate)DSL.list((TypeTemplate)DSL.fields((String)"i", (TypeTemplate)References.BLOCK_NAME.in($$0))))));
        $$0.registerType(true, References.BLOCK_ENTITY, () -> DSL.optionalFields((String)"components", (TypeTemplate)References.DATA_COMPONENTS.in($$0), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)$$2)));
        $$0.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields((String)"Riding", (TypeTemplate)References.ENTITY_TREE.in($$0), (TypeTemplate)References.ENTITY.in($$0)));
        $$0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        $$0.registerType(true, References.ENTITY, () -> DSL.and((TypeTemplate)References.ENTITY_EQUIPMENT.in($$0), (TypeTemplate)DSL.optionalFields((String)"CustomName", (TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.taggedChoiceLazy((String)"id", (Type)DSL.string(), (Map)$$1))));
        $$0.registerType(true, References.ITEM_STACK, () -> DSL.hook((TypeTemplate)DSL.optionalFields((String)"id", (TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)References.ITEM_NAME.in($$0)), (String)"tag", (TypeTemplate)V99.itemStackTag($$0)), (Hook.HookFunction)ADD_NAMES, (Hook.HookFunction)Hook.HookFunction.IDENTITY));
        $$0.registerType(false, References.OPTIONS, DSL::remainder);
        $$0.registerType(false, References.BLOCK_NAME, () -> DSL.or((TypeTemplate)DSL.constType((Type)DSL.intType()), (TypeTemplate)DSL.constType(NamespacedSchema.namespacedString())));
        $$0.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        $$0.registerType(false, References.STATS, DSL::remainder);
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
        $$0.registerType(false, References.OBJECTIVE, DSL::remainder);
        $$0.registerType(false, References.TEAM, () -> DSL.optionalFields((String)"MemberNamePrefix", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"MemberNameSuffix", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"DisplayName", (TypeTemplate)References.TEXT_COMPONENT.in($$0)));
        $$0.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        $$0.registerType(false, References.POI_CHUNK, DSL::remainder);
        $$0.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
        $$0.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields((String)"Entities", (TypeTemplate)DSL.list((TypeTemplate)References.ENTITY_TREE.in($$0))));
        $$0.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
        $$0.registerType(true, References.VILLAGER_TRADE, () -> DSL.optionalFields((String)"buy", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"buyB", (TypeTemplate)References.ITEM_STACK.in($$0), (String)"sell", (TypeTemplate)References.ITEM_STACK.in($$0)));
        $$0.registerType(true, References.PARTICLE, () -> DSL.constType((Type)DSL.string()));
        $$0.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType((Type)DSL.string()));
        $$0.registerType(false, References.STRUCTURE, () -> DSL.optionalFields((String)"entities", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.ENTITY_TREE.in($$0))), (String)"blocks", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"nbt", (TypeTemplate)References.BLOCK_ENTITY.in($$0))), (String)"palette", (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_STATE.in($$0))));
        $$0.registerType(false, References.BLOCK_STATE, DSL::remainder);
        $$0.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
        $$0.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.optional((TypeTemplate)DSL.field((String)"Equipment", (TypeTemplate)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0)))));
    }

    public static TypeTemplate itemStackTag(Schema $$0) {
        return DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"EntityTag", (Object)References.ENTITY_TREE.in($$0)), Pair.of((Object)"BlockEntityTag", (Object)References.BLOCK_ENTITY.in($$0)), Pair.of((Object)"CanDestroy", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in($$0))), Pair.of((Object)"CanPlaceOn", (Object)DSL.list((TypeTemplate)References.BLOCK_NAME.in($$0))), Pair.of((Object)"Items", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"ChargedProjectiles", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in($$0))), Pair.of((Object)"pages", (Object)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in($$0))), Pair.of((Object)"filtered_pages", (Object)DSL.compoundList((TypeTemplate)References.TEXT_COMPONENT.in($$0))), Pair.of((Object)"display", (Object)DSL.optionalFields((String)"Name", (TypeTemplate)References.TEXT_COMPONENT.in($$0), (String)"Lore", (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in($$0))))});
    }

    protected static <T> T addNames(Dynamic<T> $$0, Map<String, String> $$1, Map<String, String> $$2) {
        return (T)$$0.update("tag", $$3 -> $$3.update("BlockEntityTag", $$2 -> {
            Object $$3 = $$0.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
            if (!"minecraft:air".equals($$3)) {
                String $$4 = (String)$$1.get($$3);
                if ($$4 == null) {
                    LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", $$3);
                } else {
                    return $$2.set("id", $$0.createString($$4));
                }
            }
            return $$2;
        }).update("EntityTag", $$2 -> {
            if ($$2.get("id").result().isPresent()) {
                return $$2;
            }
            Object $$3 = NamespacedSchema.ensureNamespaced($$0.get("id").asString(""));
            String $$4 = (String)$$2.get($$3);
            if ($$4 != null) {
                return $$2.set("id", $$0.createString($$4));
            }
            return $$2;
        })).getValue();
    }
}

