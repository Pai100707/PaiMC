package net.minecraft.world.attribute;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.MoonPhase;

public interface EnvironmentAttributes {
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> FOG_COLOR = register(
      "visual/fog_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.RGB_COLOR)
         .defaultValue(0)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> FOG_START_DISTANCE = register(
      "visual/fog_start_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(0.0F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> FOG_END_DISTANCE = register(
      "visual/fog_end_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(1024.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.NON_NEGATIVE_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> SKY_FOG_END_DISTANCE = register(
      "visual/sky_fog_end_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(512.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.NON_NEGATIVE_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> CLOUD_FOG_END_DISTANCE = register(
      "visual/cloud_fog_end_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(2048.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.NON_NEGATIVE_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> WATER_FOG_COLOR = register(
      "visual/water_fog_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.RGB_COLOR)
         .defaultValue(-16448205)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> WATER_FOG_START_DISTANCE = register(
      "visual/water_fog_start_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(-8.0F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> WATER_FOG_END_DISTANCE = register(
      "visual/water_fog_end_distance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(96.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.NON_NEGATIVE_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> SKY_COLOR = register(
      "visual/sky_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.RGB_COLOR)
         .defaultValue(0)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> SUNRISE_SUNSET_COLOR = register(
      "visual/sunrise_sunset_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ARGB_COLOR)
         .defaultValue(0)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> CLOUD_COLOR = register(
      "visual/cloud_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ARGB_COLOR)
         .defaultValue(0)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> CLOUD_HEIGHT = register(
      "visual/cloud_height",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(192.33F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> SUN_ANGLE = register(
      "visual/sun_angle",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ANGLE_DEGREES)
         .defaultValue(0.0F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> MOON_ANGLE = register(
      "visual/moon_angle",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ANGLE_DEGREES)
         .defaultValue(0.0F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> STAR_ANGLE = register(
      "visual/star_angle",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ANGLE_DEGREES)
         .defaultValue(0.0F)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<MoonPhase> MOON_PHASE = register(
      "visual/moon_phase",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.MOON_PHASE)
         .defaultValue(MoonPhase.FULL_MOON)
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> STAR_BRIGHTNESS = register(
      "visual/star_brightness",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(0.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Integer> SKY_LIGHT_COLOR = register(
      "visual/sky_light_color",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.RGB_COLOR)
         .defaultValue(-1)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> SKY_LIGHT_FACTOR = register(
      "visual/sky_light_factor",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(1.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
         .spatiallyInterpolated()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<ParticleOptions> DEFAULT_DRIPSTONE_PARTICLE = register(
      "visual/default_dripstone_particle",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.PARTICLE)
         .defaultValue(ParticleTypes.DRIPPING_DRIPSTONE_WATER)
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<List<net.minecraft.world.attribute.AmbientParticle>> AMBIENT_PARTICLES = register(
      "visual/ambient_particles",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.AMBIENT_PARTICLES)
         .defaultValue(List.of())
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<net.minecraft.world.attribute.BackgroundMusic> BACKGROUND_MUSIC = register(
      "audio/background_music",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BACKGROUND_MUSIC)
         .defaultValue(net.minecraft.world.attribute.BackgroundMusic.EMPTY)
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> MUSIC_VOLUME = register(
      "audio/music_volume",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(1.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<net.minecraft.world.attribute.AmbientSounds> AMBIENT_SOUNDS = register(
      "audio/ambient_sounds",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.AMBIENT_SOUNDS)
         .defaultValue(net.minecraft.world.attribute.AmbientSounds.EMPTY)
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> FIREFLY_BUSH_SOUNDS = register(
      "audio/firefly_bush_sounds",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false).syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> SKY_LIGHT_LEVEL = register(
      "gameplay/sky_light_level",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(15.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.ofFloat(0.0F, 15.0F))
         .notPositional()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> CAN_START_RAID = register(
      "gameplay/can_start_raid",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(true)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> WATER_EVAPORATES = register(
      "gameplay/water_evaporates",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false).syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<net.minecraft.world.attribute.BedRule> BED_RULE = register(
      "gameplay/bed_rule",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BED_RULE)
         .defaultValue(net.minecraft.world.attribute.BedRule.CAN_SLEEP_WHEN_DARK)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> RESPAWN_ANCHOR_WORKS = register(
      "gameplay/respawn_anchor_works",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> NETHER_PORTAL_SPAWNS_PIGLINS = register(
      "gameplay/nether_portal_spawns_piglin",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> FAST_LAVA = register(
      "gameplay/fast_lava",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN)
         .defaultValue(false)
         .notPositional()
         .syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> INCREASED_FIRE_BURNOUT = register(
      "gameplay/increased_fire_burnout",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<TriState> EYEBLOSSOM_OPEN = register(
      "gameplay/eyeblossom_open",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.TRI_STATE).defaultValue(TriState.DEFAULT)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> TURTLE_EGG_HATCH_CHANCE = register(
      "gameplay/turtle_egg_hatch_chance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(0.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> PIGLINS_ZOMBIFY = register(
      "gameplay/piglins_zombify",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(true).syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> SNOW_GOLEM_MELTS = register(
      "gameplay/snow_golem_melts",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> CREAKING_ACTIVE = register(
      "gameplay/creaking_active",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false).syncable()
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> SURFACE_SLIME_SPAWN_CHANCE = register(
      "gameplay/surface_slime_spawn_chance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(0.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Float> CAT_WAKING_UP_GIFT_CHANCE = register(
      "gameplay/cat_waking_up_gift_chance",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.FLOAT)
         .defaultValue(0.0F)
         .valueRange(net.minecraft.world.attribute.AttributeRange.UNIT_FLOAT)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> BEES_STAY_IN_HIVE = register(
      "gameplay/bees_stay_in_hive",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> MONSTERS_BURN = register(
      "gameplay/monsters_burn",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(false)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Boolean> CAN_PILLAGER_PATROL_SPAWN = register(
      "gameplay/can_pillager_patrol_spawn",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.BOOLEAN).defaultValue(true)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Activity> VILLAGER_ACTIVITY = register(
      "gameplay/villager_activity",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ACTIVITY).defaultValue(Activity.IDLE)
   );
   net.minecraft.world.attribute.EnvironmentAttribute<Activity> BABY_VILLAGER_ACTIVITY = register(
      "gameplay/baby_villager_activity",
      net.minecraft.world.attribute.EnvironmentAttribute.builder(net.minecraft.world.attribute.AttributeTypes.ACTIVITY).defaultValue(Activity.IDLE)
   );
   Codec<net.minecraft.world.attribute.EnvironmentAttribute<?>> CODEC = BuiltInRegistries.ENVIRONMENT_ATTRIBUTE.byNameCodec();

   static net.minecraft.world.attribute.EnvironmentAttribute<?> bootstrap(Registry<net.minecraft.world.attribute.EnvironmentAttribute<?>> $$0) {
      return RESPAWN_ANCHOR_WORKS;
   }

   private static <Value> net.minecraft.world.attribute.EnvironmentAttribute<Value> register(
      String $$0, net.minecraft.world.attribute.EnvironmentAttribute.Builder<Value> $$1
   ) {
      net.minecraft.world.attribute.EnvironmentAttribute<Value> $$2 = $$1.build();
      Registry.register(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE, Identifier.withDefaultNamespace($$0), $$2);
      return $$2;
   }
}
