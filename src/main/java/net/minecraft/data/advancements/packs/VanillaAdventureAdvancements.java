package net.minecraft.data.advancements.packs;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LightningBoltPredicate;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.advancements.criterion.EntityPredicate.Builder;
import net.minecraft.advancements.criterion.LootTableTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.JukeboxPlayablePredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList.Preset;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import org.slf4j.Logger;

public class VanillaAdventureAdvancements implements AdvancementSubProvider {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int DISTANCE_FROM_BOTTOM_TO_TOP = 384;
   private static final int Y_COORDINATE_AT_TOP = 320;
   private static final int Y_COORDINATE_AT_BOTTOM = -64;
   private static final int BEDROCK_THICKNESS = 5;
   private static final Map<MobCategory, Set<EntityType<?>>> EXCEPTIONS_BY_EXPECTED_CATEGORIES = Map.of(
      MobCategory.MONSTER, Set.of(EntityType.GIANT, EntityType.ILLUSIONER, EntityType.WARDEN)
   );
   private static final List<EntityType<?>> MOBS_TO_KILL = Arrays.asList(
      EntityType.BLAZE,
      EntityType.BOGGED,
      EntityType.BREEZE,
      EntityType.CAMEL_HUSK,
      EntityType.CAVE_SPIDER,
      EntityType.CREAKING,
      EntityType.CREEPER,
      EntityType.DROWNED,
      EntityType.ELDER_GUARDIAN,
      EntityType.ENDER_DRAGON,
      EntityType.ENDERMAN,
      EntityType.ENDERMITE,
      EntityType.EVOKER,
      EntityType.GHAST,
      EntityType.GUARDIAN,
      EntityType.HOGLIN,
      EntityType.HUSK,
      EntityType.MAGMA_CUBE,
      EntityType.PARCHED,
      EntityType.PHANTOM,
      EntityType.PIGLIN,
      EntityType.PIGLIN_BRUTE,
      EntityType.PILLAGER,
      EntityType.RAVAGER,
      EntityType.SHULKER,
      EntityType.SILVERFISH,
      EntityType.SKELETON,
      EntityType.SLIME,
      EntityType.SPIDER,
      EntityType.STRAY,
      EntityType.VEX,
      EntityType.VINDICATOR,
      EntityType.WITCH,
      EntityType.WITHER_SKELETON,
      EntityType.WITHER,
      EntityType.ZOGLIN,
      EntityType.ZOMBIE_VILLAGER,
      EntityType.ZOMBIE,
      EntityType.ZOMBIE_HORSE,
      EntityType.ZOMBIFIED_PIGLIN,
      EntityType.ZOMBIE_NAUTILUS
   );

   private static Criterion<net.minecraft.advancements.criterion.LightningStrikeTrigger.TriggerInstance> fireCountAndBystander(
      Ints $$0, Optional<EntityPredicate> $$1
   ) {
      return net.minecraft.advancements.criterion.LightningStrikeTrigger.TriggerInstance.lightningStrike(
         Optional.of(
            Builder.entity().distance(DistancePredicate.absolute(Doubles.atMost(30.0))).subPredicate(LightningBoltPredicate.blockSetOnFire($$0)).build()
         ),
         $$1
      );
   }

   private static Criterion<net.minecraft.advancements.criterion.UsingItemTrigger.TriggerInstance> lookAtThroughItem(
      Builder $$0, net.minecraft.advancements.criterion.ItemPredicate.Builder $$1
   ) {
      return net.minecraft.advancements.criterion.UsingItemTrigger.TriggerInstance.lookingAt(
         Builder.entity().subPredicate(net.minecraft.advancements.criterion.PlayerPredicate.Builder.player().setLookingAt($$0).build()), $$1
      );
   }

   @Override
   public void generate(Provider $$0, Consumer<AdvancementHolder> $$1) {
      HolderLookup<EntityType<?>> $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
      HolderLookup<Item> $$3 = $$0.lookupOrThrow(Registries.ITEM);
      HolderLookup<Block> $$4 = $$0.lookupOrThrow(Registries.BLOCK);
      AdvancementHolder $$5 = net.minecraft.advancements.Advancement.Builder.advancement()
         .display(
            Items.MAP,
            Component.translatable("advancements.adventure.root.title"),
            Component.translatable("advancements.adventure.root.description"),
            Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"),
            AdvancementType.TASK,
            false,
            false,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("killed_something", net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity())
         .addCriterion("killed_by_something", net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.entityKilledPlayer())
         .save($$1, "adventure/root");
      AdvancementHolder $$6 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Blocks.RED_BED,
            Component.translatable("advancements.adventure.sleep_in_bed.title"),
            Component.translatable("advancements.adventure.sleep_in_bed.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("slept_in_bed", net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.sleptInBed())
         .save($$1, "adventure/sleep_in_bed");
      createAdventuringTime($$0, $$1, $$6, Preset.OVERWORLD);
      AdvancementHolder $$7 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.EMERALD,
            Component.translatable("advancements.adventure.trade.title"),
            Component.translatable("advancements.adventure.trade.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("traded", net.minecraft.advancements.criterion.TradeTrigger.TriggerInstance.tradedWithVillager())
         .save($$1, "adventure/trade");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$7)
         .display(
            Items.EMERALD,
            Component.translatable("advancements.adventure.trade_at_world_height.title"),
            Component.translatable("advancements.adventure.trade_at_world_height.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "trade_at_world_height",
            net.minecraft.advancements.criterion.TradeTrigger.TriggerInstance.tradedWithVillager(
               Builder.entity().located(net.minecraft.advancements.criterion.LocationPredicate.Builder.atYLocation(Doubles.atLeast(319.0)))
            )
         )
         .save($$1, "adventure/trade_at_world_height");
      AdvancementHolder $$8 = createMonsterHunterAdvancement($$5, $$1, $$2, validateMobsToKill(MOBS_TO_KILL, $$2));
      AdvancementHolder $$9 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$8)
         .display(
            Items.BOW,
            Component.translatable("advancements.adventure.shoot_arrow.title"),
            Component.translatable("advancements.adventure.shoot_arrow.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "shot_arrow",
            net.minecraft.advancements.criterion.PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(
               net.minecraft.advancements.criterion.DamagePredicate.Builder.damageInstance()
                  .type(
                     net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                        .direct(Builder.entity().of($$2, EntityTypeTags.ARROWS))
                  )
            )
         )
         .save($$1, "adventure/shoot_arrow");
      AdvancementHolder $$10 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$8)
         .display(
            Items.TRIDENT,
            Component.translatable("advancements.adventure.throw_trident.title"),
            Component.translatable("advancements.adventure.throw_trident.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "shot_trident",
            net.minecraft.advancements.criterion.PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(
               net.minecraft.advancements.criterion.DamagePredicate.Builder.damageInstance()
                  .type(
                     net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                        .direct(Builder.entity().of($$2, EntityType.TRIDENT))
                  )
            )
         )
         .save($$1, "adventure/throw_trident");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$10)
         .display(
            Items.TRIDENT,
            Component.translatable("advancements.adventure.very_very_frightening.title"),
            Component.translatable("advancements.adventure.very_very_frightening.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "struck_villager",
            net.minecraft.advancements.criterion.ChanneledLightningTrigger.TriggerInstance.channeledLightning(
               new Builder[]{Builder.entity().of($$2, EntityType.VILLAGER)}
            )
         )
         .save($$1, "adventure/very_very_frightening");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$7)
         .display(
            Blocks.CARVED_PUMPKIN,
            Component.translatable("advancements.adventure.summon_iron_golem.title"),
            Component.translatable("advancements.adventure.summon_iron_golem.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion(
            "summoned_golem",
            net.minecraft.advancements.criterion.SummonedEntityTrigger.TriggerInstance.summonedEntity(Builder.entity().of($$2, EntityType.IRON_GOLEM))
         )
         .save($$1, "adventure/summon_iron_golem");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$9)
         .display(
            Items.ARROW,
            Component.translatable("advancements.adventure.sniper_duel.title"),
            Component.translatable("advancements.adventure.sniper_duel.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .addCriterion(
            "killed_skeleton",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               Builder.entity().of($$2, EntityType.SKELETON).distance(DistancePredicate.horizontal(Doubles.atLeast(50.0))),
               net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
            )
         )
         .save($$1, "adventure/sniper_duel");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$8)
         .display(
            Items.TOTEM_OF_UNDYING,
            Component.translatable("advancements.adventure.totem_of_undying.title"),
            Component.translatable("advancements.adventure.totem_of_undying.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("used_totem", net.minecraft.advancements.criterion.UsedTotemTrigger.TriggerInstance.usedTotem($$3, Items.TOTEM_OF_UNDYING))
         .save($$1, "adventure/totem_of_undying");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$8)
         .display(
            Items.IRON_SPEAR,
            Component.translatable("advancements.adventure.spear_many_mobs.title"),
            Component.translatable("advancements.adventure.spear_many_mobs.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("spear_many_mobs", net.minecraft.advancements.criterion.SpearMobsTrigger.TriggerInstance.spearMobs(5))
         .save($$1, "adventure/spear_many_mobs");
      AdvancementHolder $$11 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.CROSSBOW,
            Component.translatable("advancements.adventure.ol_betsy.title"),
            Component.translatable("advancements.adventure.ol_betsy.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("shot_crossbow", net.minecraft.advancements.criterion.ShotCrossbowTrigger.TriggerInstance.shotCrossbow($$3, Items.CROSSBOW))
         .save($$1, "adventure/ol_betsy");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$11)
         .display(
            Items.CROSSBOW,
            Component.translatable("advancements.adventure.whos_the_pillager_now.title"),
            Component.translatable("advancements.adventure.whos_the_pillager_now.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "kill_pillager",
            net.minecraft.advancements.criterion.KilledByArrowTrigger.TriggerInstance.crossbowKilled(
               $$3, new Builder[]{Builder.entity().of($$2, EntityType.PILLAGER)}
            )
         )
         .save($$1, "adventure/whos_the_pillager_now");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$11)
         .display(
            Items.CROSSBOW,
            Component.translatable("advancements.adventure.two_birds_one_arrow.title"),
            Component.translatable("advancements.adventure.two_birds_one_arrow.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(65))
         .addCriterion(
            "two_birds",
            net.minecraft.advancements.criterion.KilledByArrowTrigger.TriggerInstance.crossbowKilled(
               $$3, new Builder[]{Builder.entity().of($$2, EntityType.PHANTOM), Builder.entity().of($$2, EntityType.PHANTOM)}
            )
         )
         .save($$1, "adventure/two_birds_one_arrow");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$11)
         .display(
            Items.CROSSBOW,
            Component.translatable("advancements.adventure.arbalistic.title"),
            Component.translatable("advancements.adventure.arbalistic.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            true
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(85))
         .addCriterion("arbalistic", net.minecraft.advancements.criterion.KilledByArrowTrigger.TriggerInstance.crossbowKilled($$3, Ints.exactly(5)))
         .save($$1, "adventure/arbalistic");
      RegistryLookup<BannerPattern> $$12 = $$0.lookupOrThrow(Registries.BANNER_PATTERN);
      AdvancementHolder $$13 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Raid.getOminousBannerInstance($$12),
            Component.translatable("advancements.adventure.voluntary_exile.title"),
            Component.translatable("advancements.adventure.voluntary_exile.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .addCriterion(
            "voluntary_exile",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               Builder.entity().of($$2, EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.captainPredicate($$3, $$12))
            )
         )
         .save($$1, "adventure/voluntary_exile");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$13)
         .display(
            Raid.getOminousBannerInstance($$12),
            Component.translatable("advancements.adventure.hero_of_the_village.title"),
            Component.translatable("advancements.adventure.hero_of_the_village.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            true
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion("hero_of_the_village", net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.raidWon())
         .save($$1, "adventure/hero_of_the_village");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Blocks.HONEY_BLOCK.asItem(),
            Component.translatable("advancements.adventure.honey_block_slide.title"),
            Component.translatable("advancements.adventure.honey_block_slide.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("honey_block_slide", net.minecraft.advancements.criterion.SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK))
         .save($$1, "adventure/honey_block_slide");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$9)
         .display(
            Blocks.TARGET.asItem(),
            Component.translatable("advancements.adventure.bullseye.title"),
            Component.translatable("advancements.adventure.bullseye.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .addCriterion(
            "bullseye",
            net.minecraft.advancements.criterion.TargetBlockTrigger.TriggerInstance.targetHit(
               Ints.exactly(15), Optional.of(EntityPredicate.wrap(Builder.entity().distance(DistancePredicate.horizontal(Doubles.atLeast(30.0)))))
            )
         )
         .save($$1, "adventure/bullseye");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$6)
         .display(
            Items.LEATHER_BOOTS,
            Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"),
            Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "walk_on_powder_snow_with_leather_boots",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.walkOnBlockWithEquipment($$4, $$3, Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)
         )
         .save($$1, "adventure/walk_on_powder_snow_with_leather_boots");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.LIGHTNING_ROD,
            Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"),
            Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "lightning_rod_with_villager_no_fire", fireCountAndBystander(Ints.exactly(0), Optional.of(Builder.entity().of($$2, EntityType.VILLAGER).build()))
         )
         .save($$1, "adventure/lightning_rod_with_villager_no_fire");
      AdvancementHolder $$14 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.SPYGLASS,
            Component.translatable("advancements.adventure.spyglass_at_parrot.title"),
            Component.translatable("advancements.adventure.spyglass_at_parrot.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "spyglass_at_parrot",
            lookAtThroughItem(
               Builder.entity().of($$2, EntityType.PARROT),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.SPYGLASS})
            )
         )
         .save($$1, "adventure/spyglass_at_parrot");
      AdvancementHolder $$15 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$14)
         .display(
            Items.SPYGLASS,
            Component.translatable("advancements.adventure.spyglass_at_ghast.title"),
            Component.translatable("advancements.adventure.spyglass_at_ghast.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "spyglass_at_ghast",
            lookAtThroughItem(
               Builder.entity().of($$2, EntityType.GHAST),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.SPYGLASS})
            )
         )
         .save($$1, "adventure/spyglass_at_ghast");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$6)
         .display(
            Items.JUKEBOX,
            Component.translatable("advancements.adventure.play_jukebox_in_meadows.title"),
            Component.translatable("advancements.adventure.play_jukebox_in_meadows.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "play_jukebox_in_meadows",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBiomes(HolderSet.direct(new Holder[]{$$0.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.MEADOW)}))
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, new Block[]{Blocks.JUKEBOX})),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item()
                  .withComponents(
                     net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.JUKEBOX_PLAYABLE, JukeboxPlayablePredicate.any())
                        .build()
                  )
            )
         )
         .save($$1, "adventure/play_jukebox_in_meadows");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$15)
         .display(
            Items.SPYGLASS,
            Component.translatable("advancements.adventure.spyglass_at_dragon.title"),
            Component.translatable("advancements.adventure.spyglass_at_dragon.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "spyglass_at_dragon",
            lookAtThroughItem(
               Builder.entity().of($$2, EntityType.ENDER_DRAGON),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.SPYGLASS})
            )
         )
         .save($$1, "adventure/spyglass_at_dragon");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.WATER_BUCKET,
            Component.translatable("advancements.adventure.fall_from_world_height.title"),
            Component.translatable("advancements.adventure.fall_from_world_height.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "fall_from_world_height",
            net.minecraft.advancements.criterion.DistanceTrigger.TriggerInstance.fallFromHeight(
               Builder.entity().located(net.minecraft.advancements.criterion.LocationPredicate.Builder.atYLocation(Doubles.atMost(-59.0))),
               DistancePredicate.vertical(Doubles.atLeast(379.0)),
               net.minecraft.advancements.criterion.LocationPredicate.Builder.atYLocation(Doubles.atLeast(319.0))
            )
         )
         .save($$1, "adventure/fall_from_world_height");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$8)
         .display(
            Blocks.SCULK_CATALYST,
            Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"),
            Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .addCriterion("kill_mob_near_sculk_catalyst", net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntityNearSculkCatalyst())
         .save($$1, "adventure/kill_mob_near_sculk_catalyst");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Blocks.SCULK_SENSOR,
            Component.translatable("advancements.adventure.avoid_vibration.title"),
            Component.translatable("advancements.adventure.avoid_vibration.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("avoid_vibration", net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.avoidVibration())
         .save($$1, "adventure/avoid_vibration");
      AdvancementHolder $$16 = respectingTheRemnantsCriterions($$3, net.minecraft.advancements.Advancement.Builder.advancement())
         .parent($$5)
         .display(
            Items.BRUSH,
            Component.translatable("advancements.adventure.salvage_sherd.title"),
            Component.translatable("advancements.adventure.salvage_sherd.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "adventure/salvage_sherd");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$16)
         .display(
            DecoratedPotBlockEntity.createDecoratedPotItem(
               new PotDecorations(Optional.empty(), Optional.of(Items.HEART_POTTERY_SHERD), Optional.empty(), Optional.of(Items.EXPLORER_POTTERY_SHERD))
            ),
            Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"),
            Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "pot_crafted_using_only_sherds",
            net.minecraft.advancements.criterion.RecipeCraftedTrigger.TriggerInstance.craftedItem(
               ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("decorated_pot")),
               List.of(
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.DECORATED_POT_SHERDS),
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.DECORATED_POT_SHERDS),
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.DECORATED_POT_SHERDS),
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.DECORATED_POT_SHERDS)
               )
            )
         )
         .save($$1, "adventure/craft_decorated_pot_using_only_sherds");
      AdvancementHolder $$17 = craftingANewLook(net.minecraft.advancements.Advancement.Builder.advancement())
         .parent($$5)
         .display(
            new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE),
            Component.translatable("advancements.adventure.trim_with_any_armor_pattern.title"),
            Component.translatable("advancements.adventure.trim_with_any_armor_pattern.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "adventure/trim_with_any_armor_pattern");
      smithingWithStyle(net.minecraft.advancements.Advancement.Builder.advancement())
         .parent($$17)
         .display(
            new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE),
            Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"),
            Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(150))
         .save($$1, "adventure/trim_with_all_exclusive_armor_patterns");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.CHISELED_BOOKSHELF,
            Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.title"),
            Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("chiseled_bookshelf", placedBlockReadByComparator($$4, Blocks.CHISELED_BOOKSHELF))
         .addCriterion("comparator", placedComparatorReadingBlock($$4, Blocks.CHISELED_BOOKSHELF))
         .save($$1, "adventure/read_power_of_chiseled_bookshelf");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.ARMADILLO_SCUTE,
            Component.translatable("advancements.adventure.brush_armadillo.title"),
            Component.translatable("advancements.adventure.brush_armadillo.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "brush_armadillo",
            net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.BRUSH}),
               Optional.of(EntityPredicate.wrap(Builder.entity().of($$2, EntityType.ARMADILLO)))
            )
         )
         .save($$1, "adventure/brush_armadillo");
      AdvancementHolder $$18 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Blocks.CHISELED_TUFF,
            Component.translatable("advancements.adventure.minecraft_trials_edition.title"),
            Component.translatable("advancements.adventure.minecraft_trials_edition.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "minecraft_trials_edition",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inStructure(
                  $$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.TRIAL_CHAMBERS)
               )
            )
         )
         .save($$1, "adventure/minecraft_trials_edition");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$18)
         .display(
            Items.COPPER_BULB,
            Component.translatable("advancements.adventure.lighten_up.title"),
            Component.translatable("advancements.adventure.lighten_up.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "lighten_up",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(
                     net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                        .of(
                           $$4,
                           new Block[]{
                              Blocks.OXIDIZED_COPPER_BULB,
                              Blocks.WEATHERED_COPPER_BULB,
                              Blocks.EXPOSED_COPPER_BULB,
                              Blocks.WAXED_OXIDIZED_COPPER_BULB,
                              Blocks.WAXED_WEATHERED_COPPER_BULB,
                              Blocks.WAXED_EXPOSED_COPPER_BULB
                           }
                        )
                        .setProperties(
                           net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(CopperBulbBlock.LIT, true)
                        )
                  ),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, VanillaHusbandryAdvancements.WAX_SCRAPING_TOOLS)
            )
         )
         .save($$1, "adventure/lighten_up");
      AdvancementHolder $$19 = net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$18)
         .display(
            Items.TRIAL_KEY,
            Component.translatable("advancements.adventure.under_lock_and_key.title"),
            Component.translatable("advancements.adventure.under_lock_and_key.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "under_lock_and_key",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(
                     net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                        .of($$4, new Block[]{Blocks.VAULT})
                        .setProperties(
                           net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, false)
                        )
                  ),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.TRIAL_KEY})
            )
         )
         .save($$1, "adventure/under_lock_and_key");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$19)
         .display(
            Items.OMINOUS_TRIAL_KEY,
            Component.translatable("advancements.adventure.revaulting.title"),
            Component.translatable("advancements.adventure.revaulting.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion(
            "revaulting",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(
                     net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                        .of($$4, new Block[]{Blocks.VAULT})
                        .setProperties(net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, true))
                  ),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.OMINOUS_TRIAL_KEY})
            )
         )
         .save($$1, "adventure/revaulting");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$18)
         .display(
            Items.WIND_CHARGE,
            Component.translatable("advancements.adventure.blowback.title"),
            Component.translatable("advancements.adventure.blowback.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(40))
         .addCriterion(
            "blowback",
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(
               Builder.entity().of($$2, EntityType.BREEZE),
               net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType()
                  .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                  .direct(Builder.entity().of($$2, EntityType.BREEZE_WIND_CHARGE))
            )
         )
         .save($$1, "adventure/blowback");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.CRAFTER,
            Component.translatable("advancements.adventure.crafters_crafting_crafters.title"),
            Component.translatable("advancements.adventure.crafters_crafting_crafters.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "crafter_crafted_crafter",
            net.minecraft.advancements.criterion.RecipeCraftedTrigger.TriggerInstance.crafterCraftedItem(
               ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafter"))
            )
         )
         .save($$1, "adventure/crafters_crafting_crafters");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Items.LODESTONE,
            Component.translatable("advancements.adventure.use_lodestone.title"),
            Component.translatable("advancements.adventure.use_lodestone.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "use_lodestone",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, new Block[]{Blocks.LODESTONE})),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.COMPASS})
            )
         )
         .save($$1, "adventure/use_lodestone");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$18)
         .display(
            Items.WIND_CHARGE,
            Component.translatable("advancements.adventure.who_needs_rockets.title"),
            Component.translatable("advancements.adventure.who_needs_rockets.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "who_needs_rockets",
            net.minecraft.advancements.criterion.FallAfterExplosionTrigger.TriggerInstance.fallAfterExplosion(
               DistancePredicate.vertical(Doubles.atLeast(7.0)), Builder.entity().of($$2, EntityType.WIND_CHARGE)
            )
         )
         .save($$1, "adventure/who_needs_rockets");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$18)
         .display(
            Items.MACE,
            Component.translatable("advancements.adventure.overoverkill.title"),
            Component.translatable("advancements.adventure.overoverkill.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .addCriterion(
            "overoverkill",
            net.minecraft.advancements.criterion.PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(
               net.minecraft.advancements.criterion.DamagePredicate.Builder.damageInstance()
                  .dealtDamage(Doubles.atLeast(100.0))
                  .type(
                     net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType()
                        .tag(TagPredicate.is(DamageTypeTags.IS_MACE_SMASH))
                        .direct(
                           Builder.entity()
                              .of($$2, EntityType.PLAYER)
                              .equipment(
                                 net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                                    .mainhand(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.MACE}))
                              )
                        )
                  )
            )
         )
         .save($$1, "adventure/overoverkill");
      net.minecraft.advancements.Advancement.Builder.advancement()
         .parent($$5)
         .display(
            Blocks.CREAKING_HEART,
            Component.translatable("advancements.adventure.heart_transplanter.title"),
            Component.translatable("advancements.adventure.heart_transplanter.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion(
            "place_creaking_heart_dormant",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(
               Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.DORMANT
            )
         )
         .addCriterion(
            "place_creaking_heart_awake",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(
               Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.AWAKE
            )
         )
         .addCriterion("place_pale_oak_log", placedBlockActivatesCreakingHeart($$4, BlockTags.PALE_OAK_LOGS))
         .save($$1, "adventure/heart_transplanter");
   }

   public static AdvancementHolder createMonsterHunterAdvancement(
      AdvancementHolder $$0, Consumer<AdvancementHolder> $$1, HolderGetter<EntityType<?>> $$2, List<EntityType<?>> $$3
   ) {
      AdvancementHolder $$4 = addMobsToKill(net.minecraft.advancements.Advancement.Builder.advancement(), $$2, $$3)
         .parent($$0)
         .display(
            Items.IRON_SWORD,
            Component.translatable("advancements.adventure.kill_a_mob.title"),
            Component.translatable("advancements.adventure.kill_a_mob.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .save($$1, "adventure/kill_a_mob");
      addMobsToKill(net.minecraft.advancements.Advancement.Builder.advancement(), $$2, $$3)
         .parent($$4)
         .display(
            Items.DIAMOND_SWORD,
            Component.translatable("advancements.adventure.kill_all_mobs.title"),
            Component.translatable("advancements.adventure.kill_all_mobs.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .save($$1, "adventure/kill_all_mobs");
      return $$4;
   }

   private static Criterion<net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance> placedBlockReadByComparator(
      HolderGetter<Block> $$0, Block $$1
   ) {
      net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[] $$2 = ComparatorBlock.FACING
         .getPossibleValues()
         .stream()
         .map(
            $$1x -> {
               net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder $$2x = net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                  .hasProperty(ComparatorBlock.FACING, $$1x);
               net.minecraft.advancements.criterion.BlockPredicate.Builder $$3 = net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                  .of($$0, new Block[]{Blocks.COMPARATOR})
                  .setProperties($$2x);
               return LocationCheck.checkLocation(
                  net.minecraft.advancements.criterion.LocationPredicate.Builder.location().setBlock($$3), new BlockPos($$1x.getOpposite().getUnitVec3i())
               );
            }
         )
         .toArray(net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]::new);
      return net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
         new net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]{
            LootItemBlockStatePropertyCondition.hasBlockStateProperties($$1), AnyOfCondition.anyOf($$2)
         }
      );
   }

   private static Criterion<net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance> placedComparatorReadingBlock(
      HolderGetter<Block> $$0, Block $$1
   ) {
      net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[] $$2 = ComparatorBlock.FACING
         .getPossibleValues()
         .stream()
         .map(
            $$2x -> {
               net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder $$3 = net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                  .hasProperty(ComparatorBlock.FACING, $$2x);
               net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition.Builder $$4 = new net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition.Builder(
                     Blocks.COMPARATOR
                  )
                  .setProperties($$3);
               net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$5 = LocationCheck.checkLocation(
                  net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                     .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$0, new Block[]{$$1})),
                  new BlockPos($$2x.getUnitVec3i())
               );
               return AllOfCondition.allOf(new net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]{$$4, $$5});
            }
         )
         .toArray(net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]::new);
      return net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
         new net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]{AnyOfCondition.anyOf($$2)}
      );
   }

   private static Criterion<net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance> placedBlockActivatesCreakingHeart(
      HolderGetter<Block> $$0, TagKey<Block> $$1
   ) {
      net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[] $$2 = Stream.of(Direction.values())
         .map(
            $$2x -> {
               net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder $$3 = net.minecraft.advancements.criterion.StatePropertiesPredicate.Builder.properties()
                  .hasProperty(CreakingHeartBlock.AXIS, $$2x.getAxis());
               net.minecraft.advancements.criterion.BlockPredicate.Builder $$4 = net.minecraft.advancements.criterion.BlockPredicate.Builder.block()
                  .of($$0, $$1)
                  .setProperties($$3);
               Vec3i $$5 = $$2x.getUnitVec3i();
               net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$6 = LocationCheck.checkLocation(
                  net.minecraft.advancements.criterion.LocationPredicate.Builder.location().setBlock($$4)
               );
               net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$7 = LocationCheck.checkLocation(
                  net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                     .setBlock(
                        net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$0, new Block[]{Blocks.CREAKING_HEART}).setProperties($$3)
                     ),
                  new BlockPos($$5)
               );
               net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder $$8 = LocationCheck.checkLocation(
                  net.minecraft.advancements.criterion.LocationPredicate.Builder.location().setBlock($$4), new BlockPos($$5.multiply(2))
               );
               return AllOfCondition.allOf(new net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]{$$6, $$7, $$8});
            }
         )
         .toArray(net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]::new);
      return net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
         new net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder[]{AnyOfCondition.anyOf($$2)}
      );
   }

   private static net.minecraft.advancements.Advancement.Builder smithingWithStyle(net.minecraft.advancements.Advancement.Builder $$0) {
      $$0.requirements(Strategy.AND);
      Set<Item> $$1 = Set.of(
         Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE,
         Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE
      );
      VanillaRecipeProvider.smithingTrims()
         .filter($$1x -> $$1.contains($$1x.template()))
         .forEach(
            $$1x -> $$0.addCriterion(
               "armor_trimmed_" + $$1x.recipeId().identifier(),
               net.minecraft.advancements.criterion.RecipeCraftedTrigger.TriggerInstance.craftedItem($$1x.recipeId())
            )
         );
      return $$0;
   }

   private static net.minecraft.advancements.Advancement.Builder craftingANewLook(net.minecraft.advancements.Advancement.Builder $$0) {
      $$0.requirements(Strategy.OR);
      VanillaRecipeProvider.smithingTrims()
         .map(VanillaRecipeProvider.TrimTemplate::recipeId)
         .forEach(
            $$1 -> $$0.addCriterion(
               "armor_trimmed_" + $$1.identifier(), net.minecraft.advancements.criterion.RecipeCraftedTrigger.TriggerInstance.craftedItem($$1)
            )
         );
      return $$0;
   }

   private static net.minecraft.advancements.Advancement.Builder respectingTheRemnantsCriterions(
      HolderGetter<Item> $$0, net.minecraft.advancements.Advancement.Builder $$1
   ) {
      List<Pair<String, Criterion<TriggerInstance>>> $$2 = List.of(
         Pair.of("desert_pyramid", TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)),
         Pair.of("desert_well", TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)),
         Pair.of("ocean_ruin_cold", TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY)),
         Pair.of("ocean_ruin_warm", TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY)),
         Pair.of("trail_ruins_rare", TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)),
         Pair.of("trail_ruins_common", TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON))
      );
      $$2.forEach($$1x -> $$1.addCriterion((String)$$1x.getFirst(), (Criterion)$$1x.getSecond()));
      String $$3 = "has_sherd";
      $$1.addCriterion(
         "has_sherd",
         net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(
            new net.minecraft.advancements.criterion.ItemPredicate.Builder[]{
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$0, ItemTags.DECORATED_POT_SHERDS)
            }
         )
      );
      $$1.requirements(new AdvancementRequirements(List.of($$2.stream().map(Pair::getFirst).toList(), List.of("has_sherd"))));
      return $$1;
   }

   protected static void createAdventuringTime(Provider $$0, Consumer<AdvancementHolder> $$1, AdvancementHolder $$2, Preset $$3) {
      addBiomes(net.minecraft.advancements.Advancement.Builder.advancement(), $$0, $$3.usedBiomes().toList())
         .parent($$2)
         .display(
            Items.DIAMOND_BOOTS,
            Component.translatable("advancements.adventure.adventuring_time.title"),
            Component.translatable("advancements.adventure.adventuring_time.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(500))
         .save($$1, "adventure/adventuring_time");
   }

   private static net.minecraft.advancements.Advancement.Builder addMobsToKill(
      net.minecraft.advancements.Advancement.Builder $$0, HolderGetter<EntityType<?>> $$1, List<EntityType<?>> $$2
   ) {
      $$2.forEach(
         $$2x -> $$0.addCriterion(
            BuiltInRegistries.ENTITY_TYPE.getKey($$2x).toString(),
            net.minecraft.advancements.criterion.KilledTrigger.TriggerInstance.playerKilledEntity(Builder.entity().of($$1, $$2x))
         )
      );
      return $$0;
   }

   protected static net.minecraft.advancements.Advancement.Builder addBiomes(
      net.minecraft.advancements.Advancement.Builder $$0, Provider $$1, List<ResourceKey<Biome>> $$2
   ) {
      HolderGetter<Biome> $$3 = $$1.lookupOrThrow(Registries.BIOME);

      for (ResourceKey<Biome> $$4 : $$2) {
         $$0.addCriterion(
            $$4.identifier().toString(),
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inBiome($$3.getOrThrow($$4))
            )
         );
      }

      return $$0;
   }

   private static List<EntityType<?>> validateMobsToKill(List<EntityType<?>> $$0, HolderLookup<EntityType<?>> $$1) {
      List<String> $$2 = new ArrayList<>();
      Set<? extends EntityType<?>> $$3 = Set.copyOf($$0);
      Set<MobCategory> $$4 = $$3.stream().<MobCategory>map(EntityType::getCategory).collect(Collectors.toSet());
      Set<MobCategory> $$5 = Sets.symmetricDifference(EXCEPTIONS_BY_EXPECTED_CATEGORIES.keySet(), $$4);
      if (!$$5.isEmpty()) {
         $$2.add(
            "Found EntityType with MobCategory only in either expected exceptions or kill_all_mobs advancement: "
               + $$5.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))
         );
      }

      Set<EntityType<?>> $$6 = Sets.intersection(
         EXCEPTIONS_BY_EXPECTED_CATEGORIES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()), $$3
      );
      if (!$$6.isEmpty()) {
         $$2.add(
            "Found EntityType in both expected exceptions and kill_all_mobs advancement: "
               + $$6.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))
         );
      }

      Map<MobCategory, Set<EntityType<?>>> $$7 = $$1.listElements()
         .<EntityType>map(Reference::value)
         .filter(Predicate.not($$3::contains))
         .collect(Collectors.groupingBy(EntityType::getCategory, Collectors.toSet()));
      EXCEPTIONS_BY_EXPECTED_CATEGORIES.forEach(
         ($$2x, $$3x) -> {
            Set<EntityType<?>> $$4x = Sets.difference($$7.getOrDefault($$2x, Set.of()), $$3x);
            if (!$$4x.isEmpty()) {
               $$2.add(
                  String.format(
                     Locale.ROOT,
                     "Found (new?) EntityType with MobCategory %s which are in neither expected exceptions nor kill_all_mobs advancement: %s",
                     $$2x,
                     $$4x.stream().map(Object::toString).sorted().collect(Collectors.joining(", "))
                  )
               );
            }
         }
      );
      if (!$$2.isEmpty()) {
         $$2.forEach(LOGGER::error);
         throw new IllegalStateException("Found inconsistencies with kill_all_mobs advancement");
      } else {
         return $$0;
      }
   }
}
