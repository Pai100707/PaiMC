package net.minecraft.world.damagesource;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public interface DamageTypes {
   ResourceKey<net.minecraft.world.damagesource.DamageType> IN_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("in_fire"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> CAMPFIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("campfire"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> LIGHTNING_BOLT = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("lightning_bolt")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> ON_FIRE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("on_fire"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> LAVA = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("lava"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> HOT_FLOOR = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("hot_floor"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> IN_WALL = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("in_wall"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> CRAMMING = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("cramming"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> DROWN = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("drown"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> STARVE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("starve"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> CACTUS = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("cactus"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> FALL = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("fall"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> ENDER_PEARL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("ender_pearl")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FLY_INTO_WALL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("fly_into_wall")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FELL_OUT_OF_WORLD = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("out_of_world")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> GENERIC = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("generic"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> MAGIC = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("magic"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> WITHER = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("wither"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> DRAGON_BREATH = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("dragon_breath")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> DRY_OUT = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("dry_out"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> SWEET_BERRY_BUSH = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("sweet_berry_bush")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FREEZE = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("freeze"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> STALAGMITE = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("stalagmite")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FALLING_BLOCK = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("falling_block")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FALLING_ANVIL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("falling_anvil")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FALLING_STALACTITE = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("falling_stalactite")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> STING = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("sting"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> MOB_ATTACK = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("mob_attack")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> MOB_ATTACK_NO_AGGRO = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("mob_attack_no_aggro")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> PLAYER_ATTACK = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("player_attack")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> SPEAR = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("spear"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> ARROW = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("arrow"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> TRIDENT = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("trident"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> MOB_PROJECTILE = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("mob_projectile")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> SPIT = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("spit"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> WIND_CHARGE = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("wind_charge")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> FIREWORKS = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("fireworks"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> FIREBALL = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("fireball"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> UNATTRIBUTED_FIREBALL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("unattributed_fireball")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> WITHER_SKULL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("wither_skull")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> THROWN = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("thrown"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> INDIRECT_MAGIC = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("indirect_magic")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> THORNS = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("thorns"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> EXPLOSION = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("explosion"));
   ResourceKey<net.minecraft.world.damagesource.DamageType> PLAYER_EXPLOSION = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("player_explosion")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> SONIC_BOOM = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("sonic_boom")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> BAD_RESPAWN_POINT = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("bad_respawn_point")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> OUTSIDE_BORDER = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("outside_border")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> GENERIC_KILL = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("generic_kill")
   );
   ResourceKey<net.minecraft.world.damagesource.DamageType> MACE_SMASH = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.withDefaultNamespace("mace_smash")
   );

   static void bootstrap(BootstrapContext<net.minecraft.world.damagesource.DamageType> $$0) {
      $$0.register(IN_FIRE, new net.minecraft.world.damagesource.DamageType("inFire", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(CAMPFIRE, new net.minecraft.world.damagesource.DamageType("inFire", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(LIGHTNING_BOLT, new net.minecraft.world.damagesource.DamageType("lightningBolt", 0.1F));
      $$0.register(ON_FIRE, new net.minecraft.world.damagesource.DamageType("onFire", 0.0F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(LAVA, new net.minecraft.world.damagesource.DamageType("lava", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(HOT_FLOOR, new net.minecraft.world.damagesource.DamageType("hotFloor", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(IN_WALL, new net.minecraft.world.damagesource.DamageType("inWall", 0.0F));
      $$0.register(CRAMMING, new net.minecraft.world.damagesource.DamageType("cramming", 0.0F));
      $$0.register(DROWN, new net.minecraft.world.damagesource.DamageType("drown", 0.0F, net.minecraft.world.damagesource.DamageEffects.DROWNING));
      $$0.register(STARVE, new net.minecraft.world.damagesource.DamageType("starve", 0.0F));
      $$0.register(CACTUS, new net.minecraft.world.damagesource.DamageType("cactus", 0.1F));
      $$0.register(
         FALL,
         new net.minecraft.world.damagesource.DamageType(
            "fall",
            net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
            0.0F,
            net.minecraft.world.damagesource.DamageEffects.HURT,
            net.minecraft.world.damagesource.DeathMessageType.FALL_VARIANTS
         )
      );
      $$0.register(
         ENDER_PEARL,
         new net.minecraft.world.damagesource.DamageType(
            "fall",
            net.minecraft.world.damagesource.DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
            0.0F,
            net.minecraft.world.damagesource.DamageEffects.HURT,
            net.minecraft.world.damagesource.DeathMessageType.FALL_VARIANTS
         )
      );
      $$0.register(FLY_INTO_WALL, new net.minecraft.world.damagesource.DamageType("flyIntoWall", 0.0F));
      $$0.register(FELL_OUT_OF_WORLD, new net.minecraft.world.damagesource.DamageType("outOfWorld", 0.0F));
      $$0.register(GENERIC, new net.minecraft.world.damagesource.DamageType("generic", 0.0F));
      $$0.register(MAGIC, new net.minecraft.world.damagesource.DamageType("magic", 0.0F));
      $$0.register(WITHER, new net.minecraft.world.damagesource.DamageType("wither", 0.0F));
      $$0.register(DRAGON_BREATH, new net.minecraft.world.damagesource.DamageType("dragonBreath", 0.0F));
      $$0.register(DRY_OUT, new net.minecraft.world.damagesource.DamageType("dryout", 0.1F));
      $$0.register(
         SWEET_BERRY_BUSH, new net.minecraft.world.damagesource.DamageType("sweetBerryBush", 0.1F, net.minecraft.world.damagesource.DamageEffects.POKING)
      );
      $$0.register(FREEZE, new net.minecraft.world.damagesource.DamageType("freeze", 0.0F, net.minecraft.world.damagesource.DamageEffects.FREEZING));
      $$0.register(STALAGMITE, new net.minecraft.world.damagesource.DamageType("stalagmite", 0.0F));
      $$0.register(FALLING_BLOCK, new net.minecraft.world.damagesource.DamageType("fallingBlock", 0.1F));
      $$0.register(FALLING_ANVIL, new net.minecraft.world.damagesource.DamageType("anvil", 0.1F));
      $$0.register(FALLING_STALACTITE, new net.minecraft.world.damagesource.DamageType("fallingStalactite", 0.1F));
      $$0.register(STING, new net.minecraft.world.damagesource.DamageType("sting", 0.1F));
      $$0.register(MOB_ATTACK, new net.minecraft.world.damagesource.DamageType("mob", 0.1F));
      $$0.register(MOB_ATTACK_NO_AGGRO, new net.minecraft.world.damagesource.DamageType("mob", 0.1F));
      $$0.register(PLAYER_ATTACK, new net.minecraft.world.damagesource.DamageType("player", 0.1F));
      $$0.register(SPEAR, new net.minecraft.world.damagesource.DamageType("spear", 0.1F));
      $$0.register(ARROW, new net.minecraft.world.damagesource.DamageType("arrow", 0.1F));
      $$0.register(TRIDENT, new net.minecraft.world.damagesource.DamageType("trident", 0.1F));
      $$0.register(MOB_PROJECTILE, new net.minecraft.world.damagesource.DamageType("mob", 0.1F));
      $$0.register(SPIT, new net.minecraft.world.damagesource.DamageType("mob", 0.1F));
      $$0.register(FIREWORKS, new net.minecraft.world.damagesource.DamageType("fireworks", 0.1F));
      $$0.register(
         UNATTRIBUTED_FIREBALL, new net.minecraft.world.damagesource.DamageType("onFire", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING)
      );
      $$0.register(FIREBALL, new net.minecraft.world.damagesource.DamageType("fireball", 0.1F, net.minecraft.world.damagesource.DamageEffects.BURNING));
      $$0.register(WITHER_SKULL, new net.minecraft.world.damagesource.DamageType("witherSkull", 0.1F));
      $$0.register(THROWN, new net.minecraft.world.damagesource.DamageType("thrown", 0.1F));
      $$0.register(INDIRECT_MAGIC, new net.minecraft.world.damagesource.DamageType("indirectMagic", 0.0F));
      $$0.register(THORNS, new net.minecraft.world.damagesource.DamageType("thorns", 0.1F, net.minecraft.world.damagesource.DamageEffects.THORNS));
      $$0.register(EXPLOSION, new net.minecraft.world.damagesource.DamageType("explosion", net.minecraft.world.damagesource.DamageScaling.ALWAYS, 0.1F));
      $$0.register(
         PLAYER_EXPLOSION, new net.minecraft.world.damagesource.DamageType("explosion.player", net.minecraft.world.damagesource.DamageScaling.ALWAYS, 0.1F)
      );
      $$0.register(SONIC_BOOM, new net.minecraft.world.damagesource.DamageType("sonic_boom", net.minecraft.world.damagesource.DamageScaling.ALWAYS, 0.0F));
      $$0.register(
         BAD_RESPAWN_POINT,
         new net.minecraft.world.damagesource.DamageType(
            "badRespawnPoint",
            net.minecraft.world.damagesource.DamageScaling.ALWAYS,
            0.1F,
            net.minecraft.world.damagesource.DamageEffects.HURT,
            net.minecraft.world.damagesource.DeathMessageType.INTENTIONAL_GAME_DESIGN
         )
      );
      $$0.register(OUTSIDE_BORDER, new net.minecraft.world.damagesource.DamageType("outsideBorder", 0.0F));
      $$0.register(GENERIC_KILL, new net.minecraft.world.damagesource.DamageType("genericKill", 0.0F));
      $$0.register(WIND_CHARGE, new net.minecraft.world.damagesource.DamageType("mob", 0.1F));
      $$0.register(MACE_SMASH, new net.minecraft.world.damagesource.DamageType("mace_smash", 0.1F));
   }
}
