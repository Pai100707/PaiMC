package net.minecraft.data.advancements.packs;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList.Preset;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;

public class VanillaNetherAdvancements implements AdvancementSubProvider {
   @Override
   public void generate(Provider $$0, Consumer<AdvancementHolder> $$1) {
      HolderGetter<EntityType<?>> $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
      HolderGetter<Item> $$3 = $$0.lookupOrThrow(Registries.ITEM);
      HolderGetter<Block> $$4 = $$0.lookupOrThrow(Registries.BLOCK);
      AdvancementHolder $$5 = Builder.advancement()
         .display(
            Blocks.RED_NETHER_BRICKS,
            Component.translatable("advancements.nether.root.title"),
            Component.translatable("advancements.nether.root.description"),
            Identifier.withDefaultNamespace("gui/advancements/backgrounds/nether"),
            AdvancementType.TASK,
            false,
            false,
            false
         )
         .addCriterion("entered_nether", TriggerInstance.changedDimensionTo(Level.NETHER))
         .save($$1, "nether/root");
      AdvancementHolder $$6 = Builder.advancement()
         .parent($$5)
         .display(
            Items.FIRE_CHARGE,
            Component.translatable("advancements.nether.return_to_sender.title"),
            Component.translatable("advancements.nether.return_to_sender.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .addCriterion(
            "killed_ghast",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.GHAST),
               net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType()
                  .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                  .direct(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.FIREBALL))
            )
         )
         .save($$1, "nether/return_to_sender");
      AdvancementHolder $$7 = Builder.advancement()
         .parent($$5)
         .display(
            Blocks.NETHER_BRICKS,
            Component.translatable("advancements.nether.find_fortress.title"),
            Component.translatable("advancements.nether.find_fortress.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "fortress",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inStructure(
                  $$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.FORTRESS)
               )
            )
         )
         .save($$1, "nether/find_fortress");
      Builder.advancement()
         .parent($$5)
         .display(
            Items.MAP,
            Component.translatable("advancements.nether.fast_travel.title"),
            Component.translatable("advancements.nether.fast_travel.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion(
            "travelled",
            net.minecraft.advancements.criterion.DistanceTrigger.TriggerInstance.travelledThroughNether(DistancePredicate.horizontal(Doubles.atLeast(7000.0)))
         )
         .save($$1, "nether/fast_travel");
      Builder.advancement()
         .parent($$6)
         .display(
            Items.GHAST_TEAR,
            Component.translatable("advancements.nether.uneasy_alliance.title"),
            Component.translatable("advancements.nether.uneasy_alliance.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion(
            "killed_ghast",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                  .of($$2, EntityType.GHAST)
                  .located(net.minecraft.advancements.criterion.LocationPredicate.Builder.inDimension(Level.OVERWORLD))
            )
         )
         .save($$1, "nether/uneasy_alliance");
      AdvancementHolder $$8 = Builder.advancement()
         .parent($$7)
         .display(
            Blocks.WITHER_SKELETON_SKULL,
            Component.translatable("advancements.nether.get_wither_skull.title"),
            Component.translatable("advancements.nether.get_wither_skull.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "wither_skull", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Blocks.WITHER_SKELETON_SKULL})
         )
         .save($$1, "nether/get_wither_skull");
      AdvancementHolder $$9 = Builder.advancement()
         .parent($$8)
         .display(
            Items.NETHER_STAR,
            Component.translatable("advancements.nether.summon_wither.title"),
            Component.translatable("advancements.nether.summon_wither.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "summoned",
            net.minecraft.advancements.criterion.SummonedEntityTrigger.TriggerInstance.summonedEntity(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.WITHER)
            )
         )
         .save($$1, "nether/summon_wither");
      AdvancementHolder $$10 = Builder.advancement()
         .parent($$7)
         .display(
            Items.BLAZE_ROD,
            Component.translatable("advancements.nether.obtain_blaze_rod.title"),
            Component.translatable("advancements.nether.obtain_blaze_rod.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("blaze_rod", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.BLAZE_ROD}))
         .save($$1, "nether/obtain_blaze_rod");
      AdvancementHolder $$11 = Builder.advancement()
         .parent($$9)
         .display(
            Blocks.BEACON,
            Component.translatable("advancements.nether.create_beacon.title"),
            Component.translatable("advancements.nether.create_beacon.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("beacon", net.minecraft.advancements.criterion.ConstructBeaconTrigger.TriggerInstance.constructedBeacon(Ints.atLeast(1)))
         .save($$1, "nether/create_beacon");
      Builder.advancement()
         .parent($$11)
         .display(
            Blocks.BEACON,
            Component.translatable("advancements.nether.create_full_beacon.title"),
            Component.translatable("advancements.nether.create_full_beacon.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("beacon", net.minecraft.advancements.criterion.ConstructBeaconTrigger.TriggerInstance.constructedBeacon(Ints.exactly(4)))
         .save($$1, "nether/create_full_beacon");
      AdvancementHolder $$12 = Builder.advancement()
         .parent($$10)
         .display(
            Items.POTION,
            Component.translatable("advancements.nether.brew_potion.title"),
            Component.translatable("advancements.nether.brew_potion.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("potion", net.minecraft.advancements.criterion.BrewedPotionTrigger.TriggerInstance.brewedPotion())
         .save($$1, "nether/brew_potion");
      AdvancementHolder $$13 = Builder.advancement()
         .parent($$12)
         .display(
            Items.MILK_BUCKET,
            Component.translatable("advancements.nether.all_potions.title"),
            Component.translatable("advancements.nether.all_potions.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion(
            "all_effects",
            net.minecraft.advancements.criterion.EffectsChangedTrigger.TriggerInstance.hasEffects(
               net.minecraft.advancements.criterion.MobEffectsPredicate.Builder.effects()
                  .and(MobEffects.SPEED)
                  .and(MobEffects.SLOWNESS)
                  .and(MobEffects.STRENGTH)
                  .and(MobEffects.JUMP_BOOST)
                  .and(MobEffects.REGENERATION)
                  .and(MobEffects.FIRE_RESISTANCE)
                  .and(MobEffects.WATER_BREATHING)
                  .and(MobEffects.INVISIBILITY)
                  .and(MobEffects.NIGHT_VISION)
                  .and(MobEffects.WEAKNESS)
                  .and(MobEffects.POISON)
                  .and(MobEffects.SLOW_FALLING)
                  .and(MobEffects.RESISTANCE)
                  .and(MobEffects.OOZING)
                  .and(MobEffects.INFESTED)
                  .and(MobEffects.WIND_CHARGED)
                  .and(MobEffects.WEAVING)
            )
         )
         .save($$1, "nether/all_potions");
      Builder.advancement()
         .parent($$13)
         .display(
            Items.BUCKET,
            Component.translatable("advancements.nether.all_effects.title"),
            Component.translatable("advancements.nether.all_effects.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            true
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(1000))
         .addCriterion(
            "all_effects",
            net.minecraft.advancements.criterion.EffectsChangedTrigger.TriggerInstance.hasEffects(
               net.minecraft.advancements.criterion.MobEffectsPredicate.Builder.effects()
                  .and(MobEffects.SPEED)
                  .and(MobEffects.SLOWNESS)
                  .and(MobEffects.STRENGTH)
                  .and(MobEffects.JUMP_BOOST)
                  .and(MobEffects.REGENERATION)
                  .and(MobEffects.FIRE_RESISTANCE)
                  .and(MobEffects.WATER_BREATHING)
                  .and(MobEffects.INVISIBILITY)
                  .and(MobEffects.NIGHT_VISION)
                  .and(MobEffects.WEAKNESS)
                  .and(MobEffects.POISON)
                  .and(MobEffects.WITHER)
                  .and(MobEffects.HASTE)
                  .and(MobEffects.MINING_FATIGUE)
                  .and(MobEffects.LEVITATION)
                  .and(MobEffects.GLOWING)
                  .and(MobEffects.ABSORPTION)
                  .and(MobEffects.HUNGER)
                  .and(MobEffects.NAUSEA)
                  .and(MobEffects.RESISTANCE)
                  .and(MobEffects.SLOW_FALLING)
                  .and(MobEffects.CONDUIT_POWER)
                  .and(MobEffects.DOLPHINS_GRACE)
                  .and(MobEffects.BLINDNESS)
                  .and(MobEffects.BAD_OMEN)
                  .and(MobEffects.HERO_OF_THE_VILLAGE)
                  .and(MobEffects.DARKNESS)
                  .and(MobEffects.OOZING)
                  .and(MobEffects.INFESTED)
                  .and(MobEffects.WIND_CHARGED)
                  .and(MobEffects.WEAVING)
                  .and(MobEffects.TRIAL_OMEN)
                  .and(MobEffects.RAID_OMEN)
                  .and(MobEffects.BREATH_OF_THE_NAUTILUS)
            )
         )
         .save($$1, "nether/all_effects");
      AdvancementHolder $$14 = Builder.advancement()
         .parent($$5)
         .display(
            Items.ANCIENT_DEBRIS,
            Component.translatable("advancements.nether.obtain_ancient_debris.title"),
            Component.translatable("advancements.nether.obtain_ancient_debris.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "ancient_debris", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.ANCIENT_DEBRIS})
         )
         .save($$1, "nether/obtain_ancient_debris");
      Builder.advancement()
         .parent($$14)
         .display(
            Items.NETHERITE_CHESTPLATE,
            Component.translatable("advancements.nether.netherite_armor.title"),
            Component.translatable("advancements.nether.netherite_armor.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion(
            "netherite_armor",
            net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(
               new ItemLike[]{Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS}
            )
         )
         .save($$1, "nether/netherite_armor");
      AdvancementHolder $$15 = Builder.advancement()
         .parent($$5)
         .display(
            Items.CRYING_OBSIDIAN,
            Component.translatable("advancements.nether.obtain_crying_obsidian.title"),
            Component.translatable("advancements.nether.obtain_crying_obsidian.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "crying_obsidian", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.CRYING_OBSIDIAN})
         )
         .save($$1, "nether/obtain_crying_obsidian");
      Builder.advancement()
         .parent($$15)
         .display(
            Items.RESPAWN_ANCHOR,
            Component.translatable("advancements.nether.charge_respawn_anchor.title"),
            Component.translatable("advancements.nether.charge_respawn_anchor.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "charge_respawn_anchor",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(
                     net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                        .of($$4, new Block[]{Blocks.RESPAWN_ANCHOR})
                        .setProperties(
                           net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(RespawnAnchorBlock.CHARGE, 4)
                        )
                  ),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Blocks.GLOWSTONE})
            )
         )
         .save($$1, "nether/charge_respawn_anchor");
      AdvancementHolder $$16 = Builder.advancement()
         .parent($$5)
         .display(
            Items.WARPED_FUNGUS_ON_A_STICK,
            Component.translatable("advancements.nether.ride_strider.title"),
            Component.translatable("advancements.nether.ride_strider.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "used_warped_fungus_on_a_stick",
            net.minecraft.advancements.criterion.ItemDurabilityTrigger.TriggerInstance.changedDurability(
               Optional.of(
                  EntityPredicate.wrap(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .vehicle(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.STRIDER))
                  )
               ),
               Optional.of(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.WARPED_FUNGUS_ON_A_STICK}).build()),
               Ints.ANY
            )
         )
         .save($$1, "nether/ride_strider");
      Builder.advancement()
         .parent($$16)
         .display(
            Items.WARPED_FUNGUS_ON_A_STICK,
            Component.translatable("advancements.nether.ride_strider_in_overworld_lava.title"),
            Component.translatable("advancements.nether.ride_strider_in_overworld_lava.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "ride_entity_distance",
            net.minecraft.advancements.criterion.DistanceTrigger.TriggerInstance.rideEntityInLava(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                  .located(net.minecraft.advancements.criterion.LocationPredicate.Builder.inDimension(Level.OVERWORLD))
                  .vehicle(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.STRIDER)),
               DistancePredicate.horizontal(Doubles.atLeast(50.0))
            )
         )
         .save($$1, "nether/ride_strider_in_overworld_lava");
      VanillaAdventureAdvancements.addBiomes(Builder.advancement(), $$0, Preset.NETHER.usedBiomes().toList())
         .parent($$16)
         .display(
            Items.NETHERITE_BOOTS,
            Component.translatable("advancements.nether.explore_nether.title"),
            Component.translatable("advancements.nether.explore_nether.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(500))
         .save($$1, "nether/explore_nether");
      AdvancementHolder $$17 = Builder.advancement()
         .parent($$5)
         .display(
            Items.POLISHED_BLACKSTONE_BRICKS,
            Component.translatable("advancements.nether.find_bastion.title"),
            Component.translatable("advancements.nether.find_bastion.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "bastion",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inStructure(
                  $$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.BASTION_REMNANT)
               )
            )
         )
         .save($$1, "nether/find_bastion");
      Builder.advancement()
         .parent($$17)
         .display(
            Blocks.CHEST,
            Component.translatable("advancements.nether.loot_bastion.title"),
            Component.translatable("advancements.nether.loot_bastion.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion(
            "loot_bastion_other", net.minecraft.advancements.criterion.LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.BASTION_OTHER)
         )
         .addCriterion(
            "loot_bastion_treasure", net.minecraft.advancements.criterion.LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.BASTION_TREASURE)
         )
         .addCriterion(
            "loot_bastion_hoglin_stable",
            net.minecraft.advancements.criterion.LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.BASTION_HOGLIN_STABLE)
         )
         .addCriterion(
            "loot_bastion_bridge", net.minecraft.advancements.criterion.LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.BASTION_BRIDGE)
         )
         .save($$1, "nether/loot_bastion");
      ContextAwarePredicate $$18 = ContextAwarePredicate.create(
         new LootItemCondition[]{
            LootItemEntityPropertyCondition.hasProperties(
                  EntityTarget.THIS,
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .equipment(
                        net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                           .head(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.PIGLIN_SAFE_ARMOR))
                     )
               )
               .invert()
               .build(),
            LootItemEntityPropertyCondition.hasProperties(
                  EntityTarget.THIS,
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .equipment(
                        net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                           .chest(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.PIGLIN_SAFE_ARMOR))
                     )
               )
               .invert()
               .build(),
            LootItemEntityPropertyCondition.hasProperties(
                  EntityTarget.THIS,
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .equipment(
                        net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                           .legs(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.PIGLIN_SAFE_ARMOR))
                     )
               )
               .invert()
               .build(),
            LootItemEntityPropertyCondition.hasProperties(
                  EntityTarget.THIS,
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .equipment(
                        net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                           .feet(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.PIGLIN_SAFE_ARMOR))
                     )
               )
               .invert()
               .build()
         }
      );
      Builder.advancement()
         .parent($$5)
         .requirements(Strategy.OR)
         .display(
            Items.GOLD_INGOT,
            Component.translatable("advancements.nether.distract_piglin.title"),
            Component.translatable("advancements.nether.distract_piglin.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "distract_piglin",
            net.minecraft.advancements.criterion.PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByEntity(
               $$18,
               Optional.of(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.PIGLIN_LOVED).build()),
               Optional.of(
                  EntityPredicate.wrap(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .of($$2, EntityType.PIGLIN)
                        .flags(net.minecraft.advancements.criterion.EntityFlagsPredicate.Builder.flags().setIsBaby(false))
                  )
               )
            )
         )
         .addCriterion(
            "distract_piglin_directly",
            net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
               Optional.of($$18),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{PiglinAi.BARTERING_ITEM}),
               Optional.of(
                  EntityPredicate.wrap(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .of($$2, EntityType.PIGLIN)
                        .flags(net.minecraft.advancements.criterion.EntityFlagsPredicate.Builder.flags().setIsBaby(false))
                  )
               )
            )
         )
         .save($$1, "nether/distract_piglin");
   }
}
