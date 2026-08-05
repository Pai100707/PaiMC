/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements.packs;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.BeeNestDestroyedTrigger;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.EffectsChangedTrigger;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.FilledBucketTrigger;
import net.minecraft.advancements.criterion.FishingRodHookedTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.PickedUpItemTrigger;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.advancements.criterion.StartRidingTrigger;
import net.minecraft.advancements.criterion.TameAnimalTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class VanillaHusbandryAdvancements
implements AdvancementSubProvider {
    public static final List<EntityType<?>> BREEDABLE_ANIMALS = List.of(EntityType.HORSE, EntityType.DONKEY, EntityType.MULE, EntityType.SHEEP, EntityType.COW, EntityType.MOOSHROOM, EntityType.PIG, EntityType.CHICKEN, EntityType.WOLF, EntityType.OCELOT, EntityType.RABBIT, EntityType.LLAMA, EntityType.CAT, EntityType.PANDA, EntityType.FOX, EntityType.BEE, EntityType.HOGLIN, EntityType.STRIDER, EntityType.GOAT, EntityType.AXOLOTL, EntityType.CAMEL, EntityType.ARMADILLO, EntityType.NAUTILUS);
    public static final List<EntityType<?>> INDIRECTLY_BREEDABLE_ANIMALS = List.of(EntityType.TURTLE, EntityType.FROG, EntityType.SNIFFER);
    private static final Item[] FISH = new Item[]{Items.COD, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.SALMON};
    private static final Item[] FISH_BUCKETS = new Item[]{Items.COD_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.PUFFERFISH_BUCKET, Items.SALMON_BUCKET};
    private static final Item[] EDIBLE_ITEMS = new Item[]{Items.APPLE, Items.MUSHROOM_STEW, Items.BREAD, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON, Items.COOKIE, Items.MELON_SLICE, Items.BEEF, Items.COOKED_BEEF, Items.CHICKEN, Items.COOKED_CHICKEN, Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.CARROT, Items.POTATO, Items.BAKED_POTATO, Items.POISONOUS_POTATO, Items.GOLDEN_CARROT, Items.PUMPKIN_PIE, Items.RABBIT, Items.COOKED_RABBIT, Items.RABBIT_STEW, Items.MUTTON, Items.COOKED_MUTTON, Items.CHORUS_FRUIT, Items.BEETROOT, Items.BEETROOT_SOUP, Items.DRIED_KELP, Items.SUSPICIOUS_STEW, Items.SWEET_BERRIES, Items.HONEY_BOTTLE, Items.GLOW_BERRIES};
    public static final Item[] WAX_SCRAPING_TOOLS = new Item[]{Items.WOODEN_AXE, Items.GOLDEN_AXE, Items.STONE_AXE, Items.COPPER_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE};
    private static final Comparator<Holder.Reference<?>> HOLDER_KEY_COMPARATOR = Comparator.comparing($$0 -> $$0.key().identifier());

    @Override
    public void generate(HolderLookup.Provider $$0, Consumer<AdvancementHolder> $$1) {
        HolderGetter $$2 = $$0.lookupOrThrow(Registries.ENTITY_TYPE);
        HolderGetter $$3 = $$0.lookupOrThrow(Registries.ITEM);
        HolderGetter $$4 = $$0.lookupOrThrow(Registries.BLOCK);
        HolderGetter $$5 = $$0.lookupOrThrow(Registries.FROG_VARIANT);
        HolderGetter $$6 = $$0.lookupOrThrow(Registries.CAT_VARIANT);
        HolderGetter $$7 = $$0.lookupOrThrow(Registries.WOLF_VARIANT);
        HolderGetter $$8 = $$0.lookupOrThrow(Registries.ENCHANTMENT);
        AdvancementHolder $$9 = Advancement.Builder.advancement().display(Blocks.HAY_BLOCK, (Component)Component.translatable("advancements.husbandry.root.title"), (Component)Component.translatable("advancements.husbandry.root.description"), Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry"), AdvancementType.TASK, false, false, false).addCriterion("consumed_item", ConsumeItemTrigger.TriggerInstance.usedItem()).save($$1, "husbandry/root");
        AdvancementHolder $$10 = Advancement.Builder.advancement().parent($$9).display(Items.WHEAT, (Component)Component.translatable("advancements.husbandry.plant_seed.title"), (Component)Component.translatable("advancements.husbandry.plant_seed.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("wheat", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.WHEAT)).addCriterion("pumpkin_stem", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PUMPKIN_STEM)).addCriterion("melon_stem", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.MELON_STEM)).addCriterion("beetroots", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.BEETROOTS)).addCriterion("nether_wart", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.NETHER_WART)).addCriterion("torchflower", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP)).addCriterion("pitcher_pod", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP)).save($$1, "husbandry/plant_seed");
        AdvancementHolder $$11 = Advancement.Builder.advancement().parent($$9).display(Items.WHEAT, (Component)Component.translatable("advancements.husbandry.breed_an_animal.title"), (Component)Component.translatable("advancements.husbandry.breed_an_animal.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("bred", BredAnimalsTrigger.TriggerInstance.bredAnimals()).save($$1, "husbandry/breed_an_animal");
        VanillaHusbandryAdvancements.createBreedAllAnimalsAdvancement($$11, $$1, $$2, BREEDABLE_ANIMALS.stream(), INDIRECTLY_BREEDABLE_ANIMALS.stream());
        VanillaHusbandryAdvancements.addFood(Advancement.Builder.advancement(), $$3).parent($$10).display(Items.APPLE, (Component)Component.translatable("advancements.husbandry.balanced_diet.title"), (Component)Component.translatable("advancements.husbandry.balanced_diet.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save($$1, "husbandry/balanced_diet");
        Advancement.Builder.advancement().parent($$10).display(Items.NETHERITE_HOE, (Component)Component.translatable("advancements.husbandry.netherite_hoe.title"), (Component)Component.translatable("advancements.husbandry.netherite_hoe.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).addCriterion("netherite_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.NETHERITE_HOE)).save($$1, "husbandry/obtain_netherite_hoe");
        AdvancementHolder $$12 = Advancement.Builder.advancement().parent($$9).display(Items.LEAD, (Component)Component.translatable("advancements.husbandry.tame_an_animal.title"), (Component)Component.translatable("advancements.husbandry.tame_an_animal.description"), null, AdvancementType.TASK, true, true, false).addCriterion("tamed_animal", TameAnimalTrigger.TriggerInstance.tamedAnimal()).save($$1, "husbandry/tame_an_animal");
        AdvancementHolder $$13 = VanillaHusbandryAdvancements.addFish(Advancement.Builder.advancement(), $$3).parent($$9).requirements(AdvancementRequirements.Strategy.OR).display(Items.FISHING_ROD, (Component)Component.translatable("advancements.husbandry.fishy_business.title"), (Component)Component.translatable("advancements.husbandry.fishy_business.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/fishy_business");
        AdvancementHolder $$14 = VanillaHusbandryAdvancements.addFishBuckets(Advancement.Builder.advancement(), $$3).parent($$13).requirements(AdvancementRequirements.Strategy.OR).display(Items.PUFFERFISH_BUCKET, (Component)Component.translatable("advancements.husbandry.tactical_fishing.title"), (Component)Component.translatable("advancements.husbandry.tactical_fishing.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/tactical_fishing");
        AdvancementHolder $$15 = Advancement.Builder.advancement().parent($$14).requirements(AdvancementRequirements.Strategy.OR).addCriterion(BuiltInRegistries.ITEM.getKey(Items.AXOLOTL_BUCKET).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.AXOLOTL_BUCKET))).display(Items.AXOLOTL_BUCKET, (Component)Component.translatable("advancements.husbandry.axolotl_in_a_bucket.title"), (Component)Component.translatable("advancements.husbandry.axolotl_in_a_bucket.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/axolotl_in_a_bucket");
        Advancement.Builder.advancement().parent($$15).addCriterion("kill_axolotl_target", EffectsChangedTrigger.TriggerInstance.gotEffectsFrom(EntityPredicate.Builder.entity().of($$2, EntityType.AXOLOTL))).display(Items.TROPICAL_FISH_BUCKET, (Component)Component.translatable("advancements.husbandry.kill_axolotl_target.title"), (Component)Component.translatable("advancements.husbandry.kill_axolotl_target.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/kill_axolotl_target");
        VanillaHusbandryAdvancements.addCatVariants(Advancement.Builder.advancement(), (HolderLookup<CatVariant>)$$6).parent($$12).display(Items.COD, (Component)Component.translatable("advancements.husbandry.complete_catalogue.title"), (Component)Component.translatable("advancements.husbandry.complete_catalogue.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).save($$1, "husbandry/complete_catalogue");
        VanillaHusbandryAdvancements.addTamedWolfVariants(Advancement.Builder.advancement(), (HolderLookup<WolfVariant>)$$7).parent($$12).display(Items.BONE, (Component)Component.translatable("advancements.husbandry.whole_pack.title"), (Component)Component.translatable("advancements.husbandry.whole_pack.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(50)).save($$1, "husbandry/whole_pack");
        AdvancementHolder $$16 = Advancement.Builder.advancement().parent($$9).addCriterion("safely_harvest_honey", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, BlockTags.BEEHIVES)).setSmokey(true), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.GLASS_BOTTLE))).display(Items.HONEY_BOTTLE, (Component)Component.translatable("advancements.husbandry.safely_harvest_honey.title"), (Component)Component.translatable("advancements.husbandry.safely_harvest_honey.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/safely_harvest_honey");
        AdvancementHolder $$17 = Advancement.Builder.advancement().parent($$16).display(Items.HONEYCOMB, (Component)Component.translatable("advancements.husbandry.wax_on.title"), (Component)Component.translatable("advancements.husbandry.wax_on.description"), null, AdvancementType.TASK, true, true, false).addCriterion("wax_on", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, HoneycombItem.WAXABLES.get().keySet())), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.HONEYCOMB))).save($$1, "husbandry/wax_on");
        Advancement.Builder.advancement().parent($$17).display(Items.STONE_AXE, (Component)Component.translatable("advancements.husbandry.wax_off.title"), (Component)Component.translatable("advancements.husbandry.wax_off.description"), null, AdvancementType.TASK, true, true, false).addCriterion("wax_off", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, HoneycombItem.WAX_OFF_BY_BLOCK.get().keySet())), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, WAX_SCRAPING_TOOLS))).save($$1, "husbandry/wax_off");
        AdvancementHolder $$18 = Advancement.Builder.advancement().parent($$9).addCriterion(BuiltInRegistries.ITEM.getKey(Items.TADPOLE_BUCKET).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.TADPOLE_BUCKET))).display(Items.TADPOLE_BUCKET, (Component)Component.translatable("advancements.husbandry.tadpole_in_a_bucket.title"), (Component)Component.translatable("advancements.husbandry.tadpole_in_a_bucket.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/tadpole_in_a_bucket");
        AdvancementHolder $$19 = VanillaHusbandryAdvancements.addLeashedFrogVariants($$2, $$3, (HolderLookup<FrogVariant>)$$5, Advancement.Builder.advancement()).parent($$18).display(Items.LEAD, (Component)Component.translatable("advancements.husbandry.leash_all_frog_variants.title"), (Component)Component.translatable("advancements.husbandry.leash_all_frog_variants.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/leash_all_frog_variants");
        Advancement.Builder.advancement().parent($$19).display(Items.VERDANT_FROGLIGHT, (Component)Component.translatable("advancements.husbandry.froglights.title"), (Component)Component.translatable("advancements.husbandry.froglights.description"), null, AdvancementType.CHALLENGE, true, true, false).addCriterion("froglights", InventoryChangeTrigger.TriggerInstance.hasItems(Items.OCHRE_FROGLIGHT, Items.PEARLESCENT_FROGLIGHT, Items.VERDANT_FROGLIGHT)).save($$1, "husbandry/froglights");
        Advancement.Builder.advancement().parent($$9).addCriterion("silk_touch_nest", BeeNestDestroyedTrigger.TriggerInstance.destroyedBeeNest(Blocks.BEE_NEST, ItemPredicate.Builder.item().withComponents(DataComponentMatchers.Builder.components().partial(DataComponentPredicates.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate($$8.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1))))).build()), MinMaxBounds.Ints.exactly(3))).display(Blocks.BEE_NEST, (Component)Component.translatable("advancements.husbandry.silk_touch_nest.title"), (Component)Component.translatable("advancements.husbandry.silk_touch_nest.description"), null, AdvancementType.TASK, true, true, false).save($$1, "husbandry/silk_touch_nest");
        Advancement.Builder.advancement().parent($$9).display(Items.OAK_BOAT, (Component)Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.title"), (Component)Component.translatable("advancements.husbandry.ride_a_boat_with_a_goat.description"), null, AdvancementType.TASK, true, true, false).addCriterion("ride_a_boat_with_a_goat", StartRidingTrigger.TriggerInstance.playerStartsRiding(EntityPredicate.Builder.entity().vehicle(EntityPredicate.Builder.entity().of($$2, EntityTypeTags.BOAT).passenger(EntityPredicate.Builder.entity().of($$2, EntityType.GOAT))))).save($$1, "husbandry/ride_a_boat_with_a_goat");
        Advancement.Builder.advancement().parent($$9).display(Items.GLOW_INK_SAC, (Component)Component.translatable("advancements.husbandry.make_a_sign_glow.title"), (Component)Component.translatable("advancements.husbandry.make_a_sign_glow.description"), null, AdvancementType.TASK, true, true, false).addCriterion("make_a_sign_glow", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, BlockTags.ALL_SIGNS)), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.GLOW_INK_SAC))).save($$1, "husbandry/make_a_sign_glow");
        AdvancementHolder $$20 = Advancement.Builder.advancement().parent($$9).display(Items.COOKIE, (Component)Component.translatable("advancements.husbandry.allay_deliver_item_to_player.title"), (Component)Component.translatable("advancements.husbandry.allay_deliver_item_to_player.description"), null, AdvancementType.TASK, true, true, true).addCriterion("allay_deliver_item_to_player", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$2, EntityType.ALLAY))))).save($$1, "husbandry/allay_deliver_item_to_player");
        Advancement.Builder.advancement().parent($$20).display(Items.NOTE_BLOCK, (Component)Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.title"), (Component)Component.translatable("advancements.husbandry.allay_deliver_cake_to_note_block.description"), null, AdvancementType.TASK, true, true, true).addCriterion("allay_deliver_cake_to_note_block", ItemUsedOnLocationTrigger.TriggerInstance.allayDropItemOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of((HolderGetter<Block>)$$4, Blocks.NOTE_BLOCK)), ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.CAKE))).save($$1, "husbandry/allay_deliver_cake_to_note_block");
        AdvancementHolder $$21 = Advancement.Builder.advancement().parent($$9).display(Items.SNIFFER_EGG, (Component)Component.translatable("advancements.husbandry.obtain_sniffer_egg.title"), (Component)Component.translatable("advancements.husbandry.obtain_sniffer_egg.description"), null, AdvancementType.TASK, true, true, true).addCriterion("obtain_sniffer_egg", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SNIFFER_EGG)).save($$1, "husbandry/obtain_sniffer_egg");
        AdvancementHolder $$22 = Advancement.Builder.advancement().parent($$21).display(Items.TORCHFLOWER_SEEDS, (Component)Component.translatable("advancements.husbandry.feed_snifflet.title"), (Component)Component.translatable("advancements.husbandry.feed_snifflet.description"), null, AdvancementType.TASK, true, true, true).addCriterion("feed_snifflet", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, ItemTags.SNIFFER_FOOD), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$2, EntityType.SNIFFER).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true)))))).save($$1, "husbandry/feed_snifflet");
        Advancement.Builder.advancement().parent($$22).display(Items.PITCHER_POD, (Component)Component.translatable("advancements.husbandry.plant_any_sniffer_seed.title"), (Component)Component.translatable("advancements.husbandry.plant_any_sniffer_seed.description"), null, AdvancementType.TASK, true, true, true).requirements(AdvancementRequirements.Strategy.OR).addCriterion("torchflower", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCHFLOWER_CROP)).addCriterion("pitcher_pod", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.PITCHER_CROP)).save($$1, "husbandry/plant_any_sniffer_seed");
        Advancement.Builder.advancement().parent($$12).display(Items.SHEARS, (Component)Component.translatable("advancements.husbandry.remove_wolf_armor.title"), (Component)Component.translatable("advancements.husbandry.remove_wolf_armor.description"), null, AdvancementType.TASK, true, true, false).addCriterion("remove_wolf_armor", PlayerInteractTrigger.TriggerInstance.equipmentSheared(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.WOLF_ARMOR), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$2, EntityType.WOLF))))).save($$1, "husbandry/remove_wolf_armor");
        Advancement.Builder.advancement().parent($$12).display(Items.WOLF_ARMOR, (Component)Component.translatable("advancements.husbandry.repair_wolf_armor.title"), (Component)Component.translatable("advancements.husbandry.repair_wolf_armor.description"), null, AdvancementType.TASK, true, true, false).addCriterion("repair_wolf_armor", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.ARMADILLO_SCUTE), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$2, EntityType.WOLF).equipment(EntityEquipmentPredicate.Builder.equipment().body(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$3, Items.WOLF_ARMOR).withComponents(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.DAMAGE, 0)).build()))))))).save($$1, "husbandry/repair_wolf_armor");
        Advancement.Builder.advancement().parent($$9).display(Items.DRIED_GHAST, (Component)Component.translatable("advancements.husbandry.place_dried_ghast_in_water.title"), (Component)Component.translatable("advancements.husbandry.place_dried_ghast_in_water.description"), null, AdvancementType.TASK, true, true, false).addCriterion("place_dried_ghast_in_water", ItemUsedOnLocationTrigger.TriggerInstance.placedBlockWithProperties(Blocks.DRIED_GHAST, BlockStateProperties.WATERLOGGED, true)).save($$1, "husbandry/place_dried_ghast_in_water");
    }

    public static AdvancementHolder createBreedAllAnimalsAdvancement(AdvancementHolder $$0, Consumer<AdvancementHolder> $$1, HolderGetter<EntityType<?>> $$2, Stream<EntityType<?>> $$3, Stream<EntityType<?>> $$4) {
        return VanillaHusbandryAdvancements.addBreedable(Advancement.Builder.advancement(), $$3, $$2, $$4).parent($$0).display(Items.GOLDEN_CARROT, (Component)Component.translatable("advancements.husbandry.breed_all_animals.title"), (Component)Component.translatable("advancements.husbandry.breed_all_animals.description"), null, AdvancementType.CHALLENGE, true, true, false).rewards(AdvancementRewards.Builder.experience(100)).save($$1, "husbandry/bred_all_animals");
    }

    private static Advancement.Builder addLeashedFrogVariants(HolderGetter<EntityType<?>> $$0, HolderGetter<Item> $$1, HolderLookup<FrogVariant> $$2, Advancement.Builder $$32) {
        VanillaHusbandryAdvancements.sortedVariants($$2).forEach($$3 -> $$32.addCriterion($$3.key().identifier().toString(), PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of($$1, Items.LEAD), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of($$0, EntityType.FROG).components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.FROG_VARIANT, $$3)).build()))))));
        return $$32;
    }

    private static <T> Stream<Holder.Reference<T>> sortedVariants(HolderLookup<T> $$0) {
        return $$0.listElements().sorted(HOLDER_KEY_COMPARATOR);
    }

    private static Advancement.Builder addFood(Advancement.Builder $$0, HolderGetter<Item> $$1) {
        for (Item $$2 : EDIBLE_ITEMS) {
            $$0.addCriterion(BuiltInRegistries.ITEM.getKey($$2).getPath(), ConsumeItemTrigger.TriggerInstance.usedItem($$1, $$2));
        }
        return $$0;
    }

    private static Advancement.Builder addBreedable(Advancement.Builder $$0, Stream<EntityType<?>> $$1, HolderGetter<EntityType<?>> $$22, Stream<EntityType<?>> $$3) {
        $$1.forEach($$2 -> $$0.addCriterion(EntityType.getKey($$2).toString(), BredAnimalsTrigger.TriggerInstance.bredAnimals(EntityPredicate.Builder.entity().of($$22, (EntityType<?>)$$2))));
        $$3.forEach($$2 -> $$0.addCriterion(EntityType.getKey($$2).toString(), BredAnimalsTrigger.TriggerInstance.bredAnimals(Optional.of(EntityPredicate.Builder.entity().of($$22, (EntityType<?>)$$2).build()), Optional.of(EntityPredicate.Builder.entity().of($$22, (EntityType<?>)$$2).build()), Optional.empty())));
        return $$0;
    }

    private static Advancement.Builder addFishBuckets(Advancement.Builder $$0, HolderGetter<Item> $$1) {
        for (Item $$2 : FISH_BUCKETS) {
            $$0.addCriterion(BuiltInRegistries.ITEM.getKey($$2).getPath(), FilledBucketTrigger.TriggerInstance.filledBucket(ItemPredicate.Builder.item().of($$1, $$2)));
        }
        return $$0;
    }

    private static Advancement.Builder addFish(Advancement.Builder $$0, HolderGetter<Item> $$1) {
        for (Item $$2 : FISH) {
            $$0.addCriterion(BuiltInRegistries.ITEM.getKey($$2).getPath(), FishingRodHookedTrigger.TriggerInstance.fishedItem(Optional.empty(), Optional.empty(), Optional.of(ItemPredicate.Builder.item().of($$1, $$2).build())));
        }
        return $$0;
    }

    private static Advancement.Builder addCatVariants(Advancement.Builder $$0, HolderLookup<CatVariant> $$12) {
        VanillaHusbandryAdvancements.sortedVariants($$12).forEach($$1 -> $$0.addCriterion($$1.key().identifier().toString(), TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.CAT_VARIANT, $$1)).build()))));
        return $$0;
    }

    private static Advancement.Builder addTamedWolfVariants(Advancement.Builder $$0, HolderLookup<WolfVariant> $$12) {
        VanillaHusbandryAdvancements.sortedVariants($$12).forEach($$1 -> $$0.addCriterion($$1.key().identifier().toString(), TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().components(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.expect(DataComponents.WOLF_VARIANT, $$1)).build()))));
        return $$0;
    }
}

