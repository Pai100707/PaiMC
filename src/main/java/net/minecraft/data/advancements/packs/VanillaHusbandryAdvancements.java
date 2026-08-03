package net.minecraft.data.advancements.packs;

import com.google.common.collect.BiMap;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ConsumeItemTrigger.TriggerInstance;
import net.minecraft.advancements.criterion.MinMaxBounds.Ints;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class VanillaHusbandryAdvancements implements AdvancementSubProvider {
   public static final List<EntityType<?>> BREEDABLE_ANIMALS = List.of(
      EntityType.HORSE,
      EntityType.DONKEY,
      EntityType.MULE,
      EntityType.SHEEP,
      EntityType.COW,
      EntityType.MOOSHROOM,
      EntityType.PIG,
      EntityType.CHICKEN,
      EntityType.WOLF,
      EntityType.OCELOT,
      EntityType.RABBIT,
      EntityType.LLAMA,
      EntityType.CAT,
      EntityType.PANDA,
      EntityType.FOX,
      EntityType.BEE,
      EntityType.HOGLIN,
      EntityType.STRIDER,
      EntityType.GOAT,
      EntityType.AXOLOTL,
      EntityType.CAMEL,
      EntityType.ARMADILLO,
      EntityType.NAUTILUS
   );
   public static final List<EntityType<?>> INDIRECTLY_BREEDABLE_ANIMALS = List.of(EntityType.TURTLE, EntityType.FROG, EntityType.SNIFFER);
   private static final Item[] FISH = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
   private static final Item[] FISH_BUCKETS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
   private static final Item[] EDIBLE_ITEMS = new Item[]{
      Items.APPLE,
      Items.MUSHROOM_STEW,
      Items.BREAD,
      Items.PORKCHOP,
      Items.COOKED_PORKCHOP,
      Items.GOLDEN_APPLE,
      Items.ENCHANTED_GOLDEN_APPLE,
      Items.COD,
      Items.SALMON,
      Items.TROPICAL_FISH,
      Items.PUFFERFISH,
      Items.COOKED_COD,
      Items.COOKED_SALMON,
      Items.COOKIE,
      Items.MELON_SLICE,
      Items.BEEF,
      Items.COOKED_BEEF,
      Items.CHICKEN,
      Items.COOKED_CHICKEN,
      Items.ROTTEN_FLESH,
      Items.SPIDER_EYE,
      Items.CARROT,
      Items.POTATO,
      Items.BAKED_POTATO,
      Items.POISONOUS_POTATO,
      Items.GOLDEN_CARROT,
      Items.PUMPKIN_PIE,
      Items.RABBIT,
      Items.COOKED_RABBIT,
      Items.RABBIT_STEW,
      Items.MUTTON,
      Items.COOKED_MUTTON,
      Items.CHORUS_FRUIT,
      Items.BEETROOT,
      Items.BEETROOT_SOUP,
      Items.DRIED_KELP,
      Items.SUSPICIOUS_STEW,
      Items.SWEET_BERRIES,
      Items.HONEY_BOTTLE,
      Items.GLOW_BERRIES
   };
   public static final Item[] WAX_SCRAPING_TOOLS = new Item[]{
      Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.COPPER_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE
   };
   private static final Comparator<Reference<?>> HOLDER_KEY_COMPARATOR = Comparator.comparing($$0 -> $$0.key().identifier());

   @Override
   public void generate(Provider $$0, Consumer<AdvancementHolder> $$1) {
      HolderGetter<EntityType<?>> $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
      HolderGetter<Item> $$3 = $$0.lookupOrThrow(Registries.ITEM);
      HolderGetter<Block> $$4 = $$0.lookupOrThrow(Registries.BLOCK);
      HolderLookup<FrogVariant> $$5 = $$0.lookupOrThrow(Registries.FROG_VARIANT);
      HolderLookup<CatVariant> $$6 = $$0.lookupOrThrow(Registries.CAT_VARIANT);
      HolderLookup<WolfVariant> $$7 = $$0.lookupOrThrow(Registries.WOLF_VARIANT);
      RegistryLookup<Enchantment> $$8 = $$0.lookupOrThrow(Registries.ENCHANTMENT);
      AdvancementHolder $$9 = Builder.advancement()
         .display(
            Blocks.HAY_BLOCK,
            Component.translatable("advancements.husbandry.root.title"),
            Component.translatable("advancements.husbandry.root.description"),
            Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry"),
            AdvancementType.TASK,
            false,
            false,
            false
         )
         .addCriterion("consumed_item", TriggerInstance.usedItem())
         .save($$1, "husbandry/root");
      AdvancementHolder $$10 = Builder.advancement()
         .parent($$9)
         .display(
            Items.WHEAT,
            Component.translatable("advancements.husbandry.plant_seed.title"),
            Component.translatable("advancements.husbandry.plant_seed.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("wheat", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.WHEAT))
         .addCriterion("pumpkin_stem", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PUMPKIN_STEM))
         .addCriterion("melon_stem", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.MELON_STEM))
         .addCriterion("beetroots", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.BEETROOTS))
         .addCriterion("nether_wart", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.NETHER_WART))
         .addCriterion("torchflower", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP))
         .addCriterion("pitcher_pod", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP))
         .save($$1, "husbandry/plant_seed");
      AdvancementHolder $$11 = Builder.advancement()
         .parent($$9)
         .display(
            Items.WHEAT,
            Component.translatable("advancements.husbandry.breed_an_animal.title"),
            Component.translatable("advancements.husbandry.breed_an_animal.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("bred", net.minecraft.advancements.criterion.BredAnimalsTrigger.TriggerInstance.bredAnimals())
         .save($$1, "husbandry/breed_an_animal");
      createBreedAllAnimalsAdvancement($$11, $$1, $$2, BREEDABLE_ANIMALS.stream(), INDIRECTLY_BREEDABLE_ANIMALS.stream());
      addFood(Builder.advancement(), $$3)
         .parent($$10)
         .display(
            Items.APPLE,
            Component.translatable("advancements.husbandry.balanced_diet.title"),
            Component.translatable("advancements.husbandry.balanced_diet.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .save($$1, "husbandry/balanced_diet");
      Builder.advancement()
         .parent($$10)
         .display(
            Items.NETHERITE_HOE,
            Component.translatable("advancements.husbandry.netherite_hoe.title"),
            Component.translatable("advancements.husbandry.netherite_hoe.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .addCriterion(
            "netherite_hoe", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.NETHERITE_HOE})
         )
         .save($$1, "husbandry/obtain_netherite_hoe");
      AdvancementHolder $$12 = Builder.advancement()
         .parent($$9)
         .display(
            Items.LEAD,
            Component.translatable("advancements.husbandry.tame_an_animal.title"),
            Component.translatable("advancements.husbandry.tame_an_animal.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("tamed_animal", net.minecraft.advancements.criterion.TameAnimalTrigger.TriggerInstance.tamedAnimal())
         .save($$1, "husbandry/tame_an_animal");
      AdvancementHolder $$13 = addFish(Builder.advancement(), $$3)
         .parent($$9)
         .requirements(Strategy.OR)
         .display(
            Items.FISHING_ROD,
            Component.translatable("advancements.husbandry.fishy_business.title"),
            Component.translatable("advancements.husbandry.fishy_business.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/fishy_business");
      AdvancementHolder $$14 = addFishBuckets(Builder.advancement(), $$3)
         .parent($$13)
         .requirements(Strategy.OR)
         .display(
            Items.PUFFERFISH_BUCKET,
            Component.translatable("advancements.husbandry.tactical_fishing.title"),
            Component.translatable("advancements.husbandry.tactical_fishing.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/tactical_fishing");
      AdvancementHolder $$15 = Builder.advancement()
         .parent($$14)
         .requirements(Strategy.OR)
         .addCriterion(
            BuiltInRegistries.ITEM.getKey(Items.AXOLOTL_BUCKET).getPath(),
            net.minecraft.advancements.criterion.FilledBucketTrigger.TriggerInstance.filledBucket(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.AXOLOTL_BUCKET})
            )
         )
         .display(
            Items.AXOLOTL_BUCKET,
            Component.translatable("advancements.husbandry.axolotl_in_a_bucket.title"),
            Component.translatable("advancements.husbandry.axolotl_in_a_bucket.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/axolotl_in_a_bucket");
      Builder.advancement()
         .parent($$15)
         .addCriterion(
            "kill_axolotl_target",
            net.minecraft.advancements.criterion.EffectsChangedTrigger.TriggerInstance.gotEffectsFrom(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.AXOLOTL)
            )
         )
         .display(
            Items.TROPICAL_FISH_BUCKET,
            Component.translatable("advancements.husbandry.kill_axolotl_target.title"),
            Component.translatable("advancements.husbandry.kill_axolotl_target.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/kill_axolotl_target");
      addCatVariants(Builder.advancement(), $$6)
         .parent($$12)
         .display(
            Items.COD,
            Component.translatable("advancements.husbandry.complete_catalogue.title"),
            Component.translatable("advancements.husbandry.complete_catalogue.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .save($$1, "husbandry/complete_catalogue");
      addTamedWolfVariants(Builder.advancement(), $$7)
         .parent($$12)
         .display(
            Items.BONE,
            Component.translatable("advancements.husbandry.whole_pack.title"),
            Component.translatable("advancements.husbandry.whole_pack.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(50))
         .save($$1, "husbandry/whole_pack");
      AdvancementHolder $$16 = Builder.advancement()
         .parent($$9)
         .addCriterion(
            "safely_harvest_honey",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, BlockTags.BEEHIVES))
                  .setSmokey(true),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.GLASS_BOTTLE})
            )
         )
         .display(
            Items.HONEY_BOTTLE,
            Component.translatable("advancements.husbandry.safely_harvest_honey.title"),
            Component.translatable("advancements.husbandry.safely_harvest_honey.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/safely_harvest_honey");
      AdvancementHolder $$17 = Builder.advancement()
         .parent($$16)
         .display(
            Items.HONEYCOMB,
            Component.translatable("advancements.husbandry.wax_on.title"),
            Component.translatable("advancements.husbandry.wax_on.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "wax_on",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, ((BiMap)HoneycombItem.WAXABLES.get()).keySet())),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.HONEYCOMB})
            )
         )
         .save($$1, "husbandry/wax_on");
      Builder.advancement()
         .parent($$17)
         .display(
            Items.STONE_AXE,
            Component.translatable("advancements.husbandry.wax_off.title"),
            Component.translatable("advancements.husbandry.wax_off.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "wax_off",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, ((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).keySet())),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, WAX_SCRAPING_TOOLS)
            )
         )
         .save($$1, "husbandry/wax_off");
      AdvancementHolder $$18 = Builder.advancement()
         .parent($$9)
         .addCriterion(
            BuiltInRegistries.ITEM.getKey(Items.TADPOLE_BUCKET).getPath(),
            net.minecraft.advancements.criterion.FilledBucketTrigger.TriggerInstance.filledBucket(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.TADPOLE_BUCKET})
            )
         )
         .display(
            Items.TADPOLE_BUCKET,
            Component.translatable("advancements.husbandry.tadpole_in_a_bucket.title"),
            Component.translatable("advancements.husbandry.tadpole_in_a_bucket.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/tadpole_in_a_bucket");
      AdvancementHolder $$19 = addLeashedFrogVariants($$2, $$3, $$5, Builder.advancement())
         .parent($$18)
         .display(
            Items.LEAD,
            Component.translatable("advancements.husbandry.leash_all_frog_variants.title"),
            Component.translatable("advancements.husbandry.leash_all_frog_variants.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/leash_all_frog_variants");
      Builder.advancement()
         .parent($$19)
         .display(
            Items.VERDANT_FROGLIGHT,
            Component.translatable("advancements.husbandry.froglights.title"),
            Component.translatable("advancements.husbandry.froglights.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .addCriterion(
            "froglights",
            net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(
               new ItemLike[]{Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT}
            )
         )
         .save($$1, "husbandry/froglights");
      Builder.advancement()
         .parent($$9)
         .addCriterion(
            "silk_touch_nest",
            net.minecraft.advancements.criterion.BeeNestDestroyedTrigger.TriggerInstance.destroyedBeeNest(
               Blocks.BEE_NEST,
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item()
                  .withComponents(
                     net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                        .partial(
                           DataComponentPredicates.ENCHANTMENTS,
                           EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate($$8.getOrThrow(Enchantments.SILK_TOUCH), Ints.atLeast(1))))
                        )
                        .build()
                  ),
               Ints.exactly(3)
            )
         )
         .display(
            Blocks.BEE_NEST,
            Component.translatable("advancements.husbandry.silk_touch_nest.title"),
            Component.translatable("advancements.husbandry.silk_touch_nest.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .save($$1, "husbandry/silk_touch_nest");
      Builder.advancement()
         .parent($$9)
         .display(
            Items.OAK_BOAT,
            Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.title"),
            Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "ride_a_boat_with_a_goat",
            net.minecraft.advancements.criterion.StartRidingTrigger.TriggerInstance.playerStartsRiding(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                  .vehicle(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .of($$2, EntityTypeTags.BOAT)
                        .passenger(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.GOAT))
                  )
            )
         )
         .save($$1, "husbandry/ride_a_boat_with_a_goat");
      Builder.advancement()
         .parent($$9)
         .display(
            Items.GLOW_INK_SAC,
            Component.translatable("advancements.husbandry.make_a_sign_glow.title"),
            Component.translatable("advancements.husbandry.make_a_sign_glow.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "make_a_sign_glow",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, BlockTags.ALL_SIGNS)),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.GLOW_INK_SAC})
            )
         )
         .save($$1, "husbandry/make_a_sign_glow");
      AdvancementHolder $$20 = Builder.advancement()
         .parent($$9)
         .display(
            Items.COOKIE,
            Component.translatable("advancements.husbandry.allay_deliver_item_to_player.title"),
            Component.translatable("advancements.husbandry.allay_deliver_item_to_player.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .addCriterion(
            "allay_deliver_item_to_player",
            net.minecraft.advancements.criterion.PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(
               Optional.empty(),
               Optional.empty(),
               Optional.of(EntityPredicate.wrap(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.ALLAY)))
            )
         )
         .save($$1, "husbandry/allay_deliver_item_to_player");
      Builder.advancement()
         .parent($$20)
         .display(
            Items.NOTE_BLOCK,
            Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.title"),
            Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .addCriterion(
            "allay_deliver_cake_to_note_block",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.allayDropItemOnBlock(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.location()
                  .setBlock(net.minecraft.advancements.criterion.BlockPredicate.Builder.block().of($$4, new Block[]{Blocks.NOTE_BLOCK})),
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.CAKE})
            )
         )
         .save($$1, "husbandry/allay_deliver_cake_to_note_block");
      AdvancementHolder $$21 = Builder.advancement()
         .parent($$9)
         .display(
            Items.SNIFFER_EGG,
            Component.translatable("advancements.husbandry.obtain_sniffer_egg.title"),
            Component.translatable("advancements.husbandry.obtain_sniffer_egg.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .addCriterion(
            "obtain_sniffer_egg", net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance.hasItems(new ItemLike[]{Items.SNIFFER_EGG})
         )
         .save($$1, "husbandry/obtain_sniffer_egg");
      AdvancementHolder $$22 = Builder.advancement()
         .parent($$21)
         .display(
            Items.TORCHFLOWER_SEEDS,
            Component.translatable("advancements.husbandry.feed_snifflet.title"),
            Component.translatable("advancements.husbandry.feed_snifflet.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .addCriterion(
            "feed_snifflet",
            net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, ItemTags.SNIFFER_FOOD),
               Optional.of(
                  EntityPredicate.wrap(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .of($$2, EntityType.SNIFFER)
                        .flags(net.minecraft.advancements.criterion.EntityFlagsPredicate.Builder.flags().setIsBaby(true))
                  )
               )
            )
         )
         .save($$1, "husbandry/feed_snifflet");
      Builder.advancement()
         .parent($$22)
         .display(
            Items.PITCHER_POD,
            Component.translatable("advancements.husbandry.plant_any_sniffer_seed.title"),
            Component.translatable("advancements.husbandry.plant_any_sniffer_seed.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            true
         )
         .requirements(Strategy.OR)
         .addCriterion("torchflower", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP))
         .addCriterion("pitcher_pod", net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP))
         .save($$1, "husbandry/plant_any_sniffer_seed");
      Builder.advancement()
         .parent($$12)
         .display(
            Items.SHEARS,
            Component.translatable("advancements.husbandry.remove_wolf_armor.title"),
            Component.translatable("advancements.husbandry.remove_wolf_armor.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "remove_wolf_armor",
            net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.equipmentSheared(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.WOLF_ARMOR}),
               Optional.of(EntityPredicate.wrap(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, EntityType.WOLF)))
            )
         )
         .save($$1, "husbandry/remove_wolf_armor");
      Builder.advancement()
         .parent($$12)
         .display(
            Items.WOLF_ARMOR,
            Component.translatable("advancements.husbandry.repair_wolf_armor.title"),
            Component.translatable("advancements.husbandry.repair_wolf_armor.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "repair_wolf_armor",
            net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$3, new ItemLike[]{Items.ARMADILLO_SCUTE}),
               Optional.of(
                  EntityPredicate.wrap(
                     net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                        .of($$2, EntityType.WOLF)
                        .equipment(
                           net.minecraft.advancements.criterion.EntityEquipmentPredicate.Builder.equipment()
                              .body(
                                 net.minecraft.advancements.criterion.ItemPredicate.Builder.item()
                                    .of($$3, new ItemLike[]{Items.WOLF_ARMOR})
                                    .withComponents(
                                       net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                                          .exact(DataComponentExactPredicate.expect(DataComponents.DAMAGE, 0))
                                          .build()
                                    )
                              )
                        )
                  )
               )
            )
         )
         .save($$1, "husbandry/repair_wolf_armor");
      Builder.advancement()
         .parent($$9)
         .display(
            Items.DRIED_GHAST,
            Component.translatable("advancements.husbandry.place_dried_ghast_in_water.title"),
            Component.translatable("advancements.husbandry.place_dried_ghast_in_water.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "place_dried_ghast_in_water",
            net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(
               Blocks.DRIED_GHAST, BlockStateProperties.WATERLOGGED, true
            )
         )
         .save($$1, "husbandry/place_dried_ghast_in_water");
   }

   public static AdvancementHolder createBreedAllAnimalsAdvancement(
      AdvancementHolder $$0, Consumer<AdvancementHolder> $$1, HolderGetter<EntityType<?>> $$2, Stream<EntityType<?>> $$3, Stream<EntityType<?>> $$4
   ) {
      return addBreedable(Builder.advancement(), $$3, $$2, $$4)
         .parent($$0)
         .display(
            Items.GOLDEN_CARROT,
            Component.translatable("advancements.husbandry.breed_all_animals.title"),
            Component.translatable("advancements.husbandry.breed_all_animals.description"),
            null,
            AdvancementType.CHALLENGE,
            true,
            true,
            false
         )
         .rewards(net.minecraft.advancements.AdvancementRewards.Builder.experience(100))
         .save($$1, "husbandry/bred_all_animals");
   }

   private static Builder addLeashedFrogVariants(HolderGetter<EntityType<?>> $$0, HolderGetter<Item> $$1, HolderLookup<FrogVariant> $$2, Builder $$3) {
      sortedVariants($$2)
         .forEach(
            $$3x -> $$3.addCriterion(
               $$3x.key().identifier().toString(),
               net.minecraft.advancements.criterion.PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$1, new ItemLike[]{Items.LEAD}),
                  Optional.of(
                     EntityPredicate.wrap(
                        net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                           .of($$0, EntityType.FROG)
                           .components(
                              net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                                 .exact(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT, $$3x))
                                 .build()
                           )
                     )
                  )
               )
            )
         );
      return $$3;
   }

   private static <T> Stream<Reference<T>> sortedVariants(HolderLookup<T> $$0) {
      return (Stream<Reference<T>>)$$0.listElements().sorted(HOLDER_KEY_COMPARATOR);
   }

   private static Builder addFood(Builder $$0, HolderGetter<Item> $$1) {
      for (Item $$2 : EDIBLE_ITEMS) {
         $$0.addCriterion(BuiltInRegistries.ITEM.getKey($$2).getPath(), TriggerInstance.usedItem($$1, $$2));
      }

      return $$0;
   }

   private static Builder addBreedable(Builder $$0, Stream<EntityType<?>> $$1, HolderGetter<EntityType<?>> $$2, Stream<EntityType<?>> $$3) {
      $$1.forEach(
         $$2x -> $$0.addCriterion(
            EntityType.getKey($$2x).toString(),
            net.minecraft.advancements.criterion.BredAnimalsTrigger.TriggerInstance.bredAnimals(
               net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, $$2x)
            )
         )
      );
      $$3.forEach(
         $$2x -> $$0.addCriterion(
            EntityType.getKey($$2x).toString(),
            net.minecraft.advancements.criterion.BredAnimalsTrigger.TriggerInstance.bredAnimals(
               Optional.of(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, $$2x).build()),
               Optional.of(net.minecraft.advancements.criterion.EntityPredicate.Builder.entity().of($$2, $$2x).build()),
               Optional.empty()
            )
         )
      );
      return $$0;
   }

   private static Builder addFishBuckets(Builder $$0, HolderGetter<Item> $$1) {
      for (Item $$2 : FISH_BUCKETS) {
         $$0.addCriterion(
            BuiltInRegistries.ITEM.getKey($$2).getPath(),
            net.minecraft.advancements.criterion.FilledBucketTrigger.TriggerInstance.filledBucket(
               net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$1, new ItemLike[]{$$2})
            )
         );
      }

      return $$0;
   }

   private static Builder addFish(Builder $$0, HolderGetter<Item> $$1) {
      for (Item $$2 : FISH) {
         $$0.addCriterion(
            BuiltInRegistries.ITEM.getKey($$2).getPath(),
            net.minecraft.advancements.criterion.FishingRodHookedTrigger.TriggerInstance.fishedItem(
               Optional.empty(),
               Optional.empty(),
               Optional.of(net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$1, new ItemLike[]{$$2}).build())
            )
         );
      }

      return $$0;
   }

   private static Builder addCatVariants(Builder $$0, HolderLookup<CatVariant> $$1) {
      sortedVariants($$1)
         .forEach(
            $$1x -> $$0.addCriterion(
               $$1x.key().identifier().toString(),
               net.minecraft.advancements.criterion.TameAnimalTrigger.TriggerInstance.tamedAnimal(
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .components(
                        net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                           .exact(DataComponentExactPredicate.expect(DataComponents.CAT_VARIANT, $$1x))
                           .build()
                     )
               )
            )
         );
      return $$0;
   }

   private static Builder addTamedWolfVariants(Builder $$0, HolderLookup<WolfVariant> $$1) {
      sortedVariants($$1)
         .forEach(
            $$1x -> $$0.addCriterion(
               $$1x.key().identifier().toString(),
               net.minecraft.advancements.criterion.TameAnimalTrigger.TriggerInstance.tamedAnimal(
                  net.minecraft.advancements.criterion.EntityPredicate.Builder.entity()
                     .components(
                        net.minecraft.advancements.criterion.DataComponentMatchers.Builder.components()
                           .exact(DataComponentExactPredicate.expect(DataComponents.WOLF_VARIANT, $$1x))
                           .build()
                     )
               )
            )
         );
      return $$0;
   }
}
