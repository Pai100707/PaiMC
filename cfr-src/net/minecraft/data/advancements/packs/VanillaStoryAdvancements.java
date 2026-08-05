/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.advancements.packs;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.ChangeDimensionTrigger;
import net.minecraft.advancements.criterion.CuredZombieVillagerTrigger;
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EnchantedItemTrigger;
import net.minecraft.advancements.criterion.EntityHurtPlayerTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class VanillaStoryAdvancements
implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.Provider $$0, Consumer<AdvancementHolder> $$1) {
        HolderGetter $$2 = $$0.lookupOrThrow(Registries.ITEM);
        AdvancementHolder $$3 = Advancement.Builder.advancement().display(Blocks.GRASS_BLOCK, (Component)Component.translatable("advancements.story.root.title"), (Component)Component.translatable("advancements.story.root.description"), Identifier.withDefaultNamespace("gui/advancements/backgrounds/stone"), AdvancementType.TASK, false, false, false).addCriterion("crafting_table", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.CRAFTING_TABLE)).save($$1, "story/root");
        AdvancementHolder $$4 = Advancement.Builder.advancement().parent($$3).display(Items.WOODEN_PICKAXE, (Component)Component.translatable("advancements.story.mine_stone.title"), (Component)Component.translatable("advancements.story.mine_stone.description"), null, AdvancementType.TASK, true, true, false).addCriterion("get_stone", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of((HolderGetter<Item>)$$2, ItemTags.STONE_TOOL_MATERIALS))).save($$1, "story/mine_stone");
        AdvancementHolder $$5 = Advancement.Builder.advancement().parent($$4).display(Items.STONE_PICKAXE, (Component)Component.translatable("advancements.story.upgrade_tools.title"), (Component)Component.translatable("advancements.story.upgrade_tools.description"), null, AdvancementType.TASK, true, true, false).addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE)).save($$1, "story/upgrade_tools");
        AdvancementHolder $$6 = Advancement.Builder.advancement().parent($$5).display(Items.IRON_INGOT, (Component)Component.translatable("advancements.story.smelt_iron.title"), (Component)Component.translatable("advancements.story.smelt_iron.description"), null, AdvancementType.TASK, true, true, false).addCriterion("iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT)).save($$1, "story/smelt_iron");
        AdvancementHolder $$7 = Advancement.Builder.advancement().parent($$6).display(Items.IRON_PICKAXE, (Component)Component.translatable("advancements.story.iron_tools.title"), (Component)Component.translatable("advancements.story.iron_tools.description"), null, AdvancementType.TASK, true, true, false).addCriterion("iron_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_PICKAXE)).save($$1, "story/iron_tools");
        AdvancementHolder $$8 = Advancement.Builder.advancement().parent($$7).display(Items.DIAMOND, (Component)Component.translatable("advancements.story.mine_diamond.title"), (Component)Component.translatable("advancements.story.mine_diamond.description"), null, AdvancementType.TASK, true, true, false).addCriterion("diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND)).save($$1, "story/mine_diamond");
        AdvancementHolder $$9 = Advancement.Builder.advancement().parent($$6).display(Items.LAVA_BUCKET, (Component)Component.translatable("advancements.story.lava_bucket.title"), (Component)Component.translatable("advancements.story.lava_bucket.description"), null, AdvancementType.TASK, true, true, false).addCriterion("lava_bucket", InventoryChangeTrigger.TriggerInstance.hasItems(Items.LAVA_BUCKET)).save($$1, "story/lava_bucket");
        AdvancementHolder $$10 = Advancement.Builder.advancement().parent($$6).display(Items.IRON_CHESTPLATE, (Component)Component.translatable("advancements.story.obtain_armor.title"), (Component)Component.translatable("advancements.story.obtain_armor.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("iron_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_HELMET)).addCriterion("iron_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_CHESTPLATE)).addCriterion("iron_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_LEGGINGS)).addCriterion("iron_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_BOOTS)).save($$1, "story/obtain_armor");
        Advancement.Builder.advancement().parent($$8).display(Items.ENCHANTED_BOOK, (Component)Component.translatable("advancements.story.enchant_item.title"), (Component)Component.translatable("advancements.story.enchant_item.description"), null, AdvancementType.TASK, true, true, false).addCriterion("enchanted_item", EnchantedItemTrigger.TriggerInstance.enchantedItem()).save($$1, "story/enchant_item");
        AdvancementHolder $$11 = Advancement.Builder.advancement().parent($$9).display(Blocks.OBSIDIAN, (Component)Component.translatable("advancements.story.form_obsidian.title"), (Component)Component.translatable("advancements.story.form_obsidian.description"), null, AdvancementType.TASK, true, true, false).addCriterion("obsidian", InventoryChangeTrigger.TriggerInstance.hasItems(Blocks.OBSIDIAN)).save($$1, "story/form_obsidian");
        Advancement.Builder.advancement().parent($$10).display(Items.SHIELD, (Component)Component.translatable("advancements.story.deflect_arrow.title"), (Component)Component.translatable("advancements.story.deflect_arrow.description"), null, AdvancementType.TASK, true, true, false).addCriterion("deflected_projectile", EntityHurtPlayerTrigger.TriggerInstance.entityHurtPlayer(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))).blocked(true))).save($$1, "story/deflect_arrow");
        Advancement.Builder.advancement().parent($$8).display(Items.DIAMOND_CHESTPLATE, (Component)Component.translatable("advancements.story.shiny_gear.title"), (Component)Component.translatable("advancements.story.shiny_gear.description"), null, AdvancementType.TASK, true, true, false).requirements(AdvancementRequirements.Strategy.OR).addCriterion("diamond_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_HELMET)).addCriterion("diamond_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_CHESTPLATE)).addCriterion("diamond_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_LEGGINGS)).addCriterion("diamond_boots", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_BOOTS)).save($$1, "story/shiny_gear");
        AdvancementHolder $$12 = Advancement.Builder.advancement().parent($$11).display(Items.FLINT_AND_STEEL, (Component)Component.translatable("advancements.story.enter_the_nether.title"), (Component)Component.translatable("advancements.story.enter_the_nether.description"), null, AdvancementType.TASK, true, true, false).addCriterion("entered_nether", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.NETHER)).save($$1, "story/enter_the_nether");
        Advancement.Builder.advancement().parent($$12).display(Items.GOLDEN_APPLE, (Component)Component.translatable("advancements.story.cure_zombie_villager.title"), (Component)Component.translatable("advancements.story.cure_zombie_villager.description"), null, AdvancementType.GOAL, true, true, false).addCriterion("cured_zombie", CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager()).save($$1, "story/cure_zombie_villager");
        AdvancementHolder $$13 = Advancement.Builder.advancement().parent($$12).display(Items.ENDER_EYE, (Component)Component.translatable("advancements.story.follow_ender_eye.title"), (Component)Component.translatable("advancements.story.follow_ender_eye.description"), null, AdvancementType.TASK, true, true, false).addCriterion("in_stronghold", PlayerTrigger.TriggerInstance.located(LocationPredicate.Builder.inStructure($$0.lookupOrThrow(Registries.STRUCTURE).getOrThrow(BuiltinStructures.STRONGHOLD)))).save($$1, "story/follow_ender_eye");
        Advancement.Builder.advancement().parent($$13).display(Blocks.END_STONE, (Component)Component.translatable("advancements.story.enter_the_end.title"), (Component)Component.translatable("advancements.story.enter_the_end.description"), null, AdvancementType.TASK, true, true, false).addCriterion("entered_end", ChangeDimensionTrigger.TriggerInstance.changedDimensionTo(Level.END)).save($$1, "story/enter_the_end");
    }
}

