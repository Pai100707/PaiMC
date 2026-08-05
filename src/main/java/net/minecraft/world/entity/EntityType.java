package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.util.ProblemReporter.ScopedCollector;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
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
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
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
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

public class EntityType<T extends net.minecraft.world.entity.Entity> implements FeatureElement, EntityTypeTest<net.minecraft.world.entity.Entity, T> {
   private static final Logger LOGGER = LogUtils.getLogger();
   private final Reference<net.minecraft.world.entity.EntityType<?>> builtInRegistryHolder = BuiltInRegistries.ENTITY_TYPE.createIntrusiveHolder(this);
   public static final Codec<net.minecraft.world.entity.EntityType<?>> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
   public static final StreamCodec<RegistryFriendlyByteBuf, net.minecraft.world.entity.EntityType<?>> STREAM_CODEC = ByteBufCodecs.registry(
      Registries.ENTITY_TYPE
   );
   private static final float MAGIC_HORSE_WIDTH = 1.3964844F;
   private static final int DISPLAY_TRACKING_RANGE = 10;
   public static final net.minecraft.world.entity.EntityType<Boat> ACACIA_BOAT = register(
      "acacia_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.ACACIA_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> ACACIA_CHEST_BOAT = register(
      "acacia_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.ACACIA_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Allay> ALLAY = register(
      "allay",
      net.minecraft.world.entity.EntityType.Builder.of(Allay::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.35F, 0.6F)
         .eyeHeight(0.36F)
         .ridingOffset(0.04F)
         .clientTrackingRange(8)
         .updateInterval(2)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.AreaEffectCloud> AREA_EFFECT_CLOUD = register(
      "area_effect_cloud",
      net.minecraft.world.entity.EntityType.Builder.<net.minecraft.world.entity.AreaEffectCloud>of(
            net.minecraft.world.entity.AreaEffectCloud::new, net.minecraft.world.entity.MobCategory.MISC
         )
         .noLootTable()
         .fireImmune()
         .sized(6.0F, 0.5F)
         .clientTrackingRange(10)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<Armadillo> ARMADILLO = register(
      "armadillo",
      net.minecraft.world.entity.EntityType.Builder.of(Armadillo::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.7F, 0.65F)
         .eyeHeight(0.26F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ArmorStand> ARMOR_STAND = register(
      "armor_stand",
      net.minecraft.world.entity.EntityType.Builder.<ArmorStand>of(ArmorStand::new, net.minecraft.world.entity.MobCategory.MISC)
         .sized(0.5F, 1.975F)
         .eyeHeight(1.7775F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Arrow> ARROW = register(
      "arrow",
      net.minecraft.world.entity.EntityType.Builder.<Arrow>of(Arrow::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .eyeHeight(0.13F)
         .clientTrackingRange(4)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<Axolotl> AXOLOTL = register(
      "axolotl",
      net.minecraft.world.entity.EntityType.Builder.of(Axolotl::new, net.minecraft.world.entity.MobCategory.AXOLOTLS)
         .sized(0.75F, 0.42F)
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestRaft> BAMBOO_CHEST_RAFT = register(
      "bamboo_chest_raft",
      net.minecraft.world.entity.EntityType.Builder.of(chestRaftFactory(() -> Items.BAMBOO_CHEST_RAFT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Raft> BAMBOO_RAFT = register(
      "bamboo_raft",
      net.minecraft.world.entity.EntityType.Builder.of(raftFactory(() -> Items.BAMBOO_RAFT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Bat> BAT = register(
      "bat",
      net.minecraft.world.entity.EntityType.Builder.of(Bat::new, net.minecraft.world.entity.MobCategory.AMBIENT)
         .sized(0.5F, 0.9F)
         .eyeHeight(0.45F)
         .clientTrackingRange(5)
   );
   public static final net.minecraft.world.entity.EntityType<Bee> BEE = register(
      "bee",
      net.minecraft.world.entity.EntityType.Builder.of(Bee::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.7F, 0.6F)
         .eyeHeight(0.3F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Boat> BIRCH_BOAT = register(
      "birch_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.BIRCH_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> BIRCH_CHEST_BOAT = register(
      "birch_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.BIRCH_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Blaze> BLAZE = register(
      "blaze",
      net.minecraft.world.entity.EntityType.Builder.of(Blaze::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(0.6F, 1.8F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Display.BlockDisplay> BLOCK_DISPLAY = register(
      "block_display",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.Display.BlockDisplay::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(10)
         .updateInterval(1)
   );
   public static final net.minecraft.world.entity.EntityType<Bogged> BOGGED = register(
      "bogged",
      net.minecraft.world.entity.EntityType.Builder.of(Bogged::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.99F)
         .eyeHeight(1.74F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Breeze> BREEZE = register(
      "breeze",
      net.minecraft.world.entity.EntityType.Builder.of(Breeze::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.77F)
         .eyeHeight(1.3452F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<BreezeWindCharge> BREEZE_WIND_CHARGE = register(
      "breeze_wind_charge",
      net.minecraft.world.entity.EntityType.Builder.<BreezeWindCharge>of(BreezeWindCharge::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.3125F, 0.3125F)
         .eyeHeight(0.0F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Camel> CAMEL = register(
      "camel",
      net.minecraft.world.entity.EntityType.Builder.of(Camel::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.7F, 2.375F)
         .eyeHeight(2.275F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<CamelHusk> CAMEL_HUSK = register(
      "camel_husk",
      net.minecraft.world.entity.EntityType.Builder.of(CamelHusk::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.7F, 2.375F)
         .eyeHeight(2.275F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Cat> CAT = register(
      "cat",
      net.minecraft.world.entity.EntityType.Builder.of(Cat::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.6F, 0.7F)
         .eyeHeight(0.35F)
         .passengerAttachments(0.5125F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<CaveSpider> CAVE_SPIDER = register(
      "cave_spider",
      net.minecraft.world.entity.EntityType.Builder.of(CaveSpider::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.7F, 0.5F)
         .eyeHeight(0.45F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Boat> CHERRY_BOAT = register(
      "cherry_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.CHERRY_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> CHERRY_CHEST_BOAT = register(
      "cherry_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.CHERRY_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartChest> CHEST_MINECART = register(
      "chest_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartChest::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Chicken> CHICKEN = register(
      "chicken",
      net.minecraft.world.entity.EntityType.Builder.of(Chicken::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.4F, 0.7F)
         .eyeHeight(0.644F)
         .passengerAttachments(new Vec3(0.0, 0.7, -0.1))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Cod> COD = register(
      "cod",
      net.minecraft.world.entity.EntityType.Builder.of(Cod::new, net.minecraft.world.entity.MobCategory.WATER_AMBIENT)
         .sized(0.5F, 0.3F)
         .eyeHeight(0.195F)
         .clientTrackingRange(4)
   );
   public static final net.minecraft.world.entity.EntityType<CopperGolem> COPPER_GOLEM = register(
      "copper_golem",
      net.minecraft.world.entity.EntityType.Builder.of(CopperGolem::new, net.minecraft.world.entity.MobCategory.MISC)
         .sized(0.49F, 0.98F)
         .eyeHeight(0.8125F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = register(
      "command_block_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartCommandBlock::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Cow> COW = register(
      "cow",
      net.minecraft.world.entity.EntityType.Builder.of(Cow::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.4F)
         .eyeHeight(1.3F)
         .passengerAttachments(1.36875F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Creaking> CREAKING = register(
      "creaking",
      net.minecraft.world.entity.EntityType.Builder.of(Creaking::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.9F, 2.7F)
         .eyeHeight(2.3F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Creeper> CREEPER = register(
      "creeper",
      net.minecraft.world.entity.EntityType.Builder.of(Creeper::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Boat> DARK_OAK_BOAT = register(
      "dark_oak_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.DARK_OAK_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> DARK_OAK_CHEST_BOAT = register(
      "dark_oak_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.DARK_OAK_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Dolphin> DOLPHIN = register(
      "dolphin",
      net.minecraft.world.entity.EntityType.Builder.of(Dolphin::new, net.minecraft.world.entity.MobCategory.WATER_CREATURE).sized(0.9F, 0.6F).eyeHeight(0.3F)
   );
   public static final net.minecraft.world.entity.EntityType<Donkey> DONKEY = register(
      "donkey",
      net.minecraft.world.entity.EntityType.Builder.of(Donkey::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.3964844F, 1.5F)
         .eyeHeight(1.425F)
         .passengerAttachments(1.1125F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<DragonFireball> DRAGON_FIREBALL = register(
      "dragon_fireball",
      net.minecraft.world.entity.EntityType.Builder.<DragonFireball>of(DragonFireball::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Drowned> DROWNED = register(
      "drowned",
      net.minecraft.world.entity.EntityType.Builder.of(Drowned::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.74F)
         .passengerAttachments(2.0125F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<ThrownEgg> EGG = register(
      "egg",
      net.minecraft.world.entity.EntityType.Builder.<ThrownEgg>of(ThrownEgg::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<ElderGuardian> ELDER_GUARDIAN = register(
      "elder_guardian",
      net.minecraft.world.entity.EntityType.Builder.of(ElderGuardian::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.9975F, 1.9975F)
         .eyeHeight(0.99875F)
         .passengerAttachments(2.350625F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<EnderMan> ENDERMAN = register(
      "enderman",
      net.minecraft.world.entity.EntityType.Builder.of(EnderMan::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 2.9F)
         .eyeHeight(2.55F)
         .passengerAttachments(2.80625F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Endermite> ENDERMITE = register(
      "endermite",
      net.minecraft.world.entity.EntityType.Builder.of(Endermite::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.4F, 0.3F)
         .eyeHeight(0.13F)
         .passengerAttachments(0.2375F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<EnderDragon> ENDER_DRAGON = register(
      "ender_dragon",
      net.minecraft.world.entity.EntityType.Builder.of(EnderDragon::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(16.0F, 8.0F)
         .passengerAttachments(3.0F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ThrownEnderpearl> ENDER_PEARL = register(
      "ender_pearl",
      net.minecraft.world.entity.EntityType.Builder.<ThrownEnderpearl>of(ThrownEnderpearl::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<EndCrystal> END_CRYSTAL = register(
      "end_crystal",
      net.minecraft.world.entity.EntityType.Builder.<EndCrystal>of(EndCrystal::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .fireImmune()
         .sized(2.0F, 2.0F)
         .clientTrackingRange(16)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<Evoker> EVOKER = register(
      "evoker",
      net.minecraft.world.entity.EntityType.Builder.of(Evoker::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .passengerAttachments(2.0F)
         .ridingOffset(-0.6F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<EvokerFangs> EVOKER_FANGS = register(
      "evoker_fangs",
      net.minecraft.world.entity.EntityType.Builder.<EvokerFangs>of(EvokerFangs::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.8F)
         .clientTrackingRange(6)
         .updateInterval(2)
   );
   public static final net.minecraft.world.entity.EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = register(
      "experience_bottle",
      net.minecraft.world.entity.EntityType.Builder.<ThrownExperienceBottle>of(ThrownExperienceBottle::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.ExperienceOrb> EXPERIENCE_ORB = register(
      "experience_orb",
      net.minecraft.world.entity.EntityType.Builder.<net.minecraft.world.entity.ExperienceOrb>of(
            net.minecraft.world.entity.ExperienceOrb::new, net.minecraft.world.entity.MobCategory.MISC
         )
         .noLootTable()
         .sized(0.5F, 0.5F)
         .clientTrackingRange(6)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<EyeOfEnder> EYE_OF_ENDER = register(
      "eye_of_ender",
      net.minecraft.world.entity.EntityType.Builder.<EyeOfEnder>of(EyeOfEnder::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(4)
   );
   public static final net.minecraft.world.entity.EntityType<FallingBlockEntity> FALLING_BLOCK = register(
      "falling_block",
      net.minecraft.world.entity.EntityType.Builder.<FallingBlockEntity>of(FallingBlockEntity::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.98F)
         .clientTrackingRange(10)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<LargeFireball> FIREBALL = register(
      "fireball",
      net.minecraft.world.entity.EntityType.Builder.<LargeFireball>of(LargeFireball::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.0F, 1.0F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<FireworkRocketEntity> FIREWORK_ROCKET = register(
      "firework_rocket",
      net.minecraft.world.entity.EntityType.Builder.<FireworkRocketEntity>of(FireworkRocketEntity::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Fox> FOX = register(
      "fox",
      net.minecraft.world.entity.EntityType.Builder.of(Fox::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.6F, 0.7F)
         .eyeHeight(0.4F)
         .passengerAttachments(new Vec3(0.0, 0.6375, -0.25))
         .clientTrackingRange(8)
         .immuneTo(Blocks.SWEET_BERRY_BUSH)
   );
   public static final net.minecraft.world.entity.EntityType<Frog> FROG = register(
      "frog",
      net.minecraft.world.entity.EntityType.Builder.of(Frog::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.5F, 0.5F)
         .passengerAttachments(new Vec3(0.0, 0.375, -0.25))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartFurnace> FURNACE_MINECART = register(
      "furnace_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartFurnace::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Ghast> GHAST = register(
      "ghast",
      net.minecraft.world.entity.EntityType.Builder.of(Ghast::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(4.0F, 4.0F)
         .eyeHeight(2.6F)
         .passengerAttachments(4.0625F)
         .ridingOffset(0.5F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<HappyGhast> HAPPY_GHAST = register(
      "happy_ghast",
      net.minecraft.world.entity.EntityType.Builder.of(HappyGhast::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(4.0F, 4.0F)
         .eyeHeight(2.6F)
         .passengerAttachments(new Vec3(0.0, 4.0, 1.7), new Vec3(-1.7, 4.0, 0.0), new Vec3(0.0, 4.0, -1.7), new Vec3(1.7, 4.0, 0.0))
         .ridingOffset(0.5F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Giant> GIANT = register(
      "giant",
      net.minecraft.world.entity.EntityType.Builder.of(Giant::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(3.6F, 12.0F)
         .eyeHeight(10.44F)
         .ridingOffset(-3.75F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<GlowItemFrame> GLOW_ITEM_FRAME = register(
      "glow_item_frame",
      net.minecraft.world.entity.EntityType.Builder.<GlowItemFrame>of(GlowItemFrame::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .eyeHeight(0.0F)
         .clientTrackingRange(10)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<GlowSquid> GLOW_SQUID = register(
      "glow_squid",
      net.minecraft.world.entity.EntityType.Builder.of(GlowSquid::new, net.minecraft.world.entity.MobCategory.UNDERGROUND_WATER_CREATURE)
         .sized(0.8F, 0.8F)
         .eyeHeight(0.4F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Goat> GOAT = register(
      "goat",
      net.minecraft.world.entity.EntityType.Builder.of(Goat::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.3F)
         .passengerAttachments(1.1125F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Guardian> GUARDIAN = register(
      "guardian",
      net.minecraft.world.entity.EntityType.Builder.of(Guardian::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.85F, 0.85F)
         .eyeHeight(0.425F)
         .passengerAttachments(0.975F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Hoglin> HOGLIN = register(
      "hoglin",
      net.minecraft.world.entity.EntityType.Builder.of(Hoglin::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.3964844F, 1.4F)
         .passengerAttachments(1.49375F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartHopper> HOPPER_MINECART = register(
      "hopper_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartHopper::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Horse> HORSE = register(
      "horse",
      net.minecraft.world.entity.EntityType.Builder.of(Horse::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.3964844F, 1.6F)
         .eyeHeight(1.52F)
         .passengerAttachments(1.44375F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Husk> HUSK = register(
      "husk",
      net.minecraft.world.entity.EntityType.Builder.of(Husk::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.74F)
         .passengerAttachments(2.075F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Illusioner> ILLUSIONER = register(
      "illusioner",
      net.minecraft.world.entity.EntityType.Builder.of(Illusioner::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .passengerAttachments(2.0F)
         .ridingOffset(-0.6F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Interaction> INTERACTION = register(
      "interaction",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.Interaction::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<IronGolem> IRON_GOLEM = register(
      "iron_golem",
      net.minecraft.world.entity.EntityType.Builder.of(IronGolem::new, net.minecraft.world.entity.MobCategory.MISC).sized(1.4F, 2.7F).clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ItemEntity> ITEM = register(
      "item",
      net.minecraft.world.entity.EntityType.Builder.<ItemEntity>of(ItemEntity::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .eyeHeight(0.2125F)
         .clientTrackingRange(6)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Display.ItemDisplay> ITEM_DISPLAY = register(
      "item_display",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.Display.ItemDisplay::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(10)
         .updateInterval(1)
   );
   public static final net.minecraft.world.entity.EntityType<ItemFrame> ITEM_FRAME = register(
      "item_frame",
      net.minecraft.world.entity.EntityType.Builder.<ItemFrame>of(ItemFrame::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .eyeHeight(0.0F)
         .clientTrackingRange(10)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<Boat> JUNGLE_BOAT = register(
      "jungle_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.JUNGLE_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> JUNGLE_CHEST_BOAT = register(
      "jungle_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.JUNGLE_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<LeashFenceKnotEntity> LEASH_KNOT = register(
      "leash_knot",
      net.minecraft.world.entity.EntityType.Builder.<LeashFenceKnotEntity>of(LeashFenceKnotEntity::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .noSave()
         .sized(0.375F, 0.5F)
         .eyeHeight(0.0625F)
         .clientTrackingRange(10)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.LightningBolt> LIGHTNING_BOLT = register(
      "lightning_bolt",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.LightningBolt::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .noSave()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(16)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<Llama> LLAMA = register(
      "llama",
      net.minecraft.world.entity.EntityType.Builder.of(Llama::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.87F)
         .eyeHeight(1.7765F)
         .passengerAttachments(new Vec3(0.0, 1.37, -0.3))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<LlamaSpit> LLAMA_SPIT = register(
      "llama_spit",
      net.minecraft.world.entity.EntityType.Builder.<LlamaSpit>of(LlamaSpit::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<MagmaCube> MAGMA_CUBE = register(
      "magma_cube",
      net.minecraft.world.entity.EntityType.Builder.of(MagmaCube::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(0.52F, 0.52F)
         .eyeHeight(0.325F)
         .spawnDimensionsScale(4.0F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Boat> MANGROVE_BOAT = register(
      "mangrove_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.MANGROVE_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> MANGROVE_CHEST_BOAT = register(
      "mangrove_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.MANGROVE_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Mannequin> MANNEQUIN = register(
      "mannequin",
      net.minecraft.world.entity.EntityType.Builder.of(Mannequin::create, net.minecraft.world.entity.MobCategory.MISC)
         .sized(0.6F, 1.8F)
         .eyeHeight(1.62F)
         .vehicleAttachment(net.minecraft.world.entity.Avatar.DEFAULT_VEHICLE_ATTACHMENT)
         .clientTrackingRange(32)
         .updateInterval(2)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Marker> MARKER = register(
      "marker",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.Marker::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(0)
   );
   public static final net.minecraft.world.entity.EntityType<Minecart> MINECART = register(
      "minecart",
      net.minecraft.world.entity.EntityType.Builder.of(Minecart::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<MushroomCow> MOOSHROOM = register(
      "mooshroom",
      net.minecraft.world.entity.EntityType.Builder.of(MushroomCow::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.4F)
         .eyeHeight(1.3F)
         .passengerAttachments(1.36875F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Mule> MULE = register(
      "mule",
      net.minecraft.world.entity.EntityType.Builder.of(Mule::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.3964844F, 1.6F)
         .eyeHeight(1.52F)
         .passengerAttachments(1.2125F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Nautilus> NAUTILUS = register(
      "nautilus",
      net.minecraft.world.entity.EntityType.Builder.of(Nautilus::new, net.minecraft.world.entity.MobCategory.WATER_CREATURE)
         .sized(0.875F, 0.95F)
         .passengerAttachments(1.1375F)
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Boat> OAK_BOAT = register(
      "oak_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.OAK_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> OAK_CHEST_BOAT = register(
      "oak_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.OAK_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Ocelot> OCELOT = register(
      "ocelot",
      net.minecraft.world.entity.EntityType.Builder.of(Ocelot::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.6F, 0.7F)
         .passengerAttachments(0.6375F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.OminousItemSpawner> OMINOUS_ITEM_SPAWNER = register(
      "ominous_item_spawner",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.OminousItemSpawner::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Painting> PAINTING = register(
      "painting",
      net.minecraft.world.entity.EntityType.Builder.<Painting>of(Painting::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .clientTrackingRange(10)
         .updateInterval(Integer.MAX_VALUE)
   );
   public static final net.minecraft.world.entity.EntityType<Boat> PALE_OAK_BOAT = register(
      "pale_oak_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.PALE_OAK_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> PALE_OAK_CHEST_BOAT = register(
      "pale_oak_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.PALE_OAK_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Panda> PANDA = register(
      "panda",
      net.minecraft.world.entity.EntityType.Builder.of(Panda::new, net.minecraft.world.entity.MobCategory.CREATURE).sized(1.3F, 1.25F).clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Parched> PARCHED = register(
      "parched",
      net.minecraft.world.entity.EntityType.Builder.of(Parched::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.99F)
         .eyeHeight(1.74F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Parrot> PARROT = register(
      "parrot",
      net.minecraft.world.entity.EntityType.Builder.of(Parrot::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.5F, 0.9F)
         .eyeHeight(0.54F)
         .passengerAttachments(0.4625F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Phantom> PHANTOM = register(
      "phantom",
      net.minecraft.world.entity.EntityType.Builder.of(Phantom::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.9F, 0.5F)
         .eyeHeight(0.175F)
         .passengerAttachments(0.3375F)
         .ridingOffset(-0.125F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Pig> PIG = register(
      "pig",
      net.minecraft.world.entity.EntityType.Builder.of(Pig::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 0.9F)
         .passengerAttachments(0.86875F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Piglin> PIGLIN = register(
      "piglin",
      net.minecraft.world.entity.EntityType.Builder.of(Piglin::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.79F)
         .passengerAttachments(2.0125F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<PiglinBrute> PIGLIN_BRUTE = register(
      "piglin_brute",
      net.minecraft.world.entity.EntityType.Builder.of(PiglinBrute::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.79F)
         .passengerAttachments(2.0125F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Pillager> PILLAGER = register(
      "pillager",
      net.minecraft.world.entity.EntityType.Builder.of(Pillager::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .canSpawnFarFromPlayer()
         .sized(0.6F, 1.95F)
         .passengerAttachments(2.0F)
         .ridingOffset(-0.6F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<PolarBear> POLAR_BEAR = register(
      "polar_bear",
      net.minecraft.world.entity.EntityType.Builder.of(PolarBear::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .immuneTo(Blocks.POWDER_SNOW)
         .sized(1.4F, 1.4F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ThrownSplashPotion> SPLASH_POTION = register(
      "splash_potion",
      net.minecraft.world.entity.EntityType.Builder.<ThrownSplashPotion>of(ThrownSplashPotion::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<ThrownLingeringPotion> LINGERING_POTION = register(
      "lingering_potion",
      net.minecraft.world.entity.EntityType.Builder.<ThrownLingeringPotion>of(ThrownLingeringPotion::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Pufferfish> PUFFERFISH = register(
      "pufferfish",
      net.minecraft.world.entity.EntityType.Builder.of(Pufferfish::new, net.minecraft.world.entity.MobCategory.WATER_AMBIENT)
         .sized(0.7F, 0.7F)
         .eyeHeight(0.455F)
         .clientTrackingRange(4)
   );
   public static final net.minecraft.world.entity.EntityType<Rabbit> RABBIT = register(
      "rabbit",
      net.minecraft.world.entity.EntityType.Builder.of(Rabbit::new, net.minecraft.world.entity.MobCategory.CREATURE).sized(0.4F, 0.5F).clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Ravager> RAVAGER = register(
      "ravager",
      net.minecraft.world.entity.EntityType.Builder.of(Ravager::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.95F, 2.2F)
         .passengerAttachments(new Vec3(0.0, 2.2625, -0.0625))
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Salmon> SALMON = register(
      "salmon",
      net.minecraft.world.entity.EntityType.Builder.of(Salmon::new, net.minecraft.world.entity.MobCategory.WATER_AMBIENT)
         .sized(0.7F, 0.4F)
         .eyeHeight(0.26F)
         .clientTrackingRange(4)
   );
   public static final net.minecraft.world.entity.EntityType<Sheep> SHEEP = register(
      "sheep",
      net.minecraft.world.entity.EntityType.Builder.of(Sheep::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.3F)
         .eyeHeight(1.235F)
         .passengerAttachments(1.2375F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Shulker> SHULKER = register(
      "shulker",
      net.minecraft.world.entity.EntityType.Builder.of(Shulker::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .canSpawnFarFromPlayer()
         .sized(1.0F, 1.0F)
         .eyeHeight(0.5F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ShulkerBullet> SHULKER_BULLET = register(
      "shulker_bullet",
      net.minecraft.world.entity.EntityType.Builder.<ShulkerBullet>of(ShulkerBullet::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.3125F, 0.3125F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Silverfish> SILVERFISH = register(
      "silverfish",
      net.minecraft.world.entity.EntityType.Builder.of(Silverfish::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.4F, 0.3F)
         .eyeHeight(0.13F)
         .passengerAttachments(0.2375F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Skeleton> SKELETON = register(
      "skeleton",
      net.minecraft.world.entity.EntityType.Builder.of(Skeleton::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.99F)
         .eyeHeight(1.74F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<SkeletonHorse> SKELETON_HORSE = register(
      "skeleton_horse",
      net.minecraft.world.entity.EntityType.Builder.of(SkeletonHorse::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.3964844F, 1.6F)
         .eyeHeight(1.52F)
         .passengerAttachments(1.31875F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Slime> SLIME = register(
      "slime",
      net.minecraft.world.entity.EntityType.Builder.of(Slime::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.52F, 0.52F)
         .eyeHeight(0.325F)
         .spawnDimensionsScale(4.0F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<SmallFireball> SMALL_FIREBALL = register(
      "small_fireball",
      net.minecraft.world.entity.EntityType.Builder.<SmallFireball>of(SmallFireball::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.3125F, 0.3125F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Sniffer> SNIFFER = register(
      "sniffer",
      net.minecraft.world.entity.EntityType.Builder.of(Sniffer::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.9F, 1.75F)
         .eyeHeight(1.05F)
         .passengerAttachments(2.09375F)
         .nameTagOffset(2.05F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Snowball> SNOWBALL = register(
      "snowball",
      net.minecraft.world.entity.EntityType.Builder.<Snowball>of(Snowball::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<SnowGolem> SNOW_GOLEM = register(
      "snow_golem",
      net.minecraft.world.entity.EntityType.Builder.of(SnowGolem::new, net.minecraft.world.entity.MobCategory.MISC)
         .immuneTo(Blocks.POWDER_SNOW)
         .sized(0.7F, 1.9F)
         .eyeHeight(1.7F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartSpawner> SPAWNER_MINECART = register(
      "spawner_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartSpawner::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<SpectralArrow> SPECTRAL_ARROW = register(
      "spectral_arrow",
      net.minecraft.world.entity.EntityType.Builder.<SpectralArrow>of(SpectralArrow::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .eyeHeight(0.13F)
         .clientTrackingRange(4)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<Spider> SPIDER = register(
      "spider",
      net.minecraft.world.entity.EntityType.Builder.of(Spider::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.4F, 0.9F)
         .eyeHeight(0.65F)
         .passengerAttachments(0.765F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Boat> SPRUCE_BOAT = register(
      "spruce_boat",
      net.minecraft.world.entity.EntityType.Builder.of(boatFactory(() -> Items.SPRUCE_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ChestBoat> SPRUCE_CHEST_BOAT = register(
      "spruce_chest_boat",
      net.minecraft.world.entity.EntityType.Builder.of(chestBoatFactory(() -> Items.SPRUCE_CHEST_BOAT), net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(1.375F, 0.5625F)
         .eyeHeight(0.5625F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Squid> SQUID = register(
      "squid",
      net.minecraft.world.entity.EntityType.Builder.of(Squid::new, net.minecraft.world.entity.MobCategory.WATER_CREATURE)
         .sized(0.8F, 0.8F)
         .eyeHeight(0.4F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<Stray> STRAY = register(
      "stray",
      net.minecraft.world.entity.EntityType.Builder.of(Stray::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.99F)
         .eyeHeight(1.74F)
         .ridingOffset(-0.7F)
         .immuneTo(Blocks.POWDER_SNOW)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Strider> STRIDER = register(
      "strider",
      net.minecraft.world.entity.EntityType.Builder.of(Strider::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .fireImmune()
         .sized(0.9F, 1.7F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Tadpole> TADPOLE = register(
      "tadpole",
      net.minecraft.world.entity.EntityType.Builder.of(Tadpole::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.4F, 0.3F)
         .eyeHeight(0.19500001F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Display.TextDisplay> TEXT_DISPLAY = register(
      "text_display",
      net.minecraft.world.entity.EntityType.Builder.of(net.minecraft.world.entity.Display.TextDisplay::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.0F, 0.0F)
         .clientTrackingRange(10)
         .updateInterval(1)
   );
   public static final net.minecraft.world.entity.EntityType<PrimedTnt> TNT = register(
      "tnt",
      net.minecraft.world.entity.EntityType.Builder.<PrimedTnt>of(PrimedTnt::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .fireImmune()
         .sized(0.98F, 0.98F)
         .eyeHeight(0.15F)
         .clientTrackingRange(10)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<MinecartTNT> TNT_MINECART = register(
      "tnt_minecart",
      net.minecraft.world.entity.EntityType.Builder.of(MinecartTNT::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.98F, 0.7F)
         .passengerAttachments(0.1875F)
         .clientTrackingRange(8)
   );
   public static final net.minecraft.world.entity.EntityType<TraderLlama> TRADER_LLAMA = register(
      "trader_llama",
      net.minecraft.world.entity.EntityType.Builder.of(TraderLlama::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.9F, 1.87F)
         .eyeHeight(1.7765F)
         .passengerAttachments(new Vec3(0.0, 1.37, -0.3))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ThrownTrident> TRIDENT = register(
      "trident",
      net.minecraft.world.entity.EntityType.Builder.<ThrownTrident>of(ThrownTrident::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.5F, 0.5F)
         .eyeHeight(0.13F)
         .clientTrackingRange(4)
         .updateInterval(20)
   );
   public static final net.minecraft.world.entity.EntityType<TropicalFish> TROPICAL_FISH = register(
      "tropical_fish",
      net.minecraft.world.entity.EntityType.Builder.of(TropicalFish::new, net.minecraft.world.entity.MobCategory.WATER_AMBIENT)
         .sized(0.5F, 0.4F)
         .eyeHeight(0.26F)
         .clientTrackingRange(4)
   );
   public static final net.minecraft.world.entity.EntityType<Turtle> TURTLE = register(
      "turtle",
      net.minecraft.world.entity.EntityType.Builder.of(Turtle::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(1.2F, 0.4F)
         .passengerAttachments(new Vec3(0.0, 0.55625, -0.25))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Vex> VEX = register(
      "vex",
      net.minecraft.world.entity.EntityType.Builder.of(Vex::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(0.4F, 0.8F)
         .eyeHeight(0.51875F)
         .passengerAttachments(0.7375F)
         .ridingOffset(0.04F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Villager> VILLAGER = register(
      "villager",
      net.minecraft.world.entity.EntityType.Builder.<Villager>of(Villager::new, net.minecraft.world.entity.MobCategory.MISC)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.62F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Vindicator> VINDICATOR = register(
      "vindicator",
      net.minecraft.world.entity.EntityType.Builder.of(Vindicator::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .passengerAttachments(2.0F)
         .ridingOffset(-0.6F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<WanderingTrader> WANDERING_TRADER = register(
      "wandering_trader",
      net.minecraft.world.entity.EntityType.Builder.of(WanderingTrader::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.62F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Warden> WARDEN = register(
      "warden",
      net.minecraft.world.entity.EntityType.Builder.of(Warden::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.9F, 2.9F)
         .passengerAttachments(3.15F)
         .attach(net.minecraft.world.entity.EntityAttachment.WARDEN_CHEST, 0.0F, 1.6F, 0.0F)
         .clientTrackingRange(16)
         .fireImmune()
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<WindCharge> WIND_CHARGE = register(
      "wind_charge",
      net.minecraft.world.entity.EntityType.Builder.<WindCharge>of(WindCharge::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.3125F, 0.3125F)
         .eyeHeight(0.0F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Witch> WITCH = register(
      "witch",
      net.minecraft.world.entity.EntityType.Builder.of(Witch::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.62F)
         .passengerAttachments(2.2625F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<WitherBoss> WITHER = register(
      "wither",
      net.minecraft.world.entity.EntityType.Builder.of(WitherBoss::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .immuneTo(Blocks.WITHER_ROSE)
         .sized(0.9F, 3.5F)
         .clientTrackingRange(10)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<WitherSkeleton> WITHER_SKELETON = register(
      "wither_skeleton",
      net.minecraft.world.entity.EntityType.Builder.of(WitherSkeleton::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .immuneTo(Blocks.WITHER_ROSE)
         .sized(0.7F, 2.4F)
         .eyeHeight(2.1F)
         .ridingOffset(-0.875F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<WitherSkull> WITHER_SKULL = register(
      "wither_skull",
      net.minecraft.world.entity.EntityType.Builder.<WitherSkull>of(WitherSkull::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .sized(0.3125F, 0.3125F)
         .clientTrackingRange(4)
         .updateInterval(10)
   );
   public static final net.minecraft.world.entity.EntityType<Wolf> WOLF = register(
      "wolf",
      net.minecraft.world.entity.EntityType.Builder.of(Wolf::new, net.minecraft.world.entity.MobCategory.CREATURE)
         .sized(0.6F, 0.85F)
         .eyeHeight(0.68F)
         .passengerAttachments(new Vec3(0.0, 0.81875, -0.0625))
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<Zoglin> ZOGLIN = register(
      "zoglin",
      net.minecraft.world.entity.EntityType.Builder.of(Zoglin::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(1.3964844F, 1.4F)
         .passengerAttachments(1.49375F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Zombie> ZOMBIE = register(
      "zombie",
      net.minecraft.world.entity.EntityType.Builder.<Zombie>of(Zombie::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .eyeHeight(1.74F)
         .passengerAttachments(2.0125F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<ZombieHorse> ZOMBIE_HORSE = register(
      "zombie_horse",
      net.minecraft.world.entity.EntityType.Builder.of(ZombieHorse::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(1.3964844F, 1.6F)
         .eyeHeight(1.52F)
         .passengerAttachments(1.31875F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ZombieNautilus> ZOMBIE_NAUTILUS = register(
      "zombie_nautilus",
      net.minecraft.world.entity.EntityType.Builder.of(ZombieNautilus::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.875F, 0.95F)
         .passengerAttachments(1.1375F)
         .eyeHeight(0.2751F)
         .clientTrackingRange(10)
   );
   public static final net.minecraft.world.entity.EntityType<ZombieVillager> ZOMBIE_VILLAGER = register(
      "zombie_villager",
      net.minecraft.world.entity.EntityType.Builder.of(ZombieVillager::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .sized(0.6F, 1.95F)
         .passengerAttachments(2.125F)
         .ridingOffset(-0.7F)
         .eyeHeight(1.74F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = register(
      "zombified_piglin",
      net.minecraft.world.entity.EntityType.Builder.of(ZombifiedPiglin::new, net.minecraft.world.entity.MobCategory.MONSTER)
         .fireImmune()
         .sized(0.6F, 1.95F)
         .eyeHeight(1.79F)
         .passengerAttachments(2.0F)
         .ridingOffset(-0.7F)
         .clientTrackingRange(8)
         .notInPeaceful()
   );
   public static final net.minecraft.world.entity.EntityType<Player> PLAYER = register(
      "player",
      net.minecraft.world.entity.EntityType.Builder.<Player>createNothing(net.minecraft.world.entity.MobCategory.MISC)
         .noSave()
         .noSummon()
         .sized(0.6F, 1.8F)
         .eyeHeight(1.62F)
         .vehicleAttachment(net.minecraft.world.entity.Avatar.DEFAULT_VEHICLE_ATTACHMENT)
         .clientTrackingRange(32)
         .updateInterval(2)
   );
   public static final net.minecraft.world.entity.EntityType<FishingHook> FISHING_BOBBER = register(
      "fishing_bobber",
      net.minecraft.world.entity.EntityType.Builder.<FishingHook>of(FishingHook::new, net.minecraft.world.entity.MobCategory.MISC)
         .noLootTable()
         .noSave()
         .noSummon()
         .sized(0.25F, 0.25F)
         .clientTrackingRange(4)
         .updateInterval(5)
   );
   private static final Set<net.minecraft.world.entity.EntityType<?>> OP_ONLY_CUSTOM_DATA = Set.of(FALLING_BLOCK, COMMAND_BLOCK_MINECART, SPAWNER_MINECART);
   private final net.minecraft.world.entity.EntityType.EntityFactory<T> factory;
   private final net.minecraft.world.entity.MobCategory category;
   private final ImmutableSet<Block> immuneTo;
   private final boolean serialize;
   private final boolean summon;
   private final boolean fireImmune;
   private final boolean canSpawnFarFromPlayer;
   private final int clientTrackingRange;
   private final int updateInterval;
   private final String descriptionId;
   
   private Component description;
   private final Optional<ResourceKey<LootTable>> lootTable;
   private final net.minecraft.world.entity.EntityDimensions dimensions;
   private final float spawnDimensionsScale;
   private final FeatureFlagSet requiredFeatures;
   private final boolean allowedInPeaceful;

   private static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> register(
      ResourceKey<net.minecraft.world.entity.EntityType<?>> $$0, net.minecraft.world.entity.EntityType.Builder<T> $$1
   ) {
      return (net.minecraft.world.entity.EntityType<T>)Registry.register(BuiltInRegistries.ENTITY_TYPE, $$0, $$1.build($$0));
   }

   private static ResourceKey<net.minecraft.world.entity.EntityType<?>> vanillaEntityId(String $$0) {
      return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace($$0));
   }

   private static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> register(
      String $$0, net.minecraft.world.entity.EntityType.Builder<T> $$1
   ) {
      return register(vanillaEntityId($$0), $$1);
   }

   public static Identifier getKey(net.minecraft.world.entity.EntityType<?> $$0) {
      return BuiltInRegistries.ENTITY_TYPE.getKey($$0);
   }

   public static Optional<net.minecraft.world.entity.EntityType<?>> byString(String $$0) {
      return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse($$0));
   }

   public EntityType(
      net.minecraft.world.entity.EntityType.EntityFactory<T> $$0,
      net.minecraft.world.entity.MobCategory $$1,
      boolean $$2,
      boolean $$3,
      boolean $$4,
      boolean $$5,
      ImmutableSet<Block> $$6,
      net.minecraft.world.entity.EntityDimensions $$7,
      float $$8,
      int $$9,
      int $$10,
      String $$11,
      Optional<ResourceKey<LootTable>> $$12,
      FeatureFlagSet $$13,
      boolean $$14
   ) {
      this.factory = $$0;
      this.category = $$1;
      this.canSpawnFarFromPlayer = $$5;
      this.serialize = $$2;
      this.summon = $$3;
      this.fireImmune = $$4;
      this.immuneTo = $$6;
      this.dimensions = $$7;
      this.spawnDimensionsScale = $$8;
      this.clientTrackingRange = $$9;
      this.updateInterval = $$10;
      this.descriptionId = $$11;
      this.lootTable = $$12;
      this.requiredFeatures = $$13;
      this.allowedInPeaceful = $$14;
   }

   
   public T spawn(
      ServerLevel $$0,
      ItemStack $$1,
      net.minecraft.world.entity.LivingEntity $$2,
      BlockPos $$3,
      net.minecraft.world.entity.EntitySpawnReason $$4,
      boolean $$5,
      boolean $$6
   ) {
      Consumer<T> $$7;
      if ($$1 != null) {
         $$7 = createDefaultStackConfig($$0, $$1, $$2);
      } else {
         $$7 = $$0x -> {};
      }

      return this.spawn($$0, $$7, $$3, $$4, $$5, $$6);
   }

   public static <T extends net.minecraft.world.entity.Entity> Consumer<T> createDefaultStackConfig(
      Level $$0, ItemStack $$1, net.minecraft.world.entity.LivingEntity $$2
   ) {
      return appendDefaultStackConfig($$0x -> {}, $$0, $$1, $$2);
   }

   public static <T extends net.minecraft.world.entity.Entity> Consumer<T> appendDefaultStackConfig(
      Consumer<T> $$0, Level $$1, ItemStack $$2, net.minecraft.world.entity.LivingEntity $$3
   ) {
      return appendCustomEntityStackConfig(appendComponentsConfig($$0, $$2), $$1, $$2, $$3);
   }

   public static <T extends net.minecraft.world.entity.Entity> Consumer<T> appendComponentsConfig(Consumer<T> $$0, ItemStack $$1) {
      return $$0.andThen($$1x -> $$1x.applyComponentsFromItemStack($$1));
   }

   public static <T extends net.minecraft.world.entity.Entity> Consumer<T> appendCustomEntityStackConfig(
      Consumer<T> $$0, Level $$1, ItemStack $$2, net.minecraft.world.entity.LivingEntity $$3
   ) {
      TypedEntityData<net.minecraft.world.entity.EntityType<?>> $$4 = (TypedEntityData<net.minecraft.world.entity.EntityType<?>>)$$2.get(
         DataComponents.ENTITY_DATA
      );
      return $$4 != null ? $$0.andThen($$3x -> updateCustomEntityTag($$1, $$3, $$3x, $$4)) : $$0;
   }

   
   public T spawn(ServerLevel $$0, BlockPos $$1, net.minecraft.world.entity.EntitySpawnReason $$2) {
      return this.spawn($$0, null, $$1, $$2, false, false);
   }

   
   public T spawn(ServerLevel $$0, Consumer<T> $$1, BlockPos $$2, net.minecraft.world.entity.EntitySpawnReason $$3, boolean $$4, boolean $$5) {
      T $$6 = this.create($$0, $$1, $$2, $$3, $$4, $$5);
      if ($$6 != null) {
         $$0.addFreshEntityWithPassengers($$6);
         if ($$6 instanceof net.minecraft.world.entity.Mob $$7) {
            $$7.playAmbientSound();
         }
      }

      return $$6;
   }

   
   public T create(ServerLevel $$0, Consumer<T> $$1, BlockPos $$2, net.minecraft.world.entity.EntitySpawnReason $$3, boolean $$4, boolean $$5) {
      T $$6 = this.create($$0, $$3);
      if ($$6 == null) {
         return null;
      } else {
         double $$7;
         if ($$4) {
            $$6.setPos($$2.getX() + 0.5, $$2.getY() + 1, $$2.getZ() + 0.5);
            $$7 = getYOffset($$0, $$2, $$5, $$6.getBoundingBox());
         } else {
            $$7 = 0.0;
         }

         $$6.snapTo($$2.getX() + 0.5, $$2.getY() + $$7, $$2.getZ() + 0.5, Mth.wrapDegrees($$0.random.nextFloat() * 360.0F), 0.0F);
         if ($$6 instanceof net.minecraft.world.entity.Mob $$9) {
            $$9.yHeadRot = $$9.getYRot();
            $$9.yBodyRot = $$9.getYRot();
            $$9.finalizeSpawn($$0, $$0.getCurrentDifficultyAt($$9.blockPosition()), $$3, null);
         }

         if ($$1 != null) {
            $$1.accept($$6);
         }

         return $$6;
      }
   }

   protected static double getYOffset(LevelReader $$0, BlockPos $$1, boolean $$2, AABB $$3) {
      AABB $$4 = new AABB($$1);
      if ($$2) {
         $$4 = $$4.expandTowards(0.0, -1.0, 0.0);
      }

      Iterable<VoxelShape> $$5 = $$0.getCollisions(null, $$4);
      return 1.0 + Shapes.collide(Axis.Y, $$3, $$5, $$2 ? -2.0 : -1.0);
   }

   public static void updateCustomEntityTag(
      Level $$0,
      net.minecraft.world.entity.LivingEntity $$1,
      net.minecraft.world.entity.Entity $$2,
      TypedEntityData<net.minecraft.world.entity.EntityType<?>> $$3
   ) {
      MinecraftServer $$4 = $$0.getServer();
      if ($$4 != null && $$2 != null) {
         if ($$2.getType() == $$3.type()) {
            if ($$0.isClientSide() || !$$2.getType().onlyOpCanSetNbt() || $$1 instanceof Player $$5 && $$4.getPlayerList().isOp($$5.nameAndId())) {
               $$3.loadInto($$2);
            }
         }
      }
   }

   public boolean canSerialize() {
      return this.serialize;
   }

   public boolean canSummon() {
      return this.summon;
   }

   public boolean fireImmune() {
      return this.fireImmune;
   }

   public boolean canSpawnFarFromPlayer() {
      return this.canSpawnFarFromPlayer;
   }

   public net.minecraft.world.entity.MobCategory getCategory() {
      return this.category;
   }

   public String getDescriptionId() {
      return this.descriptionId;
   }

   public Component getDescription() {
      if (this.description == null) {
         this.description = Component.translatable(this.getDescriptionId());
      }

      return this.description;
   }

   @Override
   public String toString() {
      return this.getDescriptionId();
   }

   public String toShortString() {
      int $$0 = this.getDescriptionId().lastIndexOf(46);
      return $$0 == -1 ? this.getDescriptionId() : this.getDescriptionId().substring($$0 + 1);
   }

   public Optional<ResourceKey<LootTable>> getDefaultLootTable() {
      return this.lootTable;
   }

   public float getWidth() {
      return this.dimensions.width();
   }

   public float getHeight() {
      return this.dimensions.height();
   }

   public FeatureFlagSet requiredFeatures() {
      return this.requiredFeatures;
   }

   
   public T create(Level $$0, net.minecraft.world.entity.EntitySpawnReason $$1) {
      return !this.isEnabled($$0.enabledFeatures()) ? null : this.factory.create(this, $$0);
   }

   public static Optional<net.minecraft.world.entity.Entity> create(ValueInput $$0, Level $$1, net.minecraft.world.entity.EntitySpawnReason $$2) {
      return Util.ifElse(
         by($$0).map($$2x -> $$2x.create($$1, $$2)),
         $$1x -> $$1x.load($$0),
         () -> LOGGER.warn("Skipping Entity with id {}", $$0.getStringOr("id", "[invalid]"))
      );
   }

   public static Optional<net.minecraft.world.entity.Entity> create(
      net.minecraft.world.entity.EntityType<?> $$0, ValueInput $$1, Level $$2, net.minecraft.world.entity.EntitySpawnReason $$3
   ) {
      Optional<net.minecraft.world.entity.Entity> $$4 = Optional.ofNullable($$0.create($$2, $$3));
      $$4.ifPresent($$1x -> $$1x.load($$1));
      return $$4;
   }

   public AABB getSpawnAABB(double $$0, double $$1, double $$2) {
      float $$3 = this.spawnDimensionsScale * this.getWidth() / 2.0F;
      float $$4 = this.spawnDimensionsScale * this.getHeight();
      return new AABB($$0 - $$3, $$1, $$2 - $$3, $$0 + $$3, $$1 + $$4, $$2 + $$3);
   }

   public boolean isBlockDangerous(BlockState $$0) {
      if (this.immuneTo.contains($$0.getBlock())) {
         return false;
      } else {
         return !this.fireImmune && NodeEvaluator.isBurningBlock($$0)
            ? true
            : $$0.is(Blocks.WITHER_ROSE) || $$0.is(Blocks.SWEET_BERRY_BUSH) || $$0.is(Blocks.CACTUS) || $$0.is(Blocks.POWDER_SNOW);
      }
   }

   public net.minecraft.world.entity.EntityDimensions getDimensions() {
      return this.dimensions;
   }

   public static Optional<net.minecraft.world.entity.EntityType<?>> by(ValueInput $$0) {
      return $$0.read("id", CODEC);
   }

   
   public static net.minecraft.world.entity.Entity loadEntityRecursive(
      CompoundTag $$0, Level $$1, net.minecraft.world.entity.EntitySpawnReason $$2, net.minecraft.world.entity.EntityProcessor $$3
   ) {
      ScopedCollector $$4 = new ScopedCollector(LOGGER);

      net.minecraft.world.entity.Entity var5;
      try {
         var5 = loadEntityRecursive(TagValueInput.create($$4, $$1.registryAccess(), $$0), $$1, $$2, $$3);
      } catch (Throwable var8) {
         try {
            $$4.close();
         } catch (Throwable var7) {
            var8.addSuppressed(var7);
         }

         throw var8;
      }

      $$4.close();
      return var5;
   }

   
   public static net.minecraft.world.entity.Entity loadEntityRecursive(
      net.minecraft.world.entity.EntityType<?> $$0,
      CompoundTag $$1,
      Level $$2,
      net.minecraft.world.entity.EntitySpawnReason $$3,
      net.minecraft.world.entity.EntityProcessor $$4
   ) {
      ScopedCollector $$5 = new ScopedCollector(LOGGER);

      net.minecraft.world.entity.Entity var6;
      try {
         var6 = loadEntityRecursive($$0, TagValueInput.create($$5, $$2.registryAccess(), $$1), $$2, $$3, $$4);
      } catch (Throwable var9) {
         try {
            $$5.close();
         } catch (Throwable var8) {
            var9.addSuppressed(var8);
         }

         throw var9;
      }

      $$5.close();
      return var6;
   }

   
   public static net.minecraft.world.entity.Entity loadEntityRecursive(
      ValueInput $$0, Level $$1, net.minecraft.world.entity.EntitySpawnReason $$2, net.minecraft.world.entity.EntityProcessor $$3
   ) {
      return loadStaticEntity($$0, $$1, $$2).map($$3::process).map($$4 -> loadPassengersRecursive($$4, $$0, $$1, $$2, $$3)).orElse(null);
   }

   
   public static net.minecraft.world.entity.Entity loadEntityRecursive(
      net.minecraft.world.entity.EntityType<?> $$0,
      ValueInput $$1,
      Level $$2,
      net.minecraft.world.entity.EntitySpawnReason $$3,
      net.minecraft.world.entity.EntityProcessor $$4
   ) {
      return loadStaticEntity($$0, $$1, $$2, $$3).map($$4::process).map($$4x -> loadPassengersRecursive($$4x, $$1, $$2, $$3, $$4)).orElse(null);
   }

   private static net.minecraft.world.entity.Entity loadPassengersRecursive(
      net.minecraft.world.entity.Entity $$0,
      ValueInput $$1,
      Level $$2,
      net.minecraft.world.entity.EntitySpawnReason $$3,
      net.minecraft.world.entity.EntityProcessor $$4
   ) {
      for (ValueInput $$5 : $$1.childrenListOrEmpty("Passengers")) {
         net.minecraft.world.entity.Entity $$6 = loadEntityRecursive($$5, $$2, $$3, $$4);
         if ($$6 != null) {
            $$6.startRiding($$0, true, false);
         }
      }

      return $$0;
   }

   public static Stream<net.minecraft.world.entity.Entity> loadEntitiesRecursive(
      ValueInputList $$0, Level $$1, net.minecraft.world.entity.EntitySpawnReason $$2
   ) {
      return $$0.stream().mapMulti(($$2x, $$3) -> loadEntityRecursive($$2x, $$1, $$2, $$1xx -> {
         $$3.accept($$1xx);
         return $$1xx;
      }));
   }

   private static Optional<net.minecraft.world.entity.Entity> loadStaticEntity(ValueInput $$0, Level $$1, net.minecraft.world.entity.EntitySpawnReason $$2) {
      try {
         return create($$0, $$1, $$2);
      } catch (RuntimeException var4) {
         LOGGER.warn("Exception loading entity: ", var4);
         return Optional.empty();
      }
   }

   private static Optional<net.minecraft.world.entity.Entity> loadStaticEntity(
      net.minecraft.world.entity.EntityType<?> $$0, ValueInput $$1, Level $$2, net.minecraft.world.entity.EntitySpawnReason $$3
   ) {
      try {
         return create($$0, $$1, $$2, $$3);
      } catch (RuntimeException var5) {
         LOGGER.warn("Exception loading entity: ", var5);
         return Optional.empty();
      }
   }

   public int clientTrackingRange() {
      return this.clientTrackingRange;
   }

   public int updateInterval() {
      return this.updateInterval;
   }

   public boolean trackDeltas() {
      return this != PLAYER
         && this != LLAMA_SPIT
         && this != WITHER
         && this != BAT
         && this != ITEM_FRAME
         && this != GLOW_ITEM_FRAME
         && this != LEASH_KNOT
         && this != PAINTING
         && this != END_CRYSTAL
         && this != EVOKER_FANGS;
   }

   public boolean is(TagKey<net.minecraft.world.entity.EntityType<?>> $$0) {
      return this.builtInRegistryHolder.is($$0);
   }

   public boolean is(HolderSet<net.minecraft.world.entity.EntityType<?>> $$0) {
      return $$0.contains(this.builtInRegistryHolder);
   }

   
   public T tryCast(net.minecraft.world.entity.Entity $$0) {
      return (T)($$0.getType() == this ? $$0 : null);
   }

   public Class<? extends net.minecraft.world.entity.Entity> getBaseClass() {
      return net.minecraft.world.entity.Entity.class;
   }

   @Deprecated
   public Reference<net.minecraft.world.entity.EntityType<?>> builtInRegistryHolder() {
      return this.builtInRegistryHolder;
   }

   public boolean isAllowedInPeaceful() {
      return this.allowedInPeaceful;
   }

   private static net.minecraft.world.entity.EntityType.EntityFactory<Boat> boatFactory(Supplier<Item> $$0) {
      return ($$1, $$2) -> new Boat($$1, $$2, $$0);
   }

   private static net.minecraft.world.entity.EntityType.EntityFactory<ChestBoat> chestBoatFactory(Supplier<Item> $$0) {
      return ($$1, $$2) -> new ChestBoat($$1, $$2, $$0);
   }

   private static net.minecraft.world.entity.EntityType.EntityFactory<Raft> raftFactory(Supplier<Item> $$0) {
      return ($$1, $$2) -> new Raft($$1, $$2, $$0);
   }

   private static net.minecraft.world.entity.EntityType.EntityFactory<ChestRaft> chestRaftFactory(Supplier<Item> $$0) {
      return ($$1, $$2) -> new ChestRaft($$1, $$2, $$0);
   }

   public boolean onlyOpCanSetNbt() {
      return OP_ONLY_CUSTOM_DATA.contains(this);
   }

   public static class Builder<T extends net.minecraft.world.entity.Entity> {
      private final net.minecraft.world.entity.EntityType.EntityFactory<T> factory;
      private final net.minecraft.world.entity.MobCategory category;
      private ImmutableSet<Block> immuneTo = ImmutableSet.of();
      private boolean serialize = true;
      private boolean summon = true;
      private boolean fireImmune;
      private boolean canSpawnFarFromPlayer;
      private int clientTrackingRange = 5;
      private int updateInterval = 3;
      private net.minecraft.world.entity.EntityDimensions dimensions = net.minecraft.world.entity.EntityDimensions.scalable(0.6F, 1.8F);
      private float spawnDimensionsScale = 1.0F;
      private net.minecraft.world.entity.EntityAttachments.Builder attachments = net.minecraft.world.entity.EntityAttachments.builder();
      private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
      private DependantName<net.minecraft.world.entity.EntityType<?>, Optional<ResourceKey<LootTable>>> lootTable = $$0x -> Optional.of(
         ResourceKey.create(Registries.LOOT_TABLE, $$0x.identifier().withPrefix("entities/"))
      );
      private final DependantName<net.minecraft.world.entity.EntityType<?>, String> descriptionId = $$0x -> Util.makeDescriptionId("entity", $$0x.identifier());
      private boolean allowedInPeaceful = true;

      private Builder(net.minecraft.world.entity.EntityType.EntityFactory<T> $$0, net.minecraft.world.entity.MobCategory $$1) {
         this.factory = $$0;
         this.category = $$1;
         this.canSpawnFarFromPlayer = $$1 == net.minecraft.world.entity.MobCategory.CREATURE || $$1 == net.minecraft.world.entity.MobCategory.MISC;
      }

      public static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType.Builder<T> of(
         net.minecraft.world.entity.EntityType.EntityFactory<T> $$0, net.minecraft.world.entity.MobCategory $$1
      ) {
         return new net.minecraft.world.entity.EntityType.Builder<>($$0, $$1);
      }

      public static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType.Builder<T> createNothing(
         net.minecraft.world.entity.MobCategory $$0
      ) {
         return new net.minecraft.world.entity.EntityType.Builder<>(($$0x, $$1) -> null, $$0);
      }

      public net.minecraft.world.entity.EntityType.Builder<T> sized(float $$0, float $$1) {
         this.dimensions = net.minecraft.world.entity.EntityDimensions.scalable($$0, $$1);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> spawnDimensionsScale(float $$0) {
         this.spawnDimensionsScale = $$0;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> eyeHeight(float $$0) {
         this.dimensions = this.dimensions.withEyeHeight($$0);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> passengerAttachments(float... $$0) {
         for (float $$1 : $$0) {
            this.attachments = this.attachments.attach(net.minecraft.world.entity.EntityAttachment.PASSENGER, 0.0F, $$1, 0.0F);
         }

         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> passengerAttachments(Vec3... $$0) {
         for (Vec3 $$1 : $$0) {
            this.attachments = this.attachments.attach(net.minecraft.world.entity.EntityAttachment.PASSENGER, $$1);
         }

         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> vehicleAttachment(Vec3 $$0) {
         return this.attach(net.minecraft.world.entity.EntityAttachment.VEHICLE, $$0);
      }

      public net.minecraft.world.entity.EntityType.Builder<T> ridingOffset(float $$0) {
         return this.attach(net.minecraft.world.entity.EntityAttachment.VEHICLE, 0.0F, -$$0, 0.0F);
      }

      public net.minecraft.world.entity.EntityType.Builder<T> nameTagOffset(float $$0) {
         return this.attach(net.minecraft.world.entity.EntityAttachment.NAME_TAG, 0.0F, $$0, 0.0F);
      }

      public net.minecraft.world.entity.EntityType.Builder<T> attach(net.minecraft.world.entity.EntityAttachment $$0, float $$1, float $$2, float $$3) {
         this.attachments = this.attachments.attach($$0, $$1, $$2, $$3);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> attach(net.minecraft.world.entity.EntityAttachment $$0, Vec3 $$1) {
         this.attachments = this.attachments.attach($$0, $$1);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> noSummon() {
         this.summon = false;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> noSave() {
         this.serialize = false;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> fireImmune() {
         this.fireImmune = true;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> immuneTo(Block... $$0) {
         this.immuneTo = ImmutableSet.copyOf($$0);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> canSpawnFarFromPlayer() {
         this.canSpawnFarFromPlayer = true;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> clientTrackingRange(int $$0) {
         this.clientTrackingRange = $$0;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> updateInterval(int $$0) {
         this.updateInterval = $$0;
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> requiredFeatures(FeatureFlag... $$0) {
         this.requiredFeatures = FeatureFlags.REGISTRY.subset($$0);
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> noLootTable() {
         this.lootTable = DependantName.fixed(Optional.empty());
         return this;
      }

      public net.minecraft.world.entity.EntityType.Builder<T> notInPeaceful() {
         this.allowedInPeaceful = false;
         return this;
      }

      public net.minecraft.world.entity.EntityType<T> build(ResourceKey<net.minecraft.world.entity.EntityType<?>> $$0) {
         if (this.serialize) {
            Util.fetchChoiceType(References.ENTITY_TREE, $$0.identifier().toString());
         }

         return new net.minecraft.world.entity.EntityType<>(
            this.factory,
            this.category,
            this.serialize,
            this.summon,
            this.fireImmune,
            this.canSpawnFarFromPlayer,
            this.immuneTo,
            this.dimensions.withAttachments(this.attachments),
            this.spawnDimensionsScale,
            this.clientTrackingRange,
            this.updateInterval,
            (String)this.descriptionId.get($$0),
            (Optional<ResourceKey<LootTable>>)this.lootTable.get($$0),
            this.requiredFeatures,
            this.allowedInPeaceful
         );
      }
   }

   @FunctionalInterface
   public interface EntityFactory<T extends net.minecraft.world.entity.Entity> {
      
      T create(net.minecraft.world.entity.EntityType<T> var1, Level var2);
   }
}
