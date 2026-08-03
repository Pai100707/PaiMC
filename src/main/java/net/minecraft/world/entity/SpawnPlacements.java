package net.minecraft.world.entity;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.nautilus.AbstractNautilus;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.jspecify.annotations.Nullable;

public class SpawnPlacements {
   private static final Map<net.minecraft.world.entity.EntityType<?>, net.minecraft.world.entity.SpawnPlacements.Data> DATA_BY_TYPE = Maps.newHashMap();

   private static <T extends net.minecraft.world.entity.Mob> void register(
      net.minecraft.world.entity.EntityType<T> $$0,
      net.minecraft.world.entity.SpawnPlacementType $$1,
      Types $$2,
      net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> $$3
   ) {
      net.minecraft.world.entity.SpawnPlacements.Data $$4 = DATA_BY_TYPE.put($$0, new net.minecraft.world.entity.SpawnPlacements.Data($$2, $$1, $$3));
      if ($$4 != null) {
         throw new IllegalStateException("Duplicate registration for type " + BuiltInRegistries.ENTITY_TYPE.getKey($$0));
      }
   }

   public static net.minecraft.world.entity.SpawnPlacementType getPlacementType(net.minecraft.world.entity.EntityType<?> $$0) {
      net.minecraft.world.entity.SpawnPlacements.Data $$1 = DATA_BY_TYPE.get($$0);
      return $$1 == null ? net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS : $$1.placement;
   }

   public static boolean isSpawnPositionOk(net.minecraft.world.entity.EntityType<?> $$0, LevelReader $$1, BlockPos $$2) {
      return getPlacementType($$0).isSpawnPositionOk($$1, $$2, $$0);
   }

   public static Types getHeightmapType(@Nullable net.minecraft.world.entity.EntityType<?> $$0) {
      net.minecraft.world.entity.SpawnPlacements.Data $$1 = DATA_BY_TYPE.get($$0);
      return $$1 == null ? Types.MOTION_BLOCKING_NO_LEAVES : $$1.heightMap;
   }

   public static <T extends net.minecraft.world.entity.Entity> boolean checkSpawnRules(
      net.minecraft.world.entity.EntityType<T> $$0, ServerLevelAccessor $$1, net.minecraft.world.entity.EntitySpawnReason $$2, BlockPos $$3, RandomSource $$4
   ) {
      net.minecraft.world.entity.SpawnPlacements.Data $$5 = DATA_BY_TYPE.get($$0);
      return $$5 == null || $$5.predicate.test($$0, $$1, $$2, $$3, $$4);
   }

   static {
      register(
         net.minecraft.world.entity.EntityType.AXOLOTL,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Axolotl::checkAxolotlSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.COD,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         WaterAnimal::checkSurfaceWaterAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.DOLPHIN,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         AgeableWaterCreature::checkSurfaceAgeableWaterCreatureSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.DROWNED,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Drowned::checkDrownedSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.GUARDIAN,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Guardian::checkGuardianSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PUFFERFISH,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         WaterAnimal::checkSurfaceWaterAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SALMON,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         WaterAnimal::checkSurfaceWaterAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SQUID,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         AgeableWaterCreature::checkSurfaceAgeableWaterCreatureSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.TROPICAL_FISH,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         TropicalFish::checkTropicalFishSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ARMADILLO,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Armadillo::checkArmadilloSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.BAT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Bat::checkBatSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.BLAZE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkAnyLightMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.BOGGED,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.BREEZE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkAnyLightMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CAMEL,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Camel::checkCamelSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CAMEL_HUSK,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkSurfaceMonstersSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CAVE_SPIDER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CHICKEN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.COW,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CREEPER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.DONKEY,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ENDERMAN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ENDERMITE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Endermite::checkEndermiteSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ENDER_DRAGON,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.FROG,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Frog::checkFrogSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.GHAST,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Ghast::checkGhastSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.HAPPY_GHAST,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.GIANT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.GLOW_SQUID,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         GlowSquid::checkGlowSquidSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.GOAT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Goat::checkGoatSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.HORSE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.HUSK,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkSurfaceMonstersSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.IRON_GOLEM,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.LLAMA,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.MAGMA_CUBE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         MagmaCube::checkMagmaCubeSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.MOOSHROOM,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         MushroomCow::checkMushroomSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.MULE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.NAUTILUS,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         AbstractNautilus::checkNautilusSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.OCELOT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING,
         Ocelot::checkOcelotSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PARROT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING,
         Parrot::checkParrotSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PIG,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.HOGLIN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Hoglin::checkHoglinSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PIGLIN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Piglin::checkPiglinSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PILLAGER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         PatrollingMonster::checkPatrollingMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.POLAR_BEAR,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         PolarBear::checkPolarBearSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.RABBIT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Rabbit::checkRabbitSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SHEEP,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SILVERFISH,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Silverfish::checkSilverfishSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SKELETON,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SKELETON_HORSE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         SkeletonHorse::checkSkeletonHorseSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SLIME,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Slime::checkSlimeSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SNOW_GOLEM,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SPIDER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.STRAY,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Stray::checkStraySpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PARCHED,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkSurfaceMonstersSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.STRIDER,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_LAVA,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Strider::checkStriderSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.TURTLE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Turtle::checkTurtleSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.VILLAGER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WITCH,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WITHER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WITHER_SKELETON,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WOLF,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Wolf::checkWolfSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ZOGLIN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkAnyLightMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CREAKING,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ZOMBIE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ZOMBIE_HORSE,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         ZombifiedPiglin::checkZombifiedPiglinSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ZOMBIE_VILLAGER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.CAT,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ELDER_GUARDIAN,
         net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Guardian::checkGuardianSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.EVOKER,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.FOX,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Fox::checkFoxSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.ILLUSIONER,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PANDA,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.PHANTOM,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.RAVAGER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.SHULKER,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.TRADER_LLAMA,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Animal::checkAnimalSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.VEX,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.VINDICATOR,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WANDERING_TRADER,
         net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
         Types.MOTION_BLOCKING_NO_LEAVES,
         net.minecraft.world.entity.Mob::checkMobSpawnRules
      );
      register(
         net.minecraft.world.entity.EntityType.WARDEN,
         net.minecraft.world.entity.SpawnPlacementTypes.NO_RESTRICTIONS,
         Types.MOTION_BLOCKING_NO_LEAVES,
         Monster::checkMonsterSpawnRules
      );
   }

   record Data(Types heightMap, net.minecraft.world.entity.SpawnPlacementType placement, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<?> predicate) {
   }

   @FunctionalInterface
   public interface SpawnPredicate<T extends net.minecraft.world.entity.Entity> {
      boolean test(
         net.minecraft.world.entity.EntityType<T> var1,
         ServerLevelAccessor var2,
         net.minecraft.world.entity.EntitySpawnReason var3,
         BlockPos var4,
         RandomSource var5
      );
   }
}
