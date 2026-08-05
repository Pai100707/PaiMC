/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.criterion.AnyBlockInteractionTrigger;
import net.minecraft.advancements.criterion.BeeNestDestroyedTrigger;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.BrewedPotionTrigger;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger;
import net.minecraft.advancements.criterion.ChanneledLightningTrigger;
import net.minecraft.advancements.criterion.ConstructBeaconTrigger;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.CuredZombieVillagerTrigger;
import net.minecraft.advancements.criterion.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.criterion.DistanceTrigger;
import net.minecraft.advancements.criterion.EffectsChangedTrigger;
import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.advancements.criterion.EnterBlockTrigger;
import net.minecraft.advancements.criterion.EntityHurtPlayerTrigger;
import net.minecraft.advancements.criterion.FallAfterExplosionTrigger;
import net.minecraft.advancements.criterion.FilledBucketTrigger;
import net.minecraft.advancements.criterion.FishingRodHookedTrigger;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemDurabilityTrigger;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.KilledByArrowTrigger;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.advancements.criterion.LevitationTrigger;
import net.minecraft.advancements.criterion.LightningStrikeTrigger;
import net.minecraft.advancements.criterion.LootTableTrigger;
import net.minecraft.advancements.criterion.PickedUpItemTrigger;
import net.minecraft.advancements.criterion.PlayerHurtEntityTrigger;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.RecipeCraftedTrigger;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.advancements.criterion.ShotCrossbowTrigger;
import net.minecraft.advancements.criterion.SlideDownBlockTrigger;
import net.minecraft.advancements.criterion.SpearMobsTrigger;
import net.minecraft.advancements.criterion.StartRidingTrigger;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.advancements.criterion.TameAnimalTrigger;
import net.minecraft.advancements.criterion.TargetBlockTrigger;
import net.minecraft.advancements.criterion.TradeTrigger;
import net.minecraft.advancements.criterion.UsedEnderEyeTrigger;
import net.minecraft.advancements.criterion.UsedTotemTrigger;
import net.minecraft.advancements.criterion.UsingItemTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class CriteriaTriggers {
    public static final Codec<CriterionTrigger<?>> CODEC = BuiltInRegistries.TRIGGER_TYPES.byNameCodec();
    public static final ImpossibleTrigger IMPOSSIBLE = CriteriaTriggers.register("impossible", new ImpossibleTrigger());
    public static final KilledTrigger PLAYER_KILLED_ENTITY = CriteriaTriggers.register("player_killed_entity", new KilledTrigger());
    public static final KilledTrigger ENTITY_KILLED_PLAYER = CriteriaTriggers.register("entity_killed_player", new KilledTrigger());
    public static final EnterBlockTrigger ENTER_BLOCK = CriteriaTriggers.register("enter_block", new EnterBlockTrigger());
    public static final InventoryChangeTrigger INVENTORY_CHANGED = CriteriaTriggers.register("inventory_changed", new InventoryChangeTrigger());
    public static final RecipeUnlockedTrigger RECIPE_UNLOCKED = CriteriaTriggers.register("recipe_unlocked", new RecipeUnlockedTrigger());
    public static final PlayerHurtEntityTrigger PLAYER_HURT_ENTITY = CriteriaTriggers.register("player_hurt_entity", new PlayerHurtEntityTrigger());
    public static final EntityHurtPlayerTrigger ENTITY_HURT_PLAYER = CriteriaTriggers.register("entity_hurt_player", new EntityHurtPlayerTrigger());
    public static final EnchantedItemTrigger ENCHANTED_ITEM = CriteriaTriggers.register("enchanted_item", new EnchantedItemTrigger());
    public static final FilledBucketTrigger FILLED_BUCKET = CriteriaTriggers.register("filled_bucket", new FilledBucketTrigger());
    public static final BrewedPotionTrigger BREWED_POTION = CriteriaTriggers.register("brewed_potion", new BrewedPotionTrigger());
    public static final ConstructBeaconTrigger CONSTRUCT_BEACON = CriteriaTriggers.register("construct_beacon", new ConstructBeaconTrigger());
    public static final UsedEnderEyeTrigger USED_ENDER_EYE = CriteriaTriggers.register("used_ender_eye", new UsedEnderEyeTrigger());
    public static final SummonedEntityTrigger SUMMONED_ENTITY = CriteriaTriggers.register("summoned_entity", new SummonedEntityTrigger());
    public static final BredAnimalsTrigger BRED_ANIMALS = CriteriaTriggers.register("bred_animals", new BredAnimalsTrigger());
    public static final PlayerTrigger LOCATION = CriteriaTriggers.register("location", new PlayerTrigger());
    public static final PlayerTrigger SLEPT_IN_BED = CriteriaTriggers.register("slept_in_bed", new PlayerTrigger());
    public static final CuredZombieVillagerTrigger CURED_ZOMBIE_VILLAGER = CriteriaTriggers.register("cured_zombie_villager", new CuredZombieVillagerTrigger());
    public static final TradeTrigger TRADE = CriteriaTriggers.register("villager_trade", new TradeTrigger());
    public static final ItemDurabilityTrigger ITEM_DURABILITY_CHANGED = CriteriaTriggers.register("item_durability_changed", new ItemDurabilityTrigger());
    public static final LevitationTrigger LEVITATION = CriteriaTriggers.register("levitation", new LevitationTrigger());
    public static final ChangeDimensionTrigger CHANGED_DIMENSION = CriteriaTriggers.register("changed_dimension", new ChangeDimensionTrigger());
    public static final PlayerTrigger TICK = CriteriaTriggers.register("tick", new PlayerTrigger());
    public static final TameAnimalTrigger TAME_ANIMAL = CriteriaTriggers.register("tame_animal", new TameAnimalTrigger());
    public static final ItemUsedOnLocationTrigger PLACED_BLOCK = CriteriaTriggers.register("placed_block", new ItemUsedOnLocationTrigger());
    public static final ConsumeItemTrigger CONSUME_ITEM = CriteriaTriggers.register("consume_item", new ConsumeItemTrigger());
    public static final EffectsChangedTrigger EFFECTS_CHANGED = CriteriaTriggers.register("effects_changed", new EffectsChangedTrigger());
    public static final UsedTotemTrigger USED_TOTEM = CriteriaTriggers.register("used_totem", new UsedTotemTrigger());
    public static final DistanceTrigger NETHER_TRAVEL = CriteriaTriggers.register("nether_travel", new DistanceTrigger());
    public static final FishingRodHookedTrigger FISHING_ROD_HOOKED = CriteriaTriggers.register("fishing_rod_hooked", new FishingRodHookedTrigger());
    public static final ChanneledLightningTrigger CHANNELED_LIGHTNING = CriteriaTriggers.register("channeled_lightning", new ChanneledLightningTrigger());
    public static final ShotCrossbowTrigger SHOT_CROSSBOW = CriteriaTriggers.register("shot_crossbow", new ShotCrossbowTrigger());
    public static final SpearMobsTrigger SPEAR_MOBS_TRIGGER = CriteriaTriggers.register("spear_mobs", new SpearMobsTrigger());
    public static final KilledByArrowTrigger KILLED_BY_ARROW = CriteriaTriggers.register("killed_by_arrow", new KilledByArrowTrigger());
    public static final PlayerTrigger RAID_WIN = CriteriaTriggers.register("hero_of_the_village", new PlayerTrigger());
    public static final PlayerTrigger RAID_OMEN = CriteriaTriggers.register("voluntary_exile", new PlayerTrigger());
    public static final SlideDownBlockTrigger HONEY_BLOCK_SLIDE = CriteriaTriggers.register("slide_down_block", new SlideDownBlockTrigger());
    public static final BeeNestDestroyedTrigger BEE_NEST_DESTROYED = CriteriaTriggers.register("bee_nest_destroyed", new BeeNestDestroyedTrigger());
    public static final TargetBlockTrigger TARGET_BLOCK_HIT = CriteriaTriggers.register("target_hit", new TargetBlockTrigger());
    public static final ItemUsedOnLocationTrigger ITEM_USED_ON_BLOCK = CriteriaTriggers.register("item_used_on_block", new ItemUsedOnLocationTrigger());
    public static final DefaultBlockInteractionTrigger DEFAULT_BLOCK_USE = CriteriaTriggers.register("default_block_use", new DefaultBlockInteractionTrigger());
    public static final AnyBlockInteractionTrigger ANY_BLOCK_USE = CriteriaTriggers.register("any_block_use", new AnyBlockInteractionTrigger());
    public static final LootTableTrigger GENERATE_LOOT = CriteriaTriggers.register("player_generates_container_loot", new LootTableTrigger());
    public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_ENTITY = CriteriaTriggers.register("thrown_item_picked_up_by_entity", new PickedUpItemTrigger());
    public static final PickedUpItemTrigger THROWN_ITEM_PICKED_UP_BY_PLAYER = CriteriaTriggers.register("thrown_item_picked_up_by_player", new PickedUpItemTrigger());
    public static final PlayerInteractTrigger PLAYER_INTERACTED_WITH_ENTITY = CriteriaTriggers.register("player_interacted_with_entity", new PlayerInteractTrigger());
    public static final PlayerInteractTrigger PLAYER_SHEARED_EQUIPMENT = CriteriaTriggers.register("player_sheared_equipment", new PlayerInteractTrigger());
    public static final StartRidingTrigger START_RIDING_TRIGGER = CriteriaTriggers.register("started_riding", new StartRidingTrigger());
    public static final LightningStrikeTrigger LIGHTNING_STRIKE = CriteriaTriggers.register("lightning_strike", new LightningStrikeTrigger());
    public static final UsingItemTrigger USING_ITEM = CriteriaTriggers.register("using_item", new UsingItemTrigger());
    public static final DistanceTrigger FALL_FROM_HEIGHT = CriteriaTriggers.register("fall_from_height", new DistanceTrigger());
    public static final DistanceTrigger RIDE_ENTITY_IN_LAVA_TRIGGER = CriteriaTriggers.register("ride_entity_in_lava", new DistanceTrigger());
    public static final KilledTrigger KILL_MOB_NEAR_SCULK_CATALYST = CriteriaTriggers.register("kill_mob_near_sculk_catalyst", new KilledTrigger());
    public static final ItemUsedOnLocationTrigger ALLAY_DROP_ITEM_ON_BLOCK = CriteriaTriggers.register("allay_drop_item_on_block", new ItemUsedOnLocationTrigger());
    public static final PlayerTrigger AVOID_VIBRATION = CriteriaTriggers.register("avoid_vibration", new PlayerTrigger());
    public static final RecipeCraftedTrigger RECIPE_CRAFTED = CriteriaTriggers.register("recipe_crafted", new RecipeCraftedTrigger());
    public static final RecipeCraftedTrigger CRAFTER_RECIPE_CRAFTED = CriteriaTriggers.register("crafter_recipe_crafted", new RecipeCraftedTrigger());
    public static final FallAfterExplosionTrigger FALL_AFTER_EXPLOSION = CriteriaTriggers.register("fall_after_explosion", new FallAfterExplosionTrigger());

    private static <T extends CriterionTrigger<?>> T register(String $$0, T $$1) {
        return (T)Registry.register(BuiltInRegistries.TRIGGER_TYPES, $$0, $$1);
    }

    public static CriterionTrigger<?> bootstrap(Registry<CriterionTrigger<?>> $$0) {
        return IMPOSSIBLE;
    }
}

