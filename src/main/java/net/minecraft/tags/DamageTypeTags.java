package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageType;

public interface DamageTypeTags {
   net.minecraft.tags.TagKey<DamageType> DAMAGES_HELMET = create("damages_helmet");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_ARMOR = create("bypasses_armor");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_SHIELD = create("bypasses_shield");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_INVULNERABILITY = create("bypasses_invulnerability");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_COOLDOWN = create("bypasses_cooldown");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_EFFECTS = create("bypasses_effects");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_RESISTANCE = create("bypasses_resistance");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_ENCHANTMENTS = create("bypasses_enchantments");
   net.minecraft.tags.TagKey<DamageType> IS_FIRE = create("is_fire");
   net.minecraft.tags.TagKey<DamageType> IS_PROJECTILE = create("is_projectile");
   net.minecraft.tags.TagKey<DamageType> WITCH_RESISTANT_TO = create("witch_resistant_to");
   net.minecraft.tags.TagKey<DamageType> IS_EXPLOSION = create("is_explosion");
   net.minecraft.tags.TagKey<DamageType> IS_FALL = create("is_fall");
   net.minecraft.tags.TagKey<DamageType> IS_DROWNING = create("is_drowning");
   net.minecraft.tags.TagKey<DamageType> IS_FREEZING = create("is_freezing");
   net.minecraft.tags.TagKey<DamageType> IS_LIGHTNING = create("is_lightning");
   net.minecraft.tags.TagKey<DamageType> NO_ANGER = create("no_anger");
   net.minecraft.tags.TagKey<DamageType> NO_IMPACT = create("no_impact");
   net.minecraft.tags.TagKey<DamageType> ALWAYS_MOST_SIGNIFICANT_FALL = create("always_most_significant_fall");
   net.minecraft.tags.TagKey<DamageType> WITHER_IMMUNE_TO = create("wither_immune_to");
   net.minecraft.tags.TagKey<DamageType> IGNITES_ARMOR_STANDS = create("ignites_armor_stands");
   net.minecraft.tags.TagKey<DamageType> BURNS_ARMOR_STANDS = create("burns_armor_stands");
   net.minecraft.tags.TagKey<DamageType> AVOIDS_GUARDIAN_THORNS = create("avoids_guardian_thorns");
   net.minecraft.tags.TagKey<DamageType> ALWAYS_TRIGGERS_SILVERFISH = create("always_triggers_silverfish");
   net.minecraft.tags.TagKey<DamageType> ALWAYS_HURTS_ENDER_DRAGONS = create("always_hurts_ender_dragons");
   net.minecraft.tags.TagKey<DamageType> NO_KNOCKBACK = create("no_knockback");
   net.minecraft.tags.TagKey<DamageType> ALWAYS_KILLS_ARMOR_STANDS = create("always_kills_armor_stands");
   net.minecraft.tags.TagKey<DamageType> CAN_BREAK_ARMOR_STAND = create("can_break_armor_stand");
   net.minecraft.tags.TagKey<DamageType> BYPASSES_WOLF_ARMOR = create("bypasses_wolf_armor");
   net.minecraft.tags.TagKey<DamageType> IS_PLAYER_ATTACK = create("is_player_attack");
   net.minecraft.tags.TagKey<DamageType> BURN_FROM_STEPPING = create("burn_from_stepping");
   net.minecraft.tags.TagKey<DamageType> PANIC_CAUSES = create("panic_causes");
   net.minecraft.tags.TagKey<DamageType> PANIC_ENVIRONMENTAL_CAUSES = create("panic_environmental_causes");
   net.minecraft.tags.TagKey<DamageType> IS_MACE_SMASH = create("mace_smash");

   private static net.minecraft.tags.TagKey<DamageType> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace($$0));
   }
}
