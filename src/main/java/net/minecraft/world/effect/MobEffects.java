package net.minecraft.world.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

public class MobEffects {
   private static final int DARKNESS_EFFECT_FACTOR_PADDING_DURATION_TICKS = 22;
   public static final Holder<net.minecraft.world.effect.MobEffect> SPEED = register(
      "speed",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 3402751)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.withDefaultNamespace("effect.speed"), 0.2F, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> SLOWNESS = register(
      "slowness",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 9154528)
         .addAttributeModifier(Attributes.MOVEMENT_SPEED, Identifier.withDefaultNamespace("effect.slowness"), -0.15F, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> HASTE = register(
      "haste",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 14270531)
         .addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.withDefaultNamespace("effect.haste"), 0.1F, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> MINING_FATIGUE = register(
      "mining_fatigue",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 4866583)
         .addAttributeModifier(Attributes.ATTACK_SPEED, Identifier.withDefaultNamespace("effect.mining_fatigue"), -0.1F, Operation.ADD_MULTIPLIED_TOTAL)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> STRENGTH = register(
      "strength",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16762624)
         .addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.strength"), 3.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> INSTANT_HEALTH = register(
      "instant_health", new net.minecraft.world.effect.HealOrHarmMobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16262179, false)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> INSTANT_DAMAGE = register(
      "instant_damage", new net.minecraft.world.effect.HealOrHarmMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 11101546, true)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> JUMP_BOOST = register(
      "jump_boost",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16646020)
         .addAttributeModifier(Attributes.SAFE_FALL_DISTANCE, Identifier.withDefaultNamespace("effect.jump_boost"), 1.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> NAUSEA = register(
      "nausea", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 5578058).setBlendDuration(150, 20, 60)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> REGENERATION = register(
      "regeneration", new net.minecraft.world.effect.RegenerationMobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 13458603)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> RESISTANCE = register(
      "resistance", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 9520880)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> FIRE_RESISTANCE = register(
      "fire_resistance", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16750848)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> WATER_BREATHING = register(
      "water_breathing", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 10017472)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> INVISIBILITY = register(
      "invisibility",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16185078)
         .addAttributeModifier(
            Attributes.WAYPOINT_TRANSMIT_RANGE, Identifier.withDefaultNamespace("effect.waypoint_transmit_range_hide"), -1.0, Operation.ADD_MULTIPLIED_TOTAL
         )
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> BLINDNESS = register(
      "blindness", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 2039587)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> NIGHT_VISION = register(
      "night_vision", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 12779366)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> HUNGER = register(
      "hunger", new net.minecraft.world.effect.HungerMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 5797459)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> WEAKNESS = register(
      "weakness",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 4738376)
         .addAttributeModifier(Attributes.ATTACK_DAMAGE, Identifier.withDefaultNamespace("effect.weakness"), -4.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> POISON = register(
      "poison", new net.minecraft.world.effect.PoisonMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 8889187)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> WITHER = register(
      "wither", new net.minecraft.world.effect.WitherMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 7561558)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> HEALTH_BOOST = register(
      "health_boost",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16284963)
         .addAttributeModifier(Attributes.MAX_HEALTH, Identifier.withDefaultNamespace("effect.health_boost"), 4.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> ABSORPTION = register(
      "absorption",
      new net.minecraft.world.effect.AbsorptionMobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 2445989)
         .addAttributeModifier(Attributes.MAX_ABSORPTION, Identifier.withDefaultNamespace("effect.absorption"), 4.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> SATURATION = register(
      "saturation", new net.minecraft.world.effect.SaturationMobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 16262179)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> GLOWING = register(
      "glowing", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 9740385)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> LEVITATION = register(
      "levitation", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 13565951)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> LUCK = register(
      "luck",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 5882118)
         .addAttributeModifier(Attributes.LUCK, Identifier.withDefaultNamespace("effect.luck"), 1.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> UNLUCK = register(
      "unluck",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 12624973)
         .addAttributeModifier(Attributes.LUCK, Identifier.withDefaultNamespace("effect.unluck"), -1.0, Operation.ADD_VALUE)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> SLOW_FALLING = register(
      "slow_falling", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 15978425)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> CONDUIT_POWER = register(
      "conduit_power", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 1950417)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> DOLPHINS_GRACE = register(
      "dolphins_grace", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 8954814)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> BAD_OMEN = register(
      "bad_omen",
      new net.minecraft.world.effect.BadOmenMobEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 745784)
         .withSoundOnAdded(SoundEvents.APPLY_EFFECT_BAD_OMEN)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> HERO_OF_THE_VILLAGE = register(
      "hero_of_the_village", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 4521796)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> DARKNESS = register(
      "darkness", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 2696993).setBlendDuration(22)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> TRIAL_OMEN = register(
      "trial_omen",
      new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 1484454, ParticleTypes.TRIAL_OMEN)
         .withSoundOnAdded(SoundEvents.APPLY_EFFECT_TRIAL_OMEN)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> RAID_OMEN = register(
      "raid_omen",
      new net.minecraft.world.effect.RaidOmenMobEffect(net.minecraft.world.effect.MobEffectCategory.NEUTRAL, 14565464, ParticleTypes.RAID_OMEN)
         .withSoundOnAdded(SoundEvents.APPLY_EFFECT_RAID_OMEN)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> WIND_CHARGED = register(
      "wind_charged", new net.minecraft.world.effect.WindChargedMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 12438015)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> WEAVING = register(
      "weaving",
      new net.minecraft.world.effect.WeavingMobEffect(
         net.minecraft.world.effect.MobEffectCategory.HARMFUL, 7891290, $$0 -> Mth.randomBetweenInclusive($$0, 2, 3)
      )
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> OOZING = register(
      "oozing", new net.minecraft.world.effect.OozingMobEffect(net.minecraft.world.effect.MobEffectCategory.HARMFUL, 10092451, $$0 -> 2)
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> INFESTED = register(
      "infested",
      new net.minecraft.world.effect.InfestedMobEffect(
         net.minecraft.world.effect.MobEffectCategory.HARMFUL, 9214860, 0.1F, $$0 -> Mth.randomBetweenInclusive($$0, 1, 2)
      )
   );
   public static final Holder<net.minecraft.world.effect.MobEffect> BREATH_OF_THE_NAUTILUS = register(
      "breath_of_the_nautilus", new net.minecraft.world.effect.MobEffect(net.minecraft.world.effect.MobEffectCategory.BENEFICIAL, 65518)
   );

   private static Holder<net.minecraft.world.effect.MobEffect> register(String $$0, net.minecraft.world.effect.MobEffect $$1) {
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Identifier.withDefaultNamespace($$0), $$1);
   }

   public static Holder<net.minecraft.world.effect.MobEffect> bootstrap(Registry<net.minecraft.world.effect.MobEffect> $$0) {
      return SPEED;
   }
}
