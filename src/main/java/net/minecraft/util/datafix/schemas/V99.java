package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class V99 extends Schema {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Map<String, String> ITEM_TO_BLOCKENTITY = (Map<String, String>)DataFixUtils.make(Maps.newHashMap(), $$0 -> {
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
   protected static final HookFunction ADD_NAMES = new HookFunction() {
      public <T> T apply(DynamicOps<T> $$0, T $$1) {
         return V99.addNames(new Dynamic($$0, $$1), V99.ITEM_TO_BLOCKENTITY, V99.ITEM_TO_ENTITY);
      }
   };

   public V99(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   protected static void registerThrowableProjectile(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register($$1, $$2, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
   }

   protected static void registerMinecart(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register($$1, $$2, () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0)));
   }

   protected static void registerInventory(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register($$1, $$2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in($$0))));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = Maps.newHashMap();
      $$0.register($$1, "Item", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      $$0.registerSimple($$1, "XPOrb");
      registerThrowableProjectile($$0, $$1, "ThrownEgg");
      $$0.registerSimple($$1, "LeashKnot");
      $$0.registerSimple($$1, "Painting");
      $$0.register($$1, "Arrow", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
      $$0.register($$1, "TippedArrow", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
      $$0.register($$1, "SpectralArrow", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
      registerThrowableProjectile($$0, $$1, "Snowball");
      registerThrowableProjectile($$0, $$1, "Fireball");
      registerThrowableProjectile($$0, $$1, "SmallFireball");
      registerThrowableProjectile($$0, $$1, "ThrownEnderpearl");
      $$0.registerSimple($$1, "EyeOfEnderSignal");
      $$0.register($$1, "ThrownPotion", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0), "Potion", References.ITEM_STACK.in($$0)));
      registerThrowableProjectile($$0, $$1, "ThrownExpBottle");
      $$0.register($$1, "ItemFrame", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      registerThrowableProjectile($$0, $$1, "WitherSkull");
      $$0.registerSimple($$1, "PrimedTnt");
      $$0.register($$1, "FallingSand", $$1x -> DSL.optionalFields("Block", References.BLOCK_NAME.in($$0), "TileEntityData", References.BLOCK_ENTITY.in($$0)));
      $$0.register($$1, "FireworksRocketEntity", $$1x -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in($$0)));
      $$0.registerSimple($$1, "Boat");
      $$0.register($$1, "Minecart", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0))));
      registerMinecart($$0, $$1, "MinecartRideable");
      $$0.register(
         $$1, "MinecartChest", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      registerMinecart($$0, $$1, "MinecartFurnace");
      registerMinecart($$0, $$1, "MinecartTNT");
      $$0.register($$1, "MinecartSpawner", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), References.UNTAGGED_SPAWNER.in($$0)));
      $$0.register(
         $$1, "MinecartHopper", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      $$0.register(
         $$1, "MinecartCommandBlock", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "LastOutput", References.TEXT_COMPONENT.in($$0))
      );
      $$0.registerSimple($$1, "ArmorStand");
      $$0.registerSimple($$1, "Creeper");
      $$0.registerSimple($$1, "Skeleton");
      $$0.registerSimple($$1, "Spider");
      $$0.registerSimple($$1, "Giant");
      $$0.registerSimple($$1, "Zombie");
      $$0.registerSimple($$1, "Slime");
      $$0.registerSimple($$1, "Ghast");
      $$0.registerSimple($$1, "PigZombie");
      $$0.register($$1, "Enderman", $$1x -> DSL.optionalFields("carried", References.BLOCK_NAME.in($$0)));
      $$0.registerSimple($$1, "CaveSpider");
      $$0.registerSimple($$1, "Silverfish");
      $$0.registerSimple($$1, "Blaze");
      $$0.registerSimple($$1, "LavaSlime");
      $$0.registerSimple($$1, "EnderDragon");
      $$0.registerSimple($$1, "WitherBoss");
      $$0.registerSimple($$1, "Bat");
      $$0.registerSimple($$1, "Witch");
      $$0.registerSimple($$1, "Endermite");
      $$0.registerSimple($$1, "Guardian");
      $$0.registerSimple($$1, "Pig");
      $$0.registerSimple($$1, "Sheep");
      $$0.registerSimple($$1, "Cow");
      $$0.registerSimple($$1, "Chicken");
      $$0.registerSimple($$1, "Squid");
      $$0.registerSimple($$1, "Wolf");
      $$0.registerSimple($$1, "MushroomCow");
      $$0.registerSimple($$1, "SnowMan");
      $$0.registerSimple($$1, "Ozelot");
      $$0.registerSimple($$1, "VillagerGolem");
      $$0.register(
         $$1,
         "EntityHorse",
         $$1x -> DSL.optionalFields(
            "Items", DSL.list(References.ITEM_STACK.in($$0)), "ArmorItem", References.ITEM_STACK.in($$0), "SaddleItem", References.ITEM_STACK.in($$0)
         )
      );
      $$0.registerSimple($$1, "Rabbit");
      $$0.register(
         $$1,
         "Villager",
         $$1x -> DSL.optionalFields(
            "Inventory", DSL.list(References.ITEM_STACK.in($$0)), "Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in($$0)))
         )
      );
      $$0.registerSimple($$1, "EnderCrystal");
      $$0.register($$1, "AreaEffectCloud", $$1x -> DSL.optionalFields("Particle", References.PARTICLE.in($$0)));
      $$0.registerSimple($$1, "ShulkerBullet");
      $$0.registerSimple($$1, "DragonFireball");
      $$0.registerSimple($$1, "Shulker");
      return $$1;
   }

   public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = Maps.newHashMap();
      registerInventory($$0, $$1, "Furnace");
      registerInventory($$0, $$1, "Chest");
      $$0.registerSimple($$1, "EnderChest");
      $$0.register($$1, "RecordPlayer", $$1x -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in($$0)));
      registerInventory($$0, $$1, "Trap");
      registerInventory($$0, $$1, "Dropper");
      $$0.register($$1, "Sign", () -> sign($$0));
      $$0.register($$1, "MobSpawner", $$1x -> References.UNTAGGED_SPAWNER.in($$0));
      $$0.registerSimple($$1, "Music");
      $$0.registerSimple($$1, "Piston");
      registerInventory($$0, $$1, "Cauldron");
      $$0.registerSimple($$1, "EnchantTable");
      $$0.registerSimple($$1, "Airportal");
      $$0.register($$1, "Control", () -> DSL.optionalFields("LastOutput", References.TEXT_COMPONENT.in($$0)));
      $$0.registerSimple($$1, "Beacon");
      $$0.register($$1, "Skull", () -> DSL.optionalFields("custom_name", References.TEXT_COMPONENT.in($$0)));
      $$0.registerSimple($$1, "DLDetector");
      registerInventory($$0, $$1, "Hopper");
      $$0.registerSimple($$1, "Comparator");
      $$0.register($$1, "FlowerPot", $$1x -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in($$0))));
      $$0.register($$1, "Banner", () -> DSL.optionalFields("CustomName", References.TEXT_COMPONENT.in($$0)));
      $$0.registerSimple($$1, "Structure");
      $$0.registerSimple($$1, "EndGateway");
      return $$1;
   }

   public static TypeTemplate sign(Schema $$0) {
      return DSL.optionalFields(
         new Pair[]{
            Pair.of("Text1", References.TEXT_COMPONENT.in($$0)),
            Pair.of("Text2", References.TEXT_COMPONENT.in($$0)),
            Pair.of("Text3", References.TEXT_COMPONENT.in($$0)),
            Pair.of("Text4", References.TEXT_COMPONENT.in($$0)),
            Pair.of("FilteredText1", References.TEXT_COMPONENT.in($$0)),
            Pair.of("FilteredText2", References.TEXT_COMPONENT.in($$0)),
            Pair.of("FilteredText3", References.TEXT_COMPONENT.in($$0)),
            Pair.of("FilteredText4", References.TEXT_COMPONENT.in($$0))
         }
      );
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
      $$0.registerType(
         false,
         References.PLAYER,
         () -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in($$0)), "EnderItems", DSL.list(References.ITEM_STACK.in($$0)))
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
               DSL.list(DSL.fields("i", References.BLOCK_NAME.in($$0)))
            )
         )
      );
      $$0.registerType(
         true,
         References.BLOCK_ENTITY,
         () -> DSL.optionalFields("components", References.DATA_COMPONENTS.in($$0), DSL.taggedChoiceLazy("id", DSL.string(), $$2))
      );
      $$0.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Riding", References.ENTITY_TREE.in($$0), References.ENTITY.in($$0)));
      $$0.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
      $$0.registerType(
         true,
         References.ENTITY,
         () -> DSL.and(
            References.ENTITY_EQUIPMENT.in($$0), DSL.optionalFields("CustomName", DSL.constType(DSL.string()), DSL.taggedChoiceLazy("id", DSL.string(), $$1))
         )
      );
      $$0.registerType(
         true,
         References.ITEM_STACK,
         () -> DSL.hook(
            DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in($$0)), "tag", itemStackTag($$0)),
            ADD_NAMES,
            HookFunction.IDENTITY
         )
      );
      $$0.registerType(false, References.OPTIONS, DSL::remainder);
      $$0.registerType(false, References.BLOCK_NAME, () -> DSL.or(DSL.constType(DSL.intType()), DSL.constType(NamespacedSchema.namespacedString())));
      $$0.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
      $$0.registerType(false, References.STATS, DSL::remainder);
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
      $$0.registerType(false, References.OBJECTIVE, DSL::remainder);
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
      $$0.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
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
      $$0.registerType(false, References.BLOCK_STATE, DSL::remainder);
      $$0.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
      $$0.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.optional(DSL.field("Equipment", DSL.list(References.ITEM_STACK.in($$0)))));
   }

   public static TypeTemplate itemStackTag(Schema $$0) {
      return DSL.optionalFields(
         new Pair[]{
            Pair.of("EntityTag", References.ENTITY_TREE.in($$0)),
            Pair.of("BlockEntityTag", References.BLOCK_ENTITY.in($$0)),
            Pair.of("CanDestroy", DSL.list(References.BLOCK_NAME.in($$0))),
            Pair.of("CanPlaceOn", DSL.list(References.BLOCK_NAME.in($$0))),
            Pair.of("Items", DSL.list(References.ITEM_STACK.in($$0))),
            Pair.of("ChargedProjectiles", DSL.list(References.ITEM_STACK.in($$0))),
            Pair.of("pages", DSL.list(References.TEXT_COMPONENT.in($$0))),
            Pair.of("filtered_pages", DSL.compoundList(References.TEXT_COMPONENT.in($$0))),
            Pair.of("display", DSL.optionalFields("Name", References.TEXT_COMPONENT.in($$0), "Lore", DSL.list(References.TEXT_COMPONENT.in($$0))))
         }
      );
   }

   protected static <T> T addNames(Dynamic<T> $$0, Map<String, String> $$1, Map<String, String> $$2) {
      return (T)$$0.update("tag", $$3 -> $$3.update("BlockEntityTag", $$2xx -> {
         String $$3x = $$0.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
         if (!"minecraft:air".equals($$3x)) {
            String $$4 = $$1.get($$3x);
            if ($$4 != null) {
               return $$2xx.set("id", $$0.createString($$4));
            }

            LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", $$3x);
         }

         return $$2xx;
      }).update("EntityTag", $$2xx -> {
         if ($$2xx.get("id").result().isPresent()) {
            return $$2xx;
         } else {
            String $$3x = NamespacedSchema.ensureNamespaced($$0.get("id").asString(""));
            String $$4 = $$2.get($$3x);
            return $$4 != null ? $$2xx.set("id", $$0.createString($$4)) : $$2xx;
         }
      })).getValue();
   }
}
