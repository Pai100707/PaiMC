package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class VanillaStoryAdvancements implements AdvancementSubProvider {
   @Override
   public void generate(Provider $$0, Consumer<AdvancementHolder> $$1) {
      HolderGetter<Item> $$2 = $$0.lookupOrThrow(Registries.ITEM);
      AdvancementHolder $$3 = Builder.advancement()
         .display(
            Blocks.GRASS_BLOCK,
            Component.translatable("advancements.story.root.title"),
            Component.translatable("advancements.story.root.description"),
            Identifier.withDefaultNamespace("gui/advancements/backgrounds/stone"),
            AdvancementType.TASK,
            false,
            false,
            false
         )
         .addCriterion("crafting_table", TriggerInstance.hasItems(new ItemLike[]{Blocks.CRAFTING_TABLE}))
         .save($$1, "story/root");
      AdvancementHolder $$4 = Builder.advancement()
         .parent($$3)
         .display(
            Items.WOODEN_PICKAXE,
            Component.translatable("advancements.story.mine_stone.title"),
            Component.translatable("advancements.story.mine_stone.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "get_stone",
            TriggerInstance.hasItems(
               new net.minecraft.advancements.criterion.ItemPredicate.Builder[]{
                  net.minecraft.advancements.criterion.ItemPredicate.Builder.item().of($$2, ItemTags.STONE_TOOL_MATERIALS)
               }
            )
         )
         .save($$1, "story/mine_stone");
      AdvancementHolder $$5 = Builder.advancement()
         .parent($$4)
         .display(
            Items.STONE_PICKAXE,
            Component.translatable("advancements.story.upgrade_tools.title"),
            Component.translatable("advancements.story.upgrade_tools.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("stone_pickaxe", TriggerInstance.hasItems(new ItemLike[]{Items.STONE_PICKAXE}))
         .save($$1, "story/upgrade_tools");
      AdvancementHolder $$6 = Builder.advancement()
         .parent($$5)
         .display(
            Items.IRON_INGOT,
            Component.translatable("advancements.story.smelt_iron.title"),
            Component.translatable("advancements.story.smelt_iron.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("iron", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_INGOT}))
         .save($$1, "story/smelt_iron");
      AdvancementHolder $$7 = Builder.advancement()
         .parent($$6)
         .display(
            Items.IRON_PICKAXE,
            Component.translatable("advancements.story.iron_tools.title"),
            Component.translatable("advancements.story.iron_tools.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("iron_pickaxe", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_PICKAXE}))
         .save($$1, "story/iron_tools");
      AdvancementHolder $$8 = Builder.advancement()
         .parent($$7)
         .display(
            Items.DIAMOND,
            Component.translatable("advancements.story.mine_diamond.title"),
            Component.translatable("advancements.story.mine_diamond.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("diamond", TriggerInstance.hasItems(new ItemLike[]{Items.DIAMOND}))
         .save($$1, "story/mine_diamond");
      AdvancementHolder $$9 = Builder.advancement()
         .parent($$6)
         .display(
            Items.LAVA_BUCKET,
            Component.translatable("advancements.story.lava_bucket.title"),
            Component.translatable("advancements.story.lava_bucket.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("lava_bucket", TriggerInstance.hasItems(new ItemLike[]{Items.LAVA_BUCKET}))
         .save($$1, "story/lava_bucket");
      AdvancementHolder $$10 = Builder.advancement()
         .parent($$6)
         .display(
            Items.IRON_CHESTPLATE,
            Component.translatable("advancements.story.obtain_armor.title"),
            Component.translatable("advancements.story.obtain_armor.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("iron_helmet", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_HELMET}))
         .addCriterion("iron_chestplate", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_CHESTPLATE}))
         .addCriterion("iron_leggings", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_LEGGINGS}))
         .addCriterion("iron_boots", TriggerInstance.hasItems(new ItemLike[]{Items.IRON_BOOTS}))
         .save($$1, "story/obtain_armor");
      Builder.advancement()
         .parent($$8)
         .display(
            Items.ENCHANTED_BOOK,
            Component.translatable("advancements.story.enchant_item.title"),
            Component.translatable("advancements.story.enchant_item.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("enchanted_item", net.minecraft.advancements.criterion.EnchantedItemTrigger.TriggerInstance.enchantedItem())
         .save($$1, "story/enchant_item");
      AdvancementHolder $$11 = Builder.advancement()
         .parent($$9)
         .display(
            Blocks.OBSIDIAN,
            Component.translatable("advancements.story.form_obsidian.title"),
            Component.translatable("advancements.story.form_obsidian.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("obsidian", TriggerInstance.hasItems(new ItemLike[]{Blocks.OBSIDIAN}))
         .save($$1, "story/form_obsidian");
      Builder.advancement()
         .parent($$10)
         .display(
            Items.SHIELD,
            Component.translatable("advancements.story.deflect_arrow.title"),
            Component.translatable("advancements.story.deflect_arrow.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "deflected_projectile",
            net.minecraft.advancements.criterion.EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(
               net.minecraft.advancements.criterion.DamagePredicate.Builder.damageInstance()
                  .type(net.minecraft.advancements.criterion.DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)))
                  .blocked(true)
            )
         )
         .save($$1, "story/deflect_arrow");
      Builder.advancement()
         .parent($$8)
         .display(
            Items.DIAMOND_CHESTPLATE,
            Component.translatable("advancements.story.shiny_gear.title"),
            Component.translatable("advancements.story.shiny_gear.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .requirements(Strategy.OR)
         .addCriterion("diamond_helmet", TriggerInstance.hasItems(new ItemLike[]{Items.DIAMOND_HELMET}))
         .addCriterion("diamond_chestplate", TriggerInstance.hasItems(new ItemLike[]{Items.DIAMOND_CHESTPLATE}))
         .addCriterion("diamond_leggings", TriggerInstance.hasItems(new ItemLike[]{Items.DIAMOND_LEGGINGS}))
         .addCriterion("diamond_boots", TriggerInstance.hasItems(new ItemLike[]{Items.DIAMOND_BOOTS}))
         .save($$1, "story/shiny_gear");
      AdvancementHolder $$12 = Builder.advancement()
         .parent($$11)
         .display(
            Items.FLINT_AND_STEEL,
            Component.translatable("advancements.story.enter_the_nether.title"),
            Component.translatable("advancements.story.enter_the_nether.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("entered_nether", net.minecraft.advancements.criterion.ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.NETHER))
         .save($$1, "story/enter_the_nether");
      Builder.advancement()
         .parent($$12)
         .display(
            Items.GOLDEN_APPLE,
            Component.translatable("advancements.story.cure_zombie_villager.title"),
            Component.translatable("advancements.story.cure_zombie_villager.description"),
            null,
            AdvancementType.GOAL,
            true,
            true,
            false
         )
         .addCriterion("cured_zombie", net.minecraft.advancements.criterion.CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager())
         .save($$1, "story/cure_zombie_villager");
      AdvancementHolder $$13 = Builder.advancement()
         .parent($$12)
         .display(
            Items.ENDER_EYE,
            Component.translatable("advancements.story.follow_ender_eye.title"),
            Component.translatable("advancements.story.follow_ender_eye.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion(
            "in_stronghold",
            net.minecraft.advancements.criterion.PlayerTrigger.TriggerInstance.located(
               net.minecraft.advancements.criterion.LocationPredicate.Builder.inStructure(
                  $$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD)
               )
            )
         )
         .save($$1, "story/follow_ender_eye");
      Builder.advancement()
         .parent($$13)
         .display(
            Blocks.END_STONE,
            Component.translatable("advancements.story.enter_the_end.title"),
            Component.translatable("advancements.story.enter_the_end.description"),
            null,
            AdvancementType.TASK,
            true,
            true,
            false
         )
         .addCriterion("entered_end", net.minecraft.advancements.criterion.ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END))
         .save($$1, "story/enter_the_end");
   }
}
