/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.data.advancements.packs;

import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.lang.invoke.CallSite;
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
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.ChanneledLightningTrigger;
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.DistanceTrigger;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.FallAfterExplosionTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.KilledByArrowTrigger;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.advancements.criterion.LightningBoltPredicate;
import net.minecraft.advancements.criterion.LightningStrikeTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.LootTableTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.advancements.criterion.PlayerPredicate;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.RecipeCraftedTrigger;
import net.minecraft.advancements.criterion.ShotCrossbowTrigger;
import net.minecraft.advancements.criterion.SlideDownBlockTrigger;
import net.minecraft.advancements.criterion.SpearMobsTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.advancements.criterion.TargetBlockTrigger;
import net.minecraft.advancements.criterion.TradeTrigger;
import net.minecraft.advancements.criterion.UsedTotemTrigger;
import net.minecraft.advancements.criterion.UsingItemTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.JukeboxPlayablePredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.advancements.packs.VanillaHusbandryAdvancements;
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
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.VaultBlock;
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
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.slf4j.Logger;

public class VanillaAdventureAdvancements
implements AdvancementSubProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DISTANCE_FROM_BOTTOM_TO_TOP = 384;
    private static final int Y_COORDINATE_AT_TOP = 320;
    private static final int Y_COORDINATE_AT_BOTTOM = -64;
    private static final int BEDROCK_THICKNESS = 5;
    private static final Map<MobCategory, Set<EntityType<?>>> EXCEPTIONS_BY_EXPECTED_CATEGORIES = Map.of(MobCategory.MONSTER, Set.of(EntityType.GIANT, EntityType.ILLUSIONER, EntityType.WARDEN));
    private static final List<EntityType<?>> MOBS_TO_KILL = Arrays.asList(EntityType.BLAZE, EntityType.BOGGED, EntityType.BREEZE, EntityType.CAMEL_HUSK, EntityType.CAVE_SPIDER, EntityType.CREAKING, EntityType.CREEPER, EntityType.DROWNED, EntityType.ELDER_GUARDIAN, EntityType.ENDER_DRAGON, EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.EVOKER, EntityType.GHAST, EntityType.GUARDIAN, EntityType.HOGLIN, EntityType.HUSK, EntityType.MAGMA_CUBE, EntityType.PARCHED, EntityType.PHANTOM, EntityType.PIGLIN, EntityType.PIGLIN_BRUTE, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.SHULKER, EntityType.SILVERFISH, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.STRAY, EntityType.VEX, EntityType.VINDICATOR, EntityType.WITCH, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIE_VILLAGER, EntityType.ZOMBIE, EntityType.ZOMBIE_HORSE, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOMBIE_NAUTILUS);

    private static Criterion<LightningStrikeTrigger.TriggerInstance> fireCountAndBystander(MinMaxBounds.Ints $$0, Optional<EntityPredicate> $$1) {
        return LightningStrikeTrigger.TriggerInstance.lightningStrike(Optional.of(EntityPredicate.Builder.entity().distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(30.0))).subPredicate(LightningBoltPredicate.blockSetOnFire($$0)).build()), $$1);
    }

    private static Criterion<UsingItemTrigger.TriggerInstance> lookAtThroughItem(EntityPredicate.Builder $$0, ItemPredicate.Builder $$1) {
        return UsingItemTrigger.TriggerInstance.lookingAt(EntityPredicate.Builder.entity().subPredicate(PlayerPredicate.Builder.player().setLookingAt($$0).build()), $$1);
    }

    @Override
    public void generate(HolderLookup.Provider $$0, Consumer<AdvancementHolder> $$1) {
        HolderGetter $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
        HolderGetter $$3 = $$0.lookupOrThrow(Registries.ITEM);
        HolderGetter $$4 = $$0.lookupOrThrow(Registries.BLOCK);
        AdvancementHolder $$5 = Advancement.Builder.advancement().display(Items.MAP, (Component)Component.translatable("advancements.adventure.root.title"), (Component)Component.translatable("advancements.adventure.root.description"), Identifier.withDefaultNamespace("gui/advancements/backgrounds/adventure"), AdvancementType.TASK, false, false, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity()).addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer()).save($$1, "adventure/root");
        AdvancementHolder $$6 = Advancement.Builder.advancement().parent($$5).display(Blocks.RED_BED, (Component)Component.translatable("advancements.adventure.sleep_in_bed.title"), (Component)Component.translatable("advancements.adventure.sleep_in_bed.description"), null, AdvancementType.TASK, true, true, false).addCriterion("slept_in_bed", PlayerTrigger.TriggerInstance.sleptInBed()).save($$1, "adventure/sleep_in_bed");
        VanillaAdventureAdvancements.createAdventuringTime($$0, $$1, $$6, MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD);
        AdvancementHolder $$7 = Advancement.Builder.advancement().parent($$5).display(Items.EMERALD, (Component)Component.translatable("advancements.adventure.trade.title"), (Component)Component.translatable("advancements.adventure.trade.description"), null, AdvancementType.TASK, true, true, false).addCriterion("traded", TradeTrigger.TriggerInstance.tradedWithVillager()).save($$1, "adventure/trade");
        Advancement.Builder.advancement().parent($$7).display(Items.EMERALD, (Component)Component.translatable("advancements.adventure.trade_at_world_height.title"), (Component)Component.translatable("advancements.adventure.trade_at_world_height.description"), null, AdvancementType.TASK, true, true, false).addCriterion("trade_at_world_height", TradeTrigger.TriggerInstance.tradedWithVillager(EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0))))).save($$1, "adventure/trade_at_world_height");
        AdvancementHolder $$8 = VanillaAdventureAdvancements.createMonsterHunterAdvancement($$5, $$1, $$2, VanillaAdventureAdvancements.validateMobsToKill(MOBS_TO_KILL, $$2));
        AdvancementHolder $$9 = Advancement.Builder.advancement().parent($$8).display(Items.BOW, (Component)Component.translatable("advancements.adventure.shoot_arrow.title"), (Component)Component.translatable("advancements.adventure.shoot_arrow.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_arrow", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of($$2, EntityTypeTags.ARROWS))))).save($$1, "adventure/shoot_arrow");
        AdvancementHolder $$10 = Advancement.Builder.advancement().parent($$8).display(Items.TRIDENT, (Component)Component.translatable("advancements.adventure.throw_trident.title"), (Component)Component.translatable("advancements.adventure.throw_trident.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_trident", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of($$2, EntityType.TRIDENT))))).save($$1, "adventure/throw_trident");
        Advancement.Builder.advancement().parent($$10).display(Items.TRIDENT, (Component)Component.translatable("advancements.adventure.very_very_frightening.title"), (Component)Component.translatable("advancements.adventure.very_very_frightening.description"), null, AdvancementType.TASK, true, true, false).addCriterion("struck_villager", ChanneledLightningTrigger.TriggerInstance.channeledLightning(EntityPredicate.Builder.entity().of($$2, EntityType.VILLAGER))).save($$1, "adventure/very_very_frightening");
        Advancement.Builder.advancement().parent($$7).display(Blocks.CARVED_PUMPKIN, (Component)Component.translatable("advancements.adventure.summon_iron_golem.title"), (Component)Component.translatable("advancements.adventure.summon_iron_golem.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("summoned_golem", SummonedEntityTrigger.TriggerInstance.summonedEntity(EntityPredicate.Builder.entity().of($$2, EntityType.IRON_GOLEM))).save($$1, "adventure/summon_iron_golem");
        Advancement.Builder.advancement().parent($$9).display(Items.ARROW, (Component)Component.translatable("advancements.adventure.sniper_duel.title"), (Component)Component.translatable("advancements.adventure.sniper_duel.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("killed_skeleton", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of($$2, EntityType.SKELETON).distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(50.0))), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)))).save($$1, "adventure/sniper_duel");
        Advancement.Builder.advancement().parent($$8).display(Items.TOTEM_OF_UNDYING, (Component)Component.translatable("advancements.adventure.totem_of_undying.title"), (Component)Component.translatable("advancements.adventure.totem_of_undying.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("used_totem", UsedTotemTrigger.TriggerInstance.usedTotem($$3, Items.TOTEM_OF_UNDYING)).save($$1, "adventure/totem_of_undying");
        Advancement.Builder.advancement().parent($$8).display(Items.IRON_SPEAR, (Component)Component.translatable("advancements.adventure.spear_many_mobs.title"), (Component)Component.translatable("advancements.adventure.spear_many_mobs.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("spear_many_mobs", SpearMobsTrigger.TriggerInstance.spearMobs(5)).save($$1, "adventure/spear_many_mobs");
        AdvancementHolder $$11 = Advancement.Builder.advancement().parent($$5).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.ol_betsy.title"), (Component)Component.translatable("advancements.adventure.ol_betsy.description"), null, AdvancementType.TASK, true, true, false).addCriterion("shot_crossbow", ShotCrossbowTrigger.TriggerInstance.shotCrossbow($$3, Items.CROSSBOW)).save($$1, "adventure/ol_betsy");
        Advancement.Builder.advancement().parent($$11).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.whos_the_pillager_now.title"), (Component)Component.translatable("advancements.adventure.whos_the_pillager_now.description"), null, AdvancementType.TASK, true, true, false).addCriterion("kill_pillager", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)$$3, EntityPredicate.Builder.entity().of($$2, EntityType.PILLAGER))).save($$1, "adventure/whos_the_pillager_now");
        Advancement.Builder.advancement().parent($$11).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.two_birds_one_arrow.title"), (Component)Component.translatable("advancements.adventure.two_birds_one_arrow.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(65)).addCriterion("two_birds", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)$$3, EntityPredicate.Builder.entity().of($$2, EntityType.PHANTOM), EntityPredicate.Builder.entity().of($$2, EntityType.PHANTOM))).save($$1, "adventure/two_birds_one_arrow");
        Advancement.Builder.advancement().parent($$11).display(Items.CROSSBOW, (Component)Component.translatable("advancements.adventure.arbalistic.title"), (Component)Component.translatable("advancements.adventure.arbalistic.description"), null, AdvancementType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(85)).addCriterion("arbalistic", KilledByArrowTrigger.TriggerInstance.crossbowKilled((HolderGetter<Item>)$$3, MinMaxBounds.Ints.exactly(5))).save($$1, "adventure/arbalistic");
        HolderGetter $$12 = $$0.lookupOrThrow(Registries.BANNER_PATTERN);
        AdvancementHolder $$13 = Advancement.Builder.advancement().parent($$5).display(Raid.getOminousBannerInstance($$12), (Component)Component.translatable("advancements.adventure.voluntary_exile.title"), (Component)Component.translatable("advancements.adventure.voluntary_exile.description"), null, AdvancementType.TASK, true, true, true).addCriterion("voluntary_exile", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of($$2, EntityTypeTags.RAIDERS).equipment(EntityEquipmentPredicate.captainPredicate($$3, $$12)))).save($$1, "adventure/voluntary_exile");
        Advancement.Builder.advancement().parent($$13).display(Raid.getOminousBannerInstance($$12), (Component)Component.translatable("advancements.adventure.hero_of_the_village.title"), (Component)Component.translatable("advancements.adventure.hero_of_the_village.description"), null, AdvancementType.CHALLENGE, true, true, true).rewards(AdvancementRewards.Builder.experience(100)).addCriterion("hero_of_the_village", PlayerTrigger.TriggerInstance.raidWon()).save($$1, "adventure/hero_of_the_village");
        Advancement.Builder.advancement().parent($$5).display(Blocks.HONEY_BLOCK.asItem(), (Component)Component.translatable("advancements.adventure.honey_block_slide.title"), (Component)Component.translatable("advancements.adventure.honey_block_slide.description"), null, AdvancementType.TASK, true, true, false).addCriterion("honey_block_slide", SlideDownBlockTrigger.TriggerInstance.slidesDownBlock(Blocks.HONEY_BLOCK)).save($$1, "adventure/honey_block_slide");
        Advancement.Builder.advancement().parent($$9).display(Blocks.TARGET.asItem(), (Component)Component.translatable("advancements.adventure.bullseye.title"), (Component)Component.translatable("advancements.adventure.bullseye.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("bullseye", TargetBlockTrigger.TriggerInstance.targetHit(MinMaxBounds.Ints.exactly(15), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(30.0))))))).save($$1, "adventure/bullseye");
        Advancement.Builder.advancement().parent($$6).display(Items.LEATHER_BOOTS, (Component)Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.title"), (Component)Component.translatable("advancements.adventure.walk_on_powder_snow_with_leather_boots.description"), null, AdvancementType.TASK, true, true, false).addCriterion("walk_on_powder_snow_with_leather_boots", PlayerTrigger.TriggerInstance.walkOnBlockWithEquipment($$4, $$3, Blocks.POWDER_SNOW, Items.LEATHER_BOOTS)).save($$1, "adventure/walk_on_powder_snow_with_leather_boots");
        Advancement.Builder.advancement().parent($$5).display(Items.LIGHTNING_ROD, (Component)Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.title"), (Component)Component.translatable("advancements.adventure.lightning_rod_with_villager_no_fire.description"), null, AdvancementType.TASK, true, true, false).addCriterion("lightning_rod_with_villager_no_fire", VanillaAdventureAdvancements.fireCountAndBystander(MinMaxBounds.Ints.exactly(0), Optional.of(EntityPredicate.Builder.entity().of($$2, EntityType.VILLAGER).build()))).save($$1, "adventure/lightning_rod_with_villager_no_fire");
        AdvancementHolder $$14 = Advancement.Builder.advancement().parent($$5).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_parrot.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_parrot.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_parrot", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of($$2, EntityType.PARROT), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.SPYGLASS))).save($$1, "adventure/spyglass_at_parrot");
        AdvancementHolder $$15 = Advancement.Builder.advancement().parent($$14).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_ghast.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_ghast.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_ghast", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of($$2, EntityType.GHAST), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.SPYGLASS))).save($$1, "adventure/spyglass_at_ghast");
        Advancement.Builder.advancement().parent($$6).display(Items.JUKEBOX, (Component)Component.translatable("advancements.adventure.play_jukebox_in_meadows.title"), (Component)Component.translatable("advancements.adventure.play_jukebox_in_meadows.description"), null, AdvancementType.TASK, true, true, false).addCriterion("play_jukebox_in_meadows", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBiomes(HolderSet.direct($$0.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.MEADOW))).setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.JUKEBOX)), ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.JUKEBOX_PLAYABLE, JukeboxPlayablePredicate.any()).build()))).save($$1, "adventure/play_jukebox_in_meadows");
        Advancement.Builder.advancement().parent($$15).display(Items.SPYGLASS, (Component)Component.translatable("advancements.adventure.spyglass_at_dragon.title"), (Component)Component.translatable("advancements.adventure.spyglass_at_dragon.description"), null, AdvancementType.TASK, true, true, false).addCriterion("spyglass_at_dragon", VanillaAdventureAdvancements.lookAtThroughItem(EntityPredicate.Builder.entity().of($$2, EntityType.ENDER_DRAGON), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.SPYGLASS))).save($$1, "adventure/spyglass_at_dragon");
        Advancement.Builder.advancement().parent($$5).display(Items.WATER_BUCKET, (Component)Component.translatable("advancements.adventure.fall_from_world_height.title"), (Component)Component.translatable("advancements.adventure.fall_from_world_height.description"), null, AdvancementType.TASK, true, true, false).addCriterion("fall_from_world_height", DistanceTrigger.TriggerInstance.fallFromHeight(EntityPredicate.Builder.entity().located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atMost(-59.0))), DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(379.0)), LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0)))).save($$1, "adventure/fall_from_world_height");
        Advancement.Builder.advancement().parent($$8).display(Blocks.SCULK_CATALYST, (Component)Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.title"), (Component)Component.translatable("advancements.adventure.kill_mob_near_sculk_catalyst.description"), null, AdvancementType.CHALLENGE, true, true, false).addCriterion("kill_mob_near_sculk_catalyst", KilledTrigger.TriggerInstance.playerKilledEntityNearSculkCatalyst()).save($$1, "adventure/kill_mob_near_sculk_catalyst");
        Advancement.Builder.advancement().parent($$5).display(Blocks.SCULK_SENSOR, (Component)Component.translatable("advancements.adventure.avoid_vibration.title"), (Component)Component.translatable("advancements.adventure.avoid_vibration.description"), null, AdvancementType.TASK, true, true, false).addCriterion("avoid_vibration", PlayerTrigger.TriggerInstance.avoidVibration()).save($$1, "adventure/avoid_vibration");
        AdvancementHolder $$16 = VanillaAdventureAdvancements.respectingTheRemnantsCriterions($$3, Advancement.Builder.advancement()).parent($$5).display(Items.BRUSH, (Component)Component.translatable("advancements.adventure.salvage_sherd.title"), (Component)Component.translatable("advancements.adventure.salvage_sherd.description"), null, AdvancementType.TASK, true, true, false).save($$1, "adventure/salvage_sherd");
        Advancement.Builder.advancement().parent($$16).display(DecoratedPotBlockEntity.createDecoratedPotItem(new PotDecorations(Optional.empty(), Optional.of(Items.HEART_POTTERY_SHERD), Optional.empty(), Optional.of(Items.EXPLORER_POTTERY_SHERD))), (Component)Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.title"), (Component)Component.translatable("advancements.adventure.craft_decorated_pot_using_only_sherds.description"), null, AdvancementType.TASK, true, true, false).addCriterion("pot_crafted_using_only_sherds", RecipeCraftedTrigger.TriggerInstance.craftedItem(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("decorated_pot")), List.of(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, ItemTags.DECORATED_POT_SHERDS)))).save($$1, "adventure/craft_decorated_pot_using_only_sherds");
        AdvancementHolder $$17 = VanillaAdventureAdvancements.craftingANewLook(Advancement.Builder.advancement()).parent($$5).display(new ItemStack(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE), (Component)Component.translatable("advancements.adventure.trim_with_any_armor_pattern.title"), (Component)Component.translatable("advancements.adventure.trim_with_any_armor_pattern.description"), null, AdvancementType.TASK, true, true, false).save($$1, "adventure/trim_with_any_armor_pattern");
        VanillaAdventureAdvancements.smithingWithStyle(Advancement.Builder.advancement()).parent($$17).display(new ItemStack(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE), (Component)Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.title"), (Component)Component.translatable("advancements.adventure.trim_with_all_exclusive_armor_patterns.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(150)).save($$1, "adventure/trim_with_all_exclusive_armor_patterns");
        Advancement.Builder.advancement().parent($$5).display(Items.CHISELED_BOOKSHELF, (Component)Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.title"), (Component)Component.translatable("advancements.adventure.read_power_from_chiseled_bookshelf.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("chiseled_bookshelf", VanillaAdventureAdvancements.placedBlockReadByComparator($$4, Blocks.CHISELED_BOOKSHELF)).addCriterion("comparator", VanillaAdventureAdvancements.placedComparatorReadingBlock($$4, Blocks.CHISELED_BOOKSHELF)).save($$1, "adventure/read_power_of_chiseled_bookshelf");
        Advancement.Builder.advancement().parent($$5).display(Items.ARMADILLO_SCUTE, (Component)Component.translatable("advancements.adventure.brush_armadillo.title"), (Component)Component.translatable("advancements.adventure.brush_armadillo.description"), null, AdvancementType.TASK, true, true, false).addCriterion("brush_armadillo", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.BRUSH), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$2, EntityType.ARMADILLO))))).save($$1, "adventure/brush_armadillo");
        AdvancementHolder $$18 = Advancement.Builder.advancement().parent($$5).display(Blocks.CHISELED_TUFF, (Component)Component.translatable("advancements.adventure.minecraft_trials_edition.title"), (Component)Component.translatable("advancements.adventure.minecraft_trials_edition.description"), null, AdvancementType.TASK, true, true, false).addCriterion("minecraft_trials_edition", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure($$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.TRIAL_CHAMBERS)))).save($$1, "adventure/minecraft_trials_edition");
        Advancement.Builder.advancement().parent($$18).display(Items.COPPER_BULB, (Component)Component.translatable("advancements.adventure.lighten_up.title"), (Component)Component.translatable("advancements.adventure.lighten_up.description"), null, AdvancementType.TASK, true, true, false).addCriterion("lighten_up", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.OXIDIZED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB, Blocks.EXPOSED_COPPER_BULB, Blocks.WAXED_OXIDIZED_COPPER_BULB, Blocks.WAXED_WEATHERED_COPPER_BULB, Blocks.WAXED_EXPOSED_COPPER_BULB).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CopperBulbBlock.LIT, true))), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, VanillaHusbandryAdvancements.WAX_SCRAPING_TOOLS))).save($$1, "adventure/lighten_up");
        AdvancementHolder $$19 = Advancement.Builder.advancement().parent($$18).display(Items.TRIAL_KEY, (Component)Component.translatable("advancements.adventure.under_lock_and_key.title"), (Component)Component.translatable("advancements.adventure.under_lock_and_key.description"), null, AdvancementType.TASK, true, true, false).addCriterion("under_lock_and_key", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.VAULT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, false))), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.TRIAL_KEY))).save($$1, "adventure/under_lock_and_key");
        Advancement.Builder.advancement().parent($$19).display(Items.OMINOUS_TRIAL_KEY, (Component)Component.translatable("advancements.adventure.revaulting.title"), (Component)Component.translatable("advancements.adventure.revaulting.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("revaulting", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.VAULT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(VaultBlock.OMINOUS, true))), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.OMINOUS_TRIAL_KEY))).save($$1, "adventure/revaulting");
        Advancement.Builder.advancement().parent($$18).display(Items.WIND_CHARGE, (Component)Component.translatable("advancements.adventure.blowback.title"), (Component)Component.translatable("advancements.adventure.blowback.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(40)).addCriterion("blowback", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of($$2, EntityType.BREEZE), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).direct(EntityPredicate.Builder.entity().of($$2, EntityType.BREEZE_WIND_CHARGE)))).save($$1, "adventure/blowback");
        Advancement.Builder.advancement().parent($$5).display(Items.CRAFTER, (Component)Component.translatable("advancements.adventure.crafters_crafting_crafters.title"), (Component)Component.translatable("advancements.adventure.crafters_crafting_crafters.description"), null, AdvancementType.TASK, true, true, false).addCriterion("crafter_crafted_crafter", RecipeCraftedTrigger.TriggerInstance.crafterCraftedItem(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("crafter")))).save($$1, "adventure/crafters_crafting_crafters");
        Advancement.Builder.advancement().parent($$5).display(Items.LODESTONE, (Component)Component.translatable("advancements.adventure.use_lodestone.title"), (Component)Component.translatable("advancements.adventure.use_lodestone.description"), null, AdvancementType.TASK, true, true, false).addCriterion("use_lodestone", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.LODESTONE)), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.COMPASS))).save($$1, "adventure/use_lodestone");
        Advancement.Builder.advancement().parent($$18).display(Items.WIND_CHARGE, (Component)Component.translatable("advancements.adventure.who_needs_rockets.title"), (Component)Component.translatable("advancements.adventure.who_needs_rockets.description"), null, AdvancementType.TASK, true, true, false).addCriterion("who_needs_rockets", FallAfterExplosionTrigger.TriggerInstance.fallAfterExplosion(DistancePredicate.vertical(MinMaxBounds.Doubles.atLeast(7.0)), EntityPredicate.Builder.entity().of($$2, EntityType.WIND_CHARGE))).save($$1, "adventure/who_needs_rockets");
        Advancement.Builder.advancement().parent($$18).display(Items.MACE, (Component)Component.translatable("advancements.adventure.overoverkill.title"), (Component)Component.translatable("advancements.adventure.overoverkill.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).addCriterion("overoverkill", PlayerHurtEntityTrigger.TriggerInstance.playerHurtEntityWithDamage(DamagePredicate.Builder.damageInstance().dealtDamage(MinMaxBounds.Doubles.atLeast(100.0)).type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_MACE_SMASH)).direct(EntityPredicate.Builder.entity().of($$2, EntityType.PLAYER).equipment(EntityEquipmentPredicate.Builder.equipment().mainhand(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.MACE))))))).save($$1, "adventure/overoverkill");
        Advancement.Builder.advancement().parent($$5).display(Blocks.CREAKING_HEART, (Component)Component.translatable("advancements.adventure.heart_transplanter.title"), (Component)Component.translatable("advancements.adventure.heart_transplanter.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("place_creaking_heart_dormant", ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.DORMANT)).addCriterion("place_creaking_heart_awake", ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(Blocks.CREAKING_HEART, BlockStateProperties.CREAKING_HEART_STATE, CreakingHeartState.AWAKE)).addCriterion("place_pale_oak_log", VanillaAdventureAdvancements.placedBlockActivatesCreakingHeart($$4, BlockTags.PALE_OAK_LOGS)).save($$1, "adventure/heart_transplanter");
    }

    public static AdvancementHolder createMonsterHunterAdvancement(AdvancementHolder $$0, Consumer<AdvancementHolder> $$1, HolderGetter<EntityType<?>> $$2, List<EntityType<?>> $$3) {
        AdvancementHolder $$4 = VanillaAdventureAdvancements.addMobsToKill(Advancement.Builder.advancement(), $$2, $$3).parent($$0).display(Items.IRON_SWORD, (Component)Component.translatable("advancements.adventure.kill_a_mob.title"), (Component)Component.translatable("advancements.adventure.kill_a_mob.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).save($$1, "adventure/kill_a_mob");
        VanillaAdventureAdvancements.addMobsToKill(Advancement.Builder.advancement(), $$2, $$3).parent($$4).display(Items.DIAMOND_SWORD, (Component)Component.translatable("advancements.adventure.kill_all_mobs.title"), (Component)Component.translatable("advancements.adventure.kill_all_mobs.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save($$1, "adventure/kill_all_mobs");
        return $$4;
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockReadByComparator(HolderGetter<Block> $$0, Block $$12) {
        LootItemCondition.Builder[] $$2 = (LootItemCondition.Builder[])ComparatorBlock.FACING.getPossibleValues().stream().map($$1 -> {
            StatePropertiesPredicate.Builder $$2 = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, $$1);
            BlockPredicate.Builder $$3 = BlockPredicate.Builder.block().of($$0, Blocks.COMPARATOR).setProperties($$2);
            return LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock($$3), new BlockPos($$1.getOpposite().getUnitVec3i()));
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(LootItemBlockStatePropertyCondition.hasBlockStateProperties($$12), AnyOfCondition.anyOf($$2));
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedComparatorReadingBlock(HolderGetter<Block> $$0, Block $$1) {
        LootItemCondition.Builder[] $$22 = (LootItemCondition.Builder[])ComparatorBlock.FACING.getPossibleValues().stream().map($$2 -> {
            StatePropertiesPredicate.Builder $$3 = StatePropertiesPredicate.Builder.properties().hasProperty(ComparatorBlock.FACING, $$2);
            LootItemBlockStatePropertyCondition.Builder $$4 = new LootItemBlockStatePropertyCondition.Builder(Blocks.COMPARATOR).setProperties($$3);
            LootItemCondition.Builder $$5 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of($$0, $$1)), new BlockPos($$2.getUnitVec3i()));
            return AllOfCondition.allOf($$4, $$5);
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf($$22));
    }

    private static Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placedBlockActivatesCreakingHeart(HolderGetter<Block> $$0, TagKey<Block> $$1) {
        LootItemCondition.Builder[] $$22 = (LootItemCondition.Builder[])Stream.of(Direction.values()).map($$2 -> {
            StatePropertiesPredicate.Builder $$3 = StatePropertiesPredicate.Builder.properties().hasProperty(CreakingHeartBlock.AXIS, $$2.getAxis());
            BlockPredicate.Builder $$4 = BlockPredicate.Builder.block().of($$0, $$1).setProperties($$3);
            Vec3i $$5 = $$2.getUnitVec3i();
            LootItemCondition.Builder $$6 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock($$4));
            LootItemCondition.Builder $$7 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of($$0, Blocks.CREAKING_HEART).setProperties($$3)), new BlockPos($$5));
            LootItemCondition.Builder $$8 = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock($$4), new BlockPos($$5.multiply(2)));
            return AllOfCondition.allOf($$6, $$7, $$8);
        }).toArray(LootItemCondition.Builder[]::new);
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AnyOfCondition.anyOf($$22));
    }

    private static Advancement.Builder smithingWithStyle(Advancement.Builder $$0) {
        $$0.requirements(AdvancementRequirements.Strategy.AND);
        Set<Item> $$12 = Set.of(Items.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, Items.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE, Items.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE);
        VanillaRecipeProvider.smithingTrims().filter($$1 -> $$12.contains($$1.template())).forEach($$1 -> $$0.addCriterion("armor_trimmed_" + String.valueOf($$1.recipeId().identifier()), RecipeCraftedTrigger.TriggerInstance.craftedItem($$1.recipeId())));
        return $$0;
    }

    private static Advancement.Builder craftingANewLook(Advancement.Builder $$0) {
        $$0.requirements(AdvancementRequirements.Strategy.OR);
        VanillaRecipeProvider.smithingTrims().map(VanillaRecipeProvider.TrimTemplate::recipeId).forEach($$1 -> $$0.addCriterion("armor_trimmed_" + String.valueOf($$1.identifier()), RecipeCraftedTrigger.TriggerInstance.craftedItem($$1)));
        return $$0;
    }

    private static Advancement.Builder respectingTheRemnantsCriterions(HolderGetter<Item> $$0, Advancement.Builder $$12) {
        List<Pair> $$2 = List.of(Pair.of((Object)"desert_pyramid", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY)), Pair.of((Object)"desert_well", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.DESERT_WELL_ARCHAEOLOGY)), Pair.of((Object)"ocean_ruin_cold", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY)), Pair.of((Object)"ocean_ruin_warm", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY)), Pair.of((Object)"trail_ruins_rare", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_RARE)), Pair.of((Object)"trail_ruins_common", LootTableTrigger.TriggerInstance.lootTableUsed(BuiltInLootTables.TRAIL_RUINS_ARCHAEOLOGY_COMMON)));
        $$2.forEach($$1 -> $$12.addCriterion((String)$$1.getFirst(), (Criterion)$$1.getSecond()));
        String $$3 = "has_sherd";
        $$12.addCriterion("has_sherd", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of($$0, ItemTags.DECORATED_POT_SHERDS)));
        $$12.requirements(new AdvancementRequirements(List.of($$2.stream().map(Pair::getFirst).toList(), List.of("has_sherd"))));
        return $$12;
    }

    protected static void createAdventuringTime(HolderLookup.Provider $$0, Consumer<AdvancementHolder> $$1, AdvancementHolder $$2, MultiNoiseBiomeSourceParameterList.Preset $$3) {
        VanillaAdventureAdvancements.addBiomes(Advancement.Builder.advancement(), $$0, $$3.usedBiomes().toList()).parent($$2).display(Items.DIAMOND_BOOTS, (Component)Component.translatable("advancements.adventure.adventuring_time.title"), (Component)Component.translatable("advancements.adventure.adventuring_time.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(500)).save($$1, "adventure/adventuring_time");
    }

    private static Advancement.Builder addMobsToKill(Advancement.Builder $$0, HolderGetter<EntityType<?>> $$1, List<EntityType<?>> $$22) {
        $$22.forEach($$2 -> $$0.addCriterion(BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)$$2).toString(), KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of($$1, (EntityType<?>)$$2))));
        return $$0;
    }

    protected static Advancement.Builder addBiomes(Advancement.Builder $$0, HolderLookup.Provider $$1, List<ResourceKey<Biome>> $$2) {
        HolderGetter $$3 = $$1.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> $$4 : $$2) {
            $$0.addCriterion($$4.identifier().toString(), PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inBiome($$3.getOrThrow($$4))));
        }
        return $$0;
    }

    private static List<EntityType<?>> validateMobsToKill(List<EntityType<?>> $$0, HolderLookup<EntityType<?>> $$1) {
        Sets.SetView $$6;
        ArrayList<CallSite> $$22 = new ArrayList<CallSite>();
        Set<EntityType<?>> $$32 = Set.copyOf($$0);
        Set $$4 = $$32.stream().map(EntityType::getCategory).collect(Collectors.toSet());
        Sets.SetView $$5 = Sets.symmetricDifference(EXCEPTIONS_BY_EXPECTED_CATEGORIES.keySet(), $$4);
        if (!$$5.isEmpty()) {
            $$22.add((CallSite)((Object)("Found EntityType with MobCategory only in either expected exceptions or kill_all_mobs advancement: " + $$5.stream().map(Object::toString).sorted().collect(Collectors.joining(", ")))));
        }
        if (!($$6 = Sets.intersection(EXCEPTIONS_BY_EXPECTED_CATEGORIES.values().stream().flatMap(Collection::stream).collect(Collectors.toSet()), $$32)).isEmpty()) {
            $$22.add((CallSite)((Object)("Found EntityType in both expected exceptions and kill_all_mobs advancement: " + $$6.stream().map(Object::toString).sorted().collect(Collectors.joining(", ")))));
        }
        Map $$7 = $$1.listElements().map(Holder.Reference::value).filter(Predicate.not($$32::contains)).collect(Collectors.groupingBy(EntityType::getCategory, Collectors.toSet()));
        EXCEPTIONS_BY_EXPECTED_CATEGORIES.forEach(($$2, $$3) -> {
            Sets.SetView $$4 = Sets.difference($$7.getOrDefault($$2, Set.of()), (Set)$$3);
            if (!$$4.isEmpty()) {
                $$22.add((CallSite)((Object)String.format(Locale.ROOT, "Found (new?) EntityType with MobCategory %s which are in neither expected exceptions nor kill_all_mobs advancement: %s", $$2, $$4.stream().map(Object::toString).sorted().collect(Collectors.joining(", ")))));
            }
        });
        if (!$$22.isEmpty()) {
            $$22.forEach(arg_0 -> ((Logger)LOGGER).error(arg_0));
            throw new IllegalStateException("Found inconsistencies with kill_all_mobs advancement");
        }
        return $$0;
    }
}

