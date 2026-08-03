package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentTags {
   net.minecraft.tags.TagKey<Enchantment> TOOLTIP_ORDER = create("tooltip_order");
   net.minecraft.tags.TagKey<Enchantment> ARMOR_EXCLUSIVE = create("exclusive_set/armor");
   net.minecraft.tags.TagKey<Enchantment> BOOTS_EXCLUSIVE = create("exclusive_set/boots");
   net.minecraft.tags.TagKey<Enchantment> BOW_EXCLUSIVE = create("exclusive_set/bow");
   net.minecraft.tags.TagKey<Enchantment> CROSSBOW_EXCLUSIVE = create("exclusive_set/crossbow");
   net.minecraft.tags.TagKey<Enchantment> DAMAGE_EXCLUSIVE = create("exclusive_set/damage");
   net.minecraft.tags.TagKey<Enchantment> MINING_EXCLUSIVE = create("exclusive_set/mining");
   net.minecraft.tags.TagKey<Enchantment> RIPTIDE_EXCLUSIVE = create("exclusive_set/riptide");
   net.minecraft.tags.TagKey<Enchantment> TRADEABLE = create("tradeable");
   net.minecraft.tags.TagKey<Enchantment> DOUBLE_TRADE_PRICE = create("double_trade_price");
   net.minecraft.tags.TagKey<Enchantment> IN_ENCHANTING_TABLE = create("in_enchanting_table");
   net.minecraft.tags.TagKey<Enchantment> ON_MOB_SPAWN_EQUIPMENT = create("on_mob_spawn_equipment");
   net.minecraft.tags.TagKey<Enchantment> ON_TRADED_EQUIPMENT = create("on_traded_equipment");
   net.minecraft.tags.TagKey<Enchantment> ON_RANDOM_LOOT = create("on_random_loot");
   net.minecraft.tags.TagKey<Enchantment> CURSE = create("curse");
   net.minecraft.tags.TagKey<Enchantment> SMELTS_LOOT = create("smelts_loot");
   net.minecraft.tags.TagKey<Enchantment> PREVENTS_BEE_SPAWNS_WHEN_MINING = create("prevents_bee_spawns_when_mining");
   net.minecraft.tags.TagKey<Enchantment> PREVENTS_DECORATED_POT_SHATTERING = create("prevents_decorated_pot_shattering");
   net.minecraft.tags.TagKey<Enchantment> PREVENTS_ICE_MELTING = create("prevents_ice_melting");
   net.minecraft.tags.TagKey<Enchantment> PREVENTS_INFESTED_SPAWNS = create("prevents_infested_spawns");
   net.minecraft.tags.TagKey<Enchantment> TREASURE = create("treasure");
   net.minecraft.tags.TagKey<Enchantment> NON_TREASURE = create("non_treasure");
   net.minecraft.tags.TagKey<Enchantment> TRADES_DESERT_COMMON = create("trades/desert_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_JUNGLE_COMMON = create("trades/jungle_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_PLAINS_COMMON = create("trades/plains_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SAVANNA_COMMON = create("trades/savanna_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SNOW_COMMON = create("trades/snow_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SWAMP_COMMON = create("trades/swamp_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_TAIGA_COMMON = create("trades/taiga_common");
   net.minecraft.tags.TagKey<Enchantment> TRADES_DESERT_SPECIAL = create("trades/desert_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_JUNGLE_SPECIAL = create("trades/jungle_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_PLAINS_SPECIAL = create("trades/plains_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SAVANNA_SPECIAL = create("trades/savanna_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SNOW_SPECIAL = create("trades/snow_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_SWAMP_SPECIAL = create("trades/swamp_special");
   net.minecraft.tags.TagKey<Enchantment> TRADES_TAIGA_SPECIAL = create("trades/taiga_special");

   private static net.minecraft.tags.TagKey<Enchantment> create(String $$0) {
      return net.minecraft.tags.TagKey.create(Registries.ENCHANTMENT, Identifier.withDefaultNamespace($$0));
   }
}
