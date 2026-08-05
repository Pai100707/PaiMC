/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.fixes.References;

public class GameRuleRegistryFix
extends DataFix {
    public GameRuleRegistryFix(Schema $$0) {
        super($$0, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("GameRuleRegistryFix", this.getInputSchema().getType(References.LEVEL), $$02 -> $$02.update(DSL.remainderFinder(), $$0 -> $$0.renameAndFixField("GameRules", "game_rules", $$02 -> {
            int $$5;
            boolean $$1 = Boolean.parseBoolean($$02.get("doFireTick").asString("true"));
            boolean $$2 = Boolean.parseBoolean($$02.get("allowFireTicksAwayFromPlayer").asString("false"));
            if (!$$1) {
                boolean $$3 = false;
            } else if (!$$2) {
                int $$4 = 128;
            } else {
                $$5 = -1;
            }
            if ($$5 != 128) {
                $$02 = $$02.set("minecraft:fire_spread_radius_around_player", $$02.createInt($$5));
            }
            return $$02.remove("spawnChunkRadius").remove("entitiesWithPassengersCanUsePortals").remove("doFireTick").remove("allowFireTicksAwayFromPlayer").renameAndFixField("allowEnteringNetherUsingPortals", "minecraft:allow_entering_nether_using_portals", GameRuleRegistryFix::convertBoolean).renameAndFixField("announceAdvancements", "minecraft:show_advancement_messages", GameRuleRegistryFix::convertBoolean).renameAndFixField("blockExplosionDropDecay", "minecraft:block_explosion_drop_decay", GameRuleRegistryFix::convertBoolean).renameAndFixField("commandBlockOutput", "minecraft:command_block_output", GameRuleRegistryFix::convertBoolean).renameAndFixField("enableCommandBlocks", "minecraft:command_blocks_work", GameRuleRegistryFix::convertBoolean).renameAndFixField("commandBlocksEnabled", "minecraft:command_blocks_work", GameRuleRegistryFix::convertBoolean).renameAndFixField("commandModificationBlockLimit", "minecraft:max_block_modifications", $$0 -> GameRuleRegistryFix.convertInteger($$0, 1)).renameAndFixField("disableElytraMovementCheck", "minecraft:elytra_movement_check", GameRuleRegistryFix::convertBooleanInverted).renameAndFixField("disablePlayerMovementCheck", "minecraft:player_movement_check", GameRuleRegistryFix::convertBooleanInverted).renameAndFixField("disableRaids", "minecraft:raids", GameRuleRegistryFix::convertBooleanInverted).renameAndFixField("doDaylightCycle", "minecraft:advance_time", GameRuleRegistryFix::convertBoolean).renameAndFixField("doEntityDrops", "minecraft:entity_drops", GameRuleRegistryFix::convertBoolean).renameAndFixField("doImmediateRespawn", "minecraft:immediate_respawn", GameRuleRegistryFix::convertBoolean).renameAndFixField("doInsomnia", "minecraft:spawn_phantoms", GameRuleRegistryFix::convertBoolean).renameAndFixField("doLimitedCrafting", "minecraft:limited_crafting", GameRuleRegistryFix::convertBoolean).renameAndFixField("doMobLoot", "minecraft:mob_drops", GameRuleRegistryFix::convertBoolean).renameAndFixField("doMobSpawning", "minecraft:spawn_mobs", GameRuleRegistryFix::convertBoolean).renameAndFixField("doPatrolSpawning", "minecraft:spawn_patrols", GameRuleRegistryFix::convertBoolean).renameAndFixField("doTileDrops", "minecraft:block_drops", GameRuleRegistryFix::convertBoolean).renameAndFixField("doTraderSpawning", "minecraft:spawn_wandering_traders", GameRuleRegistryFix::convertBoolean).renameAndFixField("doVinesSpread", "minecraft:spread_vines", GameRuleRegistryFix::convertBoolean).renameAndFixField("doWardenSpawning", "minecraft:spawn_wardens", GameRuleRegistryFix::convertBoolean).renameAndFixField("doWeatherCycle", "minecraft:advance_weather", GameRuleRegistryFix::convertBoolean).renameAndFixField("drowningDamage", "minecraft:drowning_damage", GameRuleRegistryFix::convertBoolean).renameAndFixField("enderPearlsVanishOnDeath", "minecraft:ender_pearls_vanish_on_death", GameRuleRegistryFix::convertBoolean).renameAndFixField("fallDamage", "minecraft:fall_damage", GameRuleRegistryFix::convertBoolean).renameAndFixField("fireDamage", "minecraft:fire_damage", GameRuleRegistryFix::convertBoolean).renameAndFixField("forgiveDeadPlayers", "minecraft:forgive_dead_players", GameRuleRegistryFix::convertBoolean).renameAndFixField("freezeDamage", "minecraft:freeze_damage", GameRuleRegistryFix::convertBoolean).renameAndFixField("globalSoundEvents", "minecraft:global_sound_events", GameRuleRegistryFix::convertBoolean).renameAndFixField("keepInventory", "minecraft:keep_inventory", GameRuleRegistryFix::convertBoolean).renameAndFixField("lavaSourceConversion", "minecraft:lava_source_conversion", GameRuleRegistryFix::convertBoolean).renameAndFixField("locatorBar", "minecraft:locator_bar", GameRuleRegistryFix::convertBoolean).renameAndFixField("logAdminCommands", "minecraft:log_admin_commands", GameRuleRegistryFix::convertBoolean).renameAndFixField("maxCommandChainLength", "minecraft:max_command_sequence_length", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("maxCommandForkCount", "minecraft:max_command_forks", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("maxEntityCramming", "minecraft:max_entity_cramming", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("minecartMaxSpeed", "minecraft:max_minecart_speed", GameRuleRegistryFix::convertInteger).renameAndFixField("mobExplosionDropDecay", "minecraft:mob_explosion_drop_decay", GameRuleRegistryFix::convertBoolean).renameAndFixField("mobGriefing", "minecraft:mob_griefing", GameRuleRegistryFix::convertBoolean).renameAndFixField("naturalRegeneration", "minecraft:natural_health_regeneration", GameRuleRegistryFix::convertBoolean).renameAndFixField("playersNetherPortalCreativeDelay", "minecraft:players_nether_portal_creative_delay", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("playersNetherPortalDefaultDelay", "minecraft:players_nether_portal_default_delay", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("playersSleepingPercentage", "minecraft:players_sleeping_percentage", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("projectilesCanBreakBlocks", "minecraft:projectiles_can_break_blocks", GameRuleRegistryFix::convertBoolean).renameAndFixField("pvp", "minecraft:pvp", GameRuleRegistryFix::convertBoolean).renameAndFixField("randomTickSpeed", "minecraft:random_tick_speed", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0)).renameAndFixField("reducedDebugInfo", "minecraft:reduced_debug_info", GameRuleRegistryFix::convertBoolean).renameAndFixField("sendCommandFeedback", "minecraft:send_command_feedback", GameRuleRegistryFix::convertBoolean).renameAndFixField("showDeathMessages", "minecraft:show_death_messages", GameRuleRegistryFix::convertBoolean).renameAndFixField("snowAccumulationHeight", "minecraft:max_snow_accumulation_height", $$0 -> GameRuleRegistryFix.convertInteger($$0, 0, 8)).renameAndFixField("spawnMonsters", "minecraft:spawn_monsters", GameRuleRegistryFix::convertBoolean).renameAndFixField("spawnRadius", "minecraft:respawn_radius", GameRuleRegistryFix::convertInteger).renameAndFixField("spawnerBlocksEnabled", "minecraft:spawner_blocks_work", GameRuleRegistryFix::convertBoolean).renameAndFixField("spectatorsGenerateChunks", "minecraft:spectators_generate_chunks", GameRuleRegistryFix::convertBoolean).renameAndFixField("tntExplodes", "minecraft:tnt_explodes", GameRuleRegistryFix::convertBoolean).renameAndFixField("tntExplosionDropDecay", "minecraft:tnt_explosion_drop_decay", GameRuleRegistryFix::convertBoolean).renameAndFixField("universalAnger", "minecraft:universal_anger", GameRuleRegistryFix::convertBoolean).renameAndFixField("waterSourceConversion", "minecraft:water_source_conversion", GameRuleRegistryFix::convertBoolean);
        })));
    }

    private static Dynamic<?> convertInteger(Dynamic<?> $$0) {
        return GameRuleRegistryFix.convertInteger($$0, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static Dynamic<?> convertInteger(Dynamic<?> $$0, int $$1) {
        return GameRuleRegistryFix.convertInteger($$0, $$1, Integer.MAX_VALUE);
    }

    private static Dynamic<?> convertInteger(Dynamic<?> $$0, int $$1, int $$2) {
        String $$3 = $$0.asString("");
        try {
            int $$4 = Integer.parseInt($$3);
            return $$0.createInt(Mth.clamp($$4, $$1, $$2));
        }
        catch (NumberFormatException $$5) {
            return $$0;
        }
    }

    private static Dynamic<?> convertBoolean(Dynamic<?> $$0) {
        return $$0.createBoolean(Boolean.parseBoolean($$0.asString("")));
    }

    private static Dynamic<?> convertBooleanInverted(Dynamic<?> $$0) {
        return $$0.createBoolean(!Boolean.parseBoolean($$0.asString("")));
    }
}

