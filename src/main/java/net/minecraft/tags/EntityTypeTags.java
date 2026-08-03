package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public interface EntityTypeTags {
   net.minecraft.tags.TagKey<EntityType<?>> SKELETONS = create("skeletons");
   net.minecraft.tags.TagKey<EntityType<?>> ZOMBIES = create("zombies");
   net.minecraft.tags.TagKey<EntityType<?>> RAIDERS = create("raiders");
   net.minecraft.tags.TagKey<EntityType<?>> UNDEAD = create("undead");
   net.minecraft.tags.TagKey<EntityType<?>> BURN_IN_DAYLIGHT = create("burn_in_daylight");
   net.minecraft.tags.TagKey<EntityType<?>> BEEHIVE_INHABITORS = create("beehive_inhabitors");
   net.minecraft.tags.TagKey<EntityType<?>> ARROWS = create("arrows");
   net.minecraft.tags.TagKey<EntityType<?>> IMPACT_PROJECTILES = create("impact_projectiles");
   net.minecraft.tags.TagKey<EntityType<?>> POWDER_SNOW_WALKABLE_MOBS = create("powder_snow_walkable_mobs");
   net.minecraft.tags.TagKey<EntityType<?>> AXOLOTL_ALWAYS_HOSTILES = create("axolotl_always_hostiles");
   net.minecraft.tags.TagKey<EntityType<?>> AXOLOTL_HUNT_TARGETS = create("axolotl_hunt_targets");
   net.minecraft.tags.TagKey<EntityType<?>> FREEZE_IMMUNE_ENTITY_TYPES = create("freeze_immune_entity_types");
   net.minecraft.tags.TagKey<EntityType<?>> FREEZE_HURTS_EXTRA_TYPES = create("freeze_hurts_extra_types");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_BREATHE_UNDER_WATER = create("can_breathe_under_water");
   net.minecraft.tags.TagKey<EntityType<?>> FROG_FOOD = create("frog_food");
   net.minecraft.tags.TagKey<EntityType<?>> FALL_DAMAGE_IMMUNE = create("fall_damage_immune");
   net.minecraft.tags.TagKey<EntityType<?>> DISMOUNTS_UNDERWATER = create("dismounts_underwater");
   net.minecraft.tags.TagKey<EntityType<?>> NON_CONTROLLING_RIDER = create("non_controlling_rider");
   net.minecraft.tags.TagKey<EntityType<?>> DEFLECTS_PROJECTILES = create("deflects_projectiles");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_TURN_IN_BOATS = create("can_turn_in_boats");
   net.minecraft.tags.TagKey<EntityType<?>> ILLAGER = create("illager");
   net.minecraft.tags.TagKey<EntityType<?>> AQUATIC = create("aquatic");
   net.minecraft.tags.TagKey<EntityType<?>> ARTHROPOD = create("arthropod");
   net.minecraft.tags.TagKey<EntityType<?>> IGNORES_POISON_AND_REGEN = create("ignores_poison_and_regen");
   net.minecraft.tags.TagKey<EntityType<?>> INVERTED_HEALING_AND_HARM = create("inverted_healing_and_harm");
   net.minecraft.tags.TagKey<EntityType<?>> WITHER_FRIENDS = create("wither_friends");
   net.minecraft.tags.TagKey<EntityType<?>> ILLAGER_FRIENDS = create("illager_friends");
   net.minecraft.tags.TagKey<EntityType<?>> NOT_SCARY_FOR_PUFFERFISH = create("not_scary_for_pufferfish");
   net.minecraft.tags.TagKey<EntityType<?>> SENSITIVE_TO_IMPALING = create("sensitive_to_impaling");
   net.minecraft.tags.TagKey<EntityType<?>> SENSITIVE_TO_BANE_OF_ARTHROPODS = create("sensitive_to_bane_of_arthropods");
   net.minecraft.tags.TagKey<EntityType<?>> SENSITIVE_TO_SMITE = create("sensitive_to_smite");
   net.minecraft.tags.TagKey<EntityType<?>> NO_ANGER_FROM_WIND_CHARGE = create("no_anger_from_wind_charge");
   net.minecraft.tags.TagKey<EntityType<?>> IMMUNE_TO_OOZING = create("immune_to_oozing");
   net.minecraft.tags.TagKey<EntityType<?>> IMMUNE_TO_INFESTED = create("immune_to_infested");
   net.minecraft.tags.TagKey<EntityType<?>> REDIRECTABLE_PROJECTILE = create("redirectable_projectile");
   net.minecraft.tags.TagKey<EntityType<?>> BOAT = create("boat");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_EQUIP_SADDLE = create("can_equip_saddle");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_EQUIP_HARNESS = create("can_equip_harness");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_WEAR_HORSE_ARMOR = create("can_wear_horse_armor");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_WEAR_NAUTILUS_ARMOR = create("can_wear_nautilus_armor");
   net.minecraft.tags.TagKey<EntityType<?>> FOLLOWABLE_FRIENDLY_MOBS = create("followable_friendly_mobs");
   net.minecraft.tags.TagKey<EntityType<?>> CANNOT_BE_PUSHED_ONTO_BOATS = create("cannot_be_pushed_onto_boats");
   net.minecraft.tags.TagKey<EntityType<?>> ACCEPTS_IRON_GOLEM_GIFT = create("accepts_iron_golem_gift");
   net.minecraft.tags.TagKey<EntityType<?>> CANDIDATE_FOR_IRON_GOLEM_GIFT = create("candidate_for_iron_golem_gift");
   net.minecraft.tags.TagKey<EntityType<?>> NAUTILUS_HOSTILES = create("nautilus_hostiles");
   net.minecraft.tags.TagKey<EntityType<?>> CAN_FLOAT_WHILE_RIDDEN = create("can_float_while_ridden");

   private static net.minecraft.tags.TagKey<EntityType<?>> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace($$0));
   }
}
