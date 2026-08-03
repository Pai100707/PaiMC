package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;

public class DefaultAttributes {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity>, AttributeSupplier> SUPPLIERS = ImmutableMap.builder()
      .put(net.minecraft.world.entity.EntityType.ALLAY, Allay.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ARMADILLO, Armadillo.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ARMOR_STAND, ArmorStand.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.AXOLOTL, Axolotl.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.BAT, Bat.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.BEE, Bee.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.BLAZE, Blaze.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.BOGGED, Bogged.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CAT, Cat.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CAMEL, Camel.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CAMEL_HUSK, Camel.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CAVE_SPIDER, CaveSpider.createCaveSpider().build())
      .put(net.minecraft.world.entity.EntityType.CHICKEN, Chicken.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.COD, AbstractFish.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.COPPER_GOLEM, CopperGolem.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.COW, Cow.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CREAKING, Creaking.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.CREEPER, Creeper.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.DOLPHIN, Dolphin.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.DONKEY, AbstractChestedHorse.createBaseChestedHorseAttributes().build())
      .put(net.minecraft.world.entity.EntityType.DROWNED, Drowned.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ELDER_GUARDIAN, ElderGuardian.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ENDERMAN, EnderMan.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ENDERMITE, Endermite.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ENDER_DRAGON, EnderDragon.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.EVOKER, Evoker.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.BREEZE, Breeze.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.FOX, Fox.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.FROG, Frog.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.GHAST, Ghast.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.HAPPY_GHAST, HappyGhast.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.GIANT, Giant.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.GLOW_SQUID, GlowSquid.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.GOAT, Goat.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.GUARDIAN, Guardian.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.HOGLIN, Hoglin.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.HORSE, AbstractHorse.createBaseHorseAttributes().build())
      .put(net.minecraft.world.entity.EntityType.HUSK, Zombie.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ILLUSIONER, Illusioner.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.IRON_GOLEM, IronGolem.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.LLAMA, Llama.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.MAGMA_CUBE, MagmaCube.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.MANNEQUIN, net.minecraft.world.entity.LivingEntity.createLivingAttributes().build())
      .put(net.minecraft.world.entity.EntityType.MOOSHROOM, Cow.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.MULE, AbstractChestedHorse.createBaseChestedHorseAttributes().build())
      .put(net.minecraft.world.entity.EntityType.NAUTILUS, Nautilus.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.OCELOT, Ocelot.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PANDA, Panda.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PARCHED, Parched.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PARROT, Parrot.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PHANTOM, Monster.createMonsterAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PIG, Pig.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PIGLIN, Piglin.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PIGLIN_BRUTE, PiglinBrute.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PILLAGER, Pillager.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PLAYER, Player.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.POLAR_BEAR, PolarBear.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.PUFFERFISH, AbstractFish.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.RABBIT, Rabbit.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.RAVAGER, Ravager.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SALMON, AbstractFish.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SHEEP, Sheep.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SHULKER, Shulker.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SILVERFISH, Silverfish.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SKELETON, AbstractSkeleton.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SKELETON_HORSE, SkeletonHorse.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SLIME, Monster.createMonsterAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SNIFFER, Sniffer.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SNOW_GOLEM, SnowGolem.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SPIDER, Spider.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.SQUID, Squid.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.STRAY, AbstractSkeleton.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.STRIDER, Strider.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.TADPOLE, Tadpole.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.TRADER_LLAMA, Llama.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.TROPICAL_FISH, AbstractFish.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.TURTLE, Turtle.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.VEX, Vex.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.VILLAGER, Villager.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.VINDICATOR, Vindicator.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WARDEN, Warden.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WANDERING_TRADER, net.minecraft.world.entity.Mob.createMobAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WITCH, Witch.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WITHER, WitherBoss.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WITHER_SKELETON, AbstractSkeleton.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.WOLF, Wolf.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOGLIN, Zoglin.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOMBIE, Zombie.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOMBIE_HORSE, ZombieHorse.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOMBIE_NAUTILUS, ZombieNautilus.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER, Zombie.createAttributes().build())
      .put(net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN, ZombifiedPiglin.createAttributes().build())
      .build();

   public static AttributeSupplier getSupplier(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity> $$0) {
      return SUPPLIERS.get($$0);
   }

   public static boolean hasSupplier(net.minecraft.world.entity.EntityType<?> $$0) {
      return SUPPLIERS.containsKey($$0);
   }

   public static void validate() {
      BuiltInRegistries.ENTITY_TYPE
         .stream()
         .filter($$0 -> $$0.getCategory() != net.minecraft.world.entity.MobCategory.MISC)
         .filter($$0 -> !hasSupplier((net.minecraft.world.entity.EntityType<?>)$$0))
         .<Identifier>map(BuiltInRegistries.ENTITY_TYPE::getKey)
         .forEach($$0 -> Util.logAndPauseIfInIde("Entity " + $$0 + " has no attributes"));
   }
}
