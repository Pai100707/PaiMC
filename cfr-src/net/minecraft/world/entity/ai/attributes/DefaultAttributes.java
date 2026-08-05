/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
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
    private static final Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS = ImmutableMap.builder().put(EntityType.ALLAY, (Object)Allay.createAttributes().build()).put(EntityType.ARMADILLO, (Object)Armadillo.createAttributes().build()).put(EntityType.ARMOR_STAND, (Object)ArmorStand.createAttributes().build()).put(EntityType.AXOLOTL, (Object)Axolotl.createAttributes().build()).put(EntityType.BAT, (Object)Bat.createAttributes().build()).put(EntityType.BEE, (Object)Bee.createAttributes().build()).put(EntityType.BLAZE, (Object)Blaze.createAttributes().build()).put(EntityType.BOGGED, (Object)Bogged.createAttributes().build()).put(EntityType.CAT, (Object)Cat.createAttributes().build()).put(EntityType.CAMEL, (Object)Camel.createAttributes().build()).put(EntityType.CAMEL_HUSK, (Object)Camel.createAttributes().build()).put(EntityType.CAVE_SPIDER, (Object)CaveSpider.createCaveSpider().build()).put(EntityType.CHICKEN, (Object)Chicken.createAttributes().build()).put(EntityType.COD, (Object)AbstractFish.createAttributes().build()).put(EntityType.COPPER_GOLEM, (Object)CopperGolem.createAttributes().build()).put(EntityType.COW, (Object)Cow.createAttributes().build()).put(EntityType.CREAKING, (Object)Creaking.createAttributes().build()).put(EntityType.CREEPER, (Object)Creeper.createAttributes().build()).put(EntityType.DOLPHIN, (Object)Dolphin.createAttributes().build()).put(EntityType.DONKEY, (Object)AbstractChestedHorse.createBaseChestedHorseAttributes().build()).put(EntityType.DROWNED, (Object)Drowned.createAttributes().build()).put(EntityType.ELDER_GUARDIAN, (Object)ElderGuardian.createAttributes().build()).put(EntityType.ENDERMAN, (Object)EnderMan.createAttributes().build()).put(EntityType.ENDERMITE, (Object)Endermite.createAttributes().build()).put(EntityType.ENDER_DRAGON, (Object)EnderDragon.createAttributes().build()).put(EntityType.EVOKER, (Object)Evoker.createAttributes().build()).put(EntityType.BREEZE, (Object)Breeze.createAttributes().build()).put(EntityType.FOX, (Object)Fox.createAttributes().build()).put(EntityType.FROG, (Object)Frog.createAttributes().build()).put(EntityType.GHAST, (Object)Ghast.createAttributes().build()).put(EntityType.HAPPY_GHAST, (Object)HappyGhast.createAttributes().build()).put(EntityType.GIANT, (Object)Giant.createAttributes().build()).put(EntityType.GLOW_SQUID, (Object)GlowSquid.createAttributes().build()).put(EntityType.GOAT, (Object)Goat.createAttributes().build()).put(EntityType.GUARDIAN, (Object)Guardian.createAttributes().build()).put(EntityType.HOGLIN, (Object)Hoglin.createAttributes().build()).put(EntityType.HORSE, (Object)AbstractHorse.createBaseHorseAttributes().build()).put(EntityType.HUSK, (Object)Zombie.createAttributes().build()).put(EntityType.ILLUSIONER, (Object)Illusioner.createAttributes().build()).put(EntityType.IRON_GOLEM, (Object)IronGolem.createAttributes().build()).put(EntityType.LLAMA, (Object)Llama.createAttributes().build()).put(EntityType.MAGMA_CUBE, (Object)MagmaCube.createAttributes().build()).put(EntityType.MANNEQUIN, (Object)LivingEntity.createLivingAttributes().build()).put(EntityType.MOOSHROOM, (Object)Cow.createAttributes().build()).put(EntityType.MULE, (Object)AbstractChestedHorse.createBaseChestedHorseAttributes().build()).put(EntityType.NAUTILUS, (Object)Nautilus.createAttributes().build()).put(EntityType.OCELOT, (Object)Ocelot.createAttributes().build()).put(EntityType.PANDA, (Object)Panda.createAttributes().build()).put(EntityType.PARCHED, (Object)Parched.createAttributes().build()).put(EntityType.PARROT, (Object)Parrot.createAttributes().build()).put(EntityType.PHANTOM, (Object)Monster.createMonsterAttributes().build()).put(EntityType.PIG, (Object)Pig.createAttributes().build()).put(EntityType.PIGLIN, (Object)Piglin.createAttributes().build()).put(EntityType.PIGLIN_BRUTE, (Object)PiglinBrute.createAttributes().build()).put(EntityType.PILLAGER, (Object)Pillager.createAttributes().build()).put(EntityType.PLAYER, (Object)Player.createAttributes().build()).put(EntityType.POLAR_BEAR, (Object)PolarBear.createAttributes().build()).put(EntityType.PUFFERFISH, (Object)AbstractFish.createAttributes().build()).put(EntityType.RABBIT, (Object)Rabbit.createAttributes().build()).put(EntityType.RAVAGER, (Object)Ravager.createAttributes().build()).put(EntityType.SALMON, (Object)AbstractFish.createAttributes().build()).put(EntityType.SHEEP, (Object)Sheep.createAttributes().build()).put(EntityType.SHULKER, (Object)Shulker.createAttributes().build()).put(EntityType.SILVERFISH, (Object)Silverfish.createAttributes().build()).put(EntityType.SKELETON, (Object)AbstractSkeleton.createAttributes().build()).put(EntityType.SKELETON_HORSE, (Object)SkeletonHorse.createAttributes().build()).put(EntityType.SLIME, (Object)Monster.createMonsterAttributes().build()).put(EntityType.SNIFFER, (Object)Sniffer.createAttributes().build()).put(EntityType.SNOW_GOLEM, (Object)SnowGolem.createAttributes().build()).put(EntityType.SPIDER, (Object)Spider.createAttributes().build()).put(EntityType.SQUID, (Object)Squid.createAttributes().build()).put(EntityType.STRAY, (Object)AbstractSkeleton.createAttributes().build()).put(EntityType.STRIDER, (Object)Strider.createAttributes().build()).put(EntityType.TADPOLE, (Object)Tadpole.createAttributes().build()).put(EntityType.TRADER_LLAMA, (Object)Llama.createAttributes().build()).put(EntityType.TROPICAL_FISH, (Object)AbstractFish.createAttributes().build()).put(EntityType.TURTLE, (Object)Turtle.createAttributes().build()).put(EntityType.VEX, (Object)Vex.createAttributes().build()).put(EntityType.VILLAGER, (Object)Villager.createAttributes().build()).put(EntityType.VINDICATOR, (Object)Vindicator.createAttributes().build()).put(EntityType.WARDEN, (Object)Warden.createAttributes().build()).put(EntityType.WANDERING_TRADER, (Object)Mob.createMobAttributes().build()).put(EntityType.WITCH, (Object)Witch.createAttributes().build()).put(EntityType.WITHER, (Object)WitherBoss.createAttributes().build()).put(EntityType.WITHER_SKELETON, (Object)AbstractSkeleton.createAttributes().build()).put(EntityType.WOLF, (Object)Wolf.createAttributes().build()).put(EntityType.ZOGLIN, (Object)Zoglin.createAttributes().build()).put(EntityType.ZOMBIE, (Object)Zombie.createAttributes().build()).put(EntityType.ZOMBIE_HORSE, (Object)ZombieHorse.createAttributes().build()).put(EntityType.ZOMBIE_NAUTILUS, (Object)ZombieNautilus.createAttributes().build()).put(EntityType.ZOMBIE_VILLAGER, (Object)Zombie.createAttributes().build()).put(EntityType.ZOMBIFIED_PIGLIN, (Object)ZombifiedPiglin.createAttributes().build()).build();

    public static AttributeSupplier getSupplier(EntityType<? extends LivingEntity> $$0) {
        return SUPPLIERS.get($$0);
    }

    public static boolean hasSupplier(EntityType<?> $$0) {
        return SUPPLIERS.containsKey($$0);
    }

    public static void validate() {
        BuiltInRegistries.ENTITY_TYPE.stream().filter($$0 -> $$0.getCategory() != MobCategory.MISC).filter($$0 -> !DefaultAttributes.hasSupplier($$0)).map(BuiltInRegistries.ENTITY_TYPE::getKey).forEach($$0 -> Util.logAndPauseIfInIde("Entity " + String.valueOf($$0) + " has no attributes"));
    }
}

