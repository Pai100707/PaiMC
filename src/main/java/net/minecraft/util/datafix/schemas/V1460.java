package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1460 extends NamespacedSchema {
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
      Map<String, Supplier<TypeTemplate>> $$1 = Maps.newHashMap();
      $$0.register($$1, "minecraft:area_effect_cloud", $$1x -> DSL.optionalFields("Particle", References.PARTICLE.in($$0)));
      registerMob($$0, $$1, "minecraft:armor_stand");
      $$0.register($$1, "minecraft:arrow", $$1x -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:bat");
      registerMob($$0, $$1, "minecraft:blaze");
      $$0.registerSimple($$1, "minecraft:boat");
      registerMob($$0, $$1, "minecraft:cave_spider");
      $$0.register(
         $$1,
         "minecraft:chest_minecart",
         $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      registerMob($$0, $$1, "minecraft:chicken");
      $$0.register(
         $$1,
         "minecraft:commandblock_minecart",
         $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0), "LastOutput", References.TEXT_COMPONENT.in($$0))
      );
      registerMob($$0, $$1, "minecraft:cow");
      registerMob($$0, $$1, "minecraft:creeper");
      $$0.register(
         $$1, "minecraft:donkey", $$1x -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0))
      );
      $$0.registerSimple($$1, "minecraft:dragon_fireball");
      $$0.registerSimple($$1, "minecraft:egg");
      registerMob($$0, $$1, "minecraft:elder_guardian");
      $$0.registerSimple($$1, "minecraft:ender_crystal");
      registerMob($$0, $$1, "minecraft:ender_dragon");
      $$0.register($$1, "minecraft:enderman", $$1x -> DSL.optionalFields("carriedBlockState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:endermite");
      $$0.registerSimple($$1, "minecraft:ender_pearl");
      $$0.registerSimple($$1, "minecraft:evocation_fangs");
      registerMob($$0, $$1, "minecraft:evocation_illager");
      $$0.registerSimple($$1, "minecraft:eye_of_ender_signal");
      $$0.register(
         $$1,
         "minecraft:falling_block",
         $$1x -> DSL.optionalFields("BlockState", References.BLOCK_STATE.in($$0), "TileEntityData", References.BLOCK_ENTITY.in($$0))
      );
      $$0.registerSimple($$1, "minecraft:fireball");
      $$0.register($$1, "minecraft:fireworks_rocket", $$1x -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in($$0)));
      $$0.register($$1, "minecraft:furnace_minecart", $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:ghast");
      registerMob($$0, $$1, "minecraft:giant");
      registerMob($$0, $$1, "minecraft:guardian");
      $$0.register(
         $$1,
         "minecraft:hopper_minecart",
         $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      $$0.register($$1, "minecraft:horse", $$1x -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in($$0), "SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:husk");
      registerMob($$0, $$1, "minecraft:illusion_illager");
      $$0.register($$1, "minecraft:item", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      $$0.register($$1, "minecraft:item_frame", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      $$0.registerSimple($$1, "minecraft:leash_knot");
      $$0.register(
         $$1,
         "minecraft:llama",
         $$1x -> DSL.optionalFields(
            "Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0), "DecorItem", References.ITEM_STACK.in($$0)
         )
      );
      $$0.registerSimple($$1, "minecraft:llama_spit");
      registerMob($$0, $$1, "minecraft:magma_cube");
      $$0.register($$1, "minecraft:minecart", $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:mooshroom");
      $$0.register(
         $$1, "minecraft:mule", $$1x -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0))
      );
      registerMob($$0, $$1, "minecraft:ocelot");
      $$0.registerSimple($$1, "minecraft:painting");
      registerMob($$0, $$1, "minecraft:parrot");
      registerMob($$0, $$1, "minecraft:pig");
      registerMob($$0, $$1, "minecraft:polar_bear");
      $$0.register($$1, "minecraft:potion", $$1x -> DSL.optionalFields("Potion", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:rabbit");
      registerMob($$0, $$1, "minecraft:sheep");
      registerMob($$0, $$1, "minecraft:shulker");
      $$0.registerSimple($$1, "minecraft:shulker_bullet");
      registerMob($$0, $$1, "minecraft:silverfish");
      registerMob($$0, $$1, "minecraft:skeleton");
      $$0.register($$1, "minecraft:skeleton_horse", $$1x -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:slime");
      $$0.registerSimple($$1, "minecraft:small_fireball");
      $$0.registerSimple($$1, "minecraft:snowball");
      registerMob($$0, $$1, "minecraft:snowman");
      $$0.register(
         $$1, "minecraft:spawner_minecart", $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0), References.UNTAGGED_SPAWNER.in($$0))
      );
      $$0.register($$1, "minecraft:spectral_arrow", $$1x -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:spider");
      registerMob($$0, $$1, "minecraft:squid");
      registerMob($$0, $$1, "minecraft:stray");
      $$0.registerSimple($$1, "minecraft:tnt");
      $$0.register($$1, "minecraft:tnt_minecart", $$1x -> DSL.optionalFields("DisplayState", References.BLOCK_STATE.in($$0)));
      registerMob($$0, $$1, "minecraft:vex");
      $$0.register(
         $$1,
         "minecraft:villager",
         $$1x -> DSL.optionalFields(
            "Inventory", DSL.list(References.ITEM_STACK.in($$0)), "Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in($$0)))
         )
      );
      registerMob($$0, $$1, "minecraft:villager_golem");
      registerMob($$0, $$1, "minecraft:vindication_illager");
      registerMob($$0, $$1, "minecraft:witch");
      registerMob($$0, $$1, "minecraft:wither");
      registerMob($$0, $$1, "minecraft:wither_skeleton");
      $$0.registerSimple($$1, "minecraft:wither_skull");
      registerMob($$0, $$1, "minecraft:wolf");
      $$0.registerSimple($$1, "minecraft:xp_bottle");
      $$0.registerSimple($$1, "minecraft:xp_orb");
      registerMob($$0, $$1, "minecraft:zombie");
      $$0.register($$1, "minecraft:zombie_horse", $$1x -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:zombie_pigman");
      $$0.register(
         $$1, "minecraft:zombie_villager", $$1x -> DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in($$0))))
      );
      return $$1;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = Maps.newHashMap();
      registerInventory($$0, $$1, "minecraft:furnace");
      registerInventory($$0, $$1, "minecraft:chest");
      registerInventory($$0, $$1, "minecraft:trapped_chest");
      $$0.registerSimple($$1, "minecraft:ender_chest");
      $$0.register($$1, "minecraft:jukebox", $$1x -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in($$0)));
      registerInventory($$0, $$1, "minecraft:dispenser");
      registerInventory($$0, $$1, "minecraft:dropper");
      $$0.register($$1, "minecraft:sign", () -> V99.sign($$0));
      $$0.register($$1, "minecraft:mob_spawner", $$1x -> References.UNTAGGED_SPAWNER.in($$0));
      $$0.register($$1, "minecraft:piston", $$1x -> DSL.optionalFields("blockState", References.BLOCK_STATE.in($$0)));
      registerInventory($$0, $$1, "minecraft:brewing_stand");
      $$0.register($$1, "minecraft:enchanting_table", () -> V1458.nameable($$0));
      $$0.registerSimple($$1, "minecraft:end_portal");
      $$0.register($$1, "minecraft:beacon", () -> V1458.nameable($$0));
      $$0.register($$1, "minecraft:skull", () -> DSL.optionalFields("custom_name", References.TEXT_COMPONENT.in($$0)));
      $$0.registerSimple($$1, "minecraft:daylight_detector");
      registerInventory($$0, $$1, "minecraft:hopper");
      $$0.registerSimple($$1, "minecraft:comparator");
      $$0.register($$1, "minecraft:banner", () -> V1458.nameable($$0));
      $$0.registerSimple($$1, "minecraft:structure_block");
      $$0.registerSimple($$1, "minecraft:end_gateway");
      $$0.register($$1, "minecraft:command_block", () -> DSL.optionalFields("LastOutput", References.TEXT_COMPONENT.in($$0)));
      registerInventory($$0, $$1, "minecraft:shulker_box");
      $$0.registerSimple($$1, "minecraft:bed");
      return $$1;
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      $$0.registerType(
         false,
         References.LEVEL,
         () -> DSL.optionalFields(
            "CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", References.TEXT_COMPONENT.in($$0))), References.LIGHTWEIGHT_LEVEL.in($$0)
         )
      );
      $$0.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
      $$0.registerType(false, References.RECIPE, () -> DSL.constType(namespacedString()));
      $$0.registerType(
         false,
         References.PLAYER,
         () -> DSL.optionalFields(
            new Pair[]{
               Pair.of("RootVehicle", DSL.optionalFields("Entity", References.ENTITY_TREE.in($$0))),
               Pair.of("ender_pearls", DSL.list(References.ENTITY_TREE.in($$0))),
               Pair.of("Inventory", DSL.list(References.ITEM_STACK.in($$0))),
               Pair.of("EnderItems", DSL.list(References.ITEM_STACK.in($$0))),
               Pair.of("ShoulderEntityLeft", References.ENTITY_TREE.in($$0)),
               Pair.of("ShoulderEntityRight", References.ENTITY_TREE.in($$0)),
               Pair.of("recipeBook", DSL.optionalFields("recipes", DSL.list(References.RECIPE.in($$0)), "toBeDisplayed", DSL.list(References.RECIPE.in($$0))))
            }
         )
      );
      $$0.registerType(
         false,
         References.CHUNK,
         () -> DSL.fields(
            "Level",
            DSL.optionalFields(
               "Entities",
               DSL.list(References.ENTITY_TREE.in($$0)),
               "TileEntities",
               DSL.list(DSL.or(References.BLOCK_ENTITY.in($$0), DSL.remainder())),
               "TileTicks",
               DSL.list(DSL.fields("i", References.BLOCK_NAME.in($$0))),
               "Sections",
               DSL.list(DSL.optionalFields("Palette", DSL.list(References.BLOCK_STATE.in($$0))))
            )
         )
      );
      $$0.registerType(
         true,
         References.BLOCK_ENTITY,
         () -> DSL.optionalFields("components", References.DATA_COMPONENTS.in($$0), DSL.taggedChoiceLazy("id", namespacedString(), $$2))
      );
      $$0.registerType(
         true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in($$0)), References.ENTITY.in($$0))
      );
      $$0.registerType(
         true,
         References.ENTITY,
         () -> DSL.and(
            References.ENTITY_EQUIPMENT.in($$0),
            DSL.optionalFields("CustomName", References.TEXT_COMPONENT.in($$0), DSL.taggedChoiceLazy("id", namespacedString(), $$1))
         )
      );
      $$0.registerType(
         true,
         References.ITEM_STACK,
         () -> DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in($$0), "tag", V99.itemStackTag($$0)), V705.ADD_NAMES, HookFunction.IDENTITY)
      );
      $$0.registerType(false, References.HOTBAR, () -> DSL.compoundList(DSL.list(References.ITEM_STACK.in($$0))));
      $$0.registerType(false, References.OPTIONS, DSL::remainder);
      $$0.registerType(
         false,
         References.STRUCTURE,
         () -> DSL.optionalFields(
            "entities",
            DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in($$0))),
            "blocks",
            DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in($$0))),
            "palette",
            DSL.list(References.BLOCK_STATE.in($$0))
         )
      );
      $$0.registerType(false, References.BLOCK_NAME, () -> DSL.constType(namespacedString()));
      $$0.registerType(false, References.ITEM_NAME, () -> DSL.constType(namespacedString()));
      $$0.registerType(false, References.BLOCK_STATE, DSL::remainder);
      $$0.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
      Supplier<TypeTemplate> $$3 = () -> DSL.compoundList(References.ITEM_NAME.in($$0), DSL.constType(DSL.intType()));
      $$0.registerType(
         false,
         References.STATS,
         () -> DSL.optionalFields(
            "stats",
            DSL.optionalFields(
               new Pair[]{
                  Pair.of("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:crafted", $$3.get()),
                  Pair.of("minecraft:used", $$3.get()),
                  Pair.of("minecraft:broken", $$3.get()),
                  Pair.of("minecraft:picked_up", $$3.get()),
                  Pair.of("minecraft:dropped", $$3.get()),
                  Pair.of("minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.intType()))),
                  Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType())))
               }
            )
         )
      );
      $$0.registerType(false, References.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
      $$0.registerType(false, References.SAVED_DATA_TICKETS, DSL::remainder);
      $$0.registerType(
         false,
         References.SAVED_DATA_MAP_DATA,
         () -> DSL.optionalFields("data", DSL.optionalFields("banners", DSL.list(DSL.optionalFields("Name", References.TEXT_COMPONENT.in($$0)))))
      );
      $$0.registerType(false, References.SAVED_DATA_MAP_INDEX, DSL::remainder);
      $$0.registerType(false, References.SAVED_DATA_RAIDS, DSL::remainder);
      $$0.registerType(false, References.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
      $$0.registerType(
         false,
         References.SAVED_DATA_SCOREBOARD,
         () -> DSL.optionalFields(
            "data",
            DSL.optionalFields(
               "Objectives",
               DSL.list(References.OBJECTIVE.in($$0)),
               "Teams",
               DSL.list(References.TEAM.in($$0)),
               "PlayerScores",
               DSL.list(DSL.optionalFields("display", References.TEXT_COMPONENT.in($$0)))
            )
         )
      );
      $$0.registerType(false, References.SAVED_DATA_STOPWATCHES, DSL::remainder);
      $$0.registerType(
         false,
         References.SAVED_DATA_STRUCTURE_FEATURE_INDICES,
         () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in($$0))))
      );
      $$0.registerType(false, References.SAVED_DATA_WORLD_BORDER, DSL::remainder);
      $$0.registerType(false, References.DEBUG_PROFILE, DSL::remainder);
      $$0.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
      Map<String, Supplier<TypeTemplate>> $$4 = V1451_6.createCriterionTypes($$0);
      $$0.registerType(
         false,
         References.OBJECTIVE,
         () -> DSL.hook(
            DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), $$4), "DisplayName", References.TEXT_COMPONENT.in($$0)),
            V1451_6.UNPACK_OBJECTIVE_ID,
            V1451_6.REPACK_OBJECTIVE_ID
         )
      );
      $$0.registerType(
         false,
         References.TEAM,
         () -> DSL.optionalFields(
            "MemberNamePrefix",
            References.TEXT_COMPONENT.in($$0),
            "MemberNameSuffix",
            References.TEXT_COMPONENT.in($$0),
            "DisplayName",
            References.TEXT_COMPONENT.in($$0)
         )
      );
      $$0.registerType(
         true,
         References.UNTAGGED_SPAWNER,
         () -> DSL.optionalFields(
            "SpawnPotentials", DSL.list(DSL.fields("Entity", References.ENTITY_TREE.in($$0))), "SpawnData", References.ENTITY_TREE.in($$0)
         )
      );
      $$0.registerType(
         false,
         References.ADVANCEMENTS,
         () -> DSL.optionalFields(
            "minecraft:adventure/adventuring_time",
            DSL.optionalFields("criteria", DSL.compoundList(References.BIOME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:adventure/kill_a_mob",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:adventure/kill_all_mobs",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string()))),
            "minecraft:husbandry/bred_all_animals",
            DSL.optionalFields("criteria", DSL.compoundList(References.ENTITY_NAME.in($$0), DSL.constType(DSL.string())))
         )
      );
      $$0.registerType(false, References.BIOME, () -> DSL.constType(namespacedString()));
      $$0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(namespacedString()));
      $$0.registerType(false, References.POI_CHUNK, DSL::remainder);
      $$0.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
      $$0.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in($$0))));
      $$0.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
      $$0.registerType(
         true,
         References.VILLAGER_TRADE,
         () -> DSL.optionalFields("buy", References.ITEM_STACK.in($$0), "buyB", References.ITEM_STACK.in($$0), "sell", References.ITEM_STACK.in($$0))
      );
      $$0.registerType(true, References.PARTICLE, () -> DSL.constType(DSL.string()));
      $$0.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType(DSL.string()));
      $$0.registerType(
         true,
         References.ENTITY_EQUIPMENT,
         () -> DSL.and(
            DSL.optional(DSL.field("ArmorItems", DSL.list(References.ITEM_STACK.in($$0)))),
            new TypeTemplate[]{
               DSL.optional(DSL.field("HandItems", DSL.list(References.ITEM_STACK.in($$0)))),
               DSL.optional(DSL.field("body_armor_item", References.ITEM_STACK.in($$0))),
               DSL.optional(DSL.field("saddle", References.ITEM_STACK.in($$0)))
            }
         )
      );
   }
}
