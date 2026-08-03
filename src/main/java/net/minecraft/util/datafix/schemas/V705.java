package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V705 extends NamespacedSchema {
   static final Map<String, String> ITEM_TO_ENTITY = ImmutableMap.builder()
      .put("minecraft:armor_stand", "minecraft:armor_stand")
      .put("minecraft:painting", "minecraft:painting")
      .put("minecraft:armadillo_spawn_egg", "minecraft:armadillo")
      .put("minecraft:allay_spawn_egg", "minecraft:allay")
      .put("minecraft:axolotl_spawn_egg", "minecraft:axolotl")
      .put("minecraft:bat_spawn_egg", "minecraft:bat")
      .put("minecraft:bee_spawn_egg", "minecraft:bee")
      .put("minecraft:blaze_spawn_egg", "minecraft:blaze")
      .put("minecraft:bogged_spawn_egg", "minecraft:bogged")
      .put("minecraft:breeze_spawn_egg", "minecraft:breeze")
      .put("minecraft:cat_spawn_egg", "minecraft:cat")
      .put("minecraft:camel_spawn_egg", "minecraft:camel")
      .put("minecraft:cave_spider_spawn_egg", "minecraft:cave_spider")
      .put("minecraft:chicken_spawn_egg", "minecraft:chicken")
      .put("minecraft:cod_spawn_egg", "minecraft:cod")
      .put("minecraft:cow_spawn_egg", "minecraft:cow")
      .put("minecraft:creeper_spawn_egg", "minecraft:creeper")
      .put("minecraft:dolphin_spawn_egg", "minecraft:dolphin")
      .put("minecraft:donkey_spawn_egg", "minecraft:donkey")
      .put("minecraft:drowned_spawn_egg", "minecraft:drowned")
      .put("minecraft:elder_guardian_spawn_egg", "minecraft:elder_guardian")
      .put("minecraft:ender_dragon_spawn_egg", "minecraft:ender_dragon")
      .put("minecraft:enderman_spawn_egg", "minecraft:enderman")
      .put("minecraft:endermite_spawn_egg", "minecraft:endermite")
      .put("minecraft:evoker_spawn_egg", "minecraft:evoker")
      .put("minecraft:fox_spawn_egg", "minecraft:fox")
      .put("minecraft:frog_spawn_egg", "minecraft:frog")
      .put("minecraft:ghast_spawn_egg", "minecraft:ghast")
      .put("minecraft:glow_squid_spawn_egg", "minecraft:glow_squid")
      .put("minecraft:goat_spawn_egg", "minecraft:goat")
      .put("minecraft:guardian_spawn_egg", "minecraft:guardian")
      .put("minecraft:hoglin_spawn_egg", "minecraft:hoglin")
      .put("minecraft:horse_spawn_egg", "minecraft:horse")
      .put("minecraft:husk_spawn_egg", "minecraft:husk")
      .put("minecraft:iron_golem_spawn_egg", "minecraft:iron_golem")
      .put("minecraft:llama_spawn_egg", "minecraft:llama")
      .put("minecraft:magma_cube_spawn_egg", "minecraft:magma_cube")
      .put("minecraft:mooshroom_spawn_egg", "minecraft:mooshroom")
      .put("minecraft:mule_spawn_egg", "minecraft:mule")
      .put("minecraft:ocelot_spawn_egg", "minecraft:ocelot")
      .put("minecraft:panda_spawn_egg", "minecraft:panda")
      .put("minecraft:parrot_spawn_egg", "minecraft:parrot")
      .put("minecraft:phantom_spawn_egg", "minecraft:phantom")
      .put("minecraft:pig_spawn_egg", "minecraft:pig")
      .put("minecraft:piglin_spawn_egg", "minecraft:piglin")
      .put("minecraft:piglin_brute_spawn_egg", "minecraft:piglin_brute")
      .put("minecraft:pillager_spawn_egg", "minecraft:pillager")
      .put("minecraft:polar_bear_spawn_egg", "minecraft:polar_bear")
      .put("minecraft:pufferfish_spawn_egg", "minecraft:pufferfish")
      .put("minecraft:rabbit_spawn_egg", "minecraft:rabbit")
      .put("minecraft:ravager_spawn_egg", "minecraft:ravager")
      .put("minecraft:salmon_spawn_egg", "minecraft:salmon")
      .put("minecraft:sheep_spawn_egg", "minecraft:sheep")
      .put("minecraft:shulker_spawn_egg", "minecraft:shulker")
      .put("minecraft:silverfish_spawn_egg", "minecraft:silverfish")
      .put("minecraft:skeleton_spawn_egg", "minecraft:skeleton")
      .put("minecraft:skeleton_horse_spawn_egg", "minecraft:skeleton_horse")
      .put("minecraft:slime_spawn_egg", "minecraft:slime")
      .put("minecraft:sniffer_spawn_egg", "minecraft:sniffer")
      .put("minecraft:snow_golem_spawn_egg", "minecraft:snow_golem")
      .put("minecraft:spider_spawn_egg", "minecraft:spider")
      .put("minecraft:squid_spawn_egg", "minecraft:squid")
      .put("minecraft:stray_spawn_egg", "minecraft:stray")
      .put("minecraft:strider_spawn_egg", "minecraft:strider")
      .put("minecraft:tadpole_spawn_egg", "minecraft:tadpole")
      .put("minecraft:trader_llama_spawn_egg", "minecraft:trader_llama")
      .put("minecraft:tropical_fish_spawn_egg", "minecraft:tropical_fish")
      .put("minecraft:turtle_spawn_egg", "minecraft:turtle")
      .put("minecraft:vex_spawn_egg", "minecraft:vex")
      .put("minecraft:villager_spawn_egg", "minecraft:villager")
      .put("minecraft:vindicator_spawn_egg", "minecraft:vindicator")
      .put("minecraft:wandering_trader_spawn_egg", "minecraft:wandering_trader")
      .put("minecraft:warden_spawn_egg", "minecraft:warden")
      .put("minecraft:witch_spawn_egg", "minecraft:witch")
      .put("minecraft:wither_spawn_egg", "minecraft:wither")
      .put("minecraft:wither_skeleton_spawn_egg", "minecraft:wither_skeleton")
      .put("minecraft:wolf_spawn_egg", "minecraft:wolf")
      .put("minecraft:zoglin_spawn_egg", "minecraft:zoglin")
      .put("minecraft:zombie_spawn_egg", "minecraft:zombie")
      .put("minecraft:zombie_horse_spawn_egg", "minecraft:zombie_horse")
      .put("minecraft:zombie_villager_spawn_egg", "minecraft:zombie_villager")
      .put("minecraft:zombified_piglin_spawn_egg", "minecraft:zombified_piglin")
      .put("minecraft:item_frame", "minecraft:item_frame")
      .put("minecraft:boat", "minecraft:oak_boat")
      .put("minecraft:oak_boat", "minecraft:oak_boat")
      .put("minecraft:oak_chest_boat", "minecraft:oak_chest_boat")
      .put("minecraft:spruce_boat", "minecraft:spruce_boat")
      .put("minecraft:spruce_chest_boat", "minecraft:spruce_chest_boat")
      .put("minecraft:birch_boat", "minecraft:birch_boat")
      .put("minecraft:birch_chest_boat", "minecraft:birch_chest_boat")
      .put("minecraft:jungle_boat", "minecraft:jungle_boat")
      .put("minecraft:jungle_chest_boat", "minecraft:jungle_chest_boat")
      .put("minecraft:acacia_boat", "minecraft:acacia_boat")
      .put("minecraft:acacia_chest_boat", "minecraft:acacia_chest_boat")
      .put("minecraft:cherry_boat", "minecraft:cherry_boat")
      .put("minecraft:cherry_chest_boat", "minecraft:cherry_chest_boat")
      .put("minecraft:dark_oak_boat", "minecraft:dark_oak_boat")
      .put("minecraft:dark_oak_chest_boat", "minecraft:dark_oak_chest_boat")
      .put("minecraft:mangrove_boat", "minecraft:mangrove_boat")
      .put("minecraft:mangrove_chest_boat", "minecraft:mangrove_chest_boat")
      .put("minecraft:bamboo_raft", "minecraft:bamboo_raft")
      .put("minecraft:bamboo_chest_raft", "minecraft:bamboo_chest_raft")
      .put("minecraft:minecart", "minecraft:minecart")
      .put("minecraft:chest_minecart", "minecraft:chest_minecart")
      .put("minecraft:furnace_minecart", "minecraft:furnace_minecart")
      .put("minecraft:tnt_minecart", "minecraft:tnt_minecart")
      .put("minecraft:hopper_minecart", "minecraft:hopper_minecart")
      .build();
   protected static final HookFunction ADD_NAMES = new HookFunction() {
      public <T> T apply(DynamicOps<T> $$0, T $$1) {
         return V99.addNames(new Dynamic($$0, $$1), V704.ITEM_TO_BLOCKENTITY, V705.ITEM_TO_ENTITY);
      }
   };

   public V705(int $$0, Schema $$1) {
      super($$0, $$1);
   }

   protected static void registerMob(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.registerSimple($$1, $$2);
   }

   protected static void registerThrowableProjectile(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, String $$2) {
      $$0.register($$1, $$2, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema $$0) {
      Map<String, Supplier<TypeTemplate>> $$1 = Maps.newHashMap();
      $$0.register($$1, "minecraft:area_effect_cloud", $$1x -> DSL.optionalFields("Particle", References.PARTICLE.in($$0)));
      registerMob($$0, $$1, "minecraft:armor_stand");
      $$0.register($$1, "minecraft:arrow", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:bat");
      registerMob($$0, $$1, "minecraft:blaze");
      $$0.registerSimple($$1, "minecraft:boat");
      registerMob($$0, $$1, "minecraft:cave_spider");
      $$0.register(
         $$1,
         "minecraft:chest_minecart",
         $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      registerMob($$0, $$1, "minecraft:chicken");
      $$0.register(
         $$1,
         "minecraft:commandblock_minecart",
         $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "LastOutput", References.TEXT_COMPONENT.in($$0))
      );
      registerMob($$0, $$1, "minecraft:cow");
      registerMob($$0, $$1, "minecraft:creeper");
      $$0.register(
         $$1, "minecraft:donkey", $$1x -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0))
      );
      $$0.registerSimple($$1, "minecraft:dragon_fireball");
      registerThrowableProjectile($$0, $$1, "minecraft:egg");
      registerMob($$0, $$1, "minecraft:elder_guardian");
      $$0.registerSimple($$1, "minecraft:ender_crystal");
      registerMob($$0, $$1, "minecraft:ender_dragon");
      $$0.register($$1, "minecraft:enderman", $$1x -> DSL.optionalFields("carried", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:endermite");
      registerThrowableProjectile($$0, $$1, "minecraft:ender_pearl");
      $$0.registerSimple($$1, "minecraft:eye_of_ender_signal");
      $$0.register(
         $$1, "minecraft:falling_block", $$1x -> DSL.optionalFields("Block", References.BLOCK_NAME.in($$0), "TileEntityData", References.BLOCK_ENTITY.in($$0))
      );
      registerThrowableProjectile($$0, $$1, "minecraft:fireball");
      $$0.register($$1, "minecraft:fireworks_rocket", $$1x -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in($$0)));
      $$0.register($$1, "minecraft:furnace_minecart", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:ghast");
      registerMob($$0, $$1, "minecraft:giant");
      registerMob($$0, $$1, "minecraft:guardian");
      $$0.register(
         $$1,
         "minecraft:hopper_minecart",
         $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), "Items", DSL.list(References.ITEM_STACK.in($$0)))
      );
      $$0.register($$1, "minecraft:horse", $$1x -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in($$0), "SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:husk");
      $$0.register($$1, "minecraft:item", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      $$0.register($$1, "minecraft:item_frame", $$1x -> DSL.optionalFields("Item", References.ITEM_STACK.in($$0)));
      $$0.registerSimple($$1, "minecraft:leash_knot");
      registerMob($$0, $$1, "minecraft:magma_cube");
      $$0.register($$1, "minecraft:minecart", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:mooshroom");
      $$0.register(
         $$1, "minecraft:mule", $$1x -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0))
      );
      registerMob($$0, $$1, "minecraft:ocelot");
      $$0.registerSimple($$1, "minecraft:painting");
      registerMob($$0, $$1, "minecraft:parrot");
      registerMob($$0, $$1, "minecraft:pig");
      registerMob($$0, $$1, "minecraft:polar_bear");
      $$0.register($$1, "minecraft:potion", $$1x -> DSL.optionalFields("Potion", References.ITEM_STACK.in($$0), "inTile", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:rabbit");
      registerMob($$0, $$1, "minecraft:sheep");
      registerMob($$0, $$1, "minecraft:shulker");
      $$0.registerSimple($$1, "minecraft:shulker_bullet");
      registerMob($$0, $$1, "minecraft:silverfish");
      registerMob($$0, $$1, "minecraft:skeleton");
      $$0.register($$1, "minecraft:skeleton_horse", $$1x -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:slime");
      registerThrowableProjectile($$0, $$1, "minecraft:small_fireball");
      registerThrowableProjectile($$0, $$1, "minecraft:snowball");
      registerMob($$0, $$1, "minecraft:snowman");
      $$0.register(
         $$1, "minecraft:spawner_minecart", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0), References.UNTAGGED_SPAWNER.in($$0))
      );
      $$0.register($$1, "minecraft:spectral_arrow", $$1x -> DSL.optionalFields("inTile", References.BLOCK_NAME.in($$0)));
      registerMob($$0, $$1, "minecraft:spider");
      registerMob($$0, $$1, "minecraft:squid");
      registerMob($$0, $$1, "minecraft:stray");
      $$0.registerSimple($$1, "minecraft:tnt");
      $$0.register($$1, "minecraft:tnt_minecart", $$1x -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in($$0)));
      $$0.register(
         $$1,
         "minecraft:villager",
         $$1x -> DSL.optionalFields(
            "Inventory", DSL.list(References.ITEM_STACK.in($$0)), "Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in($$0)))
         )
      );
      registerMob($$0, $$1, "minecraft:villager_golem");
      registerMob($$0, $$1, "minecraft:witch");
      registerMob($$0, $$1, "minecraft:wither");
      registerMob($$0, $$1, "minecraft:wither_skeleton");
      registerThrowableProjectile($$0, $$1, "minecraft:wither_skull");
      registerMob($$0, $$1, "minecraft:wolf");
      registerThrowableProjectile($$0, $$1, "minecraft:xp_bottle");
      $$0.registerSimple($$1, "minecraft:xp_orb");
      registerMob($$0, $$1, "minecraft:zombie");
      $$0.register($$1, "minecraft:zombie_horse", $$1x -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in($$0)));
      registerMob($$0, $$1, "minecraft:zombie_pigman");
      $$0.register(
         $$1, "minecraft:zombie_villager", $$1x -> DSL.optionalFields("Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in($$0))))
      );
      $$0.registerSimple($$1, "minecraft:evocation_fangs");
      registerMob($$0, $$1, "minecraft:evocation_illager");
      registerMob($$0, $$1, "minecraft:illusion_illager");
      $$0.register(
         $$1,
         "minecraft:llama",
         $$1x -> DSL.optionalFields(
            "Items", DSL.list(References.ITEM_STACK.in($$0)), "SaddleItem", References.ITEM_STACK.in($$0), "DecorItem", References.ITEM_STACK.in($$0)
         )
      );
      $$0.registerSimple($$1, "minecraft:llama_spit");
      registerMob($$0, $$1, "minecraft:vex");
      registerMob($$0, $$1, "minecraft:vindication_illager");
      return $$1;
   }

   public void registerTypes(Schema $$0, Map<String, Supplier<TypeTemplate>> $$1, Map<String, Supplier<TypeTemplate>> $$2) {
      super.registerTypes($$0, $$1, $$2);
      $$0.registerType(
         true,
         References.ENTITY,
         () -> DSL.and(
            References.ENTITY_EQUIPMENT.in($$0),
            DSL.optionalFields("CustomName", DSL.constType(DSL.string()), DSL.taggedChoiceLazy("id", namespacedString(), $$1))
         )
      );
      $$0.registerType(
         true,
         References.ITEM_STACK,
         () -> DSL.hook(DSL.optionalFields("id", References.ITEM_NAME.in($$0), "tag", V99.itemStackTag($$0)), ADD_NAMES, HookFunction.IDENTITY)
      );
   }
}
